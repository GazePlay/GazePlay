"""Brings the recorded sounds of the games to the same loudness (20/11/2026, after a tester's feedback on the voice
much softer than the reward; measured with the filter ebur128 of FFmpeg: from -27.8 to -12.9 LUFS, peaks over 0
dBFS). The recordings of "Quel est ce son ?" (whereIsTheSound), of "Les sons de la vie" (soundsoflife), the frog of
"La course des grenouilles" and the applause of "Bravo" are set to -17 LUFS, the loudness of the voices of the games
(make_voices.py), their peaks at most -1.5 dBFS: the loudness measured by the filter loudnorm of FFmpeg, then a constant
gain, so that the sound keeps its shape, and a limiter on the few peaks it would bring too high. The sounds made by the
scripts of this folder are not changed (between -15 and -22 LUFS); nor the music (it has its own volume).

python normalize_sounds.py <ffmpeg> [<part of a path>]   (from the root of the repository)
Run it again on a sound replaced or added; a sound already at the level is left as it is.
"""
import glob
import json
import os
import re
import subprocess
import sys
import tempfile

DATA = 'gazeplay-games/src/main/resources/data'
CORE = 'gazeplay-core/src/main/resources/data'
LOUDNESS = -17.0
PEAK = -1.5
# A sound this close to the level (LU), its peaks under PEAK_KEPT (dBFS), is left as it is: each writing of an MP3 loses
# a little, and the encoding brings the peaks back a little over PEAK; a short sound with high peaks (the leopard) stays
# a little under the level, the limiter cannot bring it higher without crushing it
TOLERANCE = 3.0
PEAK_KEPT = -0.5

SOUNDS = (sorted(glob.glob(DATA + '/whereIsTheSound/sounds/*.mp3'))
          + sorted(glob.glob(DATA + '/soundsoflife/*/*.mp3'))
          + [DATA + '/race/sounds/frog.mp3', CORE + '/common/sounds/applause.mp3'])


def measure(ffmpeg, path):
    run = subprocess.run([ffmpeg, '-hide_banner', '-nostats', '-i', path, '-af',
                          'loudnorm=I=%s:TP=%s:LRA=20:print_format=json' % (LOUDNESS, PEAK), '-f', 'null', '-'],
                         capture_output=True, text=True, encoding='utf-8', errors='replace')
    return json.loads(run.stderr[run.stderr.rfind('{'):run.stderr.rfind('}') + 1])


def stream(ffmpeg, path):
    """The sample rate, the channels and the bit rate of a sound, kept when it is written again (at least 64 kb/s, so
    that a sound of 32 kb/s does not lose more)."""
    run = subprocess.run([ffmpeg, '-hide_banner', '-i', path], capture_output=True, text=True, encoding='utf-8',
                         errors='replace')
    audio = re.search(r'Audio: [^,]+, (\d+) Hz, (mono|stereo)[^,]*, \w+(?:, (\d+) kb/s)?', run.stderr)
    bitrate = max(64, int(audio[3])) if audio[3] else 128
    return audio[1], '1' if audio[2] == 'mono' else '2', '%dk' % bitrate


def normalize(ffmpeg, path):
    first = measure(ffmpeg, path)
    if abs(float(first['input_i']) - LOUDNESS) <= TOLERANCE and float(first['input_tp']) <= PEAK_KEPT:
        return False
    rate, channels, bitrate = stream(ffmpeg, path)
    with tempfile.TemporaryDirectory() as folder:
        out = os.path.join(folder, os.path.basename(path))
        # The gain measured, then a limiter (looking 5 ms ahead) on the few peaks it would bring over PEAK - 1.5 (the
        # MP3 encoding adds a little): loudnorm itself gives up the constant gain when the peaks would go over
        subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-y', '-i', path, '-af',
                        'volume=%.2fdB,alimiter=limit=%.3f:attack=5:release=50:level=false'
                        % (LOUDNESS - float(first['input_i']), 10 ** ((PEAK - 1.5) / 20)),
                        '-ar', rate, '-ac', channels, '-c:a', 'libmp3lame', '-b:a', bitrate, out], check=True)
        os.replace(out, path)
    return True


ffmpeg = sys.argv[1]
only = sys.argv[2] if len(sys.argv) > 2 else None
changed = 0
for sound in SOUNDS:
    if only is None or only in sound:
        changed += normalize(ffmpeg, sound)
print(changed, 'sons mis au niveau sur', len(SOUNDS))
