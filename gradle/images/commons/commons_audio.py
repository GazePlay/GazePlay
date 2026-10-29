"""Lists candidate sounds on Wikimedia Commons (licences compatible with the GPL 3), and downloads the chosen ones."""
import html
import json
import re
import sys
import time
import urllib.parse
import urllib.request

API = "https://commons.wikimedia.org/w/api.php"
UA = "GazePlayLearning-image-review/1.0 (https://github.com/GazePlay/GazePlay)"
LICENCE_OK = re.compile(r"^(CC0( 1\.0)?|Public domain|PD.*|CC BY [0-9.]+|CC BY-SA 4\.0)$", re.I)

TERMS = {
    "accordion": ["accordion"], "drums": ["drum kit", "drums beat", "drum solo"], "elephant": ["elephant trumpeting", "elephant"],
    "flute": ["flute solo", "flute"], "harp": ["harp"], "piano": ["piano solo", "piano"],
    "sheep": ["sheep bleating", "sheep"], "snake": ["snake hiss", "rattlesnake", "hissing"],
    "triangle": ["triangle instrument", "triangle"], "trumpet": ["trumpet solo", "trumpet"],
    "violin": ["violin solo", "violin"],
}


def api(params):
    url = API + "?" + urllib.parse.urlencode(dict(params, format="json", formatversion="2"))
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def clean(text):
    return html.unescape(re.sub(r"<[^>]+>", "", text or "")).strip()


def candidates(term):
    titles = [r["title"] for r in api({"action": "query", "list": "search", "srnamespace": 6,
                                       "srsearch": term + " filetype:audio", "srlimit": 25})["query"]["search"]]
    if not titles:
        return []
    pages = api({"action": "query", "titles": "|".join(titles), "prop": "imageinfo",
                 "iiprop": "url|size|mime|extmetadata",
                 "iiextmetadatafilter": "LicenseShortName|Artist|LicenseUrl"})["query"]["pages"]
    out = []
    for p in pages:
        if "imageinfo" not in p:
            continue
        ii = p["imageinfo"][0]
        meta = ii.get("extmetadata", {})
        licence = clean(meta.get("LicenseShortName", {}).get("value"))
        if not LICENCE_OK.match(licence):
            continue
        out.append({"title": p["title"], "licence": licence, "licence_url": clean(meta.get("LicenseUrl", {}).get("value")),
                    "author": clean(meta.get("Artist", {}).get("value")), "page": ii["descriptionurl"],
                    "url": ii["url"], "duration": round(ii.get("duration") or 0, 1), "mime": ii.get("mime")})
    return out


if sys.argv[1] == "list":
    result = {}
    for name, terms in TERMS.items():
        seen, found = set(), []
        for term in terms:
            for c in candidates(term):
                if c["title"] not in seen and 0 < c["duration"] <= 60:
                    seen.add(c["title"])
                    found.append(c)
            time.sleep(0.3)
        result[name] = found[:12]
    json.dump(result, open(sys.argv[2], "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    for name, found in result.items():
        for i, c in enumerate(found):
            print(f"{name} {i} | {c['duration']}s | {c['licence']} | {c['title'][5:80]} | {c['author'][:30]}")
else:  # download <candidates.json> <name=index>... <out folder>
    data = json.load(open(sys.argv[2], encoding="utf-8"))
    chosen = {}
    for arg in sys.argv[3:-1]:
        name, index = arg.split("=")
        c = data[name][int(index)]
        ext = c["url"].rsplit(".", 1)[1].lower()
        path = f"{sys.argv[-1]}/{name}.{ext}"
        req = urllib.request.Request(c["url"], headers={"User-Agent": UA})
        with urllib.request.urlopen(req, timeout=120) as r, open(path, "wb") as f:
            f.write(r.read())
        c["file"] = path
        chosen[name] = c
        time.sleep(0.5)
    json.dump(chosen, open(sys.argv[-1] + "/chosen.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print("downloaded", list(chosen))
