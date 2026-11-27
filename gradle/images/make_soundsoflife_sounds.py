"""Makes the sound of the boat of "Les cris des animaux" (name code SoundsOfLife; gazeplay-games:
data/soundsoflife/sea/boat.wav), made for GazePlay Learning on 27/11/2026 (tester's feedback, point 81: the sea with
its surface, a sailing boat and its bell). No free recording of a ship's bell was found. Same license as GazePlay
Learning (GPL-3.0-or-later).

python make_soundsoflife_sounds.py <data/soundsoflife folder>
sea/boat.wav: the bell of a ship struck twice, then twice again ("ding-ding, ding-ding", as the bells of the watches).
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


def strike(frequency, length):
    """A bronze bell struck: its partials (hum, prime, tierce, quint, nominal), each fading at its own speed."""
    partials = ((0.5, 0.5, 1.2), (1.0, 1.0, 1.8), (1.19, 0.6, 2.6), (1.5, 0.35, 3.0), (2.0, 0.5, 3.4),
                (2.74, 0.25, 5.0))
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        attack = min(1.0, t / 0.002)
        value = sum(level * math.exp(-decay * t) * math.sin(2 * math.pi * frequency * ratio * t)
                    for ratio, level, decay in partials)
        samples.append(attack * value)
    return samples


def boat():
    sound = []
    for at in (0.0, 0.35, 1.2, 1.55):
        mix(sound, strike(880, 1.6), at)
    return sound


write(os.path.join(sys.argv[1], 'sea', 'boat.wav'), boat(), 0.5)
print('ok')
