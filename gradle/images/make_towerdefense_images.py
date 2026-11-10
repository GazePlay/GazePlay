"""Draws the pictures of "Tower Defense" (gazeplay-games: data/towerDefense/images), made for GazePlay Learning on
3/11/2026 to replace pictures whose source was not known (maintainer's decision: everything redrawn, in the style of
the other reviewed games). Same license as GazePlay Learning (GPL-3.0-or-later).
- Tiles (128 x 128, drawn side by side by the game): grass.png, dirt.png (the road), towerBase.png (a place for a
  tower), castle.png (the end of the road), arrow1.png and arrow2.png (the start of the road, blinking arrows).
- Towers seen from above, their barrels up (the game turns them): basicTower.png, doubleTower.png, missileTower.png,
  canonTower.png; missile.png (up).
- Enemies seen from above, going right (the game turns them): basicEnemy.png (a green slime), fastEnemy.png (a
  small yellow one), tankEnemy.png (a big violet armored one).
- explosion.png (768 x 96): the explosion of a missile, 8 pictures of 96 x 96.
- Icons (384 x 384): money.png (coins), moneyGain.png and moneyLoss.png (coins, + and -),
  sellBasic.png, sellDouble.png, sellMissile.png, sellCanon.png (selling a tower: that tower, an arrow, coins),
  wave.png (a wave of enemies), blizzardIcon.png (a snowflake).
- With one more argument, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/TowerDefense.png,
  960 x 540): the first map with towers and enemies.

python make_towerdefense_images.py <data/towerDefense/images folder> [<thumbnail.png>]
Pillow is required.
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
K = 4
INK = (45, 40, 60, 255)


def canvas(size):
    picture = Image.new('RGBA', (size * K, size * K), (0, 0, 0, 0))
    return picture, ImageDraw.Draw(picture)


def s(*values):
    return [v * K for v in values]


def save(picture, size, name):
    picture.resize((size, size), Image.LANCZOS).save(os.path.join(out, name))


def darker(color, amount=0.7):
    return tuple(int(c * amount) for c in color[:3]) + (255,)


def lighter(color, amount=0.4):
    return tuple(int(c + (255 - c) * amount) for c in color[:3]) + (255,)


GRASS = (150, 205, 115)
DIRT = (225, 190, 135)


def grass(draw, seed):
    draw.rectangle(s(0, 0, 128, 128), fill=GRASS + (255,))
    rnd = random.Random(seed)
    for _ in range(9):
        x, y = rnd.uniform(8, 120), rnd.uniform(8, 120)
        for dx in (-3, 0, 3):
            draw.line(s(x + dx, y + 4, x + dx * 1.6, y - 3), fill=(125, 180, 95, 255), width=2 * K)


def dirt(draw, seed):
    draw.rectangle(s(0, 0, 128, 128), fill=DIRT + (255,))
    rnd = random.Random(seed)
    for _ in range(7):
        x, y, r = rnd.uniform(10, 118), rnd.uniform(10, 118), rnd.uniform(3, 6)
        draw.ellipse(s(x - r, y - r * 0.7, x + r, y + r * 0.7), fill=(205, 165, 110, 255))


picture, draw = canvas(128)
grass(draw, 1)
save(picture, 128, 'grass.png')

picture, draw = canvas(128)
dirt(draw, 2)
save(picture, 128, 'dirt.png')

picture, draw = canvas(128)
grass(draw, 3)
draw.rounded_rectangle(s(12, 12, 116, 116), radius=22 * K, fill=(200, 200, 210, 255), outline=(150, 150, 165, 255),
                       width=5 * K)
draw.rounded_rectangle(s(30, 30, 98, 98), radius=14 * K, fill=(220, 220, 228, 255))
save(picture, 128, 'towerBase.png')

for name, phase in (('arrow1.png', 0), ('arrow2.png', 1)):
    picture, draw = canvas(128)
    dirt(draw, 4)
    for i, y in enumerate((30, 72)):
        color = (255, 255, 255, 255) if (i + phase) % 2 == 0 else (255, 255, 255, 110)
        draw.line(s(36, y, 64, y + 24, 92, y), fill=color, width=11 * K, joint='curve')
    save(picture, 128, name)

# The castle: towers with blue roofs and flags, a door, on the grass
picture, draw = canvas(128)
grass(draw, 5)
wall, line = (235, 230, 225, 255), (150, 140, 135, 255)
draw.rectangle(s(22, 52, 106, 118), fill=wall, outline=line, width=3 * K)
for x in (14, 90):
    draw.rectangle(s(x, 40, x + 24, 118), fill=wall, outline=line, width=3 * K)
    draw.polygon(s(x - 3, 42, x + 27, 42, x + 12, 16), fill=(110, 170, 235, 255), outline=(70, 120, 190, 255))
    draw.line(s(x + 12, 16, x + 12, 5), fill=line, width=2 * K)
    draw.polygon(s(x + 12, 5, x + 24, 9, x + 12, 13), fill=(240, 105, 110, 255))
draw.polygon(s(46, 54, 82, 54, 64, 30), fill=(110, 170, 235, 255), outline=(70, 120, 190, 255))
for x in range(26, 104, 12):
    draw.rectangle(s(x, 46, x + 7, 53), fill=wall, outline=line, width=2 * K)
draw.chord(s(50, 82, 78, 130), 180, 360, fill=(140, 95, 60, 255))
draw.rectangle(s(50, 105, 78, 118), fill=(140, 95, 60, 255))
for x in (36, 92):
    draw.rounded_rectangle(s(x - 4, 64, x + 4, 76), radius=3 * K, fill=(90, 110, 150, 255))
save(picture, 128, 'castle.png')


def tower(color, barrels, wide=False, missiles=False):
    """A turret seen from above: a round body of the color, the barrels up."""
    picture, draw = canvas(128)
    line = darker(color, 0.6)
    if missiles:
        for x in (44, 84):
            draw.rounded_rectangle(s(x - 9, 14, x + 9, 64), radius=9 * K, fill=(245, 245, 250, 255),
                                   outline=(120, 120, 140, 255), width=3 * K)
            draw.polygon(s(x - 9, 24, x, 6, x + 9, 24), fill=(240, 105, 110, 255))
    for x in barrels:
        width = 15 if wide else 9
        draw.rounded_rectangle(s(x - width, 8, x + width, 66), radius=5 * K, fill=(150, 150, 165, 255),
                               outline=(95, 95, 115, 255), width=3 * K)
        draw.rectangle(s(x - width - 2, 8, x + width + 2, 18), fill=(110, 110, 128, 255))
    draw.ellipse(s(26, 40, 102, 116), fill=color + (255,), outline=line, width=5 * K)
    draw.ellipse(s(42, 56, 86, 100), fill=lighter(color, 0.35))
    draw.ellipse(s(56, 70, 72, 86), fill=line)
    return picture


save(tower((120, 200, 120), (64,)), 128, 'basicTower.png')
save(tower((240, 110, 110), (52, 76)), 128, 'doubleTower.png')
save(tower((150, 160, 180), (), missiles=True), 128, 'missileTower.png')
save(tower((110, 150, 230), (64,), wide=True), 128, 'canonTower.png')

picture, draw = canvas(128)
draw.rounded_rectangle(s(54, 26, 74, 100), radius=10 * K, fill=(245, 245, 250, 255), outline=(120, 120, 140, 255),
                       width=3 * K)
draw.polygon(s(54, 40, 64, 14, 74, 40), fill=(240, 105, 110, 255))
draw.polygon(s(54, 88, 44, 106, 54, 100), fill=(240, 105, 110, 255))
draw.polygon(s(74, 88, 84, 106, 74, 100), fill=(240, 105, 110, 255))
draw.polygon(s(58, 100, 64, 122, 70, 100), fill=(255, 190, 80, 255))
save(picture, 128, 'missile.png')


def enemy(color, size, spikes=False, armor=False):
    """An enemy seen from above, going right: a round body, two eyes on its front (the right)."""
    picture, draw = canvas(128)
    line = darker(color, 0.6)
    r = size / 2
    if spikes:
        for a in range(8):
            angle = math.radians(a * 45 + 22)
            draw.polygon(s(64 + (r + 8) * math.cos(angle), 64 + (r + 8) * math.sin(angle),
                           64 + r * math.cos(angle + 0.3), 64 + r * math.sin(angle + 0.3),
                           64 + r * math.cos(angle - 0.3), 64 + r * math.sin(angle - 0.3)), fill=line)
    draw.ellipse(s(64 - r, 64 - r, 64 + r, 64 + r), fill=color + (255,), outline=line, width=4 * K)
    if armor:
        for dx in (-r * 0.45, 0, r * 0.45):
            draw.line(s(64 + dx - 4, 64 - r * 0.8, 64 + dx - 4, 64 + r * 0.8), fill=line, width=4 * K)
    draw.ellipse(s(64 - r * 0.6, 64 - r * 0.6, 64 + r * 0.1, 64 - r * 0.05), fill=lighter(color, 0.4))
    for dy in (-r * 0.38, r * 0.38):
        ex, ey = 64 + r * 0.5, 64 + dy
        draw.ellipse(s(ex - r * 0.24, ey - r * 0.24, ex + r * 0.24, ey + r * 0.24), fill=(255, 255, 255, 255),
                     outline=INK, width=2 * K)
        draw.ellipse(s(ex - r * 0.02, ey - r * 0.11, ex + r * 0.2, ey + r * 0.11), fill=INK)
    return picture


save(enemy((120, 205, 110), 70), 128, 'basicEnemy.png')
save(enemy((250, 210, 80), 56, spikes=True), 128, 'fastEnemy.png')
save(enemy((170, 120, 210), 92, armor=True), 128, 'tankEnemy.png')

# The explosion: 8 pictures, a ball of fire growing then fading in smoke
sheet = Image.new('RGBA', (768 * K // 4, 96 * K // 4), (0, 0, 0, 0))
for i in range(8):
    frame = Image.new('RGBA', (96 * K, 96 * K), (0, 0, 0, 0))
    draw = ImageDraw.Draw(frame)
    t = (i + 1) / 8
    radius = 12 + 32 * min(1.0, t * 1.6)
    alpha = int(255 * (1 - max(0.0, t - 0.5) * 1.6))
    rnd = random.Random(i)
    for color, scale in (((240, 120, 80), 1.0), ((255, 190, 90), 0.72), ((255, 240, 170), 0.42)):
        if t > 0.7:
            color = (150, 140, 150)
        for _ in range(5):
            dx, dy = rnd.uniform(-8, 8) * scale, rnd.uniform(-8, 8) * scale
            r = radius * scale * rnd.uniform(0.7, 1.0)
            draw.ellipse(s(48 + dx - r, 48 + dy - r, 48 + dx + r, 48 + dy + r), fill=color + (max(0, alpha),))
    sheet.paste(frame.resize((96 * K // 4, 96 * K // 4), Image.LANCZOS), (i * 96 * K // 4, 0))
sheet.resize((768, 96), Image.LANCZOS).save(os.path.join(out, 'explosion.png'))


def icon_canvas():
    return canvas(384)


def coins(draw, x, y):
    for i in range(3):
        cy = y - i * 34
        draw.ellipse(s(x - 90, cy - 36, x + 90, cy + 36), fill=(250, 200, 70, 255), outline=(200, 140, 40, 255),
                     width=8 * K)
    draw.ellipse(s(x - 55, y - 68 - 22, x + 55, y - 68 + 22), outline=(255, 235, 150, 255), width=6 * K)


# heart.png (the lives) removed on 10/11/2026: the lives are drawn by the game, as in the other reviewed games

picture, draw = icon_canvas()
coins(draw, 192, 280)
save(picture, 384, 'money.png')

for name, color, plus in (('moneyGain.png', (70, 190, 100), True), ('moneyLoss.png', (230, 80, 80), False)):
    picture, draw = icon_canvas()
    coins(draw, 160, 290)
    draw.ellipse(s(232, 30, 362, 160), fill=color + (255,), outline=(255, 255, 255, 255), width=8 * K)
    draw.rounded_rectangle(s(257, 85, 337, 105), radius=8 * K, fill=(255, 255, 255, 255))
    if plus:
        draw.rounded_rectangle(s(287, 55, 307, 135), radius=8 * K, fill=(255, 255, 255, 255))
    save(picture, 384, name)

# Selling a tower (maintainer's requests of 3/11/2026: the picture must show the action, with the tower sold): the
# tower becomes coins, the tower on a white badge, an arrow, the coins; one picture for each tower
for name, turret in (('sellBasic.png', tower((120, 200, 120), (64,))),
                     ('sellDouble.png', tower((240, 110, 110), (52, 76))),
                     ('sellMissile.png', tower((150, 160, 180), (), missiles=True)),
                     ('sellCanon.png', tower((110, 150, 230), (64,), wide=True))):
    picture, draw = icon_canvas()
    draw.ellipse(s(6, 6, 378, 378), fill=(255, 255, 255, 235), outline=(70, 190, 100, 255), width=10 * K)
    # The body of the tower, the arrow and the middle of the coins on one line, at 200 (maintainer's remark of
    # 3/11/2026: the coins looked lower); the body of a turret is at 78 of its 128 pixels
    middle = 200
    turret_top = middle - 78 * 170 / 128
    picture.alpha_composite(turret.resize((170 * K, 170 * K), Image.LANCZOS), (int(18 * K), int(turret_top * K)))
    draw = ImageDraw.Draw(picture)
    draw.rounded_rectangle(s(172, middle - 12, 222, middle + 12), radius=10 * K, fill=(70, 190, 100, 255))
    draw.polygon(s(214, middle - 36, 252, middle, 214, middle + 36), fill=(70, 190, 100, 255))
    for i in range(3):
        cy = middle + 30 - i * 30
        draw.ellipse(s(250, cy - 30, 360, cy + 30), fill=(250, 200, 70, 255), outline=(200, 140, 40, 255),
                     width=6 * K)
    draw.ellipse(s(278, middle - 45, 332, middle - 15), outline=(255, 235, 150, 255), width=5 * K)
    save(picture, 384, name)

picture, draw = icon_canvas()
for i, (x, y) in enumerate(((90, 230), (192, 170), (294, 230))):
    body = enemy((120, 205, 110), 70).resize((150 * K, 150 * K), Image.LANCZOS)
    picture.alpha_composite(body, (int((x - 75) * K), int((y - 75) * K)))
draw = ImageDraw.Draw(picture)
draw.arc(s(30, 250, 354, 380), 200, 340, fill=(110, 170, 235, 255), width=12 * K)
save(picture, 384, 'wave.png')

picture, draw = icon_canvas()
draw.ellipse(s(10, 10, 374, 374), fill=(225, 240, 255, 255), outline=(120, 180, 240, 255), width=10 * K)
for a in range(6):
    angle = math.radians(a * 60)
    x2, y2 = 192 + 135 * math.cos(angle), 192 + 135 * math.sin(angle)
    draw.line(s(192, 192, x2, y2), fill=(80, 150, 230, 255), width=16 * K)
    for side in (-1, 1):
        bx, by = 192 + 85 * math.cos(angle), 192 + 85 * math.sin(angle)
        draw.line(s(bx, by, bx + 45 * math.cos(angle + side * 0.8), by + 45 * math.sin(angle + side * 0.8)),
                  fill=(80, 150, 230, 255), width=12 * K)
draw.ellipse(s(170, 170, 214, 214), fill=(80, 150, 230, 255))
save(picture, 384, 'blizzardIcon.png')

MAP_ONE = [
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0],
    [0, 0, 0, 0, 1, 2, 0, 2, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1, 2, 0],
    [0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1],
    [0, 0, 0, 2, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1],
    [9, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 2, 1, 1, 1],
    [0, 0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 2, 1, 0, 1, 0, 0, 1, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
]


def thumbnail(path):
    """The first map, with towers and enemies on the road."""
    tile = 48
    image = Image.new('RGBA', (20 * tile, 11 * tile + 12))
    names = {0: 'grass.png', 1: 'dirt.png', 2: 'towerBase.png', 8: 'arrow1.png', 9: 'castle.png'}
    pictures = {key: Image.open(os.path.join(out, name)).convert('RGBA').resize((tile, tile), Image.LANCZOS)
                for key, name in names.items()}
    for row, cells in enumerate(MAP_ONE):
        for col, cell in enumerate(cells):
            image.alpha_composite(pictures[cell], (col * tile, row * tile))

    def paste(name, col, row, angle=0, scale=1.0):
        size = int(tile * scale)
        picture = Image.open(os.path.join(out, name)).convert('RGBA').resize((size, size), Image.LANCZOS)
        picture = picture.rotate(angle, resample=Image.BICUBIC)
        image.alpha_composite(picture, (int((col + 0.5) * tile - size / 2), int((row + 0.5) * tile - size / 2)))

    for name, col, row, angle in (('basicTower.png', 5, 3, 30), ('doubleTower.png', 7, 3, -20),
                                  ('missileTower.png', 13, 3, 40), ('canonTower.png', 16, 6, 90),
                                  ('basicTower.png', 11, 7, -60), ('doubleTower.png', 3, 5, 10)):
        paste(name, col, row, angle)
    for name, col, row, angle in (('basicEnemy.png', 17, 2, -90), ('basicEnemy.png', 17.6, 4.0, 0),
                                  ('tankEnemy.png', 19, 5, -90), ('fastEnemy.png', 14, 7, 90),
                                  ('basicEnemy.png', 12, 5, 90), ('basicEnemy.png', 9, 4, -90)):
        paste(name, col, row, angle)
    draw = ImageDraw.Draw(image)
    for x, y in ((290, 160), (335, 148), (760, 300)):
        draw.ellipse((x - 5, y - 5, x + 5, y + 5), fill=(255, 255, 255), outline=(120, 120, 140))
    image = image.crop((0, 0, 960, 528)).resize((960, 540), Image.LANCZOS)
    image.convert('RGB').save(path)


if len(sys.argv) > 2:
    thumbnail(sys.argv[2])
print('ok')
