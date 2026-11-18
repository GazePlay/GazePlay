"""
Inventory of the sounds shipped by GazePlay Learning (maintainer's request of 18/11/2026, on the model of the
inventory of the images, INVENTAIRE_IMAGES.md). For each sound of a src/main folder followed by git:

1. its origin: the first commit of the file, following its moves (one pass on the history, renames detected);
2. what the file says of itself: ID3 tags (title, artist, comment, copyright, encoder), the encoder written in the
   MP3 frames (LAME, Lavf of FFmpeg), the INFO chunk of a WAV;
3. its source when the project knows it: a gradle/images/make_*.py script that makes it, a line of a CREDITS.txt
   shipped with a game, a known file (the music of the shooting game).

Writes gradle/images/inventaire-sons.csv. Run from the root of the repository:
    python gradle/images/inventaire_sons.py            (writes the CSV)
    python gradle/images/inventaire_sons.py --survey   (prints the metadata found, by folder)
"""
import collections
import csv
import os
import re
import struct
import subprocess
import sys

AUDIO = re.compile(r'\.(mp3|wav|ogg|m4a|aac|aif|aiff|flac|mid|midi)$', re.IGNORECASE)
DATA = re.compile(r'^[^/]+/src/main/resources/data/')
HERE = os.path.dirname(os.path.abspath(__file__))

# The data folder of the sounds made by each script of this folder
SCRIPT_FOLDERS = {
    'make_biboulejump_sounds.py': ['biboulejump'],
    'make_blocs_sounds.py': ['blocs'],
    'make_bottle_sounds.py': ['bottle'],
    'make_bubbles_sounds.py': ['bubble'],
    'make_cakes_sounds.py': ['cake'],
    'make_connect4_sounds.py': ['connect4'],
    'make_creampie_images.py': ['creampie'],
    'make_cups_sounds.py': ['cups'],
    'make_dice_sounds.py': ['dice', 'common'],
    'make_egg_sounds.py': ['egg'],
    'make_flowerofnumbers_sounds.py': ['flowerOfNumbers'],
    'make_follow_sounds.py': ['follow'],
    'make_goose_sounds.py': ['goosegame'],
    'make_magiccards_sounds.py': ['magiccards'],
    'make_math101_sounds.py': ['math101'],
    'make_moles_sounds.py': ['whackmole'],
    'make_ninja_sounds.py': ['ninja'],
    'make_order_sounds.py': ['order'],
    'make_potions_sounds.py': ['potions'],
    'make_rockpaperscissors_sounds.py': ['rockPaperScissors'],
    'make_scratchcard_sounds.py': ['scratchcard'],
    'make_shooter_sounds.py': ['shooter'],
    'make_simon_sounds.py': ['simonGame'],
    'make_spotdifferences_sound.py': ['spotthedifferences'],
    'make_sprintfinish_sounds.py': ['sprintFinish'],
    'make_tictactoe_sounds.py': ['noughtsandcrosses'],
    'make_towerdefense_sounds.py': ['towerDefense'],
    'make_trainswitches_sounds.py': ['trainSwitches'],
}

# Sounds whose source is written elsewhere than in a CREDITS.txt (gradle/images/README.txt)
KNOWN = {
    'colorblend/sounds/drop.wav': ('libre, source connue',
                                   'copie du « plop » de make_cakes_sounds.py (GPL-3.0-or-later)'),
    'shooter/music/': ('libre, source connue',
                       '« Talking Cute (Chiptune) », Pro Sensory (Alex McCulloch), OpenGameArt, CC0'),
    'home/sounds/songidea(copycat)_0.mp3': ('libre, source connue',
                                            '« copycat », syncopika, OpenGameArt '
                                            '(https://opengameart.org/content/copycat), CC BY 3.0'),
    'spotthedifferences/ding.wav': ('libre, source connue',
                                    'fait par gradle/images/make_spotdifferences_sound.py (GPL-3.0-or-later)'),
    # Copies of sounds of Where Is The Sound (data/soundsoflife/CREDITS.txt)
    'soundsoflife/farm/sheep.mp3': ('libre, source connue',
                                    'domaine public : Sheep bleating.ogg, earthcalling, Wikimedia Commons'),
    'soundsoflife/jungle/elephant.mp3': ('libre, source connue',
                                         'CC0 : Elephant voice - trumpeting.ogg, Wikimedia Commons'),
    'soundsoflife/jungle/snake.mp3': ('libre, source connue', 'domaine public : Rattlesnake.ogg, Wikimedia Commons'),
}

TEXT_FRAMES = {'TIT2': 'titre', 'TPE1': 'artiste', 'TALB': 'album', 'TCOP': 'copyright', 'TENC': 'encode_par',
               'TSSE': 'encodeur', 'TCON': 'genre', 'TYER': 'annee', 'TDRC': 'annee', 'WXXX': 'url', 'COMM': 'comm'}
TEXT_FRAMES_V22 = {'TT2': 'titre', 'TP1': 'artiste', 'TAL': 'album', 'TEN': 'encode_par', 'TSS': 'encodeur',
                   'COM': 'comm', 'TCR': 'copyright'}


def git(*args):
    return subprocess.run(['git', *args], capture_output=True, text=True, encoding='utf-8', check=True).stdout


def shipped_sounds():
    return sorted(path for path in git('ls-files', '-z').split('\0')
                  if AUDIO.search(path) and '/src/main/' in path)


def origins():
    """The first commit of each path, its moves followed: {path: (date, author, message)}."""
    # No limit to the detection of the renames: the resources moved by thousands at once (December 2019) were else
    # seen as added then
    log = git('-c', 'diff.renameLimit=1000000', 'log', '--reverse', '-M', '--name-status', '--date=short',
              '--format=%x01%ad%x09%an%x09%s',
              '--', '*.mp3', '*.wav', '*.MP3', '*.WAV', '*.mid', '*.ogg', '*.m4a')
    found = {}
    commit = None
    for line in log.splitlines():
        if line.startswith('\x01'):
            commit = tuple(line[1:].split('\t', 2))
            continue
        if not line.strip():
            continue
        parts = line.split('\t')
        status = parts[0]
        if status.startswith('A'):
            found.setdefault(parts[1], commit)
        elif status.startswith('R') or status.startswith('C'):
            first = found.get(parts[1], commit)
            found[parts[2]] = first
    return found


MOVES = re.compile(r'Moved resources into games module|Moved all game resources')


def followed_origin(path):
    """The first commit of a file, followed one by one (for the files of the moves of December 2019)."""
    lines = git('log', '--follow', '--diff-filter=A', '--date=short', '--format=%ad%x09%an%x09%s', '--',
                path).strip().splitlines()
    return tuple(lines[-1].split('\t', 2)) if lines else None


def decode(encoding, data):
    try:
        if encoding == 0:
            return data.decode('latin-1')
        if encoding == 1:
            return data.decode('utf-16')
        if encoding == 2:
            return data.decode('utf-16-be')
        return data.decode('utf-8')
    except UnicodeDecodeError:
        return data.decode('latin-1', 'replace')


def id3_tags(data):
    tags = {}
    if data[:3] != b'ID3' or len(data) < 10:
        return tags
    version = data[3]
    size = (data[6] << 21) | (data[7] << 14) | (data[8] << 7) | data[9]
    position, end = 10, min(10 + size, len(data))
    while position + 6 < end:
        if version == 2:
            frame = data[position:position + 3].decode('latin-1', 'replace')
            length = int.from_bytes(data[position + 3:position + 6], 'big')
            body = data[position + 6:position + 6 + length]
            position += 6 + length
            name = TEXT_FRAMES_V22.get(frame)
        else:
            frame = data[position:position + 4].decode('latin-1', 'replace')
            raw = data[position + 4:position + 8]
            length = ((raw[0] << 21) | (raw[1] << 14) | (raw[2] << 7) | raw[3]) if version == 4 \
                else int.from_bytes(raw, 'big')
            body = data[position + 10:position + 10 + length]
            position += 10 + length
            name = TEXT_FRAMES.get(frame)
        if not frame.strip('\0') or length <= 0:
            break
        if name and body:
            if frame in ('COMM', 'COM'):
                text = decode(body[0], body[4:])
            elif frame == 'WXXX':
                text = decode(0, body[1:])
            else:
                text = decode(body[0], body[1:])
            text = ' '.join(text.replace('\0', ' ').split())
            if text:
                tags[name] = text
    return tags


def mp3_encoder(data):
    for marker in (b'LAME', b'Lavf', b'Lavc', b'GOGO', b'Xing'):
        index = data.find(marker, 0, 20000)
        if index >= 0:
            return data[index:index + 9].decode('latin-1', 'replace').strip('\0 ').split('\0')[0]
    return ''


def wav_info(data):
    tags = {}
    if data[:4] != b'RIFF' or data[8:12] != b'WAVE':
        return tags
    position = 12
    while position + 8 <= len(data):
        chunk = data[position:position + 4]
        length = struct.unpack('<I', data[position + 4:position + 8])[0]
        if chunk == b'LIST' and data[position + 8:position + 12] == b'INFO':
            inner, end = position + 12, position + 8 + length
            while inner + 8 <= end:
                key = data[inner:inner + 4].decode('latin-1', 'replace')
                size = struct.unpack('<I', data[inner + 4:inner + 8])[0]
                value = data[inner + 8:inner + 8 + size].decode('latin-1', 'replace').strip('\0 ')
                if value:
                    tags[key] = value
                inner += 8 + size + (size & 1)
        position += 8 + length + (length & 1)
    return tags


def metadata(path):
    with open(path, 'rb') as file:
        data = file.read()
    if path.lower().endswith('.wav'):
        info = wav_info(data)
        return '; '.join('%s=%s' % item for item in sorted(info.items()))
    tags = id3_tags(data)
    encoder = mp3_encoder(data[10:] if data[:3] != b'ID3' else data)
    if encoder:
        tags.setdefault('trame', encoder)
    return '; '.join('%s=%s' % item for item in sorted(tags.items()))


def made_by_scripts():
    """{data-relative path pattern (regex): script} for the sounds written by the scripts of this folder."""
    patterns = []
    for script, folders in SCRIPT_FOLDERS.items():
        with open(os.path.join(HERE, script), encoding='utf-8') as file:
            text = file.read()
        for name in re.findall(r"""['"]([\w%]+\.wav)['"]""", text):
            name_regex = re.escape(name).replace('%d', r'\d+')
            for folder in folders:
                patterns.append((re.compile('^' + re.escape(folder) + '/(.*/)?' + name_regex + '$'), script))
    return patterns


def credits():
    """{data-relative path: (licence, source)} from the CREDITS.txt files shipped with the games."""
    found = {}
    for path in git('ls-files', '-z').split('\0'):
        if not path.endswith('CREDITS.txt') or '/src/main/' not in path:
            continue
        folder = DATA.sub('', os.path.dirname(path).replace('\\', '/'))
        with open(path, encoding='utf-8') as file:
            for line in file:
                parts = [part.strip() for part in line.split(' | ')]
                if len(parts) >= 4 and AUDIO.search(parts[0]):
                    sound = parts[0] if '/' in parts[0] else 'sounds/' + parts[0]
                    found[folder + '/' + sound] = (parts[-2], parts[1] + ', ' + parts[2] + ', ' + parts[-1])
    return found


# Voices, from the history of the project (pull requests and issues of GitHub), by the message of their first commit
VOICES = [
    (re.compile(r'Added sounds for where-is-the-letter and where-is-the-number'),
     'source connue, conditions à vérifier',
     'voix de synthèse Amazon Polly, script GazePlay/gazeplay-voice-synthesis (ticket #1116, PR #1121, 2020)'),
    (re.compile(r'eng and fra text to speech|text to speech', re.IGNORECASE),
     'source connue, conditions à vérifier', 'voix de synthèse de ttsmp3.com (PR #1516, 2021)'),
]


def classify(relative, scripts, credited, message='', tags='', date='', author=''):
    for pattern, script in scripts:
        if pattern.match(relative):
            return 'libre, source connue', 'fait par gradle/images/%s (GPL-3.0-or-later)' % script
    for known, answer in KNOWN.items():
        if relative == known or (known.endswith('/') and relative.startswith(known)):
            return answer
    for credited_path, (licence, source) in credited.items():
        if relative == credited_path or relative.endswith('/' + credited_path.split('/', 1)[1]) \
                and relative.split('/')[0] == credited_path.split('/')[0]:
            return 'libre, source connue', '%s : %s' % (licence, source)
    if 'LaSonotheque.org' in tags:
        # Credited in data/whereIsTheSound/CREDITS.txt and data/soundsoflife/CREDITS.txt
        return 'libre, source connue', 'La Sonothèque (https://lasonotheque.org), CC0 1.0 (CREDITS.txt du jeu)'
    if 'Aludra Software' in tags:
        return 'non libre probable', 'étiquette du fichier : © Aludra Software 1998 (Sound Forge)'
    if 'dvdvideosoft' in tags:
        return 'source inconnue', 'étiquette du fichier : www.dvdvideosoft.com (convertisseur de vidéos, YouTube ?)'
    for pattern, category, source in VOICES:
        if pattern.search(message):
            return category, source
    folder = relative.split('/')[0]
    if folder == 'literacy' or folder.startswith('whereIsThe') or folder == 'findTheOddOneOut':
        where = ' (PR #1626 ou #1603)' if date.startswith('2022') else ''
        return 'source inconnue', 'voix ajoutée en %s par %s%s, outil ou voix non indiqués' % (date[:4], author, where)
    return 'source inconnue', ''


def main():
    sounds = shipped_sounds()
    first = origins()
    scripts = made_by_scripts()
    credited = credits()
    if '--survey' in sys.argv:
        by_folder = collections.defaultdict(collections.Counter)
        for path in sounds:
            relative = DATA.sub('', path)
            folder = '/'.join(relative.split('/')[:2])
            by_folder[folder][metadata(path) or '(rien)'] += 1
        for folder, counter in sorted(by_folder.items()):
            print('==', folder, sum(counter.values()))
            for value, count in counter.most_common(6):
                print('   %4d  %s' % (count, value[:160]))
        return
    rows = []
    for path in sounds:
        relative = DATA.sub('', path)
        date, author, message = first.get(path, ('', '', ''))
        if MOVES.search(message):
            date, author, message = followed_origin(path) or (date, author, message)
        tags = metadata(path)
        category, source = classify(relative, scripts, credited, message, tags, date, author)
        rows.append([path, 'oui', category, source, date, author, message, tags])
    with open(os.path.join(HERE, 'inventaire-sons.csv'), 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(['fichier', 'livree', 'categorie', 'source_ou_indice', 'ajoutee_le', 'ajoutee_par',
                         'message_du_commit', 'metadonnees'])
        writer.writerows(rows)
    counter = collections.Counter(row[2] for row in rows)
    print(len(rows), 'sons livrés :', dict(counter))


if __name__ == '__main__':
    main()
