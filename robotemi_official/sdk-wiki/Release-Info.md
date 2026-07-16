## Version 1.138.0
- Released on June 18, 2026.
- Compatibile with temi 138 Launcher, minimum version 19627.

#### Navigation
- Support call tiltAngle() to tilt temi's head when robot is going to a location and position.
- Fix SpeedLevel.VERY_SLOW not respected in `goto` and `gotoPosition` methods.

#### System
- Add es-CO, es-Ar and ur-PK to TTS language.
- Add ur-PK to STT language.
- Add `getOrganizationInfo()` to get organization info.

#### Map
- Add Navigation Zone support. 
  - SDK can get current zone, and all zones.
  - SDK can listen to zone entrance and exit events.
  - SDK can apply zone profiles anytime when robot is going to a location and position.

#### Voice flow
- If app is overriding conversation layer, LLM history can be cleared in `WakeupRequest` with newSession=true.
- Fix `OnConversationStatusChangedListener` sending multiple `SPEAKING` and `IDLE` during LLM TTS.


## Version 1.137.1
- Released on February 9, 2026.
- Compatibile with temi 137 Launcher, minimum version 19232.

#### Navigation
- Add new values to `SpeedLevel`, `VERY_SLOW` for 0.3 m/s and `VERY_HIGH` for 1.2 m/s. Only for used for `goto` and `gotoPosition` methods.

#### System
- Set volume can take a new boolean parameter `showDrawer`

#### Map
   - Add a new boolean parameter `saveHomeBaseIfCharging` to `resetMap` method
   - Add new method `getFloorAndMapData` to get Floor and MapDataModel by floorId.
   - MultiFloor management
      - Add `newFloor` to create new floor.
      - Add `deleteFloor` to delete floor by floorId
      - Add `renameFloor` to rename floor.
      - Add `renameLocation` and `renameLocationOnFloor` rename location and move its position of a floor.
      - Add `deleteLocationOnFloor` to delete location on a floor.
      - Add a new paramter `floorId` to `upsertMapLayer` to support multifloor
      - Add a new paramter `floorId` to `deleteMapLayer` to support multifloor

#### Sequence
   - Add `onSequenceStepChanged` callback to `OnSequencePlayStatusChangedListener` to monitor on going sequence playback.
 
#### temi Go / temi Go Pro
 - Add `getLcdPersistBytes` method to set persisted LCD text.

#### TTS
- TTS can take `<break />` tag. Example: `robot.speak(TtsRequest.create("Let's pause <break time=\"2000ms\"/> and continue."))`

#### Bug Fixes in 137 launcher :
1. Fix pl-PL TTS spoken in English voice
2. Fix Standby settings not persisted correctly.
3. Fix import map from public disk file not working
4. Fix potential ANR in SDK callbacks.
5. Fix repeat in `playSequence` not correctly respected.
6. Fix emergency button status broadcated twice.
7. Fix Sequence TTS might not immediately stopped when started from or stopped by SDK.


## Version 1.136.0
- Released on July 2, 2025.
- Compatibile with temi 136 Launcher, minimum version 18610.

### What's new

#### Navigation
- `SpeedLevel` as the parameter of `goTo` and `goToPosition`can be customized from 0.1-1.5 m/s

#### System
- Improve the `isReady` check on the boot flow, will return false if robot boot is not completed.
-  `OnButtonStatusChangedListener`  will report button `Status.CLICKED`
- Add `OnButtonModeChangedListener` to broadcast `Mode` of buttons if they are disabled or enabled.
- `hideTopBar` takes a new parameter `completely`, this will hide the top bar completely including the white indicator bar.

#### Map
   - Map status query, `isMapLocked`, `isMapLost`
   - Map status listener `OnMapStatusChangedListener`
   - Map name listener `OnMapNameChangedListener`
   - Add `getMapElements` and `getMapImage`, this should be more efficient than `getMapData` if only the desired data is expected.
   - `getReposeStatus` to get repose status actively.
   - `Position` contains a new boolean field `isInMapArea`

 #### Sequence
   - `playSequence` takes a new int paramter `startFromStep` to start sequence from s specific step.
   - `OnSequencePlayStatusChangedListener` contains currently `sequenceId`
   
#### Bug Fixes in 136 launcher :
1. Fix a potential chained crash from Content Provider when Face app is in dying state.
2. Fix a potential crash in getMapData when map elements contains illegal data.
3. Return last cached postion rather than Position(0, 0, 0) on `getPosition` timeout failure.
4. **[BREAKING]** Fix the error that startFaceRecognition withSdkFaces was always enabled. With 136 launcher, use `startFaceRecognition(withSdkFaces = true)` if SDK registerd faces should be used.
5. Goto `10007` going status will be received without debouncing


## Version 1.135.1
- Released on Novement 25, 2024.
- Compatibile with temi 135 Launcher, minimum version 18158.

### What's new

#### Voice flow
- `wakeup()` can take `WakeupRequest` as parameter
- `WakeupRequest` can control `withResponse` so SDK wake up can also have wakeup response.
- Add `WakeupOrigin` to `onWakeupWord()` callback.

#### Navigation
- Follow `beWithMe()` can take `SpeedLevel` as parameter
- Set and get system wide follow speed
- `goTo()` can take `highAccuracyArrival` and `noRotationAtEnd` as parameters
- `goToPosition()` can take `highAccuracyArrival` as parameter.

#### System
- Add `Gender.GIRL` and `Gender.BOY` as TTS voice, supported in 135 Chinese version temi.
- Add `getHomeScreenMode()` to check current home screen mode.

#### Map
- Improve local map import and export support on tar.gz, tgz and tar.

## Version 1.134.1
- Released on August 12, 2024.
- Compatibile with temi 134 Launcher, minimum version 18024.

### What's new

#### Map
- resetMap
- finishMapping
- updateMapName
- continueMapping
- upsertMapLayer
- deleteMapLayer

#### Voice flow
- Support MS_MY. VI_VN, EL_GR, RU_RU as STT language
- Support MS_MY. VI_VN, EL_GR as TTS language
- Add TtsRequest queue support

#### Navigation
- Add navigation path listener
- Repose can assign a position
- Add 10008，10009 status code for go to.

#### System
- Add secondary battery level info.
- Add emergency stop button listener and query.
- Turn off Kiosk mode can assign a return mode.

## Version 1.133.0
- Released on May 2, 2024.
- Compatibile with temi 133 Launcher, minimum version 17878.

### What's new

#### Position
- New API to get current position with `getPosition()`
- Update `addOnCurrentPositionChangedListener`, it will trigger a current position broadcast when the listener is added.

#### Meetings
- Change microphone gain level in temi Meetings with `setMicGainLevel()`

#### Map
- Add map eraser layer support

#### Voice flow
- Support HI_IN and EN_IN as STT language
- Support EN_IN as TTS language
- `wakeup()` can take SttRequest as argument
- `startDefaultNlu()` can take SttLanguage as argument
- `askQuestion()` can take TtsRequest and SttRequest as arguments
- Add continuous conversation support, in `wakeup()` use `timeout` and `multipleConversation` in SttRequest. (an experimental version, to be improved in next versions)


## Version 1.132.1
- Released on December 15, 2023.
- Compatibile with temi 132 Launcher, minimum version 17711.

### What's new

Add layer direction in virtual wall layers of MapDataModel to support one-way virtual wall.

## Version 1.132.0
- Released on November 23, 2023.
- Compatibile with temi 132 Launcher, minimum version 17683.

### What's new

#### General

SDK will not run [forceStop()](https://github.com/robotemi/sdk/blob/825ecc18a4c9a48a80c4516e2948068636e8b75b/sdk/src/main/java/com/robotemi/sdk/TemiSdkServiceConnection.kt#L80-L85) when temi launcher is not detected.

#### Tour
- Launch the tour list page with [startPage(Page.TOURS)](https://github.com/robotemi/sdk/wiki/Utils#page)
- Get all the tours of the robot with [getAllTours()](https://github.com/robotemi/sdk/wiki/temi-Center#getAllTours)
- Start a tour with [playTour()](https://github.com/robotemi/sdk/wiki/temi-Center#playtour)

#### Face recognition
- Return [faceRect](https://github.com/robotemi/sdk/wiki/temi-Center#attributes) in the recognition results

#### Multi-language ASR
- With 132 temi launcher, apps can [wakeup()](https://github.com/robotemi/sdk/wiki/Speech#wakeup) temi with list of [SttLanguage](https://github.com/robotemi/sdk/wiki/Speech#sttlanguage)
- Kiosk app can [setAsrLanguages()](https://github.com/robotemi/sdk/wiki/Speech#setasrlanguages), the settings will persisit when the kiosk app is running.
  - Languages set from wakeup() have higher priority than setAsrLanguages
 - [AsrListener](https://github.com/robotemi/sdk/wiki/Speech#asrlistener) will return both the text and language

#### Meetings
- startTelepresence() will be supported by the same logic as startMeeting() inside temi launcher. Except it doesn't require Meetings permssion
- [startMeeting()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#startmeeting) will take a new parameter `blockRobotInteraction` to disable some UI to protect from misuse on temi side when doing remote call.
- [LinkBasedMeeting](https://github.com/robotemi/sdk/blob/825ecc18a4c9a48a80c4516e2948068636e8b75b/sdk/src/main/java/com/robotemi/sdk/telepresence/LinkBasedMeeting.kt#L89C23-L89C23) will take parameter `blockRobotInteraction` as well

## Version 1.131.4

- Released on August 7, 2023.
- Compatibile with temi 131 Launcher, minimum version 17589.

### What's new

1. Support minimum obstacle distance `minimumObstacleDistance`

    Adding an extra safe margin to your temi in navigation, useful when your temi is wider than normal because of some add-ons.

## Version 1.131.3

- Released on July 31, 2023.
- Compatibile with temi 131 Launcher, minimum version 17579.

### What's new

1. Support face registration from SDK
2. Support backup and load map from local cache
3. Add Turkish tr-TR to TTS language

## Version 1.131.1

- Released on July 5, 2023.
- Compatibile with temi 131 Launcher, minimum version 17533.

### What's new

1. Start a multiparty meeting with `startMeeting()`, with a parameter `firstParticipantJoinedAsHost` to assign first joined participant as meeting host

## Version 1.131.0

- Released on June 13, 2023.
- Compatibile with temi 131 Launcher, minimum version 17487.

### What's new

1. Start a multiparty meeting with `startMeeting()`, require `MEETINGS` permission

2. Turn ON/OFF StandBy Mode with `enableStandBy()`, require `SETTINGS` permission

3. Add Estonian `et-EE` as TTS language.

## Version 1.130.4

- Released on February 2, 2023.
- Compatibile with temi 130 Launcher, minimum version 17276.

### What's new

1. Add Hindi `hi-IN` as TTS language。

2. `Serial.weight` will return 0 when it is not supported by firmware.

## Version 1.130.2

- Released on January 18, 2023.
- Compatibile with temi 130 Launcher, minimum version 17266.

### What's new

1. Create link based meeting from SDK with `createLinkBasedMeeting` API.

    It has the same capability as temi center and temi mobile, to create a link based telepresence meeting to call current robot from any web browser, without the need to have a temi account.

2. Add `MEETINGS` permission for creating link based meetings, and for stopping telepresence.

3. `setInteractionState` in Greet Mode to hold greet mode in the interaction phase.

    Previously in the 3rd step step of Greet Mode, if there is no touching, active detection, moving, speaking, ongoing sequence, etc, temi robot will end interaction phase and continue to post-interaction phase and then finish this round of greeting session.

    Now in 130 version, temi will respect interaction event set from SDK, so 3rd party apps can set interaction state to ON, to keep Greet Mode in the interaction phase, and the app can use all the time it needs to play a music, speak with app's built-in TTS engine. Finally set interaction state to OFF, and temi will bring back its original logic to detect and end this round of greeting session.

4. Boot complete broadcast. `com.robotemi.intent.action.BOOT_COMPLETED`

    temi will send a broadcast to this action, with serial number in `SN` as extra value.

    Note: You still need to wait for the readiness of robot using `OnRobotReadyListener` after this broadcast is received. As there is a small gap of time for your app to connect to temi SDK service.

5. Add `Serial.getLcdColorBytes` util method to change LCD background and text color for temi GO.

6. Add `Serial.weight` to get weight on the trays from temi GO. (Works with MCU 1.8 firmware.)

7. Drag listener `onRobotDragStateChanged`, a callback to notify robot is being dragged when itself is not in a movement state.

8. `stopTelepresence()` with Meetings permission to end ongoing telepresence call.

9. Add `ca_ES` Catalan as TTS language.

10. Current map name will be returned in `MapDataModel` in the field of `mapName`

11. `lowLightMode` ON/OFF will be returned from `CallState` callback in telepresence meeting.

      When LowLightMode is ON, the video feed from the robot will be brightened.

12. Add more examples to sample app to control the LED strip.

13. Add an example to launch temi browser to show a webpage.

14. Add `<queries>` in manfest to fix package visibility issue for Android 11+ tablets connected to temi Platform.

---
## Version 1.129.4

- Released on September 27, 2022.
- Compatibile with temi 129 Launcher, minimum version 17009.

### What's new

- Fix `OnLoadMapStatusChangedListener` not working on V2 robot.

---
## Version 1.129.2

- Released on September 8, 2022.
- Compatibile with temi 129 Launcher, minimum version 17009.

### What's new

- Add serial communication implementation for temi GO. Check it out at https://www.robotemi.com/robots/
- Starting from temi launcher 17009, face recognition will return age and gender for all use cases. (previously only for greet mode visitors)

---

## Version 1.129.1

- Released on August 22, 2022.
- Compatibile with temi 129 Launcher, minimum version 16933.

### What's new

- Add patrol
- onTelepresenceStatusChanged initialization and CallState update
- getAllContact will provide more roles according to organization member and assigned contacts.
- Face recognition callback will provide userType for different recognition results.

---

## Version 1.129.0

- Released on August 10, 2022.
- Compatibile with temi 129 Launcher, minimum version 16892.

### What's new

- Kiosk permission update.
- TtsVoice, StandBy control.
- Multi-floor
- TTS cache


Check full release note:

https://github.com/robotemi/sdk/releases/tag/v1.129.0

---

## Version 0.10.81

- Released on **July 11, 2022**.
- Required minimum version of Launcher is **16398**.

### What's new

#### Changes

- Fix `SafetyLevel.MEDIUM` is returned as `SafetyLevel.HIGH` from [navigationSafety](https://github.com/robotemi/sdk/wiki/Utils#getnavigationsafety) #303

---
## Version 0.10.80

- Released on **March 15, 2022**.
- Required minimum version of Launcher is **16398**.

### What's new

#### Interfaces

- [Distance left to destination changed listener](https://github.com/robotemi/sdk/wiki/Locations#onDistanceToDestinationChangedListener)

#### Changes

- [More parameters for Go-to location](https://github.com/robotemi/sdk/wiki/Locations#goTo)
- [More parameters for Go-to position](https://github.com/robotemi/sdk/wiki/Locations#goToPosition)

---
## Version 0.10.79

- Released on **December 25, 2021**.
- Required minimum version of Launcher is **16013**.

### What's new

### Methods

- [Bypass obstacles during skidJoy](https://github.com/robotemi/sdk/wiki/Movement#skidJoy)

#### Interfaces

- [State changed listener of Greet mode](https://github.com/robotemi/sdk/wiki/temi-Center#onGreetModeStateChangedListener)

---

## Version 0.10.78

- Released on **September 22, 2021**.
- Required minimum version of Launcher is **15567**.

### What's new

#### Methods

- [Control the currently playing Sequence](https://github.com/robotemi/sdk/wiki/temi-Center#controlSequence)
- [Ground depth sensor(Enable/Disable, Get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/Utils#setGroundDepthCliffDetectionEnabled)
- [Does temi have cliff sensor](https://github.com/robotemi/sdk/wiki/Utils#hasCliffSensor)
- [Set/get the mode of cliff sensor](https://github.com/robotemi/sdk/wiki/Utils#setCliffSensorMode)
- [Set/get the level of the head depth sensitivity](https://github.com/robotemi/sdk/wiki/Utils#setHeadDepthSensitivity)
- [Front TOF sensor(Enable/Disable, Get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/Utils#setFrontTOFEnabled)
- [Back TOF sensor(Enable/Disable, Get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/Utils#setBackTOFEnabled)

---

## Version 0.10.77

- Released on **May 1, 2021**.
- Required minimum version of Launcher is **14654**.

### What's new

#### Methods

- [Shutdown](https://github.com/robotemi/sdk/wiki/Utils#shutdown)
- [Set sound mode](https://github.com/robotemi/sdk/wiki/Utils#setSoundMode)
- [Set(get) hard buttons mode](https://github.com/robotemi/sdk/wiki/Utils#setHardButtonMode)
- [Get temi's nick name](https://github.com/robotemi/sdk/wiki/Utils#getNickName)
- [Set(get) system mode(Default, Greet, Privacy)](https://github.com/robotemi/sdk/wiki/Utils#setMode)
- [Kiosk mode(enable, disable, get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/kiosk-mode#setKioskModeOn)
- [AOSP Latin keyboard(get supported latin keyboards, enable specific latin keyboards)](https://github.com/robotemi/sdk/wiki/Utils#getSupportedLatinKeyboards)
- [Cover original temi's voice flow(TTS)](https://github.com/robotemi/sdk/wiki/Speech#overrideTts)

#### Interfaces

- [Velocity changed of movement](https://github.com/robotemi/sdk/wiki/Movement#onMovementVelocityChangedListener)
- [Status changed listener of movement(skidJoy, turnBy)](https://github.com/robotemi/sdk/wiki/Movement#onMovementStatusChangedListener)
- [Continuous face recognition listener](https://github.com/robotemi/sdk/wiki/temi-Center#onContinuousFaceRecognizedListener)

#### Changes

- [Add parameter **language** for TTS](https://github.com/robotemi/sdk/wiki/Speech#ttsLanguage)
- [Add parameter **user ID** for Face recognition result](https://github.com/robotemi/sdk/wiki/temi-Center#contactModel)
- [Add parameters **image key** and **description** for Seqeunce](https://github.com/robotemi/sdk/wiki/temi-Center#sequenceModel)
- [Add status **reposing** for Go to location](https://github.com/robotemi/sdk/wiki/Locations#OnGoToLocationStatusChangedListener)
- [Add getting all sequences by tags](https://github.com/robotemi/sdk/wiki/temi-Center#getAllSequences)
- [Separate **Detection Mode** from **Track User**](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#detectionModeAndTrackUser)
- [Add parameter **speed coefficient** for turnBy, tiltBy, tiltAngle](https://github.com/robotemi/sdk/wiki/Movement#turnBy)

---

## Version 0.10.76

- Released on **February 8, 2021**.
- Required minimum version of Launcher is **14315**.

### What's new

#### Methods

- [Load the map by map ID and the specific position of the target map](https://github.com/robotemi/sdk/wiki/Locations#loadMapWithPosition)

---

## Version 0.10.75

- Released on **January 26, 2021**.
- Required minimum version of Launcher is **14293**.

### What's new

#### Methods

- [Mute Alexa](https://github.com/robotemi/sdk/wiki/Utils#muteAlexa)

---

## Version 0.10.74

- Released on **January 15, 2021**.
- Required minimum version of Launcher is **14293**.

### What's new

#### Methods

- [Get map list](https://github.com/robotemi/sdk/wiki/Locations#getMapList)
- [Load map by map ID](https://github.com/robotemi/sdk/wiki/Locations#loadMap)
- [Protection by password](https://github.com/robotemi/sdk/wiki/Utils#setLocked)

#### Interfaces

- [Status changed listener of loading map](https://github.com/robotemi/sdk/wiki/Locations#onLoadMapStatusChangedListener)
- [Disabled features list updated listener](https://github.com/robotemi/sdk/wiki/Utils#onDisabledFeatureListUpdatedListener)

#### Changes

- [Get virtual walls, navigation paths and locations from map data](https://github.com/robotemi/sdk/wiki/Locations#getMapData)

---

## Version 0.10.73

- Released on **November 23, 2020**.
- Required minimum version of Launcher is **14048**.

### What's new

#### Methods

- [Conver original UI of conversation layer](https://github.com/robotemi/sdk/wiki/Speech#overrideConversationLayer)
- [Repositioning](https://github.com/robotemi/sdk/wiki/Locations#repose)
- [Restart temi](https://github.com/robotemi/sdk/wiki/Utils#restart)
- [Start system internal page(Setttings, Contacts, Locations, Map Editor, temi's apps)](https://github.com/robotemi/sdk/wiki/Utils#startPage)
- [Get members'(Administrator, Owners) availability status(temi Center, temi App)](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#getMembersStatus)
- [Start telepresence to temi Center](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#startTelepresence)
- [Play specific sequence and set wheather show the player panel when sequence is playing](https://github.com/robotemi/sdk/wiki/temi-Center_chn#playSequenceWithPlayer)

#### Interfaces

- [Status and text changed listener of conversation layer](https://github.com/robotemi/sdk/wiki/Speech#onConversationStatusChangedListener)
- [**Wave form** data of audio visualization capture changed listener of TTS](https://github.com/robotemi/sdk/wiki/Speech#onTtsVisualizerWaveFormDataChangedListener)
- [**fft** data of audio visualization capture changed listener of TTS](https://github.com/robotemi/sdk/wiki/Speech#onTtsVisualizerFttDataChangedListener)
- [Status changed listener of repositioning](https://github.com/robotemi/sdk/wiki/Locations#onReposeStatusChangedListener)

### Changes

- [Allow to modify the system volume in no-Kiosk mode](https://github.com/robotemi/sdk/wiki/Utils#setVolume)

---

## Version 0.10.71

- Released on **September 14, 2020**.
- Required minimum version of Launcher is **13716**.

### What's new

- Fixed getting empty data in large map

---

## Version 0.10.70

- Released on **July 31, 2020**.
- Required minimum version of Launcher is **13646**.

### What's new

#### Methods

- [temi Permission](https://github.com/robotemi/sdk/wiki/permission)
- [Request to be the selected Kiosk skill](https://github.com/robotemi/sdk/wiki/kiosk-mode#requestToBeKioskApp)
- [Top movement status green badge(Enable/Disable, Get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/Utils#setTopBadgeEnabled)
- [Detection mode(Enable/Disable, Get wheather it is enabled, Set the max distance for detecting)](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#setDetectionModeOn)
- [Auto return(Enable/Disable, Get wheather it is enabled)](https://github.com/robotemi/sdk/wiki/Utils#setAutoReturnOn)
- [Set/Get current volume](https://github.com/robotemi/sdk/wiki/Utils#setVolume)
- [Set/Get navigation obstacles safety level](https://github.com/robotemi/sdk/wiki/Utils#setNavigationSafetyLevel)
- [Set/Get go to speed level](https://github.com/robotemi/sdk/wiki/Utils#setGoToSpeed)
- [Start/Stop face recognition](https://github.com/robotemi/sdk/wiki/temi-Center#startFaceRecognition)
- [Get/Play sequences](https://github.com/robotemi/sdk/wiki/temi-Center#getAllSequences)
- [Go to position](https://github.com/robotemi/sdk/wiki/Locations#goToPosition)
- [Get map data](https://github.com/robotemi/sdk/wiki/Locations#getMapData)
- [Trigger default NLU service](https://github.com/robotemi/sdk/wiki/Speech#startDefaultNlu)
- [Get wheather wake-up is enabled](https://github.com/robotemi/sdk/wiki/Utils#isWakeupDisabled)
- [Override default ASR](https://github.com/robotemi/sdk/wiki/Speech#overrideAsr)

#### Interfaces

- [temi permission request result callback](https://github.com/robotemi/sdk/wiki/permission#onRequestPermissionResultListener)
- [Distance from current position to locations changed listener](https://github.com/robotemi/sdk/wiki/Locations#onDistanceToLocationChangedListener)
- [Current position changed listener](https://github.com/robotemi/sdk/wiki/Locations#onCurrentPositionChangedListener)
- [Sequence playing status changed listener](https://github.com/robotemi/sdk/wiki/temi-Center#onSequencePlayStatusChangedListener)
- [Robot lifted listener](https://github.com/robotemi/sdk/wiki/Movement#onRobotLiftedListener)
- [Detection mode data(Distance, angle..) changed listener](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#onDetectionDataChangedListener)
- [Face recognized listener](https://github.com/robotemi/sdk/wiki/temi-Center#onFaceRecognizedListener)
- [temi SDK error callback](https://github.com/robotemi/sdk/wiki/Utils#onSdkExceptionListener)

---

## Version 0.10.65

- Released on **May 19, 2020**.
- Required minimum version of Launcher is **13365**.

### What's new

#### Methods

- [Get Version of Launcher](https://github.com/robotemi/sdk/wiki/Utils#getLauncherVersion)
- [Get Version of Robox](https://github.com/robotemi/sdk/wiki/Utils#getRoboxVersion)

#### Interfaces

- [Telepresence Event Listener](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#addOnTelepresenceEventChangedListener)

---

## Version 0.10.63

- Released on **April 8, 2020**.
- Required minimum version of Launcher is **13146**.

### What's new

#### Methods

- [temi speak actively and wait for the user to answer](https://github.com/robotemi/sdk/wiki/Speech#askQuestion)
- [Finish Conversation (Stop recording for ASR)](https://github.com/robotemi/sdk/wiki/Speech#finishConversation)
- [Cover temi’s original voice flow(NLP)](https://github.com/robotemi/sdk/wiki/Speech#overrideNlu)

---

## Version 0.10.6

- Released on **January 22, 2020**.
- Required minimum version of Launcher is **12668**.

### What's new

#### Methods

- [Disable(Enable) Hard Buttons](https://github.com/robotemi/sdk/wiki/Utils#setHardButtonsDisabled)

---

## Version 0.10.53

- Released on **December 10, 2019**.
- Required minimum version of Launcher is **11969**.

### What's new

#### Methods

- [Constraint Follow](https://github.com/robotemi/sdk/wiki/Follow#constraintBeWith)
- [Toggle Privacy Mode](https://github.com/robotemi/sdk/wiki/Utils#setPrivacyMode)

#### Interfaces

- [Privacy mode status changed listener](https://github.com/robotemi/sdk/wiki/Utils#onPrivacyModeChangedListener)
- [User Interaction and Detection](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction)
- [Battery Status Listener](https://github.com/robotemi/sdk/wiki/Utils#OnBatteryStatusChangedListener)
- [ASR Listener](https://github.com/robotemi/sdk/wiki/Speech#asrListener)

#### Note

- There is no distinction between Chinese and English Launchers in using temi SDK from 0.10.53 version. That is, the same SDK can be used for Chinese and English robots. The integration mode is as follow:

  ``` groovy
  implementation 'com.robotemi:sdk:0.10.53'
  ```

- Welcoming Mode Listeners that were released in previous SDK versions are now replaced with User Detection & Interaction listeners. This is to provide more feedback on temi's state machines and more flexibility for development.

---

## Version 0.10.49

- Released on **November 14, 2019**.
- Required minimum version of Launcher is **11642**.

### What's new

#### Methods

- [Wakeup](https://github.com/robotemi/sdk/wiki/Speech#wakeup)
- [Get Wakeup Word](https://github.com/robotemi/sdk/wiki/Speech#getWakeupWord)

---

## Version 0.10.44

- Released on **September 19, 2019**.
- Required minimum version of Launcher is **11254**.

### What's new

#### Methods

- [Kiosk Mode](https://github.com/robotemi/sdk/wiki/Kiosk-Mode)
- [Toggle Wakeup](https://github.com/robotemi/sdk/wiki/Utils#toggleWakeup)
- [Toggle Navigation Billboard](https://github.com/robotemi/sdk/wiki/Utils#toggleNavigationBillboard)
- [Delete Location](https://github.com/robotemi/sdk/wiki/Locations#deleteLocation)

---

## Version 0.10.43

- Released on **September 5, 2019**.
- Required minimum version of Launcher is **10723**.

### What's new

#### Methods

- [Get Battery Data](https://github.com/robotemi/sdk/wiki/Utils#getBatteryData)
- [Get Robot Serial Number](https://github.com/robotemi/sdk/wiki/Utils#getSerialNumber)
- [Stop Movement](https://github.com/robotemi/sdk/wiki/Movement#stopMovement)
