
# Follow

Be With Me AKA Follow Mode tells temi to search for a person that is standing next to it, lock on and then follow it's movement. Generally, a user would click on temi's top hard button or verbally ask temi to follow them, however, with the SDK you can trigger this feature programatically. temi also has the option to constraint follow, in this case temi tracks only on its axis using turn and tilt but with no movement.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|void|[beWithMe()](#beWithMe)|Iitiate follow mode|
|void|[constraintBeWith()](#constraintBeWith)|Iitiate constraint follow mode|

|Interface|Description|
|-|-|
|[OnBeWithMeStatusChangedListener](#onBeWithMeStatusChangedListener)|Listener for follow mode status|
|[OnConstraintBeWithStatusChangedListener](#onConstraintBeWithStatusChangedListener)|Listener for constraint follow mode status|

<br>

## Methods

### beWithMe()

Use this method to manually invoke the follow mode. Follow mode is the state where temi searches for a person standing in front of it and once found it locks on and follows their movement until told otherwise.

From 1.135.1, this method can take SpeedLevel as parameter to control follow speed

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |speedLevel|SpeedLevel|control the follow speed, set `null` to use system settings, added in 1.135.1 SDK, supported by 135 Launcher|

- **Prototype**

  ``` java
  void beWithMe(SpeedLevel speedLevel);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### constraintBeWith()

Use this method to manually invoke the constraint follow mode. Constraint Follow mode is the state where temi searches for a person standing in front of it and once found it locks on to their movement until told otherwise. Unlike the regular follow in constraint mode temi only tilts and turns on its' axis it does not leave its' position.

- **Prototype**

  ``` java
  void constraintBeWith();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.53

<br>

## Interfaces

### OnBeWithMeStatusChangedListener

Set your context to implement this listener and add the override method to get the latest status regarding the follow mode. Possible statuses for follow mode are (value in parenthesis is the actual string value that will be returned).

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnBeWithMeStatusChangedListener {}
```

#### Static constants <a name="staticConstantOnBeWithMeStatus" />

All constants here are only for the status of following.

|Constant|Type|Value|Description|
|-|-|-|-|
|ABORT|String|"abort"|When the user or temi aborts the follow action|
|CALCULATING|String|"calculating"|When temi gets stuck following due to an obstacle and is trying to figure its' way around it|
|SEARCH|String|"search"|Follow mode is triggered and temi is looking for a person to follow|
|START|String|"start"|temi temi has found a person and following has began|
|TRACK|String|"track"|temi is following|
|OBSTACLE_DETECTED|String|"obstacle detected"|temi detected obstacles|

#### Abstract methods

- **Parameters**

  |Parameters|Type|Description|
  |-|-|-|
  |status|String|[Status](#staticConstantOnBeWithMeStatus) of following mode|

- **Prototype**

  ``` java
  void onBeWithMeStatusChanged(String status);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnBeWithMeStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnBeWithMeStatusChangedListener(OnBeWithMeStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnBeWithMeStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnBeWithMeStatusChangedListener(OnBeWithMeStatusChangedListener listener);
  ```

- **Support from**

  0.10.36

---

### OnConstraintBeWithStatusChangedListener

Set your context to implement this listener and add the override method to get the latest status regarding the constraint follow mode. There are two possible statuses for the constraint follow mode.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnConstraintBeWithStatusChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameters|Type|Description|
  |-|-|-|
  |isConstraint|boolean|In constraint follow mode or not|

- **Prototype**

  ``` java
  void onConstraintBeWithStatusChanged(boolean isConstraint);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnConstraintBeWithStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnConstraintBeWithStatusChangedListener(OnConstraintBeWithStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnConstraintBeWithStatusChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnConstraintBeWithStatusChangedListener(OnConstraintBeWithStatusChangedListener listener);
  ```

- **Support from**

  0.10.49
