package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Driver(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "carModel") val carModel: String,
    @Json(name = "plateNumber") val plateNumber: String,
    @Json(name = "lat") var lat: Double,
    @Json(name = "lng") var lng: Double,
    @Json(name = "rating") val rating: Double = 4.9,
    @Json(name = "heading") var heading: Float = 0f,
    @Json(name = "isAvailable") var isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class RideRequest(
    @Json(name = "pickupLat") val pickupLat: Double = 30.5,
    @Json(name = "pickupLng") val pickupLng: Double = 47.78,
    @Json(name = "destination") val destination: String = "كورنيش البصرة",
    @Json(name = "passengerName") val passengerName: String = "راكب البصرة",
    @Json(name = "driverId") val driverId: String? = null
)

@JsonClass(generateAdapter = true)
data class RideResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "driverId") val driverId: String? = null,
    @Json(name = "status") val status: String? = "CONFIRMED"
)

enum class RideStatus {
    IDLE,
    SEARCHING,
    DRIVER_COMING,
    DRIVER_ARRIVED,
    RIDE_IN_PROGRESS,
    COMPLETED
}

data class BasraLandmark(
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String
)
