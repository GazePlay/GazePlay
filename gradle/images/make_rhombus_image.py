"""Draws the rhombus of "Where is the shape?" (gazeplay-games: data/whereIsTheShape/images/rhombus/rhombus.jpg): the former
one was a square on its corner, taken for the square (tester's feedback of 23/11/2026, point 37). Higher than wide, as a
diamond of playing cards, black on white at the size of the other shapes. The image is under the license of GazePlay
Learning (GPL-3.0-or-later).

Usage: python make_rhombus_image.py <output.jpg>
"""
import sys

from PIL import Image, ImageDraw

W, H, S = 1424, 890, 4  # size of the pictures of the shapes, and supersampling for smooth edges
WIDTH, HEIGHT = 340, 580  # of the rhombus

img = Image.new('L', (W * S, H * S), 255)
cx, cy = W * S / 2, H * S / 2
dx, dy = WIDTH * S / 2, HEIGHT * S / 2
ImageDraw.Draw(img).polygon([(cx, cy - dy), (cx + dx, cy), (cx, cy + dy), (cx - dx, cy)], fill=0)
img.resize((W, H), Image.LANCZOS).convert('RGB').save(sys.argv[1], 'JPEG', quality=92, optimize=True)
