"""Installs the chosen Commons photos in the games, and writes their credits."""
import json
import os
import sys

from PIL import Image

CANDIDATES = sys.argv[1:-2]  # folders of candidates (commons, commons2...), searched in order
CHOICES, DATA = sys.argv[-2], sys.argv[-1]
GAMES = {"animal": "whereIsTheAnimal", "sound": "whereIsTheSound"}
# Photos kept (free, already credited)
KEEP = {("sound", "harp"): ["harp_1.jpg"]}
MAX_SIZE = 1280

credits = {game: [] for game in GAMES}
for line in open(CHOICES, encoding="utf-8"):
    parts = line.split()
    if not parts:
        continue
    key, picks = parts[0], parts[1:]
    game, folder = key.split("-", 1)
    target = os.path.join(DATA, GAMES[game], "images", folder)
    keep = KEEP.get((game, folder), [])
    for name in os.listdir(target):
        if name not in keep:
            os.remove(os.path.join(target, name))
    number = len(keep)
    for pick in picks:
        # "2" is candidate 2 of the first folder; "b3" candidate 3 of the second folder (commons2), etc.
        source = 0
        if pick[0].isalpha():
            source, pick = ord(pick[0]) - ord("a"), pick[1:]
        meta = json.load(open(os.path.join(CANDIDATES[source], key, "meta.json"), encoding="utf-8"))
        c = meta[int(pick) - 1]
        number += 1
        name = f"{folder}_{number}.jpg"
        im = Image.open(os.path.join(CANDIDATES[source], key, c["file"])).convert("RGB")
        im.thumbnail((MAX_SIZE, MAX_SIZE), Image.LANCZOS)
        im.save(os.path.join(target, name), "JPEG", quality=82, optimize=True, progressive=True)
        licence = c["licence"] + (f" ({c['licence_url']})" if c["licence_url"] else "")
        credits[game].append(f"{folder}/{name} | {c['title'][5:]} | {' '.join(c['author'].split()) or 'unknown'} | {licence} | {c['page']}")
    print(key, number)

for game, lines in credits.items():
    if not lines:
        continue
    path = os.path.join(DATA, GAMES[game], "CREDITS.txt")
    header = [f"Photos of the game (data/{GAMES[game]}/images), from Wikimedia Commons, under licences compatible with",
              "the GPL version 3 of GazePlay Learning (CC0, public domain, CC BY, CC BY-SA 4.0). They were resized to",
              f"{MAX_SIZE} pixels at most and saved as JPEG (October 2026); they replace photos whose source was unknown",
              "or whose licence was not free. See REVUE_DES_JEUX.md.",
              "",
              "File | Title on Wikimedia Commons | Author | Licence | Page", ""]
    extra = []
    if game == "sound":
        extra = ["", "harp/harp_1.jpg: The Metropolitan Museum of Art, Open Access (CC0)."]
    open(path, "w", encoding="utf-8", newline="\n").write("\n".join(header + sorted(lines) + extra) + "\n")
    print("credits", path, len(lines))
