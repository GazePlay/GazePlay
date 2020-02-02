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

There is a separate task that will run at build time, `jre`, that will used the stored JDKs to create the JREs for the
platform.

Before running `distribution` for the first time, you must run
```
> gradlew downloadAndExtractJDKs
$ ./gradlew downloadAndExtractJDKs
```

The JDKs will be downloaded to your local Maven repository. By default, this will be `/home/user/.m2/repository/jre` on 
UNIX, and `C:\Users\current user\.m2\repository\jre` on Windows.

Any future builds will rely on these folders being present locally. You can always rerun the task to restore your 
repository.

The JRE can then be found in [the build folder](../build/jre) and will be built into the distribution for the relevant OS.
