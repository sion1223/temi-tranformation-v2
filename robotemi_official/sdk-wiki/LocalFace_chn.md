# 本地人脸注册

本地人脸注册于 131 版本加入，需要 SDK 版本为 1.131.2

此功能允许应用通过 SDK 注册人脸用于人脸识别功能，而无须使用 temi center 创建联系人及添加人脸。

通过应用注册的人脸只能被当前应用使用，无法被其它应用使用，也无法在迎宾模式中使用。只有应用在调用 [startFaceRecognition](https://github.com/robotemi/sdk/wiki/temi-Center_chn#startfacerecognition) 方法时，应用注册的人脸才会生效。


#### 隐私

通过应用注册的人脸将会被算法转换成匿名特征数据并存储于 temi 本机当中。特征数据为无字面意义的纯字符串，并且不能被反转成原始图片。本地注册的人脸数据不会在任何时候离开本机，只能在本地被注册的应用使用。注册用的图片也不会被保存或用于其它非算法特征提取的目的。

---

## 使用

每张照片注册只需要做一次，即可完成特征提取并存储在本地，并在之后任何时候的识别过程中起效。无须反复注册。

当应用开启人脸识别后，如果本地注册的人脸相比于 temi center 注册的人脸有更高的匹配度，则会返回本地注册的数据作为匹配结果。

注册、查询、删除人脸是通过 Content Provider 实现的，具体细节在下方列出。使用示例可以参考 [FaceActivity](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/FaceActivity.kt)，该页面可以通过 Resources -> Test Face Recognition 路请在 SDK 示例应用中启动。


> 所有的 API 都需要 **PRO** 专业版订阅以及 **FACE_RECOGNITION** 权限

### 人脸注册

每次注册一个人脸，都要提供  `uid`, `username`, 和 `uri`。前两个参数不是必须的，它们会在识别匹配的时候返回给应用。所以最好在实际注册过程中提供这两个信息。

`uid` 字符长度限制为 100, `username` 字符长度限制为 200.

`uri` 人脸图片 Uri 可用的 scheme 包括 `file://`, `https://`, `http://` 和 `content://`.

```Kotlin
val fileUri = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "sdk-sample.jpeg").toUri().toString()

val httpsUri = Uri.parse("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQREZ_D6BLC36i4kt8QdNVbXbmW-idmWRD5Xg")

val httpUri = Uri.parse("http://192.168.1.11:8080/files/face.jpg")

val contentUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", File(filesDir, "face1.jpg"))
// 对于来自应用的 content:// 资源，要先在 AndroidManifest.xml 中声明 FileProvider，并且在 res/xml/provider_paths 中添加 path
grantUriPermission("com.robotemi.face", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) // 必须授予权限给目标包名


val contentValues = ContentValues()
contentValues.put("uid", "0")   // 可选参数，可以为任何 String, 如应用本身用户系统的用户 UUID。字符最长为100
contentValues.put("username", "Jane Doe") // O 可选参数，可以为任何 String, 字符最长为200

// 如果只注册一张图
contentValues.put("uri", fileUri.toString())
contentResolver.insert(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), contentValues)

// 如果想一次性注册多张图。
val bundle = Bundle()
bundle.putParcelableArrayList("uri", arrayListOf(fileUri, httpUri))
contentResolver.insert(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), contentValues, bundle)
```

请注意：

如果你反复注册同一张图片，并使用相同的非空 uid，那么它将会替换掉已有的记录

譬如，如果依次执行

> 1. 注册 uid=0, username=王先生, image=001.jpeg
> 2. 注册 uid=0, username=王先生, image=001.jpeg
> 3. 注册 uid=0, username=胡小姐, image=001.jpeg

特征提取算法理论上是幂等的，所以同一张图片的特征值应该是相同的。将会根据 uid + 特征数据 的匹配进行去重。

最终只会注册一条记录，为 `[uid = 0, username = 胡小姐]`.

但是，如果你再次执行

> 4. 注册 uid=0, username=王先生, image=002.png

那么将会得到两条记录, `[uid = 0, username = 胡小姐]` and `[uid = 0, username = 王先生]`

所以建议应用端应维护一个 uid 系统，让用户数据之间不要冲突。

---
### 人脸查询

查询是通过 Content Provider 实现。它支持简单的 Content Provider SQLite 语法

使用以下示例代码，你可以查询并添加过滤条件。

```Kotlin
private fun queryAllFacesRegistered(selection: String? = null, selectionArgs: Array<String>? = null) {
    try {
        val cursor = contentResolver.query(
            Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"),
            arrayOf("username", "uid"),
            selection,
            selectionArgs,
            null)

        if (cursor != null) {
            val indexOrUserName = cursor.getColumnIndex("username")
            val colCount = cursor.columnCount
            var counter = 0
            var string = ""
            while (cursor.moveToNext()) {
                val username = cursor.getString(0)
                val uid = cursor.getString(1)
                string += "$counter: USER $username, Uid $uid\n"
                counter++
            }
            cursor.close()
            tvLog.text = string
            Log.d("Query", "$indexOrUserName, $colCount")
        }

    } catch (e: IllegalArgumentException) {
        Log.e("Query", "Query Exception", e)
    }
}

// 查询所有本应用注册的人脸，直接调用该方法。
queryAllFacesRegistered()

// 查询所有 uid == 0 的人脸
queryAllFacesRegistered("uid = ?", arrayOf("0"))

// 查询所有 uid == 0 和 2 的人脸
queryAllFacesRegistered("uid IN (?,?)", arrayOf("0", "2"))

// 查询所有用户名为 Jane Doe 的人脸
queryAllFacesRegistered("username = ?", arrayOf("Jane Doe"))

// 查询所有用户名中包含 "Ja" 的人脸
queryAllFacesRegistered("username LIKE ?", arrayOf("%Ja%"))
```

---
## 人脸删除

删除操作也是通过 Content Provider 实现，但是不支持 SQLite 语句。

删除本应用注册的所有人脸数据

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "", arrayOf())
```

根据 uid 删除所有人脸数据

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "uid", arrayOf("1", "2"))
```

根据 username 删除所有人脸数据

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "username", arrayOf("Jane Doe"))
```