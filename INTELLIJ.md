# Lancer GazePlay sous IntelliJ IDEA

Guide pour développer et lancer GazePlay depuis IntelliJ depuis la modernisation d'octobre 2026, qui a retiré Spring
Boot. Les commandes en ligne et la construction des distributions sont décrites dans [BUILD.MD](BUILD.MD).

## Ce qui a changé par rapport à Spring

| Avant | Maintenant |
|---|---|
| Spring Boot démarrait l'application (`@SpringBootApplication`) | Java simple : `GazePlayLauncher.main` → `CompositionRoot` construit les composants |
| Configurations IntelliJ de type **Spring Boot** (IntelliJ Ultimate uniquement) | Configurations de type **Application**, disponibles aussi dans IntelliJ **Community** |
| Classe principale `net.gazeplay.GazePlayLauncher` | **Inchangée** : `net.gazeplay.GazePlayLauncher` |
| Zulu 17 | **Zulu 25 obligatoire** depuis le 21/10/2026 : le code est compilé pour Java 25. Avec un JDK plus ancien, le build s'arrête avec le message « GazePlay needs a JDK 25 or newer » |
| JavaFX 21 à la compilation | JavaFX 21.0.13 à la compilation, fourni par Gradle : votre JDK n'a pas besoin d'inclure JavaFX |

Les anciennes configurations Spring Boot de votre IntelliJ ne fonctionnent plus : supprimez-les toutes
(*Run > Edit Configurations…*, catégorie *Spring Boot*). Celles qui lançaient les éditions AFSR ou BERA, ou passaient
une autre option retirée, échouent avec `Unknown option` et l'application s'arrête aussitôt. Les éditions AFSR et BERA
(GazePlay Eval) ont été retirées en octobre 2026 : seul GazePlay normal reste.

## 1. Prérequis

- IntelliJ IDEA, édition Community ou Ultimate.
- Un **JDK 25** ou plus récent. On recommande **Azul Zulu 25.0.4.1**, la version du runtime livré avec GazePlay
  (`gradle/jre.gradle`). La section 1 bis explique comment l'installer.
- Le plugin Lombok, intégré à IntelliJ et actif par défaut.

## 1 bis. Installer le JDK 25

Un JDK **sans** JavaFX suffit : Gradle fournit JavaFX. Choisissez l'une des deux méthodes.

**Méthode A, depuis IntelliJ (la plus simple)**
1. *File > Project Structure > SDKs*, puis **+** et **Download JDK…**
2. **Version** : 25. **Vendor** : *Azul Zulu*.
3. **Location** : laissez la valeur proposée (`C:\Users\<vous>\.jdks\azul-25…`), puis cliquez sur **Download**.

**Méthode B, téléchargement manuel avec vérification de la somme de contrôle**
1. Téléchargez le fichier zip *Zulu 25 JDK* pour votre système sur <https://www.azul.com/downloads/?version=java-25-lts&package=jdk>.
   Sous Windows x64 : `zulu25.36.205-ca-jdk25.0.4.1-win_x64.zip`.
2. Vérifiez la somme SHA-256 publiée sur la page de téléchargement. Dans PowerShell :
   `Get-FileHash .\zulu25.36.205-ca-jdk25.0.4.1-win_x64.zip -Algorithm SHA256`. Pour ce fichier, la somme est
   `5e91bc55aaa08750370d60b7030bb79193901ce1e93aba31becbf78c3ade5826`.
3. Décompressez le dossier dans `C:\Users\<vous>\.jdks\`, et renommez-le par exemple `azul-25.0.4`.
4. Vérifiez la version installée : `C:\Users\<vous>\.jdks\azul-25.0.4\bin\java -version` doit afficher `25.0.4.1`.
5. Dans IntelliJ : *File > Project Structure > SDKs*, puis **+** et **Add JDK from disk…** Choisissez le dossier
   `azul-25.0.4`.

Les anciens JDK 17 et 21 peuvent rester installés : ils ne servent simplement plus pour GazePlay.

## 2. Recharger le projet Gradle

La structure des modules a changé :
- **ajoutés** : `gazeplay-results`, `gazeplay-gaze` ;
- **supprimés** : `gazeplay-melordi`, `gazeplay-picto-pick`.

1. Ouvrez le dossier du dépôt (`File > Open`, puis le dossier `GazePlay`).
2. Ouvrez la fenêtre **Gradle**, à droite, puis cliquez sur **Reload All Gradle Projects** (flèches circulaires).
3. Vérifiez que la fenêtre **Project** montre ces modules : `gazeplay-results`, `gazeplay-gaze`, `gazeplay-commons`,
   `gazeplay-games-commons`, `gazeplay-core`, `gazeplay-games`, `gazeplay-dist`. S'il reste un module `melordi`,
   `picto-pick` ou `side-projects`, faites un clic droit dessus, puis **Remove Module**.

Le premier chargement télécharge Gradle 9.8.2 et les dépendances : comptez quelques minutes.

## 3. Réglages à vérifier, une seule fois

**`File > Settings > Build, Execution, Deployment > Build Tools > Gradle`** :

| Réglage | Valeur |
|---|---|
| Gradle JVM | Zulu **25** (pas Zulu 17 ni 21) |
| Build and run using | **Gradle** |
| Run tests using | **Gradle** (indispensable, voir la section 6) |

**`File > Project Structure > Project`** :

| Réglage | Valeur |
|---|---|
| SDK | Zulu **25** |
| Language level | 25 |

Les deux réglages sont nécessaires :
- **Gradle JVM** fait tourner Gradle, qui compile et lance l'application.
- **SDK** sert à l'éditeur : coloration des erreurs et complétion.

Avec seulement l'un des deux en 25, on obtient soit une erreur de build, soit du code souligné en rouge à tort.

Après avoir changé ces réglages, cliquez sur **Reload All Gradle Projects** dans la fenêtre Gradle.

**En ligne de commande** (terminal d'IntelliJ ou autre) : `gradlew` utilise le JDK indiqué par `JAVA_HOME`, et non celui
d'IntelliJ. Si `JAVA_HOME` pointe encore vers un JDK 17 ou 21, le build s'arrête avec « GazePlay needs a JDK 25 or
newer ». Dans PowerShell, pour la session en cours : `$env:JAVA_HOME = "$HOME\.jdks\azul-25.0.4"`.

### Vérifier que tout est en place

1. Dans la fenêtre Gradle : *GazePlay > Tasks > build > **classes***, double-clic. Le résultat attendu est
   `BUILD SUCCESSFUL`. Une compilation complète prend environ 40 s.
2. Lancez la configuration **GazePlay** (section 4) : la page de choix du profil s'ouvre.
3. Facultatif : la configuration **Tests (tous les modules)** doit se terminer sans échec (environ 400 tests, 4 à 5 minutes).

Des messages `Note: … uses unchecked or unsafe operations` ou `deprecated API` pendant la compilation sont normaux :
ce sont des avertissements, pas des erreurs. Deux avertissements Gradle (« deprecated… Gradle 10 ») viennent de plugins
tiers, pas du code de GazePlay.

## 4. Lancer l'application

### Avec les configurations fournies (recommandé)

Le dossier `.run/` du dépôt contient des configurations prêtes, qu'IntelliJ affiche automatiquement dans la liste
déroulante en haut à droite (il faut parfois recharger le projet) :

| Configuration | Ce qu'elle lance |
|---|---|
| **GazePlay** | GazePlay normal : choix du profil, puis menu des jeux |
| **GazePlay - jeu direct** | Un jeu sans passer par le menu (`--default-user --game WhereIsTheShape`) : modifiez le jeu dans les paramètres |
| **Tests (tous les modules)** | Toute la suite de tests, par Gradle |
| **Test de lancement (smokeTest)** | Démarre la vraie application dans une JVM séparée et vérifie qu'elle tourne sans erreur |

Choisissez une configuration, puis cliquez sur **Run** (▶) ou **Debug** (🐞). Les points d'arrêt fonctionnent
normalement en Debug.

### Créer une configuration à la main

1. **Run > Edit Configurations… > + > Application**.
2. **Name** : par exemple `GazePlay`.
3. **Module / Use classpath of module** : `gazeplay.gazeplay-core.main`. Son classpath d'exécution inclut les jeux.
4. **Main class** : `net.gazeplay.GazePlayLauncher`.
5. **Program arguments** : vide pour GazePlay normal, ou l'une des options ci-dessous.
6. **VM options** : `--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow`. Les configurations
   de `.run/` les contiennent déjà. Sans ces options, l'application fonctionne, mais Java 25 affiche des avertissements
   au démarrage (voir la section 7).

### Options de la ligne de commande

| Option | Effet |
|---|---|
| *(aucune)* | GazePlay normal, avec la page de choix du profil |
| `--default-user` | Profil par défaut, sans la page de choix du profil |
| `--user <nom>` | Ouvre directement ce profil |
| `--game <nom>` (`-g`) | Lance directement un jeu, par son nom de code (ex. `WhereIsTheShape`, `Ninja`) |
| `--variant <variante>` (`-v`) | Variante du jeu (ex. `DimensionDifficultyGameVariant:2:2:Easy`) |
| `--random-game` | Un jeu au hasard |
| `--width <l> --height <h>` | Taille de la fenêtre de jeu |
| `--replayJsonFile <fichier>` (`-json`) | Rejoue un fichier `replayData.json` |
| `--help` | Affiche la liste des options |

## 5. Ce que l'application lit et écrit

Lancée depuis IntelliJ, l'application utilise **vos vraies données**, comme la version installée : le dossier
`~/GazePlay` (sous Windows `C:\Users\<vous>\GazePlay`), avec les profils, les réglages (`GazePlay.properties`), les
résultats (`statistics/`) et les journaux (`gazeplay.log`).

**Édition** : seul GazePlay normal existe désormais. Le fichier `~/GazePlay/argsGazeplay.txt` contient `gazeplay`. Une
ancienne valeur `bera` ou `afsrGazeplay`, laissée par une version précédente, est lue comme `gazeplay`.

## 6. Lancer les tests

**Toujours par Gradle**, avec la configuration **Tests (tous les modules)**, le menu Gradle ou le bouton ▶ d'un test,
à condition d'avoir réglé « Run tests using : Gradle » (section 3).

Pourquoi : `build.gradle` isole les tests. Il redirige `user.home` vers `build/test-home` et lance JavaFX sans fenêtre
(Monocle). Lancés directement par IntelliJ (« Run tests using : IntelliJ IDEA »), les tests liraient et écriraient votre
vrai dossier `~/GazePlay`.

- Pour voir les fenêtres pendant un test, ajoutez `-Pgui` aux arguments Gradle de la configuration.
- Les tests JavaFX des différents modules tournent l'un après l'autre : la suite complète prend 4 à 5 minutes.

## 7. Dépannage

| Symptôme | Cause et solution |
|---|---|
| `Unknown option: '--bera'` ou `'--afsrgazeplay'` (ou une autre option) | Ancienne configuration Spring Boot : supprimez-la et utilisez celles de `.run/` |
| Classes introuvables (`GazeEvent`, `HeatMapGrid`…) ou modules en rouge | Le projet n'a pas été rechargé depuis l'ajout de `gazeplay-results` et `gazeplay-gaze` : **Reload All Gradle Projects** |
| Erreurs Lombok (`getX()` introuvable, `log` inconnu) | Vérifiez « Build and run using : Gradle ». Si vous compilez avec IntelliJ, activez *Settings > Build > Compiler > Annotation Processors > Enable annotation processing* |
| `JavaFX runtime components are missing` | La classe principale n'est pas la bonne : utilisez `net.gazeplay.GazePlayLauncher`, pas `GazePlayFxApp` |
| Au démarrage : `WARNING: A restricted method in java.lang.System has been called` ou `sun.misc.Unsafe … has been called` | La configuration de lancement n'a pas les *VM options* de la section 4 : reprenez celles de `.run/` ou ajoutez-les. Les jeux lancés depuis le menu les reçoivent automatiquement |
| Au démarrage : `AVERTISSEMENT: Unsupported JavaFX configuration: classes were loaded from 'unnamed module…'` | Normal depuis IntelliJ et sans conséquence : JavaFX est chargé depuis le classpath, et non comme module. Les distributions, dont JavaFX fait partie du runtime livré, n'affichent pas ce message |
| `GazePlay needs a JDK 25 or newer` ou `invalid target release: 25` | Gradle JVM (section 3) ou `JAVA_HOME` (en ligne de commande) pointe encore vers un JDK 17 ou 21 |
| Code souligné en rouge alors que le build Gradle passe | Le **SDK** du projet ou le **Language level** n'est pas en 25 (section 3) |
| Pas de regard, seulement la souris | Normal sans eye tracker. Avec un Tobii, l'eye tracker se choisit dans les réglages de GazePlay (Windows x64 et Linux x64 uniquement) |
| Tobii Eye Tracker 5 branché, mais aucun regard | Installez **Tobii Experience** et calibrez l'appareil : la bulle de Tobii Experience doit suivre vos yeux. Sans calibrage, la connexion réussit, mais aucune position n'arrive. Ne remplacez pas la DLL `tobii_stream_engine.dll` fournie par GazePlay par celle de Tobii : elle est incompatible |
