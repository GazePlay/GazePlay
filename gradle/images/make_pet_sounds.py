"""Makes the sounds of "J'ai un Biboule !" (name code Pet; gazeplay-games: data/pet/sounds), made for GazePlay Learning
on 27/11/2026 (tester's feedback, point 80: sounds supporting the actions that work and fill a gauge). Same license as
GazePlay Learning (GPL-3.0-or-later).

python make_pet_sounds.py <data/pet folder>
sounds/stroke.wav: the Biboule stroked, a happy little chirp going up and down.
sounds/munch.wav: a bite, two soft crunches.
sounds/splash.wav: the water of the shower, a short soft splash.
sounds/boing.wav: a toy touching the Biboule, a springy "boing".
sounds/gain.wav: a quarter of a gauge filled, three bell notes going up.
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


def chirp(start, top, length):
    """A sweep of pitch up then down, as a little bird."""
    samples = []
    phase = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        u = t / length
        frequency = start + (top - start) * math.sin(math.pi * u)
        phase += 2 * math.pi * frequency / RATE
        envelope = math.sin(math.pi * u) ** 0.7
        samples.append(envelope * (math.sin(phase) + 0.25 * math.sin(2 * phase)))
    return samples


def stroke():
    sound = []
    mix(sound, chirp(900, 1500, 0.14), 0)
    mix(sound, chirp(1000, 1800, 0.18), 0.16)
    return sound


def crunch(seed):
    rnd = random.Random(seed)
    samples = []
    low = 0.0
    length = 0.11
    for i in range(int(RATE * length)):
        t = i / RATE
        low += (rnd.uniform(-1, 1) - low) * 0.35
        envelope = math.exp(-28 * t) * min(1.0, t / 0.002)
        samples.append(envelope * low)
    return samples


def munch():
    sound = []
    mix(sound, crunch(3), 0)
    mix(sound, crunch(5), 0.16)
    return sound


def splash():
    rnd = random.Random(11)
    samples = []
    low = 0.0
    length = 0.45
    for i in range(int(RATE * length)):
        t = i / RATE
        # Noise, smoother as it fades (the water falling)
        low += (rnd.uniform(-1, 1) - low) * (0.6 - 0.4 * t / length)
        envelope = math.sin(math.pi * min(1.0, t / 0.05) / 2) * math.exp(-5 * t)
        samples.append(envelope * low)
    return samples


def boing():
    samples = []
    phase = 0.0
    length = 0.5
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 180 + 260 * math.exp(-9 * t) + 25 * math.sin(2 * math.pi * 14 * t) * math.exp(-4 * t)
        phase += 2 * math.pi * frequency / RATE
        envelope = math.exp(-5 * t) * min(1.0, t / 0.004)
        samples.append(envelope * math.sin(phase))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-6 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.3 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-8 * t)))
    return samples


def gain():
    sound = []
    for k, frequency in enumerate((659, 784, 1047)):
        mix(sound, bell(frequency, 0.5 if k == 2 else 0.25), k * 0.1)
    return sound


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'stroke.wav'), stroke(), 0.5)
write(os.path.join(folder, 'sounds', 'munch.wav'), munch(), 0.55)
write(os.path.join(folder, 'sounds', 'splash.wav'), splash(), 0.35)
write(os.path.join(folder, 'sounds', 'boing.wav'), boing(), 0.5)
write(os.path.join(folder, 'sounds', 'gain.wav'), gain(), 0.45)
print('ok')
