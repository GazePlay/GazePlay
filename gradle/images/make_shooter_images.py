"""Draws the pictures of the shooting game (gazeplay-games: data/shooter/biboule/images and data/shooter/robot/images),
made for GazePlay Learning in November 2026 to replace pictures whose author was not known for sure (maintainer's
decision: redrawn). Same license as GazePlay Learning (GPL-3.0-or-later).
- biboule/images/Blue.png to Red.png (500 x 400): the Biboule, mascot of the laboratory, modernized on 3/11/2026
  (maintainer's choice: soft shading, thin outline, rosy cheeks, with the mouth and the wings of the original drawing;
  the original pictures are kept in gradle/images/biboule-originale).
- Background.png (1600 x 900): the classroom, the street.
- robot/images/Blue.png to Red.png (500 x 400): a robot of each color, the target changing color on its way.
- Flash.png (500 x 400): the target hit.
- Cage.png: the bird cage of the Biboules (300 x 400), the flying saucer of the robots (480 x 260).
- hand.png, handShot.png (360 x 2088): the magic wand held by an arm, or the gun, from the bottom of the picture to its
  top; the game centers the picture on the bottom of the screen and turns it around that point, so only its top half
  shows. handShot: with the sparkle or the flame of the shot.

python make_shooter_images.py <data/shooter folder>
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

out = sys.argv[1]
W, H, K = 1600, 900, 2


def s(*values):
    return [v * K for v in values]


def canvas():
    image = Image.new('RGB', (W * K, H * K))
    return image, ImageDraw.Draw(image)


def finish(image, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.resize((W, H), Image.LANCZOS).save(path)


# The classroom: a wall, a blackboard, windows, a floor of boards and two rows of desks (seen from the back)
image, draw = canvas()
draw.rectangle(s(0, 0, W, 560), fill=(246, 222, 178))
for x in range(0, W, 80):
    draw.rectangle(s(x, 0, x + 40, 560), fill=(240, 214, 168))
draw.rectangle(s(0, 540, W, 575), fill=(190, 140, 90))
draw.rectangle(s(0, 575, W, H), fill=(196, 150, 100))
for y in range(600, H, 60):
    draw.line(s(0, y, W, y), fill=(176, 130, 84), width=3 * K)
draw.rounded_rectangle(s(470, 70, 1130, 400), radius=12 * K, fill=(48, 92, 68), outline=(150, 105, 60),
                       width=16 * K)
draw.rectangle(s(560, 400, 1040, 418), fill=(150, 105, 60))
for x, y, w in ((540, 140, 180), (540, 190, 260), (540, 240, 140)):
    draw.line(s(x, y, x + w, y), fill=(225, 235, 225), width=6 * K)
for x in (120, 1300):
    draw.rectangle(s(x, 90, x + 200, 330), fill=(170, 220, 245), outline=(255, 255, 255), width=12 * K)
    draw.line(s(x + 100, 90, x + 100, 330), fill=(255, 255, 255), width=8 * K)
    draw.line(s(x, 210, x + 200, 210), fill=(255, 255, 255), width=8 * K)
rnd = random.Random(3)
for row, (top, scale) in enumerate(((620, 1.0), (760, 1.25))):
    for i in range(4):
        width = 260 * scale
        x = 120 + i * 360 * scale - (row * 120)
        draw.rectangle(s(x, top, x + width, top + 30 * scale), fill=(214, 160, 96), outline=(150, 105, 60),
                       width=4 * K)
        draw.rectangle(s(x + 20, top + 30 * scale, x + 34, top + 110 * scale), fill=(120, 120, 130))
        draw.rectangle(s(x + width - 34, top + 30 * scale, x + width - 20, top + 110 * scale),
                       fill=(120, 120, 130))
        color = rnd.choice([(230, 90, 90), (90, 150, 230), (250, 200, 70), (110, 190, 110)])
        draw.rectangle(s(x + 60, top - 26 * scale, x + 120, top), fill=color)
finish(image, os.path.join(out, 'biboule', 'images', 'Background.png'))

# The street: a sky, houses on both sides, a road going away
image, draw = canvas()
for y in range(500 * K):
    t = y / (500 * K)
    draw.line([(0, y), (W * K, y)], fill=(int(140 + 60 * t), int(170 + 50 * t), int(235 + 10 * t)))
for cx, cy in ((260, 120), (900, 80), (1350, 150)):
    for dx, dy, r in ((-60, 10, 45), (0, -10, 60), (60, 10, 45)):
        draw.ellipse(s(cx + dx - r, cy + dy - r, cx + dx + r, cy + dy + r), fill=(255, 255, 255))
draw.rectangle(s(0, 500, W, H), fill=(140, 190, 110))
draw.polygon(s(700, 500, 900, 500, 1350, H, 250, H), fill=(110, 110, 120))
for i in range(6):
    t0 = i / 6
    t1 = t0 + 0.06
    y0 = 500 + (H - 500) * t0 ** 1.4
    y1 = 500 + (H - 500) * t1 ** 1.4
    w0 = 4 + 14 * t0
    w1 = 4 + 14 * t1
    draw.polygon(s(800 - w0, y0, 800 + w0, y0, 800 + w1, y1, 800 - w1, y1), fill=(250, 250, 240))
rnd = random.Random(5)
colors = [(240, 160, 120), (250, 210, 120), (160, 200, 230), (200, 170, 220), (170, 220, 170)]
for side in (-1, 1):
    for i in range(5):
        width = 260 - i * 40
        height = 360 - i * 50
        base = 520 + (4 - i) * 70
        x = 800 + side * (330 + i * 110) - (width if side < 0 else 0)
        x = max(-40, min(W + 40 - width, x)) if i < 4 else x
        left = 800 - 330 - (4 - i) * 150 - width if side < 0 else 800 + 330 + (4 - i) * 150
        color = colors[(i + (side > 0)) % len(colors)]
        draw.rectangle(s(left, base - height, left + width, base), fill=color, outline=(90, 80, 80),
                       width=4 * K)
        draw.polygon(s(left - 15, base - height, left + width + 15, base - height, left + width / 2,
                       base - height - 70), fill=(190, 80, 70), outline=(90, 60, 50))
        for wx in (0.18, 0.58):
            for wy in (0.2, 0.5):
                draw.rectangle(s(left + width * wx, base - height + height * wy, left + width * (wx + 0.24),
                                 base - height + height * (wy + 0.18)), fill=(200, 230, 250),
                               outline=(255, 255, 255), width=3 * K)
        draw.rectangle(s(left + width * 0.4, base - height * 0.22, left + width * 0.6, base), fill=(140, 90, 60))
finish(image, os.path.join(out, 'robot', 'images', 'Background.png'))

COLORS = {'Blue': (110, 180, 245), 'Green': (130, 210, 120), 'Yellow': (250, 215, 80), 'Orange': (250, 160, 80),
          'Red': (240, 105, 110)}


def sprite(width, height):
    picture = Image.new('RGBA', (width * K, height * K), (0, 0, 0, 0))
    return picture, ImageDraw.Draw(picture)


def save(picture, width, height, path):
    picture.resize((width, height), Image.LANCZOS).save(path)


def lighter(color, amount=0.45):
    return tuple(int(c + (255 - c) * amount) for c in color[:3]) + (255,)


def robot(color):
    """A friendly robot head: antenna, eyes and mouth of the color, ears."""
    picture, draw = sprite(500, 400)
    grey, dark, ear = (185, 190, 200, 255), (90, 95, 110, 255), (160, 165, 178, 255)
    draw.line(s(250, 95, 250, 45), fill=dark, width=8 * K)
    draw.ellipse(s(228, 18, 272, 62), fill=color + (255,), outline=dark, width=4 * K)
    for x in (95, 405):
        draw.rounded_rectangle(s(x - 30, 170, x + 30, 270), radius=14 * K, fill=ear, outline=dark, width=4 * K)
    draw.rounded_rectangle(s(120, 95, 380, 345), radius=40 * K, fill=grey, outline=dark, width=6 * K)
    for ex in (195, 305):
        draw.ellipse(s(ex - 42, 160, ex + 42, 244), fill=(255, 255, 255, 255), outline=dark, width=4 * K)
        draw.ellipse(s(ex - 26, 176, ex + 26, 228), fill=color + (255,))
        draw.ellipse(s(ex - 12, 184, ex - 2, 194), fill=(255, 255, 255, 255))
    draw.rounded_rectangle(s(185, 270, 315, 312), radius=10 * K, fill=dark)
    for x in range(205, 305, 22):
        draw.rectangle(s(x, 276, x + 12, 306), fill=color + (255,))
    return picture


INK = (30, 28, 40, 255)

# The Biboule of each color: its body and the lower lip of its mouth (the colors of the lips are the original ones)
BIBOULES = {'Blue': ((150, 210, 245), (160, 235, 150)), 'Green': ((175, 230, 140), (244, 181, 154)),
            'Yellow': ((250, 235, 130), (244, 154, 193)), 'Orange': ((250, 190, 140), (240, 154, 230)),
            'Red': ((245, 150, 160), (205, 154, 244))}


def leaf_wing(draw, cx, cy, rx, ry, angle):
    """The wing of the original: an oval leaf a little wider at its base, a dark blue outline, a central vein and side
    veins that alternate, pointing to the tip."""
    fill, line = (125, 205, 200, 255), (25, 60, 130, 255)
    ca, sa = math.cos(angle), math.sin(angle)

    def point(u, v):
        return (cx + u * ca - v * sa) * K, (cy + u * sa + v * ca) * K

    def half_width(u):
        t = math.acos(max(-1.0, min(1.0, u / rx)))
        return ry * math.sin(t) * (1 - 0.12 * math.cos(t))

    outline = []
    for i in range(90):
        t = 2 * math.pi * i / 90
        outline.append(point(rx * math.cos(t), ry * math.sin(t) * (1 - 0.12 * math.cos(t))))
    draw.polygon(outline, fill=fill)
    draw.line(outline + [outline[0]], fill=line, width=4 * K, joint='curve')
    draw.line([point(-0.6 * rx, 0), point(0.72 * rx, 0)], fill=line, width=3 * K)
    for u0, side in ((-0.35, 1), (-0.15, -1), (0.1, 1), (0.3, -1), (0.48, 1)):
        u = u0 * rx
        length = 0.55 * half_width(u + 0.25 * rx)
        draw.line([point(u, 0), point(u + 1.1 * length, side * length)], fill=line, width=3 * K)


def fluffy(cx, cy, r):
    """The outline of a fluffy ball: twelve round puffs all around."""
    points = []
    for i in range(720):
        a = math.radians(i / 2)
        phase = (12 * a / (2 * math.pi)) % 1
        rr = r * (1 + 0.07 * math.sqrt(max(0.0, 1 - (2 * phase - 1) ** 2)))
        points.append(((cx + rr * math.cos(a)) * K, (cy + rr * math.sin(a) * 0.88) * K))
    return points


def biboule_mouth(draw, cx, top, lip):
    """The mouth of the original: two small smiles meeting in a point in the middle (a "3" lying down), over a round
    lower lip filled with a color."""
    a, depth = 25, 17
    left = []
    for i in range(41):  # the lower half of an oval, from the outer end (a little raised) to the point in the middle
        t = i / 40
        left.append((cx - a + a * math.cos(math.pi - math.pi * t),
                     top + depth * math.sin(math.pi * t) - 4 * (1 - t) ** 2 - 14 * t ** 4))
    right = [(2 * cx - x, y) for x, y in reversed(left)]
    start = 17  # the lip starts on each smile, a little before its lowest point
    lx, ly = left[start]
    bottom = [(cx + (cx - lx) * math.cos(math.radians(d)), ly + 25 * math.sin(math.radians(d)))
              for d in range(0, 181, 4)]
    draw.polygon([(x * K, y * K) for x, y in bottom + left[start:] + right[1:len(right) - start]], fill=lip)
    for curve in (bottom, left, right):
        draw.line([(x * K, y * K) for x, y in curve], fill=INK, width=4 * K, joint='curve')
    for x, y in (left[0], right[-1], left[-1]):
        draw.ellipse(s(x - 2, y - 2, x + 2, y + 2), fill=INK)


def biboule(color, lip):
    """The Biboule: a fluffy ball with a thin outline of its color, a soft light and shadow, big eyes, rosy cheeks, the
    mouth and the leaf wings of the original drawing."""
    picture, draw = sprite(500, 400)
    leaf_wing(draw, 140, 110, 100, 48, math.radians(-140))
    leaf_wing(draw, 360, 110, 100, 48, math.radians(-40))
    draw.polygon(fluffy(250, 225, 128), fill=tuple(int(c * 0.78) for c in color) + (255,))
    draw.polygon(fluffy(250, 225, 122), fill=color + (255,))
    mask = Image.new('L', picture.size, 0)
    ImageDraw.Draw(mask).polygon(fluffy(250, 225, 122), fill=255)
    light = Image.new('RGBA', picture.size, (0, 0, 0, 0))
    shading = ImageDraw.Draw(light)
    shading.ellipse(s(140, 109, 274, 219), fill=(255, 255, 255, 110))
    shading.ellipse(s(226, 255, 384, 371), fill=tuple(int(c * 0.75) for c in color) + (90,))
    light = light.filter(ImageFilter.GaussianBlur(28 * K))
    picture.paste(light, (0, 0), Image.composite(light, Image.new('RGBA', picture.size, (0, 0, 0, 0)), mask))
    draw = ImageDraw.Draw(picture)
    for ex in (207, 293):
        draw.ellipse(s(ex - 34, 159, ex + 34, 251), fill=INK)
        draw.ellipse(s(ex - 19, 173, ex - 2, 196), fill=(255, 255, 255, 255))
        draw.ellipse(s(ex + 5, 217, ex + 15, 228), fill=(255, 255, 255, 255))
    for cx in (163, 337):
        draw.ellipse(s(cx - 20, 250, cx + 20, 268), fill=(255, 140, 160, 120))
    biboule_mouth(draw, 250, 260, lip + (255,))
    return picture


def flash(fill, outline):
    """A burst of light: the target hit."""
    picture, draw = sprite(500, 400)
    points = []
    for i in range(24):
        radius = 190 if i % 2 == 0 else 110
        angle = i * math.pi / 12
        points.append(((250 + radius * math.cos(angle) * 1.2) * K, (200 + radius * math.sin(angle)) * K))
    draw.polygon(points, fill=fill, outline=outline)
    draw.ellipse(s(170, 130, 330, 270), fill=lighter(fill, 0.6))
    return picture


def cage():
    """A golden bird cage, its little door open."""
    picture, draw = sprite(300, 400)
    gold, dark = (240, 200, 90, 255), (170, 125, 40, 255)
    draw.ellipse(s(132, 5, 168, 41), outline=dark, width=6 * K)
    draw.chord(s(30, 40, 270, 230), 180, 360, fill=(255, 245, 210, 90), outline=dark, width=6 * K)
    for x in range(50, 260, 30):
        top = 135 - math.sqrt(max(0, 120 ** 2 - (x - 150) ** 2)) * 0.79
        draw.line(s(x, top, x, 360), fill=gold, width=6 * K)
    draw.rectangle(s(30, 135, 270, 360), outline=dark, width=6 * K)
    draw.line(s(30, 250, 270, 250), fill=dark, width=5 * K)
    draw.rounded_rectangle(s(15, 355, 285, 390), radius=10 * K, fill=dark)
    draw.rectangle(s(205, 260, 270, 350), fill=(255, 245, 210, 160), outline=dark, width=4 * K)
    return picture


def saucer():
    """A flying saucer with its glass dome and colored lights."""
    picture, draw = sprite(480, 260)
    dark = (80, 85, 100, 255)
    for x in (130, 240, 350):
        draw.line(s(x, 170, x - 40 + (x - 240) * 0.2, 245), fill=dark, width=6 * K)
    draw.ellipse(s(150, 25, 330, 135), fill=(120, 190, 245, 230), outline=dark, width=5 * K)
    draw.ellipse(s(185, 45, 225, 70), fill=(230, 245, 255, 255))
    draw.ellipse(s(15, 95, 465, 195), fill=(175, 180, 195, 255), outline=dark, width=6 * K)
    draw.ellipse(s(60, 115, 420, 165), fill=(150, 155, 172, 255))
    for i, x in enumerate(range(70, 430, 60)):
        color = list(COLORS.values())[i % 5]
        draw.ellipse(s(x - 14, 135, x + 14, 160), fill=color + (255,), outline=dark, width=3 * K)
    return picture


def wand(shot):
    """An arm from the bottom, its fist, and the magic wand up to the top (with a sparkle when it shoots)."""
    picture, draw = sprite(360, 2088)
    skin, skin_dark = (245, 205, 160, 255), (190, 140, 100, 255)
    draw.rounded_rectangle(s(110, 1150, 250, 2088), radius=60 * K, fill=skin, outline=skin_dark, width=6 * K)
    draw.rounded_rectangle(s(95, 980, 265, 1200), radius=70 * K, fill=skin, outline=skin_dark, width=6 * K)
    for y in (1035, 1085, 1135):
        draw.line(s(130, y, 230, y), fill=skin_dark, width=5 * K)
    draw.polygon(s(168, 1000, 192, 1000, 186, 160, 174, 160), fill=(110, 70, 40, 255), outline=(70, 40, 20, 255))
    draw.rounded_rectangle(s(166, 120, 194, 185), radius=10 * K, fill=(255, 255, 255, 255),
                           outline=(70, 40, 20, 255), width=4 * K)
    if shot:
        for i in range(10):
            angle = i * math.pi / 5
            radius = 110 if i % 2 == 0 else 45
            draw.line(s(180, 140, 180 + radius * math.cos(angle), 140 + radius * math.sin(angle)),
                      fill=(255, 220, 90, 255), width=10 * K)
        draw.ellipse(s(140, 100, 220, 180), fill=(255, 250, 200, 255))
    return picture


def gun(shot):
    """A toy blaster from the bottom up to the top (with a flame when it shoots)."""
    picture, draw = sprite(360, 2088)
    dark, body = (70, 75, 90, 255), (110, 120, 140, 255)
    draw.rounded_rectangle(s(110, 1000, 250, 2088), radius=40 * K, fill=body, outline=dark, width=6 * K)
    draw.rounded_rectangle(s(140, 300, 220, 1050), radius=30 * K, fill=body, outline=dark, width=6 * K)
    for y in (420, 620, 820):
        draw.rectangle(s(140, y, 220, y + 40), fill=(240, 105, 110, 255))
    draw.rounded_rectangle(s(125, 240, 235, 320), radius=20 * K, fill=dark)
    draw.ellipse(s(150, 1100, 210, 1160), fill=(110, 180, 245, 255), outline=dark, width=4 * K)
    if shot:
        draw.polygon(s(180, 20, 240, 150, 210, 140, 230, 250, 180, 200, 130, 250, 150, 140, 120, 150),
                     fill=(255, 140, 60, 255))
        draw.polygon(s(180, 90, 210, 180, 180, 160, 150, 180), fill=(255, 230, 120, 255))
    return picture


biboules = os.path.join(out, 'biboule', 'images')
robots = os.path.join(out, 'robot', 'images')
for name, color in COLORS.items():
    save(robot(color), 500, 400, os.path.join(robots, name + '.png'))
for name, (color, lip) in BIBOULES.items():
    save(biboule(color, lip), 500, 400, os.path.join(biboules, name + '.png'))
save(flash((255, 245, 190, 255), (240, 190, 90, 255)), 500, 400, os.path.join(biboules, 'Flash.png'))
save(flash((255, 120, 110, 255), (200, 60, 60, 255)), 500, 400, os.path.join(robots, 'Flash.png'))
save(cage(), 300, 400, os.path.join(biboules, 'Cage.png'))
save(saucer(), 480, 260, os.path.join(robots, 'Cage.png'))
save(wand(False), 360, 2088, os.path.join(biboules, 'hand.png'))
save(wand(True), 360, 2088, os.path.join(biboules, 'handShot.png'))
save(gun(False), 360, 2088, os.path.join(robots, 'hand.png'))
save(gun(True), 360, 2088, os.path.join(robots, 'handShot.png'))
print('ok')
