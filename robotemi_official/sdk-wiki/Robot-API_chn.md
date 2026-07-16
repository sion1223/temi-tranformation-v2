## 静态方法

|返回值|方法|
|-|-|
|Robot|getInstance()|

## 公共成员方法

|返回值|方法|
|-|-|
|void|[askQuestion(String question)](https://github.com/robotemi/sdk/wiki/Speech_chn#askQuestion)|
|void|[beWith()](https://github.com/robotemi/sdk/wiki/Follow_chn#beWith)|
|void|[cancelAllTtsRequests()](https://github.com/robotemi/sdk/wiki/Speech_chn#cancelAllTtsRequests)|
|int|[checkSelfPermission(Permission permission)](https://github.com/robotemi/sdk/wiki/permission_chn#checkSelfPermission)|
|void|[constraintBeWith()](https://github.com/robotemi/sdk/wiki/Follow_chn#constraintBeWith)|
|boolean|[deleteLocation(String location)](https://github.com/robotemi/sdk/wiki/Locations_chn#deleteLocation)|
|void|[finishConversation()](https://github.com/robotemi/sdk/wiki/Speech_chn#finishConversation)|
|[UserInfo](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#userInfo)|[getAdminInfo()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#getAdminInfo)|
|List<[UserInfo](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#userInfo)>|[getAllContact()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#getAllContact)|
|List<[SequenceModel](https://github.com/robotemi/sdk/wiki/temi-Center_chn#sequenceModel)>|[getAllSequences()](https://github.com/robotemi/sdk/wiki/temi-Center#getAllSequences)|
|[Battery](https://github.com/robotemi/sdk/wiki/Utils_chn#batteryData)|[getBatteryData()](https://github.com/robotemi/sdk/wiki/Utils_chn#getBatteryData)|
|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils_chn#speedLevel)|[getGoToSpeed()](https://github.com/robotemi/sdk/wiki/Utils_chn#getGoToSpeed)|
|InputStream|[getInputStreamByMediaKey(ContentType contentType, String mediaKey)](https://github.com/robotemi/sdk/wiki/temi-Center_chn#getInputStreamByMediaKey)|
|String|[getLauncherVersion()](https://github.com/robotemi/sdk/wiki/Utils_chn#getLauncherVersion)|
|List\<String\>|[getLocations()](https://github.com/robotemi/sdk/wiki/Locations_chn#getLocations)|
|[MapData](https://github.com/robotemi/sdk/wiki/Locations_chn#mapDataModel)|[getMapData()](https://github.com/robotemi/sdk/wiki/Locations_chn#getMapData)|
|[SafetyLevel](https://github.com/robotemi/sdk/wiki/Utils_chn#safetyLevel)|[getNavigationSafety()](https://github.com/robotemi/sdk/wiki/Utils_chn#setNavigationSafety)|
|boolean|[getPrivacyMode()](https://github.com/robotemi/sdk/wiki/Utils_chn#getPrivacyMode)|
|List\<RecentCallModel\>|[getRecentCalls()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#getRecentCalls)|
|String|[getRoboxVersion()](https://github.com/robotemi/sdk/wiki/Utils_chn#getRoboxVersion)|
|String|[getSerialNumber()](https://github.com/robotemi/sdk/wiki/Utils_chn#getSerialNumber)|
|int|[getVolume()](https://github.com/robotemi/sdk/wiki/Utils_chn#getVolume)|
|String|[getWakeupWord()](https://github.com/robotemi/sdk/wiki/Speech_chn#getWakeupWord)|
|void|[hideTopBar()](https://github.com/robotemi/sdk/wiki/Utils_chn#hideTopBar)|
|boolean|[isAutoReturnOn()](https://github.com/robotemi/sdk/wiki/Utils_chn#isAutoReturnOn)|
|boolean|[isDetectionModeOn()](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#isDetectionModeOn)|
|boolean|[isHardButtonsDisabled()](https://github.com/robotemi/sdk/wiki/Utils_chn#isHardButtonsDisabled)|
|boolean|[isNavigationBillboardDisabled()](https://github.com/robotemi/sdk/wiki/Utils_chn#isNavigationBillboardDisabled)|
|boolean|isReady()|
|boolean|[isSelectedKioskApp()](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn#isSelectedKioskApp)|
|boolean|[isTopBadgeEnabled()](https://github.com/robotemi/sdk/wiki/Utils_chn#isTopBadgeEnabled)|
|boolean|[isWakeupDisabled()](https://github.com/robotemi/sdk/wiki/Utils_chn#isWakeupDisabled)|
|void|[playSequence(String sequenceId)](https://github.com/robotemi/sdk/wiki/temi-Center_chn#playSequence)|
|void|[repose()](https://github.com/robotemi/sdk/wiki/Locations_chn#repose)|
|void|[requestPermissions(List\<Permission\> permissions, int requestCode)](https://github.com/robotemi/sdk/wiki/permission_chn#requestPermissions)|
|void|[requestToBeKioskApp()](https://github.com/robotemi/sdk/wiki/Kiosk-Mode_chn#requestToBeKioskApp)|
|void|[restart()](https://github.com/robotemi/sdk/wiki/Utils_chn#restart)|
|void|[saveLocation(String location)](https://github.com/robotemi/sdk/wiki/Locations_chn#saveLocation)|
|void|[setAutoReturn(boolean on)](https://github.com/robotemi/sdk/wiki/Utils_chn#setAutoReturnOn)|
|void|[setDetectionModeOn(boolean on)](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#setDetectionModeOn)|
|void|[setDetectionModeOn(boolean on, float distance)](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction_chn#setDetectionModeOnWithDistance)|
|void|[setGoToSpeed(SpeedLevel level)](https://github.com/robotemi/sdk/wiki/Utils_chn#setGoToSpeed)|
|void|[setHardButtonsDisabled(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils_chn#setHardButtonsDisabled)|
|void|[setNavigationSafety(SafetyLevel level)](https://github.com/robotemi/sdk/wiki/Utils_chn#setNavigationSafety)|
|void|[setPrivacyMode(boolean on)](https://github.com/robotemi/sdk/wiki/Utils_chn#setPrivacyMode)|
|void|[setTopBadgeEnabled(boolean enabled)](https://github.com/robotemi/sdk/wiki/Utils_chn#setTopBadgeEnabled)|
|void|[setVolume(int volume)](https://github.com/robotemi/sdk/wiki/Utils_chn#setVolume)|
|void|[showAppList()](https://github.com/robotemi/sdk/wiki/Utils_chn#showAppList)|
|void|[showTopBar()](https://github.com/robotemi/sdk/wiki/Utils_chn#showTopBar)|
|void|[skidJoy(float x, float y)](https://github.com/robotemi/sdk/wiki/Movement_chn#skidJoy)|
|void|[speak(TtsRequest ttsRequest)](https://github.com/robotemi/sdk/wiki/Speech_chn#speak)|
|void|[startDefaultNlu(String text)](https://github.com/robotemi/sdk/wiki/Speech_chn#startDefaultNlu)|
|void|[startFaceRecognition()](https://github.com/robotemi/sdk/wiki/temi-Center_chn#startFaceRecognition)|
|void|[startTelepresence(String displayName, String peerId, Platform platform)](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence_chn#startTelepresence)|
|void|[stopFaceRecognition()](https://github.com/robotemi/sdk/wiki/temi-Center_chn#stopFaceRecognition)|
|void|[stopMovement()](https://github.com/robotemi/sdk/wiki/Movement_chn#stopMovement)|
|void|[tiltAngle(int degrees)](https://github.com/robotemi/sdk/wiki/Movement_chn#tiltAngle)|
|void|[tiltBy(int degrees)](https://github.com/robotemi/sdk/wiki/Movement_chn#tiltBy)|
|void|[toggleNavigationBillboard(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils_chn#toggleNavigationBillboard)|
|void|[toggleWakeup(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils_chn#toggleWakeup)|
|void|[turnBy(int degrees)](https://github.com/robotemi/sdk/wiki/Movement_chn#turnBy)|
|void|[wakeup()](https://github.com/robotemi/sdk/wiki/Speech_chn#wakeup)|
