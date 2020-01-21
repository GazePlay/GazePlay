# Writing Tests

This document explains how to write unit tests for GazePlay.

## Running tests

Run all the tests in the project by running `./gradlew test`.
Run specific tests by importing the Gradle project into your IDE (such as IntelliJ) and clicking the included `Run` or
`Debug` buttons. 

## JUnit 5 Tests

All tests in GazePlay use JUnit 5 annotations. The most common you will use are `@BeforeEach`, `@AfterEach`, and `@Test`.
There are other annotations you may use, you can find more of these in [the official JUnit docs](https://junit.org/junit5/docs/current/user-guide/#writing-tests-annotations).

`@BeforeEach` methods will run before each test. You should put any setup code here, for example:
```java
@BeforeEach
void setup() {
    String uri = new File(localDataFolder + "song.mp3").toURI().toString();
    musicManager.getAudioFromFolder(localDataFolder);
    mediaPlayer = musicManager.createMediaPlayer(uri);
    previousVolume = musicManager.getCurrentMusic().getVolume();
}
```

`@AfterEach` methods will run after each test. You should put any code that clears up the testing environment, for example:
```java
@AfterEach
void tearDown() {
    if (new File("build/resources/test/META-INF/MANIFEST.MF").isFile()) {
        new File("build/resources/test/META-INF/MANIFEST.MF").delete();
        new File("build/resources/test/META-INF").delete();
    }
}
```

The main tests you write must be annotated by `@Test`, for example:
```java
@Test
void shouldReturnNullOnError() {
    String uri = new File(localDataFolder + "test.properties").toURI().toString();
    mediaPlayer = musicManager.createMediaPlayer(uri);
    assert mediaPlayer == null;
}
```

If you `import static org.junit.jupiter.api.Assertions.*;` you can use many useful assertions in your tests.

### TestFX

To test JavaFX in JUnit, you need to add [TestFX](https://github.com/TestFX/TestFX/wiki) to the test class. 
This can be done by adding this annotation to the test class, as so:
```java
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
public class AnotherJavaFXTest {
    ...
}
```

This will allow the test to run in headless mode during Travis and GitHub CI tests. 

_Note:_ In CI, this doesn't work with tests that use the `MediaPlayer` object from JavaFX. If you cannot mock this, make sure 
to include "Music" in the test file so Gradle knows not to run that test in CI. You should still be able to run the tests
locally.

## Mocking in Tests

In addition to JUnit 5 tests, you can also mock classes to inject into your tests. Mocked classes are very useful when 
you're testing methods that rely on a particular external call. When using Mocks, you can set the return value of methods
before running your test to ensure the right conditions are set.

### Mockito

[Mockito](site.mockito.org) is used to mock Objects you inject into methods for testing. The general way to mock an object is;
1. Initialise the Mock. This uses the `mockito.Mock` annotation on the Object you wish to mock. By convention, this should
be named `mock<Object Name>` such as `mockConfiguration
```java
import org.mockito.Mock;

@Mock
Configuration mockConfiguration;
```
2. Initialise the Mocks. Make sure this happens before each test, so the mocks are reset.
```java
@BeforeEach
public void initMocks() {
    MockitoAnnotations.initMocks(this);
}
```
3. Stub your methods. This tells Mockito that whenever this method is called, it should return the value you specify. 
There are lots of ways to return values a certain number of times, or to throw Exceptions, so read the docs for more 
information.
```java
// This will return the value 2 when getFixationLength() is called on this mock object.
when(mockConfiguration.getFixationLength()).thenReturn(2);
```
4. Apply your mock object to the method under test.
```java
testObject.testMethod(mockConfiguration);
```
5. You can verify if methods were called too.
```java
verify(mockConfiguration.getFixationLength()); // Will pass the test if the method is called.
```

### JMockit

In certain circumstances, you may need to mock static methods. This cannot be done in Mockito, so [JMockit](https://jmockit.github.io/) 
has been brought in to allow this functionality. To mock a static method, you should follow the same structure as below.
```java
new MockUp<StatsContext>() {
    @mockit.Mock
    public StatsContext newInstance(GazePlay gazePlay, Stats stats) {
        return mock(StatsContext.class);
    }
};
```
In this example `StatsContext` contains the static method `newInstance`. We are using it to return another mock in this
case, but you can set the return to be whatever you need.

JMockit also requires a specific JVM arg, which should be applied to the `build.gradle` file for the module you are 
testing within. If it doesn't already have it, add the following block to that file:
```groovy
test {
    jvmArgs "-javaagent:${classpath.find { it.name.contains("jmockit") }.absolutePath}"
}
```

