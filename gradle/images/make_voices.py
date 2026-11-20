"""
Makes the voices saying the questions of the games, in French and English (maintainer's decision of 18/11/2026: the
voices of before were of unknown source, or made by services whose terms were not checked; see INVENTAIRE_SONS.md).
Each sound file of the voices is made again, with the text the game shows, by Piper (https://github.com/OHF-Voice/
piper1-gpl, GPL-3.0-or-later), a speech synthesizer working offline, with four voices chosen by the maintainer, after
their licenses were read in their model cards (https://huggingface.co/rhasspy/piper-voices):

- French, woman ("w"): fr_FR-siwis-medium, SIWIS French Speech Synthesis Database (University of Edinburgh), CC BY 4.0,
  fine-tuned from hifi_tts_92 (Hi-Fi TTS, NVIDIA, CC BY 4.0);
- French, man ("m"): fr_FR-tom-medium, sentences read by its author (Tom Darboux), model under the AGPL 3.0;
- English, woman ("w"): en_US-kristin-medium, LibriVox recordings, public domain, trained from scratch;
- English, man ("m"): en_US-norman-medium, LibriVox recordings, public domain, trained from scratch.

The files made (games "Where is...?", "Find the odd one out" and "Identify the letters") are the ones in the
repository, made again in place; the list of the files, their texts and their voices is written to voix.csv (this
folder), read by inventaire_sons.py.

Setup (once, nothing of it is shipped): from the root of the repository,
    python -m venv build/piper/venv
    build/piper/venv/Scripts/python -m pip install piper-tts
    build/piper/venv/Scripts/python -m piper.download_voices --data-dir build/piper/voices fr_FR-siwis-medium
        fr_FR-tom-medium en_US-kristin-medium en_US-norman-medium
Then, from the root of the repository:
    build/piper/venv/Scripts/python gradle/images/make_voices.py <ffmpeg> [--only <part of a path>]
<ffmpeg>: an ffmpeg program, e.g. the one of the FFmpeg of JavaCV (org/bytedeco/ffmpeg/<platform>/ffmpeg.exe in the
jar of the platform, in the Gradle cache).
"""
import csv
import os
import re
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

import numpy as np
from piper import PiperVoice, SynthesisConfig

ROOT = Path.cwd()
DATA = ROOT / "gazeplay-games/src/main/resources/data"
VOICES_FOLDER = ROOT / "build/piper/voices"
HERE = Path(__file__).resolve().parent

VOICES = {
    ("fra", "w"): "fr_FR-siwis-medium",
    ("fra", "m"): "fr_FR-tom-medium",
    ("eng", "w"): "en_US-kristin-medium",
    ("eng", "m"): "en_US-norman-medium",
}

# Games whose questions are said by a voice: folder of the sounds, translation file of their texts
QUESTION_GAMES = {
    "whereIsTheAnimal": "sounds", "whereIsTheAnimalDynamic": "sounds", "whereIsTheColor": "sounds",
    "whereIsTheFlag": "sounds", "whereIsTheLetter": "sounds", "whereIsTheNumber": "sounds",
    "whereIsTheShape": "sounds", "findTheOddOneOut": "sound",
}

QUESTION_FILE = re.compile(r"^(?P<key>.+)\.(?P<voice>[mw])\.(?P<lang>[a-z]{3})\.mp3$")


def translations(path):
    """{key: {language: text}} of a translation file of GazePlay (comma separated, languages in the first line)."""
    with open(path, encoding="utf-8-sig") as file:
        lines = [line.rstrip("\r\n") for line in file]
    languages = lines[0].split(",")
    found = {}
    for line in lines[1:]:
        cells = line.split(",")
        if cells and cells[0]:
            found[cells[0]] = {languages[i]: cells[i].strip() for i in range(1, min(len(cells), len(languages)))}
    return found


def jobs():
    """(file, language, voice, text) of each file of the voices to make."""
    found = []
    for game, sounds in QUESTION_GAMES.items():
        texts = translations(DATA / game / (game + ".csv"))
        for language in ("fra", "eng"):
            for file in sorted((DATA / game / sounds / language).glob("*.mp3")):
                match = QUESTION_FILE.match(file.name)
                if not match:
                    raise SystemExit("Unexpected file name: %s" % file)
                text = texts.get(match["key"], {}).get(language)
                if not text:
                    raise SystemExit("No text for %s" % file)
                found.append((file, language, match["voice"], text))
    find = translations(ROOT / "gazeplay-core/src/main/resources/data/multilinguism/multilinguism.csv")["LettersFind"]
    for language in ("fra", "eng"):
        for gender, voice in (("f", "w"), ("m", "m")):
            folder = DATA / "literacy/sounds" / language / gender
            for file in sorted((folder / "quest").glob("*.mp3")):
                found.append((file, language, voice, "%s %s" % (find[language], file.stem)))
            for file in sorted((folder / "letter").glob("*.mp3")):
                found.append((file, language, voice, file.stem))
    return found


def synthesize(voice, text):
    chunks = list(voice.synthesize(text, SynthesisConfig()))
    return np.concatenate([chunk.audio_float_array for chunk in chunks]), chunks[0].sample_rate


VOICE_LEVEL = -16.0  # dBFS: the level of the voice, its silences left out (20/11/2026)
PEAK_LIMIT = -3.0  # dBFS: the highest peak out of the limiter (the MP3 encoding adds up to about 2 dB)


def level_gain(audio):
    """The same level for every voice (20/11/2026, tester's feedback: the voice much softer than the reward; measured:
    from -28 to -10 LUFS, peaks over 0 dBFS): the gain, in dB, bringing the mean level of the voice, its silences left
    out (the parts 40 dB under its peak), to VOICE_LEVEL; a limiter then keeps the peaks under PEAK_LIMIT (write_mp3).
    The filter loudnorm of FFmpeg was tried first: it does not bring the short voices (a letter, under 3 seconds) to the
    level of the others; a gain kept under the peak neither (a voice with a high peak stayed soft)."""
    peak = float(np.max(np.abs(audio))) if audio.size else 0
    if peak <= 0:
        return 0.0
    voiced = audio[np.abs(audio) > peak * 10 ** (-40 / 20)]
    rms = float(np.sqrt(np.mean(voiced ** 2)))
    return VOICE_LEVEL - 20 * np.log10(rms)


def write_mp3(ffmpeg, audio, rate, target):
    with tempfile.TemporaryDirectory() as folder:
        wav = Path(folder) / "voice.wav"
        # Written with room under 0 dBFS (nothing clipped), the gain given to FFmpeg, which works in floats
        peak = float(np.max(np.abs(audio))) if audio.size else 0
        room = 0.9 / peak if peak > 0 else 1
        gain = level_gain(audio) - 20 * np.log10(room)
        with wave.open(str(wav), "wb") as file:
            file.setnchannels(1)
            file.setsampwidth(2)
            file.setframerate(rate)
            file.writeframes((np.clip(audio * room, -1, 1) * 32767).astype(np.int16).tobytes())
        # The gain, then a limiter (looking 5 ms ahead) keeping the peaks under PEAK_LIMIT; mono, 64 kb/s: short
        # sentences, small files
        subprocess.run([ffmpeg, "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav), "-af",
                        "volume=%.2fdB,alimiter=limit=%.3f:attack=5:release=50:level=false"
                        % (gain, 10 ** (PEAK_LIMIT / 20)), "-ar", str(rate), "-ac", "1", "-c:a", "libmp3lame",
                        "-b:a", "64k", str(target)], check=True)


def main():
    if len(sys.argv) < 2:
        raise SystemExit(__doc__)
    ffmpeg = sys.argv[1]
    only = sys.argv[sys.argv.index("--only") + 1] if "--only" in sys.argv else None
    loaded = {key: PiperVoice.load(VOICES_FOLDER / (name + ".onnx")) for key, name in VOICES.items()}
    rows = []
    for file, language, voice, text in jobs():
        relative = file.relative_to(ROOT).as_posix()
        rows.append([relative, VOICES[(language, voice)], text])
        if only and only not in relative:
            continue
        audio, rate = synthesize(loaded[(language, voice)], text)
        write_mp3(ffmpeg, audio, rate, file)
    if not only:
        with open(HERE / "voix.csv", "w", newline="", encoding="utf-8") as out:
            writer = csv.writer(out)
            writer.writerow(["fichier", "voix", "texte"])
            writer.writerows(rows)
    print(len(rows), "voix" + (" (seulement %s)" % only if only else ""))


if __name__ == "__main__":
    main()
