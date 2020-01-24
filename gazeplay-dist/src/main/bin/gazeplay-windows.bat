@echo on

SET JAVA_HOME=..\lib\jre
SET PATH=%JAVA_HOME%\bin;%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

java -cp "..\lib\*" -Xms256m -Xmx1g -Dlogging.appender.console.level=WARN net.gazeplay.GazePlayLauncher

pause
