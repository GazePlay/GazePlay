"""Makes the sounds of the shooting game (gazeplay-games: data/shooter/biboule/sounds and data/shooter/robot/sounds),
made for GazePlay Learning in November 2026 to replace sounds of unknown source. Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_shooter_sounds.py <data/shooter folder>
biboule/sounds/shot1.wav to shot3.wav: the magic wand (a quick sparkle of bell notes going up), three pitches.
robot/sounds/shot1.wav to shot3.wav: the gun (a laser "pew" gliding down), three pitches.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
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


def sparkle(base):
    samples = [0.0] * int(RATE * 0.5)
    for k in range(5):
        frequency = base * 2 ** (k * 4 / 12)
        begin = int(k * 0.04 * RATE)
        for i in range(len(samples) - begin):
            t = i / RATE
            value = math.sin(2 * math.pi * frequency * t) + 0.3 * math.sin(2 * math.pi * frequency * 2.76 * t)
            samples[begin + i] += value * math.exp(-14 * t) * min(1.0, t / 0.002)
    return samples


def pew(start):
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.22)):
        t = i / RATE
        frequency = start * math.exp(-9 * t)
        phase += 2 * math.pi * frequency / RATE
        square = 1.0 if math.sin(phase) >= 0 else -1.0
        samples.append((0.6 * math.sin(phase) + 0.25 * square) * math.exp(-9 * t) * min(1.0, t / 0.002))
    return samples


folder = sys.argv[1]
for n, base in enumerate((1046.5, 1174.7, 1318.5), start=1):
    write(os.path.join(folder, 'biboule', 'sounds', 'shot%d.wav' % n), sparkle(base), 0.5)
for n, start in enumerate((1500, 1800, 2100), start=1):
    write(os.path.join(folder, 'robot', 'sounds', 'shot%d.wav' % n), pew(start), 0.5)
print('ok')
