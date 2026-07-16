
# 跟随

Be With Me 又名跟随模式，是让temi去搜索站在他前面的人，并锁定然后开始跟随人体的移动。通常，用户会轻拍temi的顶部硬件按钮或语音指令让temi跟随自己，当然你也可以通过使用SDK，用编程的方式触发此功能。temi也可以有限制性跟随，在这种情况下temi的跟随只局限于转身（不在充电桩上时）和头部倾斜而没有其他移动。

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|void|[beWithMe()](#beWithMe)|开启跟随模式|
|void|[constraintBeWith()](#constraintBeWith)|开启限制性跟随模式|

|接口|说明|
|-|-|
|[OnBeWithMeStatusChangedListener](#onBeWithMeStatusChangedListener)|跟随模式状态变化监听器|
|[OnConstraintBeWithStatusChangedListener](#onConstraintBeWithStatusChangedListener)|限制性跟随模式状态变化监听器|

<br>

## 方法

### beWithMe()

用这个方法可以手动调用跟随模式。跟随模式是一种状态，在这种状态下temi会搜索站在其前方的人，一旦搜索到便锁定并开始跟随，直到temi收到终止跟随的指令。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |speedLevel|SpeedLevel|控制跟随速度，`null` 则使用系统默认配置，1.135.1 版本加入，配合 135 Launcher 使用|

- **原型**

  ``` java
  void beWithMe(SpeedLevel speedLevel);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### constraintBeWith()

用这个方法可以手动调用限制性跟随模式。 约束跟随模式是一种状态，在这种状态下，temi搜索站在它前面的人，一旦发现，它就会锁定他们的移动，直到收到其他指令。与常规跟随模式不同，在限制性跟随模式下，temi只在当前的位置上倾斜头部和转身而不离开当前位置。

- **原型**

  ``` java
  void constraintBeWith();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.53

<br>

## 接口

### OnBeWithMeStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取跟随过程中的最新状态信息。跟随模式中可能的状态为（括号中的值为返回的实际字符串值）。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnBeWithMeStatusChangedListener {}
```

#### 静态常量 <a name="staticConstantOnBeWithMeStatus" />

这里的常量均为跟随模式状态。

|常量|类型|值|说明|
|-|-|-|-|
|ABORT|String|"abort"|用户或 temi 中止跟随指令|
|CALCULATING|String|"calculating"|temi 被障碍物挡住并试图计算出绕过障碍物的路线|
|SEARCH|String|"search"|跟随模式开启并正在搜索要跟随的人体|
|START|String|"start"|temi 找到要跟随的人并开始跟随|
|TRACK|String|"track"|temi 正在跟随|
|OBSTACLE_DETECTED|String|"obstacle detected"|temi 检测到障碍物|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|String|跟随模式中的[状态](#staticConstantOnBeWithMeStatus)|

- **原型**

  ``` java
  void onBeWithMeStatusChanged(String status);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnBeWithMeStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnBeWithMeStatusChangedListener(OnBeWithMeStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnBeWithMeStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnBeWithMeStatusChangedListener(OnBeWithMeStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### OnConstraintBeWithStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取限制性跟随过程中的最新状态信息。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnConstraintBeWithStatusChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isConstraint|boolean|是否正在限制性跟随|

- **原型**

  ``` java
  void onConstraintBeWithStatusChanged(boolean isConstraint);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnConstraintBeWithStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnConstraintBeWithStatusChangedListener(OnConstraintBeWithStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnConstraintBeWithStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnConstraintBeWithStatusChangedListener(OnConstraintBeWithStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.49
