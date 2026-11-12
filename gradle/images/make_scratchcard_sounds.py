"""Makes the sounds of "Cartes à gratter" (ScratchCard; gazeplay-games: data/scratchcard/sounds), made for GazePlay
Learning on 12/11/2026. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_scratchcard_sounds.py <data/scratchcard folder>
scratch.wav: a short scratch of a coin on a card, about 0.22 s (played again while the gaze scratches).
reveal.wav: the picture uncovered, a sparkle of bell notes going up.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
import random
import struct
import sys
import wave

RATE = 44100


def write(path, samples, volume):
    peak = max(abs(v) for v in samples)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with wave.open(path, 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b''.join(struct.pack('<h', int(32767 * volume * v / peak)) for v in samples))


def mix(base, sound, at, gain=1.0):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += gain * v
    return base


def scratch():
    """A band of noise (no low rumble, no hiss), in small rough strokes, softly in and out."""
    rnd = random.Random(7)
    length = 0.22
    samples = []
    low = 0.0
    previous = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        noise = rnd.uniform(-1, 1)
        low += 0.45 * (noise - low)
        band = low - previous
        previous = low
        roughness = 0.55 + 0.45 * abs(math.sin(2 * math.pi * 38 * t + 1.3 * math.sin(2 * math.pi * 7 * t)))
        envelope = min(1.0, t / 0.02) * min(1.0, (length - t) / 0.06)
        samples.append(envelope * roughness * band)
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-6 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.3 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-8 * t)))
    return samples


def reveal():
    sound = []
    for k, frequency in enumerate((784, 988, 1175, 1568, 1976)):
        mix(sound, bell(frequency, 0.9), 0.07 * k, 1.0 - 0.1 * k)
    return sound


def main():
    folder = os.path.join(sys.argv[1], 'sounds')
    write(os.path.join(folder, 'scratch.wav'), scratch(), 0.5)
    write(os.path.join(folder, 'reveal.wav'), reveal(), 0.6)


if __name__ == '__main__':
    main()
