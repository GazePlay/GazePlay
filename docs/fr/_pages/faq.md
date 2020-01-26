---
title: FAQ
permalink: /faq/fr
layout: single
toc: true
toc_label: "Sur cette page"
toc_sticky: true
---

## Où se trouve le dossier GazePlay par défaut?

| | Emplacement |
| ----------------- |: ----------- |
| Windows | `C:\Users\<username>\GazePlay` |
| MacOS   | `/Utilisateurs/<username>/GazePlay` |
| Linux   | `/home/<nomutilisateur>/GazePlay` |

## Comment puis-je ajouter mes propres images à GazePlay?
Dans un dossier de votre choix, créez un dossier appelé «images».

`images` peut avoir 3 sous-dossiers:
* `portraits`: images utilisées par les creampie, les ninja et les bulles de portraits. Les images doivent être petites (300 × 300 pixels).
* `magiccards`: images utilisées par les cartes magiques. Vous pouvez utiliser toutes les tailles d'image.
* `blocs`: images utilisées par les blocs et la carte à gratter. Choisissez de grandes images, de préférence de la même taille que l'écran.

Vous pouvez modifier le dossier d'images dans GazePlay dans le menu de configuration, afin d'avoir autant de dossiers d'images que vous le souhaitez.

### Ajout de jeux personnalisés à _Où est-ce?_
Suivez ces étapes pour trouver le dossier Where Is It sur votre ordinateur et ajouter vos propres jeux personnalisés:

#### Windows
{% include figure image_path="/assets/images/tutorial/my_pc.png" alt="Mon PC" caption="Dans l'Explorateur, accédez à 'Mon PC'. Cliquez sur le C: / Drive" %}

{% include figure image_path="/assets/images/tutorial/c_disk.png" alt="C Drive" caption="Cliquez sur 'Utilisateurs', puis sélectionnez votre nom d'utilisateur." %}

{% include figure image_path="/assets/images/tutorial/gazeplay_folder.png" alt="Dossier GazePlay" caption="Dans votre dossier utilisateur, vous devriez trouver le dossier GazePlay. Cliquez pour l'ouvrir." %}

{% include figure image_path="/assets/images/tutorial/where_is_it.png" alt="Dossier Where Is It" caption="Dans le dossier GazePlay, vous devriez trouver le dossier 'where-is-it'. Cliquez pour l'ouvrir." %}

Avant d'ajouter vos photos, vous devrez créer un fichier `questions`.
Pour ce faire, ouvrez un tableur, comme Microsoft Excel, LibreOffice Calc ou [Google Sheets](https://docs.google.com/spreadsheets). Ensuite, remplissez un tableau tel que l'exemple ci-dessous:

| | pictos | fra |
| --------- |: -------------- |: ---------------------- |
| baignoire | `baignoire.png` | Où est la baignoire? |
| biscuit | `biscuit.png` | Où est le biscuit? |
| bus | `bus.png` | Où est le bus? |

La première colonne est le nom des dossiers que vous allez ajouter, la colonne `pictos` est le nom du fichier image dans ce dossier, et `fra` représente la traduction francais de la question à poser au joueur. Vous pouvez passer  de `eng` à une autre langue si vous le souhaitez - le jeu choisira la bonne traduction pour la langue choisie. Les langues prises en charge sont:
`alb, ara, chn, deu, ell, eng, fin, fra, hrv, ind, ita, jpn, pol, por, zsm, nld, rus, spa, vnm`

Enregistrez votre feuille de calcul en tant que `questions.csv` et de la placer dans le dossier `where-is-it`.

{% include figure image_path="/assets/images/tutorial/images.png" alt="Mon PC" caption ="Cliquez sur 'images'" %}

{% include figure image_path="/assets/images/tutorial/folders.png" alt="Mon PC" caption="Placez toutes vos images dans des dossiers classés. Un dossier par type d'image que vous souhaitez ajouter." %}

## Comment créer un raccourci sous Windows?
1. Localisez `gazeplay-windows.bat` dans le dossier` / bin` de votre dossier d'installation de GazePlay.
1. Faites un clic droit sur «gazeplay-windows.bat» et sélectionnez créer un raccourci.
2. Déplacez le raccourci sur le bureau.
3. Double-cliquez sur le raccourci pour lancer GazePlay.

## Je clique sur gazeplay-windows.bat mais cela ne fonctionne pas. Pourquoi?
Vous pouvez voir un message d'erreur indiquant que vous n'avez pas de machine virtuelle Java sur votre ordinateur.
Pour résoudre ce problème, vérifiez qu'il existe un dossier `jre` dans votre dossier d'installation de GazePlay. S'il y a
ne l'est pas, vous devrez télécharger à nouveau la dernière version de GazePlay.

Si vous rencontrez d'autres problèmes, envoyez un e-mail à <didier.schwab@univ-grenoble-alpes.fr> ou [posez directement un problème à nos développeurs](https://github.com/GazePlay/GazePlay/issues/new)
