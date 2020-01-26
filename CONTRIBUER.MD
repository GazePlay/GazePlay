# Comment contribuer à GazePlay : bonnes pratiques

Cette page est destinée aux étudiants réalisant un stage encadré par Didier Schwab.

Tout contributeur peut s'inspirer de cette page mais n'a évidement pas l'obligation de chercher l'approbation *a priori* de Didier Schwab. 

Dans ces instructions, les actions effectuées dans la ligne de commande Windows sont préfixées par `>`, et celles sur Unix
shell sont préfixés avec `$`. Sélectionnez la commande appropriée à votre plateforme.

## Git et GitHub

GazePlay est un projet open source hébergé sur GitHub.

À part cette page, destinée aux étudiants francophone, la langue de développement de GazePlay est l'**anglais**.

## Ouvrir un ticket
La manière la plus simple pour contribuer est d'ouvrir un ticket (*issue*). Vous pouvez y déclarer un bug, proposer une amélioration, un nouveau jeu, 
... Ce ticket pourra être discuté par tous les contributeurs (y compris les autres stagières) et finalement accepté par Didier Schwab.

## Coder
Une bonne pratique consiste à suivre les étapes suivantes.
1. Repérer une issue à résoudre et proposer une solution
2. *Forker* le projet et implanter sa solution
3. Proposer une *pull request*

Vous trouverez des informations supplémentaires [ici](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github/contribuer-a-des-projets-open-source)

## Pull request
Il vous sera demandé de faire *a minima* une *pull request* **quotidienne** afin d'éviter que votre code s'éloigne trop de la branche principale. Chaque **pull request** doit être documentée au mieux. En faisant le lien, entre autres, avec l'issue correspondante.

Afin de faire une *pull request*, toujours faire un `gradlew` qui exécutera tous les tests et s'assurera que le code n'est pas cassé.

Chaque requête sera examinée par un des administrateur.

## Gradle

[Gradle](https://gradle.org/) est un système d'automatisation de construction open source qui utilise un langage spécifique au domaine (DSL) basé sur Groovy.
Il est utilisé pour des tâches automatisées telles que la construction, les tests et la publication de GazePlay.
Si vous importez le projet dans IntelliJ, vous serez peut-être invité à importer le projet Gradle - cela permet à tous
tâches de test à effectuer à l'aide de Gradle.

Avant de pouvoir commencer à développer GazePlay, vous devrez exécuter une version initiale. Cela peut être fait sur la ligne de commande avec
```
> gradlew
$ ./gradlew
```

Tous les composants requis seront installés à partir d'Internet, alors assurez-vous d'être en ligne.

### Java

GazePlay est compilé avec Java 11 construit avec OpenJDK 11. Si vous avez besoin d'une distribution, [AdoptOpenJDK](https://adoptopenjdk.net/)
offre un package simple à installer sur n'importe quel ordinateur Windows ou Unix. Gradle vous dira si vous n'utilisez pas une
version compatible de Java et empêcher les builds sur tout ce qui est inférieur à Java 11.

## Tests

Une bonne pratique, indispensable pour trouver les bogues majeurs, est d'écrire des tests unitaires pour le code. Pour commencer à écrire
des tests, utilisez votre IDE pour trouver si un fichier de tests existe déjà. Dans IntelliJ, cela se fait avec `CMD + SHIFT + T` (MacOS) ou 
`CTRL + SHIFT + T` (Windows et Linux). Vous pouvez également rechercher le fichier dans `src/test/java/<java nom du package>` (tel que 
src/test/java/net/gazeplay").

Un bon test couvre autant de code de la méthode testée que possible. Chaque test doit paramétrer et installer l'objet testé et 
couvrir un seul déroulement de la méthode. Par exemple, si la méthode contient une déclaration "if", vous devez écrire un test
pour les cas où le test est à "vrai", et un cas où le test est à "faux". Vous pouvez obtenir une visualisation du code qui a 
a été testé en exécutant `./gradlew jacocoTestReport`. Cette commande lance tous les tests et produit un index HTML que vous pourrez
ouvrir dans votre navigateur. Il mettra en évidence toutes les parties du code qui ont été testés et celles qui ne l'ont pas été. 

Donnez des noms explicites à vos tests. Par exemple, si une méthode doit émettre un son lorsque l'on clique sur un bouton, donnez-lui un nom 
'shouldPlaySoundWhenButtonClicks'. Cela aide les autres développeurs à comprendre ce que votre code devrait faire, et aide à
des tests de débogage si un autre code dépendant a changé.

**Il existe en ligne de nombreuses et très bonnes ressources sur la manière d'écrire des tests. Si vous cherchez un endroit par lequel commencer, lisez 
[this article](https://manifesto.co.uk/unit-testing-best-practices-java/ **

GazePlay utilise [JUnit 5](https://junit.org/junit5) comme framework de test, Vous pouvez trouver de nombreux exemples de construction 
de ces tests en ligne et dans les fichiers de tests eux-mêmes.

Vous aurez souvent besoin d'utiliser [TestFX](https://github.com/TestFX/TestFX/wiki) dans vos tests pour accéder aux composants JavaFX. Ici encore,
n'oubliez de vous inspirer de tests existants pour créer les votres.

En programmation orientée objet, les mocks (simulacres, objets fantaisie, 'mock object') sont des objets simulés qui reproduisent le comportement d'objets réels de manière contrôlée ([Wikipedia])(https://fr.wikipedia.org/wiki/Mock_(programmation_orientée_objet\)))
Dans GazePlay, nous utilisons [Mockito](http://site.mockito.org). Pour les objets statiques, nous utilisons
[JMockit](https://jmockit.github.io/). Vous trouverez des exemples dans ce dépôt et en ligne. N'hésitez pas à proposer des alternatives qui faciliteraient l'utilisation de tests.


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

La gestion des logs se fait dans gazeplay-commons/src/main/resources/logback.xml file (à la fois pour les sorties dans la console et la sortie dans le fichier de log gazeplay.log).

### Lombok

Lombok permet de simplifier le code Java. Le principe est de placer en début de classe des balises qui seront remplacées à la compilation par le code machine correspondant.

Par exemple :
- @slf4j pour la gestion des logs
- @getter pour les accesseurs des attributs d'une classe
- @setter pour les mutateurs des attributs d'une classe

Plus d'informations : 
https://projectlombok.org

### Spotbugs

Spotbugs permet d'éviter l'introduction de bugs qui pourraient être automatiquement détectés grâce à une analyse statique du code.

La configuration utilisée dans GazePlay inclut la recherche de bogues. Vous pouvez arrêter l'exécution de Spotbugs avec cette commande.

```
> gradlew -PSpotBugs=false
$ ./gradlew -PSpotBugs=false
```


Assurez-vous que l'utilisation de spotbugs ne trouve pas de bug avant de faire un *pull request*.

```
> gradlew
$ ./gradlew
```

Plus d'informations sur https://spotbugs.github.io/

### TravisCI

Travis CI est un logiciel libre d'intégration continue ([Wikipedia](https://fr.wikipedia.org/wiki/Travis_CI)). L'intégration continue est un ensemble de pratiques utilisées en génie logiciel consistant à vérifier à chaque modification de code source que le résultat des modifications ne produit pas de régression dans l'application développée ([Wikipedia](https://fr.wikipedia.org/wiki/Intégration_continue)).
 
TravisCI construit un *build* pour chaque *commit*, pour chaque *pull request* et ainsi trouver plus rapidement les problèmes.

Un badge est ajouté en haut du README.md, indiquant le status de la branche principale (qui devrait toujours être en *build | passing*).

En savoir plus https://docs.travis-ci.com/user/getting-started/

### Ligne de commande "idéale"

Cette ligne de commande réalise l'ensemble des opérations vérifiées par Travis
```
> gradlew
$ ./gradlew
```

