# Comment contribuer à GazePlay : bonnes pratiques

Cette page est destinée aux étudiants réalisant un stage encadré par Didier Schwab.

Tout contributeur peut s'inspirer de cette page mais n'a évidement pas l'obligation de chercher l'approbation *a priori* de Didier Schwab. 

## Git et GitHub

GazePlay est un projet open source hébergé sur GitHub.

À part cette page, destinée aux étudiants francophone, la langue de développement de GazePlay est l'**anglais**.

## Ouvrir un ticket
La manière la plus simple pour contribuer est d'ouvrir un ticket (*issue*). Vous pouvez déclarer y un bug, proposer une amélioration, un nouveau jeu, ... Ce ticket pourra être discuté par tous les contributeurs (y compris les autres stagières) et finalement accepté par Didier Schwab.

## Coder
Une bonne pratique consiste à suivre les étapes suivantes.
1. Repérer une issue à résoudre et proposer une solution
2. *Forker* le projet et implanter sa solution
3. Proposer une *pull request*

Vous trouverez des informations supplémentaires [ici](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github/contribuer-a-des-projets-open-source)

## Pull request
Il vous sera demandé de faire *a minima* une *pull request* **quotidienne** afin d'éviter que votre code s'éloigne trop de la branche principale. Chaque pull resquest doit être documentée au mieux. En faisant le lien, entre autres, avec l'issue correspondante.

Afin de faire une *pull request*, toujours faire un *maven clean install* qui a pour effet d'uniformiser le code (indentations, espaces,...).

Chaque requête sera examinée par un des contributeurs.

## Maven
Maven est un outil de gestion et d'automatisation de production des projets logiciels Java ([Wikipedia](https://fr.wikipedia.org/wiki/Apache_Maven)). Maven est généralement intégré à son IDE.

Vous trouverez des informations complémentaires sur le site https://maven.apache.org

Pour l'utilisation commune de *maven*, vous devrez faire au départ pour installer tous les packages puis avant toute *pull request*

```
mvn clean install
```

## Utilitaires

### Sl4J

Simple Logging Facade for Java (https://www.slf4j.org). Gère les affichages sur la console ou dans le fichier de debug gazeplay.log (situé automatiquement dans le répertoire par défaut de GazePlay).

**Par conséquent, l'usage des System.out ou System.err sont à proscrire dans GazePlay.**

Pour l'utiliser, il suffit de mettre avant l'ouverture de la classe @Sl4J puis de l'utiliser dans le code (voir *Lombok*).

Par exemple,
```
int x = 3;
int y = 4
log.info("positionX : {} ; positionY : {}", x, y);
```

affiche 

```
positionX : 3 ; positionY : 4
```

Plus d'informations et niveaux de messages : 
https://www.tutorialspoint.com/log4j/log4j_logging_levels.htm


### Lombok

Lombok permet de simplifier le code Java. Le principe est de placer en début de classe des balises qui seront remplacées à la compilation par le code machine correspondant.

Par exemple :
- @slf4j pour la gestion des logs
- @getter pour les accesseurs des attributs d'une classe
- @setter pour les mutateurs des attributs d'une classe

Plus d'informations : 
https://projectlombok.org

### Findbugs

Findbugs permet d'éviter l'introduction de bugs qui pourraient être automatiquement détectés grâce à une analyse statique du code.

La configuration utilisée dans GazePlay exclut la recherche des bugs car il rend les builds bien plus longs.

```
mvn clean install -Dfindbugs.skip=true
```

Assurez-vous que l'utilisation de findbugs ne trouve pas de bug avant de faire un *pull request*.

```
mvn clean install -Dfindbugs.skip=false
```

Plus d'informations sur http://findbugs.sourceforge.net

### formatter-maven-plugin

Ce plugin est automatiquement appelé lorsqu'on réalise un appel à Maven. Il permet de réaliser un certain nombre d'opérations comme la normalisation des fins de ligne, des espaces, ...

Plus d'informations sur http://code.revelc.net/formatter-maven-plugin/

### TravisCI

Travis CI est un logiciel libre d'intégration continue ([Wikipedia](https://fr.wikipedia.org/wiki/Travis_CI)). L'intégration continue est un ensemble de pratiques utilisées en génie logiciel consistant à vérifier à chaque modification de code source que le résultat des modifications ne produit pas de régression dans l'application développée ([Wikipedia](https://fr.wikipedia.org/wiki/Intégration_continue)).
 
TravisCI construit un *build* pour chaque *commit*, pour chaque *pull request* et ainsi trouver plus rapidement les problèmes.

Un badge est ajouté en haut du README.md, indiquant le status de la branche principale (qui devrait toujours être en *build | passing*).

En savoir plus https://docs.travis-ci.com/user/getting-started/


