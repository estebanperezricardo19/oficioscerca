package com.example.ui.worker

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Professional
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.ui.components.EmptyStateView
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
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import com.example.viewmodel.ProfileActionUiState
import com.example.viewmodel.WorkerActionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRequestDetailScreen(viewModel: OficiosViewModel) {
    val req by viewModel.workerSelectedRequest.collectAsState()
    val actionState by viewModel.workerActionState.collectAsState()

    if (req == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSoft),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No se encontró la solicitud seleccionada.",
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OficiosButton(
                    text = "Regresar",
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.width(160.dp)
                )
            }
        }
        return
    }

    val request = req!!
    val isActingOnThis = actionState is WorkerActionUiState.Loading &&
            (actionState as WorkerActionUiState.Loading).requestId == request.id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Detalle de solicitud",
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
            actions = {
                IconButton(
                    onClick = { viewModel.refreshSelectedWorkerRequest() },
                    modifier = Modifier.testTag("worker_detail_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
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
            // Action feedback banner if error
            if (actionState is WorkerActionUiState.Error && (actionState as WorkerActionUiState.Error).requestId == request.id) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (actionState as WorkerActionUiState.Error).message,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Cliente Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        initial = request.clientName.firstOrNull()?.toString() ?: "C",
                        size = 52.dp,
                        backgroundColor = SecondaryTeal,
                        textColor = SurfaceWhite
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.clientName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Cliente verificado",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    StatusBadge(status = request.status)
                }
            }

            // Datos de la solicitud
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información del trabajo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    DetailRow(label = "Servicio", value = request.categoryName)
                    DetailRow(label = "Descripción", value = request.description)
                    DetailRow(label = "Ubicación", value = request.location)
                    DetailRow(label = "Fecha programada", value = request.scheduledDate)
                    DetailRow(label = "Hora", value = request.scheduledTime)
                    DetailRow(label = "Teléfono de contacto", value = request.clientPhone.ifBlank { "No disponible" })
                    if (request.createdAt.isNotBlank()) {
                        DetailRow(label = "Fecha de solicitud", value = request.createdAt.take(19).replace("T", " "))
                    }
                }
            }

            // Status explanation banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (request.status) {
                    ServiceStatus.PENDING -> Color(0xFFFEF3C7)
                    ServiceStatus.ACCEPTED -> Color(0xFFE0F7FA)
                    ServiceStatus.ON_THE_WAY, ServiceStatus.IN_PROGRESS -> Color(0xFFE0F2FE)
                    ServiceStatus.COMPLETED -> Color(0xFFD1FAE5)
                    ServiceStatus.CANCELLED -> Color(0xFFF3F4F6)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (request.status) {
                            ServiceStatus.PENDING -> "Esta solicitud está pendiente de tu respuesta. Puedes aceptarla para coordinar con el cliente o rechazarla."
                            ServiceStatus.ACCEPTED -> "Has aceptado esta solicitud. Comunícate con el cliente o inicia el viaje cuando estés listo."
                            ServiceStatus.ON_THE_WAY -> "Has notificado al cliente que estás en camino a su ubicación."
                            ServiceStatus.IN_PROGRESS -> "El trabajo se encuentra actualmente en ejecución."
                            ServiceStatus.COMPLETED -> "Este servicio fue completado con éxito."
                            ServiceStatus.CANCELLED -> "Esta solicitud fue cancelada o rechazada."
                        },
                        fontSize = 13.sp,
                        color = when (request.status) {
                            ServiceStatus.PENDING -> Color(0xFF92400E)
                            ServiceStatus.ACCEPTED -> Color(0xFF0E7490)
                            ServiceStatus.ON_THE_WAY, ServiceStatus.IN_PROGRESS -> Color(0xFF0369A1)
                            ServiceStatus.COMPLETED -> Color(0xFF065F46)
                            ServiceStatus.CANCELLED -> Color(0xFF4B5563)
                        }
                    )
                }
            }
        }

        // Action Buttons
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
                if (isActingOnThis) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SecondaryTeal,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    }
                } else if (request.status == ServiceStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OficiosOutlinedButton(
                            text = "Rechazar solicitud",
                            onClick = {
                                viewModel.rejectWorkerRequest(request.id) {
                                    viewModel.navigateBack()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "detail_reject_btn"
                        )

                        OficiosButton(
                            text = "Aceptar solicitud",
                            onClick = {
                                viewModel.acceptWorkerRequest(request.id) {
                                    viewModel.openWorkerActiveService(request.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "detail_accept_btn"
                        )
                    }
                } else if (request.status != ServiceStatus.CANCELLED) {
                    OficiosButton(
                        text = "Ver servicio activo",
                        onClick = {
                            viewModel.openWorkerActiveService(request.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "go_to_active_service_btn"
                    )
                } else {
                    OficiosOutlinedButton(
                        text = "Solicitud rechazada",
                        onClick = { viewModel.navigateBack() },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "detail_rejected_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryNavy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerActiveServiceScreen(viewModel: OficiosViewModel) {
    val req by viewModel.workerSelectedRequest.collectAsState()
    val actionState by viewModel.workerActionState.collectAsState()

    if (req == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSoft)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Servicio Activo",
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
                Text("No hay servicio activo seleccionado.", color = TextPrimary)
            }
        }
        return
    }

    val request = req!!
    val isActingOnThis = actionState is WorkerActionUiState.Loading &&
        (actionState as WorkerActionUiState.Loading).requestId == request.id

    val orderedStatuses = listOf(
        ServiceStatus.ACCEPTED,
        ServiceStatus.ON_THE_WAY,
        ServiceStatus.IN_PROGRESS,
        ServiceStatus.COMPLETED
    )
    val currentStatusOrder = orderedStatuses.indexOf(request.status)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Servicio Activo",
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
            actions = {
                IconButton(
                    onClick = { viewModel.refreshSelectedWorkerRequest() },
                    modifier = Modifier.testTag("worker_active_refresh_btn")
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
            if (actionState is WorkerActionUiState.Error &&
                (actionState as WorkerActionUiState.Error).requestId == request.id
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (actionState as WorkerActionUiState.Error).message,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Header card
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
                            text = request.clientName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        StatusBadge(status = request.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Servicio: ${request.categoryName}", fontSize = 14.sp, color = SecondaryTeal, fontWeight = FontWeight.SemiBold)
                    Text(text = "Ubicación: ${request.location}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Contacto cliente: ${request.clientPhone.ifBlank { "No disponible" }}", fontSize = 13.sp, color = TextSecondary)
                    Text(text = "Problema: ${request.description}", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (request.status == ServiceStatus.CANCELLED) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Solicitud cancelada",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Esta solicitud fue cancelada y ya no admite cambios de estado.",
                            fontSize = 13.sp,
                            color = Color(0xFF7F1D1D)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OficiosButton(
                            text = "Volver a mis solicitudes",
                            onClick = { viewModel.navigateTo(Screen.WorkerRequests) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Cambiar estados del servicio (transiciones válidas hacia adelante)
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
                                text = "Progreso del servicio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            if (isActingOnThis) {
                                CircularProgressIndicator(
                                    color = SecondaryTeal,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val stepSpecs = listOf(
                            Triple(ServiceStatus.ACCEPTED, "1. Aceptado", "Confirmar y agendar la visita"),
                            Triple(ServiceStatus.ON_THE_WAY, "2. En camino", "Avisar al cliente que vas hacia su casa"),
                            Triple(ServiceStatus.IN_PROGRESS, "3. En progreso", "Iniciar los trabajos de reparación"),
                            Triple(ServiceStatus.COMPLETED, "4. Finalizado", "Concluir el servicio y solicitar reseña")
                        )

                        stepSpecs.forEachIndexed { idx, (targetStatus, title, subtitle) ->
                            val isActive = request.status == targetStatus
                            val isDone = currentStatusOrder >= idx
                            val canAdvanceTo = !isActingOnThis && request.status.canTransitionTo(targetStatus)

                            ServiceStatusAction(
                                title = title,
                                subtitle = subtitle,
                                isActive = isActive,
                                isDone = isDone,
                                enabled = canAdvanceTo,
                                onClick = {
                                    viewModel.updateRequestStatusExplicit(request.id, targetStatus)
                                }
                            )
                            if (idx < stepSpecs.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            if (request.status == ServiceStatus.COMPLETED) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Servicio marcado como finalizado!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Se ha notificado al cliente que el trabajo concluyó. Ahora el cliente podrá calificar tu atención.",
                            fontSize = 13.sp,
                            color = Color(0xFF047857)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OficiosButton(
                            text = "Volver a mis solicitudes",
                            onClick = {
                                viewModel.navigateTo(Screen.WorkerRequests)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "worker_completed_back_cta"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceStatusAction(
    title: String,
    subtitle: String,
    isActive: Boolean,
    isDone: Boolean = isActive,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .testTag("status_action_${title.take(3)}"),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isActive -> Color(0xFFD6F2F5)
            isDone -> Color(0xFFECFEFF)
            else -> Color(0xFFF8FAFC)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive || isDone) SecondaryTeal else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = when {
                    isActive || isDone -> SecondaryTeal
                    enabled -> Color(0xFF94A3B8)
                    else -> Color(0xFFCBD5E1)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isActive || isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled || isDone) PrimaryNavy else TextSecondary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val professionals by viewModel.professionals.collectAsState()

    val workerPro = resolveWorkerProfessional(professionals, currentUser)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Perfil del trabajador",
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
            actions = {
                if (workerPro != null) {
                    IconButton(onClick = {
                        viewModel.resetProfileActionState()
                        viewModel.navigateTo(Screen.WorkerEditProfile)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = SecondaryTeal
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        if (workerPro == null) {
            EmptyStateView(
                title = "Perfil profesional en sincronización",
                message = "No se encontró tu ficha profesional sincronizada aún. Puedes actualizar los datos o volver al inicio.",
                actionText = "Sincronizar datos",
                onActionClick = { viewModel.loadDataFromSupabase() }
            )
            return
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
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
                    UserAvatar(
                        initial = workerPro.avatarInitial,
                        size = 72.dp,
                        backgroundColor = PrimaryNavy,
                        textColor = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = workerPro.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = workerPro.profession,
                        fontSize = 14.sp,
                        color = SecondaryTeal,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${workerPro.rating} ★", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            Text(text = "Calificación", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${workerPro.servicesCompleted}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            Text(text = "Servicios", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${workerPro.experienceYears} años", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            Text(text = "Experiencia", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Servicios ofrecidos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Servicios que presto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    workerPro.servicesOffered.forEach { serv ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = serv, fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // Disponibilidad y Ubicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Disponibilidad y Cobertura",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (workerPro.available) "Disponible para nuevos servicios" else "Actualmente ocupado",
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Zona: ${workerPro.locationName}",
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }
                }
            }

            OficiosButton(
                text = "Editar perfil",
                onClick = { viewModel.navigateTo(Screen.WorkerEditProfile) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "edit_profile_cta_btn"
            )

            OficiosOutlinedButton(
                text = "Cerrar sesión",
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                testTag = "worker_logout_btn"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerEditProfileScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val profileActionState by viewModel.profileActionState.collectAsState()

    val workerPro = resolveWorkerProfessional(professionals, currentUser)
    val isSaving = profileActionState is ProfileActionUiState.Loading

    var name by remember(workerPro, currentUser) { mutableStateOf(workerPro?.name ?: currentUser.name) }
    var phone by remember(currentUser) { mutableStateOf(currentUser.phone) }
    var profession by remember(workerPro) { mutableStateOf(workerPro?.profession ?: "Plomero") }
    var desc by remember(workerPro) { mutableStateOf(workerPro?.description ?: "") }
    var expYears by remember(workerPro) { mutableIntStateOf(workerPro?.experienceYears ?: 1) }
    var available by remember(workerPro) { mutableStateOf(workerPro?.available ?: true) }
    var location by remember(workerPro, currentUser) { mutableStateOf(workerPro?.locationName ?: currentUser.location) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Editar Perfil Profesional",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_name"),
                singleLine = true,
                colors = oficiosTextFieldColors()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono de contacto") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_phone"),
                singleLine = true,
                colors = oficiosTextFieldColors()
            )

            OutlinedTextField(
                value = profession,
                onValueChange = { profession = it },
                label = { Text("Profesión / Título") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_profession"),
                singleLine = true,
                colors = oficiosTextFieldColors()
            )

            // Sugerencias de oficios oficiales para actualizar rápidamente
            val tradeSuggestions = listOf(
                "Plomero",
                "Electricista",
                "Carpintero",
                "Pintor",
                "Cerrajero",
                "Técnico de electrodomésticos"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tradeSuggestions.forEach { suggestion ->
                    val isSelected = profession.equals(suggestion, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SecondaryTeal else BackgroundSoft,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                        modifier = Modifier
                            .clickable { profession = suggestion }
                            .testTag("suggestion_chip_${suggestion.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SurfaceWhite else PrimaryNavy,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Descripción profesional") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("worker_edit_desc"),
                colors = oficiosTextFieldColors()
            )

            OutlinedTextField(
                value = expYears.toString(),
                onValueChange = { expYears = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 },
                label = { Text("Años de experiencia") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_exp_years"),
                singleLine = true,
                colors = oficiosTextFieldColors()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Zona o ubicación") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_location"),
                singleLine = true,
                colors = oficiosTextFieldColors()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estado de disponibilidad",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = if (available) "Disponible para recibir solicitudes" else "No disponible por el momento",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = SecondaryTeal
                        )
                    )
                }
            }

            if (profileActionState is ProfileActionUiState.Error) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (profileActionState as ProfileActionUiState.Error).message,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OficiosButton(
                text = if (isSaving) "Guardando cambios..." else "Guardar cambios",
                onClick = {
                    viewModel.updateWorkerProfile(name, phone, profession, desc, expYears, available, location)
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_worker_profile_btn"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerReviewsScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val professionals by viewModel.professionals.collectAsState()

    val workerPro = resolveWorkerProfessional(professionals, currentUser)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Reseñas y Calificaciones",
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
            actions = {
                IconButton(
                    onClick = { viewModel.loadDataFromSupabase() },
                    modifier = Modifier.testTag("worker_reviews_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar reseñas",
                        tint = PrimaryNavy
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        if (workerPro == null) {
            EmptyStateView(
                title = "Sin ficha profesional activa",
                message = "Sincroniza tus datos para consultar tus reseñas y calificación promedio.",
                actionText = "Sincronizar",
                onActionClick = { viewModel.loadDataFromSupabase() }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // General Rating Header
                item {
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
                                text = if (workerPro.rating > 0.0) workerPro.rating.toString() else "Nuevo",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            RatingStars(rating = workerPro.rating, starSize = 22.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Basado en ${workerPro.reviews.size} reseñas de clientes",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (workerPro.reviews.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "Sin reseñas aún",
                            message = "Tus calificaciones aparecerán aquí cuando los clientes finalicen sus servicios."
                        )
                    }
                } else {
                    items(workerPro.reviews) { rev ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rev.clientName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                    RatingStars(rating = rev.rating, starSize = 14.dp)
                                }

                                Text(
                                    text = rev.comment,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                Text(
                                    text = rev.date,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
