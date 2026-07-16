# Navigation & Map & Floor

### Location

There are two main use cases with regards to locations. The first is location management, and the second is location navigation. The SDK provides methods that allows developers to handle both.

Location management allows you to control temi's locations by saving, deleting, fetching and monitoring their changes, while location navigation allows you to send temi to one of those locations and monitor its navigation status while it's in motion.

### Map

Map is a combination of:
- Processed lidar data from ROBOX, the algorithm core of temi
- Map elements, including locations, green paths, virtual walls, and navigation zones. 
- Map image, a visual abstraction of map raw data for UI presentation.

A map can be loaded, switched, backed up, and cached.

#### Map Id

There may be some confusion as map id is referred under its context.

When reference map on algorithm wise, e.g. [MapDataModel](#mapDataModel), it has a map id which is unique across algorithm map, the one from process lidar data. This id should be only used by ROBOX as an identifier, and served no other purpose.

When (SDK) users load a map, they are using the map id idenfying the combination. e.g. [MapModel](#mapmodel). This id is generated from temi cloud service when we back up a map to cloud. Thus it can be fetched from cloud, and reloaded to the robot.


#### Map cache

When user backups or imports a map from UI or SDK, the map combination will be cached locally for quicker access next time. So when you switching between different maps, the large map file will not be downloaded every time. Starting from 129 version, there are methods to load map from cloud to cache, also force load map from cache. 


#### Local map backup.

From 131 version, with SDK 1.131.3, you can backup current map to you app, and load map backup from your app to temi. The whole process can be made offline, without using temi cloud services.

Navigate to [Local Map Backup](https://github.com/robotemi/sdk/wiki/LocalMap) section for details.


### Navigation Zone

From 138 version, SDK supports Navigation Zone. A zone is a polygon area on the map with configurable name, navigation speed, bypass strategy, and obstacle avoidance distance. SDK can get the current zone and all zones, listen to zone entrance and exit events, and dynamically apply zone profiles during `goTo` / `goToPosition` navigation via `setCurrentGoToSpeed`, `setCurrentGoToBypassObstacles`, and `setCurrentGoToObstacleAvoidanceDistance`.


### Multi-Floor

From 129 version, we offered the developer access of multifloor function from SDK. This should benefit temi robots serving in complex or dynamic cartographic environments. Once enabled, all the floor APIs can be used, also a new map editor with multifloor functions will be available in temi settings.

Every floor contains a standalone map, which has all map attributes and can be created, recreated, replaced, backed up, plus the features in multifloor of like copying map from other floor. Such operations on one floor will not affect map on other floors.

All the floors and their maps are stored locally on the robot and can be switched offline until they are deleted from the user operations in the map editor.


<br>

## API Overview

|Return|Method|Description|
|-|-|-|
|boolean|[saveLocation(String location)](#saveLocation)|Save location|
|boolean|[deleteLocation(String location)](#deleteLocation)|Delete location|
|List\<String\>|[getLocations()](#getLocations)|Fetch all saved locations|
|void|[goTo()](#goTo)|Send temi to a specific location|
|void|[goToPosition()](#goToPosition)|Send temi to a specific position(coordinate)|
|[MapDataModel](#mapDataModel)|[getMapData()](#getmapdata)|Get the map data|
|List<[Layer](#layer)>|[getMapElements()](#getMapElements)|Get the map elements data|
|[MapImage](#mapImage)|[getMapImage()](#getMapImage)|Get the map image data|
|void|[repose(Position position)](#repose)|Repositioning temi|
|List<[MapModel](#mapModel)>|[getMapList()](#getMapList)|Get the list of backed up map|
|String|[loadMap(String mapId)](#loadMap)|Load map by map ID|
|String|[loadMap(String mapId, boolean reposeRequired, Position position, boolean offline, boolean withoutUI)](#loadmap-)|Load the map by map ID and the specific position of the target map|
|boolean|[setMultiFloorEnabled(boolean enabled)](#setMultiFloorEnabled)|Enable/Disable Multi-floor function|
|boolean|[isMultiFloorEnabled()](#isMultiFloorEnabled)|Check Multi-floor function|
|[Floor](#floor)|[getCurrentFloor()](#getCurrentFloor)|Get current floor data|
|List<[Floor](#floor)>|[getAllFloors()](#getAllFloors)|Get all floors' data|
|Pair<[Floor](#floor), [MapDataModel](#mapDataModel)>|[getFloorAndMapData(int floorId)](#getFloorAndMapData)|Get the map data of the target floor|
|void|[loadFloor(int floorId, Position position)](#loadFloor)|load target floor|
|Position|[getPosition()](#getPosition)|get current position|
|int|[resetMap()](#resetMap)|Reset map|
|int|[finishMapping()](#finishMapping)|Finish mapping, lock the map|
|int|[updateMapName()](#updateMapName)|Update current map name|
|int|[continueMapping()](#continueMapping)|Continue mapping, unlock the map|
|int|[upsertMapLayer()](#upsertMapLayer)|Add or edit map layer|
|int|[deleteMapLayer()](#deleteMapLayer)|Delete map layer|
|int|[newFloor()](#newFloor)|Create new floor|
|int|[deleteFloor()](#deleteFloor)|Delete floor by floorId|
|int|[renameFloor()](#renameFloor)|Rename floor|
|int|[renameLocation()](#renameLocation)|Rename location|
|int|[renameLocationOnFloor()](#renameLocationOnFloor)|Rename location on floor|
|int|[deleteLocationOnFloor()](#deleteLocationOnFloor)|Delete location on floor|
|boolean|[isMapLocked()](#isMapLocked)|Check if the map is locked|
|boolean|[isMapLost()](#isMapLost)|Check if the map is lost|
|[ReposeStatus](#reposeStatus)|[getReposeStatus()](#getReposeStatus)|Get repositioning status|
|List<[Layer](#layer)>|[getAllZones()](#getAllZones)|Get all navigation zones on the current map|
|List<[Layer](#layer)>|[getCurrentZones()](#getCurrentZones)|Get navigation zones that contain the robot's current position|
|int|[setCurrentGoToSpeed(float speed)](#setCurrentGoToSpeed)|Dynamically set speed for the current goTo session|
|int|[setCurrentGoToBypassObstacles(boolean bypassObstacles)](#setCurrentGoToBypassObstacles)|Dynamically set bypass strategy for the current goTo session|
|int|[setCurrentGoToObstacleAvoidanceDistance(int obstacleAvoidanceDistance)](#setCurrentGoToObstacleAvoidanceDistance)|Dynamically set obstacle avoidance distance for the current goTo session|

|Interface|Description|
|-|-|
|[OnLocationsUpdatedListener](#onLocationsUpdatedListener)|Locations updated listener|
|[OnGoToLocationStatusChangedListener](#onGoToLocationStatusChangedListener)|Go to location(position) status changed listener|
|[OnDistanceToLocationChangedListener](#onDistanceToLocationChangedListener)|Distance to locations changed listener|
|[OnCurrentPositionChangedListener](#onCurrentPositionChangedListener)|Current position changed listener|
|[OnReposeStatusChangedListener](#onReposeStatusChangedListener)|Listener for the status changed of repositioning|
|[OnLoadMapStatusChangedListener](#onLoadMapStatusChangedListener)|Listener for the status changed of loading map|
|[OnDistanceToDestinationChangedListener](#onDistanceToDestinationChangedListener)|Listener for the distance left to the destination|
|[OnLoadFloorStatusChangedListener](#onLoadFloorStatusChangedListener)|Listener for the status of switching of floor|
|[OnRobotDragStateChangedListener](#onRobotDragStateChangedListener)|Listener for robot being dragged|
|[OnGoToNavPathChangedListener](#onGoToNavPathChangedListener)|Listener for robot navigation path|
|[OnMapStatusChangedListener](#onMapStatusChangedListener)|Listener for map status changes|
|[OnMapNameChangedListener](#onMapNameChangedListener)|Listener for map name changes|
|[OnZoneEntranceStatusChangedListener](#onZoneEntranceStatusChangedListener)|Listener for navigation zone entrance and exit|

|Model|Description|
|-|-|
|[Position](#position)|Position(coordinate) in the map|
|[MapDataModel](#mapDataModel)|The map data|
|[MapImage](#mapImage)|The map image data|
|[Floor](#floor)|The floor data|
|[ReposeStatus](#reposeStatus)|Repose status|
|[ZoneProperty](#zoneProperty)|Navigation zone property|

<br>

## Methods

### saveLocation()

Use this method to save a new location for temi. Locate temi at the location you wish to save and give it a name, the location coordinates are extracted and passed automatically in the request.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |location|String|Location name you wish to save|

- **Return**

  |Type|Description|
  |-|-|
  |boolean|`true` if save successfully, `false` otherwise|

- **Prototype**

  ``` java
  boolean saveLocation(String location);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### deleteLocation()

Use this method to delete a saved location.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |location|String|Location name you wish to delete|

- **Return**

  |Type|Description|
  |-|-|
  |boolean|`true` if delete successfully, `false` otherwise|

- **Prototype**

  ``` java
  boolean deleteLocation(String location);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.44

---

### getLocations()

Use this method to fetch a list of saved locations.

- **Return**

  |Type|Description|
  |-|-|
  |List\<String\>|Locations list|

- **Prototype**

  ``` java
  List<String> getLocations();
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### goTo()

Use this method to send temi to one of your saved locations.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |location|String|Location name you wish the robot to navigate to. Use `home base` as location name when go back to charge.|
  |backwards|boolean|If `true` will walk backwards to the destination. `false` by default. Support from 0.10.80|
  |noByPass|boolean|If `true` will disallow bypass the obstacles during go to. Pass `null` to follow the  *Settings -> Navigation Settings*. Support from 0.10.80|
  |speedLevel|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils#speedLevel)|The speed level of this single go to session. Pass `null` to start with the speed level in *Settings -> Navigation Settings*. Support from 0.10.80|
  |highAccuracyArrival|boolean|If `true` , temi will try to make this go to session precisly on the pose of location, Support from 1.135.1, with 135 launcher|
  |noRotationAtEnd|boolean|If `true` , temi will no rorate to the saved angle of location upon arrival, Support from 1.135.1, with 135 launcher|

- **Prototype**

  ``` java
  void goTo(String location, boolean backwards, boolean noBypass, SpeedLevel speedLevel, boolean highAccuracyArrival, boolean noRotationAtEnd);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.36

---

### goToPosition()

Use this method to send temi to a specific position(coordinate).

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |position|[Position](#position)|Destination coordinate. Ignore its' attribute `tiltAngle` in this method. Set `yaw` in `position` to 999, will cancel the rotation upon arrival.|
  |backwards|boolean|If `true` will walk backwards to the destination. `false` by default. Support from 0.10.80|
  |noByPass|boolean|If `true` will disallow bypass the obstacles during go to. Pass `null` to follow the  *Settings -> Navigation Settings*. Support from 0.10.80|
  |speedLevel|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils#speedLevel)|The speed level of this single go to session. Pass `null` to start with the speed level in *Settings -> Navigation Settings*. Support from 0.10.80|
  |highAccuracyArrival|boolean|If `true` , temi will try to make this go to session precisly on the pose of location, Support from 1.135.1, with 135 launcher|

- **Prototype**

  ``` java
  void goToPosition(Position position, boolean backwards, boolean noBypass, SpeedLevel speedLevel, boolean highAccuracyArrival);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.70

---

### getMapData()

Use this method to get the map data.

~~**This method currently(0.10.70) only supports relatively small map, we will support large map in the next version**.~~ <br/>(**Fixed in version 0.10.71**)

Newly added virtual walls, navigation paths and locations data in **0.10.74**.

- **Return**

  |Type|Description|
  |-|-|
  |[MapDataModel](#mapDataModel)|The map data|

- **Prototype**

  ``` java
  MapDataModel getMapData();
  ```

- **Required permissions**

  Map

- **Support from**

  0.10.70

- **Note**

  This method is a time-consuming operation and is recommended to be used in a non-main thread. For more details please refer to the [Sampe code](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/MapActivity.kt).

---

### getMapElements()

Use this method to get the map elements data.

- **Return**

  |Type|Description|
  |-|-|
  |List<[Layer](#layer)>|Map elements data|

- **Prototype**

  ``` java
  List<Layer> getMapElements();
  ```

- **Required permissions**

  Map

- **Support from**

  1.136.0

---

### getMapImage()

Use this method to get the map image data.

- **Return**

  |Type|Description|
  |-|-|
  |[MapImage](#mapImage)|Map image data|

- **Prototype**

  ``` java
  MapImage getMapImage();
  ```

- **Required permissions**

  Map

- **Support from**

  1.136.0

---

### repose()

Use this method to start repositioning if temi has lost his position(Caused by being lifted, dragged, etc.).

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |position|[Position](#position)|Added in 134 version, optional, default is null. Assign a position to repose upon|

- **Prototype**

  ``` java
  void repose(Position position);
  ```

- **Required permissions**

  None.

- **Support from**

  0.10.72

---

### getMapList()

Use this method to get the list of the backed up maps.

- **Return**

  |Type|Description|
  |-|-|
  |List<[MapModel](#mapModel)>|List of maps|

- **Prototype**

  ``` java
  List<MapModel> getMapList();
  ```

- **Required permissions**

  Map

- **Support from**

  0.10.74

---

### loadMap()

Use this method to load the map by map ID.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mapId|String|ID of the map backup in [MapModel](#mapmodel)|

- **Return**

  |Type|Description|
  |-|-|
  |String|Request id. In the format of UUID, e.g. 538b44c9-fdcf-426a-9693-d72e9c0f9550. Used in onLoadMapStatusChanged callback to track load map result. Supported from 129 version, otherwise void is returned|

- **Prototype**

  ``` java
  String loadMap(String mapId);
  ```

- **Required permissions**

  Map

- **Support from**

  0.10.74

---

### loadMap() <a name="loadMapWithPosition">

Use this method to load the map by map ID and the specific position of the target map.

Only map Id is required, other parameters are optional with default values set.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mapId|String|ID of the map backup in [MapModel](#mapmodel)|
  |reposeRequired|boolean|Need to do repose after loading map or not, default as false|
  |position|[Position](#position)|The position of robot on the target map to loading the map. If not set, target map will be loaded from home base. Default as null|
  |offline|boolean|Skip fetching the latest map data of target mapId and use local cache instead, default as false. Supported from 129 version|
  |withoutUI|boolean|Load the map in the background without showing any blocking UI, default as false. Supported from 129 version|


- **Return**

  |Type|Description|
  |-|-|
  |String|Request id. In the format of UUID, e.g. 538b44c9-fdcf-426a-9693-d72e9c0f9550. Used in onLoadMapStatusChanged callback to track load map result. Supported from 129 version, otherwise void is returned|

- **Prototype**

  ``` java
  String loadMap(String mapId, boolean reposeRequired, Position position, boolean offline, boolean withoutUI);
  ```

- **Required permissions**

  Map

- **Support from**

  0.10.76

---

### setMultiFloorEnabled() <a name="setMultiFloorEnabled">

Enable/Disable Multi-floor function

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |enabled|boolean|true to enable，false to disable|

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if set, false if failed|

- **Prototype**

  ``` java
   boolean setMultiFloorEnabled(boolean enabled);
  ```

- **Required permissions**

  MAP, SETTINGS

- **Support from**

  1.129.0

---

### isMultiFloorEnabled() <a name="isMultiFloorEnabled">

Check Multi-floor function

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if enabled, false if not, null if Robot in not initiated|

- **Prototype**

  ``` java
   boolean isMultiFloorEnabled();
  ```

- **Required permissions**

  None.

- **Support from**

  1.129.0

---

### getCurrentFloor() <a name="getCurrentFloor">

Get current floor data

- **Return**

  |Type|Description|
  |-|-|
  |[Floor](#floor)|Current floor data, null if service is not initiated, or lack of permission, or no floor data|

- **Prototype**

  ``` java
    Floor getCurrentFloor();
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.129.0

---

### getAllFloors() <a name="getAllFloors">

Get all floors' data

- **Return**

  |Type|Description|
  |-|-|
  |List<[Floor](#floor)>|All floors' data, empty if service is not initiated, or lack of permission, or no floor data|

- **Prototype**

  ``` java
    List<Floor> getAllFloors();
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.129.0

---

### getFloorAndMapData() <a name="getFloorAndMapData">

Get the map data of the target floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|

- **Return**

  |Type|Description|
  |-|-|
  |Pair<[Floor](#floor), [MapDataModel](#mapDataModel)>|The map data of the target floor|

- **Prototype**

  ``` java
    Pair<Floor, MapDataModel> getFloorAndMapData(int floorId);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.137.1

---
### loadFloor() <a name="loadFloor">

load target floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|
  |position|[Position](#position)|Position in map on the target floor to be loaded from|

- **Prototype**

  ``` java
    void loadFloor(int floorId, Position position);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.129.0

---

### getPosition() <a name="getPosition">

get current position

- **Return**

  |Type|Description|
  |-|-|
  |[Position](#position)|Get current position, will return Position(0, 0, 0, 0) if failed.|

- **Prototype**

  ``` java
    Position getPosition();
  ```

- **Required permissions**

  NONE
  
- **Support from**

  1.133.0

---

### resetMap() <a name="resetMap">

Reset current map or reset all floors

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |allFloor|boolean|true to reset all floors|
  |saveHomeBaseIfCharging|boolean|save home base if charging, added in 1.137.1 version, default is false|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，400 invalid operation，403 permission required，408 timeout|

- **Prototype**

  ``` java
    int resetMap(boolean allFloor, boolean saveHomeBaseIfCharging);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### finishMapping() <a name="finishMapping">

Finish mapping, lock the map

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mapName|String|an optional map name, default as null|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，304 map already locked, 400 invalid operation，403 permission required，408 timeout|

- **Prototype**

  ``` java
    int finishMapping(String mapName);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### updateMapName() <a name="updateMapName">

Update current map name

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mapName|String|map name|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，400 invalid operation，403 permission required，429 too many requests, wait 2s|

- **Prototype**

  ``` java
    int updateMapName(String mapName);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### continueMapping() <a name="continueMapping">

Continue mapping, unlock the map

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，304 map already unlocked，400 invalid operation，403 permission required，408 timeout，429 too many requests, wait 5s|

- **Prototype**

  ``` java
    int continueMapping();
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### upsertMapLayer() <a name="upsertMapLayer">

Add or edit map layer, if layerId exists then update, otherwise create new layer.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |layer|[Layer](#layer)|map layer|
  |floorId|int|floor id, added in 1.137.1 version, default is null|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，400 invalid operation，403 permission required，413 contains layer pose out of map |

- **Prototype**

  ``` java
    int upsertMapLayer(Layer layer, int floorId);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### deleteMapLayer() <a name="deleteMapLayer">

Delete map layer, only support Green path and Virtual wall.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |layerId|String|map layer id|
  |layerCategory|int|map layer category|
  |floorId|int|floor id, added in 1.137.1 version, default is null|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，400 invalid operation，403 permission required，404 target layer doesn't exist |

- **Prototype**

  ``` java
    int deleteMapLayer(String layerId, int LayerCategory, int floorId);
  ```

- **Required permissions**

  MAP
  
- **Support from**

  1.134.0

---

### newFloor() <a name="newFloor">

Create new floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorName|String|floor name|
  |saveHomeBaseIfCharging|boolean|save home base if charging, default is false|

- **Return**

  |Type|Description|
  |-|-|
  |int| id（id!=0）floor id if succeed, -400 Package name exception, -403 Package permission exception, -405 Current floor/map not locked cannot be called, -408 Failed|

- **Prototype**

  ``` java
    int newFloor(String floorName, boolean saveHomeBaseIfCharging);
  ```

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### deleteFloor() <a name="deleteFloor">

Delete floor by floorId

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|

- **Return**

  |Type|Description|
  |-|-|
  |int|200 succeed，-400 Package name exception，-403 Package permission exception，-409 The current floor cannot be modified，-408 Failed|

- **Prototype**

  ``` java
    int deleteFloor(int floorId);
  ```

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### renameFloor() <a name="renameFloor">

Rename floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|
  |floorName|String|floor name|

- **Return**

  |Type|Description|
  |-|-|
  |int|200 succeed，-400 Package name exception，-403 Package permission exception，-408 Failed|

- **Prototype**

  ``` java
    int renameFloor(int floorId, String floorName);
  ```

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### renameLocation() <a name="renameLocation">

Rename location

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |oldLocationName|String|old location name|
  |newLocationName|String|new location name|
  |layer|Layer, optional|layer's layerCategory must be location type (Location type)|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 not supported，200 succeed，400 invalid operation，403 permission required，404 target layer doesn't exist，409 Cannot rename Home Base and cannot rename location to Home Base|

- **Prototype**

  ``` java
    int renameLocation(String oldLocationName, String newLocationName, Layer layer);
  ```

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### renameLocationOnFloor() <a name="renameLocationOnFloor">

Rename location on floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|
  |oldLocationName|String|old location name|
  |newLocationName|String|new location name|
  |layer|Layer, optional|layer's layerCategory must be location type (Location type)|

- **Return**

  |Type|Description|
  |-|-|
  |int|0 if the operation is not supported by current launcher,<br>200 for success,<br>400 invalid parameter,<br>403 for [Permission.MAP] permission required,<br>404 target map layer doesn't exist,<br>409 home base cannot rename And cannot rename to home base|

- **Prototype**

  ``` java
    int renameLocationOnFloor(int floorId, String oldLocationName, String newLocationName, Layer layer);
  ```

- **Required permissions**

  MAP

  |Type|Description|
  |-|-|
  |int|0 if the operation is not supported by current launcher, 400 package names are abnormal, -403 Map permission in package is abnormal, 404 target map layer doesn't exist, 409 The current floor cannot be modified, 413 Location data is out of bounds, 200 Success, 408 Failure|

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### deleteLocationOnFloor() <a name="deleteLocationOnFloor">

Delete location on floor

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |floorId|int|floor id|
  |locationName|String|location name|

  |Type|Description|
  |-|-|
  |int|200 Success, -400 package names are abnormal, -403 Map permission in package is abnormal, -409 The current floor cannot be modified, -408 Failure|

- **Prototype**

  ``` java
    int deleteLocationOnFloor(int floorId, String locationName);
  ```

- **Required permissions**

  MAP

- **Support from**

  1.137.1

---

### isMapLocked() <a name="isMapLocked">

Check if the map is locked

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if locked, false if not, null if launcher does not support this feature|

- **Prototype**

  ``` java
    boolean isMapLocked();
  ```

- **Required permissions**

  NONE

- **Support from**

  1.136.0

---

### isMapLost() <a name="isMapLost">

Check if the map is lost

- **Return**

  |Type|Description|
  |-|-|
  |boolean|true if lost, false if not, null if launcher does not support this feature|

- **Prototype**

  ``` java
    boolean isMapLost();
  ```

- **Required permissions**

  NONE

- **Support from**

  1.136.0

---

### getReposeStatus() <a name="getReposeStatus">

Get repositioning status

- **Return**

  |Type|Description|
  |-|-|
  |[ReposeStatus](#reposeStatus)|Repose status|

- **Prototype**

  ``` java
    ReposeStatus getReposeStatus();
  ```

- **Required permissions**

  NONE

- **Support from**

  1.136.0

---

### getAllZones() <a name="getAllZones">

Get all navigation zones defined on the current map.

- **Return**

  |Type|Description|
  |-|-|
  |List<[Layer](#layer)>|All layers of `ZONE` category on the current map|

- **Prototype**

  ``` java
  List<Layer> getAllZones();
  ```

- **Required permissions**

  [MAP](https://github.com/robotemi/sdk/wiki/Permission)

- **Support from**

  1.138.0

---

### getCurrentZones() <a name="getCurrentZones">

Get navigation zones that contain the robot's current position. The robot can be in multiple overlapping zones simultaneously.

- **Return**

  |Type|Description|
  |-|-|
  |List<[Layer](#layer)>|List of zone layers currently containing the robot|

- **Prototype**

  ``` java
  List<Layer> getCurrentZones();
  ```

- **Required permissions**

  [MAP](https://github.com/robotemi/sdk/wiki/Permission)

- **Support from**

  1.138.0

---

### setCurrentGoToSpeed() <a name="setCurrentGoToSpeed">

Dynamically set the speed for the current `goTo` / `goToPosition` navigation session triggered by this application. Can be used to apply zone profile speed during navigation.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |speed|float|Speed value, range 0.3 - 1.5 m/s|

- **Return**

  |Type|Description|
  |-|-|
  |int|`0` not supported by current launcher; `200` success; `400` package verification failed or invalid parameter; `408` failure|

- **Prototype**

  ``` java
  int setCurrentGoToSpeed(float speed);
  ```

- **Required permissions**

  None.

- **Support from**

  1.138.0

---

### setCurrentGoToBypassObstacles() <a name="setCurrentGoToBypassObstacles">

Dynamically set the bypass obstacles strategy for the current `goTo` / `goToPosition` navigation session triggered by this application.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |bypassObstacles|boolean|`true` to enable bypassing; `false` to stop when encountering obstacles|

- **Return**

  |Type|Description|
  |-|-|
  |int|`0` not supported by current launcher; `200` success; `400` package verification failed; `408` failure|

- **Prototype**

  ``` java
  int setCurrentGoToBypassObstacles(boolean bypassObstacles);
  ```

- **Required permissions**

  None.

- **Support from**

  1.138.0

---

### setCurrentGoToObstacleAvoidanceDistance() <a name="setCurrentGoToObstacleAvoidanceDistance">

Dynamically set the obstacle avoidance distance for the current `goTo` / `goToPosition` navigation session triggered by this application.

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |obstacleAvoidanceDistance|int|Distance in centimeters, range 0 - 100|

- **Return**

  |Type|Description|
  |-|-|
  |int|`0` not supported by current launcher; `200` success; `400` package verification failed or invalid parameter; `408` failure|

- **Prototype**

  ``` java
  int setCurrentGoToObstacleAvoidanceDistance(int obstacleAvoidanceDistance);
  ```

- **Required permissions**

  None.

- **Support from**

  1.138.0

<br>

## Interfaces

### OnLocationsUpdatedListener

Set your context to implement this listener and add the override method to get the list of saved locations every time it changes.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnLocationsUpdatedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |locations|List\<String\>|All saved locations list|

- **Prototype**

  ``` java
  abstract void onLocationsUpdated(List<String> locations)
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLocationsUpdatedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnLocationsUpdatedListener(OnLocationsUpdatedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLocationsUpdatedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnLocationsUpdatedListener(OnLocationsUpdatedListener listener);
  ```

- **Support from**

  0.10.36

---

### OnGoToLocationStatusChangedListener

Set your context to implement this listener and add the override method to get navigation information regarding temi's go to location action.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnGoToLocationStatusChangedListener {}
```

#### Static constants

|Constant|Type|Value|Description|
|-|-|-|-|
|START|String|"start"|Navigation to the location has started|
|CALCULATING|String|"calculating"|Calculating the route to the location|
|GOING|String|"going"|Calculated the route and is on its' way|
|COMPLETE|String|"complete"|Arrived at the desired location |
|ABORT|String|"abort"|Navigation aborted|
|REPOSING|String|"reposing"|Reposing during the navigation|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |location|String|The name of the location temi is navigating to|
  |status|String|Navigation status|
  |descriptionId|int|Numerical code that reflects the description of the status|
  |description|String|Verbose more informative description of the navigation status (Such as obstacle info)|


- DescriptionId

  |DescriptionId|Description|
  |-|-|
  |500|"Complete"|
  |0|"Abort General"|
  |1003|"Abort no movement"|
  |1004|"Abort timeout"|
  |1005|"Abort by user"|
  |1006|"Abort out of map bounds"|
  |1060|"Path Plan"|
  |2000|"Obstacle"|
  |2001|"Ground Obstacle"|
  |2002|"Hight Obstacle"|
  |2003|"Lidar Obstacle"|
  |2004|"Front Obstacle"|
  |2005|"Back Obstacle"|
  |2006|"Abyss Obstacle"|
  |2007|"Virtual Wall Obstacle"|
  |2008|"Cliff Detected"|
  |2009|"Stuck Wheel"|
  |1020- 1050|"Calculating"|
  |5000- 5021|"Calculating"|
  |10007|"Going"|
  |10008|"Path Planing" // Added in 134 version|
  |10009|"Docking" // Added in 134 version|
  |-|"Unknown"|

- **Prototype**

  ``` java
  void onGoToLocationStatusChanged(String location, String status, int descriptionId, String description);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnGoToLocationStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnGoToLocationStatusChangedListener(OnGoToLocationStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnGoToLocationStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnGoToLocationStatusChangedListener(OnGoToLocationStatusChangedListener listener);
  ```

- **Support from**

  0.10.36

---

### OnDistanceToLocationChangedListener

Set your context to implement this listener and add the override method to get the distances to saved locations.

#### Prototype

``` java
package com.robotemi.sdk.navigation.listener;

interface OnDistanceToLocationChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |distances|Map\<String, Float\>|A key-value pair collection with `String` type key(location name) and `Float` type value(distance)|

- **Prototype**

  ``` java
  void onDistanceToLocationChanged(Map<String, Float> distances);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDistanceToLocationChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnDistanceToLocationChangedListener(OnDistanceToLocationChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDistanceToLocationChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnDistanceToLocationChangedListener(OnDistanceToLocationChangedListener listener);
  ```

- **Support from**

  0.10.70

---

### OnCurrentPositionChangedListener

Set your context to implement this listener and add the override method to get current position information.

#### Prototype

``` java
package com.robotemi.sdk.navigation.listener;

interface OnCurrentPositionChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |position|[Position](#position)|Current position|

- **Prototype**

  ``` java
  void onCurrentPositionChanged(Position position);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnCurrentPositionChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnCurrentPositionChangedListener(OnCurrentPositionChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnCurrentPositionChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnCurrentPositionChangedListener(OnCurrentPositionChangedListener listener);
  ```

- **Support from**

  0.10.70

---

### OnReposeStatusChangedListener

Set your context to implement this listener and add the override method to listen to the status changes of repositioning.

#### Prototype

``` java
package com.robotemi.sdk.navigation.listener;

interface OnReposeStatusChangedListener {}
```

#### Static constant

All constants here are only for the status of repositioning.

|Constant|Type|Value|Description|
|-|-|-|-|
|IDLE|int|0|Idle|
|REPOSE_REQUIRED|int|1|Ready to reposition|
|REPOSING_START|int|2|Reposition started|
|REPOSING_GOING|int|3|Repositioning|
|REPOSING_COMPLETE|int|4|Reposition completed|
|REPOSING_OBSTACLE_DETECTED|int|5|Obstacle detected during repositioning|
|REPOSING_ABORT|int|6|Reposition abort|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |status|int|Status of reposition|
  |description|String|Description of status|

- **Prototype**

  ``` java
  void onReposeStatusChanged(int status, String description);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnReposeStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnReposeStatusChangedListener(OnReposeStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnReposeStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnReposeStatusChangedListener(OnReposeStatusChangedListener listener);
  ```

- **Support from**

  0.10.72

---

### OnLoadMapStatusChangedListener

Set your context to implement this listener and add the override method to listen to the status changes of loading map.

#### Prototype

``` java
package com.robotemi.sdk.map;

interface OnLoadMapStatusChangedListener {}
```

#### Static constant

All constants here are only for the status of loading map.

|Constant|Type|Value|Description|
|-|-|-|-|
|COMPLETE|int|0|Complete|
|START|int|1|Start|
|ERROR_UNKNOWN|int|1000|Unknown error|
|ERROR_ABORT_FROM_ROBOX|int|2000|Abort by Robox|
|ERROR_ABORT_ON_NOT_CHARGING|int|2001|Loading without charging|
|ERROR_ABORT_BUSY|int|2002|temi is on another blocking task|
|ERROR_ABORT_ON_TIMEOUT|int|3000|Timeout|
|ERROR_PB_STREAM_FILE_INVALID|int|4000|Invalid PB file|
|ERROR_GET_MAP_DATA|int|5000|Get error while getting map data from remote|

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |status|int|Status of loading map|
  |requestId|String|Request id obtained from [loadMap()](#loadMapWithPosition), Supported from 129.|

- **Prototype**

  ``` java
  void onLoadMapStatusChanged(int status, String requestId);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLoadMapStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnLoadMapStatusChangedListener(OnLoadMapStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLoadMapStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnLoadMapStatusChangedListener(OnLoadMapStatusChangedListener listener);
  ```

- **Support from**

  0.10.74

### OnDistanceToDestinationChangedListener

Set your context to implement this listener and add the override method to get the distance to the destination.

#### Prototype

``` java
package com.robotemi.sdk.navigation.listener;

interface OnDistanceToDestinationChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |location|String|Location name of the destination|
  |distance|float|Distance to the destination|

- **Prototype**

  ``` java
  void onDistanceToDestinationChanged(location: String, distance: Float);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDistanceToDestinationChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnDistanceToDestinationChangedListener(OnDistanceToDestinationChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnDistanceToDestinationChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnDistanceToDestinationChangedListener(OnDistanceToDestinationChangedListener listener);
  ```

- **Support from**

  0.10.80

### OnLoadFloorStatusChangedListener

Set your context to implement this listener and add the override method to get the status for loading.

#### Prototype

``` java
package com.robotemi.sdk.map;

interface OnLoadFloorStatusChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |status|int|0 complete<br>1 start<br>-1 error|

- **Prototype**

  ``` java
  void onLoadFloorStatusChanged(int status);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLoadFloorStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnLoadFloorStatusChangedListener(OnLoadFloorStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnLoadFloorStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnLoadFloorStatusChangedListener(OnLoadFloorStatusChangedListener listener);
  ```

- **Support from**

  1.129.0

### OnRobotDragStateChangedListener

A callback to notify robot is being dragged when itself is not in a movement state.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnRobotDragStateChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |isDragged|Boolean|true being dragged|

- **Prototype**

  ``` java
  void onRobotDragStateChanged(Boolean isDragged);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRobotDragStateChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnRobotDragStateChangedListener(OnRobotDragStateChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnRobotDragStateChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnRobotDragStateChangedListener(OnRobotDragStateChangedListener listener);
  ```

- **Support from**

  1.130.1

### OnGoToNavPathChangedListener

A callback to notify robot‘s path plan on navigation.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnGoToNavPathChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |path|List<LayerPose>|The path from current postion to destination|

- **Prototype**

  ``` java
  void onGoToNavPathChanged(List<LayerPose> path);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnGoToNavPathChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnGoToNavPathChangedListener(OnGoToNavPathChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnGoToNavPathChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnGoToNavPathChangedListener(OnGoToNavPathChangedListener listener);
  ```

- **Support from**

  1.134.0

---

### OnMapStatusChangedListener


A callback to notify map status changes.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnMapStatusChangedListener {}
```


#### Abstract methods


- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |isLost|boolean|Whether the map is lost|
  |isLocked|boolean|Whether the map is locked|  

- **Prototype** 

  ``` java
  void onMapStatusChanged(boolean isLost, boolean isLocked);
  ```

- **Support from**

  1.136.0

---

### OnMapNameChangedListener

A callback to notify map name changes.

#### Prototype


``` java
package com.robotemi.sdk.listeners;
interface OnMapNameChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |mapName|String|Map name|


- **Prototype**

  ``` java
  void onMapNameChanged(String mapName);
  ```

- **Support from**

  1.136.0

---

### OnZoneEntranceStatusChangedListener

A callback to notify when the navigation zone where the robot is located changes.

#### Prototype

``` java
package com.robotemi.sdk.listeners;

interface OnZoneEntranceStatusChangedListener {}
```

#### Abstract methods

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |layers|List<[Layer](#layer)>|List of zone layers currently containing the robot|

- **Prototype**

  ``` java
  void onZoneEntranceStatusChanged(List<Layer> layers);
  ```

#### Method for adding listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnZoneEntranceStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void addOnZoneEntranceStatusChangedListener(OnZoneEntranceStatusChangedListener listener);
  ```

#### Method for removing listener

- **Parameters**

  |Parameter|Type|Description|
  |-|-|-|
  |listener|OnZoneEntranceStatusChangedListener|An instance of a class that implements this interface|

- **Prototype**

  ``` java
  void removeOnZoneEntranceStatusChangedListener(OnZoneEntranceStatusChangedListener listener);
  ```

- **Support from**

  1.138.0

---

<br>

## Models

### Position

Used to hold the position information.

#### Prototype

``` java
package com.robotemi.sdk.navigation.model;

class Position {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|x|float|Position coordinate x|
|y|float|Position coordinate y|
|yaw|float|Will be round to [-π, π], 0 degree is the direction of robot when map is reset on home base|
|tiltAngle|int|Head tilt angle|
|isInMapArea|boolean|Whether the position is in the map area, added in 1.136.0 version|

---

### MapDataModel

Used to hold the map data.

#### Prototype

``` java
package com.robotemi.sdk.map;

class MapDataModel {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|mapImage|[MapImage](#mapImage)|The map image data|
|mapId|String|Map ID, this is the identifier of map data, which is different from id in [MapModel](#mapModel), which is used to identify map backups|
|mapInfo|[MapInfo](#mapInfo)|Map information|
|virtualWalls|List<[Layer](#layer)>|Layer collection of virtual walls|
|greenPaths|List<[Layer](#layer)>|Layer collection of navigation paths|
|locations|List<[Layer](#layer)>|Layer collection of locations|
|zones|List<[Layer](#layer)>|Layer collection of navigation zones (added in 1.138.0)|
|mapName|String|Current map name (added in 130 version)|

### MapImage

Used to hold the map image data.

#### Prototype

``` java
package com.robotemi.sdk.map;

class MapImage {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|typeId|String|-|
|rows|int|The size of the rows of the map data matrix|
|cols|int|The size of the columns of the map data matrix|
|dt|String|-|
|data|List\<Integer\>|The one-dimensional array converted from map data matrix|

### MapInfo

Used to hold the map information.

#### Prototype

``` java
package com.robotemi.sdk.map;

class MapInfo {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|height|int|height|
|width|int|width|
|originX|float|Origin coordinate x|
|originY|float|Origin coordinate y|
|resolution|float|Resolution|

### Layer

Used to hold the map layer data.

#### Prototype

``` java
package com.robotemi.sdk.map;

class Layer {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|layerCreationUTC|int|Timestamp of creating the layer|
|layerCategory|int|[Layer category](#layerCategory)|
|layerId|String|Layer ID|
|layerThickness|float|Layer thickness|
|layerStatus|int|[Layer status](#layerStatus)|
|layerPoses|List<[LayerPose](#layerPose)>|Coordinate collection of the layer|
|layerDirection|int|Direction of virtual wall layer, value can be -1, 0, 1. Added in 1.132.1 version.|
|layerData|String|String data of layer, added in 1.133.0 version to support MapEraser layer. For `ZONE` layers, can be parsed as [ZoneProperty](#zoneProperty)|
|zoneProperty|[ZoneProperty](#zoneProperty)|Navigation zone property. Available when `layerCategory` is `ZONE` and `layerData` is valid (added in 1.138.0)|

### ZoneProperty

Used to hold navigation zone configuration.

#### Prototype

``` java
package com.robotemi.sdk.map;

class ZoneProperty {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|name|String|Zone name|
|speed|String|Navigation speed level, e.g. `Medium`|
|bypassObstacles|boolean|Whether bypassing obstacles is enabled|
|obstacleAvoidanceDistance|int|Obstacle avoidance distance in centimeters|

### LayerPose

Used to hold the coordinate data that make up the map layer.

#### Prototype

``` java
package com.robotemi.sdk.map;

class LayerPose {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|x|float|Coordinate x|
|y|float|Coordinate y|
|theta|float|-|

### MapDataModelKt

#### Prototype

``` java
package com.robotemi.sdk.map;

class MapDataModelKt {}
```

#### Static constant

##### Layer category <a name="layerCategory">

|Constant|Type|Value|Description|
|-|-|-|-|
|GREEN_PATH|int|0|Navigation path|
|VIRTUAL_WALL|int|3|Virtual wall|
|LOCATION|int|4|Location|
|MAP_ERASER|int|6|Map eraser|
|ZONE|int|7|Navigation zone (added in 1.138.0)|

##### Layer status <a name="layerStatus">

|Constant|Type|Value|Description|
|-|-|-|-|
|STATUS_CURRENT|int|0||
|STATUS_UPDATE|int|1||
|STATUS_ADD_POSE|int|2||
|STATUS_DELETE|int|3||

### MapModel

Used to hold the map information of the backed up map list.

#### Prototype

``` java
package com.robotemi.sdk.map;

class MapModel {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|id|String|ID of the map backup|
|name|String|Name of the map|

### Floor

Used to hold the information of the floor.

#### Prototype

``` java
package com.robotemi.sdk.map;

class Floor {}
```

#### Attributes

|Attribute|Type|Description|
|-|-|-|
|id|String|ID of the floor|
|name|String|Name of the floor|
|mapId|String|mapId of the floor, this is the one from [MapDataModel](#mapdatamodel)|
|locations|List<Location>|locations on the floor|


### ReposeStatus

Repose status.

#### Prototype

``` kotlin
package com.robotemi.sdk.constants;

enum class ReposeStatus(val value: Int) {
  UNKNOWN(0),
  IDLE(1),
  REPOSING(2),
  REPOSE_REQUIRED(3)
}
```