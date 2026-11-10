"""Makes the sounds of "Répète après moi" (Repeat After Me, name code Simon; gazeplay-games: data/simonGame/sounds), made
for GazePlay Learning in November 2026 to replace 7 notes of unknown source (do.wav to sol.wav). Same license as
GazePlay Learning (GPL-3.0-or-later).

python make_simon_sounds.py <data/simonGame folder>
sounds/note0.wav to note3.wav: the notes of the green, red, yellow and blue buttons, do, mi, sol and the higher do (a
major chord: they always sound right together), a soft bell of 0.6 second.
sounds/mistake.wav: a mistake, a low soft buzz going down.
sounds/success.wav: the melody complete, the four notes going up.
Mono, 16 bits, 44.1 kHz.
"""
import math
import os
import struct
import sys
import wave

RATE = 44100
NOTES = (261.63, 329.63, 392.00, 523.25)


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


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-3.5 * t) * min(1.0, t / 0.005) * min(1.0, (length - t) / 0.05)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.3 * math.sin(2 * math.pi * frequency * 2 * t) * math.exp(-4 * t)
                                   + 0.12 * math.sin(2 * math.pi * frequency * 3 * t) * math.exp(-8 * t)))
    return samples


def buzz():
    samples = []
    phase = 0.0
    length = 0.45
    for i in range(int(RATE * length)):
        t = i / RATE
        frequency = 160 * (1 - 0.3 * t / length)
        phase += 2 * math.pi * frequency / RATE
        envelope = min(1.0, t / 0.01) * math.exp(-3 * t)
        samples.append(envelope * (math.sin(phase) + 0.4 * math.sin(2 * phase) + 0.2 * math.sin(3 * phase)))
    return samples


def success():
    sound = []
    for k, frequency in enumerate(NOTES):
        mix(sound, bell(frequency, 0.6 if k == 3 else 0.25), k * 0.1)
    return sound


folder = sys.argv[1]
for button, frequency in enumerate(NOTES):
    write(os.path.join(folder, 'sounds', 'note%d.wav' % button), bell(frequency, 0.6), 0.5)
write(os.path.join(folder, 'sounds', 'mistake.wav'), buzz(), 0.4)
write(os.path.join(folder, 'sounds', 'success.wav'), success(), 0.45)
print('ok')
