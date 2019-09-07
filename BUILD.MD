# Build instructions 

In these instructions, actions performed in the Windows command line are prefixed with `>`, and those on the Unix
shell are prefixed with `$`. Select the command appropriate for your platform.

## How to build the software from source

Once you have cloned the repository, run the following command:
```
> gradlew clean build
$ ./gradlew clean build
```

## How to build the software, for distribution

```
# Make sure local repository is clean, so that you get the same result as a first time user
> gradlew clean
$ ./gradlew clean

# Build binaries once and install them in local repository, this is needed for the license plugin to work
> gradlew clean build
$ ./gradlew clean build

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
> gradlew clean build
$ ./gradlew clean build

# Remove the -SNAPSHOT from the version label, commit and tag the version in SCM, build binaries, including license information, and increment to the next -SNAPSHOT version label.  
> gradlew clean downloadLicenses build
$ ./gradlew clean downloadLicenses build
```

At this time, a release task has not been created. One will come in the future, but for now a zip and tar archive will 
be found in [`/build/distributions/`]() for the version built. 
