# System Settings Related

## API Overview

|Return|Method|Description|
|-|-|-|
|String|[getSerialNumber()](#getSerialNumber)|Get temi's Serial Number|
|[BatteryData](#batteryData)|[getBatteryData()](#getBatteryData)|Get temis's Battery information |
|void|[showTopBar()](#showTopBar)|Show Top Bar|
|void|[hideTopBar()](#hideTopBar)|Hide Top Bar|
|void|[showAppList()](#showAppList)|Show App List|
|boolean|[isHardButtonsDiabled()](#isHardButtonsDisabled)|Is Hard Buttons disabled |
|void|[setHardButtonsDisabled(boolean disable)](#setHardButtonsDisabled)|Disable/Enable Hard Buttons|
|boolean|[getPrivacyMode()](#getPrivacyMode)|Is Privacy Mode on|
|void|[setPrivacyMode(boolean on)](#setPrivacyMode)|Turn on/off Privacy Mode|
|String|[getLauncherVersion()](#getLauncherVersion)|Get the version of Launcher|
|String|[getRoboxVersion()](#getRoboxVersion)|Get the version of Robox|
|void|[toggleWakeup()](#toggleWakeup)|Disable(Enable) wake-up|
|boolean|[isWakeupDisabled()](#isWakeupDisabled)|Check is wake-up disabled|
|void|[toggleNavigationBillboard()](#toggleNavigationBillboard)|Hide(show) billboard during go to|
|boolean|[isNavigationBillboardDisabled()](#isNavigationBillboardDisabled)|Check is billboard invisible during go to|
|void|[setTopBadgeEnabled(boolean enabled)](#setTopBadgeEnabled)|Enable(Disable) top badge|
|boolean|[isTopBadgeEnabled()](#isTopBadgeEnabled)|Check is top badge enabled|
|void|[setAutoReturnOn(boolean on)](#setAutoReturnOn)|Turn on(off) auto-return|
|boolean|[isAutoReturnOn()](#isAutoReturnOn)|Check is auto-return on|
|void|[setVolume(int volume)](#setVolume)|Set system volume|
|int|[getVolume()](#getVolume)|Get system volume|
|void|[setNavigationSafety(SafetyLevel level)](#setNavigationSafety)|Set obstacles avoidance sensitivity level of navigation|
|[SafetyLevel](#safetyLevel)|[getNavigationSafety()](#getNavigationSafety)|Get obstacles avoidance sensitivity level of navigation|
|void|[setGoToSpeed(SpeedLevel level)](#setgotoSpeed)|Set speed level of go-to|
|[SpeedLevel](#speedLevel)|[getGoToSpeed()](#getgotospeed)|Get speed level of go-to|
|[SafetyLevel](#safetyLevel)|[getNavigationSafety()](#getNavigationSafety)|Get obstacles avoidance sensitivity level of navigation|
|int|[setFollowSpeed(SpeedLevel level)](#setfollowspeed)|Set speed level of follow|
|[SpeedLevel](#speedLevel)|[getFollowSpeed()](#getfollowspeed)|Get speed level of follow|
|int|[setMinimumObstacleDistance()](#setMinimumObstacleDistance)|set minimum obstacle distance|
|int|[minimumObstacleDistance()](#minimumObstacleDistance)|Get minimum obstacle distance|
|void|[restart()](#restart)|Restart temi|
|void|[startPage(Page page)](#startPage)|Start system internal page|
|void|[setLocked(boolean locked)](#setLocked)|Enable(disable) protection by password|
|boolean|[isLocked()](#isLocked)|Check is protection enabled|
|void|[muteAlexa()](#muteAlexa)|Mute Alexa's microphone|
|void|[shutdown](#shutdown)|Shutdown temi|
|void|[setSoundMode(SoundMode soundMode)](#setSoundMode)|Set the mode of sound|
|void|[setHardButtonMode(HardButton type, HardButton.Mode mode)](#setHardButtonMode)|Set mode for the specific hard button|
|[HardButton.Mode](#hardButtonMode)|[getHardButtonMode(HardButton type)](#getHardButtonMode)|Get the mode of the specific hard button|
|[HardButton.Status](#hardbuttonstatus)|[getButtonStatus(HardButton type)](#getbuttonstatus)|Get the status of the specific hard button|
|String|[getNickName()](#getNickName)|Get temi's nick name|
|void|[setMode(Mode mode)](#setMode)|Set system mode|
|[Mode](#mode)|[getMode()](#getMode)|Get system mode|
|Map\<String, Boolean\>|[getSupportedLatinKeyboards()](#getSupportedLatinKeyboards)|Get all supported Latin keyboards and their enabled status.|
|void|[enabledLatinKeyboards(List\<String\>)](#enabledLatinKeyboards)|Enable the required Latin keyboard|
|void|[setGroundDepthCliffDetectionEnabled(boolean enabled)](#setGroundDepthCliffDetectionEnabled)|Enable(Disable) ground depth sensor|
|boolean|[isGroundDepthCliffDetectionEnabled()](#isGroundDepthCliffDetectionEnabled)|Check is ground depth sensor enabled|
|boolean|[hasCliffSensor()](#hasCliffSensor)|Check if temi has cliff sensor|
|void|[setCliffSensorMode(CliffSensorMode cliffSensorMode)](#setCliffSensorMode)|Set the mode of cliff sensor|
|[CliffSensorMode](#cliffSensorMode)|[getCliffSensorMode()](#getCliffSensorMode)|Check the mode of cliff sensor|
|void|[setHeadDepthSensitivity(SensitivityLevel sensitivityLevel)](#setHeadDepthSensitivity)|Set the level of the head depth sensitivity|
|[SensitivityLevel](#sensitivityLevel)|[getHeadDepthSensitivity()](#getHeadDepthSensitivity)|Get the level of the head depth sensitivity|
|void|[setFrontTOFEnabled(boolean enabled)](#setFrontTOFEnabled)|Enable(Disable) front TOF sensor|
|boolean|[isFrontTOFEnabled()](#isFrontTOFEnabled)|Check is front TOF sensor enabled|
|void|[setBackTOFEnabled(boolean enabled)](#setBackTOFEnabled)|Enable(Disable) back TOF sensor|
|boolean|[isBackTOFEnabled()](#isBackTOFEnabled)|Check is back TOF sensor enabled|
|boolean|[isStandByOn()](#isStandByOn)|Get StandBy status|
|int|[startStandBy()](#startStandBy)|Start StandBy|
|int|[stopStandBy()](#stopStandBy)|Stop StandBy with optional password|
|int|[enableStandBy(Boolean enabled, String password)](#enableStandBy)|Turn ON/OFF StandBy Mode|
|[HomeScreenMode]($homescreenmode)|[getHomeScreenMode()](#gethomescreenmode)|Get current home screen mode|

|Interface|Description|
|-|-|
|[OnBatteryStatusChangedListener](#OnBatteryStatusChangedListener)|Battery Data Status Changed listener|
|[OnPrivacyModeChangedListener](#OnPrivacyModeChangedListener)|Privacy Mode Changed listener|
|[OnDisabledFeatureListUpdatedListener](#onDisabledFeatureListUpdatedListener)|Disabled features list updated listener|
|[OnButtonStatusChangedListener](#onButtonStatusChangedListener)|Hard button status listener|
|[OnButtonModeChangedListener](#onButtonModeChangedListener)|Hard button mode listener|

|Model|Description|
|-|-|
|[BatteryData](#batteryData)|Battery Data|
|[SafetyLevel](#safetyLevel)|Obstacles avoidance sensitivity level|
|[SpeedLevel](#speedLevel)|Go to speed level|
|[Page](#page)|System internal page|
|[SoundMode](#soundMode)|Sound mode|
|[HardButton.Mode](#hardButtonMode)|Hard button mode|
|[HardButton.Status](#hardbuttonstatus)|Hard button status|
|[Mode](#mode)|System mode|
|[CliffSensorMode](#cliffSensorMode)|The mode of cliff sensor|
|[SensitivityLevel](#sensitivityLevel)|Sensitivity level|
|[HomeScreenMode](#homescreenmode)|Home Screen Mode|

<br>

## Methods

### getSerialNumber()

Use this method to fetch temi's serial number. This method can be used if you need to use a unique identifier for your temi.

- **Return**

  |Type|Description|
  |-|-|
  |String|temi's serial number|

- **Prototype**

  ``` java
  String getSerialNumber();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.43

---

### getBatteryData()

Use this method to manually request information on temi's battery. This information includes: Battery percentage and charging status.

- **Return**

  |Type|Description|
  |-|-|
  |[BatteryData](#batteryData)|Battery data|

- **Prototype**

  ``` java
  BatteryData getBatteryData();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.43

---

### showTopBar()

Use this method to manually request to show temi's top bar.

- **Prototype**

  ``` java
  void showTopBar();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.43

---

### hideTopBar()

Use this method to manually request to hide temi's top bar

- **Prototype**

  ``` java
  void hideTopBar();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.43

---

### showAppList()

Use this method to manually request to show temi's all apps.

- **Prototype**

  ``` java
  void showAppList();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### getPrivacyMode()

Use this method to get temi's privacy mode status.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if privacy mode is on, false otherwise|

- **Prototype**

  ``` java
  boolean getPrivacyMode();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.53

---

### setPrivacyMode()

Use this method to set the status programmatically.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|true to turn on privacy mode, false to turn off|

- **Prototype**

  ``` java
  void setPrivacyMode(boolean on);
  ```

- **Required permissions**

Selected Kiosk (Before 129 version), SETTINGS.

- **Support from**

  0.10.53

---

### isHardButtonsDisabled()

Use this method to get whether the hardware button is currently disabled.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if hard buttons is disabled, false otherwise|

- **Prototype**

  ``` java
  boolean isHardButtonsDisabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.6

---

### setHardButtonsDisabled()

Use this method to disable or enable hard buttons.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |disable|boolean|true to disable the hard buttons, false to enable hard buttons|

- **Prototype**

  ``` java
  void setHardButtonsDisabled(boolean disable);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.6

---

### getHardButtonMode()

Use this method to get the mode of the specific hard button.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |type|HardButton|Hard button type, [HardButton.MAIN], [HardButton.POWER], [HardButton.VOLUME]|

- **Return**

  |Type|Description|
  |-|-|
  |[HardButton.Mode](#hardButtonMode)|The mode of the specific hard button|

- **Prototype**

  ``` java
  HardButton.Mode getHardButtonMode();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.77

---

### setHardButtonMode()

Use this method to set hard button mode for the specific hard button.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |type|[HardButton](#hardButton)|Type of the specific hard button|
  |mode|[HardButton.Mode](#hardButtonMode)|The mode of the specific hard button(ENABLED, DISABLED, MAIN_BLOCK_FOLLOW)|

- **Prototype**

  ``` java
  void setHardButtonMode(HardButton type, HardButton.Mode mode);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.77

---

### getHardButtonStatus()

Use this method to get the status of the specific hard button.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |type|HardButton|Hard button type, [HardButton.EMERGENCY_STOP]|

- **Return**

  |Type|Description|
  |-|-|
  |[HardButton.Status](#hardbuttonstatus)|The status of the specific hard button|

- **Prototype**

  ``` java
  HardButton.Status getHardButtonStatus();
  ```

- **Required permissions**

  None.

- **Support from**

  1.134.0

---

### getLauncherVersion()

Use this method to get the version of Launcher.

- **Return**

  |Type|Description|
  |-|-|
  |String|The version of Launcher|

- **Prototype**

  ``` java
  String getLauncherVersion();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.65

---

### getRoboxVersion()

Use this method to get the version of Robox.

- **Return**

  |Type|Description|
  |-|-|
  |String|The version of Robox|

- **Prototype**

  ``` java
  String getRoboxVersion();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.65

---

### toggleWakeup()

In kiosk mode you have the option of toggling the wakeup trigger on and off to your liking.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |disabled|boolean|Set true(false) to disable(enable)|

- **Prototype**

  ``` java
  void toggleWakeup(boolean disabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.44

---

### isWakeupDisabled()

Check wheather wake-up is disabled.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means disabled(enabled)|

- **Prototype**

  ``` java
  boolean isWakeupDisabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### toggleNavigationBillboard()

In kiosk mode you have the option of toggling the visibility of the navigation billboard when you perform goTo commands.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |disabled|boolean|Set true(false) to hide(show)|

- **Prototype**

  ``` java
  void toggleNavigationBillboard(boolean disabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version),Settings

- **Support from**

  0.10.44

---

### isNavigationBillboardDisabled()

Check wheather navigation billboard is invisible.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means invisible(visible)|

- **Prototype**

  ``` java
  boolean isNavigationBillboardDisabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setTopBadgeEnabled()

Enable(Disable) top green badge. When badge is enabled, the movement status will be displayed in the top badge in real time when following, navigating, etc.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|boolean|Set true(false) to enable(disable) top badge|

- **Prototype**

  ``` java
  void setTopBadgeEnabled(boolean enabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### isTopBadgeEnabled()

Check wheather top badge is enabled. Enabled is as the default.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means enabled(disabled)|

- **Prototype**

  ``` java
  boolean isTopBadgeEnabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setAutoReturnOn()

Enable(disable) auto return. With Auto Return ON, temi will return to one of your locations after a predefined amount of time. How to set up in Launcher: *Settings > General > Auto Return > Duration or Locations*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|Set true(false) to turn on(off) auto return|

- **Prototype**

  ``` java
  void setAutoReturnOn(boolean on);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### isAutoReturnOn()

Check wheather auto return is on. Off is as the default.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|ture(false) means auto return is on(off)|

- **Prototype**

  ``` java
  boolean isAutoReturnOn();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setVolume()

Set the volume of system, the value range is 0-10. In the system, you can adjust the volume by touching the "+" and "-" buttons at the top of head, or by sliding up from the bottom edge of the screen to pop up the volume and brightness adjustment sliders, and sliding the volume adjustment slider left and right to adjust the volume.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |volume|int|volume|
  |showDrawer|boolean|show drawer, added in 1.137.1 version, default is false|

- **Prototype**

  ``` java
  void setVolume(int volume, boolean showDrawer);
  ```

- **Required permissions**

  Settings

- **Support from**

  0.10.70

---

### getVolume()

Get the current system volume, the range is 0-10.

- **Return**

  |Type|Description|
  |-|-|
  |int|Current volume|

- **Prototype**

  ``` java
  int getVolume();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setNavigationSafety() <a name="setNavigationSafety" />

Set the sensitivity level of obstacle avoidance during navigation. How to set in Launcher: *Settings > Navigation > Obstacle avoidance sensitivity*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |level|[SafetyLevel](#safetyLevel)|Obstacle avoidance sensitivity level|

- **Prototype**

  ``` java
  void setNavigationSafety(SafetyLevel level);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### getNavigationSafety()

Get current navigation safety level.

- **Return**

  |Type|Description|
  |-|-|
  |[SafetyLevel](#safetyLevel)|Current obstacle avoidance sensitivity level|

- **Prototype**

  ``` java
  SafetyLevel getNavigationSafety();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setGoToSpeed()

Set speed level of go to. How to set in Launcher: *Launcher > Navigation > “Go To” configuration > Speed Control*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |level|[SpeedLevel](#speedLevel)|Speed level|

- **Prototype**

  ``` java
  void setGoToSpeed(SpeedLevel level);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### getGoToSpeed()

Get the speed level of go to.

- **Return**

  |Type|Description|
  |-|-|
  |[SpeedLevel](#speedLevel)|Current speed level of go to|

- **Prototype**

  ``` java
  SpeedLevel getGoToSpeed();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setFollowSpeed()

Set speed level of follow.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |level|[SpeedLevel](#speedLevel)|Speed level|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not support by current launcher <br>200 success <br>400 invalid parameter <br>403 SETTINGS Permission required|

- **Prototype**

  ``` java
  int setFollowSpeed(SpeedLevel level);
  ```

- **Required permissions**

  Settings

- **Support from**

  1.135.1

---

### getFollowSpeed()

Get the speed level of follow.

- **Return**

  |Type|Description|
  |-|-|
  |[SpeedLevel](#speedLevel)|Current speed level of follow|

- **Prototype**

  ``` java
  SpeedLevel getFollowSpeed();
  ```

- **Required permissions**

  None.

- **Support from**

  1.135.1

### setMinimumObstacleDistance() <a name="setMinimumObstacleDistance" />

Set minimum obstacle distance to add more safe margin in navigation when you robot is wider than normal because of some add-ons.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |value|int|Value of distance in centimeters, range from 0 to 100, step by 5|

- **Return**

  The value that was set as integer.
  
  Return 0 may stand for failure

- **Prototype**

  ``` java
  int setMinimumObstacleDistance(int value);
  ```

- **Required permissions**

  Settings

- **Support from**

  1.131.4

---

### minimumObstacleDistance() <a name="minimumObstacleDistance" />

Get value of minimum obstacle distance.

- **Return**

  |Type|Description|
  |-|-|
  |int|Distance in centimeters, or error code 400 or 403, if invalid argument or permission required.|

- **Prototype**

  ``` java
  int minimumObstacleDistance();
  ```

- **Required permissions**

  None.

- **Support from**

  1.131.4

### restart()

Using this method to restart temi.

- **Prototype**

  ``` java
  void restart();
  ```

- **Required permissions**

  Selected Kiosk

- **Support from**

  0.10.72

---

### startPage()

Using this method to start system internal page.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |page|[Page](#page)|System page|

- **Prototype**

  ``` java
  void startPage(Page page);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.72

---

### setLocked()

Using this method to enable(disable) protection by password.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |locked|boolean|Set true(false) to enable(disable) protection|

- **Prototype**

  ``` java
  void setLocked(boolean);
  ```

- **Required permissions**

  Settings

- **Support from**

  0.10.74

---

### isLocked()

Check whether the protection by password is on.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|ture(false) means protection by password is enabled(disabled)|

- **Prototype**

  ``` java
  boolean isLocked();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.74

---

### muteAlexa()

Using this method to mute Alexa's microphone. The microphone resource will be occupied when the assistant Alexa is selected. So using this to release the microphone if you want to use that in your App.

- **Prototype**

  ``` java
  void muteAlexa();
  ```

- **Support from**

  0.10.75

### shutdown()

Using this method to shutdown temi.

- **Prototype**

  ``` java
  void shutdown();
  ```

- **Required permissions**

  Selected Kiosk

- **Support from**

  0.10.77

---

### setSoundMode()

Use this method to set the sound mode(NORMAL, VIDEO_CALL) that is for improving the user experience of sound in the video call scenario.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |soundMode|[SoundMode](#soundMode)|The sound mode to be set|

- **Prototype**

  ``` java
  void setSoundMode(SoundMode soundMode);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.77

- **Note**

  To improve user experience, if you set the sound mode to video call mode when you start your own video call application, it is recommended to switch the sound mode back to normal mode after ending the video call ([NORMAL](#soundMode)).

---

### getNickName()

Use this method to get the nick name of temi.

- **Return**

  |Type|Description|
  |-|-|
  |String|temi's nick name|

- **Prototype**

  ``` java
  String getNickName();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.77

---

### setMode()

Use this method to set temi's system mode(Default, Greet, Privacy). You can also set the mode manually in *Settings > Modes*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mode|[Mode](#mode)|System mode|

- **Prototype**

  ``` java
  void setMode(Mode mode);
  ```

- **Required permissions**

  Selected kiosk (Before 129 version), Settings

- **Support from**

  0.10.77

### getMode()

Use this method to get the current system mode of temi.

- **Return**

  |Type|Description|
  |-|-|
  |[Mode](#mode)|Current system mode of temi|

- **Prototype**

  ``` java
  Mode getMode();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.77

---

### getSupportedLatinKeyboards()

Use this method to get all supported Latin keyboards and their enabled status.

- **Return**

  |Type|Description|
  |-|-|
  |Map\<String, Boolean\>|Specific language and corresponding enabled status|

- **Prototype**

  ``` java
  Map<String, Boolean> getSupportedLatinKeyboards();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.77

---

### enabledLatinKeyboards()

Use this method to enable the required Latin keyboard.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |keyboards|List\<String\>|The list of required Latin keyboards and its first element will be the current selected keyboard. **Get all supported Latin keyboards by method [getSupportedLatinKeyboards()](#getSupportedLatinKeyboards)**.|

- **Prototype**

  ``` java
  void enabledLatinKeyboards(List<String> keyboards);
  ```

- **Required permissions**

  Settings

- **Support from**

  0.10.77

---

### setGroundDepthCliffDetectionEnabled()

Use this method to enable(disable) the ground depth sensor. You can also set it manually in *Settings > Navigation > Sensors Settings*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|boolean|Set true(false) to enable(disable) ground depth sensor|

- **Prototype**

  ``` java
  void setGroundDepthCliffDetectionEnabled(boolean enabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.78

---

### isGroundDepthCliffDetectionEnabled()

Use this method to check wheather the ground depth sensor is enabled, You can also check it manually in *Settings > Navigation > Sensors Settings*.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means ground depth sensor is enabled(disabled)|

- **Prototype**

  ``` java
  boolean isGroundDepthCliffDetectionEnabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### hasCliffSensor()

Use this method to check wheather temi has cliff sensor.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means temi has(does not have) cliff sensor|

- **Prototype**

  ``` java
  boolean hasCliffSensor();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### setCliffSensorMode()

Use this method to set the mode of cliff sensor. You can also set it manually in *Settings > Navigation > Sensors Settings*.

Note, the premise of effectively calling this method is that the machine has a cliff sensor, [how to check whether the machine has a cliff sensor](#hasCliffSensor).

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |cliffSensorMode|[CliffSensorMode](#cliffSensorMode)|The mode of cliff sensor|

- **Prototype**

  ``` java
  void setCliffSensorMode(CliffSensorMode cliffSensorMode);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.78

---

### getCliffSensorMode()

Use this method to get the mode of cliff sensor. You can also check it manually in *Settings > Navigation > Sensors Settings*.

Note, the premise of effectively calling this method is that the machine has a cliff sensor, [how to check whether the machine has a cliff sensor](#hasCliffSensor).

- **Return**

  |Type|Description|
  |-|-|
  |[CliffSensorMode](#cliffSensorMode)|The mode of cliff sensor|

- **Prototype**

  ``` java
  CliffSensorMode getCliffSensorMode();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### setHeadDepthSensitivity()

Use this method to set the sensitivity level of head depth sensor. You can also set it manually in *Settings > Navigation > Sensors Settings*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |sensitivityLevel|[SensitivityLevel](#sensitivityLevel)|Sensitivity level of head depth sensor|

- **Prototype**

  ``` java
  void setHeadDepthSensitivity(SensitivityLevel sensitivityLevel);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.78

---

### getHeadDepthSensitivity()

Use this method to get the sensitivity level of head depth sensor. You can also check it manually in *Settings > Navigation > Sensors Settings*.

- **Return**

  |Type|Description|
  |-|-|
  |[SensitivityLevel](#sensitivityLevel)|Sensitivity level of head depth sensor|

- **Prototype**

  ``` java
  SensitivityLevel getHeadDepthSensitivity();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### setFrontTOFEnabled()

Use this method to enable(disable) the front TOF sensor. You can also set it manually in *Settings > Navigation > Sensors Settings*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|boolean|Set true(false) to enabel(disable) front TOF sensor|

- **Prototype**

  ``` java
  void setFrontTOFEnabled(boolean enabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.78

---

### isFrontTOFEnabled()

Use this method to check wheather the front TOF sensor is enabeld. You can also check it manually in *Settings > Navigation > Sensors Settings*.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means enabled(disabled)|

- **Prototype**

  ``` java
  boolean isFrontTOFEnabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### setBackTOFEnabled()

Use this method to enable(disable) the back TOF sensor. You can also set it manually in *Settings > Navigation > Sensors Settings*.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|boolean|Set true(false) to enabel(disable) back TOF sensor|

- **Prototype**

  ``` java
  void setBackTOFEnabled(boolean enabled);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.78

---

### isBackTOFEnabled()

Use this method to check wheather the back TOF sensor is enabeld. You can also check it manually in *Settings > Navigation > Sensors Settings*.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means enabled(disabled)|

- **Prototype**

  ``` java
  boolean isBackTOFEnabled();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.78

---

### isStandByOn()

Get StandBy status

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if under standBy status, false if not, null if check failed|

- **Prototype**

  ``` java
  boolean isStandByOn();
  ```

- **Required permissions**

  None.

- **Support from**

  1.129.0

---

### startStandBy()

 Start StandBy

- **Return**

  |Type|Description|
  |-|-|
  |int|Check result code below|

  <ul>
    <li> -1 for failed to request, maybe robot is not ready
    <li> 0 for standBy is started
    <li> 1 for standBy was already running
    <li> 2 for standby if disabled in settings
    <li> 3 for robot is busy, e.g. OTA, Greet Mode
    <li> 403 for SETTINGS permission required
    <li> 429 for too many requests, should be longer than 5 seconds between 2 calls
  </ul>


- **Prototype**

  ``` java
  int startStandBy();
  ```

- **Required permissions**

  SETTINGS

- **Support from**

  1.129.0

---

### stopStandBy()

Stop StandBy with optional password.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |password|String|When temi requires password to unlock, this method only works when here a valid password is passed. <br>Default as empty|

- **Return**

  |Type|Description|
  |-|-|
  |int|Check result code below|

  <ul>
      <li> -1 for failed to request, maybe robot is not ready
      <li> 0 for standBy is stopped
      <li> 1 for standBy was not running
      <li> 2 for password required
      <li> 3 for wrong password
      <li> 403 for SETTINGS permission required
      <li> 429 for too many requests, should be longer than 5 seconds between 2 calls
  </ul>

- **Prototype**

  ``` java
  void stopStandBy(String password);
  ```

- **Required permissions**

  Settings

- **Support from**

  1.129.0

---

### enableStandBy()

Enable or disable StandBy Mode, Disable may need password.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|Boolean|true or false to turn ON/OFF StandBy Mode|
  |password|String|When temi requires password to unlock, this method only works when here a valid password is passed. <br>Default as empty|

- **Return**

  |Type|Description|
  |-|-|
  |int|Check result code below|

  <ul>
      <li> -1 for failed to request, maybe robot is not ready
      <li> 0 for operation failed
      <li> 1 for operation succeed.
      <li> 2 for password required
      <li> 3 for wrong password
      <li> 403 for SETTINGS permission required
      <li> 429 for too many requests, should be longer than 5 seconds between 2 calls
  </ul>

- **Prototype**

  ``` java
  int enableStandBy(Boolean enabled, String password);
  ```

- **Required permissions**

  Settings

- **Support from**

  1.131.0

---

### getHomeScreenMode()

Get current home screen mode,

- **Return**

  |Type|Description|
  |-|-|
  |HomeScreenMode|Current home screen mode|

- **Prototype**

  ``` java
  HomeScreenMode getHomeScreenMode();
  ```

- **Required permissions**

  None

- **Support from**

  1.135.1

<br>

## Interfaces

Below are details regarding utils listeners.

### OnBatteryStatusChangedListener

This listener will listen to the status of battery data changed. Set your context to implement this listener and add the override method to get the battery status changes.

#### Prototype

``` java
package com.robotemi.sdk.listener;

interface OnBatteryStatusChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameters|Type|Description|
  |-|-|-|
  |batteryData|[BatteryData](#batteryData)|BatteryData object containing temi's battery status information|

- **Prototype**

  ``` java
  void onBatteryStatusChanged(BatteryData batteryData);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnBatteryStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnBatteryStatusChangedListener(OnBatteryStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnBatteryStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnBatteryStatusChangedListener(OnBatteryStatusChangedListener listener);
  ```

#### Support from

  0.10.53

---

### OnPrivacyModeChangedListener

This listener will listen to the status of privacy mode changed. Set your context to implement this listener and add the override method to get the privacy mode state changes.

#### Prototype

``` java
package com.robotemi.sdk.listener;

interface OnPrivacyModeChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |state|boolean|state will be true if privacy mode is on else flase|

- **Prototype**

  ``` java
  void onPrivacyModeChanged(boolean state);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnPrivacyModeChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnPrivacyModeStateChangedListener(OnPrivacyModeChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnPrivacyModeChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnPrivacyModeStateChangedListener(OnPrivacyModeChangedListener listener);
  ```

#### Support from

  0.10.53

---

### OnSdkExceptionListener

Set your context to implement this listener and add the override method to get the exception message in using temi SDK API(Such as permission denied, invoke too frequently), so that developers can quickly locate the problem to improve development efficiency.

#### Prototype

``` java
package com.robotemi.sdk.exception;

interface OnSdkExceptionListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |sdkException|[SdkException](#sdkException)|temi SDK exception information|

- **Prototype**

  ``` java
  void onSdkError(SdkException sdkException);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnSdkExceptionListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnSdkExceptionListener(OnSdkExceptionListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnSdkExceptionListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnSdkExceptionListener(OnSdkExceptionListener listener);
  ```

#### Support from

  0.10.70


---

### OnButtonStatusChangedListener

Set your context to implement this listener and add the override method to get the updates of hard button status.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnButtonStatusChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |hardButton|HardButton|Button type, only support Emergency Stop button.|
  |status|HardButton.Status|Button status|

- **Prototype**

  ``` java
  void onButtonStatusChanged(HardButton hardButton, HardButton.Status status);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnButtonStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnButtonStatusChangedListener(OnButtonStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnButtonStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnButtonStatusChangedListener(OnButtonStatusChangedListener listener);
  ```

#### Support from

  1.134.0

---

### OnButtonModeChangedListener

Set your context to implement this listener and add the override method to get the updates of hard button mode.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnButtonModeChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |buttonType|HardButton|Button type|
  |buttonMode|HardButton.Mode|Button mode|

- **Prototype**

  ``` java
  void onButtonModeChanged(HardButton buttonType, HardButton.Mode buttonMode);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnButtonModeChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnButtonModeChangedListener(OnButtonModeChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnButtonModeChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnButtonModeChangedListener(OnButtonModeChangedListener listener);
  ```

#### Support from

  1.136.0

---

### OnDisabledFeatureListUpdatedListener

Set your context to implement this listener and add the override method to get the updates of the list of disabled features(Navigation, follow, video call...)

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnDisabledFeatureListUpdatedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |disabledFeatureList|List\<String\>|List of disabled features|

- **Prototype**

  ``` java
  void onDisabledFeatureListUpdated(List<String> disabledFeatureList);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDisabledFeatureListUpdatedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnDisabledFeatureListUpdatedListener(OnDisabledFeatureListUpdatedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDisabledFeatureListUpdatedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnDisabledFeatureListUpdatedListener(OnDisabledFeatureListUpdatedListener listener);
  ```

#### Support from

  0.10.74

<br>

## Models

Below are the list of models used in the utils methods.

### BatteryData

Object used to hold information regarding temi's battery.

#### Prototype

``` java
package com.robotemi.sdk;

class BatteryData {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|level|int|Battery level in percentage 1-100%|
|isCharging|boolean|true if temi is charging else false|
|battery2Level|int|The battery level for the second battery, added in 134 version|

---

### SafetyLevel <a name="safetyLevel" />

Sensitivity level of obstacle avoidance during navigation.

#### Prototype

``` java
package com.robotemi.sdk.navigation.model;

enum SafetyLevel {
   HIGH,
   MEDIUM;
}
```

---

### SpeedLevel

Speed level of go to.

#### Prototype

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

`SpeedLevel.customSpeed()` method is used to set goto custom speed, not for follow speed, supported from 1.136.0

`VERY_HIGH` and `VERY_SLOW` are added in 1.137.1 version.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floatValue|float|speed, 0.1-1.5 m/s|
---

### SdkException

Exception information in using temi SDK, such as permission denied, invoke too frequently.

#### Prototype

``` java
package com.robotemi.sdk.exception;

class SdkException {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|code|int|Exception code|
|message|String|Exception message|

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|CODE_ILLEGAL_ARGUMENT|int|400|Illgeal arguments|
|CODE_PERMISSION_DENIED|int|403|Permission denied|
|CODE_OPERATION_CONFLICT|int|409|Operation conflict|
|CODE_LAUNCHER_ERROR|int|500|Interal error in Launcher|

### Page

System internal page.

#### The current system interface that can be started

|Page|Enum Value|Value|
|-|-|-|
|Settings|SETTINGS|com.robotemi.page.settings|
|Map Editor|MAP_EDITOR|com.robotemi.page.map_editor|
|Contacts|CONTACTS|com.robotemi.page.contacts|
|Locations|LOCATIONS|com.robotemi.page.locations|
|App List|ALL_APPS|com.robotemi.page.all_apps|
|Home Page|HOME|com.robotemi.page.home|
|Tour list (Supported in 132 version)|TOURS|com.robotemi.page.tours|

#### Prototype

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

Hard button.

#### Subclass

##### Mode <a name="hardButtonMode" />

###### Prototype

  ``` java
  enum Mode {
      ENABLED,
      DISABLED,
      MAIN_BLOCK_FOLLOW;  // Blocking follow(Only works for the main button).
  }
  ```

##### Status <a name="hardbuttonstatus" />

###### Prototype

  ``` java
  enum Status {
      UNKNOWN,
      HOLD,  // emergency button is pressed/hold
      RELEASED;  // emergency button is released.
      CLICKED;  // Clicked, added in 1.136.0
  }
  ```

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum HardButton {
    MAIN,  // Main button
    POWER,  // Power button
    VOLUME,  // Volumes buttons
    EMERGENCY_STOP,  // Emergency stop button
    VOLUME_UP,  // Volume up button
    VOLUME_DOWN,  // Volume down button
    ;
}
```

---

### SoundMode

Sound mode.

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum SoundMode {
    NORMAL,  // Default mode(for media playing).
    VIDEO_CALL;  // For video call scenarios.
}
```

---

### Mode

System mode, including Default, Greet and Privacy mode.

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum Mode {
   DEFAULT,
   GREET,
   PRIVACY;
}
```

---

### CliffSensorMode <a name="cliffSensorMode" />

The mode of cliff sensor, including Off, low sensitivity and high sensitivity.

#### Prototype

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

Sensitivity level, including low sensitivity and high sensitivity.

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum SensitivityLevel {
    HIGH,
    LOW;
}
```

---

### HomeScreenMode

The mode of home screen

#### Prototype

``` kotlin

enum class HomeScreenMode {
    DEFAULT,
    CLEAR,
    CUSTOM_SCREEN,
    URL,
    APPLICATION,
}
```
