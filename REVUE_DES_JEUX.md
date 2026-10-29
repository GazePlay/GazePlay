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
- **Images données par l'utilisateur (décision du mainteneur, 29/10/2026)** : elles sont **toujours une variante** du
  jeu, jamais un remplacement silencieux des images du jeu. Pour un jeu qui en lit (voir la liste ci-dessous) :
  1. ajouter une valeur de variante qui implémente `UserImagesVariant` (`gazeplay-games-commons`), avec pour dossier
     le name code du jeu : le jeu reçoit alors **sans autre code** son dossier `files/images/<NameCode>` du profil, sa
     ligne dans les réglages (Dossiers, « Images des jeux ») et la fenêtre d'erreur du menu quand la variante est
     choisie sans image (`UserImagesDialog`) ; exemple : `ColoringVariant.CUSTOM` ;
  2. dans le jeu, lire les images avec `UserImagesVariant.images(dossier)`, et afficher un message si la liste est vide
     (jeu lancé sans passer par le menu) ;
  3. ne plus lire l'ancien dossier partagé ; ne pas reprendre les images qu'il contient : **pas de migration** (passage
     à GazePlay Learning 2.0, les utilisateurs remettent leurs images ; à dire dans les notes de version) ;
  4. ajouter les tests (variante, dossier vide) et vérifier la fenêtre d'erreur en vrai.

  Anciens dossiers partagés de `files/images` encore lus (à retirer au fil de la revue ; quand plus aucun jeu ne les
  lit, retirer aussi l'ancien réglage « Répertoire des fichiers » et sa fenêtre à 4 sections, `CustomFileChooser`) :

  | Ancien dossier | Jeux qui le lisent encore |
  |---|---|
  | `magiccards` | Magic Cards, Memory |
  | `default` | Memory |
  | `portraits` | Colored/Portrait Bubbles, Cream Pie, Ladder, Whac-A-Mole (moles), Ninja, Divisor |
  | `blocs` | Blocks, Egg Game, Letters |
  | `opinions` | Opinions, Casse-Brique |

  Fait : Coloring (`files/images/Coloring`, variante « Palette personnalisée », 29/10/2026).

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
| **Where Is The Animal** (`WhereIsTheAnimal`) | **Remplacées le 29/10** : 144 photos Wikimedia Commons (CREDITS.txt). Le dossier `whereIsTheAnimalDynamic` (80 photos Pixabay d'avant 2019) est libre probable | ✔ 29/10 (cartes, anneau, bandeau) | ✔ 29/10 (variantes exactes, Réécouter, pas de répétition) |
| **Where Is The Sound** (`WhereIsTheSound`) | **Remplacées le 29/10** : 64 photos Commons, triangle dessiné, harpe du Met ; 11 sons remplacés, 13 de La Sonothèque (CC0) | ✔ 29/10 | ✔ 29/10 (son dans toutes les langues) |
| Where Is The Color, Flag, Letter, Number, Shape | OK (couleurs unies, drapeaux, lettres, chiffres, formes) | ✔ 29/10 | ✔ 29/10 (Voyelles, Consonnes justes) |
| Find The Odd One Out (`FindTheOddOneOut`) | Libre probable : 48 photos Pixabay d'avant 2019 | ✔ 29/10 | ✔ 29/10 (consigne dite, pas de doublon) |
| Where Is It (`WhereIsIt`, images de l'utilisateur) | Pas d'image propre | ✔ 29/10 (et fenêtre du dossier) | ✔ 29/10 |
| **Coloring** (`Coloring`) | **Remplacés le 29/10** : 24 contours OpenMoji (CC BY-SA 4.0) ; Hello Kitty et les anciens coloriages retirés | ✔ 29/10 (boîte à outils, anneau, carte) | ✔ 29/10 (3 palettes + palette personnalisée, annuler, recommencer) |
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

### Famille Where Is It (9 entrées, code `whereisit`) — 29/10/2026

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/whereisit/`
(40 images, 1366×768, mode souris, français ; 4 temps par essai : question, images, fixation sur l'image en haut à
gauche, 2 s après). Elles ont été prises avec le script `build/game-screenshots/captures.ps1` (vraie fenêtre, dossier
GazePlay isolé). Pas d'essai avec le Tobii.

- **Ce qu'il fait** : une question est écrite au centre et dite (voix d'homme ou de femme au hasard) pendant 5 s
  (`QUESTION_LENGTH`), puis descend en bas de l'écran ; les images apparaissent en grille ; une fixation
  (`FIXATION_LENGTH`) choisit une image. Si c'est la bonne : les autres disparaissent, elle grandit jusqu'à remplir
  l'écran, puis l'animation de victoire (confettis, Biboule) et une nouvelle manche. Sinon : l'image pâlit et une croix
  apparaît ; la question est redite si le réglage le demande. Code commun : `WhereIsIt` (794 lignes), `PictureCard`
  (384), `GameSizingComputer` ; une énumération `WhereIsItGameType` (28 valeurs) ; un lanceur, un générateur de
  variantes et une spec par entrée.
- **Variantes** (grilles 1×2, 2×2, 2×3, 3×2, 3×3) :
  - Where Is The Animal : Ferme, Forêt, Savane, Oiseaux, Mer, Tous (50 animaux, 150 photos), et Dynamique (1×2
    seulement : toujours « Où est le chat ? », les autres animaux par niveau 1 à 5, niveau ajusté tous les 8 points
    par un test du χ²) ;
  - Where Is The Sound : Animaux, Instruments, Tous (24 sons, 65 photos) ;
  - Color (Facile, Normal, Difficile ; 16 couleurs), Flag (Les plus connus, Afrique, Amérique, Asie, Europe, Tous ;
    44 drapeaux), Letter (Voyelles, Consonnes, Toutes ; 26), Number (10 chiffres), Shape (Facile, Normal, Difficile ;
    20 formes) ;
  - Find The Odd One Out (6 thèmes, 48 photos ; 2×2 à 3×3) ; Where Is It (dossier d'images de l'utilisateur, avec
    `questions.csv`, sons et pictogrammes facultatifs).
- **Défauts vus** :
  - **Fait (vu en capture)** : le filtre des variantes compare des **sous-chaînes du chemin entier**
    (`WhereIsIt.java:382`, `s::contains`). La variante **Voyelles** montre des consonnes : `I`, `T` et `L` sont dans
    « data/where**I**s**T**he**L**etter/… », donc tous les dossiers passent ; Consonnes laisse passer aussi toutes les
    lettres. Dans **Ferme**, `pig` laisse passer `pigeon` (vu : une tourterelle dans « Où est la poule ? ») et `horse`
    laisse passer `seahorse` (déduit du code, pas vu).
  - **Fait (code)** : hors français, anglais et chinois, **aucun son** n'est joué (`WhereIsIt.java:624`), y compris
    dans **Where Is The Sound**, dont le son ne dépend pourtant pas de la langue : ce jeu est injouable dans les
    19 autres langues. Sa consigne « listen » n'est traduite qu'en anglais et en français.
  - **Fait (code)** : dans Where Is The Sound, le son n'est rejoué qu'après une erreur, et seulement si le réglage
    « redire la question » est actif ; aucun moyen de le réentendre autrement.
  - **Fait (code)** : Find The Odd One Out ne dit jamais sa consigne, alors que `findTheOddOneOut/sound/` contient
    `odd.m.fra.mp3`, `odd.w.fra.mp3` (et l'anglais), jamais lus. Traduction chinoise fausse (« 找出奇数 » : « trouvez
    le nombre impair »).
  - **Fait (vu)** : Where Is It lancé sans dossier d'images : écran noir, le message d'erreur (`WII-error`) n'est pas
    visible. **Hypothèse** : depuis le menu, `GameWhereIsItErrorPathDialog` empêche ce cas ; à vérifier.
  - **Fait (vu)** : à la victoire, l'image agrandie n'est pas centrée (drapeau de l'Argentine collé à gauche, bande
    noire à droite) : le calcul ne tient pas compte du recadrage de l'image dans sa case.
  - **Fait (vu)** : aucun espace ni contour entre les images (les fonds blancs des formes et des lettres se touchent ;
    les cases de Color se touchent et la case **noire** se confond avec le fond noir du jeu). Le bouton des réglages
    (roue en bas à gauche) recouvre le coin de l'image en bas à gauche.
  - **Fait (vu)** : style d'origine, sans lien avec les menus refaits : fond noir, texte blanc en police système, croix
    rose (`error.png`), indicateur de progression gris à 50 % d'opacité au centre de l'image.
  - **Fait (code)** : les photos sont chargées à leur taille d'origine (`new Image(imagePath)`), jusqu'à 5760×3840
    (`accordion_1.jpg`), soit environ 88 Mo en mémoire pour une seule image. Le dossier `whereIsTheAnimal` pèse 97 Mo
    (26 photos de plus de 1 Mo). **Risque** : lenteur à l'apparition des images et mémoire, surtout en 3×3. Rien n'a été
    mesuré.
  - **Fait (code)** : dans Find The Odd One Out et Dynamique, les images d'une même manche sont tirées avec remise : la
    même photo peut apparaître deux fois.
  - **Fait (code)** : du code mort : le paramètre `fourThree` vaut toujours `false`, `whereIsTheAnimalDynamic/levels.json`
    n'est pas lu (et contient des fautes : « turtules », « pigeaons », « tires »).
  - **Fait** : tests existants : `WhereIsItTest` (2 tests), `PictureCardTest` (3), `WhereIsItValidatorTest`. Rien ne
    vérifie les variantes ni les sons.
- **Images et sons** : voir le tableau plus haut (Animal : 149 photos à risque et `beaver_1` non libre ; Sound :
  5 photos iStock/Getty non libres et 59 à risque). **Sons pas encore inventoriés** : voix des questions (Animal,
  Color, Flag, Letter, Number, Shape, Dynamique, Odd), 24 sons de Where Is The Sound sans source connue.
- **Propositions (étape 2, 29/10/2026)**, en attente de validation (✔ / ✘). Aucune ne change le format des résultats
  (fichiers, colonnes, `formatVersion`) ni ce qui est compté (points, erreurs, χ²). **Risque commun à A1, A6 et C2** :
  le rejeu d'une partie déjà enregistrée refait les tirages à partir de sa graine ; si l'ensemble des images ou la
  façon de tirer change, le rejeu d'une ancienne partie de ces jeux montre d'autres images que celles vues à
  l'époque (les fichiers enregistrés ne changent pas).
  - **A. Corrections** (sans choix de style, à faire en premier) :
    - A1. Variantes : comparer le **nom exact du dossier** au lieu d'un morceau du chemin (Voyelles, Consonnes, Ferme
      redeviennent justes). Test ajouté : chaque variante ne contient que ses dossiers.
    - A2. Where Is The Sound : jouer le son **dans toutes les langues** (il ne dépend pas de la langue), et traduire
      « listen » dans les langues du fichier.
    - A3. Find The Odd One Out : dire la consigne (`odd.m/w.fra/eng.mp3`, déjà livrés) ; corriger la traduction
      chinoise.
    - A4. Victoire : image agrandie **centrée**.
    - A5. Charger les images **à la taille de leur case** (même aspect, moins de mémoire).
    - A6. Pas deux fois la même photo dans une manche (Odd, Dynamique).
    - A7. Where Is It sans dossier : message d'erreur lisible.
    - A8. Retirer le code mort (`fourThree`, `levels.json`).
  - **B. Graphisme** (style des menus : couleur principale, polices Andika et Fredoka, coins arrondis) :
    - B1. **Cartes** : un espace entre les images, coins arrondis, fond et contour discrets (la case noire de Color
      et les fonds blancs des formes et lettres se distinguent du fond) ; suit le réglage clair / sombre.
    - B2. **Fixation** plus visible au regard : un anneau ou un contour de la couleur principale qui se remplit
      autour de la carte, et un léger grossissement, à la place de l'indicateur gris au centre de l'image.
    - B3. **Question** en Fredoka, dans un bandeau arrondi en bas, qui ne chevauche ni les images ni la roue des
      réglages ; la grille laisse libre la place de la roue.
    - B4. **Réponses** : bonne réponse entourée de vert avant l'agrandissement ; mauvaise réponse : croix dessinée
      (couleur « danger » du thème) à la place de `error.png`, image grisée.
  - **C. Gameplay** :
    - C1. Bouton **« Réécouter »** (grande cible, activée par fixation) dans le bandeau, pour les jeux qui ont un son
      (indispensable dans Where Is The Sound, utile partout).
    - C2. La bonne réponse ne revient pas **deux manches de suite**.
    - C3. Where Is The Sound : un haut-parleur animé pendant l'écoute, en plus de « Écoute le son ».
  - **D. Images et sons** :
    - D1. Remplacer tout de suite les **6 images non libres** (5 iStock/Getty de Where Is The Sound, `beaver_1`),
      en gardant les noms de fichiers.
    - D2. Source des remplacements : **photos de Wikimedia Commons** sous CC0, CC BY ou CC BY-SA, avec un crédit
      par fichier (recommandé : le jeu fait reconnaître de vrais animaux et objets), ou dessins OpenMoji comme pour les
      images par défaut (homogène, mais moins réaliste). **Hypothèse à vérifier** : les photos Pixabay publiées
      depuis 2019 (licence Pixabay, non libre au sens de la GPL) ne conviennent pas ; celles d'avant 2019 (CC0)
      conviennent.
    - D3. Les **208 photos à risque** (149 Animal, 59 Sound) : les remplacer toutes maintenant (gros travail, script
      de téléchargement et crédits), ou par lots plus tard.
    - D4. Réduire les photos livrées à 1280 px au plus (le dossier Animal pèse 97 Mo ; gain non mesuré).
    - D5. **Sons** : inventaire de la famille (voix des questions : chercher l'auteur dans l'historique git ; 24 sons
      de Where Is The Sound : remplacer par des sons CC0 ou CC BY si la source reste inconnue).
  **Validées le 29/10/2026 : toutes (A, B, C, D) ✔** ; D2 : photos de Wikimedia Commons ; D3 : tout de suite.
- **Défauts trouvés en chemin (29/10/2026)** : la variante Oiseaux et « Tous » ne montraient jamais le flamant rose
  (`"greater flamingo"` dans `variants.json`, dossier `greaterflamingo`) ; la question du serpent avait la clé
  `snakev` (jamais trouvée) ; « Où est le chat? » sans espace. Corrigés.
- **Défaut du menu signalé par le mainteneur (29/10/2026)** : après une partie lancée par la fenêtre des variantes, le
  menu revenait flou (navigable). Cause : choisir une variante fermait la fenêtre sans retirer le flou du menu (seule
  la croix le retirait), et le menu est gardé en mémoire. Ancien (avant la refonte), pour tout jeu à plusieurs
  variantes ; même oubli dans la fenêtre du dossier de Where Is It. Corrigé : le flou est retiré à la fermeture de la
  fenêtre, quoi qui la ferme (`setOnHidden`, comme `EvalChooserDialog`) ; test `GameVariantDialogTest`.
- **Fenêtre « dossier de Where Is It » (signalée par le mainteneur le 29/10/2026)** : restée à l'ancien style (cadre du
  système, texte rouge, boutons gris) ; son message principal était une clé absente des traductions
  (`WhereIsItNot Config Directory`, affichée telle quelle) et son titre « error ». Refaite au style des menus
  (`MenuDialog` : carte arrondie, titre « Où est-ce ? », boutons arrondis, couleur « danger » pour un mauvais dossier,
  Valider au clavier aussi) ; message `WhereIsItDirNotConfig` ; « Réinitialiser » avec une majuscule (aussi dans les
  réglages). Test `GameWhereIsItErrorPathDialogTest` ; capture `3d-ou-est-ce-dossier` ajoutée à `menuScreenshots`.
- **Images et sons remplacés** :
  - Photos de Where Is The Animal (150) et Where Is The Sound (71, la harpe du Met gardée) : Wikimedia Commons,
    « Quality images » d'abord, licences CC0, domaine public, CC BY (toutes versions) ou CC BY-SA 4.0 seulement (les
    CC BY-SA 2.0 à 3.0 ne sont pas compatibles avec la GPL 3) ; choisies une par une sur planches (pas d'œufs, de
    squelettes, de personnes reconnaissables pour les instruments) ; 3 par dossier, nommées `<dossier>_1..3.jpg`,
    1280 px au plus. Crédits : `data/whereIsTheAnimal/CREDITS.txt`, `data/whereIsTheSound/CREDITS.txt`, licence
    affichée.
  - Sons de Where Is The Sound : 13 de La Sonothèque (CC0, vérifié sur leur site) gardés ; 11 remplacés par des sons
    de Commons (accordéon, batterie, éléphant, flûte, harpe, piano, mouton, serpent, triangle, trompette, violon),
    raccourcis à 8 s, normalisés (ffmpeg, loudnorm), mono. Remplacés parce que sans source (8), sous licence
    propriétaire Sound Fishing (mouton, serpent) ou SoundJay (harpe). **Choisis d'après leur titre, sans écoute** :
    à écouter pendant l'essai.
  - Voix des questions (toute la famille) : ajoutées par Didier Schwab (2017) et Sakraft (2022), sans source notée ;
    probablement de la synthèse vocale (**hypothèse**). **Question** : à confirmer auprès d'eux ; gardées.
  - **Deuxième tri (29/10/2026, demande du mainteneur : des images plus faciles à comprendre pour des enfants ; les
    pictogrammes ARASAAC, sous CC BY-NC-SA 4.0, ne sont pas compatibles avec la GPL 3)** : règle stricte, l'animal
    entier et bien visible, sans gros plan de la tête, sans camouflage ni animal minuscule. 30 photos remplacées
    (girafe, aigle, chouette, paon, perroquet, pigeon, zèbre, hippopotame, dauphin, crocodile, pieuvre, hippocampe,
    vautour, loup, renard, chat, ours, raton laveur, mouton, serpent, chien, poule…), 5 retirées sans remplaçant
    (castor : 1 photo ; abeille, chouette, zèbre, vautour : 2). Where Is The Animal : 144 photos.
  - Triangle de Where Is The Sound : aucune photo libre utilisable, dessiné par `gradle/images/make_triangle_image.py`.
    Castor, poule, applaudissements, flûte, harpe, trompette, violon : 1 ou 2 photos seulement (rien de mieux sur
    Commons) ; les animaux sont gardés (un animal nouveau n'aurait pas de voix pour sa question). Scripts et choix :
    `gradle/images/commons/`.
- **Fait (graphisme, gameplay), 29/10/2026** :
  - A1 filtre exact (`WhereIsIt.filterFolders`) ; A2 son de Where Is The Sound dans toutes les langues, « listen »
    traduit ; A3 consigne de l'intrus dite, traduction chinoise corrigée ; A4 victoire centrée (la carte entière) ;
    A5 images chargées au plus à la taille de la zone de jeu ; A6 pas de doublon dans une manche (Odd, Dynamique) ;
    A7 message d'erreur dans un bandeau ; A8 `fourThree` et `levels.json` retirés.
  - B : `GameSizingComputer` (grille avec espaces au-dessus d'un bandeau qui laisse la roue libre), `PictureCard`
    (carte arrondie aux couleurs du thème, anneau de fixation de la couleur principale qui fait le tour de la carte,
    grossissement léger, cadre vert, croix « danger » dessinée, contour des échantillons de Color), question en
    Fredoka dans un bandeau arrondi (`GameStyle`, nouveau, dans `gazeplay-games-commons`, réutilisable par les
    autres jeux).
  - C1 bouton « Réécouter » (`ReplayQuestionButton`, fixation, icône Material Symbols `volume_up`) ; C2 pas deux fois
    la même réponse (échange de places, sans tirage en plus) ; C3 haut-parleur animé pendant l'écoute.
  - Rejeu : les tirages gardent l'ordre des versions précédentes (dossier, image, voix) ; seules les manches touchées
    par A1, A6 ou C2 se rejouent autrement.
  - **Changé sans être demandé** : le réglage « couleur de la barre de progression » ne s'applique plus à ces jeux
    (l'anneau prend la couleur principale, comme validé en B2) ; les zones d'intérêt (écran des statistiques, rien sur
    disque) sont maintenant le centre de chaque carte.
- **Vérifications (29/10/2026)** : `build` réussi, **539 tests, 0 échec** (509 avant ; nouveaux tests dans
  `WhereIsItTest` : variantes exactes, sons par langue, consigne de l'intrus, pas de doublon, pas deux fois la même
  réponse, disposition) ; `allGamesTest` complet réussi (77 entrées) ; les 9 entrées de la famille lancées 5 fois
  chacune (`-PallGamesRepeat=5`) sans échec. Captures « après » : `build/game-screenshots/apres/whereisit/` et
  `essai1/whereisit/`. **Reste** : l'essai réel du mainteneur (souris et Tobii ; écouter les 11 sons remplacés),
  puis l'étape 7 (nouvelles vignettes du menu).
- **Essai réel du mainteneur (29/10/2026)** : validé (« tout est bon »), après les corrections du flou du menu et de
  la fenêtre du dossier de Where Is It.
- **Étape 7 (finitions), 29/10/2026** : 9 vignettes du menu refaites (`common/Thumbnails`, mêmes noms) à partir de
  captures des jeux, roue des réglages masquée, 960 × 540 (format des cartes du menu) ; Where Is It montré avec un
  dossier d'exemple (4 photos de Where Is The Animal). Crédits : `gradle/images/README.txt` (les vignettes montrent les
  photos créditées dans les `CREDITS.txt`), inventaire mis à jour. Traductions : « listen » dans les 21 langues, pas
  d'autre texte nouveau (le bouton « Réécouter » n'a pas de texte). `build` : **543 tests, 0 échec**.
- **Commit** : `c3c67bb5e` (29/10/2026), `feat(whereisit): revue de la famille Where Is It`.

### Coloring (`Coloring`, code `colors`) — 29/10/2026

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/coloring/` (mode
souris, 1366×768). Pas d'essai avec le Tobii.

- **Ce qu'il fait** : un dessin noir et blanc à gauche, une boîte à outils à droite (palette de 5 couleurs plus une
  couleur choisie, boutons Charger, Sauvegarder, Imprimer) ; une fixation sur une zone du dessin la remplit de la couleur
  choisie (remplissage par zones, un point compté par remplissage) ; flèches « précédent » et « suivant » pour changer de
  dessin parmi 17. Pas de variante. Code : `ColorsGame` (765 lignes), `ColorToolBox` (617), `ColorBox`,
  `CustomColorPicker` ; aucun test.
- **Défauts vus** :
  - **Fait (vu, journal)** : la flèche « suivant » (et « précédent ») lève `IllegalArgumentException: duplicate children
    added` (`ColorsGame.launch`, ligne 204) : elle relance tout le jeu, qui rajoute des nœuds déjà présents. Le dessin
    change quand même (le changement est fait avant), mais le reste du lancement est interrompu à chaque fois.
  - **Fait (code)** : chaque changement de dessin ajoute un gestionnaire de coloriage et deux écouteurs de taille sans
    retirer les anciens. **Hypothèse** : après plusieurs dessins, un clic de souris colorie (et compte un point) autant
    de fois qu'il y a de gestionnaires ; à vérifier. `dispose()` est vide.
  - **Fait (vu)** : palette pauvre : rouge, bleu, vert, blanc, ocre (+ une case « couleur personnalisée » blanche au
    départ, d'où deux cases blanches) ; ni jaune, orange, rose, violet, marron, gris. Boutons du bas tronqués
    (« Char… », « Sauvegar… », « Impri… »). Style d'origine (fond doré, cases rectangulaires, flèches vertes).
  - **Fait (journal)** : traductions absentes : `imgChooserTitle`, `customColorDialogTitle`.
  - **Fait (code)** : `draw-leaf.png` fait 6000×7500 pixels, converti pixel par pixel en noir et blanc : lent et lourd
    en mémoire (non mesuré). Le remplissage n'atteint jamais la dernière colonne de pixels.
  - Pas d'annulation : un coloriage raté au regard ne peut pas être défait (seulement recouvert).
- **Images** : **non libre** : `draw-hellokitty.jpg` (Sanrio). **Source inconnue** : les 16 autres coloriages
  (ajoutés par Karine Benhaimi en 2022 et Yaniissou en 2024, style de cliparts du web), `next.png`, `prev.png`,
  `palette.png` (2018). `add-button-inside-black-circle.png` et `error.png` : Flaticon CC BY 3.0, crédités.

**Étape 2 (propositions), 29/10/2026**, en attente de validation (✔ / ✘). Aucune ne change le format des résultats ni
ce qui est compté (un point par zone remplie).

- **A. Corrections** :
  - A1. Flèches précédent / suivant : changer seulement le dessin, sans relancer le jeu (plus d'exception) ; retirer les
    anciens gestionnaires et écouteurs ; `dispose()` qui nettoie. Tests ajoutés.
  - A2. Dessins réduits au chargement (2000 px au plus) : plus de lenteur avec les grands fichiers.
  - A3. Traductions manquantes (français, anglais) ; remplissage jusqu'au bord.
- **B. Graphisme** (style des menus) :
  - B1. Boîte à outils : panneau arrondi aux couleurs du thème, grandes pastilles de couleur, la couleur choisie
    entourée de la couleur principale ; boutons Charger / Sauvegarder / Imprimer avec icônes (Material Symbols), sans
    texte tronqué.
  - B2. Flèches précédent / suivant rondes au style des menus, avec l'anneau de fixation ; « 3 / 17 » entre les deux.
  - B3. Dessin sur une carte blanche arrondie, avec une marge ; indicateur de fixation sur le dessin de la couleur
    principale.
- **C. Gameplay** :
  - C1. Palette de 12 couleurs, toutes visibles sans pages (rouge, orange, jaune, vert clair, vert, bleu clair, bleu,
    violet, rose, marron, gris, blanc pour gommer), plus la couleur personnalisée.
  - C2. Bouton « Annuler » (le dernier remplissage), activé par fixation. Le point déjà compté reste compté.
  - C3. Bouton « Recommencer » (le dessin redevient blanc).
- **D. Images** :
  - D1. Retirer Hello Kitty tout de suite.
  - D2. Remplacer les 16 coloriages de source inconnue par les contours noirs d'OpenMoji (CC BY-SA 4.0, déjà utilisé
    pour les animaux par défaut) : dessins simples, zones fermées, faciles à colorier au regard ; une vingtaine de sujets
    (pomme, poisson, papillon, gâteau, dauphin, tulipe, raisin, maison, arc-en-ciel, lapin, soleil, lune, tortue, chat,
    voiture, étoile…), à choisir sur planche. Autre choix : demander aux deux auteurs d'où viennent leurs dessins.
  - D3. Flèches et boutons : icônes Material Symbols (déjà livrées) à la place de `next.png`, `prev.png`, `palette.png`.
- **Validées le 29/10/2026 : toutes (A, B, C, D) ✔** ; D2 : contours d'OpenMoji.
- **Images remplacées** : 17 coloriages (dont Hello Kitty) → 20 contours noirs d'OpenMoji (CC BY-SA 4.0 ; pomme,
  ballon, papillon, gâteau, poussin, poisson, fleur, grenouille, cadeau, raisin, maison, glace, coccinelle, étoile,
  fraise, soleil, ourson, train, arbre, tortue), générés par `gradle/images/make_coloring_images.py` : agrandis, traits
  épaissis ; **zones fermées vérifiées une à une** (le chat, le chien, la voiture, le lapin entier, l'éléphant,
  l'arc-en-ciel d'OpenMoji ont des traits ouverts : la couleur aurait coulé sur tout le fond, non retenus). Images des
  flèches, de la palette et des boutons retirées (icônes Material Symbols : `arrow_forward`, `undo`, `restart_alt`,
  `save`, `print` ajoutées à `data/icons`). Crédits : licence affichée, `gradle/images/README.txt`, inventaire.
- **Fait (29/10/2026)** :
  - A1 flèches : changent seulement le dessin (`changeDrawing`) ; plus de relance du jeu ni d'exception ; nœuds et
    écouteurs créés une fois, retirés par `dispose()`. A2 dessins chargés à 1600 px au plus ; pixels traités dans un
    tableau (plus rapide que la lecture pixel par pixel). A3 traductions (français, anglais) ; remplissage jusqu'au bord.
  - B : `ColorToolBox` refaite (panneau arrondi, pastilles rondes, couleur choisie entourée de la couleur principale,
    outils et fichiers en boutons ronds à icône avec info-bulle) ; `GazeRoundButton` (nouveau, `gazeplay-games-commons`,
    fixation ou clic, réutilisable) ; flèches rondes et « n / 20 » dans un bandeau ; dessin sur une carte blanche
    arrondie ; anneau de fixation qui suit le regard (`FixationRing`, à la place de l'indicateur gris et de son
    triangle) ; fenêtre « Autres couleurs » au style des menus (15 couleurs, le coloriage en pause pendant ce temps).
  - C1 palette de 12 couleurs, toutes visibles, + couleur personnalisée ; C2 « Annuler » (10 niveaux ; le point compté
    reste compté) ; C3 « Recommencer » (lui-même annulable).
  - `CustomColorBox` et `CustomColorPicker` retirés (remplacés).
- **Vérifications** : `ColorsGameTest` (6 tests : noir et blanc, remplissage jusqu'au bord, 20 dessins, flèches sans
  relance, annuler et recommencer, niveaux d'annulation) ; `build` : **549 tests, 0 échec** ; `allGamesTest
  -PallGamesOnly=Coloring -PallGamesRepeat=5` réussi. Captures : `build/game-screenshots/essai2/coloring/`.
  **Reste** : l'essai réel du mainteneur (souris et Tobii), puis l'étape 7 (vignette).
- **Demandes du mainteneur après l'essai (29/10/2026)** :
  - Le jeu commençait à « 7 / 20 » : il démarrait sur le dessin du réglage `COLORS_DEFAULT_IMAGE` (par défaut
    `draw-flower.png`, 7e par ordre alphabétique). Il démarre maintenant sur le premier dessin de la variante ; le
    réglage ne sert plus qu'à retrouver la dernière image chargée, dans « Mes images ».
  - **4 variantes** (fenêtre des variantes avant le jeu, `ColoringVariant`) : **Facile** (1 ou 2 zones à colorier),
    **Normal** (3 à 6), **Difficile** (7 et plus), 8 dessins chacune, et **Mes images** (seulement les images chargées
    par l'utilisateur, copiées dans le dossier `colors` du profil ; message s'il n'y en a pas encore). Zones comptées par
    calcul sur la page telle que le jeu la voit (zones fermées d'au moins 0,05 % de la page). 24 dessins OpenMoji
    (ajoutés : hibou, fusée, tournesol, château, grande roue, diamant) dans `data/colors/images/easy|normal|hard`.
    Une partie enregistrée avant les variantes se rejoue en Facile.
  - Les images sont chargées au plus à 1600 px, sans être agrandies (une petite image restait floue) ; flèches grisées
    quand il n'y a qu'un dessin.
  - Vérifications : `ColorsGameTest` (10 tests), `build` réussi, `allGamesTest -PallGamesOnly=Coloring
    -PallGamesRepeat=3` réussi ; captures `build/game-screenshots/essai3/coloring/`.
- **Décision du mainteneur (29/10/2026)** : variante « Mes images » retirée (des images non adaptées seraient trop
  difficiles à colorier) ; les trois autres renommées **Petite palette** (1 ou 2 zones), **Moyenne palette** (3 à 6)
  et **Grande palette** (7 et plus). Les valeurs internes (`EASY`, `NORMAL`, `HARD`) sont gardées, car elles sont
  écrites dans les parties enregistrées. Le bouton « Charger une image » de la boîte à outils reste (il existait avant
  la revue). `build` réussi ; `allGamesTest -PallGamesOnly=Coloring -PallGamesRepeat=3` réussi.
- **Décision du mainteneur (29/10/2026) : les images de l'utilisateur sont toujours une variante.** Bouton « Charger une
  image » retiré de Coloring ; variante **Palette personnalisée** ajoutée : elle joue les images du dossier
  `files/images/Coloring` du profil (par défaut `Documents\GazePlay_Learning\files\images\Coloring`).
  - Mécanisme commun (`UserImagesVariant`, `gazeplay-games-commons`) : une valeur de variante qui l'implémente donne son
    dossier ; sans autre code, le jeu a alors un réglage (réglages, Dossiers, « Images des jeux », une ligne par jeu,
    trouvée dans le catalogue), une fenêtre d'images (`UserImagesDialog` : vignettes, suppression, « Ajouter des
    images », copiées dans le dossier) et, dans le menu, la même fenêtre en rouge quand la variante est choisie sans
    image (comme pour « Où est-ce ? ») : « Valider » lance le jeu dès qu'il y a une image.
  - Tests : `UserImagesDialogTest` (2), `ColorsGameTest` (11) ; capture `3e-images-utilisateur` de `menuScreenshots`.
  - **Reste (à décider, voir la réponse au mainteneur)** : les autres jeux qui lisent les anciens dossiers partagés de
    `files/images` (`magiccards` : Magic Cards, Memory ; `default` : Memory ; `portraits` : bulles, tartes à la crème,
    échelle, taupes, Ninja, Divisor ; `blocs` : Blocs, Egg, Letters ; `opinions` : Opinions, Casse-briques), sans
    variante (les images de l'utilisateur remplacent celles du jeu sans le dire).
- **Essai réel du mainteneur (29/10/2026)** : validé (« tout est ok »), avec la palette personnalisée.
- **Étape 7 (finitions), 29/10/2026** : vignette du menu refaite (`common/Thumbnails/colors.png`, même nom) : capture
  de la Moyenne palette, papillon à moitié colorié, roue des réglages masquée, 960 × 540. Crédits :
  `gradle/images/README.txt`, inventaire. Traductions des textes nouveaux : français et anglais. `build` : **556
  tests, 0 échec**.
