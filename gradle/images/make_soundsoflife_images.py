"""Draws the scenes of Sounds Of Life (gazeplay-games: data/soundsoflife/<scene>), made for GazePlay Learning in
October 2026 to replace pictures of unknown source (some from picture banks). Same license as GazePlay Learning
(GPL-3.0-or-later) for the backgrounds; the animals are OpenMoji (CC BY-SA 4.0).

python make_soundsoflife_images.py <folder of the 618x618 PNG files of OpenMoji> <data/soundsoflife folder>
Pillow is required. For each scene: background.png (1920 x 1080, drawn here), the animals (<name>.png, 500 x 500,
transparent, from OpenMoji) and elements.json (the background, then each animal: picture, scale, center on the
background, sounds, name). The sounds (<name>.mp3) are installed separately (see the CREDITS.txt of the folder).
"""
import json
import math
import os
import sys

from PIL import Image, ImageDraw

W, H, K = 1920, 1080, 2  # drawn at scale 2, then reduced

SCENES = {
    # name: [(animal, OpenMoji code point, center x, center y, scale, [sounds])]
    'farm': [('sheep', '1F411', 430, 720, 0.55, ['sheep.mp3']), ('cow', '1F404', 1330, 700, 0.6, ['cow.mp3']),
             ('horse', '1F40E', 1680, 600, 0.55, ['horse.mp3']), ('pig', '1F416', 930, 880, 0.5, ['pig.mp3']),
             ('rooster', '1F413', 200, 900, 0.42, ['rooster.mp3']), ('hen', '1F414', 620, 930, 0.42, ['hen.mp3'])],
    'jungle': [('monkey', '1F412', 420, 330, 0.42, ['monkey.mp3']), ('parrot', '1F99C', 1580, 270, 0.4, ['parrot.mp3']),
               ('elephant', '1F418', 1480, 740, 0.7, ['elephant.mp3']), ('leopard', '1F406', 860, 660, 0.55,
               ['leopard.mp3']), ('snake', '1F40D', 300, 860, 0.45, ['snake.mp3']), ('frog', '1F438', 940, 930,
               0.4, ['frog.mp3'])],
    'savanna': [('lion', '1F981', 450, 720, 0.5, ['lion.mp3']), ('zebra', '1F993', 960, 680, 0.55, ['zebra.mp3']),
                ('giraffe', '1F992', 1500, 560, 0.85, ['giraffe.mp3']), ('crocodile', '1F40A', 1140, 960, 0.55,
                ['crocodile.mp3'])],
    'sea': [('dolphin', '1F42C', 560, 300, 0.55, ['dolphin.mp3']), ('whale', '1F433', 1330, 320, 0.75, ['whale.mp3']),
            ('fish', '1F420', 960, 600, 0.46, ['fish.mp3']), ('octopus', '1F419', 400, 830, 0.48, ['octopus.mp3']),
            ('crab', '1F980', 1460, 900, 0.46, ['crab.mp3'])],
}

src, out = sys.argv[1], sys.argv[2]


def canvas():
    image = Image.new('RGB', (W * K, H * K))
    return image, ImageDraw.Draw(image)


def s(*values):
    return [v * K for v in values]


def gradient(draw, top, bottom, color_top, color_bottom):
    for y in range(int(top * K), int(bottom * K)):
        t = (y - top * K) / max(1, (bottom - top) * K)
        draw.line([(0, y), (W * K, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(color_top, color_bottom)))


def blob(draw, cx, cy, rx, ry, color):
    draw.ellipse(s(cx - rx, cy - ry, cx + rx, cy + ry), fill=color)


def farm():
    image, draw = canvas()
    gradient(draw, 0, 560, (150, 210, 245), (215, 240, 250))
    blob(draw, 1650, 150, 70, 70, (255, 214, 92))
    for cx, cy in ((300, 140), (900, 110)):
        for dx, r in ((-60, 45), (0, 62), (60, 45)):
            blob(draw, cx + dx, cy, r, r * 0.8, (255, 255, 255))
    # Hills, then the meadow
    blob(draw, 400, 640, 700, 200, (150, 200, 100))
    blob(draw, 1500, 650, 800, 220, (135, 190, 90))
    gradient(draw, 560, 1080, (165, 210, 110), (120, 175, 80))
    # The barn
    draw.rectangle(s(1000, 330, 1260, 560), fill=(200, 60, 50))
    draw.polygon(s(980, 340, 1130, 230, 1280, 340), fill=(150, 45, 40))
    draw.rectangle(s(1090, 430, 1170, 560), fill=(245, 230, 210))
    draw.line(s(1090, 430, 1170, 560), fill=(200, 60, 50), width=8 * K)
    draw.line(s(1170, 430, 1090, 560), fill=(200, 60, 50), width=8 * K)
    # The fence
    for x in range(0, W, 70):
        draw.rectangle(s(x, 520, x + 16, 600), fill=(205, 170, 120))
    draw.rectangle(s(0, 535, W, 547), fill=(205, 170, 120))
    draw.rectangle(s(0, 570, W, 582), fill=(205, 170, 120))
    # A path
    draw.polygon(s(820, 1080, 1080, 1080, 1010, 600, 960, 600), fill=(225, 200, 150))
    return image


def jungle():
    image, draw = canvas()
    gradient(draw, 0, 760, (60, 120, 80), (150, 190, 110))
    # Trees far away, then near trunks with leaves
    for i, x in enumerate(range(-100, W + 200, 230)):
        blob(draw, x, 300 + (i % 2) * 60, 190, 230, (70, 130, 80) if i % 2 else (80, 145, 85))
    for x, width in ((140, 70), (700, 50), (1250, 60), (1800, 80)):
        draw.rectangle(s(x - width / 2, 150, x + width / 2, 800), fill=(110, 80, 55))
        blob(draw, x, 150, 230, 140, (45, 110, 60))
    # The branch of the monkey
    draw.line(s(140, 420, 640, 390), fill=(110, 80, 55), width=22 * K)
    # Vines
    for x in (400, 980, 1520):
        points = [(x + 25 * math.sin(y / 60), y) for y in range(0, 460, 20)]
        draw.line([tuple(v * K for v in p) for p in points], fill=(60, 140, 60), width=8 * K)
    gradient(draw, 760, 1080, (110, 160, 70), (80, 130, 55))
    # Big leaves in front
    for x, y, r in ((60, 1000, 160), (1860, 980, 180), (1100, 1060, 120)):
        blob(draw, x, y, r, r * 0.55, (40, 115, 55))
    return image


def savanna():
    image, draw = canvas()
    gradient(draw, 0, 600, (250, 200, 120), (253, 235, 190))
    blob(draw, 1760, 170, 110, 110, (255, 170, 70))
    blob(draw, 900, 620, 900, 120, (220, 175, 110))
    gradient(draw, 600, 1080, (230, 195, 110), (205, 165, 80))
    # Acacias: a thin trunk and a flat top
    for x, y, size in ((250, 560, 1.0), (1780, 520, 1.2), (1150, 580, 0.6)):
        draw.line(s(x, y, x, y + 150 * size), fill=(110, 80, 50), width=int(16 * size) * K)
        draw.ellipse(s(x - 170 * size, y - 50 * size, x + 170 * size, y + 20 * size), fill=(110, 140, 60))
    # A water hole for the crocodile
    blob(draw, 1140, 980, 330, 80, (110, 170, 200))
    # Tufts of grass
    for i in range(60):
        x = (i * 157) % W
        y = 700 + (i * 89) % 360
        for dx in (-8, 0, 8):
            draw.line(s(x, y, x + dx * 2, y - 26), fill=(185, 145, 60), width=3 * K)
    return image


def sea():
    image, draw = canvas()
    gradient(draw, 0, 1080, (90, 180, 225), (20, 70, 130))
    # Rays of light from the surface
    for x in (300, 800, 1400):
        draw.polygon(s(x, 0, x + 120, 0, x + 380, 1080, x + 180, 1080), fill=(110, 190, 230))
    gradient(draw, 960, 1080, (225, 205, 150), (205, 185, 130))
    blob(draw, 960, 975, 1100, 60, (225, 205, 150))
    # Seaweed and corals
    for x in (120, 700, 1180, 1800):
        points = [(x + 20 * math.sin(y / 40), y) for y in range(990, 700, -15)]
        draw.line([tuple(v * K for v in p) for p in points], fill=(60, 150, 90), width=14 * K)
    for x, color in ((250, (240, 110, 100)), (1650, (250, 160, 90)), (980, (230, 90, 140))):
        for angle in (-40, -15, 15, 40):
            dx = 90 * math.sin(math.radians(angle))
            draw.line(s(x, 1000, x + dx, 880), fill=color, width=14 * K)
    # Bubbles
    for i in range(18):
        x = 100 + (i * 263) % 1720
        y = 120 + (i * 151) % 700
        r = 6 + i % 4 * 4
        draw.ellipse(s(x - r, y - r, x + r, y + r), outline=(220, 240, 255), width=3 * K)
    return image


DRAW = {'farm': farm, 'jungle': jungle, 'savanna': savanna, 'sea': sea}

for scene, animals in SCENES.items():
    folder = os.path.join(out, scene)
    os.makedirs(folder, exist_ok=True)
    DRAW[scene]().resize((W, H), Image.LANCZOS).save(os.path.join(folder, 'background.png'), optimize=True)
    elements = []
    for name, code, x, y, scale, sounds in animals:
        emoji = Image.open(os.path.join(src, code + '.png')).convert('RGBA').resize((500, 500), Image.LANCZOS)
        emoji.save(os.path.join(folder, name + '.png'), optimize=True)
        elements.append({'image': name + '.png', 'scale': scale, 'coords': {'x': x, 'y': y}, 'sounds': sounds,
                         'name': 'SoundsOfLife_' + name})
    with open(os.path.join(folder, 'elements.json'), 'w', encoding='utf-8') as f:
        json.dump({'background': 'background.png', 'elements': elements}, f, indent=2)
print('ok')
