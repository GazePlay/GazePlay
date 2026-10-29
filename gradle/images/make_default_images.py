"""Builds the default images of GazePlay Learning from OpenMoji (CC BY-SA 4.0) PNG files (see README.txt).

python make_default_images.py <folder of the 618x618 OpenMoji PNG files> <output folder>
The output folder must hold the empty folders "default" and "noBackGround". Pillow is required."""
import sys, os
from PIL import Image

src, out = sys.argv[1], sys.argv[2]
ANIMALS = {  # file name in GazePlay -> OpenMoji code point
    'bear': '1F43B', 'dog': '1F415', 'elephant': '1F418', 'elephant2': '1F9A3', 'fox': '1F98A',
    'giraffe': '1F992', 'hippo': '1F99B', 'lion': '1F981', 'snake': '1F40D', 'turtle': '1F422',
}

def emoji(code, size):
    return Image.open(os.path.join(src, code + '.png')).convert('RGBA').resize((size, size), Image.LANCZOS)

for name, code in ANIMALS.items():
    # On a white background, as the former JPEG images
    page = Image.new('RGBA', (1000, 1000), 'white')
    page.alpha_composite(emoji(code, 920), (40, 40))
    page.convert('RGB').save(os.path.join(out, 'default', name + '.jpg'), quality=92)
    if name != 'elephant2':
        # Without background, for the labyrinth
        emoji(code, 512).save(os.path.join(out, 'noBackGround', name + '.png'), optimize=True)

# The explosion of Magic Potions: same size, frames and timing as the former animation (700 x 316, 8 x 350 ms)
burst = Image.open(os.path.join(src, '1F4A5.png')).convert('RGBA')
main = [90, 150, 210, 260, 300, 300, 230, 140]
side = [0, 0, 60, 110, 150, 150, 110, 60]
frames = []
for i in range(8):
    frame = Image.new('RGBA', (700, 316), (0, 0, 0, 0))
    for x, size, angle in ((170, side[i], 12), (530, side[i], -12), (350, main[i], 8 * (i % 2))):
        if size:
            b = burst.resize((size, size), Image.LANCZOS).rotate(angle, resample=Image.BICUBIC)
            frame.alpha_composite(b, (x - size // 2, 316 - size if size > 300 else (316 - size) // 2 + 20))
    # GIF transparency is all or nothing
    alpha = frame.getchannel('A').point(lambda a: 255 if a >= 128 else 0)
    rgb = Image.new('RGB', frame.size, (255, 0, 255))
    rgb.paste(frame.convert('RGB'), mask=alpha)
    p = rgb.quantize(colors=255, method=Image.MEDIANCUT)
    pal = p.getpalette()[:765] + [255, 0, 255]
    p.putpalette(pal)
    p.paste(255, mask=alpha.point(lambda a: 255 if a == 0 else 0))
    frames.append(p)
frames[0].save(os.path.join(out, 'explosion.gif'), save_all=True, append_images=frames[1:], duration=350,
               disposal=2, transparency=255, optimize=False)
print('ok')
