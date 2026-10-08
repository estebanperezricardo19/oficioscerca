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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Professional
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
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientSearchScreen(viewModel: OficiosViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val suggestedSearches = listOf(
        "Reparación de fugas",
        "Instalación eléctrica",
        "Pintura de paredes",
        "Reparación de electrodomésticos",
        "Cambio de cerradura",
        "Arreglo de puertas"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "¿Qué estás buscando?",
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

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Busca un servicio o profesional") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SecondaryTeal)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SecondaryTeal,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Categorías
            Text(
                text = "Todas las categorías",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().height(180.dp)
            ) {
                items(categories) { cat ->
                    Card(
                        modifier = Modifier
                            .clickable {
                                viewModel.selectCategory(cat.id)
                                viewModel.navigateTo(Screen.ClientResults)
                            }
                            .testTag("search_cat_${cat.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                color = Color(0xFFE2F4F7)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = SecondaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryNavy,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Búsquedas sugeridas
            Text(
                text = "Búsquedas sugeridas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestedSearches.forEach { suggestion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSearchQuery(suggestion)
                                viewModel.navigateTo(Screen.ClientResults)
                            }
                            .testTag("suggested_$suggestion"),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceWhite
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = suggestion,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientResultsScreen(viewModel: OficiosViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val professionals by viewModel.filteredProfessionals.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCatId by viewModel.selectedCategoryFilter.collectAsState()

    var sortByDistance by remember { mutableStateOf(false) }
    var sortByRating by remember { mutableStateOf(false) }
    var filterAvailableOnly by remember { mutableStateOf(false) }

    val sortedList = remember(professionals, sortByDistance, sortByRating, filterAvailableOnly) {
        var list = professionals
        if (filterAvailableOnly) {
            list = list.filter { it.available }
        }
        when {
            sortByDistance -> list.sortedBy { it.distanceKm }
            sortByRating -> list.sortedByDescending { it.rating }
            else -> list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (searchQuery.isNotBlank()) "Resultados: $searchQuery" else "Profesionales disponibles",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy,
                    maxLines = 1
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
                IconButton(onClick = { viewModel.navigateTo(Screen.ClientMap) }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ver mapa",
                        tint = SecondaryTeal
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSoft)
        )

        // Filter / Sort row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCatId != null,
                    onClick = { viewModel.selectCategory(null) },
                    label = {
                        val activeCat = categories.find { it.id == selectedCatId }
                        Text(if (activeCat != null) "Cat: ${activeCat.name}" else "Todas categorías")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryTeal,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
            item {
                FilterChip(
                    selected = sortByDistance,
                    onClick = {
                        sortByDistance = !sortByDistance
                        if (sortByDistance) sortByRating = false
                    },
                    label = { Text("Más cercanos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryTeal,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
            item {
                FilterChip(
                    selected = sortByRating,
                    onClick = {
                        sortByRating = !sortByRating
                        if (sortByRating) sortByDistance = false
                    },
                    label = { Text("Mejor calificados") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryTeal,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
            item {
                FilterChip(
                    selected = filterAvailableOnly,
                    onClick = { filterAvailableOnly = !filterAvailableOnly },
                    label = { Text("Disponibles hoy") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryTeal,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
        }

        if (sortedList.isEmpty()) {
            EmptyStateView(
                title = "No encontramos profesionales",
                message = "Intenta cambiar los filtros o el término de búsqueda.",
                actionText = "Limpiar filtros",
                onActionClick = {
                    viewModel.setSearchQuery("")
                    viewModel.selectCategory(null)
                    sortByDistance = false
                    sortByRating = false
                    filterAvailableOnly = false
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedList) { pro ->
                    DetailedWorkerCard(
                        professional = pro,
                        onClick = { viewModel.openProfessionalDetail(pro.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedWorkerCard(
    professional: Professional,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("result_card_${professional.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    initial = professional.avatarInitial,
                    size = 56.dp,
                    backgroundColor = PrimaryNavy,
                    textColor = AccentCyan
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = professional.name,
                            fontSize = 17.sp,
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
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = professional.rating.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }
                    }

                    Text(
                        text = professional.profession,
                        fontSize = 14.sp,
                        color = SecondaryTeal,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${professional.servicesCompleted} servicios",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(text = "•", color = BorderSubtle)
                        Text(
                            text = "${professional.distanceKm} km",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(text = "•", color = BorderSubtle)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (professional.available) Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = if (professional.available) "Disponible" else "Ocupado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (professional.available) Color(0xFF047857) else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Servicios principales chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                professional.servicesOffered.take(2).forEach { serv ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDF8FA),
                        border = BorderStroke(0.5.dp, BorderSubtle)
                    ) {
                        Text(
                            text = serv,
                            fontSize = 11.sp,
                            color = PrimaryNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
