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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OficiosButton
import com.example.ui.components.OficiosOutlinedButton
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

@Composable
fun WorkerHomeScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val requests by viewModel.requests.collectAsState()

    val workerPro = professionals.find { it.userId == currentUser.id || it.id == "pro_mateo" }
        ?: professionals.first()

    val pendingCount = requests.count { it.status == ServiceStatus.PENDING }
    val activeCount = requests.count { it.status == ServiceStatus.ACCEPTED || it.status == ServiceStatus.ON_THE_WAY || it.status == ServiceStatus.IN_PROGRESS }
    val completedCount = requests.count { it.status == ServiceStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryNavy)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${workerPro.name.split(" ").first()} 👋",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Text(
                            text = workerPro.profession,
                            fontSize = 14.sp,
                            color = AccentCyan,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (workerPro.available) Color(0xFF10B981) else Color(0xFF6B7280)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SurfaceWhite, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (workerPro.available) "Disponible" else "No disponible",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Dashboard Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkerStatBadge(
                        title = "Pendientes",
                        value = pendingCount.toString(),
                        modifier = Modifier.weight(1f),
                        badgeColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFFD97706)
                    )
                    WorkerStatBadge(
                        title = "Activos",
                        value = activeCount.toString(),
                        modifier = Modifier.weight(1f),
                        badgeColor = Color(0xFFE0F7FA),
                        textColor = SecondaryTeal
                    )
                    WorkerStatBadge(
                        title = "Completados",
                        value = completedCount.toString(),
                        modifier = Modifier.weight(1f),
                        badgeColor = Color(0xFFD1FAE5),
                        textColor = Color(0xFF059669)
                    )
                    WorkerStatBadge(
                        title = "Calificación",
                        value = "${workerPro.rating} ★",
                        modifier = Modifier.weight(1f),
                        badgeColor = Color(0xFFFEF9C3),
                        textColor = Color(0xFFCA8A04)
                    )
                }
            }
        }

        // Accesos rápidos
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Accesos rápidos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkerQuickAction(
                        title = "Ver solicitudes",
                        icon = Icons.Default.Assignment,
                        onClick = { viewModel.navigateTo(Screen.WorkerRequests) },
                        modifier = Modifier.weight(1f)
                    )
                    WorkerQuickAction(
                        title = "Servicio activo",
                        icon = Icons.Default.Build,
                        onClick = {
                            val active = requests.firstOrNull { it.status != ServiceStatus.COMPLETED && it.status != ServiceStatus.CANCELLED }
                                ?: requests.firstOrNull()
                            if (active != null) {
                                viewModel.openWorkerActiveService(active.id)
                            } else {
                                viewModel.navigateTo(Screen.WorkerRequests)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkerQuickAction(
                        title = "Mi perfil",
                        icon = Icons.Default.Person,
                        onClick = { viewModel.navigateTo(Screen.WorkerProfile) },
                        modifier = Modifier.weight(1f)
                    )
                    WorkerQuickAction(
                        title = "Mis reseñas",
                        icon = Icons.Default.Star,
                        onClick = { viewModel.navigateTo(Screen.WorkerReviews) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Solicitudes recientes
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solicitudes recientes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Ver todas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryTeal,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.WorkerRequests) }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        items(requests.take(3)) { req ->
            WorkerRequestItemCard(
                request = req,
                onViewDetail = { viewModel.openWorkerRequestDetail(req.id) },
                onAccept = {
                    viewModel.updateRequestStatusExplicit(req.id, ServiceStatus.ACCEPTED)
                    viewModel.openWorkerActiveService(req.id)
                },
                onReject = {
                    viewModel.updateRequestStatusExplicit(req.id, ServiceStatus.CANCELLED)
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Switch to Client Role shortcut
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                OficiosOutlinedButton(
                    text = "Cambiar a modo Cliente",
                    onClick = { viewModel.selectRoleAndProceed(UserRole.CLIENT) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "switch_to_client_mode_btn"
                )
            }
        }
    }
}

@Composable
fun WorkerStatBadge(
    title: String,
    value: String,
    badgeColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WorkerQuickAction(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("worker_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = Color(0xFFE2F4F7)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavy
            )
        }
    }
}

@Composable
fun WorkerRequestItemCard(
    request: ServiceRequest,
    onViewDetail: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("worker_request_item_${request.id}"),
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
                        initial = request.clientName.firstOrNull()?.toString() ?: "C",
                        size = 40.dp,
                        backgroundColor = SecondaryTeal,
                        textColor = SurfaceWhite
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.clientName,
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
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = request.location, fontSize = 12.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = request.scheduledDate, fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (request.status == ServiceStatus.PENDING) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OficiosOutlinedButton(
                        text = "Rechazar",
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        testTag = "reject_btn_${request.id}"
                    )
                    OficiosButton(
                        text = "Aceptar",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        testTag = "accept_btn_${request.id}"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRequestsScreen(viewModel: OficiosViewModel) {
    val requests by viewModel.requests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Solicitudes recibidas",
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

        if (requests.isEmpty()) {
            EmptyStateView(
                title = "No tienes solicitudes",
                message = "Cuando los clientes soliciten tus servicios aparecerán aquí."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { req ->
                    WorkerRequestItemCard(
                        request = req,
                        onViewDetail = { viewModel.openWorkerRequestDetail(req.id) },
                        onAccept = {
                            viewModel.updateRequestStatusExplicit(req.id, ServiceStatus.ACCEPTED)
                            viewModel.openWorkerActiveService(req.id)
                        },
                        onReject = {
                            viewModel.updateRequestStatusExplicit(req.id, ServiceStatus.CANCELLED)
                        }
                    )
                }
            }
        }
    }
}
