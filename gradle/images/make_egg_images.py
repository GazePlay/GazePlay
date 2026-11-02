"""Draws the pictures of Egg Game (gazeplay-games: data/egg/images), made for GazePlay Learning in November 2026 to
replace photos of unknown license. The meadow with its nest (meadow.png, the back of the nest; nest-front.png, its
front edge, drawn over the egg; 1600 x 900) is drawn here, same license as GazePlay Learning (GPL-3.0-or-later); the
animals coming out of the egg (animals/*.png, 300 x 300) are OpenMoji (CC BY-SA 4.0). The egg and its cracks are
drawn by the game.

python make_egg_images.py <folder of the 618x618 PNG files of OpenMoji> <data/egg/images folder>
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

src, out = sys.argv[1], sys.argv[2]
os.makedirs(os.path.join(out, 'animals'), exist_ok=True)

ANIMALS = {
    'chick': '1F425', 'duck': '1F986', 'turtle': '1F422', 'crocodile': '1F40A', 'snake': '1F40D',
    'sauropod': '1F995', 't-rex': '1F996', 'penguin': '1F427', 'lizard': '1F98E', 'bird': '1F426',
    'owl': '1F989', 'parrot': '1F99C', 'swan': '1F9A2', 'flamingo': '1F9A9',
}


def emoji(code):
    image = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    return image.crop(image.getbbox())


def square(image, size, margin=0.04):
    side = max(image.size)
    canvas = Image.new('RGBA', (side, side), (0, 0, 0, 0))
    # Standing at the bottom of the square: it comes out of the egg
    canvas.paste(image, ((side - image.width) // 2, side - image.height), image)
    inner = int(size * (1 - 2 * margin))
    canvas = canvas.resize((inner, inner), Image.LANCZOS)
    result = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    result.paste(canvas, ((size - inner) // 2, size - inner), canvas)
    return result


for name, code in ANIMALS.items():
    square(emoji(code), 300).save(os.path.join(out, 'animals', name + '.png'))

W, H, K = 1600, 900, 2
NEST_X, NEST_Y, NEST_RX, NEST_RY = 800, 700, 330, 95


def s(*values):
    return [v * K for v in values]


# The meadow: a sky, hills, grass, a few flowers (fixed seed)
rnd = random.Random(11)
meadow = Image.new('RGB', (W * K, H * K))
draw = ImageDraw.Draw(meadow)
for y in range(H * K):
    t = y / (H * K)
    draw.line([(0, y), (W * K, y)], fill=(int(170 + 40 * t), int(220 + 15 * t), int(250 - 10 * t)))
draw.ellipse(s(-300, 380, 900, 900), fill=(150, 205, 110))
draw.ellipse(s(700, 420, 1900, 950), fill=(135, 195, 100))
draw.rectangle(s(0, 560, W, H), fill=(140, 200, 105))
for _ in range(220):
    x, y = rnd.uniform(0, W), rnd.uniform(560, H)
    for dx in (-6, 0, 6):
        draw.line([(x + dx * 0.4) * K, y * K, (x + dx) * K, (y - 14) * K], fill=(100, 160, 75), width=3 * K)
for _ in range(40):
    x, y = rnd.uniform(0, W), rnd.uniform(580, H)
    color = rnd.choice([(255, 255, 255), (255, 236, 120), (250, 180, 210)])
    for dx, dy in ((-5, 0), (5, 0), (0, -5), (0, 5)):
        draw.ellipse(s(x + dx - 4, y + dy - 4, x + dx + 4, y + dy + 4), fill=color)
    draw.ellipse(s(x - 3, y - 3, x + 3, y + 3), fill=(240, 170, 60))
# A sun and two clouds
draw.ellipse(s(1340, 70, 1480, 210), fill=(255, 220, 90))
for cx, cy in ((300, 140), (980, 100)):
    for dx, dy, r in ((-60, 10, 45), (0, -10, 60), (60, 10, 45)):
        draw.ellipse(s(cx + dx - r, cy + dy - r, cx + dx + r, cy + dy + r), fill=(255, 255, 255))


def straws(draw, top, bottom, count, seed, colors):
    rnd = random.Random(seed)
    for _ in range(count):
        a = rnd.uniform(0, math.pi * 2)
        x = NEST_X + math.cos(a) * NEST_RX * rnd.uniform(0.75, 1.05)
        y = NEST_Y + math.sin(a) * NEST_RY * rnd.uniform(0.75, 1.05)
        if not top <= y <= bottom:
            continue
        length = rnd.uniform(40, 90)
        angle = rnd.uniform(-0.5, 0.5)
        draw.line(s(x - length / 2 * math.cos(angle), y - length / 2 * math.sin(angle),
                    x + length / 2 * math.cos(angle), y + length / 2 * math.sin(angle)),
                  fill=rnd.choice(colors), width=5 * K)


# The back of the nest: the hollow, in the background picture
draw.ellipse(s(NEST_X - NEST_RX, NEST_Y - NEST_RY, NEST_X + NEST_RX, NEST_Y + NEST_RY), fill=(160, 110, 55))
draw.ellipse(s(NEST_X - NEST_RX * 0.8, NEST_Y - NEST_RY * 0.7, NEST_X + NEST_RX * 0.8, NEST_Y + NEST_RY * 0.5),
             fill=(110, 75, 35))
straws(draw, 0, NEST_Y, 260, 12, [(200, 150, 80), (180, 130, 60), (220, 175, 100)])
meadow.resize((W, H), Image.LANCZOS).save(os.path.join(out, 'meadow.png'))

# The front edge of the nest, drawn over the bottom of the egg
front = Image.new('RGBA', (W * K, H * K), (0, 0, 0, 0))
draw = ImageDraw.Draw(front)
draw.chord(s(NEST_X - NEST_RX, NEST_Y - NEST_RY * 0.9, NEST_X + NEST_RX, NEST_Y + NEST_RY * 1.15), 0, 180,
           fill=(175, 125, 60, 255))
straws(draw, NEST_Y - 5, H, 320, 13, [(205, 155, 80, 255), (185, 135, 65, 255), (225, 180, 105, 255)])
front = front.filter(ImageFilter.SMOOTH)
front.resize((W, H), Image.LANCZOS).save(os.path.join(out, 'nest-front.png'))
print('ok')
