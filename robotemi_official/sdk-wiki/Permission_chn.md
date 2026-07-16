# temi 权限

为了更好地保护用户隐私以及 temi 的数据安全，权限机制应运而生。这有点像Android中的动态权限申请机制，当你要访问或修改一些敏感信息时，需要动态地申请这些权限。权限机制适用于 **0.10.70** 及更高的版本。

<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|int|[checkSelfPermission(Permission permission)](#checkSelfPermission)|检查是否有权限|
|void|[requestPermissions(List\<Permission\> permissions, int requestCode)](#requestPermissions)|请求权限|

|接口|说明|
|-|-|
|[OnRequestPermissionResultListener](#onRequestPermissionResultListener)|权限请求结果监听器|

|模型|说明|
|-|-|
|[Permission](#permission)|权限|

<br>

## 请求权限

### 向清单添加元数据权限声明

在申请权限之前，技能必须在 **AndroidManifest.xml** 的 `<application>` 下添加 `<meta-data>` 来声明所需的权限。例如，想要修改系统设置的技能须添加以下代码：

``` xml
<application>
  ···
    <meta-data
        android:name="@string/metadata_permissions"
        android:value="com.robotemi.permission.settings" />
  ···
</application>
```

如需申请多种权限，则追加并以","（注意是英文逗号）分隔开：

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

查看[目前所有的权限](#currentPermissions)。

### 检查权限

如果调用一些需要权限才能使用的接口，那么都每次必须检查是否具有该权限。因为用户可随时从 Launcher 中的 *设置 > 权限* 中撤销任何技能所具有的任何权限，例如撤销人脸识别：*设置 > 权限 > 人脸识别 > 你的技能 > 关闭*。

#### 确定技能是否已获得权限

如需检查用户是否已向你的技能授予特定的权限，请将改权限传入 [`Robot.getInstance().checkSelfPermission()`](#checkSelfPermission) 方法。根据你的技能是否具有相应的权限，此方法会返回 [`GRANTED`](#granted) 或 [`DENIED`](#denied)。

#### 请求权限

如果你的技能还没有相关权限，有两种方式来获得权限，一种是在 *设置 > 权限 > 点击所需权限 > 打开你的技能对应的开关*；另一种方式是通过 temi SDK 的接口来请求权限，然后系统弹出的对话框，用户可在其中选择是否向你的技能授予特定的权限；这里主要介绍第二种方式。

通过调用[`Robot.getInstance().requestPermissions()`](#requestPermissions)来弹出权限请求对话框。和 Android 中的动态[权限请求](https://developer.android.google.cn/training/permissions/requesting)类似，可以在权限请求过程中自行管理请求代码，并将此请求代码包含在你的[权限请求回调](#onRequestPermissionResultListener)逻辑中。

#### 监听权限请求的授予结果

和监听其他监听器一样来监听[权限请求回调](#onRequestPermissionResultListener)，可结合[权限请求](#requestPermissions)时传入的请求代码，待权限被用户授予后，可以在你的技能中继续你的操作或工作流。

<br>

## 方法

### checkSelfPermission()

用这个方法来检查你的技能是否拥有该权限。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |permission|[Permission](#permission)|所要检查的权限对象|

- **返回值**

  |类型|说明|
  |-|-|
  |int|授权结果，可能的值为 [`GRANTED`](#granted) 或 [`DENIED`](#denied)|

- **原型**

  ``` java
  int checkSelfPermission(Permission permission);
  ```

---

### requestPermissions()

用这个方法来请求技能所需要的权限。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |permissions|List\<[Permission](#permission)\>|需要请求的权限集合|
  |requestCode|int|特定于应用程序的请求代码，以报告给[OnRequestPermissionResultListener](#onRequestPermissionResultListener)的结果相匹配|

- **原型**

  ``` java
  void requestPermissions(List<Permission> permissions);
  ```

<br>

## 接口

### OnRequestPermissionResultListener

可监听所请求权限的授予与否结果。在你的上下文中实现这个监听器接口，并重写接口中的方法以获取权限请求结果。

``` java
package com.robotemi.sdk.permission;

interface OnRequestPermissionResultListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |permission|[Permission](#permission)|所请求的权限|
  |grantResult|int|权限的授予状态|
  |requestCode|int|从[requestPermissions()](#requestPermissions)传入的请求代码|

- **原型**

  ``` java
  abstract void onRequestPermissionResult(Permission permission,
                                          int grantResult,
                                          int requestCode);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRequestPermissionResultListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void addOnRequestPermissionResultListener(OnRequestPermissionResultListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRequestPermissionResultListener|实现了此接口的类的实例|

- **原型**

  ``` java
  void removeOnRequestPermissionResultListener(OnRequestPermissionResultListener listener);
  ```
  
<br>

## 模型

以下是上述方法、接口中用到的数据模型。

### Permission

#### 目前所有的权限 <a name="currentPermissions"/>

|权限|枚举值|值|是否为商店模式权限|
|-|-|-|:-:|
|人脸识别|FACE_RECOGNITION|com.robotemi.permission.face_recognition|否|
|地图|MAP|com.robotemi.permission.map|否|
|系统设置|SETTINGS|com.robotemi.permission.settings|否|
|导览|SEQUENCE|com.robotemi.permission.sequence|否|
|会议|MEETINGS|com.robotemi.permission.meetings|否|

***~~注：只有商店模式技能才可以请求商店模式权限。~~ 从0.10.72版本以后所有权限无须要在 Kiosk 模式下申请。***

#### 原型

``` java
package com.robotemi.sdk.permission;

enum Permission {
    FACE_RECOGNITION,  // 人脸识别数据
    MAP,  // 地图数据
    SETTINGS,  // 修改系统设置
    SEQUENCE;  // 导览数据
}
```

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|GRANTED <a name="granted" />|int|1|用户已授权|
|DENIED <a name="denied" />|int|0|用户未授权或已拒绝|
