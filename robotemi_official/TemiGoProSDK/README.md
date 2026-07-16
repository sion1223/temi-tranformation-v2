# SwitchGo SDK

[中文文档](README_CN.md)

SwitchGo is a hardware diagnostics and control SDK for TemiGoPro smart delivery robots. It communicates with the robot MCU via USB HID for door control, lighting, sensor monitoring, and firmware upgrades.

The demo app (`app/` module) serves as an SDK integration reference with complete usage examples.

## Integration

### 1. Add Dependency

Place `SwitchGoLibrary_<version>.aar` in `app/libs/` and add to `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/SwitchGoLibrary_1.1.7.aar"))
}
```

### 2. Permissions

```xml
<uses-feature android:name="android.hardware.usb.host" android:required="true" />
```

`USB_PERMISSION` is declared in the SDK's `AndroidManifest.xml` and merged automatically.

### 3. USB Device Filter

`res/xml/usb_device_filter.xml`:
```xml
<resources>
    <usb-device vendor-id="1155" product-id="22336" />
</resources>
```

## Usage

The SDK auto-initializes via `ContentProvider`. Get the instance directly:

```kotlin
val switchGo = SwitchGo.getInstance()
```

### Connection Lifecycle

```kotlin
// In onResume — start auto-connect
switchGo.startAutoConnect()

// Observe connection state
switchGo.isConnected.collect { connected -> ... }

// In onPause — stop
switchGo.stopAutoConnect()
```

### API Reference

| Category | Method | Description |
|----------|--------|-------------|
| Door | `controllerAllDoors(g1,g2,g3,g4)` | 2=close, 1=open, 0=skip |
| Door | `clearDoorBlockAlert(value)` | Clear anti-pinch alarm |
| Light | `toggleAmbientLight(mode,r,g,b)` | 0=off 1=flash 2=breathe 3=solid 4=gradient |
| Light | `controllerTurnSignal(left,right)` | Turn signals: 0=off, 1=flash |
| Light | `toggleInteriorLight(value)` | Interior light: 0=off, 1=on |
| Battery | `setBatteryIndicator(value)` | 0–100 |
| State | `getAllSwitchStates()` | Raw hex string (suspend) |
| State | `getFullState()` | Structured full state (suspend) |
| MCU | `getMcuVersion(value)` | 1=master, 2=slave (suspend) |
| MCU | `rebootMcu(value)` | Reboot MCU (suspend) |
| Firmware | `updateMcuVersion(value,path)` | Flash firmware |
| Firmware | `recoveryMcu(value,path)` | Recovery flash |
| Device | `getHidDevices()` | List connected HID devices |

> Firmware update progress via `McuUpdateCallback` (`onUpdateStart` / `onProgress` / `onSuccess` / `onFail`).

> `openDevice()` and `closeDevice()` are deprecated. Use `startAutoConnect()` / `stopAutoConnect()`.

See the demo app source for detailed examples.

## Version

| SDK | minSdk | Kotlin | Coroutines |
|-----|--------|--------|------------|
| 1.1.7 | 24 | 2.0 | 1.7.3 |
