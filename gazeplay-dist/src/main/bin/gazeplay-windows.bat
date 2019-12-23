@echo on

SET JAVA_HOME=..\lib\jre
SET PATH=%JAVA_HOME%\bin;%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

java -cp "..\lib\*" -Xms256m -Xmx1g net.gazeplay.GazePlayLauncher

pause
