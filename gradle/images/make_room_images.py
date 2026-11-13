"""Makes the objects of "La pièce" (Room; gazeplay-games: data/room/objects), on 13/11/2026: 32 OpenMoji pictures
(CC BY-SA 4.0, https://openmoji.org, hfg-gmuend/openmoji on GitHub), cropped to what they draw and reduced to 300
pixels at most. The walls, floors and arrows are drawn by the game.

python make_room_images.py <folder of the 618x618 PNG files of OpenMoji> <data/room folder>
"""
import os
import sys

from PIL import Image

OBJECTS = {
    # Living room
    'couch': '1F6CB', 'television': '1F4FA', 'plant': '1FAB4', 'clock': '1F570',
    'books': '1F4DA', 'window': '1FA9F', 'door': '1F6AA', 'chair': '1FA91',
    # Kitchen
    'pan': '1F373', 'teapot': '1FAD6', 'cup': '2615', 'bowl': '1F963',
    'apple': '1F34E', 'bread': '1F35E', 'milk': '1F95B', 'spoon': '1F944',
    # Bedroom
    'bed': '1F6CF', 'teddy': '1F9F8', 'alarm': '23F0', 'tshirt': '1F455',
    'socks': '1F9E6', 'book': '1F4D6', 'ball': '26BD', 'bulb': '1F4A1',
    # Bathroom
    'bathtub': '1F6C1', 'shower': '1F6BF', 'toilet': '1F6BD', 'soap': '1F9FC',
    'toothbrush': '1FAA5', 'mirror': '1FA9E', 'sponge': '1F9FD', 'paper': '1F9FB',
}


def main():
    source, target = sys.argv[1], os.path.join(sys.argv[2], 'objects')
    os.makedirs(target, exist_ok=True)
    for name, code in OBJECTS.items():
        picture = Image.open(os.path.join(source, code + '.png')).convert('RGBA')
        picture = picture.crop(picture.getbbox())
        picture.thumbnail((300, 300), Image.LANCZOS)
        picture.save(os.path.join(target, name + '.png'), optimize=True)
        print(name, picture.size)


main()
