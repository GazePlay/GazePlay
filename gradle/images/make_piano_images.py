"""Makes the pictures of the songs of the Piano (gazeplay-games: data/piano/images), made for GazePlay Learning on
27/11/2026 (tester's feedback, points 39 and 41: the cards of the choice of the song, and the picture swinging at each
note while the whole song is played again).

The pictures are the emoji of OpenMoji (https://openmoji.org/, CC BY-SA 4.0): crescent moon (1F319, "Au clair de la
lune"), star (2B50, "Ah ! vous dirai-je maman"), sun with face (1F31E, "L'Ode à la joie"), birthday cake (1F382,
"Joyeux anniversaire"); cropped to their drawing and put in squares of 256 x 256. The die of "Au hasard" is drawn by
the game.

python make_piano_images.py <OpenMoji folder of the 618 x 618 color PNG files> <data/piano/images>
Pillow is required.
"""
import os
import sys

from PIL import Image

PICTURES = {'moon': '1F319', 'star': '2B50', 'sun': '1F31E', 'cake': '1F382'}
SIZE = 256

source, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
for name, code in PICTURES.items():
    drawing = Image.open(os.path.join(source, code + '.png')).convert('RGBA')
    drawing = drawing.crop(drawing.getbbox())
    scale = SIZE * 0.94 / max(drawing.size)
    drawing = drawing.resize((round(drawing.width * scale), round(drawing.height * scale)), Image.LANCZOS)
    square = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    square.paste(drawing, ((SIZE - drawing.width) // 2, (SIZE - drawing.height) // 2), drawing)
    square.save(os.path.join(out, name + '.png'), optimize=True)
print('ok')
