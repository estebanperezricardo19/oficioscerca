package com.example.ui.client

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Professional
import com.example.ui.components.OficiosButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.OficiosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMapScreen(viewModel: OficiosViewModel) {
    val professionals by viewModel.professionals.collectAsState()
    var selectedProOnMap by remember { mutableStateOf<Professional?>(professionals.firstOrNull()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map Canvas simulation with streets, grid lines, and markers
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5ECEF))
        ) {
            val w = size.width
            val h = size.height

            // Simulated map roads & river in Medellín
            drawLine(
                color = Color(0xFFD0DCE0),
                start = Offset(0f, h * 0.35f),
                end = Offset(w, h * 0.45f),
                strokeWidth = 24f
            )
            drawLine(
                color = Color(0xFFD0DCE0),
                start = Offset(w * 0.25f, 0f),
                end = Offset(w * 0.4f, h),
                strokeWidth = 20f
            )
            drawLine(
                color = Color(0xFFD0DCE0),
                start = Offset(w * 0.7f, 0f),
                end = Offset(w * 0.6f, h),
                strokeWidth = 16f
            )
            drawLine(
                color = Color(0xFFD0DCE0),
                start = Offset(0f, h * 0.65f),
                end = Offset(w, h * 0.6f),
                strokeWidth = 22f
            )

            // Park green block
            drawRect(
                color = Color(0xFFD8EDD9),
                topLeft = Offset(w * 0.45f, h * 0.2f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.15f)
            )

            // Water canal
            drawLine(
                color = Color(0xFFBCE6EE),
                start = Offset(w * 0.85f, 0f),
                end = Offset(w * 0.8f, h),
                strokeWidth = 32f
            )
        }

        // Top App Bar overlaid on map
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Profesionales cercanos",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Laureles y alrededores • Medellín",
                        fontSize = 12.sp,
                        color = SecondaryTeal
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = PrimaryNavy
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceWhite.copy(alpha = 0.95f)
            )
        )

        // User Location Pin
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = Color(0xFF2563EB).copy(alpha = 0.25f)
            ) {}
            Surface(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                color = Color(0xFF2563EB),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {}
        }

        // Professional Map Markers (Positions distributed across map canvas)
        val markerPositions = listOf(
            Pair(-100, -120), // Mateo
            Pair(80, -90),    // Julián
            Pair(-70, 70),    // Andrés
            Pair(90, 80),     // Sofía
            Pair(-110, 0),    // Carlos
            Pair(40, -160)    // Daniel
        )

        professionals.forEachIndexed { index, pro ->
            val pos = markerPositions.getOrElse(index) { Pair(0, 0) }
            val isSelected = selectedProOnMap?.id == pro.id

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = pos.first.dp, y = pos.second.dp)
                    .clickable { selectedProOnMap = pro }
                    .testTag("map_marker_${pro.id}")
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentCyan else PrimaryNavy,
                    shadowElevation = if (isSelected) 8.dp else 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isSelected) PrimaryNavy else AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pro.name.split(" ").first(),
                            color = if (isSelected) PrimaryNavy else SurfaceWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recenter FAB
        FloatingActionButton(
            onClick = { /* Simulated GPS recenter */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 160.dp, end = 20.dp),
            containerColor = SurfaceWhite,
            contentColor = PrimaryNavy
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
        }

        // Selected Worker Popup Card
        selectedProOnMap?.let { pro ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("map_selected_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            initial = pro.avatarInitial,
                            size = 50.dp,
                            backgroundColor = PrimaryNavy,
                            textColor = AccentCyan
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pro.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Text(
                                text = pro.profession,
                                fontSize = 13.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = StarGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = pro.rating.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "•", color = TextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${pro.distanceKm} km de ti",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OficiosButton(
                        text = "Ver perfil completo",
                        onClick = {
                            viewModel.openProfessionalDetail(pro.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "map_view_profile_btn"
                    )
                }
            }
        }
    }
}
