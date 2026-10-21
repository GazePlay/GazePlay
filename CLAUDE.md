# CLAUDE.md — GazePlay

Contexte de travail pour Claude Code. Synthèse de l'audit technique et de l'étude de modernisation (octobre 2026, branche `addClaudeCode` @ `4a035dd4f`). Les références `fichier:ligne` correspondent à ce commit.

## Règles de travail en vigueur

- **Phase actuelle (depuis le 20/10/2026) : modernisation progressive autorisée.** Modifications du code, des tests, du build, des dépendances et de la documentation permises, par étapes vérifiées. Le suivi se fait dans `MODERNIZATION.md`. Restent interdits sans accord explicite : commit, push, changement de branche, reset, réécriture d'historique, migration irréversible de données, publication ou déploiement.
- Les tests sont rendus hermétiques par `build.gradle` (`user.home` et `gazeplay.argsFile` pointent vers `build/test-home`). Toujours les lancer via Gradle, jamais avec le vrai `user.home`.
- Ne jamais toucher aux 4 stashes existants, ni supprimer ou réinitialiser des modifications existantes.
- Ne jamais fabriquer de chiffres de performance : tout reste à mesurer.
- Hors Gradle (IDE, exécution manuelle), les tests liraient et écriraient le vrai `~/GazePlay` et le vrai fichier d'édition : ne pas les lancer ainsi.
- Aucun changement de format de résultats sans expliquer l'impact sur les données déjà produites, ni sans versionnage (`formatVersion`).
- Pour chaque constat, distinguer Fait / Hypothèse / Risque / Recommandation / Question.
- Répondre en français.

## Architecture réelle (faits)

- Java 17, Gradle 8.14 multi-modules. Spring Boot 3.4.5 sert surtout de *service locator* (19 fichiers, tous dans `core` sauf 2 dans `commons`, **0 dans les jeux**). picocli et JavaFX 21.0.1 à la compilation, mais **la JRE embarquée est Zulu FX 17.0.2** (`gradle/jre.gradle`), d'où un décalage de version.
- Modules : `commons` (configuration, regard, `Stats`), `games-commons` (contrat des jeux), `core` (application), `games` (53 paquets, 77 `SpecSource`, 73 launchers, 290 Mo de ressources), `dist`. `melordi` et `picto-pick`, distribués mais inaccessibles, ont été **retirés** (étape 9). `side-projects`, `gazeplay-api` et `old-website` sont morts ou expérimentaux.
- **Une JVM par partie** : `GameMenuController.createBuilder` lance `java -cp … GazePlayLauncher --game …` sans `-Xmx` ni `--add-exports`. Le menu attend 6 s dans un thread hors FX. L'enfant termine par `System.exit` (`StatDisplayUtils:50-53`).
- Jeux découverts via `games-catalog.txt` / `evals-catalog.txt`, générés au build (`gazeplay-core/build.gradle:77-125`) d'après les noms de fichiers `*SpecSource`.
- Édition du produit (gazeplay / afsr / bera = Eval) choisie par le fichier `argsGazeplay.txt` (`GazePlayArgs`), relu dans 9 classes d'interface. **Les éditions AFSR et BERA ont été retirées le 21/10/2026** (décision du mainteneur) : seul gazeplay reste ; le code Eval est conservé, inaccessible (voir MODERNIZATION.md, 2k).
- Configuration : singleton statique `ActiveConfigurationContext`, fichier `GazePlay.properties` réécrit à chaque changement, **de façon non atomique** (`Configuration.java:361`). Les clés sont un format persistant : garder `AREA_OF_INTEREST_DISABLED`, dont la valeur `true` signifie « AOI activées » (case « Activer les zones d'intérêt »), même si le nom est trompeur.
- Champs statiques modifiables : 13 au total (liste finie, migrable).
- Les fichiers journaux tournants `gazeplay.log.N` sont écrits dans le **répertoire courant** (`logback.xml`, `fileNamePattern` relatif).

## Flux du regard

`PositionPollerRunnable` (sleep 10 ms) → `Tobii.gazePosition()` → `Platform.runLater` → `AbstractGazeDeviceManager.updatePosition` → `runLater(fireEvent(GazeEvent))` par nœud → filtres `Stats` sur la racine.

**Depuis l'étape 10** : `TobiiGazeSource` / `EyeTribeGazeSource` (interface `GazeSource`) → `GazeSample` → `GazeSourceDeviceManager` → `onGazeSample`, avec un seul `runLater` → `fireEvent` direct sur une copie des nœuds → filtres `Stats`.

- Wrapper `TobiiStreamEngineForJava:5.0` (GPL-3.0) : **JNI x64 seulement** (Windows et Linux). Rien pour x86, macOS ni ARM. L'API se limite à `float[] gazePosition()` : pas d'horodatage, pas de validité. Le code natif lit la validité sans la tester, utilise un tableau partagé sans verrou et ne se reconnecte pas.
- **En cas d'erreur, le wrapper renvoie (0,5 ; 0,5)**, soit un regard fantôme au centre de l'écran. La valeur par défaut de la configuration est `tobii`.
- En mode `mouse_control`, aucun `GazeEvent` n'est émis.
- `PetHouse.java:186` appelle `Tobii` directement, en contournant l'abstraction.

## Anomalies de résultats (confirmées par lecture du code, non encore testées)

Le JSON brut `coordinatesAndTimeStamp` de `replayData.json` est la seule source récupérable : toutes les corrections peuvent s'appliquer **hors ligne** aux données historiques.

| # | Anomalie | Où | Récupération historique |
|---|---|---|---|
| R1 | **Corrigé en format v2 (étape 9)** : Une même matrice de heatmap pour souris et regard ; un même rendu pour les 3 PNG `metrics*` ; `MouseEvent.ANY` enregistre aussi les clics | `Stats.java:532,570,744-748` | Recalcul par source depuis le JSON (`event`) |
| R2 | **Corrigé en format v2 (étape 9)** : Durée de fixation = 2 × le temps écoulé depuis le début du groupe ; le 2ᵉ point n'est jamais fusionné | `Stats.java:896-912` | Recalcul depuis le JSON (pas ÷2) |
| R3 | **Corrigé (étape 1)** : `timeNow()` utilisait `HH:MM:ss` (MM = mois) dans la colonne `Time` des `info-game.csv` (24 classes Stats) | `DateUtils.java` | Minutes à reprendre dans le nom de fichier |
| R4 | **Corrigé (étape 3)** : Les filtres sont ajoutés à `scene.getRoot()` mais retirés de `scene` (sans effet) ; `reset()` en rajoute. Chaque échantillon est enregistré k fois. Simon et GazeRace dès la manche 1 (`reset()` dans `launch()`) ; 16 jeux via « Continuer » de `showRoundStats` ; tous les jeux via Rejouer | `Stats.java:598-639`, `GameContext.java:335,494` | Dédoublonnage (entrées identiques) |
| R5 | **Corrigé en format v2 (étape 9)** : `coordinateData` n'était jamais vidé : le JSON de la manche N contient les manches 1 à N, et `time` redescend | `Stats.java` | Découpage aux ruptures de `time` |
| R6 | Les échantillons « gaze » sont des cycles d'interrogation à environ 100 Hz, pas les échantillons de l'appareil ; le dernier point est répété quand le regard est perdu | `PositionPollerRunnable` | Partielle |
| R7 | Résolution de heatmap largeur/200 ou /400 selon `maxMemory`, donc selon la RAM et le mode de lancement (hypothèse) | `Stats.computeHeatMapPixelSize` | Normaliser |
| R8 | **Corrigé (étape 3)** : Durée Eval `timeGame / 100.` affichée comme « secondes » (en réalité des ms) | `GazeplayEval.java:598,655`, `StatsContext` | ÷10 |
| R9 | **Corrigé (étape 8, constaté sans appareil sous Windows x64)** : Regard fantôme au centre si la bibliothèque native est absente ou échoue | wrapper Tobii | Détectable |
| R10 | **Corrigé (étape 3)** : **Export XLSX Eval : `ArrayIndexOutOfBounds` si `Anonymous=false`** (tableau `bookData` trop petit de 8 lignes) | `GazeplayEval.java:645` | XLSX manquants |
| R11 | **Corrigé en format v2 (étape 9)** : `fixationSequence` du JSON : `x` contient Y et `y` contient X (en pixels, non normalisés) | `Stats.java:902` | Inverser |
| R12 | La branche `GazePlayLearning` filtre par eye tracker et **n'écrit plus `heatmap.csv` ni `metrics*.png` pour tous les jeux** : rupture de format non versionnée | diff de branche | — |
| R13 | Zones d'intérêt : la souris garde tout déplacement horizontal mais un déplacement vertical sur trois (`x != px \|\| y != py && counter == 2`) ; variables « Gaze »/« Mouse » inversées | `Stats` (filtre souris) | Non corrigé, caractérisé par test |

Autres points :
- La variance est une variance de population (÷n) : à documenter, pas un bug.
- `FixationSequence.vertexReduction` ignore le dernier point.
- `AreaOfInterest` n'écrit rien sur disque. C'est le seul usage de JAVE/ffmpeg (environ 190 Mo), uniquement si la vidéo est activée.

## GazePlay Eval

- Import d'un zip vers `<fileDir>\evals\<nom>\config.json` : JSON positionnel non versionné (`EvalName`, `Scores[[nom,tag,valeur,max]]`, `Assets[[img1,desc1,img2,desc2,son,descSon,"First"|…,"tags"]]`, `Output` csv|xls|all, `Anonymous`, `Profil[6]`).
- Sélection par fixation de 2 000 ms. Clavier : X = correct (ajout manuel), C = faux, V = annuler.
- **Règle d'arrêt implicite : 5 erreurs consécutives**. Les items restants valent `null` dans le CSV.
- CSV en UTF-16, texte libre, avec les données personnelles en clair si non anonyme.
- Chemins `\\` codés en dur : incorrect sous Linux et macOS.
- `gazeplayEvalTest` n'est **pas** une copie d'Eval : c'est l'ancien test BERA codé en dur, dont les scores ne sont ni affichés ni exportés.
- **La version active d'Eval est sur la branche `GazePlayLearning`** (15 juillet 2026, 64 commits d'avance, non fusionnée, nouveau format Excel). Toute modification d'Eval doit partir de là.

## Plateformes et dépendances

- Tobii fonctionne uniquement sur Windows x64 et Linux x64 (pilotes Tobii 4C). Les distributions ARM (aarch64 depuis l'étape 9) et macOS (x64, et aarch64 depuis l'étape 10) fonctionnent en souris seulement. Runtime livré : Zulu 21.0.12.1 FX (étape 10). **Plus de distribution 32 bits** (décision du mainteneur, 20/10/2026).
- `gazeplay-linux.sh` : `set -e` combiné à `$(dpkg -s … | grep …)` fait probablement sortir le script si les paquets Tobii sont absents. Le script exécute aussi `chmod -R 777` à chaque lancement.
- Les JRE, SDK et pilotes sont téléchargés **sans somme de contrôle**, dans `~/.m2/repository`.
- Dépendances mortes à retirer après vérification :
  - tobby-transactional-outbox, qui apporte Kafka ;
  - `jmf`, `javax.servlet-api`, `video-recorder-core` ;
  - log4j 1.2.17 (via screen-recorder) ;
  - RXTX hors side-projects ;
  - `spring-boot-autoconfigure` 2.2.5 ;
  - `HeatChart`, `LatestNewsPopup` et `javafx.web`, `ClasspathScanningGamesLocator`, `HelloCommand`, `TobiiDllUtils`.
- Les versions déclarées diffèrent d'un module à l'autre ; plusieurs dépôts sont fragiles (`jcenter`, `mavenLocal` en premier, `raw.github`).
- La CI (`ci.yml`) lance SpotBugs et, depuis l'étape 1, les tests sous `xvfb` (job pas encore exécuté sur GitHub). `cd.yml` utilise `jakejarvis/s3-sync-action@master`, non épinglé, avec des secrets AWS.
- Tests : référence du 20/10/2026, 62 échecs sur 305 (non hermétiques). Après l'étape 1 de `MODERNIZATION.md` : 331 tests, 0 échec, hermétiques, avec un job CI `test`. JaCoCo 0.8.3 ne gère pas Java 17.

## Faits externes vérifiés (octobre 2026)

- Spring Boot 3.4 : support OSS terminé le 31/12/2025. 3.5 : terminé le 30/06/2026. 4.0 : jusqu'au 31/12/2026. 4.1 : jusqu'en 07/2027.
- JavaFX (Gluon) : 17 jusqu'en octobre 2026 ; 21 demande JDK 17 ou plus ; 25 demande JDK 23 ou plus. Zulu 17, 21 et 25 maintenus jusqu'en 2029, 2031 et 2033.
- **JEP 503 : JDK 25 n'existe plus en x86 32 bits.** Les builds Azul avec OpenJFX sont en 64 bits seulement.
- Gradle 9.8.1 actuel (Java 25 pris en charge depuis 9.1). JaCoCo : Java 17 depuis 0.8.8, Java 21 depuis 0.8.11, Java 25 depuis 0.8.14. Lombok : JDK 21 depuis 1.18.30, JDK 25 depuis 1.18.40.
- `jpackage` ne construit pas pour un autre OS (Windows exige WiX).

## Stratégie recommandée (en attente de validation)

- **Ni réécriture, ni Kotlin/Compose.** Garder Java, JavaFX et Gradle. Remplacer Spring par une racine de composition (environ 19 fichiers, aucun jeu touché). Garder le multi-JVM jusqu'à preuve mesurée.
- Cible :
  - `gazeplay-app` (ex-core) ;
  - `games-api` (contrat actuel et façade `Stats` conservés) ;
  - `gazeplay-results` (sans JavaFX : heatmap par source, fixations, writer vN et reader v1, outil de retraitement historique) ;
  - `gazeplay-gaze` (`GazeSource`, `GazeSample(t,x,y,valid?,source)`, un seul saut vers le thread FX) : **créé à l'étape 10** (acquisition ; la diffusion reste dans `commons`, liée à `Stats`) ;
  - `gazeplay-eval` ;
  - `commons` ;
  - `build-logic` avec catalogue de versions.
- JRE : mise à jour immédiate en 17.0.x, puis JDK 21 + JavaFX 21 si le 32 bits reste, ou JDK 25 + JavaFX 25 sinon.
- Horodatage à l'acquisition : **impossible sans réécrire le JNI Tobii (C++)**.

### Première étape proposée : filet de caractérisation

Ne commence qu'après validation, sur la branche de base choisie par le mainteneur (recommandation : `GazePlayLearning`).

**Contenu** (seulement `src/test` et la configuration de test) :
- isolation de `user.home` et `FILEDIR` ;
- Monocle sans affichage ;
- un `SyntheticGazeDeviceManager` ;
- des fixtures `config.json` Eval, anonyme et nominative ;
- un test de caractérisation par anomalie R1–R5, R8, R10 et R11 ;
- des fichiers de référence (`replayData.json`, `heatmap.csv`) ;
- l'exécution des tests en CI, avec une liste d'exclusions documentée.

**Critères de validation :**
- zéro ligne modifiée dans `src/main` ;
- trois exécutions identiques ;
- aucune écriture dans le vrai `~/GazePlay` ;
- CI verte.

Ordre ensuite : R10 et R8, puis R4 et R5, puis le format v2 après décision scientifique.

## Questions ouvertes pour le mainteneur et le laboratoire

1. Branche de référence et fusion de `GazePlayLearning` (R12) ?
2. Politique de format v2 : double écriture v1 + v2 ? Dédoublonnage ?
3. Plateformes à garder : win-x32, ARM, macOS (ce choix conditionne JDK 25) ? EyeTribe ?
4. Pourquoi une JVM par jeu ?
5. Format officiel des exports du site Eval ? Règle d'arrêt à 5 erreurs ? Données personnelles et RGPD ?
6. Fonctionnalités à garder : vidéo/AOI (ffmpeg), rejeu, melordi, picto-pick, `gazeplayEvalTest` ?
7. Licence Tobii pour l'enregistrement de données brutes ?
8. Le comportement natif sans appareil sous Windows x64 (R9) reste à tester sur matériel.
