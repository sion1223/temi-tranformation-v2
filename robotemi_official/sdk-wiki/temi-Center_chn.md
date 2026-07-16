# temi 管理平台相关

这里的特性都需要提前在 [temi 管理平台](https://center.robotemi.cn/) 进行配置之后才能使用。


### 导览

导览（Sequence）是可以经过简单的图形化界面的配置之后，即可是实现强大的链式操作的工具。

用户可以在 temi 管理平台上创建导览，通过点击，拖拽，拼接，将一系列移动，说话，视频，图片等动作串联在一起，实现如先移动到某地点，同时进行语音播报，到达地点后，展示视频图片等等复杂链式操作。

导览可用于多种场景，首页一键播放、定时任务，语音触发、事件触发等等。

通过 SDK，可以获取、播放属于组织资源的导览数据，并控制其播放进程。

### 讲解

讲解是将导览、问答等资源与地点结合起来后的功能。

它适用于展厅等场景，让 temi 扮演一个讲解员，带领访客、观众沿路线参观展厅。在每个地点、地点之间播放导览、提供问答讲解。

你可以在 temi center 创建讲解。首先创建一条讲解路线，然后附加上导览等资源，最后可以自定义讲解过程中的各个阶段的 UI。

### 迎宾模式

迎宾模式（Greet Mode）是将多个软硬件功能集成在一起之后提供的一个集合了检测
识别、迎接、引导等流程的功能模块。

用户同样可以在 temi 管理平台上经过简单的配置，为迎宾的检测、迎接、引导、结束等环节配置相应的参数及播放指定导览，实现强大的迎宾功能。

通过 SDK，可以监听迎宾模式的状态切换，对开发者来说通常是配合 Kiosk 模式的首页应用，在 temi 管理平台上关闭不需要的迎宾配置项，仅保留基础的检测逻辑，让开发者不需要写复杂的接入实现，即可以为自己的应用加入全程保持在应用前台运行，并能实时感知状态的迎宾功能。


### 人脸识别

人脸识别是一个注册、检测、识别的过程的集合，可以在迎宾模式中开启人脸识别实现 VIP 迎宾，也可以通过 SDK 单独调用识别。

129 版本起，在迎宾模式下，对于识别到的未注册的用户会提供更多的回调数据。包括在多轮迎宾下都保持一致的 UUID，可用于首次来访、再次来访等场景。

未注册的用户数据最大容量为 200 条，每次开机会清空。如果夜间 12 点仍在运行没有处于迎宾模式，也会清除未注册的用户数据。

#### 本地注册人脸识别

从 1.131.2 版本开始，除了通过 temi center 创建联系人添加人脸识别照片的方式之外，应用本身可以通过 SDK 本地注册人脸。既，应用通过 SDK 传入一张照片，并提供用户 id，姓名等信息，即可完成本地人脸注册。
通过这种方式注册的人脸仅会用于当前应用所启动的人脸识别，而不会在迎宾模式中的第二阶段的人脸识别中生效。

本地注册的人脸图像仅会被算法处理转换成匿名的、不可逆的特征数据，图片本身不会被存储或用于特征提取之外的用途。

本地人脸注册只需要做一次，之后调用人脸识别功能就回生效，无须每次启动识别之前反复注册。

如果此前注册过本地人脸信息，当应用启动启动人脸识别后，如本地注册的人脸匹配结果优于 temi center 端注册的联系人，将把本地注册的人脸用户作为人脸识别的结果返回给应用。

本地人脸注册和管理的过程全部通过 content provider 实现。可以参考 [FaceActivity](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/FaceActivity.kt) 中的例子。这一页面也可以在 SDK sample 中的 RESOURCES -> Test Face Recognition 的路径启动。

更多信息，请查看页面 [本地人脸注册](https://github.com/robotemi/sdk/wiki/LocalFace_chn)

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|void|[startFaceRecognition()](#startFaceRecognition)|开始人脸识别|
|void|[stopFaceRecognition()](#stopFaceRecognition)|停止人脸识别|
|List\<[SequenceModel](#sequenceModel)\>|[getAllSequences(List\<String\>)](#getAllSequences)|获取所有导览数据|
|void|[playSequence(String sequenceId, boolean withPlayer, int repeat, int startFromStep)](#playSequenceWithPlayer)|播放指定导览|
|InputStream|[getInputStreamByMediaKey(ContentType contentType, String mediaKey)](#getInputStreamByMediaKey)|根据 `contentType` 和 `mediaKey` 获取文件的输入流（目前仅用于获取人脸识别的联系人照片）|
|List<Pair<String, String>>|[getSignedUrlByMediaKey(List\<String\> mediaKeys, int width, int height)](#getSignedUrlByMediaKey)|根据 mediaKeys、宽、高获取多个媒体资源的签名 URL|
|void|[controlSequence(SequenceCommand sequenceCommand)](#controlSequence)|控制当前正在播放的导览|
|int|[setInteractionState(Boolean on)](#setInteractionState)|使迎宾模式保持在第三阶段互动状态|
|List\<[TourModel](#tourmodel)\>|[getAllTours()](#getAllTours)|获取所有讲解数据|
|int|[playTour(String tourId)](#playTour)|播放指定讲解|


|接口|说明|
|-|-|
|[OnSequencePlayStatusChangedListener](#onSequencePlayStatusChangedListener)|导览播放状态变化监听器|
|[OnFaceRecognizedListener](#onFaceRecognizedListener)|人脸识别监听器|
|[OnContinuousFaceRecognizedListener](#onContinuousFaceRecognizedListener)|连续人脸识别结果监听器|
|[OnGreetModeStateChangedListener](#onGreetModeStateChangedListener)|迎宾模式状态变化监听器|

|模型|说明|
|-|-|
|[ContactModel](#contactModel)|联系人数据|
|[SequenceModel](#sequenceModel)|导览数据|
|[ContentType](#contentType)|内容类型|
|[SequenceCommand](#sequenceCommand)|导览控制命令|
|[TourModel](#tourmodel)|讲解数据|

<br>

## 方法

### startFaceRecognition()

用这个方法来开启 temi 的人脸识别，并在识别到人脸后将人脸数据回传到 [OnFaceRecognizedListener](#onFaceRecognizedListener) 的回调方法中。

- **原型**

  ``` java
  void startFaceRecognition();
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>人脸识别

- **最小支持版本**

  0.10.70

---

### stopFaceRecognition()

用这个方法来停止 temi 的人脸识别服务。

- **原型**

  ``` java
  void stopFaceRecognition();
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>人脸识别

- **最小支持版本**

  0.10.70

---

### getAllSequences()

用这个方法来获取所有在 [temi 管理平台](https://center.robotemi.cn/) 中配置过的所属组织的导览。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |tags|List\<String\>|导览标签（在 temi 管理平台中配置），可选参数，不传入则返回所有所属组织下的导览|

- **返回值**

  |类型|说明|
  |-|-|
  |List\<[SequenceModel](#sequenceModel)\>|所有的导览集合|

- **原型**

  ``` java
  List<SequenceModel> getAllSequences();
  ```

- **所需权限**

  导览

- **最小支持版本**

  0.10.70

- **注意**

  这个方法是耗时操作，建议在非主线程中使用。

---

### playSequence() <a name="playSequenceWithPlayer" />

用这个方法来获播放一个指定的导览，并可选择是否显示导览播放器控制界面，播放器控制界面可对导览进行暂停、播放、上一步、下一步操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |sequenceId|String|导览ID|
  |withPlayer|boolean|是否显示播放器操作界面|
  |repeat|int|重复次数|
  |startFromStep|int|开始播放的步骤, 从 1 开始. 1.136.0 版本新增|
  

- **原型**

  ``` java
  void playSequence(String sequenceId, boolean withPlayer, int repeat, int startFromStep);
  ```

- **所需权限**

  导览

- **最小支持版本**

  0.10.73

---

### getInputStreamByMediaKey()

用这个方法来根据传入的 `contentType` 和 `mediaKey` 获取文件的输入流，目前用于获取人脸识别中的联系人照片文件。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |contentType|[ContentType](#contentType)|内容类型|
  |mediaKey|String|文件的媒体资源的 key|

- **返回值**

  |类型|说明|
  |-|-|
  |InputStream|文件的输入流|

- **原型**

  ``` java
  InputStream getInputStreamByMediaKey(ContentType contentType, String mediaKey);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

- **注意**

  这个方法是耗时操作，建议在非主线程中使用。

---

### getSignedUrlByMediaKey()

用这个方法来根据传入的 `mediaKeys` 、`width`、`height` 来获取对应媒体资源的签名URL。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mediaKeys|List\<String\>|待获取的媒体资源的 key 列表|
  |width|int|媒体（图片）的宽，不传则返回原资源|
  |height|int|媒体（图片）的高，不传则返回原资源|

- **返回值**

  |类型|说明|
  |-|-|
  |List<Pair<String, String>>|mediaKey 以及其对应的签名URL键值对列表|

- **原型**

  ``` java
  List<Pair<String, String>> getSignedUrlByMediaKey(List<String> mediaKeys, int width, int height);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.77

- **注意**

  这个方法是耗时操作，建议在非主线程中使用。

---

### controlSequence()

用这个方法来控制当前正在播放的导览可对导览进行停止、播放、暂停、上一步、下一步操作。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |sequenceCommand|[SequenceCommand](#sequenceCommand)|控制命令|

- **原型**

  ``` java
  void controlSequence(SequenceCommand sequenceCommand);
  ```

- **所需权限**

  作为选中的 Kiosk（小于 129 版本）<br>导览

- **最小支持版本**

  0.10.78

---

### setInteractionState()

使迎宾模式保持在第三阶段互动状态。

130 版本之前，迎宾模式的互动状态全部由内部逻辑控制，如机器人是否在移动，语音互动等等。

130 版本后，通过这个方法，SDK 可以控制迎宾模式保持在互动状态。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |on|Boolean|true 使迎宾模式保持为互动状态|

- **返回值**

  |类型|说明|
  |-|-|
  |int|-1 请求失败，可能 SDK 未初始化完成<br>0 成功<br>403 当前应用不是所选主页应用|

- **原型**

  ``` java
  int setInteractionState(Boolean on);
  ```

- **所需权限**

  作为选中的主页应用.

- **最小支持版本**

  1.130.0

---

### getAllTours()

获取讲解列表

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |tags|List\<String\>|讲解标签（在 temi 管理平台中配置），可选参数，不传入则返回所有机器人的讲解|

- **返回值**

  |类型|说明|
  |-|-|
  |List\<[TourModel](#tourmodel)\>|讲解列表|

- **原型**

  ``` java
  List<TourModel> getAllTours();
  ```

- **所需权限**

  Sequence

- **最小支持版本**

  1.132.0

- **注意**

  这个方法是耗时操作，建议在非主线程中使用。

---

### playTour()

启动指定讲解

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |tourId|String|讲解id|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 成功, 403 无权限, 404 找不到对应讲解, -1 方法不支持或其它错误|

- **原型**

  ``` java
  int playTour(String tourId);
  ```

- **所需权限**

  Sequence

- **最小支持版本**

  1.132.0

<br>

## 接口

### OnFaceRecognizedListener

让你的上下文实现这个监听器接口，并重写相关方法以获取识别到的人脸对应的联系人信息。当识别到的人脸已经在 [temi 管理平台](https://center.robotemi.cn/) 中配置过时，会返回所对应的[联系人](#contactModel)的其他信息；而如果未在 [temi 管理平台](https://center.robotemi.cn/) 配置过，则会返回空的 [ContactModel](#contactModel) 对象。

#### 原型

``` java
package com.robotemi.sdk.face;

interface OnFaceRecognizedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |contactModelList|List\<[ContactModel](#contactModel)\>|识别到的人脸对应的联系人集合|

- **原型**

  ``` java
  void onFaceRecognized(List<ContactModel> contactModelList);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnFaceRecognizedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnFaceRecognizedListener(OnFaceRecognizedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnFaceRecognizedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnFaceRecognizedListener(OnFaceRecognizedListener listener);
  ```

- **所需权限**

  人脸识别

- **最小支持版本**

  0.10.70

---

### OnContinuousFaceRecognizedListener

让你的上下文实现这个监听器接口，并重写相关方法以获取识别到的人脸对应的联系人信息。当识别到的人脸已经在 [temi 管理平台](https://center.robotemi.cn/) 中配置过时，会返回所对应的[联系人](#contactModel)的其他信息；而如果未在 [temi 管理平台](https://center.robotemi.cn/) 配置过，则会返回空的 [ContactModel](#contactModel) 对象。

**注意：** 人脸识别结果会频繁下发，请考虑按需过滤部分数据，达到理想的频率。

#### 原型

``` java
package com.robotemi.sdk.face;

interface OnContinuousFaceRecognizedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |contactModelList|List\<[ContactModel](#contactModel)\>|识别到的人脸对应的联系人集合|

- **原型**

  ``` java
  void onContinuousFaceRecognized(List<ContactModel> contactModelList);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnContinuousFaceRecognizedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnContinuousFaceRecognizedListener(OnContinuousFaceRecognizedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnContinuousFaceRecognizedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnContinuousFaceRecognizedListener(OnContinuousFaceRecognizedListener listener);
  ```

- **所需权限**

  人脸识别

- **最小支持版本**

  0.10.77

---

### OnSequencePlayStatusChangedListener

让你的上下文实现这个监听器接口，并重写相关方法以监听导览播放的状态。

#### 原型

``` java
package com.robotemi.sdk.sequence;

interface OnSequencePlayStatusChangedListener {}
```

#### 静态常量 <a name="sequenceStatus" />

|常量|类型|值|说明|
|-|-|-|-|
|IDLE|int|0|播放完成|
|PREPARING|int|1|资源准备中|
|PLAYING|int|2|播放中|
|ERROR|int|-1|播放错误|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|导览的播放[状态](#sequenceStatus)|
  |sequenceId|String|当前播放的导览ID, 1.136.0 版本新增|

- **原型**

  ``` java
  void onSequencePlayStatusChanged(int status, String sequenceId);
  ```

  ``` java
  void onSequenceStepChanged(sequenceId: String, stepIndex: Int, totalSteps: Int);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnSequencePlayStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnSequencePlayStatusChangedListener(OnSequencePlayStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnSequencePlayStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnSequencePlayStatusChangedListener(OnSequencePlayStatusChangedListener listener);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### OnGreetModeStateChangedListener

让你的上下文实现这个监听器接口，并重写相关方法以监听迎宾模式的状态。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnGreetModeStateChangedListener {}
```

#### 静态常量 <a name="greetModeState" />

|常量|类型|值|说明|
|-|-|-|-|
|HOLD|int|0|完成|
|SEARCHING|int|1|等待中|
|PREPARING|int|2|资源准备中|
|GREETING|int|3|迎宾中|
|INTERACTION|int|4|用户交互中|
|POST_INTERACTION|int|5|用户离开|
|ERROR|int|-1|出现错误|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|迎宾模式的[状态](#greetModeState)|

- **原型**

  ``` java
  void onGreetModeStateChanged(int state);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGreetModeStateChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnGreetModeStateChangedListener(OnGreetModeStateChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGreetModeStateChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnGreetModeStateChangedListener(OnGreetModeStateChangedListener listener);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.79

<br>

## 模型

### ContactModel

用于存放人脸识别中的联系人信息。

#### 原型

  ``` java
  package com.robotemi.sdk.face;

  class ContactModel {}
  ```

#### 属性

|属性|类型|说明|
|-|-|-|
|firstName|String|联系人的名|
|lastName|String|联系人的姓|
|gender|String|性别|
|imageKey|String|联系人照片的媒体资源的 key，通过[getInputStreamByMediaKey(ContentType contentType, String mediaKey)](#getInputStreamByMediaKey) 获取对应的文件输入流|
|description|String|描述信息|
|userId|String|联系人 ID,  temi 注册用户 (userType = 0) 对应 MD5 格式， 如 `d7cbcc25cc3fa002d28687ea1136324e`。<br>仅人脸识别用户 (userType = 1) 对应 24 位十六进制字符，如`507fffffbcf86cd799430000`。 <br>迎宾模式下访客 (userType = 2) 对应 12 位十六进制 UUID ， 如 `3965f7ac0d8b`  (129 版本加入). <br> 从 129 版本，对应 Launcher 版本 17009 起，其他情况下，仅检测未识别 (userType = -1) 会返回人脸 id, 同一轮识别下，当人脸未离开摄像头范围则 id 保持恒定，从 1 开始计数<br> (userType = 3) 会返回应用自定义的 uid|
|age|int|年龄 (129 版本加入)|
|userType|int|0：已注册的 temi 用户。<br>1：temi 联系人，非 temi 注册用户，仅用于人脸识别。<br>2：迎宾模式中的访客，提供 UUID。<br>3: SDK 本地注册的人脸<br>-1：检测到人脸但无法识别 (129 版本加入)|
|similarity|double|识别结果相似度，当且识别结果对比与识别到的注册用户的或访客用户的相似度，范围在 0.7 到 1.0  (129 版本加入)|
|faceRect|[Rect](https://developer.android.com/reference/android/graphics/Rect)|人脸的 Rect 位置尺寸数据 (132 版本加入)，temi V3 上返回范围为 800 x 600 [参考文档](https://github.com/robotemi/sdk/blob/5f0dc8d1a999ab0f4c874823e0d1fd1bbc30ec27/sdk/src/main/java/com/robotemi/sdk/face/ContactModel.kt#L15-L17)|

#### 示例

使用方法可参考[示例代码](https://github.com/robotemi/sdk/blob/5f0dc8d1a999ab0f4c874823e0d1fd1bbc30ec27/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L1937-L2024)。

---

### ContentType

用于存放内容类型，目前只有人脸识别图片以及地图数据。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum ContentType {
    FACE_RECOGNITION_IMAGE,
    MAP_DATA_IMAGE
}
```

---

### SequenceModel

用于存放导览数据。

#### 原型

``` java
package com.robotemi.sdk.sequence;

class SequenceModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|String|导览ID|
|name|String|导览名称|
|description|String|导览描述|
|imageKey|String|导览封面图片资源的 key，通过 [getSignedUrlByMediaKey()](#getSignedUrlByMediaKey) 获取图片的签名URL|
|tags|List\<String\>|导览的标签|

---

### SequenceCommand

用于存放导览控制的指令，包括停止、播放、暂停、下一步、上一步。

``` java
package com.robotemi.sdk.constants;

enum SequenceCommand {
    STOP,
    PLAY,
    PAUSE,
    STEP_FORWARD,
    STEP_BACKWARD;
}
```

---

### TourModel

讲解数据 model

#### 原型

``` java
package com.robotemi.sdk.tourguide;

class TourModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|String|讲解 id|
|name|String|讲解名称|
|description|String|讲解描述|
|language|String|讲解语言|
|imageKey|String|讲解封面图片资源的 key，通过 [getSignedUrlByMediaKey()](#getSignedUrlByMediaKey) 获取图片的签名URL|
|tags|List\<String\>|讲解的标签|

---
