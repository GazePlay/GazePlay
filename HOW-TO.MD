# Frequently Asked Questions

In these instructions, actions performed in the Windows command line are prefixed with `>`, and those on the Unix
shell are prefixed with `$`. Select the command appropriate for your platform.

## Issues with DLL dependencies

See this [example issue](https://github.com/schwabdidier/GazePlay/issues/161#issuecomment-362115364)

In order to be able to run GazePlay in your IDE, 
without having to mess around your global PATH env variable for the Windows user, just configure the run configuration of your IDE, in order to add the dll directory to the ```Path``` env variable.

The value to set would be
```
C:\Users\<username>\AppData\Local\TobiiStreamEngineForJava\lib\tobii\x64
```
If your Windows username is `yann` , change `<username>` to `yann`

Here is a screenshot showing how to do it with Intellij

![intellij_run_configuration_screenshot](https://user-images.githubusercontent.com/1653590/35654449-7125e424-06ed-11e8-847e-3f3a5c5169a9.png)

## Git Branch Issues
See this [example issue](https://github.com/schwabdidier/GazePlay/pull/207#issuecomment-363957589)

I guess that you branched from an previous PR instead of branching from ```Gazeplay:master```

Here are the basic step when you want to work on a new Pull Request: 

Assuming you have a remote repository named ```origin``` for ```https://github.com/WaffleBuffer/GazePlay.git``` that were added when you cloned your own github fork

Assuming you have a remote repository named ```upstream``` for ```https://github.com/GazePlay/GazePlay.git``` that you added with 
```
git remote add upstream https://github.com/GazePlay/GazePlay.git
```

Go to your master branch of your local repository
```
git checkout master
```

Make sure you get the latest version from master branch of the upstream repository
```
git fetch upstream
```

at this point, you should make sure your working copy is clean, that there is no local change
```
git status
```

If you have a local change that is not important, you will need to discard it
```
git reset --hard upstream/master
git clean -d -f
```

If you don't have any local change, you can get remote change to your master branch
```
git rebase upstream/master
```
or simply 
```
git pull upstream master
```

at this point, you should now have a clean working copy.
```
git status
```

It's good to update your own repository's ```master``` branch up-to-date, or, at least, don't let it contain any commit that is not in the original (```upstream```) repository.
```
git push origin/master
```
If this fails, this means you have already pushed non merged changes to your ```master``` branch. As you don't want that, just force push in order to reset your remote ```master``` branch.
```
git push --force origin/master
```

Hooray, now you have a clean state, in order to start a new change.

Now, you know you want to make some change.
If you know what you will be working on, then just pick a new branch name that describe your next work, like ```FixIssueXYZ``` if you plan to work on fixing issue XYZ.
If you don't know what you are going to do, just branch anyways, with any other name of your choice, like ```ImproveCSS``` or ```RefactorThisThing```

Now you can edit any file, and make any change you like.
Just be careful not to change too many things. This may indicate you are working on multiple issues at the same time, so you will need multiple Pull Request for them, so multiple branches.
For simplicity, just try to make the minimal change that works.

When you find any change is not strictly required to fix the issue you're working on, then discard this particular change (or commit it to another branch). Use your IDE to compare files, and cancel any line change that is not needed.

When you're happy with your change, then build the code with Gradle, generally it will be
```
> gradlew
$ ./gradlew
```

Then test, test a lot.
then type 
```
git status
```
to check all of the file you changed.

Review the diff, by using ``` git diff ``` or by using your IDE to make the diff. 
The diff should again only contain the changes you were intending.

If you're happy with the change(s), then add the modified files to the git staging.
```git add -u``` or ```git add .```

Build again using Gradle
```
> gradlew
$ ./gradlew
```

then check ```git status``` if maven reformatted any code.
If so, add the modified files again with ```git add -u```

Now you're ready to make a commit
```
git commit -m "improve ... in order to ..."
```

You can add multiple commits if you need to.

When finished, push your branch to github
```
git push origin FixIssueXYZ
```

Then go to ```github.com``` and review you branch ("compare")

You can create a new PR. 

You can review again the content of the change. 
Check the PR only contains the changes related to the issue you want to fix.
