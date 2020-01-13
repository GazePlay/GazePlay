---
title: FAQ
permalink: /faq/
layout: single
---

## Where is the default GazePlay folder?
|         | Location                       |
|---------|--------------------------------|
| Windows | `C:\Users\<username>\GazePlay` |
| MacOS   | `/Users/<username>/GazePlay`   |
| Linux   | `/home/<username>/GazePlay`    |

## How can I add my own Images to GazePlay?
In a folder of your choice, create a folder called `images`.

`images` can have 3 sub-folders:
* `portraits`: images used by creampie, ninja and portrait bubbles. Images should be small (300×300 pixels).
* `magiccards`: images used by magic cards. You can use all image sizes.
* `blocs`: images used by blocks and scratchcard. Choose big images, preferably the same size as the screen.

You can change the image folder within GazePlay in the configuration menu, so you can have as many image folders as you like.

## How can I create a shortcut on Windows?
1. Locate `gazeplay-windows.bat` in the `/bin` folder in your GazePlay installation folder.
1. Right-click on `gazeplay-windows.bat` and select create a shortcut.
2. Move the shortcut on the desktop.
3. Double-click on the shortcut to launch GazePlay.

## I'm clicking on gazeplay.bat but it doesn't work. Why?
You may see an error message stating that you don’t have a Java Virtual Machine on your computer.
To fix this, check that there is a `jre` folder in your GazePlay installation folder. If there
is not, you will need to redownload the latest version of GazePlay. 

If you experience any other problems, send an email to [mailto](didier.schwab@univ-grenoble-alpes.fr) or [raise an Issue with our Developers directly](https://github.com/GazePlay/GazePlay/issues/new)
