"""Makes the sounds of "Aiguillages" (Train Switches; gazeplay-games: data/trainSwitches/sounds), made for GazePlay
Learning in November 2026 to replace sounds of unknown source (train.mp3, whistle.mp3, click.mp3, correct.mp3,
wrong.mp3). Same license as GazePlay Learning (GPL-3.0-or-later).

python make_trainswitches_sounds.py <data/trainSwitches folder>
sounds/whistle.wav: a train leaving the tunnel, two puffs of steam then a soft whistle of two notes.
sounds/click.wav: a switch changing its way, a short wooden "clack".
sounds/correct.wav: a train at the right station, a small bell of two notes going up.
sounds/wrong.wav: a train at a wrong station, a soft "bonk" of two notes going down.
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


def silence(seconds):
    return [0.0] * int(RATE * seconds)


def mix(base, sound, at):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += v
    return base


def puff(seed, length=0.16):
    """A puff of steam: noise, low-passed, quickly fading."""
    rnd = random.Random(seed)
    samples = []
    low = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        low += 0.12 * (rnd.uniform(-1, 1) - low)
        samples.append(low * math.exp(-18 * t) * min(1.0, t / 0.004))
    return samples


def whistle_note(frequency, length):
    """A soft whistle: two near tones, a breath of noise, a gentle start and end."""
    rnd = random.Random(int(frequency))
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = min(1.0, t / 0.05) * min(1.0, (length - t) / 0.08)
        vibrato = 1 + 0.004 * math.sin(2 * math.pi * 5.5 * t)
        tone = (math.sin(2 * math.pi * frequency * vibrato * t) + 0.6 * math.sin(2 * math.pi * frequency * 1.25 * t)
                + 0.25 * math.sin(2 * math.pi * frequency * 2 * t))
        samples.append(envelope * (tone + 0.08 * rnd.uniform(-1, 1)))
    return samples


def whistle():
    sound = silence(0.0)
    mix(sound, puff(1), 0.0)
    mix(sound, puff(2), 0.22)
    mix(sound, [0.35 * v for v in whistle_note(620, 0.32)], 0.42)
    mix(sound, [0.35 * v for v in whistle_note(540, 0.42)], 0.74)
    return sound


def click():
    rnd = random.Random(5)
    samples = []
    for i in range(int(RATE * 0.09)):
        t = i / RATE
        knock = math.sin(2 * math.pi * 900 * t) * math.exp(-70 * t)
        wood = math.sin(2 * math.pi * 1700 * t) * math.exp(-110 * t)
        samples.append(0.7 * knock + 0.4 * wood + 0.3 * rnd.uniform(-1, 1) * math.exp(-300 * t))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t) + 0.4 * math.sin(2 * math.pi * frequency * 2.76 * t)
                                   * math.exp(-6 * t) + 0.2 * math.sin(2 * math.pi * frequency * 5.4 * t)
                                   * math.exp(-10 * t)))
    return samples


def correct():
    sound = silence(0.0)
    mix(sound, bell(880, 0.5), 0.0)
    mix(sound, bell(1175, 0.7), 0.12)
    return sound


def bonk(frequency, length):
    samples = []
    phase = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        f = frequency * (1 - 0.25 * t / length)
        phase += 2 * math.pi * f / RATE
        envelope = math.exp(-7 * t) * min(1.0, t / 0.004)
        samples.append(envelope * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


def wrong():
    sound = silence(0.0)
    mix(sound, bonk(330, 0.25), 0.0)
    mix(sound, bonk(247, 0.4), 0.2)
    return sound


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'whistle.wav'), whistle(), 0.5)
write(os.path.join(folder, 'sounds', 'click.wav'), click(), 0.5)
write(os.path.join(folder, 'sounds', 'correct.wav'), correct(), 0.5)
write(os.path.join(folder, 'sounds', 'wrong.wav'), wrong(), 0.45)
print('ok')
