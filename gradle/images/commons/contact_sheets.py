"""Contact sheets of the candidate photos: 3 subjects per sheet, 8 numbered candidates each."""
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT, OUT = sys.argv[1], sys.argv[2]
os.makedirs(OUT, exist_ok=True)
TW, TH, PER_ROW = 300, 200, 4
font = ImageFont.truetype("arial.ttf", 22)
subjects = sorted(d for d in os.listdir(ROOT) if os.path.exists(os.path.join(ROOT, d, "meta.json")))
for start in range(0, len(subjects), 3):
    group = subjects[start:start + 3]
    rows = 2 * len(group)
    sheet = Image.new("RGB", (PER_ROW * TW, rows * (TH + 30)), "white")
    draw = ImageDraw.Draw(sheet)
    for g, subject in enumerate(group):
        meta = json.load(open(os.path.join(ROOT, subject, "meta.json"), encoding="utf-8"))
        for i, c in enumerate(meta):
            im = Image.open(os.path.join(ROOT, subject, c["file"])).convert("RGB")
            im.thumbnail((TW - 6, TH - 6))
            x = (i % PER_ROW) * TW
            y = (2 * g + i // PER_ROW) * (TH + 30)
            sheet.paste(im, (x + (TW - im.width) // 2, y + 30 + (TH - im.height) // 2))
            draw.text((x + 4, y + 4), f"{subject} c{i + 1}", fill="red", font=font)
    sheet.save(os.path.join(OUT, f"sheet{start // 3 + 1:02d}.jpg"), quality=80)
    print("sheet", start // 3 + 1, group)
