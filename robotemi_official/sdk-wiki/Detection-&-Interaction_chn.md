# 人体检测与用户交互

用户检测和用户交互是两个状态机，这两个状态机允许我们监听当一个人体是否被检测到以及用户是否正在和temi进行交互。

temi要开启欢迎模式才能使用这些状态机，请参考如下的集成说明。在欢迎模式中，temi将：

- 不断地搜寻用户
- 在RGB相机框内0.8米的距离可以被识别
- 如果前方有1位以上的用户，则选择距离最近和在最中心的用户

如果Launcher处于没有用户交互和移动的状态一段时间后Launcher会返回桌面，这时temi将启动欢迎模式，并且你的技能可以通过此功能监听欢迎模式的状态事件，并根据这些事让temi做你想要它做的的事情。

有关检测和交互状态机的更多信息，请参阅下面的监听器部分。

**提示: 我们已经开发了一个可以满足大多数B2B需求的 [欢迎模式B2B](https://github.com/ROBOTEAM-HOME/temi-welcomingmode-b2b)，这个App可以为任何想用欢迎模式能力开发App的开发者提供一个比较好的参考。**

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|boolean|[isDetectionModeOn()](#isDetectionModeOn)|检查用户检测模式是否已打开|
|void|[setDetectionModeOn(boolean on)](#setDetectionModeOn)|打开（关闭）用户检测模式|
|void|[setDetectionModeOn(boolean on, float distance)](#setDetectionModeOnWithDistance)|打开用户检测模式并设置最大检测距离|
|boolean|[isTrackUserOn](#isTrackUserOn)|检查原地跟随是否以打开|
|void|[setTrackUserOn(boolean isOn)](#setTrackUserOn)|打开（关闭）原地跟随|

|接口|说明|
|-|-|
|[OnDetectionStateChangedListener](#onDetectionStateChangedListener)|用户检测状态变化监听器|
|[OnUserInteractionChangedListener](#onUserInteractionChangedListener)|用户交互状态变化监听器|
|[OnDetectionDataChangedListener](#onDetectionDataChangedListener)|用户检测数据变化监听器|

|模型|说明|
|-|-|
|[DetectionData](#detectionData)|用户检测模式下的检测数据|

<br>

## 集成

要在你的技能中集成欢迎模式能力，那么需要完成以下步骤：

1. 首先你的技能必须是 [Kiosk 模式](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn) 技能。请从 [这里](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn) 学习如何配置 [Kiosk 模式](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn) 技能。

2. 回到temi并打开 *设置 > 通用设定 > 打开原地跟随开关*。

3. 返回桌面。

4. 编译并运行你的技能。

<br>

## 更新

- ### 126版本（Launcher 版本 15567-chinaTencent）

  **如果[原地跟随](#setTrackUserOn)已开启，[关闭用户检测模式](#setDetectionModeOn)将失效。**

- ### 0.10.77 更新用户检测模式与原地跟随的关系 <a name="detectionModeAndTrackUser" />

  [打开用户检测模式](#setDetectionModeOn)，不会同时打开[原地跟随](#setTrackUserOn)。~~关闭用户检测模式，原地跟随也将被关闭。~~ 开（关）原地跟随的同时也会开（关）用户检测模式。

  |操作 \ 结果|用户检测模式|原地跟随|
  |-|-|-|
  |打开用户检测模式|打开|-|
  |关闭用户检测模式|关闭|~~关  闭~~|
  |打开原地跟随|打开|打开|
  |关闭原地跟随|关闭|关闭|

<br>

## 方法

### isDetectionModeOn()

查看用户检测模式是否已打开，默认为关闭。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示用户检测模式已打开（关闭）|

- **原型**

  ``` java
  boolean isDetectionModeOn();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setDetectionModeOn()

用这个方法来打开（关闭）用户检测模式，用这个方法来打开用户检测模式的最大检测距离为默认的 **0.8** 米。**如果当前出于迎宾模式，这个方法将失效**。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|传入 `true`（`false`）打开（关闭）用户检测模式|

- **原型**

  ``` java
  void setDectionModeOn(boolean on);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### setDetectionModeOn() <a name="setDetectionModeOnWithDistance" />

用这个方法来打开（关闭）用户检测模式，用这个方法来打开用户检测模式可以同时设置其最大的检测距离。**如果当前处于迎宾模式，这个方法将失效**。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|传入 `true`（`false`）打开（关闭）用户检测模式|
  |distance|float|用户检测模式所能检测的最大距离（仅当 `on` 为`true` 时起作用）。有效的取值范围为 **0.5~2.0** 米。|

- **原型**

  ``` java
  void setDectionModeOn(boolean on, float distance);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

---

### isTrackUserOn()

查看原地跟随是否已打开，默认为关闭。

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true（false）表示原地跟随已打开（关闭）|

- **原型**

  ``` java
  boolean isTrackUserOn();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### setTrackUserOn()

用这个方法来打开（关闭）原地跟随。原地跟随指在回到无用户交互状态后，开启用户人体检测，在检测到用户后开启原地跟随，并在顶部显示绿色的欢迎词徽章。也可在 *设置 > 通用设定 > 原地跟随* 进行打开（关闭）。**如果当前出于迎宾模式，这个方法将失效**。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|boolean|传入 `true`（`false`）打开（关闭）原地跟随|

- **原型**

  ``` java
  void setTrackUserOn(boolean on);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>设置

- **最小支持版本**

  0.10.70

<br>

## 接口

### OnDetectionStateChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取用户检测事件的最新状态信息。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnDetectionStateChangedListener {}
```

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|DETECTED <a name="detected" />|int|2|检测到人体|
|LOST <a name="lost" />|int|1|检测到的人体目标丢失|
|IDLE <a name="idle" />|int|0|丢失目标后，十秒内未检测到人体|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |state|int|用户检测状态，可能的值为 [`DETECTED`](#detected), [`LOST`](#lost), [`IDLE`](#idle)|

- **原型**

  ``` java
  abstract void onDetectionStateChanged(int state);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDetectionStateChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnDetectionStateChangedListener(OnDetectionStateChangedListener listener)
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDetectionStateChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnDetectionStateChangedListener(OnDetectionStateChangedListener listener)
  ```

#### 最小支持版本

0.10.53

---

### OnUserInteractionChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取用户交互事件的最新状态信息。如果满足以下条件之一，用户交互状态机将激活：

- 检测到用户
- 用户正在通过触摸屏幕或语音与temi交互，或正在进行远程控制（视频通话）
- temi正在行动中

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnUserInteractionChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isInteracting|boolean|`true` 表示正在交互，`false` 反之|

- **原型**

  ``` java
  abstract void onUserInteraction(boolean isInteracting);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnUserInteractionChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnUserInteractionChangedListener(OnUserInteractionChangedListener listener)
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnUserInteractionChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnUserInteractionChangedListener(OnUserInteractionChangedListener listener)
  ```

#### 最小支持版本

0.10.53

---

### OnDetectionDataChangedListener

在用户检测模式打开后，可接收到用户检查相关的数据。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnDetectionDataChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |detectionData|[DetectionData](#detectionData)|用户检测模式的数据|

- **原型**

  ``` java
  abstract void onDetectionDataChanged(DetectionData detectionData);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDetectionDataChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnDetectionDataChangedListener(OnDetectionDataChangedListener listener)
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDetectionDataChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnDetectionDataChangedListener(OnDetectionDataChangedListener listener)
  ```

#### 最小支持版本

0.10.70

<br>

## 模型

下面是有关上述监听器用到的模型详细信息。

### DetectionData

用于保存用户检测相关的数据。

#### 原型

``` java
package com.robotemi.sdk.model;

class DetectionData {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|angle|double|-|
|distance|double|temi 到人体的距离|
|isDetected|boolean|是否检测到人体，`true` 表示检测到，`false` 反之|
