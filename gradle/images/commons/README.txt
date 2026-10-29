Choice of the photos and sounds of Where Is The Animal and Where Is The Sound on Wikimedia Commons (October 2026,
see REVUE_DES_JEUX.md). Kept to trace the choices; the results are in the games and their CREDITS.txt files.

1. commons_fetch.py <folder>: 8 candidate photos per subject (Quality images first, licences CC0, public domain,
   CC BY, CC BY-SA 4.0; at most 2 photos of an author), with their author and licence in meta.json.
   commons_refetch.py <folder2> ["game-folder=term1;term2"...]: the same with other search terms.
2. contact_sheets.py <folder> <sheets>: numbered sheets of the candidates, looked at one by one.
3. choices.txt: the candidates kept per subject ("2" = candidate 2 of the first folder, "b3" = candidate 3 of the
   second folder, "c"/"d" the third/fourth: commons_refetch3.py, then
   commons_refetch.py for the crocodile); install_photos.py <folder> <folder2> choices.txt <data>: resizes and installs them, writes
   CREDITS.txt.
4. commons_audio.py list|download: the candidate sounds, then the chosen ones (accordion=0 drums=4 elephant=0 flute=2
   harp=2 piano=3 sheep=4 snake=0 triangle=0 trumpet=4 violin=0), converted with ffmpeg:
   silenceremove (start, -45 dB), atrim 0-8 s, afade out 0.6 s, loudnorm (I=-16, TP=-1.5, LRA=11), mono, 44.1 kHz,
   MP3 128 kb/s.
