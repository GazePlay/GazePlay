"""Draws the pictures of Biboule Jump (gazeplay-games: data/biboulejump), made for GazePlay Learning in November 2026 to
replace pictures of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).
- sky.png (1600 x 900): a soft sky with a sun and far clouds, pale so that the clouds to land on stand out.
- clouds/cloud1.png, cloud2.png, cloud3.png (600 x 200): round white clouds with a shaded bottom and a thin outline.
- bouncepad.png (300 x 150): a red springboard on two springs.

python make_biboulejump_images.py <data/biboulejump folder>
Pillow is required.
"""
import os
import random
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter

out = sys.argv[1]
os.makedirs(os.path.join(out, 'clouds'), exist_ok=True)
K = 3

# The sky
W, H = 1600, 900
sky = Image.new('RGB', (W, H))
draw = ImageDraw.Draw(sky)
for y in range(H):
    t = y / H
    draw.line([(0, y), (W, y)], fill=(int(128 + 92 * t), int(198 + 46 * t), int(242 + 10 * t)))
glow = Image.new('L', (W, H), 0)
ImageDraw.Draw(glow).ellipse([1170, 20, 1490, 340], fill=150)
sky.paste((255, 246, 205), (0, 0), glow.filter(ImageFilter.GaussianBlur(50)))
ImageDraw.Draw(sky).ellipse([1265, 115, 1395, 245], fill=(255, 240, 176))
rnd = random.Random(8)
far = Image.new('L', (W, H), 0)
d = ImageDraw.Draw(far)
for _ in range(9):
    cx, cy = rnd.uniform(0, W), rnd.uniform(90, H - 60)
    for _ in range(6):
        x, y = cx + rnd.uniform(-90, 90), cy + rnd.uniform(-14, 14)
        r = rnd.uniform(24, 50)
        d.ellipse([x - r, y - r * 0.6, x + r, y + r * 0.6], fill=105)
sky.paste((255, 255, 255), (0, 0), far.filter(ImageFilter.GaussianBlur(7)))
sky.save(os.path.join(out, 'sky.png'))

# The clouds
CW, CH = 600, 200


def cloud(seed):
    rnd = random.Random(seed)
    S = (CW * K, CH * K)
    mask = Image.new('L', S, 0)
    d = ImageDraw.Draw(mask)
    d.rounded_rectangle([40 * K, 86 * K, (CW - 40) * K, 176 * K], radius=44 * K, fill=255)
    x = 62
    while x < CW - 60:
        r = rnd.uniform(46, 74) * (1 - abs(x - CW / 2) / CW * 0.8)
        y = 112 - rnd.uniform(0, 18)
        d.ellipse([(x - r) * K, (y - r) * K, (x + r) * K, (y + r) * K], fill=255)
        x += r * rnd.uniform(1.0, 1.25)
    mask = mask.filter(ImageFilter.GaussianBlur(1.5 * K)).point(lambda v: 255 if v > 128 else 0)
    # A shadow at the bottom, inside the cloud
    moved = ImageChops.offset(mask, 0, -24 * K)
    shadow = ImageChops.subtract(mask, moved).filter(ImageFilter.GaussianBlur(9 * K))
    body = Image.composite(Image.new('RGB', S, (176, 196, 232)), Image.new('RGB', S, (255, 255, 255)), shadow)
    result = Image.new('RGBA', S, (0, 0, 0, 0))
    outline = mask.filter(ImageFilter.GaussianBlur(2.2 * K)).point(lambda v: 255 if v > 12 else 0)
    result.paste((122, 150, 204, 255), (0, 0), outline)
    result.paste(body, (0, 0), mask)
    return result.resize((CW, CH), Image.LANCZOS)


for n in (1, 2, 3):
    cloud(n * 13).save(os.path.join(out, 'clouds', 'cloud%d.png' % n))

# The springboard: the game puts its bottom on the cloud
PW, PH = 300, 150
pad = Image.new('RGBA', (PW * K, PH * K), (0, 0, 0, 0))
d = ImageDraw.Draw(pad)
for cx in (86, 214):
    for i in range(4):
        y = 66 + i * 18
        d.arc([(cx - 34) * K, y * K, (cx + 34) * K, (y + 24) * K], 0, 360, fill=(84, 96, 122, 255), width=6 * K)
d.rounded_rectangle([18 * K, 34 * K, (PW - 18) * K, 74 * K], radius=20 * K, fill=(222, 66, 78, 255),
                    outline=(136, 34, 52, 255), width=5 * K)
d.rounded_rectangle([40 * K, 42 * K, (PW - 40) * K, 52 * K], radius=5 * K, fill=(250, 150, 150, 255))
pad.resize((PW, PH), Image.LANCZOS).save(os.path.join(out, 'bouncepad.png'))
print('ok')
