"""Makes the sounds of the moles game (gazeplay-games: data/whackmole/sounds), made for GazePlay Learning in November 2026
to replace sounds of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_moles_sounds.py <data/whackmole folder>
sounds/hit1.wav, hit2.wav: a soft, funny "boing" (a round note gliding up, with a small knock), two pitches.
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


def boing(start, seed):
    rnd = random.Random(seed)
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.32)):
        t = i / RATE
        frequency = start * (1 + 0.9 * (1 - math.exp(-16 * t))) * (1 + 0.03 * math.sin(2 * math.pi * 26 * t))
        phase += 2 * math.pi * frequency / RATE
        note = math.sin(phase) * math.exp(-9 * t) * min(1.0, t / 0.003)
        knock = rnd.uniform(-1, 1) * math.exp(-260 * t)
        samples.append(0.8 * note + 0.35 * knock)
    return samples


folder = sys.argv[1]
for n, start in enumerate((230, 280), start=1):
    write(os.path.join(folder, 'sounds', 'hit%d.wav' % n), boing(start, n), 0.55)
print('ok')
