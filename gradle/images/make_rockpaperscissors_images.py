"""Makes the hands of "Pierre papier ciseaux" (Rock Paper Scissors; gazeplay-games: data/rockPaperScissors), made for
GazePlay Learning in November 2026 to replace 7 pictures of unknown source (cartoon gloves, a park, thumbs).

The hands are the emoji of OpenMoji (https://openmoji.org/, CC BY-SA 4.0): raised fist (270A), raised hand (270B),
victory hand (270C), cropped to their drawing and put in squares of 512 x 512.

python make_rockpaperscissors_images.py <OpenMoji folder of the 618 x 618 color PNG files> <data/rockPaperScissors folder>
Pillow is required.
"""
import os
import sys

from PIL import Image

HANDS = {'rock': '270A', 'paper': '270B', 'scissors': '270C'}
SIZE = 512

source, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
for name, code in HANDS.items():
    hand = Image.open(os.path.join(source, code + '.png')).convert('RGBA')
    hand = hand.crop(hand.getbbox())
    scale = SIZE * 0.94 / max(hand.size)
    hand = hand.resize((round(hand.width * scale), round(hand.height * scale)), Image.LANCZOS)
    square = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    square.paste(hand, ((SIZE - hand.width) // 2, (SIZE - hand.height) // 2), hand)
    square.save(os.path.join(out, name + '.png'), optimize=True)
print('ok')
