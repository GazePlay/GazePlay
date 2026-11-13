"""Makes the sound of "Blocs" (Blocks; gazeplay-games: data/blocs/sounds), made for GazePlay Learning on 13/11/2026.
Same license as GazePlay Learning (GPL-3.0-or-later). The blocks are drawn by the game; a picture uncovered takes the
sound of the goose game.

python make_blocs_sounds.py <data/blocs folder>
pop.wav: a block that vanishes, a short light "pop" going up.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
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


def pop():
    samples = []
    phase = 0.0
    length = 0.12
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 600 + 900 * (t / length)
        phase += 2 * math.pi * frequency / RATE
        envelope = math.exp(-30 * t) * min(1.0, t / 0.002)
        samples.append(envelope * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


def main():
    write(os.path.join(sys.argv[1], 'sounds', 'pop.wav'), pop(), 0.45)


main()
