"""Draws the pictures of the sliding puzzle "Jeu de taquin" (gazeplay-games: data/sliding-puzzle), made for GazePlay
Learning in November 2026 to replace pictures of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later),
except joconde.jpg (public domain).
- sea.png (900 x 900): a big fish, small fishes, sea weeds, a starfish and bubbles under the sea.
- balloons.png (900 x 900): three hot-air balloons over the hills, a house, the sun and clouds.
- biboules.png (900 x 900): the Biboules of the shooting game (data/shooter/biboule/images, drawn for the project) in a
  meadow under a rainbow.
- joconde.jpg (900 x 900): the top of the Mona Lisa of Leonardo da Vinci, public domain, from Wikimedia Commons
  ("Mona Lisa, by Leonardo da Vinci, from C2RMF retouched.jpg", 1280 px wide version,
  https://commons.wikimedia.org/wiki/File:Mona_Lisa,_by_Leonardo_da_Vinci,_from_C2RMF_retouched.jpg).
The game cuts each picture into pieces itself; the numbers are drawn by the game.
- With a thumbnail path, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/slidingpuzzle.png, 960 x 540).

python make_puzzle_images.py <data/sliding-puzzle folder> <data/shooter/biboule/images folder> <Mona Lisa .jpg>
                             [<thumbnail.png>]
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

out, biboules, mona = sys.argv[1], sys.argv[2], sys.argv[3]
os.makedirs(out, exist_ok=True)
K = 3
S = 900


def s(v):
    return int(round(v * K))


def gradient(top, bottom, height_from=0, height_to=S, size=S):
    """A vertical gradient over the whole canvas, from a color to another between two heights."""
    img = Image.new('RGBA', (s(size), s(size)), top + (255,))
    d = ImageDraw.Draw(img)
    for y in range(s(size)):
        t = min(1, max(0, (y / K - height_from) / max(1, height_to - height_from)))
        d.line([(0, y), (s(size), y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(top, bottom)) + (255,))
    return img


def finish(img, name):
    img.resize((S, S), Image.LANCZOS).convert('RGB').save(os.path.join(out, name), optimize=True)


def fish(d, cx, cy, size, body, fin, flip=False):
    """A round fish with a tail, a fin, a big eye and a smile; facing left, or right when flipped."""
    f = -1 if flip else 1
    line = (60, 40, 50, 255)
    w, h = size, size * 0.72
    tail = [(cx + f * w * 0.42, cy), (cx + f * w * 0.78, cy - h * 0.42), (cx + f * w * 0.7, cy),
            (cx + f * w * 0.78, cy + h * 0.42)]
    d.polygon([(s(x), s(y)) for x, y in tail], fill=fin, outline=line)
    d.line([(s(x), s(y)) for x, y in tail + [tail[0]]], fill=line, width=s(size * 0.02), joint='curve')
    # The fin on the back, under the body
    back = [(cx - f * w * 0.2, cy - h * 0.36), (cx + f * w * 0.02, cy - h * 0.72), (cx + f * w * 0.3, cy - h * 0.62),
            (cx + f * w * 0.3, cy - h * 0.3)]
    d.polygon([(s(x), s(y)) for x, y in back], fill=fin)
    d.line([(s(x), s(y)) for x, y in back], fill=line, width=s(size * 0.02), joint='curve')
    d.ellipse([s(cx - w / 2), s(cy - h / 2), s(cx + w / 2), s(cy + h / 2)], fill=body, outline=line,
              width=s(size * 0.025))
    # The stripes
    for k in (0.08, 0.22):
        x = cx + f * w * k
        d.arc([s(x - w * 0.1), s(cy - h * 0.42), s(x + w * 0.1), s(cy + h * 0.42)], -70 if flip else 110,
              70 if flip else 250, fill=fin, width=s(size * 0.05))
    # The eye and the smile
    ex = cx - f * w * 0.24
    d.ellipse([s(ex - size * 0.1), s(cy - h * 0.3), s(ex + size * 0.1), s(cy - h * 0.3 + size * 0.2)],
              fill=(255, 255, 255, 255), outline=line, width=s(size * 0.018))
    d.ellipse([s(ex - size * 0.045 - f * size * 0.02), s(cy - h * 0.3 + size * 0.05),
               s(ex + size * 0.045 - f * size * 0.02), s(cy - h * 0.3 + size * 0.14)], fill=line)
    mx = cx - f * w * 0.36
    d.arc([s(mx - size * 0.08), s(cy), s(mx + size * 0.08), s(cy + size * 0.12)], 20, 160, fill=line,
          width=s(size * 0.02))


def sea():
    img = gradient((120, 206, 236), (30, 92, 168), 0, 760)
    d = ImageDraw.Draw(img)
    # Rays of light from the surface
    rays = Image.new('RGBA', img.size, (0, 0, 0, 0))
    rd = ImageDraw.Draw(rays)
    for x in (120, 360, 600, 800):
        rd.polygon([(s(x - 30), 0), (s(x + 40), 0), (s(x + 160), s(760)), (s(x - 20), s(760))],
                   fill=(255, 255, 255, 34))
    img.alpha_composite(rays.filter(ImageFilter.GaussianBlur(s(8))))
    d = ImageDraw.Draw(img)
    # The sand, with waves
    d.polygon([(0, s(740))] + [(s(x), s(740 + 18 * math.sin(x / 70))) for x in range(0, 901, 10)]
              + [(s(900), s(900)), (0, s(900))], fill=(240, 214, 150, 255))
    rnd = random.Random(4)
    for _ in range(60):
        x, y = rnd.uniform(0, 900), rnd.uniform(770, 895)
        d.ellipse([s(x - 3), s(y - 2), s(x + 3), s(y + 2)], fill=(214, 184, 120, 255))
    # Sea weeds
    for x0, height, color in ((70, 300, (60, 160, 90)), (150, 210, (90, 190, 100)), (780, 340, (50, 150, 100)),
                              (850, 230, (100, 196, 110)), (470, 150, (80, 176, 96))):
        points = [(x0 + 22 * math.sin(t / 22), 760 - t) for t in range(0, height, 6)]
        d.line([(s(x), s(y)) for x, y in points], fill=color + (255,), width=s(18), joint='curve')
        for i in range(2, len(points) - 2, 5):
            x, y = points[i]
            side = 1 if (i // 5) % 2 else -1
            d.ellipse([s(x + side * 6 - 16), s(y - 10), s(x + side * 6 + 16), s(y + 10)], fill=color + (255,))
    # A starfish and a shell on the sand
    cx, cy, r = 620, 820, 48
    star = []
    for k in range(10):
        a = -math.pi / 2 + k * math.pi / 5
        rr = r if k % 2 == 0 else r * 0.45
        star.append((s(cx + rr * math.cos(a)), s(cy + rr * math.sin(a))))
    d.polygon(star, fill=(246, 120, 96, 255), outline=(170, 70, 60, 255))
    d.line(star + [star[0]], fill=(170, 70, 60, 255), width=s(4), joint='curve')
    d.pieslice([s(250), s(790), s(350), s(890)], 180, 360, fill=(250, 196, 214, 255), outline=(190, 120, 150, 255),
               width=s(4))
    for a in range(200, 360, 32):
        d.line([(s(300), s(840)), (s(300 + 48 * math.cos(math.radians(a))), s(840 + 48 * math.sin(math.radians(a))))],
               fill=(190, 120, 150, 255), width=s(3))
    # The big fish and the small ones
    fish(d, 430, 420, 420, (250, 150, 60, 255), (250, 92, 80, 255))
    fish(d, 160, 160, 140, (250, 220, 80, 255), (90, 170, 230, 255), flip=True)
    fish(d, 760, 600, 130, (170, 120, 220, 255), (250, 200, 90, 255))
    fish(d, 740, 190, 110, (110, 210, 170, 255), (240, 110, 150, 255))
    # Bubbles
    for x, y, r in ((200, 360, 18), (226, 316, 12), (210, 280, 8), (640, 120, 20), (668, 76, 13), (420, 660, 14),
                    (440, 620, 9), (560, 300, 10)):
        d.ellipse([s(x - r), s(y - r), s(x + r), s(y + r)], outline=(255, 255, 255, 230), width=s(3))
        d.ellipse([s(x - r * 0.5), s(y - r * 0.6), s(x - r * 0.1), s(y - r * 0.2)], fill=(255, 255, 255, 220))
    finish(img, 'sea.png')


def cloud(d, cx, cy, size):
    for dx, dy, r in ((-0.5, 0.1, 0.32), (-0.15, -0.15, 0.42), (0.25, -0.05, 0.36), (0.55, 0.12, 0.26),
                      (0.05, 0.18, 0.34)):
        d.ellipse([s(cx + (dx - r) * size), s(cy + (dy - r) * size), s(cx + (dx + r) * size),
                   s(cy + (dy + r) * size)], fill=(255, 255, 255, 255))


def balloon(img, cx, cy, size, colors):
    """A hot-air balloon with stripes, its ropes and its basket."""
    d = ImageDraw.Draw(img)
    line = (70, 50, 60, 255)
    w, h = size * 0.5, size * 0.56
    envelope = Image.new('RGBA', img.size, (0, 0, 0, 0))
    ed = ImageDraw.Draw(envelope)
    shape = [(cx + w * math.sin(t) * (1 - 0.35 * max(0, math.cos(t)) ** 3),
              cy - h * math.cos(t) * (0.95 if math.cos(t) > 0 else 1.25)) for t in
             [i * math.pi / 60 for i in range(-60, 61)]]
    mask = Image.new('L', img.size, 0)
    ImageDraw.Draw(mask).polygon([(s(x), s(y)) for x, y in shape], fill=255)
    bands = len(colors) * 2
    for i in range(bands):
        x1 = cx - w + 2 * w * i / bands
        ed.rectangle([s(x1), 0, s(x1 + 2 * w / bands), s(S)], fill=colors[i % len(colors)] + (255,))
    img.paste(envelope, (0, 0), mask)
    d.line([(s(x), s(y)) for x, y in shape], fill=line, width=s(size * 0.02), joint='curve')
    bottom = cy + h * 1.25
    basket_top = bottom + size * 0.16
    for k in (-1, 1):
        d.line([(s(cx + k * w * 0.32), s(bottom - size * 0.03)), (s(cx + k * size * 0.1), s(basket_top))], fill=line,
               width=s(size * 0.014))
    d.rounded_rectangle([s(cx - size * 0.12), s(basket_top), s(cx + size * 0.12), s(basket_top + size * 0.16)],
                        radius=s(size * 0.03), fill=(196, 140, 84, 255), outline=line, width=s(size * 0.016))
    d.line([(s(cx - size * 0.12), s(basket_top + size * 0.06)), (s(cx + size * 0.12), s(basket_top + size * 0.06))],
           fill=line, width=s(size * 0.01))


def balloons():
    img = gradient((140, 200, 246), (226, 240, 250), 0, 620)
    d = ImageDraw.Draw(img)
    d.ellipse([s(680), s(60), s(820), s(200)], fill=(255, 214, 90, 255))
    for k in range(12):
        a = k * math.pi / 6
        d.line([(s(750 + 84 * math.cos(a)), s(130 + 84 * math.sin(a))),
                (s(750 + 108 * math.cos(a)), s(130 + 108 * math.sin(a)))], fill=(255, 200, 80, 255), width=s(8))
    cloud(d, 170, 120, 170)
    cloud(d, 520, 230, 120)
    cloud(d, 820, 380, 110)
    # The hills, from far to near
    for base, amp, period, phase, color in ((600, 50, 260, 0.5, (150, 200, 120)), (680, 60, 210, 2.0, (110, 180, 96)),
                                            (770, 40, 300, 4.0, (84, 156, 80))):
        d.polygon([(0, s(900))] + [(s(x), s(base - amp * math.sin(x / period * math.pi + phase)))
                                   for x in range(0, 901, 10)] + [(s(900), s(900))], fill=color + (255,))
    # A house and trees
    line = (80, 56, 50, 255)
    d.rectangle([s(600), s(700), s(720), s(790)], fill=(250, 232, 190, 255), outline=line, width=s(5))
    d.polygon([(s(586), s(704)), (s(660), s(640)), (s(734), s(704))], fill=(214, 84, 72, 255), outline=line)
    d.rectangle([s(646), s(740), s(674), s(790)], fill=(150, 96, 64, 255), outline=line, width=s(4))
    d.rectangle([s(614), s(716), s(638), s(740)], fill=(170, 214, 240, 255), outline=line, width=s(3))
    for x, y, r in ((120, 760, 44), (200, 790, 36), (820, 740, 40)):
        d.rectangle([s(x - 8), s(y), s(x + 8), s(y + r * 1.4)], fill=(130, 90, 60, 255))
        d.ellipse([s(x - r), s(y - r * 1.4), s(x + r), s(y + r * 0.4)], fill=(60, 140, 70, 255))
    balloon(img, 250, 330, 300, [(240, 90, 80), (255, 210, 90)])
    balloon(img, 560, 470, 200, [(90, 150, 230), (255, 255, 255)])
    balloon(img, 780, 260, 150, [(150, 100, 210), (120, 210, 160)])
    finish(img, 'balloons.png')


def biboule_meadow():
    img = gradient((150, 210, 250), (220, 240, 252), 0, 560)
    d = ImageDraw.Draw(img)
    # The rainbow
    for k, color in enumerate(((236, 96, 96), (246, 160, 80), (250, 220, 90), (120, 196, 110), (90, 150, 230),
                               (150, 110, 210))):
        r = 420 - k * 26
        d.arc([s(450 - r), s(560 - r), s(450 + r), s(560 + r)], 180, 360, fill=color + (255,), width=s(27))
    cloud(d, 120, 470, 150)
    cloud(d, 800, 480, 150)
    cloud(d, 690, 120, 110)
    d.polygon([(0, s(900))] + [(s(x), s(560 - 40 * math.sin(x / 300 * math.pi))) for x in range(0, 901, 10)]
              + [(s(900), s(900))], fill=(140, 204, 110, 255))
    d.polygon([(0, s(900))] + [(s(x), s(700 - 30 * math.sin(x / 220 * math.pi + 1))) for x in range(0, 901, 10)]
              + [(s(900), s(900))], fill=(110, 186, 96, 255))
    rnd = random.Random(7)
    for _ in range(46):
        x, y = rnd.uniform(20, 880), rnd.uniform(600, 880)
        color = rnd.choice(((250, 120, 150), (255, 230, 100), (255, 255, 255), (180, 140, 240)))
        for a in range(5):
            ang = a * 2 * math.pi / 5
            d.ellipse([s(x + 9 * math.cos(ang) - 7), s(y + 9 * math.sin(ang) - 7), s(x + 9 * math.cos(ang) + 7),
                       s(y + 9 * math.sin(ang) + 7)], fill=color + (255,))
        d.ellipse([s(x - 5), s(y - 5), s(x + 5), s(y + 5)], fill=(250, 190, 60, 255))
    for name, x, y, w in (('Blue', 60, 520, 330), ('Red', 470, 590, 300), ('Yellow', 330, 250, 230),
                          ('Green', 640, 300, 210), ('Orange', 140, 140, 170)):
        b = Image.open(os.path.join(biboules, name + '.png')).convert('RGBA')
        b = b.resize((s(w), s(w * b.height / b.width)), Image.LANCZOS)
        img.alpha_composite(b, (s(x), s(y)))
    finish(img, 'biboules.png')


def joconde():
    """The top of the painting, square: the face, the hands at the bottom edge, the landscape at the sides."""
    painting = Image.open(mona).convert('RGB')
    w = painting.width
    top = int(painting.height * 0.07)
    painting.crop((0, top, w, top + w)).resize((S, S), Image.LANCZOS).save(os.path.join(out, 'joconde.jpg'),
                                                                           quality=90, optimize=True)


sea()
balloons()
biboule_meadow()
joconde()


def thumbnail(path):
    """The thumbnail of the menu (960 x 540): the sea cut into 3 x 3 pieces with the empty place, and its model."""
    W, H = 960, 540
    image = Image.new('RGBA', (W, H), (236, 228, 214, 255))
    d = ImageDraw.Draw(image)
    side, gap = 420, 8
    x0, y0 = 150, (H - side) // 2
    d.rounded_rectangle([x0 - 18, y0 - 18, x0 + side + 18, y0 + side + 18], radius=22, fill=(176, 138, 100, 255))
    d.rounded_rectangle([x0 - 6, y0 - 6, x0 + side + 6, y0 + side + 6], radius=12, fill=(120, 92, 70, 255))
    picture = Image.open(os.path.join(out, 'sea.png')).convert('RGBA').resize((side, side), Image.LANCZOS)
    cell = side // 3
    order = [1, 5, 2, 0, 4, 8, 3, 6, None]
    for place, piece in enumerate(order):
        if piece is None:
            continue
        tile = picture.crop(((piece % 3) * cell, (piece // 3) * cell, (piece % 3 + 1) * cell, (piece // 3 + 1) * cell))
        tile = tile.crop((gap // 2, gap // 2, cell - gap // 2, cell - gap // 2))
        mask = Image.new('L', tile.size, 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, tile.width - 1, tile.height - 1], radius=10, fill=255)
        image.paste(tile, (x0 + (place % 3) * cell + gap // 2, y0 + (place // 3) * cell + gap // 2), mask)
    # The model, smaller, at the right
    m = 230
    mx, my = 640, (H - m) // 2
    d.rounded_rectangle([mx - 10, my - 10, mx + m + 10, my + m + 10], radius=16, fill=(255, 255, 255, 255))
    image.paste(picture.resize((m, m), Image.LANCZOS), (mx, my))
    image.convert('RGB').save(path)


if len(sys.argv) > 4:
    thumbnail(sys.argv[4])
print('ok')
