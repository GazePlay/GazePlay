@echo on

SET PATH=%PATH%;%LocalAppData%\TobiiStreamEngineForJava\lib\tobii\x64

java -Xms256m -Xmx1g -jar ..\lib\@NAME@-@VERSION@.jar


REM prints a nice "Press any key to continue . . . " message
pause
