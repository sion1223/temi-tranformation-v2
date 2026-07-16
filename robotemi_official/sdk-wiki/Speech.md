# Speech

temi's speech flow is comprised of four main components - wakeup, ASR (Automatic Speech Recognition), NLP (Natural language processing) and TTS (text to speech).

temi's SDK provides developers tools to utilize, customize and listen to any of the components.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|void|[speak(TtsRequest ttsRequest)](#speak)|Ask temi to speak(play TTS)|
|void|[cancelAllTtsRequests()](#cancelAllTtsRequests)|cancel TTS request|
|void|[wakeup()](#wakeup)|Wake up temi|
|String|[getWakeupWord()](#getWakeupWord)|Get current wake-up word|
|void|[askQuestion(String question)](#askQuestion)|temi speak actively and wait for user's reply|
|void|[finishConversation()](#finishConversation)|Finish a conversation(Stop recording for ASR)|
|void|[startDefaultNlu(String text, SttLanguage sttLanguage)](#startDefaultNlu)|Trigger default NLU service|
|boolean|[setTtsVoice(TtsVoice ttsVoice)](#setTtsVoice)|Set TTS voice, speed, and pitch|
|[TtsVoice](#ttsVoice)|[getTtsVoice()](#getTtsVoice)|Trigger default NLU service|
|int|[setAsrLanguages()](#setasrlanguages)|Set system ASR languages|

|Interface|Description|
|-|-|
|[TtsListener](#ttsListener)|TTS status listener|
|[WakeupWordListener](#wakeupWordListener)|Wake-up event listener|
|[AsrListener](#asrListener)|ASR result listener|
|[ConversationViewAttachesListener](#conversationViewAttachesListener)|Conversation view attaches listener|
|[OnConversationStatusChangedListener](#onConversationStatusChangedListener)|Listener for status chagned of Conversation view|
|[OnTtsVisualizerWaveFormDataChangedListener](#onTtsVisualizerWaveFormDataChangedListener)|Listener for wave form data changes of TTS audio visualizer|
|[OnTtsVisualizerFftDataChangedListener](#onTtsVisualizerFftDataChangedListener)|Listener for fft data changes of TTS audio visualizer|

|Model|Description|
|-|-|
|[TtsRequest](#ttsRequest)|TTS request instance|
|[TtsVoice](#ttsVoice)|TTS voice configuration|
|[Gender](#gender)|TTS voice gender|
|[SttLanguage](#sttlanguage)|ASR / STT languages|
|[WakeupRequest](#wakeuprequest)|Wakeup behavior|
|[WakeupOrigin](#wakeuporigin)|Wakeup Origin|

<br>

## Methods

### speak()

Use this method to let temi speak something that from the parameter `ttsRequest` of this method.

From 134 verison, if the TtsRequest.id is the same, the requested will be queued when there is an ongoing TTS.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|An object of type TtsRequest in this object you will add the text to be spoken.|

- **Prototype**

  ``` java
  void speak(TtsRequest ttsRequest);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### cancelAllTtsRequests()

Stops currently processed TTS request and empty the queue.

- **Prototype**

  ``` java
  void cancelAllTtsRequests();
  ```

- **Required permissions**

  None.

- **Support from**

---

### wakeup()

Use this method to trigger temi's wakeup programmatically. 

In 132 version, this method can take an optional list to make temi listen to specific langauges. The languages set will temporarily override setAsrLanguages() for current wake up session. Max. 3 extra languages can be set.

In 133 version there is a new overload method wakeup(SttRequest sttRequest)
In 135 version there is a new parameter WakeupRequest

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |languages|List<[SttLanguage](#sttlanguage)>|Langauges for STT, empty list to use system language. Add in 1.132.0, works with 132 version|
  |wakeupRequest|WakeupRequest|Control behavior of wake up. Add in 1.135.1, works with 135 version|


  |Parameter|Type|Description|
  |-|-|-|
  |sttRequest|SttRequest|Contains languages, and some experimental paramters, added in 133.|
  |wakeupRequest|WakeupRequest|Control behavior of wake up. Add in 1.135.1, works with 135 version|
- **Prototype**

  ``` java
  void wakeup(List<SttLanguege> languages, WakeupRequest wakeupRequest);

  void wakeup(SttRequest sttRequest, WakeupRequest wakeupRequest);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.49

---

### getWakeupWord()

Use this method to get temi's wake word assistant.

- **Return**

  |Type|Description|
  |-|-|
  |String|Wake-up word|

- **Prototype**

  ``` java
  String getWakeupWord();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.49

---

### askQuestion()

Use this method to let temi actively speak to the user and wait for the user to answer.

In 133 version, there is an overload method to pass TtsRequest, and SttRequest parameter.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |question|String|The text to be spoken|

  |Parameter|Type|Description|
  |-|-|-|
  |question|TtsRequest|The text to be spoken, support all TtsRequest parameters added in 133 version.|
  |sttRequest|SttRequest|Control the speech in the STT session after the TTS, similiar to `wakeup(SttRequest sttRequest)`, default is null, added in 133 version.|


- **Prototype**

  ``` java
  void askQuestion(String question);

  void askQuestion(TtsReqeust question, SttRequest sttRequest);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.63

- **Recommendation**

  A custom dialog flow can be realized by cooperating with [AsrListener](#asrListener). For details, please refer to  [Sample](https://github.com/robotemi/sdk/blob/390a4880064af6b550dc24f04ffe6e2634a0af9b/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L1486-L1524) code.

---

### finishConversation()

Use this method to finish the conversation (Stop recording for ASR).

- **Prototype**

  ``` java
  void finishConversation();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.63

---

### startDefaultNlu()

Use this method to trigger the system's default natural language understanding (NLU). If you want to directly trigger system skills such as weather or music in your skill, you can directly pass in "What's the wheather today" or "Play Music" as a parameter and invoke this method to achieve it.

In 1.133.0 version. It will allow to assign system langauge, to trigger an NLU recognition of target language.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |text|String|Natural language text to be processed|
  |sttLanguage|[SttLanguage](#sttlanguage)|Language of NLU, default as SttLangauge.SYSTEM, added in 1.133.0|

- **Prototype**

  ``` java
  void startDefaultNlu(String text, SttLanguage sttLanguage);
  ```

- **Required permissions**

  Selected Kiosk

- **Support from**

  0.10.70

- **Note**

  This interface can only be called once every 5 seconds.

---

### setTtsService()

Use this method to configure your own TTS service. After the correct configuration, temi's TTS function requirements will depend on this TTS service. If you want to use temi's original TTS service, call method [speak()](#speak) to do that.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsService|[ITtsService](#ITtsService)|The instance of class that implemented [ITtsService](#ITtsService). If the passed paramenter is `null`, the TTS service will be unbound.|

- **Prototype**

  ``` java
  void setTtsService(ITtsService ttsService);
  ```

- **Required permissions**

  `com.robotemi.sdk.metadata.OVERRIDE_TTS` declared in the manifest to override original TTS

- **Support from**

  0.10.77

---

### publishTtsStatus()

Use this method to publish the TTS status from your TTS service to temi.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsReqeust|[TtsRequest](#ttsReqeust)|Current instance of [TtsRequest](#ttsReqeust) (need to include status))|

- **Prototype**

  ``` java
  void setTtsService(TtsRequest ttsReqeust);
  ```

- **Required permissions**

  `com.robotemi.sdk.metadata.OVERRIDE_TTS` declared in the manifest to override original TTS

- **Support from**

  0.10.77

---

### setTtsVoice()

Set TTS voice, speed and pitch. Only available for temi Global version.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsVoice|[TtsVoice](#ttsVoice)|TtsVoice configuration|

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if set is successful|


- **Prototype**

  ``` java
  boolean setTtsVoice(TtsVoice ttsVoice);
  ```

- **Required permissions**

  SETTINGS

- **Support from**

  1.129.0

---

### getTtsVoice()

Get TTS voice, speed and pitch. Only available for temi Global version.

- **Return**

  |Type|Description|
  |-|-|
  |[TtsVoice](#ttsVoice)|current TTS voice configuration|


- **Prototype**

  ``` java
  TtsVoice getTtsVoice();
  ```

- **Support from**

  1.129.0

---

### setAsrLanguages()

Change ASR langauges, this settings will persist when this kiosk app is running. Max. 3 extra languages can be set.

This languages can be temporarily overriden by wakeup()

- **Return**

  |Type|Description|
  |-|-|
  |int|0 OK, -1 invalid, 403 no permission|


- **Prototype**

  ``` java
  int setAsrLanguages(List<SttLanguage> languages);
  ```

- **Required permissions**

  KIOSK

- **Support from**

  1.132.0

<br>

## Interfaces

### TtsListener

Set your context to implement this listener and add the override method to get TTS status changes.

#### Prototype

``` java
package com.robotemi.sdk;

interface Robot.TtsListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|TTS request object that holds its text content and status|

- **Prototype**

  ``` java
  void onTtsStatusChanged(TtsRequest ttsRequest);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|TtsListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addTtsListener(TtsListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|TtsListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeTtsListener(TtsListener listener);
  ```

- **Support from**

  0.10.36

---

### WakeupWordListener

Set your context to implement this listener and add the override method to get wake word value when triggered by the user.

#### Prototype

  ``` java
  package com.robotemi.sdk;

  interface Robot.WakeupWordListener {}
  ```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |wakeupWord|String|The wakeup word used to trigger|
  |direction|int|<ul><li>0 - temi was triggered from the front</li><li>90 - temi was triggered from the left</li><li>180 - temi was triggered from the back</li><li>270 - temi was triggered from the right</li><li>555 - cannot detect wakeup direction</li></ul>|
  |origin|WakeupOrigin|The origin of this wakeup event|

- **Prototype**

  ``` java
  void onWakeupWord(String wakeupWord, int direction, WakeupOrigin origin);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|WakeupWordListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addWakeupWordListener(WakeupWordListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|WakeupWordListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeWakeupWordListener(WakeupWordListener listener);
  ```

- **Support from**

  0.10.36

---

### AsrListener

Set your context to implement this listener and add the override method to get the ASR result.

From 132 version, both the text and language will be returned.

#### Prototype

``` java
package com.robotemi.sdk;

interface Robot.AsrListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |asrResult|String|The ASR result|
  |sttLanguage|SttLanguage|The language of the ASR result (Added in 132)|

- **Prototype**

  ``` java
  void onAsrResult(String asrResult, SttLanguage sttLanguage);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|AsrListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addAsrListener(AsrListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|AsrListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeAsrListener(AsrListener listener);
  ```

- **Support from**

  0.10.53

---

### ConversationViewAttachesListener

Set your context to implement this listener and add the override method to listen if the conversation view attaches.

#### Prototype

``` java
package com.robotemi.sdk;

interface Robot.ConversationViewAttachesListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |isAttached|boolean|`true` means the conversation view attaches, `false` otherwise|

- **Prototype**

  ``` java
  void onConversationAttaches(boolean isAttached);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|ConversationViewAttachesListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addConversationViewAttachesListenerListener(ConversationViewAttachesListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|ConversationViewAttachesListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeConversationViewAttachesListenerListener(ConversationViewAttachesListener listener);
  ```

- **Support from**

  0.10.36

---

### OnConversationStatusChangedListener

Set your context to implement this interface and override its' abstract method to listen to the status and text changes of the Conversation layer.

**Note:** Only the selected Kiosk App declared in the AndroidManifest.xml file to override the original conversation layer UI can receive the related callback data.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnConversationStatusChangedListener {}
```

#### Static constants

All constants here are only for the status of Conversation layer.

|Constant|Type|Value|Description|
|-|-|-|-|
|IDLE|int|0|Idle, no useriteraction|
|LISTENING|int|1|Listening user's voice|
|THINKING|int|2|Doing NLP|
|SPEAKING|int|3|Playing TTS|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |status|int|Status of Conversation layer|
  |text|String|Text of Conversation layer|

- **Prototype**

  ``` java
  void onConversationStatusChanged(int status, String text);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnConversationStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnConversationStatusChangedListener(OnConversationStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnConversationStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnConversationStatusChangedListener(OnConversationStatusChangedListener listener);
  ```

- **Support from**

  0.10.72

---

### OnTtsVisualizerWaveFormDataChangedListener

Set your context to implements this interface and override its' abstract method to listen to the wave form data changes of the TTS audio visualizer.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnTtsVisualizerWaveFormDataChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |waveForm|byte[]|Wave form data|

- **Prototype**

  ``` java
  void onTtsVisualizerWaveFormDataChanged(byte[] waveForm);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTtsVisualizerWaveFormDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnTtsVisualizerWaveFormDataChangedListener(OnTtsVisualizerWaveFormDataChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTtsVisualizerWaveFormDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnTtsVisualizerWaveFormDataChangedListener(OnTtsVisualizerWaveFormDataChangedListener listener);
  ```

- **Support from**

  0.10.72

---

### OnTtsVisualizerFftDataChangedListener

Set your context to implements this interface and override its' abstract method to listen to the fft data changes of the TTS audio visualizer.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnTtsVisualizerFftDataChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |fft|byte[]|fft data|

- **Prototype**

  ``` java
  void OnTtsVisualizerFftDataChangedListener(byte[] fft);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTtsVisualizerFftDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnTtsVisualizerFftDataChangedListener(OnTtsVisualizerFftDataChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTtsVisualizerFftDataChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnTtsVisualizerFftDataChangedListener(OnTtsVisualizerFftDataChangedListener listener);
  ```

- **Support from**

  0.10.72

---

### ITtsService

Implement this interface and override the abstract methods, and use method [setTtsService()](#setTtsService) to bind the TTS service.

#### Prototype

``` java
package com.robotemi.sdk.voice;

interface ITtsService {}
```

#### Abstract methods

##### speak() <a name="ITtsService.speak" />

temi will call this method indirectly to play TTS.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |ttsRequest|[TtsRequest](#ttsRequest)|Pending [TtsRequest](#ttsRequest) instance|

- **Prototype**

  ``` java
  void speak(TtsRequest ttsRequest);
  ```

##### cancel()  <a name="ITtsService.cancel" />

temi will call this method indirectly to cancel(stop) current TTS.

- **Prototype**

  ``` java
  void cancel();
  ```

##### pause()  <a name="ITtsService.pause" />

temi will call this method indirectly to pause current TTS.

- **Prototype**

  ``` java
  void pause();
  ```

##### resume()  <a name="ITtsService.resume" />

temi will call this method indirectly to resume current TTS.

- **Prototype**

  ``` java
  void resume();
  ```

- **Support from**

  0.10.77

<br>

## Models

### TtsRequest

Request object passed to temi, which contains all the information temi needs to in order to speak and for the skill to track its' request.

#### Prototype

``` java
package com.robotemi.sdk;

class TtsRequest {}
```

#### Subclass

- **Status** <a name="ttsRequestStatus" />

  - **The status currently in use** <a name="currentStatus" />

    |Status|description|
    |-|-|
    |STARTED|Start playing|
    |COMPLETED|Finish playing|
    |ERROR|Errors occurred while playing|
    |NOT_ALLOWED|Play is not allowed|

  - **Prototype**

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

  - **Current supported TTS language** <a name="currentTtsLanguage" />
  
    |Language|Description|
    |-|-|
    |SYSTEM(0)|Follow system|
    |EN_US(1)|English (United States)|
    |ZH_CN(2)|Chinese (Mandarin, Simplified)|
    |ZH_HK(3)|Chinese (Cantonese, Traditional)|
    |ZH_TW(4)|Chinese (Taiwanese Mandarin)|
    |TH_TH(5)|Thai (Thailand)|
    |HE_IL(6)|Hebrew (Israel)|
    |KO_KR(7)|Korean (Korea)|
    |JA_JP(8)|Japanese (Japan)|
    |ID_ID(10)|Indonesian (Indonesia)|
    |DE_DE(11)|German (Germany)|
    |FR_FR(12)|French (France)|
    |FR_CA(13)|French (Canada)|
    |PT_BR(14)|Portuguese (Brazil)|
    |AR_EG(15)|Arabic (Egypt)|
    |RU_RU(18)|Russian (Russia)|
    |IT_IT(19)|Italian (Italy)|
    |PL_PL(20)|Polish (Poland)|
    |ES_ES(21)|Spanish (Spain)|
    |CA_ES(22)|Catalan (Spain) (supported from 130 version)|
    |HI_IN(23)|Hindi (supported from 130 version)|
    |ET_EE(24)|Estonian (supported from 131 version)|
    |TR_TR(25)|Turkish (supported from 131 version)|
    |EN_IN(26)|English (India) (supported from 133 version)|
    |MS_MY(27)|Malay (supported from 134 version)|
    |VI_VN(28)|Vietnamese (supported from 134 version)|
    |EL_GR(29)|Greek (supported from 134 version)|
    |ES_CO(31)|Spanish (Colombia) (supported from 138 version)|
    |UR_PK(32)|Urdu (Pakistan) (supported from 138 version)|
    |ES_AR(33)|Spanish (Argentina) (supported from 138 version)|

  - **Prototype**

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
        ET_EE(24)，
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

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|id|[UUID](https://developer.android.com/reference/java/util/UUID)|Unique number that identifies each tts request|
|speech|String|The text to be spoken|
|packageName|String|Skill package name so that temi knows who made the request|
|status|[Status](#ttsRequestStatus)|Status of the request|
|isShowOnConversationLayer|boolean|Should the conversation line be shown when temi speaks the text.</br> **Note:** Only relevant for 'Hey temi' assistant skills|
|language|int|Language|
|showAnimationOnly|boolean|true if you want to show a face animation while the speech is ongoing.<br>This only works if there is an assigned interaction animation in temi Settings,<br>otherwise it will just display the text on screen without a face animation.<br>Set this as true will override `isShowOnConversationLayer` if that value is set to false|
|cached|boolean|true if you want to have this tts cached. Default as false.<br>If there is cache, it will be spoken offline.<br> This is useful for TTS from some sentences you have in the strings.xml (Supported from 129 version)|

#### Static methods

Create a TtsRequest object and pass it to [speak(TtsRequest ttsRequest)](#speak) method to play TTS. Only `speech` is mandatory. The other parameters are optional.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |speech|String|The text to be spoken|
  |isShowOnConversationLayer|boolean|default as true|
  |language|[Language](#ttsLanguage)|default as `Language.SYSTEM`|
  |showAnimationOnly|boolean|default as false|
  |cached|boolean|default as false|

- **Return**

  |Type|Description|
  |-|-|
  |[TtsRequest](#ttsRequest)|TTS request object created by this method|

- **Prototype**

  ``` java
  static TtsRequest create(String speech, boolean isShowOnConversationLayer);
  ```

---

### TtsVoice

Tts voice configuration.

#### Prototype

``` java
package com.robotemi.sdk.voice.model;

class TtsVoice {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|gender|[Gender](#gender)|only female and male can be used as parameter|
|speed|float|0.5 - 2.0,   stepping by 0.1, default 1.0|
|pitch|int|-10 - 10,    stepping by 1, default 0|

---

### Gender

Tts voice gender.

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum class Gender {
  FEMALE, MALE, GIRL, BOY, UNKNOWN
}
```


### SttLanguage <a name="sttlanguage" />

  - **Current supported ASR language** <a name="currentSttLanguage" />
  
    |Language|Description|
    |-|-|
    |SYSTEM(0)|Follow system|
    |EN_US(1)|English (United States)|
    |ZH_CN(2)|Chinese (Mandarin, Simplified)|
    |JA_JP(3)|Japanese (Japan)|
    |KO_KR(4)|Korean (Korea)|
    |ZH_HK(5)|Chinese (Cantonese, Traditional)|
    |ZH_TW(6)|Chinese (Taiwanese Mandarin)|
    |DE_DE(7)|German (Germany)|
    |TH_TH(8)|Thai (Thailand)|
    |IN_ID(9)|Indonesian (Indonesia)|
    |PT_BR(10)|Portuguese (Brazil)|
    |AR_EG(11)|Arabic (Egypt)|
    |FR_CA(12)|French (Canada)|
    |FR_FR(13)|French (France)|
    |ES_ES(14)|Spanish (Spain)|
    |CA_ES(15)|Catalan (Spain)|
    |IW_IL(16)|Hebrew (Israel)|
    |IT_IT(17)|Italian (Italy)|
    |ET_EE(18)|Estonian|
    |TR_TR(19)|Turkish|
    |HI_IN(20)|Hindi, added in 1.133.0 version|
    |EN_IN(21)|English (India), added in 1.133.0 version|
    |MS_MY(22)|Malay, added in 134 version|
    |VI_VN(23)|Vietnamese, added in 134 version|
    |RU_RU(24)|Russian, added in 134 version|
    |EL_GR(25)|Greek, added in 134 version|
    |AZ_AZ(26)|Azerbaijani, added in 136 version|
    |UR_PK(27)|Urdu (Pakistan), added in 138 version|

  #### **Prototype**

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

Wakeup request

#### Prototype

``` kotlin

data class WakeupRequest(
  val wakeupResponse: Boolean = false,
  val newSession: Boolean = false
)
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|wakeupResponse|Boolean|whether wakeup response will be triggered|
|newSession|Boolean|For kiosk apps that override the conversation layer, set to true to start a new session without previous LLM conversation history from the AI and user. Added in 1.138.0|
---

### WakeupOrigin

Wakeup origin

#### Prototype

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
#### Attributes

|Attribute|Description|
|-|-|
|ROBOX|listening from Robox, apply for V3|
|ANDROID|listening from Android, apply for temi GO and temi Platform|
|TOP_BAR|trigger by touch on top bar wake up button|
|SDK|trigger by SDK wakeup() method from an app|
|ANALOG|trigger by temi internal flow, like dynamic mode auto-listening, continuous conversation etc.|
|UNKNOWN|unknown origin under current SDK version|


<br>

## Override original voice flow <a name="overrideVoiceFlow" />

### Override the NLP <a name="overrideNlu" />

#### Steps

- Add the following `<meta-data>`s under the `<application>` element to AndroidManifest.xml file:

  ``` java
  <!-- Kiosk mode is required -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_nlu" android:value="true" />
  ```

- [Listen to the ASR](#asrListener) and access your own NLP service in its callback method.

- Operate in Launcher: *Settings > Home Screen > Application > Select your skill*. Or request to be the selected Kiosk App by method [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode#requestToBeKioskApp).

#### Required permissions

Selected Kiosk

#### Support from

  0.10.63

---

### Override the ASR <a name="overrideAsr" />

#### Steps

- Add the following `<meta-data>`s under the `<application>` element to AndroidManifest.xml file:

  ``` java
  <!-- Kiosk mode is required -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_stt" android:value="true" />
  ```

- [Listen to the wake-up event](#wakeupWordListener) and access your own ASR service in its callback method.

- Operate in Launcher: *Settings > Kiosk > Select your skill*. Or request to be the selected Kiosk App by method [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode#requestToBeKioskApp).

#### Required permissions

Selected Kiosk

#### Support from

  0.10.70

---

### Override the Conversation layer <a name="overrideConversationLayer" />

#### Steps

- Add the following `<meta-data>`s under the `<application>` element to AndroidManifest.xml file:

  ``` java
  <!-- Kiosk mode is required -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_conversation_layer" android:value="true" />
  ```

- [Listen to onConversationStatusChangedListener](#onConversationStatusChangedListener) and draw the UI according to the data(status, text) from its callback method.

- Operate in Launcher *Settings > App > Kiosk > Select your skill*. Or request to be the selected Kiosk App by method [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode#requestToBeKioskApp).

#### Required permissions

Selected Kiosk

#### Support from

  0.10.72

---

### Override the TTS <a name="overrideTts" />

#### Steps

- Add the following `<meta-data>`s under the `<application>` element to AndroidManifest.xml file:

  ``` java
  <!-- Kiosk mode is required -->
  <meta-data android:name="@string/metadata_kiosk" android:value="true" />

  <meta-data android:name="@string/metadata_override_tts" android:value="true" />
  ```

- Implement interface [ITtsService](#ITtsService) and override methods `speak()`, `cancel()`, `pause()`, `resume()` . temi will call the corresponding method to achieve TTS requirements.

- Use method [setTtsService()](#setTtsService) to bind the instance of the class that implemented [ITtsService](#ITtsService) in the previous step.

- **[Very important]** Use method [publishTtsStatus()](#publishTtsStatus) to publish the TTS status to temi, temi needs these status to respond things(UI, Sequence steps).

- Operate in Launcher *Settings > App > Kiosk > Select your skill*. Or request to be the selected Kiosk App by method [`Robot.getInstance().requestToBeKioskApp()`](https://github.com/robotemi/sdk/wiki/kiosk-mode#requestToBeKioskApp).

- Refer to [Sample](https://github.com/robotemi/sdk/blob/390a4880064af6b550dc24f04ffe6e2634a0af9b/sample/src/main/java/com/robotemi/sdk/sample/MainActivity.kt#L2539-L2628) for details.

#### Required permissions

Selected Kiosk

#### Support from

  0.10.77
