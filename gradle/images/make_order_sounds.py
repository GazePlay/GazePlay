"""Makes the sounds of "Ordre" (Order; gazeplay-games: data/order/sounds), made for GazePlay Learning in November 2026
(the former game had no sound). Same license as GazePlay Learning (GPL-3.0-or-later).

python make_order_sounds.py <data/order folder>
sounds/appear.wav: a target appearing, a short soft "pop" going up.
sounds/right.wav: the right target, a small bell.
sounds/wrong.wav: a wrong target, a soft low "bonk".
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
        frequency = 380 + 900 * t / length
        phase += 2 * math.pi * frequency / RATE
        samples.append(math.sin(phase) * math.exp(-28 * t) * min(1.0, t / 0.003))
    return samples


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
write(os.path.join(folder, 'sounds', 'appear.wav'), pop(), 0.45)
write(os.path.join(folder, 'sounds', 'right.wav'), bell(988, 0.5), 0.45)
write(os.path.join(folder, 'sounds', 'wrong.wav'), bonk(), 0.45)
print('ok')
