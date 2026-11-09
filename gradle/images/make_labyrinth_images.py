"""Draws the pictures of the Labyrinth (gazeplay-games: data/labyrinth/images), made for GazePlay Learning in November
2026 to replace pictures of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).
- mouse.png (400 x 400): a grey mouse seen from above, its nose up; the game turns it the way it goes.
- cheese.png (400 x 400): a piece of cheese with holes.
- house.png (400 x 400): a small house with a red roof.
The hedges and the paths of the labyrinth are drawn by the game.

- With the folder of the 618 x 618 color PNG files of OpenMoji (CC BY-SA 4.0, https://github.com/hfg-gmuend/openmoji),
  the animals without background that replace those of unknown source (gazeplay-games:
  data/common/default/images/noBackGround, 512 x 512): chouette, crabe, ecureuil, herisson, lapin, meduse, poulpe, and a
  penguin and a frog in place of renard.png and tortue.png, which were a second fox and a second turtle.
- With a thumbnail path too, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/labyrinth.png, 960 x 540).

python make_labyrinth_images.py <data/labyrinth/images folder> [<OpenMoji folder> <noBackGround folder> [<thumbnail.png>]]
Pillow is required.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
K = 3
S = 400


def canvas():
    return Image.new('RGBA', (S * K, S * K), (0, 0, 0, 0))


def finish(img, name):
    img.resize((S, S), Image.LANCZOS).save(os.path.join(out, name))


def s(v):
    return v * K


def shadow(img, box, radius):
    mask = Image.new('L', img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(box, radius=radius, fill=80)
    img.paste((30, 40, 30, 255), (0, 0), mask.filter(ImageFilter.GaussianBlur(s(10))))


def mouse():
    img = canvas()
    shadow(img, [s(118), s(96), s(290), s(352)], s(86))
    d = ImageDraw.Draw(img)
    line = (70, 72, 84, 255)
    # The tail, curling down
    d.line([(s(200), s(330)), (s(214), s(362)), (s(246), s(380)), (s(282), s(376)), (s(300), s(358))], fill=(214, 150, 160, 255),
           width=s(10), joint='curve')
    # The body, then the head pointing up
    d.ellipse([s(122), s(150), s(278), s(348)], fill=(176, 180, 192, 255), outline=line, width=s(5))
    d.polygon([(s(200), s(40)), (s(270), s(170)), (s(130), s(170))], fill=(176, 180, 192, 255))
    d.ellipse([s(136), s(110), s(264), s(230)], fill=(176, 180, 192, 255))
    d.line([(s(200), s(40)), (s(136), s(160))], fill=line, width=s(5))
    d.line([(s(200), s(40)), (s(264), s(160))], fill=line, width=s(5))
    # The ears
    for cx in (138, 262):
        d.ellipse([s(cx - 44), s(112), s(cx + 44), s(200)], fill=(176, 180, 192, 255), outline=line, width=s(5))
        d.ellipse([s(cx - 26), s(130), s(cx + 26), s(182)], fill=(240, 182, 192, 255))
    # The eyes, the nose and the whiskers
    for cx in (176, 224):
        d.ellipse([s(cx - 11), s(100), s(cx + 11), s(124)], fill=(34, 36, 44, 255))
        d.ellipse([s(cx - 4), s(104), s(cx + 2), s(111)], fill=(255, 255, 255, 255))
    d.ellipse([s(188), s(32), s(212), s(56)], fill=(232, 120, 140, 255))
    for side in (-1, 1):
        for dy in (-10, 4):
            d.line([(s(200 + side * 16), s(70)), (s(200 + side * 78), s(70 + dy))], fill=line, width=s(2))
    finish(img, 'mouse.png')


def cheese():
    img = canvas()
    shadow(img, [s(56), s(150), s(356), s(330)], s(30))
    d = ImageDraw.Draw(img)
    line = (176, 126, 30, 255)
    # The top, lighter, then the front
    d.polygon([(s(60), s(170)), (s(250), s(96)), (s(350), s(170))], fill=(255, 226, 120, 255), outline=line)
    d.polygon([(s(60), s(170)), (s(350), s(170)), (s(350), s(320)), (s(60), s(320))], fill=(250, 200, 70, 255),
              outline=line)
    d.line([(s(60), s(170)), (s(250), s(96)), (s(350), s(170)), (s(350), s(320)), (s(60), s(320)), (s(60), s(170))],
           fill=line, width=s(6), joint='curve')
    for cx, cy, r in ((120, 220, 22), (210, 260, 30), (300, 215, 18), (290, 285, 14), (110, 290, 12), (200, 140, 12),
                      (270, 138, 9)):
        d.ellipse([s(cx - r), s(cy - r), s(cx + r), s(cy + r)], fill=(214, 156, 40, 255))
    finish(img, 'cheese.png')


def house():
    img = canvas()
    shadow(img, [s(76), s(170), s(330), s(356)], s(16))
    d = ImageDraw.Draw(img)
    line = (90, 60, 50, 255)
    d.rounded_rectangle([s(86), s(176), s(314), s(344)], radius=s(10), fill=(250, 222, 160, 255), outline=line,
                        width=s(6))
    d.polygon([(s(200), s(48)), (s(346), s(186)), (s(54), s(186))], fill=(220, 82, 74, 255))
    d.line([(s(54), s(186)), (s(200), s(48)), (s(346), s(186)), (s(54), s(186))], fill=(150, 44, 44, 255), width=s(7),
           joint='curve')
    d.rounded_rectangle([s(170), s(236), s(230), s(344)], radius=s(8), fill=(150, 92, 64, 255), outline=line,
                        width=s(5))
    d.ellipse([s(214), s(286), s(224), s(296)], fill=(250, 222, 120, 255))
    for x in (104, 248):
        d.rounded_rectangle([s(x), s(214), s(x + 48), s(262)], radius=s(6), fill=(170, 214, 240, 255), outline=line,
                            width=s(5))
        d.line([(s(x + 24), s(214)), (s(x + 24), s(262))], fill=line, width=s(4))
        d.line([(s(x), s(238)), (s(x + 48), s(238))], fill=line, width=s(4))
    finish(img, 'house.png')


mouse()
cheese()
house()

# The animals without background, from OpenMoji, at the size of the others (512 x 512)
LABYRINTH_ANIMALS = {
    'chouette': '1F989', 'crabe': '1F980', 'ecureuil': '1F43F', 'herisson': '1F994', 'lapin': '1F407',
    'meduse': '1FABC', 'poulpe': '1F419', 'penguin': '1F427', 'frog': '1F438',
}
if len(sys.argv) > 3:
    for name, code in LABYRINTH_ANIMALS.items():
        animal = Image.open(os.path.join(sys.argv[2], code + '.png')).convert('RGBA').resize((512, 512), Image.LANCZOS)
        animal.save(os.path.join(sys.argv[3], name + '.png'), optimize=True)


def thumbnail(path):
    """The thumbnail of the menu (960 x 540): a small labyrinth of hedges, the mouse and the cheese."""
    W, H = 960, 540
    image = Image.new('RGBA', (W, H), (150, 196, 120, 255))
    d = ImageDraw.Draw(image)
    rows, cols, cell = 4, 7, 110
    x0, y0 = (W - cols * cell) // 2, (H - rows * cell) // 2
    d.rounded_rectangle([x0, y0, x0 + cols * cell, y0 + rows * cell], radius=18, fill=(244, 232, 200, 255))
    # Walls: (row, col, 'r' for the right side or 'd' for the bottom side)
    walls = [(0, 1, 'd'), (0, 2, 'r'), (0, 4, 'd'), (0, 5, 'r'), (1, 0, 'r'), (1, 1, 'd'), (1, 3, 'r'), (1, 3, 'd'),
             (1, 5, 'd'), (1, 6, 'd'), (2, 1, 'r'), (2, 2, 'd'), (2, 4, 'r'), (2, 5, 'd'), (3, 3, 'r'), (2, 0, 'd')]
    t = 22
    hedge, light = (64, 128, 70, 255), (96, 160, 92, 255)

    def bar(x1, y1, x2, y2):
        d.rounded_rectangle([x1 - t / 2, y1 - t / 2, x2 + t / 2, y2 + t / 2], radius=t / 2, fill=hedge)
        d.rounded_rectangle([x1 - t / 2 + 4, y1 - t / 2 + 4, x2 + t / 2 - 4, y2 + t / 2 - 8], radius=t / 2 - 4,
                            fill=light)

    for r, c, side in walls:
        if side == 'r':
            bar(x0 + (c + 1) * cell, y0 + r * cell, x0 + (c + 1) * cell, y0 + (r + 1) * cell)
        else:
            bar(x0 + c * cell, y0 + (r + 1) * cell, x0 + (c + 1) * cell, y0 + (r + 1) * cell)
    bar(x0, y0, x0 + cols * cell, y0)
    bar(x0, y0 + rows * cell, x0 + cols * cell, y0 + rows * cell)
    bar(x0, y0, x0, y0 + rows * cell)
    bar(x0 + cols * cell, y0, x0 + cols * cell, y0 + rows * cell)

    def put(name, col, row, angle=0):
        picture = Image.open(os.path.join(out, name)).convert('RGBA').resize((96, 96), Image.LANCZOS)
        if angle:
            picture = picture.rotate(angle, resample=Image.BICUBIC)
        image.alpha_composite(picture, (x0 + col * cell + 7, y0 + row * cell + 7))

    put('mouse.png', 0, 0, -90)
    put('cheese.png', 6, 3)
    image.convert('RGB').save(path)


if len(sys.argv) > 4:
    thumbnail(sys.argv[4])
print('ok')
