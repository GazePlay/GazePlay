"""Makes the picture of "Apprends les Maths" (Math101; gazeplay-games: data/math101/images), for GazePlay Learning in
November 2026, to replace the pictures of unknown source of the former game (the blackboard and the cards are now drawn
by the game). Same license as GazePlay Learning (GPL-3.0-or-later).
- biboule.png: the yellow Biboule of the shooting game (data/shooter/biboule/images/Yellow.png, the mascot of the
  laboratory modernized for the project, see make_shooter_images.py), cut to its drawing.

python make_math101_images.py <data/math101 folder> <data/shooter/biboule/images folder>
Pillow is required.
"""
import os
import sys

from PIL import Image

out, shooter = sys.argv[1], sys.argv[2]
folder = os.path.join(out, 'images')
os.makedirs(folder, exist_ok=True)
biboule = Image.open(os.path.join(shooter, 'Yellow.png')).convert('RGBA')
box = biboule.getchannel('A').point(lambda a: 255 if a > 8 else 0).getbbox()
biboule = biboule.crop(box)
biboule.save(os.path.join(folder, 'biboule.png'), optimize=True)
print('ok', biboule.size)
