"""Draws the pictures of "Space Game" (gazeplay-games: data/space/images), made for GazePlay Learning on 3/11/2026 to
replace pictures whose source was not known (maintainer's decision: everything redrawn, in the style of the shooting
game). Same license as GazePlay Learning (GPL-3.0-or-later).
- night.png (1600 x 900): the space, a soft night (blue to violet): stars, planets, a moon (maintainer's choice
  of 3/11/2026, between it and a pastel one).
- ship.png (500 x 400): the spaceship of the player, a round rocket seen from above, its window and flames.
- Blue.png to Red.png (500 x 400): the aliens, in a flying saucer of each color, a little alien under its glass dome.
- boss.png (800 x 500): the boss, a big saucer with angry eyes.

- With one more argument, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/space.png, 960 x 540),
  made of these pictures.

python make_space_images.py <data/space/images folder> [<thumbnail.png>]
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
K = 2
INK = (40, 36, 60, 255)


def s(*values):
    return [v * K for v in values]


def lighter(color, amount=0.45):
    return tuple(int(c + (255 - c) * amount) for c in color[:3]) + (255,)


def darker(color, amount=0.7):
    return tuple(int(c * amount) for c in color[:3]) + (255,)


def space(top, bottom, star, seed):
    """The space: a soft gradient, stars of several sizes, two planets with a ring or spots, a moon."""
    W, H = 1600, 900
    image = Image.new('RGB', (W * K, H * K))
    draw = ImageDraw.Draw(image)
    for y in range(H * K):
        t = y / (H * K)
        draw.line([(0, y), (W * K, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bottom)))
    glow = Image.new('RGBA', image.size, (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse(s(300, 150, 1300, 650), fill=lighter(top, 0.25)[:3] + (60,))
    glow = glow.filter(ImageFilter.GaussianBlur(120 * K))
    image.paste(glow, (0, 0), glow)
    draw = ImageDraw.Draw(image)
    rnd = random.Random(seed)
    for _ in range(260):
        x, y, r = rnd.uniform(0, W), rnd.uniform(0, H), rnd.choice((1.5, 1.5, 2, 2.5, 3))
        draw.ellipse(s(x - r, y - r, x + r, y + r), fill=star)
    for _ in range(14):
        x, y = rnd.uniform(40, W - 40), rnd.uniform(40, H - 40)
        for a in range(4):
            angle = a * math.pi / 2
            draw.line(s(x, y, x + 9 * math.cos(angle), y + 9 * math.sin(angle)), fill=star, width=3 * K)
    # A ringed planet at the top left, a planet with spots at the right, a moon at the bottom
    draw.ellipse(s(130, 110, 330, 310), fill=(250, 190, 120), outline=(220, 150, 90), width=4 * K)
    draw.arc(s(70, 185, 390, 245), 160, 380, fill=(255, 230, 170), width=10 * K)
    draw.ellipse(s(1300, 330, 1460, 490), fill=(150, 200, 250), outline=(110, 160, 220), width=4 * K)
    for x, y, r in ((1350, 380, 18), (1410, 440, 12), (1370, 455, 9)):
        draw.ellipse(s(x - r, y - r, x + r, y + r), fill=(120, 175, 235))
    draw.ellipse(s(560, 720, 660, 820), fill=(235, 235, 245), outline=(200, 200, 215), width=3 * K)
    for x, y, r in ((590, 750, 12), (625, 785, 8)):
        draw.ellipse(s(x - r, y - r, x + r, y + r), fill=(215, 215, 228))
    return image.resize((W, H), Image.LANCZOS)


def sprite(width, height):
    picture = Image.new('RGBA', (width * K, height * K), (0, 0, 0, 0))
    return picture, ImageDraw.Draw(picture)


def ship():
    """The spaceship of the player: a round rocket going up, a window, two fins, two flames."""
    picture, draw = sprite(500, 400)
    body, fin, line = (245, 245, 250, 255), (240, 105, 110, 255), (90, 95, 120, 255)
    for x in (205, 295):
        draw.polygon(s(x - 22, 330, x + 22, 330, x, 395), fill=(255, 190, 80, 255))
        draw.polygon(s(x - 11, 330, x + 11, 330, x, 370), fill=(255, 240, 150, 255))
    draw.polygon(s(150, 330, 190, 210, 200, 330), fill=fin, outline=line)
    draw.polygon(s(350, 330, 310, 210, 300, 330), fill=fin, outline=line)
    draw.polygon(s(150, 330, 110, 290, 120, 350, 200, 330), fill=fin, outline=line)
    draw.polygon(s(350, 330, 390, 290, 380, 350, 300, 330), fill=fin, outline=line)
    draw.ellipse(s(180, 60, 320, 340), fill=body, outline=line, width=5 * K)
    draw.chord(s(180, 60, 320, 340), 200, 340, fill=fin, outline=line, width=5 * K)
    draw.ellipse(s(215, 165, 285, 235), fill=(120, 190, 245, 255), outline=line, width=6 * K)
    draw.ellipse(s(228, 178, 250, 200), fill=(220, 240, 255, 255))
    return picture


def alien(color):
    """An alien in its flying saucer: the saucer of the color, a glass dome, a little alien with an antenna."""
    picture, draw = sprite(500, 400)
    line = darker(color, 0.55)
    skin = (150, 220, 120, 255) if color[1] < 200 or color[0] > 200 else (200, 160, 240, 255)
    draw.ellipse(s(140, 60, 360, 270), fill=(200, 230, 255, 150), outline=(150, 190, 230, 255), width=5 * K)
    draw.line(s(250, 105, 250, 75), fill=darker(skin), width=6 * K)
    draw.ellipse(s(238, 58, 262, 82), fill=(255, 220, 90, 255))
    draw.ellipse(s(195, 100, 305, 215), fill=skin, outline=darker(skin), width=4 * K)
    for x in (225, 275):
        draw.ellipse(s(x - 16, 130, x + 16, 168), fill=INK)
        draw.ellipse(s(x - 9, 136, x + 1, 148), fill=(255, 255, 255, 255))
    draw.arc(s(228, 165, 272, 195), 20, 160, fill=INK, width=4 * K)
    draw.ellipse(s(40, 190, 460, 320), fill=color + (255,), outline=line, width=6 * K)
    draw.ellipse(s(110, 215, 390, 265), fill=lighter(color, 0.35))
    for i, x in enumerate(range(95, 420, 65)):
        draw.ellipse(s(x - 14, 268, x + 14, 292), fill=(255, 240, 150, 255), outline=line, width=3 * K)
    return picture


def boss():
    """The boss: a big dark violet saucer, angry eyes, red lights, spikes."""
    picture, draw = sprite(800, 500)
    color, line = (150, 100, 200), (80, 50, 120, 255)
    for x in (180, 320, 480, 620):
        draw.polygon(s(x - 25, 330, x + 25, 330, x, 440), fill=(110, 70, 160, 255), outline=line)
    draw.ellipse(s(230, 60, 570, 320), fill=(230, 200, 255, 170), outline=(180, 150, 230, 255), width=6 * K)
    draw.ellipse(s(40, 230, 760, 400), fill=color + (255,), outline=line, width=8 * K)
    draw.ellipse(s(140, 260, 660, 330), fill=lighter(color, 0.3))
    # The angry face under the dome
    draw.ellipse(s(300, 110, 500, 300), fill=(240, 120, 130, 255), outline=(170, 60, 80, 255), width=5 * K)
    for x, side in ((360, 1), (440, -1)):
        draw.ellipse(s(x - 24, 170, x + 24, 220), fill=(255, 255, 255, 255))
        draw.ellipse(s(x - 11, 185, x + 11, 210), fill=INK)
        draw.line(s(x - 30 * side, 155, x + 25 * side, 172), fill=INK, width=8 * K)
    draw.arc(s(360, 235, 440, 285), 200, 340, fill=INK, width=6 * K)
    for x in range(110, 700, 85):
        draw.ellipse(s(x - 16, 350, x + 16, 378), fill=(255, 90, 90, 255), outline=line, width=3 * K)
    return picture


def save(picture, width, height, name):
    picture.resize((width, height), Image.LANCZOS).save(os.path.join(out, name))


space((40, 45, 100), (95, 70, 140), (255, 250, 220), 3).save(os.path.join(out, 'night.png'))
save(ship(), 500, 400, 'ship.png')
COLORS = {'Blue': (110, 180, 245), 'Green': (130, 210, 120), 'Yellow': (250, 215, 80), 'Orange': (250, 160, 80),
          'Red': (240, 105, 110)}
for name, color in COLORS.items():
    save(alien(color), 500, 400, name + '.png')
save(boss(), 800, 500, 'boss.png')


def thumbnail(path):
    """The thumbnail of the menu (960 x 540): the night, aliens and the boss at the top, the rocket shooting."""
    image = Image.open(os.path.join(out, 'night.png')).convert('RGBA').resize((960, 540), Image.LANCZOS)
    draw = ImageDraw.Draw(image)

    def paste(name, x, y, width, height):
        picture = Image.open(os.path.join(out, name)).convert('RGBA').resize((width, height), Image.LANCZOS)
        image.alpha_composite(picture, (int(x - width / 2), int(y - height / 2)))

    paste('boss.png', 480, 110, 300, 188)
    for name, x, y in (('Green.png', 150, 150), ('Yellow.png', 800, 140), ('Blue.png', 260, 270),
                       ('Orange.png', 700, 280)):
        paste(name, x, y, 125, 100)
    for y in (380, 320, 260):
        draw.rounded_rectangle((474, y - 18, 486, y + 18), radius=6, fill=(150, 230, 255), outline=(255, 255, 255),
                               width=2)
    draw.ellipse((690, 345, 710, 365), fill=(240, 80, 80), outline=(150, 30, 40), width=3)
    paste('ship.png', 480, 465, 140, 112)
    image.convert('RGB').save(path)


if len(sys.argv) > 2:
    thumbnail(sys.argv[2])
print('ok')
