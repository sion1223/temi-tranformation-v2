## Static Methods

|Return|Method|
|-|-|
|Robot|getInstance()|

## Public Member Methods

|Return|Method|
|-|-|
|void|[askQuestion(String question)](https://github.com/robotemi/sdk/wiki/Speech#askQuestion)|
|void|[beWith()](https://github.com/robotemi/sdk/wiki/Follow#beWith)|
|void|[cancelAllTtsRequests()](https://github.com/robotemi/sdk/wiki/Speech#cancelAllTtsRequests)|
|int|[checkSelfPermission(Permission permission)](https://github.com/robotemi/sdk/wiki/permission#checkSelfPermission)|
|void|[constraintBeWith()](https://github.com/robotemi/sdk/wiki/Follow#constraintBeWith)|
|boolean|[deleteLocation(String location)](https://github.com/robotemi/sdk/wiki/Locations#deleteLocation)|
|void|[finishConversation()](https://github.com/robotemi/sdk/wiki/Speech#finishConversation)|
|[UserInfo](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#userInfo)|[getAdminInfo()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#getAdminInfo)|
|List<[UserInfo](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#userInfo)>|[getAllContact()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#getAllContact)|
|List<[SequenceModel](https://github.com/robotemi/sdk/wiki/temi-Center#sequenceModel)>|[getAllSequences()](https://github.com/robotemi/sdk/wiki/temi-Center#getAllSequences)|
|[Battery](https://github.com/robotemi/sdk/wiki/Utils#batteryData)|[getBatteryData()](https://github.com/robotemi/sdk/wiki/Utils#getBatteryData)|
|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils#speedLevel)|[getGoToSpeed()](https://github.com/robotemi/sdk/wiki/Utils#getGoToSpeed)|
|InputStream|[getInputStreamByMediaKey(ContentType contentType, String mediaKey)](https://github.com/robotemi/sdk/wiki/temi-Center#getInputStreamByMediaKey)|
|String|[getLauncherVersion()](https://github.com/robotemi/sdk/wiki/Utils#getLauncherVersion)|
|List\<String\>|[getLocations()](https://github.com/robotemi/sdk/wiki/Locations#getLocations)|
|[MapData](https://github.com/robotemi/sdk/wiki/Locations#mapDataModel)|[getMapData()](https://github.com/robotemi/sdk/wiki/Locations#getMapData)|
|[SafetyLevel](https://github.com/robotemi/sdk/wiki/Utils#safetyLevel)|[getNavigationSafety()](https://github.com/robotemi/sdk/wiki/Utils#setNavigationSafety)|
|boolean|[getPrivacyMode()](https://github.com/robotemi/sdk/wiki/Utils#getPrivacyMode)|
|List\<RecentCallModel\>|[getRecentCalls()](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#getRecentCalls)|
|String|[getRoboxVersion()](https://github.com/robotemi/sdk/wiki/Utils#getRoboxVersion)|
|String|[getSerialNumber()](https://github.com/robotemi/sdk/wiki/Utils#getSerialNumber)|
|int|[getVolume()](https://github.com/robotemi/sdk/wiki/Utils#getVolume)|
|String|[getWakeupWord()](https://github.com/robotemi/sdk/wiki/Speech#getWakeupWord)|
|void|[hideTopBar()](https://github.com/robotemi/sdk/wiki/Utils#hideTopBar)|
|boolean|[isAutoReturnOn()](https://github.com/robotemi/sdk/wiki/Utils#isAutoReturnOn)|
|boolean|[isDetectionModeOn()](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#isDetectionModeOn)|
|boolean|[isHardButtonsDisabled()](https://github.com/robotemi/sdk/wiki/Utils#isHardButtonsDisabled)|
|boolean|[isNavigationBillboardDisabled()](https://github.com/robotemi/sdk/wiki/Utils#isNavigationBillboardDisabled)|
|boolean|isReady()|
|boolean|[isSelectedKioskApp()](https://github.com/robotemi/sdk/wiki/Kiosk-Mode#isSelectedKioskApp)|
|boolean|[isTopBadgeEnabled()](https://github.com/robotemi/sdk/wiki/Utils#isTopBadgeEnabled)|
|boolean|[isWakeupDisabled()](https://github.com/robotemi/sdk/wiki/Utils#isWakeupDisabled)|
|void|[playSequence(String sequenceId)](https://github.com/robotemi/sdk/wiki/temi-Center#playSequence)|
|void|[repose()](https://github.com/robotemi/sdk/wiki/Locations#repose)|
|void|[requestPermissions(List\<Permission\> permissions, int requestCode)](https://github.com/robotemi/sdk/wiki/permission#requestPermissions)|
|void|[requestToBeKioskApp()](https://github.com/robotemi/sdk/wiki/Kiosk-Mode#requestToBeKioskApp)|
|void|[restart()](https://github.com/robotemi/sdk/wiki/Utils#restart)|
|void|[saveLocation(String location)](https://github.com/robotemi/sdk/wiki/Locations#saveLocation)|
|void|[setAutoReturn(boolean on)](https://github.com/robotemi/sdk/wiki/Utils#setAutoReturnOn)|
|void|[setDetectionModeOn(boolean on)](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#setDetectionModeOn)|
|void|[setDetectionModeOn(boolean on, float distance)](https://github.com/robotemi/sdk/wiki/Detection-&-Interaction#setDetectionModeOnWithDistance)|
|void|[setGoToSpeed(SpeedLevel level)](https://github.com/robotemi/sdk/wiki/Utils#setGoToSpeed)|
|void|[setHardButtonsDisabled(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils#setHardButtonsDisabled)|
|void|[setNavigationSafety(SafetyLevel level)](https://github.com/robotemi/sdk/wiki/Utils#setNavigationSafety)|
|void|[setPrivacyMode(boolean on)](https://github.com/robotemi/sdk/wiki/Utils#setPrivacyMode)|
|void|[setTopBadgeEnabled(boolean enabled)](https://github.com/robotemi/sdk/wiki/Utils#setTopBadgeEnabled)|
|void|[setVolume(int volume)](https://github.com/robotemi/sdk/wiki/Utils#setVolume)|
|void|[showAppList()](https://github.com/robotemi/sdk/wiki/Utils#showAppList)|
|void|[showTopBar()](https://github.com/robotemi/sdk/wiki/Utils#showTopBar)|
|void|[skidJoy(float x, float y)](https://github.com/robotemi/sdk/wiki/Movement#skidJoy)|
|void|[speak(TtsRequest ttsRequest)](https://github.com/robotemi/sdk/wiki/Speech#speak)|
|void|[startDefaultNlu(String text)](https://github.com/robotemi/sdk/wiki/Speech#startDefaultNlu)|
|void|[startFaceRecognition()](https://github.com/robotemi/sdk/wiki/temi-Center#startFaceRecognition)|
|void|[startTelepresence(String displayName, String peerId, Platform platform)](https://github.com/robotemi/sdk/wiki/Users-&-Telepresence#startTelepresence)|
|void|[stopFaceRecognition()](https://github.com/robotemi/sdk/wiki/temi-Center#stopFaceRecognition)|
|void|[stopMovement()](https://github.com/robotemi/sdk/wiki/Movement#stopMovement)|
|void|[tiltAngle(int degrees)](https://github.com/robotemi/sdk/wiki/Movement#tiltAngle)|
|void|[tiltBy(int degrees)](https://github.com/robotemi/sdk/wiki/Movement#tiltBy)|
|void|[toggleNavigationBillboard(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils#toggleNavigationBillboard)|
|void|[toggleWakeup(boolean disabled)](https://github.com/robotemi/sdk/wiki/Utils#toggleWakeup)|
|void|[turnBy(int degrees)](https://github.com/robotemi/sdk/wiki/Movement#turnBy)|
|void|[wakeup()](https://github.com/robotemi/sdk/wiki/Speech#wakeup)|
