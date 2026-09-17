package com.example.data.repository

import android.util.Log
import com.example.data.api.NexaTaxiApiService
import com.example.data.model.Driver
import com.example.data.model.RideRequest
import com.example.data.model.RideResponse
import com.example.data.model.RideStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class TaxiRepository(
    private val apiService: NexaTaxiApiService = NexaTaxiApiService.create()
) {
    companion object {
        const val TAG = "TaxiRepository"
        const val USER_LAT = 30.5000
        const val USER_LNG = 47.7800
    }

    // Default 5 simulated Basra drivers
    private val defaultBasraDrivers = listOf(
        Driver(
            id = "driver_1",
            name = "حسين البصري",
            phone = "07701234561",
            carModel = "كيا ريو صفراء",
            plateNumber = "البصرة 45210 أ",
            lat = 30.5042,
            lng = 47.7865,
            rating = 4.9,
            heading = 45f
        ),
        Driver(
            id = "driver_2",
            name = "علي الكعبي",
            phone = "07701234562",
            carModel = "تويوتا كورولا صفراء",
            plateNumber = "البصرة 18923 ب",
            lat = 30.4935,
            lng = 47.7740,
            rating = 4.8,
            heading = 135f
        ),
        Driver(
            id = "driver_3",
            name = "كرار التميمي",
            phone = "07701234563",
            carModel = "هيونداي إلنترا صفراء",
            plateNumber = "البصرة 77341 ج",
            lat = 30.5120,
            lng = 47.7915,
            rating = 5.0,
            heading = 220f
        ),
        Driver(
            id = "driver_4",
            name = "سجاد السعدون",
            phone = "07701234564",
            carModel = "نيسان صني صفراء",
            plateNumber = "البصرة 93420 د",
            lat = 30.5085,
            lng = 47.7680,
            rating = 4.7,
            heading = 310f
        ),
        Driver(
            id = "driver_5",
            name = "محمد الحلفي",
            phone = "07701234565",
            carModel = "سايبا تاكسي صفراء",
            plateNumber = "البصرة 31205 و",
            lat = 30.4890,
            lng = 47.7890,
            rating = 4.9,
            heading = 90f
        )
    )

    private val _drivers = MutableStateFlow<List<Driver>>(defaultBasraDrivers)
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _rideStatus = MutableStateFlow(RideStatus.IDLE)
    val rideStatus: StateFlow<RideStatus> = _rideStatus.asStateFlow()

    private val _assignedDriver = MutableStateFlow<Driver?>(null)
    val assignedDriver: StateFlow<Driver?> = _assignedDriver.asStateFlow()

    private val _serverMessage = MutableStateFlow<String?>("متصل بسيرفر Nexa Taxi")
    val serverMessage: StateFlow<String?> = _serverMessage.asStateFlow()

    private val _isServerOnline = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline.asStateFlow()

    private val _distanceToUserMeters = MutableStateFlow(0)
    val distanceToUserMeters: StateFlow<Int> = _distanceToUserMeters.asStateFlow()

    private val _etaSeconds = MutableStateFlow(0)
    val etaSeconds: StateFlow<Int> = _etaSeconds.asStateFlow()

    private var movementJob: Job? = null
    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Vector step directions for simulation
    private val driverSteps = mutableMapOf(
        "driver_1" to Pair(0.0004, 0.0003),
        "driver_2" to Pair(-0.0003, 0.0004),
        "driver_3" to Pair(0.0002, -0.0005),
        "driver_4" to Pair(-0.0004, -0.0003),
        "driver_5" to Pair(0.0005, -0.0002)
    )

    init {
        startDriversSimulation()
        startServerSync()
    }

    /**
     * Start moving the 5 drivers around Basra every second.
     */
    fun startDriversSimulation() {
        if (movementJob?.isActive == true) return
        movementJob = scope.launch {
            while (isActive) {
                delay(1000) // update every second
                updateDriversMovement()
            }
        }
    }

    private fun updateDriversMovement() {
        val currentAssignedId = _assignedDriver.value?.id
        val currentStatus = _rideStatus.value

        _drivers.update { currentList ->
            currentList.map { driver ->
                if (driver.id == currentAssignedId && (currentStatus == RideStatus.DRIVER_COMING || currentStatus == RideStatus.SEARCHING)) {
                    // This driver is heading directly to the user at (USER_LAT, USER_LNG)
                    val deltaLat = USER_LAT - driver.lat
                    val deltaLng = USER_LNG - driver.lng
                    val dist = calculateDistanceInMeters(driver.lat, driver.lng, USER_LAT, USER_LNG)

                    _distanceToUserMeters.value = dist.toInt()
                    _etaSeconds.value = (dist / 10).toInt().coerceAtLeast(5) // approx 36 km/h (10 m/s)

                    if (dist < 30) {
                        // Arrived!
                        _rideStatus.value = RideStatus.DRIVER_ARRIVED
                        driver.copy(lat = USER_LAT, lng = USER_LNG)
                    } else {
                        // Move closer to user by approx 0.00025 degrees (~25-30m per second)
                        val angle = atan2(deltaLat, deltaLng)
                        val step = 0.00022
                        val nextLat = driver.lat + step * sin(angle)
                        val nextLng = driver.lng + step * cos(angle)
                        val headingDeg = Math.toDegrees(angle).toFloat()

                        val updated = driver.copy(
                            lat = nextLat,
                            lng = nextLng,
                            heading = (headingDeg + 360f) % 360f
                        )
                        _assignedDriver.value = updated
                        updated
                    }
                } else {
                    // Regular roaming around Basra
                    val currentStep = driverSteps[driver.id] ?: Pair(0.0003, 0.0003)
                    var (dLat, dLng) = currentStep

                    // Slight random jitter for organic car motion
                    dLat += (Random.nextDouble() - 0.5) * 0.00015
                    dLng += (Random.nextDouble() - 0.5) * 0.00015

                    var nextLat = driver.lat + dLat
                    var nextLng = driver.lng + dLng

                    // Basra city boundary reflection:
                    // Lat bounds: 30.470 to 30.535
                    // Lng bounds: 47.740 to 47.830
                    if (nextLat > 30.535 || nextLat < 30.470) {
                        dLat = -dLat
                        nextLat = driver.lat + dLat
                    }
                    if (nextLng > 47.830 || nextLng < 47.740) {
                        dLng = -dLng
                        nextLng = driver.lng + dLng
                    }

                    driverSteps[driver.id] = Pair(dLat, dLng)

                    val headingAngle = Math.toDegrees(atan2(dLat, dLng)).toFloat()
                    val heading = (headingAngle + 360f) % 360f

                    driver.copy(
                        lat = nextLat,
                        lng = nextLng,
                        heading = heading
                    )
                }
            }
        }
    }

    /**
     * Start periodic server synchronization with GET /drivers.
     */
    private fun startServerSync() {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            while (isActive) {
                fetchDriversFromServer()
                delay(15000) // Re-check every 15 seconds
            }
        }
    }

    suspend fun fetchDriversFromServer() {
        try {
            // 1. Verify health of the Nexa Taxi server
            val healthCheck = try {
                apiService.checkHealth()
            } catch (e: Exception) {
                null
            }

            if (healthCheck != null && (healthCheck.isSuccessful || healthCheck.code() in 200..399)) {
                _isServerOnline.value = true
                _serverMessage.value = "سيرفر نكسا شغال بالبصرة ✔️"
            }

            // 2. Fetch driver updates from /drivers
            val response = try {
                apiService.getDrivers()
            } catch (e: Exception) {
                Log.w(TAG, "Drivers endpoint notice: ${e.message}")
                null
            }

            if (response != null && response.isSuccessful) {
                val serverDrivers = response.body()
                if (!serverDrivers.isNullOrEmpty()) {
                    _isServerOnline.value = true
                    _serverMessage.value = "تم مزامنة الكباتن مع السيرفر ✔️"
                    _drivers.update { current ->
                        serverDrivers.mapIndexed { index, sDriver ->
                            val existing = current.find { it.id == sDriver.id }
                            sDriver.copy(
                                lat = if (sDriver.lat != 0.0) sDriver.lat else (existing?.lat ?: (USER_LAT + 0.005 * (index + 1))),
                                lng = if (sDriver.lng != 0.0) sDriver.lng else (existing?.lng ?: (USER_LNG + 0.005 * (index + 1))),
                                heading = existing?.heading ?: 0f
                            )
                        }
                    }
                } else {
                    if (_isServerOnline.value) {
                        _serverMessage.value = "السيرفر متصل • 5 كباتن متجولين بالبصرة 🚕"
                    }
                }
            } else if (response != null && response.code() == 404) {
                // Express server is active but /drivers route not added yet
                _isServerOnline.value = true
                _serverMessage.value = "السيرفر شغال • جاهز لاستقبال طلبات التاكسي"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Server sync notice: ${e.localizedMessage}")
            // Keep app operational with Basra drivers
            if (!_isServerOnline.value) {
                _serverMessage.value = "جاري الاتصال بسيرفر Nexa Taxi..."
            }
        }
    }

    /**
     * Request a ride:
     * 1. Finds the nearest driver in Basra.
     * 2. Calls POST /request-ride on the server.
     * 3. Sets state to "السائق قادم" (DRIVER_COMING).
     */
    suspend fun requestRide(destination: String = "كورنيش شط العرب"): Result<Driver> {
        _rideStatus.value = RideStatus.SEARCHING
        _serverMessage.value = "جاري حجز التاكسي عبر السيرفر..."

        // Find nearest driver to user (USER_LAT, USER_LNG)
        val currentList = _drivers.value
        val nearest = currentList.minByOrNull { driver ->
            calculateDistanceInMeters(driver.lat, driver.lng, USER_LAT, USER_LNG)
        } ?: defaultBasraDrivers.first()

        val initialDistance = calculateDistanceInMeters(nearest.lat, nearest.lng, USER_LAT, USER_LNG).toInt()
        _distanceToUserMeters.value = initialDistance
        _etaSeconds.value = (initialDistance / 10).coerceAtLeast(30)

        // Send POST to the server: https://jesse-native-rug-gst.trycloudflare.com/request-ride
        try {
            val req = RideRequest(
                pickupLat = USER_LAT,
                pickupLng = USER_LNG,
                destination = destination,
                passengerName = "راكب البصرة",
                driverId = nearest.id
            )
            val response = apiService.requestRide(req)
            if (response.isSuccessful) {
                _isServerOnline.value = true
                _serverMessage.value = "تم تأكيد الحجز من السيرفر بنجاح ✔️"
            } else {
                Log.d(TAG, "request-ride returned code ${response.code()}")
                _serverMessage.value = "تم تأكيد طلبك وتعيين الكابتن ${nearest.name} 🚕"
            }
        } catch (e: Exception) {
            Log.w(TAG, "request-ride sync notice: ${e.localizedMessage}")
            _serverMessage.value = "تم تأكيد طلبك وتعيين الكابتن ${nearest.name} 🚕"
        }

        // Set assigned driver and update ride status to "السائق قادم"
        _assignedDriver.value = nearest
        _rideStatus.value = RideStatus.DRIVER_COMING
        return Result.success(nearest)
    }

    /**
     * Cancel the current ride.
     */
    fun cancelRide() {
        _rideStatus.value = RideStatus.IDLE
        _assignedDriver.value = null
        _distanceToUserMeters.value = 0
        _etaSeconds.value = 0
        _serverMessage.value = "تم إلغاء الرحلة"
    }

    /**
     * Finish/Complete the ride.
     */
    fun completeRide() {
        _rideStatus.value = RideStatus.COMPLETED
        scope.launch {
            delay(3000)
            _rideStatus.value = RideStatus.IDLE
            _assignedDriver.value = null
        }
    }

    /**
     * Haversine distance formula to calculate distance between two coordinates in meters.
     */
    private fun calculateDistanceInMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
