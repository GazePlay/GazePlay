# How to contribute to GazePlay: good practices

This page is intended for students doing an internship supervised by Didier Schwab.

Any contributor can take inspiration from this page but is obviously not obliged to seek Didier Schwab's *prior* approval. 

In these instructions, actions performed in the Windows command line are prefixed with `>`, and those on the Unix
shell are prefixed with `$`. Select the command appropriate for your platform.

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

### Updating your own fork
You will often find that your own fork of GazePlay is lagging behind the main GazePlay repo. To update your fork, follow 
these steps:

```shell script
# Set your upstream repository if you haven't already
git remote add upstream https://github.com/GazePlay/GazePlay.git

# Pull the latest master into your master
git checkout master
git pull upstream master
git push <myrepo> master

# Make your changes
git checkout -b MyChange
# - your commits go here -
git push <myrepo> MyChange

# Reset to master and repeat the process for the next PR
git checkout master
```

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

## Java

GazePlay is compiled with Java 11 built with OpenJDK 11. If you need a distribution, [AdoptOpenJDK](https://adoptopenjdk.net/) 
offer a simple package to install on any Windows or Unix computer. Gradle will tell you if you're not running a 
compatible version of Java and prevent builds on anything lower than Java 11.

## Testing

It is good practise, and necessary for finding breaking bugs, to write Unit Tests for the code you write. To start writing
tests, use your IDE to find if any test file already exists. In IntelliJ, this is done with `CMD + SHIFT + T` (MacOS) or 
`CTRL + SHIFT + T` (Windows and Linux). You can also look for the file in `src/test/java/<java package name>` (such as 
`src/test/java/net/gazeplay`).

A good test will cover as much of the tested method as possible. Each one should set up the object-under-test and 
cover a single path through the method. For example, if your method has an `if` statement in it, you should write a test
for when that resolves to `true`, and one where it resolves to `false`. You can get a visualisation of which code has 
been tested by running `./gradlew jacocoTestReport`. This will run all the tests and output an HTML index that you can
open in your browser. It will highlight all code that has been tested and what has not. 

Give your tests meaningful names. If a method should play a sound when a button is clicked, name it 
`shouldPlaySoundWhenButtonClicks`. This helps other developers understand what your code should be doing, and helps with
debugging tests if other dependant code has changed.

**There are lot of good resources online on how to write good unit tests. If you need somewhere to start, read 
[this article](https://manifesto.co.uk/unit-testing-best-practices-java/**

GazePlay uses [JUnit 5](https://junit.org/junit5) as its testing framework, and you can find many examples of how
these tests are written both online and in the test files themselves.

You will often need to use [TestFX](https://github.com/TestFX/TestFX/wiki) in your tests to access JavaFX components, see
existing tests on how to add this to your own.

Mocking of objects is done using [Mockito](site.mockito.org), whilst static objects should be mocked with 
[JMockit](https://jmockit.github.io/). You can find examples in this repo, whilst far more are available online. Please
feel free to suggest new alternatives if they provide a better testing experience.

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
