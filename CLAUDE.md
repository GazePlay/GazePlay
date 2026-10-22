# CLAUDE.md — GazePlay

Contexte de travail pour Claude Code. Mis à jour le 22/10/2026 (branche `addClaudeCode`, après l'étape 24 de
`MODERNIZATION.md`). L'audit d'origine (20/10/2026, commit `4a035dd4f`) a été corrigé ici partout où le code a changé ;
le détail de chaque changement, ses vérifications et ses décisions sont dans `MODERNIZATION.md`, la carte du code dans
`ARCHITECTURE.md`.

## Règles de travail en vigueur

- **Phase actuelle (depuis le 20/10/2026) : modernisation progressive autorisée.** Modifications du code, des tests, du build, des dépendances et de la documentation permises, par étapes vérifiées. Le suivi se fait dans `MODERNIZATION.md` (une section par étape, et la section 9.0 pour reprendre).
- **Restent interdits sans accord explicite** : commit, push, changement de branche, reset, réécriture d'historique, migration irréversible de données, publication ou déploiement. Le mainteneur autorise commits et pushs demande par demande.
- **Branche : `addClaudeCode` uniquement.** Ne rien reporter sur `GazePlayLearning`, ne rien fusionner, pas de pull request pour l'instant (décision du mainteneur).
- Les tests sont rendus hermétiques par `build.gradle` (`user.home` et `gazeplay.argsFile` pointent vers `build/test-home`). Toujours les lancer via Gradle, jamais avec le vrai `user.home`. Hors Gradle (IDE, exécution manuelle), ils liraient et écriraient le vrai `~/GazePlay`.
- **JDK 25 requis** pour compiler. Dans le terminal du mainteneur, le `java` du PATH est un Zulu 17 : préfixer les commandes Gradle par `$env:JAVA_HOME = 'C:\Users\arrig\.jdks\azul-25.0.4';` (chemin entre apostrophes, sans `$HOME`).
- Ne jamais toucher aux 4 stashes existants, ni supprimer ou réinitialiser des modifications existantes.
- Ne jamais fabriquer de chiffres de performance : tout reste à mesurer.
- Aucun changement de format de résultats sans expliquer l'impact sur les données déjà produites, ni sans versionnage (`formatVersion`).
- Pour chaque constat, distinguer Fait / Hypothèse / Risque / Recommandation / Question.
- Répondre en français.

## Architecture réelle (faits)

- **Java 25** (bytecode `release 25`), **Gradle 9.8.2** (pas de Gradle 10 : décision du mainteneur), versions réunies dans `gradle/libs.versions.toml`. Les distributions embarquent **Zulu 25.0.4.1 FX (JavaFX 25.0.4)**, avec leur somme SHA-256 vérifiée (`gradle/jre.gradle`). Le code est compilé et testé contre l'**API JavaFX 21.0.13**, la plus ancienne prise en charge : il n'existe pas de Monocle 25 pour les tests sans fenêtre.
- **Spring Boot a été retiré** (étape 5) : `CompositionRoot` construit et relie les composants.
- Modules : `gazeplay-results` (calculs et fichiers de résultats, sans JavaFX), `gazeplay-gaze` (acquisition du regard), `commons` (configuration, diffusion du regard, `Stats`), `games-commons` (contrat des jeux), `core` (application), `games` (75 jeux au catalogue), `dist` (scripts de lancement). Retirés : `melordi`, `picto-pick`, `side-projects`, `gazeplay-api`, `old-website`.
- **Une JVM par partie** : `GameMenuController.createBuilder` lance `java -cp … GazePlayLauncher --game …` avec les options de `GazePlayLauncher.jvmOptions()` (`--enable-native-access`, `--sun-misc-unsafe-memory-access=allow`). Le menu attend 6 s, puis se réaffiche sur le thread JavaFX. L'enfant termine par `System.exit`.
- Jeux découverts via `games-catalog.txt` / `evals-catalog.txt`, générés au build (`gazeplay-core/build.gradle`) d'après les noms de fichiers `*SpecSource`.
- **Édition du produit** : seule `gazeplay` reste. Les éditions AFSR et BERA (Eval) ont été retirées le 21/10/2026 ; le code Eval est conservé mais inaccessible. Le fichier d'édition `argsGazeplay.txt` est dans `~/GazePlay` (l'ancien emplacement est encore lu).
- Configuration : singleton `ActiveConfigurationContext` (conservé, décision du mainteneur ; les jeux passent par `IGameContext.getConfiguration()`), fichier `GazePlay.properties` **écrit de façon atomique**. Les anciennes clés d'avant 1.11.1 sont migrées à la lecture (`LegacyConfigurationKeys`). Les clés sont un format persistant : garder `AREA_OF_INTEREST_DISABLED`, dont la valeur `true` signifie « AOI activées », même si le nom est trompeur.
- Journaux : `~/GazePlay/gazeplay.log` et ses fichiers tournants (`logback.xml`). `sysout-over-slf4j` redirige `System.out`/`System.err` vers le journal (gardé exprès, voir `MODERNIZATION.md` 2z).
- Analyse statique : **SpotBugs** seulement (dans `build` et en CI). PMD a été retiré.

## Flux du regard

`TobiiGazeSource` / `EyeTribeGazeSource` (interface `GazeSource`, module `gazeplay-gaze`) → `GazeSample(time, x, y, source)` → `GazeSourceDeviceManager` → `AbstractGazeDeviceManager.onGazeSample`, avec un seul `runLater` → `fireEvent` direct sur une copie des nœuds → filtres `Stats`. Comportement figé par `GazeDispatchCharacterizationTest`.

- `PositionPollerRunnable` interroge le wrapper Tobii toutes les 10 ms et ignore la valeur (0,5 ; 0,5) renvoyée sans donnée (R9).
- Wrapper `TobiiStreamEngineForJava:5.0` (GPL-3.0) : **JNI x64 seulement** (Windows et Linux). L'API se limite à `float[] gazePosition()` : pas d'horodatage de l'appareil, pas de validité. Le code natif lit la validité sans la tester, utilise un tableau partagé sans verrou et ne se reconnecte pas.
- **Appareil du laboratoire : Tobii Eye Tracker 5** (vérifié le 21-22/10/2026 sous Windows x64, Stream Engine 2.0.2 embarqué ; ne pas le remplacer par celui de Tobii Experience, incompatible). Il faut un calibrage dans Tobii Experience, sinon l'initialisation réussit mais aucune position n'arrive. Les valeurs `-1` (regard perdu) ne sont jamais enregistrées : `Stats` exige des coordonnées strictement positives.
- **Format v3** : `GazeEvent.getSampleTime()` porte l'instant de lecture du point (`GazeSample.time`), et `Stats` date les points de regard à cet instant. `GazeEvent.getTime()` reste l'instant d'entrée sur le nœud, utilisé par les jeux. Vérifié sur une vraie partie : plus aucun point au même instant.
- En mode `mouse_control`, aucun `GazeEvent` n'est émis.
- `PetHouse` lit la position dans l'événement de regard (il appelait autrefois Tobii directement).

## Anomalies de résultats

Le JSON brut `coordinatesAndTimeStamp` de `replayData.json` est la seule source récupérable : toutes les corrections peuvent s'appliquer **hors ligne** aux données historiques. Les fichiers déjà produits ne sont jamais réécrits ; `ReplayDataFormat` documente les versions 1, 2 et 3 et les lit toutes.

| # | Anomalie | État | Récupération historique |
|---|---|---|---|
| R1 | Une même matrice de heatmap et un même rendu pour souris et regard ; les clics enregistrés comme mouvements | **Corrigé en v2** (fichiers par source) | Recalcul par source depuis le JSON (`event`) |
| R2 | Durée de fixation = 2 × le temps écoulé ; le 2ᵉ point jamais fusionné | **Corrigé en v2** | Recalcul depuis le JSON |
| R3 | `HH:MM:ss` (MM = mois) dans la colonne `Time` des `info-game.csv` | **Corrigé (étape 1)** | Minutes à reprendre dans le nom de fichier |
| R4 | Filtres de `Stats` ajoutés plusieurs fois : chaque point enregistré k fois | **Corrigé (étape 3)** | Dédoublonnage (entrées identiques) |
| R5 | `coordinateData` jamais vidé : la manche N contient les manches 1 à N | **Corrigé en v2** | Découpage aux ruptures de `time` |
| R6 | Points de regard = cycles d'interrogation à environ 100 Hz, le dernier point répété quand l'appareil n'envoie rien de neuf (Eye Tracker 5 : environ 70 % de répétitions) | **Ouvert, documenté.** Depuis la v3, chaque point porte son instant de lecture | Partielle |
| R7 | Résolution de heatmap largeur/200 ou /400 selon `maxMemory` (hypothèse : selon la RAM et le mode de lancement) | **Ouvert, documenté** | Normaliser |
| R8 | Durée Eval `timeGame / 100.` affichée en « secondes » (en réalité des ms) | **Corrigé (étape 3)** | ÷10 |
| R9 | Regard fantôme au centre si la bibliothèque native est absente ou échoue | **Corrigé (étape 8)**, vérifié sans appareil sous Windows x64 | Détectable |
| R10 | Export XLSX Eval : `ArrayIndexOutOfBounds` si `Anonymous=false` | **Corrigé (étape 3)** | XLSX manquants |
| R11 | `fixationSequence` du JSON : `x` contient Y et `y` contient X | **Corrigé en v2** dans le fichier. En mémoire, les points gardent Y dans `x` et X dans `y` (convention de `FixationSequenceRecorder`) | Inverser |
| R12 | La branche `GazePlayLearning` n'écrit plus `heatmap.csv` ni `metrics*.png` : rupture de format non versionnée | **Ouvert** (branche non fusionnée) | — |
| R13 | Zones d'intérêt : la souris garde un déplacement vertical sur trois ; variables « Gaze »/« Mouse » inversées | **Ouvert**, caractérisé par test. Rien n'est écrit sur disque | Non applicable |

Autres points :
- La variance est une variance de population (÷n) : à documenter, pas un bug.
- `AreaOfInterest` n'écrit rien sur disque. C'est le seul usage de JAVE/ffmpeg, uniquement si la vidéo est activée.

## GazePlay Eval (code laissé de côté, décision du mainteneur)

- Import d'un zip vers `<fileDir>\evals\<nom>\config.json` : JSON positionnel non versionné (`EvalName`, `Scores[[nom,tag,valeur,max]]`, `Assets[[img1,desc1,img2,desc2,son,descSon,"First"|…,"tags"]]`, `Output` csv|xls|all, `Anonymous`, `Profil[6]`).
- Sélection par fixation de 2 000 ms. Clavier : X = correct (ajout manuel), C = faux, V = annuler.
- **Règle d'arrêt implicite : 5 erreurs consécutives.** Les items restants valent `null` dans le CSV.
- CSV en UTF-16 (`EvalReport`), texte libre, avec les données personnelles en clair si non anonyme.
- Chemins `\\` codés en dur : incorrect sous Linux et macOS.
- `gazeplayEvalTest` n'est **pas** une copie d'Eval : c'est l'ancien test BERA codé en dur.
- **La version active d'Eval est sur la branche `GazePlayLearning`** (non fusionnée, nouveau format Excel). Toute modification d'Eval doit partir de là.

## Plateformes, build et CI

- Distributions 64 bits seulement : Windows x64 (+ installeur Inno Setup), Linux x64, Linux ARM aarch64, macOS x64 et Apple Silicon, et une version sans JRE (Java 25 et JavaFX 21+ requis). **Plus de 32 bits** (décision du mainteneur, 20/10/2026). Les distributions avec runtime ne livrent pas de seconde copie de JavaFX.
- Tobii fonctionne uniquement sur Windows x64 et Linux x64 ; Linux n'a pas été testé avec l'appareil. ARM et macOS fonctionnent en souris seulement.
- **CI** (`ci.yml`) : SpotBugs ; tests sous JDK 25 et 26 (`xvfb`, sortie audio virtuelle) ; construction **et lancement réel** des distributions Windows, Linux x64 et macOS Apple Silicon (menu puis un jeu). Chaque job a une durée maximale (20 ou 30 min). ARM Linux et macOS Intel ne sont lancés nulle part.
- `./gradlew build` réussit ; l'installeur Windows se construit avec Inno Setup 6 (`generateWindowsInstaller`) ou dans Docker. L'installation réelle n'a pas été testée.
- Tests : **409 tests, 0 échec** (22/10/2026), hermétiques, sans fenêtre (Monocle), séquentiels (ne pas réactiver le parallélisme), plus un `smokeTest` de lancement.
- Dépréciations Gradle restantes : uniquement dans les plugins `release` et `javafxplugin`, déjà à leur dernière version, sans effet en Gradle 9.8.2.
- Le site public est publié depuis la branche **`gh-pages`** ; le dossier `docs/` en est une ancienne copie.
- `cd.yml` (déploiement sur S3, branche `master` seulement) utilise l'AWS CLI du runner, à la place de `jakejarvis/s3-sync-action` (archivée, autrefois non épinglée avec les secrets AWS). Les actions tierces sont épinglées par SHA.

## Faits externes vérifiés (octobre 2026)

- Spring Boot 3.4 : support OSS terminé le 31/12/2025 (retiré du projet).
- JavaFX (Gluon) : 17 jusqu'en octobre 2026 ; 21 demande JDK 17 ou plus ; 25 demande JDK 23 ou plus. Zulu 17, 21 et 25 maintenus jusqu'en 2029, 2031 et 2033.
- **JEP 503 : JDK 25 n'existe plus en x86 32 bits.** Les builds Azul avec OpenJFX sont en 64 bits seulement.
- Gradle 9.8.x prend en charge Java 25 (depuis 9.1). JaCoCo : Java 25 depuis 0.8.14. Lombok : JDK 25 depuis 1.18.40.
- `jpackage` ne construit pas pour un autre OS (Windows exige WiX).

## Questions ouvertes pour le mainteneur et le laboratoire

Réglées : branche de travail (`addClaudeCode`), format v2 puis v3, plateformes (64 bits seulement, ARM et macOS gardés), melordi et picto-pick (retirés), comportement sans appareil (R9, testé).

Décisions du mainteneur du 22/10/2026 :
- **Fusion de `GazePlayLearning`** (R12, Eval) et passage par `developMain` : **après la fin de la modernisation**.
- **Pas de retraitement des fichiers historiques v1** : ils restent tels quels, lisibles par le rejeu.
- **Numéro et publication de la version : après la fin de la modernisation.** Tester alors l'installation réelle par l'installeur Windows.

Encore ouvertes :
1. R13 (échantillonnage souris des zones d'intérêt) : à corriger seulement si le laboratoire utilise ces zones.
2. Eval : format officiel des exports, règle d'arrêt à 5 erreurs, données personnelles et RGPD.
3. Maintien d'EyeTribe (appareil plus fabriqué).

Également tranchées le 22/10/2026 :
- **Une JVM par jeu : on la garde** (isolation et remise à zéro de l'état à chaque partie, au prix du temps de démarrage).
- **Licence Tobii : réglée selon le mainteneur.** L'usage de GazePlay est analytique (enregistrement et analyse du regard), et le mainteneur confirme que c'est couvert. Rien à changer dans le code.
