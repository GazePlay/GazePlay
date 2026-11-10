"""Makes the sounds of "Pierre papier ciseaux" (Rock Paper Scissors; gazeplay-games: data/rockPaperScissors/sounds),
made for GazePlay Learning in November 2026 (the former game had no sound). Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_rockpaperscissors_sounds.py <data/rockPaperScissors folder>
sounds/tick.wav: each word of "rock, paper, scissors!", a short wooden knock.
sounds/win.wav: a round won (or a right answer), three notes going up.
sounds/lose.wav: a round lost (or a wrong answer), two soft notes going down.
sounds/draw.wav: a draw, two notes of the same pitch.
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


def mix(base, sound, at):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += v
    return base


def knock():
    samples = []
    for i in range(int(RATE * 0.1)):
        t = i / RATE
        samples.append(math.sin(2 * math.pi * 700 * t) * math.exp(-60 * t)
                       + 0.4 * math.sin(2 * math.pi * 1400 * t) * math.exp(-90 * t))
    return samples


def note(frequency, length, decay):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-decay * t) * min(1.0, t / 0.004)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t) + 0.3 * math.sin(4 * math.pi * frequency * t)))
    return samples


def sequence(notes):
    sound = []
    for k, (frequency, length, at) in enumerate(notes):
        mix(sound, note(frequency, length, 5), at)
    return sound


folder = sys.argv[1]
write(os.path.join(folder, 'sounds', 'tick.wav'), knock(), 0.45)
write(os.path.join(folder, 'sounds', 'win.wav'), sequence([(523, 0.3, 0), (659, 0.3, 0.12), (784, 0.6, 0.24)]), 0.45)
write(os.path.join(folder, 'sounds', 'lose.wav'), sequence([(392, 0.35, 0), (311, 0.6, 0.22)]), 0.4)
write(os.path.join(folder, 'sounds', 'draw.wav'), sequence([(440, 0.25, 0), (440, 0.4, 0.2)]), 0.4)
print('ok')
