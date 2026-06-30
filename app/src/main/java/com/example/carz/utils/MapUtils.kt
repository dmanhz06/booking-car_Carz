package com.example.carz.utils

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale

object MapUtils {
    // Chuyển địa chỉ nhập tay thành tọa độ (để hiển thị đúng điểm đến)
    fun getPointFromAddress(context: Context, address: String): GeoPoint {
        return try {
            val geocoder = Geocoder(context, Locale("vi", "VN"))
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                GeoPoint(results[0].latitude, results[0].longitude)
            } else GeoPoint(10.8456, 106.7533)
        } catch (e: Exception) { GeoPoint(10.8456, 106.7533) }
    }

    // Lấy đường đi thực tế theo đường bộ (không còn đường thẳng tắp)
    suspend fun getRoutePoints(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"
                val json = java.net.URL(url).readText()
                val obj = org.json.JSONObject(json)
                val coords = obj.getJSONArray("routes").getJSONObject(0)
                    .getJSONObject("geometry").getJSONArray("coordinates")

                val points = mutableListOf<GeoPoint>()
                for (i in 0 until coords.length()) {
                    val c = coords.getJSONArray(i)
                    points.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                }
                points
            } catch (e: Exception) { listOf(start, end) }
        }
    }
}