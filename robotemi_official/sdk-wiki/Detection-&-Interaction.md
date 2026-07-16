# Detection & Interaction

User detection and interaction are two state machines which allow us to monitor when temi has detected a person and when a user is interacting with temi.

To make use of these state machines temi has to have the Detection Mode turned on, see integration instructions below. In Detection Mode temi will:

- Constantly look for users
- Distance 0.8 meter in frame of RGB camera
- If there is more than 1 user - pick the closest one and the most center one

temi will start the Detection Mode session if Launcher in a state without user interaction and movement, and your skill can listen for the events from that via this feature and according it to do what you want temi to do.

For more information on the detection and interaction state machines see below the Listeners section.

**Note: There's a [Welcoming Mode app](https://github.com/ROBOTEAM-HOME/temi-welcomingmode-b2b) already developed that would satisfy most B2B needs, and can provide a good reference for anyone who like to develop an app using the Detection Mode abilities.**

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|boolean|[isDetectionModeOn()](#isDetectionModeOn)|Check if Detection Mode is on|
|void|[setDetectionModeOn(boolean on)](#setDetectionModeOn)|Turn on(off) Detection Mode|
|void|[setDetectionModeOn(boolean on, float distance)](#setDetectionModeOnWithDistance)|Turn on Detection Mode with the maximum dection distance|
|boolean|[isTrackUserOn](#isTrackUserOn)|Check if Track User is on|
|void|[setTrackUserOn(boolean isOn)](#setTrackUserOn)|Turn on(off) Track User|

|Interface|Description|
|-|-|
|[OnDetectionStateChangedListener](#onDetectionStateChangedListener)|Detection Mode state changed listener|
|[OnUserInteractionChangedListener](#onUserInteractionChangedListener)|User interaction state changed listener|
|[OnDetectionDataChangedListener](#onDetectionDataChangedListener)|Detection Mode data changed listener|

|Model|Description|
|-|-|
|[DetectionData](#detectionData)|Detection data in Detection Mode|

<br>

## Integration

To configure the skill can use Detection Mode you must take the following steps:

1. The skill should be a [Kiosk Mode](https://github.com/robotemi/sdk/wiki/Kiosk-Mode) skill first. Please learn how to configure skill into a [Kiosk Mode](https://github.com/robotemi/sdk/wiki/Kiosk-Mode) from [here](https://github.com/robotemi/sdk/wiki/Kiosk-Mode).

2. Return temi and go to *Settings > General Settings > Turn on the Tracking User*.

3. Return to home page.

4. Compile and run your skill.

<br>

## Update

- ### Version 126(Launcher version is 15567)

  **[Turning of Detection Mode](#setDetectionModeOn) will be invalid if [Track User(welcome mode)](#setTrackUserOn) is turned on.**

- ### 0.10.77 Update the relationship between Detection Mode and Track User <a name="detectionModeAndTrackUser" />

  [Turning on Detection Mode](#setDetectionModeOn) will not affect [Track User(welcome mode)](#setTrackUserOn), ~~but turning off Detection Mode will turning off Track User~~. Turning on(off) Track User will turning on(off) Detection Mode.

  |Operation \ Effect|Detection Mode|Track User|
  |-|-|-|
  |Turning on Detection Mode|ON|-|
  |Turning off Detection Mode|OFF|~~OFF~~|
  |Turning on Track User|ON|ON|
  |Turning off Track User|OFF|OFF|

<br>

## Methods

### isDetectionModeOn()

Check if Detection Mode is on. Off is as the default.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|ture(false) means Detection Mode is on(off)|

- **Prototype**

  ``` java
  boolean isDetectionModeOn();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setDetectionModeOn()

Use this method to turn on (off) the Detection Mode. If use this method to turn on the Detection Mode, the maximum detection distance would be the default( **0.8** meters). **This method will not work if in Greet Mode**.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|`true`(`false`) to turn on(off) the Detection Mode|

- **Prototype**

  ``` java
  void setDectionModeOn(boolean on);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### setDetectionModeOn() <a name="setDetectionModeOnWithDistance" />

Use this method to turn on(off) the Detection Mode. If you use this method to turn on the Detection Mode, you can set its maximum detection distance at the same time. **This method will not work if in Greet Mode**.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|`true`(`false`) to turn on(off) the Detection Mode|
  |distance|float|The maximum distance that the Detection Mode can detect (only works when `on` is `true`). **0.5 - 2.0** are valid.|

- **Prototype**

  ``` java
  void setDectionModeOn(boolean on, float distance);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

---

### isTrackUserOn()

Check if Track User is on. Off is as the default.

- **Return**

  |Type|Description|
  |-|-|
  |boolean|ture(false) means Track User is on(off)|

- **Prototype**

  ``` java
  boolean isTrackUserOn();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### setTrackUserOn()

Use this method to turn on (off) the Track User. Track User means that after transiting to the state that without user interaction, temi will start constraint following the user that detected by Detection Mode and the green badge shows "Hello" will appear at the top bar. **This method will not work if in Greet Mode**.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |on|boolean|`true`(`false`) to turn on(off) the Track User|

- **Prototype**

  ``` java
  void setTrackUserOn(boolean on);
  ```

- **Required permissions**

  Selected Kiosk (Before 129 version), Settings

- **Support from**

  0.10.70

<br>

## Interfaces

### OnDetectionStateChangedListener

Set your context to implement this listener and add the override method to get the latest state regarding the user detection.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnDetectionStateChangedListener {}
```

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|DETECTED <a name="detected" />|int|2|When a human body is detected|
|LOST <a name="lost" />|int|1|Target detected lost|
|IDLE <a name="idle" />|int|0|No active detection occurring and 4 seconds have passed since last detection was lost|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |state|int|Detection Mode state, possible states are [`DETECTED`](#detected), [`LOST`](#lost), [`IDLE`](#idle)|

- **Prototype**

  ``` java
  abstract void onDetectionStateChanged(int state);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDetectionStateChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnDetectionStateChangedListener(OnDetectionStateChangedListener listener)
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDetectionStateChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnDetectionStateChangedListener(OnDetectionStateChangedListener listener)
  ```

#### Support from

0.10.53

---

### OnUserInteractionChangedListener

Set your context to implement this listener and add the override method to get the latest state regarding the user interaction. `isInteracting` will be returned `true` if the user is interacting otherwise it will be false. The interaction state machine will become active if one of the following condition is met:

- User is detected
- User is interacting with temi by touch or voice, or is in telepresence
- temi is in motion

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnUserInteractionChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |isInteracting|boolean|`true`(`false`) means that user is(is not) interacting|

- **Prototype**

  ``` java
  abstract void onUserInteraction(boolean isInteracting);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnUserInteractionChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnUserInteractionChangedListener(OnUserInteractionChangedListener listener)
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnUserInteractionChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnUserInteractionChangedListener(OnUserInteractionChangedListener listener)
  ```

#### Support from

0.10.53

---

### OnDetectionDataChangedListener

After the Detection Mode is turned on, data related to Detection Mode can be received in its subclass.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnDetectionDataChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |detectionData|[DetectionData](#detectionData)|Detected data|

- **Prototype**

  ``` java
  abstract void onDetectionDataChanged(DetectionData detectionData);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDetectionDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnDetectionDataChangedListener(OnDetectionDataChangedListener listener)
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDetectionDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnDetectionDataChangedListener(OnDetectionDataChangedListener listener)
  ```

#### Support from

0.10.70

<br>

## Models

Below are the list of models used in the utils methods.

### DetectionData

Used for holding the data related Detection Mode.

#### Prototype

``` java
package com.robotemi.sdk.model;

class DetectionData {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|angle|double|-|
|distance|double|The distance from temi to the detected user|
|isDetected|boolean|`true`(`false`) means temi did(did not) detect someone|
