# Activity Stream

Developers can share the image, video to the temi App(installed on mobile phone) from temi robot by this feature.

## API Overview

|Return|Method|
|-|-|
|void|[shareActivityObject(ActivityStreamObject activityStreamObject)](#shareActivityObject)|
|void|[setActivityStreamPublishListener(ActivityStreamPublishListener activityStreamPublishListener)](#setActivityStreamPublishListener)|

|Model|Description|
|-|-|
|[ActivityStreamObject](#activityStreamObject)|-|
|[ActivityStreamPublishListener](#activityStreamPublishListener)|-|

## Methods

### shareActivityObject()

Use this method to check share the activity object to mobile App.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |activityStreamObject|[ActivityStreamObject](#activityStreamObject)|The activity to be shared|

- **Prototype**

  ``` java
  void shareActivityObject(ActivityStreamObject activityStreamObject);
  ```

<br>

## Interfaces

### ActivityStreamPublishListener

Implement this listener interface in your context, and override the methods in the interface to listen to the result of activity stream publishing.

``` java
package com.robotemi.sdk;

interface Robot.ActivityStreamPublishListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |message|[ActivityStreamPublishMessage](#activityStreamPublishMessage)|Pending permissions|

- **Prototype**

  ``` java
  abstract void onPublish(ActivityStreamPublishMessage message);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRequestPermissionResultListener|The object of the class implements this listener interface|

- **Prototype**

  ``` java
  void setActivityStreamPublishListener(ActivityStreamPublishListener listener);
  ```

#### Method for removing listener

Pass `null` to method `setActivityStreamPublishListener()` to remove this listener.

## Model

The following is the data model used in the above methods and interfaces.

### ActivityStreamObject

#### Prototype

``` java
package com.robotemi.sdk.activitystream;

class ActivityStreamObject {}
```

#### Create instance

Currently, temi SDK can share fllowing kinds of activity stream to temi mobile App.

``` java
// Create a image activity stream
File photoFile = new File("/storage/emulated/0/photo.png");
ActivityStreamObject imageStream = ActivityStreamObject.builder()
        .activityType(ActivityStreamObject.ActivityType.PHOTO)
        .title("Activity stream title - Image")
        .media(MediaObject.create(MediaObject.MimeType.IMAGE, photoFile))
        .source("", ""))
        .build();

// Create a GIF activity stream
File gifFile = new File("/storage/emulated/0/gif.gif")
ActivityStreamObject gifStream = ActivityStreamObject.builder()
        .activityType(ActivityStreamObject.ActivityType.PHOTO)
        .title("Activity stream title - GIF")
        .media(MediaObject.create(MediaObject.MimeType.GIF, gifFile))
        .source("", ""))
        .build();

// Create a video activity stream
File videoFile = new File("/storage/emulated/0/video.mp4");
ActivityStreamObject videoStream = ActivityStreamObject.builder()
        .activityType(ActivityStreamObject.ActivityType.PHOTO)
        .title("Activity stream title - video")
        .media(MediaObject.create(MediaObject.MimeType.VIDEO, videoFile))
        .source(""), ""))
        .build();

// Create a simple text activity stream
ActivityStreamObject textStream = ActivityStreamObject.builder()
        .activityType(ActivityStreamObject.ActivityType.SIMPLE)
        .title("Simple text activity stream")
        .source(""), ""))
        .build();
```