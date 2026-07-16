# 商店模式（Kiosk Mode）

商店模式（Kiosk Mode）是给 temi 应用的一种属性。

在当前版本的系统的 “设置 -> 主屏幕 -> 应用” 中，你可以将某个应用固定为 temi 的主屏应用，既每次返回主页时，都会拉起被选中的的主屏幕应用。

而声明了商店模式的 temi 应用在被选为主屏幕应用时，将会使更多的权限生效。包括可以使应用所声明的覆盖语音流功能，可以调用重启、关机的接口等等。

举例来说：当多个应用都声明了 商店模式 及 覆盖 ASR，仅有当前被选中作为主屏幕的应用可以实际覆盖系统 ASR。

<br>

## 概述

|返回值|方法|说明|
|-|-|-|
|void|[requestToBeKioskApp()](#requestToBeKioskApp)|请求成为当前被选中的商店模式技能|
|boolean|[isSelectedKioskApp()](#isSelectedKioskApp)|检查技能是否为当前被选中的商店模式技能|
|void|[setKioskModeOn(boolean on, HomeScreenMode mode)](#setKioskModeOn)|开启（关闭）商店模式|
|boolean|[isKioskModeOn](#isKioskModeOn)|检查商店模式是否开启|

<br>

## 配置

要将技能配置为Kiosk技能，你必须执行以下步骤：

1. 打开 `AndroidManifest.xml` 。

2. 在 **application** 标签下粘贴以下代码：

    ``` xml
    <!-- 如果已添加，请忽略 -->
    <meta-data
        android:name="com.robotemi.sdk.metadata.SKILL"
        android:value="@string/app_name" />

    <meta-data
        android:name="com.robotemi.sdk.metadata.KIOSK"
        android:value="true" />
    ```

3. 编译并运行你的技能。

4. 尽管现在你可看到这个技能跑起来了，但仍不能将它设置为Kisok技能，这时候前往到设置菜单。因为你的temi已经有一个Kiosk技能了，所以这时你应该可以在设置中看到一个名为“商店模式”的新选项。在这个技能被Launcher设置成默认技能之前，“商店模式”会一直保持关闭状态。

5. 在 *设置 > 应用 > 商店模式* 打开商店模式界面，然后在界面的右上角打开商店模式。

6. 从Kiosk mode配置可用的技能列表中选择你的技能。如果列表中只有一个可用技能，那么在商店模式打开时会自动为你选中它。

<br>

## 方法

### requestToBeKioskApp()

用这个方法来 **动态申请** 成为当前的商店模式技能，和请求权限类似，方法被调用后系统会弹出对话框，在用户点击“允许”按钮后便可成为当前的商店模式技能。

- **原型**

  ``` java
  void requestToBeKioskApp();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### isSelectedKioskApp()

检查技能是否为当前选中的商店模式技能。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示该技能是（不是）当前选择的商店模式技能|

- **原型**

  ``` java
  boolean isSelectedKioskApp();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setKioskModeOn()

开启（关闭）商店模式。也可以从 *设置 > 应用 > 商店模式* 中开启或关闭商店模式。如果传入的参数为 `true` ，则会和 [requestToBeKioskApp()](#requestToBeKioskApp) 方法的处理逻辑一样，动态申请成为选中的商店模式技能。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|传入true（false）打开（关闭）商店模式|
  |mode|HomeScreenMode|134 版本加入，当关闭 kiosk mode 时，可以指定返回的模式，默认为 HomeScreenMode.DEFAULT|

- **原型**

  ``` java
  void setKioskModeOn(boolean on, HomeScreenMode mode);
  ```

- **所需权限**

  设置, Kiosk

- **最小支持版本**

  0.10.77

---

### isKioskModeOn()

检查商店模式是否开启。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示商店模式已开启（关闭）|

- **原型**

  ``` java
  boolean isKioskModeOn();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77
