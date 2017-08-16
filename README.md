# GazePlay

* Read this in other languages: [English](README/README.en.md) [French](README.md)

Gazeplay est un logiciel **gratuit** qui rassemble plusieurs mini-jeux jouables grâce à un occulomètre (*Eye-tracker*). La dernière version compte 12 jeux.

Le logiciel est fourni avec des images par défaut mais il est facile de mettre les siennes avec lesquelles le joueur interagira plus facilement (voir section).

## Vidéo de présentation

<iframe width="560" height="315" src="https://www.youtube.com/embed/yMjBgVmhXV8" frameborder="0" allowfullscreen></iframe>


## Système requis

GazePlay fonctionne sous Windows, MacOs et Linux. Il est utilisable par tout occulomètre capable de diriger la souris.

Il a est également complètement compatible avec un Eye Tribe sur Windows et MacOs.

## Principe

De nombreux polyhandicapés ne sont pas capables d'utiliser de manière assez précise ni leurs membres (mains, pieds), ni leur voix. Les outils ou jeux sur terminaux numériques leur sont ainsi inaccessibles. Le regard semble alors être, dans bien des cas, la manière la plus directe de leur permettre d'interagir.

Il s’agit d’utiliser ce qu’on appelle des eye-trackers, appareils qui permettent de savoir ou se pose le regard d’un individu. Évidement ces jeux doivent rester simples et les interactions assez directes pour permettre aux individus de comprendre et assimiler les conséquences de leurs actions, c'est-à-dire les conséquences de leur regard. On peut imaginer des jeux consistant à tirer sur des cibles, des casses-briques, des jeux de cartes, des mémory,...

Ces jeux offrent au moins deux avantages à l'individu :

1) le divertir ;

2) lui permettre d'entraîner son regard afin de lui offrir la possibilité d'acquérir des interactions plus complexes. Il s’agit ainsi d’une première étape visant la possibilité de mettre en place une véritable communication basée sur des pictogrammes par exemple.

## Jeux

Ils sont actuellement 12 jeux (dont 6 variantes).

<!-- ![Écran principal GazePlay](README/images/gazePlay.jpg "Écran principal GazePlay") -->

### Tartes à la crème

Le joueur lance une tarte à la crème sur la cible statique lorsqu'il la regarde.

![Jeu des tartes à la crème](README/images/CreamPie.jpg)

### Ninja

La cible suit des mouvements aléatoires et explose lorsque le joueur la regarde.

![Ninja](README/images/ninja-1.jpg)

### Cartes magiques

Plusieurs cartes sont affichées et le joueur doit les regarder pour les retourner. L'une de ces cartes comporte une image.
Une récompense est offerte au joueur lorsqu'il l'a trouvée.

![magic-card-2](README/images/magic-card-2.jpg)

### Blocs

Une grande image est cachée par des blocs de couleur que le joueur doit regarder pour les faire disparaître. Lorsque l'ensemble des blocs ont disparu, la récompense est offerte.

![blocs](README/images/blocs.jpg)

### Cartes à gratter

Une grande image est cachée dans une carte à gratter. Le joueur gratte la carte grâce à son regard. Lorsque une grande partie de la carte a été grattée, l'image est affichée et la récompense offerte.

![Scratchcard](README/images/Scratchcard.jpg)

### Bulles

Des bulles de couleur remontent lentement à la surface et exploser lorsque le joueur les regarde.

![Colored Bubbles](README/images/colored-bubbles.jpg)

### Bulles Portrait

Des bulles contenant des portraits remontent lentement à la surface et exploser lorsque le joueur les regarde.

![Portrait Bubbles](README/images/portrait-bubbles.jpg)

## Téléchargements

[Dernière Version](https://github.com/schwabdidier/GazePlay/releases/download/v1-beta-candidate-4/GazePlay-beta-4.jar)

## Installation

Copiez-collez ou déplacez le fichier téléchargé sur votre disque dur à l'endroit que vous préférez. Le bureau sera parfait.

Double cliquez sur le fichier. Si tout se passe bien, le logiciel se lance.

Dans le cas contraire, il est probable que vous n'ayez pas de machine virtuelle Java installée.

[Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

En cas de problème, didier.schwab à univ tiret grenoble tiret alpes point fr (remplacer le à par @ et le point par .).

## Modifier les images

Quel que soit le système d'exploitation, le logiciel cherche dans le répertoire de l'utilisateur un répertoire nommé *GazePlay*. Par exemple, le nom de l'utilisateur est *NomUtilisateur*

* Sous **Windows** C:\Users\NomUtilisateur\Documents

* Sous **MacOs** /Users/NomUtilisateur/GazePlay

* Sous **Linux** /home/NomUtilisateur/GazePlay

Dans ce répertoire, il faudra créer un répertoire *images* dans lequel vous aurez à créer 3 autres répertoires :

1) *portraits* : contient les images utilisées par *tartes à la crème*, *ninja* et *bulles portrait*. Il vaut mieux choisir des images relativement petites (300x300 pixels).

2) *magiccards* : contient les images utilisées par *cartes magique*. Toutes les tailles d'image conviennent.

3) *blocs* : contient les images utilisées par *blocs* et par *cartes à gratter*. Choisir des images assez grandes (idéalement au moins la taille de l'écran).

## Contributions

Ce logiciel est destiné à être amélioré. Toute personne peut contribuer par ses idées d'amélioration. Il est évidement également possible de contribuer par ses talents de graphiste ou d'informaticien pour créer de nouveaux jeux/améliorer les existants.

Contact : Didier Schwab didier.schwab à univ-grenoble-alpes point fr (remplacer le à par @ et le point par .).