# Refonte graphique des menus — état des lieux et plan

Rédigé le 27/10/2026, branche `addClaudeCode`. Travail commencé le 28/10/2026 : étapes 0 à 2 validées et commitées,
étape 3 (profils) faite, en attente du retour du mainteneur (section 8). Ce document sert de référence à chaque étape
et sera complété au fur et à mesure.

Écrans concernés : **choix du mode**, **choix de l'utilisateur** (profils), **menu principal** (jeux et évaluations),
**paramètres**.

## 1. Décisions du mainteneur (27/10/2026)

| Sujet | Décision |
|---|---|
| Direction | Quelque chose de **plus moderne, actuel**. |
| Clair / sombre | **Gardés tous les deux.** |
| Thèmes de couleur | On **peut s'en passer** si besoin. |
| Paramètres | Mise en page **au choix de Claude** ; le mainteneur fera des retours. |
| Menu des jeux | **On garde** les cartes, l'orientation, les filtres et les favoris, mais on les modernise. |
| Profils | ~~Tout au même endroit~~ : précisé plus bas, **on laisse comme aujourd'hui** (question 4). |
| Choix du mode | **Ajouter des icônes.** Boutons nommés **« GazePlay Learning Games »** et **« GazePlay Learning Eval »**. |
| Organisation | Une première passe pour s'organiser et tout documenter aujourd'hui ; on code à partir de demain. |
| Couleurs (réponse à la question 1) | **Chaleureux et un peu coloré : le public est surtout composé d'enfants.** |
| Thèmes de couleur (question 2) | **Au choix de Claude** : ils deviennent un choix de couleur d'accent (3.3). |
| Police (question 3) | **Adaptée aux enfants en priorité** (3.4). |
| Profils (question 4) | **On laisse comme aujourd'hui** : mêmes fenêtres de création, de modification et de suppression. Seule l'apparence est mise au style commun (4.2). |

Rappel : les menus restent à la souris et au clavier (regard dans les menus retiré, `GazePlayEval.md` 14.3).

## 2. État actuel (faits relevés dans le code)

### 2.1 Les écrans

| Écran | Code | Taille | Constats |
|---|---|---|---|
| Choix du mode | `ui/scenes/modeselect/ModeSelectionContext` | 110 lignes | Logo animé, une question, deux gros boutons de texte (nom + description), quitter. 6 styles écrits dans le code (`setStyle`), sans icône. |
| Profils | `ui/scenes/userselect/UserProfileContext` | 737 lignes | Grille de cartes (image + nom), carte « Ajouter » ; création, modification et suppression dans **des fenêtres séparées** (`createDialog`, `createRemoveDialog`) ; boutons modifier/supprimer en petites images PNG ; rappel du mode et « Changer de mode ». |
| Menu des jeux | `ui/scenes/gamemenu/HomeMenuScreen` (535) + cartes `GameMenuFactory` (286) | 821 lignes | Barre de recherche, filtres de catégorie en **cases à cocher**, cartes (miniature, nom, description, icône de catégorie, cœur des favoris), favoris d'abord, flèches de défilement, barre du bas (réglages, musique, rejeu, plein écran), profils et quitter en haut. Orientation horizontale ou verticale (`GameButtonOrientation`). |
| Fenêtre des variantes | `GameVariantDialog` | ~430 lignes | Fenêtre séparée, boutons `gameChooserButton gameVariation`. |
| Menu des évaluations | `ui/scenes/evalmenu/EvalMenuScreen` (194) + `EvalChooserDialog` (122) | 316 lignes | Écrit le 27/10/2026, styles dans le code (8 `setStyle`). |
| Paramètres | `ui/scenes/configuration/ConfigurationContext` | 1 416 lignes | **Une seule longue grille** « libellé : champ » dans un `ScrollPane`, titres de rubrique (Langue, Jeux, Oculomètre, Graphismes, Dossiers, Évaluations, Statistiques avec sous-rubriques). Rubriques selon le mode. |

### 2.2 Les styles

- `CssUtil.setPreferredStylesheets` (module `games-commons`) met sur une scène, dans l'ordre :
  1. `base-light.css` ou `base-dark.css` (139 lignes chacun, presque identiques : couleurs de base `-fx-base`,
     `-fx-background`, titres) ;
  2. un fichier de tailles selon la largeur de l'écran, de `base-max-width-0800.css` à `2560`. Il ne règle que les
     cartes des jeux et les boutons des variantes ;
  3. le thème de couleur (`CSS_FILE`) : 6 feuilles `main-*.css` (dégradés de 2017 sur `.button`, `.choice-box`,
     `.spinner`, `.toggle-button`), plus « Système » (aucune feuille), **Argent et or** par défaut ;
  4. les `.css` du dossier `styles` de l'utilisateur.
- Style actuel : angles droits (`-fx-background-radius: 0`), cartes noires bordées de blanc avec une ombre blanche,
  dégradés, police `sans-serif` du système à 11 pt. **Aucune police n'est livrée** (le logo est une image).
- **Le fond de chaque écran est écrit dans le code Java** : noir ou `#fffaf0` selon `isDarkThemeEnabled()`, recopié
  dans au moins 6 écrans (choix du mode, profils, menu des jeux, menu des évaluations, paramètres, statistiques).
- Icônes : des PNG de styles variés dans `data/common/images` (`power-off.png`, `logout.png`, `arrow-up.png`,
  `configuration-button-alt3.png`, `heart_filled.png`…), affichés par `CustomButton` (17 appels). **Ils servent aussi
  dans les jeux** (barre de contrôle).

### 2.3 Ce qui est partagé avec les jeux (risque principal)

- **Fait** : la JVM d'une partie applique les mêmes feuilles de style (`GameContextFactory` lit aussi le thème sombre).
  Les classes `.button`, `.choice-box`, `gameChooserButton` (utilisée par `games/colors/ColorToolBox` et
  `CustomColorPicker`) ou `.root` touchent donc aussi les jeux.
- **Fait** : les réglages Couleur de fond, Fond activé et le style de fond (`BackgroundStyle`) sont lus **par les
  jeux** (Biboule Jump, Bulles, Gâteaux, Diviseur, Dés…), pas par les menus. Ils restent tels quels.
- **Recommandation** : mettre les nouveaux styles **sous une classe racine propre aux menus** (`.gp-menu`, posée sur la
  racine de chaque écran refait). Les jeux ne la portent pas : rien ne change pour eux.

## 3. Direction proposée

### 3.1 Principes

- **Plat et aéré** : surfaces unies, angles arrondis (12 à 16 px), ombres légères, marges régulières (8, 16, 24,
  32 px), pas de dégradés.
- **Chaleureux et coloré, pour des enfants** : une couleur d'accent chaude (orange corail) pour l'action principale,
  la sélection et le focus. Des couleurs vives secondaires donnent à chaque catégorie de jeux et à chaque mode sa
  couleur. Les fonds restent doux (crème en clair, brun très foncé en sombre) pour ne pas fatiguer les yeux.
- **Hiérarchie claire** : un grand titre par écran, des sous-titres, du texte secondaire plus discret.
- **Gros éléments cliquables** : le public (enfants, personnes en situation de handicap, examinateurs) et l'usage sur
  grand écran le demandent.
- **États visibles** : survol, appui, focus clavier (anneau d'accent), désactivé.
- **Mêmes composants partout** : barre du haut, cartes, boutons principaux et secondaires, champs, interrupteurs.

### 3.2 Couleurs (jetons)

JavaFX (API 21) n'a pas de variables CSS, mais il a les **couleurs nommées** (*looked-up colors*), définies une fois et
réutilisées. Proposition, à ajuster sur captures :

| Jeton | Clair | Sombre | Rôle |
|---|---|---|---|
| `-gp-bg` | `#FFF8EF` (crème) | `#1B1612` (brun très foncé) | Fond de l'écran |
| `-gp-surface` | `#FFFFFF` | `#2A221C` | Cartes, panneaux |
| `-gp-surface-hover` | `#FFF0DE` | `#362C24` | Survol |
| `-gp-border` | `#F1E2CF` | `#45382D` | Bordures fines |
| `-gp-text` | `#2B2118` | `#FFF4E8` | Texte |
| `-gp-text-muted` | `#76665A` | `#C8B8A8` | Texte secondaire |
| `-gp-accent` | `#F2643A` (orange corail) | `#FF7E57` | Action principale, sélection, focus |
| `-gp-on-accent` | `#FFFFFF` | `#1B1612` | Texte sur l'accent |
| `-gp-yellow` | `#FFC23C` | `#FFCF5C` | Couleurs secondaires : catégories, |
| `-gp-teal` | `#22B3A6` | `#3CCFC1` | modes, icônes, illustrations |
| `-gp-violet` | `#8A63D2` | `#A988EA` | |
| `-gp-sky` | `#3D9BE9` | `#63B3F5` | |
| `-gp-danger` | `#D64545` | `#FF6B6B` | Suppression |

Exemples : **GazePlay Learning Games** en orange corail, **GazePlay Learning Eval** en turquoise (l'adulte qui fait
passer l'évaluation distingue ainsi les deux modes au premier coup d'œil). Valeurs de départ, à ajuster sur
captures.

Les fichiers `base-light.css` / `base-dark.css` définissent les jetons ; une nouvelle feuille `menus.css` les utilise
sous `.gp-menu`. Les contrastes texte/fond visent au moins 4,5:1 (WCAG AA), à vérifier.

### 3.3 Thèmes de couleur

- **Décision (laissée à Claude par le mainteneur)** : les 7 thèmes deviennent un **choix de couleur d'accent**,
  libellé « Couleur principale ». Par défaut : **orange corail**. Les autres choix restent chaleureux et lisibles sur
  les deux fonds : orange, vert, bleu ciel, bleu, violet, rose. La clé `CSS_FILE` garde ses valeurs
  (`builtin:ORANGE`…) : chaque valeur donne une couleur d'accent, et un ancien fichier de réglages reste lisible.
  `builtin:SILVER_AND_GOLD`, ancien défaut, et « Système » donnent l'orange corail. Les feuilles `main-*.css` ne
  servent plus aux menus.
- **À vérifier** : `.button`, `.choice-box` et `.spinner` des feuilles `main-*.css` touchent aussi les jeux. Si on
  arrête de les charger, l'apparence de certains contrôles dans les jeux change. À regarder jeu par jeu, ou alors on
  continue de charger ces feuilles dans les jeux seulement.
- Le dossier `styles` de l'utilisateur reste chargé en dernier.

### 3.4 Police

- **Fait** : aucune police n'est livrée ; l'apparence dépend du système.
- **Décision du mainteneur** : une police **adaptée aux enfants en priorité**.
- **Recommandation** : deux polices libres (licence OFL 1.1, compatible avec GPL-3.0), chargées au démarrage
  (`Font.loadFont`) :
  - **Andika** (SIL) pour les textes. Elle est conçue pour les lecteurs débutants : les lettres sont bien
    distinctes (I, l, 1) et le « a » et le « g » ont la forme apprise à l'école ;
  - **Fredoka** pour les titres et les noms des modes : ronde et joyeuse, c'est déjà la police du mot « Learning »
    du logo.
- **À vérifier à l'intégration** : la licence et les fichiers de chaque police, les accents et les autres alphabets
  des langues de GazePlay (dont l'arabe et le chinois : la police du système prendra le relais pour ces écritures),
  et le poids des fichiers.

### 3.5 Icônes

- **Recommandation** : un **jeu d'icônes unique**, dessiné en vecteurs (`SVGPath` de JavaFX), donc net à toutes les
  tailles et coloré par le jeton de texte ou d'accent (clair et sombre sans deux jeux d'images). Candidats : **Material
  Symbols** (Apache-2.0) ou **Lucide** (ISC), deux licences compatibles avec GPL-3.0. Licence à vérifier avant
  d'intégrer, et mention dans les licences de la distribution.
- Les icônes des menus seulement : les PNG restent pour les jeux (barre de contrôle), pour ne rien changer de leur côté.
- Choix du mode : **une manette** pour « GazePlay Learning Games », **un presse-papiers avec une coche** (ou un
  graphique) pour « GazePlay Learning Eval ».

## 4. Proposition par écran

### 4.1 Choix du mode

- En haut : le logo GazePlay Learning (plus petit qu'aujourd'hui) et, à droite, quitter (icône).
- Au centre : « Choisissez un mode », puis **deux grandes cartes côte à côte** : une icône (environ 96 px), le nom
  **« GazePlay Learning Games »** ou **« GazePlay Learning Eval »**, une ligne de description. Survol, focus clavier,
  Entrée et Espace comme aujourd'hui. Sur un écran étroit, les cartes passent l'une sous l'autre.
- Les noms des modes sont des noms de produit, identiques dans toutes les langues. Les clés `ModeGames` et
  `ModeEvaluations` servent aussi au rappel du mode sur la page des profils et au titre du menu des évaluations : le
  nouveau nom y apparaîtra aussi (cohérent).
- `ModeSelectionContext` est petit : c'est **le premier écran à refaire**, pour valider le style avant les autres.

### 4.2 Profils (fonctionnement inchangé)

- **Décision du mainteneur** : on laisse le fonctionnement actuel. La grille des profils, la carte « Ajouter » et les
  fenêtres de création, de modification et de suppression restent les mêmes.
- **Seule l'apparence suit le style commun** : fond, cartes arrondies, avatars ronds, police, boutons modifier et
  supprimer en icônes, fenêtres aux couleurs des menus. En haut : le logo, le mode en cours (« GazePlay Learning
  Games » ou « Eval ») avec « Changer de mode », et quitter.
- Code : changements de style seulement. `UserProfileContextTest` ne devrait pas changer (hypothèse).

### 4.3 Menu principal des jeux

- **Barre du haut** : petit logo, **champ de recherche** (icône loupe), puis à droite le mode et le profil (puce), les
  réglages, le plein écran, les profils et quitter. Tout en icônes avec infobulles.
- **Filtres** : les cases à cocher des catégories deviennent des **puces à bascule** (*chips*), avec l'icône de la
  catégorie. Même réglage enregistré (catégories cachées).
- **Favoris** : une rangée « Favoris » en tête quand il y en a, puis « Tous les jeux ». Le cœur reste sur la carte, à
  l'angle, visible au survol (et toujours visible s'il est plein).
- **Cartes** : arrondies, grande miniature, nom en gras, description sur deux lignes au plus, icône de catégorie
  discrète, légère élévation au survol.
  - Orientation **verticale** : grille de cartes « image au-dessus ».
  - Orientation **horizontale** : cartes larges « image à gauche, texte à droite ».
- **Défilement** : la molette et la barre restent ; les flèches de défilement restent, avec un nouveau style.
- **Barre du bas** : musique (`QuickControlPanel`) et rejeu, dans le même style.
- **Fenêtre des variantes** : même style que les cartes (titre, boutons-cartes, Annuler). Elle reste une fenêtre, comme
  la fenêtre des évaluations et les fenêtres des profils.

### 4.4 Menu principal des évaluations

- Même barre du haut que le menu des jeux.
- Au centre : une **grande carte « Lancer une évaluation »** (action principale, couleur d'accent) avec le nombre
  d'évaluations importées. Dessous, deux cartes secondaires : « Importer une évaluation » et « Résultats des
  évaluations ».
- La fenêtre de choix : une liste de cartes (une par évaluation, avec son nom), Annuler.

### 4.5 Paramètres

- **Proposition** : une page en deux colonnes.
  - **À gauche, la liste des rubriques**, avec une icône chacune : Langue, Jeux, Oculomètre, Apparence, Dossiers,
    Évaluations, Statistiques. Seules celles du mode en cours sont affichées, comme aujourd'hui.
  - **À droite, la rubrique choisie**, en cartes. Une ligne par réglage : le libellé (et une courte explication au
    besoin) à gauche, le contrôle à droite.
  - Un retour au menu en haut à gauche.
- **Contrôles modernisés** : interrupteurs (*switches*, une `CheckBox` stylée) pour les oui/non, listes déroulantes
  et champs numériques arrondis, boutons « Choisir un dossier » avec le chemin affiché.
- Les réglages s'appliquent toujours dès qu'on les change (rien ne change dans `Configuration` ni dans les clés).
- **Code** : `ConfigurationContext` (1 416 lignes) sera découpé, une petite classe par rubrique. Les contrôles et leurs
  liaisons aux propriétés sont gardés tels quels. Les tests qui comptent les nœuds de la grille
  (`ConfigurationContextTest`) seront réécrits pour chercher les réglages par leur nom, pas par leur position.
- Le mainteneur fera ses retours sur cette proposition.

## 5. Organisation du travail

Chaque étape : `./gradlew build` (tests et SpotBugs), `smokeTest`, `allGamesTest` dès qu'une feuille de style
partagée change, vérification à l'œil en clair et en sombre, en 1366 et 1920 de large. Puis retour du mainteneur avant
l'étape suivante. Commit et push sur demande, sur `addClaudeCode` seulement.

| Étape | Contenu | Pourquoi dans cet ordre |
|---|---|---|
| 0 | Captures « avant » des quatre écrans, en clair et en sombre | Comparer avant/après |
| 1 | Socle : jetons clair/sombre, `menus.css` sous `.gp-menu`, police, jeu d'icônes, composants communs (barre du haut, carte, boutons), fond des écrans passé du code Java aux feuilles de style | Tout le reste s'appuie dessus |
| 2 | Choix du mode (icônes, nouveaux noms) | Petit écran : valide le style |
| 3 | Profils : apparence seulement | Indépendant des menus |
| 4 | Menu des jeux, puis fenêtre des variantes | Le plus visible |
| 5 | Menu des évaluations et sa fenêtre | Réutilise les composants de l'étape 4 |
| 6 | Paramètres (rubriques, découpage du code, tests réécrits) | Le plus gros chantier |
| 7 | Thèmes de couleur (accent ou suppression), ménage des anciennes feuilles et images inutilisées, documentation | Une fois les écrans faits |

## 6. Risques

- **Les jeux** partagent les feuilles de style et les icônes PNG : d'où la classe `.gp-menu`, des icônes vecteurs pour
  les menus seulement, et `allGamesTest` à chaque changement partagé.
- **Les tests** ne voient pas le rendu (Monocle, sans fenêtre). Seuls des essais réels valident le graphisme.
  `ConfigurationContextTest` dépend de la structure actuelle de la page des paramètres et sera à revoir.
- **Clés de réglages** : aucune clé n'est renommée ni supprimée (format persistant). `CSS_FILE` change seulement de
  sens si on passe à une couleur d'accent.
- **Traductions** : les nouveaux textes (« Choisissez un mode », « Nouveau profil », descriptions des réglages) sont à
  ajouter à `multilinguism.csv`, au moins en français et en anglais, comme aujourd'hui.
- **Taille de l'écran** : les écrans doivent rester utilisables de 800 à 2560 px de large. Les fichiers
  `base-max-width-*` règlent aujourd'hui seulement les cartes des jeux.

## 7. Questions pour le mainteneur

Les quatre questions du 27/10/2026 ont leur réponse (section 1). Il n'y a plus de question bloquante pour commencer :
les retours viendront sur les captures de chaque étape, en commençant par l'écran du choix du mode.

Consigne du mainteneur (28/10/2026) : **uniquement des ressources libres et open source, libres de droits**, comme le
projet (GPL-3.0).

## 8. Avancement

### 8.1 Étape 0 — captures « avant » (28/10/2026)

- **Outil** : `MenuScreenshots` (sources de test de `gazeplay-core`), lancé par
  `./gradlew :gazeplay-core:menuScreenshots -Plabel=<nom>`. Une JVM par thème (clair, sombre) et par taille
  (800 × 600, 1366 × 768, 1920 × 1080), sans fenêtre (Monocle, `headless.geometry`), avec un dossier GazePlay
  temporaire (`build/menu-screenshots-home`), un profil « Camille » en plus du profil par défaut, en français et en
  mode souris. Sept captures par configuration : choix du mode ; profils, menu et paramètres de chacun des deux modes.
  Résultat dans `gazeplay-core/build/menu-screenshots/<nom>/<thème>-<largeur>/` (non versionné).
- **Fait** : captures « avant » faites en 1366 et 1920 (`-Plabel=avant`, 28 images), avant toute modification. Le
  800 × 600 a été ajouté ensuite : il n'a pas de capture « avant ».
- **Limite** : le rendu de Monocle (logiciel) est proche de celui de l'écran, sans être identique ; le survol et
  l'animation ne se voient pas. Les essais réels restent nécessaires.

### 8.2 Étape 1 — socle (28/10/2026)

- **Couleurs** : les jetons `-gp-*` de 3.2 sont définis dans `.root` de `base-light.css` et `base-dark.css` (plus
  `-gp-shadow`). Écart : `-gp-teal` clair passe de `#22B3A6` à **`#15978B`**, pour qu'une icône blanche sur le
  turquoise atteigne le contraste 3:1 (formule WCAG : 2,6:1 avant, 3,6:1 après). Sur l'orange corail clair, le
  blanc fait 3,15:1 : à réserver aux icônes et aux textes en gros caractères (WCAG AA, 3:1). Les autres paires
  vérifiées passent 4,5:1 : texte sur fond 14,9 (clair) et 16,5 (sombre), texte secondaire 5,2 (clair) et 9,3
  (sombre), pastilles sombres 7,2 (orange) et 9,3 (turquoise).
- **`menus.css`** : toutes les règles sont sous `.gp-menu`. La feuille est ajoutée par `CssUtil` à toutes les scènes,
  après le thème (pour l'emporter sur lui) et avant les feuilles de l'utilisateur. Les jeux ne portent jamais
  `.gp-menu` : rien ne change pour eux (vérifié par `allGamesTest`, 8.4).
- **Polices** (`data/fonts`, `MenuFonts`) : **Andika** Regular et Bold (SIL, OFL 1.1, fichiers inchangés, environ
  670 Ko chacun) et **Fredoka SemiBold** (OFL 1.1, 50 Ko), version fixe tirée de la police variable de google/fonts avec
  fontTools (JavaFX n'utilise que l'instance par défaut d'une police variable : pas de gras). Chargées une fois, à la
  première mise en style d'un écran des menus ; familles vérifiées dans le journal : « Andika » et « Fredoka
  SemiBold ». Origine, commande et licences : `data/fonts/README.txt`.
- **Icônes** (`MenuIcon`) : **Material Symbols Rounded** (Google, Apache-2.0, licence dans `data/icons`), un seul
  tracé par icône, copié du fichier SVG 24 px (comparaison automatique avec les fichiers téléchargés : identiques).
  Dessinées en `SVGPath`, colorées par la feuille de style (`gp-icon-shape`). Pour l'instant : quitter, manette,
  presse-papiers coché, changer, modifier, supprimer, ajouter un profil. Les PNG des jeux ne changent pas.
- **Composants** : `MenuStyle` (classe racine, titre, bouton-icône rond avec infobulle), `CardButton` (carte
  cliquable : un bouton qui garde son nom pour les lecteurs d'écran). `CardButton` mesure sa hauteur à sa propre
  largeur : sinon JavaFX mesure le texte sur plusieurs lignes avec une largeur négative, un mot par ligne, et la carte
  devient plusieurs fois trop haute (constaté sur capture). Barre du haut (`gp-top-bar`), fond, défilement, focus.
- **Focus clavier** : anneau d'accent avec `:focus-visible` (JavaFX 19 et plus) : visible quand on navigue au
  clavier, pas sur le premier bouton à l'ouverture ni après un clic.
- **Pas encore fait** : le fond écrit dans le code Java des autres écrans (profils, menus, paramètres) sera retiré à
  leur étape.

### 8.3 Étape 2 — choix du mode (28/10/2026)

- `ModeSelectionContext` refait : logo statique (un dixième de la hauteur) et quitter (icône) en haut ; titre
  « Choisissez un mode » en Fredoka ; deux cartes de même taille (`TilePane`), côte à côte, ou l'une sous l'autre sur un
  écran étroit, avec défilement si besoin (vérifié en 800 × 600). Chaque carte : pastille ronde de 144 px avec
  l'icône (manette sur orange corail, presse-papiers coché sur turquoise), nom, description. Plus aucun `setStyle`.
- **Noms des modes** : `ModeGames` = « GazePlay Learning Games », `ModeEvaluations` = « GazePlay Learning Eval »
  (anglais et français ; les autres langues reprennent l'anglais). Ils apparaissent aussi sur la page des profils et
  en titre du menu des évaluations (4.1).
- Le logo animé n'est plus sur cet écran (il reste sur la page des profils pour l'instant).
- Clic, Entrée et Espace inchangés (`getGamesButton().fire()` dans `EvalMenuScreensTest`).

**Retours du mainteneur (28/10/2026) et changements :**

| Retour | Changement |
|---|---|
| Le bouton quitter est moins visible et moins marquant qu'avant | `MenuStyle.quitButton` : disque rouge plein (`-gp-danger`), icône de 40 px, ombre, grossit au survol. Nouveau jeton `-gp-on-danger` (blanc en clair, 4,4:1 ; brun foncé en sombre, 6,5:1). Servira sur tous les écrans. |
| Le logo devrait être au milieu, au-dessus de « Choisissez un mode » | Logo centré au-dessus du titre ; la barre du haut ne garde que quitter, à droite. |
| Retirer les phrases sous les noms des modes | Descriptions retirées des cartes ; clés `ModeGamesDescription` et `ModeEvaluationsDescription` retirées de `multilinguism.csv` (plus utilisées ailleurs). |

Captures `etape2b` (clair et sombre ; 800, 1366, 1920) regardées ; `./gradlew build` réussi (462 tests, 0 échec).
Écran validé par le mainteneur (« nickel »).

**Plein écran (signalé par le mainteneur le 28/10/2026)** : la barre des tâches de Windows restait visible en bas et le
haut de la fenêtre était un peu coupé.
- **Cause (lue dans le code)** : la première page affiche la fenêtre, déjà en plein écran (`goToModePage` appelle
  `show()`), puis `GazePlayFxApp.start` appelait `centerOnScreen()`. Cette méthode place la fenêtre dans la zone utile
  de l'écran (sans la barre des tâches), au tiers de la hauteur : une fenêtre de la taille de l'écran remontait de
  quelques pixels, le haut coupé et la barre des tâches visible dessous. Même chose avec le bouton plein écran des
  menus (`GazePlay.toggleFullScreen`) au retour en plein écran. Défaut antérieur à la refonte (même ordre d'appels
  avec la page des profils). Les parties, lancées par la même méthode `start`, étaient aussi concernées.
- **Correction** : la fenêtre n'est centrée que si elle n'est pas en plein écran (démarrage et bouton). La position et
  la taille de la fenêtre sont écrites dans le journal au démarrage (`Window: x = …`). Test ajouté
  (`GazePlayModeTest.shouldCenterTheWindowOnlyWhenLeavingTheFullScreen`) ; `build` réussi, **463 tests, 0 échec**.
- **Hypothèse à confirmer par un essai réel** : Monocle n'a pas de vrai plein écran, la correction n'est vérifiée que
  par le test et la lecture du code.

Étapes 0 à 2 et correction du plein écran : commit `1703ea4b5` (28/10/2026, à la demande du mainteneur).

### 8.3 bis Étape 3 — profils (28/10/2026)

Fonctionnement inchangé (décision du mainteneur) : mêmes cartes dans une grille, mêmes fenêtres de création,
modification et suppression, mêmes contrôles dans le même ordre. Seule l'apparence change.

- **Page** : plus de `setStyle` (fond, bordure blanche et ombre noire écrits dans le code). En haut à droite :
  « Changer de mode » (bouton secondaire avec icône) et quitter (disque rouge, comme le choix du mode). Au centre :
  logo statique (l'animé n'est plus utilisé ici), **puce du mode** (icône et nom, orange pour Games, turquoise pour
  Eval), titre « Choisissez un profil » (nouvelle clé `ProfileChoose`, anglais et français), puis les cartes.
- **Cartes** : surface arrondie, image ronde, nom centré sur deux lignes au plus (plus de coupure à 20 caractères),
  même largeur et même hauteur pour toutes. Sans photo, un disque coloré avec une icône : profil par défaut en
  orange avec une personne et un engrenage, autres profils en turquoise, violet ou bleu ciel selon leur nom (toujours
  la même couleur pour un même profil), « Ajouter » en cercle pointillé avec un plus. Les images
  `DefaultUser.png`, `ConfigUser.png` et `AddUser.png` ne servent plus (à retirer à l'étape 7).
- **Modifier et supprimer** : boutons ronds avec icônes (crayon ; corbeille rouge) et infobulles, à côté de la carte
  comme avant.
- **Clavier** : une carte de profil prend le focus et se choisit avec Entrée ou Espace (avant : souris seulement).
- **Fenêtres** : au style des menus (fond, police, champ de texte arrondi, boutons « pilule » : Ok en accent,
  « Oui (supprimer) » en rouge, les autres secondaires) ; Ok est le bouton par défaut (Entrée). Message « déjà
  utilisé » en rouge par la feuille de style.
- **Correction en passant** : annuler le choix d'une image (fenêtre de fichiers fermée) levait une
  `NullPointerException` ; c'est maintenant sans effet.
- **`lookupProfilePicture`** renvoie `null` sans photo (au lieu de l'image par défaut) : la carte montre alors le
  disque coloré. Rien ne change dans les réglages enregistrés.
- **Tests** (`UserProfileContextTest`) : adaptés aux nouveaux composants (avatar, boutons, barre du haut) ; ajoutés :
  retour au choix du mode, disque coloré sans photo. **Constat** : `shouldLookupProfilePicture` lisait un fichier
  absent (`src/test/resources/bear.jpg` n'existe pas dans `gazeplay-core`) et ne passait que grâce à l'image par
  défaut ; il lit maintenant une vraie image des ressources.
- **Captures** : l'outil crée un profil avec photo (« Leo ») et capture aussi les fenêtres de création et de
  suppression (`2a-profil-nouveau`, `2b-profil-supprimer`, mode Jeux). Regardées en clair 1366 et 800, sombre 1920.
- **Vérifications** : `build` réussi, `smokeTest` réussi : **468 tests, 0 échec** (465 + 3 lancements).
  `allGamesTest` non relancé : seules des règles sous `.gp-menu` ont changé.

**Retour du mainteneur (28/10/2026)** : page et contenu des fenêtres validés, mais le cadre des fenêtres (barre de
titre de Windows, style `UTILITY`, croix rouge) « fait tâche ».
- **`MenuDialog`** (nouveau composant, `ui/menus`) : fenêtre sans cadre système (`StageStyle.TRANSPARENT`), dessinée
  comme une carte arrondie avec une ombre ; en haut, le titre de la fenêtre (Fredoka) et un bouton rond de fermeture
  (icône Material « close », infobulle « Fermer », nouvelle clé `Close`). La fenêtre se déplace en tirant son titre ;
  Échap la ferme. Fermer retire aussi le flou de la page. Servira aux fenêtres des variantes et des évaluations.
- Fenêtres des profils : hauteur à leur contenu (plus de hauteur minimale d'un tiers de l'écran, qui laissait un grand
  vide dans la fenêtre de suppression) ; largeur minimale d'un tiers de la fenêtre, comme avant.
- Test ajouté : la fenêtre est sans cadre et son bouton la ferme, retire le flou et ne supprime rien. Il utilise
  `WaitForAsyncUtils.asyncFx(...).get()` : **constat**, les autres tests des fenêtres de ce fichier vérifient dans
  `Platform.runLater`, où une assertion qui échoue ne fait pas échouer le test (à corriger à part).
- `build` et `smokeTest` réussis : **469 tests, 0 échec**. Captures `etape3b` regardées (clair 1366, sombre 1920).
- **À vérifier sur la vraie machine** : fenêtre transparente sous Windows (ombre, coins), déplacement par le titre.

### 8.4 Vérifications

- `./gradlew build` : réussi, **462 tests, 0 échec**, SpotBugs sans remarque (un `synchronized` statique de
  `MenuFonts` signalé puis retiré : la méthode n'est appelée que sur le thread JavaFX).
- Captures `etape2` (clair et sombre ; 800, 1366, 1920) : regardées.
- `smokeTest` et `allGamesTest` : réussis, **79 lancements, 0 échec** (les 76 jeux et les 3 essais de démarrage),
  avec `menus.css` et les nouveaux jetons chargés dans la JVM de chaque partie.
- **À faire par le mainteneur** : essai réel (survol, clavier, plein écran, écran du laboratoire), et retour sur le
  style avant l'étape 3.
