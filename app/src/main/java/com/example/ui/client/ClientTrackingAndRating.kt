package com.example.ui.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.ui.components.OficiosButton
import com.example.ui.components.OficiosOutlinedButton
import com.example.ui.components.RatingStars
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundSoft
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

data class StepItem(
    val status: ServiceStatus,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientTrackingScreen(viewModel: OficiosViewModel) {
    val req by viewModel.trackingRequest.collectAsState()

    if (req == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay servicio activo para seguimiento.")
        }
        return
    }

    val request = req!!

    val steps = listOf(
        StepItem(ServiceStatus.PENDING, "1. Solicitud enviada", "Tu solicitud fue registrada en el sistema.", Icons.Default.Send),
        StepItem(ServiceStatus.ACCEPTED, "2. Esperando respuesta / Aceptado", "${request.professionalName} revisa y confirma la cita.", Icons.Default.HourglassTop),
        StepItem(ServiceStatus.ON_THE_WAY, "3. Profesional en camino", "El profesional se desplaza a tu dirección.", Icons.Default.Navigation),
        StepItem(ServiceStatus.IN_PROGRESS, "4. Servicio en progreso", "Se realizan los trabajos en tu domicilio.", Icons.Default.Build),
        StepItem(ServiceStatus.COMPLETED, "5. Servicio finalizado", "Trabajo completado con satisfacción.", Icons.Default.CheckCircle)
    )

    // Current step index calculation
    val currentStepIndex = when (request.status) {
        ServiceStatus.PENDING -> 0
        ServiceStatus.ACCEPTED -> 1
        ServiceStatus.ON_THE_WAY -> 2
        ServiceStatus.IN_PROGRESS -> 3
        ServiceStatus.COMPLETED -> 4
        ServiceStatus.CANCELLED -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Seguimiento",
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
            // Header card de estado actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Estado del servicio",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = request.status)
                        Text(
                            text = request.scheduledDate,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (request.status) {
                            ServiceStatus.PENDING -> "${request.professionalName} está revisando tu solicitud."
                            ServiceStatus.ACCEPTED -> "${request.professionalName} aceptó tu solicitud y confirmó la cita."
                            ServiceStatus.ON_THE_WAY -> "¡${request.professionalName} va en camino hacia tu ubicación!"
                            ServiceStatus.IN_PROGRESS -> "El trabajo de ${request.categoryName} está siendo realizado."
                            ServiceStatus.COMPLETED -> "El servicio ha concluido exitosamente."
                            ServiceStatus.CANCELLED -> "Esta solicitud ha sido cancelada."
                        },
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            }

            // Timeline de estados
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Línea de estados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    steps.forEachIndexed { index, step ->
                        val isDone = index <= currentStepIndex
                        val isCurrent = index == currentStepIndex

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier.size(34.dp),
                                    shape = CircleShape,
                                    color = when {
                                        isCurrent -> AccentCyan
                                        isDone -> SecondaryTeal
                                        else -> Color(0xFFE5ECEF)
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = if (isDone || isCurrent) PrimaryNavy else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(28.dp)
                                            .background(
                                                if (index < currentStepIndex) SecondaryTeal else Color(0xFFE2EDF1)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 14.dp else 0.dp)) {
                                Text(
                                    text = step.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isDone) PrimaryNavy else TextSecondary
                                )
                                Text(
                                    text = step.description,
                                    fontSize = 12.sp,
                                    color = if (isCurrent) TextPrimary else TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Información del profesional y servicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Detalles del servicio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            initial = request.professionalName.firstOrNull()?.toString() ?: "P",
                            size = 46.dp,
                            backgroundColor = PrimaryNavy,
                            textColor = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = request.professionalName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            Text(text = request.professionalProfession, fontSize = 13.sp, color = SecondaryTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2EDF1)))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Servicio: ${request.categoryName}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryNavy)
                    Text(text = "Ubicación: ${request.location}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Problema: ${request.description}", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // Botón de prueba para avanzar estado o finalizar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6F2F5))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Simulación de flujo (Prototipo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Permite probar los cambios de estado hasta la finalización.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OficiosOutlinedButton(
                            text = "Avanzar estado",
                            onClick = {
                                viewModel.advanceServiceStatus(request.id)
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "advance_status_btn"
                        )

                        OficiosButton(
                            text = "Finalizar y calificar",
                            onClick = {
                                viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.COMPLETED)
                                viewModel.navigateTo(Screen.ClientRating)
                            },
                            modifier = Modifier.weight(1.3f),
                            backgroundColor = PrimaryNavy,
                            testTag = "finish_and_rate_btn"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRatingScreen(viewModel: OficiosViewModel) {
    val req by viewModel.trackingRequest.collectAsState()
    var selectedStars by remember { mutableDoubleStateOf(5.0) }
    var reviewComment by remember { mutableStateOf("Excelente servicio, muy puntual y resolvió el problema rápidamente.") }

    val request = req ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Calificar servicio",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            UserAvatar(
                initial = request.professionalName.firstOrNull()?.toString() ?: "P",
                size = 72.dp,
                backgroundColor = PrimaryNavy,
                textColor = AccentCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = request.professionalName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )

            Text(
                text = "Servicio de ${request.categoryName}",
                fontSize = 14.sp,
                color = SecondaryTeal
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Cómo fue tu experiencia?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Text(
                        text = "Toca las estrellas para calificar",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    RatingStars(
                        rating = selectedStars,
                        maxStars = 5,
                        starSize = 36.dp,
                        interactive = true,
                        onRatingChanged = { newRating ->
                            selectedStars = newRating.toDouble()
                        },
                        modifier = Modifier.testTag("rating_stars_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("Cuéntanos cómo fue el servicio...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("review_comment_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OficiosButton(
                text = "Enviar calificación",
                onClick = {
                    viewModel.submitRatingAndReview(request.id, selectedStars, reviewComment)
                },
                enabled = selectedStars > 0,
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_rating_btn"
            )
        }
    }
}
