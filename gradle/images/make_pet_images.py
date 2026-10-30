"""Draws the pictures of Pet (gazeplay-games: data/pet/images), made for GazePlay Learning in October 2026 to replace
photos and pictures of unknown source (some with brands). Same license as GazePlay Learning (GPL-3.0-or-later) for
the rooms, the food and the sad mouth; the objects are OpenMoji (CC BY-SA 4.0). The Biboule (body, eyes, mouths,
wings) is a drawing of the project, kept.

python make_pet_images.py <folder of the 618x618 PNG files of OpenMoji> <data/pet/images folder>
Pillow is required. Writes the 4 rooms (room.png, kitchen.png, bathroom.png, garden.png, 1200 x 900), the objects
following the gaze (hand.png, spoon.png, spoon-full.png, shower.png, ball.png, 400 x 400), the bowl (bowl.png) and the
sad mouth of the Biboule (sad.png, the size and line of mouth.png).
"""
import math
import os
import sys

from PIL import Image, ImageDraw

W, H, K = 1200, 900, 2  # size of a room, drawn at scale 2, then reduced
FOOD = (240, 170, 70)
FOOD_DARK = (200, 120, 40)

src, out = sys.argv[1], sys.argv[2]


def canvas():
    image = Image.new('RGB', (W * K, H * K))
    return image, ImageDraw.Draw(image)


def s(*values):
    return [v * K for v in values]


def gradient(draw, top, bottom, color_top, color_bottom):
    for y in range(int(top * K), int(bottom * K)):
        t = (y - top * K) / max(1, (bottom - top) * K)
        draw.line([(0, y), (W * K, y)], fill=tuple(int(a + (b - a) * t) for a, b in zip(color_top, color_bottom)))


def blob(draw, cx, cy, rx, ry, color):
    draw.ellipse(s(cx - rx, cy - ry, cx + rx, cy + ry), fill=color)


def floor(draw, top, color, line):
    draw.rectangle(s(0, top, W, H), fill=color)
    for y in range(top + 30, H, 70):
        draw.line(s(0, y, W, y), fill=line, width=3 * K)


def room():
    image, draw = canvas()
    draw.rectangle(s(0, 0, W, 640), fill=(250, 225, 185))
    for x in range(0, W, 90):
        draw.rectangle(s(x, 0, x + 45, 640), fill=(246, 216, 172))
    floor(draw, 640, (200, 145, 95), (182, 128, 82))
    # A window with curtains, a rug, a lamp, a shelf
    draw.rectangle(s(820, 120, 1060, 400), fill=(160, 215, 245), outline=(130, 90, 60), width=12 * K)
    draw.line(s(940, 120, 940, 400), fill=(130, 90, 60), width=8 * K)
    draw.rectangle(s(780, 100, 830, 440), fill=(120, 170, 200))
    draw.rectangle(s(1050, 100, 1100, 440), fill=(120, 170, 200))
    blob(draw, 600, 790, 420, 70, (225, 120, 110))
    blob(draw, 600, 790, 330, 48, (235, 150, 135))
    draw.line(s(160, 640, 160, 330), fill=(90, 80, 70), width=8 * K)
    draw.polygon(s(100, 330, 220, 330, 190, 250, 130, 250), fill=(250, 200, 90))
    draw.rectangle(s(330, 250, 600, 268), fill=(150, 100, 60))
    for i, color in enumerate(((200, 80, 80), (80, 130, 200), (240, 190, 80), (90, 160, 100))):
        draw.rectangle(s(350 + i * 32, 180, 376 + i * 32, 250), fill=color)
    return image


def kitchen():
    image, draw = canvas()
    draw.rectangle(s(0, 0, W, 560), fill=(235, 242, 245))
    for x in range(0, W, 60):
        for y in range(280, 560, 60):
            draw.rectangle(s(x + 3, y + 3, x + 57, y + 57), outline=(205, 220, 228), width=2 * K)
    # Cupboards, a window, the worktop, the table
    for x in range(40, W - 40, 280):
        draw.rectangle(s(x, 40, x + 250, 220), fill=(170, 210, 190), outline=(140, 180, 160), width=4 * K)
        draw.ellipse(s(x + 115, 120, x + 135, 140), fill=(120, 140, 130))
    draw.rectangle(s(0, 520, W, 560), fill=(150, 105, 70))
    floor(draw, 560, (220, 205, 180), (200, 185, 160))
    draw.rectangle(s(120, 700, 1080, 740), fill=(190, 135, 85))
    draw.rectangle(s(160, 740, 190, 900), fill=(170, 120, 75))
    draw.rectangle(s(1010, 740, 1040, 900), fill=(170, 120, 75))
    return image


def bathroom():
    image, draw = canvas()
    draw.rectangle(s(0, 0, W, 620), fill=(215, 235, 245))
    for x in range(0, W, 80):
        for y in range(0, 620, 80):
            draw.rectangle(s(x + 3, y + 3, x + 77, y + 77), fill=(200, 228, 242))
    floor(draw, 620, (235, 235, 240), (215, 215, 225))
    # A bathtub, a mirror, a towel
    draw.rounded_rectangle(s(250, 560, 950, 760), radius=60 * K, fill=(255, 255, 255), outline=(190, 200, 210),
                           width=6 * K)
    draw.rectangle(s(300, 760, 330, 820), fill=(190, 200, 210))
    draw.rectangle(s(870, 760, 900, 820), fill=(190, 200, 210))
    draw.ellipse(s(60, 120, 220, 330), fill=(225, 240, 250), outline=(170, 185, 200), width=8 * K)
    draw.rectangle(s(1010, 300, 1130, 520), fill=(240, 150, 150))
    draw.rectangle(s(990, 290, 1150, 310), fill=(160, 160, 170))
    return image


def garden():
    image, draw = canvas()
    gradient(draw, 0, 560, (140, 205, 245), (215, 240, 250))
    blob(draw, 1030, 130, 70, 70, (255, 214, 92))
    for cx, cy in ((250, 140), (700, 100)):
        for dx, r in ((-60, 45), (0, 62), (60, 45)):
            blob(draw, cx + dx, cy, r, r * 0.8, (255, 255, 255))
    blob(draw, 300, 600, 600, 150, (150, 200, 100))
    blob(draw, 950, 610, 500, 160, (135, 190, 90))
    gradient(draw, 560, H, (160, 210, 105), (115, 170, 75))
    # A tree, a hedge, flowers
    draw.rectangle(s(110, 330, 160, 640), fill=(120, 85, 55))
    blob(draw, 135, 300, 140, 120, (80, 150, 80))
    for x in range(560, W + 60, 90):
        blob(draw, x, 570, 60, 45, (95, 160, 85))
    for i in range(14):
        x = 60 + (i * 211) % (W - 120)
        y = 700 + (i * 97) % 170
        for a in range(5):
            angle = a * 2 * math.pi / 5
            blob(draw, x + 12 * math.cos(angle), y + 12 * math.sin(angle), 9, 9,
                 ((240, 120, 140), (250, 220, 90), (180, 140, 230))[i % 3])
        blob(draw, x, y, 7, 7, (255, 240, 200))
    return image


def emoji(code, size=400):
    item = Image.open(os.path.join(src, code + '.png')).convert('RGBA')
    item = item.crop(item.getbbox())
    scale = size / max(item.size)
    item = item.resize((round(item.width * scale), round(item.height * scale)), Image.LANCZOS)
    square = Image.new('RGBA', (size, size))
    square.paste(item, ((size - item.width) // 2, (size - item.height) // 2))
    return square


def food(image, cx, cy, rx, ry):
    """A heap of porridge, with a black line like OpenMoji."""
    big = Image.new('RGBA', (image.width * K, image.height * K))
    draw = ImageDraw.Draw(big)
    draw.ellipse(s(cx - rx, cy - ry, cx + rx, cy + ry), fill=FOOD + (255,), outline=(0, 0, 0, 255), width=5 * K)
    for dx, dy in ((-0.4, -0.2), (0.15, -0.35), (0.35, 0.1), (-0.1, 0.25)):
        draw.ellipse(s(cx + dx * rx - 6, cy + dy * ry - 6, cx + dx * rx + 6, cy + dy * ry + 6), fill=FOOD_DARK + (255,))
    image.alpha_composite(big.resize(image.size, Image.LANCZOS))
    return image


def head_of_spoon(spoon):
    """The center of the head of the spoon (OpenMoji 1F944, upright): the widest row of the top half."""
    alpha = spoon.getchannel('A')
    best = (0, 0, 0)
    for y in range(spoon.height // 2):
        xs = [x for x in range(spoon.width) if alpha.getpixel((x, y)) > 0]
        if xs and xs[-1] - xs[0] > best[0]:
            best = (xs[-1] - xs[0], (xs[0] + xs[-1]) / 2, y)
    return best


def sad_mouth():
    """The sad mouth: the line of mouth.png turned down (476 x 258)."""
    width, height = 476, 258
    big = Image.new('RGBA', (width * K, height * K))
    draw = ImageDraw.Draw(big)
    draw.arc(s(60, 90, 416, 420), start=205, end=335, fill=(0, 0, 0, 255), width=22 * K)
    return big.resize((width, height), Image.LANCZOS)


rooms = {'room': room, 'kitchen': kitchen, 'bathroom': bathroom, 'garden': garden}
for name, draw_room in rooms.items():
    draw_room().resize((W, H), Image.LANCZOS).save(os.path.join(out, name + '.png'), optimize=True)

emoji('1F590').save(os.path.join(out, 'hand.png'), optimize=True)
emoji('1F6BF').save(os.path.join(out, 'shower.png'), optimize=True)
emoji('26BD').save(os.path.join(out, 'ball.png'), optimize=True)
spoon = emoji('1F944')
spoon.save(os.path.join(out, 'spoon.png'), optimize=True)
width, x, y = head_of_spoon(spoon)
food(spoon.copy(), x, y + width * 0.15, width * 0.42, width * 0.3).save(os.path.join(out, 'spoon-full.png'),
                                                                       optimize=True)
bowl = emoji('1F963')
food(bowl, 200, 230, 110, 40).save(os.path.join(out, 'bowl.png'), optimize=True)
sad_mouth().save(os.path.join(out, 'sad.png'), optimize=True)
print('ok, head of the spoon at', round(x), round(y), 'of 400')
