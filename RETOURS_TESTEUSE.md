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
| **Fait aussi** (demande du mainteneur en cours de route) | liste des jeux du menu dans l'ordre alphabétique (24/11) |
| **Nouveaux retours du 23 et 24/11** (à décider) | 33 à 83, voir la dernière partie ; 6 déjà réglés entre-temps, 2 en partie |

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
| 28 | Eval : la croix de fixation n'apparaît pas dans `echantillons.csv` (« - ») | Le mainteneur trouve bizarre de la mettre dans la colonne des zones, qui ne liste que des cibles choisissables | En parler avec elle. Piste : une colonne à part (`sur_la_croix`). **Demande du mainteneur du 25/11 : point 87** |

## Refusés ou laissés tels quels (décisions du mainteneur)

| N° | Retour de la testeuse | Raison |
|---|---|---|
| 21 | Point par point : zone de sélection plus large | La zone fait déjà 1,45 fois le point |
| 30 | Cartes à gratter : du blanc sur la carte, et la fin de la carte | Le blanc venait des images transparentes (corrigé par le point 29) ; la fin à 90 % de la carte entière est gardée |
| 32 | Eval : supprimer une évaluation importée | Existe déjà, dans les réglages du mode Évaluations |
| — | Retours positifs (écran des statistiques, menus, menu Échap, Labyrinthe, Quatre en ligne, Biboule Jump, Ordre, Jeu de taquin, Bulles) | Rien à faire |

## Demande du mainteneur en cours de route

- **Liste des jeux du menu dans l'ordre alphabétique** (demande du 20/11) : **faite le 24/11/2026**, d'après les noms
  affichés dans la langue choisie (accents, ligatures et majuscules ignorés).

# Nouveaux retours du 23 et du 24/11/2026

Relevés le 24/11/2026 dans le même document de la testeuse (sections « Retours du 23-11-26 » et « Retours sur GPL
24-11-26 »), partie GazePlay Learning seulement. Ordre de travail décidé : d'abord les deux défauts (58 et 33), puis le groupe 1
(34, 78, 79, 82), le soir du 24/11. Les autres points attendent la décision du mainteneur.

## Déjà réglés par les changements faits entre-temps

| Retour | État |
|---|---|
| Identifier les lettres : la lettre du haut peu lisible en thème sombre | Réglé le 24/11 : carte blanche, lettre foncée dans les deux thèmes (point 29) |
| Changer de thème depuis le menu Échap | Fait le 24/11 : ligne « Thème des jeux », appliquée en direct |
| Trouve l'intrus : consigne dite en anglais | Probablement réglé : la voix française dit « Trouve l'intrus » depuis le 20/11 (voix refaites) ; à lui faire revérifier avec la version actuelle |
| Cartes à gratter : la grosse pièce en premier | Fait le 20/11 (point 3) |
| Bouteille : les grosses bouteilles en premier | Fait le 20/11 (point 3) |
| Les bruits de la vie : le lion | Fait le 23/11 (point 16, même son que dans Où est le son ?) |
| Cartes magiques : contraste entre la carte et le fond | En partie : face des cartes blanche (point 29) ; un cadre plus marqué reste à décider (point 79) |
| Fleur des nombres : contraste des cases | En partie : cartes des valeurs blanches le 24/11 ; les cadres restent à décider (point 68) |

## Faits le 24/11/2026 au soir (validés par le mainteneur)

| N° | Jeu | Retour | Ce qui a été fait |
|---|---|---|---|
| 33 | Tous | Anneau qui paraît plein avant de l'être | Bouts droits (`StrokeLineCap.BUTT`) dans l'anneau commun et `ProgressButton` (`800b00856`) |
| 58 | Dés | Un dé passe devant le menu Échap | Le dé 3D dans sa propre sous-scène (`DiceView`, `e499ec1ba`) |
| 78 | Bouteille | Casser librement, avec ou sans fixation | Modes « Tout casser » (de retour) et « Détruire les cibles » ; « Dès qu'on regarde » (par défaut) ou « Après une fixation » en « Tout casser » seulement ; « Détruire les cibles » toujours avec une fixation (`1d6793c13`) |
| 79 | Cartes magiques | Une seule carte à retrouver | Choix 1, 2 ou 3 cartes à trouver (1 par défaut) ; anneau de la couleur des réglages aussi sur le dos des cartes, Memory compris, liseré clair seulement avec la couleur principale (`1ad767ed0`) |
| 82 | Mélanger les couleurs / Magic Potions | Deux fois le même jeu | Magic Potions devient **Recettes magiques** : recette de 2, 3 ou 4 ingrédients en images (OpenMoji) à trouver sur l'étagère ; cœurs ; 3, 5, 10 recettes ou sans fin ; explosion seulement à la recette complète, puis la Biboule boit la potion ; pas de voix ; anneau de fixation au bord des cartes (25/11). Validé et commité le 25/11. Le tirage sans répétition de Mélanger les couleurs : fait le 25/11 |

## Faits le 25/11/2026, groupe 2 (validés par le mainteneur)

| N° | Jeu | Retour | Ce qui a été fait |
|---|---|---|---|
| 73 | Coloriage | Couleurs du « + » : fixation qui reprend à zéro, points qui bougent | Le trait de l'anneau dépassait la place de sa pastille et la grille les replaçait toutes : positions fixes, 5 par rangée ; la fenêtre fermée ne suit plus le regard |
| 44 | Gribouille | Impossible de quitter la gomme en arc-en-ciel | Bouton « crayon arc-en-ciel » à côté de la gomme, entouré au départ ; aides |
| 42 | Identifier les lettres | Images pas à la bonne taille par rapport aux lettres (2 × 3) | Image entière (elle était coupée, jusqu'à 44 %), sans ses marges blanches ou transparentes, sous des cartes carrées, aux 3 niveaux ; vignette refaite |
| 37 | Où est la forme | Le losange ressemble à un carré | C'était un carré sur sa pointe : redessiné plus haut que large (340 × 580), vignette et inventaire |
| 56 | Survivre contre des robots | Nouveaux robots loin du Biboule | À 0,7 hauteur d'écran au moins de la Biboule (0,45 avant) et 0,45 de l'endroit où elle va |
| 57 | Tir | Nom « Invasion » | Renommé en français et en anglais (nom code `Shooter` gardé), licence |
| 62, 82 | Apprends les Maths, Mélanger les couleurs | Répétitions | Rien ne revient avant que tout le niveau soit passé ; maths : 3 + 4 et 4 + 3 comptent pour un, et les calculs autour des mêmes nombres sont séparés (`MathDeck`) |
| 70 | Morpion | Niveaux moyen et difficile inversés | L'ordinateur moyen oublie de bloquer une fois sur deux : un joueur attentif gagne 77, 58 et 30 parties sur 100 (facile, moyen, difficile ; 77, 9 et 30 avant) |
| 72 | Coloriage | Même palette à tous les niveaux | La palette (« nickel » pour elle) est gardée, les niveaux sont renommés d'après leurs dessins : Facile : 1 ou 2 zones, Moyen : 3 à 6, Difficile : 7 ou plus, Mes dessins ; aide corrigée |
| 84 | Où est la forme (demande du mainteneur) | « ovale » avec un « e » | Texte français et `voix.csv` ; enregistrements gardés (même prononciation) |
| 85 | Recettes magiques (demande du mainteneur) | Recette visible ou cachée | Choix en haut des variantes ; cachée : montrée 3, 4 ou 5 s, puis des « ? » ; un ingrédient trouvé réapparaît coché ; rappel en fondu à chaque erreur ; rien à choisir pendant qu'elle est montrée |
| 86, 31 | Où est-ce ? (demande du mainteneur ; la testeuse n'avait pas réussi avec le dossier) | Faire soi-même son jeu dans une fenêtre | Maquette A : « Mes jeux » (slider, au moins 2 visibles, « Créer mon Où est-ce ? » toujours à droite, rond rouge pour supprimer), éditeur écran par écran (grille, images, phrase, son, bonne réponse) ; dossier fait par GazePlay (`ou-est-ce\<nom>\jeu.json`, copies des images et des sons) ; chaque écran une fois, mélangés ; cœurs ; ancien dossier `images\<réponse>`, `questions.csv` et ligne des réglages retirés |

## Faits le 26/11/2026 (validés par le mainteneur)

| N° | Jeu | Retour | Ce qui a été fait |
|---|---|---|---|
| 45 | Gribouille | Bouton « + » qui efface tout : trop dangereux | « Nouvelle feuille » devient une corbeille, seule en bas de la barre, sous un espace ; sa fixation n'efface rien : « Effacer le dessin ? » avec ✔ et ✖ à sa place, de part et d'autre (pas sous le regard) ; ✖, un autre bouton ou 5 s sans réponse : le dessin reste ; le temps s'arrête pendant qu'on regarde ✔ ou ✖ ; boutons de la barre environ 18 % plus petits (8,5 rangées) ; aides |
| — | Gribouille | Reprendre la palette du Coloriage (remarque du 24/11) | **Abandonné** (mainteneur, 26/11) |
| 83 | Cartes à gratter | La pièce suit le regard qui sort de la zone | La zone du regard dépasse la carte d'une marge (1,5 rayon de pièce, au moins 8 % de la hauteur) : la pièce suit le regard le long du bord et gratte le bord ; plus loin, elle se lève ; levée, elle glisse jusqu'au regard sans gratter au lieu de sauter (demande du mainteneur) ; grosse pièce déjà en premier ; aides |
| 76 | Petits chevaux | Dés aidés après plusieurs échecs | Après des lancers de suite sans pouvoir bouger, le dé donne la face qu'il faut (faire arriver un pion, sinon un 6 pour sortir, sinon la plus petite qui fait bouger) : 2 lancers pour une personne, 3 pour l'ordinateur (choix du mainteneur ; elle proposait 3, et 5 pour le 6 de l'ordinateur) ; tirage gardé pour le rejeu ; aides |
| 77 | Petits chevaux | L'ordinateur fait avancer tous ses pions | Ses pions sortis avancent chacun à son tour (celui qui a bougé il y a le plus longtemps), après ses priorités (prendre, monter l'échelle, sortir) ; aides |
| 74 | Coloriage | Bouton « Imprimer » à pouvoir masquer | Réglage « Coloriage : bouton Imprimer » (Jeux, coché par défaut, choix du mainteneur ; clé `COLORING_PRINT_ENABLED`) |
| 75 | Coloriage | Enregistrement dans un dossier défini, à pouvoir interdire | Comme Gribouille : plus de fenêtre Windows, dessin gardé dans `dessins` (`coloriage_date.png`) avec un message ; réglage « Coloriage : bouton Garder mon dessin » (coché par défaut ; `COLORING_SAVE_ENABLED`) ; ligne des réglages « Mes dessins (Gribouille et Coloriage) » ; aides |
| 71 | Pierre papier ciseaux | Erreur plus visible pour qui n'entend pas | Croix rouge sur la main perdante (la main du joueur tremble), cercle et coche verts sur la gagnante, « = » jaune à l'égalité, dans les deux modes ; demandes du mainteneur : le résultat reste jusqu'à la flèche « Manche suivante » (étiquette dessous, plus de temps fixe), « Pierre… papier… ciseaux ! » et « Gagné / Perdu / Égalité » entre les deux mains ; aides |
| 68 | Fleur des nombres | Cadres de la couleur des pétales, plus épais ou remplis | Sa possibilité 1 (choix du mainteneur) : fond de chaque cadre de la couleur de son pétale, éclaircie, bordure de 10 px (5 avant), marge de 16 px ; cadres à leur place ; même rendu dans les deux thèmes (captures regardées) |
| 63 | Embouteillage | Niveau facile trop difficile | Niveau « Très facile » en premier (proposé par défaut) : 30 parkings 6 × 6, un seul véhicule à déplacer (2 coups) parmi 3 à 6 ; faits par `make_trafficjam_levels.py --very-easy` (tirage à part, les 90 autres parkings inchangés) ; aides |
| 59 | Dés | Icône « Lancer » ; dé à lancer surligné | Choix du mainteneur après essai : le bouton « Lancer » gardé dans les deux modes, avec un halo jaune qui pulse tant qu'on peut lancer (les dés à fixer en « compter » essayés puis écartés) ; aides |
| 60 | Dés | Inverser les deux modes | « Lancer les dés » en premier (proposé à l'ouverture), puis « Compter les points », qui s'ouvre sur 5 totaux (fin cachée propre à « lancer ») |
| 61 | Dés | Total plus visible (dit à voix haute, calcul décomposé) | Calcul écrit pas à pas en haut (« 3 + 5 = 8 », un élément toutes les 0,6 s, plus grand à la fin), chaque dé grossit quand son nombre s'écrit ; un seul dé : son nombre ; « Total » retiré ; **sans voix** (il faudrait générer les nombres 1 à 36, « plus », « égale ») |
| 55 | La course de grenouilles | Niveaux trop simples | Adverses plus rapides : Facile 60 à 75 s, Moyen 35 à 45 s, Difficile 20 à 26 s (90-110, 60-75, 40-50 avant ; estimation à ajuster à l'essai) ; demande du mainteneur : interrupteur « Attrapés dès qu'on les regarde / après une fixation » (anneau autour de l'insecte, réglage des cibles en mouvement dans le menu Échap) ; couleurs des grenouilles non touchées ; aides |
| 53 | Diviseur | Choix du fond | Selon le thème (choix du mainteneur) : Lapins garde la prairie aux œufs de Pâques ; Animaux et Mes images ont la même prairie sans œufs, dessinée par le jeu (touffes d'herbe) ; un disque blanc sous l'image de chaque cible (les images à fond transparent montraient l'herbe) ; aides |
| 52 | Biboule Jump | Saut et chute plus lents | Selon le niveau (choix du mainteneur) : Facile 1,6 fois plus lents, Moyen 1,25 fois, Difficile comme avant ; même hauteur de saut, gauche-droite aussi rapide ; aides |
| — | Menu (demande du mainteneur) | Nombre de variantes inutile | Ligne retirée du bas du menu, le nombre de jeux reste (`c93b94c58`) |
| 49 | Bulles | Mode plus facile (moins de bulles, plus grosses) | Interrupteur Facile / Moyen / Difficile (Facile par défaut) : 9, 16 ou 36 vitres, bulles 1,5 ou 1,2 fois plus grosses en Facile et Moyen ; Difficile = le jeu d'avant ; aides |
| 51 | Bulles | Image gardée, avec un bouton « suite » | L'image découverte reste affichée sans les bulles, sans limite de temps (choix du mainteneur), jusqu'à la flèche ronde à droite (fixation ou clic de l'adulte) ; seulement dans les Bulles pour l'instant ; aides |
| 43 | Gobelets Pass Pass | Transition entre deux tours trompeuse (la balle d'avant semble encore là) | En fin de manche, les gobelets soulevés redescendent doucement (0,45 s, ils retombaient d'un coup) ; au début de chaque manche, tous les gobelets se soulèvent ensemble : une seule balle, sous l'un d'eux ; aides |
| 36 | Où est la forme, la couleur | Modes de consigne avec exemple | Interrupteur Apprentissage / Entraînement / Maîtrise (ses noms, Apprentissage par défaut, avant les cœurs) pour la forme, la couleur et le chiffre (pas la lettre : la question la montre déjà) ; carte blanche de l'image de la bonne réponse à droite de la phrase, qui descend avec elle dans la bande ; Apprentissage : elle reste pendant le choix ; Entraînement : elle s'efface une fois les images affichées ; Maîtrise : sans exemple ; aides. Les réponses déjà affichées à la consigne : avec le point 35 (débat) |
| 46 | Gribouille | Couleur blanche à la place de la gomme | Choix du mainteneur : la gomme reste, mais se voit : une gomme dessinée suit le regard sur la feuille (comme la pièce de Cartes à gratter), une brosse à tableau sur le tableau noir (aussi dans le bouton) ; largeur effacée 2,5 %, 4,5 % ou 7 % de la hauteur de l'écran selon l'épaisseur (avant : deux fois le trait) ; aides |
| 47 | Gribouille | Points d'épaisseur de la couleur choisie | Points des 3 épaisseurs de la couleur choisie (en dégradé en Arc-en-ciel ; avec la gomme, la couleur qu'on retrouvera), contour de la couleur du texte des boutons ; aides |
| 48 | Gribouille | Mode « la fixation commence et finit le trait » | Interrupteur « Trait libre / Trait par fixation » (8 variantes, libre sans variante) ; par fixation : crayon levé, un cercle vide de la couleur suit le regard ; une fixation (3,5 % de la hauteur, temps des réglages, anneau) pose le crayon sous le cercle, une autre le lève, bouger avant la suivante ; sortir de la feuille ou perdre le regard plus de 0,35 s le lève ; souris immobile = fixation ; aides |
| — | Menu (demande du mainteneur) | Recherche vidée au retour d'une partie | Recherche, filtre « Favoris » et défilement gardés quand le menu est reconstruit (fin de partie, orientation des cartes), pas au changement de profil ; croix ronde pour vider la recherche, grisée quand elle est vide (`0500c0ada`) |

## À décider

| N° | Jeu | Retour | Remarque |
|---|---|---|---|
| 34 | Page des jeux | Catégories par compétence (découverte du regard, fixation, exploration…), jeux rangés par difficulté | **Mis de côté** (mainteneur, 24/11 : « à débattre ») |
| 35 | Où est… | Délai trop long entre la consigne et les réponses | **À débattre à la prochaine réunion** (mainteneur, 26/11) : avec le réglage « Durée d'affichage des questions » (Jeux, 5 s par défaut, de 0,5 à 20 s), il trouve cela rapide. Pistes : images dès la fin de la voix, images avec la consigne, durée par défaut plus courte, voix redite à l'apparition des images |
| 38 | Piano | Version courte (début de la chanson, puis la chanson en récompense) | **Décidé le 26/11** (maquette validée) : interrupteur « Début de la chanson / Chanson entière », début en premier ; débuts : Au clair de la lune 11 notes, Ah ! vous dirai-je maman 14, L'Ode à la joie 15, Joyeux anniversaire 6 ; récompense : la chanson entière rejouée en rythme ; statistiques inchangées |
| 39 | Piano | Choisir la chanson | **Décidé le 26/11** (maquette validée) : interrupteur « La personne choisit / Au hasard », choisir en premier ; écran « Choisis ta chanson » au début et après chaque chanson : 5 cartes par fixation (anneau au bord des cartes) au-dessus du clavier grisé, 4 chansons avec leur image et « Au hasard » (dé) ; la fin (1, 3, 5, sans fin) compte les chansons jouées |
| 40 | Piano | Pastilles sur les touches noires (couleurs des deux touches blanches voisines) | **Décidé le 26/11** (maquette validée) : pastille ronde moitié-moitié, cerclée de blanc, dans le bas de chaque touche noire, là où se remplit l'anneau. **Codé le 27/11**, à essayer |
| 41 | Piano | Animation de fin | **Décidé le 26/11** (maquette validée) : pendant la chanson rejouée, une grande image de la chanson au-dessus du clavier se balance et rebondit à chaque note ; images OpenMoji (CC BY-SA 4.0) : lune, étoile, soleil souriant (Ode à la joie), gâteau ; les mêmes sur les cartes du choix |
| 50 | Bulles | Jeu lent avec de grosses images (Mes images) | |
| 54 | Course du regard | Adaptation au regard ; récompense à mesure qu'on évite les voitures | |
| 64 | Jeu de l'oie | Visuel : contraste et taille des pions, pion actif et dé surlignés, plateau sans cases inutiles en version simple | |
| 65 | Jeu de l'oie | Règles adoucies : dés aidés, pas de retour au départ, prison réduite, arrivée aidée | |
| 66 | Jeu de l'oie | Compréhension : pion plus lent, points du dé remplis case par case, couleur du pion choisie | |
| 67 | Fleur des nombres | Variante « même nombre dans chaque pétale » | **À débattre à la prochaine réunion** (mainteneur, 26/11) : la testeuse doit préciser la règle (« à discuter pour que je t'en dise plus ») |
| 69 | Morpion | Écran de récompense | |
| 80 | J'ai un Biboule ! (`Pet`) | Sons sur les actions ; plus de choix (jeux, plats) | |
| 81 | Les bruits de la vie | Sons du singe et du dauphin ; scène de la mer avec une surface | |

## Demandes du mainteneur du 25/11/2026 (à faire)

Notées telles que demandées ; rien n'est codé.

| N° | Jeu | Demande | Remarque |
|---|---|---|---|
| 87 | Eval | **À faire en dernier (mainteneur, 25/11).** Écrans avec une **croix de fixation** : noter quand la personne entre dessus, en sort, combien de temps elle y reste, etc., **que la croix soit fixable ou non** | Répond au point 28 (croix absente d'`echantillons.csv`). Touche les fichiers de résultats d'Eval : expliquer l'impact et versionner (`formatVersion`) avant de coder, et mettre à jour l'aide « Comprendre les fichiers de résultats » |
| 88 | Tous les jeux (écran « Bravo ») | **À faire à la fin (mainteneur, 26/11).** La Biboule de l'écran de victoire est mal détourée et un peu pixelisée : la rendre propre | Image `data/common/images/bravo.gif` (`Bravo`), 891 × 627, GIF à palette (2 images) : sa transparence est tout ou rien, d'où le bord en escalier. Piste : la redessiner (comme la Biboule des jeux revus) ou en faire un PNG à transparence douce, à plus grande taille |

## Rien à faire

Labyrinthe, Quatre en ligne, Ordre, Jeu de taquin, Défendre le château, La bataille de l'espace, Sprint jusqu'à
l'arrivée, Suivre le chemin, Échelle, J'ai un œuf : « nickel » selon la testeuse.
