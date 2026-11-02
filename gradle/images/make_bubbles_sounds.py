"""Makes the sounds of Bubbles (gazeplay-games: data/bubble/sounds), made for GazePlay Learning in November 2026 to
replace two sounds of SoundBible whose license was not known. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_bubbles_sounds.py <data/bubble/sounds folder>
pop1.wav, pop2.wav, pop3.wav: a bubble bursting, three pitches (a short sine gliding up, then a click).
picture.wav: five bell notes going up when the hidden picture is all found.
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


def pop(start, rise):
    samples = []
    phase = 0.0
    for i in range(int(RATE * 0.12)):
        t = i / RATE
        frequency = start + rise * t
        phase += 2 * math.pi * frequency / RATE
        value = math.sin(phase) * math.exp(-38 * t) * min(1.0, t / 0.0015)
        # A small click at the start, like the skin of the bubble breaking
        value += 0.5 * math.exp(-900 * t) * math.sin(2 * math.pi * 3200 * t)
        samples.append(value)
    return samples


def picture():
    notes = [(0.0, 659.3), (0.08, 784.0), (0.16, 987.8), (0.24, 1174.7), (0.32, 1568.0)]
    samples = []
    for i in range(int(RATE * 1.2)):
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
write(os.path.join(folder, 'pop1.wav'), pop(500, 5200), 0.6)
write(os.path.join(folder, 'pop2.wav'), pop(650, 6500), 0.6)
write(os.path.join(folder, 'pop3.wav'), pop(820, 7600), 0.6)
write(os.path.join(folder, 'picture.wav'), picture(), 0.7)
print('ok')
