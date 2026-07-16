[temi GO](https://www.robotemi.com/robots/) series as a new member in the temi robot family, is temi's next exploration and innovation in the robotics industry.

It inherts temi's featured design language and derives from the same technologies and algorithms that powered all the temi robots.

temi GO robot as a new species, has evolved with more hardware level controllable and configurable components, like the rotation compartment door for delivery, tray with sensors and LEDs. We also aimed to provide customizable hardware to fit various of needs in practice.

Like temi robot, with the same sets of SDK interfaces, developers can migrate their apps from temi robot to temi GO with zero efforts to achieve the same capabilities of mapping, navigation, and automation. To deal with the new dynamical and controllable hardware in temi GO, in temi SDK, the solution is to use unified interface to send commands and read status and responses.

> In temi SDK sample -> Resources -> Serial, you can try this unified serial communication interface.


<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|int|[sendSerialCommand(int command, byte[] data)](#sendSerialCommand)|Send command to serial interface|

|Interface|Description|
|-|-|
|[OnSerialRawDataListener](#OnSerialRawDataListener)|Serial raw data listener|

<br>

## Methods

### sendSerialCommand()

All the control and query commands are sent from this interface.

The latest serial commands can be found at this class [com.robotemi.sdk.Serial](https://github.com/robotemi/sdk/blob/master/sdk/src/main/java/com/robotemi/sdk/serial/Serial.kt)


- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |command|int|Command id, will be represeted in hex format|
  |data|byte[]|Data content of command|

- **Return**

  |Type|Description|
  |-|-|
  |int|`0` success<br>`-1` fail|

- **Prototype**

  ``` java
  int sendSerialCommand(int command, byte[] data);
  ```

- **Required permissions**

  None.

- **Support from**

  1.129.2 (17009 temi Launcher)

---

## Interfaces

### OnSerialRawDataListener

All the serial responses and status will be returned from this listener in raw data format.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnSerialRawDataListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |data|byte[]|Serial raw data|

- **Prototype**

  ``` java
  void onSerialRawData(byte[] data)
  ```

#### Method for adding listener

- **Prototype**

  ``` java
  void addOnSerialRawDataListener(OnSerialRawDataListener listener);
  ```

#### Method for removing listener

- **Prototype**

  ``` java
  void removeOnSerialRawDataListener(OnSerialRawDataListener listener);
  ```

- **Support from**

  1.129.2

---


<br>

### Commands list

To use command, send it like this.

``` kotlin

// Examples of send

// Calibrate temi Tray
Robot.getInstance().sendSerialCommand(Serial.CMD_TRAY_CALIBRATE, byteArrayOf())

// Open door 1.
Robot.getInstance().sendSerialCommand(Serial.CMD_DOOR_OPEN, byteArrayOf(1))

// Set tray 1 LED to red
Robot.getInstance().sendSerialCommand(Serial.CMD_TRAY_LIGHT, byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00))

// Set LCD text
Robot.getInstance().sendSerialCommand(Serial.CMD_LCD_TEXT, Serial.getLcdBytes("Hello"))

// Set LCD text or background color
Robot.getInstance().sendSerialCommand(
  Serial.CMD_LCD_TEXT,
  getLcdColorBytes(byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00), target = Serial.LCD.TEXT_0_COLOR)
)

Robot.getInstance().sendSerialCommand(
  Serial.CMD_LCD_TEXT,
  getLcdColorBytes(byteArrayOf(0x00, 0xff.toByte(), 0x00, 0x00), target = Serial.LCD.TEXT_0_BACKGROUND)
)

// Set Strip light to breathing red/green
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

|Command id|Id in hex|Data|Description|
|-|-|-|-|
|`CMD_DOOR_CALIBRATE`|0x01|-|Calibrate temi Go delivery compartment door|
|`CMD_DOOR_OPEN`|0x02|1/2/3|Open target door, data is target door number|
|`CMD_DOOR_CLOSE`|0x03|1/2/3|Close target door, data is target door number|
|`CMD_DOOR_MOTOR`|0x08|0/1|Turn ON/OFF door motor|
|`CMD_STRIP_LIGHT`|0x04|See Serial.getStripBytes()|Control light strip pattern and color|
|`CMD_TRAY_CALIBRATE`|0x1007|-|Calibrate tray sensor to reset it as empty|
|`CMD_TRAY_SENSOR`|0x1008|0/1/2|Query if tray 1/2/3 is occupied|
|`CMD_TRAY_LIGHT`|0x1009|[0/1/2, R, G, B]|Set tray 1/2/3 LED RGB color|
|`CMD_LCD_TEXT`|0x100B|See Serial.getLcdBytes()|Change text of the LCD on the back of tray robot|
|`CMD_SYSTEM_GET_VERSION`|0x11|-|Get MCU version|
|`CMD_SYSTEM_START_OTA`|0x1005|-|Hidden API to start OTA, cannot used by SDK|


---

<br>

### Responses list

The serial response raw data is sent back from `OnSerialRawDataListener` in the format of byte array.

``` kotlin

// Examples of read

override fun onSerialRawData(data: ByteArray) {
    // Command id of response
    val cmd = data.cmd

    // Data frame of response
    val dataFrame = data.dataFrame

    // To see the hex array of raw data
    Log.d("Serial", "cmd $cmd raw data ${data.dataHex}")

    when (cmd) {
        Serial.RESP_TRAY_SENSOR -> {

            // The first place in data frame stands for tray number, starts from 0
            val trayIndex = dataFrame[0].toInt()
            val trayNum = trayIndex + 1

            // The second place in data frame stands for tray occupied(1) or empty(0). 
            val loaded = dataFrame[1].toInt() == 1

            // Weight on the tray， supported since MCU 1.8 firmware
            val weight = dataFrame.weight

            val speech = if (loaded) {
                "Tray $trayNum is loaded"
            } else {
                "Tray $trayNum is empty"
            }

            if (trayStatus[trayNum] != loaded) { // Broadcast when the loaded status is changed.
              // Speak it out load
              Robot.getInstance().speak(
                  TtsRequest.create(
                      speech,
                      isShowOnConversationLayer = false,
                      cached = true
                  )
              )

              if (loaded) {

                  // Set tray light to red when it is occupied
                  Robot.getInstance().sendSerialCommand(
                      Serial.CMD_TRAY_LIGHT,
                      byteArrayOf(trayIndex, 0xFF.toByte(), 0x00, 0x00)
                  )
              } else {

                  // Set tray light to green when it is empty
                  Robot.getInstance().sendSerialCommand(
                      Serial.CMD_TRAY_LIGHT,
                      byteArrayOf(trayIndex, 0x20, 0xD1.toByte(), 0x99.toByte())
                  )
              }
            }
            trayStatus[trayNum] = loaded
        }
        Serial.RESP_TRAY_BACK_BUTTON -> {

            // Just in case it doesn't exist
            val event = dataFrame.firstOrNull() ?: return

            Log.d("Serial", "Button data frame $event")

            val speech = when (event.toInt()) {
                0 -> "touch"
                1 -> "press"
                2 -> "" // release after press
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


|Command id|Id in hex|Description|
|-|-|-|
|`RESP_TRAY_SENSOR`|0x100B|A tray is loaded or emptied|
|`RESP_TRAY_BACK_BUTTON`|0x06|The touch button on the back of tray robot is touched (0) or pressed (1)|
|`RESP_SYSTEM_VERSION`|0x1003|System version in hex string|


---
<br>

### Notes

Every command and response is composed with such format.

```
| 0x5A, 0x01, CMD%256, CMD/256, LEN%256, LEN/256, DATA1, DATA2, ... , DATAn, CHECKSUM |
```

`0x5A` and `0x01` is a placeholder

`CMD` is the command or response id.

`LEN` is the length of `DATA` frame coming next to it.

`CHECKSUM` is used for validation.

<br>

SDK user doesn't construct the raw data directly when sending commands and only needs to care about `CMD` and `DATA` section when reading responses.

As shown above, sending command is wrapped into `sendSerialCommand(cmd, data)`. There are util methods `Serial.getLcdBytes` and `Serial.getStripBytes` when change LCD screen text and changing light strip color.

To read responses, there are Kotlin extension methods `Serial.cmd` to extract `CMD` from 3rd and 4th place of raw data. Also `Serial.dataFrame` to extract data frame, and finally `Serial.dataHex` if you want to see how the raw data looks.

Please check the SDK source and SDK example for how to send and read in practice.



### How to tell robot version

There hasn't been an interface in SDK to support that. But this can be done with the following code.


```kotlin
fun getRobotType(): RobotType {
    val serialNumber = robot.getSerialNumber()
    val isV2 = Build.MODEL.startsWith("rk3288")

    return when {
        (!isV2 && serialNumber.length == 11 && serialNumber.startsWith("0022")) -> RobotType.PLATFORM
        !isV2 -> {
            if (serialNumber.length == 11 && serialNumber[2] == '3' && !serialNumber.startsWith("00319") ) {
                RobotType.TRAY
            } else {
                RobotType.V3
            }
        }
        else -> RobotType.V2
    }
}

```

