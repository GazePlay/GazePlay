"""Makes the sounds of "Gobelets Pass Pass" (Cups and Balls, name code CupsBalls; gazeplay-games: data/cups/sounds),
made for GazePlay Learning in November 2026 (the former game had no sound). Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_cups_sounds.py <data/cups folder>
sounds/slide.wav: two cups exchanged, a short soft slide on the table.
sounds/found.wav: the ball found, three bell notes going up.
sounds/missed.wav: an empty cup, a soft low "bonk".
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


def mix(base, sound, at):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += v
    return base


def slide():
    rnd = random.Random(3)
    length = 0.3
    samples = []
    low = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        low += 0.08 * (rnd.uniform(-1, 1) - low)
        samples.append(low * math.sin(math.pi * t / length))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.35 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-7 * t)))
    return samples


def found():
    sound = []
    for k, frequency in enumerate((784, 988, 1175)):
        mix(sound, bell(frequency, 0.6 if k == 2 else 0.3), k * 0.11)
    return sound


def bonk():
    samples = []
    phase = 0.0
    length = 0.35
    for i in range(int(RATE * length)):
        t = i / RATE
        phase += 2 * math.pi * 200 * (1 - 0.3 * t / length) / RATE
        samples.append(math.exp(-8 * t) * min(1.0, t / 0.004) * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'slide.wav'), slide(), 0.35)
write(os.path.join(folder, 'sounds', 'found.wav'), found(), 0.45)
write(os.path.join(folder, 'sounds', 'missed.wav'), bonk(), 0.45)
print('ok')
