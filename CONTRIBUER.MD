# Comment contribuer à GazePlay : bonnes pratiques

Cette page est destinée aux étudiants réalisant un stage encadré par Didier Schwab.

Tout contributeur peut s'inspirer de cette page mais n'a évidement pas l'obligation de chercher l'approbation *a priori* de Didier Schwab. 

## Git et GitHub

GazePlay est un projet open source hébergé sur GitHub.

À part cette page, destinée aux étudiants francophone, la langue de développement de GazePlay est l'**anglais**.

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

##Maven
Selon [Wikipedia](https://fr.wikipedia.org/wiki/Apache_Maven),
Maven est un outil de gestion et d'automatisation de production des projets logiciels Java. Maven est généralement intégré à un IDE



##Utilitaires

### Sl4J

Simple Logging Facade for Java (https://www.slf4j.org). Gère les affichages sur la console ou dans le fichier de debug gazeplay.log (situé automatiquement dans le répertoire par défaut de GazePlay).

**Par conséquent, l'usage des System.out ou System.err sont à proscrire dans GazePlay.**

Pour l'utiliser, il suffit de mettre avant l'ouverture de la classe @Sl4J puis de l'utiliser dans le code.

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
https://projectlombok.org

