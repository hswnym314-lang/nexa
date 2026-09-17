package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Driver
import com.example.data.model.RideStatus
import com.example.ui.theme.TaxiBlack
import com.example.ui.theme.TaxiBorder
import com.example.ui.theme.TaxiDarkCard
import com.example.ui.theme.TaxiDarkSurface
import com.example.ui.theme.TaxiSuccessGreen
import com.example.ui.theme.TaxiYellow
import com.example.ui.theme.TaxiYellowDark
import kotlin.math.cos
import kotlin.math.sin

enum class MapRenderMode {
    HYBRID_WEBVIEW,
    VECTOR_CANVAS
}

@Composable
fun BasraMapView(
    userLat: Double = 30.5000,
    userLng: Double = 47.7800,
    drivers: List<Driver>,
    assignedDriver: Driver? = null,
    rideStatus: RideStatus = RideStatus.IDLE,
    modifier: Modifier = Modifier
) {
    var mapMode by remember { mutableStateOf(MapRenderMode.VECTOR_CANVAS) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier.fillMaxSize()) {
        when (mapMode) {
            MapRenderMode.VECTOR_CANVAS -> {
                BasraVectorMap(
                    userLat = userLat,
                    userLng = userLng,
                    drivers = drivers,
                    assignedDriver = assignedDriver,
                    rideStatus = rideStatus,
                    zoomLevel = zoomLevel,
                    panOffsetX = panOffsetX,
                    panOffsetY = panOffsetY,
                    onPanAndZoom = { dPanX, dPanY, dZoom ->
                        panOffsetX += dPanX
                        panOffsetY += dPanY
                        zoomLevel = (zoomLevel * dZoom).coerceIn(0.6f, 3.5f)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            MapRenderMode.HYBRID_WEBVIEW -> {
                BasraWebViewMap(
                    userLat = userLat,
                    userLng = userLng,
                    drivers = drivers,
                    assignedDriver = assignedDriver,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Map controls: Re-center, Zoom In/Out, Mode switch
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Map Type Toggle Button
            SmallFloatingActionButton(
                onClick = {
                    mapMode = if (mapMode == MapRenderMode.VECTOR_CANVAS) {
                        MapRenderMode.HYBRID_WEBVIEW
                    } else {
                        MapRenderMode.VECTOR_CANVAS
                    }
                },
                containerColor = TaxiDarkCard,
                contentColor = TaxiYellow,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "تبديل نمط الخريطة",
                    modifier = Modifier.size(22.dp)
                )
            }

            if (mapMode == MapRenderMode.VECTOR_CANVAS) {
                // Zoom In
                SmallFloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(3.5f) },
                    containerColor = TaxiDarkCard,
                    contentColor = TaxiYellow,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تكبير الخريطة",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Zoom Out
                SmallFloatingActionButton(
                    onClick = { zoomLevel = (zoomLevel * 0.8f).coerceAtLeast(0.6f) },
                    containerColor = TaxiDarkCard,
                    contentColor = TaxiYellow,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "تصغير الخريطة",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Recenter on Basra User Location (30.5, 47.78)
            SmallFloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomLevel = 1.0f
                },
                containerColor = TaxiYellow,
                contentColor = Color.Black,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "موقعي الحالي في البصرة",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Basra location badge at top left
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = TaxiDarkCard.copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, TaxiBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(TaxiSuccessGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "البصرة • 30.50, 47.78",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Custom high-performance Basra Vector Map Canvas.
 * Renders real Basra geography: Shatt Al-Arab waterway, bridges, major Basra streets,
 * animated radar for current user location (30.5, 47.78), and 5 moving yellow taxi markers.
 */
@Composable
fun BasraVectorMap(
    userLat: Double,
    userLng: Double,
    drivers: List<Driver>,
    assignedDriver: Driver?,
    rideStatus: RideStatus,
    zoomLevel: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    onPanAndZoom: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onPanAndZoom(pan.x, pan.y, zoom)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = (width / 2f) + panOffsetX
        val centerY = (height / 2f) + panOffsetY

        // Coordinate projection relative to Basra center (30.5000, 47.7800)
        // 0.01 deg in Basra ~ 1.1 km
        val baseScale = (width.coerceAtMost(height) / 0.07f) * zoomLevel

        fun toScreenOffset(lat: Double, lng: Double): Offset {
            val dLat = lat - userLat
            val dLng = lng - userLng
            val x = centerX + (dLng * baseScale * 0.95f).toFloat()
            val y = centerY - (dLat * baseScale * 1.15f).toFloat()
            return Offset(x, y)
        }

        // 1. Draw Basra City Grid & Districts Background
        drawBasraDistricts(this, ::toScreenOffset)

        // 2. Draw Shatt Al-Arab River (شط العرب)
        drawShattAlArab(this, ::toScreenOffset)

        // 3. Draw Major Basra Highways & Streets
        drawBasraRoads(this, ::toScreenOffset)

        // 4. If a ride is active ("السائق قادم"), draw route line from assigned driver to user
        if (assignedDriver != null && (rideStatus == RideStatus.DRIVER_COMING || rideStatus == RideStatus.SEARCHING)) {
            val driverPos = toScreenOffset(assignedDriver.lat, assignedDriver.lng)
            val userPos = toScreenOffset(userLat, userLng)

            // Outer route glow
            drawLine(
                color = TaxiYellow.copy(alpha = 0.35f),
                start = driverPos,
                end = userPos,
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            // Dashed taxi navigation line
            drawLine(
                color = TaxiYellow,
                start = driverPos,
                end = userPos,
                strokeWidth = 6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f),
                cap = StrokeCap.Round
            )
        }

        // 5. Draw 5 Moving Yellow Taxi Markers
        drivers.forEach { driver ->
            val isAssigned = (driver.id == assignedDriver?.id)
            val pos = toScreenOffset(driver.lat, driver.lng)
            drawTaxiCabMarker(this, pos, driver, isAssigned)
        }

        // 6. Draw User Location Beacon at Basra (30.5, 47.78)
        val userPos = toScreenOffset(userLat, userLng)

        // Animated Radar pulse
        drawCircle(
            color = TaxiYellow.copy(alpha = pulseAlpha),
            radius = pulseRadius * zoomLevel.coerceIn(0.8f, 1.6f),
            center = userPos,
            style = Stroke(width = 3f)
        )

        // Outer halo
        drawCircle(
            color = Color(0x33FFD600),
            radius = 18f,
            center = userPos
        )

        // User Pin core
        drawCircle(
            color = Color.Black,
            radius = 11f,
            center = userPos
        )
        drawCircle(
            color = TaxiYellow,
            radius = 8f,
            center = userPos
        )
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = userPos
        )

        // User Label Tag
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
        }
        drawContext.canvas.nativeCanvas.drawText("موقعي الحالي", userPos.x, userPos.y + 36f, paint)
    }
}

private fun drawBasraDistricts(scope: DrawScope, toScreen: (Double, Double) -> Offset) {
    val textPaint = Paint().apply {
        color = android.graphics.Color.argb(90, 200, 200, 200)
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // Basra major landmark labels
    val districts = listOf(
        Pair("العشار", toScreen(30.508, 47.795)),
        Pair("الجزائر", toScreen(30.492, 47.770)),
        Pair("كورنيش شط العرب", toScreen(30.518, 47.818)),
        Pair("تايم سكوير", toScreen(30.512, 47.765)),
        Pair("باب الزبير", toScreen(30.485, 47.802)),
        Pair("المعقل", toScreen(30.535, 47.775)),
        Pair("الطويسة", toScreen(30.505, 47.778))
    )

    districts.forEach { (name, pos) ->
        scope.drawContext.canvas.nativeCanvas.drawText(name, pos.x, pos.y, textPaint)
    }
}

private fun drawShattAlArab(scope: DrawScope, toScreen: (Double, Double) -> Offset) {
    // Shatt Al-Arab water curve coordinates
    val riverPath = Path()
    val riverPoints = listOf(
        toScreen(30.550, 47.740),
        toScreen(30.535, 47.765),
        toScreen(30.518, 47.810),
        toScreen(30.495, 47.842),
        toScreen(30.465, 47.875)
    )

    riverPath.moveTo(riverPoints.first().x, riverPoints.first().y)
    for (i in 1 until riverPoints.size) {
        val prev = riverPoints[i - 1]
        val curr = riverPoints[i]
        val midX = (prev.x + curr.x) / 2
        val midY = (prev.y + curr.y) / 2
        riverPath.quadraticTo(prev.x, prev.y, midX, midY)
    }
    riverPath.lineTo(riverPoints.last().x, riverPoints.last().y)

    // Draw wide river body
    scope.drawPath(
        path = riverPath,
        color = Color(0xFF1B3854),
        style = Stroke(width = 44f, cap = StrokeCap.Round)
    )
    scope.drawPath(
        path = riverPath,
        color = Color(0xFF24486D),
        style = Stroke(width = 28f, cap = StrokeCap.Round)
    )

    // Bridges over Shatt Al-Arab
    val bridge1Start = toScreen(30.519, 47.805)
    val bridge1End = toScreen(30.517, 47.820)
    scope.drawLine(
        color = Color(0xFFD4AF37),
        start = bridge1Start,
        end = bridge1End,
        strokeWidth = 6f
    )
}

private fun drawBasraRoads(scope: DrawScope, toScreen: (Double, Double) -> Offset) {
    val roadColorSecondary = Color(0xFF23242A)
    val roadColorMain = Color(0xFF333540)
    val roadColorHighlight = Color(0xFF424452)

    // Corniche Road along river
    scope.drawLine(
        color = roadColorHighlight,
        start = toScreen(30.540, 47.770),
        end = toScreen(30.490, 47.830),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Baghdad-Basra Highway
    scope.drawLine(
        color = roadColorMain,
        start = toScreen(30.550, 47.740),
        end = toScreen(30.480, 47.760),
        strokeWidth = 9f,
        cap = StrokeCap.Round
    )

    // 14th July Street / Al-Jaza'er main avenue
    scope.drawLine(
        color = roadColorMain,
        start = toScreen(30.505, 47.740),
        end = toScreen(30.500, 47.810),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Bab Al-Zubair road
    scope.drawLine(
        color = roadColorSecondary,
        start = toScreen(30.470, 47.790),
        end = toScreen(30.520, 47.790),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
}

private fun drawTaxiCabMarker(
    scope: DrawScope,
    center: Offset,
    driver: Driver,
    isAssigned: Boolean
) {
    val markerRadius = if (isAssigned) 18f else 14f

    // Glow if assigned
    if (isAssigned) {
        scope.drawCircle(
            color = TaxiYellow.copy(alpha = 0.4f),
            radius = markerRadius + 12f,
            center = center
        )
    }

    // Outer dark ring
    scope.drawCircle(
        color = Color(0xFF0F0F0F),
        radius = markerRadius + 3f,
        center = center
    )

    // Vibrant Yellow Taxi Body
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF176), TaxiYellow, TaxiYellowDark),
            center = center,
            radius = markerRadius
        ),
        radius = markerRadius,
        center = center
    )

    // Mini Taxi cab black roof symbol rotated according to heading
    scope.rotate(degrees = driver.heading, pivot = center) {
        // Front arrow / windshield
        val carBody = Path().apply {
            moveTo(center.x, center.y - markerRadius * 0.7f)
            lineTo(center.x + markerRadius * 0.45f, center.y + markerRadius * 0.5f)
            lineTo(center.x - markerRadius * 0.45f, center.y + markerRadius * 0.5f)
            close()
        }
        scope.drawPath(path = carBody, color = Color.Black)
    }

    // Driver name tag
    val namePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 22f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
    }
    val shortName = driver.name.split(" ").firstOrNull() ?: driver.name
    scope.drawContext.canvas.nativeCanvas.drawText(
        shortName,
        center.x,
        center.y - markerRadius - 8f,
        namePaint
    )
}

/**
 * Interactive Web-based Map view showing OpenStreetMap/Google Carto tiles for Basra (30.5, 47.78).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BasraWebViewMap(
    userLat: Double,
    userLng: Double,
    drivers: List<Driver>,
    assignedDriver: Driver?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Update markers on the WebView when drivers move
    LaunchedEffect(drivers, assignedDriver) {
        webViewRef?.let { webView ->
            val driversJson = StringBuilder("[")
            drivers.forEachIndexed { index, d ->
                driversJson.append("{\"id\":\"${d.id}\",\"name\":\"${d.name}\",\"lat\":${d.lat},\"lng\":${d.lng},\"heading\":${d.heading},\"isAssigned\":${d.id == assignedDriver?.id}}")
                if (index < drivers.size - 1) driversJson.append(",")
            }
            driversJson.append("]")
            webView.evaluateJavascript("if (window.updateDrivers) { window.updateDrivers($driversJson); }", null)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = WebViewClient()

                val html = """
                    <!DOCTYPE html>
                    <html dir="rtl" lang="ar">
                    <head>
                        <meta charset="utf-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #121212; }
                            .taxi-pin {
                                background: #FFD600;
                                border: 2px solid #000;
                                border-radius: 50%;
                                width: 28px;
                                height: 28px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                box-shadow: 0 0 10px rgba(255,214,0,0.8);
                                font-weight: bold;
                                font-size: 13px;
                            }
                            .user-pin {
                                background: #00E676;
                                border: 3px solid #FFF;
                                border-radius: 50%;
                                width: 22px;
                                height: 22px;
                                box-shadow: 0 0 14px #00E676;
                            }
                            .driver-label {
                                background: rgba(0,0,0,0.8);
                                color: #FFD600;
                                font-size: 11px;
                                padding: 2px 6px;
                                border-radius: 4px;
                                white-space: nowrap;
                                border: 1px solid #FFD600;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="map"></div>
                        <script>
                            var map = L.map('map', { zoomControl: false }).setView([$userLat, $userLng], 14);
                            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                                maxZoom: 19,
                                attribution: 'Nexa Taxi Basra'
                            }).addTo(map);

                            var userIcon = L.divIcon({
                                className: 'user-marker',
                                html: '<div class="user-pin"></div>',
                                iconSize: [22, 22],
                                iconAnchor: [11, 11]
                            });
                            var userMarker = L.marker([$userLat, $userLng], { icon: userIcon }).addTo(map);
                            userMarker.bindPopup('<b>موقعي الحالي في البصرة</b><br>30.50, 47.78').openPopup();

                            var driverMarkers = {};

                            window.updateDrivers = function(drivers) {
                                drivers.forEach(function(d) {
                                    var iconHtml = '<div class="taxi-pin" style="transform: rotate(' + d.heading + 'deg)">🚕</div>' +
                                                   '<div class="driver-label">' + d.name + '</div>';
                                    var taxiIcon = L.divIcon({
                                        className: 'taxi-marker',
                                        html: iconHtml,
                                        iconSize: [30, 48],
                                        iconAnchor: [15, 24]
                                    });

                                    if (driverMarkers[d.id]) {
                                        driverMarkers[d.id].setLatLng([d.lat, d.lng]);
                                        driverMarkers[d.id].setIcon(taxiIcon);
                                    } else {
                                        driverMarkers[d.id] = L.marker([d.lat, d.lng], { icon: taxiIcon }).addTo(map);
                                    }
                                });
                            };
                        </script>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL("https://basra.nexataxi.local", html, "text/html", "utf-8", null)
                webViewRef = this
            }
        },
        modifier = modifier
    )
}
