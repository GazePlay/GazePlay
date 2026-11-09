"""Draws the vehicles of 3 boxes of "Embouteillage" (Traffic Jam, name code RushHour; gazeplay-games: data/rushHour),
made for GazePlay Learning in November 2026 to replace pictures of unknown source (carH.png, carV.png, taxiH.png,
taxiV.png). Same license as GazePlay Learning (GPL-3.0-or-later).
- truck-1.png, truck-2.png (540 x 200): a truck seen from above, facing right: its cab and its box.
- bus.png (540 x 200): a bus seen from above, facing right, with its roof windows.
The cars of 2 boxes are those of "Course du regard" (data/gazeRace/car-1.png to car-5.png and car-player.png, the red
car-1.png being the car to take out); the parking is drawn by the game, which turns the vehicles standing up.

- With the folder of the cars of "Course du regard" and a thumbnail path, the thumbnail of the menu (gazeplay-core:
  data/common/Thumbnails/rushHour.png, 960 x 540).

python make_trafficjam_images.py <data/rushHour folder> [<data/gazeRace folder> <thumbnail.png>]
Pillow is required.
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
K = 3
W, H = 540, 200


def s(v):
    return int(round(v * K))


def base():
    img = Image.new('RGBA', (s(W), s(H)), (0, 0, 0, 0))
    shadow = Image.new('L', img.size, 0)
    ImageDraw.Draw(shadow).rounded_rectangle([s(16), s(24), s(W - 10), s(H - 14)], radius=s(30), fill=90)
    img.paste((20, 24, 32, 255), (0, 0), shadow.filter(ImageFilter.GaussianBlur(s(8))))
    d = ImageDraw.Draw(img)
    for x in (60, 300, 390, 448):
        for y in (12, H - 42):
            d.rounded_rectangle([s(x), s(y), s(x + 54), s(y + 30)], radius=s(10), fill=(44, 46, 54, 255))
    return img


def lights(d):
    for y in (44, H - 64):
        d.rounded_rectangle([s(W - 32), s(y), s(W - 18), s(y + 20)], radius=s(6), fill=(255, 250, 220, 255))
        d.rounded_rectangle([s(18), s(y), s(30), s(y + 20)], radius=s(5), fill=(214, 56, 60, 255))


def truck(cab, cab_dark, box, box_dark):
    img = base()
    d = ImageDraw.Draw(img)
    # The box, at the back (left), with its ribs
    d.rounded_rectangle([s(14), s(22), s(392), s(H - 22)], radius=s(16), fill=box + (255,), outline=box_dark + (255,),
                        width=s(6))
    for x in range(70, 380, 52):
        d.line([(s(x), s(30)), (s(x), s(H - 30))], fill=box_dark + (255,), width=s(4))
    # The cab, in front, with its windscreen
    d.rounded_rectangle([s(402), s(28), s(W - 14), s(H - 28)], radius=s(36), fill=cab + (255,),
                        outline=cab_dark + (255,), width=s(6))
    d.polygon([(s(476), s(44)), (s(506), s(54)), (s(506), s(H - 54)), (s(476), s(H - 44))], fill=(196, 222, 240, 255))
    d.rounded_rectangle([s(418), s(46), s(466), s(H - 46)], radius=s(14), fill=cab_dark + (255,))
    lights(d)
    return img.resize((W, H), Image.LANCZOS)


def bus():
    body, dark = (92, 170, 214), (40, 104, 150)
    img = base()
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([s(14), s(22), s(W - 14), s(H - 22)], radius=s(40), fill=body + (255,), outline=dark + (255,),
                        width=s(6))
    # The roof: a light band, the hatches, the windscreen in front
    d.rounded_rectangle([s(40), s(56), s(W - 70), s(H - 56)], radius=s(24), fill=(236, 244, 250, 255))
    for x in (90, 230, 370):
        d.rounded_rectangle([s(x), s(70), s(x + 70), s(H - 70)], radius=s(10), fill=(170, 200, 224, 255),
                            outline=dark + (255,), width=s(3))
    d.polygon([(s(W - 58), s(40)), (s(W - 26), s(54)), (s(W - 26), s(H - 54)), (s(W - 58), s(H - 40))],
              fill=(196, 222, 240, 255))
    lights(d)
    return img.resize((W, H), Image.LANCZOS)


truck((236, 120, 84), (168, 70, 44), (238, 234, 226), (170, 160, 146)).save(os.path.join(out, 'truck-1.png'))
truck((120, 120, 200), (70, 70, 140), (250, 214, 120), (184, 144, 52)).save(os.path.join(out, 'truck-2.png'))
bus().save(os.path.join(out, 'bus.png'))


def thumbnail(cars, path):
    """The thumbnail of the menu (960 x 540): a parking of 6 x 6 boxes, its vehicles and the red car near the exit."""
    TW, TH = 960, 540
    image = Image.new('RGBA', (TW, TH), (226, 230, 236, 255))
    d = ImageDraw.Draw(image)
    cell = 76
    x0, y0 = (TW - 6 * cell) // 2 - 30, (TH - 6 * cell) // 2
    d.rounded_rectangle([x0 - 16, y0 - 16, x0 + 6 * cell + 16, y0 + 6 * cell + 16], radius=18, fill=(150, 156, 168, 255))
    d.rectangle([x0, y0, x0 + 6 * cell, y0 + 6 * cell], fill=(98, 104, 118, 255))
    for k in range(1, 6):
        d.line([(x0 + k * cell, y0 + 6), (x0 + k * cell, y0 + 6 * cell - 6)], fill=(122, 128, 142, 255), width=2)
        d.line([(x0 + 6, y0 + k * cell), (x0 + 6 * cell - 6, y0 + k * cell)], fill=(122, 128, 142, 255), width=2)
    # The exit, and its arrow
    d.rectangle([x0 + 6 * cell, y0 + 2 * cell, x0 + 6 * cell + 16, y0 + 3 * cell], fill=(98, 104, 118, 255))
    ax = x0 + 6 * cell + 40
    ay = y0 + 2.5 * cell
    d.polygon([(ax, ay - 22), (ax + 40, ay), (ax, ay + 22)], fill=(250, 200, 70, 255))

    def put(name, column, line, length, standing):
        picture = Image.open(name).convert('RGBA')
        size = (length * cell - 8, cell - 8)
        picture = picture.resize(size, Image.LANCZOS)
        if standing:
            picture = picture.rotate(90, expand=True)
        image.alpha_composite(picture, (x0 + column * cell + 4, y0 + line * cell + 4))

    put(os.path.join(cars, 'car-1.png'), 1, 2, 2, False)
    put(os.path.join(out, 'truck-1.png'), 3, 1, 3, True)
    put(os.path.join(cars, 'car-4.png'), 4, 3, 2, True)
    put(os.path.join(out, 'bus.png'), 0, 5, 3, False)
    put(os.path.join(cars, 'car-2.png'), 0, 0, 2, False)
    put(os.path.join(cars, 'car-5.png'), 5, 0, 2, True)
    put(os.path.join(cars, 'car-player.png'), 0, 3, 2, True)
    image.convert('RGB').save(path)


if len(sys.argv) > 3:
    thumbnail(sys.argv[2], sys.argv[3])
print('ok')
