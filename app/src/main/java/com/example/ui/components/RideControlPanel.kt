package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Driver
import com.example.data.model.RideStatus
import com.example.ui.theme.TaxiBlack
import com.example.ui.theme.TaxiBorder
import com.example.ui.theme.TaxiDarkCard
import com.example.ui.theme.TaxiDarkSurface
import com.example.ui.theme.TaxiErrorRed
import com.example.ui.theme.TaxiSuccessGreen
import com.example.ui.theme.TaxiTextPrimary
import com.example.ui.theme.TaxiTextSecondary
import com.example.ui.theme.TaxiYellow
import com.example.ui.theme.TaxiYellowDark

val basraDestinations = listOf(
    "كورنيش شط العرب",
    "تايم سكوير البصرة",
    "العشار - شارع الوطني",
    "مطار البصرة الدولي",
    "المدينة الرياضية (جذع النخلة)",
    "جامعة البصرة - باب الزبير",
    "حي الجزائر",
    "المعقل"
)

@Composable
fun RideControlPanel(
    rideStatus: RideStatus,
    assignedDriver: Driver?,
    distanceMeters: Int,
    etaSeconds: Int,
    selectedDestination: String,
    onSelectDestination: (String) -> Unit,
    onRequestTaxi: () -> Unit,
    onCancelRide: () -> Unit,
    onCompleteRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = TaxiDarkSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, TaxiBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .background(TaxiBorder, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = rideStatus,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "RidePanelState"
            ) { status ->
                when (status) {
                    RideStatus.IDLE -> {
                        IdleRideState(
                            selectedDestination = selectedDestination,
                            onSelectDestination = onSelectDestination,
                            onRequestTaxi = onRequestTaxi
                        )
                    }
                    RideStatus.SEARCHING -> {
                        SearchingRideState(onCancelRide = onCancelRide)
                    }
                    RideStatus.DRIVER_COMING -> {
                        DriverComingState(
                            driver = assignedDriver,
                            distanceMeters = distanceMeters,
                            etaSeconds = etaSeconds,
                            onCancelRide = onCancelRide
                        )
                    }
                    RideStatus.DRIVER_ARRIVED -> {
                        DriverArrivedState(
                            driver = assignedDriver,
                            onCompleteRide = onCompleteRide
                        )
                    }
                    RideStatus.RIDE_IN_PROGRESS -> {
                        RideInProgressState(
                            driver = assignedDriver,
                            onCompleteRide = onCompleteRide
                        )
                    }
                    RideStatus.COMPLETED -> {
                        RideCompletedState()
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleRideState(
    selectedDestination: String,
    onSelectDestination: (String) -> Unit,
    onRequestTaxi: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Location header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TaxiYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TaxiYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "نقطة الانطلاق: موقعك الحالي (البصرة)",
                    color = TaxiTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إلى: $selectedDestination",
                    color = TaxiYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Basra landmark destinations chips
        Text(
            text = "اختر الوجهة في البصرة:",
            color = TaxiTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            basraDestinations.forEach { dest ->
                val isSelected = (dest == selectedDestination)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TaxiYellow else TaxiDarkCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) TaxiYellowDark else TaxiBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectDestination(dest) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dest,
                        color = if (isSelected) Color.Black else TaxiTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fare and Available Taxis Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaxiDarkCard, RoundedCornerShape(14.dp))
                .border(1.dp, TaxiBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = TaxiYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "تاكسي نكسا البصرة",
                        color = TaxiTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "5 كباتن جاهزين بالقرب منك",
                        color = TaxiSuccessGreen,
                        fontSize = 11.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "3,000 د.ع",
                    color = TaxiYellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "وصول خلال 2-3 دقائق",
                    color = TaxiTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BIG PROMINENT "اطلب تاكسي الان" BUTTON
        Button(
            onClick = onRequestTaxi,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("order_taxi_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TaxiYellow,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "اطلب تاكسي الان",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun SearchingRideState(onCancelRide: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = TaxiYellow,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "جاري البحث عن أقرب تاكسي في شوارع البصرة...",
            color = TaxiTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "يتم الاتصال بسيرفر Nexa Taxi وتعيين الكابتن",
            color = TaxiTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onCancelRide,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TaxiErrorRed)
        ) {
            Text("إلغاء الطلب")
        }
    }
}

@Composable
private fun DriverComingState(
    driver: Driver?,
    distanceMeters: Int,
    etaSeconds: Int,
    onCancelRide: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Status header banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaxiYellow.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, TaxiYellow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(TaxiYellow, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "السائق قادم 🚕",
                    color = TaxiYellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "${(distanceMeters).coerceAtLeast(20)} م • ${etaSeconds / 60} د و ${etaSeconds % 60} ث",
                color = TaxiTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Driver details card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaxiDarkCard, RoundedCornerShape(16.dp))
                .border(1.dp, TaxiBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Driver Avatar / Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(TaxiYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚕",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = driver?.name ?: "كابتن البصرة",
                        color = TaxiTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = TaxiYellow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${driver?.rating ?: 4.9}",
                            color = TaxiYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = driver?.carModel ?: "كيا ريو صفراء",
                    color = TaxiYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "رقم اللوحة: ${driver?.plateNumber ?: "البصرة 45210 أ"}",
                    color = TaxiTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Call & Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { /* Simulated Call */ },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaxiSuccessGreen,
                    contentColor = Color.Black
                )
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "اتصال", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCancelRide,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TaxiErrorRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, TaxiErrorRed)
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "إلغاء الطلب", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DriverArrivedState(
    driver: Driver?,
    onCompleteRide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = TaxiSuccessGreen,
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "وصل السائق إلى موقعك في البصرة! 🚕",
            color = TaxiYellow,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${driver?.name} بانتظارك في سيارة ${driver?.carModel}",
            color = TaxiTextPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCompleteRide,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TaxiYellow,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "بدء الرحلة الآن",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RideInProgressState(
    driver: Driver?,
    onCompleteRide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "الرحلة جارية في شوارع البصرة 🚕",
            color = TaxiYellow,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "مع الكابتن: ${driver?.name}",
            color = TaxiTextSecondary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCompleteRide,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TaxiYellow,
                contentColor = Color.Black
            )
        ) {
            Text(text = "إنهاء الرحلة", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RideCompletedState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "شكراً لاستخدامكم Nexa Taxi البصرة! ✨",
            color = TaxiYellow,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "نتمنى لك يوماً سعيداً في أم الخير البصرة",
            color = TaxiTextSecondary,
            fontSize = 13.sp
        )
    }
}
