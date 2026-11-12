"""Makes the sound of a card turned over in "Cartes magiques" (MagicCards; gazeplay-games:
data/magiccards/sounds/flip.wav), made for GazePlay Learning on 12/11/2026. Same license as GazePlay Learning
(GPL-3.0-or-later). The other sounds of the game are those of the cups (a card sliding), of "Dés" (a wrong card) and
of the goose game (the picture found).

python make_magiccards_sounds.py <data/magiccards folder>
flip.wav: a short "fwip" of paper, about 0.16 s.
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


def flip():
    """Noise whose color rises then falls (a paper swept through the air), with a soft tap at the end."""
    rnd = random.Random(5)
    length = 0.16
    samples = []
    low = 0.0
    previous = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        u = t / length
        brightness = 0.15 + 0.6 * math.sin(math.pi * u)
        low += brightness * (rnd.uniform(-1, 1) - low)
        band = low - 0.6 * previous
        previous = low
        envelope = math.sin(math.pi * min(1.0, u * 1.1)) ** 1.5
        tap = 0.0
        if u > 0.8:
            tt = t - 0.8 * length
            tap = 0.6 * math.exp(-90 * tt) * math.sin(2 * math.pi * 320 * tt)
        samples.append(envelope * band + tap)
    return samples


def main():
    write(os.path.join(sys.argv[1], 'sounds', 'flip.wav'), flip(), 0.5)


if __name__ == '__main__':
    main()
