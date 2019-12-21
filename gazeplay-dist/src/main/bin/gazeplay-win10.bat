@echo on

SET PATH=%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

..\lib\jre\bin\java -cp "..\lib\*" -Xms256m -Xmx1g net.gazeplay.GazePlayLauncher

pause
