# Retours de la testeuse : récapitulatif

Tour fait le 24/11/2026. Source : le document de retours de la testeuse (non publié), reçu le 20/11/2026, partie
GazePlay Learning seulement (la partie sur le site de création d'évaluations n'est pas traitée ici). Chaque point a été
vérifié dans le code puis décidé par le mainteneur le 20/11/2026 ; le détail de chaque point (fichiers, choix,
candidats écartés) est dans `REVUE_DES_JEUX.md`, section « RETOURS DE LA TESTEUSE (20/11/2026) ».

Les tests de la testeuse ont été faits **avant le 20/11** : elle a entendu les anciennes voix.

**Tour de vérification du 24/11/2026** : commits, code, crédits des sons et des photos, inventaires, licence affichée et
aides vérifiés point par point. Corrigé à cette occasion : 7 aides anglaises qui gardaient l'ancien nom du réglage du
délai (point 22) ; la phrase du délai ajoutée aux aides de Coloriage et des 9 jeux de la famille Où est… ; dans ces 9
aides, le compteur décrit comme le numéro de l'écran en cours (point 14) ; `gradle/images/inventaire-sons.csv`
régénéré (1 080 sons, tous libres et de source connue ; deux générations de suite identiques).

## En résumé

| État | Points |
|---|---|
| **Fait** (commité et poussé, validé par le mainteneur) | 1 à 12, 14 à 19, 22 à 26, 27 (mise en page), 29 |
| **En attente** | 13, 20, 27 (reformulations), 28, 31 |
| **Refusé / laissé tel quel** | 21, 30, 32, retours positifs |
| **Reste à faire** (demande du mainteneur en cours de route) | liste des jeux du menu dans l'ordre alphabétique |

## Faits

| N° | Retour de la testeuse | Décision | Ce qui a été fait | Commits |
|---|---|---|---|---|
| 1 | Résultats des jeux mélangés dans le dossier du jour | Un dossier par partie ; le bouton « Résultats » ouvre celui de la partie | `statistics\<jeu>\<aaaa-mm-jj>\<hh-mm-ss>_<variante>\`, fichiers sans l'heure devant ; le rejeu lit aussi les anciens fichiers ; format v6 inchangé | `534e83dbe` |
| 2 | La fixation repart de zéro quand le regard quitte une cible qui bouge | Option générale, « reprend de zéro » par défaut | Option « Si le regard quitte la cible en mouvement » (réglages et menu Échap), active seulement dans Ninja, Bulles, La tarte à la crème et Diviseur / Lapins (Fixation), grisée ailleurs | `c0145f025` |
| 3 | Options pas rangées dans l'ordre de difficulté | Du plus facile au plus difficile, le plus facile proposé par défaut | Tous les jeux vérifiés ; « Sans cœurs » avant « Avec cœurs » partout ; Tarte à la crème, Bouteille, Cartes à gratter remis dans l'ordre | `569f3bdcf` |
| 4 | Point par point : points trop proches (tronc du petit sapin) | Écart minimum dans toutes les figures Faciles | Écart minimum 0,185 de la hauteur ; sapin, bateau, poisson, drapeau corrigés ; tests | `fa4b65bdc` |
| 5 | Trouve les différences : deux micros identiques comptés comme une différence | Corriger et vérifier toutes les scènes | Même cause que le 6 : palette commune par scène, changements gardés seulement s'ils se voient | `61835d8f0` |
| 6 | Trouve les différences : différences de couleur visibles mais non déclarées | Double vérification pixel par pixel | Vérification dans le script et dans `SpotTheDifferencesTest` pour toutes les scènes | `61835d8f0` |
| 7 | Casse-brique trop difficile | Le niveau change le nombre et la taille des briques | 4 niveaux (6, ~32, ~78, ~120 briques), balle lente à tous les niveaux, grande raquette, dessins refaits par niveau | `6c996a5d9` |
| 8 | Aiguillages : le train suivant part tout seul | Il attend un bouton « Départ » | Bouton rond avec un train, à la place libre la plus proche du tunnel | `3f7d10f6b` |
| 9 | Aiguillages : pas de rythme assez facile | Nouveau rythme le plus facile | « Un train à la fois et pause à chaque aiguillage », proposé par défaut | `130566284` |
| 10 | Aiguillages : « Reprendre » loin de l'aiguillage | À côté de l'aiguillage arrêté | Place libre la plus proche, sans recouvrir les rails | `7063243e4` |
| 11 | Vignettes d'Où est le son ? et d'Où est-ce ? avec des animaux | Instruments et objets du quotidien | Vignettes refaites ; les 6 autres vérifiées | `8a888d478` |
| 12 | Volumes différents (voix plus faible que la récompense) | Égaliser voix et enregistrements, musiques inchangées | Voix au même niveau, enregistrements à −17 LUFS | `3a47c688c` |
| 14 | Où est… : le compteur montre les écrans trouvés | Numéro de l'écran en cours | « Écran 7 », dès l'arrivée des images | `8a13ef0e8` |
| 15 | Où est… : la même bonne réponse revient souvent | Tirage sans répétition | Liste mélangée, remélangée une fois épuisée ; aussi Trouve l'intrus | `dd3028f39` |
| 16 | Où est le son ? : le lion ressemble à un cochon | Nouveau rugissement libre | Candidat 1 choisi (domaine public), aussi dans Les sons de la vie ; crédits et inventaire | `cecb8f470` |
| 17 | Photo d'éléphants inadaptée | Éléphant seul ; revue de toutes les photos | Remplacée (CC BY-SA 4.0) ; revue complète faite avec le point 26 | `9baac32eb` |
| 18 | Violon accompagné de percussions et de guitare | Violon seul ; analyser les autres instruments | Violon seul (CC0) ; guitare seule aussi remplacée à la demande du mainteneur | `96789966b` |
| 19 | Accordéon de musée peu reconnaissable | Accordéon courant | Accordéon à touches piano (CC0) | `61e94bbc1` |
| 22 | Choix involontaires à la souris quand de nouveaux boutons apparaissent | Pas de « quitter puis revenir » : rien n'est choisissable pendant le délai des réglages à chaque nouveau choix | Délai commun (`IGameContext.onGameStarted`) dans une vingtaine de jeux ; réglages de la partie et délais écrits dans `replayData.json` (v6). Magic Potions (décision du 24/11) : délai seulement au début de chaque nouvelle potion à faire, la première comprise | `ad8b1dbf3` à `fbcc4aa42`, `976fa2aa7` |
| 23 | La Fabrique à gâteaux : impossible de corriger une erreur | Bouton « Annuler » sans limite | Annule jusqu'au début du gâteau ; en mode Copier, l'erreur reste comptée | `0b7f43927` |
| 24 | Le mot « Normal » renvoie à une norme | « Moyen » / « Medium » partout ; « Plateau classique » aux Petits chevaux | 22 textes et 56 aides ; les autres mots signalés (Classique, Standard) gardés par le mainteneur | `adabe1b69` |
| 25 | Blocs : trop long de retirer tous les blocs | Réussite à 90 % | Les blocs restants disparaissent seuls ; 9 sur 9 gardé en 3 × 3 | `8083aabcb` |
| 26 | Photos avec des personnes reconnaissables | Toutes remplacées (droit à l'image) | 209 photos revues : 17 remplacées (musiciens, contenus choquants…), 25 ajoutées ; crédits et inventaire | `153d11a21` |
| 27 | Aide Eval « Comprendre les fichiers de résultats » difficile à lire | Nouvelle mise en page | Tableaux, exemples, « À quoi il sert », « Bon à savoir » (les reformulations sont en attente, voir plus bas) | `6091c97b6` |
| 29 | Images à fond transparent : fond sombre visible, il faut viser le dessin | Cartes à choisir blanches dans les deux thèmes, choisies sur toute leur surface | Memory, Cartes magiques, Où est…, Opinions, Blocs, Identifier les lettres, Fleur des nombres. Exclus : La pièce, GazePlay Eval, objets posés sur un décor. Suite (24/11) : thèmes des jeux et des évaluations séparés, boutons clairs ou sombres au choix | `a9ab50a15`, `e8472447c`, `87fa9ba20`, `18eadf455`, `8903e6a9b`, `718bd2606`, `86adf41c0`, `24cb766f0`, puis `62ad247de` |

## En attente

| N° | Retour de la testeuse | Pourquoi c'est en attente | Ce qu'il faut pour avancer |
|---|---|---|---|
| 13 | Voix trop rapides, sans intonation | Les voix ont toutes été refaites le 20/11, après ses essais | Qu'elle écoute les nouvelles voix ; si besoin, un réglage Normal / Lent (voix lentes générées avec Piper) |
| 20 | Point par point : le son et la secousse d'une erreur peuvent pousser à la refaire exprès | Question pédagogique | Réunion avec Amélie. Pistes : A rien changer, B ne rien montrer, C option dans les variantes, D option générale pour tous les jeux qui réagissent aux erreurs |
| 27 | Passages de l'aide Eval difficiles à comprendre | La mise en page est faite, mais on ne sait pas quels passages la gênent | Qu'elle indique les passages à reformuler |
| 28 | Eval : la croix de fixation n'apparaît pas dans `echantillons.csv` (« - ») | Le mainteneur trouve bizarre de la mettre dans la colonne des zones, qui ne liste que des cibles choisissables | En parler avec elle. Piste : une colonne à part (`sur_la_croix`) |
| 31 | Où est-ce ? (avec ses images) : elle n'a pas réussi | Jeu et fonctionnement gardés tels quels | Revoir avec elle : dossier `images\<réponse>\`, textes dans `questions.csv`, sons dans `sounds\`. Limite notée : chaque sous-dossier peut devenir la réponse |

## Refusés ou laissés tels quels (décisions du mainteneur)

| N° | Retour de la testeuse | Raison |
|---|---|---|
| 21 | Point par point : zone de sélection plus large | La zone fait déjà 1,45 fois le point |
| 30 | Cartes à gratter : du blanc sur la carte, et la fin de la carte | Le blanc venait des images transparentes (corrigé par le point 29) ; la fin à 90 % de la carte entière est gardée |
| 32 | Eval : supprimer une évaluation importée | Existe déjà, dans les réglages du mode Évaluations |
| — | Retours positifs (écran des statistiques, menus, menu Échap, Labyrinthe, Quatre en ligne, Biboule Jump, Ordre, Jeu de taquin, Bulles) | Rien à faire |

## Reste à faire

- **Liste des jeux du menu dans l'ordre alphabétique** (demande du mainteneur du 20/11), d'après les noms affichés dans
  la langue choisie.
