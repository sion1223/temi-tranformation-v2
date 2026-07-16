# Local Face Registration

Local face registration is supported in 131 version, with SDK 1.131.2

This feature allows your app to register faces for recogniton offline, without the need to create contacts or add faces on temi center.

The faces registered by your app belongs to your app exclusively, they cannot be used by other apps and not even in Dynamic (Greet) Mode. It can only be used when you app calls the method [startFaceRecognition](https://github.com/robotemi/sdk/wiki/temi-Center#startfacerecognition).


#### Privacy

Faces registered to temi will be converted by algorithm and stored as anomymous face feature data. This data is a pure string and cannot be reverted back to the image. Also the data will not leave the robot at any time, as it is only used locally when your app starts face recognition. The images will not be saved or used for other purpose.

---

## Usage

You only need to register a face once, and use it at any time when your app start face recognition.

After face recognition start, if local registered face has a better match than faces registered from temi center, then local registered user will be returned as detected user.

The registration, query, and delete of faces are made with Content Provider, details will be listed below. Examples can be found in [FaceActivity](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/FaceActivity.kt), which can be launched in the sample app from Resources -> Test Face Recognition.


> ⚠️ All the APIs will require **PRO** subscription and **FACE_RECOGNITION** permission.

### Register Faces

To register face to temi. You need to provide `uid`, `username`, and `uri`. The former two field are not compulsory, they will be returned to you when the similiar face is detected, so it is better to provider them to know who is actually detected when put in use. 

The max length for `uid` is 100 and for `username` is 200.

Face image uri accepts the following schemes. `file://`, `https://`, `http://` and `content://`.

```Kotlin

// ⚠️ Make sure your app has declared and requested for FACE_RECOGNITION permission
val fileUri = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "sdk-sample.jpeg").toUri().toString()

val httpsUri = Uri.parse("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQREZ_D6BLC36i4kt8QdNVbXbmW-idmWRD5Xg")

val httpUri = Uri.parse("http://192.168.1.11:8080/files/face.jpg")

val contentUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", File(filesDir, "face1.jpg"))
// Declare FileProvider in AndroidManifest.xml and add supported paths in res/xml/provider_paths
grantUriPermission("com.robotemi.face", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) // This is a must for FileProvider.


val contentValues = ContentValues()
contentValues.put("uid", "0")   // Optional. Any string is OK, can be an UUID from your user system. Max length is 100.
contentValues.put("username", "Jane Doe") // Optional. Any string is OK. Max length is 200.

// To register just one image
contentValues.put("uri", fileUri.toString())
contentResolver.insert(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), contentValues)

// Or, to register multiple faces for one user
val bundle = Bundle()
bundle.putParcelableArrayList("uri", arrayListOf(fileUri, httpUri))
contentResolver.insert(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), contentValues, bundle)
```

Notice:

If you added faces with the same image and non-null uid, it will replace the existing one by doing nothing or updating the username field.

i.e. If you do 

> 1. Register uid=0, username=John, image=001.jpeg
> 2. Register uid=0, username=John, image=001.jpeg
> 3. Register uid=0, username=Jerry, image=001.jpeg

The algorithm is suppose to be idempotent, so the same image will end up with the same feature data, and the duplication is checked by uid + feature data.

You will only end up with 1 face registered, which is `[uid = 0, username = Jerry]`.

But if you do 

> 4. Register uid=0, username=John, image=002.png

You will end up with 2 faces, `[uid = 0, username = Jerry]` and `[uid = 0, username = John]`

So keep uid consistant is a good practice.

---
### Query Faces

Query is also made through Content Provider as well. It should support simple SQLite Query with Content Provider

With this method in the sample code. you can query with filters.

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

// To query all faces registered by your app. Just call the method.
queryAllFacesRegistered()

// To query all faces with uid == 0
queryAllFacesRegistered("uid = ?", arrayOf("0"))

// To query all faces with uid == 0 and 2
queryAllFacesRegistered("uid IN (?,?)", arrayOf("0", "2"))

// To query all faces with username Jane Doe
queryAllFacesRegistered("username = ?", arrayOf("Jane Doe"))

// To query all faces with username contains "Ja"
queryAllFacesRegistered("username LIKE ?", arrayOf("%Ja%"))
```

---
## Delete Faces

Delete is also made through Content Provider, but it doesn't support SQLite Query.

To delete all faces registered by your app.

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "", arrayOf())
```

To delete all faces by uid

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "uid", arrayOf("1", "2"))
```

To delete all faces by username

```Kotlin
contentResolver.delete(Uri.parse("content://com.robotemi.sdk.TemiSdkDocumentContentProvider/face"), "username", arrayOf("Jane Doe"))
```