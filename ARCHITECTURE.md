# GazePlay architecture

Short map of the code, for developers. The history of the changes and the open decisions are in
[MODERNIZATION.md](MODERNIZATION.md).

## Modules

| Module | Content |
|---|---|
| `gazeplay-results` | Computations and files of the results, **without JavaFX** (the JavaFX plugin is not applied: an import of `javafx.*` does not compile): `HeatMapGrid` (heatmap and its CSV files), `FixationSequenceRecorder`, `ReplayDataWriter` (`replayData.json`), `ReplayDataFormat` (versions of the format), `AreaOfInterestGrouper` (areas of interest computed during a game) and `CoordinatesTracker`, `FixationPoint`. Same packages as before (`net.gazeplay.commons.utils...`), so no import changed. |
| `gazeplay-gaze` | Acquisition of the gaze: `GazeSample`, `GazeEvent`, `EyeTracker`, the eye trackers as `GazeSource` (`TobiiGazeSource` and its `PositionPollerRunnable`; EyeTribe was removed in October 2026) and its native libraries. Same packages as before (`net.gazeplay.commons.gaze...`). |
| `gazeplay-commons` | Configuration (`Configuration`, `ActiveConfigurationContext`), dispatch of the gaze samples to the nodes of the games (`gaze/devicemanager`: `AbstractGazeDeviceManager`, `GazeSourceDeviceManager`), recording of the results during a game (`utils/stats/Stats`, which delegates the computations to `gazeplay-results`) and their rendering (heatmap and scanpath images), translations, shared utilities. Test fixtures (`src/testFixtures`) shared by the other modules. |
| `gazeplay-games-commons` | Contract of the games: `GameSpec`, `GameSummary`, `IGameLauncher`, `GameLifeCycle`, variants, specialized `Stats`. |
| `gazeplay-core` | The application: launcher, composition root, menus, configuration screen, statistics screens, game catalog. |
| `gazeplay-games` | The games (one package per game, one `*SpecSource` per game entry) and GazePlay Eval (`gazeplayEval`). |
| `gazeplay-dist` | Launch scripts of the distributions. |

Removed in October 2026, unused: `gazeplay-melordi` and `gazeplay-picto-pick` (shipped but unreachable),
`gazeplay-side-projects` (experiments: Arduino, lightning simulator), `gazeplay-api` and `old-website`.

Versions of all libraries and plugins: `gradle/libs.versions.toml`.

## Startup

1. `GazePlayLauncher.main` finds the GazePlay folder (`Documents/GazePlay_Learning`, formerly `~/GazePlay`: found, and
   moved if needed, by `GazePlayDirectories.initGazePlayFolder` before anything is logged), then parses the command
   line with picocli (`MainCommand`). There are no product editions any more (AFSR and BERA removed; the dormant Eval
   code is not reachable).
2. `CompositionRoot` builds and wires the components (no dependency injection framework): translator, game locator,
   `GazePlay` (navigation between screens), gaze device and sound factories, game menu.
3. `GazePlayFxApp` (JavaFX `Application`) shows the profile selection, the menu, or directly a game (`--game`).
4. Each game started from the menu runs **in its own JVM** (`GameMenuController.createBuilder`), launched with
   `--user`/`--default-user`, `--game` and `--variant`.

## Games

The `*SpecSource` classes of `gazeplay-games` are listed at build time in `games-catalog.txt` and
`evals-catalog.txt` (tasks `generateGamesCatalog`, `generateEvalsCatalog` of `gazeplay-core`) and loaded by
`CatalogBasedGamesLocator`. A game provides a `GameSpecSource` (summary, variants, launcher), an `IGameLauncher`
(creates its `Stats` and its `GameLifeCycle`) and runs in the `GameContext` given by the application.

## Gaze

An eye tracker is a `GazeSource` (`TobiiGazeSource`, `GazeSource.NONE` for the mouse) that
gives `GazeSample`s (acquisition time, screen position, source) from any thread. `TobiiGazeSource` polls the Tobii
wrapper every 10 ms (`PositionPollerRunnable`, which ignores the value (0.5, 0.5) returned without gaze data).
`GazeSourceDeviceManager` hands the samples to `AbstractGazeDeviceManager.onGazeSample`, which reaches the JavaFX
thread once per sample and fires the `GazeEvent`s (`GAZE_ENTERED`, `GAZE_MOVED`, `GAZE_EXITED`) directly on a copy of
the registered nodes, then on the root of the game (statistics); gaze and mouse have separate states, `GAZE_MOVED`
carries the time the gaze entered the node. The behaviour is pinned by `GazeDispatchCharacterizationTest`.

## Results

`Stats` records the gaze and mouse samples with event filters on the root of the game scene, and writes, for each
round, in `Documents/GazePlay_Learning/statistics/<game>/<date>/` (or the profile folder): `replayData.json` (raw samples
`coordinatesAndTimeStamp`, fixation sequence), `heatmap.csv` (mouse and gaze), `heatmapGaze.csv`, `heatmapMouse.csv`,
`metrics*.png`, screenshot and `info-game.csv`. The heatmaps always have 400 columns (`Stats.HEAT_MAP_COLUMNS`).
These are persistent formats used by researchers. `replayData.json` holds its version in `formatVersion` (absent in
the files of version 1, 4 since October 2026); the files are never rewritten, and `ReplayDataFormat` documents the
differences between the versions and converts the fixation sequence when the replay reads a file. The gaze samples
are dated when they are read from the eye tracker (`GazeSample.time`, carried by `GazeEvent.getSampleTime()`). The behaviour is pinned by
`StatsRecordingCharacterizationTest` and `ReplayDataWriterTest`; the history is in MODERNIZATION.md.

When the areas of interest are enabled, `Stats` also computes them during the game, for each source separately
(`AreaOfInterestRecording`, one for the gaze and one for the mouse, grouping with `AreaOfInterestGrouper`). The
`AreaOfInterest` screen shows one source at a time; nothing is written to disk.

GazePlay Eval writes its exports (CSV in UTF-16, Excel) through `EvalReport`, tested by `EvalReportTest`.

## Configuration

`GazePlay.properties` (default profile in the GazePlay folder, one file per profile in its `profiles/<name>`) is read
by `ConfigurationSource` (old keys renamed in 1.11.1 are migrated by `LegacyConfigurationKeys`), held by
`ApplicationConfig` and saved by `Configuration` after each change, atomically. The keys are a persistent format.

## Tests

See [BUILD.MD](BUILD.MD): hermetic (`build/test-home`), without window (Monocle), plus a launch smoke test.
