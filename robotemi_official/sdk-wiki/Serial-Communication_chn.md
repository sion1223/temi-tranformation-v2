[temi GO](https://www.robotemi.com/robots/) 系列机器人是 temi 机器人家族的最新成员，它的诞生代表了 temi 在机器人领域的又一个探索与创新。

它继承了 temi 机器人标志性的设计语言，也沿袭了 temi 机器人上久经实际场景考验的硬件、系统、算法的结合。

temi GO 作为一个新诞生的机器人系列，它进化出了更多的可控、可配置的元件，如送货形态是的旋转舱门，及送餐形态下的托盘传感器、LED 指示灯等等。在软件可控的基础上，甚至还提供了硬件配置的自定义组合，以实现更多具体场景的可适应性。

基于相同的 SDK 接口，开发者可以零成本地把适用于原 temi 机器人的应用迁移到 temi GO 系列机器人上。实现同样效果的建图、导航、及自动化控制能力。此外，为了适应 temi GO 系列的硬件形态多样化的潜力，在 temi SDK 中，我们提供一套统一的用于发送指令及接收消息的接口。

> 你可以打开 SDK sample -> Resources -> Serial 页面，去试用基于这套统一的串口通讯接口下的功能。


<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|int|[sendSerialCommand(int command, byte[] data)](#sendSerialCommand)|发送串口指令|

|接口|描述|
|-|-|
|[OnSerialRawDataListener](#OnSerialRawDataListener)|串口数据监听|

<br>

## 方法

### sendSerialCommand()

所有的串口指令都是通过这个接口发出

最新的指令可以在这个类中查看 [com.robotemi.sdk.Serial](https://github.com/robotemi/sdk/blob/master/sdk/src/main/java/com/robotemi/sdk/serial/Serial.kt)


- **参数**

  |参数|类型|描述|
  |-|-|-|
  |command|int|Command id 指令 id，以 16 进制展示|
  |data|byte[]|指令数据|

- **返回值**

  |类型|描述|
  |-|-|
  |int|`0` 成功<br>`-1` 失败|

- **原型**

  ``` java
  int sendSerialCommand(int command, byte[] data);
  ```

- **所需权限**

  无

- **最小支持版本**

  1.129.2 (17009 temi Launcher)

---

## 接口

### OnSerialRawDataListener

所有的串口数据返回都通过这个监听接口以原始数据的形式返回

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnSerialRawDataListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|描述|
  |-|-|-|
  |data|byte[]|串口原始数据|

- **原型**

  ``` java
  void onSerialRawData(byte[] data)
  ```

#### 添加监听器的方法

- **原型**

  ``` java
  void addOnSerialRawDataListener(OnSerialRawDataListener listener);
  ```

#### 移除监听器的方法

- **原型**

  ``` java
  void removeOnSerialRawDataListener(OnSerialRawDataListener listener);
  ```

- **最小支持版本**

  1.129.2

---


<br>

### 指令列表

以下为发送指令实例，以及指令列表

``` kotlin

// 发送指令示例

// 校准托盘 Calibrate temi Tray
Robot.getInstance().sendSerialCommand(Serial.CMD_TRAY_CALIBRATE, byteArrayOf())

// 开启一号门 Open door 1.
Robot.getInstance().sendSerialCommand(Serial.CMD_DOOR_OPEN, byteArrayOf(1))

// 设置一号托盘 LED 为红色 Set tray 1 LED to red
Robot.getInstance().sendSerialCommand(Serial.CMD_TRAY_LIGHT, byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00))

// 设置 LCD 屏内容 Set LCD text
Robot.getInstance().sendSerialCommand(Serial.CMD_LCD_TEXT, Serial.getLcdBytes("Hello"))

// 设置 LCD 屏幕文字或背景颜色
Robot.getInstance().sendSerialCommand(
  Serial.CMD_LCD_TEXT,
  getLcdColorBytes(byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00), target = Serial.LCD.TEXT_0_COLOR)
)

Robot.getInstance().sendSerialCommand(
  Serial.CMD_LCD_TEXT,
  getLcdColorBytes(byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00), target = Serial.LCD.TEXT_0_BACKGROUND)
)

// 设置灯带为红、绿交替呼吸灯 Set Strip light to breathing red/green
Robot.getInstance().sendSerialCommand(
  Serial.CMD_STRIP_LIGHT,
  Serial.getStripBytes(
    mode = 2,
    primaryColor = byteArrayOf(0xff.toByte(), 0x00, 0x00),
    secondaryColor = byteArrayOf(0x00, 0xff.toByte(), 0x00),
    interval = 20
  )
)
```

|指令 ID|以 16 进制展示|指令数据|描述|
|-|-|-|-|
|`CMD_DOOR_CALIBRATE`|0x01|-|校准 temi GO 送货机器人舱门|
|`CMD_DOOR_OPEN`|0x02|1/2/3|开启舱门，需传入 1/2/3 为数据|
|`CMD_DOOR_CLOSE`|0x03|1/2/3|关闭舱门，需传入 1/2/3 为数据|
|`CMD_DOOR_MOTOR`|0x08|0/1|开启/关闭舱门电机|
|`CMD_STRIP_LIGHT`|0x04|查看 `Serial.getStripBytes()`|控制 LED 灯带颜色和模式|
|`CMD_TRAY_CALIBRATE`|0x1007|-|校准 temi GO 送餐机器人托盘，重置为空盘状态|
|`CMD_TRAY_SENSOR`|0x1008|0/1/2|查看 1/2/3 号托盘是否有物品|
|`CMD_TRAY_LIGHT`|0x1009|[0/1/2, R, G, B]|控制 1/2/3 号托盘 LED 颜色|
|`CMD_LCD_TEXT`|0x100B|查看 `Serial.getLcdBytes()`|控制 temi GO 送餐机器人背后 LCD 屏文字|
|`CMD_SYSTEM_GET_VERSION`|0x11|-|查询主控固件版本|
|`CMD_SYSTEM_START_OTA`|0x1005|-|私有 API，无法通过 SDK 调用。开启 OTA 模式|


---

<br>

### 返回值列表

串口数据会以 Byte 数组的形式从 `OnSerialRawDataListener` 中返回。

``` kotlin

// 读取返回值示例

override fun onSerialRawData(data: ByteArray) {
    // 获取指令 ID
    val cmd = data.cmd

    // 获取指令数据帧
    val dataFrame = data.dataFrame

    // 以 HEX 数组的形式展示数据内容
    Log.d("Serial", "cmd $cmd raw data ${data.dataHex}")

    when (cmd) {
        Serial.RESP_TRAY_SENSOR -> {

            // 数据帧第一位为托盘标号，从 0 起
            val trayIndex = dataFrame[0].toInt()
            val trayNum = trayIndex + 1

            // 数据帧第二位为该托盘是否有盛物，是(1)，否(0). 
            val loaded = dataFrame[1].toInt() == 1

            // 托盘上的置物重量, 1.8 固件后支持。
            val weight = dataFrame.weight

            val speech = if (loaded) {
                "托盘$trayNum已占用"
            } else {
                "托盘$trayNum已清空"
            }

            if (trayStatus[trayNum] != loaded) { // 用来保存托盘占用情况，避免过多播报和控制
              // 语音播报
              Robot.getInstance().speak(
                  TtsRequest.create(
                      speech,
                      isShowOnConversationLayer = false,
                      cached = true
                  )
              )

              if (loaded) {
                  // 将对应盛物托盘的 LED 灯设置为红色
                  Robot.getInstance().sendSerialCommand(
                      Serial.CMD_TRAY_LIGHT,
                      byteArrayOf(trayIndex, 0xFF.toByte(), 0x00, 0x00)
                  )
              } else {
                  // 将对应清空托盘的 LED 灯设置为绿色
                  Robot.getInstance().sendSerialCommand(
                      Serial.CMD_TRAY_LIGHT,
                      byteArrayOf(trayIndex, 0x20, 0xD1.toByte(), 0x99.toByte())
                  )
              }
            }
            trayStatus[trayNum] = loaded
        }
        Serial.RESP_TRAY_BACK_BUTTON -> {

            // 对数据帧首位判空
            val event = dataFrame.firstOrNull() ?: return

            Log.d("Serial", "Button data frame $event")

            val speech = when (event.toInt()) {
                0 -> "点击"
                1 -> "长按"
                2 -> "" // 长按后释放事件
                else -> ""
            }

            Robot.getInstance().speak(
                TtsRequest.create(
                    speech,
                    isShowOnConversationLayer = false,
                    cached = true
                )
            )
        }
        Serial.RESP_SYSTEM_VERSION -> {
            val decode = dataFrame.decodeToString()
            Log.d("Serial", "decode $decode")
            btnVersion.text = "Version:${dataFrame.decodeToString()}"
        }
    }
}

```


|指令 ID|以 16 进制展示|描述|
|-|-|-|
|`RESP_TRAY_SENSOR`|0x100B|托盘是否盛物|
|`RESP_TRAY_BACK_BUTTON`|0x06| temi GO 背后触摸键点击事件：点击(0)，长按 (1)|
|`RESP_SYSTEM_VERSION`|0x1003|系统版本|


---
<br>

### 备注

所有发送出去的指令及读取到的数据都为以下格式

```
| 0x5A, 0x01, CMD%256, CMD/256, LEN%256, LEN/256, DATA1, DATA2, ... , DATAn, CHECKSUM |
```

`0x5A`， `0x01` 为占位帧头

`CMD` 为指令 ID

`LEN` 表示其后 DATA 数据帧的长度

`CHECKSUM` 为校验位

<br>

SDK 用户不需要自己去手动装配原始指令，而在读取数据时只需要关心 `CMD` 和 `DATA` 片段。

参考上方示例，发送指令应直接调用 `sendSerialCommand(cmd, data)` 方法。其中 `Serial.getLcdBytes` 及 `Serial.getStripBytes` 是用来生成控制 LCD 屏文字及控制灯带模式/颜色指令的数据帧内容的 util 方法。

读取数据时，可以参照上方格式计算，也可以使用扩展方法 `Serial.cmd` 从 3、4 位来提取 `CMD` 指令 ID，使用 `Serial.dataFrame` 提取数据帧内容，以及 `Serial.dataHex` 来获得可以用于打印的 16 进制转换文本

更多收发串口消息的示例可以参考 SDK 源码及 sample 源码获取