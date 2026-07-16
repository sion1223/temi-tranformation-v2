# Movement

Movement control or in the case of the SDK manual movement control, is essentially a set of methods used in temi's telepresence that allow the user to manually control temi's movement. These movements are explained in further details below.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|void|[skidJoy(float x, float y, boolean smart)](#skidJoy)|Manually move temi|
|void|[turnBy(int degrees, float speed)](#turnBy)|Turn by by a specific degree|
|void|[tiltAngle(int degrees, float speed)](#tiltAngle)|Tilt temi's head to a specific angle|
|void|[tiltBy(int degrees, float speed)](#tiltBy)|Tilt temi's head to by a specific degree|
|void|[stopMovement()](#stopMovement)|Stop all movement|
|boolean|[patrol(List\<String> locations, boolean nonstop, int times, int waiting)](#patrol)|Patrol|

|Interface|Description|
|-|-|
|[OnRobotLiftedListener](#onRobotLiftedListener)|Robot lifted listener|
|[OnMovementVelocityChangedListener](#onMovementVelocityChangedListener)|Velocity changed of movement listener|
|[OnMovementStatusChangedListener](#onMovementStatusChangedListener)|Status changed listener of movement(skidJoy, turnBy) listener|

<br>

## Methods

### skidJoy()

Use this method to manually navigate temi on its' axis in a similat fashion to the way it's done in the video call.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |x|float|float value of distance to cover on temi's x axis. The range of values is from 1-(-)1, where 1 is a complete step forward and -1 is complete step backwards on the axis.|
  |y|float|float value of distance to cover on temi's y axis. The range of values is from 1-(-)1, where 1 is a complete step forward and -1 is complete step backwards on the axis.|
  |smart|boolean|Bypass obstacles while moving, supported from version **0.10.79**|

- **Prototype**

  ``` java
  void skidJoy(float x, float y);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

- **Recommendation**

  - Use the graph below to better understand temi's axis.
  - It's best to use the values 1 and -1 as since values in between will not be noticeable to the untrained eye.
  - It's recommended to give a buffer of at least 500 milliseconds between each command for the best outcome.
  - Backwards speed is lower than forward's since there are less sensors and we want to reduce the risk of damage.

  ![](https://github.com/robotemi/sdk/blob/master/images/Unknown.png)

- **Examples**

  - Forward - robot.skidJoy(1,0) for how long as you want it to move forward.
  - Left - robot.skidJoy(0,1) until the robot is facing the direction you want it to move to

---

### turnBy()

Use this method to manually turn temi's body by a certain degree.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |degrees|int|The degrees you want to temi's body to turn.|
  |speed|float|[Optional parameter] The coefficient of the maximum speed, the value range is 0~1. Support from 0.10.77.|

- **Prototype**

  ``` java
  void turnBy(int degrees, float speed);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### tiltAngle()

Use this method to manually tilt temi's head to a certain degree.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |degrees|int|The degrees you want to temi's head to tilt to.|
  |speed|float|[Optional parameter] The coefficient of the maximum speed, the value range is 0~1. Support from 0.10.77.|

- **Prototype**

  ``` java
  void tiltAngle(int degrees, float speed);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

- **Notes**

  - Take into account that the tilt range is from **-30** degrees which means temi is looking all the way down until **+50** degrees which means it's looking all the way up.
  - 0 degrees means temi is looking straight ahead.

---

### tiltBy()

Use this method to manually tilt temi's head by a certain degree.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |degrees|int|The amount of degrees you want to tilt by|
  |speed|float|[Optional parameter] The coefficient of the maximum speed, the value range is 0~1. Support from 0.10.77.|

- **Prototype**

  ``` java
  void tiltBy(int degrees, float speed);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

- **Notes**

  - Take into account that the tilt range is from **-30** degrees which means temi is looking all the way down until **+50** degrees which means it's looking all the way up.
  - 0 degrees means temi is looking straight ahead.
  - It's recommended to use this method to make small tilt adjustments and not big ones, if you want temi to make bigger adjustments use [tiltAngle()](#tiltAngle) instead.

---

### stopMovement()

Use this method to manually stop temi from moving.

- **Prototype**

  ``` java
  void stopMovement();
  ```

- **Notes**

  - Use this method to abort any of the actions taken by the methods above.
  - Use this method to stop navigation commands as well.

---

### patrol()

Use this method to patrol between locations.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |locations|List\<String>|at least 3 valid locations, can be duplicated. Home base will be ignored and should not be included.|
  |nonstop|boolean|[Optional parameter] if set as true, it will just arrive at the position of location, without tilt and turn, and then immediately head to next location. default as false|
  |times|int|[how many times should it go on the route. 0: infinite, 1: once, and so on.|
  |waiting|int|[Optional parameter] If [nonstop] is false, the time in seconds it should wait on each location. Range from 3 - 60, default is 3|

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if patrol is executed.|

- **Prototype**

  ``` java
  boolean patrol(List<String> locations, boolean nonstop, int times, int waiting);
  ```

- **Required permissions**

  None.

- **Support from**

  1.129.1

<br>

## Interfaces

### OnRobotLiftedListener

Set your context to implement this listener and add the override method to perceive whether the robot is lifted.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnRobotLiftedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |isLifted|boolean|`true` means robot is lifted, `false` otherwise|
  |reason|String|-|

- **Prototype**

  ``` java
  void onRobotLifted(boolean isLifted, String reason);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRobotLiftedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnRobotLiftedListener(OnRobotLiftedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRobotLiftedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnRobotLiftedListener(OnRobotLiftedListener listener);
  ```

- **Support from**

  0.10.70

---

### OnMovementVelocityChangedListener

Set your context to implement this listener and add the override method to listen to the velocity changes of movement.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnMovementVelocityChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |velocity|float|Velocity of movement, the unit is m/s|

- **Prototype**

  ``` java
  void onMovementVelocityChanged(float velocity);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnMovementVelocityChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnMovementVelocityChangedListener(OnMovementVelocityChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnMovementVelocityChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnMovementVelocityChangedListener(OnMovementVelocityChangedListener listener);
  ```

- **Support from**

  0.10.77

---

### OnMovementStatusChangedListener

Set your context to implement this listener and add the override method to listen to the status changes of movement.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnMovementStatusChangedListener {}
```

#### Static constant

|Constant|Type|Value|Description|
|-|-|-|-|
|TYPE_SKID_JOY|String|"skidJoy"|Movement type - skidJoy|
|TYPE_TURN_BY|String|"turnBy"|Movement type - turnBy|
|STATUS_START|String|"start"|Movement started|
|STATUS_GOING|String|"going"|Moving|
|STATUS_OBSTACLE_DETECTED|String|"obstacle detected"|Obstacle detected during the movement|
|STATUS_NODE_INACTIVE|String|"node inactive"|Node inactive|
|STATUS_CALCULATING|String|"calculating"|Calculating|
|STATUS_COMPLETE|String|"complete"|Movement completed|
|STATUS_ABORT|String|"abort"|Movement abort|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |type|String|Movement type|
  |status|String|Movement status|

- **Prototype**

  ``` java
  void onMovementStatusChanged(String type, String status);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnMovementStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnMovementStatusChangedListener(OnMovementStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnMovementStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnMovementStatusChangedListener(OnMovementStatusChangedListener listener);
  ```

- **Support from**

  0.10.77
