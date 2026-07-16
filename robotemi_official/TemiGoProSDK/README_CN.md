# SwitchGo SDK

[English Documentation](README.md)

SwitchGo 是 TemiGoPro 智能配送机器人硬件诊断与控制 SDK，通过 USB HID 与 MCU 通信，提供仓门控制、灯光、传感器状态读取及固件升级功能。

Demo 应用（`app/` 模块）可作为 SDK 集成参考，展示了完整的使用方式。

## 集成

### 1. 添加依赖

将 `SwitchGoLibrary_<version>.aar` 放入 `app/libs/`，在 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(files("libs/SwitchGoLibrary_1.1.7.aar"))
}
```

### 2. 权限

```xml
<uses-feature android:name="android.hardware.usb.host" android:required="true" />
```

SDK 的 `AndroidManifest.xml` 已声明 `USB_PERMISSION`，通过 manifest merger 自动合并。

### 3. USB 设备过滤

`res/xml/usb_device_filter.xml`：
```xml
<resources>
    <usb-device vendor-id="1155" product-id="22336" />
</resources>
```

## 使用

SDK 通过 `ContentProvider` 自动初始化，直接获取实例即可：

```kotlin
val switchGo = SwitchGo.getInstance()
```

### 连接管理

```kotlin
// onResume 中开启自动连接
switchGo.startAutoConnect()

// 监听连接状态
switchGo.isConnected.collect { connected -> ... }

// onPause 中关闭
switchGo.stopAutoConnect()
```

### 主要 API

| 分类 | 方法 | 说明 |
|------|------|------|
| 仓门 | `controllerAllDoors(g1,g2,g3,g4)` | 2=关, 1=开, 0=不操作 |
| 仓门 | `clearDoorBlockAlert(value)` | 清除防夹报警 |
| 灯光 | `toggleAmbientLight(mode,r,g,b)` | 0=关 1=闪 2=呼吸 3=常亮 4=渐变 |
| 灯光 | `controllerTurnSignal(left,right)` | 转向灯 0=关 1=闪 |
| 灯光 | `toggleInteriorLight(value)` | 内部灯 0=关 1=开 |
| 电池 | `setBatteryIndicator(value)` | 0-100 |
| 状态 | `getAllSwitchStates()` | 原始 hex 字符串（suspend） |
| 状态 | `getFullState()` | 结构化完整状态（suspend） |
| MCU | `getMcuVersion(value)` | 1=主 2=从（suspend） |
| MCU | `rebootMcu(value)` | 重启 MCU（suspend） |
| 固件 | `updateMcuVersion(value,path)` | 升级固件 |
| 固件 | `recoveryMcu(value,path)` | 恢复固件 |
| 设备 | `getHidDevices()` | 已连接 HID 设备列表 |

> 固件升级进度通过 `McuUpdateCallback` 回调监听（`onUpdateStart` / `onProgress` / `onSuccess` / `onFail`）。

> `openDevice()` 和 `closeDevice()` 已废弃，请使用 `startAutoConnect()` / `stopAutoConnect()`。

更详细的用法请参考 Demo 应用源码。

## 版本

| SDK | minSdk | Kotlin | 协程 |
|-----|--------|--------|------|
| 1.1.7 | 24 | 2.0 | 1.7.3 |
