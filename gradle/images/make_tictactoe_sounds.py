"""Makes the sounds of the Morpion (Tic-tac-toe, name code TicTacToe; gazeplay-games: data/noughtsandcrosses/sounds),
made for GazePlay Learning in November 2026 (the former game had no sound). Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_tictactoe_sounds.py <data/noughtsandcrosses folder>
sounds/place.wav: a mark drawn, a short soft felt-pen stroke.
sounds/win.wav: the player wins a game, three bell notes going up.
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


def stroke():
    """Noise through a band filter, swelling then fading: a felt pen on paper."""
    rnd = random.Random(7)
    length = 0.28
    samples = []
    low = high = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        noise = rnd.uniform(-1, 1)
        low += 0.35 * (noise - low)
        high += 0.05 * (low - high)
        band = low - high
        envelope = math.sin(math.pi * t / length) ** 1.5
        samples.append(band * envelope * (1 + 0.3 * math.sin(2 * math.pi * 18 * t)))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.4 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-6 * t)))
    return samples


def soft(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.006)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t) + 0.25 * math.sin(4 * math.pi * frequency * t)))
    return samples


def win():
    sound = []
    for k, frequency in enumerate((660, 880, 1046)):
        mix(sound, bell(frequency, 0.7 if k == 2 else 0.35), k * 0.12)
    return sound


def lose():
    sound = []
    mix(sound, soft(440, 0.35), 0)
    mix(sound, soft(349, 0.6), 0.22)
    return sound


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'place.wav'), stroke(), 0.45)
write(os.path.join(folder, 'sounds', 'win.wav'), win(), 0.45)
write(os.path.join(folder, 'sounds', 'lose.wav'), lose(), 0.4)
print('ok')
