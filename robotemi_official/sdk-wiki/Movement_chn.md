# 移动

手动控制移动实际上是temi远程控制中使用的一个方法集合，这个方法可以让用户手动控制temi的移动，下面会对这些移动做进一步详细说明。

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|void|[skidJoy(float x, float y, boolean smart)](#skidJoy)|手动操纵temi移动|
|void|[turnBy(int degrees, float speed)](#turnBy)|让temi按指定的角度转身|
|void|[tiltAngle(int degrees, float speed)](#tiltAngle)|让temi的头部倾斜到指定的角度|
|void|[tiltBy(int degrees, float speed)](#tiltBy)|让temi的头部在现在的基础上再倾斜指定的角度|
|void|[stopMovement()](#stopMovement)|停止移动|
|boolean|[patrol(List\<String> locations, boolean nonstop, int times, int waiting)](#patrol)|巡逻|

|接口|说明|
|-|-|
|[OnRobotLiftedListener](#onRobotLiftedListener)|机器人被抬起监听器|
|[OnMovementVelocityChangedListener](#onMovementVelocityChangedListener)|机器人移动速度变化监听器|
|[OnMovementStatusChangedListener](#onMovementStatusChangedListener)|机器人移动状态变化监听器（skidJoy，turnBy）|

<br>

## 方法

### skidJoy()

用这个方法以与视频通话中类似的方式手动让temi在其轴线移动。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |x|float|线速度，取值范围为 -1 ~ 1|
  |y|float|角速度，取值范围为 -1 ~ 1|
  |smart|boolean|移动时是否自动绕过障碍，为 **0.10.79** 版本新增参数|

- **原型**

  ``` java
  void skidJoy(float x, float y, boolean smart);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

- **推荐**

  - 使用下图可以更好的理解temi的坐标系。
  - 参数最好使用 1 或者 -1。
  - 为了获得最佳效果，建议在每个命令之间至少留出500ms的缓冲时间
  - 因为后部传感器较少，为了降低机器损坏的风险，后退时会比前进时的速度小。

  ![](https://github.com/robotemi/sdk/blob/master/images/Unknown.png)

- **示例**

  - 前进 - robot.skidJoy(1,0) 持续调用直到前进至你想要它直线到达的位置。
  - 向左转 - robot.skidJoy(0,1) 持续调用直到转到你想要的方位。

---

### turnBy()

用这个方法让temi转身指定的角度。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |degrees|int|你要temi转身的角度。向 左 转用正（+）角度值, 向 右 转用负（-）角度值。|
  |speed|float|【可选参数】最大转动速度的系数，取值范围为 0 ~ 1。从 0.10.77 开始支持。|

- **原型**

  ``` java
  void turnBy(int degrees, float speed);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### tiltAngle()

用这个方法可以将temi的头部倾斜到一个指定的角度（绝对角度）。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |degrees|int|你要temi的头部倾斜的角度，取值范围为 -30 ~ 50|
  |speed|float|【可选参数】最大转动速度的系数，取值范围为 0 ~ 1。从 0.10.77 开始支持。|

- **原型**

  ``` java
  void tiltAngle(int degrees, float speed);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

- **注意**

  - temi头部的倾斜角度取值范围为 -30 ~ 55，-30度意味着temi会一直向下看，+55度则意味着temi会一直向上看。
  - 0度则意味着temi会直视前方。

---

### tiltBy()

用这个方法可以让temi的头部在当前的角度基础上再倾斜指定的角度（相对角度）。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |degrees|int|你要temi继续倾斜的角度值|
  |speed|float|【可选参数】最大转动速度的系数，取值范围为 0 ~ 1。从 0.10.77 开始支持。|

- **原型**

  ``` java
  void tiltBy(int degrees, float speed);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

- **注意**

  - temi头部的倾斜角度取值范围为 -30 ~ 50，-30度意味着temi会一直向下看，+50度则意味着temi会一直向上看。
  - 0度则意味着temi会直视前方。
  - 建议用这个方法进行较小角度的调整，而不要进行较大的角度调整。如果一定要进行大角度调整请使用 [tiltAngle()](#tiltAngle) 方法代替。

---

### stopMovement()

用这个方法让temi停止移动。

- **原型**

  ``` java
  void stopMovement();
  ```

- **注意**

  - 可以通过这个方法终止上述的任何移动动作。
  - 也可以通过这个方法来终止 [goTo()](#goTo) 命令。

---

### patrol()

巡逻

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |locations|List\<String>|至少传入 3 个地点，可以重复，home base 充电桩不可以作为巡逻地点，会被忽略|
  |nonstop|boolean|【可选参数】如果设置为 true，巡逻过程不会在到达地点后停顿，及旋转、抬头低头至地点对应角度，而是立即前往下一个地点。默认为 false|
  |times|int|巡逻的次数，0 为一直重复巡逻，1为巡逻一次，依次类推|
  |waiting|int|【可选参数】如果 [nonstop] 是 false, waiting 可以控制在每个地点停留的时长，范围在 3 - 60 之间，单位为秒。 默认为 3|

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 表示巡逻成功执行|

- **原型**

  ``` java
  boolean patrol(List<String> locations, boolean nonstop, int times, int waiting);
  ```

- **所需权限**

  无.

- **最小支持版本**

  1.129.1

<br>

## 接口

### OnRobotLiftedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以感知机器人是否被抬起。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnRobotLiftedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isLifted|boolean|`true` 表示机器人被抬起，否则为 `false`|
  |reason|String|目前暂无内容，预留用|

- **原型**

  ``` java
  void onRobotLifted(boolean isLifted, String reason);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRobotLiftedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnRobotLiftedListener(OnRobotLiftedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRobotLiftedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnRobotLiftedListener(OnRobotLiftedListener listener);
  ```

- **最小支持版本**

  0.10.70

---

### OnMovementVelocityChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以监听机器人移动的速度变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnMovementVelocityChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |velocity|float|移动速度，单位为米/秒|

- **原型**

  ``` java
  void onMovementVelocityChanged(float velocity);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnMovementVelocityChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnMovementVelocityChangedListener(OnMovementVelocityChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnMovementVelocityChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnMovementVelocityChangedListener(OnMovementVelocityChangedListener listener);
  ```

- **最小支持版本**

  0.10.77

---

### OnMovementStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以监听机器人移动的状态变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnMovementStatusChangedListener {}
```

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|TYPE_SKID_JOY|String|"skidJoy"|移动类型 - skidJoy|
|TYPE_TURN_BY|String|"turnBy"|移动类型 - turnBy|
|STATUS_START|String|"start"|移动开始|
|STATUS_GOING|String|"going"|移动进行中|
|STATUS_OBSTACLE_DETECTED|String|"obstacle detected"|检测到障碍物|
|STATUS_NODE_INACTIVE|String|"node inactive"|节点无效|
|STATUS_CALCULATING|String|"calculating"|移动规划计算中|
|STATUS_COMPLETE|String|"complete"|移动结束|
|STATUS_ABORT|String|"abort"|移动中断|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |type|String|移动类型，目前有 ”skidJoy“ 和 ”turnBy“|
  |status|String|移动状态|

- **原型**

  ``` java
  void onMovementStatusChanged(String type, String status);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnMovementStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnMovementStatusChangedListener(OnMovementStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnMovementStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnMovementStatusChangedListener(OnMovementStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.77
