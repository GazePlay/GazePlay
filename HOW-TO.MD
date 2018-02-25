From https://github.com/schwabdidier/GazePlay/issues/161#issuecomment-362115364

In order to be able to run gazeplay in your IDE, 
without having to mess around your global PATH env variable for the Windows user.

Just configure the run configuration of your IDE, in order to add the dll directory to the ```Path``` env variable.

The value to set would be
```
C:\Users\yann\AppData\Local\TobiiStreamEngineForJava\lib\tobii\x64
```
if your windows username is ```yann``` (adapt to your own username)
(did not managed to use a environment variable reference here, it seems you have to use the fully hardcoded value)

Here is a screenshot showing how to do it with Intellij

![intellij_run_configuration_screenshot](https://user-images.githubusercontent.com/1653590/35654449-7125e424-06ed-11e8-847e-3f3a5c5169a9.png)

From 