"""Makes the sounds of Sprint Finish (gazeplay-games: data/sprintFinish/sounds), made for GazePlay Learning in November
2026 (the game had no sound). Same license as GazePlay Learning (GPL-3.0-or-later).

python make_sprintfinish_sounds.py <data/sprintFinish/sounds folder>
tick.wav: a short wooden "tock" for each number of the countdown.
door.wav: a soft "clack" when a button opens its fences.
caught.wav: a "boing" going down when a dog (or a log) catches the cat.
dish.wav: four bell notes going up when the cat reaches its bowl.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
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


def knock(frequency, length, decay):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        attack = min(1.0, t / 0.002)
        value = math.sin(2 * math.pi * frequency * t) + 0.4 * math.sin(2 * math.pi * frequency * 2.3 * t)
        samples.append(attack * math.exp(-decay * t) * value)
    return samples


def caught():
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.55)):
        t = i / RATE
        frequency = 420 - 520 * t + 30 * math.sin(2 * math.pi * 14 * t)
        phase += 2 * math.pi * frequency / RATE
        samples.append(min(1.0, t / 0.005) * math.exp(-4 * t) * (math.sin(phase) + 0.3 * math.sin(2 * phase)))
    return samples


def dish():
    notes = [(0.0, 784.0), (0.08, 987.8), (0.16, 1174.7), (0.24, 1568.0)]
    samples = []
    for i in range(int(RATE * 1.1)):
        t = i / RATE
        value = 0.0
        for start, frequency in notes:
            if t >= start:
                u = t - start
                attack = min(1.0, u / 0.004)
                for ratio, amplitude, decay in PARTIALS:
                    value += attack * amplitude * math.exp(-decay * u) * math.sin(2 * math.pi * frequency * ratio * u)
        samples.append(value)
    return samples


folder = sys.argv[1]
os.makedirs(folder, exist_ok=True)
write(os.path.join(folder, 'tick.wav'), knock(880, 0.12, 45), 0.5)
write(os.path.join(folder, 'door.wav'), knock(330, 0.2, 30), 0.6)
write(os.path.join(folder, 'caught.wav'), caught(), 0.6)
write(os.path.join(folder, 'dish.wav'), dish(), 0.7)
print('ok')
