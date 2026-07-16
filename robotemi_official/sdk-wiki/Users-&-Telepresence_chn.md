# 用户与视频电话

在本页中，我们将概述所有与temi用户和视频电话相关的SDK方法。temi用户是拥有temi帐户的人，这意味着他们已经下载了移动应用程序，并且已经完成注册，这时你可以从temi给他们打视频电话。

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|List<[UserInfo](#userInfo)>|[getAllContact()](#getAllContact)|获取所有的 temi 联系人|
|[UserInfo](#userInfo)|[getAdminInfo()](getAdminInfo)|获取 temi 管理员信息|
|[OrganizationInfo](#organizationInfo)|[getOrganizationInfo()](#getOrganizationInfo)|获取机器人所属组织信息|
|String|[startTelepresence(String displayName, String peerId, Platform platform)](#startTelepresence)|开启一个视频电话|
|int|[stopTelepresence()](#stopTelepresence)|结束当前通话|
|List<[RecentCallModel](#recentCallModel)>|[getRecentCalls()](#getRecentCalls)|获取最近通话记录|
|List<[MemberStatusModel](#memberStatusModel)>|[getMembersStatus()](#getMembersStatus)|获取成员（管理员，主人）空闲状态|
|Pair<Int, String>|[createLinkBasedMeeting(LinkBasedMeeting linkBasedMeeing)](#createlinkbasedmeeting)|创建一个会议链接|
|String|[startMeeting(List<Participant> participants, boolean firstParticipantJoinedAsHost, boolean blockRobotInteraction)](#startmeeting)|开启多方通话|
|int|[setMicGainLevel(int micGainLevel)](#setMicGainLevel)|设置麦克风增益|

|接口|描述|
|-|-|
|[OnUsersUpdatedListener](#onusersupdatedlistener)|用户信息变化监听器|
|[OnTelepresenceStatusChangedListener](#ontelepresencestatuschangedlistener)|视频电话状态变化监听器|
|[OnTelepresenceEventChangedListener](#ontelepresenceeventchangedlistener)|视频电话事件变化监听器|

|模型|描述|
|-|-|
|[UserInfo](#userInfo)|用户信息|
|[OrganizationInfo](#organizationInfo)|组织信息|
|[RecentCallModel](#recentcallmodel)|最近通话记录|
|[CallState](#callState)|视频电话状态|
|[CallEventModel](#calleventmodel)|视频通话事件|
|[Platform](#platform)|用户平台|
|[MemberStatusModel](#memberstatusmodel)|成员（管理员，主人）空闲状态|
|[LinkBasedMeeting](#linkbasedmeeting)|创建会议链接请求实体|
|[Participant](#participant)|会议参与者实体|

<br>

## 方法

### getAllContact()

用这个方法获取与 Launcher 同步的所有 temi 联系人。这些联系人已经保存在管理员的移动设备上，并已注册到 temi 服务器。

- **返回值**

  |类型|说明|
  |-|-|
  |List<[UserInfo](#userInfo)>|联系人信息列表|

- **原型**

  ``` java
  List<UserInfo> getAllContact();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### getAdminInfo()

用这个方法来获取temi的管理员用户信息，该信息可用于许多用途，但主要是用于从temi向管理员的移动设备拨打视频电话。

- **返回值**

  |类型|说明|
  |-|-|
  |[UserInfo](#userInfo)|管理员用户信息|

- **原型**

  ``` java
  UserInfo getAdminInfo();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### getOrganizationInfo()

获取机器人所属组织的信息。该信息在机器人启动后更新一次，运行期间不会实时刷新。

- **返回值**

  |类型|说明|
  |-|-|-|
  |[OrganizationInfo](#organizationInfo)|组织信息；若当前 Launcher 不支持或解析失败则返回 `null`|

- **原型**

  ``` java
  OrganizationInfo getOrganizationInfo();
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.138.0

---

### startTelepresence()

用这个方法向 temi 联系人发起一个视频电话。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |displayName|String|~~你要拨打视频电话的人名字的字符串值~~|
  |peerId|String|你要拨打视频电话的联系人ID|
  |platform|[Platform](#platform)|要拨打视频通话的目标平台，可以向任何联系人的 temi App端拨打视频通话，但只可以向管理员或拥有者的 temi 管理平台拨打视频通话|

- **返回值**

  |类型|说明|
  |-|-|
  |String|-|

- **原型**

  ``` java
  String startTelepresence(String displayName, String peerId, Platform platform);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36
---

### stopTelepresence()

结束当前进行的通话

- **参数**

  无.

- **返回值**

  |类型|说明|
  |-|-|
  |int|200 成功<br>400 包名验证错误<br>403 需要 Meetings 权限<br>404 没有进行中的通话<br>500 SDK 内部错误|

- **原型**

  ``` java
  int stopTelepresence();
  ```

- **所需权限**

  Meetings

- **最小支持版本**

  1.130.1
---

### getRecentCalls()

用这个方法来获取最近通话记录列表。

- **返回值**

  |类型|说明|
  |-|-|
  |List<[RecentCallModel](#recentCallModel)>|最近通话列表|

- **原型**

  ``` java
  List<RecentCallModel> getRecentCalls();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### getMembersStatus()

用这个方法来获取 temi 成员（管理员，主人）的空闲状态，一般用于判断是否可以拨打视频电话。

- **返回值**

  |类型|说明|
  |-|-|
  |List<[MemberStatusModel](#memberStatusModel)>|成员空闲状态列表|

- **原型**

  ``` java
  List<MemberStatusModel> getMembersStatus();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.72


---

### createLinkBasedMeeting()

以 SDK 的身份为当前 temi 创建一个会议链接，与 temi 手机应用与 temi 管理平台的功能一致。

- **返回值**

  |类型|说明|
  |-|-|
  |Pair<int, String>|返回值状态码及会议链接或错误描述<br> 200 成功, 伴有会议链接如 `https://center.robotemi.cn/meetings/{linkId}`<br> 403, 需要 Meetings 权限<br>429, 请求过于频繁，需间隔 5 秒以上|

- **原型**

  ``` java
  Pair<int, String> createLinkBasedMeeting(LinkBasedMeeting linkbasedMeeting);
  ```

- **所需权限**

  Meetings.

- **最小支持版本**

  1.130.0

---

### startMeeting()

开启多方通话，以机器人的私人会议链接开启会议，会向 participants 发出邀请并自动允许入会。
支持传参并将会议转为第一个参会人的私人会议。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |participants|List<Participant>|参会人列表|
  |firstParticipantJoinedAsHost|Boolean|是否指派第一个入会的参会人为主持人，否则 Launcher 将作为主持人|
  |blockRobotInteraction|Boolean|是否禁止 temi 端 UI，防止通话被 temi 端的用户误操作|

- **返回值**

  |类型|说明|
  |-|-|
  |String|返回值状态码<br> 200 成功<br> 403, 需要 Meetings 权限|

- **原型**

  ``` java
  String startMeeting(List<Participant> participants, boolean firstParticipantJoinedAsHost, boolean blockRobotInteraction);
  ```

- **所需权限**

  Meetings.

- **最小支持版本**

  1.131.1

---

### setMicGainLevel()

设置麦克风增益，1倍为无增益，最高为4倍

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |micGainLevel|int|取值1-4|

- **返回值**

  |类型|说明|
  |-|-|
  |int|返回值状态码<br> 0 失败<br> 1 成功<br> 403 需要 SETTINGS 权限<br> 429 请求过于频繁，需要间隔2秒|

- **原型**

  ``` java
  int setMicGainLevel(int micGainLevel);
  ```

- **所需权限**

  SETTINGS

- **最小支持版本**

  1.133.0

<br>

## 抽象类

### OnUsersUpdatedListener

可监听 temi 联系人信息更新。在你的上下文中实现这个监听器接口，并重写接口中的方法以监听联系人信息变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

abstract class OnUsersUpdatedListener {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|userIds|List\<String\>|temi 联系人的 ID 列表|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |user|[UserInfo](#userInfo)|发生变更的联系人用户信息|

- **原型**

  ``` java
  void onUserUpdated(UserInfo user);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnUsersUpdatedListener|这个类的子类实例|

- **原型**

  ``` java
  void addOnUsersUpdatedListener(OnUsersUpdatedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnUsersUpdatedListener|这个类的子类实例|

- **原型**

  ``` java
  void removeOnUsersUpdatedListener(OnUsersUpdatedListener listener);
  ```

---

### OnTelepresenceStatusChangedListener

可监听视频电话的状态变化的监听器。通过这个监听器你主要可以知道视频通话是否开始、结束或者被拒绝。

129 版本后传入 `sessionId=""` 即可，可监听所有通话的状态回调。 

#### 原型

``` java
package com.robotemi.sdk.listeners;

abstract class OnTelepresenceStatusChangedListener {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|sessionId|String|~~会话 ID~~|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |callState|[CallState](#callState)|视频电话状态|

- **原型**

``` java
void onTelepresenceStatusChanged(CallState callState);
```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTelepresenceStatusChangedListener|这个类的子类实例|

- **原型**

  ``` java
  void addOnTelepresenceStatusChangedListener(OnTelepresenceStatusChangedListener listener);
  ```
  
#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTelepresenceStatusChangedListener|这个类的子类实例|

- **原型**

  ``` java
  void removeOnTelepresenceStatusChangedListener(OnTelepresenceStatusChangedListener listener);
  ```

<br>

## 接口

### OnTelepresenceEventChangedListener

通过这个监听器你可以监听所有的来电（开始和结束）、去电（开始和结束）事件。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnTelepresenceEventChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |callEventModel|[CallEventModel](#callEventModel)|视频电话事件|

- **原型**

  ``` java
  void onTelepresenceEventChanged(CallEventModel calleventModel);
  ```

#### 添加监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTelepresenceEventChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnTelepresenceEventChangedListener(OnTelepresenceEventChangedListener listener);
  ```

#### 移除监听的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnTelepresenceEventChangedListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnTelepresenceEventChangedListener(OnTelepresenceEventChangedListener listener);
  ```

<br>

## 模型

### UserInfo

用于保存temi联系人信息的对象。

#### 原型

``` java
package com.robotemi.sdk;

class UserInfo {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|userId|String|用户 ID|
|name|String|用户名字|
|picUrl|String|用户头像图片链接地址|
|role|int|用户角色，0 为管理员，1 为主人，2 为一般联系人<br>1.129.1 版本后，0 表示管理员，1 表示分级管理员，2 表示访客，3 表示分配给这台机器的联系人，可用于通话。10 表示分配给这台机器的联系人，仅用于人脸识别，不能用于通话|

---

### OrganizationInfo

用于保存机器人所属组织信息的对象。

#### 原型

``` java
package com.robotemi.sdk.model;

class OrganizationInfo {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|String|组织 ID|
|name|String|组织名称|
|profileImage|String|组织头像的 media key，可通过 [getSignedUrlByMediaKey](https://github.com/robotemi/sdk/wiki/temi-Center_chn#getSignedUrlByMediaKey) 换取签名 URL|
|robotCount|int|组织内机器人数量|
|region|String|组织所在区域，可为 `global` 或 `chn`|
|rootAccount|String|组织根账号的 userId，可与 [UserInfo](#userInfo) 的 `userId` 匹配|

---

### CallState

用于保存视频电话中关于会话状态的信息。

#### 原型

``` java
package com.robotemi.sdk.telepresence;

class CallState {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|sessionId|String|~~视频电话的唯一 ID~~|
|state|[State](#state)|视频电话状态|
|lowLightMode|Boolean|夜景增强开关 (130 版本加入)|

---

### State

视频电话状态。

#### 原型

``` java
package com.robotemi.sdk.telepresence.CallState;

enum State {
    ENDED,  // 通话结束
    DECLINED,  // 呼叫被被叫方拒绝
    STARTED  // 被叫方已接受呼叫并开始通话
    // 以下状态与 1.129.1 版本加入。
    INITIALIZED, // 通话初始化
    NOT_ANSWERED, // 对方未接听
    BUSY, // 对方正忙
    POOR_CONNECTION, // 连接遇到问题
    CANT_JOIN, // 无法加入通话
}
```

---

### CallEventModel

用于保存视频电话事件的信息。

#### 原型

``` java
package com.robotemi.sdk.model;

class CallEventModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|sessionId|String|~~视频电话唯一 ID~~|
|type|int|视频电话类型，来电（0）或去电（1）|
|state|int|视频电话状态，开始（0）或结束（1）|

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|TYPE_INCOMING|int|0|来电|
|TYPE_OUTGOING|int|1|去电|
|STATE_STARTED|int|0|视频电话开始|
|STATE_ENDED|int|1|视频电话结束|

---

### Platform

temi 客户端平台类型。

#### 原型

``` java
package com.robotemi.sdk.constants;

enum Platform {
    MOBILE,  // temi mobile App
    TEMI_CENTER  // temi 管理平台
}
```

---

### MemberStatusModel

用于保存成员（管理员，主人）的空闲状态。

#### 原型

``` java
package com.robotemi.sdk.model;

class MemberStatusModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|memberId|String|成员的用户 ID|
|mobileStatus|int|用户在 temi App 上的空闲状态|
|centerStatus|int|用户在 temi Center 上的空闲状态|

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|STATUS_ONLINE|int|0|在线并空闲|
|STATUS_OFFLINE|int|1|离线|
|STATUS_BUSY|int|2|忙碌|

---

### LinkBasedMeeting

创建会议链接请求实体

#### 原型

``` java
package com.robotemi.sdk.telepresence;

class LinkBasedMeeting {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|topic|String|会议主题|
|availability|Availability|链接的可用时间，指定起始时间或设置为永久有效|
|limit|Limit|通话时长及使用次数限制|
|permission|Permission|配置会议中链接使用者的权限，控制机器人，创建会议记录等|
|security|Security|配置会议链接的密码|

---

### Participant

会议参与者实体

#### 原型

``` java
package com.robotemi.sdk.telepresence;

class Participant {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|peerId|String|参会人 ID|
|platform|Platform|Platform.MOBILE 手机端， Platform.TEMI_CENTER 网页端|