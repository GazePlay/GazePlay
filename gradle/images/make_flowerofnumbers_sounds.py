"""Makes the sounds of Flower Of Numbers (gazeplay-games: data/flowerOfNumbers/sounds), made for GazePlay Learning in
November 2026 (the game had no sound). Same license as GazePlay Learning (GPL-3.0-or-later).

python make_flowerofnumbers_sounds.py <data/flowerOfNumbers/sounds folder>
put.wav: a short soft "pop" when a value lands in a petal (a sine gliding down, 0.15 s).
petal.wav: three bell notes going up (C6, E6, G6) when a petal makes the number.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
import struct
import sys
import wave

RATE = 44100
PARTIALS = [(1.0, 1.0, 5.0), (2.0, 0.35, 8.0), (2.76, 0.2, 11.0), (5.4, 0.08, 16.0)]  # ratio, amplitude, decay


def write(path, samples, volume):
    peak = max(abs(v) for v in samples)
    with wave.open(path, 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b''.join(struct.pack('<h', int(32767 * volume * v / peak)) for v in samples))


def put():
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.15)):
        t = i / RATE
        frequency = 900 - 2400 * t
        phase += 2 * math.pi * frequency / RATE
        samples.append(min(1.0, t / 0.003) * math.exp(-28 * t) * math.sin(phase))
    return samples


def petal():
    notes = [(0.0, 1046.5), (0.09, 1318.5), (0.18, 1568.0)]
    samples = []
    for i in range(int(RATE * 1.0)):
        t = i / RATE
        value = 0.0
        for start, frequency in notes:
            if t >= start:
                u = t - start
                attack = min(1.0, u / 0.004)
                for ratio, amplitude, decay in PARTIALS:
                    value += attack * amplitude * math.exp(-decay * u) * math.sin(2 * math.pi * frequency * ratio * u)
        samples.append(value)
    return samples


folder = sys.argv[1]
os.makedirs(folder, exist_ok=True)
write(os.path.join(folder, 'put.wav'), put(), 0.6)
write(os.path.join(folder, 'petal.wav'), petal(), 0.7)
print('ok')
