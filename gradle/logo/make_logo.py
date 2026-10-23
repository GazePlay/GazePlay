"""Builds the GazePlay Learning logos: the drawn GAZE PLAY letters, with "Learning" below in Fredoka (OFL 1.1).

Usage: python make_logo.py <font.ttf> <output dir>

<font.ttf>: Fredoka, variable version (https://github.com/google/fonts/tree/main/ofl/fredoka, file
"Fredoka[wdth,wght].ttf", SIL Open Font License 1.1: see Fredoka-OFL.txt). The font itself is not shipped: only the
images it draws: copy them to gazeplay-core/src/main/resources/data/common/images (logos/gazeplay-learning-top-logo.png,
GazePlayLetters/learning.png). The source logo, gazeplay-top-logo.png (drawn GAZE PLAY letters and mascot), is next to
this script. Needs Pillow.
"""
import os
import sys

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

PINK = (236, 0, 84)
BLUE = (2, 106, 229)
YELLOW = (255, 251, 2)
GREEN = (13, 189, 3)
COLORS = [PINK, BLUE, YELLOW, GREEN]


def font(path, size):
    f = ImageFont.truetype(path, size)
    try:
        f.set_variation_by_axes([700, 100])  # weight: Bold, width: normal
    except Exception:
        try:
            f.set_variation_by_name("Bold")
        except Exception:
            pass
    return f


def word_image(font_path, text, height, start_color=0):
    """Colored letters with a soft shadow and a white halo, like the drawn letters."""
    size = int(height * 0.95)
    f = font(font_path, size)
    pad = int(height * 0.35)
    spacing = int(size * 0.02)
    widths = [f.getbbox(c)[2] - f.getbbox(c)[0] for c in text]
    total = sum(widths) + spacing * (len(text) - 1) + 2 * pad
    canvas_h = int(height * 1.6) + 2 * pad
    letters = Image.new("RGBA", (total, canvas_h), (0, 0, 0, 0))
    mask = Image.new("L", (total, canvas_h), 0)
    draw = ImageDraw.Draw(letters)
    mdraw = ImageDraw.Draw(mask)
    x = pad
    for i, c in enumerate(text):
        box = f.getbbox(c)
        color = COLORS[(start_color + i) % len(COLORS)]
        draw.text((x - box[0], pad), c, font=f, fill=color + (255,))
        mdraw.text((x - box[0], pad), c, font=f, fill=255)
        x += widths[i] + spacing

    # Rounded white halo: the same text drawn with a thick round stroke
    halo = Image.new("L", (total, canvas_h), 0)
    hdraw = ImageDraw.Draw(halo)
    x = pad
    for i, c in enumerate(text):
        box = f.getbbox(c)
        hdraw.text((x - box[0], pad), c, font=f, fill=255, stroke_width=max(2, int(height * 0.13)), stroke_fill=255)
        x += widths[i] + spacing
    halo = halo.filter(ImageFilter.GaussianBlur(1.2))

    shadow_off = max(2, int(height * 0.06))
    shadow = ImageChops.offset(mask, shadow_off, shadow_off).filter(ImageFilter.GaussianBlur(height * 0.04))

    out = Image.new("RGBA", letters.size, (0, 0, 0, 0))
    out.paste(Image.new("RGBA", letters.size, (255, 255, 255, 255)), (0, 0), halo)
    out.paste(Image.new("RGBA", letters.size, (60, 60, 60, 140)), (0, 0),
              shadow.point(lambda v: int(v * 0.55)))
    out = Image.alpha_composite(out, letters)
    return out.crop(out.getbbox())


def stack(top, bottom, overlap):
    width = max(top.width, bottom.width)
    out = Image.new("RGBA", (width, top.height + bottom.height - overlap), (0, 0, 0, 0))
    out.alpha_composite(top, ((width - top.width) // 2, 0))
    out.alpha_composite(bottom, ((width - bottom.width) // 2, top.height - overlap))
    return out


def main():
    font_path, output = sys.argv[1:3]
    os.makedirs(output, exist_ok=True)
    # Static logo of the menu: the drawn GAZE PLAY letters and the mascot, "Learning" below
    logo = Image.open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "gazeplay-top-logo.png")).convert("RGBA")
    logo = logo.crop(logo.getbbox())
    word = word_image(font_path, "Learning", int(logo.height * 0.30))
    result = stack(logo, word, int(logo.height * 0.08))
    result.save(os.path.join(output, "gazeplay-learning-top-logo.png"))
    print("gazeplay-learning-top-logo.png", logo.size, "->", result.size)

    # Word alone, shown below the animated letters
    word = word_image(font_path, "Learning", 120)
    word.save(os.path.join(output, "learning.png"))
    print("learning.png", word.size)


if __name__ == "__main__":
    main()
