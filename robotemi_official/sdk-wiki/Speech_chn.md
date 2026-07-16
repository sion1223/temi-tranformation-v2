# 语音

temi 的语音流由四个主要部分组成：唤醒、自动语音识别（ASR）、自然语言处理（NLP）和文本到语音的转换（TTS）。

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|void|[speak(TtsRequest ttsRequest)](#speak)|语音播报文本（TTS）|
|void|[cancelAllTtsRequests()](#cancelAllTtsRequests)|取消 TTS 播报|
|void|[wakeup()](#wakeup)|唤醒 temi|
|String|[getWakeupWord()](#getWakeupWord)|获取当前唤醒词|
|void|[askQuestion(String question)](#askQuestion)|让 temi 主动说话，并等待用户回答|
|void|[finishConversation()](#finishConversation)|结束一个对话|
|void|[startDefaultNlu(String text, SttLanguage sttLanguage)](#startDefaultNlu)|触发系统默认的自然语言理解（NLU）|
|void|[setTtsService(IttsService ttsService)](#setTtsService)|设置 TTS 服务|
|void|[publishTtsStatus(TtsRequest ttsRequest)](#publishTtsStatus)||
|boolean|[setTtsVoice(TtsVoice ttsVoice)](#setTtsVoice)|设置 TTS 声音，语速，语调|
|[TtsVoice](#ttsVoice)|[getTtsVoice()](#getTtsVoice)|获取 TTS 声音，语速，语调|
|int|[setAsrLanguages()](#setasrlanguages)|设置 ASR 语言|

|接口|说明|
|-|-|
|[TtsListener](#ttsListener)|TTS 播报状态监听器|
|[WakeupWordListener](#wakeupWordListener)|唤醒监听器|
|[AsrListener](#asrListener)|语音识别（ASR）监听器|
|[ConversationViewAttachesListener](#conversationViewAttachesListener)|对话视图监听器|
|[OnConversationStatusChangedListener](#onConversationStatusChangedListener)|对话层状态、文本内容变化监听器|
|[OnTtsVisualizerWaveFormDataChangedListener](#onTtsVisualizerWaveFormDataChangedListener)|TTS 音频可视化 wave form 数据变化监听器|
|[OnTtsVisualizerFftDataChangedListener](#onTtsVisualizerFftDataChangedListener)|TTS 音频可视化 fft 数据变化监听器|
|[ITtsService](#ITtsService)|自定义 TTS 服务规范接口|

|模型|说明|
|-|-|
|[TtsRequest](#ttsRequest)|TTS 请求对象|
|[TtsVoice](#ttsVoice)|TTS 声音参数|
|[Gender](#gender)|TTS 性别参数|
|[SttLanguage](#sttlanguage)|ASR / STT 语言|
|[WakeupRequest](#wakeuprequest)|Wakeup 行为|
|[WakeupOrigin](#wakeuporigin)|Wakeup 来源|

<br>

## 方法

### speak()

用这个方法来语音播报一段文本。

134 版本开始，如果 TtsRequest.id 相同，在当前同一 id TtsRequest 正在说话的情况下，新的 TtsRequest 将被添加至 TtsRequest 队列。[示例](https://github.com/robotemi/sdk/blob/5f0dc8d1a999ab0f4c874823e0d1fd1bbc30ec27/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L1013-L1042)

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|TTS 请求对象，包含待播报文本内容|

- **原型**

  ``` java
  void speak(TtsRequest ttsRequest);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### cancelAllTtsRequests()

用这个方法来停止当前的TTS播报并清空TTS请求队列。

- **原型**

  ``` java
  void cancelAllTtsRequests();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### wakeup()

触发temi的唤醒。132 版本支持传入语言，指定 temi 可以听懂的语言。可以设置三种额外的语言。

此方法指定的语言优先级高于 setAsrLanguages() 方法。

目前此方法仅在英文版中支持，中文版 temi 暂时无法使用。

133 版本增加了一个覆写方法 `wakeup(SttRequest sttRequest)`

135 版本增加了一个参数 `WakeupRequest`

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |languages|List<[SttLanguage](#sttlanguage)>|唤醒语言，传空列表表示使用系统语言，1.132.0 版本加入，配合 132 版本使用|
  |wakeupRequest|WakeupRequest|唤醒请求，1.135.1 版本加入，配合 135 版本使用|


  |参数|类型|说明|
  |-|-|-|
  |sttRequest|SttRequest|唤醒语言及一些实验性参数，133版本加入。|
  |wakeupRequest|WakeupRequest|唤醒请求，1.135.1 版本加入，配合 135 版本使用|

- **原型**

  ``` java
  void wakeup(List<SttLanguage> languages, WakeupRequest wakeupRequest);

  void wakeup(SttRequest sttRequest, WakeupRequest wakeupRequest);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.49

---

### getWakeupWord()

用这个方法来获取 temi 的唤醒词。

- **返回值**

  |类型|说明|
  |-|-|
  |String|唤醒词|

- **原型**

  ``` java
  String getWakeupWord();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.49

---

### askQuestion()

用这个方法让 temi 主动向用户说话，并等待用户回答。

133 版本增加了一个覆写方法，支持传入 TtsRequest、SttRequest 作为参数。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |question|String|待播报的内容|


  |参数|类型|说明|
  |-|-|-|
  |question|TtsRequest|待播报的内容，可以使用 TtsRequest 中的参数控制 TTS，133版本加入|
  |sttRequest|SttRequest|控制播报之后的 STT 行为，类似于 `wakeup(SttRequest sttRequest)` 的效果，默认为 null, 133版本加入|

- **原型**

  ``` java
  void askQuestion(String question);


  void askQuestion(TtsRequest question, SttRequest sttRequest);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.63

- **推荐**

  可通过配合 [AsrListener](#asrListener) 来实现一个自定义的对话流，具体可参考 [Sample](https://github.com/robotemi/sdk/blob/390a4880064af6b550dc24f04ffe6e2634a0af9b/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L1486-L1524) 的代码。

---

### finishConversation()

用这个方法以结束会话（关闭ASR收音等）。

- **原型**

  ``` java
  void finishConversation();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.63

---

### startDefaultNlu()

用这个方法来触发系统默认的自然语言理解（NLU）。如果希望能在技能中直接触发系统的技能比如天气或音乐，可直接将“今天天气”或“播放音乐”作为参数传入并调用此方法来实现。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |text|String|待处理的自然语言文本|
  |sttLanguage|[SttLanguage](#sttlanguage)|指定文本语言，133 版本增加，默认为 SttLangauge.SYSTEM。中文版 temi 不支持该参数。|

- **原型**

  ``` java
  void startDefaultNlu(String text, SttLanguage sttLanguage);
  ```

- **所需权限**

  作为选中的Kiosk

- **最小支持版本**

  0.10.70

- **注意**

  这个接口每 5 秒内只能被调用一次。

---

### setTtsService()

用这个方法配置你自己的 TTS 服务，正确配置之后，temi 的 TTS 功能需求将依赖于这个 TTS 服务，这里不包括腾讯云小微的自有技能（天气、百科等），除此之外都将使用这个自定义的 TTS服务。如果有需要（比如播报自有 TTS 服务无法播报的语言）使用回 temi 原有 TTS 服务，则可通过调用 [speak()](#speak) 方法来实现。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsService|[ITtsService](#ITtsService)|实现了 [ITtsService](#ITtsService) 的子类实例，如果传入的为 `null`，将解绑 TTS 服务|

- **原型**

  ``` java
  void setTtsService(ITtsService ttsService);
  ```

- **所需权限**

  AndroidManifest 中声明 `com.robotemi.sdk.metadata.OVERRIDE_TTS`  覆盖原 TTS

- **最小支持版本**

  0.10.77

---

### publishTtsStatus()

用这个方法来发布自定义 TTS 服务的 TTS 播报状态，将状态通知给 temi。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsReqeust|[TtsRequest](#ttsReqeust)|当前正在处理的 [TtsRequest](#ttsReqeust) 对象（需包含状态）|

- **原型**

  ``` java
  void setTtsService(TtsRequest ttsReqeust);
  ```

- **所需权限**

  AndroidManifest 中声明 `com.robotemi.sdk.metadata.OVERRIDE_TTS`  覆盖原 TTS

- **最小支持版本**

  0.10.77


---

### setTtsVoice()

设置 TTS 声音，语速，语调。 中文版软件暂不支持。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsVoice|[TtsVoice](#ttsVoice)|TtsVoice 声音参数|

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 表示配置成功，false 表示配置失败|


- **原型**

  ``` java
  boolean setTtsVoice(TtsVoice ttsVoice);
  ```

- **所需权限**

  SETTINGS

- **最小支持版本**

  1.129.0

---

### getTtsVoice()

获取 TTS 声音，语速，语调。中文版软件暂不支持。

- **返回值**

  |Type|Description|
  |-|-|
  |[TtsVoice](#ttsVoice)|当前 TTS 声音参数|


- **原型**

  ``` java
  TtsVoice getTtsVoice();
  ```

- **最小支持版本**

  1.129.0

---

### setAsrLanguages()

改变 ASR 语言，在 KIOSK 应用活跃期间保持生效。可以设置三种额外的语言。

wakeup() 指定的语言会在其触发的对话中临时覆盖掉当前方法指定的语言。

目前此方法仅在英文版中支持，中文版 temi 暂时无法使用。

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 成功, -1 无效, 403 无权限|


- **原型**

  ``` java
  int setAsrLanguages(List<SttLanguage> languages);
  ```

- **所需权限**

  KIOSK

- **最小支持版本**

  1.132.0

<br>

## 接口

### TtsListener

让你的上下文实现这个监听器接口，并重写相关方法以获取TTS状态变化信息。

#### 原型

``` java
package com.robotemi.sdk;

interface Robot.TtsListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|TTS 请求对象，包含待播报文本内容及状态|

- **原型**

  ``` java
  void onTtsStatusChanged(TtsRequest ttsRequest);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|TtsListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addTtsListener(TtsListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|TtsListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeTtsListener(TtsListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### WakeupWordListener

让你的上下文实现这个监听器接口，并重写相关方法以在用户触发唤醒词可获取到唤醒词的内容以及方向角度。

#### 原型

  ``` java
  package com.robotemi.sdk;

  interface Robot.WakeupWordListener {}
  ```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |wakeupWord|String|包含用于触发的唤醒词的字符串对象|
  |direction|int|<ul><li>0 - temi被正前方唤醒</li><li>90 - temi被左边唤醒</li><li>180 - temi被后方唤醒</li><li>270 - temi被右边唤醒</li><li>555 - 无法确定唤醒方位</li></ul>|
  |origin|WakeupOrigin|唤醒来源|

- **原型**

  ``` java
  void onWakeupWord(String wakeupWord, int direction, WakeupOrigin origin);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|WakeupWordListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addWakeupWordListener(WakeupWordListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|WakeupWordListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeWakeupWordListener(WakeupWordListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### AsrListener

让你的上下文实现这个监听器接口，并重写相关方法以获取ASR结果。

132 版本后，同时会返回识别到的文字和语言。

#### 原型

``` java
package com.robotemi.sdk;

interface Robot.AsrListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |asrResult|String|temi被唤醒后收听到的语音转成文字内容之后的字符串对象。|
  |sttLanguage|SttLanguage|文字结果对应的语言 (132 版本加入)|

- **原型**

  ``` java
  void onAsrResult(String asrResult, SttLanguage sttLanguage);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|AsrListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addAsrListener(AsrListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|AsrListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeAsrListener(AsrListener listener);
  ```

- **最小支持版本**

  0.10.53

---

### ConversationViewAttachesListener

让你的上下文实现这个监听器接口，并重写相关方法以监听会话视图是否出现。

#### 原型

``` java
package com.robotemi.sdk;

interface Robot.ConversationViewAttachesListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isAttached|boolean|`true`（`false`）表示会话视图已出现（消失）|

- **原型**

  ``` java
  void onConversationAttaches(boolean isAttached);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|ConversationViewAttachesListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addConversationViewAttachesListenerListener(ConversationViewAttachesListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|ConversationViewAttachesListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeConversationViewAttachesListenerListener(ConversationViewAttachesListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### OnConversationStatusChangedListener

让你的上下文实现这个监听器接口，并重写相关方法以监听会话层状态已经文本内容变化。

**注意：** 只有是作为选中的主屏幕应用并且在 AndroidManifest.xml 文件中声明了 Kiosk, [覆盖原有会话层用户交互界面](#overrideConversationLayer)才能收到相关回调数据。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnConversationStatusChangedListener {}
```

#### 静态常量

这里的常量均为对话层状态。

|常量|类型|值|说明|
|-|-|-|-|
|IDLE|int|0|空闲，无交互|
|LISTENING|int|1|收音中|
|THINKING|int|2|自然语言处理中|
|SPEAKING|int|3|TTS 播报中|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|会话层状态|
  |text|String|会话层文本内容|

- **原型**

  ``` java
  void onConversationStatusChanged(int status, String text);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnConversationStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnConversationStatusChangedListener(OnConversationStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnConversationStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnConversationStatusChangedListener(OnConversationStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.72

---

### OnTtsVisualizerWaveFormDataChangedListener

让你的上下文实现这个监听器接口，并重写相关方法以监听 TTS 音频可视化 Wave form 的数据变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnTtsVisualizerWaveFormDataChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |waveForm|byte[]|Wave form 数据|

- **原型**

  ``` java
  void onTtsVisualizerWaveFormDataChanged(byte[] waveForm);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTtsVisualizerWaveFormDataChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnTtsVisualizerWaveFormDataChangedListener(OnTtsVisualizerWaveFormDataChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTtsVisualizerWaveFormDataChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnTtsVisualizerWaveFormDataChangedListener(OnTtsVisualizerWaveFormDataChangedListener listener);
  ```

- **最小支持版本**

  0.10.72

---

### OnTtsVisualizerFftDataChangedListener

让你的上下文实现这个监听器接口，并重写相关方法以监听 TTS 音频可视化 fft 的数据变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnTtsVisualizerFftDataChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |fft|byte[]|fft 数据|

- **原型**

  ``` java
  void OnTtsVisualizerFftDataChangedListener(byte[] fft);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTtsVisualizerFftDataChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnTtsVisualizerFftDataChangedListener(OnTtsVisualizerFftDataChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTtsVisualizerFftDataChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnTtsVisualizerFftDataChangedListener(OnTtsVisualizerFftDataChangedListener listener);
  ```

- **最小支持版本**

  0.10.72

---

### ITtsService

实现这个接口并重写抽象方法，并将子类的实例通过 [setTtsService()](#setTtsService) 绑定 TTS 服务。

#### 原型

``` java
package com.robotemi.sdk.voice;

interface ITtsService {}
```

#### 抽象方法

##### speak()

temi 将间接调用这个方法来播报 TTS。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|待处理的 [TtsRequest](#ttsRequest) 对象|

- **原型**

  ``` java
  void speak(TtsRequest ttsRequest);
  ```

##### cancel()

temi 将间接调用这个方法来取消（中止）当前 TTS。

- **原型**

  ``` java
  void cancel();
  ```

##### pause()

temi 将间接调用这个方法来暂停当前 TTS。

- **原型**

  ``` java
  void pause();
  ```

##### resume()

temi 将间接调用这个方法来继续播报当前 TTS。

- **原型**

  ``` java
  void resume();
  ```

- **最小支持版本**

  0.10.77

<br>

## 模型

### TtsRequest

传递给temi的TTS请求对象，其中包含要temi播报的内容信息以及播报的状态信息。

#### 原型

``` java
package com.robotemi.sdk;

class TtsRequest {}
```

#### 子类

- **Status** <a name="ttsRequestStatus" />

  - **目前在用状态** <a name="currentStatus" />

    |状态|说明|
    |-|-|
    |STARTED|TTS 播报开始|
    |COMPLETED|TTS 播报完成|
    |ERROR| TTS 播报错误|
    |NOT_ALLOWED|TTS 播报不被允许|

  - **原型**

    ``` java
    enum Status {
        PENDING,
        PROCESSING,
        STARTED,
        COMPLETED,
        ERROR,
        NOT_ALLOWED;
    }
    ```

- **Language** <a name="ttsLanguage" />

  - **目前可用 TTS 语言** <a name="currentTtsLanguage" />
  
    |语言|说明|
    |-|-|
    |SYSTEM(0)|跟随系统|
    |EN_US(1)|英语（美）|
    |ZH_CN(2)|普通话|
    |ZH_HK(3)|广东话|
    |ZH_TW(4)|繁体中文（台湾）|
    |TH_TH(5)|泰语|
    |HE_IL(6)|希伯来语|
    |KO_KR(7)|韩语|
    |JA_JP(8)|日语|
    |ID_ID(10)|印度尼西亚语|
    |DE_DE(11)|德语|
    |FR_FR(12)|法语（法）|
    |FR_CA(13)|法语（加拿大）|
    |PT_BR(14)|葡萄牙语（巴西）|
    |AR_EG(15)|阿拉伯语|
    |RU_RU(18)|俄语|
    |IT_IT(19)|意大利语|
    |PL_PL(20)|波兰语|
    |ES_ES(21)|西班牙语|
    |CA_ES(22)|加泰罗尼亚语 (130 版本加入)|
    |HI_IN(23)|印地语 (130 版本加入)|
    |ET_EE(24)|爱沙尼亚语 (131 版本加入)|
    |TR_TR(25)|土耳其语 (131 版本加入)|
    |EN_IN(26)|英语（印度） (133 版本加入)|
    |MS_MY(27)|马来语 (134 版本加入)|
    |VI_VN(28)|越南语 (134 版本加入)|
    |EL_GR(29)|希腊语 (134 版本加入)|
    |ES_CO(31)|西班牙语（哥伦比亚） (138 版本加入)|
    |UR_PK(32)|乌尔都语（巴基斯坦） (138 版本加入)|
    |ES_AR(33)|西班牙语（阿根廷） (138 版本加入)|

  - **原型**

    ``` java
    enum Language {
        SYSTEM(0),
        EN_US(1),
        ZH_CN(2),
        ZH_HK(3),
        ZH_TW(4),
        TH_TH(5),
        HE_IL(6),
        KO_KR(7),
        JA_JP(8),
        IN_ID(9),
        ID_ID(10),
        DE_DE(11),
        FR_FR(12),
        FR_CA(13),
        PT_BR(14),
        AR_EG(15),
        AR_AE(16),
        AR_XA(17),
        RU_RU(18),
        IT_IT(19),
        PL_PL(20),
        ES_ES(21),
        CA_ES(22),
        HI_IN(23),
        ET_EE(24),
        TR_TR(25),
        EN_IN(26),
        MS_MY(27),
        VI_VN(28),
        EL_GR(29),
        AZ_AZ(30),
        ES_CO(31),
        UR_PK(32),
        ES_AR(33);
    }
    ```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|[UUID](https://developer.android.com/reference/java/util/UUID)|标识每一个TTS请求的唯一编号|
|speech|String|播报的文本内容|
|packageName|String|技能的包名，以便 temi 知道是哪个应用发出的请求|
|status|[Status](#ttsRequestStatus)|TTS 请求的状态|
|isShowOnConversationLayer|boolean|temi 播报 TTS 时是否显示对话视图|
|language|int|语言|
|showAnimationOnly|boolean|true 表示想要显示动画表情。<br>需要在设置中已经选择了非默认的互动表情<br>否则会以全屏文字显示<br>这个参数设置为 true 会覆盖 `isShowOnConversationLayer`的 false|
|cached|boolean|true 表示需要对当前文字创建或使用缓存<br>已缓存过的 TTS 可以离线播放<br>对于较为固定的文字，如 strings.xml 中的内容推荐使用缓存 (129 版本加入)|

#### 静态方法

创建一个 TtsRequest 对象传入 [speak(TtsRequest ttsRequest)](#speak) 方法以播报 TTS。仅 `speech` 为必传参数，其它参数可选，并有默认值。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |speech|String|待播报文本内容|
  |isShowOnConversationLayer|boolean|默认为 true|
  |language|[Language](#ttsLanguage)|默认为 `Language.SYSTEM`|
  |showAnimationOnly|boolean|默认为 false|
  |cached|boolean|默认为 false|

- **返回值**

  |类型|说明|
  |-|-|
  |[TtsRequest](#ttsRequest)|创建的 TTS 请求对象|

- **原型**

  ``` java
  static TtsRequest create(String speech, boolean isShowOnConversationLayer, Language language, boolean showAnimationOnly, boolean cached);
  ```


---

### TtsVoice

Tts 声音参数

#### 原型

``` java
package com.robotemi.sdk.voice.model;

class TtsVoice {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|gender|[Gender](#gender)|只有 FEMALE 和 MALE 可被传入|
|speed|float|0.5 - 2.0,   步进为 0.1, 默认为 1.0|
|pitch|int|-10 - 10,    步进为 1, 默认为 0|

---

### Gender

Tts 性别参数.

#### 原型

``` java
package com.robotemi.sdk.constants;

enum class Gender {
  FEMALE, MALE, GIRL, BOY, UNKNOWN
}
```

### SttLanguage <a name="sttlanguage" />

  - **当前支持的 ASR / STT 语言（与系统语言选项一致） ** <a name="currentSttLanguage" />
  
    |语言|说明|
    |-|-|
    |SYSTEM(0)|跟随系统|
    |EN_US(1)|英语|
    |ZH_CN(2)|简体中文|
    |JA_JP(3)|日语|
    |KO_KR(4)|韩语|
    |ZH_HK(5)|粤语|
    |ZH_TW(6)|繁体中文|
    |DE_DE(7)|德语|
    |TH_TH(8)|泰语|
    |IN_ID(9)|印尼语|
    |PT_BR(10)|葡萄牙语（巴西）|
    |AR_EG(11)|阿拉伯语|
    |FR_CA(12)|法语（加拿大）|
    |FR_FR(13)|法语（法国）|
    |ES_ES(14)|西班牙语|
    |CA_ES(15)|加泰罗尼亚语|
    |IW_IL(16)|希伯来语|
    |IT_IT(17)|意大利语|
    |ET_EE(18)|爱沙尼亚语|
    |TR_TR(19)|土耳其语|
    |HI_IN(20)|印地语|
    |EN_IN(21)|英语（印度）|
    |MS_MY(22)|马来语 (134 版本)|
    |VI_VN(23)|越南语 (134 版本)|
    |RU_RU(24)|俄语 (134 版本)|
    |EL_GR(25)|希腊语 (134 版本)|
    |AZ_AZ(26)|阿塞拜疆语 (136 版本加入)|
    |UR_PK(27)|乌尔都语（巴基斯坦） (138 版本加入)|

  #### **原型**

  ``` java
enum SttLanguage {
      SYSTEM(0),
      EN_US(1),
      ZH_CN(2),
      JA_JP(3),
      KO_KR(4),
      ZH_HK(5),
      ZH_TW(6),
      DE_DE(7),
      TH_TH(8),
      IN_ID(9),
      PT_BR(10),
      AR_EG(11),
      FR_CA(12),
      FR_FR(13),
      ES_ES(14),
      CA_ES(15),
      IW_IL(16),
      IT_IT(17),
      ET_EE(18),
      TR_TR(19),
      HI_IN(20),
      EN_IN(21),
      MS_MY(22),
      VI_VN(23),
      RU_RU(24),
      EL_GR(25),
      AZ_AZ(26),
      UR_PK(27);
}
  ```
---

### WakeupRequest

唤醒请求.

#### 原型

``` kotlin

data class WakeupRequest(
  val wakeupResponse: Boolean = false,
  val newSession: Boolean = false
)
```

#### 属性

|属性|类型|说明|
|-|-|-|
|wakeupResponse|Boolean|是否触发唤醒应答|
|newSession|Boolean|对于覆盖了对话层的 Kiosk 应用，设为 `true` 可开启新会话并清除此前 AI 与用户的 LLM 对话历史。1.138.0 版本加入，配合 138 版本 Launcher 使用|
---

### WakeupOrigin

唤醒来源

#### 原型

``` kotlin

enum class WakeupOrigin {
  ROBOX,
  ANDROID,
  TOP_BAR,
  SDK,
  ANALOG,
  UNKNOWN,
}
```
#### 属性

|属性|说明|
|-|-|
|ROBOX|由 Robox 端监听触发的唤醒，V3 的场景|
|ANDROID|由 Android 端监听触发的唤醒，temi GO, temi Platform 的场景|
|TOP_BAR|由手动点击顶栏触发的唤醒|
|SDK|由 SDK 应用调用 wakeup() 触发的唤醒|
|ANALOG|由 temi 内部逻辑，如连续对话，迎宾模式自动聆听等流程触发的 ASR 流程对应的唤醒|
|UNKNOWN|当前版本未知来源的唤醒|


<br>

## 覆盖原有语音流 <a name="overrideVoiceFlow" />

### 覆盖自然语言处理（NLP） <a name="overrideNlu" />

#### 实现步骤

- 在 AndroidManifest.xml 清单文件中的 `<application>` 元素下添加如下元数据 `<meta-data>`：

  ``` java
  <!-- ⚠️ 需要声明在 application 标签内部⚠️-->
  <!-- 必须是 Kiosk 技能 -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_nlu" android:value="true" />
  ```

- [监听 ASR](#asrListener) 并在回调方法中接入你自己的 NLP 服务。

- 在 Launcher 的 *设置 > 主屏幕 > 应用 > 选中你的技能*。或者通过方法 [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#requestToBeKioskApp) 请求成为选中的 Kiosk（商店模式）技能应用。

#### 所需权限

作为选中的Kiosk

#### 最小支持版本

  0.10.63

---

### 覆盖语音识别（ASR）<a name="overrideAsr" />

#### 实现步骤

- 在 AndroidManifest.xml 清单文件中的 `<application>` 元素下添加如下元数据 `<meta-data>`：

  ``` java
  <!-- 必须是 Kiosk 技能 -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_stt" android:value="true" />
  ```

- [监听唤醒事件](#wakeupWordListener) 并在回调方法中接入你自己的 ASR 服务。

- 在 Launcher 的 *设置 > 应用 > 商店模式 > 选中你的技能*。或者通过方法 [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#requestToBeKioskApp) 请求成为选中的 Kiosk（商店模式）技能应用。

#### 所需权限

作为选中的Kiosk

#### 最小支持版本

  0.10.70

---

### 覆盖语音对话层用户交互界面 <a name="overrideConversationLayer" />

#### 实现步骤

- 在 AndroidManifest.xml 清单文件中的 `<application>` 元素下添加如下元数据 `<meta-data>`：

  ``` java
  <!-- 必须是 Kiosk 技能 -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_conversation_layer" android:value="true" />
  ```

- [监听对话层状态、文本变化](#onConversationStatusChangedListener) 并在回调方法中处理相关数据并绘制对应的用户交互界面。

- 在 Launcher 的 *设置 > 应用 > 商店模式 > 选中你的技能*。或者通过方法 [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#requestToBeKioskApp) 请求成为选中的 Kiosk（商店模式）技能应用。

#### 所需权限

作为选中的Kiosk

#### 最小支持版本

  0.10.72

---

### 覆盖文字转语音 <a name="overrideTts" />

#### 实现步骤

- 在 AndroidManifest.xml 清单文件中的 `<application>` 元素下添加如下元数据 `<meta-data>`：

  ``` java
  <!-- 必须是 Kiosk 技能 -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_tts" android:value="true" />
  ```

- 实现 [ITtsService](#ITtsService) 接口，并重写 `speak()`、`cancel()`、`pause()`、`resume()` 方法。temi 将会调用对应的方法来实现 TTS 播报需求。

- 将上个步骤中实现的 [ITtsService](#ITtsService) 子类通过 [setTtsService()](#setTtsService) 方法进行绑定。

- 【**非常重要**】通过 [publishTtsStatus()](#publishTtsStatus) 方法将 TTS 的状态通知 temi，temi 需要根据这些状态进行响应（UI 展示、导览的步骤推进等）。

- 在 Launcher 的 *设置 > 应用 > 商店模式 > 选中你的技能*。或者通过方法 [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#requestToBeKioskApp) 请求成为选中的 Kiosk（商店模式）技能应用。

- 更多细节请参考 [Sample](https://github.com/robotemi/sdk/blob/390a4880064af6b550dc24f04ffe6e2634a0af9b/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L2539-L2628)。

#### 所需权限

作为选中的Kiosk

#### 最小支持版本

  0.10.77
