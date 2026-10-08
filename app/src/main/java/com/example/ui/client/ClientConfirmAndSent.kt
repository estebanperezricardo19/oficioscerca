package com.example.ui.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OficiosButton
import com.example.ui.components.OficiosOutlinedButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientConfirmRequestScreen(viewModel: OficiosViewModel) {
    val pro by viewModel.selectedProfessional.collectAsState()
    val desc by viewModel.draftDescription.collectAsState()
    val date by viewModel.draftDate.collectAsState()
    val time by viewModel.draftTime.collectAsState()
    val loc by viewModel.draftLocation.collectAsState()

    val professional = pro ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Confirmar solicitud",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resumen del servicio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            initial = professional.avatarInitial,
                            size = 44.dp,
                            backgroundColor = PrimaryNavy,
                            textColor = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Profesional:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = professional.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Text(
                                text = professional.profession,
                                fontSize = 13.sp,
                                color = SecondaryTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE2EDF1))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Problema o requerimiento:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = desc,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Fecha:", fontSize = 12.sp, color = TextSecondary)
                            Text(text = date, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                        }
                        Column {
                            Text(text = "Hora:", fontSize = 12.sp, color = TextSecondary)
                            Text(text = time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Ubicación:", fontSize = 12.sp, color = TextSecondary)
                    Text(text = loc, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                }
            }

            // Aviso informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6F2F5))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "El trabajador confirmará la tarifa estimada directamente contigo antes de iniciar. No se te cobrará nada ahora.",
                        fontSize = 13.sp,
                        color = PrimaryNavy,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Botón Enviar solicitud
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceWhite,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                OficiosButton(
                    text = "Enviar solicitud",
                    onClick = {
                        viewModel.confirmDraftAndSend()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "send_request_confirm_btn"
                )
            }
        }
    }
}

@Composable
fun ClientRequestSentScreen(viewModel: OficiosViewModel) {
    val pro by viewModel.selectedProfessional.collectAsState()
    val activeTrackingId by viewModel.activeTrackingRequestId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = Color(0xFFD1FAE5)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¡Solicitud enviada!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryNavy
        )

        Text(
            text = "Tu solicitud fue enviada a ${pro?.name ?: "el profesional"}.",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "Estado: Esperando respuesta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = pro?.name ?: "Mateo Ramírez",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
                Text(
                    text = "Servicio de ${pro?.profession ?: "Plomería"}",
                    fontSize = 14.sp,
                    color = SecondaryTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OficiosButton(
            text = "Ver solicitud",
            onClick = {
                activeTrackingId?.let { id ->
                    viewModel.openTracking(id)
                } ?: viewModel.navigateTo(Screen.ClientHistory)
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "view_sent_request_btn"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OficiosOutlinedButton(
            text = "Volver al inicio",
            onClick = {
                viewModel.navigateTo(Screen.ClientHome)
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "back_home_after_sent_btn"
        )
    }
}
