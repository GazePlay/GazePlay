"""Second search for the subjects lacking good candidates: same script, other search terms."""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
TERMS = {
    ("animal", "beaver"): ["Eurasian beaver", "North American beaver", "beaver swimming", "beaver lodge animal"],
    ("animal", "chicken"): ["hen chicken breed", "domestic hen", "chicken Gallus gallus domesticus female"],
    ("animal", "seahorse"): ["Hippocampus kuda", "Hippocampus guttulatus", "Hippocampus hippocampus"],
    ("sound", "accordion"): ["piano accordion instrument", "accordion museum", "button accordion"],
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
