package com.example.ui.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Professional
import com.example.model.ServiceCategory
import com.example.ui.components.RatingStars
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

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "plumbing" -> Icons.Default.Plumbing
        "bolt" -> Icons.Default.ElectricalServices
        "carpenter" -> Icons.Default.Carpenter
        "format_paint" -> Icons.Default.FormatPaint
        "vpn_key" -> Icons.Default.VpnKey
        "kitchen" -> Icons.Default.Kitchen
        else -> Icons.Default.Handyman
    }
}

@Composable
fun ClientHomeScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unreadCount = notifications.count { !it.read }
    val bestRatedPro = professionals.maxByOrNull { it.rating } ?: professionals.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryNavy)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${currentUser.name} 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser.location,
                                fontSize = 13.sp,
                                color = Color(0xFFD6F2F5)
                            )
                        }
                    }

                    // Notification Button with badge
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ClientNotifications) },
                        modifier = Modifier.testTag("home_notif_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = AccentCyan, contentColor = PrimaryNavy) {
                                        Text(text = unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = SurfaceWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "¿Qué necesitas resolver hoy?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SurfaceWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar Trigger
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(Screen.ClientSearch)
                        }
                        .testTag("home_search_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SecondaryTeal
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Busca un servicio o profesional",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Categorías
        item {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    text = "Categorías de servicios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { cat ->
                        CategoryItemCard(
                            category = cat,
                            onClick = {
                                viewModel.selectCategory(cat.id)
                                viewModel.navigateTo(Screen.ClientResults)
                            }
                        )
                    }
                }
            }
        }

        // Tarjeta de ayuda
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6F2F5)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = SecondaryTeal
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = SurfaceWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Urgencia en el hogar?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "Encuentra plomeros, cerrajeros y electricistas con respuesta rápida.",
                            fontSize = 12.sp,
                            color = PrimaryNavy.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Sección "Cerca de ti"
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cerca de ti",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Ver mapa",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecondaryTeal,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.ClientMap) }
                            .padding(4.dp)
                            .testTag("home_view_map_link")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Trabajadores cercanos (Mateo Ramírez, Julián Castro, etc.)
        items(professionals.take(3)) { pro ->
            ProfessionalCardItem(
                professional = pro,
                onClick = { viewModel.openProfessionalDetail(pro.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Sección "Mejor calificado"
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "Mejor calificado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfessionalCardItem(
                    professional = bestRatedPro,
                    onClick = { viewModel.openProfessionalDetail(bestRatedPro.id) },
                    isFeatured = true
                )
            }
        }
    }
}

@Composable
fun CategoryItemCard(
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
            .testTag("cat_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = Color(0xFFE2F4F7)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconName),
                        contentDescription = category.name,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryNavy,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProfessionalCardItem(
    professional: Professional,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFeatured: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("pro_card_${professional.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) Color(0xFFF0F9FB) else SurfaceWhite
        ),
        border = if (isFeatured) BorderStroke(1.5.dp, AccentCyan) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initial = professional.avatarInitial,
                size = 54.dp,
                backgroundColor = if (isFeatured) SecondaryTeal else PrimaryNavy,
                textColor = SurfaceWhite
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = professional.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = professional.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                }

                Text(
                    text = professional.profession,
                    fontSize = 13.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${professional.servicesCompleted} servicios",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(text = "•", color = BorderSubtle)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${professional.distanceKm} km",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
