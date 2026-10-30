# CLAUDE.md — GazePlay Learning (anciennement GazePlay)

Contexte de travail pour Claude Code. Mis à jour le 23/10/2026 (branche `addClaudeCode`, après l'étape 35 de
`MODERNIZATION.md`). L'audit d'origine (20/10/2026, commit `4a035dd4f`) a été corrigé ici partout où le code a changé ;
le détail de chaque changement, ses vérifications et ses décisions sont dans `MODERNIZATION.md`, la carte du code dans
`ARCHITECTURE.md`.

## Règles de travail en vigueur

- **Phase actuelle (depuis le 20/10/2026) : modernisation progressive autorisée.** Modifications du code, des tests, du build, des dépendances et de la documentation permises, par étapes vérifiées. Le suivi se fait dans `MODERNIZATION.md` (une section par étape, et la section 9 pour reprendre).
- **Restent interdits sans accord explicite** : commit, push, changement de branche, reset, réécriture d'historique, migration irréversible de données, publication ou déploiement. Le mainteneur autorise commits et pushs demande par demande.
- **Branche : `addClaudeCode` uniquement.** Ne rien reporter sur `GazePlayLearning`, ne rien fusionner, pas de pull request pour l'instant (décision du mainteneur).
- Les tests sont rendus hermétiques par `build.gradle` (`user.home`, `gazeplay.folder`, `gazeplay.documents` et `gazeplay.argsFile` pointent vers `build/test-home`). Toujours les lancer via Gradle. Hors Gradle (IDE, exécution manuelle), ils liraient et écriraient le vrai dossier GazePlay, et pourraient le déplacer.
- **Dossier GazePlay : `Documents\GazePlay_Learning`** (décision du mainteneur, 23/10/2026 ; « Mes documents » trouvé par `FileSystemView` sous Windows, `~/Documents` ailleurs). Au premier lancement, l'ancien `~/GazePlay` y est déplacé (`GazePlayFolderMigration`, rien n'est supprimé ni écrasé). Si Windows refuse le déplacement (un sous-dossier tenu ouvert), il est copié, et l'ancien dossier est gardé avec la note `DONNEES-COPIEES-DANS-DOCUMENTS.txt` puis ignoré. Si la copie échoue aussi, l'ancien dossier reste utilisé. Les chemins des réglages qui pointaient dans l'ancien dossier sont réécrits à la lecture. Toute JVM lancée par GazePlay reçoit `-Dgazeplay.folder`. Le dossier `Documents\Gazeplay` des anciennes versions n'est pas touché.
- **JDK 25 requis** pour compiler. Dans le terminal du mainteneur, le `java` du PATH est un Zulu 17 : préfixer les commandes Gradle par `$env:JAVA_HOME = 'C:\Users\arrig\.jdks\azul-25.0.4';` (chemin entre apostrophes, sans `$HOME`).
- Ne jamais toucher aux 4 stashes existants, ni supprimer ou réinitialiser des modifications existantes.
- Ne jamais fabriquer de chiffres de performance : tout reste à mesurer.
- Aucun changement de format de résultats sans expliquer l'impact sur les données déjà produites, ni sans versionnage (`formatVersion`).
- Pour chaque constat, distinguer Fait / Hypothèse / Risque / Recommandation / Question.
- Répondre en français.

## Architecture réelle (faits)

- **Java 25** (bytecode `release 25`), **Gradle 9.8.2** (pas de Gradle 10 : décision du mainteneur), versions réunies dans `gradle/libs.versions.toml`. Les distributions embarquent **Zulu 25.0.4.1 FX (JavaFX 25.0.4)**, avec leur somme SHA-256 vérifiée (`gradle/jre.gradle`). Le code est compilé et testé contre l'**API JavaFX 21.0.13**, la plus ancienne prise en charge : il n'existe pas de Monocle 25 pour les tests sans fenêtre.
- **Spring Boot a été retiré** (étape 5) : `CompositionRoot` construit et relie les composants.
- Modules : `gazeplay-results` (calculs et fichiers de résultats, sans JavaFX), `gazeplay-gaze` (acquisition du regard), `commons` (configuration, diffusion du regard, `Stats`), `games-commons` (contrat des jeux), `core` (application), `games` (77 entrées au catalogue, GazePlay Eval et GazePlay Eval Test compris), `dist` (scripts de lancement). Retirés : `melordi`, `picto-pick`, `side-projects`, `gazeplay-api`, `old-website`.
- **Une JVM par partie** : `GameMenuController.createBuilder` lance `java -cp … GazePlayLauncher --game …` avec les options de `GazePlayLauncher.jvmOptions()` (`--enable-native-access`, `--sun-misc-unsafe-memory-access=allow`). Le menu attend 6 s, puis se réaffiche sur le thread JavaFX. L'enfant termine par `System.exit`.
- Jeux découverts via `games-catalog.txt`, généré au build (`gazeplay-core/build.gradle`) d'après les noms de fichiers `*SpecSource` (GazePlay Eval compris ; `evals-catalog.txt` n'existe plus).
- **Nom du produit : GazePlay Learning** (décision du mainteneur, 23/10/2026), pour le nom visible seulement : titre de la fenêtre, archives et scripts `gazeplay-learning-*`, installeur Windows (`AppId=GazePlayLearning`, installé à côté d'un ancien GazePlay). Les paquets `net.gazeplay`, les modules Gradle et les fichiers du dossier de données gardent leur nom. Logo : les lettres dessinées GAZE PLAY sont gardées, avec « Learning » dessous, écrit avec la police Fredoka (OFL 1.1, seules les images sont livrées ; générateur `gradle/logo/make_logo.py`).
- **Plus d'éditions** : AFSR et BERA (Eval) retirées le 21/10/2026, puis tout le mécanisme d'édition le 23/10/2026 (`GazePlayArgs`, page de réglages BERA, `ImportGame`) : `argsGazeplay.txt` n'est plus lu ni écrit. GazePlay Eval est revenu le 27/10/2026 comme un jeu du catalogue (section dédiée ci-dessous).
- Configuration : singleton `ActiveConfigurationContext` (conservé, décision du mainteneur ; les jeux passent par `IGameContext.getConfiguration()`), fichier `GazePlay.properties` **écrit de façon atomique**. Les anciennes clés d'avant 1.11.1 sont migrées à la lecture (`LegacyConfigurationKeys`). Les clés sont un format persistant : garder `AREA_OF_INTEREST_DISABLED`, dont la valeur `true` signifie « AOI activées », même si le nom est trompeur.
- Journaux : `gazeplay.log` du dossier GazePlay et ses fichiers tournants (`logback.xml`, propriété `gazeplay.folder` donnée par le lanceur avant tout journal ; le logger de `GazePlayLauncher` est créé tard exprès). `sysout-over-slf4j` redirige `System.out`/`System.err` vers le journal (gardé exprès, voir `MODERNIZATION.md` 2z).
- Analyse statique : **SpotBugs** seulement (dans `build` et en CI). PMD a été retiré.

## Flux du regard

`TobiiGazeSource` (interface `GazeSource`, module `gazeplay-gaze` ; EyeTribe retiré le 23/10/2026) → `GazeSample(time, x, y, source)` → `GazeSourceDeviceManager` → `AbstractGazeDeviceManager.onGazeSample`, avec un seul `runLater` → `fireEvent` direct sur une copie des nœuds → filtres `Stats`. Comportement figé par `GazeDispatchCharacterizationTest`.

- `PositionPollerRunnable` interroge le wrapper Tobii toutes les 10 ms, ignore la valeur (0,5 ; 0,5) renvoyée sans donnée (R9) et une position identique à la précédente, qui n'est pas une nouvelle mesure (R6, format v4).
- Wrapper `TobiiStreamEngineForJava:5.0` (GPL-3.0) : **JNI x64 seulement** (Windows et Linux). L'API se limite à `float[] gazePosition()` : pas d'horodatage de l'appareil, pas de validité. Le code natif lit la validité sans la tester, utilise un tableau partagé sans verrou et ne se reconnecte pas.
- **Appareil du laboratoire : Tobii Eye Tracker 5** (vérifié le 21-22/10/2026 sous Windows x64, Stream Engine 2.0.2 embarqué ; ne pas le remplacer par celui de Tobii Experience, incompatible). Il faut un calibrage dans Tobii Experience, sinon l'initialisation réussit mais aucune position n'arrive. Les valeurs `-1` (regard perdu) ne sont jamais enregistrées : `Stats` exige des coordonnées strictement positives.
- **Format v3** : `GazeEvent.getSampleTime()` porte l'instant de lecture du point (`GazeSample.time`), et `Stats` date les points de regard à cet instant. `GazeEvent.getTime()` reste l'instant d'entrée sur le nœud, utilisé par les jeux. Vérifié sur une vraie partie : plus aucun point au même instant.
- **Modes du réglage `EYE_TRACKER`** (étape 35) : `mouse_control` (souris seule, aucun `GazeEvent` émis), `tobii` (regard seul : la zone de jeu bloque la souris, `GameContextFactory.applyInputMode` ; Casse-briques, Follow et Gaze Race, qui écoutent la souris sur toute la scène, vérifient le mode eux-mêmes), `tobii_and_mouse` (les deux, valeur par défaut, comportement de l'ancien `tobii`). Les statistiques enregistrent la souris dans tous les cas ; l'écran des statistiques ne propose que les images des entrées du mode (`StatsContext.metricsShown`).
- `PetHouse` lit la position dans l'événement de regard (il appelait autrefois Tobii directement).

## Anomalies de résultats

Le JSON brut `coordinatesAndTimeStamp` de `replayData.json` est la seule source récupérable : toutes les corrections peuvent s'appliquer **hors ligne** aux données historiques. Les fichiers déjà produits ne sont jamais réécrits ; `ReplayDataFormat` documente les versions 1 à 4 et les lit toutes (la v4 ne change que la résolution des heatmaps).

| # | Anomalie | État | Récupération historique |
|---|---|---|---|
| R1 | Une même matrice de heatmap et un même rendu pour souris et regard ; les clics enregistrés comme mouvements | **Corrigé en v2** (fichiers par source) | Recalcul par source depuis le JSON (`event`) |
| R2 | Durée de fixation = 2 × le temps écoulé ; le 2ᵉ point jamais fusionné | **Corrigé en v2** | Recalcul depuis le JSON |
| R3 | `HH:MM:ss` (MM = mois) dans la colonne `Time` des `info-game.csv` | **Corrigé (étape 1)** | Minutes à reprendre dans le nom de fichier |
| R4 | Filtres de `Stats` ajoutés plusieurs fois : chaque point enregistré k fois | **Corrigé (étape 3)** | Dédoublonnage (entrées identiques) |
| R5 | `coordinateData` jamais vidé : la manche N contient les manches 1 à N | **Corrigé en v2** | Découpage aux ruptures de `time` |
| R6 | Points de regard = cycles d'interrogation à environ 100 Hz, le dernier point répété quand l'appareil n'envoie rien de neuf (Eye Tracker 5 : environ 70 % de répétitions) | **Corrigé en v4 (étape 33)** : une position identique à la précédente n'est plus transmise (filtre dans `PositionPollerRunnable`)  ; vérifié sur une vraie partie : 32,3 points de regard par seconde | Partielle (dédoublonner les points consécutifs identiques) |
| R7 | Résolution de heatmap largeur/200 ou /400 selon `maxMemory` (une partie lancée du menu reçoit un quart de la mémoire : 200 colonnes sous 4 Go de mémoire, hypothèse non mesurée) | **Corrigé en v4 (étape 33)** : toujours 400 colonnes | Normaliser (les fichiers v1 à v3 de 200 colonnes se reconnaissent à leur nombre de colonnes) |
| R8 | Durée Eval `timeGame / 100.` affichée en « secondes » (en réalité des ms) | **Corrigé (étape 3)** | ÷10 |
| R9 | Regard fantôme au centre si la bibliothèque native est absente ou échoue | **Corrigé (étape 8)**, vérifié sans appareil sous Windows x64 | Détectable |
| R10 | Export XLSX Eval : `ArrayIndexOutOfBounds` si `Anonymous=false` | **Corrigé (étape 3)** | XLSX manquants |
| R11 | `fixationSequence` du JSON : `x` contient Y et `y` contient X | **Corrigé en v2** dans le fichier. En mémoire, les points gardent Y dans `x` et X dans `y` (convention de `FixationSequenceRecorder`) | Inverser |
| R12 | La branche `GazePlayLearning` n'écrit plus `heatmap.csv` ni `metrics*.png` : rupture de format non versionnée | **Sans objet** : la fusion d'Eval (27/10/2026) n'a pas repris ces modifications de `Stats` | — |
| R13 | Zones d'intérêt : la souris gardait un déplacement vertical sur trois ; regard et souris mélangés dans une même liste | **Corrigé (étape 32)** : chaque source garde chaque changement de position, dans ses propres zones (`AreaOfInterestRecording`) ; l'écran des zones montre le regard ou la souris. Rien n'est écrit sur disque | Non applicable |

Autres points :
- La variance est une variance de population (÷n) : à documenter, pas un bug.
- `AreaOfInterest` n'écrit rien sur disque. C'est le seul usage de JAVE/ffmpeg, uniquement si la vidéo est activée.

## GazePlay Eval (fusionné le 27/10/2026, détail dans `GazePlayEval.md`)

- Joue les évaluations créées avec le **site GazePlay Eval** (dépôt `Documents\Tests\GazePlay-Eval`, en ligne sur lig-gazeplay.imag.fr) : `.zip` importé (menu ou réglages du mode Évaluations) vers `Documents\GazePlay_Learning\evaluations\<nom>\` (`evalData.json`, `evalInfo.json`, `images/`, `audio/`, `videos/`). Une variante du jeu `GazePlayEval` par évaluation. Évaluation de référence pour les essais : `Téléchargements\Tests sons et vidéos-gazeplayEval.zip`.
- **Deux modes au lancement** (27/10/2026, `GazePlayEval.md` 13-14) : écran « Jeux » / « Évaluations » avant les profils (communs), choix gardé en mémoire seulement (`AppMode`, jamais transmis à la JVM d'une partie). Mode Jeux : menu sans GazePlay Eval, réglages sans les évaluations. Mode Évaluations : `EvalMenuScreen` (« Lancer une évaluation » ouvre la fenêtre des évaluations importées), réglages des évaluations seulement. `--user`/`--default-user` : mode Jeux, comme avant ; pas d'option `--mode` ni de raccourci d'évaluation (décision du mainteneur). **GazePlay Eval Test** (28/10/2026, `GazePlayEval.md` 16) : carte « Tester une évaluation » du menu des évaluations ; joue l'évaluation sans formulaire et sans rien enregistrer (pour tester la calibration) ; `Stats.disableSaving` : ni statistiques, ni vidéo, ni écran des statistiques.
- **Les menus ne lisent pas l'oculomètre** (souris et clavier) : seule la JVM d'une partie se connecte au Tobii. Un regard dans les menus (`MenuGaze`) a été essayé puis retiré le 27/10/2026 (décision du mainteneur : erreurs dans les jeux, menu pas adapté à l'oculomètre ; `GazePlayEval.md` 14.3).
- **Refonte graphique des menus (28/10/2026)** : les 8 étapes de **`REFONTE_GRAPHIQUE.md`** sont faites (avancement en section 8 ; étape 7 : couleur principale, ménage, plantage audio). Paquet `net.gazeplay.ui.menus`, `menus.css` (règles sous `.gp-menu`, que les jeux ne portent pas), ressources libres seulement (polices OFL, icônes Apache-2.0). Le réglage `CSS_FILE` est devenu la **« Couleur principale »** des menus (7 couleurs, valeurs gardées, `builtin:VIOLET` nouveau) ; les jeux gardent leurs feuilles `main-*.css`. Bilan du 27/10/2026 : `GazePlayEval.md` 15.1.
- **Liste prioritaire du 29/10/2026 : faite le 30/10/2026** (points 1 à 5 de `REVUE_DES_JEUX.md`, validés et commités avec la revue du lecteur Eval) : plus de touche pour quitter ni de clé `QUIT_KEY` (on quitte par l'engrenage, clavier entièrement bloqué menu ouvert), réglages des évaluations allégés, menu des évaluations à 5 cartes (dont « Créer une évaluation (site GazePlay Eval) », qui ouvre `https://lig-gazeplay.imag.fr/#/home`, et l'aide, texte dans `data/help/eval-help-*.txt`), bouton « Commencer » du formulaire sans raccourci, souris permise sur le formulaire en mode « Tobii seul » (`IGameContext.MOUSE_ALLOWED_PROPERTY`). Reste la tâche de fin : refonte de l'engrenage des jeux, après la revue de tous les jeux.
- **Prochaine étape (depuis le 30/10/2026 : Frogs Race, puis Horses)** : revue des jeux un par un (graphisme et gameplay), suivie dans **`REVUE_DES_JEUX.md`** (un tableau par jeu, avec les points de l'inventaire des images déjà notés).
- **Images données par l'utilisateur (décision du mainteneur, 29/10/2026)** : toujours une **variante** du jeu (`UserImagesVariant`, `gazeplay-games-commons`), avec un dossier par jeu `files/images/<NameCode>`, une ligne dans les réglages (Dossiers, « Images des jeux ») et une fenêtre d'erreur dans le menu sans image (`UserImagesDialog`). Les anciens dossiers partagés (`magiccards`, `default`, `portraits`, `blocs`, `opinions`) sont retirés **au fil de la revue**, jeu par jeu (marche à suivre et liste dans `REVUE_DES_JEUX.md`, « Règles pour chaque jeu ») ; **pas de migration** : avec le passage à **GazePlay Learning 2.0**, les utilisateurs remettent leurs images (à écrire dans les notes de version). Fait : Coloring.
- **Plantage audio sous Windows** (corrigé le 28/10/2026) : `MMDevAPI.dll` déchargée pendant le démarrage d'un son de JavaFX Media, d'où un arrêt 0xC0000005 dans `rpcrt4.dll` sans `hs_err` (Puissance 4 : 3 lancements sur 40). Chaque JVM la garde chargée (`GazePlayLauncher.keepTheWindowsAudioLibraryLoaded`) : 80 sur 80 ensuite. Les vidages de Windows sont dans `%LOCALAPPDATA%\CrashDumps`.
- Le format du site est un contrat : clés françaises, durées en secondes décimales ; « Mettre un temps de fixation » d'une instruction = bouton de démarrage. Lecture tolérante (`EvalReader`, module `games-commons`), import en deux temps (`EvalImporter`).
- Résultats versionnés (`formatVersion` 1) dans **`Documents\GazePlayLearning_Eval\Eval<n>_<aaaa-mm-jj>\`** (décision du mainteneur ; numéro qui repart de 1 chaque jour ; emplacement et noms de l'ancienne version, dont les dossiers sont gardés) : `session.json`, `resultats.csv/.xlsx`, `evenements.csv` (entrées, sorties, sélections par source), `echantillons.csv`, `heatmaps\` (carte de chaleur du regard et de la souris de chaque écran). Données participant en clair (RGPD : question ouverte). Les tests remplacent Documents (`gazeplay.documents` dans `build.gradle`).
- Modes de passation `EVAL_OPTIONS` (classique, soutien oral, soutien visuel, autonomie totale) gardés. Plus de règle d'arrêt à 5 erreurs, plus d'ancien format `config.json`, plus de `gazeplayEvalTest` de l'ancienne branche (retirés, décision du mainteneur ; remplacé par GazePlay Eval Test, autre chose).
- La branche `GazePlayLearning` reste intacte, comme référence ; ses modifications de `Stats` (R12) n'ont pas été reprises.

## Plateformes, build et CI

- Distributions 64 bits seulement : Windows x64 (+ installeur Inno Setup), Linux x64, Linux ARM aarch64, macOS x64 et Apple Silicon, et une version sans JRE (Java 25 et JavaFX 21+ requis). **Plus de 32 bits** (décision du mainteneur, 20/10/2026). Les distributions avec runtime ne livrent pas de seconde copie de JavaFX.
- **Seul appareil pris en charge : Tobii Eye Tracker 5, sous Windows x64** (décision du mainteneur, 23/10/2026 : EyeTribe et les pilotes Linux du Tobii 4C retirés). Le wrapper contient aussi une bibliothèque Linux x64, jamais testée avec un appareil. Les autres plateformes fonctionnent en souris. Un réglage `eyetribe` est lu comme la souris.
- **CI** (`ci.yml`) : SpotBugs ; tests sous JDK 25 et 26 (`xvfb`, sortie audio virtuelle, paquets installés par `.github/scripts/install-apt-packages.sh`) ; lancement des 77 entrées du catalogue (`all-games`) ; construction **et lancement réel** des distributions Windows, Linux x64 et macOS Apple Silicon (menu puis un jeu). Chaque job a une durée maximale (20 ou 30 min). ARM Linux et macOS Intel ne sont lancés nulle part.
- `./gradlew build` réussit ; l'installeur Windows se construit avec Inno Setup 6 (`generateWindowsInstaller`) ou dans Docker. L'installation réelle n'a pas été testée.
- Tests : **509 tests, 0 échec** (28/10/2026, fin de la refonte graphique), hermétiques, sans fenêtre (Monocle), séquentiels (ne pas réactiver le parallélisme), plus un `smokeTest` de lancement et `allGamesTest`, qui lance chacune des 77 entrées avec une variante (GazePlay Eval et Eval Test sans variante : aucune évaluation importée) (environ 7 min, 4 JVM à la fois, en mode souris ; job de CI `all-games` ; `-PallGamesOnly=<jeu> -PallGamesRepeat=<n>` pour relancer un seul jeu).
- La scène JavaFX ne doit être modifiée que sur son thread : un audit complet a été fait le 23/10/2026 (`MODERNIZATION.md` 2ad). Pour un délai, utiliser `PauseTransition` plutôt qu'un `java.util.Timer`.
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

Réglées : branche de travail (`addClaudeCode`), format v2 puis v3 puis v4, plateformes (64 bits seulement, ARM et macOS gardés), melordi et picto-pick (retirés), comportement sans appareil (R9, testé).

Décisions du mainteneur du 22/10/2026 :
- **Fusion de `GazePlayLearning`** (R12, Eval) et passage par `developMain` : **après la fin de la modernisation**.
- **Pas de retraitement des fichiers historiques v1** : ils restent tels quels, lisibles par le rejeu.
- **Numéro et publication de la version : après la fin de la modernisation.** Tester alors l'installation réelle par l'installeur Windows.

Encore ouverte :
1. Eval : format officiel des exports, règle d'arrêt à 5 erreurs, données personnelles et RGPD.

Également tranchées le 22/10/2026 :
- **Une JVM par jeu : on la garde** (isolation et remise à zéro de l'état à chaque partie, au prix du temps de démarrage).
- **Licence Tobii : réglée selon le mainteneur.** L'usage de GazePlay est analytique (enregistrement et analyse du regard), et le mainteneur confirme que c'est couvert. Rien à changer dans le code.
- **Licence du projet : GPL-3.0-or-later** (28/10/2026 : le mainteneur accepte toute ressource compatible avec la GPL 3). La licence affichée par l'application et l'installeur est `gradle/templates/licence.txt` (en-tête, ressources d'autres auteurs, puis le texte de la GPL) ; mise à jour le 28/10/2026 (nom, années, contributeurs depuis 2021, polices, icônes, wrapper Tobii, images). Les images Freepik (animaux par défaut, version sans fond du Labyrinthe, `explosion.gif`, images de Magic Potions) et 3 animaux sans source (dog, elephant2, snake) ont été remplacés le 28/10/2026 : OpenMoji (CC BY-SA 4.0) et dessins faits pour le projet, scripts et attributions dans `gradle/images`. **Inventaire des images** (28/10/2026) : **`INVENTAIRE_IMAGES.md`** et `gradle/images/inventaire-images.csv` (1 186 images livrées : 110 libres, 153 libres probables, 8 non libres dont iStock/Getty, CC BY-NC et Hello Kitty, 915 de source inconnue classées par risque). Rien n'est encore remplacé au-delà de Freepik ; les sons ne sont pas inventoriés. Changer de licence demanderait l'accord de tous les auteurs, et le wrapper Tobii (GPL-3.0) impose une licence compatible GPL 3.
