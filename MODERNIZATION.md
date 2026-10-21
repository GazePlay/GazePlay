# Modernisation de GazePlay — suivi

Document de suivi vivant : à mettre à jour à chaque étape. Le diagnostic complet (architecture, anomalies R1–R12, stratégie cible) est dans `CLAUDE.md`. Ce document ne le répète pas : il trace ce qui a été **fait**, **vérifié** et **reste à décider**.

Convention : **Fait** (constaté dans le code ou par exécution) / **Hypothèse** / **Risque** / **Décision** / **Question**.

---

## 1. État de référence (20/10/2026)

- Branche `addClaudeCode` @ `4a035dd4f` (= `developMain` + `CLAUDE.md`). Seul fichier non suivi au départ : `CLAUDE.md`. Les 4 stashes ne sont pas touchés.
- Environnement : Windows 11, Zulu 17.0.11, Gradle 8.14 (wrapper). Pas de Docker ni de WSL : **aucune exécution Linux ou macOS possible ici**.
- Commandes :
  - compilation : `./gradlew compileJava compileTestJava` → OK (avertissements de dépréciation et d'opérations non vérifiées, non traités) ;
  - tests : `./gradlew :gazeplay-commons:test :gazeplay-games-commons:test :gazeplay-core:test :gazeplay-games:test` ;
  - analyse : `./gradlew spotbugsMain` (seule étape de la CI au départ).
- `--offline` est impossible : l'agent JaCoCo 0.8.3 n'est pas dans le cache local.

### Tests au départ (mesuré, avec `user.home` redirigé vers un répertoire temporaire)

| Module | Tests | Échecs |
|---|---|---|
| gazeplay-commons | 134 | 12 |
| gazeplay-games-commons | 18 | 5 |
| gazeplay-core | 106 | 34 |
| gazeplay-games | 47 | 11 |
| **Total** | **305** | **62** |

Causes des 62 échecs, après tri :
- **Fait : tests non hermétiques.** Ils lisent et écrivent le vrai `~/GazePlay`. Sous Windows, `GazePlayArgs` lit aussi `C:\Users\<user>\Documents\Gazeplay\argsGazeplay.txt` ; sur ce poste, ce fichier contient `bera`. Les tests du cœur tournaient donc en édition BERA et dépendaient de la machine (14 échecs uniquement pour cette raison).
- **Fait : tests périmés** (ils ne reflètent plus le code actuel) :
  - fixture de configuration avec les clés d'avant le commit `bddc3f1e8` (2022) ;
  - mocks qui stubent `getXxxProperty()` alors que le code appelle désormais `setXxx()` ou `isXxx()` ;
  - classe de jeu supprimée (`VideoGridGameSpecSource`) ;
  - choix de la touche Quitter réduit à Q et E ;
  - musique qui ne redémarre plus quand on change de dossier (voulu, commit `850e26f9a`) ;
  - champ `@NonNull` absent des builders de test ;
  - clé `normal` au lieu de `Normal` ;
  - tentative de mock d'un enum ;
  - disposition de l'écran de configuration modifiée.
- **Fait : 2 vrais bugs produit** (voir §3) : le plantage de « Où est la forme ? » et un `NullPointerException` dans `StatsContext`.

---

## 2. Étape 1 — Filet de tests hermétique et vert, et corrections sûres (TERMINÉE)

**Objectif.** Disposer d'une suite de tests fiable, déterministe, qui ne touche pas aux données réelles et qui s'exécute en CI. C'est un préalable à tout refactoring. Y ajouter les corrections à faible risque révélées en route.

**Critères de réussite et résultats :**

| Critère | Résultat |
|---|---|
| Suite verte | **331 tests, 0 échec** (3 exécutions successives identiques : voir §5) |
| Aucune écriture dans le vrai `~/GazePlay` ni dans `Documents\Gazeplay` | Vérifié (`find -newer` après chaque exécution) |
| Aucun format persistant modifié | Oui (voir §4) |
| Aucun test désactivé ni exclu | Oui : chaque test en échec a été corrigé, aucun `@Disabled` |
| Tests exécutés en CI | Job ajouté, **non exécuté** (pas de Linux ici) |

### Changements de build et de CI
- `build.gradle` : toutes les tâches `Test` reçoivent `user.home=build/test-home/<tâche>` (recréé à chaque exécution) et `gazeplay.argsFile` qui pointe vers un fichier d'édition `gazeplay` généré.
- `.github/workflows/ci.yml` : nouveau job `test` (Ubuntu, Zulu 17, `xvfb-run`, rapports publiés en artefact).

### Changements de code de production (minimes et justifiés)
| Fichier | Changement | Comportement en production |
|---|---|---|
| `GazePlayArgs` | Chemin du fichier d'édition centralisé dans `getArgsFile()`, surchargeable par `-Dgazeplay.argsFile` | Identique si la propriété est absente |
| `GazePlayLauncher.saveArgs` | Utilise `getArgsFile()` au lieu de dupliquer la logique par OS | Identique (y compris la création du dossier sous Windows) |
| `StatsContext.addAllToGrid` | `"X".equals(stats.variantType)` au lieu de `stats.variantType.equals("X")` | Identique quand `variantType` n'est pas nul (valeur par défaut `""`) ; plus de `NullPointerException` sinon |
| `I18N.loadFromFile` | Une ligne qui a plus de colonnes que l'en-tête est journalisée (`ERROR`), et les colonnes en trop sont ignorées | Avant : `RuntimeException` et le jeu entier plantait |
| `whereIsTheShape.csv` | Réparation de la régression introduite par `a86cf7fe7` (voir §3) | « Où est la forme ? » fonctionne à nouveau |
| `DateUtils.timeNow` | `HH:MM:ss` → `HH:mm:ss` (**R3**) | Colonne `Time` des `info-game.csv` : minutes correctes (voir §4) |
| `Configuration.saveConfig` | Écriture dans un fichier temporaire du même dossier, puis remplacement atomique (`ATOMIC_MOVE`, avec repli sur un déplacement simple si le système de fichiers ne le permet pas). **Si le remplacement est impossible** (fichier ouvert ailleurs sans partage de suppression, cas vérifié sous Windows), réécriture en place comme avant, avec un `WARN`. Permissions POSIX conservées. Méthode `synchronized` | Contenu et format du fichier inchangés ; jamais pire qu'avant |

### Tests ajoutés
- `DateUtilsTest.shouldPrintMinutesNotMonthInTime` : régression R3.
- `I18NTest.shouldIgnoreExtraColumnsOfAMalformedLine` : robustesse d'`I18N`.
- `ConfigurationTest.shouldSaveWithoutLeavingTemporaryFiles` et `shouldKeepThePreviousFileWhenSavingFails` : sauvegarde atomique. Le second simule une panne au milieu de l'écriture ; le fichier précédent doit rester intact octet pour octet.
- `ConfigurationTest.shouldStillSaveWhenTheFileCannotBeReplaced` (Windows seulement) : le fichier est tenu ouvert par un `FileInputStream`. Le journal du test montre que le remplacement atomique échoue réellement dans ce cas et que le repli sauvegarde quand même le réglage.
- `TranslationFilesTest` (module games) : contrôle les 11 CSV de traduction des modules games et core. Il vérifie qu'aucune ligne ne dépasse le nombre de colonnes de l'en-tête et que chaque clé a une traduction anglaise. Cela empêche qu'un tableur décale de nouveau les colonnes sans que personne le voie.

### Tests corrigés (périmés ; sens d'origine conservé)
- `ConfigurationTest` : clés actuelles dans la fixture, et copie dans `@TempDir` au lieu de `src/test/resources`.
- `MultilinguismTest` : clé présente dans la fixture.
- `SourceSetTest` : clé `Normal` dans la fixture.
- `GameSummaryComparatorTest` : `gameThumbnail` ajouté.
- `ConfigurationContextTest` :
  - les mocks reproduisent les setters (`writeInto`) ;
  - choix de la touche Quitter réduit à Q et E ;
  - tests musique alignés sur `850e26f9a`, avec suppression d'un doublon ;
  - disposition de la grille relevée sur le code actuel et commentée.
- `StatsContextTest` et `StatsContextFactoryTest` : stub de `isAreaOfInterestDisabled()`.
- `GameSpecSourceInstantiatorTest` : jeu existant.
- `DivisorTest` : vrai `BackgroundStyle.DARK`, car un enum ne se mocke pas.
- `PictureCardTest` : type de jeu stubé.

---

## 2b. Étape 2 — Outillage de test (TERMINÉE)

Branche `addClaudeCode`, décision du mainteneur du 20/10/2026 : pas de report vers d'autres branches, pas de commit sans demande.

| Avant | Après | Raison |
|---|---|---|
| JUnit Jupiter 5.6.0 + JUnit 4.12/4.13.2 (pour des `@RunWith` sans effet) | JUnit 6.1.3 (BOM) + `junit-platform-launcher` ; JUnit 4 supprimé | JUnit 4 ne servait qu'à des annotations ignorées par JUnit 5 ; le launcher sera exigé par Gradle 9 |
| Mockito `3.12.+` (version **dynamique**, build non reproductible) + fichier `mock-maker-inline` | Mockito 5.24.0 (fabrique « inline » par défaut) | Reproductibilité ; `mockStatic` natif |
| TestFX 4.0.16-alpha, Monocle jdk-11+26 (non configuré) | TestFX 4.0.18, Monocle 17.0.10, **mode sans affichage par défaut** (`-Pgui` pour voir les fenêtres) | Les tests n'ouvrent plus de fenêtres. Monocle 21.0.2 exige Java 21 (classes en version 65) : à reprendre lors du passage à JDK 21 |
| JMockit 1.49 (abandonné depuis 2020, `-javaagent` dans 3 modules) | **Supprimé** : 12 classes de test migrées vers Mockito ou vers de vrais répertoires dans le `user.home` temporaire | JMockit bloquait JaCoCo en 0.8.3 et la montée de version de Java |
| JaCoCo 0.8.3 (ne gère pas Java 17) | JaCoCo 0.8.15 | Couverture enfin mesurable : `commons` = 48 % des instructions (premier chiffre fiable) |
| Tests de `commons` dépendants des classes de test de `core` (cycle) | Fixtures partagées `gazeplay-commons/src/testFixtures` (`TestingUtils`, `bear.jpg`, `licence.txt`, `logback.xml`) via `java-test-fixtures` | Suppression du cycle entre modules |

Code de production touché : `ActiveConfigurationContext.setInstance` et `BackgroundMusicManager.setInstance`, deux points d'injection explicites qui remplacent la réécriture de bytecode par JMockit. Rien ne change en production.

Tests rendus plus justes à l'occasion :
- `BackgroundMusicManagerTest` ne télécharge plus un MP3 depuis opengameart.org (il utilise une URL `file:` locale).
- `StatsTest.shouldSaveStats` vérifie maintenant la création réelle des 6 fichiers de résultats, au lieu d'une assertion toujours vraie (« `build/` n'est pas vide »), et ne masque plus les exceptions levées sur le thread JavaFX.
- `MenuUtilsTest` : le test de profil nommé, jusque-là commenté, est réactivé.
- `BravoTest` : suppression d'un `System.exit(0)` qui pouvait tuer la JVM de test, et du plein écran.
- JMockit cachait deux `MockUp` sans effet : l'un visait une méthode inexistante (`VersionInfo.findVersionInfo(String, boolean)`), l'autre une annotation `@Mock` absente.

Résultat : **332 tests, 0 échec**, en mode sans affichage comme avec `-Pgui`.

Reste à traiter :
- `junit5-system-exit` 1.1.2 repose sur le `SecurityManager`, qui fonctionne sous Java 17 mais exige un drapeau à partir de Java 18. Sa version 2.x passe par un agent. À traiter avec JDK 21, ou en supprimant les `System.exit` directs de l'interface.
- Les tests vidéo (`java.awt.Robot`) ont toujours besoin d'un affichage : `xvfb` reste nécessaire en CI.

---

## 2c. Étape 3 — Résultats, GazePlay Eval et configuration (TERMINÉE)

### Caractérisation du pipeline de résultats
`StatsRecordingCharacterizationTest` (commons) injecte des `GazeEvent` et des `MouseEvent` sur la racine d'une vraie scène JavaFX, avec une horloge contrôlée : `Stats.clock`, un `LongSupplier` dont la valeur par défaut est `System::currentTimeMillis`, donc sans effet en production. **Toutes les anomalies ci-dessous sont désormais confirmées par exécution** (elles ne l'étaient jusqu'ici que par lecture du code).

| # | Comportement établi par test | Décision |
|---|---|---|
| R1 | Souris et regard s'additionnent dans **la même** matrice de heatmap. `MouseEvent.ANY` enregistre aussi les appuis, relâchements et clics (4 échantillons pour un déplacement suivi d'un clic) | **Non corrigé** : une heatmap par source impose de nouveaux fichiers, donc une décision de format (v2) |
| — (nouveau) | Règles de validité différentes : un échantillon de regard exige x > 0 **et** y > 0, un échantillon souris x > 0 **ou** y > 0 | Non corrigé (même raison) |
| R2 | Fixations. Le 2ᵉ point n'est jamais fusionné. La durée fusionnée vaut 2 × (t − début du groupe). Un nouveau point reçoit le temps écoulé depuis le début du groupe **précédent** | **Non corrigé** : la définition correcte d'une durée de fixation est une décision scientifique (options au §7) |
| R4 | Les filtres étaient retirés de la scène alors qu'ils avaient été ajoutés à sa racine. Conséquences : enregistrement **après `stop()`**, et chaque échantillon compté k fois après k−1 `reset()` (Rejouer, « Continuer », Simon, GazeRace) | **CORRIGÉ** : filtres retirés du nœud où ils ont été ajoutés, et réinscription idempotente dans `start()`. 3 tests de non-régression |
| R5 | Les échantillons bruts s'accumulent d'une manche à l'autre et `time` repart de 0 à chaque manche, alors que la heatmap est recréée à chaque manche | **Non corrigé** : vider les échantillons change le contenu de chaque `replayData.json` ; les manches restent séparables par les ruptures de `time` |
| R11 | `FixationPoint.x` contient Y et `y` contient X. `FixationSequence` compense à l'affichage, mais le JSON garde l'inversion | **Non corrigé** : changer la sémantique d'un champ persistant exige un `formatVersion` |

**Impact de la correction R4 sur les données.** Les nouvelles parties n'ont plus d'échantillons en double après Rejouer ou « Continuer » (ni de heatmap amplifiée k fois), et plus d'échantillons enregistrés entre la fin d'une partie et la suivante. Les fichiers historiques ne sont pas modifiés ; ils restent dédoublonnables hors ligne, puisque les doublons sont des entrées identiques et consécutives. Le format est inchangé.

### GazePlay Eval
- La mise en forme des exports est extraite dans `EvalReport` (record sans JavaFX). `EvalReportTest` contient les fichiers de référence CSV et Excel, en modes anonyme et nominatif.
- **R10 corrigé** : l'export XLSX d'une évaluation **nominative** plantait (`ArrayIndexOutOfBounds`, tableau trop petit de 8 lignes) et aucun fichier n'était produit. Les lignes sont désormais construites dans une liste. La disposition anonyme est strictement identique à l'ancienne ; un test relit un vrai `.xlsx` avec POI.
- **R8 corrigé** : « Temps de l'évaluation » valait millisecondes ÷ 100, présenté comme des secondes. Il vaut maintenant millisecondes ÷ 1000, dans le CSV, l'XLSX et l'écran BERA. **Les fichiers exportés avant cette correction doivent être divisés par 10.** Le libellé et le format restent les mêmes.
- Chemins : `répertoire + "\\" + nom` est remplacé par `new File(répertoire, nom)`. Rien ne change sous Windows ; sous Linux et macOS, le fichier n'atterrit plus dans le dossier parent avec un nom contenant un `\`.
- Écran BERA : « ScoreRightTargetItems » affichait le score **gauche** en Morphosyntaxe et en Phonologie. C'est corrigé (affichage seulement, rien n'est persisté).
- Le contenu du CSV est inchangé à l'octet près, à la durée près (vérifié par le fichier de référence ; encodage toujours UTF-16).

### Anciennes clés de configuration (question 3)
- **Fait** : les 17 clés renommées dans la version 1.11.1 (`bddc3f1e8`) ont toutes un équivalent direct, de même type et de même sens. Les cas ambigus ont été vérifiés dans le code d'avant : `LIMITERS`/`LIMITERT`, `SCREENSHOT` (devenu `isMultipleScreenshotsEnabled`), `BACKGROUND_DARK_THEME`.
- **Fait — migration réalisée** (`LegacyConfigurationKeys`, appelée au chargement) :
  - l'ancienne clé n'est lue qu'en l'absence de la nouvelle, et la nouvelle l'emporte toujours ;
  - un avertissement est journalisé ;
  - l'ancienne clé est **conservée** dans le fichier, si bien qu'un retour à une ancienne version retrouve ses réglages.
  
  Rien n'est supprimé ni réécrit à part ; c'est entièrement réversible. 4 tests, dont un sur un fichier complet d'avant 1.11.1.

### Question 4 — R3 et les CSV
- **Avant** : la colonne `Time` des `*-info-game.csv` affichait `HH:<mois>:ss`. **Après** : `HH:mm:ss`. Même format texte, même position.
- Fichiers existants : non modifiés. Leurs minutes se retrouvent dans le préfixe `yyyy-MM-dd-HH-mm-ss` du nom du fichier.
- Lecteurs automatiques : aucun dans le code de GazePlay. Un script externe qui aurait « corrigé » les minutes à partir du nom de fichier continue de fonctionner.
- **Recommandation (non publiée)** : une ligne dans les notes de la prochaine version, du type « Correction : la colonne Time des fichiers info-game.csv affichait le mois à la place des minutes ; les fichiers produits avant cette version peuvent être corrigés à partir de l'horodatage de leur nom ». La même note doit mentionner R8 (durée Eval ÷ 10) et R4 (fin des doublons après Rejouer ou Continuer).

Résultat (à la fin de l’étape 3) : **351 tests, 0 échec** ; SpotBugs passe. Les deux `setInstance` sont exclus de SpotBugs, avec la justification dans `gazeplay-commons/spotbugs-exclude.xml`.

---

## 2d. Étape 4 — Dépendances (TERMINÉE)

Méthode : pour chaque bibliothèque, recherche des usages dans le code (imports **et** noms qualifiés), la configuration, les ressources, les scripts et le packaging, puis suppression, test et vérification de la distribution.

### Supprimées (usage nul vérifié)
- `com.mageddo.tobby-transactional-outbox` (déclaré dans 8 modules, il apportait Kafka et Spring 5).
- `javax.media:jmf`.
- `javax.servlet-api`.
- RXTX, sauf dans `side-projects`.
- `screen-recorder` 0.0.3 (agomezmoron, servi par `raw.github`).
- JAVE dans `commons`, `games-commons` et `games` (seul `core` l'utilise).
- `mslinks`, `org.json` et `classgraph` dans `commons`.
- `commons-io` et `gson` là où ils n'étaient pas utilisés.
- `EyeTribeJavaFx.jar` hors de `commons` et `side-projects`.
- `spring-boot-autoconfigure` 2.2.5 (voir l'étape 5).

### Constat corrigé de l'audit initial
`video-recorder-core` n'est **pas** mort : son jar embarque les classes de Monte Screen Recorder (`org.monte.*`), qui servent à l'enregistrement vidéo de `Stats`. Il est conservé, mais en `transitive = false` : l'analyse du bytecode montre que `org.monte` ne référence aucune de ses dépendances. Cela retire 8 jars, dont `owner-java8` 1.0.10 (classes en double avec `owner` 1.0.12), `awaitility`, `hamcrest` et `log4j` 1.2.17, vulnérable.

### Classes mortes supprimées (aucune référence statique, par réflexion ni dans les catalogues)
`HeatChart` (copie de jheatchart), `ClasspathScanningGamesLocator`, `TobiiDllUtils`, `LatestNewsPopup` (son test était entièrement commenté) et `HelloCommand`. Le module JavaFX `javafx.web` (WebKit) ne servait qu'à `LatestNewsPopup` ; il est retiré.

### Journalisation
- Avant : chaque module déclarait logback et slf4j, en versions divergentes (1.2.1 ou 1.2.11 ; 1.7.25 ou 1.7.36), à côté de `log4j` 1.2.17 et `commons-logging`, qui entraient en conflit de classes avec les ponts.
- Après :
  - `slf4j-api` 2.0.20 est déclaré une fois pour tous les modules ;
  - logback 1.5.38 et les ponts (`jcl-over-slf4j`, `log4j-over-slf4j`, `log4j-to-slf4j` pour POI 5) ne sont déclarés que dans l'application ;
  - `commons-logging` est exclu.
- Correction : les journaux tournants `gazeplay.log.N` étaient écrits dans le **répertoire courant** (le `bin/` de l'installation) ; ils vont maintenant dans `~/GazePlay`. Le niveau racine `ON`, qui n'est pas un niveau valide (logback retombait sur DEBUG), est écrit explicitement `DEBUG`, sans changement de comportement.

### Mises à niveau
| Bibliothèque | Avant | Après | Raison |
|---|---|---|---|
| org.json | 20220320 (résolu en 20250107) | 20260814 | CVE-2022-45688, CVE-2023-5072 |
| Apache POI | 4.0.0 | 5.5.1 (`poi-ooxml-lite`) | CVE-2019-12415, version de 2018. Vérifié par l'aller-retour XLSX de `EvalReportTest` |
| everit-json-schema | 1.12.1 (JitPack) | 1.14.6 (Maven Central, `com.github.erosb`) | Sortie de JitPack |
| Guava | 31.1 | 33.7.2 | — |
| Gson | 2.10.1 | 2.14.0 | — |
| commons-io | 2.11.0 | 2.22.0 | — |
| picocli | 4.6.3 (via starter Spring) | 4.7.7 | — |

### Dépôts
`mavenLocal()` en premier (non reproductible), `jcenter()` (fermé depuis 2021) et `raw.github.com` sont supprimés. JitPack est restreint au groupe `com.github.GazePlay` (wrapper Tobii). Les téléchargements de JRE et de pilotes gardent le même dossier (`~/.m2/repository`), sans déclarer `mavenLocal()` comme dépôt.

### Conservées, en attente
- ~~`owner` 1.0.12~~ : remplacé à l'étape 8.
- ~~`reflections` 0.10.2~~ : supprimé à l'étape 8.
- ~~JAVE 2.5.0~~ : passé en 3.5.0 à l'étape 8, avec un binaire par distribution.
- `sysout-over-slf4j` 1.0.2 (2013) : il fonctionne avec SLF4J 2.
- Le wrapper Tobii 5.0 et `EyeTribeJavaFx.jar` : spécifiques au matériel.

---

## 2e. Étape 5 — Retrait de Spring Boot (TERMINÉE)

- **Fait** : Spring Boot 3.4.5 (fin du support OSS le 31/12/2025) ne servait qu'à localiser une dizaine de composants : 8 appels `getBean(...)` et 7 fabriques. Il tirait aussi un Spring Boot 2.x par `picocli-spring-boot-starter`, et le plugin `io.spring.dependency-management` **rétrogradait silencieusement** des versions (JAVE 3.3.1 → 2.5.0).
- **Remplacement** : `CompositionRoot` (environ 90 lignes) construit et relie explicitement les composants. Les scènes sont créées à chaque affichage, comme les anciens beans prototype, à travers l'interface `GazePlay.Screens`. `GameContextFactoryBean` devient `GameContextFactory implements Supplier<GameContext>`. `GazePlayLauncher` utilise directement picocli (`new CommandLine(new MainCommand(root)).execute(args)`).
- **Supprimé** :
  - `SpringApplication` (classe de configuration) et `GazePlayCommandLineRunner` ;
  - 5 `FactoryBean` ;
  - `HelloCommand` (jamais enregistrée) ;
  - les sous-commandes `game=<code>`. Elles n'étaient référencées nulle part et auraient lancé un jeu avant la création de la fenêtre. Le lancement d'un jeu par la JVM enfant passe toujours par `--game`, `--variant`, `--user`, `--width` et `--height`.
- **Effet de bord favorable** : Spring Boot forçait `java.awt.headless=true` par défaut, ce que `Stats` devait contourner.
- **Vérifié** :
  - 351 tests ;
  - **lancement réel** sous Monocle avec un `user.home` temporaire : menu d'accueil avec 75 jeux, puis lancement direct de « Où est la forme ? » comme le fait la JVM enfant. Aucune exception, hormis l'absence normale de `GazePlay.properties` au premier lancement et l'erreur Tobii `-2` (pas d'appareil branché).
- Limite : l'interface n'a pas été testée sur un vrai écran (limite connue de Monocle : plein écran avant affichage). À vérifier manuellement : passage en plein écran, choix du profil, menu, lancement d'un jeu, retour au menu.

---

## 2f. Étape 6 — Gradle 9 (TERMINÉE)

| Élément | Avant | Après |
|---|---|---|
| Gradle | 8.14 | **9.8.2**, somme SHA-256 de la distribution épinglée dans `gradle-wrapper.properties` |
| Versions | Éparpillées (chaînes en dur, `ext`) | **Catalogue `gradle/libs.versions.toml`** |
| Lombok | Plugin 6.4.3 → Lombok 1.18.24 (pas de support JDK 21) | Plugin 9.8.2, Lombok **1.18.48** (JDK 21 et 25) |
| SpotBugs (plugin) | 6.1.11 | 6.5.13 |
| Rapport de licences | `hierynomus.license-report` 0.16.1 (incompatible Gradle 9) | `jk1.dependency-license-report` 3.1.4, tâche `generateLicenseReport`, sortie dans `build/reports/dependency-license` |
| `release` / `download` | 2.6.0 / 4.0.4 | 3.2.0 / 5.7.0 |
| `com.gradle.enterprise` 3.1.1 (settings) | Présent, inutilisé | Supprimé |
| Cible Java | `sourceCompatibility` (convention supprimée en Gradle 9) | `options.release = 17` |
| `mainClassName` | Convention dépréciée | `application { mainClass = ... }` |
| `runtimeOnly.canBeResolved(true)` | Erreur en Gradle 9 | Supprimé |
| Source set `integrationTest` | Aucun fichier dans aucun module | Supprimé |
| Manifestes `Class-Path` | Résolus à la configuration (échec d'évaluation quand une dépendance manquait) | Évaluation paresseuse |
| `fileMode`/`dirMode`, `XmlSlurper` (Groovy 4) | — | `filePermissions`/`dirPermissions`, `groovy.xml.XmlSlurper` |

Vérifications : compilation et tests sous Gradle 9.8.2, `--dry-run` de `packageApp`, `downloadAndExtractJREs` et `generateWindowsInstallerInDocker`, construction réelle de `noJREDistZip`.

Avertissements restants, bloquants seulement en Gradle 10 ou 11 : des API dépréciées utilisées par les plugins `release` (`StartParameter.newInstance`) et `javafxplugin` (`Configuration.setVisible`). L'avertissement `Task.project` est réglé à l'étape 8 (tâches de catalogue réécrites).

### CI/CD
- `ci.yml` : job `test` (xvfb), voir l'étape 1.
- `cd.yml` :
  - actions `checkout`, `setup-java`, `upload-artifact` et `download-artifact` passées de v1/v2 à v4 (les artefacts v1 à v3 ne fonctionnent plus sur GitHub depuis 2024) ;
  - `jakejarvis/s3-sync-action@master` (**archivé**, non épinglé, il recevait les secrets AWS) remplacé par `aws s3 sync`, la CLI du runner ;
  - le déclencheur (push sur `master`) n'est pas modifié.
- `release.yml` : `free-disk-space@main` épinglé sur un SHA.
- **Aucun workflow n'a été exécuté.**

---

## 2g. Étape 7 — Java, JavaFX, runtime distribué et lancement (TERMINÉE)

### Faits établis
- **Compilation et tests** : JDK 17 (local Zulu 17.0.11, CI Zulu 17), bytecode 17.
- **Runtime livré avant** : Zulu **17.0.2** avec JavaFX (janvier 2022, environ 4 ans et demi de correctifs de sécurité manquants), pour Windows x64 et x86, Linux x64 et macOS x64. **ARM** : Temurin 17.0.3 arm32 avec le SDK OpenJFX 17.0.2 arm32.
- **Décalage** : le code était compilé contre JavaFX **21.0.1**, mais s'exécutait avec le JavaFX **17** de la JRE embarquée, dont les modules prennent le pas sur les jars du classpath.
- **Disponibilité des runtimes Azul avec JavaFX** (vérifiée sur `api.azul.com`, octobre 2026) :

| | Win x64 | Win **x86** | Linux x64 | Linux aarch64 | Linux **arm32** | macOS x64/aarch64 |
|---|---|---|---|---|---|---|
| Zulu 17 FX (17.0.20.1) | oui | **oui** | oui | oui | non | oui |
| Zulu 21 FX (21.0.12.1) | oui | **non** | oui | oui | non | oui |
| Zulu 25 FX (25.0.4.1) | oui | **non** | oui | oui | non | oui |

- **Compatibilité Java 21 mesurée** : 351 tests verts sur JDK Zulu 21.0.12, avec `--add-exports` pour Monocle quand JavaFX est chargé en modules nommés, et après suppression de `junit5-system-exit` (voir ci-dessous). Le test de lancement réel passe aussi sur JDK 21.

### Décisions appliquées (prudentes et réversibles)
1. **Runtimes mis à jour dans la même version majeure** : Zulu 17.0.2 → **17.0.20.1** FX sur les 4 plateformes Azul ; ARM : Temurin 17.0.3 → **17.0.20.1** arm32. Le SDK OpenJFX arm32 reste en 17.0.2, car Gluon ne publie pas de version plus récente (réponse 403).
2. **JavaFX de compilation aligné sur le runtime** : 21.0.1 → **17.0.20**. La compilation passe : le code n'utilisait aucune API postérieure à JavaFX 17.
   - **Correction (étape 9)** : l'alignement était supposé, pas vérifié, et il est **faux**. Zulu 17.0.20.1 FX embarque **JavaFX 22.0.10** sur Windows x64, Linux x64 et Linux aarch64 (`lib/javafx.properties`, lu dans les archives). Les anciennes JRE 17.0.2 embarquaient bien JavaFX 17.0.2.
   - La compilation contre JavaFX 17.0.20 reste valable : c'est l'API la plus ancienne prise en charge, ce qui couvre aussi la distribution sans JRE.
   - **Risque** : les tests tournent avec JavaFX 17.0.20, alors que les utilisateurs ont JavaFX 22.0.10. Le lancement de `windowsDistZip` avec son runtime (étape 7) a toutefois été fait avec ce runtime.
   - **Corrigé** : la distribution Linux x64 remplaçait les greffons multimédia `libavplugin-*.so` du runtime (construits pour JavaFX 22.0.10) par ceux du SDK OpenJFX 17.0.20. Cela mélangeait deux versions de JavaFX dans le moteur multimédia (sons, vidéos). Ce téléchargement est supprimé ; le runtime Zulu fournit déjà ses greffons (ffmpeg 54 à 62). **Non testé sous Linux.**
3. **Bytecode 17 conservé** (`options.release = 17`). Le même code peut donc tourner sur un runtime 17 ou 21.
4. **Téléchargements sécurisés** : SHA-256 vérifiée après chaque téléchargement (sommes publiées par Azul, Adoptium et Gluon ; pour les pilotes Tobii, somme épinglée au premier téléchargement). Les archives sont mises en cache **sous leur nom versionné** : avant, un changement de version réutilisait silencieusement `jre/windows-x64.zip`. Vérifié de bout en bout : téléchargement, contrôle, extraction et `java -version` de la JRE Windows x64, puis **lancement de GazePlay sur cette JRE**.
5. **CI** : matrice de tests **JDK 17 et 21**.

### Recommandation (décision métier requise)
Passer les distributions **64 bits** à **Zulu 21 FX** (support jusqu'en 2031), en gardant Zulu 17 FX pour Windows x86 tant que cette distribution existe. C'est techniquement possible dès maintenant : le bytecode reste en 17 et la seule variable à changer par plateforme est dans `gradle/jre.gradle`. Java 25 imposerait d'abandonner Windows x86, et ARM 32 bits reste à part dans tous les cas. **Questions** :
- faut-il garder Windows x86 ?
- ARM 32 bits ou aarch64 (Raspberry Pi OS 64 bits) ?

### Lancement et tests
- **`smokeTest`** (nouvelle tâche, ajoutée à la CI) : lance la vraie application dans une JVM enfant sans affichage, avec un `user.home` temporaire, pour le menu puis pour « Où est la forme ? ». Elle vérifie que le processus tourne encore au bout de 25 s, que le catalogue des jeux est chargé et qu'aucune exception imprévue n'est journalisée. Elle passe sur JDK 17 et 21.
- **`GazePlay.quit()`** : les boutons « Quitter » du menu et de la sélection de profil appelaient `System.exit(0)` directement. Le test vérifie désormais l'appel (mock), ce qui supprime `junit5-system-exit`, dont la version 1.x repose sur le `SecurityManager`, inutilisable sous Java 21 sans drapeau.
- **Tests JavaFX sérialisés** (`BuildService`, `maxParallelUsages = 1`) : plusieurs JVM de test JavaFX simultanées faisaient planter le toolkit de temps en temps (plantage natif une fois sous JDK 17, cascade de `PulseReceiver` une fois sous JDK 21). La suite dure environ 3 min au lieu d'environ 2 ; la compilation reste parallèle.

### Scripts de lancement (bugs corrigés)
| Bug | Effet | Correction |
|---|---|---|
| Distribution ARM : scripts nommés `*-amr.sh`, alors que le build copie `*-arm.sh` | **Distribution ARM livrée sans aucun script de lancement** (Gradle ignore un `from()` absent) | Renommage |
| Linux : `set -e` avec `VAR=$(dpkg -s … \| grep …)` | **Le script s'arrête sans lancer GazePlay** si les pilotes Tobii ou `dpkg` sont absents (vérifié : code de sortie 1) | Vérification optionnelle, question posée seulement depuis un terminal |
| `JAVA_CMD="java -cp \"$CP\" …"` puis `${JAVA_CMD}` | Guillemets littéraux : **le premier jar du classpath n'est pas chargé** (vérifié), et un chemin avec espace casse le lancement | Appel direct de `java` avec `"$CLASSPATH"` |
| `chmod -R 777 lib/` à chaque lancement | Jars et JRE modifiables par tout utilisateur local ; échec, donc arrêt (`set -e`), si l'installation appartient à root | `chmod u+x` sur `jre/bin` seulement si nécessaire (les zip ne conservent pas le bit d'exécution) |
| Chemins relatifs au répertoire courant (scripts macOS et `.bat` Windows) | Échec si le lancement ne se fait pas depuis `bin/` | Chemins résolus depuis le script (`$0`, `%~dp0`) |

Les arguments supplémentaires sont transmis (`"$@"`, comme `%*` sous Windows). La recherche des jars reste récursive : la distribution ARM place les jars JavaFX arm32 dans `lib/jre/lib`. Le nouveau script a été testé dans une arborescence simulée (bit d'exécution, espace dans un chemin, autre répertoire courant, pas de `dpkg`, pas de terminal). **Non testé sur une vraie distribution Linux, macOS ou ARM.**

---

## 2h. Étape 8 — Suivi du regard (EN COURS, mise en pause le 20/10/2026)

### Fait
- **R9 confirmé par l'expérience** (réponse à la question 8). Un programme de sonde appelant `tobii.Tobii.gazePosition()` sous Windows x64 **sans oculomètre** obtient `Init code error -2`, puis **(0,5 ; 0,5) à chaque appel**. Avec la configuration par défaut (`EYE_TRACKER=tobii`) et sans appareil, GazePlay recevait donc un regard fixe au centre de l'écran toutes les 10 ms : sélection possible par fixation, et échantillons « gaze » fantômes dans les résultats.
- **Correction** : `PositionPollerRunnable` ignore la paire exacte (0,5f ; 0,5f), avec un avertissement unique dans le journal. La source de positions est injectable (`Supplier<float[]>`). Le wrapper n'expose pas le code d'initialisation : sans réécrire le JNI, c'est la seule détection possible. `PositionPollerRunnableTest` couvre la conversion en coordonnées écran (avec décalage d'un second écran) et la non-régression R9. **Impact sur les données** : les nouvelles parties jouées en mode `tobii` sans appareil ne contiennent plus de faux échantillons au centre. Les fichiers historiques sont repérables : échantillons « gaze » exactement à X = 0,5 et Y = 0,5.
- **PetHouse** : sur `GAZE_MOVED`, le jeu relisait le wrapper Tobii au lieu des coordonnées de l'événement, si bien que la main restait au centre avec un EyeTribe ou sans appareil, et le décalage d'un second écran était ignoré. Il utilise maintenant les coordonnées de l'événement, converties dans le repère de la main. **Non testé automatiquement ni à la main.**
- SpotBugs : deux exclusions justifiées, `GazePlay.quit()` (`DM_EXIT`) et la comparaison sentinelle de R9 (`FE_FLOATING_POINT_EQUALITY`).

- **Fichier d'édition** (`GazePlayArgs`, fait après la reprise) : `argsGazeplay.txt` est désormais dans `~/GazePlay`, comme les autres données. Avant, il était :
  - dans le **répertoire courant** sous Linux et macOS : l'édition BERA ou AFSR était perdue lors d'un lancement depuis un raccourci de bureau, et l'écriture était impossible quand l'installation appartient à root ;
  - sous un chemin `C:\Users\<user.name>\Documents\Gazeplay` codé en dur sous Windows.

  L'ancien emplacement reste **lu en repli** si le nouveau fichier n'existe pas, car une JVM lancée avec des arguments (un raccourci `--user`, par exemple) ne réécrit pas ce fichier. Un fichier absent ou illisible donne l'édition par défaut, comme avant, mais avec un avertissement journalisé au lieu d'une pile d'exception. Pas de cache : relire le fichier n'a pas de coût mesurable. `GazePlayArgsTest` couvre le nouvel emplacement, la priorité au fichier actuel, le repli sur l'ancien, l'absence de fichier et la surcharge par `-Dgazeplay.argsFile`.
- Vérification : **359 tests, 0 échec** ; `smokeTest` (menu et jeu) et `spotbugsMain` passent (JDK 17).
- **Caractérisation de la diffusion du regard** (`GazeDispatchCharacterizationTest`, sur une vraie fenêtre Monocle ; positions en coordonnées écran, comme celles des oculomètres). Faits établis par test :
  - **aucun** événement n'est émis tant que `addStats()` n'a pas été appelé, même au-dessus d'un nœud enregistré ;
  - la séquence est `GAZE_ENTERED`, `GAZE_MOVED`, `GAZE_EXITED`, avec des coordonnées locales au nœud ;
  - `GAZE_MOVED` transporte l'heure d'**entrée** dans le nœud, et non celle de l'échantillon : c'est elle que les jeux utilisent pour mesurer la fixation ; `GAZE_EXITED` transporte `-1` ;
  - le regard et la souris ont des états d'entrée et de sortie **séparés** ;
  - un nœud désactivé ne reçoit rien.

  Aucun code de production n'est modifié par cette caractérisation.
- **`reflections` 0.10.2 supprimé** (non maintenu ; il apportait `javassist`). Son seul usage, la recherche d'images sur le classpath (`ResourceFileManager`), utilise désormais l'API standard (`ClassLoader.getResources`, puis un parcours de répertoire ou de jar), en une quarantaine de lignes.
  - Sémantique conservée : images situées sous le dossier, récursivement, sur toutes les racines du classpath, en chemins séparés par `/`.
  - Seule différence voulue : le préfixe de Reflections était un préfixe de chaîne, si bien que `data/biboule` englobait aussi `data/biboulejump` ; c'est maintenant un dossier.
  - Tests : les nombres attendus par les tests existants sont identiques (1, 2, 35, 38) ; ajout d'un test sur un **vrai jar** (cas des distributions, absent jusqu'ici) et d'un test de référence.
  - Vérifié **dans la distribution Windows construite** : `data/common/default/images` donne 37 images, toutes chargeables. Ce sont les chemins distincts des deux jars qui contiennent ce dossier : les 19 images de `core` existent à l'identique dans `games`, un doublon à nettoyer plus tard. `whereIsTheShape/images` donne 34 images, soit le nombre de fichiers source.
  - Le jeu Divisor, qui utilise ces images par défaut, démarre depuis la distribution extraite.
- Vérification complète : **365 tests, 0 échec** ; `smokeTest`, `spotbugsMain` et `windowsDistZip` passent.
- **Bug corrigé : la vidéo des zones d'intérêt ne pouvait jamais s'afficher.**
  - Avec l'enregistrement vidéo activé, `Stats` reconstruisait le nom du fichier vidéo avec l'heure au format `HH-mm-ss`. Or Monte Screen Recorder nomme lui-même le fichier `ScreenRecording yyyy-MM-dd 'at' HH.mm.ss.avi`, avec des points (vérifié dans son bytecode). `AreaOfInterest` cherchait donc un fichier inexistant ; l'exception était seulement imprimée et aucune vidéo n'apparaissait.
  - Il y avait aussi une course : l'arrêt de l'enregistrement est asynchrone, et le fichier pouvait être lu avant d'être complet.
  - Corrections :
    - `Stats.getVideoFile()` renvoie le fichier réellement créé (`getCreatedMovieFiles()` de Monte) ;
    - `Stats.awaitVideoRecordingEnd(…)` permet d'attendre la fin de l'écriture ; `AreaOfInterest` attend au plus 10 s ;
    - la conversion AVI → MP4 est extraite dans `VideoConverter` ;
    - `getDirectoryOfVideo()` est conservé mais déprécié.
  - Nouveau test `VideoConverterTest` : vrai enregistrement d'écran d'environ 1,5 s, puis conversion en MP4 dans le `user.home` temporaire. **Il passe** (il exige un affichage, comme les autres tests vidéo ; `xvfb` en CI).
  - **Non vérifié** dans l'interface réelle avec un oculomètre.
- **Première extraction du calcul des résultats : `HeatMapGrid`** (commons, sans JavaFX). Elle porte la grille de la heatmap, l'incrément en disque et l'écriture de `heatmap.csv`, que `Stats` lui délègue désormais.
  - **Équivalence démontrée avant la bascule** : 2 000 échantillons pseudo-aléatoires (graine 42, dont certains hors de l'écran) envoyés à l'ancien code de `Stats` et à `HeatMapGrid` donnent des matrices identiques. Ce test de comparaison a été retiré avec l'ancien code.
  - Le CSV est écrit par les mêmes instructions ; son format est figé par `HeatMapGridTest`, fin de ligne système comprise.
  - Les tests de caractérisation (R1, R4, R5), qui passent par `Stats`, restent verts.
- **`FixationSequenceRecorder`** (commons, sans JavaFX ni horloge, le temps est passé en paramètre) : la construction de la séquence de fixations est déplacée **à l'identique** hors de `Stats`, y compris les anomalies R2 et R11, conservées volontairement et documentées dans la classe. Les tests de caractérisation R2 et R11 passent par `Stats` et restent verts. Commons : 168 tests ; games : 74 ; SpotBugs vert.
- **Nouvelle anomalie R13 (caractérisée, non corrigée) : échantillons des zones d'intérêt.** Quand les zones d'intérêt sont activées, le regard conserve chaque changement de position. Pour la souris, la condition `x != previousX || y != previousY && counter == 2` (où `&&` est prioritaire sur `||`) conserve toujours un déplacement horizontal, mais un déplacement vertical seulement un événement sur trois. Le test le confirme : 3 positions conservées sur 5. De plus, la souris utilise les variables nommées « Gaze » et le regard celles nommées « Mouse ». La correction modifierait les zones d'intérêt calculées : c'est une **décision scientifique**. Test : `StatsRecordingCharacterizationTest.shouldKeepTheMouseAndGazeSamplesOfTheAreasOfInterestDifferently`.
- **Doublon supprimé : géométrie des zones d'intérêt.** `calculateRectangle`, `calculateConvexHull` et `orientation` existaient **à l'identique** dans `Stats` (commons) et dans `AreaOfInterest` (core) ; c'est vérifié par une comparaison des corps de méthode, hors commentaires et espaces. Elles sont regroupées dans `AoiGeometry`, dans commons, car elles utilisent `javafx.geometry.Point2D` et ne peuvent donc pas aller dans `gazeplay-results`. Les tests existants (`AreaOfInterestTest`) l'appellent désormais. Vérification : 375 tests verts, `smokeTest` et SpotBugs. Autres doublons structurels relevés dans les jeux, petits et non traités faute de tests : deux `GameSizingComputer` (whereisit et gazeplayEval), et des générateurs de variantes et classes de statistiques identiques deux à deux.
- **Nouveau module `gazeplay-results`**, sans JavaFX : première étape de l'architecture cible. Il contient `HeatMapGrid`, `FixationSequenceRecorder`, `ReplayDataWriter` et `FixationPoint`, déplacés de `commons` avec leurs tests.
  - Le plugin JavaFX n'est pas appliqué à ce module : une classe `javafx.*` n'y compile pas (classpath de compilation sans OpenJFX, vérifié).
  - Les paquets sont conservés (`net.gazeplay.commons.utils…`) : aucun import n'a changé dans les 136 fichiers qui utilisent `FixationPoint`, et la visibilité « paquet » fonctionne sur le classpath.
  - `commons`, `games-commons`, `core` et `games` en dépendent ; le jar est livré dans les distributions (vérifié dans `noJREDistZip`).
  - Le module est ajouté aux commandes de test de la CI et de `BUILD.MD`.
  - Vérification : **375 tests, 0 échec** (results 5, commons 166, games-commons 18, core 112, games 74) ; `smokeTest` et SpotBugs verts.
- **Bug corrigé : sauvegarde des résultats impossible quand la séquence de fixations est désactivée.**
  - Avec le réglage « désactiver la séquence de fixations », `saveStats` levait `IllegalStateException: Not a JSON Array: null`, constat obtenu par un test reproduisant ce cas. `replayData.json` était créé **vide**, puisqu'il était ouvert avant l'erreur, et `heatmap.csv` comme les images `metrics*.png` n'étaient pas écrits.
  - Désormais, la séquence est écrite sous la forme `[[],[]]` : deux listes vides, souris puis regard, la forme attendue par le rejeu, comme pour une manche sans échantillon.
  - Aucune donnée historique valide n'existait dans ce cas, donc aucune compatibilité n'est en jeu.
  - Tests : `StatsTest.shouldSaveStatsWithTheFixationSequenceDisabled` (fichiers réellement produits) et `ReplayDataWriterTest`.
- **Écriture de `replayData.json` extraite** dans `ReplayDataWriter` (commons, sans JavaFX). Les clés, leur ordre et leurs valeurs sont repris à l'identique. Le **texte JSON exact** est figé par `ReplayDataWriterTest`, en plus du test de structure sur un vrai fichier. Vérification : **375 tests, 0 échec** ; `smokeTest` et SpotBugs verts.
- **Format de `replayData.json` figé par test** (`StatsTest.shouldSaveStats`, sur un vrai fichier écrit par `saveStats`) :
  - les 14 clés de premier niveau ;
  - `lifeCycle` avec `startTime` et `stopTime` : `stopTime` est écrit même s'il vaut `null` ;
  - `roundsDurationReport` ;
  - `fixationSequence` : deux listes, souris puis regard, de points `{timeGaze, gazeDuration, x, y}`.

  **Constat** : le fichier est écrit dans l'encodage par défaut de la plateforme (`Charset.defaultCharset()`, Windows-1252 sur la plupart des Windows, UTF-8 ailleurs). Un nom de variante non ASCII n'est donc pas encodé de la même façon d'une plateforme à l'autre. Ce n'est pas modifié, car c'est une question de format (v2).
- **JAVE 2.5.0 (2018) → 3.5.0.**
  - Pourquoi 3.5.0 : c'est la dernière version qui fournit encore un binaire **Windows 32 bits** (la 4.x l'a retiré) ; elle apporte aussi des binaires **ARM 32 bits** (`linux-arm32`) et Apple Silicon (`osxm1`).
  - Les binaires sont déclarés un par un (win64, win32, linux64, osx64, osxm1, linux-arm32) au lieu de `jave-all-deps`. linux32 n'est plus livré, faute de distribution correspondante.
  - Chaque distribution ne garde que le sien. **La distribution ARM a désormais un ffmpeg** (elle n'en avait aucun) ; macOS garde `osx64`, puisque son runtime est x64.
  - API : `EncodingAttributes` et `VideoAttributes` sont passés dans `ws.schild.jave.encode`, et `setFormat` devient `setOutputFormat`.
  - Vérifié par `VideoConverterTest`, qui fait un vrai enregistrement puis une vraie conversion, avec le binaire win64. **Non vérifié** sur ARM ni macOS.
- **Documentation développeur** :
  - `BUILD.MD` est réécrit avec les commandes réellement valides : tests isolés et sans fenêtre, `-Pgui`, `smokeTest`, JaCoCo, SpotBugs et PMD, `./gradlew run`, distributions avec JRE vérifiées, mise à jour d'un runtime, release. L'ancienne version mentionnait une option `-PSpotBugs` inexistante.
  - Nouveau `ARCHITECTURE.md` : modules, démarrage et racine de composition, catalogue des jeux, diffusion du regard, fichiers de résultats, configuration.
- **`./gradlew run` réparé** : il échouait sur `--add-modules requires modules to be specified`, car le plugin JavaFX était appliqué au projet racine sans liste de modules. Vérifié avec `./gradlew run --args="--help"`. Cet essai a utilisé le vrai `user.home` et a seulement ajouté des lignes à `~/GazePlay/gazeplay.log`.
- **CI : nouveau job `package-linux`**.
  - Il construit la vraie distribution Linux avec `linuxDistTar` (JRE et pilotes Tobii téléchargés, sommes SHA-256 vérifiées), l'extrait, puis lance `bin/gazeplay-linux.sh --default-user` sous `xvfb`, depuis un autre répertoire, sans terminal, avec un `HOME` neuf en mode souris.
  - Le job échoue si GazePlay s'arrête avant 40 s, si le catalogue des jeux n'est pas chargé, ou si une exception imprévue apparaît dans `~/GazePlay/gazeplay.log` : le script coupe la sortie console.
  - **Pas encore exécuté** (pas de Linux ici) : surveiller le premier passage sur GitHub, en particulier les dépendances GTK de JavaFX sur le runner et le vidage du journal asynchrone quand `timeout` arrête le processus.
- **`owner` 1.0.12 supprimé** (inactif depuis 2020).
  - Constat : seules `getProperty`, `setProperty` et `store` étaient utilisées, sur l'interface `ApplicationConfig`.
  - Remplacement : `ApplicationConfig` devient une classe d'une trentaine de lignes, adossée à `java.util.Properties`. Son comportement est identique à celui d'`owner`, **vérifié dans son bytecode** : il délègue à `Properties.store` et `Properties.getProperty`, et une valeur nulle supprime la clé.
  - Le **format de `GazePlay.properties` est inchangé** : `ApplicationConfigTest` compare octet pour octet avec `Properties.store`, échappements et caractères accentués compris, hors ligne de date.
  - Les tests de configuration, de migration des anciennes clés et de sauvegarde atomique passent.
  - Vérification : **368 tests, 0 échec** ; `smokeTest` et `spotbugsMain` verts.
- **Tâches de catalogue réécrites** (`generateGamesCatalog`, `generateEvalsCatalog`, dans `core`) :
  - elles accédaient au projet `gazeplay-games` pendant l'exécution (`Task.project`, qui sera une erreur en Gradle 10) ;
  - elles partageaient le même répertoire de sortie et ne déclaraient aucune entrée.

  Désormais, tout est résolu à la configuration, les entrées sont déclarées (régénération seulement si une source change, tâche à jour au second passage) et les listes sont triées (build reproductible). **Contenu identique** à l'ancien, à l'ordre près (comparé : 75 jeux, 2 évaluations) ; catalogues présents dans le jar ; `smokeTest` trouve 75 jeux.
- **Ressources en double** : 58 fichiers de `data/common` (lettres, chiffres, images par défaut, sons) étaient présents à l'identique dans `core` et dans `games` (même chemin, même SHA-256, aucun conflit de contenu ; environ 2,7 Mo). Les copies de `games` sont supprimées ; `data/common` reste dans `core`, déjà présent à l'exécution de tous les jeux. Tous les accès passent par le classpath (vérifié). Les fichiers supprimés sont suivis par git, donc récupérables. Tests et `smokeTest` verts après suppression.

---

## 2i. Étape 9 — Décisions du mainteneur appliquées (20/10/2026, état exact en section 9)

Décisions données le 20/10/2026 :
1. **Pas de versions 32 bits** (Windows x86, ARM 32 bits).
2. **Formats de résultats** : à choisir au mieux. Choix retenu : format v2 versionné (`formatVersion`), fichiers v1 jamais réécrits, lecture du v1 conservée.
3. **melordi et picto-pick** : à retirer s'ils ne servent pas aux jeux de `gazeplay-games`.
4. **Notes de version** : oui. Elles sont rédigées dans le dépôt ; leur publication sur GitHub demande un commit, un push et une release, qui restent soumis à l'accord explicite du mainteneur.

### melordi et picto-pick — RETIRÉS
- **Vérifié avant retrait** :
  - aucune référence depuis les autres modules (code, scripts, configuration), hors `settings.gradle` et la liste des dépendances du projet racine ;
  - aucun fichier de leurs ressources (`melordi/images`, `pictogrammes/images|sounds`) n'est lu ailleurs ;
  - leurs répertoires ne contenaient que des fichiers suivis par git, qui restent récupérables (`git checkout -- gazeplay-melordi gazeplay-picto-pick`).
- Modules supprimés, ainsi que leurs entrées dans `settings.gradle` et `build.gradle`. Gain : environ 11 Mo par distribution (le jar de picto-pick). La compilation passe.

### Versions 32 bits — RETIRÉES ; ARM passe en 64 bits (aarch64)
- **Supprimé** :
  - distributions `windowsx32`, `windowsAfsrx32`, `windowsBerax32` et toutes leurs dépendances de tâches (`build.gradle`, `gradle/package.gradle`) ;
  - installeurs 32 bits (`gradle/installer.gradle`, `afterReleaseBuild`, les 3 fichiers `gradle/innosetup/setup*x32.iss.skel`) ;
  - runtime Zulu `win_i686` (`gradle/jre/windows-32.gradle`, sa somme et sa dépendance dans `downloadAndExtractJREs`) ;
  - binaire ffmpeg `jave-nativebin-win32` ;
  - lignes x32 de `release.yml` (renommage et liste des fichiers publiés).
- **ARM** : la distribution `gazeplay-arm` embarquait Temurin 17 **arm32** et le SDK OpenJFX arm32 17.0.2 (le dernier publié par Gluon). Elle embarque maintenant **Zulu 17.0.20.1 FX `linux_aarch64`**, le même produit que sur les autres plateformes. Le ffmpeg passe de `linux-arm32` à `linux-arm64` (JAVE 3.5.0), et la dépendance inutile aux pilotes Tobii x64 est retirée.
  - **Fait** : la somme SHA-256 est celle de l'API Azul ; l'archive a été téléchargée, vérifiée et extraite. On obtient `bin`, `lib` et les bibliothèques natives JavaFX (`libglass*.so`, `libjfxmedia.so`…). `jave-nativebin-linux-arm64:3.5.0` se résout.
  - **Non testé** sur une vraie machine ARM 64 bits (par exemple Raspberry Pi OS 64 bits).
  - **Choix** : le nom de l'archive reste `gazeplay-arm`, pour ne pas casser les liens de téléchargement existants. Les notes de version précisent qu'elle est désormais en 64 bits.
  - **Conséquence** : un Raspberry Pi sous système 32 bits ne peut plus lancer la distribution ARM.
- Vérifié : le `--dry-run` de `packageApp`, des 3 installeurs, de `downloadAndExtractJREs` et d'`armDistTar` donne un graphe cohérent, sans tâche x32.
- **Hypothèse, piste de taille** : les distributions avec runtime contiennent aussi les jars JavaFX de la plateforme de build. Ils sont masqués par les modules JavaFX du runtime (comportement préexistant, inchangé). Les exclure ferait gagner de la place, mais demande un test de lancement sur chaque plateforme.
- **Ce que cela débloque** : toutes les plateformes restantes (Windows x64, Linux x64, Linux aarch64, macOS x64) ont Zulu 21 FX et Zulu 25 FX. Le passage à Java 21 n'est plus bloqué par le 32 bits.

### Format de résultats v2 — APPLIQUÉ
**Principes retenus**
- Les fichiers existants ne sont **jamais réécrits** ; aucune migration.
- `replayData.json` porte `"formatVersion": 2`, placé en tête. Son absence signifie v1.
- Toutes les clés de la v1 sont conservées, dans le même ordre, avec le même type.
- Le rejeu lit les deux versions (`ReplayDataFormat.fromFile`). Gson ignore les clés inconnues, si bien qu'une ancienne version de GazePlay lit aussi un fichier v2, en inversant toutefois les x/y des fixations (sans effet sur le tracé des échantillons bruts).
- Les CSV n'ont pas d'en-tête, et en ajouter un casserait les lecteurs existants. Leur version est celle du `replayData.json` de la même manche, reconnaissable au même préfixe horaire dans le nom de fichier.

| # | v1 (fichiers historiques) | v2 | Où |
|---|---|---|---|
| R1 | Une seule matrice souris + regard pour `heatmap.csv` et les 3 PNG `metrics*` ; clics, appuis et relâchements enregistrés comme échantillons souris | `heatmap.csv` et `metricsMouseAndGaze.png` gardent la matrice mixte. **Nouveaux** fichiers `-heatmapGaze.csv` et `-heatmapMouse.csv`. `metricsGaze.png` et `metricsMouse.png` utilisent leur propre source. Seuls `MOUSE_MOVED` et `MOUSE_DRAGGED` sont des échantillons | `Stats` |
| R2 | 2ᵉ échantillon jamais fusionné ; durée fusionnée = 2 × le temps écoulé ; un nouveau point reçoit la durée du précédent | Fusion dès le 2ᵉ échantillon (même seuil de 50 px par axe). Durée = du 1ᵉʳ échantillon du point au 1ᵉʳ du point suivant, ou à son dernier échantillon pour le dernier point | `FixationSequenceRecorder` |
| R5 | `coordinatesAndTimeStamp` de la manche N contient les manches 1 à N | Seulement la manche | `Stats.start` |
| R11 | `fixationSequence` : `x` = Y et `y` = X | `x` = X et `y` = Y (pixels, non normalisés, comme en v1). En mémoire, la convention v1 est gardée, car le code de dessin en dépend : l'inversion se fait à l'écriture et à la lecture | `ReplayDataFormat` |
| — | `vertexReduction` ignorait le dernier point (images de scanpath, écran de statistiques) | Dernier point traité. Rien n'est écrit dans un fichier | `FixationSequence` |

**Effets visibles**
- Les cercles du scanpath n'ont plus la même taille relative. Le seuil d'affichage (durée > 100 ms) porte désormais sur une durée réelle : en v1, une fixation de 50 ms environ suffisait à atteindre 100.
- `heatmap.csv` contient moins d'occurrences souris (plus de clics).
- **Non changé, documenté** :
  - échantillonnage à environ 100 Hz (R6) ;
  - taille de cellule dépendant de la mémoire (R7) ;
  - échantillonnage des zones d'intérêt (R13, qui n'est pas écrit sur disque) ;
  - règles de validité différentes entre souris et regard (`x > 0 && y > 0` contre `x > 0 || y > 0`).

**Tests** : `ReplayDataWriterTest` (texte exact v2), `ReplayDataFormatTest` (lecture v1, aller-retour v2), `StatsRecordingCharacterizationTest` (R1, R2, R5, R11, chacun avec la valeur v1 en commentaire), `StatsTest` (`formatVersion`, fichiers par source), `FixationSequenceTest`. Ce dernier test ne vérifiait rien : ses assertions tournaient dans `Platform.runLater`, où un échec n'est pas remonté.

**Données historiques (v1)** : récupération possible hors ligne, depuis `coordinatesAndTimeStamp`.
- R5 : découper aux baisses de `time` et garder le dernier segment.
- R1 : filtrer par `event`. Les clics ne se distinguent pas d'un mouvement à la même position.
- R2 : recalculer depuis les échantillons, avec la règle v2.
- R11 : inverser x/y.
- Aucun outil de retraitement n'est encore écrit (à faire si le laboratoire le demande).

---

## 2j. Étape 10 — Runtime Java 21 et édition du produit (21/10/2026)

### Runtimes Zulu 21 FX — FAIT
- `gradle/jre.gradle` : Zulu **21.52.203 / 21.0.12.1** FX pour Windows x64, Linux x64, Linux aarch64 et macOS x64, avec les 4 sommes SHA-256 publiées par Azul. Les sommes de Zulu 17 sont retirées.
- **Inchangé** : `options.release = 17` et l'API JavaFX 17.0.20 à la compilation. La distribution sans JRE fonctionne donc toujours avec Java 17 ou plus.
- **Fait** : les 4 runtimes ont été téléchargés et leur somme vérifiée par `downloadAndExtractJREs`. Après extraction : `JAVA_VERSION="21.0.12.1"` et **JavaFX 23.0.8** (`lib/javafx.properties`).
- **Bug corrigé, macOS** : l'archive Zulu 21 place le runtime dans `<archive>/Contents/Home`, et non plus dans `<archive>/zulu-21.jre/Contents/Home`. `downloadAndExtractMacosJRE` copiait un dossier inexistant, sans erreur, et la distribution macOS se retrouvait **sans runtime**. La tâche cherche maintenant les deux dispositions et échoue si `bin/java` est introuvable (`gradle/jre/macos.gradle`). On ne sait pas si l'archive Zulu 17.0.20.1 était déjà concernée : elle n'était plus en cache pour vérifier.
- **Vérifié sous Windows x64** :
  - `windowsDistZip` construite, extraite, puis lancée avec sa JRE 21.0.12.1 (`-Duser.home` et `-Dgazeplay.argsFile` temporaires, `--default-user --game WhereIsTheShape`) ;
  - le jeu a tourné 40 s jusqu'au `timeout`, 75 jeux trouvés, aucune erreur ni exception dans le journal, hormis l'absence de `GazePlay.properties`, normale au premier lancement ;
  - `smokeTest` 2/2 sous JDK 21.0.7.
- **Non vérifié** : lancement réel sous Linux, macOS et ARM ; installeurs Inno Setup (Docker).

### Édition du produit au lancement — BUG CORRIGÉ
- **Fait, constaté pendant le test ci-dessus** : `GazePlayLauncher.main` n'écrivait le fichier d'édition que si le premier argument contenait `afsr` ou `bera`, ou en l'absence totale d'argument (via l'exception `ArrayIndexOutOfBounds`).
  - Un lancement avec d'autres arguments (raccourci `--user Bob`, `--default-user`) gardait donc l'édition du lancement précédent.
  - Après une séance BERA, GazePlay n'affichait que les 2 jeux d'évaluation ; un lancement direct d'un jeu échouait (`IllegalArgumentException: WhereIsTheShape`).
  - Ce comportement est préexistant : l'ancien code lisait directement l'ancien emplacement du fichier.
- **Correction** (`GazePlayArgs.editionToSave`, appelée par `GazePlayLauncher.main`) :
  - `afsr` ou `bera` en premier argument (scripts AFSR et BERA) : édition écrite, comme avant ;
  - présence de `--game` ou `--game=` (JVM d'un jeu lancée par le menu ou le rejeu, raccourci direct vers un jeu) : rien n'est écrit et l'édition du lancement parent est héritée, comme avant ;
  - tout autre lancement : `gazeplay`.
- Tests : 3 tests ajoutés dans `GazePlayArgsTest` (9 tests, 0 échec).
- **Risque résiduel** : un raccourci direct vers un jeu (`--game`) sur une machine où le dernier lancement était BERA hérite encore de BERA. C'est voulu pour les JVM enfants ; pour un raccourci, il faut ajouter `--bera` ou `--afsrgazeplay`, ou lancer d'abord GazePlay normalement.

---

### Autres changements de l'étape 10
- **JAVE 3.5.0 → 4.2.0**, possible maintenant que le binaire Windows 32 bits n'est plus nécessaire (la 4.x l'a retiré). JAVE 4.2.0 vise Java 8. Les 5 binaires (`win64`, `linux64`, `linux-arm64`, `osx64`, `osxm1`) se résolvent ; l'API utilisée par `VideoConverter` n'a pas changé. Vérifié par `VideoConverterTest`, avec un vrai enregistrement et une vraie conversion via le binaire win64. **Non vérifié** sur Linux, macOS ni ARM.
- **Premier lancement** : l'absence de `GazePlay.properties` (premier lancement, nouveau profil) n'est plus journalisée en `ERROR` avec une pile d'appels, mais en `INFO`. Comportement inchangé : réglages par défaut. Couvert par `ConfigurationSourceTest`.
- **Gradle** : une seule dépréciation à la configuration, `StartParameter.newInstance()`, qui sera supprimée dans Gradle 10. Elle vient du plugin `net.researchgate.release` 3.2.0, déjà à sa dernière version (09/2026). `javafxplugin` 0.1.0 est lui aussi à sa dernière version et ne produit pas d'avertissement. **Recommandation** : surveiller une version 3.3 du plugin `release`. À défaut, remplacer `./gradlew release` par une tâche locale (modifier `gradle.properties`, tag) avant Gradle 10. Cela touche le processus de publication (`release.yml`), donc à décider avec le mainteneur.
- **Ressources mortes — SUPPRIMÉES le 21/10/2026, avec l'accord du mainteneur** (le garde-fou local avait d'abord refusé la commande). Après suppression : tests de `core` (115) et des jeux (75) verts, `smokeTest` 2/2 ; le jar `gazeplay-core` passe de 77,6 à 34,6 Mo. Fichiers :
  - `gazeplay-core/src/main/resources/data/common/images/back.gif` (40 Mo, idée de fond de menu de 2018, jamais référencée) ;
  - `data/birds/` (images de 2017 d'un jeu jamais réalisé) ;
  - `data/common/old-default-images/` (14 images).
  - Au total : 21 fichiers suivis par git, 44 Mo dans **chaque** distribution (environ 7 % de l'archive Windows).
  - Vérifié : aucune référence littérale dans le code ni dans les fichiers de données ; les bibliothèques d'images dynamiques ne visent que `data/common/default/images` et des dossiers propres aux jeux.
  - Récupérables par git (`git checkout -- <chemin>`) tant que la suppression n'est pas commitée.
- **Nouvelle distribution macOS Apple Silicon** (`gazeplay-macos-aarch64`) :
  - runtime Zulu 21 FX `macosx_aarch64` (somme Azul vérifiée au téléchargement), ffmpeg `osxm1`, mêmes scripts de lancement que macOS x64 ;
  - la distribution x64 (`gazeplay-macos`) garde son nom ; sur Apple Silicon, elle tournait sous Rosetta 2 ;
  - `gradle/jre/macos.gradle` génère les tâches des deux architectures (noms x64 inchangés) ;
  - `build.gradle`, `gradle/package.gradle`, `gradle/jre.gradle` et `release.yml` sont mis à jour. Dans `release.yml`, l'archive aarch64 est renommée d'abord, car `gazeplay-macos*` la capturait aussi.
  - **Vérifié** : `macosDistTar` et `macosAarch64DistTar` construites ; chaque archive contient `lib/jre/bin/java` (`OS_ARCH` x86_64 et aarch64), son seul jar ffmpeg et `bin/gazeplay-macos.sh`.
  - **Non testé** sur un Mac.
- **Zones d'intérêt : calcul extrait de `Stats` vers `gazeplay-results`** (architecture cible) :
  - caractérisation d'abord, dans `StatsRecordingCharacterizationTest.shouldComputeTheAreasOfInterestFromTheGazeSamples`, des groupes, des indices de début et de fin et des rectangles produits pendant une partie ; le test passait sur l'ancien code avant l'extraction ;
  - nouveau `AreaOfInterestGrouper` (sans JavaFX) : règles inchangées, à savoir 150 px, plus de 10 ms, plus de 2 échantillons, saut de plus de 700 px et marge de 15 px. Tests : `AreaOfInterestGrouperTest` (5 tests) ;
  - `CoordinatesTracker` passe dans `gazeplay-results`, avec le même paquet ;
  - `AoiGeometry.calculateRectangle` délègue au même calcul (une seule implémentation au lieu de deux) ;
  - `Stats.generateAOIList` ne garde que la conversion en `Polygon` JavaFX et la couleur. Les listes lues par l'écran `AreaOfInterest` sont inchangées ;
  - **non corrigé** : R13 (échantillonnage de la souris, décidé dans `Stats`). Il est décrit et figé par le test existant, et sa correction change les zones calculées (décision métier).
- **Avertissements de compilation** : recensés avec `-Xlint:deprecation,unchecked` via un script d'initialisation temporaire, sans changer le build. **102 avertissements** : 23 de dépréciation et 79 `unchecked` (types bruts, surtout `Target` de *bubbles* et *order*, `GazeIndicator`, `Strategy` de *cups*, `ColorToolBox`).
  - **Corrigé** : l'API Gson dépréciée `new JsonParser().parse(reader)` est remplacée par `JsonParser.parseReader(reader)`, vers laquelle l'ancienne méthode délègue, dans 5 jeux (*Dot to Dot*, *Goose Game* ×2, *Horses*, *Sounds of Life*, *Spot the Differences*). Ces jeux n'avaient aucun test : `JsonResourcesTest` lit maintenant les 55 fichiers JSON de données des jeux avec cette API (tous valides).
  - **Laissé volontairement** :
    - `GazeplayEval` (même API) : toute évolution d'Eval part de `GazePlayLearning` ;
    - `java.util.Observable` dans `SavedStatsInfo` : API publique utilisée par l'écran de statistiques ;
    - `getDurationRatio()` : valeur ponctuelle, pas une liaison ;
    - ~~les 79 `unchecked`~~ : **ramenés à 15** le 21/10/2026, par des corrections de typage que le compilateur valide entièrement, sans changer le comportement :
      - `EventHandler`, `ChangeListener` et `ArrayList` bruts typés dans *GazeIndicator*, *bubbles*/*order* `Target`, *ColorToolBox*, *ColorsGame*, *Difference*, *Strategy*, *BibouleJump*, *SpaceGame*, *Horses*, *ScreenCake*/*CakeFactory* ;
      - contrôles de vitesse, de taille et de fixation, et favoris du menu, dans `core` ;
      - suite complète verte ensuite (393 tests, `smokeTest`, SpotBugs).
    - **Les 15 restants sont structurels** :
      - tableaux génériques en varargs (`StatDisplayUtils`) ;
      - réflexion (`GameSpecSourceInstantiator`, `IGameVariant`) ;
      - type brut `IGameLauncher` du contrat des jeux (`GameMenuController`, `ReplayingGameFromJson`) : le typer change le contrat ;
      - conversions JavaFX (`GazeEvent.getEventType`, `PropertiesStringSetProperty`) ;
      - `side-projects`.
- **Cache de configuration Gradle** : essai fait.
  - Seul blocage dans le build : le manifeste des jars (`Class-Path`) lisait `configurations` depuis une closure exécutée à l'écriture du jar. Corrigé dans les 4 modules (`commons`, `games-commons`, `core`, `games`) en capturant la collection de fichiers avant `jar {}` ; **manifestes vérifiés identiques** au caractère près.
  - `compileJava`, `jar` et les tâches de test fonctionnent avec `--configuration-cache` : stockage puis réutilisation vérifiés.
  - Reste incompatible : `generateLicenseReport` (plugin `com.github.jk1.dependency-license-report` 3.1.4, déjà à sa dernière version), utilisé par le packaging.
  - **Décision** : le cache n'est pas activé par défaut dans `gradle.properties`, que le plugin `release` réécrit et dont dépend le packaging. On peut l'activer à la demande pour le développement (`./gradlew --configuration-cache ...`), comme indiqué dans `BUILD.MD`.
- **Préparation de `GazeSource` : `clear()` pendant la diffusion, caractérisé.**
  - **Fait** : `AbstractGazeDeviceManager` envoie les événements par `Platform.runLater` pendant le parcours ; les gestionnaires s'exécutent donc après le parcours, une fois le verrou relâché. Un gestionnaire qui appelle `clear()` sur `GAZE_ENTERED` ne provoque pas d'exception : tous les nœuds touchés par l'échantillon reçoivent leur événement, puis plus rien n'est envoyé.
  - Figé par `GazeDispatchCharacterizationTest.shouldLetAHandlerClearTheManagerDuringTheDispatch` (stable sur 3 exécutions).
  - **Risque pour la refonte** : une diffusion synchrone (un seul passage sur le thread JavaFX) ferait tourner les gestionnaires pendant le parcours. Avec le verrou réentrant, `clear()` viderait alors la map en cours de parcours (`ConcurrentModificationException`). La refonte doit parcourir une copie de la liste des nœuds. Ce test est le garde-fou qui permet maintenant de l'entreprendre.
- **Refonte `GazeSource` — FAITE** (21/10/2026), sous `GazeDispatchCharacterizationTest`, dont les 5 tests existants passent sans modification.
  - `GazeSample(time, screenPosition, source)` remplace le couple `Point2D` + chaîne `"gaze"`/`"mouse"`. `Source.eventName()` donne les valeurs `event` de `replayData.json`, qui sont inchangées. `time` est l'heure d'acquisition, prise sur le thread de la source.
  - **Un seul saut vers le thread JavaFX par échantillon** (`AbstractGazeDeviceManager.onGazeSample`), contre un `Platform.runLater` par nœud et par échantillon auparavant, soit environ 100 Hz × nombre de nœuds. Les événements sont envoyés directement, en parcourant une **copie** des nœuds, et un gestionnaire peut vider ou modifier la liste pendant la diffusion (cas caractérisé).
  - `GazeSource` (interface) : `TobiiGazeSource`, qui garde le poller de 10 ms, `EyeTribeGazeSource` et `GazeSource.NONE` pour le mode souris. Un seul gestionnaire, `GazeSourceDeviceManager`, remplace `TobiiGazeDeviceManager`, `EyeTribeGazeDeviceManager` et la classe anonyme du mode souris. Les deux fichiers ont été renommés puis réécrits. `PositionPollerRunnable` livre ses échantillons à un `Consumer<GazeSample>` et ne dépend plus du gestionnaire.
  - **Bug corrigé, EyeTribe** : les positions étaient diffusées sur le thread du SDK EyeTribe, et `screenToLocal` et `contains` des nœuds JavaFX s'exécutaient hors du thread JavaFX. Elles passent désormais par le thread JavaFX. Couvert par le test `shouldDispatchOnTheJavaFxThreadASampleGivenByAnotherThread`.
  - **Effet de bord favorable** : le temps porté par `GAZE_ENTERED` et `GAZE_MOVED` est lu au moment de l'échantillon. Avant, il était lu à l'exécution différée de la lambda, et un `GAZE_ENTERED` traité après un `GAZE_EXITED` pouvait porter `-1`.
  - **Changement de comportement possible, documenté** : les gestionnaires s'exécutent maintenant pendant le parcours, et non plus après lui. Si le gestionnaire d'un nœud désactive un autre nœud touché par le même échantillon, ce dernier ne reçoit plus l'événement de cet échantillon : avant, il le recevait, parce que `isDisable()` était lu avant tout gestionnaire. Aucun test ni jeu identifié ne repose sur ce cas.
  - Tests ajoutés : 2 dans `GazeDispatchCharacterizationTest` (thread JavaFX, noms des sources) et 1 dans `PositionPollerRunnableTest` (échantillon livré au consommateur). Les 7 tests de diffusion ont donné le même résultat sur 3 exécutions. Suite complète : 397 tests, 0 échec.
  - **Non testé** : avec un vrai Tobii ou EyeTribe (pas de matériel). Le `smokeTest` démarre sans appareil (aucun échantillon, R9).
- **Module `gazeplay-gaze` — CRÉÉ** (architecture cible de CLAUDE.md) :
  - **contenu**, l'acquisition du regard : `GazeSample`, `GazeEvent`, `GazeMotionListener`, `EyeTracker`, `GazeSource`, `TobiiGazeSource`, `EyeTribeGazeSource`, `PositionPollerRunnable` et son test, avec les dépendances natives (`TobiiStreamEngineForJava` 5.0 et `EyeTribeJavaFx.jar`), retirées de `commons` ;
  - **reste dans `commons`**, la diffusion : `AbstractGazeDeviceManager`, `GazeSourceDeviceManager`, `GazeDeviceManager`, la fabrique, `GazeInfos`, `IdentityKey`, `SecondScreen`, `Lighting`. Ces classes dépendent de `Stats` (`addStats`) et de la configuration ; les sortir créerait un cycle (`Stats` utilise `GazeEvent`) ou changerait le contrat des jeux (`GazeDeviceManager.addStats(Stats)`) ;
  - **paquets inchangés**, comme pour `gazeplay-results` : aucun import ne change dans les jeux. Les modules `commons`, `games-commons`, `core`, `games` et `side-projects` déclarent `gazeplay-gaze` ;
  - **dépendance morte retirée** : `TobiiStreamEngineForJava` dans `gazeplay-games`, vestige de l'appel direct de `PetHouse`, déjà corrigé ; plus aucun `import tobii` dans les jeux ;
  - l'exclusion SpotBugs de `PositionPollerRunnable` (sentinelle R9) migre avec la classe ;
  - la CI (`ci.yml`) et `BUILD.MD` lancent `:gazeplay-gaze:test` ;
  - **vérifié** : le classpath d'exécution de la distribution contient `gazeplay-gaze`, Tobii et EyeTribe ; la suite complète donne 397 tests, 0 échec (gaze 3), avec `smokeTest` (vraie JVM enfant) et SpotBugs, y compris sur `gazeplay-gaze`.
- **Mesure** : dans la distribution Windows, la JRE pèse 258 Mo, `gazeplay-games` 265 Mo, `gazeplay-core` 78 Mo (dont 40 Mo pour `back.gif`), le ffmpeg win64 26 Mo et les jars JavaFX inutilisés 10 Mo. Les exclure ne ferait gagner qu'environ 1,5 % : ce n'est pas prioritaire.

---

## 2k. Étape 11 — Retrait des éditions AFSR et BERA et des restes « emmanuel » : seul GazePlay normal reste (21/10/2026)

**Décision du mainteneur** (21/10/2026) : retirer les modes de lancement AFSR et « emmanuel », et garder les autres.

- **emmanuel** : aucune trace dans le code ni le build (le module `gazeplay-emmanuel` avait déjà disparu). Il restait :
  - la mention dans `INTELLIJ.md`, retirée ;
  - des fichiers **locaux** d'IntelliJ (`.idea/`, ignorés par git) : la configuration Spring Boot `EmmanuelLauncher` (option `--emmanuel`, rejetée par picocli avec `Unknown option`) et le dossier `.idea/modules/gazeplay-emmanuel`. Ils ne sont pas modifiés ici, car ce sont des réglages locaux de l'IDE, que l'utilisateur supprime dans IntelliJ (voir `INTELLIJ.md`).
- **AFSR retiré** :
  - l'option `--afsrgazeplay` (`VariantSelectionOptions`) et l'écriture de l'édition `afsrGazeplay` (`GazePlayArgs.editionToSave`) ;
  - les écrans AFSR : accueil (`HomeMenuScreen.afsrGazeplayHomeMenuScreen`), chargement (`LoadingContext.afsrGazePlayLoadindContext`), profils (`UserProfileContext.afsrGazeplayUserProfileContext`), ainsi que le logo `Logo-AFSR.png` ;
  - les scripts `gazeplay-afsr-{windows.bat,linux.sh,macos.sh,arm.sh}` et leur inclusion dans les distributions Linux, macOS et ARM ;
  - la distribution `windowsAfsr` (`gazeplay-afsr-windows-x64`), son installeur (`installer.gradle`, `setupAfsr.iss.skel`, `afterReleaseBuild`), son entrée dans `package.gradle` et dans `release.yml` ;
  - la configuration IntelliJ `.run/GazePlay AFSR.run.xml`.
- **Conservé** : GazePlay normal, BERA (Eval) et toutes les autres options (`--user`, `--default-user`, `--game`, `--variant`, `--random-game`, `--width`/`--height`, `--replayJsonFile`, `--help`).
- **Conservé volontairement — question au mainteneur** : les thèmes de couleurs « AFSR-Green » et « AFSR-Purple » (`BuiltInUiTheme`). Ils sont proposés à tous les utilisateurs dans les réglages, et une valeur enregistrée dans `GazePlay.properties` peut les désigner : les retirer modifierait silencieusement des réglages existants. À retirer seulement sur décision explicite, avec un repli documenté vers le thème par défaut.
- **Compatibilité** :
  - une valeur `afsrGazeplay` laissée dans `argsGazeplay.txt` par une ancienne version donne GazePlay normal ;
  - un raccourci existant qui passe `--afsrgazeplay` échoue avec `Unknown option`. Les installations AFSR existantes gardent leurs propres scripts et ne sont pas touchées.
- **Défaut corrigé au passage** : `CatalogBasedGamesLocator` gardait le nom du catalogue dans un champ **statique**, passé au catalogue Eval dès que l'édition contenait `bera` et jamais remis. Dans une même JVM, tout chargement suivant restait sur le catalogue Eval. C'était sans effet en production, où chaque lancement est une JVM neuve, mais cela rendait les tests dépendants de leur ordre. Le catalogue est maintenant choisi à chaque appel.
- **Tests** :
  - `CatalogBasedGamesLocatorTest` (nouveau) : gazeplay → catalogue des jeux, bera → 2 évaluations, afsrGazeplay → catalogue des jeux ;
  - `GazePlayArgsTest` adapté (9 tests).
- **Vérifié** : compilation ; `--dry-run` de `packageApp` et des installeurs, sans aucune tâche AFSR ; suite complète **398 tests, 0 échec** (results 14, gaze 3, commons 172, games-commons 18, core 116, games 75), `smokeTest` 2/2 et SpotBugs OK.

---

### Suite : retrait de BERA et des thèmes AFSR (21/10/2026)

**Décision du mainteneur** (21/10/2026) : « tout enlever sauf GazePlay normal ; BERA et AFSR ne sont plus utilisés ». À la question posée, la réponse a été : **retirer le mode BERA, garder le code Eval**.

- **Thèmes AFSR retirés** : `AFSRGREEN`, `AFSRPURPLE` (`BuiltInUiTheme`), ainsi que `afsr-green.css` et `afsr-purple.css`.
  - **Impact sur les réglages existants** : une valeur enregistrée `builtin:AFSRGREEN` ou `builtin:AFSRPURPLE` n'est plus reconnue. `CssUtil` et l'écran de réglages appliquent alors le thème par défaut (Silver and Gold). La valeur enregistrée n'est pas réécrite tant que l'utilisateur ne choisit pas un autre thème.
  - Test : `BuiltInUiThemeTest`.
- **Mode BERA retiré** :
  - l'option `--bera` ;
  - l'écriture de l'édition `bera` ;
  - les scripts `gazeplay-bera-{windows.bat,linux.sh,macos.sh,arm.sh}` et leur inclusion dans les archives ;
  - la distribution `windowsBera` (`gazeplay-bera-windows-x64`) et son installeur (`installer.gradle`, `setupBera.iss.skel`) ;
  - les entrées de `package.gradle` et de `release.yml` ;
  - `.run/GazePlay BERA (Eval).run.xml`.
- **Lecture de l'édition neutralisée** : `GazePlayArgs.returnArgs()` ne renvoie plus que les éditions encore prises en charge (`gazeplay`). Une valeur `bera` ou `afsrGazeplay` laissée par une version précédente, y compris dans l'ancien emplacement `Documents\Gazeplay`, est lue comme `gazeplay`. Le code BERA ne peut donc plus être atteint, même par un ancien fichier d'édition.
- **Code Eval conservé, dormant**, pour une éventuelle fusion de `GazePlayLearning` :
  - les jeux `gazeplayEval` et `gazeplayEvalTest`, `EvalReport` et ses tests ;
  - le catalogue `evals-catalog.txt`, toujours généré ;
  - les branches `bera` de `core` (`ConfigurationContext.beraConfigGridPane`, `StatsContext`, `GameMenuController`, `CatalogBasedGamesLocator`) ;
  - les clés de configuration « Bera settings » de `GazePlay.properties`, qui sont un format persistant.

  **À décider plus tard** : supprimer ce code dormant, ou le réactiver lors de la fusion de `GazePlayLearning`.
- **Tests** :
  - `GazePlayArgsTest` : les éditions retirées sont lues comme `gazeplay` ;
  - `CatalogBasedGamesLocatorTest` : catalogue des jeux quel que soit le fichier d'édition ;
  - `BuiltInUiThemeTest`.
- **Vérifié** : compilation ; `--dry-run` de `packageApp`, de l'installeur et de `downloadAndExtractJREs`, sans aucune tâche BERA ni AFSR ; suite complète **401 tests, 0 échec** (results 14, gaze 3, commons 174, games-commons 18, core 117, games 75), `smokeTest` 2/2 et SpotBugs OK.

---

## 2l. Étape 12 — Journalisation des erreurs (21/10/2026)

- **Fait** : le code de production contenait 23 `e.printStackTrace()` et 17 `System.out.println`. Ils passaient par `sysout-over-slf4j`, qui redirige `System.out` et `System.err` vers le journal, mais sans message, sans niveau ni contexte.
- **Remplacés** par des appels SLF4J avec un message propre à chaque cas, en conservant l'exception :
  - `log.error` quand une fonction échoue (enregistrement vidéo, rejeu, lancement d'un jeu, lecture MIDI…) ;
  - `log.warn` pour les échecs non bloquants (ouverture d'un dossier, raccourci, image de profil, meilleurs scores, attente interrompue) ;
  - `log.debug` pour les anciennes traces de débogage (*Simon*, *Gaze Race*, *Ladder*, *Train Switches*, dialogue des variantes, import).
- `@Slf4j` est ajouté aux 5 classes qui n'avaient pas de logger. Le commentaire « TODO Auto-generated catch block » de *Piano* est retiré.
- **Laissé volontairement** : `GazeplayEval`, selon la règle d'Eval.
- **Comportement inchangé** : les traitements d'erreur restent les mêmes (aucune exception relancée en plus, aucun flux modifié). Seul le contenu du journal change : un message et un niveau au lieu d'une pile brute.
- `sysout-over-slf4j` est conservé : il capture les sorties des bibliothèques tierces (enregistreur Monte, SDK).
- **Vérifié** : compilation ; suite complète 401 tests, 0 échec ; `smokeTest` et SpotBugs OK.

### Versions et nouveaux détecteurs SpotBugs (21/10/2026)
- **Audit des versions** (Maven Central, 21/10/2026) : toutes les bibliothèques sont à leur dernière version stable, sauf deux, mises à jour :
  - **logback 1.5.38 → 1.6.5**. Il demande Java 11 et SLF4J 2.0.19, ce que le projet satisfait (Java 17, SLF4J 2.0.20). Vérifié : le journal du `smokeTest` est écrit normalement.
  - **outil SpotBugs 4.9.3 → 4.10.5** (plugin Gradle 6.5.13 inchangé). Ses nouveaux détecteurs ont signalé 7 points, tous corrigés (aucune exclusion ajoutée) :
    - **bug réel, *Biboule Jump* et *Space Game*** : le fichier des meilleurs scores (`statistics/<profil>/biboule-jump/highscores.dat`, `.../space-game/highscore.dat`) était créé dans un dossier qui n'existait qu'une fois des statistiques enregistrées pour le jeu. Avant cela, le score n'était pas enregistré (`createNewFile` échouait). Le fichier restait aussi ouvert après sa lecture. Le code, dupliqué dans les deux jeux, est extrait dans `net.gazeplay.games.HighScores`, qui crée le dossier et ferme le fichier ; **format et emplacement inchangés** (`120:340:560:`). Test : `HighScoresTest` ;
    - `ResourceFileManager` : appel de `getFileName()` sécurisé ;
    - `ForegroundSoundsUtils` : `static synchronized` remplacé par un verrou privé ;
    - `AbstractGazeDeviceManager` : la map utilisée comme verrou de la diffusion n'est plus exposée (les `@Getter`, inutilisés, sont retirés), et le champ public `gameScene` devient privé (inutilisé hors de la classe).
- **Vérifié** : suite complète **403 tests, 0 échec** (results 14, gaze 3, commons 174, games-commons 18, core 117, games 77) ; `smokeTest` 2/2 ; SpotBugs 4.10.5 vert sur tous les modules.

---

## 2m. Première exécution de la CI sur GitHub (21/10/2026)

Branche poussée sur `origin/addClaudeCode` à la demande du mainteneur. Seul `ci.yml` se déclenche sur un push : `cd.yml` ne réagit qu'à `master` et `release.yml` se lance à la main. Run 39586962314 :
- **SpotBugs** : vert.
- **Tests (JDK 17 et 21, Linux)** : **tous les tests unitaires passent**. Seul `smokeTest`, dans son cas « menu », échoue sur `MediaException: Could not create player!` pour la musique de fond.
  - **Cause** : sous Linux, JavaFX lit l'audio avec le ffmpeg du système, absent de la machine GitHub.
  - De plus, le JavaFX 17.0.20 des tests ne gère que ffmpeg 4 et 5 (`libavplugin-ffmpeg-56` à `59`, vérifié dans le jar), alors qu'`ubuntu-latest` (24.04) fournit ffmpeg 6.1.
  - **Correctif** : job de tests sur `ubuntu-22.04` (ffmpeg 4.4), avec installation de `ffmpeg`. Le test reste strict ; l'erreur n'est pas ajoutée aux erreurs tolérées.
- **package-linux** : la distribution a été construite et lancée.
  - **Erreur de ma part** : la vérification isolait le dossier utilisateur avec `HOME=...`, or Java sous Linux prend `user.home` dans la base des comptes, pas dans `$HOME`. GazePlay a donc écrit son journal dans `/home/runner/GazePlay`, le `tail` du journal attendu a échoué et `set -e` a arrêté le script. Le réglage du mode souris n'avait pas été lu non plus.
  - **Correctif** : `JAVA_TOOL_OPTIONS=-Duser.home=/tmp/gazeplay-home`, `ffmpeg` installé (le runtime livré, avec JavaFX 23, gère ffmpeg 6), et `tail` tolérant pour que le diagnostic s'affiche toujours.
- **Deuxième run (39588142673)** : `package-linux` lit désormais le bon dossier utilisateur (journal dans `/tmp/gazeplay-home`). L'application démarre, charge le catalogue et tourne 40 s, mais la musique échoue encore, comme dans `smokeTest`, alors que ffmpeg est installé. **L'hypothèse ffmpeg seule était donc incomplète.** Hypothèse retenue : la machine GitHub n'a **aucune sortie audio**, et le moteur multimédia de JavaFX sous Linux (GStreamer, sortie ALSA) ne peut pas créer de lecteur sans carte son.
  - **Correctif proposé** : sortie audio virtuelle. PulseAudio démarre avec un périphérique de sortie « null » et ALSA est routé vers PulseAudio (`libasound2-plugins`, `/etc/asound.conf`).
  - `HOME=` est retiré du lancement de la distribution (`user.home` passe par `JAVA_TOOL_OPTIONS`), afin que le client PulseAudio retrouve sa configuration.
  - Les tests restent stricts.
- **Troisième run (39589291873) : entièrement vert.** SpotBugs, tests JDK 17 et 21 sous Linux (avec `smokeTest`), construction et lancement réel de la distribution Linux sans aucune erreur dans le journal. L'hypothèse de la sortie audio est **confirmée**.
- **Constat pour les utilisateurs** : sous Linux, la musique et les sons de GazePlay demandent les bibliothèques ffmpeg du système. La distribution Linux (JavaFX 23) gère ffmpeg 4 à 7 environ (`libavplugin-ffmpeg-56` à `62`). La version sans JRE, avec le JavaFX 17 de Maven, ne gère que ffmpeg 4 et 5 : sons muets sur Ubuntu 24.04 (comportement préexistant). **Recommandation** : l'indiquer dans la documentation d'installation Linux (`sudo apt install ffmpeg`).

---

## 2n. Étape 13 — Actions GitHub et guides d'installation (21/10/2026)

- **Actions GitHub** : le run vert signalait que les actions v4 tournaient sur Node.js 20, déprécié, et que `setup-java` v4 n'est plus maintenu. Passage aux dernières versions majeures, sans changement incompatible pour notre usage (notes de version lues) :
  - `actions/checkout` v7, `actions/setup-java` v6, `actions/upload-artifact` v7, `actions/download-artifact` v8. Depuis v8, le contrôle d'empreinte des téléchargements est obligatoire, ce qui ne concerne que `cd.yml`, entre ses deux jobs ;
  - actions tierces **épinglées par SHA**, comme `free-disk-space`, car elles disposent d'un jeton d'écriture : `ncipollo/release-action` v1.21.0, `peter-evans/create-issue-from-file` v6.0.0 ;
  - `release.yml` : l'étape `setup-node` (Node.js 16, en fin de vie depuis 2023) est retirée, car rien n'utilisait Node.js.
- **Machines de CI** : `package-linux` est épinglé sur `ubuntu-24.04`, où la sortie audio virtuelle a été validée. `ubuntu-latest` passe à Ubuntu 26 à partir du 19/10/2026. Les tests restent sur `ubuntu-22.04` (ffmpeg 4.4 pour JavaFX 17).
- **Guides d'installation pour les développeurs** (`INSTALLATION-EN.MD`, `INSTALLATION-FR.MD`) : ils imposaient IntelliJ **Ultimate**, à cause de Spring, et s'appuyaient sur la configuration de lancement créée automatiquement par Spring. Désormais :
  - IntelliJ Community suffit ;
  - Gradle est utilisé pour le build et les tests ;
  - le lancement passe par la configuration partagée **GazePlay** (`.run/`), avec un lien vers `INTELLIJ.md` ;
  - la mention « azul-11 » est corrigée en « azul-17 ».
- **Pages d'installation du site** (`docs/_pages/installation.md`, version française) : note sur ffmpeg, nécessaire au son sous Linux. **Non fait, à signaler** : ces pages pointent encore vers l'installeur 1.7.2.
- **Vérifié** : la CI s'exécutera au prochain push.

---

## 2o. Étape 14 — Bilan complet : code mort et versions (21/10/2026)

Demande du mainteneur : vérifier que tout l'inutilisé est retiré et que toutes les dépendances sont à leur dernière version. Le code Eval est laissé de côté (« j'y reviendrai plus tard »).

### Versions (Maven Central, Gradle Plugin Portal, Azul ; vérifiées le 21/10/2026)
- **À jour** : toutes les bibliothèques du catalogue, les 6 plugins, Gradle 9.8.2, Zulu 21.0.12.1 FX, Lombok, JaCoCo, SpotBugs 4.10.5.
- **Mis à jour** :
  - **JavaFX 17.0.20 → 17.0.21** (API de compilation et des tests). Le commentaire du catalogue, devenu faux (« Same JavaFX as the bundled runtimes »), est corrigé ;
  - **PMD 7.24.0 → 7.28.0**. La version n'était pas déclarée : c'était celle par défaut de Gradle. Elle est maintenant fixée (`pmd` dans le catalogue, `gradle/pmd.gradle`).
- **Exceptions justifiées** :
  - Monocle reste en 17.0.10, car la 21.0.2 exige Java 21 pour exécuter les tests ;
  - JavaFX reste en 17.x à la compilation (version la plus ancienne prise en charge, voir 2g) ;
  - les dépendances transitives anciennes (`joda-time` 2.10.2 et `commons-collections` 3.2.2, apportées par `everit-json-schema`) ne sont pas forcées.

### Dépendances inutilisées
- Chaque dépendance déclarée a été vérifiée module par module (imports et noms complets). Le catalogue n'a plus aucune entrée inutilisée (contrôle automatique).
- **Retirées** : `rxtx` et le module qui l'utilisait (voir ci-dessous).
- **Justifiées** : `commons-io` dans `core` (utilisé par son nom complet). Les ponts `log4j-over-slf4j` (Log4j 1.2 appelé par `video-recorder-core`) et `jcl-over-slf4j` (Commons Logging appelé par `commons-validator` et `commons-digester`, via `everit-json-schema`) ont été vérifiés dans le bytecode des jars livrés.

### Code mort retiré (vérifié : aucune référence, ni par réflexion, ni dans les données, ni dans `side-projects` pour les 8 premières)
- 8 classes jamais référencées :
  - `ColorBands`, `DllUtils`, `EagerImageLibrary`, `ConvexHullProps` (commons) ;
  - `IntStringListVariantGenerator`, `StackPaneButton` (games-commons) ;
  - `ProgressiveColorPicker`, `MouseArrowsV1` (games).
- `ImageDirectoryLocator` et son test : la classe ne servait qu'à ce test.
- **Module `gazeplay-side-projects` (décision du mainteneur)** : expériences non livrées (Arduino « windmill », simulateur d'éclairs, TestGaze). Avec lui disparaissent :
  - `SecondScreen`, `SecondScreenFactory` et `Lighting` de `commons.gaze` (écran secondaire « éclairé » par le regard) et leurs tests ;
  - la dépendance RXTX ;
  - l'exclusion PMD.

  La fonction « double écran » de GazePlay (`CurrentScreenSupplier`) n'est pas concernée.
- Dossiers et fichiers morts :
  - `gazeplay-api/` (un `MavenWrapperDownloader.java` orphelin) ;
  - `old-website/` (24 Mo, aucune référence, dernière modification en 2019 ; le site est dans `docs/`) ;
  - `lib/RXTXcomm.jar` ;
  - `GazePlay.properties` (2019, chemin `/Users/admin`) et `argsGazeplay.txt` à la racine, fichiers d'exécution commités par erreur. Le `.gitignore` les exclut désormais, avec `gazeplay.log`.
- Ressources : `data/gazeMediaPlayer` (icônes d'un jeu retiré) et 4 vignettes orphelines (`gazeMedia.png`, `gazePlay.jpg`, `videogrid.png`, `youtube.png`). Environ 1,4 Mo. Le site charge ses vignettes depuis `master` : il n'est pas affecté.
- PMD : les exclusions de modules supprimés (`melordi`, `picto-pick`, `side-projects`, `org/tc33`) sont retirées du jeu de règles.

### Contrôles sans résultat (rien à retirer)
- PMD 7.28.0 sur tous les modules : **aucun** membre privé, variable ou import inutilisé (`Unused*`, `UnnecessaryImport`).
- Tous les dossiers de `data/` et sous-dossiers de `data/common` sont référencés. Le `data/common/default` de `games` (images sans fond du labyrinthe) n'est pas un doublon de celui de `core`.
- Toutes les ressources de test sont utilisées. Tous les scripts de `gradle/` sont appliqués.

### Non vérifié, signalé
- **Méthodes publiques inutilisées** : non recherchées (pas d'outil fiable sans analyse globale ; JavaFX et Lombok créent des appels implicites).
- **Clés de traduction inutilisées** (`multilinguism.csv`, fichiers des jeux) : non retirées. Une partie des clés est construite dynamiquement (noms de jeux, catégories, variantes), donc un contrôle par recherche textuelle donnerait des faux positifs.
- **Site** (`docs/_pages/games.md`) : il présente 3 jeux qui n'existent plus dans le code (*VideoPlayer with Feedback*, *Media Player*, *Video Grid*) et pointe vers l'installeur 1.7.2.
- Code Eval : laissé de côté, à la demande du mainteneur.

### Vérifié
Suite complète **395 tests, 0 échec** (8 tests de moins : ceux des classes supprimées) ; `smokeTest` 2/2 ; SpotBugs vert ; `--dry-run` de `packageApp` et de l'installeur OK ; JavaFX 17.0.21 et PMD 7.28.0 effectivement résolus. **Action côté IntelliJ** : recharger le projet Gradle (module `side-projects` retiré).

---

## 2p. Étape 15 — Tester ce qui est livré, sur Windows, macOS et Linux (21/10/2026)

**Constat** : les tests compilent et tournent avec JavaFX 17.0.21, alors que les utilisateurs reçoivent JavaFX 23.0.8 (embarqué dans Zulu 21). Seule la distribution Linux était lancée en CI, et seulement jusqu'au menu.
- La suite unitaire ne peut pas tourner telle quelle sur JavaFX 23 : Monocle 17, qui l'affiche sans fenêtre, dépend des classes internes de JavaFX 17.
- La vérification fidèle consiste donc à **lancer les vraies distributions avec leur runtime embarqué**.

**Fait** :
- `.github/scripts/launch-distribution.sh` (Linux, macOS) et `.github/scripts/launch-distribution.ps1` (Windows) démarrent une distribution comme le ferait un utilisateur :
  - avec son script de lancement, depuis un autre dossier, sans terminal ;
  - avec un dossier utilisateur isolé (`JAVA_TOOL_OPTIONS=-Duser.home=...`) et en mode souris.
  
  Au bout de 40 s, ils vérifient que la JVM tourne encore, que le catalogue des jeux est chargé et qu'aucune exception ni erreur n'a été journalisée (seules les absences de fichiers attendues au premier lancement sont tolérées).
- `ci.yml` :
  - `package-linux` utilise le script et lance **le menu puis un jeu** (`--game WhereIsTheShape`) ;
  - **nouveau `package-windows`** (`windows-latest`) : construit `windowsDistZip` (Zulu 21 FX win_x64), l'extrait, lance le menu puis un jeu ;
  - **nouveau `package-macos`** (`macos-latest`, Apple Silicon) : construit `macosAarch64DistTar` et fait les mêmes deux lancements.
- **Vérifié en local sous Windows 11** (vraie archive `windowsDistZip`, runtime embarqué) :
  - menu : en marche après 30 s, sans erreur ;
  - jeu direct : idem, code 0, aucune JVM restante ;
  - **contrôle négatif** : un jeu inexistant (`--game JeuInexistant`) fait bien échouer le contrôle (« GazePlay stopped », code 1).
  - Piège corrigé au passage : le script se termine explicitement par `exit 0`, sinon `$LASTEXITCODE` resterait vide et le workflow verrait un échec.
- **Non vérifié avant le push** : les machines GitHub Windows et macOS (affichage, audio) et la distribution macOS Intel (pas de machine x64 dans la CI). Les jobs le diront.

---

## 9. Reprise à la prochaine session (mise à jour le 21/10/2026, étape 10)

### 9.1 État exact au moment de la pause
- Branche **`addClaudeCode`**. Le 21/10/2026, à la demande du mainteneur, le travail des étapes 1 à 12 a été **commité en local** en 6 commits au-dessus de `4a035dd4f` : nettoyage (`2c98db4b5`), build/code/tests (`f92bafdf9`), scripts (`631c2e0b8`), CI (`796873891`), documentation (`f05680599`), configurations IntelliJ (`1c7ae3f30`). Chaque état a été vérifié avant le commit sur une copie de l'index : compilation pour le 1er, et pour le 2e la suite complète (403 tests, 0 échec), `smokeTest` et SpotBugs. **Rien n'a été poussé** : la branche n'a pas de branche distante associée. Les 4 stashes n'ont pas été touchés.
- Aucune branche autre que `addClaudeCode` n'a été modifiée (`GazePlayLearning` non touchée).
- Démon Gradle arrêté à la pause (`./gradlew --stop`).

### 9.2 Étape 9 : ce qui a été fait (décisions du mainteneur du 20/10/2026)
**Terminé et vérifié**
1. melordi et picto-pick retirés (détails en 2i).
2. Plus de versions 32 bits ; ARM passe en aarch64 (détails en 2i). Fichiers :
   - `build.gradle`, `gradle/package.gradle`, `gradle/installer.gradle`, `gradle/jre.gradle`, `gradle/jre/arm.gradle` ;
   - suppression de `gradle/jre/windows-32.gradle` et des 3 fichiers `gradle/innosetup/setup*x32.iss.skel` ;
   - `gradle/libs.versions.toml` (`jave-nativebin-win32` retiré, `linux-arm32` remplacé par `linux-arm64`) ;
   - `gazeplay-core/build.gradle`, `.github/workflows/release.yml`, commentaires des scripts `gazeplay-dist/src/main/bin/*.sh`, `BUILD.MD`.
   - Vérifié : `--dry-run` du packaging et des installeurs ; téléchargement réel et somme du runtime aarch64 ; résolution de JAVE `linux-arm64` ; **suite complète au vert après ces deux changements : 376 tests, 0 échec, `smokeTest` et `spotbugsMain` OK**.

**Terminé, vérification finale décrite en 9.4**
3. **Format de résultats v2** (détails en 2i). Fichiers :
   - `gazeplay-results/.../ReplayDataFormat.java` (nouveau), `ReplayDataWriter.java`, `FixationSequenceRecorder.java` ;
   - `gazeplay-commons/.../Stats.java`, `FixationSequence.java` (`vertexReduction`) ;
   - `gazeplay-core/.../ReplayingGameFromJson.java` ;
   - tests : `ReplayDataFormatTest` (nouveau), `ReplayDataWriterTest`, `StatsRecordingCharacterizationTest`, `StatsTest`, `FixationSequenceTest`.
4. **Notes de version** rédigées : `GazePlay-1.12.0.md`, en anglais comme les précédentes. Le numéro est une proposition. **Non publiées** : la publication demande commit, push et release, donc l'accord explicite du mainteneur.
5. **Runtime JavaFX, constat et correction** (détails en 2g, « Correction (étape 9) ») :
   - Zulu 17.0.20.1 FX embarque JavaFX 22.0.10, pas 17.0.20 ; la documentation est corrigée (`BUILD.MD`, `gradle/jre.gradle`) ;
   - la distribution Linux x64 n'écrase plus les greffons multimédia du runtime par ceux d'OpenJFX 17.0.20 (`gradle/jre/linux.gradle` simplifié, avec les mêmes noms de tâches ; `build.gradle` ; somme OpenJFX retirée) ;
   - **non testé sous Linux**.
6. Documentation : `MODERNIZATION.md` (2g, 2i, 4, 9), `ARCHITECTURE.md`, `CLAUDE.md` (R1, R2, R5 et R11 marquées corrigées en v2 ; plateformes ; melordi).

**Non commencé (volontairement, pause demandée)** : passage des runtimes à Zulu 21. Informations déjà réunies, à ne pas refaire :
- Zulu **21.52.203 / 21.0.12.1** FX ; sommes SHA-256 publiées par Azul :
  - `win_x64.zip` : `2cb4e16da18812247cb151971fcede513a6d427a40083a497e099142fc1ed77b` (vérifiée sur l'archive téléchargée) ;
  - `linux_x64.tar.gz` : `9488605a66a6192529c22896d2e7d268f25e2230d2fe7e560ec8acacd279ae5e` ;
  - `linux_aarch64.tar.gz` : `d797de573936bd98e547dd5fdd61455924f9fffee5a430956a521b3224c72ad5` ;
  - `macosx_x64.tar.gz` : `00e1f0462850c610c89e875372d8e22eb2501af9dae7096048ad30c7f121aeb6` ;
  - `macosx_aarch64.tar.gz` : `8ee02b3f4c79ea571a30fb975551dfaa8a3b0e4e94e7a9f151dd3d5c5fcd865f`.
- Il embarque **JavaFX 23.0.8** (lu dans `lib/javafx.properties` de l'archive Windows).
- Monocle 21.0.2 exige Java 21 pour s'exécuter : garder Monocle 17.0.10 tant que les tests tournent sur JDK 17.

### 9.2 bis Étape 10 (21/10/2026), voir la section 2j
- **Terminé et vérifié** :
  - runtimes Zulu 21.0.12.1 FX (JavaFX 23.0.8), avec correction de l'extraction macOS ;
  - nouvelle distribution macOS Apple Silicon ;
  - correction de l'édition au lancement (`GazePlayArgs.editionToSave`) ;
  - JAVE 4.2.0 ;
  - message de premier lancement (`ConfigurationSource`) ;
  - extraction du calcul des zones d'intérêt (`AreaOfInterestGrouper`), précédée de sa caractérisation ;
  - API Gson à jour dans 5 jeux, avec `JsonResourcesTest` ;
  - manifestes compatibles avec le cache de configuration.
- **Vérification finale de l'étape 10** (21/10/2026, JDK 17 local) : **393 tests, 0 échec** (results 14, commons 171, games-commons 18, core 115, games 74 + 1) ; `smokeTest` 2/2 ; `spotbugsMain` OK. Aussi :
  - `smokeTest` 2/2 sous JDK 21.0.7 ;
  - `windowsDistZip` lancée avec sa JRE 21 ;
  - `macosDistTar` et `macosAarch64DistTar` construites et inspectées.
- Ressources mortes (44 Mo) supprimées avec votre accord (section 2j).

### 9.3 Où reprendre : prochaines actions, dans l'ordre
1. ~~Ressources mortes~~ : **supprimées** (2j).
2. ~~Distribution macOS aarch64~~ : **faite** (section 2j). Reste à la tester sur un Mac.
3. **Architecture** :
   - ~~extraction du calcul des zones d'intérêt~~ : **faite** (2j). Reste éventuellement l'échantillonnage des zones (R13, décision métier) et `AreaOfInterest` (écran, 700 lignes) ;
   - ~~abstraction `GazeSource`~~ et ~~module `gazeplay-gaze`~~ : **faits** (2j). Reste éventuellement la validité ou l'horodatage de l'appareil, impossibles sans réécrire le JNI Tobii (C++) ;
   - tester un vrai appareil Tobii après la refonte (matériel requis).
4. **Code mort et qualité** :
   - avertissements : 102 → 25 (2j) ; les restants sont structurels ou laissés volontairement (Eval, contrat des jeux) ;
   - PMD sur le code modifié ;
   - `sysout-over-slf4j` (2013) : le remplacer par une redirection minimale si elle est utile.
5. **Gradle** :
   - ~~cache de configuration~~ : **compatible** pour compilation et tests, à la demande (2j). Activation par défaut bloquée par le plugin de licences ;
   - plugin `release` (dépréciation pour Gradle 10, voir 2j).
6. **Tests** :
   - CI à observer au premier push (jobs `test` sous Linux et `package-linux`, jamais exécutés) ;
   - un job qui teste avec le JavaFX livré (23.x) ;
   - fixtures Eval sur `GazePlayLearning` quand la branche sera fusionnée.
7. **Packaging** : construire et lancer réellement les distributions Linux, macOS et ARM (seulement possible en CI ou sur machine dédiée) ; installeurs Inno Setup (Docker).

### 9.4 Commandes de vérification
```
./gradlew :gazeplay-results:test :gazeplay-gaze:test :gazeplay-commons:test :gazeplay-games-commons:test :gazeplay-core:test :gazeplay-games:test :gazeplay-core:smokeTest spotbugsMain --continue
./gradlew --dry-run packageApp generateWindowsInstallerInDocker downloadAndExtractJREs linuxDistTar armDistTar
./gradlew downloadAndExtractJREs        # télécharge et vérifie les sommes (~400 Mo)
```
Résultat de la dernière exécution de la suite complète (après le format v2) : voir 9.6.

### 9.5 Problèmes connus, hypothèses, précautions
- **Ne jamais lancer les tests hors Gradle** (IDE, `java` direct) : seul `build.gradle` isole `user.home` et le fichier d'édition.
- **Format v2** : un ancien GazePlay qui rejoue un fichier v2 inverse les x/y des fixations ; c'est un effet d'affichage seulement. Les scripts d'analyse du laboratoire doivent lire `formatVersion`. C'est indiqué dans les notes de version.
- **Non testé** :
  - matériel Tobii et EyeTribe ;
  - distributions Linux, macOS et ARM sur machine réelle ;
  - greffons multimédia du runtime Linux après la correction ;
  - JavaFX 22.0.10 (version livrée) par les tests automatiques.
- Les dossiers `build/jre/macos` et `build/jre/windows-x32` sont d'anciennes extractions (runtime 17.0.2) : un `./gradlew clean` les supprime. Les anciennes archives non versionnées de `~/.m2/repository/jre` (`arm.tar.gz`, `windows-x32.zip`…) sont des données locales de l'utilisateur : ne les supprimer qu'avec son accord.
- Les tests JavaFX restent sérialisés (`JavaFxTestsLimiter`) : ne pas réactiver le parallélisme.
- Le wrapper Tobii extrait ses DLL dans `%LocalAppData%\.tobiiStreamEngineForJava` à chaque lancement (comportement préexistant).

### 9.6 Résultat des vérifications finales de l'étape 9
Exécutées le 20/10/2026, Gradle 9.8.2, JDK 17 local, Windows 11 :
- Après le retrait de melordi, picto-pick et du 32 bits : suite complète **376 tests, 0 échec** ; `smokeTest` et `spotbugsMain` OK.
- Après le format v2 : suite complète **383 tests, 0 échec** (results 9, commons 170, games-commons 18, core 112, games 74) ; `smokeTest` 2/2 OK ; `spotbugsMain` OK (BUILD SUCCESSFUL, avec `--continue`).
- Après la modification de `gradle/jre/linux.gradle` (faite pendant la dernière exécution des tests, qu'elle n'influence pas) : `--dry-run` de `packageApp`, `generateWindowsInstallerInDocker`, `downloadAndExtractJREs`, `linuxDistTar` et `armDistTar` OK.
- **Non exécuté** :
  - construction réelle de `linuxDistTar` et `armDistTar` (archives) et lancement de ces distributions ;
  - JDK 21 pour cette étape (vérifié à l'étape 7 seulement) ;
  - matériel Tobii.

### 9.7 Décisions qui demandent encore l'avis du mainteneur ou du laboratoire
1. Numéro de la prochaine version (1.12.0 proposé) et publication des notes de version (commit, push, release).
2. Commits : quand et comment découper le travail non commité (proposition : un commit par étape de ce document).
3. Retraitement des fichiers historiques v1 : outil hors ligne souhaité ? Faut-il considérer `heatmap.csv` (sources mélangées) comme obsolète au profit des fichiers par source ?
4. R13 (échantillonnage souris des zones d'intérêt) : la correction change les zones calculées.
5. Distribution macOS : x64 seulement, ou aussi Apple Silicon ?
6. Java 25 (Zulu 25 FX, JavaFX 25) : il imposerait `release` ≥ 23 à la compilation, donc Java 25 aussi pour la distribution sans JRE. Recommandation : Zulu 21 d'abord.
7. Fusion de `GazePlayLearning` (R12, Eval) ; questions Eval de CLAUDE.md (règle d'arrêt à 5 erreurs, RGPD, format officiel des exports).
8. Licence Tobii pour l'enregistrement de données brutes ; maintien d'EyeTribe.

---

## 3. Bugs produit trouvés et corrigés

### B1 — « Où est la forme ? » plante dans toutes les langues (corrigé)
- **Fait.** Le commit `a86cf7fe7` (26/04/2024, « changed a few translations ») a été édité avec un tableur qui a pris le point d'interrogation grec `;` pour un séparateur. Résultat dans `whereIsTheShape.csv` : une colonne vide après `ell` sur les 20 lignes, toutes les langues suivantes décalées d'une colonne, et une 23ᵉ colonne qui provoquait un `ArrayIndexOutOfBoundsException` dans `I18N`, donc l'échec du chargement du jeu.
- **Correction.** `,,` → `;,` à cet endroit. L'en-tête est de nouveau identique à la version précédant la régression, avec 22 colonnes partout. Les seules différences restantes sont les deux retouches anglaises voulues de ce commit (`moon`, `circle`).
- **Fait.** Les versions publiées 1.11.8 à 1.11.11 ne sont **pas** touchées. `developMain` l'est. `GazePlayLearning` ne contient pas ce commit.

### B2 — `NullPointerException` dans `StatsContext` si `variantType` est nul (corrigé)
- **Fait.** Ne se produit qu'avec des `Stats` simulés en test (la valeur par défaut est `""`). Correction défensive, sans effet en production.

---

## 4. Impact sur les données et les formats

- **R3 (`Time` dans `*-info-game.csv`)** : la **valeur** est corrigée pour les nouvelles parties ; le format (`HH:mm:ss`, texte) ne change pas. Les fichiers historiques gardent le mois à la place des minutes. On retrouve les minutes dans le préfixe `yyyy-MM-dd-HH-mm-ss` du nom du fichier, qui a toujours été correct, à quelques secondes près puisque les deux horodatages sont pris au même moment. Pas de `formatVersion` ajouté : rien ne lit cette colonne dans le code.
  - **Question** : faut-il signaler ce changement aux utilisateurs des CSV (laboratoire) dans les notes de version ?
- **`GazePlay.properties`** : contenu identique, seul le mode d'écriture change. **Risque résiduel** : si le fichier est un lien symbolique, il est remplacé par un fichier ordinaire (cas non identifié dans les distributions).
- **Aucun autre format** (JSON de rejeu, heatmap, exports Eval) n'est modifié dans cette étape.
- **Format de résultats v2** (étape 9) : voir la section 2i, « Format de résultats v2 ». `formatVersion: 2` dans `replayData.json`, nouveaux fichiers `heatmapGaze.csv` et `heatmapMouse.csv` ; fichiers v1 jamais réécrits et toujours lus.

---

## 5. Vérifications exécutées

| Vérification | Résultat |
|---|---|
| `compileJava compileTestJava` (tous les modules) | OK |
| Tests des 4 modules sur le code final, 3 exécutions `--rerun` | **3 × 331 tests, 0 échec, résultats identiques** (commons 139, games-commons 18, core 105, games 69) |
| `spotbugsMain --continue` (tous les modules, = étape CI existante) | OK. Un premier passage a signalé un déréférencement nul possible dans le nouveau `saveConfig` ; c'est corrigé |
| `pmdMain` commons et core | **Échoue déjà à la référence** (107 et 255 violations ; PMD n'est pas dans la CI). Aucune violation sur les lignes nouvelles de `Configuration` ; celles des autres fichiers modifiés portent sur du code qui existait déjà. Pas de comparaison chiffrée formelle avec la référence |
| Écritures dans le vrai `~/GazePlay` ou `Documents\Gazeplay` | aucune (`find -newer` après chaque série) |
| Job CI `test` sous Linux | **non exécuté** (pas de Linux ici) : à surveiller au premier push. Des écarts propres à Linux sont possibles (bibliothèques natives, `xvfb`) |
| Tests matériels (Tobii) et autres plateformes | **non exécutés** |
| Lancement manuel de l'application | **non effectué** |
| Étape 9 (20/10/2026) : suite complète après le format v2 | **383 tests, 0 échec**, `smokeTest` et SpotBugs OK (détails en 9.6) |

---

## 6. Constats annexes (non corrigés dans cette étape)

- **Fait — clés de configuration renommées sans migration.** Le commit `bddc3f1e8` (25/08/2022, première version concernée : 1.11.1) a renommé `EYETRACKER`, `FILEDIR`, `FIXATIONLENGTH`, `CSSFILE`, `WHEREISITDIR`, `MUSIC_FOLDER`… Aucun code ne relit les anciennes clés : un utilisateur qui passe d'une version ≤ 1.11.0 à une version plus récente perd ses réglages sans le savoir. **Question** : faut-il une migration en lecture (ancienne clé → nouvelle) ? Elle est simple à faire, mais les mises à jour depuis 1.11.0 sont-elles encore réalistes ?
- **Fait — affichage BERA.** Dans `StatsContext.addAllToGrid`, la ligne « ScoreRightTargetItems » affiche le score **gauche** pour Morphosyntaxe (l. 344) et Phonologie (l. 358) ; Sémantique est correct. Ce n'est pas corrigé ici, car toute évolution d'Eval/BERA doit partir de `GazePlayLearning` : il faut vérifier sur cette branche.
- **Fait — `multilinguism.csv`** : les clés `Farm`, `Savanna`, `Score`, `SprintFinish` et `SprintFinishMouse` sont en double (la dernière occurrence l'emporte). « Petit Ours Brun » n'a pas de traduction anglaise (exception documentée dans `TranslationFilesTest`).
- **Risque — `GazePlayArgs` sous Windows** utilise `C:\Users\<user.name>\Documents`. C'est faux si le dossier Documents est redirigé (OneDrive, autre lecteur) ou si le nom du profil diffère de `user.name`.
- **Fait — tests JavaFX** : en local, ils ouvrent de vraies fenêtres. Le mode sans affichage (Monocle) n'est pas configuré ; la dépendance `openjfx-monocle:jdk-11+26` est probablement incompatible avec JavaFX 21 (hypothèse non testée).
- **Fait — certains tests écrivent dans le répertoire du module** (`music_test`, journaux). Ils nettoient derrière eux ; rien n'apparaît dans `git status`.

---

## 7. Décisions en attente (mainteneur, laboratoire)

Elles reprennent les questions de `CLAUDE.md`, plus celles nées de cette étape :
1. Branche de référence : ces changements partent de `developMain`. Faut-il les reporter sur `GazePlayLearning` (version active d'Eval) ? Ils s'y appliquent a priori sans conflit, sauf `whereIsTheShape.csv`, qui n'y est pas cassé.
2. Commits : rien n'est commité. Découpage proposé : (a) tests hermétiques + CI, (b) tests périmés, (c) B1 + `I18N`, (d) R3, (e) sauvegarde atomique.
3. Migration des anciennes clés de configuration (§6).
4. Communication de la correction R3 aux utilisateurs des CSV.

---

## 8. Prochaine étape recommandée

**Étape 2 — Caractérisation du pipeline de résultats (R1, R2, R4, R5, R11), sans correction.**
- Prérequis : réponse à la question 1, pour savoir sur quelle branche travailler. `Stats` diffère entre les branches (R12).
- Contenu :
  - un `SyntheticGazeDeviceManager` qui injecte des `GazeEvent` et `MouseEvent` déterministes dans une scène JavaFX ;
  - des tests qui figent le comportement **actuel** (doublons des filtres, cumul de `coordinateData`, durée ×2, inversion x/y), chacun marqué comme anomalie confirmée ;
  - des fichiers de référence `replayData.json` et `heatmap.csv`.
- Ensuite, R10 et R8 (Eval, sur `GazePlayLearning`), puis R4 et R5, avec une décision scientifique et un `formatVersion`.
- En parallèle, à faible risque : nettoyage des dépendances mortes listées dans `CLAUDE.md` (une par changement, vérifiée par la suite de tests), et mise à jour de JaCoCo (0.8.3 → ≥ 0.8.8, nécessaire pour Java 17). Attention : le commentaire du build lie cette version à JMockit, il faut donc la vérifier avec la suite.
