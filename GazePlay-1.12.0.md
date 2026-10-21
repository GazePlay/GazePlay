# GazePlay 1.12.0 (draft)

Draft release notes of the modernization (branch `addClaudeCode`, October 2026). The version number is a proposal:
it is chosen by the maintainers at release time. Details, verifications and remaining work: [MODERNIZATION.md](MODERNIZATION.md).

## Important changes

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
  Starting GazePlay after a BERA or AFSR session opens GazePlay normally.
- Remaining versions: Windows x64, Linux x64, Linux ARM 64 bits, macOS (Intel and Apple
  Silicon), and the version without Java runtime.

### Results files: format version 2
The results written by the games change on some points, to fix errors found in the recorded data. **The files
already recorded are not modified**, and the replay of the games still reads them.

- `replayData.json` now starts with `"formatVersion": 2`. The files without this key are version 1 (all the files
  written by the previous versions).
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
- Areas of interest: the video of the game was never shown.
- *Biboule Jump* and *Space Game*: the high score was not saved until statistics had been saved once for the game.
- *Pet House*: the hand followed the eye tracker directly instead of the gaze events.
- EyeTribe eye trackers: the gaze positions were handled outside of the user interface thread, which could cause
  random errors. Gaze positions are now handed to the games in a single step per position, instead of one step per
  element of the game.
- Linux, macOS and ARM launch scripts: they work from any folder and with spaces in the path, the ARM version had no
  launch script at all, and they no longer make the whole installation writable by every user at each launch.
- The log files are written in `~/GazePlay` instead of the current folder.

## Removed
- *Melordi* and *Picto Pick*: shipped but not reachable from the menu.
- Unused images (44 MB in each download) and unused libraries (including Kafka, brought by an unused dependency) and dead code. The old Log4j 1.2 API used by
  the screen recorder is now redirected to the GazePlay logs.

## Technical changes
- Java runtime bundled with the distributions updated from Java 17.0.2 (January 2022) to **Java 21.0.12.1** (Azul
  Zulu, supported until 2031) with JavaFX 23.0.8, checked by SHA-256 after download. **The version without Java runtime
  now needs Java 21 and JavaFX 21 or later** (Java 17 is no longer enough).
- Spring Boot removed (it was only used to wire the application); start-up is plain Java.
- Gradle 9, dependencies updated (POI, Gson, Guava, org.json with security fixes, logging), versions gathered in
  `gradle/libs.versions.toml`.
- Tests: they no longer touch the real `~/GazePlay` folder, run without window, and run in the continuous integration on
  Java 21 and 25, with a real launch of the Windows, macOS and Linux distributions.

## Not verified
- Eye trackers (Tobii, EyeTribe): no hardware test was done.
- Linux, macOS and ARM distributions: built, not launched on a real machine (the Linux one is launched by the
  continuous integration).
