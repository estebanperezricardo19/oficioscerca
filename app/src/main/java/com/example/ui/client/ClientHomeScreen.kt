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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Professional
import com.example.model.ServiceCategory
import com.example.service.SupabaseConnectionStatus
import com.example.ui.components.EmptyStateView
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

fun getCategoryIcon(iconName: String): ImageVector {
    val clean = iconName.lowercase().trim()
    return when {
        clean.contains("plumb") || clean.contains("plomer") -> Icons.Default.Plumbing
        clean.contains("electr") || clean.contains("bolt") -> Icons.Default.ElectricalServices
        clean.contains("carpin") || clean.contains("carpent") -> Icons.Default.Carpenter
        clean.contains("paint") || clean.contains("pintu") -> Icons.Default.FormatPaint
        clean.contains("key") || clean.contains("lock") || clean.contains("cerraj") -> Icons.Default.VpnKey
        clean.contains("kitch") || clean.contains("applian") || clean.contains("electro") -> Icons.Default.Kitchen
        else -> Icons.Default.Handyman
    }
}

@Composable
fun ClientHomeScreen(viewModel: OficiosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val filteredProfessionals by viewModel.filteredProfessionals.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryFilter.collectAsState()

    val unreadCount = notifications.count { !it.read }
    val isFilterActive = searchQuery.isNotBlank() || selectedCatId != null
    val selectedCategory = categories.find { it.id == selectedCatId }

    // Featured / Best rated professionals
    val bestRatedPro = remember(professionals) {
        professionals.filter { it.rating > 0.0 }.maxByOrNull { it.rating }
            ?: professionals.firstOrNull()
    }
    val availablePros = remember(professionals) {
        professionals.filter { it.available }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // ==========================================
        // 1. ENCABEZADO PRINCIPAL (NAVY HEADER)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryNavy)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Fila Superior: Saludo y Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val clientName = currentUser.name.trim()
                    val greetingText = if (clientName.isNotBlank()) "¡Hola, $clientName! 👋" else "¡Hola! 👋"

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greetingText,
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
                                text = currentUser.location.ifBlank { "Medellín, Colombia" },
                                fontSize = 13.sp,
                                color = Color(0xFFD6F2F5),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botón de sincronización manual
                        IconButton(
                            onClick = { viewModel.loadDataFromSupabase() },
                            modifier = Modifier.testTag("home_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar datos",
                                tint = SurfaceWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Botón de Notificaciones con Badge
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Qué servicio necesitas hoy?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SurfaceWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Búsqueda Funcional en la Pantalla de Inicio
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Busca por plomero, electricista, nombre...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = SecondaryTeal
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.testTag("home_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = oficiosTextFieldColors(),
                    singleLine = true
                )

                // Píldora de Estado de Conexión con Supabase
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (supabaseStatus) {
                        is SupabaseConnectionStatus.Connected -> Color(0xFF10B981).copy(alpha = 0.2f)
                        is SupabaseConnectionStatus.Error -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        is SupabaseConnectionStatus.Checking -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        else -> Color.Transparent
                    },
                    modifier = Modifier
                        .clickable { viewModel.loadDataFromSupabase() }
                        .testTag("home_supabase_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (supabaseStatus) {
                                        is SupabaseConnectionStatus.Connected -> Color(0xFF10B981)
                                        is SupabaseConnectionStatus.Error -> Color(0xFFEF4444)
                                        is SupabaseConnectionStatus.Checking -> Color(0xFFF59E0B)
                                        else -> AccentCyan
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (val s = supabaseStatus) {
                                is SupabaseConnectionStatus.Connected -> {
                                    if (s.professionalsCount > 0) {
                                        "Supabase conectado • ${s.categoriesCount} cat. • ${s.professionalsCount} prof."
                                    } else {
                                        "Supabase conectado • ${s.categoriesCount} categorías sincronizadas"
                                    }
                                }
                                is SupabaseConnectionStatus.Error -> "Error al conectar con Supabase (Toca para reintentar)"
                                is SupabaseConnectionStatus.Checking -> "Sincronizando con Supabase..."
                                else -> "Supabase conectado"
                            },
                            color = SurfaceWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. CATEGORÍAS OFICIALES DE SERVICIOS
        // ==========================================
        item {
            Column(modifier = Modifier.padding(top = 18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categorías de servicios",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    if (selectedCatId != null) {
                        TextButton(
                            onClick = { viewModel.selectCategory(null) },
                            modifier = Modifier.testTag("clear_category_filter_link")
                        ) {
                            Text(
                                text = "Ver todas",
                                fontSize = 13.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Fila horizontal de las 6 categorías oficiales
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCatId
                        CategoryItemCard(
                            category = cat,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.toggleCategoryFilter(cat.id)
                            }
                        )
                    }
                }

                // Indicador de filtro de categoría activo
                if (selectedCategory != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F7FA)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Filtrando: ${selectedCategory.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryNavy
                                )
                            }
                            IconButton(
                                onClick = { viewModel.selectCategory(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar filtro",
                                    tint = PrimaryNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. ESTADOS DE PANTALLA Y CONTENIDO PRINCIPAL
        // ==========================================

        // Estado A: Carga inicial activa
        if (supabaseStatus is SupabaseConnectionStatus.Checking && professionals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SecondaryTeal,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Cargando profesionales y categorías de Supabase...",
                            color = PrimaryNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        // Estado B: Error de conexión
        else if (supabaseStatus is SupabaseConnectionStatus.Error && professionals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Error al consultar la base de datos de Supabase",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            fontSize = 15.sp
                        )
                        Text(
                            text = (supabaseStatus as SupabaseConnectionStatus.Error).message,
                            fontSize = 13.sp,
                            color = Color(0xFF424242)
                        )
                        TextButton(
                            onClick = { viewModel.loadDataFromSupabase() },
                            modifier = Modifier.testTag("retry_supabase_load_button")
                        ) {
                            Text(
                                text = "Reintentar conexión",
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        // Estado C: Búsqueda o filtro de categoría activo
        else if (isFilterActive) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resultados (${filteredProfessionals.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text(
                            text = "Limpiar filtros",
                            color = SecondaryTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filteredProfessionals.isEmpty()) {
                item {
                    val filterDescription = when {
                        searchQuery.isNotBlank() && selectedCategory != null ->
                            "'${searchQuery.trim()}' en ${selectedCategory.name}"
                        searchQuery.isNotBlank() ->
                            "'${searchQuery.trim()}'"
                        selectedCategory != null ->
                            "la categoría ${selectedCategory.name}"
                        else -> "tu búsqueda"
                    }
                    EmptyStateView(
                        title = "Sin resultados encontrados",
                        message = "No encontramos profesionales que coincidan con $filterDescription. Intenta con otro término o limpia los filtros.",
                        actionText = "Ver todos los profesionales",
                        onActionClick = { viewModel.clearFilters() },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                items(filteredProfessionals) { pro ->
                    ProfessionalCardItem(
                        professional = pro,
                        onClick = { viewModel.openProfessionalDetail(pro.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
        // Estado D: Pantalla de inicio regular (sin filtros activos)
        else {
            // Tarjeta de Asistencia / Urgencia en el hogar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .clickable {
                            viewModel.setSearchQuery("plomero")
                        },
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
                                color = PrimaryNavy.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (professionals.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Aún no hay profesionales registrados",
                        message = "La base de datos de Supabase no contiene profesionales en este momento. Los nuevos trabajadores aparecerán aquí una vez completen su registro.",
                        actionText = "Actualizar datos",
                        onActionClick = { viewModel.loadDataFromSupabase() },
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            } else {
                // Sección: Profesional Destacado o Mejor Calificado
                if (bestRatedPro != null) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (bestRatedPro.rating > 0.0) "Profesional destacado" else "Recomendado",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                if (bestRatedPro.rating > 0.0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = StarGold.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = StarGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${bestRatedPro.rating} calificación",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryNavy
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            ProfessionalCardItem(
                                professional = bestRatedPro,
                                onClick = { viewModel.openProfessionalDetail(bestRatedPro.id) },
                                isFeatured = true
                            )
                        }
                    }
                }

                // Sección: Disponibles Ahora
                if (availablePros.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Disponibles ahora (${availablePros.size})",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    items(availablePros.take(3)) { pro ->
                        ProfessionalCardItem(
                            professional = pro,
                            onClick = { viewModel.openProfessionalDetail(pro.id) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }

                // Sección: Todos los Profesionales
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Todos los profesionales (${professionals.size})",
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(professionals) { pro ->
                    ProfessionalCardItem(
                        professional = pro,
                        onClick = { viewModel.openProfessionalDetail(pro.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES: TARJETA DE CATEGORÍA
// ==========================================
@Composable
fun CategoryItemCard(
    category: ServiceCategory,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .clickable { onClick() }
            .testTag("cat_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE0F7FA) else SurfaceWhite
        ),
        border = if (isSelected) BorderStroke(2.dp, SecondaryTeal) else BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
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
                color = if (isSelected) SecondaryTeal else Color(0xFFE2F4F7)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(category.name.ifBlank { category.iconName }),
                        contentDescription = category.name,
                        tint = if (isSelected) SurfaceWhite else SecondaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = PrimaryNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// COMPONENTES: TARJETA DE PROFESIONAL
// ==========================================
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
        border = if (isFeatured) BorderStroke(1.5.dp, SecondaryTeal) else BorderStroke(1.dp, Color(0xFFECEFF1)),
        elevation = CardDefaults.cardElevation(if (isFeatured) 3.dp else 2.dp)
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
                // Fila 1: Nombre y Calificación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = professional.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verificado",
                            tint = SecondaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Calificación real o indicador de nuevo
                    if (professional.rating > 0.0) {
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
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "★ Nuevo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Fila 2: Oficio / Profesión
                Text(
                    text = professional.profession,
                    fontSize = 13.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fila 3: Ubicación y Disponibilidad Real
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Píldora de Disponibilidad
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (professional.available) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (professional.available) Color(0xFF10B981) else Color(0xFF90A4AE))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (professional.available) "Disponible" else "Ocupado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (professional.available) Color(0xFF2E7D32) else TextSecondary
                            )
                        }
                    }

                    Text(text = "•", color = BorderSubtle)

                    // Ubicación guardada real (sin distancias ficticias)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = professional.locationName,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Fila 4: Servicios destacados / etiquetas
                if (professional.servicesOffered.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        professional.servicesOffered.take(2).forEach { serv ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = serv,
                                    fontSize = 10.sp,
                                    color = PrimaryNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
