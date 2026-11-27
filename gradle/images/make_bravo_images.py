"""Draws the Biboule of the "Bravo" screen (gazeplay-core: data/common/images/bravo-1.png and bravo-2.png), made for
GazePlay Learning on 27/11/2026 (maintainer's request, point 88): bravo.gif, a GIF of 891 x 627 (a palette of 256
colors and a transparency of all or nothing), had jagged edges and grainy shades, and was enlarged up to 95 % of the
height of the screen. The same drawing of the original Biboule (maintainer's choice: not the modernized one) is made
here twice as large, smooth, with a true transparency: bravo-1.png winks with its mouth open, bravo-2.png has both eyes
open; the screen shows them in turn, 2 seconds each, as the frames of the GIF. The Biboule is the mascot of the
laboratory; same license as GazePlay Learning (GPL-3.0-or-later).

python make_bravo_images.py gradle/images/biboule-originale/autres/bravo.gif gazeplay-core/src/main/resources/data/common/images
(bravo.gif, no longer shipped, is kept with the other original drawings of the Biboule.)
Pillow is required. The outlines of the wings and of the body are those of bravo.gif, enlarged and smoothed; the
shades, the veins of the wings and the face are drawn again on its coordinates (891 x 627), at 6 times that size,
then reduced to 1782 x 1254.
"""
import math
import os
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter

W, H = 891, 627
OUT_SCALE = 2
K = 6

WING_LIGHT = (140, 205, 200)
WING_DARK = (80, 152, 160)
WING_EDGE = (72, 138, 150)
VEIN_DARK = (76, 145, 152)
VEIN_LIGHT = (156, 214, 210)
BODY = (255, 247, 153)
BODY_SHADE = (230, 220, 134)
BROW = (92, 58, 24)
INK = (23, 23, 23)
MOUTH_LINE = (40, 26, 32)
MOUTH = (232, 147, 183)
TONGUE = (246, 182, 208)
MOUTH_DARK = (176, 92, 128)


def p(*values):
    return [v * K for v in values]


def smooth_mask(small):
    """A mask of the GIF (0 or 255), enlarged and smoothed: the same outline, without the steps of its pixels."""
    big = small.resize((W * K, H * K), Image.NEAREST).filter(ImageFilter.GaussianBlur(K * 1.3))
    return big.point(lambda v: 255 if v >= 128 else 0)


def gradient(angle, light, dark):
    """The whole picture, from light to dark along a direction (degrees, 0: left to right)."""
    side = int(math.hypot(W * K, H * K)) + 4
    ramp = Image.linear_gradient('L').rotate(90).resize((side, side))
    ramp = ramp.rotate(-angle, resample=Image.BICUBIC)
    left, top = (side - W * K) // 2, (side - H * K) // 2
    ramp = ramp.crop((left, top, left + W * K, top + H * K))
    return Image.composite(Image.new('RGBA', ramp.size, dark + (255,)), Image.new('RGBA', ramp.size, light + (255,)),
                           ramp)


def gradient_box(box, angle, light, dark):
    """The whole picture transparent but a box, from light to dark along a direction (degrees) inside it."""
    x0, y0, x1, y1 = [v * K for v in box]
    w, h = x1 - x0, y1 - y0
    side = int(math.hypot(w, h)) + 4
    ramp = Image.linear_gradient('L').rotate(90).resize((side, side)).rotate(-angle, resample=Image.BICUBIC)
    ramp = ramp.crop(((side - w) // 2, (side - h) // 2, (side - w) // 2 + w, (side - h) // 2 + h))
    part = Image.composite(Image.new('RGBA', (w, h), dark + (255,)), Image.new('RGBA', (w, h), light + (255,)), ramp)
    whole = Image.new('RGBA', (W * K, H * K), light + (255,))
    whole.paste(part, (x0, y0))
    return whole


def paint(image, mask, fill):
    fill = fill.copy()
    fill.putalpha(mask)
    image.alpha_composite(fill)


def box_mask(box):
    mask = Image.new('L', (W * K, H * K), 0)
    ImageDraw.Draw(mask).rectangle(p(*box), fill=255)
    return mask


def erode(mask, width):
    """The mask shrunk by about a width (a blur then a high threshold: much faster than a minimum filter)."""
    return mask.filter(ImageFilter.GaussianBlur(width / 2)).point(lambda v: 255 if v >= 250 else 0)


def edge(mask, width):
    return ImageChops.subtract(mask, erode(mask, width))


def veins(draw, cx, cy, half_length, half_width, angle, count):
    a = math.radians(angle)
    ux, uy, vx, vy = math.cos(a), math.sin(a), -math.sin(a), math.cos(a)

    def at(u, v):
        return ((cx + u * ux + v * vx) * K, (cy + u * uy + v * vy) * K)

    rib = [at(-half_length * 0.85 + i * half_length * 1.7 / 20, half_width * 0.1 * math.sin(i / 20 * math.pi))
           for i in range(21)]
    lines = [rib]
    for i in range(count):
        u = -half_length * 0.55 + i * half_length * 1.15 / max(1, count - 1)
        for side in (1, -1):
            lines.append([at(u + half_length * 0.04, half_width * 0.06 * side),
                          at(u - half_length * 0.16, half_width * 0.58 * side)])
    for line in lines:
        draw.line([(x + 1.6 * K, y + 1.6 * K) for x, y in line], fill=VEIN_LIGHT + (255,), width=4 * K, joint='curve')
    for line in lines:
        draw.line(line, fill=VEIN_DARK + (255,), width=4 * K, joint='curve')


def open_eye(draw, cx, cy, rx, ry, hx, hy, hrx, hry):
    draw.ellipse(p(cx - rx, cy - ry, cx + rx, cy + ry), fill=INK + (255,))
    # A soft grey shine on the upper right, then the white light
    draw.arc(p(cx - rx * 0.78, cy - ry * 0.82, cx + rx * 0.78, cy + ry * 0.82), 280, 350, fill=(78, 78, 84, 255),
             width=int(rx * 0.16 * K))
    draw.ellipse(p(hx - hrx, hy - hry, hx + hrx, hy + hry), fill=(255, 255, 255, 255))


def crescent(draw, cx, cy, rx, ry, start, end, thickness):
    """A brow: the part of a ring between two angles, thick in the middle and thin at its ends."""
    outer, inner = [], []
    for i in range(41):
        t = math.radians(start + (end - start) * i / 40)
        width = thickness * math.sin(math.pi * i / 40) ** 0.7
        outer.append(((cx + (rx + width / 2) * math.cos(t)) * K, (cy + (ry + width / 2) * math.sin(t)) * K))
        inner.append(((cx + (rx - width / 2) * math.cos(t)) * K, (cy + (ry - width / 2) * math.sin(t)) * K))
    draw.polygon(outer + inner[::-1], fill=BROW + (255,))


def bezier(points, steps=40):
    """A Bezier curve (quadratic or cubic) of points of the GIF, in pixels of the drawing."""
    out = []
    n = len(points) - 1
    for i in range(steps + 1):
        t = i / steps
        coefficients = [math.comb(n, k) * (1 - t) ** (n - k) * t ** k for k in range(n + 1)]
        out.append((sum(c * x for c, (x, _) in zip(coefficients, points)) * K,
                    sum(c * y for c, (_, y) in zip(coefficients, points)) * K))
    return out


# The upper lip of the original: two scallops meeting in a point in the middle (a "3" lying down), on the coordinates
# of the GIF
LEFT_LIP = [(447, 488), (476, 555), (537, 497)]
RIGHT_LIP = [(537, 497), (584, 545), (609, 500)]


def smile_lines(draw):
    for lip in (LEFT_LIP, RIGHT_LIP):
        draw.line(bezier(lip), fill=MOUTH_LINE + (255,), width=7 * K, joint='curve')
        for x, y in (lip[0], lip[-1]):
            draw.ellipse(p(x - 3.5, y - 3.5, x + 3.5, y + 3.5), fill=MOUTH_LINE + (255,))


def open_mouth(image):
    """The open mouth hanging under the upper lip, as on the original: its top is the end of the two scallops, its
    bottom a round U; pink, darker under the lip, the tongue at the bottom."""
    left = bezier(LEFT_LIP)[22:]   # from about x = 484 to the middle
    right = bezier(RIGHT_LIP)[:19]  # from the middle to about x = 580
    start, end = right[-1], left[0]
    bottom = bezier([(start[0] / K, start[1] / K), (start[0] / K - 2, 590), (end[0] / K + 2, 590),
                     (end[0] / K, end[1] / K)])
    outline = left + right + bottom
    mask = Image.new('L', image.size, 0)
    ImageDraw.Draw(mask).polygon(outline, fill=255)
    inside = Image.new('RGBA', image.size, (0, 0, 0, 0))
    idraw = ImageDraw.Draw(inside)
    idraw.rectangle(p(470, 480, 600, 600), fill=MOUTH_DARK + (255,))
    idraw.ellipse(p(478, 520, 590, 620), fill=MOUTH + (255,))
    idraw.ellipse(p(500, 548, 562, 600), fill=TONGUE + (255,))
    inside = inside.filter(ImageFilter.GaussianBlur(3 * K))
    inside.putalpha(mask)
    image.alpha_composite(inside)
    ImageDraw.Draw(image).line(bottom, fill=MOUTH_LINE + (255,), width=7 * K, joint='curve')


def masks(gif):
    """The wings and the body of the first frame of the GIF (the frames differ by the face only)."""
    gif.seek(0)
    rgba = gif.convert('RGBA')
    wings = Image.new('L', (W, H), 0)
    shape = Image.new('L', (W, H), 0)
    for y in range(H):
        for x in range(W):
            r, g, b, a = rgba.getpixel((x, y))
            if a:
                shape.putpixel((x, y), 255)
                if b > r + 25 and g > r + 25:
                    wings.putpixel((x, y), 255)
    # The body: what is not of the color of the wings, without the thin lines of mixed colors along the edges of the
    # wings (an opening: they made yellow bits on the wings), its small holes closed
    body = ImageChops.subtract(shape, wings)
    body = body.filter(ImageFilter.MinFilter(5)).filter(ImageFilter.MaxFilter(5))
    body = body.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.MinFilter(3))
    # The wings: all the rest of the drawing, their edges included
    wings = ImageChops.subtract(shape, body)
    return smooth_mask(wings), smooth_mask(body), smooth_mask(shape)


def frame(wings, body, shape, winking):
    image = Image.new('RGBA', (W * K, H * K), (0, 0, 0, 0))
    # Each wing light at its tip, darker towards the body
    left = ImageChops.multiply(wings, box_mask((0, 0, 450, H)))
    right = ImageChops.multiply(wings, box_mask((450, 0, W, H)))
    paint(image, left, gradient_box((0, 0, 420, 380), 40, WING_LIGHT, WING_DARK))
    paint(image, right, gradient_box((560, 120, W, 420), 140, WING_LIGHT, WING_DARK))
    # The edge of the wings on the outline of the drawing only, not along the body
    paint(image, ImageChops.multiply(edge(shape, 3 * K), wings), Image.new('RGBA', image.size, WING_EDGE + (255,)))
    vein_layer = Image.new('RGBA', image.size, (0, 0, 0, 0))
    vdraw = ImageDraw.Draw(vein_layer)
    veins(vdraw, 200, 190, 225, 110, 220, 5)
    veins(vdraw, 742, 266, 150, 78, 142, 4)
    clipped = Image.new('RGBA', image.size, (0, 0, 0, 0))
    clipped.paste(vein_layer, (0, 0), ImageChops.multiply(vein_layer.getchannel('A'),
                                                          erode(wings, 4 * K)))
    image.alpha_composite(clipped)

    paint(image, body, gradient(225, BODY, BODY_SHADE))
    # A soft shade along the edge of the body, as on the original
    rim = edge(body, 14 * K).filter(ImageFilter.GaussianBlur(8 * K))
    rim = ImageChops.multiply(rim, body).point(lambda v: v * 0.45)
    paint(image, rim, Image.new('RGBA', image.size, BODY_SHADE + (255,)))

    draw = ImageDraw.Draw(image)
    for (cx, cy, rx, ry, start, end) in ((455, 322, 56, 52, 195, 330), (608, 372, 50, 56, 215, 345)):
        crescent(draw, cx, cy, rx, ry, start, end, 21)
    open_eye(draw, 594, 420, 40, 62, 582, 441, 14, 29)
    if winking:
        draw.arc(p(408, 380, 518, 474), 192, 348, fill=INK + (255,), width=15 * K)
        open_mouth(image)
        smile_lines(draw)
    else:
        open_eye(draw, 474, 415, 52, 56, 465, 421, 16, 32)
        smile_lines(draw)
    return image.resize((W * OUT_SCALE, H * OUT_SCALE), Image.LANCZOS)


source, out = sys.argv[1], sys.argv[2]
os.makedirs(out, exist_ok=True)
wing_mask, body_mask, shape_mask = masks(Image.open(source))
frame(wing_mask, body_mask, shape_mask, True).save(os.path.join(out, 'bravo-1.png'), optimize=True)
frame(wing_mask, body_mask, shape_mask, False).save(os.path.join(out, 'bravo-2.png'), optimize=True)
print('ok')
