# 安装与卸载

你可以从为你开发用的计算机上下载安装ADB（Android Debug Bridge）开始。请按照这个[教程](https://www.xda-developers.com/install-adb-windows-macos-linux/)了解如何在你的计算机上下载和配置ADB。

## 让计算机连接机器人

在你的计算机上配置好ADB之后，你可以通过以下步骤在temi上运行你的代码：

1. 确保你的计算机和机器人连上了同一个Wi-Fi网络。

2. 在temi上打开设置 -> temi开发者选项 -> 点击打开ADB调试端口，并且注意右上角的IP地址。

3. 在你的计算机上，利用上一步的IP地址，可以让计算机通过ADB连接到机器人并测试你的应用，在有ADB环境的终端下输入以下命令并按下回车：

    ``` shell
    adb connect IP_ADDRESS:5555
    ```
    
    如果连接成功会显示 `connected to IP_ADDRESS:5555`

## 安装应用

按上述步骤成功连接机器人后，你可以在终端输入以下命令安装你的应用：
``` shell
adb install [option] PATH_OF_APK
```

## 卸载应用

按上述步骤成功连接机器人后，你可以在终端输入以下命令卸载你的应用：

``` shell
adb uninstall [option] PACKAGE_NAME
```

如果不能确定包名是什么，你可以在你的Android工程中查看。

更多ADB相关内容请参考[Android开发者官网](https://developer.android.com/studio/command-line/adb)。
