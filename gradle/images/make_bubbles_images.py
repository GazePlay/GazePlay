"""Draws the pictures of Bubbles (gazeplay-games: data/bubble/images), made for GazePlay Learning in November 2026 to
replace pictures of unknown source. The three underwater scenes hidden under the bubbles (reef.png, wreck.png,
kelp.png, 1600 x 900) are drawn here, same license as GazePlay Learning (GPL-3.0-or-later); the animals shown in the
bubbles (animals/*.png, 300 x 300) are OpenMoji (CC BY-SA 4.0).

python make_bubbles_images.py <folder of the 618x618 PNG files of OpenMoji> <data/bubble/images folder>
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

src, out = sys.argv[1], sys.argv[2]
os.makedirs(os.path.join(out, 'animals'), exist_ok=True)

W, H, K = 1600, 900, 2

ANIMALS = {
    'fish': '1F41F', 'tropical-fish': '1F420', 'blowfish': '1F421', 'octopus': '1F419', 'crab': '1F980',
    'turtle': '1F422', 'whale': '1F433', 'dolphin': '1F42C', 'jellyfish': '1FABC', 'seal': '1F9AD',
    'shrimp': '1F990', 'squid': '1F991', 'lobster': '1F99E', 'penguin': '1F427',
}


def emoji(code):
    image = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    return image.crop(image.getbbox())


def square(image, size, margin=0.06):
    side = max(image.size)
    canvas = Image.new('RGBA', (side, side), (0, 0, 0, 0))
    canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2), image)
    inner = int(size * (1 - 2 * margin))
    canvas = canvas.resize((inner, inner), Image.LANCZOS)
    result = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    result.paste(canvas, ((size - inner) // 2, (size - inner) // 2), canvas)
    return result


for name, code in ANIMALS.items():
    square(emoji(code), 300).save(os.path.join(out, 'animals', name + '.png'))


def s(*values):
    return [v * K for v in values]


def water(top_color, bottom_color):
    image = Image.new('RGB', (W * K, H * K))
    draw = ImageDraw.Draw(image)
    for y in range(H * K):
        t = y / (H * K)
        draw.line([(0, y), (W * K, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top_color, bottom_color)))
    return image, draw


def rays(image, count, seed):
    """Light coming from the surface: soft pale beams."""
    rnd = random.Random(seed)
    layer = Image.new('RGBA', image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)
    for _ in range(count):
        x = rnd.uniform(0, W)
        width = rnd.uniform(60, 140)
        slant = rnd.uniform(-200, 200)
        draw.polygon(s(x, 0, x + width, 0, x + width + slant, H * 0.75, x + slant, H * 0.75), fill=(255, 255, 255, 26))
    layer = layer.filter(ImageFilter.GaussianBlur(25 * K))
    image.paste(layer, (0, 0), layer)


def sand(draw, top, color, dark, seed):
    rnd = random.Random(seed)
    points = [(0, H)]
    for x in range(0, W + 100, 100):
        points.append((x, top + 25 * math.sin(x / 170) + rnd.uniform(-8, 8)))
    points.append((W, H))
    draw.polygon([(px * K, py * K) for px, py in points], fill=color)
    for _ in range(140):
        x, y = rnd.uniform(0, W), rnd.uniform(top + 30, H)
        draw.ellipse(s(x - 3, y - 2, x + 3, y + 2), fill=dark)


def bubbles(draw, count, seed):
    rnd = random.Random(seed)
    for _ in range(count):
        x, y, r = rnd.uniform(0, W), rnd.uniform(0, H * 0.8), rnd.uniform(4, 14)
        draw.ellipse(s(x - r, y - r, x + r, y + r), outline=(220, 245, 255), width=2 * K)


def fish(draw, x, y, size, color, facing=1):
    body = s(x - size, y - size * 0.55, x + size, y + size * 0.55)
    draw.ellipse(body, fill=color, outline=(30, 30, 40), width=3 * K)
    tail = x - facing * size
    draw.polygon(s(tail, y, tail - facing * size * 0.7, y - size * 0.5, tail - facing * size * 0.7, y + size * 0.5),
                 fill=color, outline=(30, 30, 40))
    eye = x + facing * size * 0.5
    draw.ellipse(s(eye - size * 0.12, y - size * 0.22, eye + size * 0.12, y + 0.02 * size), fill=(30, 30, 40))


def coral(draw, x, base, height, color, seed):
    rnd = random.Random(seed)

    def branch(px, py, length, angle, width):
        if length < 18 or width < 3:
            return
        ex = px + length * math.cos(angle)
        ey = py - length * math.sin(angle)
        draw.line(s(px, py, ex, ey), fill=color, width=int(width * K))
        draw.ellipse(s(ex - width / 2, ey - width / 2, ex + width / 2, ey + width / 2), fill=color)
        for delta in (-0.45, 0.4):
            branch(ex, ey, length * rnd.uniform(0.6, 0.78), angle + delta + rnd.uniform(-0.15, 0.15), width * 0.72)

    branch(x, base, height * 0.42, math.pi / 2, 22)


def seaweed(draw, x, base, height, color, phase):
    points = []
    for i in range(0, 41):
        t = i / 40
        points.append(((x + 24 * math.sin(t * 6 + phase)) * K, (base - t * height) * K))
    draw.line(points, fill=color, width=14 * K, joint='curve')


def star(draw, x, y, r, color):
    points = []
    for i in range(10):
        radius = r if i % 2 == 0 else r * 0.45
        angle = -math.pi / 2 + i * math.pi / 5
        points.append(((x + radius * math.cos(angle)) * K, (y + radius * math.sin(angle)) * K))
    draw.polygon(points, fill=color, outline=(120, 50, 20))


def finish(image, name):
    image.resize((W, H), Image.LANCZOS).save(os.path.join(out, name))


# The reef: corals of all colors, fishes, a starfish
image, draw = water((70, 180, 225), (20, 90, 160))
rays(image, 7, 1)
draw = ImageDraw.Draw(image)
sand(draw, 720, (238, 214, 160), (215, 186, 130), 2)
for i, (x, height, color) in enumerate([(150, 330, (240, 110, 120)), (380, 260, (250, 170, 70)),
                                        (620, 360, (200, 90, 200)), (900, 280, (250, 120, 90)),
                                        (1150, 340, (90, 200, 170)), (1400, 300, (250, 200, 80))]):
    coral(draw, x, 760, height, color, i)
for x, y, size, color, facing in [(300, 220, 40, (255, 200, 60), 1), (780, 160, 34, (90, 160, 250), -1),
                                  (1100, 260, 46, (250, 120, 70), 1), (1350, 120, 30, (120, 220, 120), -1),
                                  (520, 420, 28, (250, 150, 200), 1)]:
    fish(draw, x, y, size, color, facing)
star(draw, 760, 820, 40, (250, 140, 60))
bubbles(draw, 30, 3)
finish(image, 'reef.png')

# The wreck: a sunken boat, a treasure chest, seaweed
image, draw = water((40, 120, 180), (10, 50, 100))
rays(image, 5, 4)
draw = ImageDraw.Draw(image)
sand(draw, 700, (205, 190, 150), (180, 160, 120), 5)
draw.polygon(s(380, 520, 1180, 470, 1120, 760, 470, 780), fill=(120, 80, 50), outline=(70, 45, 25), width=8 * K)
for y in (560, 620, 690):
    draw.line(s(400, y, 1150, y - 40), fill=(90, 60, 35), width=6 * K)
draw.line(s(820, 480, 760, 170), fill=(100, 70, 40), width=18 * K)
draw.polygon(s(770, 210, 980, 300, 760, 360), fill=(230, 225, 210), outline=(150, 140, 120), width=4 * K)
for x in (520, 640, 900, 1020):
    draw.ellipse(s(x - 24, 600, x + 24, 648), fill=(30, 60, 90), outline=(70, 45, 25), width=5 * K)
draw.rectangle(s(1260, 700, 1440, 800), fill=(150, 95, 40), outline=(80, 50, 20), width=6 * K)
draw.chord(s(1260, 640, 1440, 760), 180, 360, fill=(170, 110, 50), outline=(80, 50, 20), width=6 * K)
for x in range(1285, 1430, 30):
    draw.ellipse(s(x - 12, 690, x + 12, 714), fill=(250, 210, 60), outline=(180, 140, 20))
for i, x in enumerate((120, 200, 300, 1500, 1560)):
    seaweed(draw, x, 800, 380 + 40 * (i % 2), (50, 150, 80), i)
fish(draw, 300, 300, 36, (250, 200, 80), 1)
fish(draw, 1350, 330, 30, (240, 120, 160), -1)
bubbles(draw, 25, 6)
finish(image, 'wreck.png')

# The kelp forest: tall seaweed, shells, starfishes, a turtle-shaped rock
image, draw = water((90, 200, 200), (20, 100, 120))
rays(image, 8, 7)
draw = ImageDraw.Draw(image)
sand(draw, 740, (240, 225, 180), (220, 200, 150), 8)
for i, x in enumerate(range(80, 1600, 140)):
    seaweed(draw, x + (i % 3) * 20, 800, 520 + 120 * ((i * 7) % 3), (40 + 20 * (i % 3), 140 + 25 * (i % 2), 70), i)
for x, y, color in [(400, 820, (250, 120, 90)), (1000, 840, (250, 190, 70)), (1300, 810, (240, 110, 150))]:
    star(draw, x, y, 34, color)
for x in (600, 1180):
    draw.pieslice(s(x - 40, 790, x + 40, 870), 180, 360, fill=(250, 220, 210), outline=(170, 120, 110), width=4 * K)
    for k in range(-30, 40, 15):
        draw.line(s(x, 830, x + k, 795), fill=(200, 150, 140), width=3 * K)
fish(draw, 700, 250, 38, (250, 210, 70), -1)
fish(draw, 1050, 380, 30, (120, 180, 250), 1)
bubbles(draw, 35, 9)
finish(image, 'kelp.png')
print('ok')
