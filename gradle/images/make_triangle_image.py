"""Draws the triangle (the percussion instrument) of "Where is the sound?": no photo of Wikimedia Commons under a licence
compatible with the GPL 3 showed it well (October 2026). The image is under the license of GazePlay Learning
(GPL-3.0-or-later).

Usage: python make_triangle_image.py <output.jpg>
"""
import math
import sys

from PIL import Image, ImageDraw, ImageFilter

W, H, S = 1280, 960, 3  # size, and supersampling for smooth edges


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


img = Image.new("RGB", (W * S, H * S))
draw = ImageDraw.Draw(img)
# Soft background, light at the top
for y in range(H * S):
    draw.line([(0, y), (W * S, y)], fill=lerp((236, 242, 250), (196, 210, 228), y / (H * S)))

# The triangle: a steel bar bent in an equilateral triangle, open at the bottom left corner
cx, cy, side = W * S / 2, H * S * 0.56, H * S * 0.62
height = side * math.sqrt(3) / 2
top = (cx, cy - height * 2 / 3)
left = (cx - side / 2, cy + height / 3)
right = (cx + side / 2, cy + height / 3)
bar = int(48 * S)
gap = 0.10  # open part of the corner, as a fraction of a side


def point(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


segments = [(point(left, top, gap), top), (top, right), (right, point(right, left, 1 - gap))]
shadow = Image.new("L", img.size, 0)
sd = ImageDraw.Draw(shadow)
for a, b in segments:
    sd.line([(a[0] + 18 * S, a[1] + 24 * S), (b[0] + 18 * S, b[1] + 24 * S)], fill=110, width=bar)
shadow = shadow.filter(ImageFilter.GaussianBlur(14 * S))
img.paste((60, 70, 90), (0, 0), shadow)

for a, b in segments:
    draw.line([a, b], fill=(120, 128, 140), width=bar)
    draw.line([a, b], fill=(214, 220, 228), width=int(bar * 0.3))
for p in (top, right):
    draw.ellipse([p[0] - bar / 2, p[1] - bar / 2, p[0] + bar / 2, p[1] + bar / 2], fill=(120, 128, 140))
    draw.ellipse([p[0] - bar * 0.22, p[1] - bar * 0.22, p[0] + bar * 0.22, p[1] + bar * 0.22], fill=(214, 220, 228))

# The string holding it, from the top corner
draw.line([(top[0], top[1] - bar / 2), (top[0], H * S * 0.04)], fill=(150, 60, 50), width=int(9 * S))

# The beater, a thin steel rod with a handle, crossing the right side
b0, b1 = (cx + side * 0.05, cy + height * 0.05), (cx + side * 0.62, cy - height * 0.55)
draw.line([b0, b1], fill=(110, 118, 130), width=int(18 * S))
h0 = point(b0, b1, 0.68)
draw.line([h0, b1], fill=(40, 40, 46), width=int(42 * S))

img = img.resize((W, H), Image.LANCZOS)
img.save(sys.argv[1], "JPEG", quality=88, optimize=True)
