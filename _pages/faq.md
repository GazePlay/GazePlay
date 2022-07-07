---
title: FAQ
permalink: /faq/
layout: single
toc: true
toc_sticky: true
---

## Where is the default GazePlay folder?

Here are the default paths to the `GazePlay` folder depending on your operating system:

|         | Location                       | 
|---------|:-------------------------------|
| Windows | `C:\Users\<username>\GazePlay` |
| MacOS   | `/Users/<username>/GazePlay`   |
| Linux   | `/home/<username>/GazePlay`    |

### Windows

{% include figure image_path="/assets/images/tutorial/this_pc_folder.png" alt="This PC" caption="In Explorer, navigate to `This PC`. Click on the `C:` Drive" %}

{% include figure image_path="/assets/images/tutorial/c_disk_folder.png" alt="C Drive" caption="Click on `Users`, then select your username." %}

{% include figure image_path="/assets/images/tutorial/users_folder.png" alt="Users folder" caption="In your user folder, you should find the `GazePlay` folder. Click to open it." %}

## How can I add my own Images to GazePlay?

### Adding custom images

In a folder of your choice, create a folder called `images`.

`images` can have 3 sub-folders:
* `portraits`: images used by creampie, ninja and portrait bubbles. Images should be small (300×300 pixels).
* `magiccards`: images used by magic cards. You can use all image sizes.
* `blocs`: images used by blocks and scratchcard. Choose big images, preferably the same size as the screen.

You can change the image folder within GazePlay in the configuration menu, so you can have as many image folders as you like.

### Adding custom games to _Where Is It?_

Follow these steps to add your own custom variants at the _Where Is it?_ game.

#### Windows

{% include figure image_path="/assets/images/tutorial/gazeplay_folder.png" alt="GazePlay folder" caption="Go to the `where-is-it` folder, located in the default `GazePlay` folder." %}

If this folder does not exist, create it.
You can change the location or name of this folder as you see fit as long as you change the corresponding setting in the configuration menu.

This folder should contain three subfolders: `images`, `pictos`, `sounds`.
If they don't exist, create them.

The `images` folder is the most important for the proper functioning of the game.

{% include figure image_path="/assets/images/tutorial/where-is-it_folder_images.png" alt="where-is-it folder" caption ="Click on `images`." %}

You must create as many sub-folders inside as you want element to guess.
It is in each of these folders that you will place your personalized images.
You can thus put several images for the same element in the corresponding folder.
The names of the images inside have no importance, it is only the name of the folder that defines the element.

{% include figure image_path="/assets/images/tutorial/images_folder.png" alt="Images folder" caption="Here is an example of how to complete the `images` folder." %}

Empty folders will be ignored.
It is recommended to create at least 9 non-empty folders for guessing elements.
If you create less, some variants of the game will not work due to lack of items to fill the grid.
For example, in the case of the 3×3 grid with less than 9 elements to guess.

You can also add your own custom sounds.
These are played when the question is displayed or repeated when the player gets it wrong if the corresponding setting is enabled.

{% include figure image_path="/assets/images/tutorial/where-is-it_folder_sounds.png" alt="where-is-it folder" caption ="Go back to the `where-is-it` folder and click on `sounds`." %}

Place all your sounds (e.g. `.mp3` files) in this folder and give them the same name as the image folders associated with them.

{% include figure image_path="/assets/images/tutorial/sounds_folder.png" alt="Sounds folder" caption="Here is an example of how to complete the `sounds` folder." %}

In the same way, you can add personalized pictograms which will be displayed at the same time as the question.
All you have to do is add your pictograms in the `pictos` folder, without worrying about their names.

Finally, you must create a `questions.csv` file which will mainly contain the texts of the questions to ask the player.
To do this, open some spreadsheet software, like Excel or [Google Sheets](https://docs.google.com/spreadsheets).
Then, fill a table such as the example below:

|         | pictos        | eng                   | 
|---------|:--------------|:----------------------|
| bathtub | `bathtub.png` | Where is the bathtub? |
| biscuit | `biscuit.png` | Where is the biscuit? |
| bus     | `bus.png`     | Where is the bus?     |

How to fill the columns:
* The first column is the name of the folders you'll add.
* The `pictos` column is the name of the image file in that folder.
* And the `eng` column represents the english translation of the question to ask the player.

You can switch from English to another language if you wish or also add as many language columns as you think necessary.
The game will automatically choose the translation for the language selected in the settings menu.
Supported languages are:
`alb`, `ara`, `chn`, `deu`, `ell`, `eng`, `fin`, `fra`, `hrv`, `ind`, `ita`, `jpn`, `pol`, `por`, `zsm`, `nld`, `rus`, `spa`, `vnm`, `bel`, `hin`.

Remember to save your spreadsheet as `questions.csv` and place it in the `where-is-it` folder.

There you go, you've just created your own variant of the Where is it? game.

You can create and fill in the same way as many `where-is-it` folders as you want to have lots of different variants.
Then you just have to change the location of the folder in the configuration menu to choose the variant you want to play.

Here is a complete example of a `where-it-is` folder that you can play with or get inspired to create your own variations: <a href="https://github.com/GazePlay/GazePlay/raw/gh-pages/assets/example-where-is-it.zip" download>example-where-it-is</a>.

## How can I create a shortcut for a game?

1. Launch GazePlay and go to settings via the blue gear.
{% include figure image_path="/assets/images/tutorial/EN/buttonSettings.png" alt="Button settings of GazePlay." %}

2. In the 'Language settings' section then 'Create Game Shortcuts', you can:
* Choose where to put the shortcut (by default on the desktop)
* Choose the game that we will put in shortcut
* If the chosen game has variants, you can also choose its variant
* The button that generates the shortcut

{% include figure image_path="/assets/images/tutorial/EN/settings.png" alt="Settings of GazePlay." %}

Once we have chosen where to put the shortcut and which game we want in shortcut, we click on the button 'Generate the shortcut'. <br>
We thus obtain the shortcut of the chosen game:

{% include figure image_path="/assets/images/tutorial/shortcuts.png" alt="A shortcut of a game." %}

## How can I create a shortcut on Windows?

1. Locate `gazeplay-windows.bat` in the `/bin` folder in your GazePlay installation folder.
1. Right-click on `gazeplay-windows.bat` and select create a shortcut.
2. Move the shortcut on the desktop.
3. Double-click on the shortcut to launch GazePlay.

## I'm clicking on `gazeplay-windows.bat` but it doesn't work. Why?

You may see an error message stating that you don’t have a Java Virtual Machine on your computer.
To fix this, check that there is a `jre` folder in your GazePlay installation folder. If there
is not, you will need to redownload the latest version of GazePlay. 

If you experience any other problems, send an email to <didier.schwab@univ-grenoble-alpes.fr> or [raise an Issue with our Developers directly](https://github.com/GazePlay/GazePlay/issues/new).
