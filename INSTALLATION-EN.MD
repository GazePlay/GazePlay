# How to install GazePlay

## Prerequisites

You must have:
* a github account.
* a jetbrains account for Intellij. (you can create them as you install)

<br>Note :
* the github account is free.
* the jetbrains account is free for students. (you have to fill in your student email address)

## Step 1 : Install Intellij ultimate edition

The download of Intellij **VERSION ULTIMATE** is here : https://www.jetbrains.com/idea/download/#section=windows
<br>
If you don't download the ultimate version it won't work because spring is not supported by the classic version.

![telechargement intellij](AssetsInstallation/DL-intellij-EN.png)

Then run the installer and follow the installation procedure

## Step 2 : Fork the project Gazeplay

To fork the project, go to the Gazeplay git repository: https://github.com/GazePlay/GazePlay
<br>
Then click on the fork button at the top right of the screen.

![fork](AssetsInstallation/fork.png)

## Step 3 : get the forked project

Start Intellij, it will ask you to connect with your account (you have to create it if you don't have one).
<br> Once connected go to "get from vcs" select your account.

![GetFromVCS](AssetsInstallation/GetFromVCS.png)

Then select the gazeplay project and clone.

![clone](AssetsInstallation/clone.png)

## Step 4 : télécharger le SDK azul-17

In Intellij, you have to download the azul-17 SDK.
<br>
To do this: file -> project structure -> SDKs -> "+" -> download JDK

![telechargement azul](AssetsInstallation/DL-azul-FR.png)

Make sure that the project is properly configured on the previously downloaded azul-11.
<br>
To do this: file -> project structure -> Project

![mise en place azul](AssetsInstallation/DL-setAzul.png)

## Step 5 : Check JDK on Gradle settings

Check if gradle use the Azul JDK 17, else change it.

![gradle settings](AssetsInstallation/gradleSettings.png)

## Step 6 : Build the project

To launch the build: press the green hammer at the top right of the screen and the build will start.

![build](AssetsInstallation/build.png)

Normally you get this.

![buildsuccessfull](AssetsInstallation/editconfig.png)

If the edit configuration is not created after the build, restart the intellij.

## Step 7 : start the projet

To launch the project: press the green arrow at the top right of the screen and gazeplay will start.

![run](AssetsInstallation/run.png)

Try to run at least one game.

## You have completed the installation

From here you have finished the installation.

If you encounter any problems, do not hesitate to contact the team.
