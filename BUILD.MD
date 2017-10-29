# Build instructions 

## How to build the software from sources

```
mvn clean install
```

## How to build the software, for distribution

```
# Make sure local repository is clean, so that you get the same result as a first time user
find ~/.m2/repository -name "*gazeplay*" | xargs rm -rf
# Build binaries once and install them in local repository, this is needed for the license plugin to work
mvn clean install
# Build binaries, including license information
mvn clean license:download-licenses license:add-third-party install 
```

## How to release the software, for version tagging in official SCM and binaries distribution

```
# Make sure local repository is clean, so that you get the same result as a first time user
find ~/.m2/repository -name "*gazeplay*" | xargs rm -rf
# Build binaries once and install them in local repository, this is needed for the license plugin to work
mvn clean install
# Remove the -SNAPSHOT from the version label, commit and tag the version in SCM, build binaries, including license information, and increment to the next -SNAPSHOT version label.  
mvn clean license:download-licenses license:add-third-party release:prepare 
```
