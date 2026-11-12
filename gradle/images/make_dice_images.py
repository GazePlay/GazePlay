"""Draws the texture of the 3D die (gazeplay-core: data/common/images/dice.png), shared by "Dés" (Dice), the horses and
the goose game (DiceRoller), made for GazePlay Learning on 12/11/2026 in place of a texture of unknown source. Same
license as GazePlay Learning (GPL-3.0-or-later).

python make_dice_images.py <data/common/images folder> [<data/common/Thumbnails folder> <Fredoka-SemiBold.ttf>]
dice.png: 1800 x 1800, a grid of 3 x 3 cells of 600 pixels; the six faces are in the cells the texture coordinates of
DiceRoller expect (row 0: 4 in the middle; row 1: 2, 1, 5; row 2: 3 in the middle, 6 at the right), the other cells
blank. Warm white faces with a light edge, round dark pips with a soft shading.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

SIZE = 1800
CELL = 600
SCALE = 2
FACE = (251, 250, 246)
EDGE = (222, 217, 206)
PIP = (38, 34, 47)
PIP_LIGHT = (78, 72, 92)

# The pips of each face, in thirds of a cell (0 = near the top or left edge, 1 = middle, 2 = far edge)
PIPS = {
    1: [(1, 1)],
    2: [(2, 0), (0, 2)],
    3: [(2, 0), (1, 1), (0, 2)],
    4: [(0, 0), (2, 0), (0, 2), (2, 2)],
    5: [(0, 0), (2, 0), (1, 1), (0, 2), (2, 2)],
    6: [(0, 0), (2, 0), (0, 1), (2, 1), (0, 2), (2, 2)],
}
# The cell (column, row) of each face
CELLS = {4: (1, 0), 2: (0, 1), 1: (1, 1), 5: (2, 1), 3: (1, 2), 6: (2, 2)}
OFFSETS = (121, 301, 481)
RADIUS = 62


def draw():
    s = SCALE
    image = Image.new('RGB', (SIZE * s, SIZE * s), FACE)
    pen = ImageDraw.Draw(image)
    for face, (column, row) in CELLS.items():
        x0, y0 = column * CELL * s, row * CELL * s
        # A light edge, so that the sides of the cube show
        pen.rectangle((x0, y0, x0 + CELL * s - 1, y0 + CELL * s - 1), outline=EDGE, width=16 * s)
        for (px, py) in PIPS[face]:
            cx = x0 + OFFSETS[px] * s
            cy = y0 + OFFSETS[py] * s
            r = RADIUS * s
            pen.ellipse((cx - r, cy - r, cx + r, cy + r), fill=PIP)
            # A soft light at the top left of each pip
            hr = r * 0.42
            pen.ellipse((cx - r * 0.55 - hr / 2, cy - r * 0.55 - hr / 2, cx - r * 0.55 + hr, cy - r * 0.55 + hr),
                        fill=PIP_LIGHT)
    image = image.filter(ImageFilter.GaussianBlur(1.2 * s))
    return image.resize((SIZE, SIZE), Image.LANCZOS)


def face_image(texture, face):
    column, row = CELLS[face]
    return texture.crop((column * CELL, row * CELL, (column + 1) * CELL, (row + 1) * CELL)).convert('RGBA')


def paste_face(canvas, image, origin, u, v, shade):
    """Puts a face on the parallelogram origin + s*u + t*v (s, t from 0 to 1), darkened by shade (0 to 1)."""
    w, h = canvas.size
    det = u[0] * v[1] - u[1] * v[0]
    # Inverse of the affine map, from the canvas to the face image
    a = v[1] / det * CELL
    b = -v[0] / det * CELL
    d = -u[1] / det * CELL
    e = u[0] / det * CELL
    c = -(a * origin[0] + b * origin[1])
    f = -(d * origin[0] + e * origin[1])
    if shade > 0:
        image = Image.blend(image, Image.new('RGBA', image.size, (60, 50, 40, 255)), shade)
    layer = image.transform((w, h), Image.AFFINE, (a, b, c, d, e, f), resample=Image.BICUBIC)
    mask = Image.new('L', (w, h), 0)
    corners = [origin, (origin[0] + u[0], origin[1] + u[1]),
               (origin[0] + u[0] + v[0], origin[1] + u[1] + v[1]), (origin[0] + v[0], origin[1] + v[1])]
    ImageDraw.Draw(mask).polygon(corners, fill=255)
    canvas.paste(layer, (0, 0), mask)


def cube(canvas, texture, cx, cy, side, top, left, right):
    """An isometric die: its top, left and right faces."""
    k = side * 0.866
    shadow = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).ellipse((cx - side * 1.05, cy + side * 0.75, cx + side * 1.15, cy + side * 1.25),
                                   fill=(0, 0, 0, 70))
    canvas.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(side * 0.08)))
    paste_face(canvas, face_image(texture, top), (cx - k, cy - side / 2), (k, -side / 2), (k, side / 2), 0)
    paste_face(canvas, face_image(texture, left), (cx - k, cy - side / 2), (k, side / 2), (0, side), 0.12)
    paste_face(canvas, face_image(texture, right), (cx, cy), (k, -side / 2), (0, side), 0.25)


def thumbnail(texture, font_path):
    from PIL import ImageFont
    w, h = 960 * SCALE, 540 * SCALE
    canvas = Image.new('RGBA', (w, h), (34, 88, 58, 255))
    # The green felt, lighter in the middle
    glow = Image.new('L', (w, h), 0)
    ImageDraw.Draw(glow).ellipse((-w * 0.25, -h * 0.35, w * 1.25, h * 1.25), fill=255)
    glow = glow.filter(ImageFilter.GaussianBlur(h * 0.25))
    canvas = Image.composite(Image.new('RGBA', (w, h), (64, 142, 96, 255)), canvas, glow)
    pen = ImageDraw.Draw(canvas)
    inset = h * 0.035
    for x in range(int(inset * 2), int(w - inset * 2), int(h * 0.03)):
        pen.line((x, inset, x + h * 0.018, inset), fill=(120, 175, 140), width=3 * SCALE)
        pen.line((x, h - inset, x + h * 0.018, h - inset), fill=(120, 175, 140), width=3 * SCALE)
    side = h * 0.17
    for i, faces in enumerate(((5, 2, 3), (6, 3, 2), (3, 6, 5))):
        cube(canvas, texture, w / 2 + (i - 1) * side * 2.4, h * 0.36, side, *faces)
    font = ImageFont.truetype(font_path, int(h * 0.12))
    card_w, card_h, gap = h * 0.27, h * 0.25, h * 0.08
    left = (w - 3 * card_w - 2 * gap) / 2
    top = h * 0.68
    for i, (value, lit) in enumerate(((13, False), (14, True), (12, False))):
        x = left + i * (card_w + gap)
        pen.rounded_rectangle((x, top, x + card_w, top + card_h), radius=card_w * 0.09,
                              fill=(255, 238, 170) if lit else (255, 255, 255), outline=(196, 204, 220),
                              width=4 * SCALE)
        text = str(value)
        box = pen.textbbox((0, 0), text, font=font)
        pen.text((x + (card_w - (box[2] - box[0])) / 2 - box[0], top + (card_h - (box[3] - box[1])) / 2 - box[1]),
                 text, font=font, fill=(48, 54, 72))
        if lit:
            # The fixation ring, from the top, clockwise
            r = card_w * 0.46
            cx, cy = x + card_w / 2, top + card_h / 2
            pen.ellipse((cx - r, cy - r, cx + r, cy + r), outline=(255, 250, 235), width=int(r * 0.13))
            pen.arc((cx - r, cy - r, cx + r, cy + r), -90, 150, fill=(242, 106, 60), width=int(r * 0.13))
    return canvas.convert('RGB').resize((960, 540), Image.LANCZOS)


folder = sys.argv[1]
os.makedirs(folder, exist_ok=True)
texture = draw()
texture.save(os.path.join(folder, 'dice.png'), optimize=True)
if len(sys.argv) > 3:
    # python make_dice_images.py <data/common/images> <data/common/Thumbnails> <Fredoka font>: also the thumbnail of
    # "Dés" (960 x 540), made of these faces: three dice on the felt and the three cards of the counting mode
    thumbnail(texture, sys.argv[3]).save(os.path.join(sys.argv[2], 'dice.png'), optimize=True)
