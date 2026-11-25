"""Makes the ingredients of "Recettes magiques" (gazeplay-games: data/potions/images/ingredients), made for GazePlay
Learning on 24/11/2026, when the game of the potions to mix became a game of recipes (maintainer's decision, tester's
feedback point 82: mixing the colors is the game "Mélanger les couleurs").

The ingredients are the emoji of OpenMoji (https://openmoji.org/, CC BY-SA 4.0): mushroom (1F344), feather (1FAB6),
star (2B50), cherry blossom (1F338), leaf fluttering in wind (1F343), red apple (1F34E), gem stone (1F48E), crescent
moon (1F319), spiral shell (1F41A), carrot (1F955), snowflake (2744), four leaf clover (1F340); cropped to their drawing
and put in squares of 256 x 256.

python make_magicrecipes_images.py <OpenMoji folder of the 618 x 618 color PNG files> <data/potions/images/ingredients>
Pillow is required.
"""
import os
import sys

from PIL import Image

INGREDIENTS = {
    'mushroom': '1F344', 'feather': '1FAB6', 'star': '2B50', 'flower': '1F338', 'leaf': '1F343', 'apple': '1F34E',
    'crystal': '1F48E', 'moon': '1F319', 'shell': '1F41A', 'carrot': '1F955', 'snowflake': '2744', 'clover': '1F340',
}
SIZE = 256

source, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
for name, code in INGREDIENTS.items():
    drawing = Image.open(os.path.join(source, code + '.png')).convert('RGBA')
    drawing = drawing.crop(drawing.getbbox())
    scale = SIZE * 0.94 / max(drawing.size)
    drawing = drawing.resize((round(drawing.width * scale), round(drawing.height * scale)), Image.LANCZOS)
    square = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    square.paste(drawing, ((SIZE - drawing.width) // 2, (SIZE - drawing.height) // 2), drawing)
    square.save(os.path.join(out, name + '.png'), optimize=True)
print('ok')
