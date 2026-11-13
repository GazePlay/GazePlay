"""Makes the sounds of "Bouteille" (Bottle; gazeplay-games: data/bottle/sounds), made for GazePlay Learning on 13/11/2026
to replace a sound of unknown source (verre.wav). Same license as GazePlay Learning (GPL-3.0-or-later). The stall and
the bottles are drawn by the game; a wrong bottle and a series finished take the sounds of "Dés" and of the goose game.

python make_bottle_sounds.py <data/bottle folder>
glass.wav: a bottle breaking, a sharp crack then tinkling pieces of glass.
throw.wav: a ball thrown, a short soft whoosh.
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


def glass():
    rnd = random.Random(7)
    length = 0.9
    samples = [0.0] * int(RATE * length)
    # The crack: bright noise, very short
    high = 0.0
    previous = 0.0
    for i in range(int(RATE * 0.06)):
        t = i / RATE
        white = rnd.uniform(-1, 1)
        high = 0.6 * (high + white - previous)
        previous = white
        samples[i] += 1.2 * high * math.exp(-60 * t)
    # The pieces: small high rings at random moments, fading
    for _ in range(26):
        start = rnd.uniform(0.01, 0.6)
        frequency = rnd.uniform(2600, 6200)
        decay = rnd.uniform(25, 60)
        amplitude = rnd.uniform(0.15, 0.45) * math.exp(-2.5 * start)
        begin = int(start * RATE)
        for i in range(int(RATE * 0.25)):
            if begin + i >= len(samples):
                break
            t = i / RATE
            samples[begin + i] += amplitude * math.exp(-decay * t) * math.sin(2 * math.pi * frequency * t)
    return samples


def throw():
    rnd = random.Random(3)
    samples = []
    low = 0.0
    length = 0.3
    for i in range(int(RATE * length)):
        t = i / RATE
        u = t / length
        low += (0.05 + 0.25 * u) * (rnd.uniform(-1, 1) - low)
        samples.append(math.sin(math.pi * u) ** 2 * low)
    return samples


def main():
    folder = os.path.join(sys.argv[1], 'sounds')
    write(os.path.join(folder, 'glass.wav'), glass(), 0.7)
    write(os.path.join(folder, 'throw.wav'), throw(), 0.35)


main()
