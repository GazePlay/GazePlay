# Refonte graphique des menus — état des lieux et plan

Rédigé le 27/10/2026, branche `addClaudeCode`. **Rien n'est codé** : le travail commence le 28/10/2026, à la
demande du mainteneur. Ce document sert de référence à chaque étape et sera complété au fur et à mesure.

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
