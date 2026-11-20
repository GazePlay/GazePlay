"""Makes the thumbnails of "Où est le son ?" and "Où est-ce ?" (gazeplay-core: data/common/Thumbnails), 20/11/2026
(tester's feedback: they showed animals, as "Où est l'animal ?"). They are drawn as the other thumbnails of the family,
captures of the game (960 x 540): four white rounded cards on black, the question in a pill, the button of the sound.
Same license as GazePlay Learning (GPL-3.0-or-later); the pictures are those of the games (see their CREDITS.txt): the
photos of instruments of "Où est le son ?" (Wikimedia Commons, The Metropolitan Museum of Art), the objects of "La
pièce" for "Où est-ce ?" (OpenMoji, CC BY-SA 4.0); the button of the sound is cut from the thumbnail of before.

python make_whereisit_thumbnails.py <root of the repository>
Pillow is required.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = sys.argv[1]
GAMES = os.path.join(ROOT, 'gazeplay-games/src/main/resources/data')
CORE = os.path.join(ROOT, 'gazeplay-core/src/main/resources/data')
THUMBNAILS = os.path.join(CORE, 'common/Thumbnails')
K = 2  # drawn at scale 2, then reduced
W, H = 960, 540
CARDS = [(8, 8, 475, 203), (485, 8, 951, 203), (8, 212, 475, 406), (485, 212, 951, 406)]
TEXT = (43, 33, 24)
BORDER = (232, 222, 208)


def rounded(size, radius):
    mask = Image.new('L', size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return mask


def thumbnail(pictures, question, photos, name):
    image = Image.new('RGB', (W * K, H * K), 'black')
    draw = ImageDraw.Draw(image)
    for (x0, y0, x1, y1), path in zip(CARDS, pictures):
        draw.rounded_rectangle((x0 * K, y0 * K, x1 * K, y1 * K), radius=14 * K, fill='white', outline=BORDER,
                               width=2 * K)
        picture = Image.open(path).convert('RGBA')
        # A photo fills the height of the card (margin 8), an object 0.75 of it
        room_h = (y1 - y0 - 16) * K if photos else int((y1 - y0) * 0.75) * K
        room_w = (x1 - x0 - 16) * K
        scale = min(room_w / picture.width, room_h / picture.height)
        picture = picture.resize((round(picture.width * scale), round(picture.height * scale)), Image.LANCZOS)
        left = ((x0 + x1) * K - picture.width) // 2
        top = ((y0 + y1) * K - picture.height) // 2
        if photos:
            mask = rounded(picture.size, 8 * K)
            image.paste(picture.convert('RGB'), (left, top), mask)
        else:
            image.paste(picture, (left, top), picture)
    font = ImageFont.truetype(os.path.join(CORE, 'fonts/Fredoka-SemiBold.ttf'), 36 * K)
    text_w = draw.textlength(question, font=font)
    pill_w = text_w + 72 * K
    cx = 484 * K
    draw.rounded_rectangle((cx - pill_w / 2, 438 * K, cx + pill_w / 2, 516 * K), radius=39 * K, fill='white')
    draw.text((cx, 477 * K), question, font=font, fill=TEXT, anchor='mm')
    image = image.resize((W, H), Image.LANCZOS)
    # The button of the sound, as in the other thumbnails of the family
    speaker = Image.open(os.path.join(THUMBNAILS, 'whereIsTheAnimal.png')).convert('RGB').crop((856, 424, 960, 530))
    image.paste(speaker, (856, 424))
    image.save(os.path.join(THUMBNAILS, name), optimize=True)


sounds = os.path.join(GAMES, 'whereIsTheSound/images')
thumbnail([os.path.join(sounds, 'piano/piano_1.jpg'), os.path.join(sounds, 'drums/drums_2.jpg'),
           os.path.join(sounds, 'trumpet/trumpet_1.jpg'), os.path.join(sounds, 'harp/harp_3.jpg')],
          'Écoute le son', True, 'whereIsTheSound.png')
objects = os.path.join(GAMES, 'room/objects')
thumbnail([os.path.join(objects, name + '.png') for name in ('ball', 'cup', 'teddy', 'chair')],
          'Où est le ballon ?', False, 'whereIsIt.png')
print('ok')
