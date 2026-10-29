"""Coloring pages of the game Coloring (gazeplay-games: data/colors/images/<level>/draw-*.png), from the black outlines of
OpenMoji (https://openmoji.org/, CC BY-SA 4.0): put on a white background and enlarged, so that the game, which fills
the closed zones, gets smooth lines. Only emojis whose zones are closed are kept: an open line lets the color run into
the background. Made in October 2026; they replace drawings of unknown source and a Hello Kitty drawing (not free).

Usage: python make_coloring_images.py <folder of the black 618x618 OpenMoji PNG files> <output folder>
(one folder per level: easy, normal, hard)
(the files of https://github.com/hfg-gmuend/openmoji, black/618x618)
"""
import os
import sys

from PIL import Image, ImageFilter

# Level (folder) -> file name of the coloring page -> code of the OpenMoji emoji. The level is given by the number of
# closed zones the game can fill (counted on the page as the game sees it, zones of at least 0.05 % of the page):
# easy 1 or 2, normal 3 to 6, hard 7 or more (October 2026).
PAGES = {
    "easy": {"draw-fish": "1F41F", "draw-frog": "1F438", "draw-star": "2B50", "draw-sun": "1F31E",
             "draw-tree": "1F333", "draw-balloon": "1F388", "draw-chick": "1F424", "draw-strawberry": "1F353"},
    "normal": {"draw-flower": "1F33C", "draw-cake": "1F382", "draw-owl": "1F989", "draw-rocket": "1F680",
               "draw-house": "1F3E0", "draw-butterfly": "1F98B", "draw-icecream": "1F366", "draw-gift": "1F381"},
    "hard": {"draw-turtle": "1F422", "draw-teddy": "1F9F8", "draw-train": "1F682", "draw-grapes": "1F347",
             "draw-sunflower": "1F33B", "draw-castle": "1F3F0", "draw-ferriswheel": "1F3A1", "draw-gem": "1F48E"},
}

SIZE = 1236  # twice the size of the files: smoother lines once turned into black and white by the game
MARGIN = 0.06  # white margin around the drawing, as a fraction of the size
# Thicker lines (a minimum filter of 17 pixels): they close the small gaps where lines meet (apple, butterfly,
# strawberry), and are easier to follow for a child. Emojis with an open part are not kept (e.g. the cat, the dog, the
# car, the apple, the cherries, the fire engine, the mushroom of OpenMoji).
THICKENING = 17


def page(path):
    emoji = Image.open(path).convert("RGBA")
    inner = int(SIZE * (1 - 2 * MARGIN))
    emoji = emoji.resize((inner, inner), Image.LANCZOS)
    result = Image.new("RGBA", (SIZE, SIZE), "white")
    result.alpha_composite(emoji, ((SIZE - inner) // 2, (SIZE - inner) // 2))
    return result.convert("RGB").filter(ImageFilter.MinFilter(THICKENING))


if __name__ == "__main__":
    source, output = sys.argv[1], sys.argv[2]
    for level, pages in PAGES.items():
        os.makedirs(os.path.join(output, level), exist_ok=True)
        for name, code in pages.items():
            page(os.path.join(source, code + ".png")).save(os.path.join(output, level, name + ".png"), optimize=True)
            print(level, name)
