"""Draws the images of Frogs Race (gazeplay-games: data/race/images), made for GazePlay Learning in October 2026 to
replace a frog and a landscape of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_race_images.py <images folder of Frogs Race>
Pillow is required. The flying Biboules (Blue.png ... Red.png, Flash.png) are drawings of the project, kept.
- frog-<color>.png and frog-<color>-jump.png (240 x 180): a racing frog seen from the side, facing right, sitting or
  jumping; green is the frog of the player;
- frog-catcher.png (360 x 300): the frog catching the bugs, seen from the front; its mouth is at (180, 196)
  (Race.CATCHER_MOUTH_X and Race.CATCHER_MOUTH_Y, as fractions of the size);
- background.png (1920 x 1080): the sky and the trees where the bugs fly (top 40 %), the bank (40 % to 52 %), then the
  pond where the frogs race (the lanes and the finish line are drawn by the game).
"""
import math
import os
import sys

from PIL import Image, ImageDraw

SCALE = 4  # drawn larger, then reduced: smooth edges
INK = (34, 30, 38, 255)
EYE_WHITE = (255, 255, 255, 255)

# Body, belly and dark shade of each frog
COLORS = {
    'green': ((112, 196, 72, 255), (214, 240, 160, 255), (70, 148, 46, 255)),
    'blue': ((70, 160, 230, 255), (196, 228, 250, 255), (40, 112, 182, 255)),
    'orange': ((246, 150, 52, 255), (252, 222, 168, 255), (204, 104, 28, 255)),
    'violet': ((160, 110, 220, 255), (226, 208, 248, 255), (116, 72, 176, 255)),
}

folder = sys.argv[1]


def canvas(width, height, color=(0, 0, 0, 0)):
    image = Image.new('RGBA', (width * SCALE, height * SCALE), color)
    return image, ImageDraw.Draw(image)


def s(*values):
    return [v * SCALE for v in values]


def finish(image, width, height):
    return image.resize((width, height), Image.LANCZOS)


def eye(draw, cx, cy, r, look_dx=0.25):
    """A big round eye with its pupil looking forward (to the right)."""
    draw.ellipse(s(cx - r, cy - r, cx + r, cy + r), fill=EYE_WHITE, outline=INK, width=5 * SCALE)
    pr = r * 0.5
    px = cx + r * look_dx
    draw.ellipse(s(px - pr, cy - pr, px + pr, cy + pr), fill=INK)
    draw.ellipse(s(px - pr * 0.1, cy - pr * 0.75, px + pr * 0.45, cy - pr * 0.2), fill=EYE_WHITE)


def frog_body(draw, body, belly, shade, folded_leg):
    """The body, the head, the eye and the front leg of a sitting frog facing right; its back leg folded under it or
    not (drawn separately when it jumps)."""
    w = 5 * SCALE
    draw.ellipse(s(40, 70, 186, 160), fill=body, outline=INK, width=w)
    draw.chord(s(66, 104, 190, 164), 0, 180, fill=belly)
    draw.ellipse(s(110, 46, 214, 132), fill=body, outline=INK, width=w)
    draw.ellipse(s(118, 60, 206, 128), fill=body)
    draw.chord(s(130, 92, 212, 134), 0, 180, fill=belly)
    if folded_leg:
        draw.ellipse(s(28, 104, 118, 166), fill=shade, outline=INK, width=w)
        draw.rounded_rectangle(s(10, 150, 96, 170), radius=10 * SCALE, fill=shade, outline=INK, width=w)
    draw.line(s(168, 124, 178, 164), fill=shade, width=14 * SCALE)
    draw.rounded_rectangle(s(164, 156, 204, 172), radius=8 * SCALE, fill=shade, outline=INK, width=4 * SCALE)
    draw.arc(s(170, 90, 212, 116), 10, 110, fill=INK, width=4 * SCALE)
    eye(draw, 168, 50, 24)


def racing_frog(color, jumping):
    """A frog seen from the side, facing right (240 x 180): sitting, or jumping (tilted up, its back leg stretched
    behind)."""
    body, belly, shade = COLORS[color]
    if not jumping:
        image, draw = canvas(240, 180)
        frog_body(draw, body, belly, shade, True)
        return finish(image, 240, 180)
    # The back leg stretched to the lower left: a dark outline, then the leg, then the foot
    image, draw = canvas(240, 180)
    leg = s(84, 128, 44, 146, 16, 160)
    draw.line(leg, fill=INK, width=26 * SCALE, joint='curve')
    draw.line(leg, fill=shade, width=16 * SCALE, joint='curve')
    draw.rounded_rectangle(s(2, 152, 40, 170), radius=9 * SCALE, fill=shade, outline=INK, width=4 * SCALE)
    # The body tilted nose up, pasted over the leg
    body_layer, body_draw = canvas(240, 180)
    frog_body(body_draw, body, belly, shade, False)
    body_layer = body_layer.rotate(16, resample=Image.BICUBIC, center=(120 * SCALE, 110 * SCALE),
                                   translate=(6 * SCALE, -14 * SCALE))
    image.alpha_composite(body_layer)
    return finish(image, 240, 180)


def catcher_frog():
    """The frog catching the bugs, seen from the front, its mouth open (360 x 300)."""
    body, belly, shade = COLORS['green']
    image, draw = canvas(360, 300)
    w = 6 * SCALE
    # Legs and feet
    for side in (-1, 1):
        cx = 180 + side * 112
        draw.ellipse(s(cx - 58, 196, cx + 58, 290), fill=shade, outline=INK, width=w)
        draw.rounded_rectangle(s(cx - 52, 268, cx + 52, 296), radius=14 * SCALE, fill=shade, outline=INK, width=w)
    # Body and belly
    draw.ellipse(s(66, 120, 294, 298), fill=body, outline=INK, width=w)
    draw.ellipse(s(110, 176, 250, 292), fill=belly)
    # Head, with the eyes on top
    draw.ellipse(s(52, 70, 308, 236), fill=body, outline=INK, width=w)
    draw.ellipse(s(60, 78, 300, 228), fill=body)
    for side in (-1, 1):
        cx = 180 + side * 74
        draw.ellipse(s(cx - 50, 18, cx + 50, 118), fill=body, outline=INK, width=w)
        draw.ellipse(s(cx - 44, 40, cx + 44, 116), fill=body)
        eye(draw, cx, 64, 34, look_dx=0)
    # Open mouth: dark inside, pink tongue; its middle at (180, 196)
    draw.chord(s(104, 150, 256, 236), 0, 180, fill=(120, 34, 52, 255), outline=INK, width=w)
    draw.ellipse(s(146, 196, 214, 228), fill=(240, 120, 150, 255))
    draw.line(s(104, 193, 256, 193), fill=INK, width=w)
    # Cheeks
    for side in (-1, 1):
        cx = 180 + side * 96
        draw.ellipse(s(cx - 16, 168, cx + 16, 186), fill=(250, 160, 160, 160))
    return finish(image, 360, 300)


def background():
    """Sky and trees (top 40 %), the bank (40 to 52 %), the pond (52 to 100 %): 1920 x 1080, drawn at scale 2."""
    width, height, k = 1920, 1080, 2
    image = Image.new('RGBA', (width * k, height * k))
    draw = ImageDraw.Draw(image)

    def band(top, bottom, color_top, color_bottom):
        for y in range(int(top * k), int(bottom * k)):
            t = (y - top * k) / max(1, (bottom - top) * k)
            color = tuple(int(a + (b - a) * t) for a, b in zip(color_top, color_bottom))
            draw.line([(0, y), (width * k, y)], fill=color + (255,))

    band(0, 432, (170, 220, 250), (222, 244, 252))
    # Clouds
    for cx, cy, r in ((260, 110, 46), (330, 96, 60), (400, 116, 44), (1360, 150, 40), (1420, 130, 56), (1490, 152, 40)):
        draw.ellipse([(cx - r) * k, (cy - r) * k, (cx + r) * k, (cy + r) * k], fill=(255, 255, 255, 235))
    # Trees far away: round tops on trunks
    for i, cx in enumerate(range(-40, width + 120, 150)):
        top = 300 + (i % 3) * 22
        r = 92 + (i % 2) * 18
        draw.rectangle([(cx - 12) * k, (top + r - 20) * k, (cx + 12) * k, 450 * k], fill=(120, 92, 60, 255))
        draw.ellipse([(cx - r) * k, (top - r * 0.6) * k, (cx + r) * k, (top + r) * k],
                     fill=(84, 150, 74, 255) if i % 2 else (98, 168, 82, 255))
    # The bank
    band(432, 562, (150, 204, 92), (128, 186, 78))
    for x in range(0, width, 36):
        h = 18 + (x * 7) % 16
        draw.polygon([(x * k, 562 * k), ((x + 10) * k, (562 - h) * k), ((x + 20) * k, 562 * k)],
                     fill=(104, 166, 64, 255))
    # The pond, with soft ripples
    band(562, 1080, (126, 206, 236), (84, 168, 214))
    draw.rectangle([0, 562 * k, width * k, 572 * k], fill=(96, 150, 70, 255))
    for i in range(40):
        cx = (i * 211) % width
        cy = 600 + (i * 97) % 460
        rw = 40 + (i * 13) % 50
        draw.arc([(cx - rw) * k, (cy - 8) * k, (cx + rw) * k, (cy + 8) * k], 200, 340,
                 fill=(196, 236, 250, 200), width=3 * k)
    return image.resize((width, height), Image.LANCZOS).convert('RGB')


os.makedirs(folder, exist_ok=True)
for name in COLORS:
    racing_frog(name, False).save(os.path.join(folder, 'frog-' + name + '.png'))
    racing_frog(name, True).save(os.path.join(folder, 'frog-' + name + '-jump.png'))
catcher_frog().save(os.path.join(folder, 'frog-catcher.png'))
background().save(os.path.join(folder, 'background.png'), optimize=True)
