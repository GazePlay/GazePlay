# Inventaire des sons de GazePlay Learning (18/11/2026, mis à jour le 20/11/2026)

Demandé par le mainteneur (tâches de fin, après l'inventaire des images, [INVENTAIRE_IMAGES.md](INVENTAIRE_IMAGES.md)) :
savoir quels sons livrés sont libres, pour que GazePlay Learning respecte sa licence (GPL-3.0-or-later, accepte toute
ressource compatible). Ce document ne remplace aucun son : il dit où sont les risques et ce qu'il faudrait faire. Liste
complète, son par son : [`gradle/images/inventaire-sons.csv`](gradle/images/inventaire-sons.csv), refaite par
`python gradle/images/inventaire_sons.py` (depuis la racine du dépôt ; `--survey` montre les métadonnées par dossier).

## 1. Méthode

Pour chacun des **1 449 sons livrés** (fichiers des dossiers `src/main` suivis par git : 1 359 MP3, 90 WAV ; aucun
autre format) :

1. **Origine** : le premier commit du fichier, en suivant ses déplacements (un passage sur l'historique ; pour les
   ressources déplacées par milliers en décembre 2019, un `git log --follow` fichier par fichier).
2. **Métadonnées** lues dans le fichier : étiquettes ID3 (titre, artiste, commentaire, copyright, encodeur), encodeur
   écrit dans les trames MP3 (LAME, `Lavf` de FFmpeg), bloc INFO d'un WAV.
3. **Sources déjà écrites par le projet** : les scripts `gradle/images/make_*.py` qui fabriquent des sons, les
   fichiers `CREDITS.txt` livrés avec les jeux, `gradle/images/README.txt`.
4. **Historique de GitHub** : les tickets et pull requests qui ont ajouté des voix (#1116, #1121, #1415, #1516,
   #1603, #1626).

**Limites** : aucun son n'a été écouté. Un son sans métadonnées ni trace dans l'historique reste « source inconnue »,
même s'il est libre. Une voix de synthèse ne se reconnaît pas à ses métadonnées : seul l'historique le dit.

## 2. Résultat

| Catégorie | Nombre | Détail |
|---|---|---|
| **Libre, source connue** | 133 | 86 sons fabriqués par les scripts du projet (`make_*_sound(s).py`, GPL-3.0-or-later, Spot The Differences compris), Sons de la vie (21, Wikimedia Commons et La Sonothèque, `soundsoflife/CREDITS.txt`), Quel est ce son ? (24, Wikimedia Commons et La Sonothèque CC0 1.0, `whereIsTheSound/CREDITS.txt`), musique du Tir (OpenGameArt, CC0), « plop » de Mélanger les couleurs (copie de La Fabrique à gâteaux) |
| **Source connue, conditions à vérifier** | 278 | Voix de synthèse : **276 d'Amazon Polly** (2020, script du projet `GazePlay/gazeplay-voice-synthesis`, ticket #1116, PR #1121 : Où est la lettre ?, Où est le chiffre ?, une partie d'Où est la couleur ? et d'Où est l'animal ? dynamique, en anglais, français et chinois) et **2 de ttsmp3.com** (2021, PR #1516, drapeaux) |
| **Non libre, probable** | 1 | `race/sounds/frog.WAV` (Course des grenouilles) : étiquette « © Aludra Software 1998 », Sound Forge 4.0 |
| **Source inconnue** | 1 037 | 1 032 voix (section 3) et 5 sons communs (section 4) |
| **Total** | 1 449 | 9 % libres, 19 % voix de synthèse aux conditions à vérifier, 72 % de source inconnue (presque toutes des voix) |

**Mise à jour du 18/11/2026, après le retrait des langues** (décision du mainteneur : français et anglais seulement,
les autres langues reviendront une fois ces deux-là stables) : les 366 voix chinoises sont retirées. **1 083 sons
livrés** : 133 libres, 150 voix de synthèse aux conditions à vérifier (148 d'Amazon Polly, 2 de ttsmp3.com), 1 non libre
probable, 799 de source inconnue (794 voix françaises et anglaises, 5 sons communs). Les tableaux ci-dessous gardent
l'état d'avant le retrait.

**Mise à jour du 18/11/2026 (recommandation 1 faite)** : les 3 sons lus par aucun code (`bip.wav`, `explosion.mp3`,
`seeTwoImages.wav`) sont retirés. **1 080 sons livrés**, dont 796 de source inconnue (794 voix, les applaudissements
de Bravo et la musique par défaut des menus).

**Mise à jour du 18/11/2026 (recommandation 2 faite, décisions du mainteneur)** : la musique par défaut des menus est
retrouvée et gardée : « copycat » de syncopika, OpenGameArt (https://opengameart.org/content/copycat), CC BY 3.0,
créditée dans la licence affichée ; les applaudissements de Bravo sont remplacés par ceux de « Quel est ce son ? »
(La Sonothèque, CC0 1.0, `data/common/CREDITS.txt`) ; la grenouille de Course des grenouilles (Aludra Software 1998)
par le coassement des « Sons de la vie » (Wikimedia Commons, CC BY-SA 4.0, `data/race/CREDITS.txt`,
`data/race/sounds/frog.mp3`). **1 080 sons livrés : 136 libres, 150 voix de synthèse aux conditions à vérifier, 794
voix de source inconnue, aucun son non libre.** Reste la recommandation 3 (les voix).

**Mise à jour du 20/11/2026 (recommandations 3 et 4 faites, voie B, décisions du mainteneur)** : les **944 voix**
(Où est… et Trouve l'intrus 736, Identifier les lettres 208) sont **régénérées** par `gradle/images/make_voices.py`
avec Piper 1.8.0 (GPL-3.0-or-later, hors ligne, non livré), avec le texte affiché par le jeu, en 4 voix choisies par
le mainteneur après lecture de leurs fiches : `fr_FR-siwis-medium` (français, femme, CC BY 4.0, crédit obligatoire),
`fr_FR-tom-medium` (français, homme, AGPL 3.0 ; réserve : la voix de départ du modèle n'est pas indiquée),
`en_US-kristin-medium` et `en_US-norman-medium` (anglais, femme et homme, LibriVox, domaine public). Le fichier
`gradle/images/voix.csv` donne la voix et le texte de chaque fichier ; `inventaire_sons.py` le lit. Crédits :
`gazeplay-games/src/main/resources/data/CREDITS-VOICES.txt` et paragraphe « Voix des jeux » de la licence affichée
(`gradle/templates/licence.txt`). Le même jour : textes anglais rendus plus naturels (couleurs, chiffres, lettres,
quelques animaux et formes), tutoiement (« Trouve l'intrus », aussi pour le nom du jeu, et « Trouve les
différences »), et `whereIsTheShape/sounds/eng/nonagon.w.eg.mp3` renommé en `.eng` (le jeu ne le trouvait pas).
Synthèse embarquée à la place des fichiers : écartée pour la 2.0, issue #1742. **1 080 sons livrés, tous libres et
de source connue.** Les sections 3 à 6 gardent l'état d'avant.

**Retours de la testeuse, point 80 (27/11/2026)** : 5 sons de « J'ai un Biboule ! » (`data/pet/sounds` : caresse,
bouchée, douche, jouet, jauge qui monte) faits par `make_pet_sounds.py`, sous la licence du projet ; **1 085 sons
livrés, tous libres et de source connue.**

## 3. Les voix (1 310 fichiers, 90 % des sons livrés)

Les questions lues à voix haute des jeux « Où est… », de Trouve l'intrus et d'Identifier les lettres, en plusieurs
langues, une voix d'homme (`.m.`) et une de femme (`.w.`).

| Jeu (dossier) | Voix | Ajoutées | Source |
|---|---|---|---|
| Où est la lettre ? (`whereIsTheLetter`) | 156 | 2020, Tohaker (commit de milesbardon) | Amazon Polly (#1116, #1121) |
| Où est le chiffre ? (`whereIsTheNumber`) | 66 | 2020, idem | Amazon Polly |
| Où est l'animal ? dynamique (`whereIsTheAnimalDynamic`) | 32 + 64 | 2020 (chinois) / 2017, schwab | Amazon Polly / **inconnue** |
| Où est la couleur ? (`whereIsTheColor`) | 22 + 44 + 30 | 2020 / 2017, schwab / 2022, Sakraft | Amazon Polly / **inconnue** / **inconnue** |
| Où est l'animal ? (`whereIsTheAnimal`) | 300 | 2022, Sakraft (PR #1626) | **inconnue** |
| Où est le drapeau ? (`whereIsTheFlag`) | 2 + 262 | 2021, verafili / 2022, Sakraft (PR #1626) | ttsmp3.com (#1516) / **inconnue** |
| Où est la forme ? (`whereIsTheShape`) | 120 | 2022, Sakraft (ticket #1603) | **inconnue** |
| Trouve l'intrus (`findTheOddOneOut`) | 4 | 2017, schwab | **inconnue** |
| Identifier les lettres (`literacy`) | 208 | 2019, Wafaa | **inconnue** |

**Faits**
- Toutes ces voix ne portent que la trace d'un réencodage par FFmpeg (`Lavf57`, `Lavf58`) : ni auteur ni outil.
- Ticket #1116 (2020) : il est proposé de générer les questions avec Amazon Polly (« sous l'offre gratuite ») ; un
  contributeur écrit un script, publié dans l'organisation (`GazePlay/gazeplay-voice-synthesis`) ; la PR #1121 ajoute
  ainsi « toutes les voix anglaises, françaises et chinoises des jeux Où est… ». Un commentaire du même ticket dit
  qu'une partie d'Où est l'animal ? avait déjà été faite ainsi, « mais c'est très long ».
- PR #1516 (2021) : les voix des drapeaux, en anglais et en français, viennent de https://ttsmp3.com/.
- Les PR #1626 et le ticket #1603 (2022) ajoutent des centaines de voix sans dire comment elles sont faites.

**Hypothèses**
- Les voix de 2022 (Sakraft) sont des voix de synthèse : même découpage (homme / femme, une phrase par fichier), mêmes
  langues, et le script du projet existait. Rien ne le prouve.
- Les voix anglaises et françaises des drapeaux de 2022 seraient en partie celles de ttsmp3.com de 2021, renommées
  (`find-flag` devenu `whereIsTheFlag`, majuscule aux noms) : git ne les relie pas, le contenu a pu changer.
- Les voix de 2017 (schwab) et de 2019 (Wafaa) peuvent être enregistrées ou synthétisées : aucune trace.

**Risque** : les voix de synthèse dépendent des conditions d'utilisation du service (Amazon Polly, ttsmp3.com) ; je ne
les ai pas vérifiées et ne les cite pas de mémoire. Une voix enregistrée sans autorisation écrite de la personne
enregistrée pose une question de droit à la voix, en plus du droit d'auteur.

## 4. Les autres sons de source inconnue ou non libres

| Son | Utilisé ? | Indice | Risque |
|---|---|---|---|
| `common/sounds/applause.mp3` | Oui : écran Bravo de tous les jeux (`Bravo.java`) | Étiquette « www.dvdvideosoft.com » (logiciel qui convertit des vidéos, souvent de YouTube, en MP3), 2014, ajouté en 2017 | **Élevé** : probablement extrait d'une vidéo |
| `home/sounds/songidea(copycat)_0.mp3` | Oui : musique par défaut des menus (`Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC`) | Ajouté en 2018 ; le nom ressemble à celui d'un morceau d'un site de ressources pour jeux | Moyen : source à retrouver |
| `race/sounds/frog.WAV` | Oui : Course des grenouilles (`Race.java`) | « © Aludra Software 1998 », ingénieur John Wong, Sound Forge 4.0 | **Élevé** : son d'une collection commerciale probable |
| `common/sounds/bip.wav` | **Non** (lu par aucun code) | « Switch Plus (c) NCH Software » (convertisseur), ajouté en 2022 (jeux BERA) | À retirer |
| `common/sounds/explosion.mp3` | **Non** | Ajouté en 2019 (« explosion animation ») | À retirer |
| `common/sounds/seeTwoImages.wav` | **Non** | Ajouté en 2022 (jeux BERA) | À retirer |

## 5. Recommandations

1. **Retirer les 3 sons lus par aucun code** (`bip.wav`, `explosion.mp3`, `seeTwoImages.wav`) : aucun effet sur
   l'application.
2. **Remplacer les 3 sons utilisés à risque** : les applaudissements de l'écran Bravo, la grenouille de Course des
   grenouilles (sons fabriqués par script, comme les autres jeux revus, ou sons crédités de Wikimedia Commons / La
   Sonothèque, comme Quel est ce son ?), et la musique par défaut des menus si sa source ne se retrouve pas (la musique
   du Tir, CC0, est une solution déjà dans le projet).
3. **Voix** (1 310 fichiers) : le plus gros chantier. Deux voies :
   - **A** : vérifier les conditions d'Amazon Polly et de ttsmp3.com pour les 278 voix de source connue, et chercher
     l'origine des 1 032 autres (demander aux contributeurs de 2017, 2019 et 2022, si on peut les joindre) ;
   - **B** : **régénérer toutes les voix** avec un outil de synthèse libre dont les voix sont sous licence compatible
     (à choisir et vérifier : licence du logiciel ET des voix), par un script gardé dans le dépôt, comme les sons des
     jeux revus. Plus sûr et reproductible ; demande d'écouter le résultat en chaque langue.
4. **Ajouter un `CREDITS.txt`** pour les voix, quelle que soit la voie choisie.

## 6. Questions pour le mainteneur

1. Retire-t-on tout de suite les 3 sons inutilisés ?
2. Remplace-t-on les applaudissements, la grenouille et la musique par défaut (proposition à faire jeu par jeu, comme
   pendant la revue) ?
3. Pour les voix : voie A (vérifier et retrouver les sources) ou voie B (tout régénérer avec un outil libre) ?
4. Les voix d'Identifier les lettres (208, 2019) ont été gardées « de source inconnue » à la revue du jeu (décision du
   mainteneur) : la décision tient-elle pour les autres voix ?
