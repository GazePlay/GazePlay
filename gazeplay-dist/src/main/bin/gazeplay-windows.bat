@echo on

REM Paths are resolved from the location of this script, whatever the working directory
SET JAVA_HOME=%~dp0..\lib\jre
SET PATH=%JAVA_HOME%\bin;%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

java -cp "%~dp0..\lib\*" -Xms256m -Xmx1g --enable-native-access=javafx.graphics,javafx.media,ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -Dlogging.appender.console.level=OFF net.gazeplay.GazePlayLauncher %*

pause
