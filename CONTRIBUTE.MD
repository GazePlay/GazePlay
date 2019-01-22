# How to contribute to GazePlay: good practices

This page is intended for students doing an internship supervised by Didier Schwab.

Any contributor can take inspiration from this page but is obviously not obliged to seek Didier Schwab's *a priori* approval. 

## Git and GitHub

GazePlay is an open source project hosted on GitHub.

GazePlay's development language is **English**.

## Open a ticket
The easiest way to contribute is to open a ticket (*issue*). You can declare a bug, propose an improvement, a new game,... This ticket can be discussed by all contributors (including other interns) and finally accepted by Didier Schwab.

## Coding
A good practice is to follow the following steps.
1. Identify an issue to be resolved and propose a solution
2. *Fork* the project and implement its solution
3. Propose a *pull request*

You will find further information [here](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github/contribuer-a-des-projets-open-source)

## Pull request
You will be asked to do at least a *pull request* **daily** to avoid that your code deviates too far from the main branch. Each sweater must be documented as well as possible. By making the link, among other things, with the corresponding outcome.

In order to make a *pull request*, always do a *maven clean install* which has the effect of standardizing the code (indentations, spaces,...).

Each request will be examined by one of the contributors.

## Maven
Maven is a tool for managing and automating the production of Java software projects ([Wikipedia] (https://en.wikipedia.org/wiki/Apache_Maven). Maven is generally integrated into IDE.

Further information can be found at https://maven.apache.org

For the common use of *maven*, you will have to do at the beginning to install all the packages and then before any *pull request*

```
mvn clean install
```

## Utilities

#### Sl4J

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


#### Lombok

Lombok allows you to simplify the Java code. The principle is to place at the beginning of the class tags that will be replaced at compilation by the corresponding machine code.

For example:
- @slf4j for log management
- @getter for accessors of the attributes of a class
- set attributes of a class for mutators

More information: 
https://projectlombok.org

#### Findbugs

Findbugs prevents the introduction of bugs that could be automatically detected by static analysis of the code.

The configuration used in GazePlay excludes the search for bugs because it makes builds much longer.

```
mvn clean install -Dfindbugs.skip=true
```

Make sure that the use of findbugs does not find a bug before making a *pull request*.

```
mvn clean install -Dfindbugs.skip=false
```

For more information, visit http://findbugs.sourceforge.net

#### formatter-maven-plugin

This plugin is automatically called when you make a call to Maven. It allows you to perform a certain number of operations such as normalizing the end of a line, spaces, etc.

For more information, visit http://code.revelc.net/formatter-maven-plugin/

#### TravisCI

Travis CI is a free software for continuous integration ([Wikipedia] (https://en.wikipedia.org/wiki/Travis_CI)). Continuous integration is a set of practices used in software engineering to verify at each change in source code that the result of the changes does not produce regression in the developed application ([Wikipedia] (https://en.wikipedia.org/wiki/Continuous_integration)).
 
TravisCI builds a *build* for each *commit*, for each *pull request* and thus find problems more quickly.

A badge is added at the top of the README.md, indicating the status of the main branch (which should always be in *build | passing*).

For more information, visit https://docs.travis-ci.com/user/getting-started/

#### "Ideal" command line

This command line performs all the operations verified by Travis
```
mvn clean install -Dfindbugs.skip=false -DskipFormatterFormatFormat=false -DskipFormatterValidate=false
```