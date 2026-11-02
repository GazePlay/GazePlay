"""Makes the sounds of Egg Game (gazeplay-games: data/egg/sounds), made for GazePlay Learning in November 2026 to
replace two sounds of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_egg_sounds.py <data/egg/sounds folder>
crack.wav: the shell cracking (a few short dry clicks of filtered noise).
hatch.wav: the egg opening (a crack), then little chirps going up and a bell chord.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
import random
import struct
import sys
import wave

RATE = 44100
PARTIALS = [(1.0, 1.0, 5.0), (2.0, 0.35, 8.0), (2.76, 0.2, 11.0), (5.4, 0.08, 16.0)]  # ratio, amplitude, decay


def write(path, samples, volume):
    peak = max(abs(v) for v in samples)
    with wave.open(path, 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b''.join(struct.pack('<h', int(32767 * volume * v / peak)) for v in samples))


def crack(seed, length=0.25):
    rnd = random.Random(seed)
    samples = [0.0] * int(RATE * length)
    for start in (0.0, 0.035, 0.06, 0.1):
        begin = int((start + rnd.uniform(0, 0.01)) * RATE)
        previous = 0.0
        for i in range(int(RATE * 0.03)):
            if begin + i >= len(samples):
                break
            t = i / RATE
            noise = rnd.uniform(-1, 1)
            # A little high-pass: dry, like a shell
            value = noise - previous * 0.6
            previous = noise
            samples[begin + i] += value * math.exp(-160 * t) * rnd.uniform(0.6, 1.0)
    return samples


def chirps():
    samples = [0.0] * int(RATE * 1.3)
    for k, start in enumerate((0.25, 0.42, 0.59)):
        base = 2200 + 300 * k
        begin = int(start * RATE)
        phase = 0.0
        for i in range(int(RATE * 0.12)):
            t = i / RATE
            frequency = base + 1800 * t / 0.12
            phase += 2 * math.pi * frequency / RATE
            samples[begin + i] += 0.5 * math.sin(phase) * math.sin(math.pi * t / 0.12)
    for frequency in (784.0, 987.8, 1174.7):
        begin = int(0.8 * RATE)
        for i in range(len(samples) - begin):
            u = i / RATE
            value = 0.0
            for ratio, amplitude, decay in PARTIALS:
                value += amplitude * math.exp(-decay * u) * math.sin(2 * math.pi * frequency * ratio * u)
            samples[begin + i] += 0.25 * min(1.0, u / 0.004) * value
    for i, value in enumerate(crack(7)):
        samples[i] += value
    return samples


folder = sys.argv[1]
os.makedirs(folder, exist_ok=True)
write(os.path.join(folder, 'crack.wav'), crack(3), 0.6)
write(os.path.join(folder, 'hatch.wav'), chirps(), 0.7)
print('ok')
