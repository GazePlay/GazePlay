# Build instructions 
In these instructions, actions performed in the Windows command line are prefixed with `>`, and those on the Unix
shell are prefixed with `$`. Select the command appropriate for your platform.

## How to build the software from source
Once you have cloned the repository, run the following command:
```
> gradlew
$ ./gradlew
```
You may decide to add the `-x spotbugsMain` flag to stop the build from checking bugs. This is also done in the 
Travis CI build.

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

# Build binaries once and install them in local repository, this is needed for the license plugin to work
> gradlew build
$ ./gradlew build

# Build binaries, including license information
> gradlew clean downloadLicenses build
$ ./gradlew clean downloadLicenses build
```

## How to release the software, for version tagging in official SCM and binaries distribution
```
# Make sure local repository is clean, so that you get the same result as a first time user
> gradlew clean
$ ./gradlew clean

# Build binaries once and install them in local repository, this is needed for the license plugin to work
> gradlew build
$ ./gradlew build

# Remove the -SNAPSHOT from the version label, commit and tag the version in SCM, build binaries, including license information, and increment to the next -SNAPSHOT version label.  
> gradlew clean downloadLicenses build
$ ./gradlew clean downloadLicenses build
```
At this time, a release task has not been created. One will come in the future, but for now a zip and tar archive will 
be found in [`/build/distributions/`]() for the version built. 
