# GazePlay [![build status](https://secure.travis-ci.org/GazePlay/GazePlay.png)](http://travis-ci.org/GazePlay/GazePlay)<!-- [![codecov](https://codecov.io/gh/schwabdidier/GazePlay/branch/master/graph/badge.svg)](https://codecov.io/gh/schwabdidier/GazePlay)-->
All information about GazePlay can be found at [gazeplay.net](http://gazeplay.net). Download the latest version of the
game in the [releases](https://github.com/GazePlay/GazePlay/releases) tab.

## Included Games
See [here](Games-eng.md) for a list of games included in the software.

## Build from Source
You can build the project from source code by cloning this repository and following the [BUILD](BUILD.MD) instructions.

## Contribute
Please refer to the [Contribution Guidelines](CONTRIBUTING.MD).

## Update
Last updated on 02/20/2025<br>
JRE -> Java 17 (Azul Zulu), supported until 2029<br>
JavaFx -> 21, supported until (at least) 2028<br>
Gradle -> 8

## Maintenance
Check the workflow "project-check-reminder.yml", it is a workflow which allows us to recall all February 1 and August 1 of each year by creating an outcome to check the state of our dependencies, Gradle, JDK and Javafx.<br>
You can find the file in .github/workflow/project-check-reminder.yml

## Release
To make a release, this happens manually in the Actions tab on the Restity Github of the project.<br>
Just throw it and choose the number to modify for the release (major, minor or patch)
