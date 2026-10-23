# Starts GazePlay from the Windows distribution with its launch script, as a user would, and checks that after
# $Delay seconds it is still running, has loaded its game catalog and has logged no unexpected error.
#
# Usage: launch-distribution.ps1 -Launcher <path of gazeplay-learning-windows.bat> -GazeHome <folder> [-Arguments "..."]
param(
    [Parameter(Mandatory = $true)] [string] $Launcher,
    [Parameter(Mandatory = $true)] [string] $GazeHome,
    [string] $Arguments = "",
    [int] $Delay = 40
)

$ErrorActionPreference = "Stop"

if (Test-Path $GazeHome) { Remove-Item -Recurse -Force $GazeHome }
New-Item -ItemType Directory -Force (Join-Path $GazeHome "GazePlay") | Out-Null
# Mouse mode: there is no eye tracker on the CI machines
Set-Content -Path (Join-Path $GazeHome "GazePlay\GazePlay.properties") -Value "EYE_TRACKER=mouse_control" -Encoding ascii
$console = Join-Path $GazeHome "console.log"
$errors = Join-Path $GazeHome "console-errors.log"
$noInput = Join-Path $GazeHome "no-input.txt"
New-Item -ItemType File -Force $noInput | Out-Null
$log = Join-Path $GazeHome "GazePlay\gazeplay.log"

# The JVM reads JAVA_TOOL_OPTIONS whatever the launch script: the home folder of the test, not the one of the runner,
# and its GazePlay folder (otherwise in the Documents folder of the runner, which does not depend on user.home)
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$GazeHome -Dgazeplay.folder=$(Join-Path $GazeHome 'GazePlay')"
# Started from another folder than bin/; the script ends with "pause", which an empty input ends at once
$process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "`"$Launcher`" $Arguments" `
    -WorkingDirectory $env:RUNNER_TEMP -RedirectStandardInput $noInput `
    -RedirectStandardOutput $console -RedirectStandardError $errors -PassThru -WindowStyle Hidden
Start-Sleep -Seconds $Delay
Remove-Item Env:\JAVA_TOOL_OPTIONS

# The JVM of GazePlay, not the Gradle daemon nor another Java process of the runner
$jvm = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
    Where-Object { $_.CommandLine -match "GazePlayLauncher" }

Write-Host "----- console -----"
Get-Content $console, $errors -ErrorAction SilentlyContinue | Select-Object -Last 40
Write-Host "----- $log -----"
if (Test-Path $log) { Get-Content $log | Select-Object -Last 60 } else { Write-Host "No log in $log" }
Write-Host "-----"

if (-not $jvm) {
    Write-Host "GazePlay stopped"
    exit 1
}
$jvm | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
if (-not $process.HasExited) { Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue }

if (-not (Select-String -Path $log -Pattern "Games found : " -Quiet)) {
    Write-Host "The game catalog was not loaded"
    exit 1
}
$unexpected = Get-Content $console, $errors, $log -ErrorAction SilentlyContinue |
    Select-String -Pattern "Exception| ERROR " |
    Where-Object { $_.Line -notmatch "NoSuchFileException|at java.base/" }
if ($unexpected) {
    $unexpected | ForEach-Object { Write-Host $_.Line }
    Write-Host "Unexpected errors"
    exit 1
}
Write-Host "GazePlay was running without error after $Delay s: $Arguments"
# Explicit: the caller checks $LASTEXITCODE, which a script ending without exit leaves unchanged
exit 0
