# GazePlay 1.12.0 (draft)

Draft release notes of the modernization (branch `addClaudeCode`, October 2026). The version number is a proposal:
it is chosen by the maintainers at release time. Details, verifications and remaining work: [MODERNIZATION.md](MODERNIZATION.md).

## Important changes

### New name: GazePlay Learning
- GazePlay is now called **GazePlay Learning**: title of the window, Windows installer and shortcuts, names of the
  downloads (`gazeplay-learning-windows-x64.zip`, `gazeplay-learning-linux-x64.tar.gz`...) and of the launch scripts
  (`gazeplay-learning-windows.bat`, `gazeplay-learning-linux.sh`...).
- **The Windows installer installs GazePlay Learning next to a former GazePlay** (folder
  `C:\Program Files\GazePlay Learning`, its own shortcuts and uninstaller): a GazePlay already installed is left as it
  is, and can be uninstalled from the Windows settings. The data are shared (see below).
- Shortcuts to a game created by a former version start the former GazePlay (or no longer work once it is
  uninstalled): create them again from the settings.
- Logo: the GAZE PLAY letters and the mascot are kept, with "Learning" below (written with the Fredoka font, SIL Open
  Font License 1.1).

### Mouse, eye tracker, or both
- The eye tracker setting has three choices: **mouse only**, **Tobii only (gaze)** and **Tobii and mouse**. With
  "Tobii only", the mouse no longer acts in the games (it used to fill the progress indicators too); it still works in
  the menus and in the configuration panel of the games. "Tobii and mouse" is the former behaviour, and the default.
- A computer already set to "Tobii" is now in "Tobii only": choose "Tobii and mouse" to play with both.
- The statistics screen only offers the images of the inputs of this setting: the mouse, the gaze, or both (mouse,
  gaze, mouse and gaze). The three images are still saved in the results folder.

### Supported platforms: 64 bits only
- **The 32 bits versions are no longer provided**: Windows x86 (`gazeplay-windows-x32`, `gazeplay-afsr-windows-x32`,
  `gazeplay-bera-windows-x32` and their installers) and Linux ARM 32 bits.
- **The ARM version (`gazeplay-arm`) is now 64 bits (aarch64)**, for example Raspberry Pi OS 64 bits. A computer
  running a 32 bits system must keep GazePlay 1.11.
- **New version for Macs with Apple Silicon** (`gazeplay-macos-aarch64`); `gazeplay-macos` remains the version for
  Intel Macs.
- **Only GazePlay remains: the AFSR and BERA (GazePlay Eval) editions are no longer provided** (their Windows
  versions and installers, the `--afsrgazeplay` and `--bera` options, their launch scripts on every platform, and
  the "AFSR-Green" and "AFSR-Purple" colour themes: a computer set to one of these themes uses the default theme).
  Starting GazePlay after a BERA or AFSR session opens GazePlay normally. The edition file `argsGazeplay.txt` is no
  longer read nor written.
- Remaining versions: Windows x64, Linux x64, Linux ARM 64 bits, macOS (Intel and Apple
  Silicon), and the version without Java runtime.

### The GazePlay folder moves to Documents
- The data of GazePlay (profiles, settings, results, music, logs) are now in **`Documents\GazePlay_Learning`**
  (`~/Documents/GazePlay_Learning` on Linux and macOS), instead of `C:\Users\<name>\GazePlay` (`~/GazePlay`).
- **At the first start, the former folder is moved there automatically**, with all its content; the paths saved in the
  settings (files and videos folders...) follow. Nothing is deleted or overwritten. If the move is not possible (a file
  in use, no Documents folder...), GazePlay keeps using the former folder and tries again at the next start.
- The `Gazeplay` folder of Documents, where old versions wrote their edition file, is left as it is.
- Scripts or habits that read the results in `C:\Users\<name>\GazePlay\statistics` must look in
  `Documents\GazePlay_Learning\statistics`.

### Results files: format version 4
The results written by the games change on some points, to fix errors found in the recorded data. **The files
already recorded are not modified**, and the replay of the games still reads them.

- `replayData.json` now starts with `"formatVersion": 4`. The files without this key are version 1 (all the files
  written by the previous versions); versions 2 and 3 were only written by development builds of this version.
- Only the new measures of a Tobii eye tracker are recorded: the position was read every 10 ms and repeated when
  the eye tracker had sent nothing new (about 70 % of the gaze samples with an Eye Tracker 5). About 3 times fewer
  gaze samples, and gaze heatmap values lower in proportion; the durations of the fixations do not change.
- The heatmaps (`heatmap*.csv`, `metrics*.png`) always have 400 columns. They used to have 200 columns when the game
  had less than 1 GB of memory, for example on a computer with less than 4 GB (R7): the files of 200 columns are
  recognized by their number of columns.
- The `time` of the gaze samples, and the times and durations of the gaze fixations, are the times the eye tracker
  positions were read, no longer the times they were handled by the user interface: samples handled together while
  it was busy used to get the same, later, time. Each source stays in time order, but a mouse sample can now come
  just before a gaze sample of a slightly earlier time: sort or split the samples by `event` before computing
  intervals. The mouse samples are unchanged.
- `coordinatesAndTimeStamp` only holds the samples of its round. It used to also hold the samples of all the previous
  rounds of the same session.
- The mouse samples are the mouse movements only. Mouse clicks used to be recorded as additional samples.
- `fixationSequence`: `x` now holds the horizontal coordinate and `y` the vertical one (they were swapped), and the
  durations of the fixations are computed correctly (they used to be doubled or shifted to the next fixation, and the
  second sample was never merged into a fixation).
- New files `...-heatmapGaze.csv` and `...-heatmapMouse.csv`: heatmap of each source. `...-heatmap.csv` keeps both
  sources together, as before. The images `...-metricsGaze.png` and `...-metricsMouse.png` now show the heatmap of
  their own source (the three metrics images used to show the same mixed heatmap).
- The scanpath images take the last fixation into account.

**For researchers**: analysis scripts must look at `formatVersion` before using `fixationSequence` and must not
merge the rounds of a version 1 file twice. The historical files can be corrected offline from
`coordinatesAndTimeStamp` (see MODERNIZATION.md, "Format de résultats v2").

## Bug fixes
- *Where is the shape?* no longer fails to start (broken translation file).
- Results: every sample was recorded several times after *Replay* or *Continue*, and from the first round in *Simon*
  and *Gaze Race*; the recording also went on after the end of the game.
- Results: the `Time` column of `info-game.csv` showed the month instead of the minutes. In the files written before
  this version, the minutes can be read in the date and time at the beginning of the file name.
- Results: the results of a game could not be saved when the fixation sequence was disabled in the settings.
- Without eye tracker data, the gaze was seen at the center of the screen (phantom gaze).
- For the users of former versions of GazePlay Eval / BERA: the duration of the evaluation exported in seconds was 10
  times too large (CSV, Excel and screen). **The values exported by those versions must be divided by 10.**
- The settings saved before version 1.11.1 (eye tracker, folders, fixation length, music, theme...) are read again
  instead of being reset silently.
- The settings file is written atomically: a crash during the save no longer leaves an empty file.
- Areas of interest: the video of the game was never shown. The areas of the gaze and of the mouse are now computed
  separately (they were mixed), and the mouse no longer loses two vertical moves out of three; the screen of the areas
  shows the gaze or the mouse, with a button for each. It no longer fails when one of them has no position. The areas
  are not saved in any file: no recorded data changes.
- Profiles screen: with Java 25, it failed on Windows for a profile without picture; an empty music folder setting made
  GazePlay search the whole current folder for music.
- *Biboule Jump* and *Space Game*: the high score was not saved until statistics had been saved once for the game.
- *Pet House*: the hand followed the eye tracker directly instead of the gaze events.
- Gaze positions are now handed to the games in a single step per position, instead of one step per element of the
  game.
- Linux, macOS and ARM launch scripts: they work from any folder and with spaces in the path, the ARM version had no
  launch script at all, and they no longer make the whole installation writable by every user at each launch.
- The log files are written in the GazePlay folder instead of the current folder.
- Statistics screen: a new button opens the folder of the files of the game played (heatmaps...). The "see folder"
  button of the settings opened a wrong folder when the Windows profile had been renamed, did nothing on Linux and
  macOS, and was only shown by the BERA edition: it is now in the statistics settings.

## Removed
- **EyeTribe eye trackers** (no longer made) and the **Tobii 4C drivers for Linux**: only the Tobii Eye Tracker 5 on
  Windows is supported. A computer set to EyeTribe starts with the mouse.
- *Melordi* and *Picto Pick*: shipped but not reachable from the menu.
- The "videos folder" setting, unused since the video games were removed in 2022, and the hidden "multiple
  screenshots" option of the BERA edition.
- Unused images (44 MB in each download) and unused libraries (including Kafka, brought by an unused dependency) and dead code. The old Log4j 1.2 API used by
  the screen recorder is now redirected to the GazePlay logs.

## Technical changes
- Java runtime bundled with the distributions updated from Java 17.0.2 (January 2022) to **Java 25.0.4.1** (Azul
  Zulu, LTS supported until 2033) with JavaFX 25.0.4, checked by SHA-256 after download. **The version without Java
  runtime now needs Java 25 and JavaFX 21 or later** (Java 17 is no longer enough). The distributions with a runtime
  no longer ship a second, unused copy of JavaFX (10 MB).
- Spring Boot removed (it was only used to wire the application); start-up is plain Java.
- Gradle 9, dependencies updated (POI, Gson, Guava, org.json with security fixes, logging), versions gathered in
  `gradle/libs.versions.toml`.
- Tests: they no longer touch the real GazePlay folder, run without window, and run in the continuous integration on
  Java 25 and 26, with a real launch of the Windows, macOS and Linux distributions.

## Not verified
- Eye trackers: the Tobii Eye Tracker 5 was tested on Windows x64 (gaze followed in a game, results checked); no
  Linux test with hardware.
- Linux, macOS and ARM distributions: built, not launched on a real machine (the Linux one is launched by the
  continuous integration).
