"""Makes the sounds of "Magic Potions" (name code Potions; gazeplay-games: data/potions/sounds), made for GazePlay
Learning in November 2026 (the former game played the explosion sound shared by other games). Same license as GazePlay
Learning (GPL-3.0-or-later).

python make_potions_sounds.py <data/potions folder>
sounds/pour.wav: a potion poured, a few bubbling "glugs" going up.
sounds/served.wav: the client served, a magic sparkle of bell notes going up.
sounds/boom.wav: the cauldron exploding, a soft rumbling "boom".
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


def glug(frequency):
    samples = []
    phase = 0.0
    length = 0.09
    for i in range(int(RATE * length)):
        t = i / RATE
        phase += 2 * math.pi * frequency * (1 + 1.5 * t / length) / RATE
        samples.append(math.sin(phase) * math.sin(math.pi * t / length))
    return samples


def pour():
    sound = []
    for k, frequency in enumerate((260, 300, 280, 340, 320)):
        mix(sound, glug(frequency), k * 0.08)
    return sound


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-6 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.3 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-8 * t)))
    return samples


def served():
    sound = []
    for k, frequency in enumerate((784, 988, 1175, 1568)):
        mix(sound, bell(frequency, 0.6 if k == 3 else 0.3), k * 0.08)
    return sound


def boom():
    rnd = random.Random(9)
    samples = []
    low = 0.0
    length = 0.8
    for i in range(int(RATE * length)):
        t = i / RATE
        low += 0.03 * (rnd.uniform(-1, 1) - low)
        thump = math.sin(2 * math.pi * 60 * (1 - 0.4 * t) * t) * math.exp(-6 * t)
        samples.append((low * 6 + thump) * math.exp(-4 * t) * min(1.0, t / 0.005))
    return samples


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'pour.wav'), pour(), 0.4)
write(os.path.join(folder, 'sounds', 'served.wav'), served(), 0.45)
write(os.path.join(folder, 'sounds', 'boom.wav'), boom(), 0.5)
print('ok')
