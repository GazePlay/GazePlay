# How to contribute to GazePlay: good practices

This page is intended for students doing an internship supervised by Didier Schwab.

Any contributor can take inspiration from this page but is obviously not obliged to seek Didier Schwab's *prior* approval. 

## Git and GitHub

GazePlay is an open source project hosted on GitHub.

GazePlay's development language is **English**.

## Open a ticket
The easiest way to contribute is to open a ticket (*issue*). You can declare a bug, propose an improvement, a new game,
... This ticket can be discussed by all contributors (including other interns) and finally accepted by Didier Schwab.

## Coding
A good practice is to follow the following steps.
1. Identify an issue to be resolved and propose a solution
2. *Fork* the project and implement its solution
3. Propose a *pull request*

You will find further information [here](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github/contribuer-a-des-projets-open-source)

## Pull request
You will be asked to do at least a *pull request* **daily** to avoid that your code deviates too far from the main branch. 

Each request must be documented as well as possible. By making the link, among other things, with the corresponding outcome.

In order to make a *pull request*, always run `gradlew` which will run all tests and ensure the code has not broken.

Each request will be examined by one of the contributors.

## Gradle

[Gradle](https://gradle.org/) is an open-source build-automation system that uses a Groovy-based domain-specific language (DSL). 
It is used for automated tasks such as building, testing and releasing GazePlay. 
If you import the project into IntelliJ, you may be prompted to import the Gradle project - doing so allows all build and
test tasks to be performed using Gradle.

Before you can start developing GazePlay, you will need to run an initial build. This can be done on the command line with
```
> gradlew
$ ./gradlew
```

All required components will be installed from the internet, so make sure you're online.

##Java

GazePlay is compiled with Java 11 built with OpenJDK 11. If you need a distribution, [AdoptOpenJDK](https://adoptopenjdk.net/) 
offer a simple package to install on any Windows or Unix computer. Gradle will tell you if you're not running a 
compatible version of Java and prevent builds on anything lower than Java 11.

## Utilities

### Sl4J

Simple Logging Facade for Java (https://www.slf4j.org). Manages displays on the console or in the debug file gazeplay.log (automatically located in the default GazePlay directory).

**Therefore, the use of System.out or System.err is not allowed in GazePlay.**

To use it, simply put it before opening the @Sl4J class and then use it in the code (see *Lombok*).

For example,
```
int x = 3;
int y = 4
log.info("positionX : {} ; positionY : {}", x, y);
```

poster 

```
positionX : 3 ; positionY : 4
```

More information and message levels: 
https://www.tutorialspoint.com/log4j/log4j_logging_levels.htm

Log managment is done in gazeplay-commons/src/main/resources/logback.xml file (for both console and log file gazeplay.log).

### Lombok

Lombok allows you to simplify the Java code. The principle is to place at the beginning of the class tags that will be replaced at compilation by the corresponding machine code.

For example:
- @slf4j for log management
- @getter for accessors of the attributes of a class
- set attributes of a class for mutators

More information: 
https://projectlombok.org

### Spotbugs

Spotbugs prevents the introduction of bugs that could be automatically detected by static analysis of the code.

The configuration used in GazePlay includes the search for bugs. You can stop Spotbugs from running with this command.

```
> gradlew -PSpotBugs=false
$ ./gradlew -PSpotBugs=false
```

Make sure that the use of spotbugs does not find a bug before making a *pull request*.

```
> gradlew
$ ./gradlew
```

For more information, visit https://spotbugs.github.io/

### TravisCI

Travis CI is a free software for continuous integration ([Wikipedia] (https://en.wikipedia.org/wiki/Travis_CI)). 

Continuous integration is a set of practices used in software engineering to verify at each change in source code that 

the result of the changes does not produce regression in the developed application ([Wikipedia] (https://en.wikipedia.org/wiki/Continuous_integration)).
 
TravisCI builds a *build* for each *commit*, for each *pull request* and thus find problems more quickly.

A badge is added at the top of the README.md, indicating the status of the main branch (which should always be in *build | passing*).

For more information, visit https://docs.travis-ci.com/user/getting-started/

### "Ideal" command line

This command line performs all the operations verified by Travis

```
> gradlew
$ ./gradlew
```
