[Retour](../README.md)

# Installation

1) Téléchargez ce [fichier (Version 1.1.0)](https://github.com/schwabdidier/GazePlay/releases/download/GazePlay-SNAPSHOT-1.0.6/gazeplay-1.0.6.jar). Il s'agit d'un fichier compressé *zip* qui contient un répertoire nommé *gazeplay* ;

2) Extrayez le répertoire *gazeplay* dans le répertoire utilisateur (voir la faq ci-dessous) ; 

# Lancement de GazePlay

Vous trouverez dans le répertoire *bin* de quoi lancer *GazePlay*.

Sous Windows, double-cliquez sur le fichier *gazeplay.bat*.

Sous Linux ou MacOs, double-cliquez sur le fichier *gazeplay.sh*.

# FAQ

## Quel est le répertoire par défaut de GazePlay ?

Sous Windows C:\Users\NomUtilisateur\Documents

Sous MacOs /Users/NomUtilisateur/GazePlay

Sous Linux /home/NomUtilisateur/GazePlay

## Comment faire un racourci sous Windows pour *gazeplay.bat* ?

1) Cliquez-droit sur le fichier *gazeplay.bat* et sélectionnez *créez un raccourci* ;

2) déplacez le raccourci sur le bureau ;

3) double-cliquez sur le raccourci pour lancer *GazePlay*.

## Lorsque je double-clique sur *gazeplay.bat*, ça ne fonctionne pas.

Un message d'erreur doit s'afficher et il est probable qu'aucune machine virtuelle Java ne soit installée.

[Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

En cas de problème, didier.schwab à univ tiret grenoble tiret alpes point fr (remplacer le à par @, le tiret par - et le point par .).

## Comment modifier les images ?

Il faut créer un répertoire XXX dans lequel il faut mettre un répertoire nommé *images*.
 
*images* peut contenir 3 autres répertoires :

1) *portraits* : contient les images utilisées par *tartes à la crème*, *ninja* et *bulles portrait*. Il vaut mieux choisir des images relativement petites (300x300 pixels).

2) *magiccards* : contient les images utilisées par *cartes magique*. Toutes les tailles d'image conviennent.

3) *blocs* : contient les images utilisées par *blocs* et par *cartes à gratter*. Choisir des images assez grandes (idéalement au moins la taille de l'écran).

Vous pouvez avoir autant de répertoires XXX que vous voulez et changer par l'interface de configuration.


<!--## Installation d'un eye-tracker Tobii EyeX ou 4C

Notez-bien que ces deux modèles sont uniquement compatibles Windows (version 7, 8 et 10). GazePlay ne permet pas de les utiliser avec un autre système d'exploitation ou sur des ordinateurs dont les caractéristiques matérielles ne permettent pas d'utiliser ces modèles.

Téléchargez la dernière version du fichier en cliquant sur le lien suivant [GazePlay-tobii-setup](https://github.com/schwabdidier/GazePlay/releases/download/gazeplay-tobii-setup-1.1.0.jar/gazeplay-tobii-setup-1.1.0.jar) et double-cliquez dessus. L'installation ne devrait pas prendre plus d'une dizaine de secondes.

Pour information, l'installation consiste à copier dans le répertoire par défaut de *GazePlay* un répertoire *DLL* dans lequel se trouvent deux fichiers nommés *tobii_stream_engine.dll* et *GazePlayTobiiLibrary2.dll*.
-->
[Retour](../README.md)