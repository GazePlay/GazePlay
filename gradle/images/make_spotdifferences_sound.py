"""Makes the sound of a difference found in Spot The Differences (gazeplay-games: data/spotthedifferences/ding.wav),
made for GazePlay Learning in October 2026 to replace a sound of unknown source. Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_spotdifferences_sound.py <ding.wav>
Two short bell notes going up (C6, then G6): for each, a few partials of a bell fading out. Mono, 16 bits, 44.1 kHz.
"""
import math
import struct
import sys
import wave

RATE = 44100
LENGTH = 0.9  # seconds
NOTES = [(0.0, 1046.5), (0.12, 1568.0)]  # start (s), frequency (Hz)
PARTIALS = [(1.0, 1.0, 5.0), (2.0, 0.35, 8.0), (2.76, 0.2, 11.0), (5.4, 0.08, 16.0)]  # ratio, amplitude, decay

samples = []
for i in range(int(RATE * LENGTH)):
    t = i / RATE
    value = 0.0
    for start, frequency in NOTES:
        if t >= start:
            u = t - start
            attack = min(1.0, u / 0.004)
            for ratio, amplitude, decay in PARTIALS:
                value += attack * amplitude * math.exp(-decay * u) * math.sin(2 * math.pi * frequency * ratio * u)
    samples.append(value)
peak = max(abs(v) for v in samples)
with wave.open(sys.argv[1], 'wb') as out:
    out.setnchannels(1)
    out.setsampwidth(2)
    out.setframerate(RATE)
    out.writeframes(b''.join(struct.pack('<h', int(32767 * 0.7 * v / peak)) for v in samples))
print('ok')
