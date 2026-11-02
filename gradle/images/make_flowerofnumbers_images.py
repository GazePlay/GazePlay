"""Draws the pictures of Flower Of Numbers (gazeplay-games: data/flowerOfNumbers/images), made for GazePlay Learning in
November 2026 to replace pictures of unknown source and reproductions of the euro coins and banknotes. Same license as
GazePlay Learning (GPL-3.0-or-later). The coins and notes are generic drawings: only their value and the euro sign,
no design of the real ones. The flower itself is drawn by the game.

python make_flowerofnumbers_images.py <Fredoka-SemiBold.ttf> <data/flowerOfNumbers/images folder>
Pillow is required. Writes fingers/fingers1..5.png (a hand raising 1 to 5 fingers, 240 x 320), dice/die1..6.png
(300 x 300) and money/money1, 2, 5, 10.png (coins 300 x 300, notes 480 x 260), transparent around.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

K = 4  # drawn at scale 4, then reduced
font_file, out = sys.argv[1], sys.argv[2]

SKIN = (247, 205, 165)
SKIN_LINE = (196, 136, 92)
NAIL = (252, 228, 205)


def save(image, size, *path):
    folder = os.path.join(out, *path[:-1])
    os.makedirs(folder, exist_ok=True)
    image.resize(size, Image.LANCZOS).save(os.path.join(folder, path[-1]), optimize=True)


def font(size):
    return ImageFont.truetype(font_file, size * K)


def centered_text(draw, cx, cy, text, size, fill, stroke=0, stroke_fill=None):
    f = font(size)
    draw.text((cx * K, cy * K), text, font=f, fill=fill, anchor='mm', stroke_width=stroke * K,
              stroke_fill=stroke_fill)


def rounded(draw, box, radius, fill, outline=None, width=0):
    draw.rounded_rectangle([v * K for v in box], radius=radius * K, fill=fill, outline=outline, width=width * K)


def hand(raised):
    """A right hand seen from the palm side: the first fingers raised (index, middle, ring, little), the thumb too
    for 5; the other fingers folded on the palm."""
    w, h = 240, 320
    image = Image.new('RGBA', (w * K, h * K), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    line = 5
    # Fingers: x of the center, top when raised
    fingers = [(82, 30), (118, 18), (154, 30), (188, 62)]
    finger_width = 32
    for i, (x, top) in enumerate(fingers):
        if i < min(raised, 4):
            rounded(draw, (x - finger_width / 2, top, x + finger_width / 2, 200), finger_width / 2, SKIN, SKIN_LINE,
                    line)
            rounded(draw, (x - 9, top + 8, x + 9, top + 28), 8, NAIL)
    # The thumb: raised to the left for 5, else folded across the palm
    if raised == 5:
        thumb = Image.new('RGBA', image.size, (0, 0, 0, 0))
        ImageDraw.Draw(thumb).rounded_rectangle([v * K for v in (54, 130, 90, 262)], radius=18 * K, fill=SKIN,
                                                outline=SKIN_LINE, width=line * K)
        thumb = thumb.rotate(26, center=(72 * K, 245 * K), resample=Image.BICUBIC)
        image.alpha_composite(thumb)
    # The palm
    rounded(draw, (58, 150, 210, 300), 46, SKIN, SKIN_LINE, line)
    # Folded fingers: their knuckles on the top of the palm
    for i, (x, top) in enumerate(fingers):
        if i >= min(raised, 4):
            rounded(draw, (x - finger_width / 2, 146, x + finger_width / 2, 196), 15, SKIN, SKIN_LINE, line)
    if raised < 5:
        # The thumb folded over the palm
        rounded(draw, (66, 196, 150, 232), 18, SKIN, SKIN_LINE, line)
    # The wrist
    draw.line([v * K for v in (90, 300, 90, 316)], fill=SKIN_LINE, width=line * K)
    draw.line([v * K for v in (178, 300, 178, 316)], fill=SKIN_LINE, width=line * K)
    return image


PIPS = {1: [(1, 1)], 2: [(0, 0), (2, 2)], 3: [(0, 0), (1, 1), (2, 2)], 4: [(0, 0), (2, 0), (0, 2), (2, 2)],
        5: [(0, 0), (2, 0), (1, 1), (0, 2), (2, 2)], 6: [(0, 0), (2, 0), (0, 1), (2, 1), (0, 2), (2, 2)]}


def die(value):
    size = 300
    image = Image.new('RGBA', (size * K, size * K), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    rounded(draw, (12, 18, 288, 294), 52, (190, 190, 200))
    rounded(draw, (12, 8, 288, 284), 52, (255, 255, 255), (120, 120, 135), 6)
    for column, row in PIPS[value]:
        cx, cy = 66 + column * 84, 62 + row * 84
        draw.ellipse([v * K for v in (cx - 25, cy - 25, cx + 25, cy + 25)], fill=(40, 44, 60))
    return image


GOLD = (236, 190, 70)
GOLD_LINE = (176, 128, 30)
SILVER = (214, 218, 224)
SILVER_LINE = (140, 146, 156)


def coin(value, center, ring, center_line, ring_line):
    """A coin of two metals (like the real ones of 1 and 2 euros, without their designs): its value and the euro
    sign."""
    size = 300
    image = Image.new('RGBA', (size * K, size * K), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.ellipse([v * K for v in (6, 6, 294, 294)], fill=ring, outline=ring_line, width=6 * K)
    draw.ellipse([v * K for v in (52, 52, 248, 248)], fill=center, outline=center_line, width=5 * K)
    centered_text(draw, 150, 138, str(value), 120, (60, 50, 40))
    centered_text(draw, 150, 218, '€', 44, (60, 50, 40))
    return image


def note(value, color, dark):
    """A banknote: a colored rectangle with a frame, its value and the euro sign (no design of the real notes)."""
    w, h = 480, 260
    image = Image.new('RGBA', (w * K, h * K), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    rounded(draw, (6, 6, 474, 254), 20, color, dark, 6)
    rounded(draw, (24, 24, 456, 236), 14, None, dark, 4)
    # Waves in the background
    light = tuple(min(255, c + 40) for c in color)
    for i in range(4):
        y = 70 + i * 40
        draw.arc([v * K for v in (40, y - 30, 260, y + 30)], 200, 340, fill=light, width=8 * K)
    draw.ellipse([v * K for v in (330, 60, 440, 170)], fill=light)
    centered_text(draw, 160, 135, str(value), 120, (255, 255, 255), 5, dark)
    centered_text(draw, 385, 115, '€', 70, dark)
    centered_text(draw, 385, 205, 'EURO', 34, dark)
    return image


for n in range(1, 6):
    save(hand(n), (240, 320), 'fingers', 'fingers%d.png' % n)
for n in range(1, 7):
    save(die(n), (300, 300), 'dice', 'die%d.png' % n)
save(coin(1, SILVER, GOLD, SILVER_LINE, GOLD_LINE), (300, 300), 'money', 'money1.png')
save(coin(2, GOLD, SILVER, GOLD_LINE, SILVER_LINE), (300, 300), 'money', 'money2.png')
save(note(5, (120, 150, 170), (60, 80, 100)), (480, 260), 'money', 'money5.png')
save(note(10, (214, 90, 90), (130, 40, 40)), (480, 260), 'money', 'money10.png')
print('ok')
