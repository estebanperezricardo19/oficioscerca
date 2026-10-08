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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.model.ServiceRequest
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
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHistoryScreen(viewModel: OficiosViewModel) {
    val requests by viewModel.requests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Historial",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Mis servicios",
                        fontSize = 13.sp,
                        color = SecondaryTeal
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        Text(
            text = "Consulta los servicios que has solicitado.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        if (requests.isEmpty()) {
            EmptyStateView(
                title = "No tienes servicios todavía",
                message = "Explora las categorías y solicita tu primer servicio para el hogar.",
                actionText = "Buscar servicios",
                onActionClick = { viewModel.navigateTo(Screen.ClientSearch) }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { req ->
                    ClientHistoryCard(
                        request = req,
                        onClick = {
                            viewModel.openTracking(req.id)
                        }
                    )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        initial = request.professionalName.firstOrNull()?.toString() ?: "P",
                        size = 40.dp,
                        backgroundColor = PrimaryNavy,
                        textColor = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.professionalName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = request.categoryName,
                            fontSize = 13.sp,
                            color = SecondaryTeal
                        )
                    }
                }
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = request.description,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.scheduledDate,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                if (request.ratingGiven != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = request.ratingGiven.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                } else {
                    Text(
                        text = "Ver detalle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryTeal
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
                        text = "${currentUser.name} ${currentUser.lastName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = currentUser.email,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

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
                            text = currentUser.location,
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
                            text = currentUser.phone,
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }
                }
            }

            if (isEditing) {
                // Formulario editar perfil
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
                            onValueChange = { editName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editLocation,
                            onValueChange = { editLocation = it },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_location"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OficiosButton(
                                text = "Guardar",
                                onClick = {
                                    viewModel.updateClientProfile(editName, editPhone, editLocation)
                                    isEditing = false
                                },
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
                        onClick = { isEditing = !isEditing }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.History,
                        title = "Mis servicios",
                        onClick = { viewModel.navigateTo(Screen.ClientHistory) }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        onClick = { viewModel.navigateTo(Screen.ClientNotifications) }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Settings,
                        title = "Cambiar a rol Trabajador",
                        onClick = { viewModel.selectRoleAndProceed(com.example.model.UserRole.WORKER) }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Cerrar sesión",
                        onClick = { viewModel.navigateTo(Screen.Welcome) },
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        if (notifications.isEmpty()) {
            EmptyStateView(
                title = "No tienes notificaciones",
                message = "Aquí recibirás avisos sobre tus solicitudes y el estado de los servicios."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { notif ->
                    NotificationCardItem(
                        notification = notif,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            notif.relatedRequestId?.let { id ->
                                viewModel.openTracking(id)
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
