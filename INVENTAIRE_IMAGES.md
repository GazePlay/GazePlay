# Inventaire des images de GazePlay Learning (28/10/2026)

Demandé par le mainteneur : savoir quelles images du dépôt sont libres, pour que GazePlay Learning respecte sa licence
(GPL-3.0-or-later, accepte toute ressource compatible). Ce document ne remplace aucune image : il dit où sont les
risques et ce qu'il faudrait faire. Liste complète, image par image : [`gradle/images/inventaire-images.csv`](gradle/images/inventaire-images.csv).

## 1. Méthode

Pour chacune des 1 256 images suivies par git (PNG, JPEG, GIF, SVG ; 1 186 livrées dans les distributions, `src/main`) :

1. **Origine** : le premier commit de l'image, en suivant ses déplacements (`git log --follow`), avec l'auteur, la date
   et le message.
2. **Métadonnées** lues dans le fichier : auteur, droits, licence, banque d'images (iStock, Getty, Freepik, Shutterstock,
   Pixabay, Openclipart…). Les copyrights « Hewlett-Packard 1998 » et « Adobe 1999 », très fréquents, sont ceux des
   profils de couleur sRGB et Adobe RGB, pas des images : ignorés.
3. **Nom du fichier** : `nom-1234567_1280.jpg` est le nom donné par Pixabay au téléchargement ; `800px-Flag_of_…` celui
   de Wikimedia Commons.
4. **Code et textes** : crédits écrits dans le code (icônes Flaticon) ou dans les fichiers de licence.
5. **Regard** : une planche de vignettes par jeu, regardée (photos, dessins, personnages connus).

**Limites** (à garder en tête) : une image sans métadonnées ni nom parlant reste « source inconnue », même si elle est
libre ; le contenu de l'image ne dit rien de sa licence. Les sons et musiques n'ont pas été inventoriés.

## 2. Résultat pour les images livrées (1 186 le 28/10/2026, 1 039 au recompte du 13/11/2026)

**Recompte du 13/11/2026** : images présentes dans les dossiers `src/main` (suivies par git ou nouvelles, La Fabrique à
gâteaux comprise, revue faite mais pas encore commitée : 24 images retirées), avec la catégorie de chaque image donnée
par `inventaire-images.csv`, tenu à jour à chaque jeu revu. 13 icônes Material Symbols (`data/icons`, Apache-2.0),
ajoutées par la refonte des menus, manquaient dans le CSV : comptées libres. Le nombre d'images livrées baisse parce que
beaucoup de jeux revus dessinent maintenant leurs images eux-mêmes.

| Catégorie | Nombre au 13/11 | Détail |
|---|---|---|
| **Libre, source connue** | 670 (110 le 28/10, 452 le 30/10) | Images de Frogs Race et Horses, décors de Sounds Of Life, scènes de Spot The Differences et pièces de Pet (dessinés pour le projet, 30/10/2026), Biboule de Pet (dessin du projet), images par défaut (revue de Memory), animaux de Sounds Of Life et objets de Spot The Differences et de Pet (OpenMoji, CC BY-SA 4.0, 30/10/2026), coloriages de Coloring (OpenMoji, CC BY-SA 4.0, 29/10/2026), photos de Where Is The Animal et Where Is The Sound (Wikimedia Commons, 29/10/2026, voir `CREDITS.txt` de chaque jeu), triangle dessiné pour le projet, icônes des menus (Material Symbols, Apache-2.0), animaux et explosion (OpenMoji, CC BY-SA 4.0), Magic Potions (dessinées pour le projet), logo du projet, icônes Flaticon (CC BY 3.0), ciel de Room (ESA/Hubble, CC BY 4.0), tableau de Math101 (Openclipart, domaine public), harpe (Metropolitan Museum, CC0) ; depuis le 30/10, les images des jeux revus ensuite (dessinées par script, OpenMoji, Wikimedia Commons : fiches de `REVUE_DES_JEUX.md` et `gradle/images/README.txt`) |
| **Libre probable** | 153 | 128 photos Pixabay publiées avant 2019 (Find the Odd One Out, Where Is The Animal Dynamic), alors sous CC0 ; 25 drapeaux de Wikimedia Commons (domaine public en général) |
| **Non libre, certain** | 0 (7 le 28/10) | Le coloriage Hello Kitty (Colors) est retiré le 29/10/2026 ; les 5 photos iStock / Getty (Where Is The Sound) et la photo CC BY-NC-SA (castor) ont été remplacées le 29/10/2026 |
| **Non libre, probable** | 0 (1 le 28/10) | `race/images/Racer.png` (clipart de www.cutecolors.com, site commercial) : lu par aucun code, retiré le 30/10/2026 avec la revue de Frogs Race, dont la grenouille et le décor (source inconnue) sont redessinés |
| **Source inconnue** | 216 (915 le 28/10, 586 le 30/10) | 130 dessins simples des jeux « Où est… » (44 drapeaux, 34 formes, 26 lettres, 16 couleurs, 10 chiffres) ; 34 images communes (`common/images` 14, `common/Thumbnails` 10, `common/numbers` 10) ; 52 dans les jeux pas encore revus (Suivre le chemin, La pièce, Piano, Bouteille, Mélanger les couleurs, Échelle) et les Biboules (Biboule Jump, Horses, Frogs Race). Détail du 28/10 en section 3 |
| **Total** | 1 039 | 64 % libres, 15 % libres probables, 0 non libre, 21 % de source inconnue (13/11) |

**Depuis le recompte** : revue de Mélanger les couleurs (13/11/2026, plus tard) : le parc et le verre retirés, la
vignette refaite (capture du jeu) ; **1 037 images livrées, 671 libres, 153 libres probables, 213 de source inconnue**
(50 dans les jeux pas encore revus et les Biboules, 33 images communes).

**À vérifier** : les vignettes `frogsrace.png`, `horses.png` et `memory.png` de `common/Thumbnails` sont encore classées
« source inconnue » dans le CSV, alors que ces jeux ont été revus ; si leurs vignettes ont été refaites, leur ligne est à
corriger. Les sons ne sont toujours pas inventoriés.

**Hypothèse** (Pixabay) : Pixabay a quitté la licence CC0 en janvier 2019. Une image publiée avant reste sous CC0 ; la
date est estimée par le numéro de l'image (inférieur à 3 900 000 environ). Aucune image Pixabay plus récente n'a été
trouvée.

## 3. Les 915 images de source inconnue, par niveau de risque

État du 28/10/2026, barré au fil des remplacements ; il en reste 216 au 13/11/2026 (section 2).

### 3.1 Risque élevé : photos et illustrations venues d'ailleurs (372)

Images qui ressemblent à des photos ou des illustrations professionnelles, sans source ni licence.

| Jeu ou dossier | Images | Ce que c'est | Indice |
|---|---|---|---|
| ~~Where Is The Animal~~ | ~~149~~ | Photos d'animaux | **Remplacées le 29/10/2026** (Wikimedia Commons) |
| ~~Where Is The Sound~~ | ~~59~~ | Photos d'animaux et d'instruments | **Remplacées le 29/10/2026** (Wikimedia Commons) |
| ~~Sounds of Life~~ | ~~32~~ | Cliparts d'animaux, décors | **Remplacées le 30/10/2026** (décors dessinés pour le projet, animaux OpenMoji ; sons de Wikimedia Commons, voir `data/soundsoflife/CREDITS.txt`) |
| ~~Pet~~ | ~~21~~ | Photos (pièces, objets), pictogrammes | **Remplacées le 30/10/2026** (pièces dessinées, objets OpenMoji ; la Biboule, dessin du projet, gardée) |
| ~~Flower of Numbers~~ | ~~21~~ | Billets et pièces en euros, mains, dés | **Remplacées le 2/11/2026** (mains, dés, pièces et billets génériques dessinés par script ; fleur et auras dessinées par le jeu) |
| ~~Colors~~ | ~~19~~ | Coloriages (pomme, dauphin, papillon…) | **Remplacés le 29/10/2026** (OpenMoji) ; flèches et palette retirées |
| `common/images` (core) | 17 | Icônes diverses (smiley, dossier, maison…) | — |
| ~~Spot the Differences~~ | ~~15~~ | Scènes dessinées | **Remplacées le 30/10/2026** (8 scènes dessinées par script, objets OpenMoji) |
| Images par défaut (core) | 9 | Animaux dessinés (chouette, crabe, écureuil…) | Commit de 2021 « adding free of right Free Fundation images » : dites libres, source non indiquée |
| ~~Opinions~~ | ~~9~~ | Photos d'aliments, icônes brillantes | **Retirées le 2/11/2026** : le jeu ne montre plus que les images de l'utilisateur ; boutons en icônes Material Symbols |
| Room | 9 | Murs, flèches | — |
| ~~Sprint Finish~~ | ~~6~~ | Chat et chien dessinés, boutons | **Remplacées le 2/11/2026** : chat, chien (recoloré) et poisson OpenMoji ; prairie et gamelle dessinées par script ; haies, barrières, boutons et rondins dessinés par le jeu |
| ~~Bubble~~ | ~~3~~ | Fonds sous-marins | **Remplacés le 2/11/2026** : 3 paysages sous la mer dessinés par script ; animaux des bulles OpenMoji ; vignettes refaites (une seule carte) |
| ~~Egg~~ | ~~3~~ | Photos d'œufs | **Remplacées le 2/11/2026** : œuf dessiné par le jeu, prairie et nid dessinés par script, animaux OpenMoji |

### 3.2 Dessins des jeux, auteur probable : l'étudiant qui a créé le jeu (377)

Style simple, souvent fait pour le jeu (Biboule, mascotte de GazePlay, et ses variantes de couleur), ajoutés avec le
jeu. **Hypothèse** à confirmer par leurs auteurs (noms dans l'historique git et le CSV).

| Jeux | Images |
|---|---|
| Vignettes des jeux (`common/Thumbnails`) : captures d'écran des jeux (9 de la famille Where Is It et Coloring refaites le 29/10/2026, comptées comme libres) | 58 |
| ~~Train Switches~~ | ~~43~~ **Fait le 10/11/2026** (« Aiguillages ») : les 43 images retirées (les locomotives ressemblaient à l'emoji EmojiOne, les gares à une image de banque d'images) ; trains, gares, rails, aiguillages et tunnel dessinés par le jeu ; vignette faite d'une capture du jeu. Les 5 sons de source inconnue remplacés par 4 sons faits par script (`make_trainswitches_sounds.py`) |
| Sliding Puzzle (dont la Joconde, domaine public) | 36 |
| ~~Cake~~ | ~~24~~ **Fait le 12/11/2026** (« La Fabrique à gâteaux ») : les 24 images retirées (étages, nappages, bonbons, décorations, aérographe, pince, fond, icônes des menus ; faites dans Photoshop en 2018, auteur non prouvé) ; tout est dessiné par le jeu (les Biboules ailées redessinées) ; les 3 sons de source inconnue remplacés par 2 sons faits par script (`make_cakes_sounds.py`) ; vignette faite d’une capture |
| ~~Tower Defense~~ | ~~23~~ **Fait le 3/11/2026** : les 23 images retirées (dont les icônes qui ressemblaient à des icônes Flaticon et le GIF du blizzard) ; terrain, château, tours, monstres, missile, explosion et icônes dessinés par script (`make_towerdefense_images.py`), neige du blizzard dessinée par le jeu ; vignette composée des images du jeu. Les 3 sons de source inconnue (`craft.mp3`, `craftdouble.mp3`, `piou.mp3`) remplacés par 4 sons faits par script (`make_towerdefense_sounds.py`) |
| ~~Shooter~~ | ~~20~~ **Fait le 2/11/2026** : décors, robots, cage, soucoupe, éclairs, baguette et canon redessinés par script ; les 5 Biboules gardées (mascotte du laboratoire, dessinée par lui), puis **modernisées le 3/11/2026** par le même script (bouche et ailes d'origine ; originales dans `gradle/images/biboule-originale`) |
| Labyrinth | 19 |
| Follow | 13 |
| ~~Space~~ | ~~12~~ **Fait le 3/11/2026** : les 12 images retirées (vaisseaux, boss, extraterrestres, fond, lasers, bouton) ; fond de nuit douce, fusée, 5 soucoupes et boss dessinés par script (`make_space_images.py`) ; vignette composée des images du jeu |
| ~~Survive Against Robots~~ | ~~12~~ **Fait le 3/11/2026** : les 12 dessins retirés ; 3 décors (ciel, jardin, cour de l'école) et 4 bonus dessinés par script (`make_survive_images.py`) ; le joueur et les robots sont ceux du Tir ; vignette composée des images du jeu |
| Horses, Race | 11 chacun |
| ~~Divisor~~ | ~~10~~ **Fait le 12/11/2026** (Diviseur, une seule carte avec Lapins) : les 7 lapins et la prairie gardés comme **dessins du projet**, comme les Biboules (dessinés dans Photoshop de janvier à avril 2018, tous les documents insérés créés dans Photoshop du 27/1 au 24/3/2018, d'après leurs métadonnées ; commités par kuhlkrein) ; `coeur.png` et `explosion.png`, lus par aucun code, retirés ; vignette faite d'une capture, celle de Lapins retirée |
| Biboule Jump, dossier `common` des jeux | 9 chacun |
| ~~Rock Paper Scissors~~ | ~~7~~ **Fait le 10/11/2026** : les 7 images retirées (gants blancs de dessin animé à 4 doigts, proches du style d'un personnage commercial ; parc ; pouces) ; mains d'OpenMoji (CC BY-SA 4.0, `make_rockpaperscissors_images.py`), le reste dessiné par le jeu ; vignette faite d'une capture |
| Pianosight | 6 |
| Bottle, ~~Math101~~ | 5 chacun ; **Apprends les Maths fait le 10/11/2026** : ses 5 images de source inconnue retirées, avec le tableau d'Openclipart (tableau et cartes dessinés par le jeu, Biboule jaune du Tir), vignette faite d'une capture |
| Gaze Race, Rush Hour, GazePlay Eval Test (anciens jeux BERA) | 4 chacun |
| ~~Noughts and Crosses~~, ~~Order~~, Whack-a-Mole | 3 chacun ; **Morpion fait le 10/11/2026** : ses 3 images retirées (croix, rond et case dessinés par le jeu), vignette faite d'une capture ; **Ordre fait le 10/11/2026** : ses 3 images retirées (cibles dessinées par le jeu), vignette faite d'une capture |
| Colorblend, ~~Cream Pie~~, ~~Magic Cards~~, ~~Simon~~ | 2 chacun ; **Tartes à la crème fait le 12/11/2026** : la main et la tarte (`gateau.png`) de source inconnue remplacées par des dessins faits par script (`make_creampie_images.py`, `hand.png` et `pie.png`), crème dessinée par le jeu ; le son `missile.mp3` de source inconnue remplacé par 2 sons faits par le même script ; vignette faite d’une capture ; **Cartes magiques fait le 12/11/2026** : le dos des cartes (`red-card-game.png`) et `blue-card-game.png` (lu par aucun code), de source inconnue, et la croix `common/images/error.png` (Flaticon, CC BY 3.0, lue seulement par ce jeu) retirés ; cartes dessinées par le jeu, vignette faite d’une capture ; **Simon (« Répète après moi ») fait le 10/11/2026** : ses 2 images retirées (tout dessiné par le jeu), vignette faite d'une capture ; ses 7 notes de source inconnue remplacées par des sons faits par script |
| ~~Letters~~ | **Fait le 12/11/2026** : les 26 images de lettres (`common/letters`, lues par aucun code) retirées ; cartes et lettres dessinées par le jeu ; vignette faite d’une capture. **Les 208 voix restent de source inconnue** (format de certains services de synthèse vocale, hypothèse) : gardées pour l’instant (décision du mainteneur), à remplacer, issue #1741 |
| Cups, ~~Dice~~, ~~Dot to Dot~~, ~~Goose Game~~, Ladder | 1 chacun ; **Point par point fait le 12/11/2026** : `background.png` (blanc, lu par aucun code) retiré, 24 figures nommées faites par script (`make_dottodot_figures.py`), vignette faite d’une capture ; **Jeu de l’oie fait le 12/11/2026** : plateau redessiné par script sur les mêmes positions (`make_goose_images.py`), sons refaits (`make_goose_sounds.py`), Biboules du Tir, vignette faite d’une capture ; **Dés fait le 12/11/2026** : `roll.png` retiré (boutons dessinés par les jeux), texture des faces du dé (`common/images/dice.png`, commune avec les Petits chevaux et le Jeu de l’oie) et son du lancer redessinés par script (`make_dice_images.py`, `make_dice_sounds.py`), vignette composée par le même script |

### 3.3 Sans originalité ou domaine public (166)

Lettres et chiffres tapés (`common/letters`, `common/numbers`, Where Is The Letter, Where Is The Number), formes
géométriques (Where Is The Shape), couleurs unies (Where Is The Color), drapeaux nationaux (Where Is The Flag). Pas
assez d'originalité pour être protégés, ou domaine public (drapeaux) : **aucun risque réel**.

## 4. Déjà fait le 28/10/2026

- Images Freepik remplacées : animaux par défaut et leur version sans fond (OpenMoji), explosion (OpenMoji), images de
  Magic Potions (dessinées par script). Copie Freepik inutilisée de `gazeplay-commons/src/main/resources/images`
  (16 images, dont 9 dessins Freepik, anciens jeux BERA) supprimée.
- Attributions ajoutées à la licence affichée : OpenMoji, icônes Flaticon (CC BY 3.0), ciel de Room (ESA/Hubble,
  CC BY 4.0).

## 4 bis. Fait le 29/10/2026 (revue des jeux, famille Where Is It)

- Where Is The Animal (144 photos) et Where Is The Sound (64 photos et un triangle dessiné, la harpe du Met gardée) :
  photos de Wikimedia Commons sous CC0, domaine public, CC BY ou CC BY-SA 4.0, choisies une par une ; crédits dans
  `data/whereIsTheAnimal/CREDITS.txt` et `data/whereIsTheSound/CREDITS.txt`, et dans la licence affichée ; inventaire
  CSV mis à jour.
- Sons de Where Is The Sound inventoriés : 13 de La Sonothèque (CC0) gardés, 11 remplacés (Wikimedia Commons). Voix
  des questions de la famille : source à confirmer auprès de leurs auteurs (voir `REVUE_DES_JEUX.md`).

- Coloring : 17 coloriages (dont Hello Kitty) remplacés par 24 contours d'OpenMoji (CC BY-SA 4.0) ; 5 images de
  boutons retirées (icônes Material Symbols).

## 5. Recommandations

1. **Remplacer les 8 images non libres** (section 2) : les photos iStock et CC BY-NC par des photos CC0 (Pixabay
   d'avant 2019, Wikimedia Commons, Met Museum), le coloriage Hello Kitty par un autre dessin, `Racer.png` par une
   grenouille dessinée ou OpenMoji.
2. **Risque élevé (372)** : demander aux auteurs (historique git) d'où viennent ces images ; à défaut, les remplacer
   jeu par jeu, en commençant par Where Is The Animal et Where Is The Sound (photos : OpenMoji ou photos CC0).
3. **Dessins des jeux (377)** : faire confirmer par leurs auteurs qu'ils les ont faits ; sinon, même traitement.
4. **Pour la suite** : n'accepter une image qu'avec sa source et sa licence, notées dans `gradle/images/README.txt` et
   dans la licence affichée ; préférer CC0, CC BY, CC BY-SA, ou des dessins faits pour le projet.
5. **Sons et musiques** : même inventaire à faire.
