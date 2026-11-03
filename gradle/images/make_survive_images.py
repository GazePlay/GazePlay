"""Draws the pictures of "Survive Against Robots" (gazeplay-games: data/surviveAgainstRobots/images), made for
GazePlay Learning on 3/11/2026 to replace pictures whose source was not known (maintainer's decision: the game
redrawn in the style of the shooting game, whose Biboule and robots it uses). Same license as GazePlay Learning
(GPL-3.0-or-later).
- sky.png, garden.png, schoolyard.png (1600 x 900): the three scenes, one drawn at random for each game (maintainer's
  choice): the sky with round clouds, a garden seen from above, the schoolyard seen from above. Soft colors, nothing
  strong under the lives and the score at the top.
- fire.png, shield.png, slow.png, heart.png (300 x 300): the bonuses (faster shots, shield, robots slowed, one life
  back), each on a white round badge with an outline of its color.

- With two more arguments, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/surviveAgainstRobots.png,
  960 x 540), made of these pictures and of those of the shooting game.

python make_survive_images.py <data/surviveAgainstRobots/images folder> [<data/shooter folder> <thumbnail.png>]
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
W, H, K = 1600, 900, 2


def s(*values):
    return [v * K for v in values]


def sky():
    """The sky: a soft gradient, round clouds (as in the shooting game) and a few birds far away."""
    image = Image.new('RGB', (W * K, H * K))
    draw = ImageDraw.Draw(image)
    for y in range(H * K):
        t = y / (H * K)
        draw.line([(0, y), (W * K, y)], fill=(int(150 + 70 * t), int(200 + 40 * t), int(245 + 8 * t)))
    for cx, cy, scale in ((300, 230, 1.0), (850, 170, 0.8), (1330, 260, 1.1), (420, 640, 0.9), (1130, 720, 1.0),
                          (1480, 560, 0.7), (760, 470, 0.6)):
        for dx, dy, r in ((-70, 12, 50), (0, -12, 68), (70, 12, 50), (-25, 25, 45), (35, 25, 48)):
            draw.ellipse(s(cx + (dx - r) * scale, cy + (dy - r) * scale, cx + (dx + r) * scale,
                           cy + (dy + r) * scale), fill=(255, 255, 255))
        draw.line(s(cx - 110 * scale, cy + 60 * scale, cx + 110 * scale, cy + 60 * scale), fill=(225, 238, 250),
                  width=6 * K)
    rnd = random.Random(4)
    for _ in range(6):
        x, y = rnd.uniform(150, 1450), rnd.uniform(330, 520)
        draw.arc(s(x - 14, y - 6, x, y + 6), 200, 340, fill=(110, 130, 160), width=3 * K)
        draw.arc(s(x, y - 6, x + 14, y + 6), 200, 340, fill=(110, 130, 160), width=3 * K)
    return image


def flower(draw, x, y, color):
    for a in range(5):
        angle = a * 2 * math.pi / 5
        px, py = x + 9 * math.cos(angle), y + 9 * math.sin(angle)
        draw.ellipse(s(px - 6, py - 6, px + 6, py + 6), fill=color)
    draw.ellipse(s(x - 5, y - 5, x + 5, y + 5), fill=(250, 200, 60))


def garden():
    """A garden seen from above: a lawn in soft stripes, a path of flagstones, flower beds and bushes."""
    image = Image.new('RGB', (W * K, H * K), (150, 205, 120))
    draw = ImageDraw.Draw(image)
    for x in range(0, W, 160):
        draw.rectangle(s(x, 0, x + 80, H), fill=(158, 212, 128))
    for i in range(14):
        t = i / 13
        x = 120 + t * 1360
        y = 470 + 170 * math.sin(t * math.pi * 1.6)
        draw.rounded_rectangle(s(x - 45, y - 32, x + 45, y + 32), radius=14 * K, fill=(225, 215, 195),
                               outline=(190, 175, 150), width=3 * K)
    rnd = random.Random(7)
    for cx, cy in ((200, 250), (1400, 250), (230, 790), (1380, 790), (800, 220)):
        draw.ellipse(s(cx - 95, cy - 55, cx + 95, cy + 55), fill=(170, 125, 85), outline=(140, 100, 65),
                     width=4 * K)
        for _ in range(9):
            flower(draw, cx + rnd.uniform(-70, 70), cy + rnd.uniform(-35, 35),
                   rnd.choice([(250, 120, 140), (255, 210, 90), (200, 150, 240), (255, 255, 255)]))
    for cx, cy in ((560, 790), (1060, 230), (1080, 790)):
        for dx, dy, r in ((-30, 0, 45), (30, -5, 48), (0, 25, 44)):
            draw.ellipse(s(cx + dx - r, cy + dy - r, cx + dx + r, cy + dy + r), fill=(110, 170, 95),
                         outline=(85, 140, 75), width=3 * K)
    return image


def schoolyard():
    """The schoolyard seen from above: a soft ground, painted games (hopscotch, circles), a sandpit and trees."""
    image = Image.new('RGB', (W * K, H * K), (196, 200, 210))
    draw = ImageDraw.Draw(image)
    rnd = random.Random(2)
    for _ in range(1200):
        x, y = rnd.uniform(0, W), rnd.uniform(0, H)
        draw.ellipse(s(x - 1.5, y - 1.5, x + 1.5, y + 1.5), fill=(185, 190, 200))
    for x, y in ((180, 330), (180, 410), (140, 490), (220, 490), (180, 570), (140, 650), (220, 650), (180, 730)):
        draw.rectangle(s(x - 38, y - 38, x + 38, y + 38), outline=(255, 255, 255), width=5 * K)
    for r, color in ((150, (250, 200, 90)), (100, (110, 180, 245)), (50, (240, 105, 110))):
        draw.ellipse(s(800 - r, 470 - r, 800 + r, 470 + r), outline=color, width=8 * K)
    draw.rounded_rectangle(s(1230, 580, 1520, 830), radius=30 * K, fill=(245, 225, 160), outline=(200, 160, 100),
                           width=8 * K)
    for cx, cy in ((1400, 270), (520, 230), (560, 790)):
        draw.ellipse(s(cx - 85, cy - 85, cx + 85, cy + 85), fill=(120, 180, 100), outline=(90, 145, 80),
                     width=4 * K)
        draw.ellipse(s(cx - 40, cy - 50, cx + 10, cy), fill=(140, 200, 120))
    draw.line(s(0, H - 25, W, H - 25), fill=(170, 120, 80), width=12 * K)
    return image


def finish(image, name):
    image.resize((W, H), Image.LANCZOS).save(os.path.join(out, name))


finish(sky(), 'sky.png')
finish(garden(), 'garden.png')
finish(schoolyard(), 'schoolyard.png')

B, BK = 300, 4


def b(*values):
    return [v * BK for v in values]


def badge(color):
    picture = Image.new('RGBA', (B * BK, B * BK), (0, 0, 0, 0))
    draw = ImageDraw.Draw(picture)
    draw.ellipse(b(12, 12, 288, 288), fill=(255, 255, 255, 255), outline=color + (255,), width=16 * BK)
    return picture, draw


def fire():
    """Faster shots: a lightning bolt, yellow and orange."""
    picture, draw = badge((245, 150, 50))
    draw.polygon(b(170, 45, 85, 165, 140, 165, 115, 255, 215, 120, 158, 120, 190, 45), fill=(255, 205, 60, 255),
                 outline=(220, 130, 30, 255), width=6 * BK)
    return picture


def shield():
    """The shield: a blue shield with a white star."""
    picture, draw = badge((80, 140, 230))
    draw.polygon(b(150, 50, 235, 85, 225, 170, 150, 250, 75, 170, 65, 85), fill=(110, 170, 245, 255),
                 outline=(50, 100, 190, 255), width=6 * BK)
    points = []
    for i in range(10):
        radius = 42 if i % 2 == 0 else 18
        angle = -math.pi / 2 + i * math.pi / 5
        points.append(((150 + radius * math.cos(angle)) * BK, (145 + radius * math.sin(angle)) * BK))
    draw.polygon(points, fill=(255, 255, 255, 255))
    return picture


def slow():
    """The robots slowed: a snail."""
    picture, draw = badge((120, 190, 90))
    draw.rounded_rectangle(b(55, 185, 245, 225), radius=20 * BK, fill=(170, 220, 130, 255),
                           outline=(90, 150, 70, 255), width=5 * BK)
    draw.line(b(215, 190, 225, 120), fill=(90, 150, 70, 255), width=6 * BK)
    draw.line(b(235, 192, 250, 125), fill=(90, 150, 70, 255), width=6 * BK)
    draw.ellipse(b(215, 108, 237, 130), fill=(60, 60, 70, 255))
    draw.ellipse(b(240, 113, 262, 135), fill=(60, 60, 70, 255))
    draw.ellipse(b(70, 85, 200, 205), fill=(240, 180, 90, 255), outline=(190, 120, 50, 255), width=6 * BK)
    center = (135, 145)
    points = []
    for i in range(80):
        t = i / 79
        angle = t * 3.2 * math.pi
        radius = 52 * (1 - t)
        points.append(((center[0] + radius * math.cos(angle)) * BK, (center[1] + radius * math.sin(angle)) * BK))
    draw.line(points, fill=(190, 120, 50, 255), width=6 * BK, joint='curve')
    return picture


def heart():
    """One life back: a red heart."""
    picture, draw = badge((235, 80, 100))
    draw.ellipse(b(70, 80, 158, 168), fill=(240, 80, 100, 255))
    draw.ellipse(b(142, 80, 230, 168), fill=(240, 80, 100, 255))
    draw.polygon(b(75, 140, 225, 140, 150, 240), fill=(240, 80, 100, 255))
    draw.ellipse(b(92, 98, 122, 124), fill=(255, 180, 190, 255))
    return picture


for name, picture in (('fire.png', fire()), ('shield.png', shield()), ('slow.png', slow()), ('heart.png', heart())):
    picture.resize((B, B), Image.LANCZOS).save(os.path.join(out, name))


def thumbnail(shooter, path):
    """The thumbnail of the menu (960 x 540): the sky, the Biboule shooting sparkles at robots of the shooting game,
    one of them flashing, a red one shooting back, a bonus; made of the pictures of the game."""
    image = Image.open(os.path.join(out, 'sky.png')).convert('RGBA').resize((960, 540), Image.LANCZOS)
    draw = ImageDraw.Draw(image)

    def paste(file, x, y, width, height):
        sprite = Image.open(file).convert('RGBA').resize((width, height), Image.LANCZOS)
        image.alpha_composite(sprite, (int(x - width / 2), int(y - height / 2)))

    paste(os.path.join(out, 'shield.png'), 250, 400, 56, 56)
    paste(os.path.join(shooter, 'robot', 'images', 'Blue.png'), 735, 150, 125, 100)
    paste(os.path.join(shooter, 'robot', 'images', 'Red.png'), 780, 400, 125, 100)
    paste(os.path.join(shooter, 'robot', 'images', 'Flash.png'), 220, 170, 140, 112)
    for x, y in ((540, 225), (600, 200), (660, 178)):
        draw.ellipse((x - 9, y - 9, x + 9, y + 9), fill=(255, 245, 160), outline=(240, 190, 90), width=3)
    for x, y in ((700, 375), (640, 352)):
        draw.ellipse((x - 10, y - 10, x + 10, y + 10), fill=(240, 80, 80), outline=(150, 30, 40), width=3)
    paste(os.path.join(shooter, 'biboule', 'images', 'Yellow.png'), 470, 280, 175, 140)
    image.convert('RGB').save(path)


if len(sys.argv) > 3:
    thumbnail(sys.argv[2], sys.argv[3])
print('ok')
