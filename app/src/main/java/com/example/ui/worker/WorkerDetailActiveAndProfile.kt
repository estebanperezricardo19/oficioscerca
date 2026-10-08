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
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRequestDetailScreen(viewModel: OficiosViewModel) {
    val req by viewModel.workerSelectedRequest.collectAsState()

    if (req == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró la solicitud seleccionada.")
        }
        return
    }

    val request = req!!

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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    DetailRow(label = "Fecha", value = request.scheduledDate)
                    DetailRow(label = "Hora", value = request.scheduledTime)
                    DetailRow(label = "Contacto", value = request.clientPhone)
                }
            }
        }

        // Action Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceWhite,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (request.status == ServiceStatus.PENDING) {
                    OficiosOutlinedButton(
                        text = "Rechazar solicitud",
                        onClick = {
                            viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.CANCELLED)
                            viewModel.navigateBack()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "detail_reject_btn"
                    )

                    OficiosButton(
                        text = "Aceptar solicitud",
                        onClick = {
                            viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.ACCEPTED)
                            viewModel.openWorkerActiveService(request.id)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "detail_accept_btn"
                    )
                } else {
                    OficiosButton(
                        text = "Ver servicio activo",
                        onClick = {
                            viewModel.openWorkerActiveService(request.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "go_to_active_service_btn"
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

    if (req == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay servicio activo seleccionado.")
        }
        return
    }

    val request = req!!

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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    Text(text = "Problema: ${request.description}", fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                }
            }

            // Cambiar estados del servicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Cambiar estado del servicio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ServiceStatusAction(
                        title = "1. Aceptado",
                        subtitle = "Confirmar y agendar la visita",
                        isActive = request.status == ServiceStatus.ACCEPTED,
                        onClick = { viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.ACCEPTED) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ServiceStatusAction(
                        title = "2. En camino",
                        subtitle = "Avisar al cliente que vas hacia su casa",
                        isActive = request.status == ServiceStatus.ON_THE_WAY,
                        onClick = { viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.ON_THE_WAY) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ServiceStatusAction(
                        title = "3. En progreso",
                        subtitle = "Iniciar los trabajos de reparación",
                        isActive = request.status == ServiceStatus.IN_PROGRESS,
                        onClick = { viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.IN_PROGRESS) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ServiceStatusAction(
                        title = "4. Finalizado",
                        subtitle = "Concluir el servicio y solicitar reseña",
                        isActive = request.status == ServiceStatus.COMPLETED,
                        onClick = { viewModel.updateRequestStatusExplicit(request.id, ServiceStatus.COMPLETED) }
                    )
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
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("status_action_${title.take(3)}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFFD6F2F5) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isActive) SecondaryTeal else BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = if (isActive) SecondaryTeal else Color(0xFFCBD5E1)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isActive) {
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
                    color = PrimaryNavy
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

    val workerPro = professionals.find { it.userId == currentUser.id || it.id == "pro_mateo" }
        ?: professionals.first()

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
                IconButton(onClick = { viewModel.navigateTo(Screen.WorkerEditProfile) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar perfil",
                        tint = SecondaryTeal
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerEditProfileScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val professionals by viewModel.professionals.collectAsState()

    val workerPro = professionals.find { it.userId == currentUser.id || it.id == "pro_mateo" }
        ?: professionals.first()

    var name by remember(workerPro) { mutableStateOf(workerPro.name) }
    var phone by remember(currentUser) { mutableStateOf(currentUser.phone) }
    var profession by remember(workerPro) { mutableStateOf(workerPro.profession) }
    var desc by remember(workerPro) { mutableStateOf(workerPro.description) }
    var expYears by remember(workerPro) { mutableIntStateOf(workerPro.experienceYears) }
    var available by remember(workerPro) { mutableStateOf(workerPro.available) }
    var location by remember(workerPro) { mutableStateOf(workerPro.locationName) }

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
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono de contacto") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_phone"),
                singleLine = true
            )

            OutlinedTextField(
                value = profession,
                onValueChange = { profession = it },
                label = { Text("Profesión / Título") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_profession"),
                singleLine = true
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Descripción profesional") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("worker_edit_desc")
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Zona o ubicación") },
                modifier = Modifier.fillMaxWidth().testTag("worker_edit_location"),
                singleLine = true
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

            Spacer(modifier = Modifier.height(10.dp))

            OficiosButton(
                text = "Guardar cambios",
                onClick = {
                    viewModel.updateWorkerProfile(name, phone, profession, desc, expYears, available, location)
                },
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

    val workerPro = professionals.find { it.userId == currentUser.id || it.id == "pro_mateo" }
        ?: professionals.first()

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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

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
                            text = workerPro.rating.toString(),
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
