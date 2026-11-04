"""Makes the sounds of Biboule Jump (gazeplay-games: data/biboulejump/sounds), made for GazePlay Learning in November 2026
to replace sounds of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_biboulejump_sounds.py <data/biboulejump folder>
sounds/bounce.wav: a short, soft note gliding up, for a cloud.
sounds/boing.wav: a longer spring, trembling and gliding up, for a springboard.
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


def bounce():
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.16)):
        t = i / RATE
        phase += 2 * math.pi * (300 + 900 * t / 0.16) / RATE
        samples.append(math.sin(phase) * math.exp(-16 * t) * min(1.0, t / 0.004))
    return samples


def boing():
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.55)):
        t = i / RATE
        frequency = (170 + 560 * t / 0.55) * (1 + 0.16 * math.sin(2 * math.pi * 24 * t) * math.exp(-3 * t))
        phase += 2 * math.pi * frequency / RATE
        samples.append((math.sin(phase) + 0.3 * math.sin(2 * phase)) * math.exp(-5 * t) * min(1.0, t / 0.004))
    return samples


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'bounce.wav'), bounce(), 0.45)
write(os.path.join(folder, 'sounds', 'boing.wav'), boing(), 0.5)
print('ok')
