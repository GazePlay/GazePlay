# Build instructions 
In these instructions, actions performed in the Windows command line are prefixed with `>`, and those on the Unix
shell are prefixed with `$`. Select the command appropriate for your platform.

## How to build the software from source
Once you have cloned the repository, run the following command:
```
> gradlew
$ ./gradlew
```
You may decide to add the `-PSpotBugs=true` flag to check for bugs. This is disabled by default.
e.g. `gradlew -PSpotBugs=true`

## [Creating a custom JRE](gazeplay-dist/README.md)

## Utilising Spotbugs feature
You can make Gradle find any potential bugs by running a standard `gradlew` build.

Then, download the [Spotbugs GUI](http://repo.maven.apache.org/maven2/com/github/spotbugs/spotbugs/4.0.0-beta3/spotbugs-4.0.0-beta3.zip) 
and run the GUI from `/lib/spotbugs.jar`. Open the generated XML file in this software to see where all the potential
bugs are.

## How to build the software, for distribution
```
# Make sure local repository is clean, so that you get the same result as a first time user
> gradlew clean
$ ./gradlew clean

# Running the packageApp task will compile all code, generate JAR files for all modules and dependencies, and package
everything up into Zip and Tar formats.
> gradlew packageApp
$ ./gradlew packageApp
```

## How to release the software, for version tagging in the official SCM and binary distribution
```
# Make sure local repository is clean, so that you get the same result as a first time user
> gradlew clean
$ ./gradlew clean

# Run the release task and follow the on-screen instructions. This will fail if any changes haven't been commited or 
pushed.
> gradlew release
$ ./gradlew release
```
