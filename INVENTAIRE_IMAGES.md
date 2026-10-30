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

## 2. Résultat pour les 1 186 images livrées (mis à jour le 30/10/2026 : 1 195, d'après `inventaire-images.csv`)

| Catégorie | Nombre | Détail |
|---|---|---|
| **Libre, source connue** | 434 (110 le 28/10) | Images de Frogs Race et Horses, décors de Sounds Of Life et scènes de Spot The Differences (dessinés pour le projet, 30/10/2026), images par défaut (revue de Memory), animaux de Sounds Of Life et objets de Spot The Differences (OpenMoji, CC BY-SA 4.0, 30/10/2026), coloriages de Coloring (OpenMoji, CC BY-SA 4.0, 29/10/2026), photos de Where Is The Animal et Where Is The Sound (Wikimedia Commons, 29/10/2026, voir `CREDITS.txt` de chaque jeu), triangle dessiné pour le projet, icônes des menus (Material Symbols, Apache-2.0), animaux et explosion (OpenMoji, CC BY-SA 4.0), Magic Potions (dessinées pour le projet), logo du projet, icônes Flaticon (CC BY 3.0), ciel de Room (ESA/Hubble, CC BY 4.0), tableau de Math101 (Openclipart, domaine public), harpe (Metropolitan Museum, CC0) |
| **Libre probable** | 153 | 128 photos Pixabay publiées avant 2019 (Find the Odd One Out, Where Is The Animal Dynamic), alors sous CC0 ; 25 drapeaux de Wikimedia Commons (domaine public en général) |
| **Non libre, certain** | 0 (7 le 28/10) | Le coloriage Hello Kitty (Colors) est retiré le 29/10/2026 ; les 5 photos iStock / Getty (Where Is The Sound) et la photo CC BY-NC-SA (castor) ont été remplacées le 29/10/2026 |
| **Non libre, probable** | 0 (1 le 28/10) | `race/images/Racer.png` (clipart de www.cutecolors.com, site commercial) : lu par aucun code, retiré le 30/10/2026 avec la revue de Frogs Race, dont la grenouille et le décor (source inconnue) sont redessinés |
| **Source inconnue** | 608 (915 le 28/10) | Détail en section 3 |

**Hypothèse** (Pixabay) : Pixabay a quitté la licence CC0 en janvier 2019. Une image publiée avant reste sous CC0 ; la
date est estimée par le numéro de l'image (inférieur à 3 900 000 environ). Aucune image Pixabay plus récente n'a été
trouvée.

## 3. Les 915 images de source inconnue, par niveau de risque

### 3.1 Risque élevé : photos et illustrations venues d'ailleurs (372)

Images qui ressemblent à des photos ou des illustrations professionnelles, sans source ni licence.

| Jeu ou dossier | Images | Ce que c'est | Indice |
|---|---|---|---|
| ~~Where Is The Animal~~ | ~~149~~ | Photos d'animaux | **Remplacées le 29/10/2026** (Wikimedia Commons) |
| ~~Where Is The Sound~~ | ~~59~~ | Photos d'animaux et d'instruments | **Remplacées le 29/10/2026** (Wikimedia Commons) |
| ~~Sounds of Life~~ | ~~32~~ | Cliparts d'animaux, décors | **Remplacées le 30/10/2026** (décors dessinés pour le projet, animaux OpenMoji ; sons de Wikimedia Commons, voir `data/soundsoflife/CREDITS.txt`) |
| Pet | 21 | Photos (pièces, objets), pictogrammes | — |
| Flower of Numbers | 21 | Billets et pièces en euros, mains, dés | Les euros ont des règles de reproduction propres (BCE, faces nationales des pièces) |
| ~~Colors~~ | ~~19~~ | Coloriages (pomme, dauphin, papillon…) | **Remplacés le 29/10/2026** (OpenMoji) ; flèches et palette retirées |
| `common/images` (core) | 17 | Icônes diverses (smiley, dossier, maison…) | — |
| ~~Spot the Differences~~ | ~~15~~ | Scènes dessinées | **Remplacées le 30/10/2026** (8 scènes dessinées par script, objets OpenMoji) |
| Images par défaut (core) | 9 | Animaux dessinés (chouette, crabe, écureuil…) | Commit de 2021 « adding free of right Free Fundation images » : dites libres, source non indiquée |
| Opinions | 9 | Photos d'aliments, icônes brillantes | — |
| Room | 9 | Murs, flèches | — |
| Sprint Finish | 6 | Chat et chien dessinés, boutons | — |
| Bubble | 3 | Fonds sous-marins | Un porte « Koshkina L » (illustratrice de banques d'images) |
| Egg | 3 | Photos d'œufs | Auteur « FRIEDMANN Chloe » dans les métadonnées |

### 3.2 Dessins des jeux, auteur probable : l'étudiant qui a créé le jeu (377)

Style simple, souvent fait pour le jeu (Biboule, mascotte de GazePlay, et ses variantes de couleur), ajoutés avec le
jeu. **Hypothèse** à confirmer par leurs auteurs (noms dans l'historique git et le CSV).

| Jeux | Images |
|---|---|
| Vignettes des jeux (`common/Thumbnails`) : captures d'écran des jeux (9 de la famille Where Is It et Coloring refaites le 29/10/2026, comptées comme libres) | 58 |
| Train Switches | 43 |
| Sliding Puzzle (dont la Joconde, domaine public) | 36 |
| Cake | 24 |
| Tower Defense (certaines icônes ressemblent à des émojis : à vérifier) | 23 |
| Shooter | 20 |
| Labyrinth | 19 |
| Follow | 13 |
| Space, Survive Against Robots | 12 chacun |
| Horses, Race | 11 chacun |
| Divisor | 10 |
| Biboule Jump, dossier `common` des jeux | 9 chacun |
| Rock Paper Scissors | 7 |
| Pianosight | 6 |
| Bottle, Math101 | 5 chacun |
| Gaze Race, Rush Hour, GazePlay Eval Test (anciens jeux BERA) | 4 chacun |
| Noughts and Crosses, Order, Whack-a-Mole | 3 chacun |
| Colorblend, Cream Pie, Magic Cards, Simon | 2 chacun |
| Cups, Dice, Dot to Dot, Goose Game, Ladder | 1 chacun |

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
