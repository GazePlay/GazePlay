---
title: FAQ
permalink: /faq/fr
layout: single
toc: true
toc_label: "Sur cette page"
toc_sticky: true
---

## Où se trouve le dossier GazePlay par défaut ?

Voici les chemins d'accès par défaut au dossier `GazePlay` selon votre système d'exploitation :

|         | Emplacement                         |
| ------- |: ---------------------------------- |
| Windows | `C:\Users\<username>\GazePlay`      |
| MacOS   | `/Utilisateurs/<username>/GazePlay` |
| Linux   | `/home/<nomutilisateur>/GazePlay`   |

### Windows

{% include figure image_path="/assets/images/tutorial/this_pc_folder.png" alt="Ce PC" caption="Dans l'Explorateur, accédez à `Ce PC`. Cliquez sur le lecteur `C:`" %}

{% include figure image_path="/assets/images/tutorial/c_disk_folder.png" alt="Lecteur C" caption="Cliquez sur le dossier `Utilisateurs`, puis sélectionnez le dossier avec votre nom d'utilisateur." %}

{% include figure image_path="/assets/images/tutorial/users_folder.png" alt="Dossier utilisateur" caption="Dans votre dossier utilisateur, vous devriez trouver le dossier `GazePlay`. Cliquez pour l'ouvrir." %}

## Comment puis-je ajouter mes propres images à GazePlay ?

### Ajout d'images personnalisées

Dans un dossier de votre choix, créez un dossier appelé `images`.

`images` peut avoir 3 sous-dossiers :
* `portraits` : images utilisées par les creampie, les ninja et les bulles de portraits. Les images doivent être petites (300 × 300 pixels).
* `magiccards` : images utilisées par les cartes magiques. Vous pouvez utiliser toutes les tailles d'image.
* `blocs` : images utilisées par les blocs et la carte à gratter. Choisissez de grandes images, de préférence de la même taille que l'écran.

Vous pouvez modifier le dossier d'images dans GazePlay dans le menu de configuration, afin d'avoir autant de dossiers d'images que vous le souhaitez.

### Ajout de jeux personnalisés à _Où est-ce ?_

Suivez les étapes suivantes pour ajouter vos propres variantes personnalisées au jeu _Où est-ce ?_.

#### Windows

{% include figure image_path="/assets/images/tutorial/gazeplay_folder.png" alt="Dossier GazePlay" caption="Placez-vous dans le dossier `where-is-it`, situé dans le dossier par défaut `GazePlay`." %}

Si ce dossier n'existe pas, créez le.
Vous pouvez modifier l'emplacement ou le nom de ce dossier comme bon vous semble tant que vous changer bien le paramètre correspondant dans le menu de configuration.

Ce dossier doit contenir trois sous-dossiers : `images`, `pictos`, `sounds`.
S'ils n'existent pas, créez les.

Le dossier `images` est le plus important pour le bon fonctionnement du jeu.

{% include figure image_path="/assets/images/tutorial/where-is-it_folder_images.png" alt="Dossier where-is-it" caption ="Cliquez sur `images`." %}

Vous devez créer à l'intérieur autant de sous-dossiers que vous voulez d'élément à deviner.
C'est dans chacun de ces dossiers que vous allez placer vos images personnalisées.
Vous pouvez ainsi mettre plusieurs images pour le même élément dans le dossier correspondant.
Les noms des images à l'intérieur n'ont aucunes importances, c'est uniquement le nom du dossier qui défini l'élément.

{% include figure image_path="/assets/images/tutorial/images_folder.png" alt="Dossier images" caption="Voici un exemple de comment remplir le dossier `images`." %}

Les dossiers vides seront ignorés.
Il est recommandé de créer au moins 9 dossiers non vides pour les éléments à deviner.
Si vous en créez moins, certaines variantes du jeu ne fonctionneront pas dû au manque d'éléments pour remplir la grille.
Par exemple, dans le cas de la grille 3x3 avec moins de 9 éléments à deviner.

Vous pouvez également ajouter vos propres sons personnalisés.
Ceux-ci sont joués lors de l'affichage de la question ou répétés lorsque le joueur se trompe si le paramètre correspondant est activé.

{% include figure image_path="/assets/images/tutorial/where-is-it_folder_sounds.png" alt="Dossier where-is-it" caption ="Revenez au dossier `where-is-it` et cliquez sur `sounds`." %}

Placez tous vos sons (par exemple des fichiers `.mp3`) dans ce dossier et donnez-leur le même nom que celui du dossier des images qui leur est associé.

{% include figure image_path="/assets/images/tutorial/sounds_folder.png" alt="Dossier sounds" caption="Voici un exemple de comment remplir le dossier `sounds`." %}

Vous pouvez de la même manière ajouter des pictogrammes personnalisés qui s'afficheront en même temps que la question.
Il suffit pour cela d'ajouter vos pictogrammes dans le dossier `pictos`, sans se soucier de leurs noms.

Enfin, vous devez créer un fichier `questions.csv` qui va principalement contenir les textes des questions à poser au joueur.
Pour ce faire, ouvrez un tableur, comme Microsoft Excel, LibreOffice Calc ou [Google Sheets](https://docs.google.com/spreadsheets).
Ensuite, remplissez un tableau tel que l'exemple ci-dessous :

|           | pictos          | fra                      |
| --------- |: -------------- |: ----------------------- |
| baignoire | `baignoire.png` | Où est la baignoire ? |
| biscuit   | `biscuit.png`   | Où est le biscuit ?   |
| bus       | `bus.png`       | Où est le bus ?       |

Comment remplir les colonnes :
* La première colonne est le nom des dossiers d'images que vous avez créés.
* La colonne `pictos` est le nom du fichier image dans le dossier du même nom.
* Et la colonne `fra` représente la traduction française de la question à poser au joueur.

Vous pouvez passer de français à une autre langue si vous le souhaitez ou également ajouter autant de colonne de langues qu'il vous paraît nécessaire.
Le jeu choisira automatiquement la traduction pour la langue sélectionnée dans le menu configuration.
Les langues prises en charge sont :
`alb`, `ara`, `chn`, `deu`, `ell`, `eng`, `fin`, `fra`, `hrv`, `ind`, `ita`, `jpn`, `pol`, `por`, `zsm`, `nld`, `rus`, `spa`, `vnm`, `bel`, `hin`.

Enregistrez votre feuille de calcul en tant que `questions.csv` et placer le fichier dans le dossier `where-is-it`.

Et voilà, vous venez de créer votre propre variante au jeu Où est-ce ?.

Vous pouvez créer et remplir de la même manière autant de dossier `where-is-it` que vous voulez pour avoir pleins de variantes différentes.
Il vous suffit ensuite de changer dans le menu configuration l'emplacement du dossier pour choisir la variante à laquelle vous désirez jouer.

Voici un exemple complet de dossier `where-it-is` avec lequel vous pouvez jouer ou vous inspirer pour créer vos propres variantes : [exemple-where-it-is](/assets/exemple-where-is-it.zip).

## Comment créer un raccourci sous Windows ?

1. Localisez `gazeplay-windows.bat` dans le dossier `/bin` de votre dossier d'installation de GazePlay.
1. Faites un clic droit sur `gazeplay-windows.bat` et sélectionnez 'créer un raccourci'.
2. Déplacez le raccourci sur le bureau.
3. Double-cliquez sur le raccourci pour lancer GazePlay.

## Je clique sur `gazeplay-windows.bat` mais cela ne fonctionne pas. Pourquoi ?

Vous pouvez voir un message d'erreur indiquant que vous n'avez pas de machine virtuelle Java sur votre ordinateur.
Pour résoudre ce problème, vérifiez qu'il existe un dossier `jre` dans votre dossier d'installation de GazePlay.
S'il n'y est pas, vous devrez télécharger à nouveau la dernière version de GazePlay.

Si vous rencontrez d'autres problèmes, envoyez un e-mail à <didier.schwab@univ-grenoble-alpes.fr> ou [posez directement un problème à nos développeurs](https://github.com/GazePlay/GazePlay/issues/new)
