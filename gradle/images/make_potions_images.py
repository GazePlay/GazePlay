"""Draws the images of Magic Potions (gazeplay-games: data/potions/images), made for GazePlay Learning in October 2026
to replace images of Freepik, which were not free. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_potions_images.py <images folder of Magic Potions>
Pillow is required. Biboule (Biboule-Client.png) is taken from biboulejump/biboules/Yellow.png of the same module.
The sizes and the transparent areas are those the game expects (MagicPotions.launch):
- the potions are 150 x 300;
- the mixing pot is 150 x 136, with a transparent opening around (75, 45), where the color of the mix is drawn behind;
- in Biboule-Client.png (670 x 422), the body of the flask is transparent around (449, 160), where the color asked
  by the client is drawn behind.
"""
import os
import sys

from PIL import Image, ImageDraw

SCALE = 4  # drawn larger, then reduced: smooth edges
INK = (34, 30, 38, 255)
GLASS = (221, 243, 247, 255)
CORK = (181, 118, 58, 255)
CORK_DARK = (139, 86, 40, 255)

folder = sys.argv[1]


def canvas(width, height, color=(0, 0, 0, 0)):
    image = Image.new('RGBA', (width * SCALE, height * SCALE), color)
    return image, ImageDraw.Draw(image)


def s(*values):
    return [v * SCALE for v in values]


def finish(image, width, height):
    return image.resize((width, height), Image.LANCZOS)


def flask(draw, cx, top, body_r, neck_w, neck_h, liquid, outline=6):
    """A round flask: cork, neck, round body (filled with the liquid, or left transparent if liquid is None)."""
    body_cy = top + 34 + neck_h + body_r
    # Cork and neck
    draw.rounded_rectangle(s(cx - neck_w / 2 - 6, top, cx + neck_w / 2 + 6, top + 34), radius=8 * SCALE,
                           fill=CORK, outline=INK, width=outline * SCALE)
    draw.line(s(cx - neck_w / 2, top + 14, cx + neck_w / 2, top + 14), fill=CORK_DARK, width=3 * SCALE)
    draw.rectangle(s(cx - neck_w / 2, top + 30, cx + neck_w / 2, top + 34 + neck_h + 10), fill=GLASS,
                   outline=INK, width=outline * SCALE)
    # Body
    box = s(cx - body_r, body_cy - body_r, cx + body_r, body_cy + body_r)
    if liquid is None:
        draw.ellipse(box, fill=(0, 0, 0, 0), outline=INK, width=outline * SCALE)
    else:
        draw.ellipse(box, fill=GLASS, outline=INK, width=outline * SCALE)
        inner = body_r - outline
        # The liquid fills the body below a flat surface, a little above its middle
        draw.chord(s(cx - inner, body_cy - inner, cx + inner, body_cy + inner), -17, 197, fill=liquid)
    # Neck over the body, and a light reflection
    draw.rectangle(s(cx - neck_w / 2 + outline, body_cy - body_r - 2, cx + neck_w / 2 - outline,
                     body_cy - body_r + outline + 2), fill=GLASS)
    draw.arc(s(cx - body_r * 0.7, body_cy - body_r * 0.7, cx + body_r * 0.7, body_cy + body_r * 0.7), 205, 245,
             fill=(255, 255, 255, 230), width=7 * SCALE)
    return body_cy


# The three potions
for name, color in (('potionRed', (229, 57, 53, 255)), ('potionYellow', (253, 216, 53, 255)),
                    ('potionBlue', (30, 136, 229, 255))):
    image, draw = canvas(150, 300)
    flask(draw, 75, 14, 66, 40, 92, color)
    finish(image, 150, 300).save(os.path.join(folder, name + '.png'), optimize=True)

# The mixing pot: a cauldron whose opening is transparent
image, draw = canvas(150, 136)
pot = (74, 64, 88, 255)
draw.ellipse(s(8, 30, 142, 132), fill=pot, outline=INK, width=6 * SCALE)
draw.rectangle(s(20, 96, 34, 130), fill=pot, outline=INK, width=5 * SCALE)
draw.rectangle(s(116, 96, 130, 130), fill=pot, outline=INK, width=5 * SCALE)
draw.ellipse(s(8, 30, 142, 132), fill=pot, outline=INK, width=6 * SCALE)
draw.ellipse(s(2, 14, 148, 70), fill=(106, 92, 122, 255), outline=INK, width=6 * SCALE)
draw.ellipse(s(14, 22, 136, 62), fill=(0, 0, 0, 0), outline=INK, width=4 * SCALE)
draw.arc(s(24, 60, 126, 120), 120, 160, fill=(255, 255, 255, 120), width=6 * SCALE)
finish(image, 150, 136).save(os.path.join(folder, 'mixingPot.png'), optimize=True)

# Biboule asking for a potion: Biboule of Biboule Jump, a speech bubble and a flask with a transparent body
image, draw = canvas(670, 422)
# The bubble hides the colored circle drawn behind it (center (466, 127), radius 97 on a screen 1920 pixels wide)
draw.polygon(s(338, 200, 392, 220, 300, 272), fill=(255, 255, 255, 255), outline=INK, width=6 * SCALE)
draw.ellipse(s(244, 8, 666, 236), fill=(255, 255, 255, 255), outline=INK, width=6 * SCALE)
# Body centered on (449, 160): top = 160 - radius - neck - cork
assert flask(draw, 449, 160 - 52 - 16 - 34, 52, 34, 16, None) == 160
bubble = finish(image, 670, 422)
# The body of the flask stays fully transparent, as the game draws the asked color behind it
hole = Image.new('L', bubble.size, 0)
ImageDraw.Draw(hole).ellipse((449 - 46, 160 - 46, 449 + 46, 160 + 46), fill=255)
bubble.putalpha(Image.composite(Image.new('L', bubble.size, 0), bubble.getchannel('A'), hole))
biboule = Image.open(os.path.join(folder, '..', '..', 'biboulejump', 'biboules', 'Yellow.png')).convert('RGBA')
biboule = biboule.crop(biboule.getbbox())
biboule = biboule.resize((300, round(300 * biboule.height / biboule.width)), Image.LANCZOS)
bubble.alpha_composite(biboule, (24, 392 - biboule.height))
bubble.save(os.path.join(folder, 'Biboule-Client.png'), optimize=True)

# The background: a room of a wizard, with a window, shelves and flasks
WIDTH, HEIGHT = 1920, 1080
image, draw = canvas(WIDTH, HEIGHT)
for y in range(HEIGHT):  # wall, from dark to light violet
    t = y / HEIGHT
    color = (int(66 + 40 * t), int(48 + 30 * t), int(104 + 36 * t), 255)
    draw.rectangle(s(0, y, WIDTH, y + 1), fill=color)
for row in range(0, 820, 70):  # stones of the wall, a little lighter than the wall (no blending on the canvas)
    offset = 0 if (row // 70) % 2 == 0 else 90
    t = (row + 35) / HEIGHT
    stone = (int(66 + 40 * t) + 16, int(48 + 30 * t) + 14, int(104 + 36 * t) + 18, 255)
    for x in range(-offset, WIDTH, 180):
        draw.rounded_rectangle(s(x + 6, row + 6, x + 174, row + 64), radius=12 * SCALE,
                               outline=stone, width=3 * SCALE)
draw.rectangle(s(0, 820, WIDTH, HEIGHT), fill=(92, 64, 51, 255))  # floor
for x in range(0, WIDTH, 160):
    draw.line(s(x, 820, x - 60, HEIGHT), fill=(70, 48, 38, 255), width=4 * SCALE)
draw.rectangle(s(0, 812, WIDTH, 826), fill=(60, 42, 34, 255))
# Window with the night sky and the moon
draw.rounded_rectangle(s(1380, 140, 1700, 520), radius=150 * SCALE, fill=(28, 36, 82, 255),
                       outline=(60, 42, 34, 255), width=18 * SCALE)
draw.ellipse(s(1560, 210, 1640, 290), fill=(255, 236, 160, 255))
for star in ((1450, 260), (1500, 400), (1620, 430), (1440, 470)):
    draw.ellipse(s(star[0] - 5, star[1] - 5, star[0] + 5, star[1] + 5), fill=(255, 255, 255, 220))
draw.line(s(1540, 150, 1540, 512), fill=(60, 42, 34, 255), width=12 * SCALE)
draw.line(s(1390, 330, 1690, 330), fill=(60, 42, 34, 255), width=12 * SCALE)
# Shelves with flasks and books
for shelf_y in (300, 520):
    draw.rectangle(s(140, shelf_y, 760, shelf_y + 22), fill=(139, 94, 60, 255), outline=(60, 42, 34, 255),
                   width=4 * SCALE)
shelf_items = ((190, 300, (229, 57, 53, 255)), (290, 300, (67, 160, 71, 255)), (560, 300, (142, 36, 170, 255)),
               (240, 520, (30, 136, 229, 255)), (650, 520, (253, 216, 53, 255)))
for x, base, color in shelf_items:
    draw.ellipse(s(x - 34, base - 70, x + 34, base - 2), fill=color, outline=INK, width=5 * SCALE)
    draw.rectangle(s(x - 10, base - 104, x + 10, base - 62), fill=GLASS, outline=INK, width=5 * SCALE)
    draw.rectangle(s(x - 13, base - 118, x + 13, base - 100), fill=CORK, outline=INK, width=4 * SCALE)
for i, color in enumerate(((198, 40, 40, 255), (21, 101, 192, 255), (46, 125, 50, 255), (239, 108, 0, 255))):
    x = 380 + i * 34
    draw.rectangle(s(x, 400 - (i % 2) * 14, x + 28, 520), fill=color, outline=INK, width=4 * SCALE)
background = finish(image, WIDTH, HEIGHT).convert('RGB')
background.save(os.path.join(folder, 'background-potions.jpg'), quality=90)
print('ok')
