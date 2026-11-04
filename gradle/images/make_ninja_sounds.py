"""Makes the sounds of the game "Ninja" (gazeplay-games: data/ninja/sounds), made for GazePlay Learning in November 2026
to replace a sound of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_ninja_sounds.py <data/ninja folder>
sounds/slice1.wav to slice3.wav: a quick stroke of a blade (a breath of noise going up), then a small soft "pop", three
pitches. Mono, 16 bits, 44.1 kHz.
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


def slice_sound(pitch, seed):
    rnd = random.Random(seed)
    samples = []
    low = 0.0
    length = 0.34
    for i in range(int(RATE * length)):
        t = i / RATE
        # The breath: white noise through a low-pass filter opening quickly, then fading
        cutoff = 0.04 + 0.55 * min(1.0, t / 0.09)
        low += cutoff * (rnd.uniform(-1, 1) - low)
        breath = (rnd.uniform(-1, 1) - low) * math.exp(-((t - 0.07) / 0.05) ** 2)
        # The pop: a short note gliding down
        pop = 0.0
        if t > 0.08:
            u = t - 0.08
            pop = math.sin(2 * math.pi * pitch * (u - 0.6 * u * u)) * math.exp(-22 * u) * min(1.0, u / 0.002)
        samples.append(0.55 * breath + 0.6 * pop)
    return samples


folder = sys.argv[1]
for n, pitch in enumerate((520, 620, 740), start=1):
    write(os.path.join(folder, 'sounds', 'slice%d.wav' % n), slice_sound(pitch, n), 0.55)
print('ok')
