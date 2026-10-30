"""Draws the scenes of Spot The Differences (gazeplay-games: data/spotthedifferences), made for GazePlay Learning in
October 2026 to replace scenes of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later) for the
backgrounds; the objects are OpenMoji (CC BY-SA 4.0).

python make_spotdifferences_images.py <folder of the 618x618 PNG files of OpenMoji> <data/spotthedifferences folder>
Pillow is required. For each scene: <scene>.png (the first picture, 1000 x 1000) and, for each level,
<scene>-<level>.png (the second picture, with 3, 5 or 7 differences); then instances.json (the pictures and, for each
level, the center and radius of each difference, in pixels of the picture). The differences are made here (an object
removed, replaced or of another color), so that the circles of the game are always right.
"""
import colorsys
import json
import math
import os
import sys

from PIL import Image, ImageDraw

S, K = 1000, 2  # size of a picture, drawn at scale 2, then reduced

# The objects of each scene: (OpenMoji code point, center x, center y, size, code point of the replacing object or
# None). The levels take the biggest objects in Easy, the smallest in Hard (see LEVELS).
SCENES = {
    'house': [('1F6CB', 330, 640, 300, None), ('1F4FA', 780, 600, 190, '1F4BB'), ('1F5BC', 330, 270, 190, '1F3A8'),
              ('1FAB4', 900, 820, 170, '1F335'), ('1F408', 560, 860, 160, '1F415'), ('1F570', 780, 410, 110, '23F0'),
              ('1F9F8', 120, 860, 130, '1FA80'), ('1F4DA', 650, 430, 110, '1F4D6'), ('26BD', 380, 900, 100, '1F3C0'),
              ('1F3FA', 900, 420, 110, '1F490')],
    'camp': [('26FA', 330, 560, 320, None), ('1F525', 640, 740, 200, '1FAB5'), ('1F332', 860, 380, 260, '1F333'),
             ('1F392', 160, 800, 150, '1F45C'), ('1F3A3', 860, 760, 170, '1F6F6'), ('1F426', 560, 300, 100, '1F989'),
             ('1F43F', 760, 560, 110, '1F994'), ('1F344', 420, 860, 90, '1F338'), ('1F98B', 160, 380, 90, '1F41D'),
             ('1F526', 330, 900, 90, '1F56F')],
    'concert': [('1F3B9', 260, 430, 260, None), ('1F941', 720, 420, 240, '1FA98'), ('1F3B8', 500, 400, 200, '1FA95'),
                ('1F3A4', 500, 720, 170, '1F3A7'), ('1F3B7', 130, 760, 150, '1F3BA'), ('1F3BB', 860, 760, 150, '1FA97'),
                ('1F3B5', 300, 160, 100, '1F3B6'), ('1F3B6', 700, 150, 100, '1F3BC'), ('1F339', 300, 860, 90, '1F337'),
                ('1F31F', 870, 160, 90, '2B50')],
    'kitchen': [('1FAD6', 500, 560, 220, None), ('1F382', 220, 560, 200, '1F370'), ('1F35E', 790, 560, 170, '1F950'),
                ('1F373', 760, 280, 170, '1F372'), ('1F34E', 180, 830, 120, '1F350'), ('1F34C', 400, 840, 120, '1F955'),
                ('2615', 640, 830, 110, '1F95B'), ('1F9C0', 860, 840, 110, '1F95A'), ('1F374', 260, 290, 110, '1F944'),
                ('1F95E', 480, 280, 110, '1F9C7')],
    'oasis': [('1F334', 330, 450, 320, None), ('1F42B', 700, 620, 260, '1F42A'), ('1F335', 130, 720, 170, '1F33F'),
              ('2600', 820, 160, 170, '1F31E'), ('1F40D', 360, 850, 140, '1F98E'), ('1F982', 850, 870, 110, '1F577'),
              ('1F985', 560, 180, 110, '1F426'), ('1F3FA', 560, 860, 100, '1F48E'), ('1F334', 900, 480, 140, '1F335'),
              ('1F33A', 160, 880, 80, '1F33C')],
    'sea': [('26F5', 520, 150, 230, None), ('1F433', 230, 380, 230, '1F42C'), ('1F419', 760, 460, 190, '1F991'),
            ('1F422', 340, 650, 170, '1F9AD'), ('1F420', 720, 690, 140, '1F421'), ('1F980', 150, 870, 120, '1F99E'),
            ('1F41A', 450, 900, 90, '2B50'), ('1F990', 640, 890, 100, '1F99E'), ('1FABC', 880, 230, 100, '1F41F'),
            ('1F41F', 860, 880, 100, '1F420')],
    'snow': [('26C4', 300, 640, 280, None), ('1F384', 760, 500, 260, '1F332'), ('1F98C', 560, 720, 200, '1F43B-200D-2744-FE0F'),
             ('1F427', 140, 880, 140, '1F426'), ('1F6F7', 800, 860, 150, '1F3BF'), ('2744', 220, 200, 100, '2B50'),
             ('1F9E3', 420, 880, 100, '1F9E4'), ('26F8', 620, 900, 90, '1F3D2'), ('2601', 620, 170, 130, '2600'),
             ('2744', 880, 230, 80, '1F31F')],
    'farm': [('1F404', 320, 650, 260, None), ('1F69C', 760, 560, 240, '1F697'), ('1F416', 600, 800, 170, '1F411'),
             ('1F414', 150, 850, 130, '1F986'), ('1F33B', 880, 820, 140, '1F337'), ('1F415', 380, 870, 110, '1F408'),
             ('1F955', 760, 890, 90, '1F33D'), ('1F95A', 240, 900, 70, '1F423'), ('1F426', 620, 240, 90, '1F98B'),
             ('1F333', 140, 450, 180, '1F332')],
}

# The levels: the number of differences, which objects (sorted from the biggest), and what changes, in turn
LEVELS = {
    'easy': (3, lambda objects: objects[:3], ['remove', 'swap', 'remove']),
    'normal': (5, lambda objects: objects[1:6], ['swap', 'remove', 'color', 'swap', 'remove']),
    'hard': (7, lambda objects: objects[-7:], ['color', 'swap', 'remove', 'color', 'swap', 'remove', 'color']),
}

src, out = sys.argv[1], sys.argv[2]


def canvas():
    image = Image.new('RGB', (S * K, S * K))
    return image, ImageDraw.Draw(image)


def s(*values):
    return [v * K for v in values]


def gradient(draw, top, bottom, color_top, color_bottom):
    for y in range(int(top * K), int(bottom * K)):
        t = (y - top * K) / max(1, (bottom - top) * K)
        draw.line([(0, y), (S * K, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(color_top, color_bottom)))


def blob(draw, cx, cy, rx, ry, color):
    draw.ellipse(s(cx - rx, cy - ry, cx + rx, cy + ry), fill=color)


def house():
    image, draw = canvas()
    draw.rectangle(s(0, 0, S, 700), fill=(250, 220, 170))
    for x in range(0, S, 80):
        draw.rectangle(s(x, 0, x + 40, 700), fill=(245, 210, 160))
    draw.rectangle(s(0, 700, S, S), fill=(200, 140, 90))
    for y in range(720, S, 60):
        draw.line(s(0, y, S, y), fill=(180, 120, 75), width=3 * K)
    # A window and a shelf
    draw.rectangle(s(560, 120, 720, 300), fill=(150, 210, 245), outline=(120, 80, 50), width=10 * K)
    draw.line(s(640, 120, 640, 300), fill=(120, 80, 50), width=8 * K)
    draw.rectangle(s(600, 480, 960, 500), fill=(150, 100, 60))
    # A rug
    blob(draw, 420, 880, 330, 70, (220, 110, 100))
    return image


def camp():
    image, draw = canvas()
    gradient(draw, 0, 520, (130, 200, 245), (210, 238, 250))
    blob(draw, 250, 560, 500, 160, (120, 180, 90))
    blob(draw, 800, 560, 450, 140, (105, 165, 80))
    gradient(draw, 520, S, (130, 190, 90), (95, 150, 65))
    # A lake and a path
    blob(draw, 820, 760, 150, 60, (110, 180, 220))
    draw.polygon(s(450, S, 600, S, 560, 620, 520, 620), fill=(215, 190, 140))
    return image


def concert():
    image, draw = canvas()
    gradient(draw, 0, 620, (60, 40, 90), (110, 60, 120))
    # Curtains and lights
    for x in (0, S - 120):
        draw.rectangle(s(x, 0, x + 120, 620), fill=(170, 40, 50))
        for dx in range(20, 120, 30):
            draw.line(s(x + dx, 0, x + dx, 620), fill=(140, 30, 40), width=4 * K)
    for x in (300, 500, 700):
        draw.polygon(s(x - 20, 0, x + 20, 0, x + 120, 620, x - 120, 620), fill=(130, 90, 140))
    # The stage
    draw.rectangle(s(0, 560, S, 640), fill=(140, 90, 50))
    draw.rectangle(s(0, 640, S, S), fill=(160, 110, 65))
    for y in range(660, S, 50):
        draw.line(s(0, y, S, y), fill=(140, 95, 55), width=3 * K)
    return image


def kitchen():
    image, draw = canvas()
    draw.rectangle(s(0, 0, S, 640), fill=(235, 240, 245))
    for x in range(0, S, 60):
        for y in range(0, 400, 60):
            draw.rectangle(s(x + 3, y + 3, x + 57, y + 57), outline=(210, 220, 230), width=2 * K)
    # Cupboards and a table
    draw.rectangle(s(0, 380, S, 420), fill=(160, 110, 70))
    draw.rectangle(s(0, 640, S, S), fill=(190, 200, 210))
    draw.rectangle(s(40, 660, 960, 960), fill=(215, 160, 100))
    draw.rectangle(s(40, 660, 960, 690), fill=(190, 135, 80))
    # A shelf
    draw.rectangle(s(150, 345, 860, 362), fill=(160, 110, 70))
    return image


def oasis():
    image, draw = canvas()
    gradient(draw, 0, 560, (120, 190, 240), (245, 225, 180))
    # Dunes, pyramids, water
    draw.polygon(s(520, 560, 650, 400, 780, 560), fill=(225, 180, 100))
    draw.polygon(s(700, 560, 790, 450, 880, 560), fill=(210, 165, 90))
    blob(draw, 250, 600, 450, 90, (240, 200, 120))
    gradient(draw, 560, S, (245, 205, 120), (225, 180, 95))
    blob(draw, 480, 680, 230, 60, (90, 170, 210))
    return image


def sea():
    image, draw = canvas()
    gradient(draw, 0, 200, (150, 210, 245), (200, 235, 250))
    gradient(draw, 200, S, (80, 170, 220), (25, 80, 140))
    # Waves, rays, sand, seaweed
    for x in range(0, S, 80):
        blob(draw, x + 40, 205, 42, 18, (80, 170, 220))
    for x in (150, 500, 800):
        draw.polygon(s(x, 220, x + 70, 220, x + 230, S, x + 120, S), fill=(95, 180, 225))
    blob(draw, 500, 990, 700, 120, (230, 210, 155))
    for x in (60, 560, 960):
        points = [(x + 18 * math.sin(y / 30), y) for y in range(960, 720, -12)]
        draw.line([tuple(v * K for v in p) for p in points], fill=(60, 150, 90), width=12 * K)
    return image


def snow():
    image, draw = canvas()
    gradient(draw, 0, 560, (150, 200, 240), (220, 235, 250))
    # Mountains with snow tops
    for x, h, color in ((200, 330, (150, 165, 190)), (520, 400, (130, 145, 175)), (860, 300, (150, 165, 190))):
        draw.polygon(s(x - 300, 600, x, 600 - h, x + 300, 600), fill=color)
        draw.polygon(s(x - 90, 600 - h * 0.7, x, 600 - h, x + 90, 600 - h * 0.7), fill=(255, 255, 255))
    blob(draw, 500, 680, 700, 160, (245, 248, 252))
    draw.rectangle(s(0, 680, S, S), fill=(245, 248, 252))
    # A frozen pond
    blob(draw, 700, 900, 220, 55, (180, 225, 245))
    return image


def farm():
    image, draw = canvas()
    gradient(draw, 0, 520, (150, 210, 245), (215, 240, 250))
    blob(draw, 300, 560, 520, 150, (150, 200, 100))
    blob(draw, 800, 570, 480, 160, (135, 190, 90))
    gradient(draw, 520, S, (165, 210, 110), (120, 175, 80))
    # The barn and the fence
    draw.rectangle(s(420, 300, 640, 520), fill=(200, 60, 50))
    draw.polygon(s(400, 310, 530, 210, 660, 310), fill=(150, 45, 40))
    draw.rectangle(s(495, 410, 565, 520), fill=(245, 230, 210))
    for x in range(0, S, 60):
        draw.rectangle(s(x, 490, x + 14, 570), fill=(205, 170, 120))
    draw.rectangle(s(0, 505, S, 515), fill=(205, 170, 120))
    draw.rectangle(s(0, 540, S, 550), fill=(205, 170, 120))
    return image


DRAW = {'house': house, 'camp': camp, 'concert': concert, 'kitchen': kitchen, 'oasis': oasis, 'sea': sea,
        'snow': snow, 'farm': farm}


def emoji(code, size):
    """The object, its empty margin cut, its biggest side of this size, centered on a square of this size."""
    item = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    item = item.crop(item.getbbox())
    scale = size / max(item.size)
    item = item.resize((max(1, round(item.width * scale)), max(1, round(item.height * scale))), Image.LANCZOS)
    square = Image.new('RGBA', (size, size))
    square.paste(item, ((size - item.width) // 2, (size - item.height) // 2))
    return square


def recolor(image):
    """Turns the colors of an object (not the black outline nor the greys) by a third of the color wheel."""
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, sat, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if sat > 0.15:
                r, g, b = colorsys.hsv_to_rgb((h + 1 / 3) % 1, sat, v)
                pixels[x, y] = (int(r * 255), int(g * 255), int(b * 255), a)
    return image


def picture(background, objects, changes):
    """The scene with its objects; changes: index of an object -> 'remove', 'swap' or 'color'."""
    image = background.copy()
    for index, (code, x, y, size, other) in enumerate(objects):
        change = changes.get(index)
        if change == 'remove':
            continue
        item = emoji(other if change == 'swap' else code, size)
        if change == 'color':
            item = recolor(item)
        image.paste(item, (x - size // 2, y - size // 2), item)
    return image


def save(image, path):
    # Few colors: a palette keeps the files small
    image.convert('RGB').quantize(colors=256, method=Image.Quantize.MEDIANCUT).save(path, optimize=True)


instances = []
for scene, objects in SCENES.items():
    background = DRAW[scene]().resize((S, S), Image.LANCZOS)
    save(picture(background, objects, {}), os.path.join(out, scene + '.png'))
    by_size = sorted(range(len(objects)), key=lambda i: -objects[i][3])
    levels = {}
    for level, (count, pick, kinds) in LEVELS.items():
        chosen = pick(by_size)
        assert len(chosen) == count, (scene, level)
        changes = {}
        for index, kind in zip(chosen, kinds):
            changes[index] = 'color' if kind == 'swap' and objects[index][4] is None else kind
        name = scene + '-' + level + '.png'
        save(picture(background, objects, changes), os.path.join(out, name))
        levels[level.upper()] = {'image2': name, 'differences': [
            {'x': objects[i][1], 'y': objects[i][2], 'radius': max(60, round(objects[i][3] * 0.6))}
            for i in sorted(chosen)]}
    instances.append({'scene': scene, 'image1': scene + '.png', 'levels': levels})
with open(os.path.join(out, 'instances.json'), 'w', encoding='utf-8') as f:
    json.dump(instances, f, indent=2)
print('ok')
