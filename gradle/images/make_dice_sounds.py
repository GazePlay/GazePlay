"""Makes the sound of a die rolled (gazeplay-core: data/common/sounds/diceRollSound.wav), played by the 3D die of
"Dés" (Dice), the horses and the goose game (DiceRoller), made for GazePlay Learning on 12/11/2026 in place of a sound
of unknown source; and the sounds of "Dés" (gazeplay-games: data/dice/sounds). Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_dice_sounds.py <data/common/sounds folder> <data/dice folder>
diceRollSound.wav: a die rattling on a felt table, about 1.2 s (the die turns for 1.5 s).
dice/sounds/right.wav: the right total found, three bell notes going up.
dice/sounds/wrong.wav: a wrong total, a soft low "bonk".
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


def mix(base, sound, at, gain=1.0):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += gain * v
    return base


def knock(rnd, frequency, length):
    """A short knock of the die on the table: a little noise and a damped tone."""
    samples = []
    low = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        low += 0.35 * (rnd.uniform(-1, 1) - low)
        envelope = math.exp(-60 * t) * min(1.0, t / 0.0008)
        samples.append(envelope * (0.6 * low + math.sin(2 * math.pi * frequency * t)
                                   + 0.4 * math.sin(2 * math.pi * frequency * 2.3 * t)))
    return samples


def roll():
    rnd = random.Random(11)
    sound = [0.0] * int(RATE * 1.3)
    at = 0.02
    gap = 0.05
    gain = 1.0
    while at < 1.15:
        mix(sound, knock(rnd, rnd.uniform(900, 1500), 0.06), at, gain * rnd.uniform(0.6, 1.0))
        at += gap * rnd.uniform(0.7, 1.3)
        gap *= 1.09
        gain *= 0.93
    return sound


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.35 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-7 * t)))
    return samples


def right():
    sound = []
    for k, frequency in enumerate((659, 784, 1047)):
        mix(sound, bell(frequency, 0.6 if k == 2 else 0.3), k * 0.11)
    return sound


def wrong():
    samples = []
    phase = 0.0
    length = 0.35
    for i in range(int(RATE * length)):
        t = i / RATE
        phase += 2 * math.pi * 190 * (1 - 0.3 * t / length) / RATE
        samples.append(math.exp(-8 * t) * min(1.0, t / 0.004) * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


common, dice = sys.argv[1], sys.argv[2]
write(os.path.join(common, 'diceRollSound.wav'), roll(), 0.7)
write(os.path.join(dice, 'sounds', 'right.wav'), right(), 0.6)
write(os.path.join(dice, 'sounds', 'wrong.wav'), wrong(), 0.6)
