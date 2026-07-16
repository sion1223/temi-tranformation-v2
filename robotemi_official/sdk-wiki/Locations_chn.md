# 导航与地图


### 地点

关于位置地点，主要有两个部分。第一个是地点位置管理，第二个是地点位置导航。 SDK提供了允许开发者同时处理两个部分的一些方法接口。地点位置管理部分可以让你通过保存、删除、获取以及监听地点列表变化来管理控制地点；而地点位置导航则可以让temi前往到其中的某一个地点并监听导航过程中的状态信息变化。

### 地图

地图是以下数据集合：
- ROBOX 处理后的地图雷达数据
- 地图元素，包括地点，导航路径，虚拟墙，导航区域
- 地图图片，通过地图雷达数据提取出来的用于展示的图片数据

地图可以被加载，切换，备份，以及缓存。

#### 地图 Id

地图的 id 在不同场景下的意义不同。

当指代算法端的地图时，如 [MapDataModel](#mapDataModel)，则 map id 是指算法端根据地图数据算出的唯一值。这一 id 只在算法端有意义，被用于区别不同的扫描出来的地图，没有其它用途。

当指代用户地图时，如用户想要加载地图，则需要使用 [MapModel](#mapmodel) 中的 id。这一种 id 是由 temi 的云端服务创建，在用户备份地图到云端时生成。因此可被用于获取已保存的地图列表，并且可用于加载指定的地图。

#### 地图缓存

当用户在 UI 上或通过 SDK 导入、导出（备份）地图时，整个地图数据的集合都会被缓存在本地，供下次快速加载使用。加载存在备份的地图时，可以省略掉从云端下载地图数据的过程。从 129 版本起，还加入了
`loadMapToCache` 及强制使用缓存数据，离线加载地图的接口。

#### Local map backup.

从 131 版本开始，配合 SDK 1.131.3,。应用可以将当前地图备份至本地，也可以将本地的地图加载至 temi。整个过程可以在离线状态下进行，不需要访问 temi 的云端服务。

请访问 [本地地图备份](https://github.com/robotemi/sdk/wiki/LocalMap_chn) 页面查看更多介绍.

### 导航区域 Navigation Zone

从 138 版本起，SDK 支持导航区域（Zone）功能。区域是在地图上绘制的多边形范围，可配置名称、导航速度、绕障策略和避障距离等属性。SDK 可获取当前所在区域及全部区域，监听区域进出事件，并在 `goTo` / `goToPosition` 导航过程中通过 `setCurrentGoToSpeed`、`setCurrentGoToBypassObstacles`、`setCurrentGoToObstacleAvoidanceDistance` 动态应用区域配置。

### 多楼层

从 129 版本起，我们在 SDK 中为开发者提供了开启多楼层（Multifloor）功能的方法。多楼层功能将有助于处于专属场景及复杂地图切换任务下的 temi 工作与使用。当通过 SDK 启动多楼层功能之后即可使用相关接口，同时一个功能更强的多楼层地图编辑器也会在设置中出现，替代原有的单楼层地图编辑器。

每一个楼层 (Floor) 都包含一张独立的地图，拥有一张地图的所有属性，并可以对楼层地图单独操作。如：建地图、重建地图、替换为其它地图，导入导出，以及多楼层专属的复制地图等功能。对一个楼层地图的操作不会影响其它楼层的地图。

所有的楼层及其地图数据集合都是存储于机器人本地，所以可以离线切换。只有删除楼层之后，才会清除掉本地存储的地图数据。


<br>

## API 概览

|返回值|方法|说明|
|-|-|-|
|boolean|[saveLocation(String location)](#saveLocation)|保存地点|
|boolean|[deleteLocation(String location)](#deleteLocation)|删除地点|
|List\<String\>|[getLocations()](#getLocations)|获取所有已保存的地点|
|void|[goTo()](#goTo)|让 temi 前往某个指定地点|
|void|[goToPosition()](#goToPosition)|让 temi 前往某个位置（坐标）|
|[MapDataModel](#mapDataModel)|[getMapData()](#getmapdata)|获取地图数据|
|List<[Layer](#layer)>|[getMapElements()](#getMapElements)|获取地图元素数据|
|[MapImage](#mapImage)|[getMapImage()](#getMapImage)|获取地图图片数据|
|void|[repose(Position position)](#repose)|重定位|
|List<[MapModel](#mapModel)>|[getMapList()](#getMapList)|获取已备份的地图列表|
|String|[loadMap(String mapId)](#loadMap)|通过地图 ID 加载地图|
|String|[loadMap(String mapId, boolean reposeRequired, Position position, boolean offline, boolean withoutUI)](#loadmap-)|在指定的位置坐标上通过地图 ID 来加载地图|
|boolean|[setMultiFloorEnabled(boolean enabled)](#setMultiFloorEnabled)|开启/关闭多楼层功能|
|boolean|[isMultiFloorEnabled()](#isMultiFloorEnabled)|检查多楼层功能开关状态|
|[Floor](#floor)|[getCurrentFloor()](#getCurrentFloor)|获取当前楼层数据|
|List<[Floor](#floor)>|[getAllFloors()](#getAllFloors)|获取所有楼层数据|
|Pair<[Floor](#floor), [MapDataModel](#mapDataModel)>|[getFloorAndMapData(int floorId)](#getFloorAndMapData)|获取指定楼层的地图数据|
|void|[loadFloor(int floorId, Position position)](#loadFloor)|加载指定楼层|
|Position|[getPosition()](#getPosition)|获取当前位置|
|int|[resetMap()](#resetMap)|重置地图|
|int|[finishMapping()](#finishMapping)|完成地图绘制|
|int|[updateMapName()](#updateMapName)|更新地图名称|
|int|[continueMapping()](#continueMapping)|继续绘制地图|
|int|[upsertMapLayer()](#upsertMapLayer)|更新地图元素|
|int|[deleteMapLayer()](#deleteMapLayer)|删除地图元素|
|int|[newFloor()](#newFloor)|创建新楼层|
|int|[deleteFloor()](#deleteFloor)|删除指定楼层|
|int|[renameFloor()](#renameFloor)|重命名楼层|
|int|[renameLocation()](#renameLocation)|重命名地点|
|int|[renameLocationOnFloor()](#renameLocationOnFloor)|重命名指定楼层上的地点|
|int|[deleteLocationOnFloor()](#deleteLocationOnFloor)|删除指定楼层上的地点|
|boolean|[isMapLocked()](#isMapLocked)|检查地图是否处于锁定状态|
|boolean|[isMapLost()](#isMapLost)|检查地图是否处于丢失状态|
|[ReposeStatus](#reposeStatus)|[getReposeStatus()](#getReposeStatus)|获取重定位状态|
|List<[Layer](#layer)>|[getAllZones()](#getAllZones)|获取当前地图上的全部导航区域|
|List<[Layer](#layer)>|[getCurrentZones()](#getCurrentZones)|获取机器人当前所在导航区域|
|int|[setCurrentGoToSpeed(float speed)](#setCurrentGoToSpeed)|为当前 goTo 导航会话动态设置速度|
|int|[setCurrentGoToBypassObstacles(boolean bypassObstacles)](#setCurrentGoToBypassObstacles)|为当前 goTo 导航会话动态设置绕障策略|
|int|[setCurrentGoToObstacleAvoidanceDistance(int obstacleAvoidanceDistance)](#setCurrentGoToObstacleAvoidanceDistance)|为当前 goTo 导航会话动态设置避障距离|

|接口|说明|
|-|-|
|[OnLocationsUpdatedListener](#onLocationsUpdatedListener)|地点变化监听器|
|[OnGoToLocationStatusChangedListener](#onGoToLocationStatusChangedListener)|go to 地点（位置坐标）时的状态变化监听器|
|[OnDistanceToLocationChangedListener](#onDistanceToLocationChangedListener)|当前位置坐标到其他已保存地点的距离变化监听器|
|[OnCurrentPositionChangedListener](#onCurrentPositionChangedListener)|当前位置坐标变化监听器|
|[OnReposeStatusChangedListener](#onReposeStatusChangedListener)|重定位状态变化监听器|
|[OnLoadMapStatusChangedListener](#onLoadMapStatusChangedListener)|加载地图过程中状态变化监听器|
|[OnDistanceToDestinationChangedListener](#onDistanceToDestinationChangedListener)|到目的地剩余导航距离变化监听器|
|[OnLoadFloorStatusChangedListener](#onLoadFloorStatusChangedListener)|楼层切换状态监听器|
|[OnRobotDragStateChangedListener](#onRobotDragStateChangedListener)|机器人非移动状态下被拖拽的监听器|
|[OnGoToNavPathChangedListener](#onGoToNavPathChangedListener)|机器人导航规划路径监听器|
|[OnMapStatusChangedListener](#onMapStatusChangedListener)|地图状态变化监听器|
|[OnMapNameChangedListener](#onMapNameChangedListener)|地图名称变化监听器|
|[OnZoneEntranceStatusChangedListener](#onZoneEntranceStatusChangedListener)|导航区域进出变化监听器|

|模型|说明|
|-|-|
|[Position](#position)|位置坐标|
|[MapDataModel](#mapDataModel)|地图数据|
|[MapImage](#mapImage)|地图图片数据|
|[Floor](#floor)|楼层数据|
|[ReposeStatus](#reposeStatus)|重定位状态|
|[ZoneProperty](#zoneProperty)|导航区域属性|

<br>

## 方法

### saveLocation()

用这个方法来给temi保存一个新地点。将temi带到你想要保存的地点，并给这个地点起个名字，系统将会自动提取当前的位置坐标并传入到保存地点的请求中。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |location|String|你要保存的地点名称|

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|`true`（`false`）表示保存地点成功（失败）|

- **原型**

  ``` java
  boolean saveLocation(String location);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### deleteLocation()

用这个方法来删除一个已保存的地点。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |location|String|你要删除的地点名称|

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|`true`（`false`）表示删除地点成功（失败）|

- **原型**

  ``` java
  boolean deleteLocation(String location);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.44

---

### getLocations()

用这个方法来获取已保存的地点。

- **返回值**

  |类型|说明|
  |-|-|
  |List\<String\>|地点集合|

- **原型**

  ``` java
  List<String> getLocations();
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### goTo()

用这个方法来让 temi 前往一个已保存的地点。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |location|String|目的地点名称，返回充电时，要使用`home base`作为 location 的值，`充电桩`是`home base`用于 UI 展示时的名称|
  |backwards|boolean|传入 `true` 让 temi 倒着前往目的地。默认值为 `false` 。最小支持版本为 0.10.80。|
  |noByPass|boolean|传入 `true` 将不允许在 go-to 过程中绕过障碍物。传入 `null` 将遵循系统中的设置 *设置 -> 导航*。最小支持版本为 0.10.80。|
  |speedLevel|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils_chn#speedLevel)|此次 go-to 的最大运行速度。传入 `null` 将遵循系统中的设置的速度 *设置 -> 导航*。最小支持版本为 0.10.80。|
  |highAccuracyArrival|boolean|传入 `true` 将严格控制本次移动的坐标和水平角度到更精准的位置。最小支持版本为 1.135.1。配合 135 版本 Launcher|
  |noRotationAtEnd|boolean|传入 `true` 将取消到达地点后的水平角度旋转。最小支持版本为 1.135.1。配合 135 版本 Launcher|

- **原型**

  ``` java
  void goTo(String location, boolean backwards, boolean noBypass, SpeedLevel speedLevel, boolean highAccuracyArrival, boolean noRotationAtEnd);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.36

---

### goToPosition()

用这个方法来让 temi 前往一个指定的位置坐标。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |position|[Position](#position)|目的位置坐标。在此方法中，忽略其 `tiltAngle` 属性。Position 的 yaw 值传入 999 可以取消到达后的旋转|
  |backwards|boolean|传入 `true` 让 temi 倒着前往目的地。默认值为 `false` 。最小支持版本为 0.10.80。|
  |noByPass|boolean|传入 `true` 将不允许在 go-to 过程中绕过障碍物。传入 `null` 将遵循系统中的设置 *设置 -> 导航*。最小支持版本为 0.10.80。|
  |speedLevel|[SpeedLevel](https://github.com/robotemi/sdk/wiki/Utils_chn#speedLevel)|此次 go-to 的最大运行速度。传入 `null` 将遵循系统中的设置的速度 *设置 -> 导航*。最小支持版本为 0.10.80。|
  |highAccuracyArrival|boolean|传入 `true` 将严格控制本次移动的坐标和水平角度到更精准的位置。最小支持版本为 1.135.1。配合 135 版本 Launcher|

- **原型**

  ``` java
  void goToPosition(Position position, boolean backwards, boolean noBypass, SpeedLevel speedLevel, boolean highAccuracyArrival);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.70

---

### getMapData()

用这个方法来获取地图数据。

~~**目前（0.10.70）该方法仅支持较小的地图，我们将在下一版本中支持较大的地图**。~~<br/>(**已在版本0.10.71中修复**)

0.10.74 中新增虚拟墙、导览路径、地点数据。

- **返回值**

  |类型|说明|
  |-|-|
  |[MapDataModel](#mapDataModel)|地图数据|

- **原型**

  ``` java
  MapDataModel getMapData();
  ```

- **所需权限**

  地图

- **最小支持版本**

  0.10.70

- **注意**

  这个方法是耗时操作，建议在非主线程中使用。可参考[示例代码](https://github.com/robotemi/sdk/blob/master/sample/src/main/java/com/robotemi/sdk/sample/MapActivity.kt)。

---

### getMapElements()

用这个方法来获取地图元素数据。

- **返回值**

  |类型|说明|
  |-|-|
  |List<[Layer](#layer)>|地图元素数据|

- **原型**

  ``` java
  List<Layer> getMapElements();
  ```

- **所需权限**

  地图

- **最小支持版本**

  1.136.0

---

### getMapImage()

用这个方法来获取地图图片数据。

- **返回值**

  |类型|说明|
  |-|-|
  |[MapImage](#mapImage)|地图图片数据|

- **原型**

  ``` java
  MapImage getMapImage();
  ```

- **所需权限**

  地图

- **最小支持版本**

  1.136.0

---

### repose()

用这个方法来让 temi 在丢失自己位置（例如由被抬起、拖动等造成）后进行重定位。


- **参数**

  |参数|类型|说明|
  |-|-|-|
  |position|[Position](#position)|134 版本加入，可选, 默认为 null。手动指定重定位的位置|

- **原型**

  ``` java
  void repose(Position position);
  ```

- **所需权限**

  无。

- **最小支持版本**

  0.10.72

---

### getMapList()

用这个方法来获取已备份的地图列表。

- **返回值**

  |类型|说明|
  |-|-|
  |List<[MapModel](#mapModel)>|地图列表|

- **原型**

  ``` java
  List<MapModel> getMapList();
  ```

- **所需权限**

   MAP

- **最小支持版本**

  0.10.74

---

### loadMap()

用这个方法来加载地图。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mapId|String|地图 ID, 从[MapModel](#mapmodel)中读取|

- **返回值**

  |类型|说明|
  |-|-|
  |String|请求 id， UUID格式, 如 538b44c9-fdcf-426a-9693-d72e9c0f9550. 可用于 onLoadMapStatusChanged 回调。添加于 129 版本，此前无返回值|

- **原型**

  ``` java
  String loadMap(String mapId);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  0.10.74

---

### loadMap() <a name="loadMapWithPosition">

用这个方法来在指定的位置坐标上通过地图 ID 来加载地图。

仅 mapId 为必传参数，其它参数可选，并且有默认值。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mapId|String|地图 ID, 从[MapModel](#mapmodel)中读取|
  |reposeRequired|boolean|地图加载完成后是否需要做重定位, 默认为 false|
  |position|[Position](#position)|指定从哪个位置（目标地图上的坐标）加载目标地图，默认为null，则从目标地图的充电桩位置加载地图|
  |offline|boolean|强制使用本地缓存加载目标地图，默认为 false. 添加于 129 版本。|
  |withoutUI|boolean|不显示全屏阻塞加载地图 UI，默认为 false. 添加于 129 版本。|

- **返回值**

  |类型|说明|
  |-|-|
  |String|请求 id， UUID格式, 如 538b44c9-fdcf-426a-9693-d72e9c0f9550. 可用于 onLoadMapStatusChanged 回调。添加于 129 版本，此前无返回值|

- **原型**

  ``` java
  String loadMap(String mapId, boolean reposeRequired, Position position);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  0.10.76

---

### setMultiFloorEnabled() <a name="setMultiFloorEnabled">

开启/关闭多楼层功能

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |enabled|boolean|true 开启，false 关闭|

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 操作成功，false 操作失败|

- **原型**

  ``` java
   boolean setMultiFloorEnabled(boolean enabled);
  ```

- **所需权限**

  MAP, SETTINGS

- **最小支持版本**

  1.129.0

---

### isMultiFloorEnabled() <a name="isMultiFloorEnabled">

检查多楼层功能开关状态

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 已开启，false 未开启，null Robot 服务尚未初始化|

- **原型**

  ``` java
   boolean isMultiFloorEnabled();
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.129.0

---

### getCurrentFloor() <a name="getCurrentFloor">

获取当前楼层数据

- **返回值**

  |类型|说明|
  |-|-|
  |[Floor](#floor)|当前楼层数据, 如果为 null，表示服务未初始化，缺少权限，或没有楼层数据|

- **原型**

  ``` java
    Floor getCurrentFloor();
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.129.0

---

### getAllFloors() <a name="getAllFloors">

获取所有楼层数据

- **返回值**

  |类型|说明|
  |-|-|
  |List<[Floor](#floor)>|所有楼层数据, 如果为空列表，表示服务未初始化，缺少权限，或没有楼层数据|

- **原型**

  ``` java
    List<Floor> getAllFloors();
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.129.0

---

### getFloorAndMapData() <a name="getFloorAndMapData">

获取指定楼层的地图数据

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|

- **返回值**

  |类型|说明|
  |-|-|
  |Pair<[Floor](#floor), [MapDataModel](#mapDataModel)>|指定楼层的地图数据|

- **原型**

  ``` java
    Pair<Floor, MapDataModel> getFloorAndMapData(int floorId);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.137.1

---
### loadFloor() <a name="loadFloor">

加载指定楼层

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|
  |position|[Position](#position)|目标楼层地图的地图加载位置|

- **原型**

  ``` java
    void loadFloor(int floorId, Position position);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.129.0
---

### getPosition() <a name="getPosition">

获取当前位置

- **返回值**

  |类型|说明|
  |-|-|
  |[Position](#position)|当前位置信息，获取失败会返回 Position(0, 0, 0, 0)|

- **原型**

  ``` java
    Position getPosition();
  ```

- **所需权限**

  无
  
- **最小支持版本**

  1.133.0

---

### resetMap() <a name="resetMap">

重置当前地图，或重置所有楼层的地图

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |allFloor|boolean|所有楼层|
  |saveHomeBaseIfCharging|boolean|是否在充电桩上调用时保存当前位置为充电桩, 1.137.1 版本加入, 默认为 false|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，400 无效操作，403 需要权限，408 超时|

- **原型**

  ``` java
    int resetMap(boolean allFloor, boolean saveHomeBaseIfCharging);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### finishMapping() <a name="finishMapping">

完成地图绘制，锁定地图

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mapName|String|地图名称，可选，默认为 null|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，304 地图已经处于锁定状态，400 无效操作，403 需要权限，408 超时|

- **原型**

  ``` java
    int finishMapping(String mapName);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### updateMapName() <a name="updateMapName">

更改当前地图名称

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mapName|String|地图名称|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，400 无效操作，403 需要权限，429 操作过于频繁，等待 2s|

- **原型**

  ``` java
    int updateMapName(String mapName);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### continueMapping() <a name="continueMapping">

继续绘制地图，解锁地图

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，304 地图已经处于解锁状态，400 无效操作，403 需要权限，408 超时，429 操作过于频繁，等待 5s|

- **原型**

  ``` java
    int continueMapping();
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### upsertMapLayer() <a name="upsertMapLayer">

更新地图元素，如果 layerId 已存在则更新当前图层，否则会创建新的图层。。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |layer|[Layer](#layer)|地图元素|
  |floorId|int|楼层 id, 1.137.1 版本加入, 默认为 null|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，400 无效操作，403 需要权限，413 传入的点超出地图范围 |

- **原型**

  ``` java
    int upsertMapLayer(Layer layer, int floorId);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### deleteMapLayer() <a name="deleteMapLayer">

删除地图元素，只支持删除 Green path 和 Virtual wall.

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |layerId|String|地图元素id|
  |layerCategory|int|地图元素类型|
  |floorId|int|楼层 id, 1.137.1 版本加入, 默认为 null|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 不支持，200 成功，400 无效操作，403 需要权限，404 传入的图层不存在 |

- **原型**

  ``` java
    int deleteMapLayer(String layerId, int LayerCategory, int floorId);
  ```

- **所需权限**

  MAP
  
- **最小支持版本**

  1.134.0

---

### newFloor() <a name="newFloor">

创建新楼层

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorName|String|楼层名称|
  |saveHomeBaseIfCharging|boolean|是否在充电桩上调用时保存当前位置为充电桩, 默认为 false|

- **返回值**

  |类型|说明|
  |-|-|
  |int| id（id!=0）楼层 id 如果成功，-400 包名异常，-403 包权限异常，-405 当前楼层/地图未锁定无法调用，-408 失败|
- **原型**

  ``` java
    int newFloor(String floorName, boolean saveHomeBaseIfCharging);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

---

### deleteFloor() <a name="deleteFloor">

删除指定楼层

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|

- **返回值**

  |类型|说明|
  |-|-|
  |int| 200 成功，-400 包名异常，-403 包权限异常，-409 当前楼层无法修改，-408 失败|

- **原型**

  ``` java
    int deleteFloor(int floorId);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

---

### renameFloor() <a name="renameFloor">

重命名楼层

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|
  |floorName|String|楼层名称|

- **返回值**

  |类型|说明|
  |-|-|
  |int|200 成功，-400 包名异常，-403 包权限异常，-408 失败|

- **原型**

  ``` java
    int renameFloor(int floorId, String floorName);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

### renameLocation() <a name="renameLocation">

重命名地点

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |oldLocationName|String|原地点名称|
  |newLocationName|String|新地点名称|
  |layer|Layer，可选|layer 的 layerCategory 必须为地点类型（Location type）|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 当前 launcher 不支持该操作，200 成功，400 无效参数，403 需要 [Permission.MAP] 权限，404 目标地图层不存在，409 不能重命名 Home Base 且不能将地点重命名为 Home Base|

- **原型**

  ``` java
    int renameLocation(String oldLocationName, String newLocationName, Layer layer);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

---

### renameLocationOnFloor() <a name="renameLocationOnFloor">

重命名指定楼层上的地点

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|
  |oldLocationName|String|原地点名称|
  |newLocationName|String|新地点名称|
  |layer|Layer，可选|layer 的 layerCategory 必须为地点类型（Location type）|

- **返回值**

  |类型|说明|
  |-|-|
  |int|0 当前 launcher 不支持该操作，400 包名异常，-403 地图权限异常，404 目标地图层不存在，409 当前楼层不可修改，413 地点数据越界，200 成功，408 失败|

- **原型**


  ``` java
    int renameLocationOnFloor(int floorId, String oldLocationName, String newLocationName, Layer layer);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

---

### deleteLocationOnFloor() <a name="deleteLocationOnFloor">

删除指定楼层上的地点

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |floorId|int|楼层 id|
  |locationName|String|地点名称|

- **返回值**


  |类型|说明|
  |-|-|
  |int|0 当前 launcher 不支持该操作，200 成功，-400 包名异常，-403 地图权限异常，-409 当前楼层不可修改，-408 失败|

- **原型**

  ``` java
    int deleteLocationOnFloor(int floorId, String locationName);
  ```

- **所需权限**

  MAP

- **最小支持版本**

  1.137.1

---

### isMapLocked() <a name="isMapLocked">

检查地图是否处于锁定状态

- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 已锁定，false 未锁定，null 当前版本 launcher 不支持此功能|

- **原型**

  ``` java
    boolean isMapLocked();
  ```


- **所需权限**

  无

- **最小支持版本**

  1.136.0

---

### isMapLost() <a name="isMapLost">

检查地图是否处于丢失状态


- **返回值**

  |类型|说明|
  |-|-|
  |boolean|true 已丢失，false 未丢失，null 当前版本 launcher 不支持此功能|

- **原型**

  ``` java
    boolean isMapLost();
  ```

- **所需权限**

  无

- **最小支持版本**

  1.136.0

---

### getReposeStatus() <a name="getReposeStatus">

获取重定位状态

- **返回值**

  |类型|说明|
  |-|-|
  |ReposeStatus|重定位状态|


- **原型**

  ``` java
    ReposeStatus getReposeStatus();
  ```

- **所需权限**

  无

- **最小支持版本**

  1.136.0

---

### getAllZones() <a name="getAllZones">

获取当前地图上定义的全部导航区域。

- **返回值**

  |类型|说明|
  |-|-|-|
  |List<[Layer](#layer)>|当前地图上全部 `ZONE` 类型的图层|

- **原型**

  ``` java
  List<Layer> getAllZones();
  ```

- **所需权限**

  [MAP](https://github.com/robotemi/sdk/wiki/Permission_chn)

- **最小支持版本**

  1.138.0

---

### getCurrentZones() <a name="getCurrentZones">

获取机器人当前位置所在的导航区域。机器人可能同时处于多个重叠区域内，因此返回列表。

- **返回值**

  |类型|说明|
  |-|-|-|
  |List<[Layer](#layer)>|当前包含机器人的导航区域图层列表|

- **原型**

  ``` java
  List<Layer> getCurrentZones();
  ```

- **所需权限**

  [MAP](https://github.com/robotemi/sdk/wiki/Permission_chn)

- **最小支持版本**

  1.138.0

---

### setCurrentGoToSpeed() <a name="setCurrentGoToSpeed">

为当前由本应用触发的 `goTo` / `goToPosition` 导航会话动态设置速度。可用于在导航过程中应用区域配置中的速度值。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |speed|float|速度值，范围 0.3 - 1.5 m/s|

- **返回值**

  |类型|说明|
  |-|-|-|
  |int|`0` 当前 Launcher 不支持；`200` 成功；`400` 包名校验失败或参数无效；`408` 失败|

- **原型**

  ``` java
  int setCurrentGoToSpeed(float speed);
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.138.0

---

### setCurrentGoToBypassObstacles() <a name="setCurrentGoToBypassObstacles">

为当前由本应用触发的 `goTo` / `goToPosition` 导航会话动态设置绕障策略。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |bypassObstacles|boolean|`true` 允许绕障；`false` 遇到障碍物时停止|

- **返回值**

  |类型|说明|
  |-|-|-|
  |int|`0` 当前 Launcher 不支持；`200` 成功；`400` 包名校验失败；`408` 失败|

- **原型**

  ``` java
  int setCurrentGoToBypassObstacles(boolean bypassObstacles);
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.138.0

---

### setCurrentGoToObstacleAvoidanceDistance() <a name="setCurrentGoToObstacleAvoidanceDistance">

为当前由本应用触发的 `goTo` / `goToPosition` 导航会话动态设置避障距离。

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |obstacleAvoidanceDistance|int|避障距离，单位厘米，范围 0 - 100|

- **返回值**

  |类型|说明|
  |-|-|-|
  |int|`0` 当前 Launcher 不支持；`200` 成功；`400` 包名校验失败或参数无效；`408` 失败|

- **原型**

  ``` java
  int setCurrentGoToObstacleAvoidanceDistance(int obstacleAvoidanceDistance);
  ```

- **所需权限**

  无。

- **最小支持版本**

  1.138.0

---

<br>

## 接口

### OnLocationsUpdatedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取每次地点更新后的地点列表。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnLocationsUpdatedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |locations|List\<String\>|所有已保存的地点集合|

- **原型**

  ``` java
  abstract void onLocationsUpdated(List<String> locations)
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLocationsUpdatedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnLocationsUpdatedListener(OnLocationsUpdatedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLocationsUpdatedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnLocationsUpdatedListener(OnLocationsUpdatedListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### OnGoToLocationStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取 go to 地点过程中的状态信息。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnGoToLocationStatusChangedListener {}
```

#### 静态常量

|常量|类型|值|说明|
|-|-|-|-|
|START|String|"start"|导航开始|
|CALCULATING|String|"calculating"|正在规划前往目的地的路线 |
|GOING|String|"going"|路线规划完成并正在前往目的地点|
|COMPLETE|String|"complete"|到达目的地点 |
|ABORT|String|"abort"|导航终止|
|REPOSING|String|"reposing"|导航过程中的重定位|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |location|String|目的地点|
  |status|String|导航状态|
  |descriptionId|int|状态描述的数字代码|
  |description|String|状态描述，一般用于描述障碍物信息|



- DescriptionId 对应内容

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
  |10008|"Path Planing" // 134 版本新增|
  |10009|"Docking" // 134 版本新增|
  |-|"Unknown"|

- **原型**

  ``` java
  void onGoToLocationStatusChanged(String location, String status, int descriptionId, String description);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGoToLocationStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnGoToLocationStatusChangedListener(OnGoToLocationStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGoToLocationStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnGoToLocationStatusChangedListener(OnGoToLocationStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.36

---

### OnDistanceToLocationChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取当前位置距离其他已保存地点的直线距离。

#### 原型

``` java
package com.robotemi.sdk.navigation.listener;

interface OnDistanceToLocationChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |distances|Map\<String, Float\>|`key` 为 `String` 类型的地点名，`value` 为 `Float` 类型的距离的键值对集合。|

- **原型**

  ``` java
  void onDistanceToLocationChanged(Map<String, Float> distances);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDistanceToLocationChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnDistanceToLocationChangedListener(OnDistanceToLocationChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDistanceToLocationChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnDistanceToLocationChangedListener(OnDistanceToLocationChangedListener listener);
  ```

- **最小支持版本**

  0.10.70

---

### OnCurrentPositionChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取当前位置坐标信息。

#### 原型

``` java
package com.robotemi.sdk.navigation.listener;

interface OnCurrentPositionChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |position|[Position](#position)|当前位置坐标信息|

- **原型**

  ``` java
  void onCurrentPositionChanged(Position position);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnCurrentPositionChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnCurrentPositionChangedListener(OnCurrentPositionChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnCurrentPositionChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnCurrentPositionChangedListener(OnCurrentPositionChangedListener listener);
  ```

- **最小支持版本**

  0.10.70

---

### OnReposeStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以监听重定位时的状态变化。

#### 原型

``` java
package com.robotemi.sdk.navigation.listener;

interface OnReposeStatusChangedListener {}
```

#### 静态常量

这里的常量均为重定位状态。

|常量|类型|值|说明|
|-|-|-|-|
|IDLE|int|0|空闲|
|REPOSE_REQUIRED|int|1|准备重定位|
|REPOSING_START|int|2|重定位开始|
|REPOSING_GOING|int|3|重定位进行中|
|REPOSING_COMPLETE|int|4|完成重定位|
|REPOSING_OBSTACLE_DETECTED|int|5|检测到障碍物|
|REPOSING_ABORT|int|6|重定位中断|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|重定位状态|
  |description|String|状态描述|

- **原型**

  ``` java
  void onReposeStatusChanged(int status, String description);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnReposeStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnReposeStatusChangedListener(OnReposeStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnReposeStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnReposeStatusChangedListener(OnReposeStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.72

---

### OnLoadMapStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以监听加载地图过程中的状态变化。

#### 原型

``` java
package com.robotemi.sdk.map;

interface OnLoadMapStatusChangedListener {}
```

#### 静态常量

这里的常量均为地图加载的状态。

|常量|类型|值|说明|
|-|-|-|-|
|COMPLETE|int|0|完成|
|START|int|1|开始|
|ERROR_UNKNOWN|int|1000|未知错误|
|ERROR_ABORT_FROM_ROBOX|int|2000|Robox 中断加载|
|ERROR_ABORT_ON_NOT_CHARGING|int|2001|未在充电桩上加载|
|ERROR_ABORT_BUSY|int|2002|正在执行其他不可打断的任务|
|ERROR_ABORT_ON_TIMEOUT|int|3000|加载超时|
|ERROR_PB_STREAM_FILE_INVALID|int|4000|地图文件不可用|
|ERROR_GET_MAP_DATA|int|5000|从远端获取地图数据出错|

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|加载状态|

- **原型**

  ``` java
  void onLoadMapStatusChanged(int status);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLoadMapStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnLoadMapStatusChangedListener(OnLoadMapStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLoadMapStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnLoadMapStatusChangedListener(OnLoadMapStatusChangedListener listener);
  ```

- **最小支持版本**

  0.10.74

---

### OnDistanceToDestinationChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取当距离目的地的剩余导航路径。

#### 原型

``` java
package com.robotemi.sdk.navigation.listener;

interface OnDistanceToDestinationChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |location|String|目的地的地点名称|
  |distance|float|到目的地的距离|

- **原型**

  ``` java
  void onDistanceToDestinationChanged(location: String, distance: Float);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDistanceToDestinationChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnDistanceToDestinationChangedListener(OnDistanceToDestinationChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnDistanceToDestinationChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnDistanceToDestinationChangedListener(OnDistanceToDestinationChangedListener listener);
  ```

- **最小支持版本**

  0.10.80

---

### OnLoadFloorStatusChangedListener

在你的上下文中实现这个监听器接口，并重写接口中的方法以获取加载楼层的状态。

#### 原型

``` java
package com.robotemi.sdk.map;

interface OnLoadFloorStatusChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |status|int|0 完成<br>1 开始<br>-1 失败|

- **原型**

  ``` java
  void onLoadFloorStatusChanged(int status);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLoadFloorStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnLoadFloorStatusChangedListener(OnLoadFloorStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnLoadFloorStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnLoadFloorStatusChangedListener(OnLoadFloorStatusChangedListener listener);
  ```

- **最小支持版本**

  1.129.0


### OnRobotDragStateChangedListener

机器人不在移动状态时被拖拽产生的回调

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnRobotDragStateChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isDragged|Boolean|true 正在被拖拽|

- **原型**

  ``` java
  void onRobotDragStateChanged(Boolean isDragged);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRobotDragStateChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnRobotDragStateChangedListener(OnRobotDragStateChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnRobotDragStateChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnRobotDragStateChangedListener(OnRobotDragStateChangedListener listener);
  ```

- **最小支持版本**

  1.130.1



### OnGoToNavPathChangedListener

机器人导航时实时规划的路径的回调

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnGoToNavPathChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |path|List<LayerPose>|从机器人当前位置到目的地的导航规划路径|

- **原型**

  ``` java
  void onGoToNavPathChanged(List<LayerPose> path);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGoToNavPathChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnGoToNavPathChangedListener(OnGoToNavPathChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnGoToNavPathChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnGoToNavPathChangedListener(OnGoToNavPathChangedListener listener);
  ```

- **最小支持版本**

  1.134.0

---

### OnMapStatusChangedListener

监听地图状态变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnMapStatusChangedListener {}
```

#### 抽象方法

- **原型**

  ``` java
  void onMapStatusChanged(boolean isLost, boolean isLocked);
  ```

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |isLost|boolean|地图是否丢失|
  |isLocked|boolean|地图是否锁定|

- **最小支持版本**

  1.136.0

---

### OnMapNameChangedListener

监听地图名称变化。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnMapNameChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |mapName|String|地图名称|

- **原型**

  ``` java
  void onMapNameChanged(String mapName);
  ```

- **最小支持版本**

  1.136.0

---

### OnZoneEntranceStatusChangedListener

监听机器人所在导航区域的变化（进入或离开区域）。

#### 原型

``` java
package com.robotemi.sdk.listeners;

interface OnZoneEntranceStatusChangedListener {}
```

#### 抽象方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |layers|List<[Layer](#layer)>|机器人当前所在的导航区域图层列表|

- **原型**

  ``` java
  void onZoneEntranceStatusChanged(List<Layer> layers);
  ```

#### 添加监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnZoneEntranceStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void addOnZoneEntranceStatusChangedListener(OnZoneEntranceStatusChangedListener listener);
  ```

#### 移除监听器的方法

- **参数**

  |参数|类型|说明|
  |-|-|-|
  |listener|OnZoneEntranceStatusChangedListener|实现了这个接口的类的实例|

- **原型**

  ``` java
  void removeOnZoneEntranceStatusChangedListener(OnZoneEntranceStatusChangedListener listener);
  ```

- **最小支持版本**

  1.138.0

---

<br>

## 模型

### Position

用于存放位置地点信息。

#### 原型

``` java
package com.robotemi.sdk.navigation.model;

class Position {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|x|float|位置坐标 x|
|y|float|位置坐标 y|
|yaw|float|弧度，取值为 [-π, π]，传参超出范围会 ± 2π 重新取值。0 表示在充电桩上重置地图时，temi 的朝向|
|tiltAngle|int|头部倾斜角度|
|isInMapArea|boolean|是否在地图范围内, 1.136.0 版本加入|

---

### MapDataModel

用于存放地图数据。

#### 原型

``` java
package com.robotemi.sdk.map;

class MapDataModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|mapImage|[MapImage](#mapImage)|地图图片数据|
|mapId|String|地图数据唯一标识，服务于算法引擎，不等同于[MapModel](#mapModel) 的 id, 后者用于标识地图备份|
|mapInfo|[MapInfo](#mapInfo)|地图信息|
|virtualWalls|List<[Layer](#layer)>|虚拟墙图层集|
|greenPaths|List<[Layer](#layer)>|导航路径图层集|
|locations|List<[Layer](#layer)>|地点图层集|
|zones|List<[Layer](#layer)>|导航区域图层集（1.138.0 版本加入）|
|mapName|String|当前地图名称（130 版本加入）|

### MapImage

用于存放地图的图片数据。

#### 原型

``` java
package com.robotemi.sdk.map;

class MapImage {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|typeId|String|-|
|rows|int|地图数据矩阵的行数|
|cols|int|地图数据矩阵的列数|
|dt|String|-|
|data|List\<Integer\>|地图数据矩阵转换成的一维数组|

### MapInfo

用于存放地图信息数据。

#### 原型

``` java
package com.robotemi.sdk.map;

class MapInfo {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|height|int|高|
|width|int|宽|
|originX|float|原点 x 坐标|
|originY|float|原点 y 坐标|
|resolution|float|分辨率|

### Layer

用于存放地图图层数据。

#### 原型

``` java
package com.robotemi.sdk.map;

class Layer {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|layerCreationUTC|int|图层创建时间戳|
|layerCategory|int|[图层类别](#layerCategory)|
|layerId|String|图层唯一标识|
|layerThickness|float|图层厚度|
|layerStatus|int|[图层状态](#layerStatus)|
|layerPoses|List<[LayerPose](#layerPose)>|图层坐标集|
|layerDirection|int|虚拟墙的方向，1.132.1 版本加入。取值 -1，0，1，表示虚拟墙可通过的方向|
|layerData|String|图层数据，1.133.0 版本加入，用于存储地图擦图层数据；对于 `ZONE` 类型图层，可解析为 [ZoneProperty](#zoneProperty)|
|zoneProperty|[ZoneProperty](#zoneProperty)|导航区域属性，仅当 `layerCategory` 为 `ZONE` 且 `layerData` 有效时可用（1.138.0 版本加入）|

### ZoneProperty

用于存放导航区域的配置属性。

#### 原型

``` java
package com.robotemi.sdk.map;

class ZoneProperty {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|name|String|区域名称|
|speed|String|导航速度档位，如 `Medium`|
|bypassObstacles|boolean|是否允许绕障|
|obstacleAvoidanceDistance|int|避障距离，单位厘米|

### LayerPose

用于存放构成地图图层的坐标数据。

#### 原型

``` java
package com.robotemi.sdk.map;

class LayerPose {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|x|float|x 坐标|
|y|float|y 坐标|
|theta|float|-|

### MapDataModelKt

#### 原型

``` java
package com.robotemi.sdk.map;

class MapDataModelKt {}
```

#### 静态常量

##### 图层类别 <a name="layerCategory" />

|常量|类型|值|说明|
|-|-|-|-|
|GREEN_PATH|int|0|导航路径|
|VIRTUAL_WALL|int|3|虚拟墙|
|LOCATION|int|4|地点|
|MAP_ERASER|int|6|地图擦|
|ZONE|int|7|导航区域（1.138.0 版本加入）|

##### 图层状态 <a name="layerStatus" />

|常量|类型|值|说明|
|-|-|-|-|
|STATUS_CURRENT|int|0|-|
|STATUS_UPDATE|int|1|-|
|STATUS_ADD_POSE|int|2|-|
|STATUS_DELETE|int|3|-|

### MapModel

用于存放已备份地图列表中的地图信息。

#### 原型

``` java
package com.robotemi.sdk.map;

class MapModel {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|String|地图存储ID，对应于地图备份，可用于加载地图|
|name|String|地图名称|

### Floor

用于存放楼层信息。

#### 原型

``` java
package com.robotemi.sdk.map;

class  Floor {}
```

#### 属性

|属性|类型|说明|
|-|-|-|
|id|String|楼层 id|
|name|String|楼层名称|
|mapId|String|楼层对应的地图 id, 对应为 [MapDataModel](#mapdatamodel) 中的 id|
|locations|List<Location>|楼层的地点列表|

### ReposeStatus

重定位状态。

#### 原型

``` kotlin
package com.robotemi.sdk.constants;

enum class ReposeStatus(val value: Int) {
  UNKNOWN(0),
  IDLE(1),
  REPOSING(2),
  REPOSE_REQUIRED(3)
} 
```