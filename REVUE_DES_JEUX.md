# Revue des jeux, un par un

Préparée le 28/10/2026, pour la revue qui commence le 29/10/2026 (décision du mainteneur) : passer sur chaque jeu, un
par un, pour l'améliorer **graphiquement** et en **gameplay**. Pour chaque jeu sont déjà notés les points sur ses images
(licences, voir [INVENTAIRE_IMAGES.md](INVENTAIRE_IMAGES.md) et `gradle/images/inventaire-images.csv`). Les colonnes
« Graphisme » et « Gameplay » se remplissent pendant la revue.

## À FAIRE EN PRIORITÉ à la prochaine séance (demandes du mainteneur du 29/10/2026 au soir, et point 5 du 30/10)

**À faire avant de commencer la famille suivante (Frogs Race).** Rien n'est codé : chaque point passe par les étapes
habituelles (vérifier, faire, tester, montrer, commit sur demande).

**État laissé le 29/10/2026** : dernier commit `475853c04` (Coloring). **Non commité** : la revue du lecteur GazePlay
Eval (fiche plus bas : graphisme, anneau de fixation, cartes), `MenuMessageDialog` (fenêtres de l'import et de la
langue), la touche Retour arrière du formulaire, le message de plein écran retiré. Le mainteneur ne les a pas encore
essayés en vrai (sauf ce qu'il a signalé). **Ne rien commiter pour l'instant** (décision du mainteneur du
29/10/2026 : d'autres changements d'Eval vont venir) ; commit seulement quand il le demandera.

1. **[Codé le 30/10/2026 ; essai du mainteneur le 30/10 : évaluation quittée par l'engrenage, `session.json` vérifié]** Retirés : les deux lignes des réglages et
   `buildQuitKeyChooser`, `GameContext.createQuitShortcut` et `isTyping` (et leurs appels dans `GameMenuController`
   et `ReplayingGameFromJson`), l'exception de `blocksGameInput` (tout le clavier est arrêté menu ouvert), la
   traduction, la clé `QUIT_KEY` de `Configuration`. Fait vérifié : `ApplicationConfig` garde les clés inconnues ; un
   ancien fichier avec `QUIT_KEY` se lit et s'enregistre, la clé y reste sans servir
   (`ConfigurationTest.shouldReadAndSaveAFileWithTheRemovedQuitKey`). Aucun jeu ne lisait la clé.
   **Retirer le réglage « Bouton pour quitter » (QuitKey) et son fonctionnement.** Raison : dans les jeux et les
   évaluations, l'engrenage bleu en bas à gauche mène déjà à la page des statistiques puis au menu principal associé.
   - Où : la ligne `QuitKey` des réglages, deux fois (`ConfigurationContext` : section Jeux vers la ligne 192, section
     Évaluations vers la ligne 293) et `buildQuitKeyChooser` ; le raccourci `GameContext.createQuitShortcut` (et
     `isTyping`, ajouté le 29/10 pour le formulaire d'Eval) ; l'exception de la touche « quitter » dans
     `GameContextFactory.blocksGameInput` (le clavier sera entièrement bloqué pendant que le menu est ouvert) ; la
     traduction `QuitKey` ; les tests qui en parlent ; la mention dans `GazePlayEval.md` (« la touche quitter
     quitte aussi une évaluation »).
   - **Retirer aussi la clé `QUIT_KEY` de `Configuration`** (décision du mainteneur du 29/10/2026 : elle ne sert plus) :
     propriété, accesseurs, valeur par défaut. Vérifier avant qu'aucun jeu ne la lit pour autre chose, et qu'un fichier
     `GazePlay.properties` existant qui contient encore `QUIT_KEY` se lit sans erreur (clé inconnue ignorée ; regarder
     `ConfigurationSource`, `LegacyConfigurationKeys` et l'écriture atomique : la clé peut rester dans les fichiers déjà
     écrits ou disparaître au prochain enregistrement, les deux conviennent). Ajouter un test de lecture d'un ancien
     fichier avec `QUIT_KEY`.
   - Vérifier en vrai qu'on sort bien de chaque partie et d'une évaluation par l'engrenage (et que les résultats d'une
     évaluation quittée sont écrits, `terminee: false`).
2. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** Retirés : le bouton d'import de la liste
   (`EvalSettings.refreshLibrary`), la ligne `EvalResults` et `EvalSettings.buildResultsFolderButton`. Gardés :
   `importEvaluation`, `openResultsFolder` et les traductions `EvalImportButton`, `EvalResults` (menu des
   évaluations). Test : `ConfigurationContextTest` (liste sans bouton, plus de ligne `EvalResults`).
   **Réglages des évaluations** (section « Évaluations (GazePlay Eval) ») :
   - dans « Évaluations importées », **retirer le bouton « Importer une évaluation »** (déjà sur le menu principal des
     évaluations) ; garder la liste et la suppression (`EvalSettings.buildLibrary`, bouton `importButton`) ;
   - **retirer la ligne « Résultats des évaluations »** (`EvalResults`, `EvalSettings.buildResultsFolderButton`), déjà
     sur le menu principal des évaluations.
   - Garder `EvalSettings.importEvaluation` (utilisé par `EvalMenuScreen`).
3. **[Codé le 30/10/2026, essayé par le mainteneur]** D'abord un bouton rond « i » dans la barre du haut, puis
   (retour du mainteneur du 30/10) **une carte comme celle du site** : 3 cartes (Tester, Importer, Résultats), puis 2
   centrées dessous (Créer une évaluation, Aide), toutes de 92 px de haut ; page resserrée pour tenir en 1366 × 768
   (écarts 20, pastille de « Lancer » 96 px, cartes 14 px de marge verticale). Partie « Touches de l'examinateur »
   réécrite par moment (pendant un écran ; sur l'écran Recommencer / Continuer ; quitter), choix du mainteneur.
   Fenêtre `EvalHelpDialog` (`MenuDialog`, défilante). Texte dans
   `gazeplay-core/src/main/resources/data/help/eval-help-fra.txt` et `-eng.txt` (pas dans `multilinguism.csv`, qui
   n'admet ni virgule ni retour à la ligne) : `# ` titre, `- ` point de liste, sinon paragraphe ; anglais pour les
   autres langues. Styles `gp-help-*` (`menus.css`). Tests : `EvalMenuScreensTest` (ouverture et fermeture, mêmes
   sections en français et en anglais, construction du texte). Capture `3g-evaluations-aide` de `menuScreenshots`.
   Le texte parle déjà du bouton « Commencer » du point 4.
   **Menu principal des évaluations (`EvalMenuScreen`) : ajouter un bouton d'aide** qui explique le fonctionnement
   des évaluations, au style des menus (une carte secondaire comme « Importer », qui ouvre une fenêtre `MenuDialog`) :
   les touches de l'examinateur pendant une évaluation (`Espace` écran suivant, `P` rejouer le son ou la vidéo, `R` ou
   `←` recommencer, `C` ou `→` continuer, `Entrée` confirmer la proposition du participant ; l'engrenage pour quitter),
   les modes de passation (classique, soutien oral, soutien visuel, autonomie totale), le délai avant sélection, ce
   qui est enregistré et où, la différence avec « Tester une évaluation ». Source : `GazePlayEval.md` 12.3. Faire
   relire le texte au mainteneur avant de le figer ; traductions français et anglais.
4. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** Bouton « Commencer » (`EvalStartButton`, pilule de la
   couleur principale) sous les champs ; retirés : `EvalFormHint` (code et traduction), le `setOnAction` des champs,
   le cas `FORM` de `onKey`. Fait : en mode « regard seul » (`tobii`), la zone de jeu bloque la souris
   (`applyInputMode`), donc le bouton ne se clique pas ; il s'atteint avec Tab puis Espace (clavier standard de JavaFX,
   pas un raccourci du jeu). Décision du mainteneur (30/10/2026) : le formulaire n'a pas à être utilisable au
   regard, c'est l'examinateur qui le remplit et lance la passation ; **en mode « Tobii seul », la souris et le clic
   passent sur le formulaire, et sur cet écran seulement**. Fait par une marque `IGameContext.MOUSE_ALLOWED_PROPERTY`
   (propriété du nœud) que le filtre de `applyInputMode` respecte, avec ses enfants ; le formulaire est retiré au
   démarrage, donc les écrans de l'évaluation restent au regard seul. Tests :
   `GameContextFactoryInputModeTest.shouldLetTheMouseThroughAMarkedScreenWithTheEyeTrackerAlone`, et dans `GazeplayEvalTest.shouldStartTheFormWithItsButtonOnly` (Entrée ne démarre plus,
   plus de message), passation interrompue démarrée par le bouton.
   **Formulaire participant (avant l'évaluation) : un bouton « Commencer »** au lieu de devoir appuyer sur une touche,
   et **retirer le message** d'aide (`EvalFormHint`, « Remplissez les champs puis appuyez sur Entrée pour
   commencer » : c'est `Entrée`, pas `Espace`, mais le mainteneur ne veut plus de touche à expliquer). Bouton au style
   des menus sous les champs, dans `GazeplayEval.showForm`. **Aucun raccourci clavier** (décision du mainteneur du
   29/10/2026) : retirer `Entrée` (le `setOnAction` des champs et le cas `FORM` de `onKey`) ; adapter les tests
   (`GazeplayEvalTest` envoie `Entrée` pour valider le formulaire : appuyer sur le bouton à la place).
5. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** 4ᵉ carte « Créer une évaluation (site GazePlay Eval) »
   (`EvalWebsite`, icône globe `language` de Material Symbols Rounded, Apache-2.0, téléchargée du dépôt de Google ;
   source vérifiée : `info` téléchargé est identique au fichier livré). Adresse fixe `EvalMenuScreen.EVAL_WEBSITE`.
   `SystemFileManager.openWebPage` : `rundll32 url.dll,FileProtocolHandler` sous Windows (adresse transmise entière,
   `#` compris), `open` sous macOS, `xdg-open` sous Linux, lancé sans attendre. Si la commande ne peut pas être
   lancée : `MenuMessageDialog` avec l'adresse à recopier (`EvalWebsiteFailed`). Les 4 cartes sur une ligne en
   1366 × 768 (texte 190 px au lieu de 220, écart 16, `TilePane` : même taille pour toutes). Texte d'aide complété.
   Tests : `EvalMenuScreensTest` (adresse ouverte par un objet simulé, message sans navigateur),
   `SystemFileManagerTest`. Limite : on ne sait pas si le navigateur a vraiment affiché la page (seul l'échec du
   lancement est détecté).
   **Menu principal des évaluations (`EvalMenuScreen`) : un bouton qui ouvre le site GazePlay Eval** (demande du
   mainteneur du 30/10/2026), pour créer une évaluation. Adresse : `https://lig-gazeplay.imag.fr/#/home` (celle du
   README du dépôt du site et de `GazePlayEval.md`). Ouvre le navigateur par défaut du système. Ce sera une carte
   secondaire au style des menus, à côté de « Tester », « Importer » et « Résultats » (et de l'aide du point 3).
   - À vérifier avant : comment `SystemFileManager.openFolder` ouvre un dossier, pour ouvrir une adresse de la même
     façon et sous chaque système (Windows, Linux, macOS), sans bloquer le thread JavaFX. Il n'y a encore aucune
     ouverture de page web dans le code.
   - Un message si le navigateur ne s'ouvre pas (pas de navigateur, machine sans réseau), avec l'adresse à recopier
     (`MenuMessageDialog`).
   - Icône libre (Material, Apache-2.0, comme les autres), traductions français et anglais, test du bouton (adresse
     ouverte par un objet simulé, jamais un vrai navigateur dans les tests), capture du menu.
   - **Décisions du mainteneur (30/10/2026)** : libellé « Créer une évaluation (site GazePlay Eval) » ; adresse
     **fixe dans le code** (pas de réglage).
6. **Tâche de fin, après la revue de tous les jeux et leur acceptation** : refonte graphique **et fonctionnelle** de
   l'engrenage bleu des jeux (bouton en bas à gauche et son panneau : musique, volume, durée de fixation, vitesse,
   taille, barre de progression, couleur de fond ; `GameContextFactory`, `GameContext.createControlPanel`). À
   préparer comme les autres étapes (état des lieux, propositions validées). Penser à la décision encore ouverte sur
   le temps du menu compté dans les durées (`MODERNIZATION.md` 2an).
7. **À surveiller, puis à chercher à la toute fin s'il revient** (décision du mainteneur du 30/10/2026) : le
   30/10/2026 vers 11 h 19, pendant un `build` complet, la JVM des tests de `gazeplay-core` s'est arrêtée dès le
   début du module (« Gradle Test Run :gazeplay-core:test FAILED », `MessageIOException: Could not write
   '/127.0.0.1:…'`, « Connection reset by peer » ; aucun test en échec, pas de `hs_err`). Relancé aussitôt :
   `:gazeplay-core:test` seul puis `build` complet réussis (575 tests). Cause inconnue. Si ça revient : noter
   l'heure, le dernier test écrit dans `gazeplay-core/build/test-results/test`, chercher un `hs_err_pid*.log` et
   `%LOCALAPPDATA%\CrashDumps`. Si rien ne revient d'ici la fin de la revue des jeux, chercher quand même à la fin.

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

## Aide des jeux (demande du mainteneur du 30/10/2026, validée le 30/10/2026)

Pour les jeux déjà revus (hors GazePlay Eval) : un « i » rond en bas à droite de la fenêtre des variantes ouvre
l'aide du jeu (le principe, comment jouer, comment gagner, les variantes). Un jeu **sans variante** qui a une aide
ouvre, au lieu de se lancer, une fenêtre avec son aide et un bouton « Lancer le jeu » (aucun des jeux revus n'est dans
ce cas pour l'instant). Textes : `gazeplay-core/src/main/resources/data/help/games/<NameCode>-fra.txt` et `-eng.txt`
(nom de code sans espaces ; `# ` titre, `- ` point, sinon paragraphe ; anglais pour les autres langues), fenêtre
`HelpDialog` (commune avec l'aide des évaluations), `GameHelp`. Tests : `GameHelpTest` (chaque jeu revu : aide en
français et en anglais, mêmes parties ; fenêtre « Lancer le jeu »), `GameVariantDialogTest`. Captures
`3h-aide-petits-chevaux`, `3i-aide-sans-variante`. `build` : **602 tests, 0 échec**. **Validé** : l'aide fait partie de l'étape 7 de chaque
jeu revu ensuite.

## Fenêtre des variantes des jeux revus (demande du mainteneur du 30/10/2026)

Pour tout jeu revu (il a une aide, `GameHelp.exists`) : les boutons des variantes **choisissent** la variante (elle est
entourée de la couleur principale ; la première est choisie à l'ouverture) sans lancer le jeu ; un bouton
**« Commencer »** tout en bas, au centre, lance le jeu avec les choix faits (Entrée aussi) ; le « i » de l'aide est à
sa droite. **Tous les boutons radio sont en bas**, sous les variantes, au-dessus de « Commencer » (les groupes juste
au-dessus des sous-groupes pour les variantes à deux niveaux, `GroupedGameVariant`). Entre deux rangées de boutons radio, un
espace net (28 px), la fenêtre agrandie d'autant. En changeant de groupe, la
variante à la même place reste choisie. Les jeux pas encore revus gardent l'ancien fonctionnement (un clic lance, les
boutons radio en haut). Tests : `GameVariantDialogTest`. Captures `3a-variantes-memory`, `3a-variantes-ou-est-l-animal`,
`3a-variantes-petits-chevaux`. Les aides disent « En bas » pour ces boutons et expliquent « Commencer ». `build` : **610 tests, 0 échec**.

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
| **7. Finitions** | Nouvelle vignette du menu (`common/Thumbnails`), traductions (au moins français et anglais), **aide du jeu** (`data/help/games/<NameCode>-fra.txt` et `-eng.txt` : le principe, comment jouer, comment gagner, les variantes ; ajouter le jeu à `GameHelpTest`), mise à jour de ce document (colonnes et fiche) | Jeu terminé |
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

Après chaque famille : `allGamesTest` complet (71 entrées depuis le 30/10/2026), et point avec le mainteneur.

## Légende des images

- **Non libre** : à remplacer.
- **Risque** : photo ou illustration sans source : demander à l'auteur (historique git) ou remplacer.
- **À confirmer** : dessin probablement fait par l'étudiant qui a créé le jeu.
- **OK** : libre, ou sans originalité (lettres, formes, couleurs, drapeaux).
- **Remplacé le 28/10** : images refaites (OpenMoji ou dessin par script), à regarder en vrai.

## Jeux (77 entrées du catalogue au 29/10/2026, 71 depuis le 30/10 : Horses Simplified et les 5 autres Memory sont des variantes)

| Jeu (nameCode) | Images (inventaire du 28/10/2026) | Graphisme | Gameplay |
|---|---|---|---|
| **Where Is The Animal** (`WhereIsTheAnimal`) | **Remplacées le 29/10** : 144 photos Wikimedia Commons (CREDITS.txt). Le dossier `whereIsTheAnimalDynamic` (80 photos Pixabay d'avant 2019) est libre probable | ✔ 29/10 (cartes, anneau, bandeau) | ✔ 29/10 (variantes exactes, Réécouter, pas de répétition) |
| **Where Is The Sound** (`WhereIsTheSound`) | **Remplacées le 29/10** : 64 photos Commons, triangle dessiné, harpe du Met ; 11 sons remplacés, 13 de La Sonothèque (CC0) | ✔ 29/10 | ✔ 29/10 (son dans toutes les langues) |
| Where Is The Color, Flag, Letter, Number, Shape | OK (couleurs unies, drapeaux, lettres, chiffres, formes) | ✔ 29/10 | ✔ 29/10 (Voyelles, Consonnes justes) |
| Find The Odd One Out (`FindTheOddOneOut`) | Libre probable : 48 photos Pixabay d'avant 2019 | ✔ 29/10 | ✔ 29/10 (consigne dite, pas de doublon) |
| Where Is It (`WhereIsIt`, images de l'utilisateur) | Pas d'image propre | ✔ 29/10 (et fenêtre du dossier) | ✔ 29/10 |
| **Coloring** (`Coloring`) | **Remplacés le 29/10** : 24 contours OpenMoji (CC BY-SA 4.0) ; Hello Kitty et les anciens coloriages retirés | ✔ 29/10 (boîte à outils, anneau, carte) | ✔ 29/10 (3 palettes + palette personnalisée, annuler, recommencer) |
| **Sounds Of Life** (`SoundsOfLife`) | **Remplacées le 30/10** : 4 décors dessinés, 21 animaux OpenMoji ; 21 sons (15 de Commons, 6 de Where Is The Sound), crédités | ✔ 30/10 (anneau, nom de l'animal) | ✔ 30/10 (rejeu, placement sans fond, exception) |
| **Spot The Differences** (`SpotDifference`) | **Risque** : 15 scènes dessinées sans source | | |
| **Pet** (`Pet`) | **Risque** : 21 images (photos de pièces et d'objets, pictogrammes) | | |
| **Flower Of Numbers** (`FlowerOfNumbers`) | **Risque** : 21 images, dont billets et pièces en euros (règles de reproduction de la BCE), mains, dés | | |
| **Opinions** (`Opinions`) | **Risque** : 9 images (photos d'aliments, icônes brillantes) | | |
| **Sprint Finish** (`SprintFinish`, `SprintFinishMouse`) | **Risque** : 6 images (chat et chien dessinés, boutons) | | |
| **Colored Bubbles**, **Portrait Bubbles** (`ColoredBubbles`, `PortraitBubbles`) | **Risque** : 3 fonds sous-marins (un signé Koshkina L) ; Portrait utilise les images par défaut | | |
| **Egg Game**, **Personalize Egg Game** | **Risque** : 3 photos d'œufs (auteur « FRIEDMANN Chloe » dans les métadonnées) | | |
| **Frogs Race** (`FrogsRace`) | **Remplacées le 30/10** : grenouilles et mare dessinées pour le projet (`make_race_images.py`) ; `Racer.png` (non libre, inutilisé), le tube rouge et la cage retirés ; Biboules gardées (dessins du projet) | ✔ 30/10 (couloirs, « Toi », langue, pastille) | ✔ 30/10 (Facile/Normal/Difficile, classement, statistiques justes) |
| Magic Potions (`Potions`) | **Remplacé le 28/10** : fioles, chaudron, bulle, décor (dessinés par script), explosion (OpenMoji) | | |
| Labyrinth (`Labyrinth`) | **Remplacé le 28/10** : animaux sans fond (OpenMoji) ; à confirmer : 19 dessins (souris, flèches, fromage) et 9 du dossier `common` des jeux | | |
| **Memory** (une seule carte depuis le 30/10, avant 6) | **Remplacées le 30/10** : les 9 images par défaut à risque (OpenMoji, 8 jeux en profitent), dos de carte dessiné par le jeu ; lettres et chiffres dessinés (Fredoka) | ✔ 30/10 (cartes, anneau, retournement, cadres, pastille) | ✔ 30/10 (une carte, Mes images, Dynamique corrigé, 1,5 s) |
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
| **Horses, Horses Simplified** | **Remplacées le 30/10** : plateaux et boutons du dé dessinés pour le projet (`make_horses_images.py`, d'après `positions*.json`) ; `selector.png` retiré ; Biboules gardées | ✔ 30/10 (fond des menus, équipe en cours, anneaux, cartes) | ✔ 30/10 (contre l'ordinateur, dé équilibré, règles corrigées) |
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
- **Commit** : `475853c04` (29/10/2026), `feat(colors): revue du jeu Coloriage, et images de l'utilisateur en variante`.

### GazePlay Eval et GazePlay Eval Test (`GazePlayEval`, `GazePlayEvalTest`, code `gazeplayEval`) — 29/10/2026

Passée avant les autres familles (demande du mainteneur, 29/10/2026 : finir GazePlay Learning Eval, pour qu'il ne
reste que les jeux).

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/eval/`
(évaluation de référence « Tests sons et vidéos », installée dans un dossier GazePlay isolé ; une image par seconde
pour le début : `eval-video-t03..16`). Mode souris, pas d'essai avec le Tobii. Les menus des évaluations
(`EvalMenuScreen`, fenêtre de choix, réglages) ont déjà été refaits (`REFONTE_GRAPHIQUE.md`) : la revue porte sur le
**lecteur** (`GazeplayEval`, 1 260 lignes ; `EvalTarget`).

- **Écrans du lecteur** : message (aucune évaluation, évaluation illisible) ; formulaire participant ; transition
  (croix de fixation) ; consigne (texte, image, vidéo, son, bouton « Commencer » à fixer) ; grille (images et sons,
  sélection par fixation) ; écran « Recommencer / Continuer » (modes de passation) ; « Fin de l'évaluation ».
- **Défauts vus** :
  - **Fait (vu)** : style d'origine partout : police du système, tailles fixes en pixels (36, 40, 22…) qui ne suivent
    pas la taille de l'écran (formulaire minuscule en 1366 × 768, champs blancs) ; boutons « Commencer »,
    « Recommencer », « Continuer » en rectangles de la couleur de la barre de progression à 30 % (kaki sur fond noir),
    flèches en caractères (« ↺ », « → », « ▶ ») ; proposition du participant en rouge vif.
  - **Fait (code)** : indicateur de fixation = cercle de progression gris au centre de la cible (comme l'ancien Where
    Is It) ; case sans image : cadre pointillé et « ♪ » ; consigne sonore : « ♪ » géant ; message d'erreur en texte
    brut.
  - **Fait (vu)** : la vidéo s'affiche bien (les premières captures, trop tôt ou trop tard, étaient noires).
  - **Fait (code)** : dossier `data/gazeplayEvalTest` (4 photos et 2 sons de l'ancien `gazeplayEvalTest` retiré le
    27/10/2026 : vélo, voiture, cerise, ours) lu par aucun code.
- **Contraintes** : le format du site est un contrat (rien ne change dans la lecture) ; les résultats (format 1) ne
  changent pas si les **zones des cibles** restent les mêmes (image + petite marge, E8) ; les cartes de chaleur
  contiennent une capture de l'écran : elles montreront le nouvel aspect (format inchangé). Pour une évaluation, ce que
  voit le participant doit rester **neutre** : aucun retour « bonne / mauvaise réponse ».

**Étape 2 (propositions), 29/10/2026**, en attente de validation (✔ / ✘) :

- **A. Corrections** :
  - A1. Tailles proportionnelles à l'écran (textes, boutons, croix), au lieu de pixels fixes.
  - A2. Retirer le dossier inutilisé `data/gazeplayEvalTest`.
- **B. Graphisme** (style des menus, sans toucher aux zones des cibles) :
  - B1. Formulaire participant : carte arrondie, titre en Fredoka, champs arrondis aux couleurs du thème, consigne
    lisible.
  - B2. Bouton « Commencer » et écran « Recommencer / Continuer » : grands boutons arrondis (couleur de surface,
    icône Material Symbols : lecture, recommencer, flèche), contour de la couleur principale ; proposition du
    participant (soutien visuel) : contour épais de la couleur principale au lieu du rouge.
  - B3. Indicateur de fixation : anneau de la couleur principale qui fait le tour de la cible (comme Where Is It et
    Coloring), à la place du cercle gris ; même durée de fixation.
  - B4. Grille : cartes arrondies **dessinées exactement sur la zone de la cible** (aucun changement des résultats) ;
    image sélectionnée et gardée : contour de la couleur principale (neutre, ni vert ni rouge). **Question** : cartes
    claires autour des images, ou garder les images seules sur le fond (plus neutre pour une évaluation) ?
  - B5. Cases et consignes sonores : icône haut-parleur (Material Symbols) au lieu de « ♪ ».
  - B6. Messages (aucune évaluation, évaluation illisible, fin de l'évaluation) dans un bandeau arrondi ; consigne
    texte en Andika, plus lisible.
  - Croix de fixation : gardée telle quelle (forme standard), seulement à l'échelle.
- **C. Fonctionnement** : aucun changement du protocole (défini par le site et les modes de passation).
- **D. Images** : aucune (les images viennent des évaluations) ; vignettes des deux jeux : pas affichées dans les
  menus (le menu des évaluations a ses cartes à icône), rien à refaire.
- **Validées le 29/10/2026 : A et B ✔** ; B4 : cartes claires autour des images (le mainteneur veut voir l'anneau de
  fixation faire le tour de l'image, comme dans Where Is It).
- **Fait (29/10/2026)** :
  - A1 tailles proportionnelles à l'écran (`unit()` : un centième du plus petit côté) ; A2 `data/gazeplayEvalTest`
    retiré (4 photos, 2 sons).
  - B1 formulaire sur une carte arrondie (titre Fredoka, champs arrondis, textes Andika) ; B2 boutons « Commencer »,
    « Recommencer », « Continuer » arrondis à icône (Material Symbols `play_arrow`, `replay`, `arrow_forward`),
    proposition du participant en contour épais de la couleur principale ; B3 anneau de fixation de la couleur
    principale sur le bord de la cible (`EvalTarget`, cercle autour de la croix), à la place du cercle gris ; B4 carte
    claire dessinée **exactement sur la zone de la cible** (image + marge, inchangée), sélection gardée entourée de la
    couleur principale ; B5 haut-parleur au lieu de « ♪ » ; B6 messages et fin dans une carte arrondie.
  - **Données** : zones des cibles, durées de fixation, événements et fichiers de résultats inchangés (format 1) ; les
    cartes de chaleur montrent le nouvel aspect des écrans. Le réglage « taille de la barre de progression » ne
    s'applique plus à Eval (l'anneau suit la taille de la cible).
  - **Changé sans être demandé** : `GameStyle` (`gazeplay-games-commons`) gagne la police Andika, une carte de texte et
    une carte de cible arrondie, réutilisables par les autres jeux.
- **Vérifications** : tests Eval existants (passations complètes, résultats, cartes de chaleur) réussis sans
  changement ; `build` : **556 tests, 0 échec** ; `allGamesTest` GazePlayEval et GazePlayEvalTest réussis ; captures
  `build/game-screenshots/essai1/eval/` (formulaire, grille, anneau `eval-ring-t15/16`, choix). **Reste** : l'essai
  réel du mainteneur (souris et Tobii), en particulier une consigne avec bouton « Commencer », une croix à fixer et
  une case sonore, absentes de l'évaluation de référence.
- **Retours du mainteneur après l'essai (29/10/2026)**, corrigés :
  - La fenêtre « évaluation importée » (et toutes les fenêtres de l'import : erreurs, remplacement, suppression d'une
    évaluation) était une alerte du système : remplacée par `MenuMessageDialog` (nouvelle, au style des menus, dans
    `net.gazeplay.ui.menus`) ; son texte renvoie maintenant à « Lancer une évaluation » du menu des évaluations (il
    parlait encore du jeu GazePlay Eval). Les deux messages du changement de langue utilisent aussi cette fenêtre.
  - Formulaire participant : la touche Retour arrière ne marchait pas (toutes les touches du champ étaient consommées
    pour que la touche « quitter » ne quitte pas le jeu). Le champ traite maintenant ses touches ; c'est le raccourci
    « quitter » (`GameContext`) qui ignore les touches tapées dans un champ de saisie. Test
    `GazeplayEvalTest.shouldEraseALetterOfTheForm`.
  - Le message de JavaFX « Appuyez sur Échap pour quitter le mode plein écran » au début : retiré pour toute
    l'application (`setFullScreenExitHint("")`, menu et parties) ; Échap quitte toujours le plein écran.
  - Tests : `MenuMessageDialogTest` (3) ; capture `3f-message` de `menuScreenshots` ; `build` réussi.

### Frogs Race (`FrogsRace`, code `race`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, `Race.java`, 734 lignes ; pas de variante) : 14 insectes (les
  Biboules volantes) traversent le haut de l'écran ; chaque insecte où entre le regard ou la souris est attrapé (pas
  de durée de fixation : l'entrée suffit, `setOffFixationLengthControl`). Tous les 2 insectes, la grenouille du
  joueur avance de 5 % de la largeur ; il en faut 36 pour arriver (18 sauts). Trois autres grenouilles avancent
  seules, chacune en 15 à 30 s (tirées au hasard). Un tube rouge en bas au centre tourne vers le regard.
- **Défauts vus (faits, lecture du code)** :
  - D1. `Racer.png` (non libre probable, clipart de cutecolors.com) **n'est lu par aucun code** : la grenouille est
    `frogJump.gif`. `Cage.png` (143 octets) est presque vide.
  - D2. Le « tube rouge » (`hand.png`, `handShot.png`) vient du jeu des robots : il ne représente rien ici (ce n'est
    ni une grenouille ni une langue). Les branches `biboule` / `robot` de `updateHand` et `updateCage` sont copiées de
    ce jeu.
  - D3. La grenouille du joueur est **plus petite** que les autres (taille ÷ 15 au lieu de ÷ 10) et de même image :
    rien n'indique laquelle est la sienne, ni sa ligne.
  - D4. Si une autre grenouille arrive la première, la course **recommence sans rien dire** (`endGame` relance) :
    pas de classement, pas de « perdu ». Seule la victoire du joueur a l'animation « Bravo ».
  - D5. Hypothèse (non mesurée) : 36 insectes avant une grenouille qui met 15 à 30 s, soit 1,2 à 2,4 insectes par
    seconde : le joueur perd souvent, surtout au regard.
  - D6. Insectes petits (un trentième de l'écran, environ 36 px en 1080 p au départ) : cibles difficiles au regard.
    Seul le passage bleu → vert des couleurs est joué (jaune, orange, rouge : code mort).
  - D7. **Statistiques fausses** : `newCircle()` compte un but atteint à chaque apparition d'insecte
    (`incrementNumberOfGoalsReached`), en plus de celui de la capture ; le nombre de buts à atteindre n'est jamais
    compté. Corriger change les nombres enregistrés (pas le format des fichiers) : à expliquer dans les notes de
    version, sans `formatVersion` (les fichiers gardent leurs colonnes).
  - D8. À chaque course, `launch()` ajoute 4 écouteurs de taille sur la zone de jeu, jamais retirés (ils
    s'accumulent) ; `dispose()` n'arrête pas les animations des insectes.
  - D9. « Score: » écrit en dur (non traduit), en blanc sans fond ; la vignette du menu montre l'ancien score et
    l'ancien engrenage.
- **Images (licences, `INVENTAIRE_IMAGES.md`)** : `Racer.png` non libre probable (inutilisée) ; `frogJump.gif`
  (grenouille animée de style dessin animé, 372 Ko) et `Background.jpg` (jungle vectorielle de style banque
  d'images) : source inconnue, **à risque** ; Biboules (`Blue`…`Red`, `Flash`) : dessins du projet (kuhlkrein,
  2018), à confirmer ; `frog.WAV` : source inconnue.
- **Propositions** : **toutes validées ✔ par le mainteneur (30/10/2026)** ; Biboules : dessins du projet ✔ ;
  variantes Facile / Normal / Difficile ✔.
  - Images
    - I1. Supprimer `Racer.png`, `Cage.png`, `hand.png`, `handShot.png` (inutiles ou sans rapport).
    - I2. Remplacer `frogJump.gif` par des grenouilles **dessinées pour le projet** (script, comme les potions),
      vues de côté, une couleur par ligne, avec une pose « saut ».
    - I3. Remplacer `Background.jpg` par un décor dessiné pour le projet : une mare vue de face avec quatre lignes
      de nénuphars jusqu'à une ligne d'arrivée.
    - I4. Garder les Biboules volantes (dessins du projet), à confirmer par le mainteneur.
  - Graphisme
    - G1. La grenouille du joueur plus grande que les autres, sur sa ligne marquée de la couleur principale, avec
      « Toi » à côté.
    - G2. À la place du tube rouge, une grenouille en bas au centre qui **tire la langue** vers l'insecte attrapé.
    - G3. Insectes plus gros (au moins un douzième de la hauteur), dans toutes leurs couleurs.
    - G4. Le score dans une pastille au style des menus (`GameStyle.pill`), traduit ; une ligne d'arrivée dessinée.
  - Gameplay
    - P1. Fin de course : le **classement** s'affiche (1ʳᵉ, 2ᵉ…), avec « Bravo » si le joueur gagne, puis une
      nouvelle course.
    - P2. Une course **gagnable** : la vitesse des autres grenouilles dépend d'une variante (Facile, Normal,
      Difficile), Facile par défaut.
    - P3. Corriger les statistiques (D7) : un but atteint par insecte attrapé, un but à atteindre par insecte
      apparu.
    - P4. Corriger D8 (écouteurs retirés, animations arrêtées).
- **Fait (30/10/2026)** :
  - Images : `gradle/images/make_race_images.py` dessine 4 grenouilles de course (vert pour le joueur, bleu,
    orange, violet ; assise et en saut), la grenouille de la rive (de face, bouche ouverte) et la mare (ciel et
    arbres en haut, rive, eau). Retirés : `Racer.png`, `Cage.png`, `hand.png`, `handShot.png`, `frogJump.gif`,
    `Background.jpg`. Crédits : `gradle/images/README.txt`, licence affichée, inventaire.
  - `Race.java` réécrit : 8 insectes (un douzième de la hauteur, couleur au hasard) qui volent dans le ciel ; à
    l'entrée du regard ou de la souris, la grenouille de la rive tire la langue, ramène l'insecte, et la grenouille
    du joueur saute (20 insectes pour arriver). 4 couloirs sur la mare, celui du joueur bordé de la couleur
    principale avec « Toi » ; ligne d'arrivée à damier ; pastille « Insectes attrapés : n ». Les autres grenouilles
    sautent seules, à un rythme tiré au hasard selon la variante (`RaceVariant` : Facile 90 à 110 s, Normal 60 à
    75 s, Difficile 40 à 50 s ; **valeurs non mesurées, à ajuster après l'essai réel**). Fin de course : carte du
    classement 3,5 s, « Bravo » si le joueur gagne, puis nouvelle course. Plus d'écouteurs de taille ni de classes
    `Target` et `Point`.
  - Statistiques : un but à atteindre par insecte apparu, un but atteint par insecte attrapé (D7). Les colonnes
    des fichiers ne changent pas ; les nombres des parties de Frogs Race d'avant le 30/10 sont faux (buts atteints
    comptés aussi à l'apparition) : à dire dans les notes de version. Une partie enregistrée avant les variantes se
    rejoue en Facile.
  - Traductions (français, anglais) : `RaceYou`, `RaceCaught`, `RaceWon`, `RaceLost`, `RaceFrog_*` ; variantes :
    `Easy`, `Normal`, `Hard` (déjà traduites). Vignette du menu refaite (photo du jeu sans fenêtre, 960 × 540).
- **Vérifications** : `RaceTest` (7 : départ, nouvelle course dans la même zone, capture unique, victoire,
  défaite, variantes, fin de partie ; chaque test arrête sa course, dont les minuteries débordaient sur les tests
  suivants) ; `build` : **575 tests, 0 échec** ;
  `allGamesTest -PallGamesOnly=FrogsRace -PallGamesRepeat=5` réussi ; photos du jeu sans fenêtre (couloirs,
  langue, classement). **Reste** : l'essai réel du mainteneur (souris et Tobii), en particulier la difficulté des
  trois variantes.
- **Commit** : `67f6b871b` (30/10/2026), `feat(race): revue de Frogs Race`.

### Horses et Horses Simplified (`Horses`, `Horses Simplified`, code `horses`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, `Horses.java` 451 lignes, `Pawn`, `Square` et 4 sous-classes) :
  les petits chevaux, de 2 à 4 équipes (variante : nombre de joueurs). On choisit les équipes en fixant les grands
  ronds des écuries ; puis, à tour de rôle, on fixe le bouton du dé au centre, et l'on fixe le pion à déplacer. Un 6
  sort un pion de l'écurie et fait rejouer ; un pion qui tombe sur un pion adverse le renvoie à l'écurie ; il faut
  le chiffre exact pour chaque case de l'échelle finale ; **le premier pion arrivé gagne**. Horses : plateau de
  15 cases de côté, 4 pions par équipe, échelle de 6 ; Simplified : plateau de 9, 1 pion par équipe, échelle de 3
  (avec le même dé de 1 à 6).
- **Défauts vus (faits, lecture du code)** :
  - D1. Toutes les équipes sont jouées par la même personne (pas d'adversaire automatique) : seul devant
    l'oculomètre, le joueur joue 2 à 4 équipes contre lui-même.
  - D2. **Dé déséquilibré** (`DiceRoller`, commun à Horses, Dice et Goose Game) : la face dépend des angles
    arrondis au quart de tour ; calcul : 3 et 4 sortent 1 fois sur 4 chacun, 1, 2, 5 et 6 une fois sur 8
    (hypothèse : angles tirés uniformément ; à vérifier par un test). Un 6, nécessaire pour sortir, est donc plus
    rare que prévu.
  - D3. Un pion qui tombe sur un pion de **sa** équipe recule d'une case (`Square.pawnLands`) : le recul rappelle
    `pawnLands`, donc `endOfTurn()` est appelé **deux fois** (une équipe saute son tour) ; la case quittée oublie le
    premier pion.
  - D4. On ne peut pas sortir un pion si un pion **adverse** est sur la case de départ (la règle habituelle le
    renverrait à l'écurie).
  - D5. Messages en gros texte blanc cerné de noir (un dixième de la hauteur) qui se superposent au plateau ;
    clés en anglais traduites par `Multilinguism` ; **en biélorusse, « %s team's turn » est traduit avec `%d` :
    `String.format` plante** au premier tour dans cette langue.
  - D6. Statistiques : un but atteint par lancer de dé, aucun but à atteindre.
  - D7. Fond noir, couleurs pures (bleu, jaune, rouge, vert saturés) ; le pion à choisir n'est montré que par
    `selector.png` posé dessus ; l'équipe dont c'est le tour n'est dite que par un message de 4 s et la couleur du
    bouton du dé. Vignettes du menu anciennes (fond noir).
- **Images (licences)** : plateaux `horsiesboard.png`, `horsiesBoardSimplified.png`, `selector.png` et boutons du
  dé `rollImages/roll*.png` : ajoutés par Teo Blachere en 2019, source non dite (« à confirmer » ; les deux dés
  des boutons ressemblent à un clipart) ; Biboules : dessins du projet (gardées).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. Redessiner les deux plateaux par script **à partir de `positions.json`** (les cases tombent exactement
      où le jeu les attend), dans des couleurs plus douces, au style des menus.
    - I2. Remplacer les boutons du dé (`roll*.png`) par un dé dessiné (script) sur un rond de la couleur de
      l'équipe ; remplacer `selector.png` par un anneau dessiné par le jeu.
    - I3. Garder les Biboules (pions).
  - Graphisme
    - G1. Un bandeau en haut au style des menus : « Au tour de l'équipe bleue » avec sa Biboule, toujours visible ;
      les autres messages dans une carte (`GameStyle.card`), plus petits, au lieu du gros texte sur le plateau.
    - G2. Les pions jouables entourés d'un anneau de la couleur de l'équipe qui pulse ; l'écurie de l'équipe en
      cours mise en avant.
    - G3. Fond de la couleur de fond des menus (plus de noir) ; nouvelles vignettes.
  - Gameplay
    - P1. **Adversaires automatiques** : le joueur joue une équipe, l'ordinateur les autres (il lance le dé et
      choisit un pion tout seul, avec une pause pour qu'on suive) ; variantes « 1 joueur contre 1, 2 ou 3
      ordinateurs » en plus de « 2, 3 ou 4 joueurs » (à confirmer).
    - P2. Corriger D3 : un pion ne peut pas s'arrêter sur un pion de son équipe (le mouvement n'est pas proposé).
    - P3. Dé équilibré (D2), corrigé dans `DiceRoller` : profite aussi à Dice et Goose Game (à vérifier dans ces
      jeux à leur revue).
    - P4. Sortir un pion sur la case de départ occupée par un adversaire le renvoie à l'écurie (D4).
    - P5. Messages par le traducteur du jeu, `%d` biélorusse corrigé (D5).
    - P6. Statistiques : un but à atteindre par tour du joueur, atteint quand il a joué (à confirmer).
- **Décisions du mainteneur (30/10/2026)** : tout validé ✔ ; garder les deux sortes de variantes (contre
  l'ordinateur et à plusieurs) ; le premier pion arrivé gagne.
- **Fait (30/10/2026)** :
  - Images : `gradle/images/make_horses_images.py` dessine les deux plateaux d'après `positions.json` et
    `positionsSimplified.json` (les cases tombent où le jeu pose les pions ; couleurs adoucies, numéros de
    l'échelle, flèches de départ et de sortie) et les 4 boutons du dé ; `selector.png` retiré. Crédits :
    `gradle/images/README.txt`, licence affichée, inventaire.
  - Variantes (`HorsesVariant`, un seul générateur pour les deux jeux) : 1 joueur contre 1, 2 ou 3 ordinateurs ;
    2, 3 ou 4 joueurs. Une partie enregistrée avant (variante « nombre de joueurs ») se rejoue en « 2 joueurs ».
  - Ordinateur : il prend les équipes restantes après le choix des personnes, lance le dé et choisit un pion après
    une pause de 1,2 s (priorité : renvoyer un pion adverse à l'écurie, avancer sur l'échelle, sortir un pion, puis
    le pion le plus avancé) ; son pion choisi a l'anneau un instant.
  - Règles : un déplacement qui finirait sur un pion de son équipe n'est pas proposé (`Pawn.destination`, calcul
    sans effet qui suit exactement le déplacement réel, `Square.peekDestination`) ; plus de double fin de tour ;
    sortir un pion sur un adversaire le renvoie à l'écurie ; l'action d'un pion est donnée une fois au bouton (les
    écouteurs s'accumulaient à chaque tour).
  - **Dé équilibré** (`DiceRoller`, commun à Horses, Dice et Goose Game) : la face est tirée d'abord (1 chance sur
    6), puis les quarts de tour qui la montrent. **Impact** : le rejeu d'une partie de ces trois jeux enregistrée
    avant le 30/10 ne retrouve plus les mêmes lancers (même graine, autre tirage) ; à dire dans les notes de
    version. Les faces de Dice et Goose Game sont à regarder à leur revue.
  - Graphisme : fond des menus ; à gauche, la Biboule et le nom de l'équipe en cours (« (ordinateur) » s'il y a
    lieu), son écurie entourée ; messages dans une carte à droite ; pions jouables entourés d'un anneau de leur
    couleur qui pulse. Vignettes refaites (photos du jeu sans fenêtre).
  - Statistiques : un but à atteindre par tour d'une personne, atteint quand elle lance le dé (avant : un but
    atteint par lancer, sans but à atteindre) ; colonnes des fichiers inchangées.
  - Traductions : nouveaux messages `Horses*` (français, anglais) ; les anciens messages en anglais du jeu sont
    retirés, dont celui traduit avec `%d` en biélorusse (plantage).
- **Retour du mainteneur (30/10/2026) : une seule carte au menu**, la version dans la fenêtre des variantes.
  Fait : « Horses Simplified » n'est plus une entrée du catalogue (76 entrées) ; `HorsesVariant` a 12 valeurs
  (les 6 manières de jouer × plateau normal ou simplifié `SIMPLIFIED_…`) ; `GameVariantDialog` range les variantes
  de Horses par plateau, avec **deux boutons radio en bas** (« Version normale », « Version simplifiée ») ;
  libellés raccourcis (« Contre 1 ordinateur ») pour tenir dans les boutons. Statistiques des deux plateaux dans le
  dossier `horses` (l'ancien dossier `horsesSimplified` reste tel quel). Rejeu : un fichier enregistré sous
  « Horses Simplified » est rejoué comme Horses, plateau simplifié, 2 joueurs (`ReplayingGameFromJson`). Vignette
  `horsesSimplified.png` retirée. Test : `GameVariantDialogTest.shouldChooseTheBoardOfHorsesUnderTheVariants` ;
  capture `3a-variantes-petits-chevaux` de `menuScreenshots`.
- **Vérifications** : `build` : **586 tests, 0 échec** (avant la fusion des deux cartes), **587** après ; `HorsesTest` (9), `DiceRollerTest` (2, dont 6 000
  lancers : chaque face entre 880 et 1 120) ;
  `allGamesTest` Horses et Horses Simplified, 5 fois chacun ; photos du jeu sans fenêtre. **Reste** : l'essai réel
  du mainteneur (souris et Tobii), en particulier une partie contre l'ordinateur.
- **Commit** : avec l'aide des jeux, le 30/10/2026 (`feat(horses)`), à la demande du mainteneur.

### Famille Memory (6 entrées, code `memory`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code : `Memory` 394 lignes, `MemoryCard` 383, 6 lanceurs, 6 specs, un seul
  générateur de variantes) : des cartes face cachée (Memory, Memory Letters, Memory Numbers) ou face visible (Open
  Memory, Open Memory Letters, Open Memory Numbers) ; une fixation retourne (ou choisit) une carte ; deux cartes
  identiques sont retirées, sinon elles se retournent après 1 s. Quand il ne reste plus de carte : « Bravo », puis une
  nouvelle manche (pas de fin). Variantes : Normal 2×2, 2×3, 3×2, 3×4, 4×3, et Dynamique (départ 2×2 ; la taille
  change selon le nombre d'essais). Images : Memory lit les dossiers partagés `magiccards` puis `default` du profil,
  sinon les images par défaut (`data/common/default`) ; lettres et chiffres : `data/common/letters`, `numbers`.
- **Défauts vus (faits, lecture du code)** :
  - D1. **Dynamique** : la règle « 3 manches de suite » compare 3 fois le **même** nombre d'essais (celui de la
    manche qui vient de finir, `MemoryCard` lignes 173-178), au lieu des 3 dernières manches.
  - D2. Une touche **Espace** sur une carte qui a le focus retire toutes les cartes et vide l'écran
    (`gameInstance.dispose(); gameContext.clear()`), sans fin de partie ni retour au menu : écran vide.
  - D3. Statistiques : un but atteint par paire trouvée, aucun but à atteindre (le nombre de paires n'est jamais
    compté).
  - D4. Le dos de carte (`red-card-game.png`) est rechargé depuis le disque à chaque carte retournée ou remise.
  - D5. Aucun retour visible quand une paire est bonne (les cartes disparaissent) ou fausse (elles se retournent) ;
    dans les versions visibles, une paire fausse ne se voit pas du tout.
  - D6. Style d'origine : fond noir, cartes collées en hauteur, indicateur de fixation gris au centre de la carte.
    Vignettes : 6 anciennes (celle de Memory montre une infirmière, sans rapport).
  - D7. Images de l'utilisateur lues dans les anciens dossiers partagés `magiccards` et `default` (à remplacer par
    une variante, règle du 29/10/2026).
- **Images (licences)** : dos de carte `magiccards/images/red-card-game.png` : source inconnue (2017 ; aussi utilisé
  par Magic Cards) ; images par défaut : 10 animaux OpenMoji (libres) et **9 à risque** (chouette, crabe, écureuil,
  hérisson, lapin, méduse, poulpe, renard, tortue : « Free Fundation », source non dite ; aussi utilisées par Blocks,
  Casse-Brique, Ninja, Scratch Card, Magic Cards, Cream Pie, Portrait Bubbles) ; lettres et chiffres : JPG noirs sur
  blanc sans originalité (OK).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. Un dos de carte dessiné pour le projet (script), au style des menus, à la place de `red-card-game.png`
      dans Memory (Magic Cards le garde jusqu'à sa revue).
    - I2. Remplacer les 9 images par défaut à risque par des animaux OpenMoji (CC BY-SA 4.0, même méthode que les
      10 déjà remplacés, `make_default_images.py`), mêmes noms de fichiers : profite aux 8 jeux qui les utilisent.
    - I3. Lettres et chiffres dessinés par le jeu (police Fredoka, grande, centrée sur la carte) au lieu des JPG.
    - I4. « Mes images » : une variante `UserImagesVariant` (dossier `files/images/Memory`), au lieu des dossiers
      partagés `magiccards` et `default` (pas de migration).
  - Graphisme
    - G1. Fond des menus ; cartes arrondies avec un espace entre elles (`GameStyle.targetCard`), dos dessiné, face
      sur carte blanche.
    - G2. Anneau de fixation de la couleur principale autour de la carte (comme Where Is It), au lieu de
      l'indicateur gris.
    - G3. Retournement animé ; paire juste : contour vert, puis les deux cartes s'effacent ; paire fausse : contour
      « danger » un instant, puis elles se retournent (et dans les versions visibles, le même contour).
    - G4. Une pastille « Paires : 2 / 6 » ; nouvelles vignettes.
  - Gameplay
    - P1. **Une seule carte au menu « Memory » au lieu de 6** (comme les petits chevaux) : dans la fenêtre des
      variantes, en haut le contenu (Images, Lettres, Chiffres, Mes images), les tailles et Dynamique au milieu, en
      bas « Cartes cachées » ou « Cartes visibles ».
    - P2. Corriger D1 (les 3 dernières manches) et D2 (plus de touche Espace).
    - P3. Laisser la deuxième carte visible 1,5 s au lieu de 1 s avant qu'elle se retourne (à confirmer).
    - P4. Statistiques : un but à atteindre par paire de la manche, atteint quand elle est trouvée (colonnes
      inchangées).
- **Décisions du mainteneur (30/10/2026)** : tout validé ✔ ; une seule carte ; 1,5 s ; lettres dessinées.
- **Fait (30/10/2026)** :
  - Une seule entrée « Memory » (les 5 autres retirées du catalogue : 71 entrées) ; variantes `GroupedGameVariant`
    (nouvelle, `gazeplay-games-commons`, relue par `IGameVariant.toGameVariant`) : en haut Images, Lettres,
    Chiffres, Mes images ; les tailles 2 × 2, 2 × 3, 3 × 2, 3 × 4, 4 × 3 et Dynamique ; en bas Cartes cachées,
    Cartes visibles (48 variantes). `GameVariantDialog` range toute variante à deux groupes ainsi (boutons radio en
    haut et en bas). « Mes images » : dossier `files/images/Memory` (`UserImagesVariant.folderOf` connaît la
    nouvelle variante), ligne dans les réglages ; plus de lecture des dossiers partagés `magiccards` et `default`.
  - `Memory` et `MemoryCard` réécrits : fond des menus, cartes arrondies espacées (jusqu'au carré quand il y a de
    la place), dos dessiné par le jeu (couleur principale, cadre, « ? ») au lieu de `red-card-game.png` (gardé pour
    Magic Cards), lettres et chiffres en Fredoka, anneau de fixation de la couleur principale, retournement animé,
    cadre vert pour une paire, « danger » pour une paire fausse (aussi en cartes visibles), 1,5 s avant de se
    retourner, pastille « Paires : n / N ». Plus de touche Espace. Dynamique : les 3 dernières manches.
  - Statistiques : un but à atteindre par paire de la manche, atteint quand elle est trouvée (colonnes inchangées).
  - Images par défaut : chouette, crabe, écureuil, hérisson, lapin, méduse, poulpe redessinés d'après OpenMoji
    (mêmes noms) ; `renard.png` et `tortue.png` (un second renard, une seconde tortue) remplacés par `penguin.png`
    et `frog.png` (`make_default_images.py`). Les versions sans fond du labyrinthe ne changent pas. Crédits : licence
    affichée, `gradle/images/README.txt`, inventaire.
  - Rejeu : les parties des 6 anciens jeux sont rejouées par Memory avec la variante correspondante
    (`ReplayingGameFromJson.formerGame`) ; l'ordre des cartes d'une ancienne partie n'est pas retrouvé (autre tirage).
  - Vignette refaite ; les 5 autres vignettes retirées. Aide du jeu (`data/help/games/Memory-*.txt`).
- **Vérifications** : `build` : **607 tests, 0 échec** ; `MemoryTest` (7), `ReplayingGameFromJsonTest`, `GameHelpTest` ;
  `allGamesTest -PallGamesOnly=Memory -PallGamesRepeat=5` réussi ; photos du jeu sans fenêtre.
  **Reste** : l'essai réel du mainteneur.
- **Commit** : le 30/10/2026, avec la nouvelle fenêtre des variantes des jeux revus (`feat(memory)`), à la demande du mainteneur.

### Sounds Of Life (`SoundsOfLife`, code `soundsoflife`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, 371 lignes) : une scène (Ferme, Jungle, Savane, Mer) avec des
  animaux ou des objets ; une fixation sur l'un d'eux joue un de ses sons (à tour de rôle) et le fait se balancer.
  Rien à gagner : on explore (un but atteint par son joué). Scènes décrites par `elements.json` (image, échelle,
  position, sons).
- **Défauts vus (faits, lecture du code)** :
  - D1. **Rejeu** : `replayGame` n'utilise pas la graine enregistrée (nouveau tirage) : le premier son de chaque
    élément change au rejeu.
  - D2. **Fond désactivé dans les réglages** : les éléments sont placés d'après le fond, qui n'est alors jamais
    placé ; ils sont décalés vers le haut et la gauche (sauf si l'image tient exactement la zone de jeu).
  - D3. Une sortie du regard avant toute entrée lève une exception (`progressTimeline` nul) ; indicateur de
    fixation gris au centre de l'image.
  - D4. Pas de nom de l'animal : on entend le son sans savoir ce que c'est (pas d'aide à l'apprentissage).
  - D5. `farm/background.jpg` n'est pas utilisé (le jeu lit `background.png`) ; les sources `.svg` de la mer sont
    livrées avec les `.png`.
- **Images et sons (licences)** : **à risque** : décors et animaux de la ferme, de la jungle et de la savane (Téo
  Blachère, 2019, sans source ; style de banque d'images, le décor de la ferme porte le nom de Danilo Sanino,
  illustrateur de Shutterstock, dans ses métadonnées). Mer (Iluvrog, 2021, avec ses `.svg`) : source non dite, peut-être
  dessinée par l'auteur. **Sons non inventoriés** : 47 `.wav` sans source (la vache, le cochon, le mouton, l'éléphant, le
  lion, le serpent, le coq ou la poule ont un équivalent libre déjà vérifié dans Where Is The Sound).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images et sons
    - I1. Animaux d'OpenMoji (CC BY-SA 4.0), comme Memory : vache, poule, cheval, cochon, coq, mouton ; éléphant,
      grenouille, léopard (pour le jaguar), singe, perroquet, serpent ; crocodile, lion, et pour la hyène et
      l'autruche (pas d'émoji) : zèbre et girafe, ou d'autres animaux de la savane qui ont un son libre.
    - I2. Décors dessinés pour le projet (script, comme la mare de Frogs Race) : ferme, jungle, savane ; la mer
      aussi, ou gardée si son auteur confirme qu'il l'a dessinée.
    - I3. Sons : reprendre ceux de Where Is The Sound déjà vérifiés (vache, poule, cochon, mouton, éléphant, lion,
      serpent) ; chercher les autres sur Commons ou La Sonothèque (CC0, CC BY), crédités ; inventaire des sons du jeu.
  - Graphisme
    - G1. Anneau de fixation de la couleur principale autour de l'animal, à la place de l'indicateur gris.
    - G2. Quand le son joue, le **nom de l'animal** s'affiche dans une pastille au-dessus de lui (traduit).
  - Corrections
    - P1. D1 (graine du rejeu), D2 (placement sans fond), D3 (exception) ; retirer `farm/background.jpg` et les
      `.svg` inutiles.
- **Questions au mainteneur** : I1 (hyène et autruche : les remplacer par d'autres animaux ?) ; I2 (la mer :
  redessiner, ou garder ?) ; G2 (le nom affiché vous convient-il ?)
- **Réponses du mainteneur (30/10/2026)** : tout est validé ; zèbre et girafe ; la mer redessinée ; le nom affiché
  convient.
- **Fait (30/10/2026)** :
  - Images (I1, I2) : les 4 décors dessinés par `gradle/images/make_soundsoflife_images.py` (ferme avec grange et
    clôture, jungle, savane avec point d'eau, fond marin), qui place aussi les animaux et écrit `elements.json` ;
    21 animaux OpenMoji (CC BY-SA 4.0). Scènes : Ferme (mouton, vache, cheval, cochon, coq, poule), Jungle (singe,
    perroquet, éléphant, léopard, serpent, grenouille), Savane (lion, zèbre, girafe, crocodile), Mer (dauphin,
    baleine, poisson, pieuvre, crabe). Tous les anciens fichiers retirés (images, `.wav`, `.svg`, `background.jpg`).
  - Sons (I3) : un son MP3 par animal (raccourci à 8 s, normalisé, mono) : 15 de Wikimedia Commons (CC0, domaine
    public, CC BY 3.0/4.0, CC BY-SA 4.0), 6 repris de Where Is The Sound (vache, cochon, mouton, éléphant, serpent,
    lion). Auteur, licence et page de chacun dans `data/soundsoflife/CREDITS.txt`, livré avec le jeu ; crédits aussi
    dans `gradle/images/README.txt` et `gradle/templates/licence.txt`. **Les 15 sons de Commons ont été choisis
    d'après leur titre et leur description, sans écoute** : à écouter (le poisson, la pieuvre et le crabe sont des
    bruits d'eau et de bulles, le dauphin un sifflement accéléré, la girafe un bourdonnement grave).
  - G1 : anneau de la couleur principale autour de l'animal, qui se remplit pendant la fixation.
  - G2 : le nom de l'animal (traduit, 21 clés `SoundsOfLife_<animal>` en français et en anglais) s'affiche 3 s dans
    une pastille au-dessus de lui quand le son joue.
  - P1 : le rejeu reprend la graine de la partie (D1) ; les animaux sont placés au même endroit avec ou sans fond (D2) ;
    plus d'exception quand le regard sort avant d'être entré (D3).
  - Aide (`SoundsOfLife-fra.txt`, `-eng.txt`) : principe, comment jouer, rien à gagner, les 4 scènes. Vignette du
    menu : capture de la ferme (960 x 540). Inventaire : `inventaire-images.csv` et `INVENTAIRE_IMAGES.md` à jour.
- **Vérifications** : `SoundsOfLifeTest` (6 tests : fichiers de chaque scène, placement sans fond, mêmes sons au
  rejeu et nom affiché) ; `GameHelpTest` avec `SoundsOfLife` ; captures sans fenêtre de la ferme et de la mer
  (anneau et pastille vus) ; `allGamesTest -PallGamesOnly=SoundsOfLife -PallGamesRepeat=5` réussi ; `build` :
  **617 tests, 0 échec**, SpotBugs sans alerte. Reste à faire par le mainteneur : jouer, et écouter les sons.
