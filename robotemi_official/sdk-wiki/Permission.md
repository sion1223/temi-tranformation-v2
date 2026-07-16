
# temi Permission

In order to better protect user privacy and temi's data security, a permission mechanism came into being. This is a bit like the dynamic permission application mechanism in Android. When you want to access or modify some sensitive information, you need to apply for these permissions dynamically. The permission mechanism is applicable to **0.10.70** and higher versions.

<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|int|[checkSelfPermission(Permission permission)](#checkSelfPermission)|Check permission|
|void|[requestPermissions(List\<Permission\> permissions, int requestCode)](#requestPermissions)|Request permissions|

|Interface|Description|
|-|-|
|[OnRequestPermissionResultListener](#onRequestPermissionResultListener)|Listener for listening to the result of permission request|

|Model|Description|
|-|-|
|[Permission](#permission)|The permission|

<br>

## Request Skill Permissions

### Add permissions to the manifest

Before requesting permissions, to declare that your skill needs permission, put a `<meta-data>` element in your **AndroidManifest.xml**, as a child of `<application>` element. For example, skill that needs to modify the system settings would have follow codes in the manifest:

``` xml
<application>
  ···
    <meta-data
        android:name="@string/metadata_permissions"
        android:value="com.robotemi.permission.settings" />
  ···
</application>
```

If you need to request for multiple permissions, please append and separate them with ",".

``` xml
<application>
  ···
    <meta-data
        android:name="@string/metadata_permissions"
        android:value="com.robotemi.permission.settings,
                       com.robotemi.permission.face_recognition" />
  ···
</application>
```

Check [all current permissions](#currentPermissions).

### Check for permissions

You must check wheather your skill have that permission every time you perform an operation that requires that permission. Because users can revoke all the permissions from any skill at any time. For example, revoke the permission of face recognition: *Settings > Permissions > Face Recognition > Your skill > Turn off the switch*.

#### Determine whether your app was already granted the permission

To check if the user has already granted your app a particular permission, pass that permission into the [`Robot.getInstance().checkSelfPermission()`](#checkSelfPermission) method. This method returns either [`GRANTED`](#granted) or [`DENIED`](#denied), depending on wheather your skill has the permission.

#### Request permissions

If your skills does not have the relevant permissions, there are two ways to obtain permissions, one is in *Settings > Permissions > Click the required permissions > Turn on the switch of your skill*; Another way is to request permissions through the temi SDK, and then a dialog box pops up, where user can choose wheather to grant specific permissions to your skill. The second way is mainly introduced in this documentation.

To invoke [`Robot.getInstance().requestPermissions()`](#requestPermissions) method to pop up the permissions request dialog box. Similar to the dynamic [permission request](https://developer.android.google.cn/training/permissions/requesting), you can manage the request code yourself as part of the permission request and include this request code in your [permission request callback](#onRequestPermissionResultListener) logic.

#### Listen to the grant results

You can listen to [permission request callback](#onRequestPermissionResultListener) like listen to other listeners. It can be combined with the request code passed in [permission request](#requestPermissions), you can continue the action or workflow in your skill after the permission is granted by user.

<br>

## Methods

### checkSelfPermission()

Use this method to check wheather your skill has the permission.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |permission|[Permission](#permission)|The permission to be checked|

- **Return**

  |Type|Description|
  |-|-|
  |int|Grant result, possible value is [`GRANTED`](#granted) or [`DENIED`](#denied)|

- **Prototype**

  ``` java
  int checkSelfPermission(Permission permission);
  ```

---

### requestPermissions()

Use this method to request the permissions you want.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |permissions|List\<[Permission](#permission)\>|The list holds the permissions you want to request|
  |requestCode|int|Application specific request code to match with a result reported to [OnRequestPermissionResultListener](#onRequestPermissionResultListener)|

- **Prototype**

  ``` java
  void requestPermissions(List<Permission> permissions);
  ```

<br>

## Interfaces

### OnRequestPermissionResultListener

You can listen to the results of the grant of the requested permissions. Implement this listener interface in your context, and override the methods in the interface to get the permission request result.

``` java
package com.robotemi.sdk.permission;

interface OnRequestPermissionResultListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |permission|[Permission](#permission)|Pending permissions|
  |grantResult|int|Grant result|
  |requestCode|int|The request code passed in [requestPermissions()](#requestPermissions)|

- **Prototype**

  ``` java
  abstract void onRequestPermissionResult(Permission permission,
                                          int grantResult,
                                          int requestCode);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRequestPermissionResultListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void addOnRequestPermissionResultListener(OnRequestPermissionResultListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRequestPermissionResultListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void removeOnRequestPermissionResultListener(OnRequestPermissionResultListener listener);
  ```

<br>

## Model

The following is the data model used in the above methods and interfaces.

### Permission

#### Current permissions <a name="currentPermissions" />

|Permission|Enum Value|Value|Is Kiosk Permission|
|-|-|-|:-:|
|Face Recognition|FACE_RECOGNITION|com.robotemi.permission.face_recognition|No|
|Map|MAP|com.robotemi.permission.map|No|
|Settings|SETTINGS|com.robotemi.permission.settings|No|
|Sequence|SEQUENCE|com.robotemi.permission.sequence|No|
|Meetings|MEETINGS|com.robotemi.permission.meetings|No|

***~~Note: Only kiosk skills can request kiosk permissions.~~ Since version 0.10.72, all permissions do not need to be requested in Kiosk mode.***

#### Prototype

``` java
package com.robotemi.sdk.permission;

enum Permission {
    FACE_RECOGNITION,
    MAP,
    SETTINGS,
    SEQUENCE
}
```

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|GRANTED <a name="granted"/>|int|1|Granted by user|
|DENIED <a name="denied"/>|int|0|Denied by user|
