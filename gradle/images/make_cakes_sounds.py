"""Makes the sounds of "La Fabrique à gâteaux" (Cakes; gazeplay-games: data/cake/sounds), made for GazePlay Learning on
12/11/2026 to replace three sounds of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later). The
cake is drawn by the game; a wrong choice and a cake finished take the sounds of "Dés" and of the goose game.

python make_cakes_sounds.py <data/cake folder>
plop.wav: a layer, a cake or sweets put down, a soft round "plop".
cream.wav: the cream poured, a short soft squirt.
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


def plop():
    samples = []
    phase = 0.0
    length = 0.22
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 520 * math.exp(-14 * t) + 160
        phase += 2 * math.pi * frequency / RATE
        envelope = math.exp(-16 * t) * min(1.0, t / 0.002)
        samples.append(envelope * math.sin(phase))
    return samples


def cream():
    rnd = random.Random(4)
    samples = []
    low = 0.0
    length = 0.5
    for i in range(int(RATE * length)):
        t = i / RATE
        u = t / length
        low += 0.12 * (rnd.uniform(-1, 1) - low)
        bubble = math.sin(2 * math.pi * (180 + 60 * math.sin(2 * math.pi * 9 * t)) * t)
        envelope = math.sin(math.pi * u) ** 1.2
        samples.append(envelope * (1.5 * low + 0.25 * bubble))
    return samples


def main():
    folder = os.path.join(sys.argv[1], 'sounds')
    write(os.path.join(folder, 'plop.wav'), plop(), 0.6)
    write(os.path.join(folder, 'cream.wav'), cream(), 0.5)


if __name__ == '__main__':
    main()
