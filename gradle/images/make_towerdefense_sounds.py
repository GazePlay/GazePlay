"""Makes the sounds of "Tower Defense" (gazeplay-games: data/towerDefense/sounds), made for GazePlay Learning on
3/11/2026 to replace three sounds of unknown source (craft.mp3, craftdouble.mp3, piou.mp3). Same license as GazePlay
Learning (GPL-3.0-or-later).

python make_towerdefense_sounds.py <data/towerDefense/sounds folder>
build.wav: a tower built (two wooden knocks, then a little chime going up).
builddouble.wav: the double tower built (three knocks, a higher chime).
shot.wav: a shot of the green, red and grey towers (a soft "piou" gliding down).
canon.wav: a shot of the cannon (a low "boom").
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
import random
import struct
import sys
import wave

RATE = 44100
out = sys.argv[1]
os.makedirs(out, exist_ok=True)


def write(name, samples, volume):
    peak = max(abs(v) for v in samples) or 1
    with wave.open(os.path.join(out, name), 'wb') as sound:
        sound.setnchannels(1)
        sound.setsampwidth(2)
        sound.setframerate(RATE)
        sound.writeframes(b''.join(struct.pack('<h', int(32767 * volume * v / peak)) for v in samples))


def silence(seconds):
    return [0.0] * int(RATE * seconds)


def add(samples, at, other):
    begin = int(at * RATE)
    for i, value in enumerate(other):
        if begin + i < len(samples):
            samples[begin + i] += value


def knock(frequency):
    """A short wooden knock: a low tone and a little noise, dying fast."""
    rnd = random.Random(int(frequency))
    result = []
    for i in range(int(RATE * 0.12)):
        t = i / RATE
        tone = math.sin(2 * math.pi * frequency * t) + 0.5 * math.sin(2 * math.pi * frequency * 2.3 * t)
        result.append((tone + 0.4 * rnd.uniform(-1, 1)) * math.exp(-45 * t))
    return result


def chime(base, notes):
    result = silence(0.5)
    for k in range(notes):
        frequency = base * 2 ** (k * 4 / 12)
        note = [math.sin(2 * math.pi * frequency * (i / RATE)) * math.exp(-10 * i / RATE)
                * min(1.0, i / RATE / 0.003) for i in range(int(RATE * 0.35))]
        add(result, k * 0.06, note)
    return result


build = silence(0.75)
add(build, 0.0, knock(180))
add(build, 0.13, knock(210))
add(build, 0.28, chime(660, 3))
write('build.wav', build, 0.7)

double = silence(0.9)
for k, frequency in enumerate((180, 210, 240)):
    add(double, k * 0.12, knock(frequency))
add(double, 0.4, chime(784, 3))
write('builddouble.wav', double, 0.7)

shot = []
for i in range(int(RATE * 0.14)):
    t = i / RATE
    frequency = 1100 - 3500 * t
    shot.append(math.sin(2 * math.pi * frequency * t) * math.exp(-18 * t) * min(1.0, t / 0.002))
write('shot.wav', shot, 0.45)

rnd = random.Random(7)
canon = []
phase = 0.0
for i in range(int(RATE * 0.45)):
    t = i / RATE
    frequency = 130 * math.exp(-4 * t) + 45
    phase += 2 * math.pi * frequency / RATE
    noise = rnd.uniform(-1, 1) * math.exp(-25 * t)
    canon.append((math.sin(phase) * math.exp(-6 * t) + 0.5 * noise) * min(1.0, t / 0.003))
write('canon.wav', canon, 0.8)
print('ok')
