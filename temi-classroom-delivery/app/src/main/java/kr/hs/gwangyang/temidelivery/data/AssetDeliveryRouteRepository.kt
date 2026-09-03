package kr.hs.gwangyang.temidelivery.data

import android.content.Context
import kr.hs.gwangyang.temidelivery.domain.DeliveryRoute
import kr.hs.gwangyang.temidelivery.domain.DeliveryRouteRepository
import kr.hs.gwangyang.temidelivery.domain.DeliverySpeed
import kr.hs.gwangyang.temidelivery.domain.DeliveryStop
import kr.hs.gwangyang.temidelivery.domain.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AssetDeliveryRouteRepository(
    private val context: Context,
    private val assetName: String = "delivery_route.json",
) : DeliveryRouteRepository {

    override suspend fun loadConfiguredRoute(): Result<DeliveryRoute> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseRoute(JSONObject(json))
        }
    }

    private fun parseRoute(json: JSONObject): DeliveryRoute {
        require(json.getInt("schemaVersion") == 1) { "지원하지 않는 경로 스키마입니다." }
        val stopsJson = json.getJSONArray("stops")
        val stops = buildList {
            for (index in 0 until stopsJson.length()) {
                val stop = stopsJson.getJSONObject(index)
                add(
                    DeliveryStop(
                        id = stop.getString("id"),
                        recipient = stop.getString("recipient"),
                        supply = stop.getString("supply"),
                        quantity = stop.getInt("quantity"),
                        guideItemId = stop.optString("guideItemId", "").trim().ifBlank { null },
                        destination = parseDestination(stop.getJSONObject("destination")),
                    ),
                )
            }
        }
        return DeliveryRoute(
            name = json.getString("name"),
            stops = stops,
            returnDestination = parseDestination(json.getJSONObject("returnDestination")),
            speed = DeliverySpeed.valueOf(json.optString("speed", DeliverySpeed.VERY_SLOW.name)),
            highAccuracyArrival = json.optBoolean("highAccuracyArrival", true),
        )
    }

    private fun parseDestination(json: JSONObject): Destination {
        return when (json.getString("type")) {
            "saved_location" -> {
                val name = json.getString("name")
                Destination.SavedLocation(
                    name = name,
                    displayName = json.optString("label", name).ifBlank { name },
                )
            }

            "coordinate" -> Destination.Coordinate(
                x = json.getDouble("x").toFloat(),
                y = json.getDouble("y").toFloat(),
                yaw = json.getDouble("yaw").toFloat(),
                displayName = json.getString("label"),
            )

            else -> error("destination.type은 saved_location 또는 coordinate여야 합니다.")
        }
    }
}
