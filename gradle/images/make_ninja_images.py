"""Draws the pictures of the game "Ninja" (gazeplay-games: data/ninja/images), made for GazePlay Learning in November
2026 (maintainer's choice: a theme of fruits; the game used to show the default images on a plain background). Same
license as GazePlay Learning (GPL-3.0-or-later).
- orchard.png (1600 x 900): a soft orchard, pale so that the fruits stand out.
- apple.png, pear.png, orange.png, lemon.png, strawberry.png, peach.png, plum.png, kiwi.png (512 x 512): the fruits, with
  soft shading, a thin outline and a white border, centered, filling the circle the game uses as a target.

python make_ninja_images.py <data/ninja folder>
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter

out = os.path.join(sys.argv[1], 'images')
os.makedirs(out, exist_ok=True)
SIZE = 512
K = 3
S = SIZE * K
CENTER = S / 2


def ellipse(draw, cx, cy, rx, ry, fill=255):
    draw.ellipse([(cx - rx) * K, (cy - ry) * K, (cx + rx) * K, (cy + ry) * K], fill=fill)


def smooth(mask, radius=18):
    """Rounds the corners where the shapes of a fruit meet."""
    return mask.filter(ImageFilter.GaussianBlur(radius)).point(lambda v: 255 if v > 127 else 0)


def new_mask():
    mask = Image.new('L', (S, S), 0)
    return mask, ImageDraw.Draw(mask)


def fruit(shape, base, dark, light, outline, details=None):
    """A fruit: its shape filled with a color, a shadow at the bottom right, a soft light at the top left, a thin outline,
    a white border, then its details (stem, leaf, seeds)."""
    mask = shape()
    body = Image.new('RGB', (S, S), base)
    # The shadow: the shape less the same shape moved to the top left, blurred
    moved = ImageChops.offset(mask, -30 * K, -34 * K)
    shadow = ImageChops.subtract(mask, moved).filter(ImageFilter.GaussianBlur(26 * K))
    body = Image.composite(Image.new('RGB', (S, S), dark), body, shadow)
    # The light
    box = mask.getbbox()
    glow = Image.new('L', (S, S), 0)
    ImageDraw.Draw(glow).ellipse([box[0] + (box[2] - box[0]) * 0.16, box[1] + (box[3] - box[1]) * 0.1,
                                  box[0] + (box[2] - box[0]) * 0.52, box[1] + (box[3] - box[1]) * 0.42], fill=175)
    glow = glow.filter(ImageFilter.GaussianBlur(20 * K))
    body = Image.composite(Image.new('RGB', (S, S), light), body, glow)

    image = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    border = mask.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.GaussianBlur(10 * K)).point(
        lambda v: 255 if v > 8 else 0)
    image.paste((255, 255, 255, 255), (0, 0), border)
    line = mask.filter(ImageFilter.GaussianBlur(3 * K)).point(lambda v: 255 if v > 20 else 0)
    image.paste(outline + (255,), (0, 0), line)
    inner = mask.filter(ImageFilter.GaussianBlur(3 * K)).point(lambda v: 255 if v > 200 else 0)
    image.paste(body, (0, 0), inner)
    if details:
        details(image, ImageDraw.Draw(image))
    return image


def save(image, name):
    # Centered on what is drawn, so that the target circle of the game fits the fruit
    box = image.getbbox()
    width, height = box[2] - box[0], box[3] - box[1]
    side = max(width, height) + 4 * K
    square = Image.new('RGBA', (side, side), (0, 0, 0, 0))
    square.paste(image.crop(box), ((side - width) // 2, (side - height) // 2))
    square.resize((SIZE, SIZE), Image.LANCZOS).save(os.path.join(out, name + '.png'))


def leaf(draw, x, y, length, angle, color=(92, 165, 80), vein=(58, 120, 58)):
    points = []
    for i in range(41):
        t = i / 40
        half = math.sin(math.pi * t) * length * 0.27
        px = t * length
        points.append((px, half))
    for i in range(40, -1, -1):
        t = i / 40
        half = math.sin(math.pi * t) * length * 0.27
        points.append((t * length, -half))
    cos, sin = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    placed = [((x + px * cos - py * sin) * K, (y + px * sin + py * cos) * K) for px, py in points]
    draw.polygon(placed, fill=color + (255,), outline=vein + (255,))
    draw.line([(x * K, y * K), ((x + length * 0.92 * cos) * K, (y + length * 0.92 * sin) * K)],
              fill=vein + (255,), width=3 * K)


def stem(draw, x0, y0, x1, y1, width=13, color=(112, 78, 52)):
    draw.line([(x0 * K, y0 * K), (x1 * K, y1 * K)], fill=color + (255,), width=width * K)
    r = width * K / 2
    draw.ellipse([x1 * K - r, y1 * K - r, x1 * K + r, y1 * K + r], fill=color + (255,))


# The apple
def apple_shape():
    mask, draw = new_mask()
    ellipse(draw, 200, 290, 118, 145)
    ellipse(draw, 312, 290, 118, 145)
    return smooth(mask)


def apple_details(image, draw):
    ellipse_shadow = (150, 26, 40, 255)
    draw.arc([210 * K, 118 * K, 302 * K, 190 * K], 20, 160, fill=ellipse_shadow, width=5 * K)
    stem(draw, 256, 165, 268, 95)
    leaf(draw, 268, 122, 96, -24)


save(fruit(apple_shape, (226, 58, 68), (160, 28, 48), (255, 170, 165), (120, 22, 38), apple_details), 'apple')


# The pear
def pear_shape():
    mask, draw = new_mask()
    ellipse(draw, 256, 335, 150, 135)
    ellipse(draw, 256, 175, 82, 95)
    draw.polygon([(150 * K, 300 * K), (195 * K, 180 * K), (317 * K, 180 * K), (362 * K, 300 * K)], fill=255)
    return smooth(mask, 30)


def pear_details(image, draw):
    stem(draw, 256, 92, 272, 38)
    leaf(draw, 268, 62, 84, -10)


save(fruit(pear_shape, (176, 208, 78), (118, 156, 52), (238, 246, 170), (84, 118, 42), pear_details), 'pear')


# The orange
def orange_shape():
    mask, draw = new_mask()
    ellipse(draw, 256, 262, 200, 198)
    return mask


def orange_details(image, draw):
    rnd = random.Random(4)
    for _ in range(120):
        a, r = rnd.uniform(0, 2 * math.pi), 180 * math.sqrt(rnd.random())
        x, y = 256 + r * math.cos(a), 262 + r * math.sin(a)
        ellipse(draw, x, y, 2.2, 2.2, fill=(222, 124, 26, 255))
    for i in range(5):
        a = math.radians(-90 + i * 72)
        draw.line([(256 * K, 76 * K), ((256 + 26 * math.cos(a)) * K, (76 + 26 * math.sin(a)) * K)],
                  fill=(82, 128, 62, 255), width=8 * K)
    leaf(draw, 262, 70, 88, -18)


save(fruit(orange_shape, (248, 156, 44), (216, 104, 22), (255, 216, 140), (168, 84, 18), orange_details), 'orange')


# The lemon: a tilted oval with a tip at each end
def lemon_shape():
    mask = Image.new('L', (S, S), 0)
    draw = ImageDraw.Draw(mask)
    ellipse(draw, 256, 262, 196, 140)
    ellipse(draw, 62, 262, 34, 28)
    ellipse(draw, 450, 262, 34, 28)
    mask = smooth(mask, 24)
    return mask.rotate(24, resample=Image.BICUBIC, center=(CENTER, CENTER))


def lemon_details(image, draw):
    rnd = random.Random(9)
    for _ in range(70):
        x, y = rnd.uniform(120, 390), rnd.uniform(160, 360)
        ellipse(draw, x, y, 2, 2, fill=(226, 190, 40, 255))


save(fruit(lemon_shape, (250, 222, 76), (222, 178, 36), (255, 248, 190), (170, 134, 24), lemon_details), 'lemon')


# The strawberry: wide at the top, a rounded point at the bottom
def strawberry_shape():
    mask, draw = new_mask()
    ellipse(draw, 170, 195, 110, 100)
    ellipse(draw, 342, 195, 110, 100)
    draw.polygon([(66 * K, 225 * K), (446 * K, 225 * K), (290 * K, 465 * K), (222 * K, 465 * K)], fill=255)
    ellipse(draw, 256, 440, 48, 46)
    return smooth(mask, 34)


def strawberry_details(image, draw):
    for row, y in enumerate(range(195, 440, 44)):
        half = 170 * (1 - (y - 170) / 300)
        count = max(1, int(half / 34))
        for i in range(-count, count + 1):
            x = 256 + i * 44 + (22 if row % 2 else 0)
            if abs(x - 256) <= half:
                ellipse(draw, x, y, 6, 10, fill=(250, 222, 110, 255))
    for angle in (-158, -125, -90, -55, -22):
        leaf(draw, 256, 118, 104, angle)
    for angle in (22, 158, 60, 120):
        leaf(draw, 256, 112, 112, angle, color=(104, 176, 84))
    stem(draw, 256, 106, 262, 62, width=12, color=(88, 140, 66))


save(fruit(strawberry_shape, (232, 62, 78), (174, 30, 54), (255, 160, 160), (128, 22, 44), strawberry_details),
     'strawberry')


# The peach: a round fruit with a crease
def peach_shape():
    mask, draw = new_mask()
    ellipse(draw, 212, 276, 158, 176)
    ellipse(draw, 304, 276, 158, 176)
    return smooth(mask, 30)


def peach_details(image, draw):
    draw.arc([150 * K, 104 * K, 310 * K, 440 * K], -82, 62, fill=(220, 110, 96, 255), width=6 * K)
    leaf(draw, 262, 98, 108, -32, color=(104, 170, 86))
    stem(draw, 256, 110, 258, 72, width=11)


save(fruit(peach_shape, (252, 170, 118), (234, 112, 96), (255, 226, 188), (178, 90, 72), peach_details), 'peach')


# The plum
def plum_shape():
    mask, draw = new_mask()
    ellipse(draw, 256, 278, 178, 196)
    return mask


def plum_details(image, draw):
    draw.arc([172 * K, 88 * K, 322 * K, 462 * K], -80, 70, fill=(78, 42, 112, 255), width=5 * K)
    stem(draw, 254, 100, 276, 36)


save(fruit(plum_shape, (128, 76, 170), (84, 44, 124), (200, 164, 230), (60, 32, 92), plum_details), 'plum')


# The kiwi: cut in two, seen from above
def kiwi_shape():
    mask, draw = new_mask()
    ellipse(draw, 256, 256, 214, 196)
    return mask


def kiwi_details(image, draw):
    ellipse(draw, 256, 256, 190, 172, fill=(150, 200, 74, 255))
    ellipse(draw, 256, 256, 142, 128, fill=(178, 220, 96, 255))
    for i in range(32):
        a = 2 * math.pi * i / 32
        draw.line([((256 + 50 * math.cos(a)) * K, (256 + 46 * math.sin(a)) * K),
                   ((256 + 176 * math.cos(a)) * K, (256 + 160 * math.sin(a)) * K)],
                  fill=(206, 234, 136, 255), width=3 * K)
    ellipse(draw, 256, 256, 58, 50, fill=(246, 244, 206, 255))
    for i in range(18):
        a = 2 * math.pi * i / 18
        x, y = 256 + 82 * math.cos(a), 256 + 74 * math.sin(a)
        draw_a = math.degrees(a)
        seed = Image.new('RGBA', (30 * K, 30 * K), (0, 0, 0, 0))
        ImageDraw.Draw(seed).ellipse([9 * K, 4 * K, 21 * K, 26 * K], fill=(44, 32, 30, 255))
        seed = seed.rotate(90 - draw_a, resample=Image.BICUBIC)
        image.alpha_composite(seed, (int(x * K - 15 * K), int(y * K - 15 * K)))


save(fruit(kiwi_shape, (138, 102, 62), (104, 74, 44), (176, 140, 94), (82, 58, 34), kiwi_details), 'kiwi')

# The orchard: a sky, a sun, far hills, two rows of fruit trees and the grass, soft and pale
W, H = 1600, 900
image = Image.new('RGB', (W * 2, H * 2))
draw = ImageDraw.Draw(image)
for y in range(H * 2):
    t = y / (H * 2)
    draw.line([(0, y), (W * 2, y)], fill=(int(186 + 50 * t), int(224 + 22 * t), int(248 - 6 * t)))
draw.ellipse([1240 * 2, 70 * 2, 1400 * 2, 230 * 2], fill=(255, 244, 196))
for cx, cy, r in ((230, 170, 70), (320, 150, 90), (420, 180, 62), (820, 110, 58), (900, 95, 76), (985, 118, 52)):
    draw.ellipse([(cx - r) * 2, (cy - r * 0.55) * 2, (cx + r) * 2, (cy + r * 0.55) * 2], fill=(250, 252, 255))
rnd = random.Random(12)
for cx, cy, rx, ry, color in ((280, 560, 620, 190, (190, 224, 176)), (1300, 570, 680, 210, (178, 216, 168))):
    draw.ellipse([(cx - rx) * 2, (cy - ry) * 2, (cx + rx) * 2, (cy + ry) * 2], fill=color)
draw.rectangle([0, 520 * 2, W * 2, H * 2], fill=(176, 218, 134))
for y in range(520 * 2, H * 2, 2):
    t = (y - 1040) / (H * 2 - 1040)
    draw.line([(0, y), (W * 2, y)], fill=(int(186 - 30 * t), int(224 - 26 * t), int(146 - 34 * t)))


def tree(x, base, scale):
    trunk = 26 * scale
    draw.rounded_rectangle([(x - trunk / 2) * 2, (base - 150 * scale) * 2, (x + trunk / 2) * 2, base * 2],
                           radius=int(8 * scale * 2), fill=(150, 116, 86))
    for dx, dy, r in ((0, -240, 92), (-72, -190, 76), (72, -190, 76), (-40, -150, 66), (40, -150, 66)):
        r2 = r * scale
        cx, cy = x + dx * scale, base + dy * scale
        draw.ellipse([(cx - r2) * 2, (cy - r2) * 2, (cx + r2) * 2, (cy + r2) * 2], fill=(136, 190, 112))
    for _ in range(11):
        a, d = rnd.uniform(0, 2 * math.pi), 118 * scale * math.sqrt(rnd.random())
        cx, cy = x + d * math.cos(a), base - 205 * scale + d * math.sin(a) * 0.75
        r2 = 9 * scale
        color = rnd.choice(((232, 110, 104), (246, 176, 92), (240, 214, 112)))
        draw.ellipse([(cx - r2) * 2, (cy - r2) * 2, (cx + r2) * 2, (cy + r2) * 2], fill=color)


for x in range(90, W + 100, 230):
    tree(x, 560, 0.72)
for x in range(-30, W + 150, 320):
    tree(x, 720, 1.08)
for _ in range(240):
    x, y = rnd.uniform(0, W), rnd.uniform(600, H)
    draw.line([(x * 2, y * 2), ((x + rnd.uniform(-4, 4)) * 2, (y - rnd.uniform(6, 14)) * 2)], fill=(142, 192, 106),
              width=3)
image = image.filter(ImageFilter.GaussianBlur(3))
image = Image.blend(image, Image.new('RGB', image.size, (250, 250, 240)), 0.18)
image.resize((W, H), Image.LANCZOS).save(os.path.join(out, 'orchard.png'))
print('ok')
