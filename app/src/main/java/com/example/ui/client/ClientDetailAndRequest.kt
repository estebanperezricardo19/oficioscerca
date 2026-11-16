package com.example.ui.client

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Professional
import com.example.ui.components.OficiosButton
import com.example.ui.components.OficiosOutlinedButton
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
import com.example.ui.theme.oficiosTextFieldColors
import com.example.viewmodel.CreateRequestUiState
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.ProfessionalDetailUiState
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfessionalDetailScreen(viewModel: OficiosViewModel) {
    val detailState by viewModel.professionalDetailState.collectAsState()
    val fallbackPro by viewModel.selectedProfessional.collectAsState()

    val currentPro = when (val state = detailState) {
        is ProfessionalDetailUiState.Success -> state.professional
        else -> fallbackPro
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Perfil del profesional",
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

        when {
            detailState is ProfessionalDetailUiState.Loading && currentPro == null -> {
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
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando perfil del profesional...",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            detailState is ProfessionalDetailUiState.Error && currentPro == null -> {
                val errorMsg = (detailState as ProfessionalDetailUiState.Error).message
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
                                text = "No se pudo cargar el perfil",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMsg,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OficiosButton(
                                text = "Reintentar",
                                onClick = { viewModel.retryLoadProfessionalDetail() },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "detail_retry_btn"
                            )
                        }
                    }
                }
            }
            currentPro == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Profesional no encontrado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OficiosButton(
                                text = "Volver al inicio",
                                onClick = { viewModel.navigateTo(Screen.ClientHome) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "detail_empty_back_btn"
                            )
                        }
                    }
                }
            }
            else -> {
                val pro = currentPro
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Header Card
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
                                initial = pro.avatarInitial,
                                size = 72.dp,
                                backgroundColor = PrimaryNavy,
                                textColor = AccentCyan
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pro.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verificado",
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = pro.profession,
                                fontSize = 15.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats row: Rating, Reseñas, Disponibilidad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (pro.rating > 0.0) pro.rating.toString() else "Nuevo",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                    }
                                    Text(text = "Calificación", fontSize = 12.sp, color = TextSecondary)
                                }

                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(1.dp)
                                        .background(BorderSubtle)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${pro.servicesCompleted}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                    Text(text = "Reseñas", fontSize = 12.sp, color = TextSecondary)
                                }

                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(1.dp)
                                        .background(BorderSubtle)
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (pro.available) Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
                                    ) {
                                        Text(
                                            text = if (pro.available) "Disponible" else "Ocupado",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (pro.available) Color(0xFF065F46) else Color(0xFF6B7280),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(text = "Estado", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sobre el profesional
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Sobre el profesional",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pro.description.ifBlank { "Profesional de confianza verificado para la atención y resolución de servicios en el hogar." },
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = SecondaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${pro.experienceYears} ${if (pro.experienceYears == 1) "año" else "años"} exp.",
                                        fontSize = 13.sp,
                                        color = PrimaryNavy,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = SecondaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pro.locationName.ifBlank { "Medellín" },
                                        fontSize = 13.sp,
                                        color = PrimaryNavy,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Servicios y oficios ofrecidos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Servicios ofrecidos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (pro.servicesList.isNotEmpty()) {
                                pro.servicesList.forEach { sItem ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(22.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFD6F2F5)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = SecondaryTeal,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = sItem.categoryName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PrimaryNavy
                                            )
                                            if (sItem.serviceDescription.isNotBlank() && sItem.serviceDescription != sItem.categoryName) {
                                                Text(
                                                    text = sItem.serviceDescription,
                                                    fontSize = 13.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                pro.servicesOffered.forEach { serv ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(20.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFD6F2F5)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = SecondaryTeal,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = serv,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reseñas de clientes
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
                                    text = "Reseñas de clientes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                                Text(
                                    text = "${pro.reviews.size} opiniones",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (pro.reviews.isNotEmpty()) {
                                pro.reviews.forEach { rev ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rev.clientName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryNavy
                                            )
                                            RatingStars(rating = rev.rating, starSize = 14.dp)
                                        }
                                        if (rev.comment.isNotBlank()) {
                                            Text(
                                                text = rev.comment,
                                                fontSize = 13.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = rev.date,
                                            fontSize = 11.sp,
                                            color = TextSecondary.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundSoft,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Aún no hay reseñas registradas para este profesional. Al completar una solicitud podrás ser el primero en calificar su trabajo.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bottom Sticky CTA Button: Solicitar servicio
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
                        OficiosButton(
                            text = "Solicitar servicio",
                            onClick = {
                                viewModel.startServiceRequest(pro.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "detail_request_service_btn"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRequestServiceScreen(viewModel: OficiosViewModel) {
    val pro by viewModel.selectedProfessional.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCatId by viewModel.selectedCategoryIdForRequest.collectAsState()
    val formError by viewModel.requestFormError.collectAsState()
    val createState by viewModel.createRequestState.collectAsState()

    val draftDesc by viewModel.draftDescription.collectAsState()
    val draftDate by viewModel.draftDate.collectAsState()
    val draftTime by viewModel.draftTime.collectAsState()
    val draftLocation by viewModel.draftLocation.collectAsState()

    var desc by remember(draftDesc) { mutableStateOf(draftDesc) }
    var date by remember(draftDate) { mutableStateOf(draftDate) }
    var time by remember(draftTime) { mutableStateOf(draftTime) }
    var loc by remember(draftLocation) { mutableStateOf(draftLocation) }

    if (pro == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSoft)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Solicitar servicio",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No se encontró el profesional seleccionado.",
                        fontSize = 15.sp,
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
        }
        return
    }

    val professional = pro!!

    // Available services/categories offered by this professional
    val availableServices = remember(professional, categories) {
        if (professional.servicesList.isNotEmpty()) {
            professional.servicesList
        } else {
            // Fallback to categories matching professional trade
            val matching = categories.filter { cat ->
                cat.id in professional.categoryIds ||
                    cat.name.contains(professional.profession, ignoreCase = true) ||
                    professional.profession.contains(cat.name, ignoreCase = true)
            }
            if (matching.isNotEmpty()) {
                matching.map {
                    com.example.model.ProfessionalServiceItem(
                        id = it.id,
                        categoryId = it.id,
                        categoryName = it.name,
                        serviceDescription = "Servicio de ${it.name}"
                    )
                }
            } else {
                categories.take(3).map {
                    com.example.model.ProfessionalServiceItem(
                        id = it.id,
                        categoryId = it.id,
                        categoryName = it.name,
                        serviceDescription = "Servicio de ${it.name}"
                    )
                }
            }
        }
    }

    // Auto-select if single service and nothing selected yet
    LaunchedEffect(selectedCatId, availableServices) {
        if (selectedCatId == null && availableServices.size == 1) {
            viewModel.selectServiceCategory(availableServices.first().categoryId)
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
                    text = "Solicitar servicio",
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
            // Selected Pro summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        initial = professional.avatarInitial,
                        size = 48.dp,
                        backgroundColor = PrimaryNavy,
                        textColor = AccentCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = professional.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = professional.profession,
                            fontSize = 13.sp,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                        if (professional.locationName.isNotBlank()) {
                            Text(
                                text = "Zona: ${professional.locationName}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Servicio / Especialidad selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Servicio requerido *",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Selecciona el tipo de trabajo a realizar:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableServices.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { sItem ->
                                    val isSelected = selectedCatId == sItem.categoryId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.selectServiceCategory(sItem.categoryId)
                                        },
                                        label = {
                                            Text(
                                                text = sItem.categoryName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SecondaryTeal,
                                            selectedLabelColor = SurfaceWhite,
                                            selectedLeadingIconColor = SurfaceWhite,
                                            containerColor = BackgroundSoft,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Problem description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Describe el problema o necesidad *",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Sé lo más claro posible para que el profesional prepare las herramientas adecuadas.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        placeholder = { Text("Ej: Necesito reparar una fuga de agua debajo del lavaplatos...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("request_desc_input"),
                        colors = oficiosTextFieldColors()
                    )
                }
            }

            // Ubicación del servicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ubicación del servicio *",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = loc,
                        onValueChange = { loc = it },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryTeal)
                        },
                        placeholder = { Text("Dirección o barrio (Ej: Laureles, Medellín)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_location_input"),
                        singleLine = true,
                        colors = oficiosTextFieldColors()
                    )
                }
            }

            // Date & Time pickers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fecha y hora preferida",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Fecha estimada") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_date_input"),
                        singleLine = true,
                        colors = oficiosTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Horario sugerido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_time_input"),
                        singleLine = true,
                        colors = oficiosTextFieldColors()
                    )
                }
            }

            // Error display banner if any
            if (formError != null) {
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
                            text = formError ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            if (createState is CreateRequestUiState.Error) {
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
                            text = (createState as CreateRequestUiState.Error).message,
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        // Bottom Bar: Continuar solicitud
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
                val isLoading = createState is CreateRequestUiState.Loading
                OficiosButton(
                    text = if (isLoading) "Enviando solicitud..." else "Continuar con la solicitud",
                    onClick = {
                        viewModel.validateAndProceedToConfirm(
                            description = desc,
                            date = date,
                            time = time,
                            location = loc,
                            categoryId = selectedCatId
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "continue_request_btn"
                )
            }
        }
    }
}
