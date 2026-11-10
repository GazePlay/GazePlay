"""Makes the sounds of "Quatre en ligne" (Four in a Row, name code Connect4; gazeplay-games: data/connect4/sounds), made
for GazePlay Learning in November 2026 to replace a sound of unknown source (tokenFalling.wav). Same license as GazePlay
Learning (GPL-3.0-or-later).

python make_connect4_sounds.py <data/connect4 folder>
sounds/drop.wav: a plastic token landing in the grid, a short hollow "clack".
sounds/win.wav: the player wins a game, three notes going up.
sounds/lose.wav: the computer wins a game, two soft notes going down.
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


def clack():
    rnd = random.Random(4)
    samples = []
    for i in range(int(RATE * 0.16)):
        t = i / RATE
        body = math.sin(2 * math.pi * 520 * t) * math.exp(-38 * t)
        hollow = math.sin(2 * math.pi * 1240 * t) * math.exp(-60 * t)
        click = rnd.uniform(-1, 1) * math.exp(-400 * t)
        samples.append(0.7 * body + 0.35 * hollow + 0.4 * click)
    return samples


def note(frequency, length, decay):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-decay * t) * min(1.0, t / 0.004)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t) + 0.35 * math.sin(2 * math.pi * frequency * 2 * t)
                                   + 0.15 * math.sin(2 * math.pi * frequency * 3 * t)))
    return samples


def win():
    sound = []
    for k, frequency in enumerate((523, 659, 784)):
        mix(sound, note(frequency, 0.6 if k == 2 else 0.3, 5), k * 0.13)
    return sound


def lose():
    sound = []
    mix(sound, note(392, 0.35, 6), 0)
    mix(sound, note(311, 0.6, 4), 0.22)
    return sound


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'drop.wav'), clack(), 0.55)
write(os.path.join(folder, 'sounds', 'win.wav'), win(), 0.45)
write(os.path.join(folder, 'sounds', 'lose.wav'), lose(), 0.4)
print('ok')
