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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.viewmodel.ClientRequestsUiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OficiosButton
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
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.model.UserRole
import com.example.ui.theme.oficiosTextFieldColors
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.ProfileActionUiState
import com.example.viewmodel.Screen

internal fun formatDisplayDate(rawDate: String): String {
    if (rawDate.isBlank()) return "Fecha reciente"
    return try {
        val datePart = rawDate.substringBefore("T").trim()
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "ene."
                "02" -> "feb."
                "03" -> "mar."
                "04" -> "abr."
                "05" -> "may."
                "06" -> "jun."
                "07" -> "jul."
                "08" -> "ago."
                "09" -> "sep."
                "10" -> "oct."
                "11" -> "nov."
                "12" -> "dic."
                else -> parts[1]
            }
            val day = parts[2].toIntOrNull()?.toString() ?: parts[2]
            "$day $month $year"
        } else {
            rawDate
        }
    } catch (_: Exception) {
        rawDate
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHistoryScreen(viewModel: OficiosViewModel) {
    val requestsState by viewModel.clientRequestsState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todas") }

    // Trigger load on entry
    LaunchedEffect(Unit) {
        viewModel.loadClientRequests()
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
                        text = "Mis solicitudes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Historial y estado de tus servicios",
                        fontSize = 12.sp,
                        color = SecondaryTeal
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.loadClientRequests(forceRefresh = true) },
                    modifier = Modifier.testTag("refresh_requests_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar solicitudes",
                        tint = PrimaryNavy
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        when (val state = requestsState) {
            is ClientRequestsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = SecondaryTeal,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Consultando tus solicitudes en Supabase...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            is ClientRequestsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Error al cargar solicitudes",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 18.dp)
                            )
                            OficiosButton(
                                text = "Reintentar",
                                onClick = { viewModel.loadClientRequests(forceRefresh = true) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "retry_history_btn"
                            )
                        }
                    }
                }
            }
            is ClientRequestsUiState.Success -> {
                val requests = state.requests
                if (requests.isEmpty()) {
                    EmptyStateView(
                        title = "No tienes solicitudes todavía",
                        message = "Aquí aparecerán las solicitudes que realices a profesionales. Podrás hacer seguimiento a su estado en tiempo real.",
                        actionText = "Buscar profesionales",
                        onActionClick = { viewModel.navigateTo(Screen.ClientHome) }
                    )
                } else {
                    val filterOptions = listOf("Todas", "Activas", "Finalizadas", "Canceladas")
                    val filteredRequests = remember(requests, selectedFilter) {
                        when (selectedFilter) {
                            "Activas" -> requests.filter {
                                it.status == ServiceStatus.PENDING ||
                                    it.status == ServiceStatus.ACCEPTED ||
                                    it.status == ServiceStatus.ON_THE_WAY ||
                                    it.status == ServiceStatus.IN_PROGRESS
                            }
                            "Finalizadas" -> requests.filter { it.status == ServiceStatus.COMPLETED }
                            "Canceladas" -> requests.filter { it.status == ServiceStatus.CANCELLED }
                            else -> requests
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { option ->
                            val selected = selectedFilter == option
                            FilterChip(
                                selected = selected,
                                onClick = { selectedFilter = option },
                                label = {
                                    Text(
                                        text = option,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryTeal,
                                    selectedLabelColor = SurfaceWhite,
                                    containerColor = SurfaceWhite,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    if (filteredRequests.isEmpty()) {
                        EmptyStateView(
                            title = "Sin solicitudes en '$selectedFilter'",
                            message = "No hay solicitudes registradas con este estado.",
                            actionText = "Ver todas",
                            onActionClick = { selectedFilter = "Todas" }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${filteredRequests.size} ${if (filteredRequests.size == 1) "solicitud" else "solicitudes"}",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Más recientes primero",
                                        fontSize = 11.sp,
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            items(filteredRequests, key = { it.id }) { req ->
                                ClientHistoryCard(
                                    request = req,
                                    onClick = {
                                        viewModel.openTracking(req.id)
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientHistoryCard(
    request: ServiceRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${request.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Real Professional Name, Profession and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        initial = request.professionalName.firstOrNull()?.toString() ?: "P",
                        size = 44.dp,
                        backgroundColor = PrimaryNavy,
                        textColor = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.professionalName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = request.professionalProfession,
                            fontSize = 13.sp,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category tag and creation date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD6F2F5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = request.categoryName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryNavy
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDisplayDate(request.createdAt),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = request.description,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = request.location,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: rating or "Ver detalle y seguimiento"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (request.status == ServiceStatus.COMPLETED && request.ratingGiven != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Calificación: ${request.ratingGiven}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                } else {
                    Text(
                        text = if (request.scheduledDate.isNotBlank() && request.scheduledDate != "Por coordinar") "Cita: ${request.scheduledDate}" else "En seguimiento",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ver seguimiento",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val profileActionState by viewModel.profileActionState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val unreadCount = notifications.count { !it.read }
    var isEditing by remember { mutableStateOf(false) }

    var editName by remember(currentUser) { mutableStateOf(currentUser.name) }
    var editPhone by remember(currentUser) { mutableStateOf(currentUser.phone) }
    var editLocation by remember(currentUser) { mutableStateOf(currentUser.location) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Perfil del cliente",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
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
            if (profileActionState is ProfileActionUiState.Success) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD1FAE5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (profileActionState as ProfileActionUiState.Success).message,
                        color = Color(0xFF065F46),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Card info básica
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
                        initial = currentUser.name.firstOrNull()?.toString() ?: "C",
                        size = 72.dp,
                        backgroundColor = PrimaryNavy,
                        textColor = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${currentUser.name} ${currentUser.lastName}".trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    if (currentUser.email.isNotBlank()) {
                        Text(
                            text = currentUser.email,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentUser.location.ifBlank { "Sin ubicación registrada" },
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentUser.phone.ifBlank { "Sin teléfono registrado" },
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }
                }
            }

            if (isEditing) {
                val isSaving = profileActionState is ProfileActionUiState.Loading
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
                            text = "Editar información",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = {
                                editName = it
                                if (profileActionState is ProfileActionUiState.Error) viewModel.resetProfileActionState()
                            },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            singleLine = true,
                            colors = oficiosTextFieldColors()
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = {
                                editPhone = it
                                if (profileActionState is ProfileActionUiState.Error) viewModel.resetProfileActionState()
                            },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                            singleLine = true,
                            colors = oficiosTextFieldColors()
                        )

                        OutlinedTextField(
                            value = editLocation,
                            onValueChange = {
                                editLocation = it
                                if (profileActionState is ProfileActionUiState.Error) viewModel.resetProfileActionState()
                            },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_location"),
                            singleLine = true,
                            colors = oficiosTextFieldColors()
                        )

                        if (profileActionState is ProfileActionUiState.Error) {
                            Text(
                                text = (profileActionState as ProfileActionUiState.Error).message,
                                color = Color(0xFFB91C1C),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OficiosButton(
                                text = if (isSaving) "Guardando..." else "Guardar",
                                onClick = {
                                    viewModel.updateClientProfile(editName, editPhone, editLocation) {
                                        isEditing = false
                                    }
                                },
                                enabled = !isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "save_profile_btn"
                            )
                        }
                    }
                }
            }

            // Opciones de configuración
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        title = if (isEditing) "Cancelar edición" else "Editar perfil",
                        onClick = {
                            viewModel.resetProfileActionState()
                            isEditing = !isEditing
                        }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.History,
                        title = "Mis servicios",
                        onClick = { viewModel.navigateTo(Screen.ClientHistory) }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Notifications,
                        title = if (unreadCount > 0) "Notificaciones ($unreadCount nuevas)" else "Notificaciones",
                        onClick = { viewModel.navigateTo(Screen.ClientNotifications) }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Settings,
                        title = "Cambiar a rol Trabajador",
                        onClick = { viewModel.selectRoleAndProceed(UserRole.WORKER) }
                    )
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = if (authLoading) "Cerrando sesión..." else "Cerrar sesión",
                        onClick = { if (!authLoading) viewModel.logout() },
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFEF4444) else SecondaryTeal,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color(0xFFEF4444) else PrimaryNavy,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = BorderSubtle,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientNotificationsScreen(viewModel: OficiosViewModel) {
    val notifications by viewModel.notifications.collectAsState()
    val notificationsLoading by viewModel.notificationsLoading.collectAsState()
    val notificationsError by viewModel.notificationsError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Notificaciones",
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
                    onClick = { viewModel.refreshNotifications() },
                    modifier = Modifier.testTag("refresh_notifications_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar notificaciones",
                        tint = PrimaryNavy
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        if (!notificationsError.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEE2E2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = notificationsError ?: "",
                    color = Color(0xFF991B1B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        if (notificationsLoading && notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SecondaryTeal)
            }
        } else if (notifications.isEmpty()) {
            EmptyStateView(
                title = "No tienes notificaciones",
                message = "Aquí recibirás avisos sobre tus solicitudes y el estado de los servicios.",
                actionText = "Actualizar",
                onActionClick = { viewModel.refreshNotifications() }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCardItem(
                        notification = notif,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            notif.relatedRequestId?.let { id ->
                                if (currentUser.role == UserRole.WORKER) {
                                    viewModel.openWorkerRequestDetail(id)
                                } else {
                                    viewModel.openTracking(id)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notif_item_${notification.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) SurfaceWhite else Color(0xFFE3F5F7)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (notification.read) Color(0xFFE2EDF1) else SecondaryTeal
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (notification.read) TextSecondary else SurfaceWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = notification.date,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentCyan, CircleShape)
                )
            }
        }
    }
}
