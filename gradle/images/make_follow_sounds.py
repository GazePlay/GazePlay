"""Makes the sounds of "Suivre le chemin" (Follow; gazeplay-games: data/follow/sounds), made for GazePlay Learning on
13/11/2026. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_follow_sounds.py <data/follow folder>
coin.wav: a coin picked up, two quick bright notes going up, about 0.35 s.
key.wav: a key picked up, a little jingle of three bell notes.
door.wav: a door opening, a soft wooden "clack" then a slide.
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


def mix(base, sound, at, gain=1.0):
    start = int(RATE * at)
    while len(base) < start + len(sound):
        base.append(0.0)
    for i, v in enumerate(sound):
        base[start + i] += gain * v
    return base


def note(frequency, length, decay):
    """A soft bright note: a sine with a little of its octave, a quick attack and a damped end."""
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-decay * t) * min(1.0, t / 0.004) * min(1.0, (length - t) / 0.02)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.25 * math.sin(2 * math.pi * frequency * 2 * t)))
    return samples


def bell(frequency, length):
    samples = []
    for i in range(int(RATE * length)):
        t = i / RATE
        envelope = math.exp(-7 * t) * min(1.0, t / 0.003)
        samples.append(envelope * (math.sin(2 * math.pi * frequency * t)
                                   + 0.3 * math.sin(2 * math.pi * frequency * 2.76 * t) * math.exp(-9 * t)))
    return samples


def coin():
    sound = []
    mix(sound, note(988, 0.09, 12), 0.0)
    mix(sound, note(1319, 0.28, 9), 0.07)
    return sound


def key():
    sound = []
    for k, frequency in enumerate((1175, 1568, 1397)):
        mix(sound, bell(frequency, 0.5), 0.06 * k, 1.0 - 0.15 * k)
    return sound


def door():
    """A damped low knock, then a short band of noise going down (the door sliding away)."""
    rnd = random.Random(11)
    sound = []
    knock = []
    for i in range(int(RATE * 0.18)):
        t = i / RATE
        knock.append(math.exp(-30 * t) * min(1.0, t / 0.002) * (math.sin(2 * math.pi * 220 * t)
                                                                + 0.4 * rnd.uniform(-1, 1) * math.exp(-60 * t)))
    mix(sound, knock, 0.0)
    slide = []
    low = 0.0
    length = 0.35
    for i in range(int(RATE * length)):
        t = i / RATE
        low += (0.08 + 0.1 * (1 - t / length)) * (rnd.uniform(-1, 1) - low)
        envelope = min(1.0, t / 0.05) * min(1.0, (length - t) / 0.15)
        slide.append(envelope * low)
    mix(sound, slide, 0.08, 1.6)
    return sound


def main():
    folder = os.path.join(sys.argv[1], 'sounds')
    write(os.path.join(folder, 'coin.wav'), coin(), 0.5)
    write(os.path.join(folder, 'key.wav'), key(), 0.5)
    write(os.path.join(folder, 'door.wav'), door(), 0.6)


if __name__ == '__main__':
    main()
