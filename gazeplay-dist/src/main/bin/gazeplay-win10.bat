@echo on

SET PATH=%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

..\lib\jre1.8.0_221\bin\java -Xms256m -Xmx1g -jar ..\lib\gazeplay-${project.version}.jar


REM prints a nice "Press any key to continue . . . " message
pause
