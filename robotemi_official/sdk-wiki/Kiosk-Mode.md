# Kiosk Mode

Kiosk mode is a configuration of your temi skill that allows it to take over the home screen application and instead of the default temi home screen.

In the temi Settings -> Home Screen -> Application, you can set any app as home screen application.
But only the app who declares to be kiosk app in the manifest and actually selected as home screen app can be granted with special permissions, including overriding voice flow, restart the system, etc.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|void|[requestToBeKioskApp()](#requestToBeKioskApp)|Request to be the currently selected Kiosk skill|
|boolean|[isSelectedKioskApp()](#isSelectedKioskApp)|Check whether the skill is the currently selected Kiosk skill|
|void|[setKioskModeOn(boolean on, HomeScreenMode mode)](#setKioskModeOn)|Turn on(off) Kiosk mdoe|
|boolean|[isKioskModeOn](#isKioskModeOn)|Check is Kiosk mode on|

<br>

## Integration

To configure the skill as a kiosk skill you must take the following steps:

1. Open the `AndroidManifest.xml` .

2. In the **application** tag paste the following piece of code:

    ``` xml
    <!-- Ignore this if it has been added -->
    <meta-data
        android:name="com.robotemi.sdk.metadata.SKILL"
        android:value="@string/app_name" />

    <meta-data
        android:name="com.robotemi.sdk.metadata.KIOSK"
        android:value="TRUE" />
    ```

3. Compile and run the skill.

4. Even though the skill is visible now it is still not set as your temi's kiosk skill, to do so navigate to the settings menu. Now that you have a kiosk skill running you should see a new option in the settings called _Kiosk Mode_. Until the skill is set by launcher to be the default skill its' status will be _OFF_.

5. Click on the kiosk mode option and open the Kiosk Mode page. Turn on the kiosk mode from the top right corner.

6. Select your skill from the list of skills available for kiosk mode configuration. In the case where there is only one of those, it will automatically be selected for you when the mode is turned on.

<br>

## Methods

### requestToBeKioskApp()

Using this method to **dynamically apply** to become the currently selected Kiosk skill. Similar to requesting permission, the system will pop up a dialog box after the method is called. After user clicks the "Allow" button, your skill becomes the currently selected Kiosk skill.

- **Prototype**

  ``` java
  void requestToBeKioskApp();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### isSelectedKioskApp()

Check wheather your skill is the currently selected Kiosk skill.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means your skill is(is not) currently selected Kiosk skill|

- **Prototype**

  ``` java
  boolean isSelectedKioskApp();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setKioskModeOn()

Use this method to enable or disable kiosk mode. You can also enable or disable kiosk mode in *Settings > Apps > Kiosk*. If the incoming parameter is `true`, it will be the same as the processing logic of [requestToBeKioskApp()](#requestToBeKioskApp) and **dynamically apply** to become the currently selected Kiosk skill.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|true(false) to enable(disable) kiosk mode|
  |mode|HomeScreenMode|Add in 134 version, when turn off kiosk mode, can assisgn a mode to return to|

- **Prototype**

  ``` java
  void setKioskModeOn(boolean on, HomeScreenMode mode);
  ```

- **Required permissions**

  Settings, Kiosk

- **Support from**

  0.10.77

---

### isKioskModeOn()

Check if kiosk mode is eanbled.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true(false) means kiosk mode is enabled(disabled)|

- **Prototype**

  ``` java
  boolean isKioskModeOn();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.77
