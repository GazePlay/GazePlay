# screen-recorder
Java screen recorder library

<img src="resources/images/java-icon.png" height="128" />
<img src="resources/images/play-icon.png" height="128" />
<img src="resources/images/testng-logo.png" height="128" />

Description
-----------

Screen recorder is an Open Source library (under the [MIT Licence](LICENSE)) that allows you to record the screen from your Java code.

Nowadays the library "only" records the screen into a .mov format, creating videos without audio using the minimum resources.

Getting started
-----------

###Adding dependencies

--
 1. Add the repository:

   ```xml
  <repositories>
		<repository>
			<id>screen-recorder</id>
			<name>Java screen recorder library by agomezmoron</name>
			<url>https://raw.github.com/agomezmoron/screen-recorder/mvn-repo</url>
		</repository>
	</repositories>
    ```
 2. Adding the following maven dependency in you ```pom.xml``` file:


    ```xml 
    <dependency>
      <groupId>com.github.agomezmoron</groupId>
      <artifactId>screen-recorder</artifactId>
      <version>0.0.3</version>
    </dependency>
    ```
    
###How to use

It's very easy: 

 1. Configure the video interval in ms: by default it is 100ms (10 frames/sec).
 2. Configure the width/height or the full screen mode: by default it's full screen mode.
 3. Configure the video directoy where the video will be saved: by default is the temporal folder.
 4. Set the *keepFrames* option into true/false if you want to keep the frames as .jpeg files: by defaul the library doesn't keep the frames.
 5. Call the *start* method passing the video name (ex: "myVideo" -> output: myVideo.mov).
 6. Perform your actions...
 7. Call the *stop* method

Example:

 ```
  // configuration
  VideoRecorderConfiguration.setCaptureInterval(50); // 20 frames/sec
  VideoRecorderConfiguration.wantToUseFullScreen(true);
  VideoRecorderConfiguration.setVideoDirectory(new File("~/")); // home
  VideoRecorderConfiguration.setKeepFrames(false);
  // you can also change the x,y using VideoRecorderConfiguration.setCoordinates(10,20);
  
  VideoRecorder.start("test");
  // EXECUTE ALL YOU WANT TO BE RECORDED
  String videoPath = VideoRecorder.stop(); // video created
  System.out.println(videoPath);
```

###How to use in TestNG

For TestNG you have a configuration class called *VideoRecorderTestNGConfiguration* which extends from *VideoRecorderConfiguration*. So, through it, you can configure the VideoRecorder and also some specific TestNG parameters:
 
 - VideoRecorderTestNGConfiguration.wantToRecordTest(true/false); // if you want to record your tests or not. By default is true.
 - VideoRecorderTestNGConfiguration.wantToKeepVideoOnSuccess(true/false); // if you want to keep the video once the test finished successfully. By default is false.
 - VideoRecorderTestNGConfiguration.wantToKeepVideoOnFailure(true/false); // if you want to keep the video once the test  failed. By default is true.
 
To enable the recordin you only have to add the listener like:

```
   @Listeners({VideoRecorderListener.class})
   public class TestNGClass {
   
   	// if you want to configure the suite from each class
   	@BeforeClass
   	public void beforeClass() {
   		VideoRecorderTestNGConfiguration.wantToKeepVideoOnSuccess(true);
   		VideoRecorderTestNGConfiguration.setCaptureInterval(20);
   	}
    .....
   }
```
###VideoRecorder configuration

You can use the simple configuration or use your custom one defining the x,y coordinates and also the width/height size (by default, it records in full screen. Take care if before starting the video you have configured those options but you run *VideoRecorderConfiguration.wantToUseFullScreen(true);*, the previous configurations will be reverted.

License
-----------
The MIT License (MIT)

Copyright (c) 2016 Alejandro Gómez Morón

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
