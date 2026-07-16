# Installing & Uninstalling

You can begin by downloading ADB (Android Debug Bridge) on the computer you wish to develop for temi. Please follow [this](https://www.xda-developers.com/install-adb-windows-macos-linux/) tutorial on how to download and set up ADB on your computer.

## Connect Computer to Robot

Once you have ADB set up on your computer, you can run your code on temi by:

**Step 1**: Make sure you are connected to the same WiFi network as your robot.

**Step 2**: On temi - go to Settings -> temi Developer Tools -> tap on ADB Port Opening.

**Step 3**: On computer - Using the IP address on the top right of temi’s screen you can connect to the robot and test your code. In order to establish a connection with the robot, type following command in Terminal on Mac or Command Prompt on Windows.

``` shell
adb connect <IP_ADDRESS>:5555
```

## Installing Applications

Once you have established a connection between your computer and temi, you can install your app using two methods:

1. Directly through Android Studio by selecting the "rockchip rk****" and selecting Run.
2. By typing the following in the command line:

    ``` shell
    adb install [option] PATH_OF_APK
    ```

## Uninstalling Applications

Once you have established a connection between your computer and temi, you can uninstall your app on temi by typing following command in the terminal:

``` shell
adb uninstall [option] PACKAGE_NAME
```

If you are not sure what your package name is, you can check from within your Android Project.

More ADB related content please refer to [Android Developers](https://developer.android.com/studio/command-line/adb).
