# 系统设置相关

## API 概览

|返回值|方法|说明|
|-|-|-|
|String|[getSerialNumber()](#getSerialNumber)|获取序列号|
|[BatteryData](#batteryData)|[getBatteryData()](#getBatteryData)|获取电池数据 |
|void|[showTopBar()](#showTopBar)|显示顶部栏 |
|void|[hideTopBar()](#hideTopBar)|隐藏顶部栏 |
|void|[showAppList()](#showAppList)|跳转至 temi 应用列表|
|boolean|[isHardButtonsDiabled()](#isHardButtonsDisabled)|获取硬件按钮是否被禁用 |
|void|[setHardButtonsDisabled(boolean disable)](#setHardButtonsDisabled)|禁用（启用）硬件按钮|
|boolean|[getPrivacyMode()](#getPrivacyMode)|查看隐私模式是否开启|
|void|[setPrivacyMode(boolean on)](#setPrivacyMode)|开启（关闭）隐私模式|
|String|[getLauncherVersion()](#getLauncherVersion)|获取Launcher版本|
|String|[getRoboxVersion()](#getRoboxVersion)|获取Robox版本|
|void|[toggleWakeup(boolean disabled)](#toggleWakeup)|禁用（启用）唤醒|
|boolean|[isWakeupDisabled()](#isWakeupDisabled)|查看唤醒是否被禁用|
|void|[toggleNavigationBillboard(boolean disabled)](#toggleNavigationBillboard)|禁用（启用）go to 显示的大字|
|boolean|[isNavigationBillboardDisabled()](#isNavigationBillboardDisabled)|查看 go to 时是否会显示大字|
|void|[setTopBadgeEnabled(boolean enabled)](#setTopBadgeEnabled)|启用（禁用）顶部状态徽章|
|boolean|[isTopBadgeEnabled()](#isTopBadgeEnabled)|查看顶部状态徽章是否可用|
|void|[setAutoReturnOn(boolean on)](#setAutoReturnOn)|打开（关闭）自主返回|
|boolean|[isAutoReturnOn()](#isAutoReturnOn)|查看自主返回是否已打开|
|void|[setVolume(int volume)](#setVolume)|设置系统音量|
|int|[getVolume()](#getVolume)|获取当前系统音量|
|void|[setNavigationSafety(SafetyLevel level)](#setNavigationSafety)|设置导航避障灵敏度等级|
|[SafetyLevel](#safetyLevel)|[getNavigationSafety()](#getNavigationSafety)|获取当前导航避障灵敏度等级|
|void|[setGoToSpeed(SpeedLevel level)](#setgotospeed)|设置 go to 速度等级|
|[SpeedLevel](#speedLevel)|[getGoToSpeed()](#getgotospeed)|获取 go to 速度等级|
|int|[setFollowSpeed(SpeedLevel level)](#setfollowspeed)|设置跟随速度等级|
|[SpeedLevel](#speedLevel)|[getFollowSpeed()](#getfollowspeed)|获取跟随速度等级|
|int|[setMinimumObstacleDistance()](#setMinimumObstacleDistance)|设置最小障碍距离|
|int|[minimumObstacleDistance()](#minimumObstacleDistance)|获取最小障碍距离|
|void|[restart()](#restart)|重启 temi|
|void|[startPage(Page page)](#startPage)|启动系统界面|
|void|[setLocked(boolean locked)](#setLocked)|开启（关闭）密码保护|
|boolean|[isLocked()](#isLocked)|密码保护是否已开启|
|void|[shutdown](#shutdown)|关机|
|void|[setSoundMode(SoundMode soundMode)](#setSoundMode)|设置声音模式|
|void|[setHardButtonMode(HardButton type, HardButton.Mode mode)](#setHardButtonMode)|设置硬件按钮模式|
|[HardButton.Mode](#hardButtonMode)|[getHardButtonMode(HardButton type)](#getHardButtonMode)|获取硬件按钮模式|
|[HardButton.Status](#hardButtonStatus)|[getButtonStatus(HardButton type)](#getbuttonstatus)|获取硬件按钮状态|
|String|[getNickName()](#getNickName)|获取 temi 的名称|
|void|[setMode(Mode mode)](#setMode)|设置系统模式|
|[Mode](#mode)|[getMode()](#getMode)|获取系统模式|
|Map\<String, Boolean\>|[getSupportedLatinKeyboards()](#getSupportedLatinKeyboards)|获取所有支持的AOSP 拉丁键盘|
|void|[enabledLatinKeyboards(List\<String\>)](#enabledLatinKeyboards)|启用指定的拉丁键盘|
|void|[setGroundDepthCliffDetectionEnabled(boolean enabled)](#setGroundDepthCliffDetectionEnabled)|开启（关闭）地面深度传感器|
|boolean|[isGroundDepthCliffDetectionEnabled()](#isGroundDepthCliffDetectionEnabled)|查看地面深度传感器是否开启|
|boolean|[hasCliffSensor()](#hasCliffSensor)|是否有悬崖传感器|
|void|[setCliffSensorMode(CliffSensorMode cliffSensorMode)](#setCliffSensorMode)|设置悬崖传感器模式|
|[CliffSensorMode](#cliffSensorMode)|[getCliffSensorMode()](#getCliffSensorMode)|获取悬崖传感器模式|
|void|[setHeadDepthSensitivity(SensitivityLevel sensitivityLevel)](#setHeadDepthSensitivity)|设置头部深度传感器灵敏度级别|
|[SensitivityLevel](#sensitivityLevel)|[getHeadDepthSensitivity()](#getHeadDepthSensitivity)|获取头部深度传感器灵敏度级别|
|void|[setFrontTOFEnabled(boolean enabled)](#setFrontTOFEnabled)|开启（关闭）前部 TOF 传感器|
|boolean|[isFrontTOFEnabled()](#isFrontTOFEnabled)|查看前部 TOF 传感器是否可用|
|void|[setBackTOFEnabled(boolean enabled)](#setBackTOFEnabled)|开启（关闭）背部 TOF 传感器|
|boolean|[isBackTOFEnabled()](#isBackTOFEnabled)|查看背部 TOF 传感器是否可用|
|boolean|[isStandByOn()](#isStandByOn)|查询待机状态|
|int|[startStandBy()](#startStandBy)|进入待机状态|
|int|[stopStandBy()](#stopStandBy)|退出待机状态|
|int|[enableStandBy(Boolean enabled, String password)](#enableStandBy)|开关待机模式|
|[HomeScreenMode]($homescreenmode)|[getHomeScreenMode()](#gethomescreenmode)|获得当前主屏幕模式|

|接口|说明|
|-|-|
|[OnBatteryStatusChangedListener](#OnBatteryStatusChangedListener)|电池状态变化监听器|
|[OnPrivacyModeChangedListener](#OnPrivacyModeChangedListener)|隐私模式状态变化监听器|
|[OnSdkExceptionListener](#onSdkExceptionListener)|temi SDK 错误信息回调|
|[OnDisabledFeatureListUpdatedListener](#onDisabledFeatureListUpdatedListener)|不可用系统功能列表更新监听器|
|[OnButtonStatusChangedListener](#onButtonStatusChangedListener)|硬件按钮状态监听器|
|[OnButtonModeChangedListener](#onButtonModeChangedListener)|硬件按钮模式监听器|

|模型|说明|
|-|-|
|[BatteryData](#batteryData)|电池数据|
|[SafetyLevel](#safetyLevel)|导航避障灵敏度等级|
|[SpeedLevel](#speedLevel)|go to 速度等级|
|[Page](#page)|系统内置界面|
|[SoundMode](#soundMode)|声音模式|
|[HardButton.Mode](#hardButtonMode)|硬件按钮模式|
|[HardButton.Status](#hardButtonStatus)|硬件按钮状态|
|[Mode](#mode)|系统模式|
|[CliffSensorMode](#cliffSensorMode)|悬崖传感器模式|
|[SensitivityLevel](#sensitivityLevel)|灵敏度级别|
|[HomeScreenMode](#homescreenmode)|主屏幕模式|

<br>

## 方法

### getSerialNumber()

用这个方法来获取 temi 的序列号。如果需要用一个唯一编号来标识你的temi，那么可以使用这个方法。

- **返回值**

  |类型|说明|
  |-|-|
  |String|temi 的序列号|

- **原型**

  ``` java
  String getSerialNumber();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.43

---

### getBatteryData()

用这个方法可以请求获取temi的电池相关信息，信息包括剩余电量的百分比以及充电状态。

- **返回值**

  |类型|说明|
  |-|-|
  |[BatteryData](#batteryData)|电池数据|

- **原型**

  ``` java
  BatteryData getBatteryData();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.43

---

### showTopBar()

用这个方法来显示 Launcher 的顶部栏。

- **原型**

  ``` java
  void showTopBar();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.43

---

### hideTopBar()

用这个方法可以隐藏 Launcher 的顶部栏。

- **原型**

  ``` java
  void hideTopBar();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.43

---

### showAppList()

用这个方法来跳转到 temi 的应用列表。

- **原型**

  ``` java
  void showAppList();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### getPrivacyMode()

通过这个方法来获取隐私模式当前是否开启。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|是否已开启隐私模式，true 表示隐私模式已开启，false 表示已关闭|

- **原型**

  ``` java
  boolean getPrivacyMode();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.53

---

### setPrivacyMode()

通过这个方法来开启或关闭隐私模式。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|开启或关闭隐私模式，true 表示开启隐私模式，false 表示关闭|

- **原型**

  ``` java
  void setPrivacyMode(boolean on);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.53

---

### isHardButtonsDisabled()

通过这个方法来获取硬件按钮当前是否被禁用。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|是否已禁用硬件按钮，true 表示硬件按钮已禁用，false 表示未禁用|

- **原型**

  ``` java
  boolean isHardButtonsDisabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.6

---

### setHardButtonsDisabled()

通过这个方法来禁用或启用硬件按钮。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |disable|boolean|禁用或启用硬件按钮，**true** 表示禁用，**false** 表示启用|

- **原型**

  ``` java
  void setHardButtonsDisabled(boolean disable);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.6

---

### getHardButtonMode()

通过这个方法来获取指定硬件按钮当前的模式。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |type|HardButton|硬件按钮类型，参数范围 [HardButton.MAIN], [HardButton.POWER], [HardButton.VOLUME]|

- **返回值**

  |类型|说明|
  |-|-|
  |[HardButton.Mode](#hardButtonMode)|指定硬件按钮的模式|

- **原型**

  ``` java
  HardButton.Mode getHardButtonMode();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77

---

### setHardButtonMode()

通过这个方法来设置指定硬件按钮的模式。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |type|[HardButton](#hardButton)|硬件按钮类型|
  |mode|[HardButton.Mode](#hardButtonMode)|模式（启用、禁用、拦截跟随）|

- **原型**

  ``` java
  void setHardButtonMode(HardButton type, HardButton.Mode mode);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.77

---

### getHardButtonStatus()

通过这个方法来获取指定硬件按钮当前的状态。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |type|HardButton|硬件按钮类型，参数范围 [HardButton.EMERGENCY_STOP]|

- **返回值**

  |类型|说明|
  |-|-|
  |[HardButton.Status](#hardButtonStatus)|指定硬件按钮的状态|

- **原型**

  ``` java
  HardButton.Status getHardButtonStatus();
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.134.0

---

### getLauncherVersion()

用这个方法来获取 Launcher 的版本。

- **返回值**

  |类型|说明|
  |-|-|
  |String|Launcher 的版本|

- **原型**

  ``` java
  String getLauncherVersion();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.65

---

### getRoboxVersion()

用这个方法来获取 Robox 的版本。

- **返回值**

  |类型|说明|
  |-|-|
  |String|Robox 的版本|

- **原型**

  ``` java
  String getRoboxVersion();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.65

---

### toggleWakeup()

在Kiosk模式下你可以根据自己的喜好选择开启或者禁用唤醒。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |disabled|boolean|true禁用唤醒，false启用。|

- **原型**

  ``` java
  void toggleWakeup(boolean disabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.44

---

### isWakeupDisabled()

查看唤醒是否被禁用。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true: 禁用，false：启用|

- **原型**

  ``` java
  boolean isWakeupDisabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### toggleNavigationBillboard()

在Kiosk模式下，你可以选择在执行goTo命令时切换大字提示（正在前往xxx地点）的可见性。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |disabled|boolean|传入true（false）隐藏（显示）大字|

- **原型**

  ``` java
  void toggleNavigationBillboard(boolean disabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.44

---

### isNavigationBillboardDisabled()

查看 go to 导航时大字是否可见。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示大字不可见（可见）|

- **原型**

  ``` java
  boolean isNavigationBillboardDisabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setTopBadgeEnabled()

启用（禁用）位于顶部绿色徽章。启用后，当进行跟随、导航等时会将运动状态实时显示在顶部徽章中。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|boolean|传入true（false）启用（禁用）顶部徽章|

- **原型**

  ``` java
  void setTopBadgeEnabled(boolean enabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### isTopBadgeEnabled()

查看顶部徽章是否可用，默认为可用。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示顶部徽章可用（不可用）|

- **原型**

  ``` java
  boolean isTopBadgeEnabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setAutoReturnOn()

开启（关闭）自主返回功能。开启后，在系统进入无交互状态一定时间后（可配置）返回指定地点（可配置），配置方法为 *设置 > 通用设定 > 自主返回 > 时间间隔或地点名称*。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|传入true（false）打开（关闭）自主返回|

- **原型**

  ``` java
  void setAutoReturnOn(boolean on);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### isAutoReturnOn()

查看自主返回是否已打开，默认为关闭。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示自主返回已打开（关闭）|

- **原型**

  ``` java
  boolean isAutoReturnOn();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setVolume()

设置当前系统的音量，取值范围为 0 ~ 10。在系统中可通过触摸顶部的 “+”、“-” 按钮来调节音量，或者通过从屏幕底部边缘向上滑动弹出音量、亮度调节滑块，通过左右滑动音量调节滑块来调节音量。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |volume|int|音量|
  |showDrawer|boolean|是否显示音量抽屉, 1.137.1 版本加入, 默认为 false|

- **原型**

  ``` java
  void setVolume(int volume, boolean showDrawer);
  ```

- **所需权限**

  设置

- **最小支持版本**

  0.10.70

---

### getVolume()

获取当前系统音量，范围为 0 ~ 10。

- **返回值**

  |类型|说明|
  |-|-|
  |int|当前系统音量|

- **原型**

  ``` java
  int getVolume();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setNavigationSafety()

设置导航时避障的灵敏度等级。系统中可在 *设置 > 导航 > 避障灵敏度* 中设置。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |level|[SafetyLevel](#safetyLevel)|避障灵敏度等级|

- **原型**

  ``` java
  void setNavigationSafety(SafetyLevel level);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### getNavigationSafety()

获取当前导航避障灵敏度等级。

- **返回值**

  |类型|说明|
  |-|-|
  |[SafetyLevel](#safetyLevel)|当前避障灵敏度等级|

- **原型**

  ``` java
  SafetyLevel getNavigationSafety();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setGoToSpeed()

设置 go to 时的速度等级。系统中可在 *设置 > 导航 > 地点导航设置 > 导航速度控制* 中设置。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |level|[SpeedLevel](#speedLevel)|go to 速度等级|

- **原型**

  ``` java
  void setGoToSpeed(SpeedLevel level);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### getGoToSpeed()

获取当前 go to 的速度等级。

- **返回值**

  |类型|说明|
  |-|-|
  |[SpeedLevel](#speedLevel)|当前 go to 速度等级|

- **原型**

  ``` java
  SpeedLevel getGoToSpeed();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setFollowSpeed()

设置跟随时的速度等级。

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 当前 Launcher 不支持该操作 <br>200 成功 <br>400 无效参数 <br>403 需要 SETTINGS 权限|

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |level|[SpeedLevel](#speedLevel)|跟随速度等级|

- **原型**

  ``` java
  void setGoToSpeed(SpeedLevel level);
  ```

- **所需权限**

  设置

- **最小支持版本**

  1.135.1

---

### getFollowSpeed()

获取当前跟随的速度等级。

- **返回值**

  |类型|说明|
  |-|-|
  |[SpeedLevel](#speedLevel)|当前跟随速度等级|

- **原型**

  ``` java
  SpeedLevel getFollowSpeed();
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.135.1

### setMinimumObstacleDistance() <a name="setMinimumObstacleDistance" />

设置最小障碍距离，当你的机器人因一些配件而宽度增加时，这个参数可以增大其导航时的安全距离。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |value|int|0-100，以 5 递增，单位：厘米|

- **返回值**

  如果成功，将返回传入的值
  
  返回 0 可能代表配置失败

- **原型**

  ``` java
  int setMinimumObstacleDistance(int value);
  ```

- **所需权限**

  Settings

- **最小支持版本**

  1.131.4

---

### minimumObstacleDistance() <a name="minimumObstacleDistance" />

获取最小障碍距离

- **返回值**

  |类型|说明|
  |-|-|
  |int|距离的值，单位：厘米。如果出现错误，则返回 400 或 403，代表参数错误，或缺少权限|

- **原型**

  ``` java
  int minimumObstacleDistance();
  ```

- **所需权限**

  无.

- **最小支持版本**

  1.131.4

### restart()

用这个方法来重启 temi。

- **原型**

  ``` java
  void restart();
  ```

- **所需权限**

  作为选中的Kiosk

- **最小支持版本**

  0.10.72

---

### startPage()

用这个方法来启动系统的内置界面。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |page|[Page](#page)|系统界面|

- **原型**

  ``` java
  void startPage(Page page);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.72

---

### setLocked()

用这个方法来开启（关闭）密码保护。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |locked|boolean|传入 true (false) 来开启（关闭）密码保护|

- **原型**

  ``` java
  void setLocked(boolean);
  ```

- **所需权限**

  设置

- **最小支持版本**

  0.10.74

---

### isLocked()

用这个方法来获取当前是否已开启密码保护。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true (false) 表示密码保护已开启（关闭）|

- **原型**

  ``` java
  boolean isLocked();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.74

---

### shutdown()

用这个方法来关机。

- **原型**

  ``` java
  void shutdown();
  ```

- **所需权限**

  作为选中的Kiosk

- **最小支持版本**

  0.10.77

---

### setSoundMode()

用这个方法来设置声音模式（常规、视频通话模式），可优化在视频通话场景下的收音、音响的体验。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |soundMode|[SoundMode](#soundMode)|声音模式|

- **原型**

  ``` java
  void setSoundMode(SoundMode soundMode);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.77

- **注意**

  为提高用户体验，如果在开始自己开发的视频通话应用时将声音模式设置为视频通话模式，建议在结束视频通话后，将声音模式切回成常规模式（[NORMAL](#soundMode)）。

---

### getNickName()

用这个方法来获取 temi 的名称。

- **返回值**

  |类型|说明|
  |-|-|
  |String|temi 的用户名|

- **原型**

  ``` java
  String getNickName();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77

---

### setMode()

用这个方法来设置 temi 的系统模式（默认、迎宾、隐私模式）。也可在 *设置 > 模式* 手动切换。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mode|[Mode](#mode)|系统模式|

- **原型**

  ``` java
  void setMode(Mode mode);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.77

---

### getMode()

用这个方法来获取 temi 的系统模式。

- **返回值**

  |类型|说明|
  |-|-|
  |[Mode](#mode)|temi 的系统模式|

- **原型**

  ``` java
  Mode getMode();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77

---

### getSupportedLatinKeyboards()

用这个方法来获取所有支持的拉丁键盘，以及其启用状态。

- **返回值**

  |类型|说明|
  |-|-|
  |Map\<String, Boolean\>|具体的语言以及对应的启用状态|

- **原型**

  ``` java
  Map<String, Boolean> getSupportedLatinKeyboards();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77

---

### enabledLatinKeyboards()

用这个方法来启用所需要的拉丁键盘。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |keyboards|List\<String\>|要启用的拉丁键盘列表，列表第一个元素将会被设置当前选中的键盘。**通过 [getSupportedLatinKeyboards()](#getSupportedLatinKeyboards) 获取所有支持的键盘**。|

- **原型**

  ``` java
  void enabledLatinKeyboards(List<String> keyboards);
  ```

- **所需权限**

  设置

- **最小支持版本**

  0.10.77

---

### setGroundDepthCliffDetectionEnabled()

用这个方法来开启或关闭地面深度传感器，也可在 *设置 > 导航 > 传感器设置* 中操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|boolean|传入 true（false）开启（关闭）地面深度传感器|

- **原型**

  ``` java
  void setGroundDepthCliffDetectionEnabled(boolean enabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.78

---

### isGroundDepthCliffDetectionEnabled()

用这个方法来查看地面深度传感器是否开启，也可在 *设置 > 导航 > 传感器设置* 中查看。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示深度传感器开启（关闭）|

- **原型**

  ``` java
  boolean isGroundDepthCliffDetectionEnabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78

---

### hasCliffSensor()

用这个方法来查看当前机器是否有悬崖传感器。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示机器有（没有）悬崖传感器|

- **原型**

  ``` java
  boolean hasCliffSensor();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78

---

### setCliffSensorMode()

用这个方法来设置悬崖传感器的模式，也可在 *设置 > 导航 > 传感器设置* 中操作。
注意，有效调用这个方法的前提是机器有悬崖传感器，[如何判断机器是否有悬崖传感器](#hasCliffSensor)。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |cliffSensorMode|[CliffSensorMode](#cliffSensorMode)|悬崖传感器模式|

- **原型**

  ``` java
  void setCliffSensorMode(CliffSensorMode cliffSensorMode);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.78

---

### getCliffSensorMode()

用这个方法来获取悬崖传感器的模式，也可在 *设置 > 导航 > 传感器设置* 中查看。
注意，有效调用这个方法的前提是机器有悬崖传感器，[如何判断机器是否有悬崖传感器](#hasCliffSensor)。

- **返回值**

  |类型|说明|
  |-|-|
  |[CliffSensorMode](#cliffSensorMode)|悬崖传感器模式|

- **原型**

  ``` java
  CliffSensorMode getCliffSensorMode();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78

---

### setHeadDepthSensitivity()

用这个方法来设置头部深度传感器的敏感度，也可在 *设置 > 导航 > 传感器设置* 中操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |sensitivityLevel|[SensitivityLevel](#sensitivityLevel)|头部深度传感器的敏感度级别|

- **原型**

  ``` java
  void setHeadDepthSensitivity(SensitivityLevel sensitivityLevel);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.78

---

### getHeadDepthSensitivity()

用这个方法来获取头部深度传感器的敏感度级别，也可在 *设置 > 导航 > 传感器设置* 中查看。

- **返回值**

  |类型|说明|
  |-|-|
  |[SensitivityLevel](#sensitivityLevel)|敏感度级别|

- **原型**

  ``` java
  SensitivityLevel getHeadDepthSensitivity();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78

---

### setFrontTOFEnabled()

用这个方法来开启或关闭前部 TOF 传感器，也可在 *设置 > 导航 > 传感器设置* 中操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|boolean|传入 true（false）开启（关闭）前部 TOF 传感器|

- **原型**

  ``` java
  void setFrontTOFEnabled(boolean enabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.78

---

### isFrontTOFEnabled()

用这个方法来查看前部 TOF 传感器是否开启，也可在 *设置 > 导航 > 传感器设置* 中查看。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示已开启（关闭）|

- **原型**

  ``` java
  boolean isFrontTOFEnabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78

---

### setBackTOFEnabled()

用这个方法来开启或关闭背部 TOF 传感器，也可在 *设置 > 导航 > 传感器设置* 中操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|boolean|传入 true（false）开启（关闭）背部 TOF 传感器|

- **原型**

  ``` java
  void setBackTOFEnabled(boolean enabled);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.78

---

### isBackTOFEnabled()

用这个方法来查看背部 TOF 传感器是否开启，也可在 *设置 > 导航 > 传感器设置* 中查看。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示已开启（关闭）|

- **原型**

  ``` java
  boolean isBackTOFEnabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.78


---

### isStandByOn()

查询待机状态

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 表示处于待机状态, false 表示没有处于待机状态, null 表示查询失败|

- **原型**

  ``` java
  boolean isStandByOn();
  ```

- **所需权限**

  None.

- **最小支持版本**

  1.129.0

---

### startStandBy()

进入待机状态

- **返回值**

  |类型|说明|
  |-|-|
  |int|下方列出返回结果|

  <ul>
    <li> -1 执行失败，可能是 Robot 实例尚未初始化完成
    <li> 0 启动成功
    <li> 1 已经处于 standBy 状态
    <li> 2 standby 在设置中被禁用
    <li> 3 temi 正处于 OTA、 迎宾模式等状态，无法响应请求
    <li> 403 需要 SETTINGS 权限
    <li> 429 请求次数过多, 需要间隔 5 秒
  </ul>


- **原型**

  ``` java
  int startStandBy();
  ```

- **所需权限**

  SETTINGS

- **最小支持版本**

  1.129.0

---

### stopStandBy()

退出待机状态，可能需要提供密码

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |password|String|如果处于密码保护状态，需要提供密码以解锁. <br>默认为空|

- **返回值**

  |类型|说明|
  |-|-|
  |int|下方列出返回结果|

  <ul>
      <li> -1 执行失败，可能是 Robot 实例尚未初始化完成
      <li> 0 退出成功
      <li> 1 没有处于待机状态
      <li> 2 需要提供密码
      <li> 3 密码错误
      <li> 403 需要 SETTINGS 权限
      <li> 429 请求次数过多, 需要间隔 5 秒
  </ul>

- **原型**

  ``` java
  void stopStandBy(String password);
  ```

- **所需权限**

  SETTINGS

- **最小支持版本**

  1.129.0
---

### enableStandBy()

开关待机模式，关闭可能需要提供密码

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|Boolean|开启或关闭|
  |password|String|如果处于密码保护状态，需要提供密码以解锁. <br>默认为空|

- **返回值**

  |类型|说明|
  |-|-|
  |int|下方列出返回结果|

  <ul>
      <li> -1 执行失败，可能是 Robot 实例尚未初始化完成
      <li> 0 启动/退出失败
      <li> 1 启动/退出成功
      <li> 2 需要提供密码
      <li> 3 密码错误
      <li> 403 需要 SETTINGS 权限
      <li> 429 请求次数过多, 需要间隔 5 秒
  </ul>

- **原型**

  ``` java
  int enableStandBy(Boolean enabled, String password);
  ```

- **所需权限**

  SETTINGS

- **最小支持版本**

  1.131.0

---

### getHomeScreenMode()

获得当前主屏幕模式

- **返回值**

  |类型|说明|
  |-|-|
  |HomeScreenMode|当前主屏幕模式|

- **原型**

  ``` java
  HomeScreenMode getHomeScreenMode();
  ```

- **所需权限**

  无

- **最小支持版本**

  1.135.1

<br>

## 接口

以下是工具相关的监听器详细信息。

### OnBatteryStatusChangedListener

可监听电池电量、充电状态的变化。在你的上下文中实现这个监听器接口，并重写接口中的方法以获取电池状态变化信息。

#### 原型

``` java
package com.robotemi.sdk.listener;

interface OnBatteryStatusChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |batteryData|[BatteryData](#batteryData)|电池数据发生变化后的对象|

- **原型**

  ``` java
  void onBatteryStatusChanged(BatteryData batteryData);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnBatteryStatusChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnBatteryStatusChangedListener(OnBatteryStatusChangedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnBatteryStatusChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnBatteryStatusChangedListener(OnBatteryStatusChangedListener listener);
  ```

#### 最小支持版本

  0.10.53

---

### OnPrivacyModeChangedListener

可监听隐私模式开关状态的变化。在你的上下文中实现这个监听器接口，并重写接口中的方法以获取隐私模式状态变化。

#### 原型

``` java
package com.robotemi.sdk.listener;

interface OnPrivacyModeChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |state|boolean|隐私模式是否开启，true 为已开启，false 为已关闭|

- **原型**

  ``` java
  void onPrivacyModeChanged(boolean state);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnPrivacyModeChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnPrivacyModeStateChangedListener(OnPrivacyModeChangedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnPrivacyModeChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnPrivacyModeStateChangedListener(OnPrivacyModeChangedListener listener);
  ```

#### 最小支持版本

  0.10.53

---

### OnSdkExceptionListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取在 temi SDK API 使用上出现的异常问题（例如没有权限，调用过于频繁等），从而可快速定位问题以提高开发效率。

#### 原型

``` java
package com.robotemi.sdk.exception;

interface OnSdkExceptionListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |sdkException|[SdkException](#sdkException)|SDK 异常信息|

- **原型**

  ``` java
  void onSdkError(SdkException sdkException);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnSdkExceptionListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnSdkExceptionListener(OnSdkExceptionListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnSdkExceptionListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnSdkExceptionListener(OnSdkExceptionListener listener);
  ```

#### 最小支持版本

  0.10.70

---

### OnDisabledFeatureListUpdatedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取不可用系统功能列表的更新（导航、跟随、视频通话等）

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnDisabledFeatureListUpdatedListener {}
```

#### 静态常量

这里的常量均为可能的不可用系统功能 ID。

|常量|类型|值|说明|
|-|-|-|-|
|BE_WITH_ID|String|com.roboteam.teamy.robox.data.api.info.ready::BE_WITH|跟随|
|GO_TO_ID|String|com.roboteam.teamy.robox.data.api.info.ready::GO_TO|导航|
|HARD_BUTTON_ID|String|com.roboteam.teamy.robox.data.api.info.ready::HARD_BUTTON|硬件按钮|
|MIC_ID|String|com.roboteam.teamy.robox.data.api.info.ready::MIC|麦克风|
|MQTT_ID|String|com.roboteam.teamy.robox.data.api.info.ready::MQTT|MQTT|
|SPEAKER_ID|String|com.roboteam.teamy.robox.data.api.info.ready::SPEAKER|扬声器|
|ST_CLEAN_ID|String|com.roboteam.teamy.robox.data.api.info.ready::ST_CLEAN|-|
|ST_VALID_ID|String|com.roboteam.teamy.robox.data.api.info.ready::ST_VALID|-|
|WAKEUP_ID|String|com.roboteam.teamy.robox.data.api.info.ready::WAKEUP|唤醒|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |disabledFeatureList|List\<String\>|不可用系统功能列表|

- **原型**

  ``` java
  void onDisabledFeatureListUpdated(List<String> disabledFeatureList);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDisabledFeatureListUpdatedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnDisabledFeatureListUpdatedListener(OnDisabledFeatureListUpdatedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDisabledFeatureListUpdatedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnDisabledFeatureListUpdatedListener(OnDisabledFeatureListUpdatedListener listener);
  ```

#### 最小支持版本

  0.10.74



---

### OnButtonStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取按键状态的变化

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnButtonStatusChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |hardButton|HardButton|按键类型, 目前仅支持急停按钮|
  |status|HardButton.Status|按键状态|

- **原型**

  ``` java
  void onButtonStatusChanged(HardButton hardButton, HardButton.Status status);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnButtonStatusChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnButtonStatusChangedListener(OnButtonStatusChangedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnButtonStatusChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnButtonStatusChangedListener(OnButtonStatusChangedListener listener);
  ```

#### 最小支持版本

  1.134.0

---

### OnButtonModeChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取按键禁用或启用状态的变化

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnButtonModeChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |buttonType|HardButton|按键类型|
  |buttonMode|HardButton.Mode|按键模式|

- **原型**

  ``` java
  void onButtonModeChanged(HardButton buttonType, HardButton.Mode buttonMode);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnButtonModeChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnButtonModeChangedListener(OnButtonModeChangedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnButtonModeChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnButtonModeChangedListener(OnButtonModeChangedListener listener);
  ```

#### 最小支持版本

  1.136.0




<br>

## 模型

以下是上述方法中用到的数据模型。

### BatteryData

用于保存电池相关的信息的类。

#### 原型

``` java
package com.robotemi.sdk;

class BatteryData {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|level|int|电池电量的百分比 1~100 %|
|isCharging|boolean|是否在充电中，true 表示正在充电，false 反之|
|battery2Level|int|第二块电池电量，134 版本加入|

---

### SafetyLevel

导航中的避障灵敏度等级，用于接收或设置导航避障灵敏度。

#### 原型

``` java
package com.robotemi.sdk.navigation.model;

enum SafetyLevel {
    HIGH,
    MEDIUM;
}
```

---

### SpeedLevel

Go to 的速度等级，用于接收或设置 go to 速度。

#### 原型

``` java
package com.robotemi.sdk.navigation.model;

enum SpeedLevel {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    SLOW,
    VERY_SLOW;
}
```

`SpeedLevel.customSpeed()` 方法用于设置 Goto 自定义速度，不适用于 follow 跟随。最小支持版本为 1.136.0

`VERY_HIGH` 和 VERY_SLOW 是 1.137.1 版本加入的。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floatValue|float|速度，单位为 m/s, 0.1-1.5|

---

### SdkException

temi SDK 使用异常信息，例如没有权限、调用过于频繁等。

#### 原型

``` java
package com.robotemi.sdk.exception;

class SdkException {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|code|int|异常错误代码|
|message|String|异常信息|

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|CODE_ILLEGAL_ARGUMENT|int|400|参数非法|
|CODE_PERMISSION_DENIED|int|403|没有权限|
|CODE_OPERATION_CONFLICT|int|409|操作冲突|
|CODE_LAUNCHER_ERROR|int|500|Launcher 内部异常|

### Page

系统内置界面。

#### 目前可启动的系统界面

|界面|枚举值|值|
|-|-|-|
|设置|SETTINGS|com.robotemi.page.settings|
|地图编辑器|MAP_EDITOR|com.robotemi.page.map_editor|
|联系人|CONTACTS|com.robotemi.page.contacts|
|位置|LOCATIONS|com.robotemi.page.locations|
|应用列表|ALL_APPS|com.robotemi.page.all_apps|
|主页|HOME|com.robotemi.page.home|
|讲解列表（132 版本加入）|TOURS|com.robotemi.page.tours|

#### 原型

``` java
package com.robotemi.sdk.constants;

enum Page {
    SETTINGS,
    MAP_EDITOR,
    CONTACTS,
    LOCATIONS,
    ALL_APPS,
    HOME,
    TOURS;
}
```

---

### HardButton

硬件按钮。

#### 子类

##### Mode <a name="hardButtonMode" />

###### 原型

  ``` java
  enum Mode {
      ENABLED,  // 启用
      DISABLED,  // 禁用
      MAIN_BLOCK_FOLLOW;  // 拦截跟随，只对主按钮有效
  }
  ```

##### Status <a name="hardButtonStatus" />

###### 原型

  ``` java
  enum Status {
      UNKNOWN,  // 未知
      HOLD,  // 按下
      RELEASED;  // 释放
      CLICKED;  // 点击， 1.136.0 版本加入
  }
  ```

#### 原型

``` java
package com.robotemi.sdk.constants;

enum HardButton {
    MAIN,  // 主按钮
    POWER,  // 电源键
    VOLUME,  // 音量键
    EMERGENCY_STOP,  // 急停键
    VOLUME_UP,  // 音量加键
    VOLUME_DOWN,  // 音量减键
    ;
}
```

---

### SoundMode

声音模式。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum SoundMode {
    NORMAL,  // 默认模式（音视频播放）
    VIDEO_CALL;  // 视频通话模式
}
```

---

### Mode

系统模式，包括默认、迎宾和隐私模式三种。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum Mode {
   DEFAULT,  // 默认
   GREET,  // 迎宾
   PRIVACY;  // 隐私
}
```

---

### CliffSensorMode

悬崖传感器模式，包括关闭、低敏感度和高敏感度。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum CliffSensorMode {
    OFF,
    LOW_SENSITIVITY,
    HIGH_SENSITIVITY;
}
```

---

### SensitivityLevel

灵敏度级别，包括低、高敏感度。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum SensitivityLevel {
    HIGH,
    LOW;
}
```

---

### HomeScreenMode

主屏幕模式

#### 原型

``` kotlin

enum class HomeScreenMode {
    DEFAULT,
    CLEAR,
    CUSTOM_SCREEN,
    URL,
    APPLICATION,
}
```
