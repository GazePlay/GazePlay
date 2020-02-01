# Creating a custom JRE

Since Java 9, the JDK and JRE have been modular. This means that we can create a custom JRE to distribute to users
of our application, and only include the modules we need. 

In this project, the `badass-runtime-plugin` found [here](https://badass-runtime-plugin.beryx.org/releases/latest) has 
been added to allow a custom JRE to be created, based on the jmods listed in [build.gradle](build.gradle). 

There are a number of JRE related tasks in [jre.gradle](../gradle/jre.gradle) that will download a specific JDK from
[AdoptOpenJDK](https://adoptopenjdk.net/) as well as the JMods for JavaFX from [Gluon](https://gluonhq.com/). Each
platform has the same tasks; 
* Download the JDK
* Download the JMods
* Placing the JMods into the JDK
* Creating a JRE for that platform.

To run the JRE creation script for Linux x64, MacOS and Windows x64, simply run
```
> gradlew jre
$ ./gradlew jre
```

The JRE can then be found in [the build folder](../build/jre) and will be built into the distribution for the relevant OS.

## Creating JREs for a single platform

The task `create<OS>JDK` where `<OS>` can be any of `Windows`, `Linux`, or `Macos` will perform the tasks defined above
for only the OS you need. 
