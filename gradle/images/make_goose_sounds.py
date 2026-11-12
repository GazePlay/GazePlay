"""Makes the sounds of the goose game "Jeu de l'oie" (name code GooseGame; gazeplay-games: data/goosegame/sounds), made
for GazePlay Learning on 12/11/2026 in place of six sounds of unknown source. Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_goose_sounds.py <data/goosegame folder>
sounds/mvmt0.wav to mvmt5.wav: a pawn hopping onto a square, a short wooden "tock", six slightly different pitches.
sounds/goose.wav: a goose square, a short friendly honk.
sounds/trap.wav: a trap square (inn, well, prison, back to the start), a soft falling "bonk".
sounds/win.wav: the end reached, four bell notes going up.
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


def tock(frequency, seed):
    rnd = random.Random(seed)
    samples = []
    low = 0.0
    for i in range(int(RATE * 0.12)):
        t = i / RATE
        low += 0.3 * (rnd.uniform(-1, 1) - low)
        envelope = math.exp(-45 * t) * min(1.0, t / 0.001)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t) + 0.5 * math.sin(2 * math.pi * frequency
                                                                                             * 1.5 * t) + 0.3 * low))
    return samples


def honk():
    samples = []
    phase = 0.0
    length = 0.32
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 330 + 60 * math.sin(math.pi * t / length)
        phase += 2 * math.pi * frequency / RATE
        envelope = math.sin(math.pi * t / length) ** 0.6
        # A nasal sound: odd harmonics
        samples.append(envelope * (math.sin(phase) + 0.5 * math.sin(3 * phase) + 0.25 * math.sin(5 * phase)))
    return samples


def bonk():
    samples = []
    phase = 0.0
    length = 0.45
    for i in range(int(RATE * length)):
        t = i / RATE
        phase += 2 * math.pi * 260 * (1 - 0.45 * t / length) / RATE
        samples.append(math.exp(-6 * t) * min(1.0, t / 0.004) * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.35 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-7 * t)))
    return samples


def win():
    sound = []
    for k, frequency in enumerate((523, 659, 784, 1047)):
        mix(sound, bell(frequency, 0.8 if k == 3 else 0.3), k * 0.12)
    return sound


folder = sys.argv[1]
for k, frequency in enumerate((700, 760, 820, 880, 940, 1000)):
    write(os.path.join(folder, 'sounds', 'mvmt%d.wav' % k), tock(frequency, k), 0.5)
write(os.path.join(folder, 'sounds', 'goose.wav'), honk(), 0.5)
write(os.path.join(folder, 'sounds', 'trap.wav'), bonk(), 0.55)
write(os.path.join(folder, 'sounds', 'win.wav'), win(), 0.6)
