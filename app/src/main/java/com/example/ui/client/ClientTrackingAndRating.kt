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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.oficiosTextFieldColors
import com.example.viewmodel.ClientActionUiState
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.ReviewActionUiState
import com.example.viewmodel.Screen

data class StepItem(
    val status: ServiceStatus,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryNavy
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientTrackingScreen(viewModel: OficiosViewModel) {
    val req by viewModel.trackingRequest.collectAsState()
    val clientActionState by viewModel.clientActionState.collectAsState()
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    if (req == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSoft)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de la solicitud",
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(color = SecondaryTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando información del requerimiento...",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OficiosOutlinedButton(
                        text = "Volver a mis solicitudes",
                        onClick = { viewModel.navigateBack() }
                    )
                }
            }
        }
        return
    }

    val request = req!!

    val steps = listOf(
        StepItem(ServiceStatus.PENDING, "1. Solicitud enviada", "Tu requerimiento fue registrado en el sistema.", Icons.AutoMirrored.Filled.Send),
        StepItem(ServiceStatus.ACCEPTED, "2. Solicitud aceptada", "${request.professionalName} confirmó la atención.", Icons.Default.HourglassTop),
        StepItem(ServiceStatus.ON_THE_WAY, "3. Profesional en camino", "El profesional se desplaza a la dirección acordada.", Icons.Default.Navigation),
        StepItem(ServiceStatus.IN_PROGRESS, "4. Servicio en proceso", "Trabajos en desarrollo en tu ubicación.", Icons.Default.Build),
        StepItem(ServiceStatus.COMPLETED, "5. Servicio finalizado", "Trabajo completado satisfactoriamente.", Icons.Default.CheckCircle)
    )

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
                Column {
                    Text(
                        text = "Detalle de la solicitud",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Seguimiento en tiempo real",
                        fontSize = 12.sp,
                        color = TextSecondary
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
            actions = {
                IconButton(
                    onClick = { viewModel.refreshCurrentTrackingRequest() },
                    modifier = Modifier.testTag("refresh_tracking_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar estado",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado del servicio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        StatusBadge(status = request.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusMsg = when (request.status) {
                        ServiceStatus.PENDING -> "Tu requerimiento ha sido registrado. ${request.professionalName} revisará los detalles y confirmará la cita."
                        ServiceStatus.ACCEPTED -> "${request.professionalName} ha aceptado tu solicitud de servicio."
                        ServiceStatus.ON_THE_WAY -> "¡${request.professionalName} va en camino hacia la dirección acordada!"
                        ServiceStatus.IN_PROGRESS -> "El servicio de ${request.categoryName} está siendo realizado."
                        ServiceStatus.COMPLETED -> "El servicio ha finalizado con éxito."
                        ServiceStatus.CANCELLED -> "Esta solicitud ha sido cancelada."
                    }

                    Text(
                        text = statusMsg,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Timeline de estados o tarjeta de cancelación
            if (request.status == ServiceStatus.CANCELLED) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Solicitud cancelada",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Esta solicitud no generará cargos. Puedes realizar una nueva solicitud si lo requieres.",
                                fontSize = 13.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            } else {
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
                        text = "Profesional asignado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            initial = request.professionalName.firstOrNull()?.toString() ?: "P",
                            size = 50.dp,
                            backgroundColor = PrimaryNavy,
                            textColor = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = request.professionalName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Text(
                                text = request.professionalProfession,
                                fontSize = 13.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2EDF1)))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Detalles del requerimiento",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailItemRow(label = "Oficio / Servicio", value = request.categoryName)
                    DetailItemRow(label = "Ubicación del trabajo", value = request.location)
                    DetailItemRow(label = "Fecha de registro", value = formatDisplayDate(request.createdAt))
                    if (request.scheduledDate.isNotBlank()) {
                        DetailItemRow(label = "Fecha programada", value = formatDisplayDate(request.scheduledDate))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Descripción de la solicitud:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = request.description,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Opciones de acciones según estado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (request.status == ServiceStatus.COMPLETED) {
                        if (request.ratingGiven != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "¡Servicio calificado!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            RatingStars(
                                rating = request.ratingGiven,
                                maxStars = 5,
                                starSize = 20.dp
                            )
                            if (!request.reviewComment.isNullOrBlank()) {
                                Text(
                                    text = "\"${request.reviewComment}\"",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        } else {
                            Text(
                                text = "El servicio ha finalizado. Tu opinión nos ayuda a mantener profesionales de confianza.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            OficiosButton(
                                text = "Calificar servicio",
                                onClick = { viewModel.navigateTo(Screen.ClientRating) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "rate_service_btn"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else if (request.status != ServiceStatus.CANCELLED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEBF4F6), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "El profesional actualiza los avances del servicio conforme acude a la cita. Puedes refrescar para comprobar cambios de estado.",
                                fontSize = 12.sp,
                                color = PrimaryNavy,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (clientActionState is ClientActionUiState.Error) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = (clientActionState as ClientActionUiState.Error).message,
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (request.status == ServiceStatus.PENDING) {
                        val isCancelling = clientActionState is ClientActionUiState.Loading
                        OficiosOutlinedButton(
                            text = if (isCancelling) "Cancelando..." else "Cancelar solicitud",
                            onClick = { showCancelConfirmDialog = true },
                            enabled = !isCancelling,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "cancel_client_request_btn"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OficiosOutlinedButton(
                            text = "Actualizar estado",
                            onClick = { viewModel.refreshCurrentTrackingRequest() },
                            modifier = Modifier.weight(1f),
                            testTag = "refresh_status_btn"
                        )

                        OficiosButton(
                            text = "Volver a solicitudes",
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.weight(1f),
                            backgroundColor = PrimaryNavy,
                            testTag = "back_to_requests_btn"
                        )
                    }
                }
            }
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = {
                Text(
                    text = "Cancelar solicitud",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cancelar esta solicitud pendiente con ${request.professionalName}?",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmDialog = false
                        viewModel.cancelClientRequest(request.id)
                    },
                    modifier = Modifier.testTag("confirm_cancel_request_btn")
                ) {
                    Text("Sí, cancelar", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Mantener solicitud", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRatingScreen(viewModel: OficiosViewModel) {
    val req by viewModel.trackingRequest.collectAsState()
    val reviewActionState by viewModel.reviewActionState.collectAsState()
    var selectedStars by remember { mutableDoubleStateOf(5.0) }
    var reviewComment by remember { mutableStateOf("") }

    val request = req ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSoft),
            contentAlignment = Alignment.Center
        ) {
            OficiosOutlinedButton(
                text = "Volver",
                onClick = { viewModel.navigateBack() }
            )
        }
        return
    }

    val isSubmitting = reviewActionState is ReviewActionUiState.Loading
    val canRate = request.status == ServiceStatus.COMPLETED && request.ratingGiven == null

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

            Spacer(modifier = Modifier.height(24.dp))

            if (!canRate) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (request.ratingGiven != null) {
                                "Este servicio ya fue calificado con ${request.ratingGiven} ★."
                            } else {
                                "Solo puedes calificar servicios que ya hayan sido finalizados."
                            },
                            fontSize = 14.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OficiosOutlinedButton(
                            text = "Volver",
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
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
                            interactive = !isSubmitting,
                            onRatingChanged = { newRating ->
                                selectedStars = newRating.toDouble()
                                if (reviewActionState is ReviewActionUiState.Error) {
                                    viewModel.resetReviewActionState()
                                }
                            },
                            modifier = Modifier.testTag("rating_stars_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = {
                                reviewComment = it
                                if (reviewActionState is ReviewActionUiState.Error) {
                                    viewModel.resetReviewActionState()
                                }
                            },
                            placeholder = { Text("Cuéntanos cómo fue el servicio (opcional)...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("review_comment_input"),
                            colors = oficiosTextFieldColors()
                        )
                    }
                }

                if (reviewActionState is ReviewActionUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (reviewActionState as ReviewActionUiState.Error).message,
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                OficiosButton(
                    text = if (isSubmitting) "Enviando calificación..." else "Enviar calificación",
                    onClick = {
                        viewModel.submitRatingAndReview(request.id, selectedStars, reviewComment)
                    },
                    enabled = selectedStars > 0 && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_rating_btn"
                )
            }
        }
    }
}
