# Revue des jeux, un par un

Préparée le 28/10/2026, pour la revue qui commence le 29/10/2026 (décision du mainteneur) : passer sur chaque jeu, un
par un, pour l'améliorer **graphiquement** et en **gameplay**. Pour chaque jeu sont déjà notés les points sur ses images
(licences, voir [INVENTAIRE_IMAGES.md](INVENTAIRE_IMAGES.md) et `gradle/images/inventaire-images.csv`). Les colonnes
« Graphisme » et « Gameplay » se remplissent pendant la revue.

## Règles pour chaque jeu

- Ressources **libres seulement** (GPL-3.0-or-later, ou compatible : CC0, CC BY, CC BY-SA, Apache-2.0, OFL), avec la
  source et la licence notées dans `gradle/images/README.txt` et dans la licence affichée (`gradle/templates/licence.txt`).
  Remplacer en priorité les images **non libres**, puis celles **à risque**.
- Une image remplacée garde son nom de fichier quand c'est possible (le code ne change pas). Une image retirée : vérifier
  qu'aucun code ne la lit.
- Les vignettes du menu (`gazeplay-core/.../data/common/Thumbnails`) sont des captures des jeux : à refaire si le jeu
  change d'aspect.
- Vérifier : le jeu en vrai (souris, et Tobii si possible), `allGamesTest -PallGamesOnly=<nameCode>`, et `build`.
- Ne jamais changer le format des résultats sans versionnage (`formatVersion`).

## Marche à suivre pour chaque jeu

Chaque jeu passe par les mêmes 8 étapes. Les étapes 1 et 2 ne changent rien : elles finissent par une proposition que
le mainteneur valide avant tout changement.

| Étape | Quoi | Résultat |
|---|---|---|
| **1. État des lieux** | Lire le code du jeu (`gazeplay-games/.../games/<jeu>`), ses variantes et ses ressources ; le lancer en vrai (souris, Tobii si possible) ; captures « avant » | Fiche du jeu : ce qu'il fait, ses variantes, ses défauts vus (bugs, lenteurs, textes non traduits) |
| **2. Propositions** | Graphisme (style des menus : couleurs, polices, contours ; lisibilité au regard : taille des cibles, contraste, retour visuel de la fixation), gameplay (règles, difficulté, progression, récompense, fin de partie, variantes utiles ou à retirer), images à remplacer | Liste courte, classée, **validée par le mainteneur** |
| **3. Images et sons** | Remplacer les ressources non libres ou à risque (voir la colonne « Images ») ; noter source et licence | Ressources libres, créditées dans `gradle/images/README.txt` et la licence affichée |
| **4. Graphisme** | Appliquer ce qui a été validé ; garder les noms de fichiers ou adapter le code | Jeu au nouveau style |
| **5. Gameplay** | Appliquer ce qui a été validé ; ne pas changer ce que les statistiques enregistrent sans versionner le format | Règles améliorées |
| **6. Tests** | Tests du jeu (en ajouter si le jeu n'en a pas), `build`, `allGamesTest -PallGamesOnly=<nameCode> -PallGamesRepeat=5`, essai réel du mainteneur | Tout vert, retours du mainteneur pris en compte |
| **7. Finitions** | Nouvelle vignette du menu (`common/Thumbnails`), traductions (au moins français et anglais), mise à jour de ce document (colonnes et fiche) | Jeu terminé |
| **8. Commit** | Sur demande du mainteneur, un commit par jeu (`feat(<jeu>): …`) | Historique propre, un jeu à la fois |

**Fiche à remplir pour chaque jeu** (en bas de ce document, section « Fiches ») :

```
### <Jeu> (nameCode) — <date>
- Ce qu'il fait / variantes :
- Défauts vus :
- Propositions (validées ✔ / refusées ✘) :
- Images et sons remplacés :
- Fait (graphisme, gameplay) :
- Vérifications :
- Commit :
```

## Ordre de passage

Les jeux qui partagent leur code sont traités ensemble (une amélioration profite à toute la famille). L'ordre va du plus
urgent (images non libres, jeux très joués) au plus simple ; le mainteneur peut le changer.

1. **Famille Where Is It** (un même code, `whereisit`) : Where Is The Animal, Where Is The Sound (images non libres),
   puis Color, Flag, Letter, Number, Shape, Find The Odd One Out, Where Is It.
2. **Coloring** (image non libre : Hello Kitty).
3. **Frogs Race** (image non libre probable), puis Horses et Horses Simplified (mêmes Biboules).
4. **Famille Memory** (6 entrées, images par défaut) : Memory, Memory Letters, Memory Numbers, Open Memory (×3).
5. **Jeux à images à risque** : Sounds Of Life, Spot The Differences, Pet, Flower Of Numbers, Opinions, Sprint Finish
   (×2), Bubbles (×2), Egg Game (×2).
6. **Jeux de tir et d'action** : Shoot The Biboules, Shoot The Robots, Survive Against Robots (×2), Space Game, Tower
   Defense, Casse-Brique, Ninja, Whac-A-Mole, Biboule Jump, Gaze Race.
7. **Jeux de réflexion** : Labyrinth, Puzzle, Rush Hour, Train Switches, Connect4, Tic Tac Toe, Simon, Order, Rock Paper
   Scissors, Cups and Balls, Magic Potions.
8. **Jeux de calcul et de lettres** : Math101 (×5), Divisor, Rabbits, Dice, Goose Game, Letters, Dot To Dot.
9. **Jeux de création et divers** : Scribble, Scratch Card, Magic Cards, Cream Pie, Cakes, colorblend, Piano, Bottle,
   Room, Blocks, Ladder, Follow.
10. **GazePlay Eval et GazePlay Eval Test** : en dernier (contrat avec le site GazePlay Eval, format de résultats
    versionné).

Après chaque famille : `allGamesTest` complet (77 entrées), et point avec le mainteneur.

## Légende des images

- **Non libre** : à remplacer.
- **Risque** : photo ou illustration sans source : demander à l'auteur (historique git) ou remplacer.
- **À confirmer** : dessin probablement fait par l'étudiant qui a créé le jeu.
- **OK** : libre, ou sans originalité (lettres, formes, couleurs, drapeaux).
- **Remplacé le 28/10** : images refaites (OpenMoji ou dessin par script), à regarder en vrai.

## Jeux (77 entrées du catalogue)

| Jeu (nameCode) | Images (inventaire du 28/10/2026) | Graphisme | Gameplay |
|---|---|---|---|
| **Where Is The Animal** (`WhereIsTheAnimal`) | **Risque** : 149 photos d'animaux sans source ; **non libre** : `beaver/beaver_1.jpg` (CC BY-NC-SA). Le dossier `whereIsTheAnimalDynamic` (80 photos Pixabay d'avant 2019) est libre probable | | |
| **Where Is The Sound** (`WhereIsTheSound`) | **Non libre** : 5 photos iStock/Getty (`chicken_1..3`, `lion_1`, `sheep_1`) ; **risque** : 59 photos d'animaux et d'instruments ; OK : `harp_1` (Met, CC0) | | |
| Where Is The Color, Flag, Letter, Number, Shape | OK (couleurs unies, drapeaux, lettres, chiffres, formes) | | |
| Find The Odd One Out (`FindTheOddOneOut`) | Libre probable : 48 photos Pixabay d'avant 2019 | | |
| Where Is It (`WhereIsIt`, images de l'utilisateur) | Pas d'image propre | | |
| **Coloring** (`Coloring`) | **Non libre** : `draw-hellokitty.jpg` (personnage Sanrio) ; **risque** : 19 coloriages sans source ; OK : 2 icônes Flaticon (CC BY 3.0, créditées) | | |
| **Sounds Of Life** (`SoundsOfLife`) | **Risque** : 32 cliparts et décors (un décor signé Danilo Sanino, illustrateur de banques d'images) | | |
| **Spot The Differences** (`SpotDifference`) | **Risque** : 15 scènes dessinées sans source | | |
| **Pet** (`Pet`) | **Risque** : 21 images (photos de pièces et d'objets, pictogrammes) | | |
| **Flower Of Numbers** (`FlowerOfNumbers`) | **Risque** : 21 images, dont billets et pièces en euros (règles de reproduction de la BCE), mains, dés | | |
| **Opinions** (`Opinions`) | **Risque** : 9 images (photos d'aliments, icônes brillantes) | | |
| **Sprint Finish** (`SprintFinish`, `SprintFinishMouse`) | **Risque** : 6 images (chat et chien dessinés, boutons) | | |
| **Colored Bubbles**, **Portrait Bubbles** (`ColoredBubbles`, `PortraitBubbles`) | **Risque** : 3 fonds sous-marins (un signé Koshkina L) ; Portrait utilise les images par défaut | | |
| **Egg Game**, **Personalize Egg Game** | **Risque** : 3 photos d'œufs (auteur « FRIEDMANN Chloe » dans les métadonnées) | | |
| **Frogs Race** (`FrogsRace`) | **Non libre probable** : `Racer.png` (clipart de cutecolors.com) ; à confirmer : 11 dessins (Biboule) | | |
| Magic Potions (`Potions`) | **Remplacé le 28/10** : fioles, chaudron, bulle, décor (dessinés par script), explosion (OpenMoji) | | |
| Labyrinth (`Labyrinth`) | **Remplacé le 28/10** : animaux sans fond (OpenMoji) ; à confirmer : 19 dessins (souris, flèches, fromage) et 9 du dossier `common` des jeux | | |
| Memory, Memory Letters, Memory Numbers, Open Memory (×3) | Images par défaut : 10 animaux **remplacés le 28/10** (OpenMoji) ; **risque** : 9 animaux dessinés (chouette, crabe… « Free Fundation », source non dite) ; lettres et chiffres OK | | |
| Blocks, Casse-Brique, Ninja, Scratch Card, Magic Cards, Cream Pie | Utilisent les images par défaut (voir Memory) ; Magic Cards et Cream Pie : 2 dessins chacun, à confirmer | | |
| Puzzle (`Puzzle`) | À confirmer : 36 images (Biboule, paysages) ; la Joconde est dans le domaine public | | |
| Room (`Room`) | À confirmer : 9 images (murs, flèches) ; OK : ciel ESA/Hubble (CC BY 4.0, crédité) | | |
| Tower Defense (`TowerDefense`) | À confirmer : 23 dessins ; certaines icônes ressemblent à des émojis (cœur, vague, argent) : vérifier leur origine | | |
| Train Switches (`TrainSwitches`) | À confirmer : 43 dessins (trains, gares) | | |
| Shoot The Biboules, Shoot The Robots | À confirmer : 20 dessins (Biboule, robots) ; musiques OpenGameArt | | |
| Survive Against Robots (et Mouse) | À confirmer : 12 dessins | | |
| Space Game (`SpaceGame`) | À confirmer : 12 dessins (aliens, vaisseaux) | | |
| Cakes (`Cakes`) | À confirmer : 24 dessins | | |
| Follow (`Follow`) | À confirmer : 13 dessins | | |
| Horses, Horses Simplified | À confirmer : 11 dessins (Biboule, plateaux, dés) | | |
| Divisor, Rabbits | À confirmer : 10 dessins (lapins) | | |
| Biboule Jump (`BibJump`) | À confirmer : 9 dessins (Biboule) | | |
| Rock Paper Scissors | À confirmer : 7 dessins (mains façon gant de dessin animé) | | |
| Piano (`Piano`) | À confirmer : 6 images | | |
| Math101 (×5 opérations) | À confirmer : 5 images ; OK : tableau (Openclipart, domaine public) | | |
| Bottle (`Bottle`) | À confirmer : 5 images | | |
| Gaze Race (`GazeRace`) | À confirmer : 4 images | | |
| Rush Hour (`RushHour`) | À confirmer : 4 images | | |
| Tic Tac Toe (`TicTacToe`) | À confirmer : 3 images | | |
| Order (`Order`) | À confirmer : 3 images | | |
| Whac-A-Mole (`WhacAMole`) | À confirmer : 3 images (Biboule) | | |
| colorblend (`colorblend`) | À confirmer : 2 images | | |
| Simon (`Simon`) | À confirmer : 2 images | | |
| Cups and Balls, Dice, Dot To Dot, Goose Game, Ladder | À confirmer : 1 image chacun | | |
| Connect4 (`Connect4`) | Pas d'image propre. Plantage audio sous Windows corrigé le 28/10/2026 (`MMDevAPI.dll`) : à revoir en vrai | | |
| Scribble (`Scribble`), Letters (`Letters`) | Pas d'image propre relevée | | |
| GazePlay Eval, GazePlay Eval Test | Pas d'image propre ; le dossier `gazeplayEvalTest` (4 images des anciens jeux BERA) : vérifier s'il sert encore | | |

## Hors jeux

- **Vignettes du menu** (`common/Thumbnails`, 68) : captures des jeux, à refaire avec chaque jeu modifié.
- **Icônes communes** (`common/images`, 17 de source inconnue : smiley, dossier, maison…) : **risque** ; les icônes de
  la musique (Flaticon, CC BY 3.0) sont créditées.
- **Sons et musiques** : pas encore inventoriés ; à faire avec chaque jeu.

## Fiches

Une fiche par jeu ou par famille, remplie pendant la revue (modèle plus haut).
