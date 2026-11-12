"""Draws the pictures and makes the sounds of "Tartes à la crème" (Creampie; gazeplay-games: data/creampie), made for
GazePlay Learning on 12/11/2026 to replace a hand, a pie and a sound of unknown source. Same license as GazePlay
Learning (GPL-3.0-or-later).
- images/pie.png (512 x 512): a cream pie seen from above: a golden fluted crust, swirls of cream, a cherry.
- images/hand.png (512 x 512): an open hand seen from the front, the fingers up, with the cuff of a sleeve.
- sounds/throw.wav: the pie thrown, a short rising "whoosh".
- sounds/splat.wav: the pie on the picture, a wet "splotch".
The cream on the picture is drawn by the game.

python make_creampie_images.py <data/creampie folder>
Pillow is required. Sounds: mono, 16 bits, 44.1 kHz.
"""
import math
import os
import random
import struct
import sys
import wave

from PIL import Image, ImageDraw, ImageFilter

S = 4  # supersampling
SIZE = 512


def big():
    return Image.new('RGBA', (SIZE * S, SIZE * S), (0, 0, 0, 0))


def small(image):
    return image.resize((SIZE, SIZE), Image.LANCZOS)


def circle(draw, cx, cy, r, fill, outline=None, width=0):
    draw.ellipse([(cx - r) * S, (cy - r) * S, (cx + r) * S, (cy + r) * S], fill=fill, outline=outline,
                 width=int(width * S))


def pie():
    image = big()
    draw = ImageDraw.Draw(image)
    c = SIZE / 2
    outline = (128, 74, 30)
    # The fluted crust: bumps all around, then the tart
    for i in range(28):
        a = 2 * math.pi * i / 28
        circle(draw, c + 214 * math.cos(a), c + 214 * math.sin(a), 30, (214, 150, 72), outline, 3)
    circle(draw, c, c, 222, (226, 164, 84))
    for i in range(28):
        a = 2 * math.pi * (i + 0.5) / 28
        circle(draw, c + 200 * math.cos(a), c + 200 * math.sin(a), 12, (240, 186, 110))
    circle(draw, c, c, 182, (190, 120, 52))
    # The cream: a soft disc, then swirls with their shade
    circle(draw, c, c, 176, (250, 247, 240))
    rnd = random.Random(3)
    for ring, count, r in ((128, 9, 44), (60, 5, 46), (0, 1, 50)):
        for i in range(count):
            a = 2 * math.pi * i / count + rnd.uniform(-0.1, 0.1)
            x = c + ring * math.cos(a)
            y = c + ring * math.sin(a)
            circle(draw, x + 5, y + 8, r, (226, 220, 210))
            circle(draw, x, y, r, (255, 254, 250))
            circle(draw, x - r * 0.3, y - r * 0.35, r * 0.35, (255, 255, 255))
    # The cherry, with its gleam and its stalk
    draw.line([c * S, (c - 30) * S, (c + 30) * S, (c - 92) * S], fill=(92, 120, 50), width=8 * S)
    circle(draw, c + 4, c + 6, 40, (150, 20, 34))
    circle(draw, c, c, 40, (214, 34, 52), (130, 16, 30), 3)
    circle(draw, c - 13, c - 14, 11, (255, 170, 176))
    return small(image)


def capsule(draw, x0, y0, x1, y1, r, fill):
    """A finger: two round ends joined, as a thick line with round caps."""
    draw.line([x0 * S, y0 * S, x1 * S, y1 * S], fill=fill, width=int(2 * r * S))
    circle(draw, x0, y0, r, fill)
    circle(draw, x1, y1, r, fill)


def hand():
    image = big()
    draw = ImageDraw.Draw(image)
    skin = (255, 206, 160)
    shade = (236, 178, 130)
    outline = (150, 96, 62)
    w = 6
    # The fingers, a little spread, the thumb and the palm: all the outlines first, then all the skin, so that no
    # line crosses the hand
    fingers = ((178, 236, 166, 70, 33), (236, 222, 236, 40, 35), (294, 228, 304, 58, 34), (346, 248, 366, 108, 30),
               (150, 340, 84, 240, 34))
    for x0, y0, x1, y1, r in fingers:
        capsule(draw, x0, y0, x1, y1, r + w, outline)
    draw.rounded_rectangle([132 * S, 210 * S, 392 * S, 420 * S], radius=90 * S, fill=outline)
    for x0, y0, x1, y1, r in fingers:
        capsule(draw, x0, y0, x1, y1, r, skin)
    draw.rounded_rectangle([(132 + w) * S, (210 + w) * S, (392 - w) * S, (420 - w) * S], radius=84 * S, fill=skin)
    # The lines between the fingers, and of the palm
    for x0, y0, x1, y1 in ((201, 96, 204, 222), (270, 92, 268, 216), (336, 118, 330, 226)):
        draw.line([x0 * S, y0 * S, x1 * S, y1 * S], fill=outline, width=5 * S)
    draw.arc([170 * S, 300 * S, 330 * S, 400 * S], 200, 320, fill=shade, width=7 * S)
    # The cuff of the sleeve
    draw.rounded_rectangle([140 * S, 404 * S, 384 * S, 500 * S], radius=26 * S, fill=(60, 110, 190),
                           outline=(36, 70, 130), width=6 * S)
    draw.line([150 * S, 432 * S, 374 * S, 432 * S], fill=(90, 140, 214), width=8 * S)
    return small(image)


RATE = 44100


def write(path, samples, volume):
    peak = max(abs(v) for v in samples)
    with wave.open(path, 'wb') as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b''.join(struct.pack('<h', int(32767 * volume * v / peak)) for v in samples))


def throw():
    rnd = random.Random(9)
    length = 0.42
    samples = []
    low = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        u = t / length
        brightness = 0.04 + 0.3 * u
        low += brightness * (rnd.uniform(-1, 1) - low)
        envelope = math.sin(math.pi * u) ** 2
        samples.append(envelope * low)
    return samples


def splat():
    rnd = random.Random(12)
    length = 0.45
    samples = []
    low = 0.0
    for i in range(int(RATE * length)):
        t = i / RATE
        low += 0.25 * (rnd.uniform(-1, 1) - low)
        thump = math.sin(2 * math.pi * (140 - 120 * t) * t) * math.exp(-18 * t)
        wet = low * math.exp(-9 * t) * (0.6 + 0.4 * math.sin(2 * math.pi * 31 * t))
        samples.append(min(1.0, t / 0.004) * (0.9 * thump + 1.4 * wet))
    return samples


def main():
    folder = sys.argv[1]
    images = os.path.join(folder, 'images')
    sounds = os.path.join(folder, 'sounds')
    os.makedirs(images, exist_ok=True)
    os.makedirs(sounds, exist_ok=True)
    pie().save(os.path.join(images, 'pie.png'))
    hand().filter(ImageFilter.SMOOTH).save(os.path.join(images, 'hand.png'))
    write(os.path.join(sounds, 'throw.wav'), throw(), 0.5)
    write(os.path.join(sounds, 'splat.wav'), splat(), 0.7)


if __name__ == '__main__':
    main()
