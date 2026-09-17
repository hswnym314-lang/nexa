package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BasraMapView
import com.example.ui.components.NexaTopBar
import com.example.ui.components.RideControlPanel
import com.example.ui.theme.TaxiBlackDark
import com.example.ui.theme.TaxiBorder
import com.example.ui.theme.TaxiDarkCard
import com.example.ui.theme.TaxiYellow
import com.example.ui.viewmodel.TaxiViewModel

@Composable
fun NexaTaxiHomeScreen(
    viewModel: TaxiViewModel = viewModel()
) {
    val drivers by viewModel.drivers.collectAsState()
    val rideStatus by viewModel.rideStatus.collectAsState()
    val assignedDriver by viewModel.assignedDriver.collectAsState()
    val isServerOnline by viewModel.isServerOnline.collectAsState()
    val serverMessage by viewModel.serverMessage.collectAsState()
    val distanceMeters by viewModel.distanceMeters.collectAsState()
    val etaSeconds by viewModel.etaSeconds.collectAsState()
    val selectedDestination by viewModel.selectedDestination.collectAsState()

    // Enforce Arabic RTL layout direction for natural Iraqi UX
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = TaxiBlackDark,
            topBar = {
                NexaTopBar(
                    isServerOnline = isServerOnline,
                    serverMessage = serverMessage,
                    onRetryServer = { viewModel.retryServer() }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Basra Map Component (Shows current location at 30.5, 47.78 and 5 moving drivers)
                BasraMapView(
                    userLat = 30.5000,
                    userLng = 47.7800,
                    drivers = drivers,
                    assignedDriver = assignedDriver,
                    rideStatus = rideStatus,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Server info pill notification
                AnimatedVisibility(
                    visible = !serverMessage.isNullOrEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TaxiDarkCard.copy(alpha = 0.94f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TaxiBorder),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TaxiYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = serverMessage ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Bottom Ride Control Panel with big "اطلب تاكسي الان" button and live driver card
                RideControlPanel(
                    rideStatus = rideStatus,
                    assignedDriver = assignedDriver,
                    distanceMeters = distanceMeters,
                    etaSeconds = etaSeconds,
                    selectedDestination = selectedDestination,
                    onSelectDestination = { viewModel.selectDestination(it) },
                    onRequestTaxi = { viewModel.requestTaxi() },
                    onCancelRide = { viewModel.cancelRide() },
                    onCompleteRide = { viewModel.completeRide() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
