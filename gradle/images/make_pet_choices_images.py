"""Makes the pictures of the choices of "J'ai un Biboule !" (name code Pet; gazeplay-games: data/pet/images), made for
GazePlay Learning on 27/11/2026 (tester's feedback, point 80: several dishes on the table, several toys in the garden).

python make_pet_choices_images.py <folder of the 618x618 PNG files of OpenMoji> <data/pet/images folder>
Pillow is required. Writes, 400 x 400 with a transparent background:
- the dishes apple.png (red apple, 1F34E), banana.png (1F34C) and cake.png (shortcake, 1F370): emoji of OpenMoji
  (https://openmoji.org/, CC BY-SA 4.0), cropped to their drawing;
- the toys juggling.png (three juggling balls) and hoop.png (a hoop), drawn here with the black line of OpenMoji, under
  the license of GazePlay Learning (GPL-3.0-or-later). The ball of the garden (ball.png) is made by make_pet_images.py.
"""
import os
import sys

from PIL import Image, ImageDraw

SIZE, K = 400, 4
src, out = sys.argv[1], sys.argv[2]


def emoji(code):
    item = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    item = item.crop(item.getbbox())
    scale = SIZE * 0.94 / max(item.size)
    item = item.resize((round(item.width * scale), round(item.height * scale)), Image.LANCZOS)
    square = Image.new('RGBA', (SIZE, SIZE))
    square.paste(item, ((SIZE - item.width) // 2, (SIZE - item.height) // 2), item)
    return square


def juggling():
    big = Image.new('RGBA', (SIZE * K, SIZE * K))
    draw = ImageDraw.Draw(big)
    r = 78 * K
    for (cx, cy), color in (((200, 112), (231, 76, 60)), ((112, 268), (241, 196, 15)), ((288, 268), (52, 152, 219))):
        cx, cy = cx * K, cy * K
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=color + (255,), outline=(0, 0, 0, 255), width=10 * K)
        # A light band, as on a juggling ball
        draw.arc((cx - r * 0.7, cy - r * 0.7, cx + r * 0.7, cy + r * 0.7), 200, 290, fill=(255, 255, 255, 200),
                 width=12 * K)
    return big.resize((SIZE, SIZE), Image.LANCZOS)


def hoop():
    big = Image.new('RGBA', (SIZE * K, SIZE * K))
    draw = ImageDraw.Draw(big)
    box = (24 * K, 24 * K, 376 * K, 376 * K)
    draw.ellipse(box, outline=(0, 0, 0, 255), width=46 * K)
    inner = (34 * K, 34 * K, 366 * K, 366 * K)
    # A ring of colored stripes, as a hoop
    colors = [(233, 30, 99), (255, 193, 7), (76, 175, 80), (33, 150, 243)]
    for k in range(16):
        draw.arc(inner, k * 22.5, (k + 1) * 22.5, fill=colors[k % 4] + (255,), width=26 * K)
    return big.resize((SIZE, SIZE), Image.LANCZOS)


os.makedirs(out, exist_ok=True)
for name, code in (('apple', '1F34E'), ('banana', '1F34C'), ('cake', '1F370')):
    emoji(code).save(os.path.join(out, name + '.png'), optimize=True)
juggling().save(os.path.join(out, 'juggling.png'), optimize=True)
hoop().save(os.path.join(out, 'hoop.png'), optimize=True)
print('ok')
