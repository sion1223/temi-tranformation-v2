# User & Telepresence

In this page we will outline all the SDK methods that are related to temi users and telepresence. temi users are people who have a temi account, this means they have downloaded the mobile app, registered and that now you can call them from temi.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|List<[UserInfo](#userinfo)>|[getAllContact()](#getallcontact)|Get all contacts of temi|
|[UserInfo](#userinfo)|[getAdminInfo()](#getadmininfo)|Get administrator information of temi|
|[OrganizationInfo](#organizationinfo)|[getOrganizationInfo()](#getorganizationinfo)|Get organization information of the robot|
|String|[startTelepresence(String displayName, String peerId, Platform platform)](#starttelepresence)|Start a telepresence|
|int|[stopTelepresence()](#stoptelepresence)|Stop ongoing telepresence|
|List<[RecentCallModel](#recentcallmodel)>|[getRecentCalls()](#getrecentcalls)|Get recent call records|
|List<[MemberStatusModel](#memberstatusmodel)>|[getMembersStatus()](#getMembersStatus)|Get the availability of temi members(Administrator, Owners)|
|Pair<Int, String>|[createLinkBasedMeeting(LinkBasedMeeting linkBasedMeeing)](#createlinkbasedmeeting)|Create a link based meeting|
|String|[startMeeting(List<Participant> participants, boolean firstParticipantJoinedAsHost, boolean blockRobotInteraction)](#startmeeting)|Start a multiparty meeting|
|int|[setMicGainLevel(int micGainLevel)](#setMicGainLevel)|Set microphone gain level|

|Abstract Class|Description|
|-|-|
|[OnUsersUpdatedListener](#onusersupdatedlistener)|Listener of users information updated|
|[OnTelepresenceStatusChangedListener](#ontelepresencestatuschangedlistener)|Listener of telepresence status changed|

|Interface|Description|
|-|-|
|[OnTelepresenceEventChangedListener](#ontelepresenceeventchangedlistener)|Listener of telepresence event changed|

|Model|Description|
|-|-|
|[UserInfo](#userinfo)|User information|
|[OrganizationInfo](#organizationinfo)|Organization information|
|[RecentCallModel](#recentcallmodel)|Recent call record|
|[CallState](#callState)|Call state|
|[CallEventModel](#callEventModel)|Call video|
|[Platform](#platform)|User platform|
|[MemberStatusModel](#memberstatusmodel)|Availability of temi members(Administrator, Owners)|
|[LinkBasedMeeting](#linkbasedmeeting)|Link based meeting entity|
|[Participant](#participant)|Meeting participant entity|

<br>

## Methods

### getAllContact()

Use this method to fetch all the temi contacts that are synced with the launcher. These contacts are saved on the admin's mobile device and are registered to the temi service.

- **Return**

  |Type|Description|
  |-|-|
  |List<[UserInfo](#userinfo)>|List of temi contacts' user information|

- **Prototype**

  ``` java
  List<UserInfo> getAllContact();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### getAdminInfo()

Use this method to fetech temi's admin user information, this information can be used for many things but mainly it's useful to make calls from temi to the admin's mobile device.

- **Return**

  |Type|Description|
  |-|-|
  |[UserInfo](#userinfo)|User information of administrator|

- **Prototype**

  ``` java
  UserInfo getAdminInfo();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### getOrganizationInfo()

Use this method to get the organization information of the robot. This information is updated once after robot boot and does not live-update during runtime.

- **Return**

  |Type|Description|
  |-|-|
  |[OrganizationInfo](#organizationinfo)|Organization information, or `null` if the current launcher does not support this method or parsing fails|

- **Prototype**

  ``` java
  OrganizationInfo getOrganizationInfo();
  ```

- **Required permissions**

  None.

- **Support from**

  1.138.0

---

### startTelepresence()

Use this method to initiate a telepresence session using the temi telepresence service to one of the admin's temi contacts.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |displayName|String|~~String value of the name of the person you want to call~~|
  |peerId|String|String value of the id of the person you want to call|
  |platform|[Platform](#platform)|The target platform you want to make a video call. You can make a video call to the temi App of any contact, but you can only make a video call to the administrator or the owner’s temi center|

- **Return**

  |Type|Description|
  |-|-|
  |String|-|

- **Prototype**

  ``` java
  String startTelepresence(String displayName, String peerId, Platform platform);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### stopTelepresence()

Use this method to stop ongoing telepresence session.

- **Parameters**

  None.

- **Return**

  |Type|Description|
  |-|-|
  |int|200 OK<br>400 failed to verify package name<br>403 meeting permission required<br>404 No onging telepresence<br>500 SDK internal error|

- **Prototype**

  ``` java
  int stopTelepresence();
  ```

- **Required permissions**

  Meetings

- **Support from**

  1.130.1

---

### getRecentCalls()

Using this method to get the recent call records.

- **Return**

  |Type|Description|
  |-|-|
  |List<[RecentCallModel](#recentcallmodel)>|List of recent call records|

- **Prototype**

  ``` java
  List<RecentCallModel> getRecentCalls();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### getMembersStatus()

Using this method to get the availability of temi members(Administrator, Owners). Generally used to determine whether video calls can be made.

- **Return**

  |Type|Description|
  |-|-|
  |List<[MemberStatusModel](#memberStatusModel)>|List of temi members' availability status|

- **Prototype**

  ``` java
  List<MemberStatusModel> getMembersStatus();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.72

---

### createLinkBasedMeeting()

Using this method to create a link based meeting for current robot on behalf of SDK, with the same functionalities as on temi mobile and temi center.

- **Return**

  |Type|Description|
  |-|-|
  |Pair<int, String>|response code and meeting link or error reason.<br> 200 OK, with meeting link like `https://center.robotemi.com/meetings/{linkId}`<br> 403, Meetings permission required.<br>429, request too frequently, shall be 5 seconds interval|

- **Prototype**

  ``` java
  Pair<int, String> createLinkBasedMeeting(LinkBasedMeeting linkbasedMeeting);
  ```

- **Required permissions**

  Meetings.

- **Support from**

  1.130.0

---

### startMeeting()

Start a multiparty meeting with robot's private meeting link, particiants in the parameter will be invited and admitted automatically.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |participants|List<Participant>|Participants to be invited|
  |firstParticipantJoinedAsHost|Boolean|Set to true, then first participant joined will be assigned as host. Otherwise launcher will be the host|
  |blockRobotInteraction|Boolean|Disable some launcher buttons in the call. Prevent user to interrupt the call, added in 132 version|


- **Return**

  |Type|Description|
  |-|-|
  |String|response code<br> 200 OK<br> 403, Meetings permission required|

- **Prototype**

  ``` java
  String startMeeting(List<Participant> participants, boolean firstParticipantJoinedAsHost, boolean blockRobotInteraction);
  ```

- **Required permissions**

  Meetings.

- **Support from**

  1.131.0
---

### setMicGainLevel()

Set microphone gain level in temi meetings.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |micGainLevel|int|Value from 1-4|

- **Prototype**

  |Type|Description|
  |-|-|
  |int|response code<br> 0 failed<br> 1 succeed<br> 403, SETTINGS permission required<br> 429, too many request, wait for 2 seconds|

- **Prototype**

  ``` java
  int setMicGainLevel(int micGainLevel);
  ```

- **Required permissions**

  SETTINGS

- **Support from**

  1.133.0

<br>

## Abstract Classes

### OnUsersUpdatedListener

Listener for temi contacts changes. Every time one of the admin's contacts makes a change to his info, or is added or deleted from the contact's list the listener will be triggered.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

abstract class OnUsersUpdatedListener {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|userIds|List\<String\>|List of temi contacts' ID|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |user|[UserInfo](#userinfo)|The user information that has been changed|

- **Prototype**

  ``` java
  void onUserUpdated(UserInfo user);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnUsersUpdatedListener|The object of the child class of this class|

- **Prototype**

  ``` java
  void addOnUsersUpdatedListener(OnUsersUpdatedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnUsersUpdatedListener|The object of the child class of this class|

- **Prototype**

  ``` java
  void removeOnUsersUpdatedListener(OnUsersUpdatedListener listener);
  ```

---

### OnTelepresenceStatusChangedListener

Listener for telepresence status changes. By adding this listener you will mainly know if the call has started, ended or declined.


From 129 version, sessionId is not required, set `sessionId=""` is enough, status all telepresence calls can be monitored from this callback.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

abstract class OnTelepresenceStatusChangedListener {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|sessionId|String|~~Session ID~~|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |callState|[CallState](#callState)|State of the call|

- **Prototype**

``` java
void onTelepresenceStatusChanged(CallState callState);
```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTelepresenceStatusChangedListener|The object of the child class of this class|

- **Prototype**

  ``` java
  void addOnTelepresenceStatusChangedListener(OnTelepresenceStatusChangedListener listener);
  ```
  
#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTelepresenceStatusChangedListener|The object of the child class of this class|

- **Prototype**

  ``` java
  void removeOnTelepresenceStatusChangedListener(OnTelepresenceStatusChangedListener listener);
  ```

<br>

## Interfaces

### OnTelepresenceEventChangedListener

Listener for telepresence status changes. Through this listener you can listen to all incoming (start and end) and outgoing (start and end) events.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnTelepresenceEventChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameters|Type|Description|
  |-|-|-|
  |callEventModel|[CallEventModel](#calleventmodel)|Call event|

- **Prototype**

  ``` java
  void onTelepresenceEventChanged(CallEventModel calleventModel);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTelepresenceEventChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnTelepresenceEventChangedListener(OnTelepresenceEventChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnTelepresenceEventChangedListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnTelepresenceEventChangedListener(OnTelepresenceEventChangedListener listener);
  ```

<br>

## Models

### UserInfo

Object used to hold the information of user.

#### Prototype

``` java
package com.robotemi.sdk;

class UserInfo {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|userId|String|ID of user|
|name|String|Name of user|
|picUrl|String|Avatar picture URL of user|
|role|int|Role of user, 0 means administrator, 1 means owner, 2 means contact. <br>From 1.129.1 version, 0 means administrators of robot, 1 means collaborators, 2 means guest, 3 means contacts assigned to this robot, who are temi registered user, and can be called. 10 means contacts assigned to this robot, but only used for face recognition, cannot be called with [UserInfo.userId]|

---

### OrganizationInfo

Object used to hold the organization information of the robot.

#### Prototype

``` java
package com.robotemi.sdk.model;

class OrganizationInfo {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|id|String|ID of the organization|
|name|String|Name of the organization|
|profileImage|String|Profile image media key of the organization. Exchange signed url via [getSignedUrlByMediaKey](https://github.com/robotemi/sdk/wiki/temi-Center#getSignedUrlByMediaKey)|
|robotCount|int|Count of robots in the organization|
|region|String|Region of the organization. Can be `global` or `chn`|
|rootAccount|String|Root account userId of the organization. Can be matched with [UserInfo](#userinfo) `userId`|

---

### CallState

Used to hold the call state.

#### Prototype

``` java
package com.robotemi.sdk.telepresence;

class CallState {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|sessionId|String|~~Unique id for the telepresence call~~|
|state|[State](#state)|Call state|
|lowLightMode|Boolean|Low light mode ON/OFF (supported from 130 version)|

---

### State

Used to hold the state of telepresence call.

#### Prototype

``` java
package com.robotemi.sdk.telepresence.CallState;

enum State {
    ENDED,  // A call has ended
    DECLINED,  // A call attempt was declined by the other callee
    STARTED,  // Call was accepted by the callee and has started
    // The following states are added in 1.129.1 version
    INITIALIZED, // Call is made but not answered yet.
    NOT_ANSWERED, // The other side doesn't answer the call.
    BUSY, // The other side is busy.
    POOR_CONNECTION, // Cannot establish the call due to connection issue.
    CANT_JOIN, // Cannot join the call.
}
```

---

### CallEventModel

Used to hold the call event.

#### Prototype

``` java
package com.robotemi.sdk.model;

class CallEventModel {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|sessionId|String|~~Unique id for the telepresence call~~|
|type|int|Type of the call, incoming(0) or outgoing(1)|
|state|int|State of the call, started(0) or ended(1)|

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|TYPE_INCOMING|int|0|Incoming call|
|TYPE_OUTGOING|int|1|Outgoing call|
|STATE_STARTED|int|0|Call started|
|STATE_ENDED|int|1|Call ended|

---

### Platform

Type of temi clients.

#### Prototype

``` java
package com.robotemi.sdk.constants;

enum Platform {
    MOBILE,  // temi mobile App
    TEMI_CENTER  // temi Center
}
```

### MemberStatusModel

Used to hold the temi member's availability status information.

#### Prototype

``` java
package com.robotemi.sdk.model;

class MemberStatusModel {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|memberId|String|ID of temi member|
|mobileStatus|int|Availability status of member in temi mobile App|
|centerStatus|int|Availability status of member in temi Center|

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|STATUS_ONLINE|int|0|Online and available|
|STATUS_OFFLINE|int|1|Offline|
|STATUS_BUSY|int|2|Busy|

---

### LinkBasedMeeting

Link based meeting entity

#### Prototype

``` java
package com.robotemi.sdk.telepresence;

class LinkBasedMeeting {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|topic|String|Topic for the meeting|
|availability|Availability|Link availablity time range. Define start/end time of the link, or always available|
|limit|Limit|Call duration limitation, and usage time limitation|
|permission|Permission|Define the link users' permission to control the robot, or create notes.|
|security|Security|Set passcode for the meeting|

---

### Participant

Meeting participant entity

#### Prototype

``` java
package com.robotemi.sdk.telepresence;

class Participant {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|peerId|String|Participant ID, same as peerId in `startTelepresence`|
|platform|Platform|Platform.MOBILE， Platform.TEMI_CENTER|