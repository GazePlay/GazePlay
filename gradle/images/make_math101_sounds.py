"""Makes the sounds of "Apprends les Maths" (Math101; gazeplay-games: data/math101/sounds), made for GazePlay Learning in November 2026
(the former game had no sound). Same license as GazePlay Learning (GPL-3.0-or-later).

python make_math101_sounds.py <data/math101 folder>
sounds/right.wav: the right answer, two small bells going up.
sounds/wrong.wav: a wrong answer, a soft low "bonk".
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


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-6 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.35 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-8 * t)))
    return samples


def bonk():
    samples = []
    phase = 0.0
    length = 0.35
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 220 * (1 - 0.3 * t / length)
        phase += 2 * math.pi * frequency / RATE
        samples.append(math.exp(-8 * t) * min(1.0, t / 0.004) * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


folder = sys.argv[1]
two = bell(784, 0.5)
second = bell(1175, 0.6)
shift = int(RATE * 0.12)
right = [0.0] * (shift + len(second))
for i, v in enumerate(two):
    right[i] += v
for i, v in enumerate(second):
    right[shift + i] += v
write(os.path.join(folder, 'sounds', 'right.wav'), right, 0.45)
write(os.path.join(folder, 'sounds', 'wrong.wav'), bonk(), 0.45)
print('ok')
