"""Second search for the subjects lacking good candidates: same script, other search terms."""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
TERMS = {
    ("animal", "beaver"): ["beaver on land", "Castor canadensis eating", "beaver standing", "Castor fiber on bank"],
    ("animal", "chicken"): ["brown hen farmyard", "free range hen", "hen walking", "chicken breed hen"],
    ("animal", "dog"): ["dog standing on grass", "Golden Retriever", "Border Collie", "Labrador Retriever standing"],
    ("animal", "hippopotamus"): ["hippopotamus walking", "hippopotamus on land", "Hippopotamus amphibius grazing"],
    ("animal", "octopus"): ["Octopus cyanea", "octopus aquarium", "octopus swimming", "Enteroctopus dofleini"],
}
extra = sys.argv[2:]  # more "game-folder=term1;term2" pairs
for item in extra:
    key, terms = item.split("=", 1)
    game, folder = key.split("-", 1)
    TERMS[(game, folder)] = terms.split(";")

source = open(os.path.join(HERE, "commons_fetch.py"), encoding="utf-8").read()
start = source.index("SUBJECTS = {")
end = source.index("}\n", start) + 2
source = source[:start] + "SUBJECTS = " + repr(TERMS) + "\n" + source[end:]
sys.argv = [sys.argv[0], sys.argv[1]]
exec(compile(source, "commons_fetch.py", "exec"))
