# Revue des jeux, un par un

Préparée le 28/10/2026, pour la revue qui commence le 29/10/2026 (décision du mainteneur) : passer sur chaque jeu, un
par un, pour l'améliorer **graphiquement** et en **gameplay**. Pour chaque jeu sont déjà notés les points sur ses images
(licences, voir [INVENTAIRE_IMAGES.md](INVENTAIRE_IMAGES.md) et `gradle/images/inventaire-images.csv`). Les colonnes
« Graphisme » et « Gameplay » se remplissent pendant la revue.

## REPRISE DU LUNDI 9/11/2026 (bilan de la séance du vendredi 6/11/2026)

**Lundi 9/11/2026 : reprise de la revue des jeux, sur Course du regard (Gaze Race)**, décalée de vendredi à lundi par le mainteneur.
Vérifier d'abord la CI du dernier push du 6/11. Tout est poussé sur `addClaudeCode`. Dernier `build` complet : **883 tests, 0 échec**.

**Médias illisibles, validés le 6/11/2026** (deux passations et le mode test, mainteneur ; fichiers relus) : temps de passage court,
bouton grisé, image de consigne illisible, même comportement en mode test et en vraie passation. Message
lisible et centré, gris correct. Fichiers de 0 octet (`test.mp4`, `test.png`) bien signalés. Bouton fermé : 6,0 s au regard et 6,2 s à
la souris sur une consigne à image, sans déclenchement (il en faut 4 : 1 s de délai et 3 s de fixation). Sur les consignes à vidéo, le
plus long séjour réel est de 3,9 s : même code, couvert par les tests avec une souris simulée. Commité et poussé le 6/11.

**Retour d'un utilisateur, fait et essayé le 6/11/2026** : le bouton Quitter des 4 écrans de menus (choix du mode, profils, jeux,
évaluations) demande « Voulez-vous vraiment quitter GazePlay Learning ? » (`MenuButtons.confirmedQuitButton`). Décision du mainteneur :
seul le clic valide ; Entrée et Échap annulent, et le clavier n'atteint pas « Quitter » (`MenuMessageDialog.confirmByClick`). Alt+F4
reste le moyen rapide de quitter ; si la question est ouverte, il ne ferme qu'elle.

**Retour du mainteneur, fait et essayé le 6/11/2026** : plus aucun terminal avec l'installeur. Les raccourcis lancent `javaw.exe`, et les
parties aussi. Installé et validé par le mainteneur : partie lancée du menu sans terminal, jeu des bulles joué au Tobii Eye Tracker 5.
L'installeur sur le disque (6/11, 13:35) ne contient ni la correction des raccourcis de jeux, ni l'anneau ci-dessous.

**Raccourcis de jeux des réglages : reportés par le mainteneur, issue [#1740](https://github.com/GazePlay/GazePlay/issues/1740).**
Correction commitée (`GameShortcut`, `52dec18f0`) et essayée sur de vrais `.lnk` ; restent le message du bouton « Générer » et les
essais depuis ce bouton.

**Évaluation « Test formats compatibles », préparée dans un autre terminal pour les retours sur le site, 6/11/2026** : passation
réussie au Tobii. Vidéo H.264 sans piste audio jouée sans erreur, image et `.wav` corrects. Le `.aiff` a été lu jusqu'au bout hors
évaluation, mais pas entendu dans une passation (case rouge choisie, c'est la jaune qui le joue). À noter : `fin_video` arrive 5,4 s
après `debut_video` pour une vidéo de 4 s. Hypothèse : chargement de FFmpeg à la première ouverture (0,2 à 1,3 s dans les passations
du jour, puis environ 20 ms), pendant lequel la vidéo n'est pas encore affichée.

**Anneau de fixation d'une évaluation, retour du mainteneur, fait et validé le 6/11/2026** : sur « Commencer », il faisait 5 px à côté
d'une bordure de 3 px de la même couleur. Minimum de 14 px sur le bord d'une cible (boutons, cases des petites grilles), comme sur une
grille 2×2 en plein écran (`EvalTarget.ringWidth`). La croix de fixation ne change pas.

**CI** : verte sur les 7 jobs jusqu'à `52dec18f0` (run `42222833553`), `package-macos` compris. Le repli apt (`00f78624f`) vise
`archive.ubuntu.com` dès le 2ᵉ essai : à revoir la prochaine fois que le miroir sera lent. Si `package-macos` est de nouveau annulé
sans étape, essayer par exemple `macos-15` (hypothèse : pas de machine `macos-latest` attribuée).

**Reprise de la revue, sur Course du regard (Gaze Race)**, dernier jeu du groupe 6, puis groupes 7 à 10. Comme toujours : état
des lieux, **demander d'abord la règle voulue**, propositions validées, puis le travail ; un commit par jeu, sur demande. Pas de
nouveau `.exe` sans demande.

**Nouveautés à connaître pour la revue des jeux** :
- Plus d'engrenage : le menu d'une partie s'ouvre avec Échap (`GameMenuPanel`). Les coins gardés libres pour l'engrenage (Où est-ce,
  Bulles, Opinions, Fleur de nombres) sont à libérer à la refonte du menu.
- Pointeur de la souris géré par l'écran de partie (`GameCursor`) : aucun jeu n'a à s'en occuper.
- En mode Tobii + souris, le mélange des deux entrées sur une même cible n'est pas à gérer (décision du mainteneur) : dans Bulles et
  `ProgressButton`, l'anneau repart de zéro quand la seconde entrée arrive, et c'est accepté.

**Restent ouverts** :
- La tablette : issue [#1738](https://github.com/GazePlay/GazePlay/issues/1738), à faire quand un appareil sera disponible.
- Le lancement réel des vidéos par FFmpeg sur les plateformes autres que Windows.
- Les sons des évaluations restent sur JavaFX Media : `.m4a` et `.aac` illisibles sur ce PC. On ne change rien sur le site (décision
  du mainteneur) ; le message `EvalUnsupportedMedia` les annonce encore comme lisibles.
- La cause des échecs de JavaFX Media (mise à jour Windows du 14/10/2026 : hypothèse non prouvée).

**À faire après la revue des jeux** : refonte graphique du menu des jeux et de l'écran des statistiques, mémoire des tests, retrait
des limiteurs, tests instables, inventaire des sons, notes de version 2.0 (détail dans les sections suivantes).

## AVANCEMENT DU JEUDI 5/11/2026 : retours sur GazePlay Learning Eval (commité et poussé, sauf la fin du retour 4)

**Retour 1 : l'engrenage cache des images** quand une évaluation en affiche beaucoup. Décisions du mainteneur : on retire
l'engrenage de **tous les jeux et des évaluations** ; le menu s'ouvre et se ferme avec **Échap** ; aucun message à l'écran,
le geste est expliqué dans l'aide ; plein écran changeable dans les jeux, **grisé dans les évaluations**. Pas de graphisme
aujourd'hui : le panneau garde son contenu, avec un bouton « Fermer ». **Rien pour la tablette sans appareil pour essayer :
issue [#1738](https://github.com/GazePlay/GazePlay/issues/1738)** ; d'ici là, un poste sans clavier ne peut plus ouvrir le
menu. Détail : `GazePlayEval.md` section 18. **Essayé et validé par le mainteneur** (Bulles au Tobii seul : pause, menu,
fermeture à la souris, reprise au regard, statistiques et carte de chaleur corrects ; évaluations sans souci).

**Retours 2 et 3 : dossiers des passations.** Tout dans le dossier de la passation, plus rien dans `statistics` pour une
évaluation ; bouton des statistiques vers la passation ; dossiers par jour puis `<date>_<heure>_<évaluation>`. Détail :
`GazePlayEval.md` section 19. Essayé par le mainteneur (passation complète, interrompue, règles `Tout` et `BonneReponse`).

**Retour 4 : médias illisibles.** Message à l'examinateur et lecture des vidéos par FFmpeg (`EvalVideoPlayer`) essayés et validés
par le mainteneur au Tobii le 5/11/2026 (passation de 14:03 : 6 vidéos, aucune `erreur_media`, image centrée et fluide, pause,
son calé, coupure nette ; message vu en mode test) ; `GazePlayEval.md` sections 20 et 21. Décision du
mainteneur : on ne change rien sur le site. La relance automatique d'une vidéo n'est plus nécessaire pour l'instant. Même comportement en vraie passation et en mode test (décisions du mainteneur) :
vidéo, son ou image d'une consigne, avec un temps de passage, message puis écran suivant après 5 s même si ce temps est plus court, bouton « Commencer » désactivé et grisé avec ou sans temps ; sans temps, le message reste et donne
la touche Espace ; sur une grille, message 3 s et elle continue ; pas de message sur la carte de fin. **Cette règle n'est pas encore
commitée, et ses derniers ajouts sont à essayer** (voir la reprise ci-dessus). Sous Windows, JavaFX Media peut garder ouvert un son
illisible après `dispose()` : sans effet sur une passation, le nettoyage de `GazeplayEvalTest` réessaie donc pendant 10 s au plus.

**Retour 5 : carte de chaleur et pointeur** (`GazePlayEval.md` section 22). Avec l'option de cacher les images, le fond de la
carte est pris à l'affichage de la grille. Pointeur de la souris sur l'écran des parties, jeux et évaluations (`GameCursor`) :
toujours visible en mode souris ; caché sauf quand elle bouge en mode `tobii` ; il suit la dernière entrée utilisée en mode
`tobii_and_mouse`, et une souris cachée ne joue plus. Essayés et validés par le mainteneur.

**Retour 6 : écran des statistiques** (`GazePlayEval.md` section 22). Titre et « i » d'explication au-dessus de la carte de chaleur
et du graphique, même hauteur pour les deux. Essayé et validé par le mainteneur (« ça fait le travail »). Le
reste de l'écran attend sa refonte.

**Retour 7 : comprendre les fichiers de résultats.** Sixième carte du menu des évaluations, qui ouvre une fenêtre décrivant chaque
fichier, ses colonnes, ses valeurs et son usage (`GazePlayEval.md` section 22). Validé par le mainteneur.

## BILAN DE LA SÉANCE DU MERCREDI 4/11/2026

**État laissé le 4/11/2026 au soir** : tout est commité et poussé sur `addClaudeCode`. Dernier `build` complet suivi de
`allGamesTest` (avant le dernier commit, qui ne touche que la documentation) : **809 tests, 0 échec ; 66 entrées, 0 échec**.

**Fait le 4/11/2026** (chaque jeu essayé et validé par le mainteneur) :

| Commit | Quoi |
|---|---|
| `0e4ec384b` | Les Dés et les Lettres suivent la durée de fixation des réglages ; clic de l'engrenage à 2 s |
| `4400fb833` | Tests : 4 Go de mémoire ; bibliothèque audio de Windows gardée chargée dans leur JVM |
| `c25317d3e` | Réglage « temps avant sélection » (1 s par défaut, regard bloqué aussi) ; tous les anneaux partent du haut, sens des aiguilles d'une montre ; anneau visible sur les cartes cachées de Memory |
| `11c1ab4ef` | Ninja : fruits à suivre et à couper en deux ; fenêtre des variantes à plusieurs rangées |
| `965487856`, `789bb0f90` | Jeu de la taupe (taupes, Biboules ou Mes images ; zone de chaque trou resserrée après l'essai à la souris) ; limiteurs rendus à Ninja |
| `eaae774c3` | Tir : musique livrée avec le jeu (elle était téléchargée à la première partie) |
| `9f32d5a24` | Biboule Jump : 3 vies, niveaux, même saut sur tout écran, plus de record |

La suite (retours du 5/11, reprise du 6/11) est dans les sections en haut de ce fichier.

**À vérifier à la reprise** : la CI des commits du 4/11 au soir. Celle de `965487856` (run 41857896054) est en échec pour
une seule raison : `package-macos` annulé après 15 min sans exécuter aucune étape (cause inconnue) ; les 6 autres jobs sont
verts. Si le job macOS est de nouveau annulé sur les commits suivants, chercher pourquoi.

**À faire après la revue des jeux** : voir la section suivante et la liste du 4/11 (mémoire des tests, retrait des
limiteurs, refonte de l'engrenage et de l'écran des statistiques, tests instables, inventaire des sons, notes de version 2.0).

## AVANCEMENT DU MERCREDI 4/11/2026 (commité et poussé)

**Travail à faire après la revue des jeux** (en plus de la liste plus bas) : la mémoire des tests (section « Mémoire des
tests de `gazeplay-games` ») ; **retirer les limiteurs de temps et de score** (décision du mainteneur du 4/11/2026 : le
laboratoire ne s'en sert plus, chaque jeu revu a ses propres fins) : les 2 lignes des réglages, `startScoreLimiter`,
`startTimeLimiter`, leurs appels dans les jeux, les clés `LIMITER_*` de `Configuration` (les anciens fichiers de réglages
restent lisibles, comme pour `QUIT_KEY`) ; aucun fichier de résultats ne change. D'ici là, ne pas les rétablir dans Bulles,
Egg Game, GazePlay Eval, Tir et La bataille de l'espace. Le blocage du regard pendant le temps avant sélection a été essayé au Tobii et validé par le
mainteneur.

CI de `28487a21b` : verte. **Ninja** revu (voir sa fiche), essayé et validé par le mainteneur. Quatrième règle (durée
de fixation) : Dés et Lettres corrigés, clic de l'engrenage à 2 s. Tests : mémoire analysée et limite à 4 Go (section
« Mémoire des tests », suite à décider) ; plantage audio de la JVM des tests (point 7) identifié et corrigé, à surveiller.
Memory : anneau visible sur les cartes cachées. Cinquième et sixième règles (temps avant sélection, sens de l'anneau) ;
le temps avant sélection ne s'applique qu'aux jeux qui avaient déjà ce blocage (décision du mainteneur). **Jeu de la
taupe** revu (voir sa fiche), essayé et validé par le mainteneur. **Biboule Jump** revu (voir sa fiche), essayé et
validé par le mainteneur. Musique du Tir livrée avec le jeu. Ensuite : Gaze Race.

## REPRISE DU MERCREDI 4/11/2026 (bilan de la séance du mardi 3/11/2026)

**État laissé le 3/11/2026 au soir** : tout est commité et poussé sur `addClaudeCode`, dernier commit `28487a21b`.
Rien en cours dans la copie de travail, à part ce bilan. Dernier `build` complet suivi de `allGamesTest` (66 entrées) :
réussis, 0 échec (avant la correction du rebond du Casse-brique, vérifiée ensuite par ses tests et son lancement
seul). CI : verte jusqu'à `8609c0f67` (run 41655428200) ; **celle de `28487a21b` (run 41668084609) était en cours :
à vérifier à la reprise**.

**Fait le 3/11/2026** (un commit par point, chacun essayé et validé par le mainteneur) :

| Commit | Quoi |
|---|---|
| `efaee9596` | Tir : Biboule modernisée (version A, bouche et ailes d'origine), dans le Tir seulement ; vignette refaite |
| `e737d5465` | Survivre contre des robots : une seule carte, 3 vies, 3 décors au hasard, modes sans fin / temps / nombre de robots |
| `ac18c2e01` | Passe sur les jeux déjà revus pour les deux nouvelles règles (aide avec valeurs concrètes, pas de choix d'entrée dans les variantes) |
| `e56673de2` | La bataille de l'espace (Space Game) : 3 vies, boss de 15 / 30 / 50 points de vie, fond de nuit, nom français |
| `faf782014` | Défendre le château (Tower Defense) : fins de partie, niveaux, vagues gardées, anneau, boutique et rôle de la tour bleue |
| `334114085` | Bouteilles : le jeu démarre de nouveau (image `restart.png` retirée avec Space Game) |
| `8609c0f67` | `ProgressButton` dessine l'anneau de fixation (plus l'ancien cercle jaune) dans tous les jeux ; l'image ne tremble plus |
| `28487a21b` | Casse-brique revu (3 vies, 3 niveaux, 4 dessins, fond dessiné ou « Mes images » sous un voile clair qui s'efface, balle qui rebondit en haut de l'écran) **et un seul dossier d'images pour tous les jeux** (`Documents\GazePlay_Learning\images`, réglage « Mes images » ; coloriages à part ; dossier `files`, `FILE_DIR` et `CustomFileChooser` retirés ; raccourcis dans `raccourcis`) |

**Leçon du 3/11** : après avoir retiré des ressources, chercher par nom de dossier **et** de fichier, et lancer
`allGamesTest` **complet** (les Bouteilles lisaient une image de Space Game).

**À faire mercredi, dans l'ordre** :

1. Vérifier la CI de `28487a21b`.
2. **Continuer la revue, groupe 6** : **Ninja**, puis Taupes (Whac-A-Mole), Biboule Jump, Gaze Race. Pour chaque
   jeu : état des lieux, **demander d'abord la règle voulue**, propositions validées, puis le travail ; appliquer les
   règles de chaque jeu (aide avec valeurs concrètes, pas de choix d'entrée dans les variantes, anneau de fixation
   partout) ; **variante « Mes images »** pour un jeu qui lit des images de l'utilisateur (Ninja lit déjà le dossier
   commun par `Portrait`, avec repli sur ses images) ; un commit par jeu, sur demande.
3. Jeux pas encore revus qui lisent le dossier commun sans variante (à leur passer une variante lors de leur revue) :
   Cartes magiques, Tarte à la crème, Échelle, Taupes, Ninja, Diviseur, Blocs, Lettres.
4. Toujours à la fin de la revue : refonte de l'engrenage des jeux **et** de l'écran des statistiques ; tests
   instables (points 7, 8 et 9 : plantage d'une JVM de test, GazeplayEvalTest, `EggGameTest`) ; inventaire des sons ;
   notes de version 2.0 (les utilisateurs remettent leurs images dans le nouveau dossier commun).

## REPRISE DU MARDI 3/11/2026 (bilan de la séance du lundi 2/11/2026)

**État laissé le 2/11/2026 au soir** : tout est commité et poussé (demande du mainteneur), y compris le **jeu « Tir »**
(Shoot The Biboules et Shoot The Robots réunis, voir sa fiche), **pas encore essayé par le mainteneur**, et la copie de
sauvegarde de la Biboule d'origine. Dernier `build` (avec tous les nouveaux dessins du Tir) : **694
tests, 0 échec** ; `allGamesTest` complet un peu avant : **67 entrées, 0 échec** ; le Tir relancé 3 fois ensuite
avec ses nouveaux dessins : OK. CI des commits du soir : à vérifier à la reprise.

**Fait le 2/11/2026** (un commit par jeu, chacun essayé et validé par le mainteneur, CI verte) :

| Commit | Quoi |
|---|---|
| `bcb5482a4` | Sounds Of Life : un seul son à la fois (le son en cours est coupé) |
| `daa00a531` | Spot The Differences : option « Avec / Sans anneau de fixation » |
| `559b034be` | Flower Of Numbers (règle du total, 3 modes, images dessinées) |
| `e21decc27` | Bilan, clés de traduction inutilisées retirées, installeur essayé sur un autre PC (il marche, Tobii compris) |
| `d82b754e2` | Opinions (images de l'utilisateur seulement, « J'aime / Je n'aime pas », fichier `-opinions.csv`) ; fenêtre des variantes : message et « Commencer » grisé sans image de l'utilisateur |
| `31c7bb394` | Sprint Finish (une carte, seul au clavier ou au regard, à deux ; cartes au hasard ; chiens en diagonale) |
| `e71b88a5b` | Bubbles (une carte « Bulles », image à découvrir sous 36 vitres, éclatement immédiat ou après fixation) |
| `9534f5792` | Egg Game (une carte, sans fin, rester sur l'œuf suffit) ; recherche du menu sans accents ni « œ » ; nouvelle tâche de fin : refonte de l'écran des statistiques |

**Décision du mainteneur du 2/11/2026 au soir, sur la Biboule** : planche de 3 propositions
(`build/game-screenshots/propositions/biboule-propositions.png`, script `biboule_proposals.py` copié à côté) :
**le mainteneur aime la version A (« fidèle modernisée » : ombres douces, contour fin de la couleur, joues roses), mais
avec la bouche d'origine** (la petite bouche ouverte en « 3 » couché, rose, de la Biboule du laboratoire) **et les ailes
d'origine, plus détaillées** (feuilles avec leur nervure centrale et leurs nervures latérales, contour foncé). Rien n'est
appliqué : à faire demain, sur sa demande. **Copie de sauvegarde de la Biboule d'origine** (demande du mainteneur :
c'est la mascotte, pouvoir revenir en arrière) : `gradle/images/biboule-originale/` (34 dessins de tous les jeux où
elle apparaît, rangés par jeu, avec un `LISEZMOI.txt` ; non livrés, non commités pour l'instant).

**Avancement du mardi 3/11/2026** (non commité) : CI des commits du 2/11 verte (run 41483667774). Point 1 fait : la
version A avec la bouche et les ailes d'origine, validée par le mainteneur, est dans le Tir **seulement** (décision du
mainteneur : les autres jeux gardent la Biboule d'origine pour l'instant ; voir la fiche du Tir). Point 2 en partie :
Tir regardé en vraie fenêtre avec tous les nouveaux dessins (`build/game-screenshots/essai2/shooter/`, Biboules,
souris), vignette `shootTheBiboules.png` refaite (capture du 3/11, nouvelle cage, baguette et Biboules) ;
`ShooterGameTest` et `GameHelpTest` réussis. **Essai du mainteneur le 3/11 : tout est bon** (Tir validé, avec la
nouvelle Biboule). Commité et poussé : `efaee9596`. Point 3 : **Survive Against Robots** revu (règle du mainteneur,
une seule carte, 3 vies, 3 décors au hasard ; voir sa fiche), **essayé et validé par le mainteneur le 3/11**,
commité et poussé (`e737d5465`). Puis, à la demande du mainteneur, **passe sur les 23 jeux revus** pour ses deux
nouvelles règles (section « Deux règles de plus pour chaque jeu ») : aide de Frogs Race précisée, mode « seul au
clavier » de Sprint Finish retiré ; essayé et validé par le mainteneur, commité et poussé (`ac18c2e01`). **Space Game**, maintenant « La bataille de l'espace » en français, revu (règle du mainteneur : 3 vies, boss de 15 / 30 / 50 points de vie, fond de nuit ; voir sa fiche), essayé et validé par le mainteneur, commité et poussé ; puis **Défendre le château** revu (règle du mainteneur, voir sa fiche), essayé et validé par le mainteneur (souris et Tobii), commité et poussé ; puis **le Casse-brique** revu (règle du mainteneur, voir sa fiche ; voile clair sur « Mes images », balle qui rebondit en haut de l'écran), et **un seul dossier d'images pour tous les jeux** (`images`, coloriages à part ; dossier `files` retiré, voir « Règles pour chaque jeu »), essayés et validés par le mainteneur, commités et poussés ; ensuite : Ninja.

**À faire mardi, dans l'ordre** :

1. **[Fait le 3/11, Tir seulement]** **Biboule** : refaire la version A avec la **bouche d'origine** et les **ailes d'origine** (détaillées), la montrer
   au mainteneur ; une fois validée, la
   mettre dans le jeu « Tir » (`data/shooter/biboule/images/Blue.png` à `Red.png`, dessinée par
   `gradle/images/make_shooter_images.py`, aux mêmes proportions 500 × 400). **Demander** s'il faut aussi la mettre
   dans les autres jeux où la Biboule apparaît (Pet, Horses, Frogs Race, Biboule Jump…), pour qu'elle soit la même
   partout.
2. **Jeu « Tir »** (commité le 2/11 au soir) : vérifier la CI, regarder le jeu en vraie fenêtre avec les nouveaux
   dessins (robots, soucoupe, cage, éclairs, baguette, canon), refaire la vignette
   (`data/common/Thumbnails/shootTheBiboules.png`, encore une capture avec l'ancienne cage et l'ancienne baguette) ;
   puis **essai du mainteneur** (souris et Tobii : tir immédiat et après fixation, les 5 fins) ; corrections dans un
   nouveau commit sur sa demande.
3. **Continuer la revue, groupe 6** (jeux de tir et d'action) : **Survive Against Robots (×2)**, puis Space Game,
   Tower Defense, Casse-Brique, Ninja, Whac-A-Mole, Biboule Jump, Gaze Race. Chaque jeu : état des lieux, **demander
   la règle voulue d'abord**, propositions validées, puis le travail ; un commit par jeu sur demande.
4. Toujours à la fin de la revue : point 6 (refonte de l'engrenage **et** de l'écran des statistiques), points 7 et 8
   (plantage de la JVM des tests du 30/10 ; test GazePlay Eval en échec une fois le 2/11, revenu à la normale).

## REPRISE DU LUNDI 2/11/2026 (bilan de la séance du vendredi 30/10/2026)

**État laissé le 30/10/2026 au soir** : tout est commité et poussé sur `addClaudeCode`, dernier commit `2e1957327`
(Pet). Rien en cours dans la copie de travail, à part ce bilan (`REVUE_DES_JEUX.md`, `CLAUDE.md`). `build` :
**634 tests, 0 échec** ; CI verte pour tous les commits du jour, `2e1957327` compris (run 41082263427, 7 jobs).

**Fait le 30/10/2026** (un commit par point, chacun validé par le mainteneur, CI verte) :

| Commit | Quoi |
|---|---|
| `82774206c` | Eval : liste prioritaire 1 à 5 (plus de touche pour quitter ni de `QUIT_KEY`, réglages allégés, carte d'aide, bouton « Commencer » du formulaire, souris permise sur le formulaire en « Tobii seul », carte du site GazePlay Eval) et revue du lecteur |
| `67f6b871b` | Frogs Race (grenouilles dessinées, couloirs, Facile / Normal / Difficile, classement) |
| `f72d4c334` | Horses (une carte avec Horses Simplified, adversaires ordinateur, dé équitable) et **aide des jeux** (« i » de la fenêtre des variantes ; fenêtre « Lancer le jeu » pour un jeu sans variante) |
| `1029042bb` | Memory (6 entrées en une carte, `GroupedGameVariant`) et **fenêtre des variantes des jeux revus** (boutons qui choisissent, « Commencer », boutons radio en bas, écart de 28 px) |
| `5d44c5b15` | Sounds Of Life (décors dessinés, animaux OpenMoji, zèbre et girafe, 21 sons crédités, nom de l'animal, anneau) |
| `71d75fed4` | Spot The Differences (8 scènes dessinées par script, Facile / Normal / Difficile = 3, 5, 7 différences, indice après 20 s, images enchaînées) |
| `2e1957327` | Pet (4 pièces dessinées, jauges Faim / Propreté / Jeu de 2 minutes, Biboule triste et bulle, activités claires, plantage corrigé) |

**Installeur Windows construit le 30/10/2026** pour un essai sur un autre PC (Jeux et Évaluations) :
`build/distributions/gazeplay-learning-windows-x64-1.11.10-installer.exe` (296 Mo, Java 25 et JavaFX compris,
SHA-256 `81c3bcaa…f0d2a6`, construit depuis `2e1957327` ; `gradlew generateWindowsInstaller`). Non signé
(avertissement SmartScreen attendu). **Première installation réelle par l'installeur** (jamais testée avant).

**Avancement du lundi 2/11/2026** (non commité) :
- Point 1 : **installeur essayé par le mainteneur sur un autre PC, avec l'oculomètre : il fonctionne** (noté dans
  `CLAUDE.md`).
- Point 2 : Pet au regard validé tel quel ; Spot The Differences validé, avec une **option demandée : anneau de
  fixation visible ou non** (fait, voir sa fiche) ; Sounds Of Life : deux sons pouvaient se superposer, le son en
  cours est maintenant coupé (fait, voir sa fiche).
- Point 4 : Flower Of Numbers revu (propositions validées, tout fait, voir sa fiche) ; reste l'essai du mainteneur.
  Ensuite : Opinions (revu le 2/11, essayé et validé par le mainteneur, commité `d82b754e2`) ; Sprint Finish :
  revu, validé et commité le 2/11 (`31c7bb394`) ; Bubbles : revu, essayé et validé le 2/11, commité `e71b88a5b` ;
  Egg Game : revu, essayé et validé le 2/11, commité `9534f5792` ; groupe 6 (jeux de tir et d'action) : Shoot The
  Biboules et Shoot The Robots réunis en « Tir », revus le 2/11 (règle du mainteneur, voir leur fiche), en attente
  de l'essai du mainteneur ; puis Survive Against Robots.
  Un test de GazePlay Eval en échec une seule fois : point 8 ci-dessous, à surveiller.
- Point 3 : clés `Spot all the differences` et `Differences found` retirées (décision du mainteneur), des deux
  `multilinguism.csv` (application et tests) ; aucun code ne les lisait.

**À faire lundi, dans l'ordre** :

1. **Retours de l'essai sur l'autre PC** (si le mainteneur l'a fait) : installation, premier lancement (dossier
   `Documents\GazePlay_Learning`), jeux revus (fenêtre des variantes, « Commencer », aide « i », Pet avec « Lancer le
   jeu »), son, Tobii (calibrage dans Tobii Experience), Évaluations (import de `Tests sons et
   vidéos-gazeplayEval.zip`, formulaire, passation, résultats dans `Documents\GazePlayLearning_Eval\Eval<n>_<date>`,
   « Tester une évaluation », aide, carte du site). En cas de problème : `Documents\GazePlay_Learning\gazeplay.log` et
   `%LOCALAPPDATA%\CrashDumps`. Corriger ce qui est trouvé avant de continuer la revue ; noter le résultat de
   l'installation réelle dans `CLAUDE.md` (« L'installation réelle n'a pas été testée »).
2. **Retours du mainteneur sur les jeux du jour**, à corriger s'il y en a : Sounds Of Life (les 15 sons de Commons
   ont été choisis sur leur titre, **sans écoute** : à écouter, surtout poisson, pieuvre, crabe, dauphin, girafe) ;
   Spot The Differences (difficulté du niveau Difficile, réglable dans `gradle/images/make_spotdifferences_images.py`) ;
   Pet au regard (marge autour de la bouche pour la cuillère, 3 s de douche pour laver).
3. **Question en attente** : retirer de `multilinguism.csv` les clés `Spot all the differences` et
   `Differences found`, qui ne servent plus depuis la revue de Spot The Differences ?
4. **Continuer la revue** (groupe 5, « Jeux à images à risque ») : **Flower Of Numbers** (21 images à risque, dont
   billets et pièces en euros), puis Opinions, Sprint Finish (×2), Bubbles (×2), Egg Game (×2) ; ensuite le groupe 6
   (jeux de tir et d'action). Chaque jeu : les 8 étapes de la marche à suivre ci-dessous (état des lieux et
   propositions validées avant tout changement ; aide du jeu ; fenêtre des variantes des jeux revus ; un commit par
   jeu sur demande).
5. Toujours à la fin de la revue : points 6 (refonte de l'engrenage et de l'écran des statistiques), 7 (plantage de la JVM des tests du
   30/10 vers 11 h 19, pas revenu depuis) et 8 (test GazePlay Eval en échec une fois le 2/11) de la liste
   ci-dessous.

## Liste prioritaire du 29/10/2026 (points 1 à 5 faits et commités le 30/10/2026 ; points 6 et 7 pour la fin)

**À faire avant de commencer la famille suivante (Frogs Race).** Rien n'est codé : chaque point passe par les étapes
habituelles (vérifier, faire, tester, montrer, commit sur demande).

**État laissé le 29/10/2026** : dernier commit `475853c04` (Coloring). **Non commité** : la revue du lecteur GazePlay
Eval (fiche plus bas : graphisme, anneau de fixation, cartes), `MenuMessageDialog` (fenêtres de l'import et de la
langue), la touche Retour arrière du formulaire, le message de plein écran retiré. Le mainteneur ne les a pas encore
essayés en vrai (sauf ce qu'il a signalé). **Ne rien commiter pour l'instant** (décision du mainteneur du
29/10/2026 : d'autres changements d'Eval vont venir) ; commit seulement quand il le demandera.

1. **[Codé le 30/10/2026 ; essai du mainteneur le 30/10 : évaluation quittée par l'engrenage, `session.json` vérifié]** Retirés : les deux lignes des réglages et
   `buildQuitKeyChooser`, `GameContext.createQuitShortcut` et `isTyping` (et leurs appels dans `GameMenuController`
   et `ReplayingGameFromJson`), l'exception de `blocksGameInput` (tout le clavier est arrêté menu ouvert), la
   traduction, la clé `QUIT_KEY` de `Configuration`. Fait vérifié : `ApplicationConfig` garde les clés inconnues ; un
   ancien fichier avec `QUIT_KEY` se lit et s'enregistre, la clé y reste sans servir
   (`ConfigurationTest.shouldReadAndSaveAFileWithTheRemovedQuitKey`). Aucun jeu ne lisait la clé.
   **Retirer le réglage « Bouton pour quitter » (QuitKey) et son fonctionnement.** Raison : dans les jeux et les
   évaluations, l'engrenage bleu en bas à gauche mène déjà à la page des statistiques puis au menu principal associé.
   - Où : la ligne `QuitKey` des réglages, deux fois (`ConfigurationContext` : section Jeux vers la ligne 192, section
     Évaluations vers la ligne 293) et `buildQuitKeyChooser` ; le raccourci `GameContext.createQuitShortcut` (et
     `isTyping`, ajouté le 29/10 pour le formulaire d'Eval) ; l'exception de la touche « quitter » dans
     `GameContextFactory.blocksGameInput` (le clavier sera entièrement bloqué pendant que le menu est ouvert) ; la
     traduction `QuitKey` ; les tests qui en parlent ; la mention dans `GazePlayEval.md` (« la touche quitter
     quitte aussi une évaluation »).
   - **Retirer aussi la clé `QUIT_KEY` de `Configuration`** (décision du mainteneur du 29/10/2026 : elle ne sert plus) :
     propriété, accesseurs, valeur par défaut. Vérifier avant qu'aucun jeu ne la lit pour autre chose, et qu'un fichier
     `GazePlay.properties` existant qui contient encore `QUIT_KEY` se lit sans erreur (clé inconnue ignorée ; regarder
     `ConfigurationSource`, `LegacyConfigurationKeys` et l'écriture atomique : la clé peut rester dans les fichiers déjà
     écrits ou disparaître au prochain enregistrement, les deux conviennent). Ajouter un test de lecture d'un ancien
     fichier avec `QUIT_KEY`.
   - Vérifier en vrai qu'on sort bien de chaque partie et d'une évaluation par l'engrenage (et que les résultats d'une
     évaluation quittée sont écrits, `terminee: false`).
2. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** Retirés : le bouton d'import de la liste
   (`EvalSettings.refreshLibrary`), la ligne `EvalResults` et `EvalSettings.buildResultsFolderButton`. Gardés :
   `importEvaluation`, `openResultsFolder` et les traductions `EvalImportButton`, `EvalResults` (menu des
   évaluations). Test : `ConfigurationContextTest` (liste sans bouton, plus de ligne `EvalResults`).
   **Réglages des évaluations** (section « Évaluations (GazePlay Eval) ») :
   - dans « Évaluations importées », **retirer le bouton « Importer une évaluation »** (déjà sur le menu principal des
     évaluations) ; garder la liste et la suppression (`EvalSettings.buildLibrary`, bouton `importButton`) ;
   - **retirer la ligne « Résultats des évaluations »** (`EvalResults`, `EvalSettings.buildResultsFolderButton`), déjà
     sur le menu principal des évaluations.
   - Garder `EvalSettings.importEvaluation` (utilisé par `EvalMenuScreen`).
3. **[Codé le 30/10/2026, essayé par le mainteneur]** D'abord un bouton rond « i » dans la barre du haut, puis
   (retour du mainteneur du 30/10) **une carte comme celle du site** : 3 cartes (Tester, Importer, Résultats), puis 2
   centrées dessous (Créer une évaluation, Aide), toutes de 92 px de haut ; page resserrée pour tenir en 1366 × 768
   (écarts 20, pastille de « Lancer » 96 px, cartes 14 px de marge verticale). Partie « Touches de l'examinateur »
   réécrite par moment (pendant un écran ; sur l'écran Recommencer / Continuer ; quitter), choix du mainteneur.
   Fenêtre `EvalHelpDialog` (`MenuDialog`, défilante). Texte dans
   `gazeplay-core/src/main/resources/data/help/eval-help-fra.txt` et `-eng.txt` (pas dans `multilinguism.csv`, qui
   n'admet ni virgule ni retour à la ligne) : `# ` titre, `- ` point de liste, sinon paragraphe ; anglais pour les
   autres langues. Styles `gp-help-*` (`menus.css`). Tests : `EvalMenuScreensTest` (ouverture et fermeture, mêmes
   sections en français et en anglais, construction du texte). Capture `3g-evaluations-aide` de `menuScreenshots`.
   Le texte parle déjà du bouton « Commencer » du point 4.
   **Menu principal des évaluations (`EvalMenuScreen`) : ajouter un bouton d'aide** qui explique le fonctionnement
   des évaluations, au style des menus (une carte secondaire comme « Importer », qui ouvre une fenêtre `MenuDialog`) :
   les touches de l'examinateur pendant une évaluation (`Espace` écran suivant, `P` rejouer le son ou la vidéo, `R` ou
   `←` recommencer, `C` ou `→` continuer, `Entrée` confirmer la proposition du participant ; l'engrenage pour quitter),
   les modes de passation (classique, soutien oral, soutien visuel, autonomie totale), le délai avant sélection, ce
   qui est enregistré et où, la différence avec « Tester une évaluation ». Source : `GazePlayEval.md` 12.3. Faire
   relire le texte au mainteneur avant de le figer ; traductions français et anglais.
4. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** Bouton « Commencer » (`EvalStartButton`, pilule de la
   couleur principale) sous les champs ; retirés : `EvalFormHint` (code et traduction), le `setOnAction` des champs,
   le cas `FORM` de `onKey`. Fait : en mode « regard seul » (`tobii`), la zone de jeu bloque la souris
   (`applyInputMode`), donc le bouton ne se clique pas ; il s'atteint avec Tab puis Espace (clavier standard de JavaFX,
   pas un raccourci du jeu). Décision du mainteneur (30/10/2026) : le formulaire n'a pas à être utilisable au
   regard, c'est l'examinateur qui le remplit et lance la passation ; **en mode « Tobii seul », la souris et le clic
   passent sur le formulaire, et sur cet écran seulement**. Fait par une marque `IGameContext.MOUSE_ALLOWED_PROPERTY`
   (propriété du nœud) que le filtre de `applyInputMode` respecte, avec ses enfants ; le formulaire est retiré au
   démarrage, donc les écrans de l'évaluation restent au regard seul. Tests :
   `GameContextFactoryInputModeTest.shouldLetTheMouseThroughAMarkedScreenWithTheEyeTrackerAlone`, et dans `GazeplayEvalTest.shouldStartTheFormWithItsButtonOnly` (Entrée ne démarre plus,
   plus de message), passation interrompue démarrée par le bouton.
   **Formulaire participant (avant l'évaluation) : un bouton « Commencer »** au lieu de devoir appuyer sur une touche,
   et **retirer le message** d'aide (`EvalFormHint`, « Remplissez les champs puis appuyez sur Entrée pour
   commencer » : c'est `Entrée`, pas `Espace`, mais le mainteneur ne veut plus de touche à expliquer). Bouton au style
   des menus sous les champs, dans `GazeplayEval.showForm`. **Aucun raccourci clavier** (décision du mainteneur du
   29/10/2026) : retirer `Entrée` (le `setOnAction` des champs et le cas `FORM` de `onKey`) ; adapter les tests
   (`GazeplayEvalTest` envoie `Entrée` pour valider le formulaire : appuyer sur le bouton à la place).
5. **[Codé le 30/10/2026, essai réel du mainteneur à faire]** 4ᵉ carte « Créer une évaluation (site GazePlay Eval) »
   (`EvalWebsite`, icône globe `language` de Material Symbols Rounded, Apache-2.0, téléchargée du dépôt de Google ;
   source vérifiée : `info` téléchargé est identique au fichier livré). Adresse fixe `EvalMenuScreen.EVAL_WEBSITE`.
   `SystemFileManager.openWebPage` : `rundll32 url.dll,FileProtocolHandler` sous Windows (adresse transmise entière,
   `#` compris), `open` sous macOS, `xdg-open` sous Linux, lancé sans attendre. Si la commande ne peut pas être
   lancée : `MenuMessageDialog` avec l'adresse à recopier (`EvalWebsiteFailed`). Les 4 cartes sur une ligne en
   1366 × 768 (texte 190 px au lieu de 220, écart 16, `TilePane` : même taille pour toutes). Texte d'aide complété.
   Tests : `EvalMenuScreensTest` (adresse ouverte par un objet simulé, message sans navigateur),
   `SystemFileManagerTest`. Limite : on ne sait pas si le navigateur a vraiment affiché la page (seul l'échec du
   lancement est détecté).
   **Menu principal des évaluations (`EvalMenuScreen`) : un bouton qui ouvre le site GazePlay Eval** (demande du
   mainteneur du 30/10/2026), pour créer une évaluation. Adresse : `https://lig-gazeplay.imag.fr/#/home` (celle du
   README du dépôt du site et de `GazePlayEval.md`). Ouvre le navigateur par défaut du système. Ce sera une carte
   secondaire au style des menus, à côté de « Tester », « Importer » et « Résultats » (et de l'aide du point 3).
   - À vérifier avant : comment `SystemFileManager.openFolder` ouvre un dossier, pour ouvrir une adresse de la même
     façon et sous chaque système (Windows, Linux, macOS), sans bloquer le thread JavaFX. Il n'y a encore aucune
     ouverture de page web dans le code.
   - Un message si le navigateur ne s'ouvre pas (pas de navigateur, machine sans réseau), avec l'adresse à recopier
     (`MenuMessageDialog`).
   - Icône libre (Material, Apache-2.0, comme les autres), traductions français et anglais, test du bouton (adresse
     ouverte par un objet simulé, jamais un vrai navigateur dans les tests), capture du menu.
   - **Décisions du mainteneur (30/10/2026)** : libellé « Créer une évaluation (site GazePlay Eval) » ; adresse
     **fixe dans le code** (pas de réglage).
6. **Tâche de fin, après la revue de tous les jeux et leur acceptation** : refonte graphique du menu des jeux (son
   panneau : musique, volume, durée de fixation, vitesse, taille, barre de progression, couleur de fond ;
   `GameMenuPanel`, `GameContextFactory`, `GameContext.createControlPanel`). La partie fonctionnelle (Échap à la place
   de l'engrenage) est faite depuis le 5/11/2026 ; à la refonte, libérer les coins gardés pour lui. À
   préparer comme les autres étapes (état des lieux, propositions validées). Penser à la décision encore ouverte sur
   le temps du menu compté dans les durées (`MODERNIZATION.md` 2an).
   **Au même niveau (demande du mainteneur du 2/11/2026)** : **refonte graphique de l'écran des statistiques** de fin
   de partie (titre, chiffres, cartes de chaleur, courbes, boutons du bas : rejouer, dossier, menu, suivant ;
   `StatsContext`), dans le style des menus et des jeux revus. Même marche à suivre : état des lieux, propositions
   validées, sans changer les fichiers de résultats.
7. **À surveiller, puis à chercher à la toute fin s'il revient** (décision du mainteneur du 30/10/2026) : le
   30/10/2026 vers 11 h 19, pendant un `build` complet, la JVM des tests de `gazeplay-core` s'est arrêtée dès le
   début du module (« Gradle Test Run :gazeplay-core:test FAILED », `MessageIOException: Could not write
   '/127.0.0.1:…'`, « Connection reset by peer » ; aucun test en échec, pas de `hs_err`). Relancé aussitôt :
   `:gazeplay-core:test` seul puis `build` complet réussis (575 tests). Cause inconnue. Si ça revient : noter
   l'heure, le dernier test écrit dans `gazeplay-core/build/test-results/test`, chercher un `hs_err_pid*.log` et
   `%LOCALAPPDATA%\CrashDumps`. Si rien ne revient d'ici la fin de la revue des jeux, chercher quand même à la fin.
   **Revenu le 3/11/2026 à 9 h 43**, pendant un `build` complet (seuls des images et des documents avaient changé) :
   « Process 'Gradle Test Executor 2' finished with non-zero exit value -1073741819 (NTSTATUS 0xC0000005) », 48 s
   après le début ; dernier résultat écrit : `GazePlayModeTest` (4 tests réussis) ; pas de `hs_err`. **Vidages
   Windows trouvés** : `java.exe.10072.dmp` et `java.exe(1).10072.dmp` (3/11, 9 h 43) et `java.exe.3524.dmp` (30/10,
   11 h 19 : le premier plantage avait donc aussi laissé un vidage). **Hypothèse** : le même plantage audio que les
   jeux du 28/10 (`MMDevAPI.dll` déchargée, arrêt dans `rpcrt4.dll` sans `hs_err`), que
   `GazePlayLauncher.keepTheWindowsAudioLibraryLoaded` évite dans les jeux mais pas dans la JVM des tests. À vérifier
   à la fin, en ouvrant les vidages (module fautif).
   **Revenu le 4/11/2026 à 10 h 44** (`build` complet, même code de sortie, derniers résultats écrits : `AppModeTest`,
   `GazePlayModeTest`, `MenuOrientationTest` ; vidages `java.exe.8768.dmp` et `java.exe(1).8768.dmp`). **Fait, vu dans le
   journal Application de Windows (événement 1000)** : module fautif **`MMDevApi.dll`**, code 0xC0000005, processus 8768,
   `java.exe`. Le module fautif de l'hypothèse est donc confirmé pour ce plantage ; il n'a rien à voir avec la mémoire.
   **Correctif du 4/11/2026 (demande du mainteneur)** : `WindowsAudioLibrary.keepLoaded` (`gazeplay-commons`, code
   déplacé de `GazePlayLauncher`) est aussi appelé dans les JVM de test de `gazeplay-commons`, `gazeplay-core` et
   `gazeplay-games`, avant tout test, par `KeepWindowsAudioLoadedListener` (test fixtures) ; `WindowsAudioLibraryTest`
   vérifie que l'écouteur s'est exécuté. Le plantage étant rare (3 fois en 6 jours), son absence ne se juge que dans la
   durée : **à surveiller**.
8. **À surveiller, puis à chercher à la toute fin** (décision du mainteneur du 2/11/2026, comme le point 7) : le
   2/11/2026 vers 14 h 30, pendant un `build` complet, un test de GazePlay Eval a échoué une fois :
   `GazeplayEvalTest.shouldPlayAnEvaluationToItsEndAndWriteItsResults` (`gazeplay-games`), « Wanted but not
   invoked: iGameContext.showRoundStats(...) » à la ligne 164 du test. L'évaluation était bien finie (phase `END`,
   résultats écrits) ; seul l'appel à l'écran des statistiques manquait au moment de la vérification. Aucun
   changement dans `gazeplay-games` depuis le build précédent, qui passait. Relancé aussitôt : le test seul, puis le
   `build` complet réussis (665 tests). **Hypothèse** : test dépendant d'un délai (l'écran des statistiques vient
   après une pause que le test n'attend pas toujours assez), machine chargée. Si ça revient : noter l'heure et le
   message, regarder le délai attendu par le test et celui du jeu avant `showRoundStats`.
9. **À surveiller, puis à chercher à la toute fin** (comme les points 7 et 8) : le 3/11/2026 vers 14 h 40, pendant un
   `build` complet (machine chargée par des captures en vraie fenêtre juste avant), un test du Jeu de l'œuf a échoué
   une fois : `EggGameTest.shouldCrackAgainWhileStayingOnTheEgg`, « hatched without looking away ==> expected
   HATCHED but was READY » (ligne 140), en 6,8 s. Le changement en cours (`ProgressButton`) ne touche pas ce jeu.
   Relancé : le test seul 3 fois, puis le `build` complet réussis (733 tests). **Hypothèse** : test qui attend en
   temps réel, trop juste quand la machine est chargée. Si ça revient : regarder les délais attendus par le test.

## Règles pour chaque jeu

- Ressources **libres seulement** (GPL-3.0-or-later, ou compatible : CC0, CC BY, CC BY-SA, Apache-2.0, OFL), avec la
  source et la licence notées dans `gradle/images/README.txt` et dans la licence affichée (`gradle/templates/licence.txt`).
  Remplacer en priorité les images **non libres**, puis celles **à risque**.
- Une image remplacée garde son nom de fichier quand c'est possible (le code ne change pas). Une image retirée : vérifier
  qu'aucun code ne la lit.
- Les vignettes du menu (`gazeplay-core/.../data/common/Thumbnails`) sont des captures des jeux : à refaire si le jeu
  change d'aspect.
- Vérifier : le jeu en vrai (souris, et Tobii si possible), `allGamesTest -PallGamesOnly=<nameCode>`, et `build`.
- Ne jamais changer le format des résultats sans versionnage (`formatVersion`).
- **Images données par l'utilisateur (décision du mainteneur, 29/10/2026)** : elles sont **toujours une variante** du
  jeu, jamais un remplacement silencieux des images du jeu. Pour un jeu qui en lit (voir la liste ci-dessous) :
  1. ajouter une valeur de variante qui implémente `UserImagesVariant` (`gazeplay-games-commons`), avec pour dossier
     `UserImagesVariant.IMAGES` : le jeu lit alors **sans autre code** le dossier d'images **commun à tous les jeux et
     à tous les profils**, `Documents\GazePlay_Learning\images` (décision du mainteneur du 3/11/2026 : une image donnée
     une seule fois sert à tous les jeux ; il remplace les dossiers `files/images/<NameCode>` d'un jour), réglé dans
     les réglages (Dossiers, « Mes images »), avec la fenêtre d'erreur du menu quand la variante est choisie sans image
     (`UserImagesDialog`). Seul le Coloriage a son propre dossier, `UserImagesVariant.COLORING`
     (`Documents\GazePlay_Learning\coloriages`, réglage « Dessins à colorier ») : des contours à colorier, pas des
     photos ;
  2. dans le jeu, lire les images avec `UserImagesVariant.images(dossier)`, et afficher un message si la liste est vide
     (jeu lancé sans passer par le menu) ;
  3. ne plus lire l'ancien dossier partagé ; ne pas reprendre les images qu'il contient : **pas de migration** (passage
     à GazePlay Learning 2.0, les utilisateurs remettent leurs images ; à dire dans les notes de version) ;
  4. ajouter les tests (variante, dossier vide) et vérifier la fenêtre d'erreur en vrai.

  **Le dossier `files` et ses sous-dossiers sont retirés (3/11/2026, demande du mainteneur)** : plus de réglage
  « Répertoire des fichiers » (`FILE_DIR`, gardé tel quel dans les fichiers de réglages existants, plus lu), plus de
  fenêtre `CustomFileChooser`, plus de sous-dossiers `magiccards`, `portraits`, `blocs`, `opinions` ni par jeu. Les
  jeux pas encore revus qui lisaient ces anciens dossiers en silence (Magic Cards ; Cream Pie, Ladder, Whac-A-Mole,
  Ninja, Divisor par `Portrait`/`ProgressPortrait` ; Blocks, Letters) lisent désormais le dossier commun, toujours avec
  repli sur leurs images par défaut quand il est vide ; ils passeront à une variante « Mes images » lors de leur
  revue. Les raccourcis des jeux vont par défaut dans `Documents\GazePlay_Learning\raccourcis`. Le dossier `files` d'un
  utilisateur n'est ni supprimé ni repris (pas de migration, 2.0).

  Jeux avec la variante : Coloring (« Palette personnalisée », dossier des coloriages), Opinions, Memory, Bubbles,
  Egg Game, Casse-brique, Ninja, Jeu de la taupe (dossier commun).

## Aide des jeux (demande du mainteneur du 30/10/2026, validée le 30/10/2026)

Pour les jeux déjà revus (hors GazePlay Eval) : un « i » rond en bas à droite de la fenêtre des variantes ouvre
l'aide du jeu (le principe, comment jouer, comment gagner, les variantes). Un jeu **sans variante** qui a une aide
ouvre, au lieu de se lancer, une fenêtre avec son aide et un bouton « Lancer le jeu » (aucun des jeux revus n'est dans
ce cas pour l'instant). Textes : `gazeplay-core/src/main/resources/data/help/games/<NameCode>-fra.txt` et `-eng.txt`
(nom de code sans espaces ; `# ` titre, `- ` point, sinon paragraphe ; anglais pour les autres langues), fenêtre
`HelpDialog` (commune avec l'aide des évaluations), `GameHelp`. Tests : `GameHelpTest` (chaque jeu revu : aide en
français et en anglais, mêmes parties ; fenêtre « Lancer le jeu »), `GameVariantDialogTest`. Captures
`3h-aide-petits-chevaux`, `3i-aide-sans-variante`. `build` : **602 tests, 0 échec**. **Validé** : l'aide fait partie de l'étape 7 de chaque
jeu revu ensuite.

## Fenêtre des variantes des jeux revus (demande du mainteneur du 30/10/2026)

Pour tout jeu revu (il a une aide, `GameHelp.exists`) : les boutons des variantes **choisissent** la variante (elle est
entourée de la couleur principale ; la première est choisie à l'ouverture) sans lancer le jeu ; un bouton
**« Commencer »** tout en bas, au centre, lance le jeu avec les choix faits (Entrée aussi) ; le « i » de l'aide est à
sa droite. **Tous les boutons radio sont en bas**, sous les variantes, au-dessus de « Commencer » (les groupes juste
au-dessus des sous-groupes pour les variantes à deux niveaux, `GroupedGameVariant`). Entre deux rangées de boutons radio, un
espace net (28 px), la fenêtre agrandie d'autant. En changeant de groupe, la
variante à la même place reste choisie. Les jeux pas encore revus gardent l'ancien fonctionnement (un clic lance, les
boutons radio en haut). Tests : `GameVariantDialogTest`. Captures `3a-variantes-memory`, `3a-variantes-ou-est-l-animal`,
`3a-variantes-petits-chevaux`. Les aides disent « En bas » pour ces boutons et expliquent « Commencer ». `build` : **610 tests, 0 échec**.

## Deux règles de plus pour chaque jeu (demande du mainteneur du 3/11/2026, à la revue de Survive Against Robots)

1. **L'aide dit ce que fait chaque niveau** (Facile, Normal, Difficile ou autre) : des valeurs concrètes (combien
   d'images, de robots, de secondes…), pas seulement « plus difficile ».
2. **Pas de choix de l'entrée dans les variantes** (ni « regard », ni « souris », ni « clavier ») : le jeu suit le
   réglage des entrées (`EYE_TRACKER` : souris seule, Tobii seul, les deux). Un utilisateur en souris seule qui
   choisissait « regard » sans y faire attention restait coincé. Un jeu qui écoute le regard et la souris sur ses
   propres nœuds n'a rien à faire (la zone de jeu arrête la souris en « Tobii seul », aucun regard n'arrive en
   « souris seule », avec les deux le dernier qui bouge mène) ; un jeu qui écoute toute la scène vérifie le mode
   lui-même (`EyeTracker.acceptsMouse`). Un mode à deux joueurs (l'un au clavier, l'autre au regard) n'est pas un
   choix de l'entrée : il reste.

**Passe sur les jeux déjà revus (3/11/2026)**, les 23 qui ont une aide :
- Règle 1 : seul **Frogs Race** disait seulement « lentes, plus vite, vite » ; l'aide donne maintenant le temps des
  autres grenouilles (`RaceVariant` : 1 min 30 à 1 min 50, 1 min à 1 min 15, 40 à 50 s). Les autres disaient déjà
  leurs valeurs (Spot The Differences : 3, 5, 7 différences ; Sprint Finish : 5, 10, 15 cartes ; Où est la couleur et
  Où est la forme : 6, 11, 16 couleurs et 6, 13, 20 formes ; grilles de 2 à 9 images ; Memory et sa taille
  dynamique ; Flower Of Numbers, Egg Game, Bubbles, Horses, Coloring : leurs choix décrits) ; Shooter, Pet, Opinions,
  Sounds Of Life et Survive Against Robots (fait à sa revue) n'ont pas d'autre niveau.
- Règle 2 : seul **Sprint Finish** faisait choisir (« seul, le chat suit le regard » ou « seul, le chat au
  clavier ») : **le mode seul au clavier est retiré** ; « Seul » suit le regard ou la souris selon le réglage,
  « À deux (clavier et regard) » reste ; 6 variantes au lieu de 9 ; une ancienne variante « seul au clavier » se
  joue seule. Aide et traductions mises à jour. Les autres jeux revus n'utilisent ni le clavier ni la scène entière :
  « dès qu'on regarde / après une fixation » (Tir, Bulles) est une façon de toucher, pas un choix de l'entrée.

## Troisième règle : l'anneau de fixation (demande du mainteneur du 3/11/2026, après Défendre le château)

Toute fixation d'un jeu revu montre **l'anneau** des jeux revus (couleur principale sur un disque clair), jamais
l'ancien indicateur de progression (`ProgressIndicator` : le disque qui se remplit, de la couleur des réglages). Deux
composants communs le dessinent, dans `gazeplay-games-commons` : `FixationRing` (un anneau posé où l'on veut ; Coloriage,
Défendre le château) et `ProgressButton` (bouton rond choisi par une fixation).

**Passe sur les jeux déjà revus (3/11/2026)** : recherche de `ProgressIndicator`, `ProgressButton`, `ProgressPortrait`,
`ProgressPane`, `GazeIndicator`, `IGazeProgressIndicator` et `getProgressBarColor` dans les paquets des jeux revus et
dans les composants communs qu'ils utilisent (`GazeRoundButton`, `DiceRoller`, `GameStyle`… : aucun indicateur). Seul
**Les petits chevaux** gardait l'ancien indicateur, par `ProgressButton` (lancer du dé, choix des équipes, pions).
**Fait** (choix du mainteneur : le composant commun plutôt que ce seul jeu) : **`ProgressButton` dessine l'anneau**
(même délai de 300 ms, même durée de fixation, même taille d'indicateur des réglages ; pour un bouton dimensionné par sa
largeur et sa hauteur sans disque, comme les écuries des Petits chevaux, l'anneau prend le bouton entier : à la
première capture il n'était qu'un point) ; `assignIndicatorUpdatable` ne rend plus l'indicateur (aucun jeu ne s'en
servait). **Les 9 jeux pas encore revus qui l'utilisent passent aussi à l'anneau** : Biboule Jump, Bouteilles, Gâteaux,
Dés, Jeu de l'oie, Échelles, Potions magiques, Pierre-feuille-ciseaux, Morpion. Restent avec l'ancien indicateur,
jusqu'à leur revue, les 18 jeux qui créent le leur ou utilisent les autres composants (paquets `colorblend`,
`connect4`, `creampie`, `cups`, `dice` (en plus de ses `ProgressButton`), `dottodot`, `labyrinth`, `literacy`, `magiccards`,
`math101`, `moles`, `ninja`, `order`, `pianosight`, `rushhour`, `simon`, `slidingpuzzle`, `trainSwitches`), par
leur propre `ProgressIndicator` ou par `ProgressPortrait`, `ProgressPane`, `GazeIndicator`. Tests `ProgressButtonTest` (3) ; `allGamesTest` complet :
66 entrées, 0 échec ; capture en vraie fenêtre (souris sur une écurie) `build/game-screenshots/essai2/progressbutton/`.
**Retour du mainteneur (3/11/2026)** : l'anneau est bon, mais **l'image du dé ou du pion tremblait** pendant qu'il se
remplissait. Cause : l'anneau prenait part à la mise en page du bouton (un `StackPane`), et les limites d'un arc
changent un peu pendant qu'il se remplit (elles suivent ses courbes) ; le bouton recentrait son contenu à chaque
changement (l'anneau de `FixationRing`, posé à part, n'a pas ce problème). **Fait** : l'anneau est hors de la mise en
page, centré sur le bouton ; test `shouldKeepTheImageInItsPlaceWhileTheRingFills`.

## Mémoire des tests de `gazeplay-games` (analyse du 4/11/2026)

**Faits mesurés** (sonde temporaire : mémoire occupée après un nettoyage complet, à la fin de chaque classe de tests) :
la suite finit à **2 920 Mo** ; en libérant les objets simulés de Mockito après chaque classe
(`Mockito.framework().clearInlineMocks()`), elle finit à **502 Mo**. Dans l'histogramme de fin, les tableaux d'octets
pèsent environ 2 250 Mo. Les plus grosses marches : Flower Of Numbers 510 Mo, Survive 589 Mo, Where
Is It 437 Mo, Spot The Differences 227 Mo, Tower Defense 222 Mo. **Déduction** : les objets simulés retiennent les jeux
reçus en arguments, donc leurs images ; aucune chaîne de références n'a été suivie dans un vidage.

**Limite** : `build.gradle` donne 4 Go aux tests depuis le 4/11/2026.

**Si l'on libère les objets simulés, 5 tests échouent** (`DisabledMockException`) : une minuterie d'un jeu de la classe
précédente se déclenche après la fin de ses tests (Casse-brique `lose`, Egg Game `hatch`, Space Game `lose`, Sprint Finish
`startCountdown`) et l'erreur tombe sur le test suivant. Ces tests ne ferment pas leur partie. **Travail à faire après
la revue des jeux (décision du mainteneur du 4/11/2026)** : fermer ces parties à la fin de leurs tests, ajouter la
libération après chaque classe, revenir à 3 Go.

## Quatrième règle : la durée de fixation (demande du mainteneur du 4/11/2026, à la revue de Ninja)

Dans **tous les jeux**, la durée d'une fixation est **celle des réglages**, qui se change aussi **depuis le jeu**, par
l'engrenage bleu (`FixationLengthControl`) : pas de durée propre à un jeu, et une valeur lue **à chaque fixation**, pas
gardée depuis le début de la partie ou de la manche. Un jeu peut proposer de toucher « tout de suite » (sans fixation,
comme le Tir ou les Bulles) ; dès qu'il y a une fixation, sa durée suit cette règle.

**État au 4/11/2026** (recherche dans le code ; une durée écrite autrement a pu lui échapper) : presque tous les jeux
lisaient déjà la valeur à chaque fixation. **Deux exceptions corrigées** (décision du mainteneur : tout de suite, sans
attendre leur revue) : **Dés** (`ProgressPane`, 2 s écrites en dur) et **Lettres** (`Bloc`, valeur gardée à la création
de la manche). Test `ProgressPaneTest`. **Clic sur la valeur dans l'engrenage** : il remet maintenant 2 s, la vraie valeur
par défaut (il donnait 0,5 s ; `FixationLengthControlTest`). Champs copiés mais jamais lus, à retirer à la revue de leur
jeu : `CakeFactory`, `Cup`, `Potion`, `Labyrinth`.

**Fait vu en passant, non corrigé** : `GameContext.setOffFixationLengthControl()`, appelé par 6 jeux (Dessin, Blocs,
Diviseur, Room, Frogs Race, Follow), ne fait rien : son champ `fixPan` n'est jamais rempli. Le réglage de fixation reste
donc affiché dans leur engrenage. À voir à la refonte de l'engrenage.

## Cinquième règle : le temps avant sélection (demande du mainteneur du 4/11/2026, à l'essai de Memory)

Quand un jeu ou une manche apparaît, rien n'est sélectionnable pendant **le temps choisi dans les réglages** (Jeux,
« Délai avant de pouvoir sélectionner au lancement d'un jeu », clé `GAME_START_DELAY`, **1 s par défaut**, de 0 à 5 s) :
`IGameContext.onGameStarted()`, sans durée propre à un jeu (avant : 2 ou 3 s écrites dans 11 appels, rien dans Bouteilles
dont l'appel était vide). Les autres jeux n'appellent pas ce blocage et n'attendent pas. Une nouvelle clé de réglages,
sans effet sur les fichiers de résultats. Tests : `GameContextTest`, `ConfigurationContextTest`.
**Retour du mainteneur (4/11/2026)** : le délai ne bloquait que la souris, pas le regard (`AbstractGazeDeviceManager`
testait `isDisable()`, qui ignore le parent ; c'est maintenant `isDisabled()`). Les statistiques continuent d'enregistrer
le regard pendant le délai. Test `GazeDispatchCharacterizationTest`.

## Sixième règle : le sens de l'anneau (demande du mainteneur du 4/11/2026, à l'essai de Memory)

« Anneau » = tout indicateur de fixation, autour d'une carte comme rond (décision du mainteneur). Tous partent **du haut
et tournent dans le sens des aiguilles d'une montre**. Les contours de cartes allaient en sens inverse depuis le haut à
gauche (mesuré sur un rendu) : `GameStyle.clockwiseRing` et `fillRing`, utilisés par Memory, Où est… (`PictureCard`),
Flower Of Numbers, Opinions et GazePlay Eval. Les anneaux ronds sont des arcs de longueur négative (`FixationRing`,
Bulles, Tir…) ou un cercle tourné de -90° (Sounds Of Life, GazePlay Eval). Test : `GameStyleRingTest`.
**Memory** : sur une carte cachée, l'anneau prend la couleur du « ? » (`-gp-on-accent`), il était invisible.

## Marche à suivre pour chaque jeu

Chaque jeu passe par les mêmes 8 étapes. Les étapes 1 et 2 ne changent rien : elles finissent par une proposition que
le mainteneur valide avant tout changement.

| Étape | Quoi | Résultat |
|---|---|---|
| **1. État des lieux** | Lire le code du jeu (`gazeplay-games/.../games/<jeu>`), ses variantes et ses ressources ; le lancer en vrai (souris, Tobii si possible) ; captures « avant » | Fiche du jeu : ce qu'il fait, ses variantes, ses défauts vus (bugs, lenteurs, textes non traduits) |
| **2. Propositions** | Graphisme (style des menus : couleurs, polices, contours ; lisibilité au regard : taille des cibles, contraste, retour visuel de la fixation), gameplay (règles, difficulté, progression, récompense, fin de partie, variantes utiles ou à retirer), images à remplacer | Liste courte, classée, **validée par le mainteneur** |
| **3. Images et sons** | Remplacer les ressources non libres ou à risque (voir la colonne « Images ») ; noter source et licence | Ressources libres, créditées dans `gradle/images/README.txt` et la licence affichée |
| **4. Graphisme** | Appliquer ce qui a été validé ; garder les noms de fichiers ou adapter le code | Jeu au nouveau style |
| **5. Gameplay** | Appliquer ce qui a été validé ; ne pas changer ce que les statistiques enregistrent sans versionner le format | Règles améliorées |
| **6. Tests** | Tests du jeu (en ajouter si le jeu n'en a pas), `build`, `allGamesTest -PallGamesOnly=<nameCode> -PallGamesRepeat=5`, essai réel du mainteneur | Tout vert, retours du mainteneur pris en compte |
| **7. Finitions** | Nouvelle vignette du menu (`common/Thumbnails`), traductions (au moins français et anglais), **aide du jeu** (`data/help/games/<NameCode>-fra.txt` et `-eng.txt` : le principe, comment jouer, comment gagner, les variantes ; ajouter le jeu à `GameHelpTest`), mise à jour de ce document (colonnes et fiche) | Jeu terminé |
| **8. Commit** | Sur demande du mainteneur, un commit par jeu (`feat(<jeu>): …`) | Historique propre, un jeu à la fois |

**Fiche à remplir pour chaque jeu** (en bas de ce document, section « Fiches ») :

```
### <Jeu> (nameCode) — <date>
- Ce qu'il fait / variantes :
- Défauts vus :
- Propositions (validées ✔ / refusées ✘) :
- Images et sons remplacés :
- Fait (graphisme, gameplay) :
- Vérifications :
- Commit :
```

## Ordre de passage

Les jeux qui partagent leur code sont traités ensemble (une amélioration profite à toute la famille). L'ordre va du plus
urgent (images non libres, jeux très joués) au plus simple ; le mainteneur peut le changer.

1. **Famille Where Is It** (un même code, `whereisit`) : Where Is The Animal, Where Is The Sound (images non libres),
   puis Color, Flag, Letter, Number, Shape, Find The Odd One Out, Where Is It.
2. **Coloring** (image non libre : Hello Kitty).
3. **Frogs Race** (image non libre probable), puis Horses et Horses Simplified (mêmes Biboules).
4. **Famille Memory** (6 entrées, images par défaut) : Memory, Memory Letters, Memory Numbers, Open Memory (×3).
5. **Jeux à images à risque** : Sounds Of Life, Spot The Differences, Pet, Flower Of Numbers, Opinions, Sprint Finish
   (×2), Bubbles (×2), Egg Game (×2).
6. **Jeux de tir et d'action** : Shoot The Biboules, Shoot The Robots, Survive Against Robots (×2), Space Game, Tower
   Defense, Casse-Brique, Ninja, Whac-A-Mole, Biboule Jump, Gaze Race.
7. **Jeux de réflexion** : Labyrinth, Puzzle, Rush Hour, Train Switches, Connect4, Tic Tac Toe, Simon, Order, Rock Paper
   Scissors, Cups and Balls, Magic Potions.
8. **Jeux de calcul et de lettres** : Math101 (×5), Divisor, Rabbits, Dice, Goose Game, Letters, Dot To Dot.
9. **Jeux de création et divers** : Scribble, Scratch Card, Magic Cards, Cream Pie, Cakes, colorblend, Piano, Bottle,
   Room, Blocks, Ladder, Follow.
10. **GazePlay Eval et GazePlay Eval Test** : en dernier (contrat avec le site GazePlay Eval, format de résultats
    versionné).

Après chaque famille : `allGamesTest` complet (67 entrées depuis le 2/11/2026), et point avec le mainteneur.

## Légende des images

- **Non libre** : à remplacer.
- **Risque** : photo ou illustration sans source : demander à l'auteur (historique git) ou remplacer.
- **À confirmer** : dessin probablement fait par l'étudiant qui a créé le jeu.
- **OK** : libre, ou sans originalité (lettres, formes, couleurs, drapeaux).
- **Remplacé le 28/10** : images refaites (OpenMoji ou dessin par script), à regarder en vrai.

## Jeux (77 entrées du catalogue au 29/10/2026, 71 depuis le 30/10 : Horses Simplified et les 5 autres Memory sont des variantes)

| Jeu (nameCode) | Images (inventaire du 28/10/2026) | Graphisme | Gameplay |
|---|---|---|---|
| **Where Is The Animal** (`WhereIsTheAnimal`) | **Remplacées le 29/10** : 144 photos Wikimedia Commons (CREDITS.txt). Le dossier `whereIsTheAnimalDynamic` (80 photos Pixabay d'avant 2019) est libre probable | ✔ 29/10 (cartes, anneau, bandeau) | ✔ 29/10 (variantes exactes, Réécouter, pas de répétition) |
| **Where Is The Sound** (`WhereIsTheSound`) | **Remplacées le 29/10** : 64 photos Commons, triangle dessiné, harpe du Met ; 11 sons remplacés, 13 de La Sonothèque (CC0) | ✔ 29/10 | ✔ 29/10 (son dans toutes les langues) |
| Where Is The Color, Flag, Letter, Number, Shape | OK (couleurs unies, drapeaux, lettres, chiffres, formes) | ✔ 29/10 | ✔ 29/10 (Voyelles, Consonnes justes) |
| Find The Odd One Out (`FindTheOddOneOut`) | Libre probable : 48 photos Pixabay d'avant 2019 | ✔ 29/10 | ✔ 29/10 (consigne dite, pas de doublon) |
| Where Is It (`WhereIsIt`, images de l'utilisateur) | Pas d'image propre | ✔ 29/10 (et fenêtre du dossier) | ✔ 29/10 |
| **Coloring** (`Coloring`) | **Remplacés le 29/10** : 24 contours OpenMoji (CC BY-SA 4.0) ; Hello Kitty et les anciens coloriages retirés | ✔ 29/10 (boîte à outils, anneau, carte) | ✔ 29/10 (3 palettes + palette personnalisée, annuler, recommencer) |
| **Sounds Of Life** (`SoundsOfLife`) | **Remplacées le 30/10** : 4 décors dessinés, 21 animaux OpenMoji ; 21 sons (15 de Commons, 6 de Where Is The Sound), crédités | ✔ 30/10 (anneau, nom de l'animal) | ✔ 30/10 (rejeu, placement sans fond, exception) |
| **Spot The Differences** (`SpotDifference`) | **Remplacées le 30/10** : 8 scènes dessinées par script (décors du projet, objets OpenMoji), son synthétisé | ✔ 30/10 (grandes images, pastille, vert, flèche dans le coin) | ✔ 30/10 (fixation des réglages, images enchaînées, indice 20 s, 3 niveaux) |
| **Pet** (`Pet`) | **Remplacées le 30/10** : 4 pièces dessinées, objets OpenMoji, bouche triste ; la Biboule du projet gardée | ✔ 30/10 (boutons ronds, jauges nommées, bulle) | ✔ 30/10 (2 min par jauge, Biboule triste, activités claires, plantage corrigé) |
| **Flower Of Numbers** (`FlowerOfNumbers`) | **Remplacées le 2/11** : mains, dés, pièces et billets génériques dessinés par script ; fleur et auras dessinées par le jeu (avant : 21 images à risque, dont des reproductions de l'euro) | ✔ 2/11 (cartes de la couleur des pétales, anneau, plateau mis à l'échelle) | ✔ 2/11 (règle du mainteneur : total de tous les pétales, chaque valeur une fois ; modes Guidé, Libre, Tous les pétales ; jusqu'à 10 à 50 ; buts comptés) |
| **Opinions** (`Opinions`) | **Retirées le 2/11** : les 9 images (photos d'aliments jamais montrées, visages brillants) ; le jeu ne joue que les images de l'utilisateur | ✔ 2/11 (image en carte au centre, boutons « J'aime » / « Je n'aime pas » à icônes, anneau) | ✔ 2/11 (règle du mainteneur : images de l'utilisateur une par une, réponses enregistrées dans `-opinions.csv`) |
| **Sprint Finish** (`SprintFinish` ; `SprintFinishMouse` retiré, devenu une variante) | **Remplacées le 2/11** : chat, chien (recoloré) et poisson OpenMoji, prairie et gamelle dessinées ; haies, barrières, boutons, rondins dessinés par le jeu | ✔ 2/11 (plateau à l'échelle de l'écran, compte à rebours, chien regardé entouré, sons) | ✔ 2/11 (règle du mainteneur : seul, à deux ; cartes au hasard ; 5 / 10 / 15 cartes) ; 3/11 : « seul au clavier » retiré (seul : regard ou souris selon le réglage) |
| **Bubbles** (`Bubbles` ; `ColoredBubbles` et `PortraitBubbles` réunis) | **Remplacées le 2/11** : 3 paysages sous la mer dessinés, animaux OpenMoji, sons refaits | ✔ 2/11 (vraies bulles, vitres dépolies, compte 12 / 36) | ✔ 2/11 (règle du mainteneur : une carte, image à découvrir puis nouvelle image, éclater dès le regard ou après fixation) |
| **Egg Game** (`EggGame` ; `PersonalizeEggGame` réuni) | **Remplacées le 2/11** : œuf dessiné par le jeu, prairie et nid dessinés, animaux OpenMoji, sons refaits | ✔ 2/11 (nid, fêlures qui grandissent, éclosion animée, anneau) | ✔ 2/11 (règle du mainteneur : une carte, sans fin, rester sur l'œuf suffit, plus de « rétrécie ») |
| **Frogs Race** (`FrogsRace`) | **Remplacées le 30/10** : grenouilles et mare dessinées pour le projet (`make_race_images.py`) ; `Racer.png` (non libre, inutilisé), le tube rouge et la cage retirés ; Biboules gardées (dessins du projet) | ✔ 30/10 (couloirs, « Toi », langue, pastille) | ✔ 30/10 (Facile/Normal/Difficile, classement, statistiques justes) |
| Magic Potions (`Potions`) | **Remplacé le 28/10** : fioles, chaudron, bulle, décor (dessinés par script), explosion (OpenMoji) | | |
| Labyrinth (`Labyrinth`) | **Remplacé le 28/10** : animaux sans fond (OpenMoji) ; à confirmer : 19 dessins (souris, flèches, fromage) et 9 du dossier `common` des jeux | | |
| **Memory** (une seule carte depuis le 30/10, avant 6) | **Remplacées le 30/10** : les 9 images par défaut à risque (OpenMoji, 8 jeux en profitent), dos de carte dessiné par le jeu ; lettres et chiffres dessinés (Fredoka) | ✔ 30/10 (cartes, anneau, retournement, cadres, pastille) | ✔ 30/10 (une carte, Mes images, Dynamique corrigé, 1,5 s) |
| **Ninja** (`Ninja`) | **4/11** : verger et 8 fruits dessinés par script (`make_ninja_images.py`), sons refaits (`make_ninja_sounds.py`), vignette refaite ; plus d'images par défaut | ✔ 4/11 (fruits, lame et moitiés, anneau, pastille) | ✔ 4/11 (règle du mainteneur : un fruit à suivre, couper dès qu'on touche ou après fixation, sans fin / 20 / 50, Lente / Normale / Rapide / Adaptative, Mes images) |
| Blocks, Scratch Card, Magic Cards, Cream Pie | Utilisent les images par défaut (voir Memory) ; Magic Cards et Cream Pie : 2 dessins chacun, à confirmer | | |
| **Casse-brique** (`CasseBrique`) | **3/11** : plus d'image de l'ancien dossier `opinions` en fond ; fond dessiné par le jeu ou variante « Mes images » ; vignette refaite (capture) | ✔ 3/11 (briques arrondies aux couleurs douces, éclat, cœurs, pastille, extraterrestre redessiné) | ✔ 3/11 (règle du mainteneur : 3 vies, toutes les briques pour gagner, Facile / Normal / Difficile, 4 dessins gardés) |
| Puzzle (`Puzzle`) | À confirmer : 36 images (Biboule, paysages) ; la Joconde est dans le domaine public | | |
| Room (`Room`) | À confirmer : 9 images (murs, flèches) ; OK : ciel ESA/Hubble (CC BY 4.0, crédité) | | |
| **Défendre le château** (`TowerDefense`) | **3/11** : les 23 images retirées ; tout dessiné par script (`make_towerdefense_images.py`), neige par le jeu | ✔ 3/11 (polices des jeux, pastilles des prix, cercle de choix aux couleurs des menus, vignette) | ✔ 3/11 (règle du mainteneur : vagues à la demande ou automatiques, Facile / Normal / Difficile, sans fin / 10 / 20 vagues, « Perdu ! », carte au hasard ; 4 tours, revente et blizzard gardés) |
| Train Switches (`TrainSwitches`) | À confirmer : 43 dessins (trains, gares) | | |
| **Tir** (`Shooter` ; Shoot The Biboules et Shoot The Robots réunis) | **2/11** : tout redessiné par script (décors, robots, cage, soucoupe, éclairs, baguette, canon), sons refaits ; les Biboules gardées (mascotte du laboratoire), puis modernisées le 3/11 (bouche et ailes d'origine) ; musique CC0 | ✔ 2/11 (score lisible, cibles moins grandes, décors doux) | ✔ 2/11 (règle du mainteneur : une carte, tir immédiat ou fixation, sans fin / 50 / 100 cibles / 1 / 3 min) |
| **Survive Against Robots** (`SurviveAgainstRobots` ; une seule carte depuis le 3/11, avant 2) | **3/11** : les 12 dessins retirés ; 3 décors et 4 bonus dessinés par script (`make_survive_images.py`) ; la Biboule et les robots du Tir | ✔ 3/11 (cœurs, pastilles, décor au hasard parmi 3, vignette) | ✔ 3/11 (règle du mainteneur : 3 vies, sans fin / 1 / 3 min / 20 / 50 robots, souris ou regard selon le réglage des entrées, tir automatique ou vers l'avant, 3 niveaux, 4 bonus) |
| **Space Game** (`SpaceGame`) | **3/11** : les 12 images retirées ; fond, fusée, soucoupes et boss dessinés par script (`make_space_images.py`) | ✔ 3/11 (fond de nuit douce choisi, cœurs, pastilles, jauge du boss, vignette) | ✔ 3/11 (règle du mainteneur : 3 vies, sans fin ou boss après 1 / 3 min ou 20 / 50 extraterrestres, boss de 15 / 30 / 50 points de vie, 4 / 6 / 8 extraterrestres) |
| Cakes (`Cakes`) | À confirmer : 24 dessins | | |
| Follow (`Follow`) | À confirmer : 13 dessins | | |
| **Horses, Horses Simplified** | **Remplacées le 30/10** : plateaux et boutons du dé dessinés pour le projet (`make_horses_images.py`, d'après `positions*.json`) ; `selector.png` retiré ; Biboules gardées | ✔ 30/10 (fond des menus, équipe en cours, anneaux, cartes) | ✔ 30/10 (contre l'ordinateur, dé équilibré, règles corrigées) |
| Divisor, Rabbits | À confirmer : 10 dessins (lapins) | | |
| **Biboule Jump** (`BibJump`) | **4/11** : ciel, nuages, tremplin et sons faits par script (`make_biboulejump_images.py`, `make_biboulejump_sounds.py`) ; Biboules du Tir | ✔ 4/11 (Biboule non déformée, ciel, nuages et tremplin plus grand, cœurs, pastilles, vignette) | ✔ 4/11 (règle du mainteneur : 3 vies, retour sur un nouveau nuage avec les points gardés ; sans fin, 1 ou 3 min ; Facile / Normal / Difficile ; même saut sur tout écran ; plus de record) |
| Rock Paper Scissors | À confirmer : 7 dessins (mains façon gant de dessin animé) | | |
| Piano (`Piano`) | À confirmer : 6 images | | |
| Math101 (×5 opérations) | À confirmer : 5 images ; OK : tableau (Openclipart, domaine public) | | |
| Bottle (`Bottle`) | À confirmer : 5 images | | |
| Gaze Race (`GazeRace`) | À confirmer : 4 images | | |
| Rush Hour (`RushHour`) | À confirmer : 4 images | | |
| Tic Tac Toe (`TicTacToe`) | À confirmer : 3 images | | |
| Order (`Order`) | À confirmer : 3 images | | |
| **Jeu de la taupe** (`WhacAMole`) | **4/11** : les 3 images et les 2 sons retirés ; pré, trou, taupe et sons faits par script (`make_moles_images.py`, `make_moles_sounds.py`) ; Biboules du Tir | ✔ 4/11 (cible qui sort de derrière le bord du trou, sans déformation, étoile, anneau, pastilles, vignette) | ✔ 4/11 (règle du mainteneur : taupes, Biboules ou Mes images ; dès qu'on regarde ou après fixation ; 6 ou 9 trous, 1 à 3 cibles, temps dehors = fixation + 3 / 2 / 1,5 s ; sans fin, 1 ou 3 min, 20 ou 50) |
| colorblend (`colorblend`) | À confirmer : 2 images | | |
| Simon (`Simon`) | À confirmer : 2 images | | |
| Cups and Balls, Dice, Dot To Dot, Goose Game, Ladder | À confirmer : 1 image chacun | | |
| Connect4 (`Connect4`) | Pas d'image propre. Plantage audio sous Windows corrigé le 28/10/2026 (`MMDevAPI.dll`) : à revoir en vrai | | |
| Scribble (`Scribble`), Letters (`Letters`) | Pas d'image propre relevée | | |
| GazePlay Eval, GazePlay Eval Test | Pas d'image propre ; le dossier `gazeplayEvalTest` (4 images des anciens jeux BERA) : vérifier s'il sert encore | | |

## Hors jeux

- **Vignettes du menu** (`common/Thumbnails`, 68) : captures des jeux, à refaire avec chaque jeu modifié.
- **Icônes communes** (`common/images`, 17 de source inconnue : smiley, dossier, maison…) : **risque** ; les icônes de
  la musique (Flaticon, CC BY 3.0) sont créditées.
- **Sons et musiques** : pas encore inventoriés ; à faire avec chaque jeu.

## Fiches

Une fiche par jeu ou par famille, remplie pendant la revue (modèle plus haut).

### Famille Where Is It (9 entrées, code `whereisit`) — 29/10/2026

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/whereisit/`
(40 images, 1366×768, mode souris, français ; 4 temps par essai : question, images, fixation sur l'image en haut à
gauche, 2 s après). Elles ont été prises avec le script `build/game-screenshots/captures.ps1` (vraie fenêtre, dossier
GazePlay isolé). Pas d'essai avec le Tobii.

- **Ce qu'il fait** : une question est écrite au centre et dite (voix d'homme ou de femme au hasard) pendant 5 s
  (`QUESTION_LENGTH`), puis descend en bas de l'écran ; les images apparaissent en grille ; une fixation
  (`FIXATION_LENGTH`) choisit une image. Si c'est la bonne : les autres disparaissent, elle grandit jusqu'à remplir
  l'écran, puis l'animation de victoire (confettis, Biboule) et une nouvelle manche. Sinon : l'image pâlit et une croix
  apparaît ; la question est redite si le réglage le demande. Code commun : `WhereIsIt` (794 lignes), `PictureCard`
  (384), `GameSizingComputer` ; une énumération `WhereIsItGameType` (28 valeurs) ; un lanceur, un générateur de
  variantes et une spec par entrée.
- **Variantes** (grilles 1×2, 2×2, 2×3, 3×2, 3×3) :
  - Where Is The Animal : Ferme, Forêt, Savane, Oiseaux, Mer, Tous (50 animaux, 150 photos), et Dynamique (1×2
    seulement : toujours « Où est le chat ? », les autres animaux par niveau 1 à 5, niveau ajusté tous les 8 points
    par un test du χ²) ;
  - Where Is The Sound : Animaux, Instruments, Tous (24 sons, 65 photos) ;
  - Color (Facile, Normal, Difficile ; 16 couleurs), Flag (Les plus connus, Afrique, Amérique, Asie, Europe, Tous ;
    44 drapeaux), Letter (Voyelles, Consonnes, Toutes ; 26), Number (10 chiffres), Shape (Facile, Normal, Difficile ;
    20 formes) ;
  - Find The Odd One Out (6 thèmes, 48 photos ; 2×2 à 3×3) ; Where Is It (dossier d'images de l'utilisateur, avec
    `questions.csv`, sons et pictogrammes facultatifs).
- **Défauts vus** :
  - **Fait (vu en capture)** : le filtre des variantes compare des **sous-chaînes du chemin entier**
    (`WhereIsIt.java:382`, `s::contains`). La variante **Voyelles** montre des consonnes : `I`, `T` et `L` sont dans
    « data/where**I**s**T**he**L**etter/… », donc tous les dossiers passent ; Consonnes laisse passer aussi toutes les
    lettres. Dans **Ferme**, `pig` laisse passer `pigeon` (vu : une tourterelle dans « Où est la poule ? ») et `horse`
    laisse passer `seahorse` (déduit du code, pas vu).
  - **Fait (code)** : hors français, anglais et chinois, **aucun son** n'est joué (`WhereIsIt.java:624`), y compris
    dans **Where Is The Sound**, dont le son ne dépend pourtant pas de la langue : ce jeu est injouable dans les
    19 autres langues. Sa consigne « listen » n'est traduite qu'en anglais et en français.
  - **Fait (code)** : dans Where Is The Sound, le son n'est rejoué qu'après une erreur, et seulement si le réglage
    « redire la question » est actif ; aucun moyen de le réentendre autrement.
  - **Fait (code)** : Find The Odd One Out ne dit jamais sa consigne, alors que `findTheOddOneOut/sound/` contient
    `odd.m.fra.mp3`, `odd.w.fra.mp3` (et l'anglais), jamais lus. Traduction chinoise fausse (« 找出奇数 » : « trouvez
    le nombre impair »).
  - **Fait (vu)** : Where Is It lancé sans dossier d'images : écran noir, le message d'erreur (`WII-error`) n'est pas
    visible. **Hypothèse** : depuis le menu, `GameWhereIsItErrorPathDialog` empêche ce cas ; à vérifier.
  - **Fait (vu)** : à la victoire, l'image agrandie n'est pas centrée (drapeau de l'Argentine collé à gauche, bande
    noire à droite) : le calcul ne tient pas compte du recadrage de l'image dans sa case.
  - **Fait (vu)** : aucun espace ni contour entre les images (les fonds blancs des formes et des lettres se touchent ;
    les cases de Color se touchent et la case **noire** se confond avec le fond noir du jeu). Le bouton des réglages
    (roue en bas à gauche) recouvre le coin de l'image en bas à gauche.
  - **Fait (vu)** : style d'origine, sans lien avec les menus refaits : fond noir, texte blanc en police système, croix
    rose (`error.png`), indicateur de progression gris à 50 % d'opacité au centre de l'image.
  - **Fait (code)** : les photos sont chargées à leur taille d'origine (`new Image(imagePath)`), jusqu'à 5760×3840
    (`accordion_1.jpg`), soit environ 88 Mo en mémoire pour une seule image. Le dossier `whereIsTheAnimal` pèse 97 Mo
    (26 photos de plus de 1 Mo). **Risque** : lenteur à l'apparition des images et mémoire, surtout en 3×3. Rien n'a été
    mesuré.
  - **Fait (code)** : dans Find The Odd One Out et Dynamique, les images d'une même manche sont tirées avec remise : la
    même photo peut apparaître deux fois.
  - **Fait (code)** : du code mort : le paramètre `fourThree` vaut toujours `false`, `whereIsTheAnimalDynamic/levels.json`
    n'est pas lu (et contient des fautes : « turtules », « pigeaons », « tires »).
  - **Fait** : tests existants : `WhereIsItTest` (2 tests), `PictureCardTest` (3), `WhereIsItValidatorTest`. Rien ne
    vérifie les variantes ni les sons.
- **Images et sons** : voir le tableau plus haut (Animal : 149 photos à risque et `beaver_1` non libre ; Sound :
  5 photos iStock/Getty non libres et 59 à risque). **Sons pas encore inventoriés** : voix des questions (Animal,
  Color, Flag, Letter, Number, Shape, Dynamique, Odd), 24 sons de Where Is The Sound sans source connue.
- **Propositions (étape 2, 29/10/2026)**, en attente de validation (✔ / ✘). Aucune ne change le format des résultats
  (fichiers, colonnes, `formatVersion`) ni ce qui est compté (points, erreurs, χ²). **Risque commun à A1, A6 et C2** :
  le rejeu d'une partie déjà enregistrée refait les tirages à partir de sa graine ; si l'ensemble des images ou la
  façon de tirer change, le rejeu d'une ancienne partie de ces jeux montre d'autres images que celles vues à
  l'époque (les fichiers enregistrés ne changent pas).
  - **A. Corrections** (sans choix de style, à faire en premier) :
    - A1. Variantes : comparer le **nom exact du dossier** au lieu d'un morceau du chemin (Voyelles, Consonnes, Ferme
      redeviennent justes). Test ajouté : chaque variante ne contient que ses dossiers.
    - A2. Where Is The Sound : jouer le son **dans toutes les langues** (il ne dépend pas de la langue), et traduire
      « listen » dans les langues du fichier.
    - A3. Find The Odd One Out : dire la consigne (`odd.m/w.fra/eng.mp3`, déjà livrés) ; corriger la traduction
      chinoise.
    - A4. Victoire : image agrandie **centrée**.
    - A5. Charger les images **à la taille de leur case** (même aspect, moins de mémoire).
    - A6. Pas deux fois la même photo dans une manche (Odd, Dynamique).
    - A7. Where Is It sans dossier : message d'erreur lisible.
    - A8. Retirer le code mort (`fourThree`, `levels.json`).
  - **B. Graphisme** (style des menus : couleur principale, polices Andika et Fredoka, coins arrondis) :
    - B1. **Cartes** : un espace entre les images, coins arrondis, fond et contour discrets (la case noire de Color
      et les fonds blancs des formes et lettres se distinguent du fond) ; suit le réglage clair / sombre.
    - B2. **Fixation** plus visible au regard : un anneau ou un contour de la couleur principale qui se remplit
      autour de la carte, et un léger grossissement, à la place de l'indicateur gris au centre de l'image.
    - B3. **Question** en Fredoka, dans un bandeau arrondi en bas, qui ne chevauche ni les images ni la roue des
      réglages ; la grille laisse libre la place de la roue.
    - B4. **Réponses** : bonne réponse entourée de vert avant l'agrandissement ; mauvaise réponse : croix dessinée
      (couleur « danger » du thème) à la place de `error.png`, image grisée.
  - **C. Gameplay** :
    - C1. Bouton **« Réécouter »** (grande cible, activée par fixation) dans le bandeau, pour les jeux qui ont un son
      (indispensable dans Where Is The Sound, utile partout).
    - C2. La bonne réponse ne revient pas **deux manches de suite**.
    - C3. Where Is The Sound : un haut-parleur animé pendant l'écoute, en plus de « Écoute le son ».
  - **D. Images et sons** :
    - D1. Remplacer tout de suite les **6 images non libres** (5 iStock/Getty de Where Is The Sound, `beaver_1`),
      en gardant les noms de fichiers.
    - D2. Source des remplacements : **photos de Wikimedia Commons** sous CC0, CC BY ou CC BY-SA, avec un crédit
      par fichier (recommandé : le jeu fait reconnaître de vrais animaux et objets), ou dessins OpenMoji comme pour les
      images par défaut (homogène, mais moins réaliste). **Hypothèse à vérifier** : les photos Pixabay publiées
      depuis 2019 (licence Pixabay, non libre au sens de la GPL) ne conviennent pas ; celles d'avant 2019 (CC0)
      conviennent.
    - D3. Les **208 photos à risque** (149 Animal, 59 Sound) : les remplacer toutes maintenant (gros travail, script
      de téléchargement et crédits), ou par lots plus tard.
    - D4. Réduire les photos livrées à 1280 px au plus (le dossier Animal pèse 97 Mo ; gain non mesuré).
    - D5. **Sons** : inventaire de la famille (voix des questions : chercher l'auteur dans l'historique git ; 24 sons
      de Where Is The Sound : remplacer par des sons CC0 ou CC BY si la source reste inconnue).
  **Validées le 29/10/2026 : toutes (A, B, C, D) ✔** ; D2 : photos de Wikimedia Commons ; D3 : tout de suite.
- **Défauts trouvés en chemin (29/10/2026)** : la variante Oiseaux et « Tous » ne montraient jamais le flamant rose
  (`"greater flamingo"` dans `variants.json`, dossier `greaterflamingo`) ; la question du serpent avait la clé
  `snakev` (jamais trouvée) ; « Où est le chat? » sans espace. Corrigés.
- **Défaut du menu signalé par le mainteneur (29/10/2026)** : après une partie lancée par la fenêtre des variantes, le
  menu revenait flou (navigable). Cause : choisir une variante fermait la fenêtre sans retirer le flou du menu (seule
  la croix le retirait), et le menu est gardé en mémoire. Ancien (avant la refonte), pour tout jeu à plusieurs
  variantes ; même oubli dans la fenêtre du dossier de Where Is It. Corrigé : le flou est retiré à la fermeture de la
  fenêtre, quoi qui la ferme (`setOnHidden`, comme `EvalChooserDialog`) ; test `GameVariantDialogTest`.
- **Fenêtre « dossier de Where Is It » (signalée par le mainteneur le 29/10/2026)** : restée à l'ancien style (cadre du
  système, texte rouge, boutons gris) ; son message principal était une clé absente des traductions
  (`WhereIsItNot Config Directory`, affichée telle quelle) et son titre « error ». Refaite au style des menus
  (`MenuDialog` : carte arrondie, titre « Où est-ce ? », boutons arrondis, couleur « danger » pour un mauvais dossier,
  Valider au clavier aussi) ; message `WhereIsItDirNotConfig` ; « Réinitialiser » avec une majuscule (aussi dans les
  réglages). Test `GameWhereIsItErrorPathDialogTest` ; capture `3d-ou-est-ce-dossier` ajoutée à `menuScreenshots`.
- **Images et sons remplacés** :
  - Photos de Where Is The Animal (150) et Where Is The Sound (71, la harpe du Met gardée) : Wikimedia Commons,
    « Quality images » d'abord, licences CC0, domaine public, CC BY (toutes versions) ou CC BY-SA 4.0 seulement (les
    CC BY-SA 2.0 à 3.0 ne sont pas compatibles avec la GPL 3) ; choisies une par une sur planches (pas d'œufs, de
    squelettes, de personnes reconnaissables pour les instruments) ; 3 par dossier, nommées `<dossier>_1..3.jpg`,
    1280 px au plus. Crédits : `data/whereIsTheAnimal/CREDITS.txt`, `data/whereIsTheSound/CREDITS.txt`, licence
    affichée.
  - Sons de Where Is The Sound : 13 de La Sonothèque (CC0, vérifié sur leur site) gardés ; 11 remplacés par des sons
    de Commons (accordéon, batterie, éléphant, flûte, harpe, piano, mouton, serpent, triangle, trompette, violon),
    raccourcis à 8 s, normalisés (ffmpeg, loudnorm), mono. Remplacés parce que sans source (8), sous licence
    propriétaire Sound Fishing (mouton, serpent) ou SoundJay (harpe). **Choisis d'après leur titre, sans écoute** :
    à écouter pendant l'essai.
  - Voix des questions (toute la famille) : ajoutées par Didier Schwab (2017) et Sakraft (2022), sans source notée ;
    probablement de la synthèse vocale (**hypothèse**). **Question** : à confirmer auprès d'eux ; gardées.
  - **Deuxième tri (29/10/2026, demande du mainteneur : des images plus faciles à comprendre pour des enfants ; les
    pictogrammes ARASAAC, sous CC BY-NC-SA 4.0, ne sont pas compatibles avec la GPL 3)** : règle stricte, l'animal
    entier et bien visible, sans gros plan de la tête, sans camouflage ni animal minuscule. 30 photos remplacées
    (girafe, aigle, chouette, paon, perroquet, pigeon, zèbre, hippopotame, dauphin, crocodile, pieuvre, hippocampe,
    vautour, loup, renard, chat, ours, raton laveur, mouton, serpent, chien, poule…), 5 retirées sans remplaçant
    (castor : 1 photo ; abeille, chouette, zèbre, vautour : 2). Where Is The Animal : 144 photos.
  - Triangle de Where Is The Sound : aucune photo libre utilisable, dessiné par `gradle/images/make_triangle_image.py`.
    Castor, poule, applaudissements, flûte, harpe, trompette, violon : 1 ou 2 photos seulement (rien de mieux sur
    Commons) ; les animaux sont gardés (un animal nouveau n'aurait pas de voix pour sa question). Scripts et choix :
    `gradle/images/commons/`.
- **Fait (graphisme, gameplay), 29/10/2026** :
  - A1 filtre exact (`WhereIsIt.filterFolders`) ; A2 son de Where Is The Sound dans toutes les langues, « listen »
    traduit ; A3 consigne de l'intrus dite, traduction chinoise corrigée ; A4 victoire centrée (la carte entière) ;
    A5 images chargées au plus à la taille de la zone de jeu ; A6 pas de doublon dans une manche (Odd, Dynamique) ;
    A7 message d'erreur dans un bandeau ; A8 `fourThree` et `levels.json` retirés.
  - B : `GameSizingComputer` (grille avec espaces au-dessus d'un bandeau qui laisse la roue libre), `PictureCard`
    (carte arrondie aux couleurs du thème, anneau de fixation de la couleur principale qui fait le tour de la carte,
    grossissement léger, cadre vert, croix « danger » dessinée, contour des échantillons de Color), question en
    Fredoka dans un bandeau arrondi (`GameStyle`, nouveau, dans `gazeplay-games-commons`, réutilisable par les
    autres jeux).
  - C1 bouton « Réécouter » (`ReplayQuestionButton`, fixation, icône Material Symbols `volume_up`) ; C2 pas deux fois
    la même réponse (échange de places, sans tirage en plus) ; C3 haut-parleur animé pendant l'écoute.
  - Rejeu : les tirages gardent l'ordre des versions précédentes (dossier, image, voix) ; seules les manches touchées
    par A1, A6 ou C2 se rejouent autrement.
  - **Changé sans être demandé** : le réglage « couleur de la barre de progression » ne s'applique plus à ces jeux
    (l'anneau prend la couleur principale, comme validé en B2) ; les zones d'intérêt (écran des statistiques, rien sur
    disque) sont maintenant le centre de chaque carte.
- **Vérifications (29/10/2026)** : `build` réussi, **539 tests, 0 échec** (509 avant ; nouveaux tests dans
  `WhereIsItTest` : variantes exactes, sons par langue, consigne de l'intrus, pas de doublon, pas deux fois la même
  réponse, disposition) ; `allGamesTest` complet réussi (77 entrées) ; les 9 entrées de la famille lancées 5 fois
  chacune (`-PallGamesRepeat=5`) sans échec. Captures « après » : `build/game-screenshots/apres/whereisit/` et
  `essai1/whereisit/`. **Reste** : l'essai réel du mainteneur (souris et Tobii ; écouter les 11 sons remplacés),
  puis l'étape 7 (nouvelles vignettes du menu).
- **Essai réel du mainteneur (29/10/2026)** : validé (« tout est bon »), après les corrections du flou du menu et de
  la fenêtre du dossier de Where Is It.
- **Étape 7 (finitions), 29/10/2026** : 9 vignettes du menu refaites (`common/Thumbnails`, mêmes noms) à partir de
  captures des jeux, roue des réglages masquée, 960 × 540 (format des cartes du menu) ; Where Is It montré avec un
  dossier d'exemple (4 photos de Where Is The Animal). Crédits : `gradle/images/README.txt` (les vignettes montrent les
  photos créditées dans les `CREDITS.txt`), inventaire mis à jour. Traductions : « listen » dans les 21 langues, pas
  d'autre texte nouveau (le bouton « Réécouter » n'a pas de texte). `build` : **543 tests, 0 échec**.
- **Commit** : `c3c67bb5e` (29/10/2026), `feat(whereisit): revue de la famille Where Is It`.

### Coloring (`Coloring`, code `colors`) — 29/10/2026

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/coloring/` (mode
souris, 1366×768). Pas d'essai avec le Tobii.

- **Ce qu'il fait** : un dessin noir et blanc à gauche, une boîte à outils à droite (palette de 5 couleurs plus une
  couleur choisie, boutons Charger, Sauvegarder, Imprimer) ; une fixation sur une zone du dessin la remplit de la couleur
  choisie (remplissage par zones, un point compté par remplissage) ; flèches « précédent » et « suivant » pour changer de
  dessin parmi 17. Pas de variante. Code : `ColorsGame` (765 lignes), `ColorToolBox` (617), `ColorBox`,
  `CustomColorPicker` ; aucun test.
- **Défauts vus** :
  - **Fait (vu, journal)** : la flèche « suivant » (et « précédent ») lève `IllegalArgumentException: duplicate children
    added` (`ColorsGame.launch`, ligne 204) : elle relance tout le jeu, qui rajoute des nœuds déjà présents. Le dessin
    change quand même (le changement est fait avant), mais le reste du lancement est interrompu à chaque fois.
  - **Fait (code)** : chaque changement de dessin ajoute un gestionnaire de coloriage et deux écouteurs de taille sans
    retirer les anciens. **Hypothèse** : après plusieurs dessins, un clic de souris colorie (et compte un point) autant
    de fois qu'il y a de gestionnaires ; à vérifier. `dispose()` est vide.
  - **Fait (vu)** : palette pauvre : rouge, bleu, vert, blanc, ocre (+ une case « couleur personnalisée » blanche au
    départ, d'où deux cases blanches) ; ni jaune, orange, rose, violet, marron, gris. Boutons du bas tronqués
    (« Char… », « Sauvegar… », « Impri… »). Style d'origine (fond doré, cases rectangulaires, flèches vertes).
  - **Fait (journal)** : traductions absentes : `imgChooserTitle`, `customColorDialogTitle`.
  - **Fait (code)** : `draw-leaf.png` fait 6000×7500 pixels, converti pixel par pixel en noir et blanc : lent et lourd
    en mémoire (non mesuré). Le remplissage n'atteint jamais la dernière colonne de pixels.
  - Pas d'annulation : un coloriage raté au regard ne peut pas être défait (seulement recouvert).
- **Images** : **non libre** : `draw-hellokitty.jpg` (Sanrio). **Source inconnue** : les 16 autres coloriages
  (ajoutés par Karine Benhaimi en 2022 et Yaniissou en 2024, style de cliparts du web), `next.png`, `prev.png`,
  `palette.png` (2018). `add-button-inside-black-circle.png` et `error.png` : Flaticon CC BY 3.0, crédités.

**Étape 2 (propositions), 29/10/2026**, en attente de validation (✔ / ✘). Aucune ne change le format des résultats ni
ce qui est compté (un point par zone remplie).

- **A. Corrections** :
  - A1. Flèches précédent / suivant : changer seulement le dessin, sans relancer le jeu (plus d'exception) ; retirer les
    anciens gestionnaires et écouteurs ; `dispose()` qui nettoie. Tests ajoutés.
  - A2. Dessins réduits au chargement (2000 px au plus) : plus de lenteur avec les grands fichiers.
  - A3. Traductions manquantes (français, anglais) ; remplissage jusqu'au bord.
- **B. Graphisme** (style des menus) :
  - B1. Boîte à outils : panneau arrondi aux couleurs du thème, grandes pastilles de couleur, la couleur choisie
    entourée de la couleur principale ; boutons Charger / Sauvegarder / Imprimer avec icônes (Material Symbols), sans
    texte tronqué.
  - B2. Flèches précédent / suivant rondes au style des menus, avec l'anneau de fixation ; « 3 / 17 » entre les deux.
  - B3. Dessin sur une carte blanche arrondie, avec une marge ; indicateur de fixation sur le dessin de la couleur
    principale.
- **C. Gameplay** :
  - C1. Palette de 12 couleurs, toutes visibles sans pages (rouge, orange, jaune, vert clair, vert, bleu clair, bleu,
    violet, rose, marron, gris, blanc pour gommer), plus la couleur personnalisée.
  - C2. Bouton « Annuler » (le dernier remplissage), activé par fixation. Le point déjà compté reste compté.
  - C3. Bouton « Recommencer » (le dessin redevient blanc).
- **D. Images** :
  - D1. Retirer Hello Kitty tout de suite.
  - D2. Remplacer les 16 coloriages de source inconnue par les contours noirs d'OpenMoji (CC BY-SA 4.0, déjà utilisé
    pour les animaux par défaut) : dessins simples, zones fermées, faciles à colorier au regard ; une vingtaine de sujets
    (pomme, poisson, papillon, gâteau, dauphin, tulipe, raisin, maison, arc-en-ciel, lapin, soleil, lune, tortue, chat,
    voiture, étoile…), à choisir sur planche. Autre choix : demander aux deux auteurs d'où viennent leurs dessins.
  - D3. Flèches et boutons : icônes Material Symbols (déjà livrées) à la place de `next.png`, `prev.png`, `palette.png`.
- **Validées le 29/10/2026 : toutes (A, B, C, D) ✔** ; D2 : contours d'OpenMoji.
- **Images remplacées** : 17 coloriages (dont Hello Kitty) → 20 contours noirs d'OpenMoji (CC BY-SA 4.0 ; pomme,
  ballon, papillon, gâteau, poussin, poisson, fleur, grenouille, cadeau, raisin, maison, glace, coccinelle, étoile,
  fraise, soleil, ourson, train, arbre, tortue), générés par `gradle/images/make_coloring_images.py` : agrandis, traits
  épaissis ; **zones fermées vérifiées une à une** (le chat, le chien, la voiture, le lapin entier, l'éléphant,
  l'arc-en-ciel d'OpenMoji ont des traits ouverts : la couleur aurait coulé sur tout le fond, non retenus). Images des
  flèches, de la palette et des boutons retirées (icônes Material Symbols : `arrow_forward`, `undo`, `restart_alt`,
  `save`, `print` ajoutées à `data/icons`). Crédits : licence affichée, `gradle/images/README.txt`, inventaire.
- **Fait (29/10/2026)** :
  - A1 flèches : changent seulement le dessin (`changeDrawing`) ; plus de relance du jeu ni d'exception ; nœuds et
    écouteurs créés une fois, retirés par `dispose()`. A2 dessins chargés à 1600 px au plus ; pixels traités dans un
    tableau (plus rapide que la lecture pixel par pixel). A3 traductions (français, anglais) ; remplissage jusqu'au bord.
  - B : `ColorToolBox` refaite (panneau arrondi, pastilles rondes, couleur choisie entourée de la couleur principale,
    outils et fichiers en boutons ronds à icône avec info-bulle) ; `GazeRoundButton` (nouveau, `gazeplay-games-commons`,
    fixation ou clic, réutilisable) ; flèches rondes et « n / 20 » dans un bandeau ; dessin sur une carte blanche
    arrondie ; anneau de fixation qui suit le regard (`FixationRing`, à la place de l'indicateur gris et de son
    triangle) ; fenêtre « Autres couleurs » au style des menus (15 couleurs, le coloriage en pause pendant ce temps).
  - C1 palette de 12 couleurs, toutes visibles, + couleur personnalisée ; C2 « Annuler » (10 niveaux ; le point compté
    reste compté) ; C3 « Recommencer » (lui-même annulable).
  - `CustomColorBox` et `CustomColorPicker` retirés (remplacés).
- **Vérifications** : `ColorsGameTest` (6 tests : noir et blanc, remplissage jusqu'au bord, 20 dessins, flèches sans
  relance, annuler et recommencer, niveaux d'annulation) ; `build` : **549 tests, 0 échec** ; `allGamesTest
  -PallGamesOnly=Coloring -PallGamesRepeat=5` réussi. Captures : `build/game-screenshots/essai2/coloring/`.
  **Reste** : l'essai réel du mainteneur (souris et Tobii), puis l'étape 7 (vignette).
- **Demandes du mainteneur après l'essai (29/10/2026)** :
  - Le jeu commençait à « 7 / 20 » : il démarrait sur le dessin du réglage `COLORS_DEFAULT_IMAGE` (par défaut
    `draw-flower.png`, 7e par ordre alphabétique). Il démarre maintenant sur le premier dessin de la variante ; le
    réglage ne sert plus qu'à retrouver la dernière image chargée, dans « Mes images ».
  - **4 variantes** (fenêtre des variantes avant le jeu, `ColoringVariant`) : **Facile** (1 ou 2 zones à colorier),
    **Normal** (3 à 6), **Difficile** (7 et plus), 8 dessins chacune, et **Mes images** (seulement les images chargées
    par l'utilisateur, copiées dans le dossier `colors` du profil ; message s'il n'y en a pas encore). Zones comptées par
    calcul sur la page telle que le jeu la voit (zones fermées d'au moins 0,05 % de la page). 24 dessins OpenMoji
    (ajoutés : hibou, fusée, tournesol, château, grande roue, diamant) dans `data/colors/images/easy|normal|hard`.
    Une partie enregistrée avant les variantes se rejoue en Facile.
  - Les images sont chargées au plus à 1600 px, sans être agrandies (une petite image restait floue) ; flèches grisées
    quand il n'y a qu'un dessin.
  - Vérifications : `ColorsGameTest` (10 tests), `build` réussi, `allGamesTest -PallGamesOnly=Coloring
    -PallGamesRepeat=3` réussi ; captures `build/game-screenshots/essai3/coloring/`.
- **Décision du mainteneur (29/10/2026)** : variante « Mes images » retirée (des images non adaptées seraient trop
  difficiles à colorier) ; les trois autres renommées **Petite palette** (1 ou 2 zones), **Moyenne palette** (3 à 6)
  et **Grande palette** (7 et plus). Les valeurs internes (`EASY`, `NORMAL`, `HARD`) sont gardées, car elles sont
  écrites dans les parties enregistrées. Le bouton « Charger une image » de la boîte à outils reste (il existait avant
  la revue). `build` réussi ; `allGamesTest -PallGamesOnly=Coloring -PallGamesRepeat=3` réussi.
- **Décision du mainteneur (29/10/2026) : les images de l'utilisateur sont toujours une variante.** Bouton « Charger une
  image » retiré de Coloring ; variante **Palette personnalisée** ajoutée : elle joue les images du dossier
  `files/images/Coloring` du profil (par défaut `Documents\GazePlay_Learning\files\images\Coloring`).
  - Mécanisme commun (`UserImagesVariant`, `gazeplay-games-commons`) : une valeur de variante qui l'implémente donne son
    dossier ; sans autre code, le jeu a alors un réglage (réglages, Dossiers, « Images des jeux », une ligne par jeu,
    trouvée dans le catalogue), une fenêtre d'images (`UserImagesDialog` : vignettes, suppression, « Ajouter des
    images », copiées dans le dossier) et, dans le menu, la même fenêtre en rouge quand la variante est choisie sans
    image (comme pour « Où est-ce ? ») : « Valider » lance le jeu dès qu'il y a une image.
  - Tests : `UserImagesDialogTest` (2), `ColorsGameTest` (11) ; capture `3e-images-utilisateur` de `menuScreenshots`.
  - **Reste (à décider, voir la réponse au mainteneur)** : les autres jeux qui lisent les anciens dossiers partagés de
    `files/images` (`magiccards` : Magic Cards, Memory ; `default` : Memory ; `portraits` : bulles, tartes à la crème,
    échelle, taupes, Ninja, Divisor ; `blocs` : Blocs, Egg, Letters ; `opinions` : Opinions, Casse-briques), sans
    variante (les images de l'utilisateur remplacent celles du jeu sans le dire).
- **Essai réel du mainteneur (29/10/2026)** : validé (« tout est ok »), avec la palette personnalisée.
- **Étape 7 (finitions), 29/10/2026** : vignette du menu refaite (`common/Thumbnails/colors.png`, même nom) : capture
  de la Moyenne palette, papillon à moitié colorié, roue des réglages masquée, 960 × 540. Crédits :
  `gradle/images/README.txt`, inventaire. Traductions des textes nouveaux : français et anglais. `build` : **556
  tests, 0 échec**.
- **Commit** : `475853c04` (29/10/2026), `feat(colors): revue du jeu Coloriage, et images de l'utilisateur en variante`.

### GazePlay Eval et GazePlay Eval Test (`GazePlayEval`, `GazePlayEvalTest`, code `gazeplayEval`) — 29/10/2026

Passée avant les autres familles (demande du mainteneur, 29/10/2026 : finir GazePlay Learning Eval, pour qu'il ne
reste que les jeux).

**Étape 1 (état des lieux) faite le 29/10/2026.** Captures « avant » : `build/game-screenshots/avant/eval/`
(évaluation de référence « Tests sons et vidéos », installée dans un dossier GazePlay isolé ; une image par seconde
pour le début : `eval-video-t03..16`). Mode souris, pas d'essai avec le Tobii. Les menus des évaluations
(`EvalMenuScreen`, fenêtre de choix, réglages) ont déjà été refaits (`REFONTE_GRAPHIQUE.md`) : la revue porte sur le
**lecteur** (`GazeplayEval`, 1 260 lignes ; `EvalTarget`).

- **Écrans du lecteur** : message (aucune évaluation, évaluation illisible) ; formulaire participant ; transition
  (croix de fixation) ; consigne (texte, image, vidéo, son, bouton « Commencer » à fixer) ; grille (images et sons,
  sélection par fixation) ; écran « Recommencer / Continuer » (modes de passation) ; « Fin de l'évaluation ».
- **Défauts vus** :
  - **Fait (vu)** : style d'origine partout : police du système, tailles fixes en pixels (36, 40, 22…) qui ne suivent
    pas la taille de l'écran (formulaire minuscule en 1366 × 768, champs blancs) ; boutons « Commencer »,
    « Recommencer », « Continuer » en rectangles de la couleur de la barre de progression à 30 % (kaki sur fond noir),
    flèches en caractères (« ↺ », « → », « ▶ ») ; proposition du participant en rouge vif.
  - **Fait (code)** : indicateur de fixation = cercle de progression gris au centre de la cible (comme l'ancien Where
    Is It) ; case sans image : cadre pointillé et « ♪ » ; consigne sonore : « ♪ » géant ; message d'erreur en texte
    brut.
  - **Fait (vu)** : la vidéo s'affiche bien (les premières captures, trop tôt ou trop tard, étaient noires).
  - **Fait (code)** : dossier `data/gazeplayEvalTest` (4 photos et 2 sons de l'ancien `gazeplayEvalTest` retiré le
    27/10/2026 : vélo, voiture, cerise, ours) lu par aucun code.
- **Contraintes** : le format du site est un contrat (rien ne change dans la lecture) ; les résultats (format 1) ne
  changent pas si les **zones des cibles** restent les mêmes (image + petite marge, E8) ; les cartes de chaleur
  contiennent une capture de l'écran : elles montreront le nouvel aspect (format inchangé). Pour une évaluation, ce que
  voit le participant doit rester **neutre** : aucun retour « bonne / mauvaise réponse ».

**Étape 2 (propositions), 29/10/2026**, en attente de validation (✔ / ✘) :

- **A. Corrections** :
  - A1. Tailles proportionnelles à l'écran (textes, boutons, croix), au lieu de pixels fixes.
  - A2. Retirer le dossier inutilisé `data/gazeplayEvalTest`.
- **B. Graphisme** (style des menus, sans toucher aux zones des cibles) :
  - B1. Formulaire participant : carte arrondie, titre en Fredoka, champs arrondis aux couleurs du thème, consigne
    lisible.
  - B2. Bouton « Commencer » et écran « Recommencer / Continuer » : grands boutons arrondis (couleur de surface,
    icône Material Symbols : lecture, recommencer, flèche), contour de la couleur principale ; proposition du
    participant (soutien visuel) : contour épais de la couleur principale au lieu du rouge.
  - B3. Indicateur de fixation : anneau de la couleur principale qui fait le tour de la cible (comme Where Is It et
    Coloring), à la place du cercle gris ; même durée de fixation.
  - B4. Grille : cartes arrondies **dessinées exactement sur la zone de la cible** (aucun changement des résultats) ;
    image sélectionnée et gardée : contour de la couleur principale (neutre, ni vert ni rouge). **Question** : cartes
    claires autour des images, ou garder les images seules sur le fond (plus neutre pour une évaluation) ?
  - B5. Cases et consignes sonores : icône haut-parleur (Material Symbols) au lieu de « ♪ ».
  - B6. Messages (aucune évaluation, évaluation illisible, fin de l'évaluation) dans un bandeau arrondi ; consigne
    texte en Andika, plus lisible.
  - Croix de fixation : gardée telle quelle (forme standard), seulement à l'échelle.
- **C. Fonctionnement** : aucun changement du protocole (défini par le site et les modes de passation).
- **D. Images** : aucune (les images viennent des évaluations) ; vignettes des deux jeux : pas affichées dans les
  menus (le menu des évaluations a ses cartes à icône), rien à refaire.
- **Validées le 29/10/2026 : A et B ✔** ; B4 : cartes claires autour des images (le mainteneur veut voir l'anneau de
  fixation faire le tour de l'image, comme dans Where Is It).
- **Fait (29/10/2026)** :
  - A1 tailles proportionnelles à l'écran (`unit()` : un centième du plus petit côté) ; A2 `data/gazeplayEvalTest`
    retiré (4 photos, 2 sons).
  - B1 formulaire sur une carte arrondie (titre Fredoka, champs arrondis, textes Andika) ; B2 boutons « Commencer »,
    « Recommencer », « Continuer » arrondis à icône (Material Symbols `play_arrow`, `replay`, `arrow_forward`),
    proposition du participant en contour épais de la couleur principale ; B3 anneau de fixation de la couleur
    principale sur le bord de la cible (`EvalTarget`, cercle autour de la croix), à la place du cercle gris ; B4 carte
    claire dessinée **exactement sur la zone de la cible** (image + marge, inchangée), sélection gardée entourée de la
    couleur principale ; B5 haut-parleur au lieu de « ♪ » ; B6 messages et fin dans une carte arrondie.
  - **Données** : zones des cibles, durées de fixation, événements et fichiers de résultats inchangés (format 1) ; les
    cartes de chaleur montrent le nouvel aspect des écrans. Le réglage « taille de la barre de progression » ne
    s'applique plus à Eval (l'anneau suit la taille de la cible).
  - **Changé sans être demandé** : `GameStyle` (`gazeplay-games-commons`) gagne la police Andika, une carte de texte et
    une carte de cible arrondie, réutilisables par les autres jeux.
- **Vérifications** : tests Eval existants (passations complètes, résultats, cartes de chaleur) réussis sans
  changement ; `build` : **556 tests, 0 échec** ; `allGamesTest` GazePlayEval et GazePlayEvalTest réussis ; captures
  `build/game-screenshots/essai1/eval/` (formulaire, grille, anneau `eval-ring-t15/16`, choix). **Reste** : l'essai
  réel du mainteneur (souris et Tobii), en particulier une consigne avec bouton « Commencer », une croix à fixer et
  une case sonore, absentes de l'évaluation de référence.
- **Retours du mainteneur après l'essai (29/10/2026)**, corrigés :
  - La fenêtre « évaluation importée » (et toutes les fenêtres de l'import : erreurs, remplacement, suppression d'une
    évaluation) était une alerte du système : remplacée par `MenuMessageDialog` (nouvelle, au style des menus, dans
    `net.gazeplay.ui.menus`) ; son texte renvoie maintenant à « Lancer une évaluation » du menu des évaluations (il
    parlait encore du jeu GazePlay Eval). Les deux messages du changement de langue utilisent aussi cette fenêtre.
  - Formulaire participant : la touche Retour arrière ne marchait pas (toutes les touches du champ étaient consommées
    pour que la touche « quitter » ne quitte pas le jeu). Le champ traite maintenant ses touches ; c'est le raccourci
    « quitter » (`GameContext`) qui ignore les touches tapées dans un champ de saisie. Test
    `GazeplayEvalTest.shouldEraseALetterOfTheForm`.
  - Le message de JavaFX « Appuyez sur Échap pour quitter le mode plein écran » au début : retiré pour toute
    l'application (`setFullScreenExitHint("")`, menu et parties) ; Échap quitte toujours le plein écran.
  - Tests : `MenuMessageDialogTest` (3) ; capture `3f-message` de `menuScreenshots` ; `build` réussi.

### Frogs Race (`FrogsRace`, code `race`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, `Race.java`, 734 lignes ; pas de variante) : 14 insectes (les
  Biboules volantes) traversent le haut de l'écran ; chaque insecte où entre le regard ou la souris est attrapé (pas
  de durée de fixation : l'entrée suffit, `setOffFixationLengthControl`). Tous les 2 insectes, la grenouille du
  joueur avance de 5 % de la largeur ; il en faut 36 pour arriver (18 sauts). Trois autres grenouilles avancent
  seules, chacune en 15 à 30 s (tirées au hasard). Un tube rouge en bas au centre tourne vers le regard.
- **Défauts vus (faits, lecture du code)** :
  - D1. `Racer.png` (non libre probable, clipart de cutecolors.com) **n'est lu par aucun code** : la grenouille est
    `frogJump.gif`. `Cage.png` (143 octets) est presque vide.
  - D2. Le « tube rouge » (`hand.png`, `handShot.png`) vient du jeu des robots : il ne représente rien ici (ce n'est
    ni une grenouille ni une langue). Les branches `biboule` / `robot` de `updateHand` et `updateCage` sont copiées de
    ce jeu.
  - D3. La grenouille du joueur est **plus petite** que les autres (taille ÷ 15 au lieu de ÷ 10) et de même image :
    rien n'indique laquelle est la sienne, ni sa ligne.
  - D4. Si une autre grenouille arrive la première, la course **recommence sans rien dire** (`endGame` relance) :
    pas de classement, pas de « perdu ». Seule la victoire du joueur a l'animation « Bravo ».
  - D5. Hypothèse (non mesurée) : 36 insectes avant une grenouille qui met 15 à 30 s, soit 1,2 à 2,4 insectes par
    seconde : le joueur perd souvent, surtout au regard.
  - D6. Insectes petits (un trentième de l'écran, environ 36 px en 1080 p au départ) : cibles difficiles au regard.
    Seul le passage bleu → vert des couleurs est joué (jaune, orange, rouge : code mort).
  - D7. **Statistiques fausses** : `newCircle()` compte un but atteint à chaque apparition d'insecte
    (`incrementNumberOfGoalsReached`), en plus de celui de la capture ; le nombre de buts à atteindre n'est jamais
    compté. Corriger change les nombres enregistrés (pas le format des fichiers) : à expliquer dans les notes de
    version, sans `formatVersion` (les fichiers gardent leurs colonnes).
  - D8. À chaque course, `launch()` ajoute 4 écouteurs de taille sur la zone de jeu, jamais retirés (ils
    s'accumulent) ; `dispose()` n'arrête pas les animations des insectes.
  - D9. « Score: » écrit en dur (non traduit), en blanc sans fond ; la vignette du menu montre l'ancien score et
    l'ancien engrenage.
- **Images (licences, `INVENTAIRE_IMAGES.md`)** : `Racer.png` non libre probable (inutilisée) ; `frogJump.gif`
  (grenouille animée de style dessin animé, 372 Ko) et `Background.jpg` (jungle vectorielle de style banque
  d'images) : source inconnue, **à risque** ; Biboules (`Blue`…`Red`, `Flash`) : dessins du projet (kuhlkrein,
  2018), à confirmer ; `frog.WAV` : source inconnue.
- **Propositions** : **toutes validées ✔ par le mainteneur (30/10/2026)** ; Biboules : dessins du projet ✔ ;
  variantes Facile / Normal / Difficile ✔.
  - Images
    - I1. Supprimer `Racer.png`, `Cage.png`, `hand.png`, `handShot.png` (inutiles ou sans rapport).
    - I2. Remplacer `frogJump.gif` par des grenouilles **dessinées pour le projet** (script, comme les potions),
      vues de côté, une couleur par ligne, avec une pose « saut ».
    - I3. Remplacer `Background.jpg` par un décor dessiné pour le projet : une mare vue de face avec quatre lignes
      de nénuphars jusqu'à une ligne d'arrivée.
    - I4. Garder les Biboules volantes (dessins du projet), à confirmer par le mainteneur.
  - Graphisme
    - G1. La grenouille du joueur plus grande que les autres, sur sa ligne marquée de la couleur principale, avec
      « Toi » à côté.
    - G2. À la place du tube rouge, une grenouille en bas au centre qui **tire la langue** vers l'insecte attrapé.
    - G3. Insectes plus gros (au moins un douzième de la hauteur), dans toutes leurs couleurs.
    - G4. Le score dans une pastille au style des menus (`GameStyle.pill`), traduit ; une ligne d'arrivée dessinée.
  - Gameplay
    - P1. Fin de course : le **classement** s'affiche (1ʳᵉ, 2ᵉ…), avec « Bravo » si le joueur gagne, puis une
      nouvelle course.
    - P2. Une course **gagnable** : la vitesse des autres grenouilles dépend d'une variante (Facile, Normal,
      Difficile), Facile par défaut.
    - P3. Corriger les statistiques (D7) : un but atteint par insecte attrapé, un but à atteindre par insecte
      apparu.
    - P4. Corriger D8 (écouteurs retirés, animations arrêtées).
- **Fait (30/10/2026)** :
  - Images : `gradle/images/make_race_images.py` dessine 4 grenouilles de course (vert pour le joueur, bleu,
    orange, violet ; assise et en saut), la grenouille de la rive (de face, bouche ouverte) et la mare (ciel et
    arbres en haut, rive, eau). Retirés : `Racer.png`, `Cage.png`, `hand.png`, `handShot.png`, `frogJump.gif`,
    `Background.jpg`. Crédits : `gradle/images/README.txt`, licence affichée, inventaire.
  - `Race.java` réécrit : 8 insectes (un douzième de la hauteur, couleur au hasard) qui volent dans le ciel ; à
    l'entrée du regard ou de la souris, la grenouille de la rive tire la langue, ramène l'insecte, et la grenouille
    du joueur saute (20 insectes pour arriver). 4 couloirs sur la mare, celui du joueur bordé de la couleur
    principale avec « Toi » ; ligne d'arrivée à damier ; pastille « Insectes attrapés : n ». Les autres grenouilles
    sautent seules, à un rythme tiré au hasard selon la variante (`RaceVariant` : Facile 90 à 110 s, Normal 60 à
    75 s, Difficile 40 à 50 s ; **valeurs non mesurées, à ajuster après l'essai réel**). Fin de course : carte du
    classement 3,5 s, « Bravo » si le joueur gagne, puis nouvelle course. Plus d'écouteurs de taille ni de classes
    `Target` et `Point`.
  - Statistiques : un but à atteindre par insecte apparu, un but atteint par insecte attrapé (D7). Les colonnes
    des fichiers ne changent pas ; les nombres des parties de Frogs Race d'avant le 30/10 sont faux (buts atteints
    comptés aussi à l'apparition) : à dire dans les notes de version. Une partie enregistrée avant les variantes se
    rejoue en Facile.
  - Traductions (français, anglais) : `RaceYou`, `RaceCaught`, `RaceWon`, `RaceLost`, `RaceFrog_*` ; variantes :
    `Easy`, `Normal`, `Hard` (déjà traduites). Vignette du menu refaite (photo du jeu sans fenêtre, 960 × 540).
- **Vérifications** : `RaceTest` (7 : départ, nouvelle course dans la même zone, capture unique, victoire,
  défaite, variantes, fin de partie ; chaque test arrête sa course, dont les minuteries débordaient sur les tests
  suivants) ; `build` : **575 tests, 0 échec** ;
  `allGamesTest -PallGamesOnly=FrogsRace -PallGamesRepeat=5` réussi ; photos du jeu sans fenêtre (couloirs,
  langue, classement). **Reste** : l'essai réel du mainteneur (souris et Tobii), en particulier la difficulté des
  trois variantes.
- **Commit** : `67f6b871b` (30/10/2026), `feat(race): revue de Frogs Race`.

### Horses et Horses Simplified (`Horses`, `Horses Simplified`, code `horses`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, `Horses.java` 451 lignes, `Pawn`, `Square` et 4 sous-classes) :
  les petits chevaux, de 2 à 4 équipes (variante : nombre de joueurs). On choisit les équipes en fixant les grands
  ronds des écuries ; puis, à tour de rôle, on fixe le bouton du dé au centre, et l'on fixe le pion à déplacer. Un 6
  sort un pion de l'écurie et fait rejouer ; un pion qui tombe sur un pion adverse le renvoie à l'écurie ; il faut
  le chiffre exact pour chaque case de l'échelle finale ; **le premier pion arrivé gagne**. Horses : plateau de
  15 cases de côté, 4 pions par équipe, échelle de 6 ; Simplified : plateau de 9, 1 pion par équipe, échelle de 3
  (avec le même dé de 1 à 6).
- **Défauts vus (faits, lecture du code)** :
  - D1. Toutes les équipes sont jouées par la même personne (pas d'adversaire automatique) : seul devant
    l'oculomètre, le joueur joue 2 à 4 équipes contre lui-même.
  - D2. **Dé déséquilibré** (`DiceRoller`, commun à Horses, Dice et Goose Game) : la face dépend des angles
    arrondis au quart de tour ; calcul : 3 et 4 sortent 1 fois sur 4 chacun, 1, 2, 5 et 6 une fois sur 8
    (hypothèse : angles tirés uniformément ; à vérifier par un test). Un 6, nécessaire pour sortir, est donc plus
    rare que prévu.
  - D3. Un pion qui tombe sur un pion de **sa** équipe recule d'une case (`Square.pawnLands`) : le recul rappelle
    `pawnLands`, donc `endOfTurn()` est appelé **deux fois** (une équipe saute son tour) ; la case quittée oublie le
    premier pion.
  - D4. On ne peut pas sortir un pion si un pion **adverse** est sur la case de départ (la règle habituelle le
    renverrait à l'écurie).
  - D5. Messages en gros texte blanc cerné de noir (un dixième de la hauteur) qui se superposent au plateau ;
    clés en anglais traduites par `Multilinguism` ; **en biélorusse, « %s team's turn » est traduit avec `%d` :
    `String.format` plante** au premier tour dans cette langue.
  - D6. Statistiques : un but atteint par lancer de dé, aucun but à atteindre.
  - D7. Fond noir, couleurs pures (bleu, jaune, rouge, vert saturés) ; le pion à choisir n'est montré que par
    `selector.png` posé dessus ; l'équipe dont c'est le tour n'est dite que par un message de 4 s et la couleur du
    bouton du dé. Vignettes du menu anciennes (fond noir).
- **Images (licences)** : plateaux `horsiesboard.png`, `horsiesBoardSimplified.png`, `selector.png` et boutons du
  dé `rollImages/roll*.png` : ajoutés par Teo Blachere en 2019, source non dite (« à confirmer » ; les deux dés
  des boutons ressemblent à un clipart) ; Biboules : dessins du projet (gardées).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. Redessiner les deux plateaux par script **à partir de `positions.json`** (les cases tombent exactement
      où le jeu les attend), dans des couleurs plus douces, au style des menus.
    - I2. Remplacer les boutons du dé (`roll*.png`) par un dé dessiné (script) sur un rond de la couleur de
      l'équipe ; remplacer `selector.png` par un anneau dessiné par le jeu.
    - I3. Garder les Biboules (pions).
  - Graphisme
    - G1. Un bandeau en haut au style des menus : « Au tour de l'équipe bleue » avec sa Biboule, toujours visible ;
      les autres messages dans une carte (`GameStyle.card`), plus petits, au lieu du gros texte sur le plateau.
    - G2. Les pions jouables entourés d'un anneau de la couleur de l'équipe qui pulse ; l'écurie de l'équipe en
      cours mise en avant.
    - G3. Fond de la couleur de fond des menus (plus de noir) ; nouvelles vignettes.
  - Gameplay
    - P1. **Adversaires automatiques** : le joueur joue une équipe, l'ordinateur les autres (il lance le dé et
      choisit un pion tout seul, avec une pause pour qu'on suive) ; variantes « 1 joueur contre 1, 2 ou 3
      ordinateurs » en plus de « 2, 3 ou 4 joueurs » (à confirmer).
    - P2. Corriger D3 : un pion ne peut pas s'arrêter sur un pion de son équipe (le mouvement n'est pas proposé).
    - P3. Dé équilibré (D2), corrigé dans `DiceRoller` : profite aussi à Dice et Goose Game (à vérifier dans ces
      jeux à leur revue).
    - P4. Sortir un pion sur la case de départ occupée par un adversaire le renvoie à l'écurie (D4).
    - P5. Messages par le traducteur du jeu, `%d` biélorusse corrigé (D5).
    - P6. Statistiques : un but à atteindre par tour du joueur, atteint quand il a joué (à confirmer).
- **Décisions du mainteneur (30/10/2026)** : tout validé ✔ ; garder les deux sortes de variantes (contre
  l'ordinateur et à plusieurs) ; le premier pion arrivé gagne.
- **Fait (30/10/2026)** :
  - Images : `gradle/images/make_horses_images.py` dessine les deux plateaux d'après `positions.json` et
    `positionsSimplified.json` (les cases tombent où le jeu pose les pions ; couleurs adoucies, numéros de
    l'échelle, flèches de départ et de sortie) et les 4 boutons du dé ; `selector.png` retiré. Crédits :
    `gradle/images/README.txt`, licence affichée, inventaire.
  - Variantes (`HorsesVariant`, un seul générateur pour les deux jeux) : 1 joueur contre 1, 2 ou 3 ordinateurs ;
    2, 3 ou 4 joueurs. Une partie enregistrée avant (variante « nombre de joueurs ») se rejoue en « 2 joueurs ».
  - Ordinateur : il prend les équipes restantes après le choix des personnes, lance le dé et choisit un pion après
    une pause de 1,2 s (priorité : renvoyer un pion adverse à l'écurie, avancer sur l'échelle, sortir un pion, puis
    le pion le plus avancé) ; son pion choisi a l'anneau un instant.
  - Règles : un déplacement qui finirait sur un pion de son équipe n'est pas proposé (`Pawn.destination`, calcul
    sans effet qui suit exactement le déplacement réel, `Square.peekDestination`) ; plus de double fin de tour ;
    sortir un pion sur un adversaire le renvoie à l'écurie ; l'action d'un pion est donnée une fois au bouton (les
    écouteurs s'accumulaient à chaque tour).
  - **Dé équilibré** (`DiceRoller`, commun à Horses, Dice et Goose Game) : la face est tirée d'abord (1 chance sur
    6), puis les quarts de tour qui la montrent. **Impact** : le rejeu d'une partie de ces trois jeux enregistrée
    avant le 30/10 ne retrouve plus les mêmes lancers (même graine, autre tirage) ; à dire dans les notes de
    version. Les faces de Dice et Goose Game sont à regarder à leur revue.
  - Graphisme : fond des menus ; à gauche, la Biboule et le nom de l'équipe en cours (« (ordinateur) » s'il y a
    lieu), son écurie entourée ; messages dans une carte à droite ; pions jouables entourés d'un anneau de leur
    couleur qui pulse. Vignettes refaites (photos du jeu sans fenêtre).
  - Statistiques : un but à atteindre par tour d'une personne, atteint quand elle lance le dé (avant : un but
    atteint par lancer, sans but à atteindre) ; colonnes des fichiers inchangées.
  - Traductions : nouveaux messages `Horses*` (français, anglais) ; les anciens messages en anglais du jeu sont
    retirés, dont celui traduit avec `%d` en biélorusse (plantage).
- **Retour du mainteneur (30/10/2026) : une seule carte au menu**, la version dans la fenêtre des variantes.
  Fait : « Horses Simplified » n'est plus une entrée du catalogue (76 entrées) ; `HorsesVariant` a 12 valeurs
  (les 6 manières de jouer × plateau normal ou simplifié `SIMPLIFIED_…`) ; `GameVariantDialog` range les variantes
  de Horses par plateau, avec **deux boutons radio en bas** (« Version normale », « Version simplifiée ») ;
  libellés raccourcis (« Contre 1 ordinateur ») pour tenir dans les boutons. Statistiques des deux plateaux dans le
  dossier `horses` (l'ancien dossier `horsesSimplified` reste tel quel). Rejeu : un fichier enregistré sous
  « Horses Simplified » est rejoué comme Horses, plateau simplifié, 2 joueurs (`ReplayingGameFromJson`). Vignette
  `horsesSimplified.png` retirée. Test : `GameVariantDialogTest.shouldChooseTheBoardOfHorsesUnderTheVariants` ;
  capture `3a-variantes-petits-chevaux` de `menuScreenshots`.
- **Vérifications** : `build` : **586 tests, 0 échec** (avant la fusion des deux cartes), **587** après ; `HorsesTest` (9), `DiceRollerTest` (2, dont 6 000
  lancers : chaque face entre 880 et 1 120) ;
  `allGamesTest` Horses et Horses Simplified, 5 fois chacun ; photos du jeu sans fenêtre. **Reste** : l'essai réel
  du mainteneur (souris et Tobii), en particulier une partie contre l'ordinateur.
- **Commit** : avec l'aide des jeux, le 30/10/2026 (`feat(horses)`), à la demande du mainteneur.

### Famille Memory (6 entrées, code `memory`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code : `Memory` 394 lignes, `MemoryCard` 383, 6 lanceurs, 6 specs, un seul
  générateur de variantes) : des cartes face cachée (Memory, Memory Letters, Memory Numbers) ou face visible (Open
  Memory, Open Memory Letters, Open Memory Numbers) ; une fixation retourne (ou choisit) une carte ; deux cartes
  identiques sont retirées, sinon elles se retournent après 1 s. Quand il ne reste plus de carte : « Bravo », puis une
  nouvelle manche (pas de fin). Variantes : Normal 2×2, 2×3, 3×2, 3×4, 4×3, et Dynamique (départ 2×2 ; la taille
  change selon le nombre d'essais). Images : Memory lit les dossiers partagés `magiccards` puis `default` du profil,
  sinon les images par défaut (`data/common/default`) ; lettres et chiffres : `data/common/letters`, `numbers`.
- **Défauts vus (faits, lecture du code)** :
  - D1. **Dynamique** : la règle « 3 manches de suite » compare 3 fois le **même** nombre d'essais (celui de la
    manche qui vient de finir, `MemoryCard` lignes 173-178), au lieu des 3 dernières manches.
  - D2. Une touche **Espace** sur une carte qui a le focus retire toutes les cartes et vide l'écran
    (`gameInstance.dispose(); gameContext.clear()`), sans fin de partie ni retour au menu : écran vide.
  - D3. Statistiques : un but atteint par paire trouvée, aucun but à atteindre (le nombre de paires n'est jamais
    compté).
  - D4. Le dos de carte (`red-card-game.png`) est rechargé depuis le disque à chaque carte retournée ou remise.
  - D5. Aucun retour visible quand une paire est bonne (les cartes disparaissent) ou fausse (elles se retournent) ;
    dans les versions visibles, une paire fausse ne se voit pas du tout.
  - D6. Style d'origine : fond noir, cartes collées en hauteur, indicateur de fixation gris au centre de la carte.
    Vignettes : 6 anciennes (celle de Memory montre une infirmière, sans rapport).
  - D7. Images de l'utilisateur lues dans les anciens dossiers partagés `magiccards` et `default` (à remplacer par
    une variante, règle du 29/10/2026).
- **Images (licences)** : dos de carte `magiccards/images/red-card-game.png` : source inconnue (2017 ; aussi utilisé
  par Magic Cards) ; images par défaut : 10 animaux OpenMoji (libres) et **9 à risque** (chouette, crabe, écureuil,
  hérisson, lapin, méduse, poulpe, renard, tortue : « Free Fundation », source non dite ; aussi utilisées par Blocks,
  Casse-Brique, Ninja, Scratch Card, Magic Cards, Cream Pie, Portrait Bubbles) ; lettres et chiffres : JPG noirs sur
  blanc sans originalité (OK).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. Un dos de carte dessiné pour le projet (script), au style des menus, à la place de `red-card-game.png`
      dans Memory (Magic Cards le garde jusqu'à sa revue).
    - I2. Remplacer les 9 images par défaut à risque par des animaux OpenMoji (CC BY-SA 4.0, même méthode que les
      10 déjà remplacés, `make_default_images.py`), mêmes noms de fichiers : profite aux 8 jeux qui les utilisent.
    - I3. Lettres et chiffres dessinés par le jeu (police Fredoka, grande, centrée sur la carte) au lieu des JPG.
    - I4. « Mes images » : une variante `UserImagesVariant` (dossier `files/images/Memory`), au lieu des dossiers
      partagés `magiccards` et `default` (pas de migration).
  - Graphisme
    - G1. Fond des menus ; cartes arrondies avec un espace entre elles (`GameStyle.targetCard`), dos dessiné, face
      sur carte blanche.
    - G2. Anneau de fixation de la couleur principale autour de la carte (comme Where Is It), au lieu de
      l'indicateur gris.
    - G3. Retournement animé ; paire juste : contour vert, puis les deux cartes s'effacent ; paire fausse : contour
      « danger » un instant, puis elles se retournent (et dans les versions visibles, le même contour).
    - G4. Une pastille « Paires : 2 / 6 » ; nouvelles vignettes.
  - Gameplay
    - P1. **Une seule carte au menu « Memory » au lieu de 6** (comme les petits chevaux) : dans la fenêtre des
      variantes, en haut le contenu (Images, Lettres, Chiffres, Mes images), les tailles et Dynamique au milieu, en
      bas « Cartes cachées » ou « Cartes visibles ».
    - P2. Corriger D1 (les 3 dernières manches) et D2 (plus de touche Espace).
    - P3. Laisser la deuxième carte visible 1,5 s au lieu de 1 s avant qu'elle se retourne (à confirmer).
    - P4. Statistiques : un but à atteindre par paire de la manche, atteint quand elle est trouvée (colonnes
      inchangées).
- **Décisions du mainteneur (30/10/2026)** : tout validé ✔ ; une seule carte ; 1,5 s ; lettres dessinées.
- **Fait (30/10/2026)** :
  - Une seule entrée « Memory » (les 5 autres retirées du catalogue : 71 entrées) ; variantes `GroupedGameVariant`
    (nouvelle, `gazeplay-games-commons`, relue par `IGameVariant.toGameVariant`) : en haut Images, Lettres,
    Chiffres, Mes images ; les tailles 2 × 2, 2 × 3, 3 × 2, 3 × 4, 4 × 3 et Dynamique ; en bas Cartes cachées,
    Cartes visibles (48 variantes). `GameVariantDialog` range toute variante à deux groupes ainsi (boutons radio en
    haut et en bas). « Mes images » : dossier `files/images/Memory` (`UserImagesVariant.folderOf` connaît la
    nouvelle variante), ligne dans les réglages ; plus de lecture des dossiers partagés `magiccards` et `default`.
  - `Memory` et `MemoryCard` réécrits : fond des menus, cartes arrondies espacées (jusqu'au carré quand il y a de
    la place), dos dessiné par le jeu (couleur principale, cadre, « ? ») au lieu de `red-card-game.png` (gardé pour
    Magic Cards), lettres et chiffres en Fredoka, anneau de fixation de la couleur principale, retournement animé,
    cadre vert pour une paire, « danger » pour une paire fausse (aussi en cartes visibles), 1,5 s avant de se
    retourner, pastille « Paires : n / N ». Plus de touche Espace. Dynamique : les 3 dernières manches.
  - Statistiques : un but à atteindre par paire de la manche, atteint quand elle est trouvée (colonnes inchangées).
  - Images par défaut : chouette, crabe, écureuil, hérisson, lapin, méduse, poulpe redessinés d'après OpenMoji
    (mêmes noms) ; `renard.png` et `tortue.png` (un second renard, une seconde tortue) remplacés par `penguin.png`
    et `frog.png` (`make_default_images.py`). Les versions sans fond du labyrinthe ne changent pas. Crédits : licence
    affichée, `gradle/images/README.txt`, inventaire.
  - Rejeu : les parties des 6 anciens jeux sont rejouées par Memory avec la variante correspondante
    (`ReplayingGameFromJson.formerGame`) ; l'ordre des cartes d'une ancienne partie n'est pas retrouvé (autre tirage).
  - Vignette refaite ; les 5 autres vignettes retirées. Aide du jeu (`data/help/games/Memory-*.txt`).
- **Vérifications** : `build` : **607 tests, 0 échec** ; `MemoryTest` (7), `ReplayingGameFromJsonTest`, `GameHelpTest` ;
  `allGamesTest -PallGamesOnly=Memory -PallGamesRepeat=5` réussi ; photos du jeu sans fenêtre.
  **Reste** : l'essai réel du mainteneur.
- **Commit** : le 30/10/2026, avec la nouvelle fenêtre des variantes des jeux revus (`feat(memory)`), à la demande du mainteneur.

### Sounds Of Life (`SoundsOfLife`, code `soundsoflife`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, 371 lignes) : une scène (Ferme, Jungle, Savane, Mer) avec des
  animaux ou des objets ; une fixation sur l'un d'eux joue un de ses sons (à tour de rôle) et le fait se balancer.
  Rien à gagner : on explore (un but atteint par son joué). Scènes décrites par `elements.json` (image, échelle,
  position, sons).
- **Défauts vus (faits, lecture du code)** :
  - D1. **Rejeu** : `replayGame` n'utilise pas la graine enregistrée (nouveau tirage) : le premier son de chaque
    élément change au rejeu.
  - D2. **Fond désactivé dans les réglages** : les éléments sont placés d'après le fond, qui n'est alors jamais
    placé ; ils sont décalés vers le haut et la gauche (sauf si l'image tient exactement la zone de jeu).
  - D3. Une sortie du regard avant toute entrée lève une exception (`progressTimeline` nul) ; indicateur de
    fixation gris au centre de l'image.
  - D4. Pas de nom de l'animal : on entend le son sans savoir ce que c'est (pas d'aide à l'apprentissage).
  - D5. `farm/background.jpg` n'est pas utilisé (le jeu lit `background.png`) ; les sources `.svg` de la mer sont
    livrées avec les `.png`.
- **Images et sons (licences)** : **à risque** : décors et animaux de la ferme, de la jungle et de la savane (Téo
  Blachère, 2019, sans source ; style de banque d'images, le décor de la ferme porte le nom de Danilo Sanino,
  illustrateur de Shutterstock, dans ses métadonnées). Mer (Iluvrog, 2021, avec ses `.svg`) : source non dite, peut-être
  dessinée par l'auteur. **Sons non inventoriés** : 47 `.wav` sans source (la vache, le cochon, le mouton, l'éléphant, le
  lion, le serpent, le coq ou la poule ont un équivalent libre déjà vérifié dans Where Is The Sound).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images et sons
    - I1. Animaux d'OpenMoji (CC BY-SA 4.0), comme Memory : vache, poule, cheval, cochon, coq, mouton ; éléphant,
      grenouille, léopard (pour le jaguar), singe, perroquet, serpent ; crocodile, lion, et pour la hyène et
      l'autruche (pas d'émoji) : zèbre et girafe, ou d'autres animaux de la savane qui ont un son libre.
    - I2. Décors dessinés pour le projet (script, comme la mare de Frogs Race) : ferme, jungle, savane ; la mer
      aussi, ou gardée si son auteur confirme qu'il l'a dessinée.
    - I3. Sons : reprendre ceux de Where Is The Sound déjà vérifiés (vache, poule, cochon, mouton, éléphant, lion,
      serpent) ; chercher les autres sur Commons ou La Sonothèque (CC0, CC BY), crédités ; inventaire des sons du jeu.
  - Graphisme
    - G1. Anneau de fixation de la couleur principale autour de l'animal, à la place de l'indicateur gris.
    - G2. Quand le son joue, le **nom de l'animal** s'affiche dans une pastille au-dessus de lui (traduit).
  - Corrections
    - P1. D1 (graine du rejeu), D2 (placement sans fond), D3 (exception) ; retirer `farm/background.jpg` et les
      `.svg` inutiles.
- **Questions au mainteneur** : I1 (hyène et autruche : les remplacer par d'autres animaux ?) ; I2 (la mer :
  redessiner, ou garder ?) ; G2 (le nom affiché vous convient-il ?)
- **Réponses du mainteneur (30/10/2026)** : tout est validé ; zèbre et girafe ; la mer redessinée ; le nom affiché
  convient.
- **Fait (30/10/2026)** :
  - Images (I1, I2) : les 4 décors dessinés par `gradle/images/make_soundsoflife_images.py` (ferme avec grange et
    clôture, jungle, savane avec point d'eau, fond marin), qui place aussi les animaux et écrit `elements.json` ;
    21 animaux OpenMoji (CC BY-SA 4.0). Scènes : Ferme (mouton, vache, cheval, cochon, coq, poule), Jungle (singe,
    perroquet, éléphant, léopard, serpent, grenouille), Savane (lion, zèbre, girafe, crocodile), Mer (dauphin,
    baleine, poisson, pieuvre, crabe). Tous les anciens fichiers retirés (images, `.wav`, `.svg`, `background.jpg`).
  - Sons (I3) : un son MP3 par animal (raccourci à 8 s, normalisé, mono) : 15 de Wikimedia Commons (CC0, domaine
    public, CC BY 3.0/4.0, CC BY-SA 4.0), 6 repris de Where Is The Sound (vache, cochon, mouton, éléphant, serpent,
    lion). Auteur, licence et page de chacun dans `data/soundsoflife/CREDITS.txt`, livré avec le jeu ; crédits aussi
    dans `gradle/images/README.txt` et `gradle/templates/licence.txt`. **Les 15 sons de Commons ont été choisis
    d'après leur titre et leur description, sans écoute** : à écouter (le poisson, la pieuvre et le crabe sont des
    bruits d'eau et de bulles, le dauphin un sifflement accéléré, la girafe un bourdonnement grave).
  - G1 : anneau de la couleur principale autour de l'animal, qui se remplit pendant la fixation.
  - G2 : le nom de l'animal (traduit, 21 clés `SoundsOfLife_<animal>` en français et en anglais) s'affiche 3 s dans
    une pastille au-dessus de lui quand le son joue.
  - P1 : le rejeu reprend la graine de la partie (D1) ; les animaux sont placés au même endroit avec ou sans fond (D2) ;
    plus d'exception quand le regard sort avant d'être entré (D3).
  - Aide (`SoundsOfLife-fra.txt`, `-eng.txt`) : principe, comment jouer, rien à gagner, les 4 scènes. Vignette du
    menu : capture de la ferme (960 x 540). Inventaire : `inventaire-images.csv` et `INVENTAIRE_IMAGES.md` à jour.
- **Vérifications** : `SoundsOfLifeTest` (6 tests : fichiers de chaque scène, placement sans fond, mêmes sons au
  rejeu et nom affiché) ; `GameHelpTest` avec `SoundsOfLife` ; captures sans fenêtre de la ferme et de la mer
  (anneau et pastille vus) ; `allGamesTest -PallGamesOnly=SoundsOfLife -PallGamesRepeat=5` réussi ; `build` :
  **617 tests, 0 échec**, SpotBugs sans alerte. Reste à faire par le mainteneur : jouer, et écouter les sons.
- **Retour du mainteneur (2/11/2026)** : quand on choisit un animal pendant qu'un autre son joue, les deux sons se
  superposent. Deux choix : bloquer toute sélection pendant un son, ou couper le son en cours avant le suivant.
  Fait (code) : `SoundManager.add` met le son en file, `ForegroundSoundsUtils.playSound` le joue sans arrêter le
  précédent (`stopSound` existe et arrête le dernier son joué). Recommandation : couper le son en cours (au regard,
  une sélection bloquée ne se voit pas). **Décision du mainteneur (2/11/2026) : couper le son en cours.**
- **Fait (2/11/2026)** : `SoundManager.replace` (vide la file, arrête le son en cours, met le nouveau) ; un animal
  choisi arrête l'animal entendu avant (balancement arrêté, nom caché, `SoundMakingEntity.quiet`). Tests :
  `SoundManagerTest.shouldForgetTheSoundsWaitingWhenASoundReplacesThem`, `SoundsOfLifeTest` (chaque son remplace le
  précédent, seul le nom du dernier animal reste affiché).

### Spot The Differences (`SpotDifference`, code `spotthedifferences`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, 367 lignes, et capture « avant » sans fenêtre) : deux images côte à
  côte (7 paires carrées : maison, camping, concert, cuisine, oasis, mer, neige), avec 4 à 6 différences chacune,
  décrites dans `instances.json` (centre et rayon). Regarder une différence 1 s l'entoure de rouge sur les deux images,
  avec un son. Toutes trouvées : « Bravo », puis l'écran des statistiques (la partie s'arrête après une image). Un
  bouton flèche passe à l'image suivante. Pas de variante.
- **Défauts vus (faits)** :
  - D1. **Détection** : la différence n'est trouvée que sur un événement de **mouvement** après 1 s. Une souris
    immobile ne la trouve jamais ; avec le regard, depuis le format v4 une position identique n'est plus transmise,
    donc un regard parfaitement fixe peut aussi bloquer. La durée (1 s) est fixe : la durée de fixation des réglages
    n'est pas lue.
  - D2. **Mise en page** (capture) : le bouton flèche cache le score (« 0/4 ») ; les images n'occupent que les 2/3 de
    la hauteur ; trois lignes de texte blanc en grand (50 et 70 points) en bas ; police par défaut ; « trouvées: » sans
    espace avant les deux-points.
  - D3. **Toujours la même image en premier** (la maison) : la série recommence au début à chaque partie ; la partie
    s'arrête après une seule image trouvée.
  - D4. Les différences trouvées sont entourées de **rouge** (couleur de l'erreur dans les autres jeux revus).
  - D5. Aucun retour pendant la fixation : on ne sait pas si l'on regarde assez longtemps (voir la proposition G2).
  - D6. La liste des zones d'intérêt (`targetAOIList`) n'est jamais vidée entre deux images (hypothèse : les zones de
    l'image précédente restent dans les statistiques, avec des coordonnées qui ne correspondent plus) ; une
    différence sautée avec la flèche n'a pas d'heure de fin.
- **Images et sons (licences)** : **à risque**. Les 14 images (1725 x 1725, 2019) sont des scènes faites d'icônes plates
  assemblées et retouchées dans GIMP (métadonnées : GIMP 2.10, aucun auteur). Hypothèse : icônes d'une banque
  (style Flaticon/Freepik), source non dite. `next.png` (GIMP, sans source) ; `ding.wav` (son, sans source). Deux PNG
  (`camp2`, `snow2`) ont un profil de couleur abîmé (JavaFX les lit, Pillow les refuse).
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images et sons
    - I1. **Scènes composées par script** (comme Sounds Of Life) : un décor dessiné et des objets OpenMoji (CC BY-SA
      4.0). Le script fait lui-même les différences (objet en moins, objet remplacé, couleur changée) et écrit
      `instances.json` : les cercles tombent toujours juste. 8 scènes : maison, camping, concert, cuisine, oasis, mer,
      neige, ferme.
    - I2. Flèche « suivant » : icône des menus (Material Symbols, Apache-2.0) dans un bouton rond de la couleur
      principale. Son de réussite : synthétisé par script (clochette), sous la licence du projet.
  - Graphisme
    - G1. Images plus grandes (environ 85 % de la hauteur), côte à côte, avec un liseré. En haut, une seule pastille :
      « Différences : 2/5 ». La flèche dans le coin en bas à droite, sans rien cacher.
    - G2. Pendant la fixation, **rien ne s'affiche sur la différence** (un anneau dirait où elle est). La différence
      trouvée est entourée de **vert** sur les deux images, avec un petit effet d'apparition.
  - Gameplay
    - P1. D1 : détection par minuterie dès l'entrée (souris ou regard), avec la **durée de fixation des réglages**.
    - P2. **Enchaîner les images** : « Bravo », puis l'image suivante. Ordre tiré au hasard (avec la graine, pour le
      rejeu) ; le jeu ne s'arrête pas tout seul, comme Memory.
    - P3. **Indice** : après 20 s sans trouver, une différence restante clignote doucement sur les deux images.
    - P4. Variantes **Facile / Normal / Difficile** : 3, 5 ou 7 différences par image, plus petites en Difficile
      (faites par le script de I1 pour chaque scène).
  - Corrections
    - P5. D6 : zones d'intérêt rangées par image. Aucun changement du format des résultats : `info-game.csv` et le
      JSON restent les mêmes.
- **Questions au mainteneur** : I1 (scènes redessinées : d'accord avec 8 scènes ?) ; P3 (indice après 20 s, ou
  autre durée, ou pas d'indice ?) ; P4 (les 3 niveaux de difficulté vous conviennent-ils ?)
- **Réponses du mainteneur (30/10/2026)** : tout est validé ; 8 scènes ; indice après 20 s ; les 3 niveaux.
- **Fait (30/10/2026)** :
  - Images (I1) : `gradle/images/make_spotdifferences_images.py` dessine 8 décors (maison, camping, concert, cuisine,
    oasis, mer, neige, ferme) et y pose des objets OpenMoji (CC BY-SA 4.0) ; il fait lui-même les différences de
    chaque niveau (objet retiré, remplacé, ou d'une autre couleur : les plus grands objets en Facile, les plus petits
    en Difficile) et écrit `instances.json` (nouvelle forme : une première image par scène, puis pour chaque niveau la
    seconde image et ses différences). Images de 1000 x 1000 en palette : 32 images, 1,1 Mo en tout (contre 3,4 Mo
    pour les 15 anciennes). Les anciennes images et `next.png` sont retirées.
  - Son (I2) : `ding.wav` synthétisé par `gradle/images/make_spotdifferences_sound.py` (deux notes de clochette).
    Flèche : bouton rond des jeux revus (`GazeRoundButton`, icône `arrow_forward`).
  - G1 : images de 76 % de la hauteur, encadrées, côte à côte ; pastille « Différences : 2 / 5 » en haut (clé
    `SpotDifferencesFound`) ; flèche sous l'image de droite, dans le coin, sans rien cacher.
  - G2 : rien pendant la fixation ; une différence trouvée est entourée de vert sur les deux images (apparition).
  - P1 : minuterie dès l'entrée de la souris ou du regard, durée de fixation des réglages, sans besoin de bouger.
  - P2 : « Bravo », puis l'image suivante ; ordre tiré avec la graine de la partie (le rejeu montre les mêmes images),
    chaque scène une fois avant de recommencer ; plus d'écran des statistiques après une image.
  - P3 : après 20 s sans trouver, une différence restante clignote (couleur principale) sur les deux images.
  - P4 : variantes Facile / Normal / Difficile (3, 5, 7 différences) ; une partie enregistrée avant les variantes
    se rejoue en Normal.
  - P5 : en quittant une image (flèche, fin, sortie du jeu), les zones d'intérêt des différences non trouvées
    reçoivent une heure de fin. Rien n'est écrit sur disque pour ces zones : aucun changement de format.
  - Aide (`SpotDifference-fra.txt`, `-eng.txt`), vignette (capture de l'oasis, 960 x 540), crédits
    (`gradle/images/README.txt`, `gradle/templates/licence.txt`), inventaire à jour.
- **Vérifications** : `SpotTheDifferencesTest` (9 tests : fichiers et nombre de différences de chaque niveau,
  variantes, fixation sans bouger et sortie avant la fin, image suivante après « Bravo », indice, même ordre au rejeu,
  zones d'intérêt terminées) ; `GameHelpTest` avec `SpotDifference` ; captures sans fenêtre des 3 niveaux ;
  `allGamesTest -PallGamesOnly=SpotDifference -PallGamesRepeat=5` réussi ; `build` : **627 tests, 0 échec**,
  SpotBugs sans alerte. Reste à faire par le mainteneur : jouer (souris et Tobii), et dire si les différences de
  Difficile sont trop dures ou trop faciles.
- **Retour du mainteneur (2/11/2026)** : le jeu convient ; ajouter dans la fenêtre des variantes une option **avec ou
  sans l'indicateur de fixation visible**.
- **Fait (2/11/2026)** :
  - Variantes `GroupedGameVariant` (`SpotTheDifferencesVariantGenerator`) : un seul groupe, sous-groupe « Sans
    anneau de fixation » (en premier : le jeu tel que validé le 30/10) ou « Avec anneau de fixation » (clés
    `SpotDifferencesRingHidden`, `SpotDifferencesRingShown`), valeur Facile / Normal / Difficile. Forme enregistrée :
    `GroupedGameVariant:SpotDifference:<anneau>:<Easy|Normal|Hard>:-` au lieu de `EnumGameVariant:…`. Le jeu n'est
    pas dans la liste des jeux rejouables ; sans variante lisible, il se lance en Normal sans anneau.
  - Avec l'anneau : un arc de la couleur principale se remplit autour du cercle regardé (image de gauche ou de
    droite) pendant la durée de fixation, et disparaît quand le regard sort ou que la différence est trouvée.
  - Fenêtre des variantes : un groupe (ou un sous-groupe) seul n'a plus de ligne de boutons radio ; ici, seule la
    ligne de l'anneau s'affiche sous les niveaux. Memory n'est pas touché (deux lignes).
  - Aide du jeu (français et anglais) : une ligne sur l'option.
  - Tests : `SpotTheDifferencesTest` (anneau absent par défaut ; présent, placé sur le cercle regardé, caché à la
    sortie et une fois trouvée ; 6 variantes relues), `GameVariantDialogTest.shouldShowNoSwitchForAGroupAlone`.

### Pet (`Pet`, code `pet`) — 30/10/2026

- **Ce qu'il fait / variantes** (lecture du code, 1 243 lignes, et captures « avant » sans fenêtre des 4 écrans) :
  on s'occupe d'une Biboule volante. Trois barres de besoins (bleue, rouge, verte, sans nom ni icône) se vident avec
  le temps. Quatre boutons ronds (fixation) ouvrent une pièce : **lit** = maison (caresser la Biboule avec la main),
  **couverts** = cuisine (regarder le bol remplit la cuillère, l'amener à la bouche nourrit : barre rouge),
  **douche** = salle de bain (le pommeau suit le regard, l'eau sur la Biboule lave : barre bleue), **coureur** = parc
  (la Biboule vole, la toucher avec le gant lance une balle : barre verte). Un objet (main, cuillère, pommeau, gant)
  suit le regard ou la souris dans la pièce. Pas de fin, pas de variante.
- **Défauts vus (faits, lecture du code et captures)** :
  - D1. **Barres incompréhensibles** : aucune icône ni nom, couleurs sans lien avec les boutons ; le bouton de la
    maison est un **lit**. Rien ne se passe quand une barre est vide : la Biboule n'est jamais triste.
  - D2. **Vitesse** : la barre bleue perd une case toutes les 1,5 s environ (vide en une trentaine de secondes), la
    rouge toutes les 2,5 s, la verte toutes les 4,5 s : le bain est à refaire sans arrêt.
  - D3. **Plantage** : nourrir la Biboule quand la barre rouge (ou verte) est vide ou presque lit la case d'indice
    -1 (`refill`) : exception dans le gestionnaire (hypothèse : la barre ne se remplit plus ensuite).
  - D4. **Souris** : la position de la main est lue par AWT (`MouseInfo`), en coordonnées de l'écran moins la
    position de la fenêtre, sans tenir compte de la place de la zone de jeu (hypothèse : décalage avec la mise à
    l'échelle de Windows ou en plein écran sur un second écran).
  - D5. Le bol de la cuisine ne suit pas les mouvements du regard (seulement l'entrée) ; la fin du jeu n'arrête pas
    la douche (50 gouttes animées), le vol, le clignement ni les ailes.
  - D6. Statistiques : des buts atteints (chaque remplissage) sans buts à atteindre.
  - D7. Boutons gris de l'ancien style ; fond de la page d'une couleur différente à chaque pièce.
- **Images (licences)** : **à risque**. 4 **photos de pièces** (salon, cuisine, salle de bain, parc : style banque
  d'images), un **gant de baseball Rawlings** et des **céréales Froot Loops** (marques), une balle de baseball et un
  pommeau de douche (photos de produits), une main, 4 pictogrammes (style banque d'icônes) : tous sans source. La
  Biboule (corps, yeux, bouche, sourire, ailes) est un dessin du projet (celle de Biboule Jump) : gardée.
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. **4 pièces dessinées par script** (salon, cuisine, salle de bain, jardin), dans le style des jeux revus.
    - I2. Objets OpenMoji (CC BY-SA 4.0) : main, cuillère et bol, pommeau de douche, ballon ; boutons avec les
      icônes des menus (Material Symbols, Apache-2.0) : maison, repas, douche, jeu.
    - I3. Une **bouche triste** pour la Biboule, dessinée dans le même style (pour G2).
  - Graphisme
    - G1. Boutons ronds des jeux revus (anneau de fixation), en colonne à gauche ; le bouton de la pièce ouverte
      entouré ; la pièce plus grande, le fond de la page aux couleurs des menus.
    - G2. **Trois jauges nommées**, chacune avec l'icône de son bouton et la même couleur : Faim, Propreté, Jeu.
      Quand une jauge passe sous le quart, elle clignote, la Biboule est **triste** et une **bulle** au-dessus d'elle
      montre l'icône de ce qu'elle veut.
  - Gameplay
    - P1. Des jauges plus lentes et au même rythme : une jauge pleine se vide en 2 minutes environ ; chaque activité
      réussie la remplit d'un quart.
    - P2. Les 4 activités gardées, rendues plus claires : cuisine (regarder le bol remplit la cuillère, l'amener à la
      bouche), salle de bain (l'eau sur la Biboule), jardin (la Biboule vole, la toucher lance le ballon), maison
      (la caresser la rend heureuse). Jeu sans fin, comme avant (on prend soin d'elle).
  - Corrections
    - P3. D3 (plantage), D4 (position de la souris lue dans l'événement, comme le regard), D5 (bol, tout arrêter à
      la fin), D6 (un but à atteindre par jauge à remplir, comptés comme Memory : rien ne change dans le format des
      fichiers).
- **Questions au mainteneur** : P1 (2 minutes pour vider une jauge vous conviennent-elles, ou une variante « Lent /
  Normal » ?) ; G2 (la Biboule triste et la bulle : d'accord ?) ; I1 (jardin plutôt que parc : d'accord ?)
- **Réponses du mainteneur (30/10/2026)** : tout est validé ; 2 minutes ; Biboule triste et bulle ; jardin.
- **Fait (30/10/2026)** :
  - Images (I1, I2, I3) : `gradle/images/make_pet_images.py` dessine le salon, la cuisine, la salle de bain et le
    jardin (1200 x 900), la nourriture du bol et de la cuillère pleine et la bouche triste ; objets OpenMoji (main,
    cuillère, bol, douche, ballon). Les 15 anciennes images (photos, marques, pictogrammes) sont retirées ; la
    Biboule est gardée. Boutons : icônes Material Symbols ajoutées à `data/icons` (`home_fill1`, `restaurant`,
    `shower_fill1`, `sports_soccer_fill1`).
  - Code réécrit (`PetHouse`, `Mypet`) : boutons ronds des jeux revus en colonne à gauche (pièce ouverte entourée,
    nom en infobulle) ; trois jauges nommées (Faim, Propreté, Jeu : icône et couleur de chacune) ; sous un quart, la
    jauge clignote, la Biboule est triste et une bulle montre l'icône du besoin le plus bas.
  - Gameplay (P1, P2) : une jauge pleine se vide en 2 minutes, au même rythme pour les trois ; chaque activité
    remplit un quart. Salon : caresser rend heureux. Cuisine : le regard sur le bol remplit la cuillère, près de la
    bouche (avec une marge pour le regard) la Biboule mange. Salle de bain : 3 s de douche au-dessus de la Biboule la
    lavent. Jardin : la Biboule vole, le ballon qui la touche rebondit et elle joue.
  - Corrections (P3) : plus de lecture de case -1 (les jauges sont des niveaux de 0 à 1) ; la position de la souris
    et du regard est lue dans l'événement, sur la pièce seulement ; le bol réagit au regard ; tout s'arrête à la fin
    (minuteries, vol, ailes, clignement, gouttes, filtres du regard). Statistiques : chaque quart perdu est un but à
    atteindre, chaque activité réussie un but atteint (mêmes fichiers, aucun changement de format).
  - Aide (`Pet-fra.txt`, `-eng.txt`) : jeu sans variante, la fenêtre d'aide s'ouvre avec « Lancer le jeu ».
    Traductions (`PetHunger`... `PetGarden`). Vignette (salle de bain, 960 x 540). Crédits et inventaire à jour.
- **Vérifications** : `PetHouseTest` (6 tests : images, jauges qui se vident en 2 minutes avec les buts à atteindre,
  Biboule triste et bulle, cuisine, salle de bain, jardin, fin du jeu) ; `GameHelpTest` avec `Pet` ; captures sans
  fenêtre des 4 pièces ; `allGamesTest -PallGamesOnly=Pet -PallGamesRepeat=5` réussi ; `build` : **634 tests, 0
  échec**, SpotBugs sans alerte. Reste à faire par le mainteneur : jouer à la souris et au Tobii (la cuillère et la
  douche au regard : marge de la bouche, durée du lavage).

### Flower Of Numbers (`FlowerOfNumbers`, code `flowerOfNumbers`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/flowerofnumbers/`
(1366 × 768, mode souris, français ; fixation sur le chiffre 1). Pas d'essai avec le Tobii.

- **Ce qu'il fait / variantes** (lecture du code, 5 classes, 870 lignes) : un nombre de 1 à 20 (tiré avec la graine
  de la partie) est écrit au centre d'une fleur à 5 pétales. Autour, les valeurs à choisir par fixation : les chiffres
  1 à 9 (en haut à droite), les mots « un » à « vingt » (2 colonnes à gauche), des mains de 1 à 5 doigts (à gauche),
  les dés de 1 à 6 (en bas à droite), 1 €, 2 €, 5 €, 10 € (à droite). Une valeur choisie glisse en 1 s dans le pétale
  de sa famille (chiffres : 4 places ; mots : 1 ; doigts, dés, argent : 4). Une fixation sur une valeur posée la
  retire. Un pétale dont la somme vaut le nombre s'entoure d'une aura jaune ; les 5 pétales justes : « Bravo », puis
  un autre nombre. Rejouable (dans la liste des jeux rejouables). Pas de variante.
- **Défauts vus** :
  - D1. **Rien n'explique la règle** : rien ne dit qu'il faut faire le nombre dans chaque pétale, ni quel pétale
    reçoit quelle famille (les couleurs des pétales ne rappellent rien autour) ; pas de somme en cours, pas de
    signal quand un pétale dépasse le nombre (il faut deviner et retirer).
  - D2. **Cibles trop petites pour le regard** (fait, calcul et capture) : les 20 mots ont environ 27 px de haut en
    1366 × 768 (largeur commune calculée sur le mot le plus long de toutes les langues) ; les mains, environ 40 px de
    large.
  - D3. **Mise en page fixe en proportions de l'écran** (fait, calcul) : la fleur est un carré de la hauteur, centré ;
    en 800 × 600 elle va de 100 à 700 px, et la colonne des mots (centre à 128 px) et les mains passent sur ses
    pétales. Hypothèse : pareil pour l'argent et les dés à droite (non vérifié par une capture).
  - D4. Style d'origine : fond noir, textes dessinés en police Java par défaut (blanc ou noir selon le thème
    seulement), indicateur de fixation gris (`ProgressButton`), traits gris parasites au-dessus à gauche de la fleur
    (hypothèse : bords semi-transparents de `flower.png`).
  - D5. Aucun son : ni à la pose, ni pour un pétale juste, ni pour la fleur.
  - D6. **Statistiques** (fait, code) : `incrementNumberOfGoalsReached` à chaque fleur, mais jamais
    `incrementNumberOfGoalsToReach`. Hypothèse : l'écran des statistiques montre plus de buts atteints que de buts à
    atteindre.
- **Images et sons (licences)** : **à risque**, 21 images de 2022 (auteur du commit : Sakraft), sans source :
  - fleur (`flower.png`) et 5 auras ;
  - 5 mains (style banque d'images vectorielles, type Freepik : hypothèse) ;
  - 6 dés (rendu 3D, source inconnue) ;
  - **4 images de l'euro** (pièces de 1 € et 2 €, billets de 5 € et 10 €) : reproductions des dessins officiels.
    Les billets sont soumis aux règles de reproduction de la BCE, et les faces des pièces ont leurs propres droits.
    Risque : on ne sait pas si ces images respectent ces règles.
  - Aucun son.
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur) :
  - Images
    - I1. **Tout dessiner par script** (comme Spot The Differences et Pet), sous la licence du projet : la fleur (5
      pétales aux couleurs franches, sans bords parasites), les dés (carré arrondi et points), les mains (main
      stylisée, 1 à 5 doigts levés), les auras (halo dessiné par le jeu autour du pétale).
    - I2. **Argent** : pièces et billets **dessinés, génériques** (pièces dorée « 1 € » et bicolore « 2 € », billets
      colorés « 5 € » et « 10 € », sans aucun motif officiel) ; on garde l'euro comme unité. Plus aucune
      reproduction de l'euro.
  - Graphisme
    - G1. Fond et style des jeux revus (`-gp-bg`, police Fredoka, couleurs du thème) ; chaque famille de valeurs dans
      une **carte arrondie bordée de la couleur de son pétale**, placée à côté de lui : on voit tout de suite quel
      pétale reçoit quoi.
    - G2. **Cibles plus grandes** : environ 60 px au moins pour toutes les valeurs. Les mots en pastilles, sur 4
      colonnes de 5 (au lieu de 2 de 10) ; les mains et l'argent agrandis.
    - G3. Anneau de fixation de la couleur principale (comme les autres jeux revus), à la place de l'indicateur gris.
    - G4. Mise en page calculée d'après la taille de l'écran, sans chevauchement de 800 × 600 à 1920 × 1080.
  - Gameplay
    - P1. **Somme affichée sur chaque pétale** (« 6 / 14 ») ; un pétale qui dépasse le nombre devient rouge et tremble
      un peu (on retire une valeur pour corriger). Un pétale juste : aura, son de réussite. Fleur complète :
      « Bravo », puis un autre nombre (comme aujourd'hui).
    - P2. **Variantes** : « Jusqu'à 10 » (plus facile, mots de un à dix seulement) et « Jusqu'à 20 » (le jeu
      actuel) ; et, en bas, « Somme affichée » ou « Somme cachée » (pour qui veut compter seul).
    - P3. Sons (synthétisés par script, comme `ding.wav`) : pose d'une valeur, pétale juste ; fleur complète :
      l'animation de victoire habituelle.
  - Corrections
    - P4. D6 : un but à atteindre par fleur (`incrementNumberOfGoalsToReach`), comme les autres jeux. Même format de
      fichiers.
    - P5. **Rejeu** : les positions des valeurs changent (G1, G2, G4). Risque : une partie enregistrée avant la revue
      se rejouerait avec le regard enregistré sur l'ancienne disposition, donc à côté des nouvelles cibles. Comme
      pour les autres jeux revus, on n'y touche pas (pas de conversion des anciennes parties).
  - Aide du jeu (`FlowerOfNumbers-fra.txt`, `-eng.txt`), vignette, traductions, crédits, inventaire.
- **Questions au mainteneur** : I2 (pièces et billets dessinés génériques, en gardant l'euro, ou un autre support,
  comme des jetons de 1, 2, 5, 10 ?) ; P1 (somme affichée par défaut ?) ; P2 (les deux variantes vous
  conviennent-elles ?) ; G2 (mots sur 4 colonnes : d'accord ?)
- **Réponses du mainteneur (2/11/2026)** : tout est validé ; l'euro gardé, dessiné ; somme affichée par défaut ; les
  2 variantes ; les mots sur 4 colonnes.
- **Fait (2/11/2026, première version ; ses règles de jeu P1 et P2 sont remplacées plus bas)** :
  - Images (I1, I2) : `gradle/images/make_flowerofnumbers_images.py` dessine les 5 mains (main stylisée, 1 à 5 doigts
    levés), les 6 dés, les pièces de 1 € et 2 € (deux métaux, valeur et €) et les billets de 5 € et 10 € (rectangle
    coloré, valeur, € et « EURO », aucun motif officiel). Mêmes noms de fichiers. `flower.png` et les 5 auras sont
    retirées : la fleur (tige, 5 pétales, cœur jaune) et l'aura (contour vert lumineux) sont dessinées par le jeu.
  - Sons (P3) : `make_flowerofnumbers_sounds.py` fait `sounds/put.wav` (valeur posée) et `sounds/petal.wav` (pétale
    juste, trois notes de clochette).
  - Graphisme (G1 à G4) : code réécrit (`FlowerOfNumbersGame`, nouvelle `ValueTarget`, l'ancien `ProgressButton`
    n'est plus utilisé). Tout est placé sur un plateau de 1366 x 768, mis à l'échelle de l'écran et centré : plus de
    chevauchement à aucune taille. Fond, cartes et police des jeux revus ; chaque famille dans une carte bordée de la
    couleur de son pétale (à gauche : mots en 4 colonnes de 5, dans l'ordre des colonnes, puis les mains en 3 + 2 ; à
    droite : chiffres, argent, dés), loin de l'engrenage. Cibles de 54 à 150 px de large (mots 72 x 60, mains
    90 x 108) ; les mots trop longs d'une langue sont écrits plus petits. Anneau de la couleur principale autour de
    la carte regardée ; il faut quitter une valeur et revenir pour la choisir une nouvelle fois.
  - Gameplay (P1, P2) : somme « 6 / 14 » au bout de chaque pétale ; pétale qui dépasse : contour rouge (`-gp-danger`)
    et petit tremblement ; pétale juste : contour vert lumineux et son. Fleur complète : « Bravo », puis un autre
    nombre. Variantes (`GroupedGameVariant`, un seul groupe) : « Jusqu'à 10 » (mots de un à dix, 2 colonnes) ou
    « Jusqu'à 20 », et en bas « Somme affichée » (par défaut) ou « Somme cachée » (le rouge et le vert restent).
  - Corrections (P4) : un but à atteindre par fleur (`incrementNumberOfGoalsToReach` au lancement), un but atteint
    par fleur complète ; mêmes fichiers. Une partie enregistrée sans variante se rejoue « Jusqu'à 20 » (même tirage
    des nombres qu'avant), avec les sommes ; le regard enregistré tombe sur l'ancienne disposition (P5, accepté).
  - Aide (`FlowerOfNumbers-fra.txt`, `-eng.txt`), traductions (`FlowerUpTo10`, `FlowerUpTo20`, `FlowerSumShown`,
    `FlowerSumHidden`), vignette (capture, 960 x 540), crédits (`gradle/images/README.txt`,
    `gradle/templates/licence.txt`), inventaire (`inventaire-images.csv`, `INVENTAIRE_IMAGES.md`).
- **Vérifications** : `FlowerOfNumbersGameTest` (7 tests : valeurs de chaque variante, valeur posée puis retirée
  avec sa somme et son son, pétale plein et qui dépasse, victoire avec les 5 pétales justes, sommes cachées,
  disposition sans chevauchement en 800 x 600, variantes relues) ; `FlowerTest` gardé ; `GameHelpTest` avec
  `FlowerOfNumbers` ; captures en vraie fenêtre `build/game-screenshots/essai1/` et `essai2/flowerofnumbers/`.
  Reste à faire par le mainteneur : jouer à la souris et au Tobii.
- **Retour du mainteneur (2/11/2026, essai souris et Tobii)** : tout fonctionne, mais **la règle n'est pas la bonne**.
  La vraie règle : le nombre du centre est le **total à faire avec l'ensemble des pétales** (pas un nombre par
  pétale), et **chaque valeur ne sert qu'une fois** (pas trois fois le dé 1). Modes décidés (réponses aux
  questions du 2/11) : **Guidé** (le jeu indique la valeur de chaque pétale, tirée au hasard), **Libre** (seul le
  total compte, pétales vides permis), **Tous les pétales** (le total, chaque pétale doit servir) ; nombres
  **jusqu'à 10, 20, 30, 40 et 50** ; les modes remplacent « Somme affichée / cachée ».
- **Fait (2/11/2026, nouvelles règles)** :
  - `FlowerMode` (Guidé, Libre, Tous les pétales) ; variantes : 5 boutons (« Jusqu'à 10 » à « Jusqu'à 50 ») et
    les 3 modes en bas (Guidé en premier), soit 15 variantes. Clés `FlowerUpTo30/40/50`, `FlowerGuided`,
    `FlowerFree`, `FlowerAllPetals`, `FlowerTotal` ; `FlowerSumShown` et `FlowerSumHidden` retirées.
  - Nombre du cœur : de 5 (Guidé, Tous les pétales : au moins 1 par pétale) ou de 1 (Libre) jusqu'au plus grand
    de la variante, jamais deux fois de suite le même. Mots de un à vingt (jusqu'à dix pour « Jusqu'à 10 »).
  - Guidé : le nombre est partagé au hasard (graine de la partie) entre les 5 pétales, au moins 1 chacun, chaque
    part étant une somme que le pétale peut faire avec des valeurs différentes, 4 au plus (l'argent ne peut pas
    faire 4, 9 ni 14 : 1, 2, 5, 10 une fois chacun). Au bout du pétale : « 1 / 4 » ; rouge s'il dépasse sa part,
    vert avec un son quand il la fait.
  - Libre et Tous les pétales : au bout du pétale, sa somme seule ; en Tous les pétales, un pétale vide est pâle
    (couleur éclaircie). Fleur gagnée : tous les pétales verts, son.
  - Partout : chaque valeur une seule fois (pâle dans sa carte tant qu'elle est posée, de nouveau choisissable
    une fois retirée) ; « Total : 12 / 17 » en bas, sous la fleur, rouge (et qui tremble) au-dessus du nombre,
    vert une fois gagné.
  - Une partie enregistrée sans variante se rejoue en Guidé, jusqu'à 20 (ses règles étaient autres).
  - Aide réécrite (français et anglais).
- **Vérifications (nouvelles règles)** : `FlowerOfNumbersGameTest` (15 tests : valeurs et nombre de chaque
  variante ; parts du mode guidé pour 15 graines et chaque variante, sommes possibles ; chaque valeur une fois puis
  retirée ; pétale plein et total rouge ; victoire en Guidé, en Libre avec le total seul, en Tous les pétales
  seulement sans pétale vide ; disposition sans chevauchement en 800 x 600 avec le total ; 15 variantes relues) ;
  captures `build/game-screenshots/essai3/flowerofnumbers/` (Guidé jusqu'à 20, Tous les pétales jusqu'à 50).

### Opinions (`Opinions`, code `opinions`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/opinions/`
(1366 × 768, mode souris, français ; variantes Opinions et Oui/Non haut-bas). Pas d'essai avec le Tobii.

- **Ce qu'il fait / variantes** (lecture du code, 6 classes, 352 lignes, et captures) : une image est montrée en grand ;
  le joueur répond par une fixation sur un bouton, puis l'image suivante s'affiche. Après 10 réponses : animation de
  victoire, puis l'écran des statistiques (la partie s'arrête). 5 variantes : **Opinions** (3 boutons : visage
  content à gauche, visage qui lève les yeux en haut, visage rouge triste à droite) et **Oui/Non** dans 4
  dispositions (haut-bas, bas-haut, gauche-droite, droite-gauche ; coche verte et croix rouge). Rejouable (graine).
- **Défauts vus** :
  - D1. **La réponse n'est enregistrée nulle part** (fait, code) : les boutons font tous la même chose (image
    suivante, un « but atteint »). Ni le jeu ni les statistiques ne savent ce que le joueur a répondu.
  - D2. **Les images du jeu ne sont jamais montrées** (fait, code et captures) : le jeu lit le dossier de
    l'utilisateur `files/images/opinions` et, s'il est vide, les **images par défaut** (`data/common/default/images` :
    animaux OpenMoji). Les 4 photos d'aliments de `data/opinions/images` (bretzel, haricots verts, orange, pommes)
    ne sont lues par aucun code. Les images de l'utilisateur remplacent celles du jeu sans le dire (ancien dossier
    partagé `opinions`, aussi lu par Casse-Brique pour son fond).
  - D3. **Compte faux** (fait, code) : quand le tirage redonne l'image qui vient d'être montrée, la boucle du tirage
    compte une réponse de plus à chaque nouveau tirage ; une partie peut finir avant 10 réponses. Les mêmes images
    reviennent dans une partie (pas de tirage sans remise).
  - D4. **Boutons sur l'image** (captures) : en Opinions, le visage du haut cache le haut de l'image ; en Oui/Non
    haut-bas, la coche et la croix sont posées sur l'image (oreilles et menton de l'ours). Positions fixes en
    proportions de l'écran ; boutons de 140 px, ronds gris.
  - D5. Rien ne dit ce que veut dire chaque bouton (pas de texte « J'aime », « Oui »...) ; libellés des variantes
    mal écrits en anglais (« rigth ») et absents de la plupart des langues.
  - D6. Ancien style : fond noir, image étirée sur toute la hauteur, indicateur de fixation gris.
  - D7. `dispose` vide : les filtres du regard des boutons ne sont jamais retirés. `OpinionsGameStats` a une
    méthode `incrementNumberOfGoalsReached(int)` qui remplace le compte au lieu de l'augmenter (inutilisée) ; aucun
    but à atteindre n'est compté.
- **Images (licences)** : **à risque**. Les 4 photos d'aliments (2020, sans source ; `brezelle.jpg` porte l'auteur
  « Couleur » dans ses métadonnées, signature d'un photographe de banque d'images gratuite : hypothèse) ne servent
  pas. Les 5 boutons sont des visages brillants et des icônes de banque d'images (style Apple ou Emojipedia pour le
  visage content, hypothèse), sans source. Vignette du menu sans source.
- **Propositions** (à valider ✔ / refuser ✘ par le mainteneur ; elles dépendent des questions plus bas) :
  - Images
    - I1. **Séries d'images OpenMoji** (CC BY-SA 4.0), choisies par variante : par exemple Aliments, Animaux,
      Activités et jouets (une douzaine chacune). Les 4 photos d'aliments retirées.
    - I2. **Variante « Mes images »** (règle du 29/10 : `UserImagesVariant`, dossier `files/images/Opinions`, ligne
      dans les réglages, fenêtre d'erreur sans image) : photos de la famille, des activités de la semaine, etc. Le
      jeu ne lit plus l'ancien dossier `opinions`, qui reste lu par Casse-Brique jusqu'à sa revue.
    - I3. Boutons : visages OpenMoji (content, neutre, pas content) et coche / croix dessinées, chacun avec son mot
      (« J'aime », « Bof », « Je n'aime pas » ; « Oui », « Non »).
  - Graphisme
    - G1. L'image dans une carte au centre, entière, jamais cachée ; les boutons en grandes cartes autour, sans
      toucher l'image ; anneau de fixation de la couleur principale ; fond et police des jeux revus.
    - G2. Retour après la réponse : le bouton choisi s'entoure, petit son, puis l'image suivante glisse.
  - Gameplay
    - P1. Partie de 10 images **sans remise** (D3 corrigé) ; à la fin, **un récapitulatif** : les images de la
      partie avec la réponse donnée pour chacune (avant l'écran des statistiques).
    - P2. Variantes regroupées : les boutons = la série d'images (Aliments, Animaux, Activités, Mes images) ; en bas
      = la sorte de réponse (« J'aime / Bof / Je n'aime pas », « Oui / Non »).
  - Corrections
    - P3. D3 (compte), D7 (`dispose`, un but à atteindre par image).
- **Questions au mainteneur** (la règle d'abord, comme pour Flower Of Numbers) :
  1. **À quoi sert le jeu** ? Donner son avis (dire ce qu'on aime, pour la communication) ; s'entraîner à répondre
     oui / non ; ou les deux ? Pour « Oui / Non », quelle est la question (« Tu aimes ? », « C'est un animal ? »...) ?
  2. **Faut-il garder les réponses** ? Dans un fichier des résultats (nouveau fichier, par exemple `reponses.csv`,
     avec l'image, la réponse et son heure : un **ajout au format des résultats**, versionné, sans toucher aux
     fichiers existants), ou seulement dans le récapitulatif de fin (P1), ou pas du tout ?
  3. **Les 4 dispositions du oui / non** (haut-bas, bas-haut, gauche-droite, droite-gauche) servent-elles (par
     exemple à tester le regard dans chaque direction) ? Sinon : une seule disposition, oui à gauche et non à droite.
  4. **Séries d'images** : Aliments, Animaux, Activités et jouets, plus « Mes images » : d'accord, ou d'autres ?
  5. **Fin de partie** : 10 images puis récapitulatif et statistiques, ou sans fin (comme Memory), ou un nombre
     d'images au choix ?
- **Réponse du mainteneur (2/11/2026) : la règle du jeu.** Le jeu ne se lance qu'avec des **images données par
  l'utilisateur** ; elles sont montrées au centre **une par une**, et la personne choisit si elle **aime ou pas**
  l'image. Les réponses sont enregistrées : **un fichier de plus dans les statistiques**, avec le nom de l'image et le
  choix. (Les questions 3 à 5 tombent : plus de oui / non ni de séries d'images du jeu.)
- **Fait (2/11/2026)** :
  - Variante unique **Mes images** (`OpinionsGameVariant.MY_IMAGES`, `UserImagesVariant`, dossier
    `files/images/Opinions`) : ligne « Opinions » dans les réglages (Dossiers, « Images des jeux ») et fenêtre
    d'images du menu quand le dossier est vide, sans code de plus. Les anciennes valeurs (Opinions, 4 Oui/Non) restent
    dans l'énumération pour relire les parties enregistrées, mais ne sont plus proposées. Le jeu ne lit plus l'ancien
    dossier partagé `opinions` (encore lu par Casse-Brique pour son fond ; la fenêtre `CustomFileChooser` garde ses
    boutons « thumbs » qui ne servaient déjà à rien, à retirer avec ce dossier).
  - Déroulement : les images dans l'ordre de leurs noms, au centre dans une carte (entière, jamais cachée), le
    numéro au-dessus (« 3 / 12 ») ; à gauche « J'aime » (pouce levé vert), à droite « Je n'aime pas » (pouce baissé
    rouge), grandes cartes à icône (Material Symbols `thumb_up_fill1`, `thumb_down_fill1`, Apache-2.0, ajoutées à
    `data/icons`) avec l'anneau de fixation. La réponse s'entoure de sa couleur 0,7 s, puis l'image suivante ; une
    seule réponse par image. Après la dernière : « Bravo », puis les statistiques. Sans image : le message commun
    `NoUserImages`. Un but à atteindre par image, un but atteint par réponse.
  - **Nouveau fichier de résultats** `<heure>-opinions.csv` (`OpinionsAnswers`, format 1), à côté des autres fichiers
    de la partie et avec le même horodatage : UTF-8 avec BOM, « ; » (comme les résultats d'Eval), en-tête
    `image;choix`, une ligne par image répondue dans l'ordre (`aime` ou `n'aime pas`, en français quelle que soit la
    langue : format stable). **Impact** : un ajout ; aucun fichier existant n'est changé ; les parties d'avant n'ont
    pas ce fichier. Il est aussi écrit quand on quitte avant la fin (réponses données jusque-là).
  - Retirés : les 4 photos d'aliments (jamais montrées) et les 5 icônes de boutons de `data/opinions`. Traductions
    `OpinionsMyImages`, `OpinionsLike`, `OpinionsDislike`. Aide (`Opinions-fra.txt`, `-eng.txt`, avec une section
    « Les réponses »). Vignette (capture, 960 x 540, même nom `opinions.jpg`). Inventaire à jour.
- **Vérifications** : `OpinionsGameTest` (6 tests : images dans l'ordre des noms et réponses enregistrées dans
  l'ordre, fin après la dernière ; une réponse par image ; fichier des réponses, guillemets compris ; disposition sans
  recouvrir l'image ni l'engrenage ; sans image ; variante unique et anciennes valeurs relues) ; `GameHelpTest` avec
  `Opinions` ; **partie réelle** en vraie fenêtre (souris, 4 images de test, 4 réponses) : `...-opinions.csv` écrit
  avec les 4 choix, à côté des autres fichiers (`build/game-screenshots/essai1/opinions/`). Reste à faire par le
  mainteneur : jouer à la souris et au Tobii avec ses images.
- **Retour du mainteneur (2/11/2026)** : le message « il faut des images » doit être dans la **fenêtre des
  variantes**, et le jeu ne doit pas pouvoir se lancer sans image. **Fait** (pour tous les jeux revus à images de
  l'utilisateur : Opinions, Coloring, Memory) : quand la variante choisie joue les images de l'utilisateur et que son
  dossier est vide, `GameVariantDialog` montre au-dessus de « Commencer » le message `NoUserImages` (en rouge) et un
  bouton « Ajouter des images », qui ouvre la fenêtre des images par-dessus ; « Commencer » (et Entrée) reste grisé
  tant qu'il n'y a pas d'image, et se réactive à la fermeture de cette fenêtre s'il y en a. Les jeux pas encore
  revus (une variante lance le jeu tout de suite) gardent la fenêtre d'images d'avant. Test :
  `GameVariantDialogTest.shouldAskForImagesBeforeStartingAVariantPlayingWithThem`.
  - **Second retour (2/11/2026)** : le jeu se lançait encore sans image. Cause (fait) : avec **une seule variante**
    et une aide, le menu sautait la fenêtre des variantes et ouvrait l'aide, dont le bouton lançait le jeu sans
    rien vérifier. Corrigé : une variante unique qui joue les images de l'utilisateur ouvre aussi la fenêtre des
    variantes (`GameMenuController.showsTheVariants`, test
    `GameMenuControllerTest.shouldShowTheVariantsOfASingleVariantPlayingWithTheImagesOfTheUser`).

### Sprint Finish (`SprintFinish`, `SprintFinishMouse`, code `sprintFinish`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/sprintfinish/`
(1366 × 768, mode souris, français ; niveaux 1, 6, 13 et 17 de « (Regard) », 4 et 11 de l'autre ; déroulé seconde
par seconde du niveau 1 dans les deux modes : `timeline/*-planche.png`). Pas d'essai avec le Tobii ni à deux.

- **Ce qu'il fait** (lecture du code : 10 classes, 2 053 lignes, écrites en 2023 ; et captures) : un chat doit
  atteindre sa gamelle sans se faire attraper par des chiens qui le poursuivent. Obstacles : murs noirs, portes
  marron ouvertes par un bouton rouge (fixation, avec un indicateur de progression ; les chiens ralentissent pendant
  la fixation), murs rouges qui vont et viennent (le toucher fait perdre). 17 niveaux de difficulté croissante.
  Attrapé : le niveau recommence aussitôt. Gamelle atteinte : « Bravo », niveau suivant ; l'écran des statistiques
  vient quand le numéro du niveau atteint un multiple de 6 (après les niveaux 5, 11 et 17). 3 s d'attente au début
  de chaque niveau, sans rien d'affiché.
- **Deux entrées au catalogue, deux façons de jouer** (c'est un **jeu à deux**, d'après le code) :
  - **« Sprint jusqu'à l'arrivée »** (`SprintFinish`) : **un joueur déplace le chat au clavier** (flèches) ;
    **l'autre aide avec le regard** : regarder un chien l'arrête, regarder un mur mobile l'arrête (il devient bleu),
    fixer un bouton ouvre ou referme ses portes.
  - **« Sprint jusqu'à l'arrivée (Regard) »** (`SprintFinishMouse`) : **le chat suit le regard** (ou la souris) ;
    **l'aide joue au clavier** : A arrête le chien le plus proche du chat, Z appuie sur le bouton le plus proche, E
    arrête le mur mobile le plus proche (tant que la touche est tenue).
  - Rien ne l'explique à l'écran (pas d'aide, pas de consigne, touches A / Z / E à deviner). Le nom de code
    « Mouse » désigne en fait le mode où le chat suit le **regard**.
- **Défauts vus** :
  - D1. **Niveaux faits pour un grand écran** (fait ; 1920 × 1080 probable, hypothèse) : positions en pixels écrites en dur
    (chien à x = 1100, bouton à « hauteur − 900 »...). En 1366 × 768, le **niveau 6 est impossible** : le bouton qui
    ouvre la cage de la gamelle est hors de l'écran (y = −132). Tailles fixes (chat et chiens 125 px).
  - D2. **Des chiens fantômes** (hypothèse forte, code et captures) : quand le chat est attrapé, le niveau est
    reconstruit aussitôt, mais les minuteries de l'ancien chat et des anciens chiens ne s'arrêtent pas (elles ne
    s'arrêtent qu'en voyant la fin du niveau, déjà remise à zéro). Les anciens chiens, invisibles, continuent de
    courir et peuvent « attraper » le nouveau chat : en mode Regard, le chat revient au départ alors qu'aucun chien
    visible ne le touche (déroulé du niveau 1). Leur nombre augmente à chaque échec : le jeu devient de plus en plus
    dur, puis injouable. Même chose pour les murs mobiles (animations jamais arrêtées) et pour les écouteurs du
    clavier et de la souris ajoutés sur la scène à chaque niveau, jamais retirés.
  - D3. **Échec sans retour** : le niveau recommence en un instant, sans dire pourquoi (ni son, ni image). En mode
    Regard, le chat repart vers le coin haut gauche tant que le regard (la souris) n'a pas bougé.
  - D4. Compte des statistiques : un but atteint par niveau réussi, **aucun but à atteindre** ; écran des
    statistiques à des moments sans lien avec le niveau choisi (niveau 4 choisi : statistiques après le niveau 5).
  - D5. Fenêtre des variantes : 17 boutons « 1 » à « 17 » et trois boutons ronds « Niveau 1-5 / 6-11 / 12-17 »
    (traduction française fausse : « Niveau 6-5 ») ; deux cartes au menu avec la même vignette.
  - D6. Graphisme ancien : murs et portes en rectangles unis (noir, marron, rouge / bleu), bouton rouge brillant,
    indicateur de progression gris de JavaFX, chat et chien en images détourées sur une prairie.
  - D7. Code : vitesse liée au nombre d'images par seconde (60 supposées), 3 s comptées en images ; ralentissement
    des chiens multiplié à chaque entrée du regard (regard et souris ensemble : deux fois).
- **Images (licences)** : **à risque**. Les 6 images (`background.png` prairie, `chat.png`, `chien.png`,
  `gamelle.png`, `pushButtonOff/On.png`) et la vignette sont de source inconnue (ajoutées en 2023 par l'auteur du
  jeu, sans source) ; le chat et le chien ressemblent à des illustrations de banques d'images (hypothèse). Aucun
  son.
- **Propositions** (à valider ✔ / refuser ✘ ; elles dépendent des questions plus bas) :
  - Images
    - I1. Chat, chien, gamelle, os ou croquettes : **OpenMoji** (CC BY-SA 4.0) ; prairie, murs (haies), portes
      (barrières en bois) et boutons **dessinés par script** (`gradle/images/make_sprintfinish_images.py`) ; nouvelle
      vignette (capture).
  - Graphisme
    - G1. Plateau dessiné à l'échelle de l'écran (niveaux décrits sur une grille, pas en pixels) ; murs en haies,
      portes en barrières reliées à leur bouton par la même couleur ; mur mobile qui change d'aspect quand il est
      arrêté ; chien arrêté qui s'assoit (ou un « stop » au-dessus) ; anneau de fixation des jeux revus sur les
      boutons ; « Niveau 4 » affiché en haut ; compte à rebours 3-2-1 au début.
    - G2. Échec visible : le chien attrape le chat (petite animation, son), puis le niveau recommence.
  - Gameplay
    - P1. **Une seule carte** « Sprint Finish » au menu, la façon de jouer choisie en bas de la fenêtre des
      variantes (« Le chat suit le regard » / « Le chat au clavier ») ; les niveaux en boutons (groupes 1-5, 6-11,
      12-17 gardés ou non).
    - P2. Partie = le niveau choisi puis les suivants ; statistiques à la fin d'une série (par exemple 5 niveaux)
      ou quand on quitte ; un but à atteindre par niveau.
    - P3. Les touches de l'aide au clavier écrites à l'écran (A, Z, E, ou des touches plus simples : flèches pour le
      chat, barre d'espace...) et dans l'aide du jeu.
  - Corrections
    - P4. D2 (tout arrêter à la fin d'un niveau : minuteries, animations, écouteurs), D1 (niveaux à l'échelle,
      niveau 6 jouable), D4, D5 (traduction), D7 (vitesse en temps réel).
- **Questions au mainteneur** (la règle d'abord) :
  1. **À quoi sert le jeu, et à qui ?** Est-ce bien un jeu **à deux** (un au regard, un au clavier), par exemple un
     enfant et un adulte, ou un frère ou une sœur ? Faut-il aussi pouvoir y jouer **seul** au regard (chat au regard,
     chiens qui s'arrêtent tout seuls de temps en temps, ou sans aide) ?
  2. **Les deux façons de jouer** sont-elles à garder toutes les deux (chat au clavier + aide au regard ; chat au
     regard + aide au clavier) ? Une seule carte avec le choix en bas (P1) ?
  3. **Les touches de l'aide** : A / Z / E à garder, ou d'autres (une seule touche « stop » pour tout ce qui est
     proche, par exemple) ?
  4. **Les 17 niveaux** : à garder tels quels (refaits à l'échelle de l'écran), à réduire, ou à refaire ?
  5. **Fin de partie** : statistiques après combien de niveaux (5 ? le dernier niveau ? jamais, on quitte par
     l'engrenage) ?
- **Réponse du mainteneur (2/11/2026) : la règle.**
  - **À deux** : le joueur au regard arrête les chiens en les regardant (l'autre déplace le chat).
  - **Variante à un joueur** : le chat se déplace **au clavier ou au regard** ; les chiens sont **beaucoup plus
    lents** (s'amuser sans perdre trop souvent) ; les boutons s'activent quand **le chat passe dessus**.
  - **Touches** : Z / Q / S / D (clavier AZERTY) ou W / A / S / D (QWERTY), avec une option dans les réglages pour
    choisir son clavier.
  - **Niveaux** : garder les 17, ou les générer au hasard (avis demandé).
  - **Fin de partie** : variantes Facile / Normal / Difficile = gagner après 5 / 10 / 15 cartes.
- **Avis donné et validé (2/11/2026, « ok pour tout, fais comme tu proposes »)** : à deux, seulement « chat au clavier
  + aide au regard » (l'ancien mode « chat au regard + aide au clavier A / Z / E » disparaît) ; les flèches, Z Q S D
  et W A S D marchent toutes, **sans réglage** (elles ne se gênent pas) ; cartes **tirées au hasard à partir de
  modèles** ; une seule carte au menu.
- **Fait (2/11/2026)** :
  - **Une seule entrée** `SprintFinish` (`SprintFinishMouse` retiré du catalogue : 70 entrées). Variantes
    `GroupedGameVariant` : Facile (5 cartes), Normal (10), Difficile (15) en boutons ; en bas, « Seul : le chat suit
    le regard », « Seul : le chat au clavier », « À deux » (`SprintMode`, `SprintDifficulty`). Une ancienne variante
    (numéro de niveau) se joue seul au regard, en Normal. Le jeu n'est pas dans la liste du rejeu : pas de
    correspondance à garder pour les anciennes parties.
  - **Cartes au hasard** (`SprintMapGenerator`, `SprintMap`) : grille de 16 × 9 cases à l'échelle de l'écran (plus de
    pixels écrits en dur : D1 corrigé). Modèles dans l'ordre de la partie, repris des 17 niveaux : prairie avec des
    buissons, haie avec un passage, haie avec une barrière ouverte par un bouton (les chiens attendent derrière),
    gamelle dans un enclos fermé par une barrière (avec une haie à passage en plus une fois sur deux), rondins qui
    montent et descendent (1 ou 2). Chiens : 1 puis 2 dans la seconde moitié, 3 dans le dernier tiers du Difficile ;
    jamais près du chat (7 pas au moins), de la gamelle ni sous le compteur. Chaque carte est vérifiée jouable
    (boutons atteignables barrières fermées, gamelle atteignable une fois ouvertes) ; tirage rejouable (graine).
  - **Une seule boucle de jeu**, en temps réel (cases par seconde), arrêtée à chaque fin de carte ; écouteurs du
    clavier et du regard retirés à chaque carte et à la fin : **plus de chiens fantômes** (D2), vérifié par un test.
    Rien ne bouge pendant que le menu de l'engrenage est ouvert.
  - Chiens : ils prennent le plus court chemin autour des haies et des barrières fermées (ils attendent derrière une
    barrière), s'arrêtent devant un rondin ; seuls, beaucoup plus lents (0,7 case/s contre 1,6 à deux). À deux, un
    chien regardé s'arrête (cercle rouge, grisé) ; un rondin regardé s'arrête (gris) ; un bouton s'ouvre par une
    fixation (anneau de la couleur principale). Seul, le bouton s'ouvre quand le chat passe dessus. Une barrière
    ouverte le reste.
  - Chat au regard : il va vers le point regardé (il attend le premier regard, au lieu de partir dans le coin) et
    glisse le long d'une haie. Chat au clavier : vitesse constante, 3,2 cases/s.
  - Retours : compte à rebours 3-2-1 (son), « Carte 3 / 10 » en haut ; attrapé : le chat tremble (son), la même carte
    recommence ; gamelle : le chat saute (son), carte suivante ; après la dernière : « Bravo », puis les statistiques.
    Un but à atteindre par carte, un but atteint par carte gagnée (D4).
  - Images (`make_sprintfinish_images.py`) et sons (`make_sprintfinish_sounds.py`) ; les 6 anciennes images
    retirées ; classes `Cat`, `Interrupteur`, `MovingWall`, `SprintFinishMouse*` retirées. Branches propres à Sprint
    Finish retirées de `GameVariantDialog` ; traduction « Niveau 6-5 » corrigée (« Niveau 6-11 », encore utilisée par
    Rush Hour). Aide (`SprintFinish-fra.txt`, `-eng.txt`), vignette (capture d'une carte avec enclos, 960 × 540),
    crédits (`gradle/images/README.txt`, licence affichée), inventaire.
- **Vérifications** : `SprintFinishTest` (8 tests : cartes jouables sur 150 tirages pour chaque difficulté, modèles et
  nombre de chiens ; chat aux flèches, Z Q S D et W A S D, clavier écouté puis plus à la fin ; chat au regard ; même
  carte après avoir été attrapé, sans les chiens d'avant ; les 5 cartes du Facile puis la fin ; bouton ouvert par le
  chat seul et pas à deux ; chien regardé arrêté ; variantes) ; `GameHelpTest` avec `SprintFinish` ; captures en vraie
  fenêtre des deux modes (`build/game-screenshots/essai1/sprintfinish/`) et de chaque modèle de carte. Pas d'essai au
  Tobii ni à deux : à faire par le mainteneur.
- **Retour du mainteneur (2/11/2026)** : « c'est nickel », mais les chiens sont un peu raides : qu'ils puissent aller
  **en diagonale**. **Fait** : un chien court tout droit vers le chat quand rien ne le gêne ; sinon il suit le plus
  court chemin, diagonales comprises (jamais à travers le coin d'une haie), en filant vers la case la plus lointaine
  qu'il voit encore : il contourne les haies en souplesse. Test
  `SprintFinishTest.shouldRunDiagonallyToTheCat` ; vérifié en vraie fenêtre (`build/game-screenshots/essai2/`).

### Bubbles (`ColoredBubbles`, `PortraitBubbles`, code `bubbles`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/bubbles/`
(1366 × 768, mode souris, français ; Bulles colorées vers le haut et vers la gauche sans fond, Bulles portraits
fixes ; déroulé seconde par seconde, pointeur au centre ; planches `*-planche.png`). Pas d'essai avec le Tobii.

- **Ce qu'il fait** (lecture du code : 10 classes, 682 lignes ; et captures) : 10 bulles traversent l'écran dans le
  sens de la variante, ou restent immobiles ; une bulle **éclate dès que le regard (ou la souris) entre dedans** (pas
  de fixation), en petits morceaux avec un « plop », et repart d'un bord. Avec le fond activé dans les réglages,
  une image sous-marine (tirée parmi 3) est cachée sous 36 pavés noirs (6 × 6) : chaque bulle éclatée en enlève un.
  **Le jeu ne finit jamais** (on quitte par l'engrenage) ; rien ne se passe quand l'image est toute découverte.
- **Deux entrées au catalogue** : **Bulles colorées** (`ColoredBubbles` : couleurs tirées au hasard) et **Bulles
  portraits** (`PortraitBubbles` : les images du dossier partagé `portraits` de l'utilisateur, sinon les 19 animaux
  par défaut). Mêmes 5 variantes : du bas vers le haut, du haut vers le bas, de gauche à droite, de droite à gauche,
  « fixe » (immobiles : une bulle regardée grossit pendant la durée de fixation puis éclate ; toutes changent de
  place toutes les 15 s).
- **Défauts vus** :
  - D1. **Couleurs au hasard, souvent sombres** (fait, captures) : sur le fond noir, des bulles presque noires ou
    vert sombre ne se voient pas.
  - D2. **Aucun but ni fin** : un seul but à atteindre compté pour toute la partie, un but atteint par bulle ;
    l'image découverte n'apporte rien (pas de « Bravo »).
  - D3. **Bulles portraits incohérentes** (captures) : les images à fond transparent (OpenMoji) flottent sans bulle,
    les autres sont sur un disque blanc ; rien ne montre que ce sont des bulles. Elles lisent l'ancien dossier
    partagé `portraits` (règle du 29/10 : à remplacer par une variante « Mes images » propre au jeu).
  - D4. **Rejeu** (fait, code) : le constructeur d'une partie rejouée ne crée pas la liste des pavés : avec le fond
    activé, le rejeu planterait (hypothèse forte, non essayée ; le jeu n'est pas dans la liste du rejeu).
  - D5. Variante « fixe » : la bulle ne grossit qu'avec la souris (le regard passe par l'indicateur de JavaFX), et
    écrit « fixe » en minuscule ; 15 s fixes divisées par la vitesse d'animation.
  - D6. Fin de partie mal rangée : `dispose` vide l'écran mais laisse tourner les animations (attente des bulles
    fixes relancée sans fin) ; chaque bulle relance les limiteurs et `gameContext.start()` (10 fois).
  - D7. Graphisme ancien : disques plats, fond noir, pavés noirs ; en thème clair, l'image n'est qu'à 10 %
    d'opacité.
- **Images et sons (licences)** : **à risque**. Les 3 fonds sous-marins sont de source inconnue (l'un porte
  l'autrice « Koshkina L », illustratrice de banques d'images) ; les 2 vignettes du menu aussi. Les 2 sons viennent
  de SoundBible (`Blop-Mark_DiAngelo`, `Large-Bubble`) : licence non notée (sur SoundBible, selon le son :
  domaine public, CC BY 3.0 ou droits réservés ; hypothèse à vérifier, plus simple de les refaire).
- **Propositions** (à valider ✔ / refuser ✘ ; elles dépendent des questions plus bas) :
  - Images et sons
    - I1. **Fonds sous-marins dessinés par script** (3 : récif, épave, algues), libres ; vignettes refaites
      (captures).
    - I2. **Vraies bulles** dessinées par le jeu : bulle de savon translucide avec un reflet, couleurs vives tirées
      dans une palette (plus de couleurs sombres) ; dans les bulles à images, l'image ronde dans la bulle.
    - I3. « Plop » refait par script (2 ou 3 sons proches, tirés au hasard).
  - Graphisme
    - G1. L'image cachée sous des pavés en verre dépoli (on devine l'image) plutôt que noirs, avec un compte
      « 12 / 36 » en haut ; éclatement en gouttes de la couleur de la bulle.
  - Gameplay
    - P1. **Une seule carte « Bulles »** : en bas de la fenêtre des variantes, le contenu (Couleurs, Animaux
      OpenMoji, **Mes images** : dossier `files/images/Bubbles`, ligne dans les réglages, comme Opinions) ; en
      boutons, le sens (monte, descend, vers la droite, vers la gauche, immobiles).
    - P2. **Une fin** : l'image toute découverte (36 bulles éclatées), « Bravo », puis une nouvelle image ; ou les
      statistiques (au choix du mainteneur).
    - P3. Option « Éclater dès qu'on regarde » / « Éclater après une fixation » (anneau), pour le niveau de la
      personne.
  - Corrections
    - P4. D4 (rejeu), D5 (le regard fait grossir la bulle fixe comme la souris), D6 (tout arrêter à la fin), un but
      à atteindre par bulle à éclater.
- **Questions au mainteneur** (la règle d'abord) :
  1. **À quoi sert le jeu ?** Découverte libre (cause-effet : je regarde, ça éclate) sans fin, ou avec un but
     (découvrir l'image cachée, puis « Bravo ») ?
  2. **Une seule carte** avec le contenu en variante (couleurs, animaux, mes images), ou garder deux cartes ?
  3. **Éclater** : dès que le regard entre dans la bulle (comme maintenant), après une fixation, ou les deux au
     choix ?
  4. **L'image cachée** : la garder ? Toujours visible derrière (sans pavés), ou découverte morceau par morceau ? Et
     une fois découverte : nouvelle image, ou statistiques ?
  5. **Nombre et vitesse des bulles** : 10, à la vitesse d'animation des réglages, ou des variantes (lent, normal,
     rapide ; peu ou beaucoup de bulles) ?
- **Réponse du mainteneur (2/11/2026)** : **une seule carte** ; **découvrir l'image, puis une nouvelle image** ;
  éclater **dès le regard ou après une fixation, au choix**. (Question 5 sans réponse : 10 bulles gardées, à la vitesse
  d'animation des réglages.)
- **Fait (2/11/2026)** :
  - **Une seule entrée** `Bubbles` (« Bulles ») : `ColoredBubbles` et `PortraitBubbles` retirés du catalogue (69
    entrées). Variantes `GroupedGameVariant` : le sens en boutons (monte, descend, vers la droite, vers la gauche,
    immobiles) ; en bas, la façon d'éclater (`BubblesBurst` : dès qu'on les regarde, après une fixation avec
    l'anneau) et le contenu (`BubblesContent` : Couleurs, Animaux marins, **Mes images** : dossier
    `files/images/Bubbles`, ligne dans les réglages et message dans la fenêtre des variantes sans image). Une
    ancienne variante (un sens seulement) se joue en couleurs, éclatement immédiat. Le jeu ne lit plus le dossier
    partagé `portraits`.
  - **L'image à découvrir** : toujours là (plus besoin du fond des réglages), sous 36 vitres dépolies jointives (on
    la devine) ; chaque bulle éclatée en enlève une au hasard (fondu), « 12 / 36 » en haut. Les 36 enlevées :
    carillon, « Bravo », puis une nouvelle image (paysages dessinés, ou les images de l'utilisateur, dans un ordre
    tiré au hasard) ; pendant « Bravo », une bulle éclate sans compter. Un but à atteindre par vitre, un but atteint
    par bulle éclatée.
  - **Vraies bulles** : film de savon translucide, reflet, couleurs vives d'une palette (plus de couleurs sombres) ;
    animal entier ou photo de l'utilisateur ronde à l'intérieur. Éclatement : gouttes de la couleur de la bulle,
    3 « plop » tirés au hasard. Une seule boucle de jeu en temps réel ; les bulles ondulent un peu ; immobiles, elles
    flottent, changent de place toutes les 15 s, jamais l'une sur l'autre, ni sous l'engrenage ni sous le compte.
    Pause pendant le menu de l'engrenage ; tout est arrêté à la fin (D6). Le regard et la souris font la même chose
    (D5) ; plus de plantage au rejeu (D4 : plus de liste de pavés à part).
  - Images (`make_bubbles_images.py`) et sons (`make_bubbles_sounds.py`) ; les 3 anciens fonds, les 2 sons
    SoundBible et la vignette `bubble.png` retirés ; une vignette refaite (`bubblecolor.png`, capture, 960 × 540).
    Traductions (nom « Bulles », contenus, façons d'éclater ; « fixe » devient « immobiles ») ; aide
    (`Bubbles-fra.txt`, `-eng.txt`) ; crédits ; inventaire. Les clés `ColoredBubbles` et `PortraitBubbles` sont
    retirées (les autres langues n'ont plus le nom traduit : seulement français et anglais).
- **Vérifications** : `BubblesGameTest` (5 tests : chaque bulle enlève une vitre, l'image trouvée puis une nouvelle,
  rien compté pendant « Bravo » ; éclatement immédiat ou après fixation ; sens des bulles et retour ; « Mes images »
  cachées, sinon les paysages ; variantes et ancienne variante) ; `GameHelpTest` avec `Bubbles` ; captures en vraie
  fenêtre (`build/game-screenshots/essai1/bubbles/`). Pas d'essai au Tobii : à faire par le mainteneur.

### Egg Game (`EggGame`, `PersonalizeEggGame`, code `egg`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/egg/` (1366 × 768,
mode souris, français ; Jeu de l'œuf en 3 étapes, Jeu de l'œuf personnalisé en 4 étapes « Image rétrécie » ; le
pointeur entre dans l'œuf, y reste 2,7 s, sort, revient ; planches `*-planche.png`). Pas d'essai avec le Tobii.

- **Ce qu'il fait** (lecture du code : 8 classes, 521 lignes ; et captures) : **un seul œuf** au milieu ; chaque
  fixation sur lui (durée des réglages, indicateur de JavaFX jaune qui finit par une coche « Terminé ») le fêle un peu
  plus (« crac »), en 2 à 5 étapes selon la variante ; à la dernière, il s'ouvre et montre l'image cachée (son), puis
  « Bravo » et **l'écran des statistiques : la partie s'arrête après un seul œuf**. Il faut sortir de l'œuf et y
  revenir pour chaque étape. Variante « Image rétrécie » : l'œuf rapetisse à chaque étape.
- **Deux entrées au catalogue** : **Jeu de l'œuf** (un poussin sort, photo) et **Jeu de l'œuf personnalisé** (une
  image du dossier partagé `blocs` de l'utilisateur, sinon un des animaux par défaut). Fenêtre des variantes : 8
  boutons « 2 » à « 5 » et deux boutons ronds « Classique » / « Image rétrécie ». Les deux jeux sont dans la liste du
  rejeu.
- **Défauts vus** :
  - D1. **Une seule image par partie, puis les statistiques** : très court (moins de 15 s).
  - D2. **Photos sur fond blanc** dans un écran noir : l'œuf est un rectangle blanc ; l'image personnalisée aussi
    (un crabe OpenMoji sur un carré blanc).
  - D3. Indicateur de fixation de JavaFX (camembert jaune, coche, mot « Terminé » minuscule) posé sur l'œuf.
  - D4. Le jeu personnalisé lit l'ancien dossier partagé `blocs` (règle du 29/10 : à remplacer par une variante « Mes
    images ») ; le tirage de l'image n'est pas rejouable (graine ignorée).
  - D5. Taille liée à la scène entière (moitié de sa hauteur), pas à la zone de jeu ; « Image rétrécie » : l'œuf finit
    petit (0,9 par étape).
  - D6. Le texte de la fenêtre (« Choose number of steps... ») n'est traduit qu'en anglais, biélorusse et chinois.
- **Images et sons (licences)** : **à risque**. Les 3 photos (œuf, œuf fêlé, poussin) portent l'autrice « FRIEDMANN
  Chloe » dans leurs métadonnées, sans licence ; les 2 sons (`1.mp3`, `2.mp3`) sont de source inconnue ; vignette
  sans source.
- **Propositions** (à valider ✔ / refuser ✘ ; elles dépendent des questions plus bas) :
  - Images et sons
    - I1. **Œuf dessiné** par le jeu ou par script, avec des fêlures qui s'agrandissent à chaque étape, puis la
      coquille qui s'ouvre en deux ; sur un fond doux (nid de paille, prairie), plus de rectangle blanc.
    - I2. **Ce qui sort de l'œuf** : des animaux qui naissent vraiment d'un œuf, en OpenMoji (poussin, caneton,
      tortue, crocodile, serpent, dinosaure, manchot...), ou les images de l'utilisateur (« Mes images », dossier
      `files/images/EggGame`).
    - I3. Sons faits par script : fêlure (« crac ») à chaque étape, éclosion joyeuse à la fin.
  - Graphisme
    - G1. Anneau de fixation de la couleur principale autour de l'œuf (comme les jeux revus) ; l'œuf qui tremble un
      peu à chaque fêlure ; l'animal qui sort avec un petit saut.
  - Gameplay
    - P1. **Une seule carte « Jeu de l'œuf »** : le nombre d'étapes en boutons (2 à 5), en bas ce qui sort (animaux
      ou mes images) et peut-être le mode « rétrécit ».
    - P2. **Une série d'œufs** (par exemple 5, un animal différent chacun), puis « Bravo » et les statistiques ; ou
      sans fin.
    - P3. Option : rester sur l'œuf le fêle étape après étape (pas besoin de sortir et revenir), plus facile.
  - Corrections
    - P4. D4 (rejeu de l'image), D5 (taille de la zone de jeu), D6 (traductions).
- **Questions au mainteneur** (la règle d'abord) :
  1. **Combien d'œufs par partie ?** Un seul puis les statistiques (comme maintenant), une série (combien ?), ou
     sans fin (un nouvel œuf après chaque éclosion, on quitte par l'engrenage) ?
  2. **Une seule carte** avec « Animaux » / « Mes images » en variante ?
  3. **Fêler l'œuf** : il faut sortir et revenir à chaque étape (comme maintenant), ou rester dessus suffit (une
     fêlure à chaque durée de fixation) ?
  4. **Le mode « Image rétrécie »** sert-il (quel but) ? Le garder ?
  5. **Ce qui sort** : des animaux qui naissent d'un œuf (poussin, tortue, crocodile...) vous conviennent-ils ?
- **Réponse du mainteneur (2/11/2026)** : **une seule carte** ; **sans fin** ; **rester sur l'œuf suffit** ; le mode
  « Image rétrécie » n'est **pas gardé** ; d'accord pour les animaux qui naissent d'un œuf.
- **Fait (2/11/2026)** :
  - **Une seule entrée** `EggGame` (« Jeu de l'œuf ») : `PersonalizeEggGame` retiré du catalogue (68 entrées).
    Variantes `GroupedGameVariant` : 2, 3, 4 ou 5 étapes en boutons ; en bas, « Animaux » ou **« Mes images »**
    (dossier `files/images/EggGame`, ligne dans les réglages, message dans la fenêtre des variantes sans image). Une
    ancienne variante (étapes et « Classique » ou « Image rétrécie ») garde son nombre d'étapes, avec les animaux.
    **Rejeu** : une partie enregistrée du jeu personnalisé est rejouée avec « Mes images » et son nombre d'étapes
    (`ReplayingGameFromJson.formerGame`) ; le jeu ne lit plus le dossier partagé `blocs`.
  - **Déroulement** : l'œuf dessiné par le jeu (forme d'œuf, dégradé crème, taches) dans un nid de paille, le bord
    avant du nid devant lui. Regarder l'œuf : l'anneau de la couleur principale en fait le tour ; à la fin, une
    fêlure de plus (la ligne en zigzag grandit depuis le milieu, avec de petites branches), « crac », l'œuf tremble ;
    **rester dessus relance la fixation** sans regarder ailleurs ; quitter l'œuf arrête l'anneau. À la dernière
    étape : l'œuf se coupe le long de sa fêlure, le haut s'envole en tournant, l'animal (ou l'image de
    l'utilisateur, dans un cadre rond) monte et sautille, son d'éclosion ; « Éclos : 3 » en haut ; 3,2 s après, un
    nouvel œuf, **sans fin** (plus d'écran des statistiques après un œuf). Ce qui sort vient dans un ordre tiré au
    hasard, rejouable. Un but à atteindre par étape, un but atteint par fixation réussie.
  - Taille liée à la zone de jeu (D5) ; traductions des variantes ; les clés `PersonalizeEggGame`, `ImageShrink`
    et « Choose number of steps » retirées ; branches propres à l'œuf retirées de `GameVariantDialog`.
  - Images (`make_egg_images.py` : prairie avec nid, bord avant du nid, 14 animaux OpenMoji : poussin, canard,
    tortue, crocodile, serpent, deux dinosaures, manchot, lézard, oiseau, chouette, perroquet, cygne, flamant) et sons
    (`make_egg_sounds.py` : fêlure, éclosion) ; les 3 photos et les 2 sons retirés ; vignette refaite (capture,
    960 × 540) ; aide (`EggGame-fra.txt`, `-eng.txt`) ; crédits ; inventaire.
- **Vérifications** : `EggGameTest` (4 tests : éclosion après le nombre d'étapes puis nouvel œuf, sans statistiques ;
  rester sur l'œuf le fait éclore, le quitter tout de suite ne le fêle pas ; « Mes images », sinon les animaux ;
  variantes et ancienne variante) ; `ReplayingGameFromJsonTest` (ancien jeu personnalisé) ; `GameHelpTest` avec
  `EggGame` ; captures en vraie fenêtre (`build/game-screenshots/essai1/egg/`, `essai2/egg/`). Pas d'essai au Tobii :
  à faire par le mainteneur.
- **Retour du mainteneur (2/11/2026)** : le jeu marche bien ; seul point, il fallait écrire « œuf » dans la barre
  de recherche du menu pour le trouver (« oeuf » ne le trouvait pas). **Fait** pour tous les jeux : la recherche ne
  tient plus compte des majuscules, des accents, de « œ » / « æ » (écrits « oe » / « ae ») ni de l'apostrophe
  courbe (`HomeMenuScreen.searchable`, test `HomeMenuSearchTest`).

### Shoot The Biboules et Shoot The Robots (`ShootTheBiboules`, `ShootTheRobots`, code `shooter`) — 2/11/2026

**Étape 1 (état des lieux) faite le 2/11/2026.** Captures « avant » : `build/game-screenshots/avant/shooter/`
(1366 × 768, mode souris, français ; déroulé seconde par seconde, pointeur fixe à droite du centre ; planches
`biboules-planche.png`, `robots-planche.png`). Pas d'essai avec le Tobii.

- **Ce qu'ils font** (lecture du code : 7 classes, 893 lignes, un seul jeu `ShooterGame` pour les deux ; et
  captures) : une cage (Biboules) ou une soucoupe (robots) se promène en haut de l'écran ; des cibles en sortent une
  à une (5 à la fois), changent de couleur (bleu, vert, jaune, orange, rouge) et grossissent jusqu'à 10 fois en
  4 à 7 s en filant vers un bord. Une cible **est touchée dès que le regard (ou la souris) entre dedans** (pas de
  fixation) : éclair, bruit, la baguette (Biboules, dans une salle de classe) ou le canon (robots, dans une rue)
  tourne vers elle, +1 au score. **Pas de fin, pas de variante** (le jeu démarre directement depuis le menu).
- **Défauts vus** :
  - D1. **Score faux de 1** (fait, code) : le texte affiche la valeur d'avant le tir (`score++`) : le premier tir
    laisse « Score:0 ».
  - D2. **Score illisible pour les robots** (captures) : seul « Score: » se lit sur le panneau jaune ; le nombre,
    blanc, ne se voit pas. Pour les Biboules, la **date du jour** est écrite au tableau au-dessus du score (sans
    raison) ; police « AR BLANCA » (absente sous Windows et Linux).
  - D3. **Cibles énormes** : une cible grossit jusqu'à 10 fois sa taille ; près des bords, des Biboules géantes
    coupées cachent une partie de l'écran (captures).
  - D4. Positions bricolées (fractions comme 8,9/29,7 de la largeur) ; la cage passe parfois sous le tableau ou la
    soucoupe sort de l'écran en haut.
  - D5. Le déplacement de la cage utilise un `java.util.Timer` (règle du projet : `PauseTransition`) ; le rejeu ne
    relance pas les limiteurs (deux constructeurs différents) ; écouteurs de redimensionnement jamais retirés.
  - D6. Pas de durée de fixation ni de vitesse (la vitesse d'animation des réglages joue sur les cibles, mais pas
    sur la cage).
- **Images et sons (licences)** : 20 dessins de 2018 (salle de classe, rue, cage, soucoupe, Biboules de 5 couleurs,
  robots de 5 couleurs, éclair, bras et baguette, canon), ajoutés par l'auteur du jeu sans source : **probablement
  faits pour le jeu** (la Biboule est la mascotte du projet) ; 6 sons de tir (`hand_sound1-3.mp3`, deux jeux) de
  source inconnue ; deux musiques d'OpenGameArt (« Talking Cute Chiptune », « DST Tower Defense Theme »), licence à
  vérifier (hypothèse : CC BY ou CC0).
- **Propositions** (à valider ✔ / refuser ✘ ; elles dépendent des questions plus bas) :
  - Images et sons
    - I1. Garder les Biboules et les robots (dessins du projet, à faire confirmer) ; redessiner les deux décors en
      plus simple et plus doux (classe, rue) ou les remplacer par des décors dessinés par script ; sons de tir
      refaits par script.
  - Graphisme
    - G1. Score dans une étiquette (pastille) en haut, lisible, sans date ; taille des cibles limitée (3 fois au
      plus) ; anneau de fixation si un mode avec fixation est choisi.
  - Gameplay
    - P1. **Une seule carte « Tir »** avec le décor en variante (Biboules dans la classe, robots dans la rue), ou
      garder deux cartes.
    - P2. **Variantes** : vitesse (lent, normal, rapide) et nombre de cibles à la fois ; tir dès qu'on regarde ou
      après une fixation (comme Bubbles).
    - P3. **Une fin** (par exemple 20 cibles touchées : « Bravo » puis les statistiques) ou sans fin.
  - Corrections
    - P4. D1 (score), D4 (positions), D5 (`PauseTransition`, rejeu), D6.
- **Questions au mainteneur** (la règle d'abord) :
  1. **Une seule carte** avec le décor en variante, ou garder Biboules et robots séparés ?
  2. **Fin de partie** : sans fin (comme maintenant), ou un nombre de cibles à toucher (combien ?), ou une durée ?
  3. **Tirer** : dès que le regard entre dans la cible (comme maintenant), après une fixation, ou les deux au choix ?
  4. **Vitesse et nombre de cibles** : des variantes (lent / normal / rapide) ?
  5. **Les dessins** (Biboules, robots, décors) : les garder tels quels, les retoucher, ou les refaire ?
- **Réponse du mainteneur (2/11/2026)** : **une seule carte** ; tirer **dès le regard ou après une fixation, au
  choix** ; **fin de partie au choix** : sans fin, 50 ou 100 cibles, 1 ou 3 minutes. **Dessins** : avis demandé ;
  avis donné et validé (« fais comme tu proposes ») : garder les Biboules et les robots (dessins versés au projet en
  2018 par un contributeur, sous la licence du projet ; hypothèse : faits par lui, à faire confirmer par l'auteur si
  possible), refaire les deux décors par script (plus détaillés, plus à risque) et les sons ; garder la musique si sa
  licence est libre. (Vitesse : sans réponse, celle des réglages.)
- **Fait (2/11/2026)** :
  - **Une seule entrée** `Shooter` (« Tir ») : `ShootTheBiboules` et `ShootTheRobots` retirés du catalogue (67
    entrées). Variantes `GroupedGameVariant` : la fin en boutons (`ShooterEnd` : sans fin, 50 cibles, 100 cibles,
    1 minute, 3 minutes) ; en bas, la façon de tirer (`ShooterShot`) et le décor (`ShooterTheme` : les Biboules dans
    la classe, les robots au-dessus de la rue). Statistiques gardées sous leurs noms d'avant (`biboule`, `robot`).
    Les deux jeux n'étaient pas dans la liste du rejeu.
  - **Déroulement** : une seule boucle en temps réel (plus de `java.util.Timer`), à la vitesse d'animation des
    réglages, arrêtée pendant le menu de l'engrenage (le temps aussi). La cage (ou la soucoupe) va et vient en haut
    en ondulant ; 5 cibles en sortent tour à tour vers un point de l'écran (ni sous le score, ni sous la baguette),
    changent de couleur et grossissent **2,6 fois au plus** (10 fois avant, D3) ; non touchée, une cible s'efface et
    ressort. Touchée : éclair, petit saut, son, +1 ; la baguette (ou le canon) se tourne vers elle. En mode fixation,
    l'anneau de la couleur principale fait le tour de la cible et **la cible s'arrête pendant la fixation** (sinon
    impossible à fixer : 0 point en 24 s à l'essai). **Score juste** (D1) dans une pastille en haut à droite
    (« Score : 12 / 50 »), **temps restant** en haut à gauche (variantes en minutes) ; plus de date (D2). Fin :
    « Bravo » puis les statistiques. Un but à atteindre par cible sortie, un but atteint par cible touchée.
  - Décors (`make_shooter_images.py` : salle de classe, rue) et sons (`make_shooter_sounds.py` : baguette magique,
    laser) refaits ; les 2 anciens décors, les 6 sons et la vignette des robots retirés ; vignette refaite (capture,
    960 × 540). **Musiques** : « Talking Cute (Chiptune) » (Pro Sensory, Alex McCulloch) et « Tower Defense Theme »
    (DST) sont sous **CC0** (vérifié sur leurs pages OpenGameArt) ; une seule carte, donc une seule musique : la
    première, créditée dans la licence affichée. Traductions, aide (`Shooter-fra.txt`, `-eng.txt`), crédits,
    inventaire (les 18 dessins gardés notés « libre probable, auteur à confirmer »).
- **Vérifications** : `ShooterGameTest` (6 tests : fin à 50 cibles et pas avant ; sans fin après 120 ; fin après
  1 minute, le temps comptant avec le jeu ; tir immédiat ou après fixation, la cible arrêtée pendant la fixation ;
  cibles qui sortent et grossissent, tout retiré à la fin ; variantes) ; `GameHelpTest` avec `Shooter` ; captures
  en vraie fenêtre (`build/game-screenshots/essai1/shooter/`). Pas d'essai au Tobii : à faire par le mainteneur.
- **Second avis du mainteneur (2/11/2026)** : pour être tout à fait sûr, **refaire nous-mêmes les dessins**, comme pour
  les autres jeux ; mais **la Biboule est un dessin du laboratoire, sa mascotte** : gardée. **Fait** : robots (5
  couleurs : tête, antenne, yeux et bouche de la couleur), soucoupe volante, cage dorée, éclairs (pâle pour les
  Biboules, rouge pour les robots), bras et baguette magique (étincelle au tir), canon (flamme au tir), dessinés par
  `make_shooter_images.py` aux mêmes proportions que les anciens ; les 5 Biboules gardées. Inventaire : tout « libre ».
- **Biboule modernisée (3/11/2026, choix du mainteneur)** : version A de la planche du 2/11 (ombres douces, contour
  fin de la couleur du corps, joues roses), avec **la bouche d'origine** (deux petits sourires qui se rejoignent en
  pointe, sur une lèvre ronde de la couleur d'origine : verte sur la bleue, saumon sur la verte, rose sur la jaune,
  rose-violet sur l'orange, violette sur la rouge) et **les ailes d'origine** (feuille ovale, contour bleu foncé,
  nervure centrale et nervures latérales). Dessinée par `make_shooter_images.py` (`biboule`), en 500 × 400 au lieu de
  335 × 250 : le jeu garde les proportions dans un cadre de 1,25 × la taille, le rapport exact du nouveau format.
  **Dans le Tir seulement** pour l'instant (décision du mainteneur) ; les autres jeux gardent la Biboule d'origine.
  Planche : `build/game-screenshots/propositions/biboule-A2.png`. Originales : `gradle/images/biboule-originale/`.

### Survive Against Robots (`SurviveAgainstRobots`, `SurviveAgainstRobotsMouse`, code `surviveAgainstRobots`) — 3/11/2026

- **État des lieux (3/11/2026)** : deux entrées (clavier ; regard ou souris), 6 variantes chacune (Facile, Normal,
  Difficile ; tir « auto » ou non). Le joueur, une Biboule sans ailes, se déplace parmi des robots (1 au départ, un de
  plus tous les 40 points, 3 au plus) ; au clavier il tirait dans le sens de la marche, seulement en bougeant ; au
  regard, vers le point regardé ; en « auto », vers le robot le plus proche. **Une seule vie**. Score : 1 point toutes
  les 3 s et 6 par robot (la boucle comptait 6 au lieu de 5). Bonus 1 fois sur 6 : tir rapide, bouclier, ralenti
  (pas pour les robots arrivés ensuite). Défauts : temps compté en images (60 par seconde), jeu pas arrêté par
  l'engrenage, départ toujours en (500, 500), placement des robots par appel récursif sans fin possible, touches tenues
  ensemble mal gérées (← et → : lâcher → arrêtait le joueur), `setOnKeyReleased` de la scène remplacé, score en Arial
  gris, pas d'aide. Images : 12 dessins de source inconnue (2023).
- **Règle du mainteneur (3/11/2026)** : **une seule carte** ; **3 vies** ; fins **sans fin, 1 ou 3 minutes, 20 ou 50
  robots** ; joueur : **la nouvelle Biboule du Tir** ; robots : **ceux du Tir** (uniformité) ; décor retravaillé
  comme les autres jeux : des 3 propositions (planche `build/game-screenshots/propositions/survive-decors.png` : ciel,
  jardin, cour de l'école), **les 3 gardées, une tirée au hasard à chaque partie**. Propositions validées : regard ou
  clavier × tir automatique ou vers le regard (ou vers l'avant), Facile / Normal / Difficile (2, 3, 4 robots au plus ;
  aucun, 1 sur 5, 1 sur 3 qui tirent : les rouges), un robot de plus tous les 5 détruits, score = robots détruits,
  bonus gardés et redessinés, **cœur** ajouté (une vie de plus).
- **Fait** :
  - **Une seule entrée** `SurviveAgainstRobots` (catalogue : 66 entrées) : `SurviveAgainstRobotsMouse` et les deux
    anciennes énumérations retirées ; les anciennes variantes enregistrées (classe retirée) se lisent comme aucune
    variante : niveau Normal, regard, tir automatique, sans fin. Statistiques sous le même nom (`SurviveAgainstRobots`).
    Les cas particuliers de `GameVariantDialog` retirés ; fenêtre des variantes des jeux revus (boutons : la fin ; en
    bas : le niveau, puis la façon de jouer), 60 variantes (`SurviveLevel`, `SurviveControl`, `SurviveEnd`).
  - **Le jeu réécrit** (`SurviveAgainstRobots`, classes internes `Robot`, `Shot`, `Bonus`) : une seule boucle en temps
    réel, à la vitesse d'animation des réglages, arrêtée pendant le menu de l'engrenage (le temps aussi) ; tailles et
    vitesses en hauteur d'écran ; départ au centre ; place d'un robot : la plus éloignée de 20 essais (plus de boucle
    sans fin) ; un robot arrive en 0,6 s (inoffensif et pas visé pendant ce temps). Clavier : flèches, ZQSD, WASD,
    touches tenues ensemble gérées (filtres ajoutés à la scène, retirés à la fin). Regard (ou souris) : la Biboule va
    vers le point regardé ; en « tir vers le regard », **une fois arrivée elle tire vers l'avant** (sinon, le regard
    sur elle, elle ne tirait plus : trouvé à l'essai en vraie fenêtre, 0 robot en 16 s).
  - **3 vies** (cœurs en haut à gauche) : touchée par un robot ou une boule rouge, un cœur de moins et 2 s de
    clignotement sans rien qui touche ; les 3 perdues : éclair, « Perdu ! » (carte des jeux), 2 s, les statistiques.
    Fin atteinte : « Bravo » puis les statistiques. Pastilles : « Robots : 12 / 20 » en haut à droite, le temps au
    milieu ; bonus en cours sous les cœurs, avec leurs secondes.
  - **Bonus** (1 fois sur 6 par robot détruit, 8 s au sol, clignotant les 3 dernières) : éclair (tirs 2 fois plus
    rapides), bouclier (bulle de la couleur principale), escargot (robots et leurs boules ralentis, y compris ceux qui
    arrivent), 8 s ; cœur (une vie, proposé seulement si une vie manque).
  - Images : `make_survive_images.py` (ciel, jardin, cour ; 4 bonus) ; Biboule, robots, éclairs et sons du Tir
    (`data/shooter`) ; les 12 anciens dessins retirés ; vignette composée par le même script (ciel, Biboule, robots,
    tirs, éclair, bonus). Traductions (anciennes clés retirées), aide (`SurviveAgainstRobots-fra.txt`, `-eng.txt`),
    crédits, inventaire.
- **Vérifications** : `SurviveAgainstRobotsTest` (12 tests : 3 vies et clignotement ; contact d'un robot ; tir
  automatique compté ; fin à 20 robots ; fin à 1 minute et pas sans fin après 120 s ; robots selon le niveau, aucun
  tireur en Facile ; boules d'un robot rouge ; bonus cœur, bouclier, bonus qui s'en va ; clavier et regard, tir une
  fois arrivée ; décor au hasard parmi 3 ; variantes ; tout retiré à la fin) ; `GameHelpTest` avec le jeu. Captures en
  vraie fenêtre (souris) : `build/game-screenshots/essai1/survive/` et `essai2/survive/` (jardin et cour tirés ; le ciel
  pas sorti en 9 parties, tirage vérifié à part : chaque décor un tiers du temps sur 100 000 graines comme celles de
  l'horloge). `build` complet : 707 tests, 0 échec ; `allGamesTest -PallGamesOnly=SurviveAgainstRobots -PallGamesRepeat=3` réussi. Pas d'essai au clavier en vraie fenêtre ni au Tobii : à faire par le mainteneur.
- **Remarques du mainteneur sur la fenêtre des variantes (3/11/2026, avant son essai)** : dans l'aide, dire ce que
  fait chaque niveau ; **retirer le choix regard / clavier**, ne garder que **tir automatique** et **tir vers
  l'avant** ; le jeu s'adapte au réglage des entrées (souris, Tobii, les deux), car un utilisateur en souris seule qui
  choisissait le regard sans y faire attention était coincé. **Fait** : `SurviveControl` remplacé par `SurviveShot`
  (automatique, vers l'avant : dans le sens où va la Biboule, ou est allée en dernier), 30 variantes ; **le clavier
  retiré** ; la Biboule suit la souris et le regard sans rien lire du réglage : avec la souris seule aucun regard
  n'arrive, avec le Tobii seul la zone de jeu arrête la souris (`GameContextFactory.applyInputMode`), avec les deux
  elle suit le dernier qui a bougé. Aide : une ligne par niveau (robots à la fois, robots qui tirent) et par tir, et
  le rôle du réglage des entrées. Tests mis à jour (souris par un vrai `MouseEvent`, tir vers l'avant dans le sens
  du dernier déplacement).
- **Essai du mainteneur (3/11/2026)** : tout est bon ; commité et poussé.

### Space Game (`SpaceGame`, code `space`) — 3/11/2026

- **État des lieux (3/11/2026)** : une carte, Facile / Normal / Difficile, qui ne changeaient que la vitesse des
  lasers du joueur (0,5 s, 2 s, 4 s pour traverser l'écran). Fusée en bas qui suit le regard ou la souris (règle 2
  déjà suivie), tir tout seul ; 10 extraterrestres qui vont et viennent et tirent au hasard (1 chance sur 1 500 par
  image) ; un boss de 125 points de vie au 24ᵉ extraterrestre ; score 1 par extraterrestre, 125 par boss ; **une
  seule vie**, puis score, meilleur score (`space-game/highscore.dat`) et bouton pour rejouer, jamais l'écran des
  statistiques. Défauts : revisionnage cassé (extraterrestres lus dans `space/biboule`, qui n'existe pas) ; à chaque
  image, de nouveaux écouteurs de collision pour chaque paire laser–extraterrestre, jamais retirés (risque de
  ralentissement, non mesuré) ; tirs comptés en images ; pas de pause à l'engrenage ; pas d'aide. Images : 12 de
  source inconnue (les lasers portent les noms de fichiers de Kenney, CC0 : hypothèse non vérifiée), vignette qui
  ressemble à une photo du commerce.
- **Règle du mainteneur (3/11/2026)** : comme Survivre contre des robots : **3 vies**, fins sans fin, 1 ou 3 minutes,
  20 ou 50 extraterrestres ; **le boss arrive à la fin du temps ou au nombre d'extraterrestres, et le battre fait
  gagner** ; sans fin, pas de boss ; **boss de 15, 30 ou 50 points de vie** selon le niveau ; tout redessiner ;
  d'accord pour retirer les niveaux par la vitesse des lasers. Planche `build/game-screenshots/propositions/
  space-propositions.png` : **fond A (nuit douce) choisi** (pas le pastel) ; dessins, niveaux (4, 6, 8
  extraterrestres ; un tir toutes les 6, 4, 2,5 s environ) et retrait du meilleur score validés.
- **Fait** : le jeu réécrit (`SpaceGame`, classes internes `Alien`, `Boss`, `Shot` ; `Alien`, `Boss` et
  `SpaceGameVariant` retirés) : une seule boucle en temps réel, vitesse d'animation des réglages, arrêtée pendant le
  menu de l'engrenage ; collisions calculées à chaque pas (plus d'écouteurs) ; revisionnage réparé (même graine). La
  fusée va en bas vers le regard ou la souris ; lasers toutes les 0,45 s, même vitesse à tous les niveaux. Un
  extraterrestre arrive en 0,6 s (ni touché ni tireur pendant ce temps). Le boss : les autres extraterrestres
  s'effacent, il descend en 1,5 s (ni touché pendant ce temps), va et vient, lâche 3 boules toutes les 1,3 s, jauge
  de vie au-dessus de lui ; battu : éclair, « Bravo », statistiques. 3 cœurs, 2 s de clignotement ; « Perdu ! ».
  Pastilles « Extraterrestres : 12 / 20 », le temps au milieu, puis « Le boss ! ». Statistiques : un but à atteindre
  par extraterrestre venu et pour le boss, un but atteint par extraterrestre détruit et pour le boss (le nom
  `space-game` gardé ; avant, les buts atteints valaient le score). **Le meilleur score n'est plus lu ni écrit** :
  les fichiers `highscore.dat` déjà écrits restent sur le disque, sans servir. Variantes : `GroupedGameVariant`
  (`SpaceLevel`, `SpaceEnd`), 15 ; une ancienne variante se joue en Normal, sans fin. Images : `make_space_images.py`
  (nuit, fusée, 5 soucoupes, boss ; vignette composée) ; éclair et sons du Tir ; les 12 anciennes retirées.
  Traductions, aide (une ligne par niveau : extraterrestres à la fois, tirs, vie du boss), crédits, inventaire.
- **Vérifications** : `SpaceGameTest` (8 tests : 3 vies et clignotement ; laser qui détruit ; boss après 20
  extraterrestres à chaque niveau, de 15, 30, 50 points de vie, pas touché en arrivant, battu : « Bravo » ; boss
  après 1 minute, aucun sans fin ; extraterrestres du niveau ; souris, fusée en bas ; variantes ; tout retiré à la
  fin) ; `GameHelpTest` avec le jeu ; capture en vraie fenêtre (souris) `build/game-screenshots/essai1/space/` ; `build` : 716 tests, 0 échec ; `allGamesTest -PallGamesOnly=SpaceGame -PallGamesRepeat=3` réussi.
- **Essai du mainteneur (3/11/2026)** : tout est bon. **Nom français** demandé : « La bataille de l'espace » (colonne `fra` de `SpaceGame` ; les autres langues gardent leur nom).

### Défendre le château (`TowerDefense`, code `TowerDefense`) — 3/11/2026

- **État des lieux (3/11/2026)** : une carte, 2 variantes (Carte 1, Carte 2), pas de niveau. Les ennemis suivent la
  route jusqu'au château, 10 vies ; vagues envoyées par une fixation sur un bouton (5 puis 10 ennemis, +25 % de vie
  toutes les 2 vagues, gros ennemis toutes les 5, rapides toutes les 10) ; 4 tours (simple, double, missiles, canon :
  25 ou 50 pièces, 50 au départ), revente ; blizzard (5 s, toutes les 30 s). Défauts : **la partie perdue montrait
  l'animation de victoire**, pas d'autre fin ni d'écran des statistiques, aucun but compté ; revisionnage impossible
  (statistiques enregistrées absentes) ; textes en dur en français (« Pas assez d'argent », « Dégâts », « Portée »,
  « Vitesse ») ; prix affichés écrits à part ; argent « 20.0 » ; police « Agency FB » (Windows) ; déplacements
  comptés en images, blizzard à l'horloge ; pas de pause à l'engrenage ; un missile qui explosait sans toucher
  restait ; traduction du bouton inversée (« Send next wave » en français) ; « château » sans accent dans le nom.
  Règle 2 déjà suivie. Images : 23 de source inconnue (tours et ennemis proches du pack de Kenney, CC0 : hypothèse ;
  icônes proches de Flaticon : risque). La tour canon a les mêmes valeurs que la tour simple (seule l'image change).
- **Règle du mainteneur (3/11/2026)** : fin « Perdu ! » à la dernière vie, victoire au choix sans fin, 10 ou 20 vagues
  (« Bravo ») ; **les vagues restent à la demande** (que l'utilisateur ne soit pas submergé et joue à sa façon), avec
  **une variante « vagues automatiques »** ; niveaux Facile / Normal / Difficile ; **le contenu gardé** (4 tours,
  revente, blizzard : le cœur du jeu) ; tout redessiner. La carte est tirée au hasard (les variantes n'ont que trois
  réglages, pris par la fin, le niveau et les vagues).
- **Fait** : variantes `GroupedGameVariant` (`TowerDefenseWaves`, `TowerDefenseLevel`, `TowerDefenseEnd`), 18 ; une
  ancienne variante (carte) se joue à la demande, en Normal, sans fin. Niveaux : 100 pièces et 15 vies, +15 % de vie
  toutes les 2 vagues ; 50 et 10, +25 % ; 40 et 5, +40 %. Vagues automatiques : 10 s après la fin de la précédente,
  « Prochaine vague dans 7 s » en haut, le bouton l'envoie plus tôt. Le jeu tourne par pas de 1/60 s de temps de jeu
  (les valeurs d'avant gardées), à la vitesse d'animation des réglages, arrêté pendant l'engrenage, blizzard compris
  (sa neige dessinée par le jeu, son délai en temps de jeu). « Perdu ! » puis statistiques ; victoire : « Bravo » puis
  statistiques ; un but à atteindre par ennemi envoyé, un but atteint par ennemi détruit ; revisionnage réparé
  (statistiques enregistrées, graine). Textes traduits, prix lus dans les tours (pastilles), argent en entier, polices
  des jeux, cercle de choix aux couleurs des menus, missiles retirés après leur explosion. Traductions : bouton
  remis à l'endroit, « château » accentué, clés `Map1`, `Map2` retirées. Images : `make_towerdefense_images.py`
  (22 ; vignette `TowerDefense.png` composée) ; les 23 anciennes retirées ; les 3 sons de source inconnue remplacés (voir plus bas ; sons
  non inventoriés). Aide (une ligne par niveau et par façon d'envoyer les vagues ; les tours verte et bleue décrites
  comme identiques).
- **Vérifications** : `TowerDefenseTest` (8 tests : perdu sans « Bravo » ; gagné après 10 vagues ; vagues à la demande
  jamais seules, automatiques après 10 s et après la fin de la précédente, bouton plus tôt ; argent et vies du niveau ;
  construire, manquer d'argent, revendre ; blizzard qui fige 5 s et attend son délai ; les deux cartes au hasard ;
  variantes et statistiques du revisionnage) ; `GameHelpTest` avec le jeu ; capture en vraie fenêtre (souris, vagues
  automatiques) `build/game-screenshots/essai1/towerdefense/` : compte à rebours trop petit, corrigé. SpotBugs : division entière signalée (vie des vagues), écrite de façon explicite. `build` : 725 tests, 0 échec ; `allGamesTest -PallGamesOnly=TowerDefense -PallGamesRepeat=3` réussi.
- **Retours du mainteneur (3/11/2026, avant son essai)** : donner un rôle à part à la tour bleue ; question sur les
  sons de source inconnue (réponse : pas grave tout de suite, mais un risque d'atteinte au droit d'auteur si un son
  n'est pas libre, comme pour les images ; la GPL ne couvre que ce que le projet a le droit de distribuer). **Fait** :
  **la tour bleue est un canon** (`CanonTower` : un tir toutes les 1,5 s au lieu de 0,67 s, 18 dégâts au lieu de 5,
  portée 4 au lieu de 3, boulet gros, foncé et plus lent, 35 pièces) ; **les 3 sons remplacés** par 4 sons faits par
  script (`gradle/images/make_towerdefense_sounds.py` : construction, construction de la tour double, tir, coup de
  canon) ; aide mise à jour (le rôle et le prix de chaque tour) ; test `shouldGiveTheCannonARoleOfItsOwn`.
  **À faire plus tard** (recommandation) : un inventaire des sons de tous les jeux, comme celui des images.
- **Retours du mainteneur (3/11/2026, à l'essai)** : l'indicateur de progression était l'ancien (le cercle jaune qui se
  remplit), il préfère **l'anneau** des jeux revus ; sur les emplacements tout à droite, **le magasin et le texte
  d'information sortaient de l'écran** ; en jeu, **on ne savait pas ce que font les tours**. **Fait** : l'anneau de
  Coloriage devient un composant commun (`net.gazeplay.components.FixationRing`, dans `gazeplay-games-commons` ;
  Coloriage l'utilise, sans changement) ; dans Défendre le château, toutes les fixations passent par lui (bouton des
  vagues, flocon, emplacements, tours du magasin, revente : `fixate`), plus aucun `ProgressIndicator`. Le magasin est
  décalé pour rester entier dans l'écran (son centre gardé à 2,5 cases des bords), sa zone de sortie aussi. **Une fiche
  à côté du magasin** (à droite, ou à gauche près du bord droit, toujours dans l'écran) dit ce que fait la tour
  regardée : son nom, son rôle, ses dégâts, sa portée, un tir toutes les combien de secondes, son prix ; sur une tour
  construite, la même fiche et ce que rend la revente. Les messages (« Pas assez d'argent ») restent dans l'écran.
  Aide mise à jour. Test `shouldKeepTheShopAndItsCardInsideTheScreenAndSayWhatTheTowersDo` (il a trouvé la zone de
  sortie qui dépassait d'une demi-case) ; capture en vraie fenêtre (souris, emplacement le plus à droite)
  `build/game-screenshots/essai2/towerdefense/` : anneau, magasin entier, fiche, construction, revente.
- **Retour du mainteneur (3/11/2026, à l'essai, souris)** : le magasin s'ouvrait, mais **se refermait dès que la souris
  bougeait dedans** : impossible de choisir une tour. Cause : le magasin se fermait quand la souris « sortait » de sa
  zone invisible, et JavaFX compte comme une sortie le passage sur une tour posée par-dessus (le regard, lui, teste
  la forme de chaque nœud à part : pas touché). Mes tests et ma première capture avaient la souris immobile.
  **Fait** : le magasin ne se ferme que si la souris est vraiment hors de sa zone (y compris en sortant par une tour) ;
  test `shouldKeepTheShopOpenWhileTheMouseMovesOverItsTowers` ; capture en vraie fenêtre avec une souris qui bouge
  (`build/game-screenshots/essai3/towerdefense/`, `m2-p00` à `p04`) : magasin ouvert pendant le passage d'une tour à
  l'autre, fiche qui suit, tour construite. `build` : 728 tests, 0 échec.
- **Retour du mainteneur (3/11/2026, à l'essai, Tobii)** : choix des emplacements parfait, mais **la fixation d'une tour
  du magasin s'arrêtait en moins d'une seconde** ; et **l'icône de revente ne correspondait pas à l'action**. Cause du
  premier : la zone de fixation de l'emplacement (2 cases autour de lui) reste sous le magasin et recouvre le bas des
  icônes ; le regard est testé sur chaque nœud à part, et la sortie du regard de cette zone (il bouge un peu) arrêtait
  l'anneau, celui de la tour compris (la souris, elle, ne voit pas cette zone, cachée par le magasin). **Fait** : seul
  le nœud dont la fixation tourne arrête l'anneau (`ringOwner`) ; test
  `shouldKeepTheFixationOfATowerWhenTheGazeLeavesThePlaceUnderTheShop`. Icône de revente redessinée : une tour, une
  flèche, des pièces (la tour devient des pièces).
- **Retour du mainteneur (3/11/2026)** : Tobii parfait maintenant ; nouvelle icône de revente bien mieux, mais **elle
  doit montrer la tour vendue** (pas toujours la verte), pour ne pas perdre l'utilisateur. **Fait** : une icône par
  tour (`sellBasic.png`, `sellDouble.png`, `sellMissile.png`, `sellCanon.png`, dessinées par le même script ;
  `sellTower.png` retiré), le magasin montre celle de la tour de l'emplacement ; test
  `shouldShowTheTowerSoldOnItsSellPicture`.
  Puis (remarque du mainteneur) : les pièces paraissaient plus bas que la tour et la flèche ; le corps de la tour, la flèche et le milieu de la pile de pièces alignés sur une même ligne.
- **Essai du mainteneur (3/11/2026)** : tout est bon (souris et Tobii) ; commité et poussé.
- **Régression trouvée par la CI (3/11/2026, run 41638608554, job `all-games`)** : **le jeu des Bouteilles (`Bottle`)
  ne démarrait plus** depuis `e56673de2` (La bataille de l'espace) : il chargeait le bouton « rejouer » dans
  `data/space/menu/restart.png`, retiré avec les images de ce jeu (« Invalid URL or resource not found » dans
  `BottleGame`). Mes `allGamesTest` du jour ne lançaient que le jeu revu ; la recherche des usages ne cherchait que
  le chemin complet, que Bottle construit en deux morceaux. **Fait** : Bottle prend la même image (même empreinte
  SHA-256) dans `data/biboulejump/menu/restart.png` ; recherche refaite sur les noms de dossiers et de fichiers de
  toutes les ressources retirées le 3/11 : aucun autre usage ; `allGamesTest` complet : 66 entrées, 0 échec.
  **Leçon** : après avoir retiré des ressources, lancer `allGamesTest` en entier, pas seulement le jeu revu.

### Biboule Jump (`BibJump`, code `biboulejump`) — 4/11/2026

- **État des lieux (lecture du code)** : la Biboule rebondit de nuage en nuage et suit le regard de côté ; une chute finit
  la partie (score, record, bouton pour rejouer) ; 2 variantes, avec ou sans nuages mobiles. Défauts : physique en pixels
  fixes (un saut monte 53 % d'un écran 1080p, 27 % d'un écran 4K, pour des nuages jusqu'à 45 % l'un de l'autre, donc
  inaccessibles en 4K) ; le jeu continue derrière l'engrenage ; score arrondi à chaque image ; « Nouveau record ! » à
  égalité, même à 0 ; Biboule déformée, nuage à moitié hors de l'écran à droite, tremplin minuscule ; nuages mobiles jamais
  arrêtés ; durée comptée deux fois ; ni aide ni test ; images et sons de source inconnue.
- **Règle du mainteneur (4/11/2026, recommandations validées)** : 3 vies ; après une chute, elle repart sur un nouveau
  nuage et garde ses points ; sans fin, 1 ou 3 minutes ; Facile / Normal / Difficile (nuages de 1/5, 1/7, 1/9 de la largeur ;
  écart maximal 30, 40, 45 % de la hauteur ; 0, 1 sur 4, 1 sur 2 nuages mobiles) ; la Biboule modernisée du Tir ; **record
  retiré** ; pas de « Mes images » (le jeu n'en lisait pas).
- **Fait** : le jeu réécrit (`BibouleJump`, classe interne `Cloud` ; `Platform`, `MovingPlatform`, `BibouleJumpVariant`
  et `HighScores`, plus utilisé, retirés), une boucle en temps réel, tout en parts de l'écran, vitesse d'animation des
  réglages, arrêt pendant l'engrenage, aucune animation qui survit au jeu. Attente de 3 s avant le premier saut, de 1,5 s après
  une chute (temps arrêté). Après une chute tous les nuages sont refaits, pour qu'un chemin existe toujours. 1 point par
  centième de hauteur gagné. Pas de fixation dans ce jeu, donc pas d'anneau : le bouton pour rejouer a laissé la place à
  l'écran des statistiques. Les fichiers `highscores.dat` déjà écrits restent sur le disque, sans servir. Clés
  `Highscore`, `New highscore!`, `With moving platforms` et `Without moving platforms` retirées. Les 5 anciennes Biboules du
  dossier et `restart.png` restent pour le Jeu de l'oie et les Bouteilles, qui les lisent.
- **Vérifications** : `BibouleJumpTest` (21 tests, dont un joueur simulé qui monte sans tomber à tous les niveaux, en
  1366 × 768, 1080p et 4K) ; captures sans fenêtre ; `build` complet : **809 tests, 0 échec** ; `allGamesTest` complet : 66
  entrées, 0 échec (après la livraison de la musique du Tir, voir ci-dessous). **Essayé et validé par le mainteneur le
  4/11/2026.**
- **Trouvé pendant `allGamesTest` (4/11/2026)** : le Tir télécharge sa musique sur opengameart.org
  (`ShooterGameSpecSource`, `backgroundMusicUrl`), avec une attente de 10 s. Le serveur n'a pas répondu à temps (`Read timed out`, cause exacte inconnue), ce qui est journalisé
  en erreur par `BackgroundMusicManager`, et le test a échoué ; le jeu, lui, n'a pas été arrêté. Une fois la musique en cache,
  plus de téléchargement. **Décision du mainteneur (4/11/2026)** : musique livrée avec le jeu
  (`data/shooter/music/TalkingCuteChiptune.mp3`, CC0 d'après sa page OpenGameArt), plus aucun accès au réseau.

### Jeu de la taupe (`WhacAMole`, code `moles`) — 4/11/2026

- **État des lieux (lecture du code)** : 10 trous ; une Biboule ou une image sort, une fixation la tape. Ni fin, ni
  niveaux, ni pénalité, pas de taupe. Défauts : cible dehors 2 à 4,5 s pour une fixation de 2 s par défaut (impossible à
  partir de 4,5 s) ; une cible de plus toutes les 0,5 s, jusqu'à 10 à la fois ; rien d'arrêté pendant l'engrenage (buts
  comptés quand même) ; minuterie `java.util.Timer` jamais arrêtée à la fin ; images déformées ; fixation qui survit à
  la cible ; regard ignoré pendant la sortie ; vitesse d'animation partielle ; « Score: » non traduit ; ancien
  indicateur ; repli silencieux sur les images par défaut ; ni aide ni test ; images et sons de source inconnue.
- **Règle du mainteneur (4/11/2026, recommandations validées)** : voir le tableau ci-dessus. Pas de vies ni de pénalité.
- **Fait** : le jeu réécrit (`Moles`, classe interne `Hole` ; `MolesChar` et `MolesGameVariant` retirés), une boucle en
  temps réel, vitesse d'animation des réglages, arrêtée pendant l'engrenage. La zone qui reçoit le regard et la souris
  ne bouge pas : elle couvre le trou et la place de la cible, un regard déjà là compte dès que la cible apparaît. Fixation
  lue à chaque pas, sortie de moins de 0,3 s tolérée comme dans Ninja ; une fixation en cours n'est pas coupée par la fin du
  temps dehors. Le dernier trou utilisé est exclu du tirage suivant. `info-game.csv` et les zones d'intérêt
  (`TargetAOI`) gardés. Clé `UserP` retirée (seul ce jeu la lisait), `Biboule` gardée (Puzzle).
- **Retour du mainteneur (souris)** : zone trop grande, depuis un trou du haut on sélectionnait celui du bas. **Fait** :
  la zone suit ce qui est dessiné (largeur et hauteur de la cible, arrêt au bord avant du trou) et les rangées sont
  légèrement décalées ; un test vérifie que les zones de deux trous ne se touchent jamais, pour les taupes, les Biboules et
  « Mes images ».
- **Vérifications** : `MolesTest` (17 tests) ; captures sans fenêtre (taupes, Biboules) ; `build` complet : **789 tests,
  0 échec** ; `allGamesTest` complet réussi. **Essayé et validé par le mainteneur le 4/11/2026.**

### Ninja (`Ninja`, code `ninja`) — 4/11/2026

- **État des lieux (4/11/2026, lecture du code)** : une cible ronde avec une image bouge ; une fixation la coupe en 10
  petites boules ; elle revient environ 2,1 s plus tard. Ni fin, ni score, ni pénalité. 10 variantes : 5 trajectoires ×
  Statique ou Dynamique. Défauts : **« Dynamique » ne pouvait pas accélérer avec la fixation par défaut de 2 s** (il fallait
  toucher en 1 s, chaque touche comptait comme lente, la cible ralentissait jusqu'à 12 s par trajet) ; Horizontal
  Dynamique prenait X pour la hauteur (cible hors de l'écran) ; Hasard Dynamique changeait de vitesse à chaque point de
  passage, et par paliers ; moitié de la cible hors de l'écran à droite et en bas ; fixation perdue si elle commençait
  pendant la disparition ; animation de coupe non arrêtée à la fin. Images de l'utilisateur en silence, ancien
  indicateur, pas d'aide, pas de test ; son de 2017 de source inconnue, vignette montrant une lame absente du jeu.
- **Règle du mainteneur (4/11/2026, recommandations validées)** : un seul fruit à suivre des yeux, coupé en deux ; pas
  de vies ni de pénalité ; couper dès qu'on touche ou après une fixation ; sans fin, 20 ou 50 ; trajectoire et vitesse
  séparées ; **thème des fruits** ; variante « Mes images » ; **la durée de fixation est celle des réglages**, changée
  aussi par l'engrenage (quatrième règle, pour tous les jeux).
- **Fait** : le jeu réécrit (`Ninja`, `Target` retiré) : une boucle en temps réel, vitesse d'animation des réglages,
  arrêtée pendant l'engrenage. Fruit (ou image en rond) gardé dans l'écran. Vitesses (`NinjaSpeed`) : hauteur d'écran en
  5, 3 ou 2 s, à vitesse constante. Adaptative : part de 3 s, tous les 3 fruits plus rapide s'ils sont coupés en moins de
  1 s, plus lente s'ils prennent plus de 3 s, **sans compter la fixation**, entre 8 et 1,5 s ; un niveau par fruit dans la
  courbe des statistiques. Fixation comptée par le jeu, durée lue à chaque pas ; **une sortie de moins de 0,3 s n'arrête
  que l'anneau** (ajout à essayer au Tobii : le fruit bouge et l'oculomètre tremble) ; un regard déjà là quand le fruit
  apparaît compte. Après la coupe : lame claire, deux moitiés qui s'écartent et tombent, nouveau fruit 0,6 s plus tard.
  `FixationRing` gagne `show` et `setProgress`. **`GroupedGameVariant` accepte d'autres rangées de boutons radio**
  (`extras`, forme écrite inchangée pour les autres jeux), la fenêtre des variantes les affiche et reste plus petite que
  l'écran. Traductions (les 5 clés « Dynamic » retirées, les trajectoires gardent les leurs), aide, vignette (capture),
  inventaire, crédits. `info-game.csv` inchangé. **Pas de ressemblance voulue avec Fruit Ninja** : pas de lancers, de
  bombes, de combos, de fond en bois ni d'éclaboussures.
- **Vérifications** : `NinjaTest` (16 tests) ; `GameHelpTest`, `GameVariantDialogTest` (rangées supplémentaires) ;
  captures du jeu sans fenêtre (fruit, anneau, coupe), vues et bonnes ; `build` complet : **771 tests, 0 échec** ;
  `allGamesTest` : **66 entrées, 0 échec** (4/11/2026). **Essayé et validé par le mainteneur le 4/11/2026.**

### Casse-brique (`CasseBrique`, code `cassebrique`) — 3/11/2026

- **État des lieux (3/11/2026)** : une carte, 4 variantes (Bloc, « Space Invader », Arc-en-ciel, Sourire : le dessin
  des briques). La raquette (40 % de la largeur) suit le regard ou la souris en bas ; la balle part après 5 s ; une
  brique touchée disparaît ; tout cassé : « Bravo ». Balle tombée : revient sur la raquette, **sans vie ni défaite**.
  Fond : une image au hasard de l'ancien dossier partagé `opinions` (noir sans image). Défauts : **la boucle de 15 ms
  ne s'arrêtait jamais, même après la victoire, qu'elle relançait toutes les 15 ms** (« Bravo » et statistiques
  répétés) ; balle de 8 pixels par pas sur tous les écrans ; pas de pause à l'engrenage ; raquette qui sortait de
  l'écran ; collisions approximatives (traversées, plusieurs briques d'un coup) ; briques grises incassables prévues
  mais jamais utilisées ; aucun but à atteindre compté ; écouteurs sur toute la scène jamais retirés ; pas d'aide ;
  couleurs pures et criardes. Règle 2 déjà suivie ; règle 3 sans objet (pas de fixation). **Risque** : la variante
  « Space Invader » reprenait le nom et le personnage d'un jeu existant (marque déposée).
- **Règle du mainteneur (3/11/2026)** : **3 vies** ; gagné quand toutes les briques sont cassées ; Facile / Normal /
  Difficile (raquette, vitesse, briques à toucher deux fois) ; **les 4 dessins gardés** (« Space Invader » renommé et
  redessiné) ; **fond doux dessiné, et une variante « Mes images »** où l'image de l'utilisateur se découvre derrière
  les briques ; briques arrondies aux couleurs douces, éclat quand elles cassent.
- **Fait** : le jeu réécrit (`CasseBrique`, classe interne `Brick`) : une boucle en temps réel, vitesse d'animation
  des réglages, arrêtée pendant l'engrenage **et à la fin** ; vitesses et tailles en hauteur d'écran ; petits pas
  (la balle ne traverse plus une brique, une seule brique par pas) ; rebond sur le côté le plus proche de la brique ;
  sur la raquette, l'angle dépend de l'endroit touché (60° au bord) ; raquette gardée dans l'écran ; balle sur la
  raquette 3 s au départ, 1,5 s après une chute. Niveaux (`CasseBriqueLevel`) : raquette 30 %, 22 %, 16 % de la
  largeur ; balle 0,55, 0,75, 0,95 hauteur d'écran par seconde ; en Difficile une brique sur trois, plus foncée, se
  fend au premier coup. Dessins (`CasseBriqueMap`, lignes de chiffres) : Bloc, **Extraterrestre** (une tête ronde à
  antenne, deux yeux, un sourire ; une première version ressemblait encore au personnage de Space Invaders, vue en
  vraie fenêtre, redessinée), Arc-en-ciel, Sourire. Fond (`CasseBriqueBackground`) : dégradé lilas et ciel avec des
  ronds doux, ou une image de l'utilisateur (`files/images/CasseBrique`, variante à part : ligne des réglages et
  fenêtre sans image comme les autres) qui couvre l'écran derrière les briques. 3 cœurs, pastille « Briques : 4 /
  73 » ; « Perdu ! » puis statistiques. Statistiques : un but à atteindre par brique cassable, un but atteint par
  brique cassée. La raquette suit le regard (zone de jeu) et la souris (couche du jeu), plus la scène entière.
  Variantes `GroupedGameVariant` (fond, niveau, dessin), 24 ; une ancienne variante se joue en Bloc, Normal, fond
  dessiné. **L'ancien dossier `opinions` retiré** (plus aucun jeu ne le lit) : section de `CustomFileChooser` (les
  sections suivent la liste des dossiers), ses boutons « J'aime / Je n'aime pas / Peu importe », leurs traductions
  et l'icône `opinions.png`. Traductions (les clés `Bloc`, `Space`, `Arc`, `Smiley` remplacées), aide, vignette
  (capture de l'Arc-en-ciel), inventaire.
- **Vérifications** : `CasseBriqueTest` (8 tests : 3 vies ; rebond sur la raquette ; victoire une seule fois, buts
  comptés ; briques à toucher deux fois en Difficile seulement ; brique cassée par la balle ; raquette qui suit la
  souris et reste dans l'écran ; variantes, « Mes images », lignes des dessins ; tout retiré à la fin) ; `GameHelpTest`
  avec le jeu ; captures en vraie fenêtre (souris) `build/game-screenshots/essai1/cassebrique/` et
  `essai2/cassebrique/` (Bloc, Extraterrestre, Arc-en-ciel, « Mes images » avec une image de test).
