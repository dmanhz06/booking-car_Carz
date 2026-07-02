package com.example.carz.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * RoutingService chịu trách nhiệm gọi OSRM API để lấy dữ liệu đường đi.
 * Đã chuyển sang HTTPS để tránh lỗi Cleartext Traffic trên Android.
 */
class RoutingService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * fetchRoute gọi OSRM API và trả về kết quả lộ trình.
     * Tọa độ truyền vào: start, end (GeoPoint).
     * OSRM yêu cầu định dạng: longitude,latitude.
     */
    suspend fun fetchRoute(start: GeoPoint, end: GeoPoint): RouteResult? = withContext(Dispatchers.IO) {
        // Sử dụng HTTPS để đảm bảo kết nối thành công
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                "?overview=full&geometries=geojson"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "CarzApp/1.0") // Thêm User-Agent theo khuyến nghị của OSRM
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                
                // Kiểm tra mã trạng thái của OSRM
                if (json.optString("code") != "Ok") return@withContext null
                
                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val distance = route.getDouble("distance") / 1000.0 // mét sang km
                    
                    val geometry = route.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    val points = mutableListOf<GeoPoint>()
                    
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        // OSRM: [longitude, latitude] -> Osmdroid: GeoPoint(latitude, longitude)
                        points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
                    }
                    return@withContext RouteResult(points, distance)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

data class RouteResult(val points: List<GeoPoint>, val distanceKm: Double)
