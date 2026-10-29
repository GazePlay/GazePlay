"""Fetches candidate photos from Wikimedia Commons (Quality images, licences compatible with the GPL 3)."""
import html
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

OUT = sys.argv[1]
ONLY = set(sys.argv[2:])
API = "https://commons.wikimedia.org/w/api.php"
UA = "GazePlayLearning-image-review/1.0 (https://github.com/GazePlay/GazePlay)"
PER_SUBJECT = 8

# (game, folder) -> search terms, best first
SUBJECTS = {
    ("animal", "bear"): ["Ursus arctos", "brown bear"],
    ("animal", "beaver"): ["Castor fiber", "Castor canadensis"],
    ("animal", "bee"): ["Apis mellifera"],
    ("animal", "boar"): ["Sus scrofa wild boar", "wild boar"],
    ("animal", "cat"): ["domestic cat Felis catus", "cat"],
    ("animal", "chicken"): ["hen Gallus gallus domesticus", "chicken hen"],
    ("animal", "cow"): ["cow Bos taurus", "dairy cow"],
    ("animal", "crab"): ["crab Brachyura", "crab"],
    ("animal", "crocodile"): ["Crocodylus", "crocodile"],
    ("animal", "deer"): ["Cervus elaphus stag", "red deer"],
    ("animal", "dog"): ["dog Canis lupus familiaris", "dog"],
    ("animal", "dolphin"): ["Tursiops truncatus", "dolphin"],
    ("animal", "donkey"): ["donkey Equus asinus", "donkey"],
    ("animal", "eagle"): ["Aquila chrysaetos", "Haliaeetus leucocephalus", "eagle"],
    ("animal", "elephant"): ["Loxodonta africana", "Elephas maximus"],
    ("animal", "fish"): ["Amphiprion ocellaris", "Carassius auratus", "Pterophyllum"],
    ("animal", "fox"): ["Vulpes vulpes", "red fox"],
    ("animal", "giraffe"): ["Giraffa camelopardalis", "giraffe"],
    ("animal", "greaterflamingo"): ["Phoenicopterus roseus", "flamingo"],
    ("animal", "hedgehog"): ["Erinaceus europaeus", "hedgehog"],
    ("animal", "hippopotamus"): ["Hippopotamus amphibius"],
    ("animal", "horse"): ["horse Equus ferus caballus", "horse"],
    ("animal", "hyena"): ["Crocuta crocuta", "hyena"],
    ("animal", "jellyfish"): ["jellyfish Aurelia aurita", "jellyfish"],
    ("animal", "lion"): ["Panthera leo", "lion"],
    ("animal", "monkey"): ["Macaca", "monkey"],
    ("animal", "octopus"): ["Octopus vulgaris", "octopus"],
    ("animal", "owl"): ["Bubo bubo", "Asio otus", "owl"],
    ("animal", "parrot"): ["Ara macao", "Ara ararauna", "parrot"],
    ("animal", "peafowl"): ["Pavo cristatus", "peacock"],
    ("animal", "pig"): ["domestic pig Sus scrofa domesticus", "pig farm"],
    ("animal", "pigeon"): ["Columba livia", "pigeon"],
    ("animal", "rabbit"): ["Oryctolagus cuniculus", "rabbit"],
    ("animal", "raccoon"): ["Procyon lotor", "raccoon"],
    ("animal", "raven"): ["Corvus corax", "Corvus corone", "crow"],
    ("animal", "rhinoceros"): ["Ceratotherium simum", "Diceros bicornis", "rhinoceros"],
    ("animal", "roe"): ["Capreolus capreolus", "roe deer"],
    ("animal", "seahorse"): ["Hippocampus seahorse", "seahorse"],
    ("animal", "shark"): ["shark Carcharhinus", "shark"],
    ("animal", "sheep"): ["sheep Ovis aries", "sheep"],
    ("animal", "shellfish"): ["seashell", "shell of sea snail", "scallop shell"],
    ("animal", "snake"): ["snake Serpentes", "Natrix natrix", "snake"],
    ("animal", "sparrow"): ["Passer domesticus", "sparrow"],
    ("animal", "squirrel"): ["Sciurus vulgaris", "squirrel"],
    ("animal", "tiger"): ["Panthera tigris", "tiger"],
    ("animal", "turtle"): ["turtle Testudines", "tortoise", "sea turtle"],
    ("animal", "vulture"): ["Gyps fulvus", "vulture"],
    ("animal", "wolf"): ["Canis lupus wolf", "grey wolf"],
    ("animal", "whale"): ["Megaptera novaeangliae", "whale"],
    ("animal", "zebra"): ["Equus quagga", "zebra"],
    ("sound", "accordion"): ["accordion"],
    ("sound", "applause"): ["applause audience", "audience clapping", "concert audience"],
    ("sound", "bird"): ["Erithacus rubecula", "Turdus merula", "songbird singing"],
    ("sound", "car"): ["car automobile", "car"],
    ("sound", "cat"): ["domestic cat meowing", "domestic cat Felis catus", "cat"],
    ("sound", "chicken"): ["rooster crowing", "rooster Gallus gallus domesticus", "hen"],
    ("sound", "cow"): ["cow Bos taurus", "cow mooing"],
    ("sound", "dog"): ["dog barking", "dog Canis lupus familiaris"],
    ("sound", "drums"): ["drum kit", "drums"],
    ("sound", "duck"): ["Anas platyrhynchos", "duck"],
    ("sound", "elephant"): ["Loxodonta africana", "Elephas maximus"],
    ("sound", "flute"): ["transverse flute", "flute"],
    ("sound", "guitar"): ["acoustic guitar", "guitar"],
    ("sound", "harp"): ["concert harp", "harp"],
    ("sound", "lion"): ["Panthera leo roaring", "Panthera leo"],
    ("sound", "piano"): ["grand piano", "piano"],
    ("sound", "pig"): ["domestic pig Sus scrofa domesticus", "pig"],
    ("sound", "river"): ["river stream", "mountain river", "river"],
    ("sound", "sheep"): ["sheep Ovis aries", "sheep"],
    ("sound", "snake"): ["snake Serpentes", "rattlesnake", "snake"],
    ("sound", "storm"): ["lightning thunderstorm", "lightning"],
    ("sound", "triangle"): ["triangle musical instrument", "triangle percussion"],
    ("sound", "trumpet"): ["trumpet musical instrument", "trumpet"],
    ("sound", "violin"): ["violin"],
}

LICENCE_OK = re.compile(r"^(CC0( 1\.0)?|Public domain|PD.*|CC BY [0-9.]+|CC BY-SA 4\.0)$", re.I)


def api(params):
    params = dict(params, format="json", formatversion="2")
    url = API + "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=60) as r:
                return json.load(r)
        except Exception as e:  # network hiccup or rate limit
            time.sleep(3 * (attempt + 1))
    raise RuntimeError("API failed: " + url)


def download(url, path):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=120) as r, open(path, "wb") as f:
                f.write(r.read())
            return
        except Exception:
            time.sleep(3 * (attempt + 1))
    raise RuntimeError("Download failed: " + url)


def clean(text):
    return html.unescape(re.sub(r"<[^>]+>", "", text or "")).strip()


def candidates(term, quality):
    query = term + (" incategory:Quality_images" if quality else "") + " filetype:bitmap"
    titles = [r["title"] for r in api({"action": "query", "list": "search", "srnamespace": 6, "srsearch": query,
                                       "srlimit": 30}).get("query", {}).get("search", [])]
    if not titles:
        return []
    pages = api({"action": "query", "titles": "|".join(titles[:30]), "prop": "imageinfo",
                 "iiprop": "url|size|mime|extmetadata", "iiurlwidth": 1280,
                 "iiextmetadatafilter": "LicenseShortName|Artist|LicenseUrl"})["query"]["pages"]
    by_title = {p["title"]: p for p in pages}
    result = []
    for t in titles:
        p = by_title.get(t)
        if not p or "imageinfo" not in p:
            continue
        ii = p["imageinfo"][0]
        meta = ii.get("extmetadata", {})
        licence = clean(meta.get("LicenseShortName", {}).get("value"))
        w, h = ii.get("width", 0), ii.get("height", 0)
        if ii.get("mime") not in ("image/jpeg", "image/png") or not LICENCE_OK.match(licence):
            continue
        if min(w, h) < 700 or not (0.7 <= w / h <= 2.2):
            continue
        result.append({"title": t, "licence": licence, "licence_url": clean(meta.get("LicenseUrl", {}).get("value")),
                       "author": clean(meta.get("Artist", {}).get("value")), "page": ii["descriptionurl"],
                       "thumb": ii.get("thumburl") or ii["url"], "width": w, "height": h})
    return result


for (game, folder), terms in SUBJECTS.items():
    key = f"{game}-{folder}"
    if ONLY and key not in ONLY:
        continue
    target = os.path.join(OUT, key)
    if os.path.exists(os.path.join(target, "meta.json")):
        continue
    os.makedirs(target, exist_ok=True)
    found, seen, authors = [], set(), {}
    for quality in (True, False):
        for term in terms:
            for c in candidates(term, quality):
                # At most 2 photos of an author, for varied pictures
                if c["title"] not in seen and authors.get(c["author"], 0) < 2:
                    seen.add(c["title"])
                    authors[c["author"]] = authors.get(c["author"], 0) + 1
                    found.append(c)
            if len(found) >= PER_SUBJECT:
                break
        if len(found) >= PER_SUBJECT:
            break
    found = found[:PER_SUBJECT]
    for i, c in enumerate(found):
        c["file"] = f"c{i + 1}.jpg"
        download(c["thumb"], os.path.join(target, c["file"]))
        time.sleep(0.3)
    json.dump(found, open(os.path.join(target, "meta.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(key, len(found), flush=True)
