@echo on

SET JAVA_HOME=..\lib\jre
SET PATH=%JAVA_HOME%\bin;%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

java -cp "..\lib\*" -Xms256m -Xmx1g --add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED -Dlogging.appender.console.level=OFF net.gazeplay.GazePlayLauncher %*

pause
