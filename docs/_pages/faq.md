---
title: FAQ
permalink: /faq/
layout: single
toc: true
toc_sticky: true
---

## Where is the default GazePlay folder?

|           | Location                         | 
|-----------|:---------------------------------|
| Windows   |   `C:\Users\<username>\GazePlay` |
| MacOS     |   `/Users/<username>/GazePlay`   |
| Linux     |   `/home/<username>/GazePlay`    |

## How can I add my own Images to GazePlay?
In a folder of your choice, create a folder called `images`.

`images` can have 3 sub-folders:
* `portraits`: images used by creampie, ninja and portrait bubbles. Images should be small (300×300 pixels).
* `magiccards`: images used by magic cards. You can use all image sizes.
* `blocs`: images used by blocks and scratchcard. Choose big images, preferably the same size as the screen.

You can change the image folder within GazePlay in the configuration menu, so you can have as many image folders as you like.

### Adding custom games to _Where Is It?_
Follow these steps to find the Where Is It folder on your computer, and add your own custom games:

#### Windows
{% include figure image_path="/assets/images/tutorial/my_pc.png" alt="My PC" caption="In Explorer, navigate to 'My PC'. Click on the C:/ Drive" %}

{% include figure image_path="/assets/images/tutorial/c_disk.png" alt="C Drive" caption="Click on 'Users', then select your username." %}

{% include figure image_path="/assets/images/tutorial/gazeplay_folder.png" alt="GazePlay folder" caption="In your user folder, you should find the GazePlay folder. Click to open it." %}

{% include figure image_path="/assets/images/tutorial/where_is_it.png" alt="Where Is It folder" caption="In GazePlay folder, you should find the `where-is-it` folder. Click to open it." %}

Before adding your photos, you'll need to create a `questions` file. 
To do this, open some spreadsheet software, like Excel or [Google Sheets](https://docs.google.com/spreadsheets). Then, fill a table such as the example below:

|         | pictos        | eng                   | 
|---------|:--------------|:----------------------|
| bathtub | `bathtub.png` | Where is the bathtub? |
| biscuit | `biscuit.png` | Where is the biscuit? |
| bus     | `bus.png`     | Where is the bus?     |

The first column is the name of the folders you'll add, the `pictos` column is the name of the image file in that folder, and `eng` represents the english translation of the question to ask the player. You can change this from `eng` to another language if you want - the game will choose the right translation for your chosen language. Supported languages are:
`alb, ara, chn, deu, ell, eng, fin, fra, hrv, ind, ita, jpn, pol, por, zsm, nld, rus, spa, vnm`

Remember to save your spreadsheet as `questions.csv` and place it in the `where-is-it` folder.

{% include figure image_path="/assets/images/tutorial/images.png" alt="My PC" caption="Click on 'images'" %}

{% include figure image_path="/assets/images/tutorial/folders.png" alt="My PC" caption="Here, place all your images in categorised folders. One folder per type of image you wish to add." %}



## How can I create a shortcut on Windows?
1. Locate `gazeplay-windows.bat` in the `/bin` folder in your GazePlay installation folder.
1. Right-click on `gazeplay-windows.bat` and select create a shortcut.
2. Move the shortcut on the desktop.
3. Double-click on the shortcut to launch GazePlay.

## I'm clicking on gazeplay-windows.bat but it doesn't work. Why?
You may see an error message stating that you don’t have a Java Virtual Machine on your computer.
To fix this, check that there is a `jre` folder in your GazePlay installation folder. If there
is not, you will need to redownload the latest version of GazePlay. 

If you experience any other problems, send an email to <didier.schwab@univ-grenoble-alpes.fr> or [raise an Issue with our Developers directly](https://github.com/GazePlay/GazePlay/issues/new)
