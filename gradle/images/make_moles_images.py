"""Draws the pictures of the moles game (gazeplay-games: data/whackmole/images), made for GazePlay Learning in November
2026 to replace pictures of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).
- field.png (1600 x 900): a soft meadow under a sky, pale so that the targets stand out.
- hole.png (600 x 252): a hole, seen from above at an angle; the game puts the targets in it.
- mole.png (512 x 512): a mole coming out of the ground, cut at the bottom by the edge of its hole.

python make_moles_images.py <data/whackmole folder>
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter

out = os.path.join(sys.argv[1], 'images')
os.makedirs(out, exist_ok=True)

# The meadow
W, H = 1600, 900
image = Image.new('RGB', (W * 2, H * 2))
draw = ImageDraw.Draw(image)
for y in range(H * 2):
    t = y / (H * 2)
    draw.line([(0, y), (W * 2, y)], fill=(int(190 + 46 * t), int(226 + 20 * t), int(248 - 6 * t)))
for cx, cy, r in ((260, 110, 70), (350, 92, 92), (450, 118, 60), (1050, 78, 58), (1130, 62, 78), (1215, 84, 52)):
    draw.ellipse([(cx - r) * 2, (cy - r * 0.5) * 2, (cx + r) * 2, (cy + r * 0.5) * 2], fill=(250, 252, 255))
draw.ellipse([1330 * 2, 40 * 2, 1450 * 2, 160 * 2], fill=(255, 244, 196))
for cx, cy, rx, ry, color in ((300, 370, 720, 180, (186, 224, 160)), (1320, 380, 760, 200, (176, 218, 150))):
    draw.ellipse([(cx - rx) * 2, (cy - ry) * 2, (cx + rx) * 2, (cy + ry) * 2], fill=color)
for y in range(290 * 2, H * 2):
    t = (y - 580) / (H * 2 - 580)
    draw.line([(0, y), (W * 2, y)], fill=(int(176 - 30 * t), int(216 - 22 * t), int(134 - 28 * t)))
rnd = random.Random(21)
for _ in range(520):
    x, y = rnd.uniform(0, W), rnd.uniform(320, H)
    size = 5 + 12 * (y - 320) / (H - 320)
    draw.line([(x * 2, y * 2), ((x + rnd.uniform(-3, 3)) * 2, (y - size) * 2)], fill=(132, 180, 100), width=3)
for _ in range(90):
    x, y = rnd.uniform(0, W), rnd.uniform(340, H)
    r = 2 + 4 * (y - 320) / (H - 320)
    color = rnd.choice(((250, 250, 245), (250, 224, 120), (240, 170, 190)))
    draw.ellipse([(x - r) * 2, (y - r) * 2, (x + r) * 2, (y + r) * 2], fill=color)
image = image.filter(ImageFilter.GaussianBlur(2))
image = Image.blend(image, Image.new('RGB', image.size, (250, 250, 240)), 0.12)
image.resize((W, H), Image.LANCZOS).save(os.path.join(out, 'field.png'))

# The hole: a rim of earth, then the dark inside (the game clips the targets on the same inner ellipse)
K = 3
HW, HH = 600, 252
hole = Image.new('RGBA', (HW * K, HH * K), (0, 0, 0, 0))
draw = ImageDraw.Draw(hole)
draw.ellipse([4 * K, 4 * K, (HW - 4) * K, (HH - 4) * K], fill=(150, 112, 78, 255), outline=(112, 80, 54, 255),
             width=4 * K)
draw.ellipse([30 * K, 18 * K, (HW - 30) * K, (HH - 28) * K], fill=(176, 136, 98, 255))
draw.ellipse([HW * K * 0.10, HH * K * 0.13, HW * K * 0.90, HH * K * 0.87], fill=(66, 44, 32, 255))
for _ in range(36):
    a = rnd.uniform(0, 2 * math.pi)
    x = HW / 2 + (HW * 0.455) * math.cos(a)
    y = HH / 2 + (HH * 0.44) * math.sin(a)
    r = rnd.uniform(3, 8)
    draw.ellipse([(x - r) * K, (y - r * 0.7) * K, (x + r) * K, (y + r * 0.7) * K], fill=(128, 94, 66, 255))
hole.resize((HW, HH), Image.LANCZOS).save(os.path.join(out, 'hole.png'))

# The mole
SIZE = 512
K = 3
S = SIZE * K


def ellipse(d, cx, cy, rx, ry, fill):
    d.ellipse([(cx - rx) * K, (cy - ry) * K, (cx + rx) * K, (cy + ry) * K], fill=fill)


mask = Image.new('L', (S, S), 0)
d = ImageDraw.Draw(mask)
ellipse(d, 256, 225, 165, 175, 255)
d.rectangle([91 * K, 225 * K, 421 * K, 512 * K], fill=255)
base = (126, 96, 82)
body = Image.new('RGB', (S, S), base)
moved = ImageChops.offset(mask, -26 * K, -26 * K)
shadow = ImageChops.subtract(mask, moved).filter(ImageFilter.GaussianBlur(24 * K))
body = Image.composite(Image.new('RGB', (S, S), (86, 62, 54)), body, shadow)
glow = Image.new('L', (S, S), 0)
ImageDraw.Draw(glow).ellipse([130 * K, 66 * K, 280 * K, 200 * K], fill=120)
glow = glow.filter(ImageFilter.GaussianBlur(26 * K))
body = Image.composite(Image.new('RGB', (S, S), (182, 150, 134)), body, glow)
mole = Image.new('RGBA', (S, S), (0, 0, 0, 0))
border = mask.filter(ImageFilter.GaussianBlur(10 * K)).point(lambda v: 255 if v > 8 else 0)
mole.paste((255, 255, 255, 255), (0, 0), border)
line = mask.filter(ImageFilter.GaussianBlur(3 * K)).point(lambda v: 255 if v > 20 else 0)
mole.paste((70, 50, 44, 255), (0, 0), line)
inner = mask.filter(ImageFilter.GaussianBlur(3 * K)).point(lambda v: 255 if v > 200 else 0)
mole.paste(body, (0, 0), inner)
d = ImageDraw.Draw(mole)
# The belly, then the muzzle
ellipse(d, 256, 420, 112, 130, (206, 180, 160, 255))
ellipse(d, 256, 262, 88, 66, (232, 208, 190, 255))
# The eyes
for x in (190, 322):
    ellipse(d, x, 182, 27, 31, (34, 26, 26, 255))
    ellipse(d, x + 9, 170, 9, 10, (255, 255, 255, 255))
# The nose, the mouth, the teeth
ellipse(d, 256, 234, 26, 19, (232, 118, 130, 255))
ellipse(d, 249, 228, 7, 5, (250, 190, 196, 255))
d.line([(256 * K, 253 * K), (256 * K, 276 * K)], fill=(100, 72, 64, 255), width=4 * K)
d.arc([206 * K, 244 * K, 256 * K, 292 * K], 20, 160, fill=(100, 72, 64, 255), width=4 * K)
d.arc([256 * K, 244 * K, 306 * K, 292 * K], 20, 160, fill=(100, 72, 64, 255), width=4 * K)
d.rounded_rectangle([241 * K, 288 * K, 271 * K, 318 * K], radius=4 * K, fill=(255, 255, 250, 255),
                    outline=(120, 96, 88, 255), width=2 * K)
d.line([(256 * K, 289 * K), (256 * K, 317 * K)], fill=(160, 140, 130, 255), width=2 * K)
# The whiskers
for side in (-1, 1):
    for dy in (-14, 2, 18):
        d.line([((256 + side * 78) * K, (258 + dy // 2) * K), ((256 + side * 138) * K, (250 + dy) * K)],
               fill=(100, 72, 64, 255), width=3 * K)
# The paws, resting on the edge of the hole
for x in (138, 374):
    ellipse(d, x, 408, 46, 40, (232, 176, 160, 255))
    for dx in (-26, 0, 26):
        ellipse(d, x + dx, 378, 11, 15, (250, 240, 228, 255))
square = mole.resize((SIZE, SIZE), Image.LANCZOS)
square.save(os.path.join(out, 'mole.png'))
print('ok')
