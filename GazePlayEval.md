# GazePlayEval.md — Fusion de GazePlay Eval

Sections 1 à 11 : repérage du 27/10/2026, branche `addClaudeCode` (après l'étape 37 de `MODERNIZATION.md`), écrit
**avant toute modification** : ce qu'elles décrivent du code est l'état d'avant la fusion. **La réalisation est en
section 12.** Section 13 : les deux modes « Jeux » et « Évaluations » au lancement (état des lieux et plan) ;
section 14 : leur réalisation. Légende :
**Fait** (lu dans le code ou un fichier), **Hypothèse** (déduit, non vérifié en exécution), **Risque**,
**Recommandation**, **Question** (décision du mainteneur).

Ici, « fusion » ne veut pas dire `git merge` : il s'agit de comprendre ce que font `gazeplayEval` et
`gazeplayEvalTest`, de garder ce qui sert, et de faire jouer dans GazePlay Learning les évaluations créées avec le
site **GazePlay Eval**.

---

## 1. Sources examinées

| Source | Emplacement | État |
|---|---|---|
| Site GazePlay Eval (version à jour) | `C:\Users\arrig\Documents\Tests\GazePlay-Eval`, branche `addClaudeCode`, commit `ce72839` (16/10/2026). En ligne : https://lig-gazeplay.imag.fr/#/home | Angular 21, sans serveur. `CLAUDE.md`, `README.md`, guide intégré (`src/app/pages/guide/`) |
| Export du site | `services/download/download.service.ts`, `shared/screenModel.ts`, `shared/saveModel.ts`, `services/eval-validation/eval-validation.service.ts` | Lu en entier |
| Évaluation de référence | `Téléchargements\Tests sons et vidéos-gazeplayEval.zip` (47 Mo, 24 entrées) | Extraite dans le dossier temporaire de la session, rien n'a été copié dans le dépôt |
| Lecteur actif | Branche `GazePlayLearning`, dernier commit `f15e550e1` (15/07/2026), 64 commits depuis le point de séparation `4351ac74b` (16/01/2023) | Lue par `git archive`, sans changer de branche |
| Code dormant | `addClaudeCode` : `gazeplay-games/.../gazeplayEval` et `gazeplayEvalTest`, `EvalReport` (+ tests) | Ancien format `config.json`, inaccessible depuis l'étape 30 |

Autres dépôts trouvés à côté, **anciens, non retenus** : `GitHub\GazePlayEvalWebsite` (squelette Angular 15, un seul
commit), `GitHub\GazePlayEvalForm` (2023-2024), `GitHub\GazePlay-Eval-Simon` (copie de travail des stagiaires, en
retard de 65 commits sur son dépôt distant).

---

## 2. Le format produit par le site (le contrat à respecter)

### 2.1 Les deux fichiers du site

- **`.zip`** (bouton « Exporter le .zip », étape 5) : **le seul fichier destiné à GazePlay Learning.**
- **`.gpSave`** : même structure plus les noms d'écrans, les paramètres globaux et les cases masquées. Sert à
  rouvrir l'évaluation dans le site. **Fait** : GazePlay Learning n'a pas à le lire.

### 2.2 Structure du `.zip`

```
<nom de l'évaluation>/
├── evalData.json      tableau ordonné des écrans
├── evalInfo.json      nom, format des résultats, champs participant
├── images/            images des instructions et des cases
├── audio/             sons des instructions, des écrans de stimuli et des cases
└── videos/            vidéos des instructions
```

Nom du fichier : `<nom>-gazeplayEval.zip`. Les noms de fichiers gardent accents, espaces et apostrophes
(référence : `oeuf de Pâques.png`, `citrouille d'halloween.png`, `médecin(1).png`).

### 2.3 `evalInfo.json`

```json
{
  "Nom de l'évaluation": "Tests sons et vidéos",
  "Format choisi": "Csv&Xlsx",
  "Informations participant": ["Prénom", "Nom"]
}
```

- `Format choisi` : `Csv&Xlsx` (par défaut), `Csv` ou `Xlsx` (l'ancien `Xsl` n'est plus proposé).
- `Informations participant` : libellés des champs **à remplir dans GazePlay Learning à chaque passation** (guide du
  site, étape 2). Peut être vide.
- **Fait** : aucun numéro de version de format, ni ici ni dans `evalData.json`.

### 2.4 `evalData.json` : un objet par écran, clés en français

Le site construit chaque objet à partir d'un tableau positionnel (`values[]`) et d'une liste de clés
(`*ScreenConstKey`). Les clés sont donc **le contrat** : le `CLAUDE.md` du site interdit de les changer sans analyse
de compatibilité. Toutes les durées sont en **secondes, avec décimales possibles** (pas de 0,1 s dans l'éditeur).

**Transition** (`"Type": "transition"`)

| Clé | Type | Sens |
|---|---|---|
| `Mettre un temps avant passage à l'écran suivant` | booléen | passage automatique après un délai |
| `Combien de temps` | nombre (s) | ce délai |
| `Mettre une croix de fixation` | booléen | afficher une croix au centre |
| `Mettre un temps de fixation` | booléen | il faut fixer la croix pour passer |
| `Combien de temps de fixation` | nombre (s) | durée de fixation exigée |

**Instruction** (`"Type": "instruction"`)

| Clé | Type | Sens |
|---|---|---|
| `Mettre un temps avant passage à l'écran suivant` / `Combien de temps` | booléen / nombre (s) | comme la transition |
| `Ajouter un media` | booléen | l'écran affiche un contenu |
| `Type de media` | `Image` / `Video` / `Texte` / `Son` | |
| `Nom du fichier` | texte | nom du média, **ou le texte de la consigne si `Texte`** |
| `Mettre un temps de fixation` | booléen | **en réalité : « Ajouter un bouton pour lancer l'évaluation »** (le participant le fixe pour démarrer) |
| `Combien de temps de fixation` | nombre (s) | durée de fixation du bouton |

**Fait** : le nom de clé `Mettre un temps de fixation` ne correspond pas à son sens pour l'instruction (index 6 du
tableau du site : « bouton ? »). C'est un piège pour le lecteur Java, à documenter, pas à corriger côté site sans
accord.

**Stimuli** (`"Type": "stimuli"`)

| Clé | Type | Sens |
|---|---|---|
| `Nombre de lignes`, `Nombre de colonnes` | entiers ≥ 1 | grille |
| `Mettre un temps avant passage à l'écran suivant` / `Combien de temps` | booléen / nombre (s) | durée maximale de l'écran |
| `Combien de temps de fixation` | nombre (s) | fixation nécessaire pour sélectionner une case |
| `Choix de sélection` | `Tout` / `BonneReponse` | quelles cases comptent pour valider l'écran |
| `Combien à sélectionner` | entier ≤ lignes × colonnes | nombre de cases à valider pour passer |
| `Position stimuli aléatoire` | booléen | mélanger les cases |
| `Caché stimuli après selection` | booléen | masquer une case une fois choisie |
| `Mettre un son` / `Nom du fichier` | booléen / texte | son joué sur l'écran |
| `Liste des stimuli` | objet `{"0": {...}, "1": {...}}` | cases, en ordre ligne par ligne |

Une case : `{"imageName": "...", "soundName": "...", "goodAnswer": true|false}` (image et son facultatifs ; le son
est joué quand le participant regarde la case).

**Faits sur la grille** :
- Les clés de `Liste des stimuli` sont des nombres écrits en texte (`"0"` … `"n-1"`).
- L'éditeur garde exactement lignes × colonnes cases (`checkStimuliCells`), y compris les **emplacements vides**
  (`hidden: true`). Mais l'export du `.zip` **retire `hidden`** : un emplacement vide devient une case sans image ni
  son, impossible à distinguer d'une case volontairement vide.
- Le guide du site dit lui-même que le `.zip` ne garde pas « les emplacements vides des grilles ».

### 2.5 Ce que contient l'évaluation de référence

`Tests sons et vidéos` : 17 écrans, `Csv&Xlsx`, champs participant `Prénom`, `Nom`.

- 5 instructions **vidéo** (6 à 8 s, passage automatique, pas de bouton) ;
- 8 transitions (2 ou 3 s), dont 4 avec croix de fixation **sans** fixation exigée ;
- 4 grilles **2 × 2**, `Tout`, 1 case à sélectionner, fixation 2 s, une case par grille avec un son (mp3 ou wav) ;
  les trois premières **sans durée maximale**, la dernière avec 20 s.
- Toutes les valeurs numériques sont entières.

**Ce que la référence ne couvre pas** : instructions `Texte`, `Image`, `Son`, bouton de démarrage, croix à fixer,
`BonneReponse`, ordre aléatoire, masquage après sélection, son d'écran, grilles de plus de 10 cases, emplacements
vides, durées décimales, champs participant vides.
**Recommandation** : créer avec le site une seconde évaluation de test qui couvre ces cas (petits médias), pour les
tests automatiques et manuels.

---

## 3. Ce que fait `GazeplayEval` sur `GazePlayLearning`

**Fait principal : ce lecteur lit déjà le format du site.** Il ouvre
`<fileDir>\evals\<nom>\evalData.json` et lit les clés en français (`JTCconvert`, `parseTransition`,
`parseInstruction`, `parseStimuli`). La structure de dossiers attendue est celle du `.zip`.

### 3.1 Déroulement

- Import : bouton « Importer une évaluation » des réglages (`ImportGame`), qui décompresse le `.zip` dans
  `<fileDir>\evals\` (protection contre les chemins sortant du dossier : présente).
- Variantes du jeu `GazePlayEval` : un sous-dossier de `evals` = une évaluation (`GazeplayEvalVariantGenerator`).
- Lecture écran par écran (`generateScreen`) : transition (croix éventuelle, minuterie), instruction (image, texte,
  son ou vidéo, minuterie), stimuli (grille de `PictureCard`, son d'écran, durée maximale).
- Sélection d'une case : fixation de la durée demandée (`ProgressIndicator`), puis son de la case. Avec le réglage
  « Sélection avec son » (`SOUND_ENABLED`), les autres cases sont bloquées pendant le son.
- Fin : écran des statistiques habituel.

### 3.2 Contrôles de l'examinateur (clavier)

- `Espace` : passer à l'écran suivant.
- `P` : rejouer le son de l'écran de stimuli.

### 3.3 Modes de passation (réglage `EVAL_OPTIONS`, ajouté sur cette branche)

Après chaque écran terminé, un écran « Recommencer / Continuer » peut s'afficher :

| Valeur | Comportement |
|---|---|
| `Classique` (défaut) | pas d'écran intermédiaire |
| `Autonomie_Soutien_Oral` | l'examinateur clique sur un des deux boutons |
| `Autonomie_Soutien_Visuel` | le regard du participant (3 s, codé en dur) entoure un bouton en rouge, l'examinateur clique |
| `Autonomie_Total` | le regard du participant (3 s) déclenche le bouton |

« Recommencer » rejoue l'écran précédent sans remélanger les cases.

### 3.4 Résultats produits

- Dossier codé en dur : `C:/Users/<user>/Documents/GazePlayLearning_Eval/Eval<n>_<date>/`.
- `Stats_<date>.csv` (UTF-8 avec BOM, séparateur `;`), **une ligne par écran de stimuli** : `Stimuli` (noms des
  images, `image_vide` pour une case sans image), `ID écran` (index dans la liste de tous les écrans, à partir de 0),
  `Réponse attendue`, `Réponse donnée`, `Temps écran (ms)`.
- Une image de carte de chaleur du regard **par écran** (`<date-heure>-metricsGaze.png`, `Stats.screenHeatMapGaze`).
- Le fichier Excel est écrit par du code présent mais **commenté** (`createExcelFile`, `closeExcelFile`).
- Les statistiques habituelles de GazePlay (fin de partie).

### 3.5 Réglages ajoutés sur `GazePlayLearning`

`EVAL_OPTIONS`, `SOA_ENABLED` (son décalé de 2 s, utilisé par `gazeplayEvalTest`, masqué dans l'interface),
`FRAME_GAZE_POSITION` (cadence d'échantillonnage du regard de `gazeplayEvalTest`, masqué), libellé
« Sélection avec son » pour `SOUND_ENABLED`, bulle d'aide sur `DELAY_BEFORE_SELECTION_TIME`.

---

## 4. Ce que fait `GazePlayEvalTest` sur `GazePlayLearning`

**Fait : ce n'est pas un lecteur d'évaluations du site.** C'est un protocole d'entraînement « ANR » codé en dur :

- Lit l'**ancien** format positionnel `config.json` (1ʳᵉ ligne = nom ; puis par planche : lignes, colonnes, images,
  son, nombre d'images à voir, durée de fixation, durée d'affichage, mélange). La bonne réponse est reconnue au
  préfixe `C_` du nom d'image.
- Ordre des planches **mélangé** ; écran de consigne (`oculometrie.png`, `question.png`, son `sounds/Consigne.m4a`),
  croix entre les planches, **pause à mi-parcours** (`picto_pause.png`), écran de fin (`feux_artifice.png`,
  `sounds/Cloture.m4a`).
- Pendant chaque planche, une `Timeline` relève la position du regard à cadence fixe (`FRAME_GAZE_POSITION`) et
  l'écrit dans `StatsTraining_<date>.xlsx` : `Timepoint`, `X`, `Y`, identifiant de l'image cible, et les images en
  haut à gauche, haut à droite, bas à gauche, bas à droite (**grille 2 × 2 supposée**). Plus un `.txt` décrivant les
  planches.
- Dossier codé en dur : `C:/Users/<user>/Documents/ANR_Project/EvalTraining<n>_<date>/`.
- Clavier : `Entrée` lance la planche, `Retour arrière` termine, `Espace` passe la consigne ou la fin, `P` sort de la
  pause.

**Ce qui a de la valeur** : l'export « position du regard au cours du temps, avec ce qui est affiché où », qui
permet une analyse fine (de type regard pendant l'écoute). **Fait** : sur `addClaudeCode`, `Stats` enregistre déjà
chaque point de regard daté (`replayData.json`, format v4, sans répétitions) ; ce qui manque est le lien
point ↔ case de la grille.

---

## 5. État actuel sur `addClaudeCode`

- `gazeplayEval` (671 lignes) et `gazeplayEvalTest` (637 lignes) sont les versions **d'avant** `GazePlayLearning`
  (ancien `config.json` : `EvalName`, `Scores`, `Assets`, `Output`, `Anonymous`, `Profil`). **Fait : incompatibles
  avec le site.**
- `EvalReport` (export CSV UTF-16 et XLSX, extrait à l'étape 3) et `EvalReportTest` : liés à l'ancien format.
- Plus d'import d'évaluation (`ImportGame` retiré le 23/10/2026), plus de mode BERA, plus d'écran des scores Eval.
  Le dossier `files\evals` n'est plus créé. Les jeux Eval sont exclus du catalogue (`CatalogBasedGamesLocatorTest`).
  `evals-catalog.txt` est encore généré mais n'est plus lu.
- Gardés pour la fusion : `GazeplayEvalGameVariant`, la lecture des variantes Eval dans
  `IGameVariant.toGameVariant` (chemin `\\evals\\` codé en dur), les champs Eval de `Stats`,
  `SystemFileManager.showFile`, les réglages Eval de `Configuration` (`TRANSITION_TIME`, `QUESTION_TIME`, `FEEDBACK`,
  `SOUND_ENABLED`…), le code de `GameSizingComputer`.
- Ce que la modernisation apporte et que le lecteur doit utiliser : `IGameContext.getConfiguration()`, les modes
  d'entrée `mouse_control` / `tobii` / `tobii_and_mouse` (filtrage de la souris par `GameContextFactory`),
  `GazeEvent.getSampleTime()`, `Stats` par source (v4), le dossier `Documents\GazePlay_Learning`, la pause du regard
  et de la souris quand le menu de l'engrenage est ouvert (étape 37), la règle « la scène ne se modifie que sur le
  thread JavaFX, `PauseTransition` pour les délais ».

---

## 6. Écarts entre le site et le lecteur de `GazePlayLearning`

| Fonction du site | Lecteur `GazePlayLearning` | Constat |
|---|---|---|
| Structure du `.zip` | Lue telle quelle | **OK** |
| Durées décimales (`1.5`) | Lues par `getAsInt()` | **Fait** : Gson tronque (1,5 s → 1 s, 0,5 s → 0 s) |
| Ordre des cases | Clés triées **comme du texte** | **Fait** : à partir de 11 cases, `"10"` passe avant `"2"` : la grille est mélangée |
| Nombre de cases ≠ lignes × colonnes | Prend les 3 × (lignes × colonnes) dernières valeurs | **Risque** : liste plus courte → exception ; plus longue → mauvaises cases |
| Emplacements vides | Case `image_vide` encadrée et **sélectionnable** | **Fait** ; la donnée `hidden` n'est pas exportée par le site |
| `Caché stimuli après selection` | Lu, **jamais utilisé** | **Fait** |
| `BonneReponse` | Seules les bonnes réponses comptent ; les mauvaises restent enregistrées | Conforme au guide |
| Bouton de démarrage (instruction) | Seulement pour `Image` : on fixe **l'image elle-même** | **Fait** : ignoré pour `Texte`, `Son`, `Video` |
| Croix à fixer (transition) | Gérée (`ScreenCard`) | Voir défaut E1 |
| Son d'écran (stimuli) | Joué ; `P` le rejoue | OK |
| `Format choisi` | **Ignoré** : CSV seulement | **Fait** |
| `Informations participant` | **Ignoré** (aucun formulaire, `evalInfo.json` jamais lu) | **Fait**, alors que le guide du site l'annonce |
| Formats des médias | `Image` / `Media` JavaFX | **Risque** : le site accepte `image/*`, `video/*`, `audio/*` ; JavaFX ne lit ni WebP, ni WebM, ni OGG, ni FLAC (il lit PNG, JPEG, GIF, BMP, MP3, WAV, AAC/M4A, MP4 H.264) |

---

## 7. Défauts du lecteur `GazePlayLearning` repérés à la lecture

Aucun n'a été vérifié en exécution. Chaque point dit pourquoi il est probable.

- **E1 — Écran sauté.** `getScreenHeatmapGaze()` appelle déjà `afterScreenHeatMap()` (qui avance d'un écran et
  affiche le suivant). Or `ScreenCard` (croix ou image fixée) puis la touche `Espace` avancent **une seconde fois**.
  Effet probable : un écran sauté et deux écrans construits l'un sur l'autre. La référence n'emprunte pas ces
  chemins (aucune fixation exigée), sauf si l'examinateur appuie sur `Espace`.
- **E2 — Exception au premier écran.** `afterScreenHeatMap()` calcule `System.currentTimeMillis() -
  currentRoundStartTime`, un `Long` qui reste `null` tant qu'aucune grille n'a été affichée. Une croix à fixer ou
  `Espace` avant la première grille donne probablement une `NullPointerException`.
- **E3 — Colonnes du CSV décalées.** Le temps est ajouté pour **tous** les écrans passés par
  `afterScreenHeatMap()` (transitions, instructions), mais les autres colonnes seulement pour les grilles. Les
  lignes du CSV mélangent donc les temps d'écrans différents.
- **E4 — Unité fausse.** Quand la durée maximale d'une grille est atteinte, la colonne `Temps écran (ms)` reçoit
  la durée réglée **en secondes** (`get(4)`).
- **E5 — Regard ignoré en mode par défaut.** `PictureCard`, `ScreenCard` et `EvalOptionsCard` n'écoutent le regard
  que si `EYE_TRACKER` vaut exactement `tobii`, sinon la souris seule. Sur `addClaudeCode`, la valeur par défaut est
  `tobii_and_mouse` : le regard serait ignoré.
- **E6 — Blocage possible.** Une grille sans durée maximale (3 sur 4 dans la référence) ne se termine que par une
  sélection ou par `Espace` (voir E1).
- **E7 — Chemins Windows.** `"\\evals\\"`, `C:/Users/<user>/Documents/...` : faux ailleurs que sous Windows, et en
  dehors du dossier GazePlay (`Documents\GazePlay_Learning`).
- **E8 — Zones de regard.** La zone de détection d'une case (`createGazeArea`) mesure la moitié de la largeur de
  l'image et se décale selon la colonne (`firstPosition`). **Hypothèse** : la zone ne recouvre pas exactement
  l'image affichée ; à mesurer sur écran.
- **E9 — Minuteries pendant le menu de l'engrenage.** Les `Timeline` d'écran ne sont pas mises en pause quand le
  menu est ouvert (étape 37). Lié à la décision encore ouverte sur le temps de menu (`MODERNIZATION.md` 2an).
- **E10 — État partagé.** Beaucoup de champs `public` modifiés par `PictureCard`, `ScreenCard`,
  `EvalOptionsCard`, et du code mort (échantillonnage du regard, `listImages` jamais rempli) : difficile à tester.

---

## 8. Modifications de `GazePlayLearning` hors Eval

Le diff depuis le point de séparation touche 60 fichiers. Hors des deux paquets Eval :

| Modification | Recommandation |
|---|---|
| `Stats` : enregistre **soit** le regard **soit** la souris selon le réglage ; images `metrics*.png` et carte de chaleur de fin **commentées** (R12) | **Ne pas reprendre.** `addClaudeCode` enregistre les deux sources séparément (v2 à v4). Refaire seulement la carte de chaleur **par écran**, sur le `Stats` actuel |
| `getPosition()` ajouté à `GazeDeviceManager`, `TobiiGazeDeviceManager`, `PositionPollerRunnable` (champ public écrit par un autre thread) | **Ne pas reprendre** : le lecteur doit utiliser les `GazeEvent` |
| `SoundManager.stop()` | Équivalent à prévoir si besoin |
| Réglages `EVAL_OPTIONS`, `SOA_ENABLED`, `FRAME_GAZE_POSITION` et leur page | Reprendre `EVAL_OPTIONS` si les modes de passation sont gardés (question Q4) |
| `ImportGame` (bouton « Terminé », libellés) | Refaire un import propre au format du site |
| Jeu `FollowEmmanuel` (labyrinthes, 3 niveaux) | Hors Eval (question Q8) |
| `lib/EyeTribeJavaFx.jar`, dépendances et versions Gradle de 2023 | **Ne pas reprendre** (EyeTribe retiré, dépendances à jour) |
| Catalogues `FollowEmmanuel` et `OpenMemoryGame` dans `generateEvalsCatalog` | Ne pas reprendre |
| Images `blackCross*`, `whiteCrossMini`, `oculometrie`, `picto_pause`, `question`, `feux_artifice` | Reprendre celles qui servent (croix) |
| Traductions (`EvalOptions`, `SelectionWithSound`, `ImportEval`, `removeEval`…) | Reprendre celles qui servent |

**Recommandation** : pas de `git merge` de `GazePlayLearning` : les deux branches ont divergé depuis janvier 2023 et
la modernisation a réécrit `Stats`, la configuration, le lancement et le dossier de données. Un port à la main, guidé
par ce document, est plus sûr. La branche `GazePlayLearning` reste intacte comme référence.

---

## 9. Ce qu'on garde, ce qu'on laisse

**À garder (comportement, pas forcément le code)** :
- la lecture du `.zip` du site et le déroulement écran par écran ;
- la sélection par fixation avec indicateur de progression, le son des cases, le blocage pendant le son ;
- les contrôles de l'examinateur (`Espace`, `P`) ;
- les modes de passation (si Q4 les confirme) ;
- l'export par grille (stimuli affichés, attendus, choisis, temps) et la carte de chaleur par écran ;
- l'idée de `gazeplayEvalTest` : regard daté relié aux cases affichées, pour toute grille ;
- `GameSizingComputer`, `GazeplayEvalGameVariant`, l'approche testée d'`EvalReport` (CSV et XLSX).

**À laisser** :
- l'ancien format `config.json` (`gazeplayEval` actuel, `gazeplayEvalTest`), sauf si Q2 dit le contraire ;
- l'échantillonnage du regard par `Timeline` et `getPosition()` (les points existent déjà dans `Stats`) ;
- les modifications globales de `Stats`, les chemins codés en dur, les dépendances anciennes.

---

## 10. Organisation proposée

Chaque étape est vérifiée avant la suivante, et suivie dans ce fichier.

1. **Décisions** (section 11).
2. **Le contrat, en Java, sans JavaFX.** Un modèle typé (`EvalDefinition`, un type par écran) lu avec Gson, durées
   en `double`, cases triées par valeur numérique, nombre de cases vérifié, `Mettre un temps de fixation` interprété
   selon le type d'écran. Un rapport de validation qui suit celui du site (`EvalValidationService` : champs
   manquants, médias absents ou de format illisible par JavaFX). Tests unitaires sur le JSON de la référence et sur
   la seconde évaluation de test (sans les vidéos de 47 Mo dans le dépôt).
3. **Import.** Décompresser un `.zip` du site dans `Documents\GazePlay_Learning\files\evals\<nom>\` (dossier GazePlay
   actuel, pas de chemin Windows codé en dur) ; refus d'un chemin hors du dossier ; validation avant copie ; question
   avant d'écraser une évaluation du même nom ; liste et suppression. Interface : dans les réglages ou dans le futur
   menu (lié à la refonte du menu).
4. **Lecteur.** Une machine à états simple : un écran à la fois, une seule façon de passer au suivant (corrige
   E1 à E3 par construction), `PauseTransition`, écoute des `GazeEvent` et de la souris en laissant le mode d'entrée
   filtrer (corrige E5), masquage après sélection, bouton de démarrage pour tous les types d'instruction, emplacements
   vides non sélectionnables (si le site les exporte, Q6), minuteries en pause avec le menu de l'engrenage (selon la
   décision 2an).
5. **Passation et résultats.** Formulaire participant d'après `evalInfo.json` avant le premier écran ; résultats
   dans le dossier GazePlay, au format choisi (CSV et/ou XLSX), **avec `formatVersion`** ; temps en millisecondes ;
   une ligne par grille et par essai (`Recommencer` compris, numéroté) ; carte de chaleur par écran depuis le
   `Stats` actuel ; en option, regard daté relié aux cases (héritage de `gazeplayEvalTest`).
6. **Lancement.** Une JVM par passation, comme les jeux (`--game GazePlayEval --variant <nom>`), et l'entrée dans le
   menu.
7. **Nettoyage.** Retirer l'ancien code Eval (`config.json`), `gazeplayEvalTest` et ce qui ne sert plus (selon Q2).
8. **Vérification.** Tests unitaires ; passation complète sans fenêtre (Monocle) d'une évaluation courte ; ajout au
   job `all-games` ; passation réelle de la référence avec le Tobii Eye Tracker 5.

---

## 11. Questions pour le mainteneur

- **Q1 — Site comme référence.** Le dépôt `Documents\Tests\GazePlay-Eval` (branche `addClaudeCode`) et le site en
  ligne sont-ils bien la version de référence du format ?
- **Q2 — Ancien format.** Existe-t-il encore des évaluations au format `config.json` (ancien Eval, protocole ANR de
  `gazeplayEvalTest`) qu'il faut pouvoir jouer ? Sinon, on ne garde que le format du site.
- **Q3 — Protocole ANR.** Les écrans fixes de `gazeplayEvalTest` (consigne sonore, pause à mi-parcours, fin) et son
  export du regard échantillonné sont-ils encore utilisés ? Le site sait déjà faire consigne et transitions.
- **Q4 — Modes de passation.** Garder les quatre modes (`Classique`, soutien oral, soutien visuel, autonomie totale) ?
  Leur fixation de 3 s doit-elle être réglable ? Doit-on noter dans les résultats qu'un écran a été recommencé ?
- **Q5 — Résultats.** Emplacement (dans `Documents\GazePlay_Learning`, ou un dossier séparé comme
  `GazePlayLearning_Eval`) ; colonnes attendues ; faut-il aussi les résultats « de jeu » habituels de GazePlay ?
  (Rejoint la question ouverte « format officiel des exports, règle d'arrêt, RGPD ».)
- **Q6 — Évolutions du site.** Accepte-t-on de modifier l'export du site, de façon **additive** (nouvelles clés
  seulement, anciennes conservées) : `formatVersion`, emplacements vides exportés, restriction aux formats de
  médias lisibles par JavaFX ?
- **Q7 — Données personnelles.** Les champs participant sont-ils écrits en clair dans les résultats ? Anonymisation
  possible ? (RGPD, question déjà ouverte.)
- **Q8 — `FollowEmmanuel`.** Ce jeu de labyrinthe de `GazePlayLearning` est-il à reprendre ? (Le mode « emmanuel » a
  été retiré le 21/10/2026.)
- **Q9 — Règle d'arrêt.** L'ancien Eval s'arrêtait après 5 erreurs consécutives ; le site n'a pas cette notion.
  Est-elle abandonnée ?

---

## 12. Réalisation (27/10/2026)

### 12.1 Décisions du mainteneur

- Objectif : une évaluation exportée par le site s'importe, se joue et produit ses résultats dans GazePlay Learning
  modernisé, **sans rien casser pour les autres jeux**. Carte blanche sur la façon de faire.
- **Q2** : plus d'évaluation à l'ancien format `config.json` → l'ancien lecteur, `EvalReport` et `gazeplayEvalTest`
  sont retirés.
- **Q4** : les quatre modes de passation sont gardés (ils adaptent le style de l'évaluation).
- **Q5** : résultats au choix de Claude, avec les informations de la souris et du regard (entrées, sorties,
  validations…).
- **Q6** : au choix de Claude (voir 12.7).
- Q1, Q3, Q7, Q8, Q9 : non tranchées, voir 12.8.

### 12.2 Ce qui a été fait

Un **portage à la main** (pas de `git merge`) : le comportement de `GazePlayLearning` est repris et réécrit sur le
code modernisé ; la branche `GazePlayLearning` n'a pas été touchée.

| Partie | Où | Rôle |
|---|---|---|
| Format du site | `gazeplay-games-commons`, `net.gazeplay.commons.eval` : `EvalScreen`, `EvalDefinition`, `EvalReader`, `EvalProblem` | Lit `evalData.json` et `evalInfo.json`, sans JavaFX. Durées décimales, cases triées comme des nombres, cases manquantes ou sans média = emplacements vides, clés inconnues ignorées. Vérifie la présence et le format des médias, signale les écrans que seul l'examinateur peut terminer. Problèmes traduits (clé + arguments) |
| Import | `EvalImporter`, `EvalLibrary` | Extrait le `.zip` dans un dossier temporaire, le lit, **puis seulement** le met en place (question avant de remplacer une évaluation du même nom). Chemins hors du dossier refusés, taille et nombre de fichiers limités, noms UTF-8 (repli IBM437), `__MACOSX` ignoré, archive avec ou sans dossier racine |
| Réglages | `gazeplay-core`, `EvalSettings` + section « Évaluations (GazePlay Eval) » de `ConfigurationContext` | Liste des évaluations importées (suppression), import, mode de passation, délai avant sélection, blocage par le son, dossier des résultats |
| Jeu | `gazeplay-games`, `gazeplayEval` : `GazeplayEval`, `EvalTarget`, `EvalSessionRecorder`, `EvalResultsWriter` | Le lecteur, les cibles regard/souris, l'enregistrement de la passation et l'écriture des résultats |
| Catalogue | `gazeplay-core/build.gradle` | GazePlay Eval est un jeu du catalogue normal ; `evals-catalog.txt` (plus lu) n'est plus généré |
| Variante | `GazeplayEvalGameVariant` | Une variante par évaluation importée, forme persistante `GazeplayEvalGameVariant:<nom>` (aller-retour testé, accents et apostrophes compris) |

**Dossiers** : évaluations importées dans `Documents\GazePlay_Learning\evaluations\<nom>\` (communes à tous les
profils) ; résultats dans **`Documents\GazePlayLearning_Eval\Eval<n>_<aaaa-mm-jj>\`** (décision du mainteneur du
27/10/2026, voir 12.4 ; le bouton « voir dossier » des réglages y mène).

### 12.3 Déroulement d'une passation

1. Menu → GazePlay Eval → choix de l'évaluation (une JVM par passation, comme les autres jeux). Sans évaluation
   importée, le jeu explique comment en importer une ; une évaluation devenue illisible affiche ses erreurs.
2. **Formulaire participant** (champs de `evalInfo.json`), rempli par l'examinateur au clavier, `Entrée` pour
   commencer. Les lettres tapées n'atteignent pas l'écran de jeu (la touche `Q` ne quitte pas pendant la saisie).
3. **Écrans dans l'ordre** :
   - transition : croix de fixation éventuelle, qui peut devoir être fixée ;
   - instruction : texte, image, vidéo ou son ; **bouton « Commencer » à fixer pour tous les types** (le site
     l'appelle « bouton pour lancer l'évaluation ») ;
   - grille : images et/ou sons, sélection par fixation (indicateur de progression), son de la case, son de l'écran,
     règle `Tout` / `BonneReponse`, masquage après sélection, positions aléatoires (les emplacements vides restent en
     place, le même ordre est gardé quand la grille est recommencée), durée maximale.
4. **Après chaque grille**, selon le mode de passation (réglage `EVAL_OPTIONS`) : rien (classique), ou un écran
   « Recommencer / Continuer » : choisi par l'examinateur (soutien oral : la souris ou le clavier, le regard du
   participant sur les boutons est enregistré), désigné du regard puis confirmé par l'examinateur (soutien visuel),
   ou choisi du regard (autonomie totale). La fixation des boutons est celle du réglage « durée de fixation » de
   GazePlay (autrefois 3 s codées en dur).
5. Fin : « Fin de l'évaluation », résultats écrits, puis l'écran de statistiques habituel.

**Touches de l'examinateur** : `Espace` écran suivant, `P` rejouer le son (ou la vidéo) de l'écran, `R` ou `←`
recommencer, `C` ou `→` continuer, `Entrée` confirmer la proposition du participant (sinon continuer). Elles
remplacent la souris en mode « regard seul », où la souris est bloquée dans la zone de jeu.

**Corrections par construction** des défauts relevés en section 7 : un écran ne se termine qu'à un seul endroit, qui
ignore les minuteries et les rappels des écrans déjà finis (E1, E2, E3) ; temps toujours en millisecondes (E4) ;
regard **et** souris écoutés, le mode d'entrée de GazePlay faisant le tri (E5) ; dossier GazePlay, aucun chemin
Windows codé en dur (E7) ; la cible d'une image est l'image affichée plus une petite marge (E8) ; minuteries, médias
et sélection **en pause** pendant que le menu de l'engrenage est ouvert, avec les événements `menu_ouvert` /
`menu_ferme` dans les résultats (E9 ; propre à Eval, la décision 2an pour les autres jeux reste ouverte).

**Réglages** (nouvelles clés, valeurs par défaut sans effet sur les autres jeux) : `EVAL_OPTIONS` (défaut
`Classique`), `EVAL_SOUND_BLOCKS_SELECTION` (défaut vrai : le son d'une case choisie bloque les autres sélections ;
clé propre à Eval, car `SOUND_ENABLED` est aussi lu par Simon). `DELAY_BEFORE_SELECTION_TIME` (défaut 1 s, lu par
Eval seulement) est maintenant modifiable dans les réglages.

### 12.4 Résultats d'une passation (format version 1)

**Dossier** (décision du mainteneur du 27/10/2026) : `GazePlayLearning_Eval` dans Documents (créé s'il n'existe
pas ; le dossier personnel s'il n'y a pas de Documents), puis un dossier par passation **`Eval<n>_<aaaa-mm-jj>`** :
le numéro repart de 1 chaque jour et augmente avec les passations du jour (`Eval1_2026-10-27`, `Eval2_2026-10-27`…),
toutes évaluations confondues. Ce sont l'emplacement et les noms de la version `GazePlayLearning` : les dossiers
déjà présents (`Eval1_2026-06-29`…) restent à côté, intacts. Le nom de l'évaluation est dans `session.json` et le
`.xlsx`. La création du dossier est atomique (deux passations simultanées reçoivent deux numéros). Pour les tests,
le dossier Documents est remplacé (`gazeplay.documents`, `build.gradle`) : ils n'écrivent jamais dans le vrai.

CSV en UTF-8 avec BOM, séparateur `;` (ouverture directe dans un tableur français). Temps en ms depuis le début de la session, positions en pixels de la
zone de jeu. Sources : `regard`, `souris`, `clavier`, `systeme`.

| Fichier | Contenu |
|---|---|
| `session.json` | `formatVersion` (1), évaluation, début, fin, durée, `terminee` (faux si la partie a été quittée), **participant**, mode de passation, mode d'entrée, délai avant sélection, blocage par le son, fixation des choix, graine du hasard, taille de la zone de jeu, dossiers |
| `resultats.csv` (et/ou `.xlsx`, selon le « Format choisi » du site) | Une ligne par écran affiché (et par essai) : écran, essai, type, début, fin, durée, fin de l'écran (`validé`, `temps écoulé`, `délai écoulé`, `fixation`, `passé par l'examinateur`, `interrompu`) ; pour les grilles : lignes, colonnes, règle, à sélectionner, stimuli affichés (`L1C2:image.png`, ordre affiché), réponses attendues, réponses données (dans l'ordre), leurs sources, leurs temps depuis le début de l'écran, bonnes et mauvaises réponses, validé, première réponse |
| `evenements.csv` (et feuille « Événements » du `.xlsx`) | Chaque événement : `debut_session`, `debut_ecran`, `fin_ecran`, **`entree` et `sortie`** d'une cible (case, croix, bouton) par le regard ou la souris, avec position, `selection` (bonne ou mauvaise réponse), `fixation_validee`, `debut_son`/`fin_son`, `debut_video`/`fin_video`, `son_case`, `son_ecran`, `touche`, `ecran_choix`, `proposition`, `choix_option`, `menu_ouvert`/`menu_ferme`, `erreur_media`, `fin_session` |
| `echantillons.csv` | Chaque position du regard (une par échantillon de l'appareil, datée à son acquisition) et chaque déplacement de la souris, avec la cible sous le point (héritage de `gazeplayEvalTest`, pour toute grille). Toujours en CSV |
| `heatmaps\ecran-NN-<type>-essai-K-regard.png`, `…-souris.png` | **Carte de chaleur de chaque écran** (transitions, consignes et grilles), à sa fin : l'image de l'écran, la carte du regard ou de la souris par-dessus, et la légende. Même calcul et même rendu que les cartes de GazePlay (grille de 400 colonnes, `HeatMapGrid` ; couleurs et opacité des réglages). Une source sans position sur l'écran n'a pas de carte ; un écran sans aucune position a son image seule (`ecran-NN-<type>-essai-K.png`). Un écran quitté en cours a aussi la sienne |

Les fichiers habituels de GazePlay (`replayData.json` v4, cartes de chaleur de toute la partie, `info-game.csv`)
sont aussi écrits, dans `statistics\GazePlayEval\<date>\`, par `Stats`, qui n'a pas été modifié (seul un accesseur
`Stats.isConfigMenuOpen()` a été ajouté ; `HeatMapGrid` est devenu public, sans changement de calcul).

**Données personnelles** : les champs participant sont écrits en clair dans `session.json` et `resultats.xlsx`. Le
nom du dossier ne contient que la date. Voir Q7.

### 12.5 Ce qui a été retiré

L'ancien lecteur Eval (`config.json`), `PictureCard`, `RoundDetails`, `GameSizing*`, `EvalReport` et
`EvalReportTest`, tout le paquet `gazeplayEvalTest`, la tâche `generateEvalsCatalog`, la traduction
`GazePlayEvalTest`. Gardés, inutilisés : les champs de scores BERA de `Stats` (format interne, sans effet).

### 12.6 Vérifications

- Tests ajoutés : `EvalReaderTest` (9, dont la lecture du JSON de « Tests sons et vidéos »), `EvalImporterTest` (7),
  `GazeplayEvalGameVariantTest` (1), `EvalResultsWriterTest` (4), `GazeplayEvalTest` (numérotation des dossiers
  `Eval<n>_<date>`, emplacement dans le dossier de test, et 4 passations complètes sans fenêtre : grille
  sélectionnée à la souris jusqu'aux fichiers de résultats et aux cartes de chaleur ; formulaire
  participant, touche Espace et partie quittée ; soutien oral avec « Recommencer » puis « Continuer » ; aucune
  évaluation importée). `ConfigurationContextTest` et `CatalogBasedGamesLocatorTest` mis à jour.
- Le reste est donné en 12.9 (suite complète, `smokeTest`, `allGamesTest`, import du vrai zip).

### 12.7 Le site GazePlay Eval (Q6)

Choix : **aucune modification du site n'est nécessaire**, et aucune n'a été faite. GazePlay Learning s'adapte au
format actuel :
- les emplacements vides (non exportés) sont reconnus comme les cases sans image ni son ; la clé `hidden` est lue si
  le site l'exporte un jour ;
- les clés inconnues (par exemple un futur `formatVersion`) sont ignorées ;
- les médias illisibles par JavaFX (WebP, WebM, OGG, FLAC…) sont refusés **à l'import**, avec le nom du fichier et les
  formats acceptés.

Améliorations possibles du site, toutes additives (à faire dans son dépôt, avec ses tests d'aller-retour) :
`formatVersion` dans `evalInfo.json` ; limiter les sélecteurs de fichiers (`image/*`, `video/*`, `audio/*`) aux
formats lus par GazePlay, ou l'avertir à l'étape 5 ; renommer dans l'interface le réglage « Mettre un temps de
fixation » des instructions, qui est en réalité le bouton de démarrage (la clé exportée, elle, ne doit pas changer).

### 12.8 Reste ouvert

- **Essai réel** avec le Tobii Eye Tracker 5 et l'évaluation « Tests sons et vidéos » (vidéos, sons, regard) : à faire
  par le mainteneur (réglages → Évaluations → Importer, puis menu → GazePlay Eval). Les vidéos et les sons n'ont pas été lus dans les tests sans fenêtre (Monocle n'a pas de
  sortie média) : leur code d'erreur est prévu (`erreur_media`, l'écran continue).
- Q7 (anonymisation), Q9 (règle d'arrêt) : non tranchées ; rien n'est prévu pour l'instant.
- Q3 : l'export du regard échantillonné de `gazeplayEvalTest` est repris pour toutes les grilles
  (`echantillons.csv`) ; ses écrans fixes (consigne sonore, pause, fin) se construisent maintenant avec le site.
- Q8 (`FollowEmmanuel`) : non repris.

### 12.9 Vérifications faites (27/10/2026) et changements hors Eval

- `./gradlew build` : réussi, **449 tests, 0 échec** (451 après le changement de dossier des résultats : test de la
  numérotation `Eval<n>` et de l'emplacement), SpotBugs sans alerte (les alertes de chemins pouvant être nuls
  sur les nouvelles classes ont été corrigées).
- `smokeTest` : 3/3. `allGamesTest` : **76/76** (les 75 jeux d'avant + GazePlay Eval, lancé sans évaluation
  importée : il affiche le message d'import et tourne sans erreur).
- **Import du vrai zip** « Tests sons et vidéos » (Téléchargements, 47 Mo) par `EvalImporter`, avec un test temporaire
  supprimé ensuite : aucun problème, 17 écrans, 20 fichiers, champs « Prénom » et « Nom », format CSV et XLSX.
- **Pas encore fait** : la passation réelle de ce zip dans l'application, avec les vidéos, les sons et le Tobii
  Eye Tracker 5.

Changements hors Eval, tous vérifiés par la suite complète et `allGamesTest` :
- `GazePlayFxApp` : lancé sans `--variant`, un jeu **sans aucune variante** démarre directement, comme le menu le
  faisait déjà (`GameMenuController.onGameSelection`), au lieu d'ouvrir une fenêtre de choix vide. Aucun effet sur
  les jeux qui ont des variantes.
- `Math101` : **défaut existant corrigé**, révélé par `allGamesTest` (Division) : un premier tirage avec un diviseur 0
  et un dividende non nul calculait `x % 0` et faisait planter le jeu au lancement, au hasard. Le diviseur 0 est
  maintenant retiré.
- `StatsTest.shouldAddRoundDurationToReportOnNextRound` : test **instable** (égalité à la milliseconde entre deux
  mesures d'horloge prises de part et d'autre de l'appel), échoué une fois puis réussi à la relance sans changement ;
  il vérifie maintenant un encadrement. `Stats` n'a pas changé.
- `Stats` : ajout de l'accesseur `isConfigMenuOpen()`. `Configuration` : deux clés nouvelles. `GazePlayDirectories` :
  `getEvaluationsFolder()`. Aucune valeur ni comportement existant modifié.

Commit local `5751834b6` (27/10/2026), **non poussé** : le mainteneur teste d'abord.

---

## 13. Deux modes au lancement : « Jeux » et « Évaluations » (état des lieux et plan, 27/10/2026)

Écrit avant le code (demande du mainteneur : regarder, puis documenter la façon de faire). Les réponses du mainteneur
aux questions Q-M1 à Q-M6 sont en 13.6 et **priment sur 13.3 et 13.4** ; **la réalisation est en section 14**.

### 13.1 La demande

Au lancement, **avant le choix du profil**, un écran propose deux modes :
- **Jeux** : GazePlay avec ses jeux et leurs réglages, **sans** les évaluations ni leurs réglages ;
- **Évaluations** : seulement les évaluations (GazePlay Eval et « Eval Test ») et leurs réglages, **rien** qui
  concerne les jeux.

### 13.2 État actuel (faits)

**Démarrage** (`GazePlayFxApp.start`) :
- sans option : `gazePlay.goToUserPage()` → page des profils (`UserProfileContext`) ; le choix d'un profil appelle
  `gazePlay.onReturnToMenu()` → menu des jeux (`HomeMenuScreen`) ;
- avec `--user` ou `--default-user` : pas de page des profils, directement le menu (raccourcis de bureau) ;
- avec `--game` (+ `--variant`) : directement le jeu. C'est ainsi que le menu lance chaque partie dans **sa propre
  JVM** (`GameMenuController.createBuilder`).

**Navigation** : tout passe par `GazePlay` (une instance par JVM) : `goToUserPage`, `onReturnToMenu`,
`onDisplayConfigurationManagement`. Les écrans sont créés par `CompositionRoot` (`newHomeMenuScreen`,
`newConfigurationContext`, `newUserProfileContext`), et le menu et les réglages sont gardés en cache
(`loadedHomePage`, `loadedSettingsPage`), remis à zéro par `goToUserPage`.

**La JVM d'une partie se ferme à la fin** : le bouton « accueil » de l'écran de statistiques appelle
`System.exit` (`StatDisplayUtils.closeStatsWindow`), et le menu se réaffiche dans la JVM d'origine. **Le mode n'a donc
d'importance que dans la JVM du menu** : une partie (jeu ou évaluation) n'a pas besoin de le connaître.

**Un seul catalogue** (`games-catalog.txt`, 76 entrées), lu par `CatalogBasedGamesLocator`, utilisé à deux
endroits : le menu (`HomeMenuScreen`, ligne 114) et la création de raccourcis des réglages
(`ConfigurationContext.buildVariantShortcutMaker`, ligne 833). Rien ne distingue aujourd'hui une évaluation d'un jeu,
sauf son nom (`GazePlayEval`).

**Menu des jeux** (`HomeMenuScreen`) : logo, cartes des jeux (favoris d'abord), barre de recherche, filtres par
catégorie, statistiques par catégorie (`GamesStatisticsPane`), boutons : quitter, profils (« déconnexion »),
réglages, rejouer un `replayData.json`, flèches de défilement.

**Réglages** (`ConfigurationContext.buildConfigGridPane`) : une seule grille de 9 rubriques. Classement selon le mode :

| Réglage | Jeux | Évaluations |
|---|---|---|
| Langue | oui | oui |
| Créer un raccourci (Windows) | oui | non (des raccourcis vers une évaluation : plus tard, Q-M5) |
| Touche pour quitter | oui | oui (elle quitte aussi une évaluation) |
| Durée des questions, question reposée, son de récompense, limites de temps et de score, couleurs du piano | oui | non (Eval ne les lit pas) |
| Mode d'entrée (souris, Tobii, les deux) | oui | oui |
| Durée de fixation | oui | oui (boutons « Recommencer / Continuer ») |
| Thème, couleur de fond, thème sombre | oui | oui (Eval choisit la couleur de ses textes d'après le fond) |
| Orientation du menu, image de fond du menu | oui | non |
| Dossiers : images des jeux, musique, « Où est-ce ? » | oui | non |
| Évaluations : importées, import, mode de passation, délai avant sélection, blocage par le son, dossier des résultats | non | oui |
| Dossier des résultats des jeux | oui | non |
| Cartes de chaleur : désactivation, opacité, couleurs | oui | oui (les cartes de chaque écran d'une évaluation utilisent l'opacité et les couleurs) |
| Zones d'intérêt, séquence de fixation, enregistrement vidéo | oui | non |

**« Eval Test »** : le jeu `gazeplayEvalTest` (libellé du menu « Tester sa configuration ») **a été retiré** le
27/10/2026 (section 12.5) : c'était le protocole d'entraînement « ANR » codé en dur, à l'ancien format `config.json`
(Q2). Il n'existe donc plus rien sous ce nom (voir Q-M1).

**Musique** : dans la JVM d'une partie, la musique de fond n'est lancée que si le jeu en déclare une
(`GameMenuController`, ligne 191) ; GazePlay Eval n'en déclare pas (hypothèse : aucune musique pendant une
évaluation, à confirmer à l'essai). Le panneau de contrôle d'une partie (musique, vitesse, engrenage) est le même pour
tous les jeux.

**Tests qui figent le démarrage et les écrans** : `UserProfileContextTest` (le choix d'un profil appelle
`onReturnToMenu`), `ConfigurationContextTest` (position de chaque élément de la grille des réglages : il faudra un
test par mode), `CatalogBasedGamesLocatorTest`, `LaunchSmokeTest` (lance l'application avec `--default-user`, puis un
jeu), `AllGamesLaunchTest` (`--game`, non concerné).

**Leçon de l'ancienne « édition BERA »** (retirée les 21-23/10/2026, `MODERNIZATION.md` 2k et 2ag) : elle passait par
un fichier d'édition persistant (`argsGazeplay.txt`), hérité par les JVM des jeux, un nom de catalogue dans un champ
statique, et des `if (bera)` dans le menu, les réglages et les statistiques. Défauts constatés : édition qui « collait »
d'un lancement à l'autre, état statique, branches mortes difficiles à retirer. **À ne pas refaire.**

### 13.3 Conception proposée

**Principe** : le mode est un **choix de la session**, tenu en mémoire par `GazePlay` (une JVM = un menu), **ni
persistant ni statique**, et décrit en **un seul endroit** (quels jeux, quelles rubriques de réglages), sans `if` répartis
dans le code.

1. **`AppMode`** (nouveau, `gazeplay-core`) : `GAMES`, `EVALUATIONS`. Chaque valeur dit :
   - quelles entrées du catalogue elle montre (`GAMES` : tout sauf les évaluations ; `EVALUATIONS` : seulement elles) ;
   - quelles rubriques de réglages elle montre (tableau 13.2).
2. **Marquer les évaluations** : un champ `evaluation` dans `GameSummary` (`@Builder.Default false`), mis à vrai par
   `GazeplayEvalGameSpecSource` (et le futur « Eval Test »). Ajout sans effet sur les 75 jeux.
3. **`GazePlay`** reçoit le mode courant (`getMode()`, `goToModePage()`), et `onReturnToMenu()` montre le menu du mode.
   Changer de mode remet à zéro le menu et les réglages en cache, comme `goToUserPage` le fait déjà.
4. **Nouvel écran de choix du mode** (`ModeSelectionContext`, `ui/scenes/modeselect`) : logo, deux grandes cartes
   « Jeux » et « Évaluations » (image et texte, souris et clavier ; le regard aussi, avec la durée de fixation, si le
   mainteneur le souhaite, Q-M3), bouton quitter. Ensuite : la page des profils, inchangée, avec le mode rappelé en
   titre et un bouton « Changer de mode ».
5. **Démarrage** (`GazePlayFxApp`) :
   - sans option → écran des modes ;
   - nouvelle option `--mode jeux|evaluations` → saute l'écran des modes (pour deux raccourcis de bureau ou de
     l'installeur, Q-M4) ;
   - `--user` / `--default-user` sans `--mode` → mode Jeux, **comme aujourd'hui** (raccourcis existants inchangés) ;
   - `--game` → la partie, comme aujourd'hui (le mode n'y sert pas).
6. **Mode Jeux** : `HomeMenuScreen` tel quel, avec le catalogue filtré (sans GazePlay Eval) ; réglages sans la rubrique
   Évaluations. Création de raccourcis : sans les évaluations.
7. **Mode Évaluations** : un **menu propre** (`EvalMenuScreen`, `ui/scenes/evalmenu`), plutôt que le menu des jeux
   filtré (catégories, favoris, statistiques par catégorie et recherche n'ont pas de sens pour 1 ou 2 entrées) :
   - une carte par **évaluation importée** (nom, nombre d'écrans, date d'import), qui lance directement la passation
     (`GameMenuController.chooseAndStartNewGameProcess` avec la variante de l'évaluation : une JVM par passation,
     comme aujourd'hui) ;
   - boutons : importer une évaluation, supprimer, ouvrir le dossier des résultats (`GazePlayLearning_Eval`),
     « Eval Test » (Q-M1), réglages, profils, changer de mode, quitter ;
   - réutilise `EvalSettings` (import, suppression, dossier), déjà écrit pour les réglages.
8. **Réglages** : `ConfigurationContext` construit sa grille selon `gazePlay.getMode()`, en ne gardant que les rubriques
   du mode (une liste décrite par `AppMode`, chaque rubrique restant écrite une seule fois). Le bouton « accueil »
   revient au menu du mode (`onReturnToMenu`, déjà le cas).
9. **Rien ne change dans la JVM d'une partie** ni dans les fichiers de résultats.

### 13.4 Étapes d'implémentation

Chaque étape compile, garde la suite verte, et le mode Jeux reste identique à aujourd'hui.

1. `AppMode` + champ `evaluation` de `GameSummary` + filtrage du catalogue selon le mode (`HomeMenuScreen`,
   raccourcis). Tests : le mode Jeux liste 75 entrées sans GazePlay Eval, le mode Évaluations seulement les
   évaluations.
2. `GazePlay` : mode courant, `goToModePage`, `onReturnToMenu` selon le mode, remise à zéro des écrans en cache.
   Tests sur la navigation (comme `UserProfileContextTest`).
3. `ModeSelectionContext` + démarrage (`GazePlayFxApp`) + option `--mode`. Tests : écran (deux cartes, quitter),
   choix → page des profils ; `LaunchSmokeTest` avec `--mode evaluations`.
4. Réglages par mode. `ConfigurationContextTest` : une grille par mode (positions figées comme aujourd'hui).
5. `EvalMenuScreen`. Tests : une carte par évaluation importée (dossier de test), lancement de la passation
   (commande construite, comme `GameMenuControllerTest`), import et suppression.
6. « Eval Test » selon la réponse à Q-M1.
7. Traductions (français, anglais), documentation (`ARCHITECTURE.md`, `CLAUDE.md`, ce fichier), puis la suite
   complète, `smokeTest`, `allGamesTest`, et un essai réel des deux modes.

**Risque** : la refonte graphique du menu principal est prévue juste après (`MODERNIZATION.md` 9). L'écran des modes et
le menu des évaluations seront donc simples et fonctionnels, sans travail graphique poussé, pour ne pas le faire
deux fois.

### 13.5 Questions pour le mainteneur

- **Q-M1 — « Eval Test ».** Le jeu `gazeplayEvalTest` a été retiré (ancien protocole ANR à l'ancien format). Que doit
  être « Eval Test » dans le mode Évaluations ?
  a) un **test de la configuration** avant une passation (son ancien libellé : « Tester sa configuration ») :
     affichage du point de regard, quelques cibles à fixer aux coins et au centre, mesure de la précision et du taux de
     perte du regard, avec un « OK / à recalibrer » (recommandé) ;
  b) une évaluation d'entraînement à faire passer avant la vraie, construite avec le site (rien à coder) ;
  c) le retour de l'ancien protocole ANR (contraire à la réponse Q2).
- **Q-M2 — Profils.** Les profils sont-ils communs aux deux modes (comme prévu ici), ou le mode Évaluations doit-il
  avoir ses propres profils (par exemple un par examinateur) ?
- **Q-M3 — Écran des modes au regard.** Les deux cartes doivent-elles se choisir aussi au regard, ou seulement à la
  souris et au clavier (c'est l'adulte qui choisit) ?
- **Q-M4 — Raccourcis.** Faut-il deux raccourcis dans l'installeur Windows (« GazePlay Learning – Jeux » et
  « – Évaluations », avec `--mode`), en plus du raccourci actuel qui ouvre l'écran des modes ?
- **Q-M5 — Raccourci vers une évaluation.** Faut-il pouvoir créer un raccourci de bureau qui lance directement une
  évaluation donnée (comme pour un jeu) ?
- **Q-M6 — Après une évaluation.** L'écran de statistiques des jeux s'affiche à la fin d'une passation. Faut-il plutôt
  un écran de fin propre aux évaluations (résumé des réponses, bouton vers le dossier `Eval<n>_<date>`) ?

### 13.6 Réponses du mainteneur (27/10/2026) et conséquences sur le plan

| Question | Réponse | Conséquence |
|---|---|---|
| Q-M1 Eval Test | **a) test de la configuration** | Noté, **pas codé pour l'instant** (demande du mainteneur). L'étape 6 de 13.4 attend son feu vert ; le bouton « Eval Test » n'apparaît pas tant que le test n'existe pas. |
| Q-M2 Profils | **Communs aux deux modes** | Comme prévu en 13.3 : page des profils inchangée. |
| Q-M3 Regard | **Écran des modes et page des profils : souris et clavier seulement.** Le **menu principal** de chaque mode se choisit **au regard** quand le mode d'entrée est `tobii` ou `tobii_and_mouse` | Voir ci-dessous : travail nouveau. **Abandonné le 27/10/2026** (14.3) : les menus restent à la souris. |
| Q-M4 Raccourcis de l'installeur | **Aucun raccourci** | Pas d'option `--mode` (13.3 point 5 et 13.4 étape 3 allégés) : sans option → écran des modes ; `--user` / `--default-user` → mode Jeux, comme aujourd'hui ; `--game` inchangé. |
| Q-M5 Raccourci vers une évaluation | **Non** | La création de raccourcis des réglages reste dans le mode Jeux seulement, sans les évaluations. |
| Q-M6 Écran de fin | **L'écran actuel convient** (le mainteneur réfléchit à mieux) | Rien à faire pour l'instant. |

**Interprétation (à confirmer)** : « pas de raccourci tout court » est lu comme « aucun nouveau raccourci » ; la
création de raccourcis de jeux qui existe déjà dans les réglages est gardée en mode Jeux.

**Menu principal au regard (Q-M3)** — faits relevés dans le code :
- **Aujourd'hui, le menu ne réagit pas au regard.** Le gestionnaire du regard (`GazeDeviceManagerFactory`) n'est créé
  que pour une partie (`GameContextFactory`), dans la JVM de la partie. Dans la JVM du menu, aucune source Tobii n'est
  démarrée : les gestionnaires `GAZE_ENTERED` / `GAZE_EXITED` des flèches de défilement de `HomeMenuScreen` (lignes
  142-143, 184-185) ne reçoivent donc rien, et le `ProgressIndicator dwellTimeIndicator` passé aux cartes des jeux
  n'est utilisé nulle part (code resté d'une ancienne version).
- Les cartes des jeux (`GameMenuFactory.createGameButton`) ne réagissent qu'à la souris.

Ce qu'il faut ajouter (nouvelle étape, avant l'écran des évaluations) :
1. **Démarrer le regard dans la JVM du menu** quand le mode d'entrée est `tobii` ou `tobii_and_mouse` : un gestionnaire
   créé par `GazeDeviceManagerFactory` à l'ouverture du menu, les cartes et boutons utiles enregistrés
   (`addEventFilter`), arrêté en quittant le menu.
2. **Le suspendre pendant une partie** : la JVM du menu reste vivante pendant que la partie tourne dans la sienne.
   **Risque** : deux processus clients du Tobii en même temps. **Hypothèse** (non vérifiée) : Stream Engine accepte
   plusieurs clients, mais deux flux à 100 Hz consomment pour rien et le regard ne doit de toute façon pas déclencher
   une carte du menu caché. Recommandation : arrêter la source du menu au lancement d'une partie
   (`GameMenuController`) et la redémarrer au retour du menu ; à vérifier avec l'Eye Tracker 5.
3. **Sélection par fixation** des cartes (jeux et évaluations) et des boutons principaux, avec la durée de fixation des
   réglages et un indicateur de progression (comme les cibles d'Eval, `EvalTarget`), réutilisé par les deux menus.
   Les boutons à ne pas déclencher par erreur (quitter, profils, réglages) restent à la souris : à confirmer.
4. **Mode `tobii`** : dans les parties, la souris est bloquée (`GameContextFactory.applyInputMode`). Dans le menu, la
   souris reste active dans tous les modes (c'est l'adulte qui prépare) : à confirmer.
5. Tests : avec un `GazeSource` simulé (comme `GazeDispatchCharacterizationTest`), une fixation sur une carte lance la
   partie ; aucune source démarrée en mode `mouse_control`.

**Étapes révisées** (remplacent 13.4) :
1. `AppMode` + champ `evaluation` de `GameSummary` + filtrage du catalogue (inchangé).
2. `GazePlay` : mode courant, navigation (inchangé).
3. `ModeSelectionContext` + démarrage, **sans `--mode`**, écran à la souris et au clavier.
4. Réglages par mode (inchangé).
5. `EvalMenuScreen` (sans le bouton « Eval Test »).
6. **Menu au regard** (ci-dessus), pour le menu des jeux et celui des évaluations.
7. Traductions, documentation, suite complète, `smokeTest`, `allGamesTest`, essai réel des deux modes avec
   l'Eye Tracker 5.
8. Plus tard, sur demande : « Eval Test » (test de la configuration).

## 14. Réalisation des deux modes (27/10/2026)

Codé après le feu vert du mainteneur (réponses de 13.6, confirmées : « oui pour les 3 »), avec ses retours sur les
réglages du même jour. **Rien n'est commité.** « Eval Test » n'est pas codé (Q-M1 : plus tard).

### 14.1 Retours du mainteneur sur les réglages

- Mode d'entrée : « Tobii seul (regard) » devient **« Tobii seul »** (anglais « Tobii only »).
- Modes de passation : **les appellations du mainteneur** (celles de la branche `GazePlayLearning`) reviennent :
  « Classique », « Autonomie Soutien Oral », « Autonomie Soutien Visuel », « Autonomie Totale ». Seuls les libellés
  changent : les valeurs enregistrées (`EVAL_OPTIONS`) sont les mêmes.
- Évaluations importées : autant qu'on veut ; dans le menu des évaluations, **« Lancer une évaluation » ouvre une
  fenêtre** qui liste les évaluations importées, et l'on choisit celle à lancer.
- Réglage `MENU_PROGRESS_INDICATOR` (jauge pendant une fixation dans le menu) : ajouté puis retiré avec le regard
  dans les menus (14.3).

### 14.2 Ce qui a été fait

| Élément | Où | Rôle |
|---|---|---|
| `AppMode` (`GAMES`, `EVALUATIONS`) | `gazeplay-core`, `net.gazeplay` | Le mode de la session, en mémoire seulement (ni sauvé, ni transmis à la JVM d'une partie). `filter` : la part du catalogue de chaque mode. |
| `GameSummary.evaluation` | `gazeplay-games-commons` | Marque une entrée du catalogue comme évaluation (vrai pour GazePlay Eval seulement). |
| `ModeSelectionContext` | `ui/scenes/modeselect` | Premier écran : « Jeux » ou « Évaluations », à la souris et au clavier ; bouton quitter. |
| `GazePlay` | `gazeplay-core` | `goToModePage`, `chooseMode` (puis page des profils), `onReturnToMenu` montre le menu du mode ; menu et réglages reconstruits quand le mode change. |
| Page des profils | `UserProfileContext` | Mode rappelé sous le logo, bouton « Changer de mode ». Profils communs aux deux modes. |
| `EvalMenuScreen` | `ui/scenes/evalmenu` | Menu des évaluations : nombre d'évaluations importées, « Lancer une évaluation », « Importer une évaluation », « Résultats des évaluations » (dossier `GazePlayLearning_Eval`), réglages, profils, plein écran, quitter. |
| `EvalChooserDialog` | `ui/scenes/evalmenu` | Fenêtre ouverte par « Lancer une évaluation » : un bouton par évaluation importée (lance la passation dans sa JVM, comme un jeu) et « Annuler ». |
| Menu des jeux | `HomeMenuScreen` | Sans GazePlay Eval (cartes et statistiques par catégorie). Le rejeu d'un `replayData.json` garde tout le catalogue. |
| Réglages par mode | `ConfigurationContext` | Mode Jeux : la page d'avant, sans la rubrique Évaluations. Mode Évaluations : langue, oculomètre, thème (thème, couleur de fond, thème sombre), Évaluations (avec la touche pour quitter), cartes de chaleur. Raccourcis : jeux seulement. |
| Démarrage | `GazePlayFxApp` | Sans option : écran des modes. `--user` / `--default-user` : menu des jeux, comme avant (pas d'option `--mode`, Q-M4). `--game` : inchangé. |

### 14.3 Le regard dans les menus : essayé puis retiré (27/10/2026)

- **Fait** : un regard dans les menus principaux (`MenuGaze` : cartes des jeux, variantes, lancement et choix d'une
  évaluation, par fixation, avec le réglage `MENU_PROGRESS_INDICATOR`) a été codé, puis **retiré le jour même à la
  demande du mainteneur** : activer le Tobii dans le menu provoquait des erreurs dans les jeux, et le menu actuel de
  GazePlay n'est pas fait pour l'oculomètre.
- **Le menu est revenu comme avant** : il ne lit pas l'oculomètre (aucune connexion Tobii dans la JVM du menu), seule
  la JVM d'une partie s'y connecte. Retirés : `MenuGaze` et son test, le réglage `MENU_PROGRESS_INDICATOR` et son
  libellé, `AbstractGazeDeviceManager.setDispatchWithoutStats`, le suivi « partie en cours » de `GameMenuController`.
  `AbstractGazeDeviceManager`, `GameMenuController`, `GameMenuFactory`, `GameVariantDialog` et `Configuration` sont
  identiques au commit `5751834b6`. La clé `MENU_PROGRESS_INDICATOR` n'a jamais été publiée ; si elle se trouve dans un
  `GazePlay.properties` d'essai, elle est ignorée.
- **Hypothèse** (non vérifiée) sur les erreurs : deux connexions Tobii ouvertes en même temps (le wrapper n'a pas de
  fonction de déconnexion, la JVM du menu gardait la sienne pendant la partie).
- **Recommandation** : un menu au regard demanderait un menu conçu pour l'oculomètre (grandes cibles, peu de boutons)
  et une vraie déconnexion du Tobii avant une partie (processus séparé pour le regard du menu, ou fonction de
  déconnexion ajoutée au wrapper). À reprendre seulement sur demande, avec la refonte du menu.

### 14.4 Vérifications

- `./gradlew build` : **462 tests, 0 échec** (451 + 11 : `AppModeTest`, `GazePlayModeTest`, `EvalMenuScreensTest`,
  grille des réglages du mode Évaluations), SpotBugs vert, après le retrait du regard dans les menus (14.3). Avant ce
  retrait : 468 tests, avec les 6 de `MenuGazeTest`.
- Les deux tests de la grille des réglages s'exécutaient dans `Platform.runLater` : une assertion fausse n'y faisait
  pas échouer le test. Ils passent maintenant par `WaitForAsyncUtils.asyncFx(...).get()`.
- `smokeTest` 3/3 (lancement avec `--default-user` : menu des jeux, comme avant) et `allGamesTest` 76/76 (avant le
  retrait du regard dans les menus ; le retrait remet le code de lancement d'une partie à l'identique du commit).
- **Non vérifié** : un essai réel des deux modes, à la souris et avec l'Eye Tracker 5 (rendu des écrans, une
  évaluation au regard).

## 15. Bilan du 27/10/2026 et suite

### 15.1 Ce qui a été fait aujourd'hui

| Quoi | Où | État |
|---|---|---|
| État des lieux de la fusion avec la branche `GazePlayLearning` (Eval) | Sections 1 à 11 | Fait |
| GazePlay Eval joue les évaluations du site : import du `.zip`, lecteur, 4 modes de passation, résultats versionnés (`formatVersion` 1) dans `Documents\GazePlayLearning_Eval\Eval<n>_<aaaa-mm-jj>\`, carte de chaleur du regard et de la souris par écran | Section 12 | Commit `5751834b6`, poussé |
| Deux modes au lancement (« Jeux » / « Évaluations »), menu des évaluations avec la fenêtre de choix des évaluations importées, réglages propres à chaque mode, libellés du mainteneur (« Tobii seul », « Autonomie Soutien Oral »…) | Sections 13 et 14 | Commit `65a0d1622`, poussé |
| Regard dans les menus (`MenuGaze`, réglage `MENU_PROGRESS_INDICATOR`) | 14.3 | Essayé puis **retiré** (erreurs dans les jeux, menu pas adapté à l'oculomètre) |
| Deux solutions pour libérer vraiment la connexion Tobii du menu (processus séparé, ou déconnexion ajoutée au wrapper) | 14.3 (recommandation) | Expliquées, non codées : sans objet tant que le menu ne lit pas le regard |

- **Vérifications** : `./gradlew build` 462 tests, 0 échec, SpotBugs vert ; `smokeTest` 3/3 ; `allGamesTest` 76/76
  (avant le retrait du regard des menus, qui remet le lancement d'une partie à l'identique de `5751834b6`).
- **Testé par le mainteneur** avant le push : les deux modes et une évaluation.
- **Toujours ouvert** : « Eval Test » (test de configuration, plus tard), anonymisation et RGPD (Q7), règle d'arrêt
  (Q9).
- Branche de travail : `addClaudeCode` seulement (rappel du mainteneur : ne pas s'occuper des autres branches, ni des
  alertes Dependabot de la branche par défaut).

### 15.2 Demain : revoir le graphisme de quatre écrans

Demande du mainteneur : revoir le graphisme du **menu principal**, des **paramètres**, du **choix de l'utilisateur** et
du **choix du mode**. Ce qui suit est un état des lieux et une méthode ; **rien n'est codé**.

> **Suite** : les réponses du mainteneur (27/10/2026) et le plan détaillé sont dans **`REFONTE_GRAPHIQUE.md`**, qui
> remplace cette section pour la refonte.

**Les écrans et leur code (faits)**

| Écran | Classes | Taille |
|---|---|---|
| Choix du mode | `ui/scenes/modeselect/ModeSelectionContext` | 110 lignes, 6 styles écrits dans le code (`setStyle`) |
| Choix de l'utilisateur | `ui/scenes/userselect/UserProfileContext` | 737 lignes, 3 `setStyle` |
| Menu principal des jeux | `ui/scenes/gamemenu/HomeMenuScreen`, cartes `GameMenuFactory` | 535 + 286 lignes, 2 `setStyle` |
| Menu principal des évaluations | `ui/scenes/evalmenu/EvalMenuScreen`, `EvalChooserDialog` | 194 + 122 lignes, 6 + 2 `setStyle` |
| Paramètres | `ui/scenes/configuration/ConfigurationContext` | 1 416 lignes, une grille de lignes « libellé : champ », 4 `setStyle` |

- **Styles** : `gazeplay-core/src/main/resources/data/stylesheets/`. `base-light.css` / `base-dark.css` (fond clair ou
  sombre), un fichier par thème de couleur (`main-*.css`, 7 thèmes dans `BuiltInUiTheme`, **Argent et or** par
  défaut), et des tailles par largeur d'écran (`base-max-width-0800.css` à `2560`). `CssUtil.setPreferredStylesheets`
  les applique (12 appels).
- Réglages liés à l'apparence, à garder (clés persistantes) : thème (`CSS_FILE`), fond clair/sombre, couleur de fond,
  orientation du menu, fond activé.

**Méthode proposée**
1. **Voir l'existant** : captures des quatre écrans (thème par défaut, clair et sombre, 1920 et 1366 de large), à
   regarder avec le mainteneur.
2. **Choisir une direction** avec le mainteneur (questions ci-dessous), puis une maquette par écran, avant de coder.
3. **Une base commune** : couleurs, tailles de texte, marges et boutons en classes CSS ; les `setStyle` des nouveaux
   écrans (mode, évaluations) passent dans les feuilles de style.
4. **Un écran à la fois**, dans l'ordre : choix du mode, choix de l'utilisateur, menu principal (jeux, puis
   évaluations), paramètres (le plus gros).
5. **Vérifier à chaque écran** : `./gradlew build`, `smokeTest`, `allGamesTest`, les 7 thèmes, clair et sombre,
   plusieurs largeurs d'écran, à la souris.

**Risques**
- Les feuilles de style sont aussi chargées dans les jeux (`CssUtil`) : changer une classe partagée
  (`gameChooserButton`, `button`…) peut modifier l'apparence des jeux. Préférer de nouvelles classes propres aux menus.
- Les tests de la page des paramètres comptent les nœuds de la grille (`ConfigurationContextTest`) : une nouvelle mise
  en page demandera de les réécrire.
- Le graphisme ne se vérifie pas dans les tests (Monocle, sans fenêtre) : il faut des essais réels.

**Questions pour le mainteneur (demain)**
1. Une direction visuelle souhaitée (exemples, couleurs, public : enfants, personnes en situation de handicap) ?
2. Garder les 7 thèmes de couleur et le choix clair/sombre, ou en réduire le nombre ?
3. Paramètres : une seule longue page comme aujourd'hui, ou des onglets/rubriques ?
4. Menu des jeux : garder les cartes et l'orientation horizontale/verticale, les filtres par catégorie, les favoris ?
5. Page des profils : garder la grille d'images, l'ajout et la modification d'un profil au même endroit ?
