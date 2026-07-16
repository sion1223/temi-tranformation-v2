## 版本 1.138.0
- 发布于 2026年6月18日.
- 要求最小 Launcher 版本为 138 版本，版本号 19627.

#### 导航 Navigation
- 支持在 `goTo` 和 `goToPosition` 导航时通过 `tiltAngle()` 方法控制 temi 头部倾斜角度。
- 修复 `goTo` 和 `goToPosition` 方法中 `SpeedLevel.VERY_SLOW` 未生效的问题。

#### 系统 System
- TTS 语言新增 es-CO、es-Ar 和 ur-PK。
- STT 语言新增 ur-PK。
- 新增 `getOrganizationInfo()` 方法，用于获取组织信息。

#### 地图 Map
- 新增导航区域（Navigation Zone）支持。
  - SDK 可获取当前所在区域及全部区域。
  - SDK 可监听进入和离开区域事件。
  - 在 `goTo` 和 `goToPosition` 导航过程中，SDK 可随时应用区域配置（速度、避障、避障距离）。

#### 语音流程 Voice flow
- 若应用覆盖了对话层，可在 `WakeupRequest` 中设置 `newSession=true` 清除 LLM 对话历史。
- 修复 LLM TTS 播放期间 `OnConversationStatusChangedListener` 多次发送 `SPEAKING` 和 `IDLE` 状态的问题。

## 版本 1.137.1
- 发布于 2026年2月9日.
- 要求最小 Launcher 版本为 137 版本，版本号 19232.

#### 导航 Navigation
- 新增 `VERY_SLOW` 和 `VERY_HIGH` 两个值到 `SpeedLevel` 中，分别对应 0.3 m/s 和 1.2 m/s。仅用于 `goTo` 和 `goToPosition` 方法。

#### 系统 System
- `setVolume` 方法新增 `showDrawer` 参数，用于控制是否显示音量抽屉。

#### 地图 Map
   - `resetMap` 方法新增 `saveHomeBaseIfCharging` 参数，用于控制是否在充电桩上调用时保存当前位置为充电桩。
   - 新增 `getFloorAndMapData` 方法，用于获取指定楼层的地图数据。
   - 多楼层管理
      - 新增 `newFloor` 方法，用于创建新楼层。
      - 新增 `deleteFloor` 方法，用于删除指定楼层。
      - 新增 `renameFloor` 方法，用于重命名楼层。
      - 新增 `renameLocation` 和 `renameLocationOnFloor` 方法，用于重命名地点和移动其位置。
      - 新增 `deleteLocationOnFloor` 方法，用于删除指定楼层上的地点。
      - 新增 `floorId` 参数到 `upsertMapLayer` 方法，用于支持多楼层。
      - 新增 `floorId` 参数到 `deleteMapLayer` 方法，用于支持多楼层。

#### 导览 Sequence
- 新增 `onSequenceStepChanged` 回调到 `OnSequencePlayStatusChangedListener` 中，用于监听正在播放的导览步骤。

#### temi Go / temi Go Pro
- 新增 `getLcdPersistBytes` 方法，用于设置持久化的 LCD 文本。

#### TTS
- TTS 支持 `<break />` 标签。示例：`robot.speak(TtsRequest.create("吸气。<break time=\"2000ms\"/>呼气。"))`

#### 修复
- 修复 137 版本 pl-PL 语言的 TTS 发音为英语的问题
- 修复 Standby 设置未正确持久化的问题
- 修复从公共磁盘文件导入地图不工作的问题
- 修复 SDK 回调中潜在的 ANR 问题
- 修复 `playSequence` 中的 repeat 参数未正确处理的问题
- 修复紧急按钮状态广播两次的问题
- 修复 Sequence TTS 可能在 SDK 启动或停止时未立即停止的问题

## 版本 1.136.0
- 发布于 2025年7月2日.
- 要求最小 Launcher 版本为 136 版本，版本号 18610.

### 新增

#### 导航 Navigation
- `SpeedLevel` 作为 `goTo` 和 `goToPosition` 的参数，可以自定义从 0.1-1.5 m/s

#### 系统 System
- 改进 `isReady` 检查，如果机器人启动未完成，将返回 false
- 改进 `OnButtonStatusChangedListener` ，将上报按键点击的状态 `Status.CLICKED`
- 添加 `OnButtonModeChangedListener` 监听，用于广播按钮的 `Mode` ，通知按键禁用或启用
- `hideTopBar` 新增 `completely` 参数，这将完全隐藏顶部栏，包括顶部小白条

#### 地图 Map
- 地图状态查询，`isMapLocked`, `isMapLost`
- 地图状态监听 `OnMapStatusChangedListener`
- 地图名称监听 `OnMapNameChangedListener`
- `getMapData` 可被两个新增方法 `getMapElements` 和 `getMapImage` 的组合替代，同时 优化 `getMapData` 效率。
- `getReposeStatus` 可主动获取重定位状态
- `Position` 新增 `isInMapArea` 字段，用于判断当前位置是否在地图范围内

#### 导览 Sequence
- `playSequence` 新增 `startFromStep` 参数，从指定步骤开始播放导览
- `OnSequencePlayStatusChangedListener` 新增 `sequenceId` 字段，用于标识当前播放的导览

#### 修复
- 修复 136 版本 Content Provider 潜在的连锁崩溃问题
- 修复 `getMapData` 当地图元素包含非法数据时潜在的崩溃问题，
- 修复 `getPosition` 超时失败时返回会返回 Position(0, 0, 0) 的问题，修改为返回最后一次缓存的位置
- **[非兼容性变更]** 修复 `startFaceRecognition` 始终启用 SDK 注册的人脸的问题，使用 136 版本时，请使用 `startFaceRecognition(withSdkFaces = true)` 以主动启用 SDK 注册的人脸用于识别
- 修复导航状态 `10007` 可能会被 debouncing 而无法立即收到的问题

## 版本 1.135.1
- 发布于 2024年11月25日
- 要求最小 Launcher 版本为 135 版本，版本号 18158.

### 新增

#### 语音 Voice
- 唤醒 `wakeup()` 方法，新增 `WakeupRequest` 参数
- `WakeupRequest` 支持 `withResponse` 控制，来自 SDK 的唤醒可以激活唤醒应答
- 唤醒 `onWakeupWord()` 回调，新增 `WakeupOrigin` 参数

#### 导航 Navigation
- 跟随 `beWithMe()` 方法，新增 `SpeedLevel` 参数
- 配置、读取跟随速度
- 地点移动 `goTo()` 方法，新增 `highAccuracyArrival`，`noRotationAtEnd` 参数
- 坐标移动 `goToPosition()` 方法，新增 `highAccuracyArrival` 参数

#### 系统 System
- 新增 `Gender.GIRL`，`Gender.BOY` TTS 音色，需配合 135 中文 temi 使用
- 新增 `getHomeScreenMode()` 方法，获取当前主屏幕模式

#### 地图 Map
- 改进本地地图导入导出时对文件类型 tar.gz，tgz，tar 的支持.


## 版本 1.134.1
- 发布于 2024年8月12日.
- 要求最小 Launcher 版本为 134 版本，版本号 18024.

### 新增

#### 地图 Map
- resetMap
- finishMapping
- updateMapName
- continueMapping
- upsertMapLayer
- deleteMapLayer

#### 语音 Voice
- 支持 MS_MY. VI_VN, EL_GR, RU_RU 作为 STT 语言
- 支持 MS_MY. VI_VN, EL_GR 作为 TTS 语言
- TtsRequest 支持队列

#### 导航 Navigation
- 增加实时导航规划路径监听
- 重定位支持指定位置
- 新增 10008，10009 导航状态码

#### 系统 System
- 增加第二块电池电量显示
- 增加急停按钮状态监听和查询
- 关闭 Kiosk mode 可指定返回主页的模式。

## 版本 1.133.0
- 发布于 2024年5月2日.
- 要求最小 Launcher 版本为 133 版本，版本号 17878.

### 新增

#### 位置 Position
- 主动获取当前位置、角度、平板角度数据 `getPosition()`
- `addOnCurrentPositionChangedListener` 增加监听后将立即收到一次地点数据推送。

#### 会议 Meetings
- 设置 temi 会议麦克风音量增益 `setMicGainLevel()`

#### 地图 Map
- 增加对 133 新功能 地图擦 图层的支持

#### 语音 Voice
- 支持 HI_IN，EN_IN 作为 STT 语言
- 支持 EN_IN 作为 TTS 语言
- `wakeup()` 可以接收 SttRequest 参数
- `startDefaultNlu()` 可以接收 SttLanguage 参数
- `askQuestion()` 可以接收 TtsRequest 和 SttRequest 作为参数
- 增加连续对话的支持，在 `wakeup()` 中对 SttRequest 传入 `timeout` 和 `multipleConversation` 做到长超时和多轮对话。（目前仍是实验阶段，将在后续版本改善）

## 版本 1.132.1
- 发布于 2023年12月15日.
- 要求最小 Launcher 版本为 132 版本，版本号 17711.

### 新增

在 MapDataModel -> Layer 中加入 `layerDirection`，用来表示单向虚拟墙

## 版本 1.132.0
- 发布于 2023年11月23日.
- 要求最小 Launcher 版本为 132 版本，版本号 17683.

### 新增

#### 系统

SDK 在未检测到 temi launcher 时不再会执行 [forceStop()](https://github.com/robotemi/sdk/blob/825ecc18a4c9a48a80c4516e2948068636e8b75b/sdk/src/main/java/com/robotemi/sdk/TemiSdkServiceConnection.kt#L80-L85) 

#### 讲解
- 启动讲解列表页 [startPage(Page.TOURS)](https://github.com/robotemi/sdk/wiki/Utils_chn#page)
- 获取讲解列表 [getAllTours()](https://github.com/robotemi/sdk/wiki/temi-Center_chn#getAllTours)
- 启动指定讲解 [playTour()](https://github.com/robotemi/sdk/wiki/temi-Center_chn#playtour)

#### 人脸识别
- 在识别结果中返回 [faceRect](https://github.com/robotemi/sdk/wiki/temi-Center_chn#attributes)

#### 多语言 ASR
- 使用 132 temi launcher，应用可以调用 [wakeup()](https://github.com/robotemi/sdk/wiki/Speech#wakeup) 并传入希望 temi 可以听懂的 [SttLanguage](https://github.com/robotemi/sdk/wiki/Speech#sttlanguage) 语言列表
- 商店应用可以通过调用 [setAsrLanguages()](https://github.com/robotemi/sdk/wiki/Speech#setasrlanguages) 来指定其运行期间系统支持的 ASR 语言。
  - 通过 wakeup() 启动的对话，如果同时传入了语言列表，将优先于 setAsrLanguages 指定的语言被当前对话所使用。
 - [AsrListener](https://github.com/robotemi/sdk/wiki/Speech_chn#asrlistener) 在返回文字的同时也会返回语言。

#### 会议
- startTelepresence() 在 temi launcher 内部将等同于 startMeeting() 方法处理。唯一区别是不需要 Meetings 权限。
- [startMeeting()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#startmeeting) 支持新的参数 `blockRobotInteraction`。将会禁用部分 temi UI 防止远程通话被干扰。
- [LinkBasedMeeting](https://github.com/robotemi/sdk/blob/825ecc18a4c9a48a80c4516e2948068636e8b75b/sdk/src/main/java/com/robotemi/sdk/telepresence/LinkBasedMeeting.kt#L89C23-L89C23) 同样支持 `blockRobotInteraction` 参数。

## 版本 1.131.4

- 发布于 2023年8月7日.
- 要求最小 Launcher 版本为 131 版本，版本号 17589.

### 新增
1. 支持最小障碍距离配置 `minimumObstacleDistance`

    如果你的 temi 因加上了配件而导致宽度增加，这个接口可以增大其导航时的安全距离。

## 版本 1.131.1

- 发布于 2023年7月31日.
- 要求最小 Launcher 版本为 131 版本，版本号 17579.

### 新增

1. 支持本地注册人脸识别
2. 支持本地备份、导入地图
3. 加入 土耳其语 `tr-TR` 作为 TTS 语言

## 版本 1.131.0

- 发布于 2023年6月13日.
- 要求最小 Launcher 版本为 131 版本，版本号 17487.

### 新增

1. 开启多方通话会议 `startMeeting()`，需要 `MEETINGS` 权限。

2. 开关待机模式 `enableStandBy()`，需要 `SETTINGS` 权限。

3. 加入 爱沙尼亚语 `et-EE` 语作为 TTS 语言。

## 版本 1.130.4

- 发布于 2023年2月2日.
- 要求最小 Launcher 版本为 130 版本，版本号 17276.

### 新增

1. 加入 印地语 `hi-IN` 语作为 TTS 语言。

2. `Serial.weight` 属性在不支持重量检测的固件上将返回 0

## 版本 1.130.2

- 发布于 2023年1月18日.
- 要求最小 Launcher 版本为 130 版本，版本号 17266.

### 新增

1. 通过 SDK 为当前 temi 创建通话链接 `createLinkBasedMeeting`.

    该方法与 temi 手机应用及 temi 管理平台功能一致，可创建一个网络视频会议通话连接，从而在任何一个浏览器都可以与当前 temi 进行通话，而无需注册为 temi 用户

2. 添加 `MEETINGS` 权限用于创建通话连接，及结束当前通话.

3. `setInteractionState` 使迎宾模式保持在交互状态.

    此前，在迎宾模式的第三阶段，交互阶段。如果没有触摸屏幕、有效的人体检测、机器人移动、语音对话、导览、通话等行为，temi 将在指定时间后自动结束结束交互状态，进入第四阶段并结束本轮迎宾。

    从 130 版本开始，temi 会接受从 SDK 发过来的交互状态。第三方应用可以 setInteractionState 为 true，使迎宾模式保持在有交互状态。此时应用可以去播放音乐、视频，而无需担心进度被 temi 内置逻辑给打断。当应用判断自身交互结束后，可 setInteractionState 为 false。则 temi 会再次检查自身的无交互逻辑来控制结束本轮迎宾。

4. 开机完成广播. `com.robotemi.intent.action.BOOT_COMPLETED`

    temi 会向这一 action 发送广播，并带有 `SN` 作为 String extra 值。.

    请注意：即使收到这一广播，仍不代表可以调用 Robot.getInstance() 之后的方法。建议收到广播后仍去监听 `OnRobotReadyListener` 回调。因为即使 temi 启动完成，三方应用与 temi 通过 SDK 建立连接仍是一个独立的、短暂的耗时过程。只有连接建立完成后方能调用 Robot 方法。

5. 加入 `Serial.getLcdColorBytes`  util 方法用于控制 temi GO 背后的 LCD 屏幕颜色和文字颜色。

6. 加入 `Serial.weight` util 方法用于读取 temi GO 托盘的置物重量。 需要 1.8 版本的 MCU 固件支持。

7. 加入拖拽监听，产生 `onRobotDragStateChanged` 回调，表示 temi 在非主动移动的情况下被拖动。

8. 加入 `stopTelepresence()` 接口，需要 Meetings 权限，用于结束当前通话。

9. 加入 `ca_ES` 加泰罗尼亚语 TTS 语言选项

10. 在 `MapDataModel` 中返回当前地图的名称 `mapName`

11. 在 `CallState` 回调中加入 `lowLightMode` 表示夜景增强模式的开关.

      当夜景增强模式启动后，temi 视频通话的摄像头画面将会被算法优化，提高画面亮度。

12. 在示例应用中加入更多控制 temi GO LED 灯带的参考用例。

13. 在示例应用中加入启动 temi 浏览器展示指定网页的参考用例。

14. 声明文件中加入 `<queries>` 用于修复 temi Platform 上使用 Android 11 及以上的平板设备，使用 SDK 时发生的应用崩溃。

---
## 版本 1.129.4

- 发布于 **2022年9月27日**.
- 要求最小 Launcher 版本为 129 版本，版本号 **17009**.

### 新增
- 修复 `OnLoadMapStatusChangedListener` 在 temi V2 上的异常

---

## 版本 1.129.2

- 发布于 **2022年9月8日**.
- 要求最小 Launcher 版本为 129 版本，版本号 **17009**.

### 新增
- 加入 temi GO 的串口通讯支持，查看 temi GO 信息，请访问 https://www.robotemi.com/robots/
- 从 17009 版本起, 人脸识别结果会在会在全部场景下返回检测结果的性别和年龄 (此前仅对迎宾模式下的访客提供)

---

## 版本 1.129.1

- 发布于 **2022年8月22日**.
- 要求最小 Launcher 版本为 129 版本，版本号 **16933**.

### 新增
- patrol 巡逻接口
- onTelepresenceStatusChanged 初始化及回调状态更新。
- getAllContact 返回联系人角色更新，与组织角色及机器人联系人对应
- 人脸识别返回结果用户类型更新

---

## 版本 1.129.0

- 发布于 **2022年8月10日**.
- 要求最小 Launcher 版本为 **16892**.

### 新增
- Kiosk 权限更新
- TtsVoice, StandBy 控制接口.
- 多楼层
- TTS 缓存

查看完整更新日志：

https://github.com/robotemi/sdk/releases/tag/v1.129.0

---

## 版本 0.10.81

- 发布于 **2022年7月11日**.
- 要求最小 Launcher 版本为 **16398**.

### 新增

#### 变更

- 修复 [navigationSafety](https://github.com/robotemi/sdk/wiki/Utils_chn#getnavigationsafety) 接口将 `SafetyLevel.MEDIUM` 返回为 `SafetyLevel.HIGH` 的问题 #303

---
## 版本 0.10.80

- 发布于 **2022年3月15日**.
- 要求最小 Launcher 版本为 **16398-chinaTencent**.

### 新增

#### 接口

- [到目的地剩余距离变化监听器](https://github.com/robotemi/sdk/wiki/Locations_chn#onDistanceToDestinationChangedListener)

#### 变更

- [为 go-to 地点新增更多参数](https://github.com/robotemi/sdk/wiki/Locations_chn#goTo)
- [为 go-to 坐标新增更多参数](https://github.com/robotemi/sdk/wiki/Locations_chn#goToPosition)

---

## 版本 0.10.79

- 发布于 **2021年12月25日**。
- 要求最小 Launcher 版本为 **16013-chinaTencent**。

### 新增

#### 方法

- [遥控（skidJoy）时绕过障碍物](https://github.com/robotemi/sdk/wiki/Movement_chn#skidJoy)

#### 接口

- [迎宾模式状态变化监听器](https://github.com/robotemi/sdk/wiki/temi-Center_chn#onGreetModeStateChangedListener)

---

## 版本 0.10.78

- 发布于 **2021年9月23日**。
- 要求最小 Launcher 版本为 **15567-chinaTencent**。

### 新增

#### 方法

- [控制当前正在播放的导览](https://github.com/robotemi/sdk/wiki/temi-Center_chn#controlSequence)
- [地面深度传感器（开、关、获取其是否可用）](https://github.com/robotemi/sdk/wiki/Utils_chn#setGroundDepthCliffDetectionEnabled)
- [是否有悬崖传感器](https://github.com/robotemi/sdk/wiki/Utils_chn#hasCliffSensor)
- [设置、获取悬崖传感器模式](https://github.com/robotemi/sdk/wiki/Utils_chn#setCliffSensorMode)
- [设置、获取头部深度传感器灵敏度级别](https://github.com/robotemi/sdk/wiki/Utils_chn#setHeadDepthSensitivity)
- [前部 TOF 传感器（开、关、获取其是否可用）](https://github.com/robotemi/sdk/wiki/Utils_chn#setFrontTOFEnabled)
- [后部 TOF 传感器（开、关、获取其是否可用）](https://github.com/robotemi/sdk/wiki/Utils_chn#setBackTOFEnabled)

---

## 版本 0.10.77

- 发布于 **2021年5月1日**。
- 要求最小 Launcher 版本为 **14654-chinaTencent**。

### 新增

#### 方法

- [关机](https://github.com/robotemi/sdk/wiki/Utils_chn#shutdown)
- [设置声音模式](https://github.com/robotemi/sdk/wiki/Utils_chn#setSoundMode)
- [设置（获取）硬件按钮模式](https://github.com/robotemi/sdk/wiki/Utils_chn#setHardButtonMode)
- [获取机器人名称](https://github.com/robotemi/sdk/wiki/Utils_chn#getNickName)
- [设置（获取）系统模式（默认、迎宾、隐私模式）](https://github.com/robotemi/sdk/wiki/Utils_chn#setMode)
- [打开（关闭、获取是否打开）商店模式（Kiosk）](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#setKioskModeOn)
- [AOSP 拉丁键盘（获取所支持的语言种类、启用指定语言）](https://github.com/robotemi/sdk/wiki/Utils_chn#getSupportedLatinKeyboards)
- [覆盖 temi 原有语音流（TTS）](https://github.com/robotemi/sdk/wiki/Speech_chn#overrideTts)

#### 接口

- [机器人移动速度变化监听器](https://github.com/robotemi/sdk/wiki/Movement_chn#onMovementVelocityChangedListener)
- [机器人移动状态变化监听器（skidJoy，turnBy）](https://github.com/robotemi/sdk/wiki/Movement_chn#onMovementStatusChangedListener)
- [连续人脸识别监听器](https://github.com/robotemi/sdk/wiki/temi-Center_chn#onContinuousFaceRecognizedListener)

#### 变更

- [TTS 播报新增 **语言** 参数](https://github.com/robotemi/sdk/wiki/Speech_chn#ttsLanguage)
- [人脸识别结果新增 **用户ID** 参数](https://github.com/robotemi/sdk/wiki/temi-Center_chn#contactModel)
- [导览（Sequence）新增 **图片 imageKey**、**描述** 参数、**标签**](https://github.com/robotemi/sdk/wiki/temi-Center_chn#sequenceModel)
- [导航新增 **重定位中** 状态](https://github.com/robotemi/sdk/wiki/Locations_chn#OnGoToLocationStatusChangedListener)
- [新增根据标签获取所有导览集合](https://github.com/robotemi/sdk/wiki/temi-Center_chn#getAllSequences)
- [将 **用户检测模式** 与 **原地跟随（原欢迎模式）** 分离](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#detectionModeAndTrackUser)
- [turnBy、tiltBy、tiltAngle新增 **速度系数** 参数](https://github.com/robotemi/sdk/wiki/Movement_chn#turnBy)

---

## 版本 0.10.76

- 发布于 **2021年2月8日**。
- 要求最小 Launcher 版本为 **14315-chinaTencent**。

### 新增

#### 方法

- [在指定的位置坐标上通过地图 ID 来加载地图](https://github.com/robotemi/sdk/wiki/Locations_chn#loadMapWithPosition)

---

## 版本 0.10.74

- 发布于 **2021年1月15日**。
- 要求最小 Launcher 版本为 **14293-chinaTencent**。

### 新增

#### 方法

- [获取地图列表](https://github.com/robotemi/sdk/wiki/Locations_chn#getMapList)
- [加载地图](https://github.com/robotemi/sdk/wiki/Locations_chn#loadMap)
- [密码保护（开启，关闭，状态获取）](https://github.com/robotemi/sdk/wiki/Utils_chn#setLocked)

#### 接口

- [加载地图状态变化监听器](https://github.com/robotemi/sdk/wiki/Locations_chn#onLoadMapStatusChangedListener)
- [系统功能不可用列表更新监听器](https://github.com/robotemi/sdk/wiki/Utils_chn#onDisabledFeatureListUpdatedListener)

#### 变更

- [地图数据新增虚拟墙、导航路径、地点坐标数据](https://github.com/robotemi/sdk/wiki/Locations_chn#getMapData)

---

## 版本 0.10.73

- 发布于 **2020年11月23日**。
- 要求最小 Launcher 版本为 **14048-chinaTencent**。

### 新增

#### 方法

- [覆盖原有对话层用户交互界面](https://github.com/robotemi/sdk/wiki/Speech_chn#overrideConversationLayer)
- [重定位](https://github.com/robotemi/sdk/wiki/Locations_chn#repose)
- [重启 temi](https://github.com/robotemi/sdk/wiki/Utils_chn#restart)
- [启动系统界面（设置，联系人，位置，地图编辑器，主页，应用列表）](https://github.com/robotemi/sdk/wiki/Utils_chn#startPage)
- [获取机器人成员（管理员，主人）的空闲状态（temi 移动应用，temi 管理平台）](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#getMembersStatus)
- [向 temi 管理平台拨打视频通话](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#startTelepresence)
- [播放指定导览并设置是否显示导览播放器界面](https://github.com/robotemi/sdk/wiki/temi-Center_chn#playSequenceWithPlayer)

#### 接口

- [对话层状态、文本变化监听器](https://github.com/robotemi/sdk/wiki/Speech_chn#onConversationStatusChangedListener)
- [TTS 音频可视化 **wave form** 数据变化监听器](https://github.com/robotemi/sdk/wiki/Speech_chn#onTtsVisualizerWaveFormDataChangedListener)
- [TTS 音频可视化 **fft** 数据变化监听器](https://github.com/robotemi/sdk/wiki/Speech_chn#onTtsVisualizerFttDataChangedListener)
- [重定位状态变化监听器](https://github.com/robotemi/sdk/wiki/Locations_chn#onReposeStatusChangedListener)

### 变更

- [允许在非 Kiosk 模式修改系统音量](https://github.com/robotemi/sdk/wiki/Utils_chn#setVolume)

---

## 版本 0.10.71

- 发布于 **2020年9月14日**。
- 要求最小 Launcher 版本为 **13716-chinaTencent**。

### 新增

- 修复在大地图中获取到空数据问题

---

## 版本 0.10.70

- 发布于 **2020年7月31日**。
- 要求最小 Launcher 版本为 **13646-chinaTencent**。

### 新增

#### 方法

- [temi 权限机制（检查、请求权限）](https://github.com/robotemi/sdk/wiki/permission_chn)
- [请求成为选中的商店模式技能](https://github.com/robotemi/sdk/wiki/kiosk-mode_chn#requestToBeKioskApp)
- [顶部移动状态徽章（开、关、获取其是否可用）](https://github.com/robotemi/sdk/wiki/Utils_chn#setTopBadgeEnabled)
- [用户检测模式（开、关、获取其是否可用、指定检测范围）](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#setDetectionModeOn)
- [自主返回（开、关、获取其是否可用）](https://github.com/robotemi/sdk/wiki/Utils_chn#setAutoReturnOn)
- [设置、获取当前音量](https://github.com/robotemi/sdk/wiki/Utils_chn#setVolume)
- [设置、获取导航避障安全等级](https://github.com/robotemi/sdk/wiki/Utils_chn#setNavigationSafetyLevel)
- [设置、获取导航速度等级](https://github.com/robotemi/sdk/wiki/Utils_chn#setGoToSpeed)
- [开启、关闭人脸识别](https://github.com/robotemi/sdk/wiki/temi-Center_chn#startFaceRecognition)
- [获取、播放导览](https://github.com/robotemi/sdk/wiki/temi-Center_chn#getAllSequences)
- [前往某个坐标](https://github.com/robotemi/sdk/wiki/Locations_chn#goToPosition)
- [获取地图数据](https://github.com/robotemi/sdk/wiki/Locations_chn#getMapData)
- [触发系统默认的自然语言理解](https://github.com/robotemi/sdk/wiki/Speech_chn#startDefaultNlu)
- [查看当前唤醒是否可用](https://github.com/robotemi/sdk/wiki/Utils_chn#isWakeupDisabled)
- [覆盖原语音识别（ASR）](https://github.com/robotemi/sdk/wiki/Speech_chn#overrideAsr)

#### 接口

- [temi 权限请求结果回调](https://github.com/robotemi/sdk/wiki/permission_chn#onRequestPermissionResultListener)
- [机器人当前距其他地点的距离变化监听器](https://github.com/robotemi/sdk/wiki/Locations_chn#onDistanceToLocationChangedListener)
- [当前机器人位置坐标变化监听器](https://github.com/robotemi/sdk/wiki/Locations_chn#onCurrentPositionChangedListener)
- [导览播放的状态变化监听器](https://github.com/robotemi/sdk/wiki/temi-Center_chn#onSequencePlayStatusChangedListener)
- [机器人是否被抬起监听器](https://github.com/robotemi/sdk/wiki/Movement_chn#onRobotLiftedListener)
- [用户检测模式的数据（距离、角度等）变化监听器](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#OnDetectionDataChangedListener)
- [人脸识别监听器](https://github.com/robotemi/sdk/wiki/temi-Center_chn#onFaceRecognizedListener)
- [temi SDK 错误信息回调](https://github.com/robotemi/sdk/wiki/Utils_chn#onSdkExceptionListener)

---

## 版本 0.10.65

- 发布于 **2020年5月19日**。
- 要求最小 Launcher 版本为 **13365-chinaTencent**。

### 新增

#### 方法

- [获取 Launcher 版本](https://github.com/robotemi/sdk/wiki/Utils_chn#getLauncherVersion")
- [获取 Robox 版本](https://github.com/robotemi/sdk/wiki/Utils_chn#getRoboxVersion)
- [视频电话事件变化监听器](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#onTelepresenceEventChangedListener)

---

## 版本 0.10.63

- 发布于 **2020年4月8日**。
- 要求最小 Launcher 版本为 **13146-chinaTencent**。

### 新增

#### 方法

- [temi主动说话并等待用户回应](https://github.com/robotemi/sdk/wiki/Speech_chn#askQuestion)
- [结束会话（关闭ASR收音等）](https://github.com/robotemi/sdk/wiki/Speech_chn#finishConversation)
- [覆盖temi原有的语音流（NLP）](https://github.com/robotemi/sdk/wiki/Speech_chn#overrideNlu)

---

## 版本 0.10.6

- 发布于 **2020年1月22日**。
- 要求最小 Launcher 版本为 **12668-chinaTencent**。

### 新增

#### 方法

- [禁用（开启）硬件按钮](https://github.com/robotemi/sdk/wiki/Utils_chn#setHardButtonsDisabled)

---

## 版本 0.10.53

- 发布于 **2019年12月10日**。
- 要求最小 Launcher 版本为 **11969-chinaTencent**。

### 新增

#### 方法

- [限制性跟随](https://github.com/robotemi/sdk/wiki/Follow_chn#constraintBeWith)
- [隐私模式开关](https://github.com/robotemi/sdk/wiki/Utils_chn#setPrivacyMode)

#### 接口

- [ASR监听器](https://github.com/robotemi/sdk/wiki/Speech_chn#asrListener)
- [用户检测状态变化监听器](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#onDetectionStateChangedListener)
- [隐私模式状态变化监听器](https://github.com/robotemi/sdk/wiki/Utils_chn#onPrivacyModeChangedListener)
- [电池状态变化监听器](https://github.com/robotemi/sdk/wiki/Utils_chn#OnBatteryStatusChangedListener)

#### 注意

- 从0.10.53版本开始在使用temi SDK上不区分中文版和英文版的Launcher，中英文版的机器可使用同一SDK，集成方式:

  ``` groovy
  implementation 'com.robotemi:sdk:0.10.53'
  ```

- 之前SDK版本中发布的欢迎模式监听器现在被替换成用户检测与用户交互监听器，目的是为了可以更灵活地进行开发以及获取更多temi状态机的反馈。

---

## 版本 0.10.49

- 发布于 **2019年11月14日**。
- 要求最小 Launcher 版本为 **11642-chinaTencent**。

### 新增

#### 方法

- [唤醒](https://github.com/robotemi/sdk/wiki/Speech_chn#wakeup)
- [获取唤醒词](https://github.com/robotemi/sdk/wiki/Speech_chn#getWakeupWord)

#### 接口

- [用户交互事件变化监听器](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#onUserInteractionChangedListener)
- [限制性跟随状态变化监听器](https://github.com/robotemi/sdk/wiki/Follow_chn#onConstraintBeWithStatusChangedListener)

---

## 版本 0.10.44

- 发布于 **2019年9月19日** 。
- 要求最小 Launcher 版本为 **11167-chinaTencent**。

### 新增

#### 方法

- [Kiosk模式](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn)
- [禁用（启用）唤醒](https://github.com/robotemi/sdk/wiki/Utils_chn#toggleWakeup)
- [隐藏（显示）go to 过程中的大字](https://github.com/robotemi/sdk/wiki/Utils_chn#toggleNavigationBillboard)
- [删除地点](https://github.com/robotemi/sdk/wiki/Locations_chn#deleteLocation)

---

## 版本 0.10.43

- 发布于 **2019年9月5日**。
- 要求最小 Launcher 版本为 **10723-chinaTencent**。

### 新增

#### 方法

- [获取电池信息](https://github.com/robotemi/sdk/wiki/Utils_chn#getBatteryData)
- [获取机器人序列号](https://github.com/robotemi/sdk/wiki/Utils_chn#getSerialNumber)
- [停止移动](https://github.com/robotemi/sdk/wiki/Movement_chn#stopMovement)
