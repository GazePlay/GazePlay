"""Draws the images of Horses and Horses Simplified (gazeplay-games: data/horses), made for GazePlay Learning in
October 2026 to replace images of unknown source. Same license as GazePlay Learning (GPL-3.0-or-later).

python make_horses_images.py <data/horses folder>
Pillow is required. The pawns (biboules/*.png) are drawings of the project, kept.
- horsiesboard.png and horsiesBoardSimplified.png (1080 x 1080): the boards, drawn from positions.json and
  positionsSimplified.json of the same folder, so that each square is exactly where the game puts the pawns: the
  stables in the corners (with the places of the pawns), the common track, the start square of each team (an
  arrow), the square where it leaves the track (a curved arrow), its final path (numbered), and the center;
- rollImages/roll<TEAM>.png (600 x 600): the button rolling the die, a white die on a disc of the color of the team.
The image of a board is listed in its positions file ("imageName"); the game scales the board to the game area.
"""
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

SCALE = 2  # drawn larger, then reduced: smooth edges
SIZE = 1080
INK = (52, 44, 40, 255)
BOARD = (255, 248, 239, 255)
BORDER = (241, 226, 207, 255)
WHITE = (255, 255, 255, 255)

# Main color and light color of each team (softer than the former pure colors)
TEAMS = {
    'YELLOW': ((246, 190, 46, 255), (253, 236, 178, 255)),
    'BLUE': ((61, 155, 233, 255), (196, 225, 250, 255)),
    'RED': ((230, 88, 80, 255), (250, 205, 200, 255)),
    'GREEN': ((92, 184, 96, 255), (205, 236, 200, 255)),
}
# Corner of the stable of each team, as in Horses.launch (teamChooserPositions): 0 = left or top, 1 = right or bottom
CORNERS = {'YELLOW': (0, 0), 'BLUE': (1, 0), 'RED': (1, 1), 'GREEN': (0, 1)}

folder = sys.argv[1]


def font(size):
    for name in ('arialbd.ttf', 'DejaVuSans-Bold.ttf', 'Arial Bold.ttf'):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def s(*values):
    return [v * SCALE for v in values]


def disc(draw, x, y, r, fill, width=4):
    draw.ellipse(s(x - r, y - r, x + r, y + r), fill=fill, outline=INK, width=width * SCALE)


def arrow(draw, x, y, r, direction, color):
    """A triangle pointing to the next square of the track (direction: a unit vector)."""
    dx, dy = direction
    px, py = -dy, dx
    tip = (x + dx * r * 0.55, y + dy * r * 0.55)
    left = (x - dx * r * 0.4 + px * r * 0.45, y - dy * r * 0.4 + py * r * 0.45)
    right = (x - dx * r * 0.4 - px * r * 0.45, y - dy * r * 0.4 - py * r * 0.45)
    draw.polygon([tuple(v * SCALE for v in p) for p in (tip, left, right)], fill=color, outline=INK)


def unit(a, b):
    dx, dy = b[0] - a[0], b[1] - a[1]
    length = max(1e-6, (dx * dx + dy * dy) ** 0.5)
    return dx / length, dy / length


def board(positions_file):
    with open(os.path.join(folder, positions_file), encoding='utf-8') as f:
        positions = json.load(f)
    n = positions['elementsPerSide']
    cell = SIZE / n
    r = cell * 0.44
    image = Image.new('RGBA', (SIZE * SCALE, SIZE * SCALE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle(s(2, 2, SIZE - 2, SIZE - 2), radius=40 * SCALE, fill=BOARD, outline=BORDER,
                           width=4 * SCALE)
    stable = (n - 3) / 2 * cell
    numbers = font(int(cell * 0.5 * SCALE))
    for team, (color, light) in TEAMS.items():
        data = positions[team]
        # The stable: a light disc in its corner, with a place for each pawn
        cx = CORNERS[team][0] * (SIZE - stable) + stable / 2
        cy = CORNERS[team][1] * (SIZE - stable) + stable / 2
        sr = stable * 0.46
        draw.ellipse(s(cx - sr, cy - sr, cx + sr, cy + sr), fill=light, outline=color, width=10 * SCALE)
        for place in data['spawnPoints'][:positions['nbPawns']]:
            disc(draw, place['x'], place['y'], r * 1.05, WHITE, 3)
        common = [(p['x'], p['y']) for p in data['commonPath']]
        # The common track of the arm of the team, light; its first square (leaving for the final path) and its
        # second square (the start) in the color of the team
        for i, (x, y) in enumerate(common):
            disc(draw, x, y, r, color if i < 2 else light)
        start, after = common[1], common[2]
        arrow(draw, start[0], start[1], r, unit(start, after), WHITE)
        # The square leaving the track: an arrow towards the final path
        final = [(p['x'], p['y']) for p in data['finalPath']]
        arrow(draw, common[0][0], common[0][1], r, unit(common[0], final[0]), WHITE)
        # The final path: rounded squares numbered 1, 2, 3...
        for i, (x, y) in enumerate(final):
            draw.rounded_rectangle(s(x - r, y - r, x + r, y + r), radius=r * 0.35 * SCALE, fill=color,
                                   outline=INK, width=4 * SCALE)
            draw.text((x * SCALE, y * SCALE), str(i + 1), font=numbers, fill=WHITE, anchor='mm',
                      stroke_width=2 * SCALE, stroke_fill=INK)
    # The center, where the die is rolled
    c = SIZE / 2
    draw.rounded_rectangle(s(c - cell * 0.48, c - cell * 0.48, c + cell * 0.48, c + cell * 0.48),
                           radius=cell * 0.2 * SCALE, fill=WHITE, outline=INK, width=4 * SCALE)
    image = image.resize((SIZE, SIZE), Image.LANCZOS)
    image.save(os.path.join(folder, positions['imageName']), optimize=True)


def roll_button(team):
    color, light = TEAMS[team]
    size = 600
    image = Image.new('RGBA', (size * SCALE, size * SCALE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.ellipse(s(30, 30, 570, 570), fill=color, outline=INK, width=10 * SCALE)
    draw.ellipse(s(60, 60, 540, 540), outline=light, width=8 * SCALE)
    # A white die showing 5
    draw.rounded_rectangle(s(170, 170, 430, 430), radius=46 * SCALE, fill=WHITE, outline=INK, width=10 * SCALE)
    for px, py in ((230, 230), (370, 230), (300, 300), (230, 370), (370, 370)):
        draw.ellipse(s(px - 26, py - 26, px + 26, py + 26), fill=INK)
    image = image.resize((size, size), Image.LANCZOS)
    os.makedirs(os.path.join(folder, 'rollImages'), exist_ok=True)
    image.save(os.path.join(folder, 'rollImages', 'roll' + team + '.png'), optimize=True)


board('positions.json')
board('positionsSimplified.json')
for name in TEAMS:
    roll_button(name)
