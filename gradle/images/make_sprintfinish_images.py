"""Draws the pictures of Sprint Finish (gazeplay-games: data/sprintFinish/images), made for GazePlay Learning in
November 2026 to replace pictures of unknown source. The cat, the dog (recolored in brown, so that it is not taken for
the cat) and the fish are OpenMoji (CC BY-SA 4.0); the meadow and the bowl are drawn here, same license as GazePlay
Learning (GPL-3.0-or-later). The hedges, fences, buttons and logs of the board are drawn by the game itself.

python make_sprintfinish_images.py <folder of the 618x618 PNG files of OpenMoji> <data/sprintFinish/images folder>
Pillow is required. Writes cat.png, dog.png (looking to the right, 400 x 400), dish.png (the bowl with a fish,
400 x 400) and meadow.png (1600 x 900).
"""
import colorsys
import os
import random
import sys

from PIL import Image, ImageDraw

src, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)


def emoji(code):
    image = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    return image.crop(image.getbbox())


def square(image, size, margin=0.04):
    """The picture centered in a transparent square."""
    side = max(image.size)
    canvas = Image.new('RGBA', (side, side), (0, 0, 0, 0))
    canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2), image)
    inner = int(size * (1 - 2 * margin))
    canvas = canvas.resize((inner, inner), Image.LANCZOS)
    result = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    result.paste(canvas, ((size - inner) // 2, (size - inner) // 2), canvas)
    return result


def brown(image):
    """The orange of the OpenMoji dog turned into browns (the black lines kept)."""
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s > 0.35 and 0.02 < h < 0.15:
                # Light orange -> light brown, dark orange -> dark brown
                light = v > 0.9 and h > 0.08
                nr, ng, nb = (168, 124, 88) if light else (110, 74, 48)
                pixels[x, y] = (nr, ng, nb, a)
    return image


# The OpenMoji cat and dog look to the left: turned to look to the right (the game turns them when they go left)
cat = emoji('1F408').transpose(Image.FLIP_LEFT_RIGHT)
square(cat, 400).save(os.path.join(out, 'cat.png'))
dog = brown(emoji('1F415')).transpose(Image.FLIP_LEFT_RIGHT)
square(dog, 400).save(os.path.join(out, 'dog.png'))

# The bowl: a red pet bowl seen from the side, a fish on it
K = 2
size = 400 * K
dish = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(dish)
line = (30, 30, 30, 255)
red, red_dark = (226, 74, 64, 255), (176, 46, 40, 255)
top, bottom = 250 * K, 360 * K
draw.polygon([(40 * K, top), (360 * K, top), (330 * K, bottom), (70 * K, bottom)], fill=red, outline=line)
draw.line([(40 * K, top), (70 * K, bottom), (330 * K, bottom), (360 * K, top)], fill=line, width=10 * K)
draw.rectangle([70 * K, 315 * K, 330 * K, 330 * K], fill=red_dark)
draw.ellipse([30 * K, 215 * K, 370 * K, 285 * K], fill=(250, 214, 150, 255), outline=line, width=10 * K)
for x, y in [(110, 240), (160, 255), (220, 238), (270, 252), (300, 236), (140, 232)]:
    draw.ellipse([(x - 14) * K, (y - 9) * K, (x + 14) * K, (y + 9) * K], fill=(196, 130, 70, 255))
fish = emoji('1F41F')
fish = fish.resize((300 * K, int(300 * K * fish.height / fish.width)), Image.LANCZOS)
dish.paste(fish, ((size - fish.width) // 2, 230 * K - fish.height), fish)
dish = dish.crop(dish.getbbox())
square(dish, 400).save(os.path.join(out, 'dish.png'))

# The meadow: light green, tufts of grass and a few little flowers, scattered at random (fixed seed)
random.seed(7)
W, H = 1600, 900
meadow = Image.new('RGB', (W * K, H * K), (150, 204, 104))
draw = ImageDraw.Draw(meadow)
for _ in range(90):
    x, y = random.uniform(0, W), random.uniform(0, H)
    r = random.uniform(60, 150)
    draw.ellipse([(x - r) * K, (y - r * 0.6) * K, (x + r) * K, (y + r * 0.6) * K], fill=(158, 210, 112))
for _ in range(260):
    x, y = random.uniform(0, W), random.uniform(0, H)
    for dx in (-6, 0, 6):
        draw.line([(x + dx * 0.4) * K, y * K, (x + dx) * K, (y - 14) * K], fill=(110, 170, 78), width=3 * K)
for _ in range(45):
    x, y = random.uniform(0, W), random.uniform(0, H)
    color = random.choice([(255, 255, 255), (255, 236, 120), (250, 180, 210)])
    for dx, dy in ((-5, 0), (5, 0), (0, -5), (0, 5)):
        draw.ellipse([(x + dx - 4) * K, (y + dy - 4) * K, (x + dx + 4) * K, (y + dy + 4) * K], fill=color)
    draw.ellipse([(x - 3) * K, (y - 3) * K, (x + 3) * K, (y + 3) * K], fill=(240, 170, 60))
meadow.resize((W, H), Image.LANCZOS).save(os.path.join(out, 'meadow.png'))
