"""Draws the board of the goose game "Jeu de l'oie" (name code GooseGame; gazeplay-games: data/goosegame/gooseboard.png),
made for GazePlay Learning on 12/11/2026 in place of a board of unknown source. Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_goose_images.py <data/goosegame folder> <Fredoka-SemiBold.ttf>
gooseboard.png: 1799 x 1056 with a transparent background, the size of the former board, so that the centers of the
squares stay those of positions.json (square 0 is the start, 63 the end). A sandy path in a spiral, a round tile per
square with its number, and a picture on the special squares: geese (play the throw again), the bridge (7 to 32), the
inn (19, two turns asleep), the well (31) and the prison (52) (wait until another pawn passes), the arrow back to the
start (58) and the star of the end (63).
"""
import json
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFont

WIDTH, HEIGHT = 1799, 1056
S = 2
RADIUS = 47
GEESE = [5, 9, 13, 18, 24, 28, 34, 36, 40, 45, 49, 54]
BRIDGE, BRIDGE_END, INN, WELL, PRISON, BACK, END = 7, 32, 19, 31, 52, 58, 63

PATH = (238, 220, 180)
PATH_EDGE = (150, 116, 76)
TILE = (255, 250, 238)
TILE_EDGE = (176, 140, 96)
INK = (52, 46, 60)
COLORS = {
    'goose': (156, 214, 140), 'bridge': (222, 176, 112), 'inn': (160, 204, 240), 'well': (150, 200, 232),
    'prison': (190, 184, 220), 'back': (244, 150, 140), 'end': (255, 214, 92), 'start': (255, 255, 255),
}


def kind(i):
    if i in GEESE:
        return 'goose'
    return {BRIDGE: 'bridge', BRIDGE_END: 'bridge', INN: 'inn', WELL: 'well', PRISON: 'prison', BACK: 'back',
            END: 'end', 0: 'start'}.get(i)


def spline(points, steps=12):
    """A smooth line through the points (Catmull-Rom)."""
    out = []
    for i in range(len(points) - 1):
        p0 = points[max(0, i - 1)]
        p1, p2 = points[i], points[i + 1]
        p3 = points[min(len(points) - 1, i + 2)]
        for k in range(steps):
            t = k / steps
            out.append(tuple(0.5 * ((2 * p1[c]) + (-p0[c] + p2[c]) * t + (2 * p0[c] - 5 * p1[c] + 4 * p2[c] - p3[c])
                                    * t * t + (-p0[c] + 3 * p1[c] - 3 * p2[c] + p3[c]) * t ** 3) for c in (0, 1)))
    out.append(points[-1])
    return out


def thick_line(pen, points, width, fill):
    """A wide line, as discs stamped along it (a wide line of PIL leaves slivers at its joints)."""
    r = width / 2
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        steps = max(1, int(math.dist((x0, y0), (x1, y1)) / (r * 0.15)))
        for k in range(steps + 1):
            x, y = x0 + (x1 - x0) * k / steps, y0 + (y1 - y0) * k / steps
            pen.ellipse((x - r, y - r, x + r, y + r), fill=fill)


def flag(pen, cx, cy, r):
    """The flag of the start."""
    pen.line([(cx - r * 0.35, cy - r * 0.7), (cx - r * 0.35, cy + r * 0.7)], fill=INK, width=int(r * 0.12))
    pen.polygon([(cx - r * 0.3, cy - r * 0.7), (cx + r * 0.6, cy - r * 0.42), (cx - r * 0.3, cy - r * 0.12)],
                fill=(232, 84, 72), outline=INK)


def goose(pen, cx, cy, r):
    """A white goose seen from the side, its head to the right."""
    body = (cx - r * 0.75, cy - r * 0.05, cx + r * 0.45, cy + r * 0.6)
    pen.ellipse(body, fill=(255, 255, 255), outline=INK, width=int(r * 0.08))
    # The tail
    pen.polygon([(cx - r * 0.7, cy + r * 0.15), (cx - r * 0.98, cy - r * 0.08), (cx - r * 0.62, cy + r * 0.02)],
                fill=(255, 255, 255), outline=INK)
    # The neck and the head
    pen.line([(cx + r * 0.2, cy + r * 0.15), (cx + r * 0.42, cy - r * 0.35), (cx + r * 0.42, cy - r * 0.55)],
             fill=INK, width=int(r * 0.3), joint='curve')
    pen.line([(cx + r * 0.2, cy + r * 0.15), (cx + r * 0.42, cy - r * 0.35), (cx + r * 0.42, cy - r * 0.55)],
             fill=(255, 255, 255), width=int(r * 0.16), joint='curve')
    pen.ellipse((cx + r * 0.27, cy - r * 0.8, cx + r * 0.6, cy - r * 0.47), fill=(255, 255, 255), outline=INK,
                width=int(r * 0.08))
    pen.polygon([(cx + r * 0.57, cy - r * 0.7), (cx + r * 0.88, cy - r * 0.6), (cx + r * 0.57, cy - r * 0.54)],
                fill=(245, 150, 40), outline=INK)
    pen.ellipse((cx + r * 0.42, cy - r * 0.7, cx + r * 0.5, cy - r * 0.62), fill=INK)
    # The wing
    pen.arc((cx - r * 0.5, cy + r * 0.05, cx + r * 0.2, cy + r * 0.5), 200, 340, fill=INK, width=int(r * 0.07))


def bridge(pen, cx, cy, r):
    """A small wooden bridge: an arch and its planks."""
    pen.arc((cx - r * 0.85, cy - r * 0.5, cx + r * 0.85, cy + r * 0.9), 200, 340, fill=(120, 80, 40),
            width=int(r * 0.18))
    for k in range(5):
        x = cx - r * 0.6 + k * r * 0.3
        top = cy - r * 0.12 - math.sin(math.pi * (k + 0.5) / 5) * r * 0.25
        pen.rectangle((x, top, x + r * 0.22, top + r * 0.42), fill=(196, 140, 80), outline=(120, 80, 40))


def inn(pen, cx, cy, r):
    """A small house, with "z z" of sleep."""
    pen.polygon([(cx - r * 0.65, cy - r * 0.05), (cx, cy - r * 0.6), (cx + r * 0.65, cy - r * 0.05)],
                fill=(214, 92, 72), outline=INK)
    pen.rectangle((cx - r * 0.5, cy - r * 0.05, cx + r * 0.5, cy + r * 0.55), fill=(255, 238, 200), outline=INK,
                  width=int(r * 0.06))
    pen.rectangle((cx - r * 0.12, cy + r * 0.15, cx + r * 0.12, cy + r * 0.55), fill=(140, 96, 56))
    font = ImageFont.truetype(FONT, int(r * 0.45))
    pen.text((cx + r * 0.35, cy - r * 0.95), 'z', font=font, fill=INK)
    pen.text((cx + r * 0.6, cy - r * 1.2), 'z', font=ImageFont.truetype(FONT, int(r * 0.35)), fill=INK)


def well(pen, cx, cy, r):
    """A stone well with its roof."""
    pen.rectangle((cx - r * 0.55, cy - r * 0.6, cx + r * 0.55, cy - r * 0.48), fill=(150, 96, 52))
    pen.line([(cx - r * 0.45, cy - r * 0.5), (cx - r * 0.45, cy)], fill=(120, 80, 40), width=int(r * 0.1))
    pen.line([(cx + r * 0.45, cy - r * 0.5), (cx + r * 0.45, cy)], fill=(120, 80, 40), width=int(r * 0.1))
    pen.rounded_rectangle((cx - r * 0.6, cy, cx + r * 0.6, cy + r * 0.55), radius=r * 0.1, fill=(176, 176, 186),
                          outline=INK, width=int(r * 0.06))
    for row in range(2):
        y = cy + r * (0.18 + row * 0.2)
        pen.line([(cx - r * 0.58, y), (cx + r * 0.58, y)], fill=(120, 120, 132), width=int(r * 0.04))
    pen.line([(cx, cy - r * 0.48), (cx, cy - r * 0.15)], fill=INK, width=int(r * 0.04))
    pen.rectangle((cx - r * 0.12, cy - r * 0.18, cx + r * 0.12, cy + r * 0.02), fill=(110, 170, 220), outline=INK)


def prison(pen, cx, cy, r):
    """A window with bars."""
    pen.rounded_rectangle((cx - r * 0.6, cy - r * 0.55, cx + r * 0.6, cy + r * 0.5), radius=r * 0.12,
                          fill=(232, 228, 244), outline=INK, width=int(r * 0.08))
    for k in range(4):
        x = cx - r * 0.36 + k * r * 0.24
        pen.line([(x, cy - r * 0.5), (x, cy + r * 0.45)], fill=INK, width=int(r * 0.09))


def back(pen, cx, cy, r):
    """A curved arrow going back."""
    pen.arc((cx - r * 0.55, cy - r * 0.5, cx + r * 0.55, cy + r * 0.6), 180, 70, fill=INK, width=int(r * 0.17))
    tip = (cx - r * 0.55, cy + r * 0.05)
    pen.polygon([(tip[0] - r * 0.28, tip[1] - r * 0.12), (tip[0] + r * 0.28, tip[1] - r * 0.12),
                 (tip[0], tip[1] + r * 0.32)], fill=INK)


def star(pen, cx, cy, r, fill):
    points = []
    for k in range(10):
        angle = -math.pi / 2 + k * math.pi / 5
        radius = r if k % 2 == 0 else r * 0.45
        points.append((cx + math.cos(angle) * radius, cy + math.sin(angle) * radius))
    pen.polygon(points, fill=fill, outline=INK)


def draw(positions):
    image = Image.new('RGBA', (WIDTH * S, HEIGHT * S), (0, 0, 0, 0))
    pen = ImageDraw.Draw(image)
    centers = [(p['x'] * S, p['y'] * S) for p in positions]
    r = RADIUS * S
    track = spline(centers)
    thick_line(pen, track, r * 2.45, PATH_EDGE)
    thick_line(pen, track, r * 2.25, PATH)
    # The bridge from 7 to 32, across the path
    (x1, y1), (x2, y2) = centers[BRIDGE], centers[BRIDGE_END]
    pen.line([(x1, y1), (x2, y2)], fill=PATH_EDGE, width=int(r * 1.0))
    length = math.dist((x1, y1), (x2, y2))
    for k in range(1, int(length / (r * 0.35))):
        t = k * r * 0.35 / length
        x, y = x1 + (x2 - x1) * t, y1 + (y2 - y1) * t
        nx, ny = -(y2 - y1) / length, (x2 - x1) / length
        pen.line([(x + nx * r * 0.42, y + ny * r * 0.42), (x - nx * r * 0.42, y - ny * r * 0.42)],
                 fill=(206, 152, 92), width=int(r * 0.22))
    number_font = ImageFont.truetype(FONT, int(r * 0.78))
    small_font = ImageFont.truetype(FONT, int(r * 0.42))
    for i, (cx, cy) in enumerate(centers):
        k = kind(i)
        radius = r * (1.45 if i == END else 1.1 if i == 0 else 1)
        fill = COLORS.get(k, TILE)
        pen.ellipse((cx - radius - 3 * S, cy - radius - 3 * S + 4 * S, cx + radius + 3 * S, cy + radius + 3 * S + 4 * S),
                    fill=(120, 92, 60, 90))
        pen.ellipse((cx - radius, cy - radius, cx + radius, cy + radius), fill=fill, outline=TILE_EDGE,
                    width=int(3 * S))
        if k is None:
            text = str(i)
            box = pen.textbbox((0, 0), text, font=number_font)
            pen.text((cx - (box[0] + box[2]) / 2, cy - (box[1] + box[3]) / 2), text, font=number_font, fill=INK)
            continue
        picture = cy - r * 0.15
        if k == 'goose':
            goose(pen, cx, picture + r * 0.05, r * 0.72)
        elif k == 'bridge':
            bridge(pen, cx, picture, r * 0.7)
        elif k == 'inn':
            inn(pen, cx, picture + r * 0.05, r * 0.7)
        elif k == 'well':
            well(pen, cx, picture, r * 0.75)
        elif k == 'prison':
            prison(pen, cx, picture, r * 0.65)
        elif k == 'back':
            back(pen, cx, picture, r * 0.7)
        elif k == 'end':
            star(pen, cx, cy - r * 0.2, r * 0.75, (255, 248, 220))
        if k == 'start':
            flag(pen, cx, cy, r * 0.8)
            continue
        text = str(i)
        font = ImageFont.truetype(FONT, int(r * 0.62)) if i == END else small_font
        box = pen.textbbox((0, 0), text, font=font)
        y = cy + radius * 0.62
        pen.text((cx - (box[0] + box[2]) / 2, y - (box[1] + box[3]) / 2), text, font=font, fill=INK)
    return image.resize((WIDTH, HEIGHT), Image.LANCZOS)


folder, FONT = sys.argv[1], sys.argv[2]
with open(os.path.join(folder, 'positions.json'), encoding='utf-8') as source:
    squares = json.load(source)
draw(squares).save(os.path.join(folder, 'gooseboard.png'), optimize=True)
