"""Makes the figures of "Point par point" (name code DotToDot; gazeplay-games: data/dottodot/figures.json), drawn for
GazePlay Learning on 12/11/2026 in place of 40 figures without names. Same license as GazePlay Learning
(GPL-3.0-or-later).

python make_dottodot_figures.py <data/dottodot folder> [<preview.png>]
figures.json: a list of figures {"name": key of translation, "level": EASY / NORMAL / HARD, "aspect": width on
height, "color": fill color, "points": [[x, y], ...]}, the points in order (the dot 1 first), x and y from 0 to 1 in a
box of that aspect; the outline is closed by the game (the last dot goes back to the first). Easy: 4 to 7 dots,
normal: 8 to 12, hard: 13 to 20. With a second argument, a sheet of all the figures, to look at them.
"""
import json
import math
import os
import sys


def star(branches, inner, start=-90):
    points = []
    for k in range(branches * 2):
        angle = math.radians(start + k * 180 / branches)
        radius = 0.5 if k % 2 == 0 else 0.5 * inner
        points.append([0.5 + math.cos(angle) * radius, 0.5 + math.sin(angle) * radius])
    return points


def heart(count):
    points = []
    for k in range(count):
        t = math.pi * 2 * k / count
        x = 16 * math.sin(t) ** 3
        y = -(13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t))
        points.append([x, y])
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    size = max(max(xs) - min(xs), max(ys) - min(ys))
    # The dot 1 at the hollow of the top, then clockwise
    return [[(x - min(xs)) / size + (1 - (max(xs) - min(xs)) / size) / 2, (y - min(ys)) / size] for x, y in points]


def flower(count, inner):
    points = []
    for k in range(count):
        angle = math.radians(-90 + k * 360 / count)
        radius = 0.5 if k % 2 == 0 else 0.5 * inner
        points.append([0.5 + math.cos(angle) * radius, 0.5 + math.sin(angle) * radius])
    return points


def snowman():
    """A head on a body: the dot 1 at the top of the head, then clockwise."""
    head, body = (0.5, 0.2, 0.19), (0.5, 0.67, 0.32)
    points = []
    # The head, from its top round to its right
    for degrees in (270, 320, 10):
        angle = math.radians(degrees)
        points.append([head[0] + math.cos(angle) * head[2], head[1] + math.sin(angle) * head[2]])
    # The neck, narrower than the head and the body
    points.append([0.6, 0.39])
    # The body, from its upper right, down and round to its upper left
    for k in range(10):
        angle = math.radians(-40 + k * 260 / 9)
        points.append([body[0] + math.cos(angle) * body[2], body[1] + math.sin(angle) * body[2]])
    points.append([0.4, 0.39])
    # The head, from its left back to the top
    for degrees in (170, 220):
        angle = math.radians(degrees)
        points.append([head[0] + math.cos(angle) * head[2], head[1] + math.sin(angle) * head[2]])
    return points


FIGURES = [
    # Easy: 4 to 7 dots
    ("DotKite", "EASY", 0.8, "#F6A5C0", [[0.5, 0], [1, 0.4], [0.5, 1], [0, 0.4]]),
    ("DotHouse", "EASY", 1.0, "#F4B183", [[0.5, 0], [1, 0.42], [1, 1], [0, 1], [0, 0.42]]),
    ("DotGem", "EASY", 1.2, "#9ED8F0", [[0.25, 0], [0.75, 0], [1, 0.32], [0.5, 1], [0, 0.32]]),
    ("DotIceCream", "EASY", 0.75, "#F7D08A", [[0.5, 0], [0.82, 0.18], [0.88, 0.42], [0.5, 1], [0.12, 0.42],
                                               [0.18, 0.18]]),
    ("DotBoat", "EASY", 1.1, "#A8C8F0", [[0.5, 0], [0.92, 0.6], [1, 0.66], [0.8, 0.96], [0.2, 0.96], [0, 0.66],
                                         [0.5, 0.66]]),
    ("DotTree", "EASY", 0.85, "#9BD69A", [[0.5, 0], [0.92, 0.7], [0.6, 0.7], [0.6, 1], [0.4, 1], [0.4, 0.7],
                                          [0.08, 0.7]]),
    ("DotFish", "EASY", 1.6, "#FFC46B", [[1, 0.5], [0.62, 0.12], [0.28, 0.42], [0, 0.12], [0, 0.88], [0.28, 0.58],
                                         [0.62, 0.88]]),
    ("DotFlag", "EASY", 0.9, "#F28B82", [[0, 0], [0.95, 0], [0.7, 0.23], [0.95, 0.46], [0.14, 0.46], [0.14, 1],
                                         [0, 1]]),
    # Normal: 8 to 12 dots
    ("DotStar", "NORMAL", 1.0, "#FFD54F", star(5, 0.42)),
    ("DotHeart", "NORMAL", 1.1, "#F48FB1", heart(12)),
    ("DotMushroom", "NORMAL", 1.1, "#EF8A80", [[0.5, 0], [0.86, 0.12], [1, 0.46], [0.64, 0.46], [0.68, 1],
                                               [0.32, 1], [0.36, 0.46], [0, 0.46], [0.14, 0.12]]),
    ("DotRocket", "NORMAL", 0.62, "#B39DDB", [[0.5, 0], [0.7, 0.22], [0.7, 0.64], [0.95, 0.9], [0.64, 0.86],
                                              [0.5, 1], [0.36, 0.86], [0.05, 0.9], [0.3, 0.64], [0.3, 0.22]]),
    ("DotVan", "NORMAL", 1.7, "#90CAF9", [[0, 0.2], [0.68, 0.2], [0.86, 0.46], [1, 0.52], [1, 0.86], [0.66, 0.86],
                                         [0.34, 0.86], [0, 0.86]]),
    ("DotCat", "NORMAL", 1.0, "#FFB74D", [[0.15, 0], [0.38, 0.26], [0.62, 0.26], [0.85, 0], [0.92, 0.46],
                                          [0.8, 0.82], [0.5, 1], [0.2, 0.82], [0.08, 0.46]]),
    ("DotLeaf", "NORMAL", 0.8, "#AED581", [[0.5, 0], [0.82, 0.2], [0.98, 0.5], [0.76, 0.8], [0.55, 0.88],
                                           [0.5, 1], [0.45, 0.88], [0.24, 0.8], [0.02, 0.5], [0.18, 0.2]]),
    ("DotChalet", "NORMAL", 1.0, "#D7B899", [[0.5, 0.05], [0.68, 0.2], [0.68, 0], [0.82, 0], [0.82, 0.32],
                                             [1, 0.48], [1, 1], [0, 1], [0, 0.48]]),
    # Hard: 13 to 20 dots
    ("DotBigTree", "HARD", 0.8, "#81C784", [[0.5, 0], [0.74, 0.25], [0.63, 0.25], [0.86, 0.5], [0.73, 0.5],
                                            [1, 0.78], [0.58, 0.78], [0.58, 1], [0.42, 1], [0.42, 0.78], [0, 0.78],
                                            [0.27, 0.5], [0.14, 0.5], [0.37, 0.25], [0.26, 0.25]]),
    ("DotCar", "HARD", 1.8, "#E57373", [[0.32, 0.18], [0.68, 0.18], [0.8, 0.45], [1, 0.5], [1, 0.78], [0.87, 0.78],
                                        [0.79, 0.64], [0.71, 0.78], [0.29, 0.78], [0.21, 0.64], [0.13, 0.78], [0, 0.78],
                                        [0, 0.5], [0.2, 0.45]]),
    ("DotCastle", "HARD", 1.2, "#BCAAA4", [[0.5, 0.42], [0.7, 0.42], [0.7, 0.14], [0.8, 0.14], [0.8, 0.24],
                                           [0.9, 0.24], [0.9, 0.14], [1, 0.14], [1, 1], [0.6, 1], [0.6, 0.76],
                                           [0.5, 0.68], [0.4, 0.76], [0.4, 1], [0, 1], [0, 0.14], [0.1, 0.14],
                                           [0.1, 0.24], [0.2, 0.24], [0.2, 0.14], [0.3, 0.14], [0.3, 0.42]][:20]),
    ("DotButterfly", "HARD", 1.2, "#CE93D8", [[0.5, 0.25], [0.75, 0], [1, 0.1], [0.95, 0.4], [0.75, 0.5],
                                              [0.95, 0.65], [0.85, 0.95], [0.6, 0.85], [0.5, 0.65], [0.4, 0.85],
                                              [0.15, 0.95], [0.05, 0.65], [0.25, 0.5], [0.05, 0.4], [0, 0.1],
                                              [0.25, 0]]),
    ("DotMagicStar", "HARD", 1.0, "#FFE082", star(7, 0.5)),
    ("DotPlane", "HARD", 1.0, "#90A4AE", [[0.5, 0], [0.56, 0.08], [0.56, 0.35], [1, 0.55], [1, 0.62], [0.56, 0.52],
                                          [0.56, 0.8], [0.7, 0.92], [0.7, 0.97], [0.5, 0.92], [0.3, 0.97], [0.3, 0.92],
                                          [0.44, 0.8], [0.44, 0.52], [0, 0.62], [0, 0.55], [0.44, 0.35],
                                          [0.44, 0.08]]),
    ("DotFlower", "HARD", 1.0, "#F8BBD0", flower(16, 0.62)),
    ("DotSnowman", "HARD", 1.0, "#E3F2FD", snowman()),
]

LIMITS = {"EASY": (4, 7), "NORMAL": (8, 12), "HARD": (13, 20)}


def figures():
    out = []
    for name, level, aspect, color, points in FIGURES:
        low, high = LIMITS[level]
        assert low <= len(points) <= high, (name, len(points))
        out.append({"name": name, "level": level, "aspect": aspect, "color": color,
                    "points": [[round(x, 4), round(y, 4)] for x, y in points]})
    return out


def preview(items, path):
    from PIL import Image, ImageDraw
    w, h = 300, 260
    sheet = Image.new('RGB', (w * 8, h * 3), 'white')
    pen = ImageDraw.Draw(sheet)
    rows = {"EASY": 0, "NORMAL": 1, "HARD": 2}
    columns = {"EASY": 0, "NORMAL": 0, "HARD": 0}
    for item in items:
        row = rows[item["level"]]
        col = columns[item["level"]]
        columns[item["level"]] += 1
        size = 200
        bw = size * min(1, item["aspect"])
        bh = size / max(1, item["aspect"])
        ox = col * w + (w - bw) / 2
        oy = row * h + 40 + (size - bh) / 2
        pts = [(ox + x * bw, oy + y * bh) for x, y in item["points"]]
        pen.polygon(pts, fill=item["color"], outline=(40, 40, 120))
        for k, (x, y) in enumerate(pts):
            pen.ellipse((x - 4, y - 4, x + 4, y + 4), fill=(200, 40, 40) if k == 0 else (40, 40, 120))
        pen.text((col * w + 6, row * h + 6), item["name"] + " " + str(len(pts)), fill='black')
    sheet.save(path)


folder = sys.argv[1]
items = figures()
with open(os.path.join(folder, 'figures.json'), 'w', encoding='utf-8') as out:
    json.dump(items, out, indent=1)
if len(sys.argv) > 2:
    preview(items, sys.argv[2])
