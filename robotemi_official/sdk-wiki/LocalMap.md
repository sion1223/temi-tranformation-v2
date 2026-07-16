# Local Map Backup

Local map backup is supported in 131 version, with SDK 1.131.3.

This feature is an offline version of temi center map backup. 

It enables apps to dump robot's current map into an archive file, then apps can save it to their local folder or upload it to their own cloud service. Eventually it can be reloaded into robot to replace current map.

This document will explain how to create a map backup, what are contained in the backup archive, and how to load a backup file.

All of the contents will have examples in [MapActivity](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/MapActivity.kt) of the SDK sample app, which can be launched in the sample app from RESOURCES -> Get Map Data.

---
## Create a map backup

**API:**

```kotlin
getCurrentMapBackupFile(withoutUI: Boolean): ParcelFileDescriptor?
```

This method will create a pipe between temi launcher and app, with the map backup file send as a `tar.gz`

You can call this method after the mapping is finished, so you cannot call it on an unsaved map.

**Permission required:**

Map

**Parameters**


  |Parameter|Type|Description|
  |-|-|-|
  |withoutUI|Boolean|Set to true, then it will only show a success notification when it completes<br>Set to false to show a block screen while doing backup.|


The following code snippet will start a coroutine on IO thread and write the file from pipe to a local file, e.g. `/sdcard/Android/data/com.robotemi.sdk.sample/files/maps/map-1690517863775.tar.gz`

```kotlin
// ⚠️ Make sure your app has declared and requested for MAP permission
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
## What is in the backup file

After saving the map backup as `map-1690517863775.tar.gz`, you may be interested in what is in the file, and how can you use it besides a backup.

In fact you can use this backup to create a list of map backups the same as temi center.

Unpack the file, you will find two files: `data.json` and `map_package_export.tar.gz`.

`data.json` contains almost the same information as [getMapData()](https://github.com/robotemi/sdk/wiki/Locations#getmapdata) provides, except its map image is compressed.

Its `map.data.data` is an empty array, but there will be a new String field `map.data.data_base64`. This string can be converted back to the data array by 

> decode Base64 -> un-gzip -> decode Base64

Here are the sample code of decode and un-gzip.

```kotlin
private fun decodeBase64(base: String): ByteArray {
    return android.util.Base64.decode(base, android.util.Base64.NO_WRAP)
}

private fun gunzip(content: ByteArray): String {
    val bytes = GZIPInputStream(content.inputStream()).use { it.readBytes() }
    return String(bytes)
}
```

Another String field `pbFilesUrl` represents the file MD5 of `map_package_export.tar.gz`, and will be used for validation when loading the map.


You have the same infomation in the `data.json` file as you have from `getMapData` including locations, virtual walls, green paths, map size, and map origins.

The other file `map_package_export.tar.gz` contains everything for algorithm to see its world, this file should not be unpacked or modified.

After you export the backup file, you can save it in you own cloud storage and use it in the furture to reload the map back.

---

## Load a map backup

To load a map backup file, the simplest file origin is using the one you exported. But just in case you have modified something in the `data.json`, or you want to mix up `map_package_export.tar.gz` from one backup with a different `data.json` from the same map but with another set of locations or virtual walls. Then SDK supports `tar.gz` format and `zip` format of backup file.

Please make sure your archive file, either `tar.gz` or `zip` has the same structure as below, with the 2 key files name unchanged and no nested folders.

```
my_map_archive.tar.gz
├─ data.json
└─ map_package_export.tar.gz
```

or

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

This method looks like [loadMap](https://github.com/robotemi/sdk/wiki/Locations#loadmap-), with similar parameters, except it requires an Uri, not a map Id, and it is always offline.

**Permission required:**

Map

**Parameters**


  |Parameter|Type|Description|
  |-|-|-|
  |uri|Uri|`file://` and `content://` schemes of URI are supported|
  |reposeRequired|boolean|Need to do repose after loading map or not, default as false|
  |position|[Position](https://github.com/robotemi/sdk/wiki/Locations#position)|The position of robot on the target map to loading the map. If not set, target map will be loaded from home base. Default as null|
  |withoutUI|boolean|Load the map in the background without showing any blocking UI, default as false|



In this method, URI supports `file://` and `content://` schemes. So apps can provide the file from their own internal storage or take the file from system file picker.

`content://` is more secured and perferred. https://developer.android.com/reference/androidx/core/content/FileProvider

`file://` , taking file from public storage, is still supported for testing or simplified integration.

Here is an example to list files stored in application internal storage and external storage, and pick one for map file to be loaded.

```kotlin
// ⚠️ Make sure your app has declared and requested for MAP permission
    buttonLoadMapFromPrivateFile.setOnClickListener {
        // This code block will load a map backup to temi.
        // The backup files are taken from either application's internal storage or external storage.
        // These files are securely store this way and transferred by content provider that only temi launcher can read.

        // First declare FileProvider in AndroidManifest.
        
        // The folder needs to be declared in res/xml/provider_paths.xml
        // <files-path name="map_internal_file" path="maps/" />
        val internalMapDirectory = File(filesDir, "maps")

        // The folder needs to be declared in res/xml/provider_paths.xml
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
                    // It is safe to delete the file here if needed.

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


Here is another example to use Android file picker intent to select one file to import

```Kotlin

    buttonLoadMapFromFileSelector.setOnClickListener {
        // This code block is launching a file picker to select a public accessible backup file.
        // So if you app is loaded in the USB drive on V3 robot, this could be an easy way to load it.

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("application/gzip", "application/zip") // Here we are picking tar.gz and zip files
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
                // It is safe to delete the file here if needed.
            }
        }
    }
}

```


To do it simple, you can use `file://` scheme, but this is not recommended

```Kotlin
buttonLoadMapFromPublicFile.setOnClickListener {
    // This is possible but not recommended.
    // As Android doesn't recommend to use file:// scheme to send files.
    val file = File("/sdcard/map-1690428181150.tar.gz")
    if (file.exists()) {
        loadMap(Uri.fromFile(file))
    } else {
        Toast.makeText(this, "Please place a map file at public storage", Toast.LENGTH_SHORT).show()
    }
}
```