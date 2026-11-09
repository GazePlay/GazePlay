"""Draws the pictures of GazeRace (gazeplay-games: data/gazeRace), made for GazePlay Learning in November 2026 to replace
four pictures of unknown source (carH.png, carV.png, Flash.png, cone-construction-road.png). Same license as GazePlay
Learning (GPL-3.0-or-later).
- car-player.png (360 x 200): the car of the player, seen from above, facing right: deep blue with two white stripes.
- car-1.png to car-5.png (360 x 200): the other cars, facing right, in soft colors told apart by their lightness too.
- barrier.png (600 x 100): the barrier of the works, red and white stripes between two cones, lying along the road.
- crash.png (300 x 300): a soft yellow star with an orange outline, shown where a car is hit.
The game turns the cars and the barrier for the vertical road and for the cars coming the other way.

- With one more argument, the thumbnail of the menu (gazeplay-core: data/common/Thumbnails/gazeRace.png, 960 x 540),
  made of these pictures and of the road drawn by the game.

python make_gazerace_images.py <data/gazeRace folder> [<thumbnail.png>]
Pillow is required.
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFilter

out = sys.argv[1]
os.makedirs(out, exist_ok=True)
K = 3

CAR_COLORS = [
    ((232, 92, 92), (168, 52, 60)),     # red
    ((246, 166, 72), (186, 108, 30)),   # orange
    ((244, 214, 92), (178, 146, 34)),   # yellow
    ((108, 196, 128), (52, 128, 74)),   # green
    ((182, 132, 214), (118, 74, 156)),  # violet
]
PLAYER = ((40, 92, 200), (18, 48, 120))


def car(body, dark, stripes=False):
    """A car seen from above, facing right, on a transparent background."""
    W, H = 360, 200
    img = Image.new('RGBA', (W * K, H * K), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = lambda v: v * K
    # A soft shadow under the car
    shadow = Image.new('L', img.size, 0)
    ImageDraw.Draw(shadow).rounded_rectangle([s(18), s(26), s(W - 10), s(H - 14)], radius=s(54), fill=90)
    img.paste((20, 24, 32, 255), (0, 0), shadow.filter(ImageFilter.GaussianBlur(s(8))))
    d = ImageDraw.Draw(img)
    # The wheels
    for x in (70, 250):
        for y in (14, H - 44):
            d.rounded_rectangle([s(x), s(y), s(x + 56), s(y + 30)], radius=s(10), fill=(44, 46, 54, 255))
    # The body
    d.rounded_rectangle([s(14), s(24), s(W - 14), s(H - 24)], radius=s(56), fill=body + (255,), outline=dark + (255,),
                        width=s(6))
    if stripes:
        for y in (82, 108):
            d.rectangle([s(22), s(y), s(W - 22), s(y + 10)], fill=(250, 250, 252, 255))
    # The roof and the windows
    d.rounded_rectangle([s(118), s(48), s(268), s(H - 48)], radius=s(34), fill=dark + (255,))
    d.polygon([(s(236), s(56)), (s(282), s(66)), (s(282), s(H - 66)), (s(236), s(H - 56))], fill=(196, 222, 240, 255))
    d.polygon([(s(150), s(56)), (s(112), s(70)), (s(112), s(H - 70)), (s(150), s(H - 56))], fill=(170, 200, 224, 255))
    d.rounded_rectangle([s(158), s(60), s(226), s(H - 60)], radius=s(20), fill=body + (255,))
    # The lights: white in front (right), red at the back
    for y in (44, H - 64):
        d.rounded_rectangle([s(W - 34), s(y), s(W - 20), s(y + 20)], radius=s(6), fill=(255, 250, 220, 255))
        d.rounded_rectangle([s(20), s(y), s(32), s(y + 20)], radius=s(5), fill=(214, 56, 60, 255))
    return img.resize((W, H), Image.LANCZOS)


car(*PLAYER, stripes=True).save(os.path.join(out, 'car-player.png'))
for n, (body, dark) in enumerate(CAR_COLORS, start=1):
    car(body, dark).save(os.path.join(out, 'car-%d.png' % n))


def cone(d, cx, base, h):
    """A cone drawn from its base."""
    w = h * 0.62
    d.polygon([(cx - w / 2, base), (cx + w / 2, base), (cx + w * 0.12, base - h), (cx - w * 0.12, base - h)],
              fill=(246, 136, 52, 255), outline=(170, 78, 22, 255))
    for t in (0.35, 0.65):
        y = base - h * t
        half = w / 2 * (1 - t) + w * 0.12 * t
        d.rectangle([cx - half + K, y - h * 0.07, cx + half - K, y + h * 0.07], fill=(252, 248, 240, 255))
    d.rounded_rectangle([cx - w * 0.62, base - h * 0.06, cx + w * 0.62, base + h * 0.06], radius=h * 0.05,
                        fill=(170, 78, 22, 255))


def barrier():
    W, H = 600, 100
    img = Image.new('RGBA', (W * K, H * K), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    s = lambda v: v * K
    plank = Image.new('RGBA', (s(W - 140), s(40)), (252, 248, 240, 255))
    pd = ImageDraw.Draw(plank)
    for i in range(-2, 14):
        x = i * s(46)
        pd.polygon([(x, s(40)), (x + s(23), s(40)), (x + s(46), 0), (x + s(23), 0)], fill=(222, 70, 70, 255))
    mask = Image.new('L', plank.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, plank.size[0] - 1, plank.size[1] - 1], radius=s(10), fill=255)
    img.paste(plank, (s(70), s(30)), mask)
    d.rounded_rectangle([s(70), s(30), s(W - 70), s(70)], radius=s(10), outline=(140, 40, 44, 255), width=s(4))
    cone(d, s(48), s(92), s(84))
    cone(d, s(W - 48), s(92), s(84))
    return img.resize((W, H), Image.LANCZOS)


barrier().save(os.path.join(out, 'barrier.png'))


def crash():
    W = 300
    img = Image.new('RGBA', (W * K, W * K), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c = W * K / 2
    points = []
    for i in range(20):
        r = (0.46 if i % 2 == 0 else 0.27) * W * K
        a = math.pi * 2 * i / 20 - math.pi / 2
        points.append((c + r * math.cos(a), c + r * math.sin(a)))
    d.polygon(points, fill=(255, 222, 110, 255))
    d.line(points + [points[0]], fill=(238, 140, 54, 255), width=7 * K, joint='curve')
    d.ellipse([c - 0.13 * W * K, c - 0.13 * W * K, c + 0.13 * W * K, c + 0.13 * W * K], fill=(255, 248, 214, 255))
    return img.resize((W, W), Image.LANCZOS)


crash().save(os.path.join(out, 'crash.png'))


def thumbnail(path):
    """The thumbnail of the menu (960 x 540): the horizontal road, cars on both sides, the barrier, the player."""
    W, H = 960, 540
    image = Image.new('RGBA', (W, H), (122, 178, 104, 255))
    d = ImageDraw.Draw(image)
    edge = int(H * 0.08)
    d.rectangle([0, edge, W, H - edge], fill=(92, 98, 112, 255))
    d.rectangle([0, edge - 6, W, edge], fill=(236, 236, 230, 255))
    d.rectangle([0, H - edge, W, H - edge + 6], fill=(236, 236, 230, 255))
    d.rectangle([0, H / 2 - 3, W, H / 2 + 3], fill=(236, 236, 230, 255))
    for lane in (0.29, 0.71):
        y = H * lane
        for x in range(-20, W, 150):
            d.rounded_rectangle([x, y - 4, x + 80, y + 4], radius=4, fill=(222, 222, 216, 255))

    def put(name, cx, cy, width, angle=0):
        picture = Image.open(os.path.join(out, name)).convert('RGBA')
        picture = picture.resize((width, int(width * picture.height / picture.width)), Image.LANCZOS)
        if angle:
            picture = picture.rotate(angle, expand=True, resample=Image.BICUBIC)
        image.alpha_composite(picture, (int(cx - picture.width / 2), int(cy - picture.height / 2)))

    put('barrier.png', 650, H / 2, 260)
    put('car-1.png', 150, 120, 140)
    put('car-4.png', 560, 175, 140)
    put('car-2.png', 820, 370, 140, 180)
    put('car-5.png', 420, 430, 140, 180)
    put('car-player.png', 300, 205, 150)
    image.convert('RGB').save(path)


if len(sys.argv) > 2:
    thumbnail(sys.argv[2])
print('ok')
