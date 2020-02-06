# Creating a custom JRE

Since Java 9, the JDK and JRE have been modular. This means that we can create a custom JRE to distribute to users
of our application, and only include the modules we need. 

In this project, the `badass-runtime-plugin` found [here](https://badass-runtime-plugin.beryx.org/releases/latest) has 
been added to allow a custom JRE to be created, based on the jmods listed in [build.gradle](build.gradle). The
intention here is to remove the JRE from the Git repo (it is about 163MB at the time of writing) and move it to the
packaging process.

Some JMods require downloading from a vendor. In the case of the `javafx` jmods, you can find them 
[here](https://gluonhq.com/products/javafx/). Unpack them into your existing `java/jmods` folder on your development
machine.

To run the JRE creation script, simply run
```
> gradlew jre
$ ./gradlew jre
```

The JRE can then be found in [the src folder](src) and will be built into the package.
You can also remove the JRE with the `cleanJre` task.
