# 本地地图备份

本地地图备份由 131 开始支持，需要配合 SDK 1.131.3 版本使用。

这个功能相当于离线版本的 temi center 备份地图功能。

应用可以通过相关接口将机器人的当前地图打包成一个备份文件，应用可以自行存储这一文件到本地或云端。最终可以使用备份文件重新恢复加载地图。

本文档将分别介绍如何创建地图备份，备份文件内部构成，如何加载地图备份

所有内容都在 sample 应用 [MapActivity](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/MapActivity.kt) 中提供了调用示例。这个页面也可以在示例应用中的 RESOURCES -> Get Map Data 选项中启动。

---
## 创建地图备份

**API:**

```kotlin
getCurrentMapBackupFile(withoutUI: Boolean): ParcelFileDescriptor?
```

此方法会创建一个文件管道，将地图文件以 `tar.gz` 压缩包的方式从 temi launcher 传输给应用

方法仅在完成绘制地图后可用，如果地图正在绘制或尚未锁定都无法调用该方法。

**所需权限:**

Map

**参数**


  |参数|类型|描述|
  |-|-|-|
  |withoutUI|Boolean|true 不显示阻塞的UI，仅在成功后弹通知提示<br>false，过程中显示一个全屏的备份地图 UI|


以下代码片段将在 IO 线程启用一个 coroutine，将传入的文件写到本地路径，如： `/sdcard/Android/data/com.robotemi.sdk.sample/files/maps/map-1690517863775.tar.gz`

```kotlin
// ⚠️ 请确保 Launcher 版本高于 17579，并且应用已经声明并申请 MAP 权限。
buttonBackupMap.setOnClickListener {
    val parcelFileDescriptor = try {
        Robot.getInstance().getCurrentMapBackupFile(withoutUI = true) ?: return@setOnClickListener
    } catch (e: FileNotFoundException) {
        return@setOnClickListener
    }
    lifecycleScope.launch(Dispatchers.IO) {
        val dir = File(applicationContext.getExternalFilesDir(null), "maps")
         if (!dir.exists()) {
            dir.mkdir()
        }

        val file = File(dir, "map-${System.currentTimeMillis()}.tar.gz")
        file.createNewFile()
        val inputStream = ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        if (file.length() > 0) {
            launch(Dispatchers.Main) {
                Toast.makeText(applicationContext, "File generated", Toast.LENGTH_SHORT).show()
            }
        }   
    }
}
```

---
## 备份文件内部构成

当地图已经备份为诸如 `map-1690517863775.tar.gz` 的压缩包之后，你可能会想知道压缩包中有哪些文件构成，哪些数据可以为你所用。

实际上，如果你得到了一系列的压缩包后，你将有可能展现出如 temi center 地图列表一样的属于你自己的地图备份列表。

首先解压这个文件，你将会得到两个文件： `data.json` 和 `map_package_export.tar.gz`。

`data.json` 包含了与 [getMapData()](https://github.com/robotemi/sdk/wiki/Locations_chn#getmapdata) 接口提供的几乎一致的数据，除了其地图图像是经过压缩的。

其中 `map.data.data` 字段将不再有数据，是一个空的数组。但会提供一个新的 String 字段 `map.data.data_base64`。这一 String 字段可以通过以下方法被还原成数组

> Base64 解码 -> gzip 解压 -> Base64 解码

下面是解码和解压的示例方法

```kotlin
private fun decodeBase64(base: String): ByteArray {
    return android.util.Base64.decode(base, android.util.Base64.NO_WRAP)
}

private fun gunzip(content: ByteArray): String {
    val bytes = GZIPInputStream(content.inputStream()).use { it.readBytes() }
    return String(bytes)
}
```

另一个新的字段 `pbFilesUrl` 记录了了 `map_package_export.tar.gz` 这个文件的 MD5 值，在之后导入地图的时候会被用作校验。


其余 `data.json` 提供的数据与 `getMapData` 方法所提供的一致，包括地点，虚拟墙，导航路径，地图尺寸，原点等信息。

另一个文件 `map_package_export.tar.gz` 包含了算法所需的地图数据，该文件不应被解压或修改其中内容。

综上，你得到的这个备份文件就是可以用来保存的地图备份文件。也可以用来恢复地图备份。

---

## 加载地图备份

想要加载恢复一个地图备份，最简单的办法就是使用之前备份的 `tar.gz` 文件。但是假如你修改过 `data.json` 文件中的内容，或者你想要将 `map_package_export.tar.gz` 文件与另一个同一地图但不同地图元素的 `data.json` 文件混合使用。你可以使用自行打包的 `tar.gz` 文件或 `zip` 文件用来加载地图。

如果你曾修改过压缩包或自行打包，请确保 `tar.gz` 或 `zip` 中的文件结构如下方示例一致，包含两个同名关键文件，并且没有把两个文件内嵌在文件夹中。

```
my_map_archive.tar.gz
├─ data.json
└─ map_package_export.tar.gz
```

或

```
my_map_archive.zip
├─ data.json
└─ map_package_export.tar.gz
```


**API:**

```kotlin
loadMapWithBackupFile(
    uri: Uri,
    reposeRequired: Boolean = false,
    position: Position? = null,
    withoutUI: Boolean = false)
```

这一方法 与[loadMap](https://github.com/robotemi/sdk/wiki/Locations_chn#loadmap-) 类似，使用相似的参数，用 Uri 替代了 map id，并且此方法不再需要 offline 参数，因为它就是离线工作的。

**所需权限:**

Map

**参数**


  |参数|类型|描述|
  |-|-|-|
  |uri|Uri|支持 `file://` 和 `content://` 两种 Uri scheme|
  |reposeRequired|boolean|地图加载完成后是否需要做重定位, 默认为 false|
  |position|[Position](https://github.com/robotemi/sdk/wiki/Locations_chn#position)|指定从哪个位置（目标地图上的坐标）加载目标地图，默认为null，则从目标地图的充电桩位置加载地图|
  |withoutUI|boolean|不显示全屏阻塞加载地图 UI，默认为 false|



在这一方法中, URI 支持 `file://` 和 `content://` 两种 scheme。用于从应用传递文件给 temi launcher.

`content://` 更为推荐 https://developer.android.com/reference/androidx/core/content/FileProvider

`file://` 将从公共存储提取文件，建议仅用于调试。

以下是示例，从应用的私有内部和外部存储中列出地图文件，从弹窗中选择要加载的地图。

```kotlin

    buttonLoadMapFromPrivateFile.setOnClickListener {

        // 使用 FileProvider 首先要在 AndroidManifest.xml 中声明 provider

        // 然后注意要在 res/xml/provider_paths.xml 加上声明
        // <files-path name="map_internal_file" path="maps/" />
        val internalMapDirectory = File(filesDir, "maps")

        // 注意要在 res/xml/provider_paths.xml 加上声明
        // <external-files-path name="map_external_file" path="maps/"/>
        val externalMapDirectory = File(getExternalFilesDir(null), "maps")


        lifecycleScope.launch(Dispatchers.IO) {
            val internalFiles = internalMapDirectory.listFiles()?.toList() ?: listOf()
            val externalFiles = externalMapDirectory.listFiles()?.toList() ?: listOf()
            val files = (internalFiles + externalFiles).filter {
                it.isFile && it.path.endsWith("tar.gz", true)
            }

            val builder = AlertDialog.Builder(this@MapActivity)

            if (files.isNotEmpty()) {
                builder.setItems(files.map { it.path }.toTypedArray()) { _, which ->
                    val fileSelected = files[which]
                    Log.d("SDK-Sample", "Map file selected ${fileSelected.path}")
                    val uri =
                        FileProvider.getUriForFile(this@MapActivity, AUTHORITY, fileSelected)
                    loadMap(uri)
                    // 执行到此处就可以删除原始文件了。 

                }.setTitle("Select one map file to load")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
            } else {
                builder.setTitle("No map backup files found")
                    .setMessage("This sample takes map files from\n/sdcard/Android/data/com.robotemi.sdk.sample/files/maps/\nand /data/data/com.robotemi.sdk.sample/files/maps/")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
            }

            launch(Dispatchers.Main) {
                builder.show()
            }
        }
    }

...

private fun loadMap(uri: Uri) {
    val reposeRequired = checkBoxLoadMapWithRepose.isChecked
    val withoutUI = checkBoxLoadMapWithoutUI.isChecked
    val position: Position? = if (checkBoxLoadMapFromPose.isChecked) {
        Position(1f, 1f, 1f)
    } else {
        null
    }
    Robot.getInstance().loadMapWithBackupFile(
        uri,
        reposeRequired = reposeRequired,
        withoutUI = withoutUI,
        position = position
    )
}
```


下方示例是从 Android 文件选择器中选取要加载的地图文件

```Kotlin

    buttonLoadMapFromFileSelector.setOnClickListener {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("application/gzip", "application/zip") // 这里过滤了 tar.gz 和 zip 两种类型的文件
        intent.setType("*/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, REQUEST_FILE_PICKER)
    }

...

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK) {
        if (data != null) {
            val selectedFileUri = data.data

            if (selectedFileUri != null) {
                loadMap(selectedFileUri)
                Log.d("SDK-Sample", "Map file loaded")
            }
        }
    }
}

```


如果仅为了快速集成，也可以使用 `file://` scheme

```Kotlin
buttonLoadMapFromPublicFile.setOnClickListener {
    val file = File("/sdcard/map-1690428181150.tar.gz")
    if (file.exists()) {
        loadMap(Uri.fromFile(file))
    } else {
        Toast.makeText(this, "请在对应位置放入地图备份文件", Toast.LENGTH_SHORT).show()
    }
}
```