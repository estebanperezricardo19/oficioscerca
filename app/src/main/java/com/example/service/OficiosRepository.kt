package com.example.service

import com.example.data.MockData
import com.example.model.AppNotification
import com.example.model.Professional
import com.example.model.Review
import com.example.model.ServiceCategory
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.model.SupabaseCategoryDto
import com.example.model.SupabaseNotificationDto
import com.example.model.SupabaseProfessionalDto
import com.example.model.SupabaseProfessionalInsertDto
import com.example.model.SupabaseProfileDto
import com.example.model.SupabaseReviewDto
import com.example.model.SupabaseServiceRequestDto
import com.example.model.UserProfile
import com.example.model.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Status of the live Supabase connection check
 */
sealed class SupabaseConnectionStatus {
    data object Idle : SupabaseConnectionStatus()
    data object Checking : SupabaseConnectionStatus()
    data class Connected(
        val categoriesCount: Int,
        val professionalsCount: Int = 0,
        val message: String
    ) : SupabaseConnectionStatus()
    data class Error(val message: String) : SupabaseConnectionStatus()
}

/**
 * Repository interface and implementation.
 * Integrates live Supabase querying for categories, professionals, service_requests, reviews, and notifications
 * with robust local mock fallback.
 */
interface OficiosRepository {
    val currentUser: StateFlow<UserProfile>
    val categories: StateFlow<List<ServiceCategory>>
    val professionals: StateFlow<List<Professional>>
    val requests: StateFlow<List<ServiceRequest>>
    val notifications: StateFlow<List<AppNotification>>
    val supabaseStatus: StateFlow<SupabaseConnectionStatus>

    suspend fun checkAndLoadSupabaseCategories(): Result<List<ServiceCategory>>
    suspend fun checkAndLoadSupabaseProfessionals(): Result<List<Professional>>
    suspend fun checkAndLoadSupabaseRequests(): Result<List<ServiceRequest>>
    suspend fun checkAndLoadSupabaseReviews(): Result<List<Review>>
    suspend fun checkAndLoadSupabaseNotifications(): Result<List<AppNotification>>
    suspend fun syncAllFromSupabase()
    suspend fun loadCurrentUserProfile(): Result<UserProfile>

    suspend fun createProfileInSupabase(
        userId: String,
        email: String,
        name: String,
        phone: String,
        role: UserRole,
        location: String,
        profession: String = "Plomero",
        categoryId: String? = null
    ): Result<UserProfile>

    suspend fun createProfessionalInSupabase(
        profileId: String,
        location: String,
        profession: String = "Plomero",
        description: String = "Servicios confiables para el hogar",
        experienceYears: Int = 1,
        categoryId: String? = null
    ): Result<Unit>

    fun switchUserRole(role: UserRole)
    suspend fun updateClientProfile(name: String, phone: String, location: String): Result<UserProfile>
    suspend fun updateWorkerProfile(name: String, phone: String, profession: String, description: String, experienceYears: Int, available: Boolean, location: String): Result<UserProfile>
    suspend fun getProfessionalDetail(professionalId: String): Result<Professional>
    suspend fun loadReviewsForProfessional(professionalId: String): Result<List<Review>>
    suspend fun createServiceRequest(
        professionalId: String,
        description: String,
        scheduledDate: String,
        scheduledTime: String,
        location: String,
        categoryId: String? = null
    ): Result<ServiceRequest>
    suspend fun getServiceRequestDetail(requestId: String): Result<ServiceRequest>
    suspend fun updateRequestStatus(requestId: String, newStatus: ServiceStatus): Result<ServiceRequest>
    suspend fun submitReview(requestId: String, rating: Double, comment: String): Result<Review>
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit>
    fun clearSession()
}

class InMemoryOficiosRepository : OficiosRepository {

    private val _currentUser = MutableStateFlow(MockData.defaultClientUser)
    override val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _categories = MutableStateFlow(MockData.categories)
    override val categories: StateFlow<List<ServiceCategory>> = _categories.asStateFlow()

    private val _professionals = MutableStateFlow(MockData.professionals)
    override val professionals: StateFlow<List<Professional>> = _professionals.asStateFlow()

    private val _requests = MutableStateFlow(MockData.initialRequests)
    override val requests: StateFlow<List<ServiceRequest>> = _requests.asStateFlow()

    private val _notifications = MutableStateFlow(MockData.initialNotifications)
    override val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _supabaseStatus = MutableStateFlow<SupabaseConnectionStatus>(SupabaseConnectionStatus.Idle)
    override val supabaseStatus: StateFlow<SupabaseConnectionStatus> = _supabaseStatus.asStateFlow()

    private val reviewsByRequestId = java.util.concurrent.ConcurrentHashMap<String, Review>()

    private fun hasAuthenticatedSession(): Boolean =
        SupabaseClient.isConfigured && AuthService.currentUserId() != null

    override suspend fun syncAllFromSupabase() {
        checkAndLoadSupabaseCategories()
        checkAndLoadSupabaseProfessionals()
        checkAndLoadSupabaseRequests()
        checkAndLoadSupabaseReviews()
        checkAndLoadSupabaseNotifications()
    }

    override suspend fun loadCurrentUserProfile(): Result<UserProfile> {
        if (!SupabaseClient.isConfigured) {
            return Result.failure(IllegalStateException("Supabase no está configurado."))
        }
        return try {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            val userId = user?.id
                ?: return Result.failure(
                    IllegalStateException("No hay una sesión iniciada.")
                )

            android.util.Log.d("OficiosAuth", "Consultando perfil en Supabase para userId: $userId")

            val profiles = SupabaseClient.client
                .from("profiles")
                .select(columns = Columns.list("id, name, phone, role, location")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<SupabaseProfileDto>()

            var profile = profiles.firstOrNull()
            if (profile == null) {
                val metaRole = user.userMetadata?.get("role")?.jsonPrimitive?.contentOrNull
                val metaName = user.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                val metaPhone = user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull

                if (!metaRole.isNullOrBlank()) {
                    android.util.Log.d("OficiosAuth", "Perfil no encontrado en profiles, pero presente en userMetadata (rol: $metaRole). Autoreparando registro...")
                    val selfHealRole = when (metaRole.trim().uppercase()) {
                        "CLIENT" -> UserRole.CLIENT
                        "WORKER", "PROFESSIONAL" -> UserRole.WORKER
                        else -> UserRole.CLIENT
                    }
                    val selfHealName = metaName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                        ?: "Usuario"

                    val createRes = createProfileInSupabase(
                        userId = userId,
                        email = user.email.orEmpty(),
                        name = selfHealName,
                        phone = metaPhone.orEmpty(),
                        role = selfHealRole,
                        location = "Medellín"
                    )
                    if (createRes.isSuccess) {
                        return createRes
                    }
                }

                return Result.failure(
                    IllegalStateException("No se encontró ningún registro en profiles para el usuario actual.")
                )
            }

            // Strict role consistency: CLIENT (client) and WORKER (worker) only
            // Gracefully normalize legacy "PROFESSIONAL" from Supabase profiles to official WORKER role
            val role = when (profile.role?.trim()?.uppercase()) {
                "CLIENT" -> UserRole.CLIENT
                "WORKER", "PROFESSIONAL" -> UserRole.WORKER
                else -> return Result.failure(
                    IllegalStateException("El perfil tiene un rol desconocido: ${profile.role}")
                )
            }

            val nameParts = (profile.name ?: "").trim().split(Regex("\\s+"))
            val name = nameParts.firstOrNull().orEmpty()
            val lastName = nameParts.drop(1).joinToString(" ")

            val userProfile = UserProfile(
                id = profile.id,
                name = name.ifEmpty { "Usuario" },
                lastName = lastName,
                email = user.email.orEmpty(),
                phone = profile.phone.orEmpty(),
                role = role,
                location = profile.location.orEmpty()
            )

            _currentUser.value = userProfile
            android.util.Log.d("OficiosAuth", "Perfil cargado con éxito. Rol detectado: $role")

            // If user is a worker, synchronize their specific record from professionals table
            if (role == UserRole.WORKER && SupabaseClient.isConfigured) {
                try {
                    val proRecords = SupabaseClient.client
                        .from("professionals")
                        .select(columns = Columns.raw("*, professional_services(id, category_id, service_description)")) {
                            filter { eq("profile_id", userId) }
                        }
                        .decodeList<SupabaseProfessionalDto>()
                    val proDto = proRecords.firstOrNull()
                    if (proDto != null) {
                        val catIds = proDto.professionalServices.mapNotNull { it.categoryId }.ifEmpty { listOf("330571e3-9665-4088-92da-0f2c98d7f8f5") }
                        val proModel = Professional(
                            id = proDto.id,
                            userId = userId,
                            name = "${userProfile.name} ${userProfile.lastName}".trim(),
                            profession = proDto.profession ?: "Trabajador profesional",
                            description = proDto.description ?: "Servicios profesionales confiables",
                            experienceYears = proDto.experienceYears ?: 1,
                            rating = proDto.rating?.takeIf { it > 0.0 } ?: 5.0,
                            servicesCompleted = proDto.totalReviews ?: 0,
                            distanceKm = 0.5,
                            available = proDto.available ?: true,
                            categoryIds = catIds,
                            servicesOffered = proDto.professionalServices.mapNotNull { it.serviceDescription }.ifEmpty { listOf(proDto.profession ?: "Servicios") },
                            locationName = proDto.location ?: userProfile.location,
                            latitude = 6.2442,
                            longitude = -75.5925,
                            avatarInitial = userProfile.name.firstOrNull()?.toString() ?: "T"
                        )
                        _professionals.update { list ->
                            val others = list.filter { it.userId != userId && it.id != proDto.id }
                            listOf(proModel) + others
                        }
                        android.util.Log.d("OficiosAuth", "Datos del profesional trabajador sincronizados para userId: $userId")
                    }
                } catch (pe: Exception) {
                    android.util.Log.e("OficiosAuth", "Error al consultar professionals para el trabajador: ${pe.message}")
                }
            }

            Result.success(userProfile)
        } catch (e: Exception) {
            android.util.Log.e("OficiosAuth", "Excepción al consultar profiles en Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun checkAndLoadSupabaseCategories(): Result<List<ServiceCategory>> {
        if (!SupabaseClient.isConfigured) {
            val err = "Supabase no está configurado en las variables de entorno."
            _supabaseStatus.value = SupabaseConnectionStatus.Error(err)
            return Result.failure(IllegalStateException(err))
        }

        _supabaseStatus.value = SupabaseConnectionStatus.Checking

        return try {
            val dtoList = SupabaseClient.client
                .from("categories")
                .select(columns = Columns.raw("id, name, description, icon, created_at"))
                .decodeList<SupabaseCategoryDto>()

            if (dtoList.isNotEmpty()) {
                val mapped = dtoList.map { dto ->
                    ServiceCategory(
                        id = dto.id,
                        name = dto.name,
                        iconName = dto.icon ?: "handyman",
                        description = dto.description ?: "Servicios profesionales",
                        suggestedKeywords = listOf("Reparación", "Mantenimiento", "Instalación")
                    )
                }
                _categories.value = mapped
                val currentProCount = _professionals.value.size
                val success = SupabaseConnectionStatus.Connected(
                    categoriesCount = mapped.size,
                    professionalsCount = currentProCount,
                    message = "Conectado a Supabase: ${mapped.size} categorías recibidas de la base de datos."
                )
                _supabaseStatus.value = success
                Result.success(mapped)
            } else {
                val emptyMsg = "Conexión exitosa, pero la tabla categories no contiene filas."
                _supabaseStatus.value = SupabaseConnectionStatus.Connected(0, _professionals.value.size, emptyMsg)
                Result.success(MockData.categories)
            }
        } catch (e: Exception) {
            val errorMsg = "Error al consultar categories en Supabase: ${e.message ?: e.javaClass.simpleName}"
            _supabaseStatus.value = SupabaseConnectionStatus.Error(errorMsg)
            Result.failure(e)
        }
    }

    override suspend fun checkAndLoadSupabaseProfessionals(): Result<List<Professional>> {
        if (!SupabaseClient.isConfigured) {
            val err = "Supabase no está configurado en las variables de entorno."
            return Result.failure(IllegalStateException(err))
        }

        return try {
            val session = AuthService.currentSession()
            val authUid = AuthService.currentUserId()
            val hasSession = session != null || authUid != null
            android.util.Log.d("OficiosDiagnostico", "========== INICIO DIAGNÓSTICO PROFESIONALES ==========")
            android.util.Log.d("OficiosDiagnostico", "0. ESTADO DE SESIÓN CLIENTE: existe_sesion=$hasSession, auth.uid=${authUid ?: "ANÓNIMO/SIN_SESIÓN"}")

            // PRUEBA ESPECÍFICA SOLICITADA: Consultar directamente el perfil de Carlos Prueba
            try {
                val testCarlos = SupabaseClient.client
                    .from("profiles")
                    .select(columns = Columns.raw("id, name, phone, role, location")) {
                        filter {
                            eq("id", "eccd83ba-fb44-4e57-a146-4a758c9b7e29")
                        }
                    }
                    .decodeList<SupabaseProfileDto>()
                val carlosPro = testCarlos.firstOrNull()
                android.util.Log.d(
                    "OficiosDiagnostico",
                    "0.1. PRUEBA ESPECÍFICA Carlos Prueba (eccd83ba...): filas_obtenidas=${testCarlos.size}, " +
                        "encontrado=${carlosPro != null}, name='${carlosPro?.name}', role='${carlosPro?.role}'"
                )
            } catch (testEx: Exception) {
                android.util.Log.e("OficiosDiagnostico", "0.1. PRUEBA ESPECÍFICA Carlos Prueba ERROR: ${testEx.message}", testEx)
            }

            // Attempt 1: Fetch professionals with embedded profiles and professional_services
            // Real columns in profiles: id, name, phone, role, location
            val dtoList = try {
                android.util.Log.d("OficiosDiagnostico", "1. Consultando 'professionals' con join profiles e info de servicios")
                SupabaseClient.client
                    .from("professionals")
                    .select(
                        columns = Columns.raw("*, profiles(id, name, phone, role, location), professional_services(id, category_id, service_description, categories(id, name, icon))")
                    )
                    .decodeList<SupabaseProfessionalDto>()
            } catch (joinEx: Exception) {
                android.util.Log.w("OficiosDiagnostico", "Aviso: Join directo falló: ${joinEx.message}. Intentando con profiles!profile_id...")
                try {
                    SupabaseClient.client
                        .from("professionals")
                        .select(
                            columns = Columns.raw("*, profiles!profile_id(id, name, phone, role, location), professional_services(id, category_id, service_description, categories(id, name, icon))")
                        )
                        .decodeList<SupabaseProfessionalDto>()
                } catch (fkEx: Exception) {
                    android.util.Log.w("OficiosDiagnostico", "Aviso: Join profiles!profile_id falló: ${fkEx.message}. Consultando 'professionals' sin join embebido...")
                    try {
                        SupabaseClient.client
                            .from("professionals")
                            .select(
                                columns = Columns.raw("*, professional_services(id, category_id, service_description, categories(id, name, icon))")
                            )
                            .decodeList<SupabaseProfessionalDto>()
                    } catch (rawEx: Exception) {
                        android.util.Log.e("OficiosDiagnostico", "ERROR CRÍTICO al consultar tabla 'professionals': ${rawEx.message}", rawEx)
                        throw rawEx
                    }
                }
            }

            android.util.Log.d("OficiosDiagnostico", "1.1. Registros obtenidos de 'professionals': ${dtoList.size}")
            dtoList.forEachIndexed { index, dto ->
                val embeddedProfile = dto.profiles ?: dto.profile
                android.util.Log.d(
                    "OficiosDiagnostico",
                    "  [Profesional #$index] id=${dto.id}, profile_id=${dto.profileId}, profession='${dto.profession}', " +
                        "joined_profiles_presente=${embeddedProfile != null}, embedded_name='${embeddedProfile?.name}'"
                )
            }

            // Map to store profile records indexed by profile id
            val profileMap = mutableMapOf<String, SupabaseProfileDto>()

            // 1. Ingest any embedded profiles returned directly by the PostgREST join
            dtoList.forEach { dto ->
                val p = dto.profiles ?: dto.profile
                if (p != null) {
                    if (p.id.isNotBlank()) profileMap[p.id] = p
                    dto.profileId?.let { pid -> if (pid.isNotBlank()) profileMap[pid] = p }
                }
            }

            // 2. Query the profiles table for any worker profile IDs not yet resolved
            val targetProfileIds = dtoList.mapNotNull { it.profileId?.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            val missingProfileIds = targetProfileIds.filter { !profileMap.containsKey(it) }

            android.util.Log.d("OficiosDiagnostico", "2. Total profile_ids requeridos: ${targetProfileIds.size}, faltantes por consultar en 'profiles': ${missingProfileIds.size}")

            if (missingProfileIds.isNotEmpty()) {
                try {
                    android.util.Log.d("OficiosDiagnostico", "2.1. Consultando tabla 'profiles' para profile_ids=${missingProfileIds.joinToString()}")
                    val fetchedProfiles = SupabaseClient.client
                        .from("profiles")
                        .select(columns = Columns.raw("id, name, phone, role, location")) {
                            filter {
                                isIn("id", missingProfileIds)
                            }
                        }
                        .decodeList<SupabaseProfileDto>()

                    android.util.Log.d("OficiosDiagnostico", "2.2. Perfiles recuperados de 'profiles' (con filtro isIn): ${fetchedProfiles.size}")
                    fetchedProfiles.forEach { p ->
                        profileMap[p.id] = p
                        android.util.Log.d("OficiosDiagnostico", "  -> Perfil obtenido: id=${p.id}, name='${p.name}', role='${p.role}', location='${p.location}'")
                    }
                } catch (pe: Exception) {
                    android.util.Log.e("OficiosDiagnostico", "ERROR en consulta filtrada a 'profiles': ${pe.message}", pe)
                    try {
                        android.util.Log.d("OficiosDiagnostico", "2.3. Intentando consulta a 'profiles' sin filtro...")
                        val allProfiles = SupabaseClient.client
                            .from("profiles")
                            .select(columns = Columns.raw("id, name, phone, role, location"))
                            .decodeList<SupabaseProfileDto>()

                        android.util.Log.d("OficiosDiagnostico", "2.4. Perfiles recuperados de 'profiles' (sin filtro): ${allProfiles.size}")
                        allProfiles.forEach { p ->
                            profileMap[p.id] = p
                            android.util.Log.d("OficiosDiagnostico", "  -> Perfil general: id=${p.id}, name='${p.name}', role='${p.role}'")
                        }
                    } catch (pe2: Exception) {
                        android.util.Log.e("OficiosDiagnostico", "ERROR en consulta sin filtro a 'profiles': ${pe2.message}", pe2)
                        // Individual query fallback
                        for (pid in missingProfileIds) {
                            if (!profileMap.containsKey(pid)) {
                                try {
                                    val single = SupabaseClient.client
                                        .from("profiles")
                                        .select(columns = Columns.raw("id, name, phone, role, location")) {
                                            filter { eq("id", pid) }
                                        }
                                        .decodeList<SupabaseProfileDto>()
                                    single.firstOrNull()?.let { p ->
                                        profileMap[pid] = p
                                        android.util.Log.d("OficiosDiagnostico", "  -> Perfil individual obtenido para $pid: name='${p.name}', role='${p.role}'")
                                    } ?: run {
                                        android.util.Log.w("OficiosDiagnostico", "  -> Perfil individual para $pid NO RETORNÓ FILAS (0 resultados de Supabase)")
                                    }
                                } catch (singleEx: Exception) {
                                    android.util.Log.e("OficiosDiagnostico", "ERROR consultando perfil individual $pid: ${singleEx.message}", singleEx)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Fallback for the currently authenticated worker user
            val currentUser = _currentUser.value
            if (currentUser.id.isNotBlank() && currentUser.name.isNotBlank() && currentUser.name != "Usuario") {
                val currentFullName = "${currentUser.name} ${currentUser.lastName}".trim()
                if (!profileMap.containsKey(currentUser.id) || profileMap[currentUser.id]?.displayName.isNullOrBlank()) {
                    profileMap[currentUser.id] = SupabaseProfileDto(
                        id = currentUser.id,
                        name = currentFullName,
                        phone = currentUser.phone,
                        role = if (currentUser.role == UserRole.WORKER) "worker" else "client",
                        location = currentUser.location
                    )
                    android.util.Log.d("OficiosDiagnostico", "3. Perfil de sesión activa inyectado para currentUser: id=${currentUser.id}, name='$currentFullName'")
                }
            }

            android.util.Log.d("OficiosDiagnostico", "3.1. Total de perfiles en memoria para mapeo: ${profileMap.size}")

            if (dtoList.isNotEmpty()) {
                val mapped = dtoList.mapIndexed { idx, dto ->
                    val linkedProfile = dto.profileId?.let { profileMap[it] } ?: dto.profiles ?: dto.profile
                    val rawName = linkedProfile?.displayName?.trim()

                    val (resolvedName, diagnosticStatus) = when {
                        !rawName.isNullOrEmpty() -> {
                            rawName to "NOMBRE REAL ENCONTRADO EN PROFILES: '$rawName'"
                        }
                        dto.profileId == currentUser.id && currentUser.name.isNotBlank() && currentUser.name != "Usuario" -> {
                            val authName = "${currentUser.name} ${currentUser.lastName}".trim()
                            authName to "NOMBRE OBTENIDO DE SESIÓN ACTIVA (CURRENT_USER): '$authName'"
                        }
                        linkedProfile == null -> {
                            "Profesional de oficio" to "PERFIL NO ENCONTRADO EN MAPA NI EN JOIN (Supabase retornó 0 filas o bloqueado por RLS)"
                        }
                        else -> {
                            "Profesional de oficio" to "PERFIL ENCONTRADO PERO CAMPO NAME ESTÁ NULO O VACÍO EN BD"
                        }
                    }

                    android.util.Log.d(
                        "OficiosDiagnostico",
                        "  [Mapeo #$idx] Profesional id=${dto.id}, profile_id=${dto.profileId} -> $diagnosticStatus | Nombre final en tarjeta: '$resolvedName'"
                    )

                    val structuredServices = dto.professionalServices.mapNotNull { ps ->
                        val cId = ps.categoryId ?: return@mapNotNull null
                        val cName = ps.categories?.name
                            ?: _categories.value.find { it.id == cId }?.name
                            ?: "Servicio"
                        val sDesc = ps.serviceDescription?.takeIf { it.isNotBlank() } ?: cName
                        com.example.model.ProfessionalServiceItem(
                            id = ps.id,
                            categoryId = cId,
                            categoryName = cName,
                            serviceDescription = sDesc
                        )
                    }

                    val catIds = if (structuredServices.isNotEmpty()) {
                        structuredServices.map { it.categoryId }.distinct()
                    } else {
                        dto.professionalServices.mapNotNull { it.categoryId }.ifEmpty { emptyList() }
                    }

                    val serviceLabels = if (structuredServices.isNotEmpty()) {
                        structuredServices.map { it.serviceDescription.ifBlank { it.categoryName } }
                    } else {
                        _categories.value
                            .filter { it.id in catIds }
                            .map { it.name }
                            .ifEmpty {
                                dto.professionalServices.mapNotNull { it.serviceDescription }.ifEmpty {
                                    listOf(dto.profession ?: "Servicios para el hogar")
                                }
                            }
                    }

                    val rawRating = dto.rating ?: 0.0
                    val displayRating = if (rawRating < 0.0) 0.0 else Math.round(rawRating * 10.0) / 10.0

                    Professional(
                        id = dto.id,
                        userId = dto.profileId ?: dto.id,
                        name = resolvedName,
                        profession = dto.profession?.takeIf { it.isNotBlank() } ?: "Especialista",
                        description = dto.description?.takeIf { it.isNotBlank() }
                            ?: "Servicios profesionales independientes para el hogar.",
                        experienceYears = dto.experienceYears ?: 1,
                        rating = displayRating,
                        servicesCompleted = dto.totalReviews ?: 0,
                        distanceKm = 0.0,
                        available = dto.available ?: true,
                        categoryIds = catIds,
                        servicesOffered = serviceLabels,
                        servicesList = structuredServices,
                        locationName = dto.location?.takeIf { it.isNotBlank() }
                            ?: linkedProfile?.location?.takeIf { it.isNotBlank() }
                            ?: "Medellín",
                        latitude = 6.2442,
                        longitude = -75.5925,
                        reviews = emptyList(),
                        avatarInitial = resolvedName.firstOrNull()?.uppercase() ?: "P"
                    )
                }

                val realNameCount = mapped.count { it.name != "Profesional de oficio" }
                val fallbackCount = mapped.count { it.name == "Profesional de oficio" }
                android.util.Log.d(
                    "OficiosDiagnostico",
                    "4. RESUMEN DIAGNÓSTICO: Total profesionales=${mapped.size}, con nombre real=$realNameCount, con 'Profesional de oficio'=$fallbackCount"
                )
                android.util.Log.d("OficiosDiagnostico", "========== FIN DIAGNÓSTICO PROFESIONALES ==========")

                // Display real professionals loaded from Supabase database
                _professionals.value = mapped

                val currentCatCount = _categories.value.size
                _supabaseStatus.value = SupabaseConnectionStatus.Connected(
                    categoriesCount = currentCatCount,
                    professionalsCount = mapped.size,
                    message = "Conectado a Supabase: $currentCatCount categorías y ${mapped.size} profesionales sincronizados."
                )
                Result.success(mapped)
            } else {
                android.util.Log.d("OficiosDiagnostico", "4. La consulta de 'professionals' retornó 0 filas.")
                android.util.Log.d("OficiosDiagnostico", "========== FIN DIAGNÓSTICO PROFESIONALES ==========")
                val currentCatCount = _categories.value.size
                _professionals.value = emptyList()
                _supabaseStatus.value = SupabaseConnectionStatus.Connected(
                    categoriesCount = currentCatCount,
                    professionalsCount = 0,
                    message = "Conectado a Supabase ($currentCatCount categorías). Aún no hay profesionales registrados."
                )
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            val errorMsg = "Error al consultar professionals en Supabase: ${e.message ?: e.javaClass.simpleName}"
            android.util.Log.e("OficiosDiagnostico", "EXCEPCIÓN NO CONTROLADA EN checkAndLoadSupabaseProfessionals: $errorMsg", e)
            _supabaseStatus.value = SupabaseConnectionStatus.Error(errorMsg)
            Result.failure(e)
        }
    }

    override suspend fun checkAndLoadSupabaseRequests(): Result<List<ServiceRequest>> {
        val authUserId = AuthService.currentUserId()
        val isClient = _currentUser.value.role == UserRole.CLIENT

        if (!SupabaseClient.isConfigured || authUserId == null) {
            // Local fallback for test or offline demo mode
            val filtered = if (isClient) {
                val activeClientId = authUserId ?: _currentUser.value.id
                _requests.value.filter { it.clientId == activeClientId }
            } else {
                val workerPro = _professionals.value.find { it.userId == _currentUser.value.id }
                val targetProId = workerPro?.id
                    ?: if (_currentUser.value.id == MockData.defaultWorkerUser.id) "pro_mateo" else ""
                if (targetProId.isBlank()) emptyList()
                else _requests.value.filter { it.professionalId == targetProId }
            }.sortedByDescending { it.createdAt }
            return Result.success(filtered)
        }

        return try {
            val columnsJoined = "id, client_id, professional_id, category_id, description, location, status, scheduled_at, created_at, profiles(id, name, phone), professionals(id, profession, profile_id, location, profiles(id, name, phone)), categories(id, name, icon)"
            val columnsBasic = "id, client_id, professional_id, category_id, description, location, status, scheduled_at, created_at"

            // If user is a worker, determine their professional_id in Supabase
            val workerProfessionalId = if (!isClient) {
                var proId = _professionals.value.find { it.userId == authUserId }?.id
                if (proId == null) {
                    try {
                        val proList = SupabaseClient.client
                            .from("professionals")
                            .select(columns = Columns.list("id")) {
                                filter { eq("profile_id", authUserId) }
                            }
                            .decodeList<SupabaseProfessionalDto>()
                        proId = proList.firstOrNull()?.id
                    } catch (e: Exception) {
                        android.util.Log.w("OficiosSupabase", "No se pudo consultar professional_id para worker $authUserId: ${e.message}")
                    }
                }
                proId
            } else null

            if (!isClient && workerProfessionalId == null) {
                _requests.value = emptyList()
                return Result.success(emptyList())
            }

            android.util.Log.d("OficiosSupabase", "Consultando solicitudes en Supabase. isClient=$isClient, authUserId=$authUserId, workerProId=$workerProfessionalId")

            // Refresh known reviews for this user/professional so ratingGiven & reviewComment stay accurate
            try {
                val relatedReviews = SupabaseClient.client
                    .from("reviews")
                    .select(columns = Columns.raw("id, client_id, professional_id, request_id, rating, comment, created_at")) {
                        filter {
                            if (isClient) eq("client_id", authUserId)
                            else eq("professional_id", workerProfessionalId!!)
                        }
                    }
                    .decodeList<SupabaseReviewDto>()
                relatedReviews.forEach { revDto ->
                    val reqId = revDto.requestId ?: return@forEach
                    val rId = revDto.id ?: return@forEach
                    reviewsByRequestId[reqId] = Review(
                        id = rId,
                        serviceRequestId = reqId,
                        clientName = "Cliente verificado",
                        rating = revDto.rating ?: 5.0,
                        comment = revDto.comment.orEmpty(),
                        date = revDto.createdAt?.take(10) ?: "Reciente"
                    )
                }
            } catch (re: Exception) {
                android.util.Log.w("OficiosSupabase", "No se pudieron precargar reseñas relacionadas: ${re.message}")
            }

            val dtoList = try {
                SupabaseClient.client
                    .from("service_requests")
                    .select(columns = Columns.raw(columnsJoined)) {
                        filter {
                            if (isClient) {
                                eq("client_id", authUserId)
                            } else {
                                eq("professional_id", workerProfessionalId!!)
                            }
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<SupabaseServiceRequestDto>()
            } catch (joinErr: Exception) {
                android.util.Log.w("OficiosSupabase", "Consulta con join embebido falló (${joinErr.message}), intentando consulta base...", joinErr)
                SupabaseClient.client
                    .from("service_requests")
                    .select(columns = Columns.raw(columnsBasic)) {
                        filter {
                            if (isClient) {
                                eq("client_id", authUserId)
                            } else {
                                eq("professional_id", workerProfessionalId!!)
                            }
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<SupabaseServiceRequestDto>()
            }

            val existingMap = _requests.value.associateBy { it.id }

            val mapped = dtoList.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val proJoined = dto.professionals
                val catJoined = dto.categories
                val clientProfileJoined = dto.profiles
                val previous = existingMap[id]
                val linkedReview = reviewsByRequestId[id]

                val proFallback = _professionals.value.find { it.id == dto.professionalId || it.userId == proJoined?.profileId }
                val catFallback = _categories.value.find { it.id == dto.categoryId }

                val realProName = proJoined?.profiles?.displayName
                    ?: proJoined?.profiles?.name
                    ?: proFallback?.name
                    ?: previous?.professionalName
                    ?: "Profesional de oficio"

                val realProfession = proJoined?.profession
                    ?: proFallback?.profession
                    ?: previous?.professionalProfession
                    ?: "Especialista"

                val realCatName = catJoined?.name
                    ?: catFallback?.name
                    ?: previous?.categoryName
                    ?: "Servicio general"

                val clientDisplayName = if (isClient) {
                    "${_currentUser.value.name} ${_currentUser.value.lastName}".trim().ifBlank { "Cliente" }
                } else {
                    clientProfileJoined?.displayName?.takeIf { it.isNotBlank() }
                        ?: clientProfileJoined?.name?.takeIf { it.isNotBlank() }
                        ?: previous?.clientName
                        ?: "Cliente de OficiosCerca"
                }

                val clientPhone = if (isClient) {
                    _currentUser.value.phone.ifBlank { "No disponible" }
                } else {
                    clientProfileJoined?.phone?.takeIf { it.isNotBlank() }
                        ?: previous?.clientPhone
                        ?: "No disponible"
                }

                ServiceRequest(
                    id = id,
                    clientId = dto.clientId ?: authUserId,
                    clientName = clientDisplayName,
                    clientPhone = clientPhone,
                    professionalId = proJoined?.id ?: dto.professionalId ?: proFallback?.id ?: "",
                    professionalName = realProName,
                    professionalProfession = realProfession,
                    categoryId = catJoined?.id ?: dto.categoryId ?: catFallback?.id ?: "",
                    categoryName = realCatName,
                    description = dto.description?.takeIf { it.isNotBlank() } ?: "Servicio solicitado",
                    location = dto.location?.takeIf { it.isNotBlank() } ?: _currentUser.value.location.ifBlank { "Medellín" },
                    scheduledDate = dto.scheduledAt?.take(10) ?: previous?.scheduledDate ?: "Por coordinar",
                    scheduledTime = previous?.scheduledTime ?: "Por confirmar",
                    status = ServiceStatus.fromCode(dto.status),
                    ratingGiven = linkedReview?.rating ?: previous?.ratingGiven,
                    reviewComment = linkedReview?.comment ?: previous?.reviewComment,
                    createdAt = dto.createdAt ?: ""
                )
            }.sortedByDescending { it.createdAt }

            _requests.value = mapped
            android.util.Log.d("OficiosSupabase", "Solicitudes cargadas de Supabase: ${mapped.size} encontradas")
            Result.success(mapped)
        } catch (e: Exception) {
            android.util.Log.e("OficiosSupabase", "Error al cargar solicitudes de Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getServiceRequestDetail(requestId: String): Result<ServiceRequest> {
        val cached = _requests.value.find { it.id == requestId }
        if (!SupabaseClient.isConfigured || AuthService.currentUserId() == null) {
            return if (cached != null) Result.success(cached)
            else Result.failure(NoSuchElementException("Solicitud no encontrada."))
        }
        return try {
            val columnsJoined = "id, client_id, professional_id, category_id, description, location, status, scheduled_at, created_at, profiles(id, name, phone), professionals(id, profession, profile_id, location, profiles(id, name, phone)), categories(id, name, icon)"
            val dto = SupabaseClient.client
                .from("service_requests")
                .select(columns = Columns.raw(columnsJoined)) {
                    filter {
                        eq("id", requestId)
                    }
                }
                .decodeSingleOrNull<SupabaseServiceRequestDto>()

            if (dto != null) {
                val proJoined = dto.professionals
                val catJoined = dto.categories
                val clientProfileJoined = dto.profiles
                val proFallback = _professionals.value.find { it.id == dto.professionalId || it.userId == proJoined?.profileId }
                val catFallback = _categories.value.find { it.id == dto.categoryId }

                val realProName = proJoined?.profiles?.displayName
                    ?: proJoined?.profiles?.name
                    ?: proFallback?.name
                    ?: cached?.professionalName
                    ?: "Profesional de oficio"

                val realProfession = proJoined?.profession
                    ?: proFallback?.profession
                    ?: cached?.professionalProfession
                    ?: "Especialista"

                val realCatName = catJoined?.name
                    ?: catFallback?.name
                    ?: cached?.categoryName
                    ?: "Servicio general"

                val isClient = _currentUser.value.role == UserRole.CLIENT
                val clientDisplayName = if (isClient) {
                    cached?.clientName ?: "${_currentUser.value.name} ${_currentUser.value.lastName}".trim().ifBlank { "Cliente" }
                } else {
                    clientProfileJoined?.displayName?.takeIf { it.isNotBlank() }
                        ?: clientProfileJoined?.name?.takeIf { it.isNotBlank() }
                        ?: cached?.clientName
                        ?: "Cliente de OficiosCerca"
                }

                val clientPhone = if (isClient) {
                    cached?.clientPhone ?: _currentUser.value.phone.ifBlank { "300 123 4567" }
                } else {
                    clientProfileJoined?.phone?.takeIf { it.isNotBlank() }
                        ?: cached?.clientPhone
                        ?: "No disponible"
                }

                val linkedReview = reviewsByRequestId[requestId]
                val req = ServiceRequest(
                    id = dto.id ?: requestId,
                    clientId = dto.clientId ?: _currentUser.value.id,
                    clientName = clientDisplayName,
                    clientPhone = clientPhone,
                    professionalId = proJoined?.id ?: dto.professionalId ?: cached?.professionalId ?: "",
                    professionalName = realProName,
                    professionalProfession = realProfession,
                    categoryId = catJoined?.id ?: dto.categoryId ?: cached?.categoryId ?: "",
                    categoryName = realCatName,
                    description = dto.description ?: cached?.description ?: "Servicio solicitado",
                    location = dto.location ?: cached?.location ?: _currentUser.value.location,
                    scheduledDate = dto.scheduledAt?.take(10) ?: cached?.scheduledDate ?: "Por coordinar",
                    scheduledTime = cached?.scheduledTime ?: "Por confirmar",
                    status = ServiceStatus.fromCode(dto.status ?: "pending"),
                    ratingGiven = linkedReview?.rating ?: cached?.ratingGiven,
                    reviewComment = linkedReview?.comment ?: cached?.reviewComment,
                    createdAt = dto.createdAt ?: cached?.createdAt ?: ""
                )

                _requests.update { list ->
                    val others = list.filter { it.id != requestId }
                    listOf(req) + others
                }
                Result.success(req)
            } else if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(NoSuchElementException("Solicitud no encontrada en Supabase."))
            }
        } catch (e: Exception) {
            if (cached != null) Result.success(cached)
            else Result.failure(e)
        }
    }

    override suspend fun checkAndLoadSupabaseReviews(): Result<List<Review>> {
        if (!SupabaseClient.isConfigured) return Result.success(emptyList())
        return try {
            val dtoList = SupabaseClient.client
                .from("reviews")
                .select(columns = Columns.raw("id, client_id, professional_id, request_id, rating, comment, created_at"))
                .decodeList<SupabaseReviewDto>()
            val mapped = dtoList.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val reqId = dto.requestId ?: "req_hist_1"
                val rev = Review(
                    id = id,
                    serviceRequestId = reqId,
                    clientName = "Cliente verificado",
                    rating = dto.rating ?: 5.0,
                    comment = dto.comment ?: "Buen trabajo",
                    date = dto.createdAt?.take(10) ?: "Reciente"
                )
                if (!dto.requestId.isNullOrBlank()) {
                    reviewsByRequestId[dto.requestId] = rev
                }
                rev
            }
            if (mapped.isNotEmpty()) {
                android.util.Log.d("OficiosSupabase", "Reseñas cargadas desde Supabase: ${mapped.size}")
                _professionals.update { list ->
                    list.map { pro ->
                        val matching = mapped.filter { rev ->
                            dtoList.any { d -> d.id == rev.id && d.professionalId == pro.id }
                        }
                        if (matching.isNotEmpty()) {
                            val newAvg = matching.map { it.rating }.average()
                            val roundedAvg = Math.round(newAvg * 10.0) / 10.0
                            pro.copy(
                                reviews = matching,
                                rating = roundedAvg,
                                servicesCompleted = matching.size
                            )
                        } else pro
                    }
                }
                _requests.update { reqs ->
                    reqs.map { req ->
                        val rev = reviewsByRequestId[req.id]
                        if (rev != null) req.copy(ratingGiven = rev.rating, reviewComment = rev.comment)
                        else req
                    }
                }
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAndLoadSupabaseNotifications(): Result<List<AppNotification>> {
        val authUserId = AuthService.currentUserId()
        if (!SupabaseClient.isConfigured || authUserId == null) {
            return Result.success(_notifications.value)
        }
        return try {
            val dtoList = SupabaseClient.client
                .from("notifications")
                .select(columns = Columns.raw("id, profile_id, title, message, is_read, type, created_at")) {
                    filter { eq("profile_id", authUserId) }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<SupabaseNotificationDto>()

            val mapped = dtoList.map { dto ->
                AppNotification(
                    id = dto.id,
                    title = dto.title,
                    message = dto.message,
                    read = dto.isRead,
                    date = dto.createdAt?.take(16)?.replace("T", " ") ?: "Reciente"
                )
            }
            _notifications.value = mapped
            android.util.Log.d("OficiosSupabase", "Notificaciones cargadas desde Supabase: ${mapped.size}")
            Result.success(mapped)
        } catch (e: Exception) {
            android.util.Log.e("OficiosSupabase", "Error al cargar notificaciones: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun clearSession() {
        _currentUser.value = UserProfile(
            id = "",
            name = "",
            lastName = "",
            email = "",
            phone = "",
            role = UserRole.CLIENT,
            location = ""
        )
        _requests.value = emptyList()
        _notifications.value = emptyList()
        reviewsByRequestId.clear()
    }

    override suspend fun createProfileInSupabase(
        userId: String,
        email: String,
        name: String,
        phone: String,
        role: UserRole,
        location: String,
        profession: String,
        categoryId: String?
    ): Result<UserProfile> {
        val nameParts = name.trim().split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")
        val profile = UserProfile(
            id = userId,
            name = firstName.ifEmpty { "Usuario" },
            lastName = lastName,
            email = email,
            phone = phone,
            role = role,
            location = location
        )

        if (SupabaseClient.isConfigured) {
            try {
                val dbRole = if (role == UserRole.CLIENT) "client" else "worker"
                val profileDto = SupabaseProfileDto(
                    id = userId,
                    name = name,
                    phone = phone,
                    role = dbRole,
                    location = location
                )
                // Use upsert to safely create or update without duplicate key constraint collisions
                SupabaseClient.client
                    .from("profiles")
                    .upsert(profileDto)

                if (role == UserRole.WORKER) {
                    val proResult = createProfessionalInSupabase(
                        profileId = userId,
                        location = location,
                        profession = profession.ifBlank { "Plomero" },
                        description = "Servicios profesionales confiables de $profession.",
                        experienceYears = 1,
                        categoryId = categoryId
                    )
                    if (proResult.isFailure) {
                        val proEx = proResult.exceptionOrNull()
                        return Result.failure(proEx ?: Exception("Error al crear profesional: fallo desconocido en Supabase"))
                    }
                }
                _currentUser.value = profile
                android.util.Log.d("OficiosSupabase", "Perfil y rol ($dbRole) guardados exitosamente en Supabase para userId: $userId")
                return Result.success(profile)
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "Error al crear perfil en Supabase: ${e.message}", e)
                return Result.failure(e)
            }
        } else {
            _currentUser.value = profile
            return Result.success(profile)
        }
    }

    override suspend fun createProfessionalInSupabase(
        profileId: String,
        location: String,
        profession: String,
        description: String,
        experienceYears: Int,
        categoryId: String?
    ): Result<Unit> {
        if (!SupabaseClient.isConfigured) return Result.success(Unit)

        return try {
            // Check if professional record already exists for this profile_id to avoid duplicates
            val existing = try {
                SupabaseClient.client
                    .from("professionals")
                    .select(columns = Columns.list("id")) {
                        filter { eq("profile_id", profileId) }
                    }
                    .decodeList<SupabaseProfessionalInsertDto>()
            } catch (e: Exception) {
                android.util.Log.w("OficiosSupabase", "Verificando existencia en professionals: ${e.message}")
                emptyList()
            }

            if (existing.isEmpty()) {
                val proId = UUID.randomUUID().toString()
                val proInsert = SupabaseProfessionalInsertDto(
                    id = proId,
                    profileId = profileId,
                    profession = profession,
                    description = description,
                    experienceYears = experienceYears,
                    available = true,
                    location = location.ifBlank { "Medellín" },
                    rating = 5.0,
                    totalReviews = 0
                )
                SupabaseClient.client
                    .from("professionals")
                    .insert(proInsert)
                android.util.Log.d("OficiosSupabase", "Registro en professionals creado con éxito para profile_id: $profileId con oficio: $profession")

                // If categoryId is specified, also link in professional_services table
                if (!categoryId.isNullOrBlank()) {
                    try {
                        val serviceInsert = com.example.model.SupabaseProfessionalServiceDto(
                            id = UUID.randomUUID().toString(),
                            professionalId = proId,
                            categoryId = categoryId,
                            serviceDescription = "Servicios de $profession"
                        )
                        SupabaseClient.client
                            .from("professional_services")
                            .insert(serviceInsert)
                        android.util.Log.d("OficiosSupabase", "Servicio vinculado en professional_services con category_id: $categoryId")
                    } catch (pse: Exception) {
                        android.util.Log.w("OficiosSupabase", "Aviso al insertar en professional_services: ${pse.message}")
                    }
                }
            } else {
                android.util.Log.d("OficiosSupabase", "El registro en professionals ya existía para profile_id: $profileId")
            }
            Result.success(Unit)
        } catch (pe: Exception) {
            val errorMsg = pe.message ?: pe.toString()
            android.util.Log.e("OficiosSupabase", "Error al crear profesional: $errorMsg", pe)
            Result.failure(Exception("Error al crear profesional: $errorMsg"))
        }
    }

    override fun switchUserRole(role: UserRole) {
        val current = _currentUser.value
        val authUserId = AuthService.currentUserId()
        val isRealUser = authUserId != null || (current.id.isNotBlank() && current.id != MockData.defaultClientUser.id && current.id != MockData.defaultWorkerUser.id)
        if (isRealUser) {
            _currentUser.update { it.copy(role = role) }
            if (authUserId != null && SupabaseClient.isConfigured) {
                val dbRole = if (role == UserRole.CLIENT) "client" else "worker"
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        SupabaseClient.client
                            .from("profiles")
                            .update({
                                set("role", dbRole)
                            }) {
                                filter { eq("id", authUserId) }
                            }
                        android.util.Log.d("OficiosAuth", "Rol actualizado en Supabase a: $dbRole para userId: $authUserId")
                    } catch (e: Exception) {
                        android.util.Log.e("OficiosAuth", "Error actualizando rol en Supabase: ${e.message}")
                    }
                }
            }
        } else {
            if (_requests.value.isEmpty()) {
                _requests.value = MockData.initialRequests
            }
            if (_notifications.value.isEmpty()) {
                _notifications.value = MockData.initialNotifications
            }
            if (role == UserRole.CLIENT) {
                _currentUser.value = MockData.defaultClientUser
            } else {
                _currentUser.value = MockData.defaultWorkerUser
            }
        }
    }

    override suspend fun updateClientProfile(name: String, phone: String, location: String): Result<UserProfile> {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío."))
        }
        val cleanPhone = phone.trim()
        val cleanLocation = location.trim().ifBlank { "Medellín" }
        val current = _currentUser.value
        val nameParts = cleanName.split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        val updatedLocal = current.copy(
            name = firstName.ifEmpty { current.name },
            lastName = lastName,
            phone = cleanPhone,
            location = cleanLocation
        )

        val authUserId = AuthService.currentUserId()
        if (authUserId != null && SupabaseClient.isConfigured) {
            try {
                SupabaseClient.client
                    .from("profiles")
                    .update({
                        set("name", cleanName)
                        set("phone", cleanPhone)
                        set("location", cleanLocation)
                        set("role", "client")
                    }) {
                        filter { eq("id", authUserId) }
                    }
                android.util.Log.d("OficiosSupabase", "Perfil de cliente actualizado en Supabase para userId: $authUserId")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "No se pudo actualizar en profiles Supabase: ${e.message}", e)
                return Result.failure(Exception("No se pudo guardar tu perfil en Supabase: ${e.message ?: "Error de red"}"))
            }
        }

        _currentUser.value = updatedLocal
        return Result.success(updatedLocal)
    }

    override suspend fun updateWorkerProfile(
        name: String,
        phone: String,
        profession: String,
        description: String,
        experienceYears: Int,
        available: Boolean,
        location: String
    ): Result<UserProfile> {
        val cleanName = name.trim()
        val cleanProfession = profession.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre completo no puede estar vacío."))
        }
        if (cleanProfession.isBlank()) {
            return Result.failure(IllegalArgumentException("El oficio o profesión no puede estar vacío."))
        }

        val cleanPhone = phone.trim()
        val cleanDesc = description.trim()
        val cleanLocation = location.trim().ifBlank { "Medellín" }
        val safeYears = experienceYears.coerceAtLeast(0)

        val current = _currentUser.value
        val nameParts = cleanName.split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        val updatedLocal = current.copy(
            name = firstName.ifEmpty { current.name },
            lastName = lastName,
            phone = cleanPhone,
            location = cleanLocation
        )

        val authUserId = AuthService.currentUserId()
        if (authUserId != null && SupabaseClient.isConfigured) {
            try {
                SupabaseClient.client
                    .from("profiles")
                    .update({
                        set("name", cleanName)
                        set("phone", cleanPhone)
                        set("location", cleanLocation)
                        set("role", "worker")
                    }) {
                        filter { eq("id", authUserId) }
                    }

                val existing = SupabaseClient.client
                    .from("professionals")
                    .select(columns = Columns.list("id")) {
                        filter { eq("profile_id", authUserId) }
                    }
                    .decodeList<SupabaseProfessionalDto>()

                if (existing.isNotEmpty()) {
                    SupabaseClient.client
                        .from("professionals")
                        .update({
                            set("profession", cleanProfession)
                            set("description", cleanDesc)
                            set("experience_years", safeYears)
                            set("available", available)
                            set("location", cleanLocation)
                        }) {
                            filter { eq("profile_id", authUserId) }
                        }
                } else {
                    val createProRes = createProfessionalInSupabase(
                        profileId = authUserId,
                        location = cleanLocation,
                        profession = cleanProfession,
                        description = cleanDesc.ifBlank { "Servicios profesionales de $cleanProfession." },
                        experienceYears = safeYears.coerceAtLeast(1)
                    )
                    if (createProRes.isFailure) {
                        return Result.failure(createProRes.exceptionOrNull() ?: Exception("No se pudo crear el perfil profesional."))
                    }
                }
                android.util.Log.d("OficiosSupabase", "Perfil y datos profesionales actualizados en Supabase")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "Error al actualizar worker profile en Supabase: ${e.message}", e)
                return Result.failure(Exception("No se pudieron guardar los cambios en Supabase: ${e.message ?: "Error de red"}"))
            }
        }

        _currentUser.value = updatedLocal
        _professionals.update { list ->
            list.map { pro ->
                val matchesWorker = pro.userId == current.id ||
                    (authUserId == null && current.id == MockData.defaultWorkerUser.id && pro.id == "pro_mateo")
                if (matchesWorker) {
                    pro.copy(
                        name = cleanName,
                        profession = cleanProfession,
                        description = cleanDesc,
                        experienceYears = safeYears,
                        available = available,
                        locationName = cleanLocation,
                        avatarInitial = cleanName.firstOrNull()?.uppercase() ?: pro.avatarInitial
                    )
                } else pro
            }
        }
        return Result.success(updatedLocal)
    }

    override suspend fun loadReviewsForProfessional(professionalId: String): Result<List<Review>> {
        if (!SupabaseClient.isConfigured) return Result.success(emptyList())
        return try {
            val dtoList = SupabaseClient.client
                .from("reviews")
                .select(columns = Columns.raw("id, request_id, professional_id, client_id, rating, comment, created_at, profiles(id, name)")) {
                    filter { eq("professional_id", professionalId) }
                }
                .decodeList<SupabaseReviewDto>()
            val mapped = dtoList.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                Review(
                    id = id,
                    serviceRequestId = dto.requestId ?: "req",
                    clientName = dto.profiles?.displayName?.takeIf { it.isNotBlank() } ?: "Cliente verificado",
                    rating = dto.rating ?: 5.0,
                    comment = dto.comment.orEmpty(),
                    date = dto.createdAt?.take(10) ?: "Reciente"
                )
            }
            Result.success(mapped)
        } catch (e: Exception) {
            android.util.Log.e("OficiosSupabase", "Error al cargar reseñas del profesional $professionalId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProfessionalDetail(professionalId: String): Result<Professional> {
        if (SupabaseClient.isConfigured) {
            try {
                val dtoList = SupabaseClient.client
                    .from("professionals")
                    .select(
                        columns = Columns.raw(
                            "id, profile_id, profession, description, experience_years, " +
                            "rating, total_reviews, available, location, created_at, " +
                            "profiles(id, name, phone, role, location), " +
                            "professional_services(id, category_id, service_description, categories(id, name, icon))"
                        )
                    ) {
                        filter { eq("id", professionalId) }
                    }
                    .decodeList<SupabaseProfessionalDto>()

                val dto = dtoList.firstOrNull()
                if (dto != null) {
                    val linkedProfile = dto.profiles ?: dto.profile ?: run {
                        dto.profileId?.let { pid ->
                            try {
                                SupabaseClient.client
                                    .from("profiles")
                                    .select(columns = Columns.raw("id, name, phone, role, location")) {
                                        filter { eq("id", pid) }
                                    }
                                    .decodeList<SupabaseProfileDto>()
                                    .firstOrNull()
                            } catch (e: Exception) { null }
                        }
                    }

                    val resolvedName = linkedProfile?.displayName?.takeIf { it.isNotBlank() }
                        ?: _professionals.value.find { it.id == professionalId }?.name
                        ?: "Profesional de oficio"

                    val reviews = loadReviewsForProfessional(professionalId).getOrDefault(emptyList())

                    val structuredServices = dto.professionalServices.mapNotNull { ps ->
                        val cId = ps.categoryId ?: return@mapNotNull null
                        val cName = ps.categories?.name
                            ?: _categories.value.find { it.id == cId }?.name
                            ?: "Servicio"
                        val sDesc = ps.serviceDescription?.takeIf { it.isNotBlank() } ?: cName
                        com.example.model.ProfessionalServiceItem(
                            id = ps.id,
                            categoryId = cId,
                            categoryName = cName,
                            serviceDescription = sDesc
                        )
                    }

                    val catIds = if (structuredServices.isNotEmpty()) {
                        structuredServices.map { it.categoryId }.distinct()
                    } else {
                        dto.professionalServices.mapNotNull { it.categoryId }.ifEmpty { emptyList() }
                    }

                    val serviceLabels = if (structuredServices.isNotEmpty()) {
                        structuredServices.map { it.serviceDescription.ifBlank { it.categoryName } }
                    } else {
                        _categories.value
                            .filter { it.id in catIds }
                            .map { it.name }
                            .ifEmpty {
                                dto.professionalServices.mapNotNull { it.serviceDescription }.ifEmpty {
                                    listOf(dto.profession ?: "Servicios para el hogar")
                                }
                            }
                    }

                    val rawRating = if (reviews.isNotEmpty()) {
                        reviews.map { it.rating }.average()
                    } else {
                        dto.rating ?: 0.0
                    }
                    val displayRating = if (rawRating < 0.0) 0.0 else Math.round(rawRating * 10.0) / 10.0

                    val professional = Professional(
                        id = dto.id,
                        userId = dto.profileId ?: dto.id,
                        name = resolvedName,
                        profession = dto.profession?.takeIf { it.isNotBlank() } ?: "Especialista",
                        description = dto.description.orEmpty(),
                        experienceYears = dto.experienceYears ?: 1,
                        rating = displayRating,
                        servicesCompleted = if (reviews.isNotEmpty()) reviews.size else (dto.totalReviews ?: 0),
                        distanceKm = 0.0,
                        available = dto.available ?: true,
                        categoryIds = catIds,
                        servicesOffered = serviceLabels,
                        servicesList = structuredServices,
                        locationName = dto.location?.takeIf { it.isNotBlank() }
                            ?: linkedProfile?.location?.takeIf { it.isNotBlank() }
                            ?: "Medellín",
                        latitude = 6.2442,
                        longitude = -75.5925,
                        reviews = reviews,
                        avatarInitial = resolvedName.firstOrNull()?.uppercase() ?: "P"
                    )

                    _professionals.update { current ->
                        val index = current.indexOfFirst { it.id == professional.id }
                        if (index >= 0) {
                            current.toMutableList().also { it[index] = professional }
                        } else {
                            current + professional
                        }
                    }

                    return Result.success(professional)
                }
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "Error al consultar detalle del profesional: ${e.message}", e)
            }
        }

        val localPro = _professionals.value.find { it.id == professionalId }
        return if (localPro != null) {
            Result.success(localPro)
        } else {
            Result.failure(NoSuchElementException("Profesional no encontrado"))
        }
    }

    private fun matchesCategoryTrade(categoryName: String, profession: String): Boolean {
        val cat = categoryName.lowercase()
        val pro = profession.lowercase()
        return when {
            cat.contains("plomer") && (pro.contains("plom") || pro.contains("fontan")) -> true
            cat.contains("electri") && pro.contains("electr") -> true
            cat.contains("carpin") && pro.contains("carpin") -> true
            cat.contains("pintu") && (pro.contains("pinto") || pro.contains("pintu")) -> true
            cat.contains("cerraj") && pro.contains("cerraj") -> true
            cat.contains("electro") && (pro.contains("electro") || pro.contains("técnico") || pro.contains("tecnico")) -> true
            else -> false
        }
    }

    override suspend fun createServiceRequest(
        professionalId: String,
        description: String,
        scheduledDate: String,
        scheduledTime: String,
        location: String,
        categoryId: String?
    ): Result<ServiceRequest> {
        val pro = _professionals.value.find { it.id == professionalId }
            ?: _professionals.value.firstOrNull()
            ?: return Result.failure(IllegalStateException("No se encontró el profesional seleccionado"))

        val cat = if (!categoryId.isNullOrBlank()) {
            _categories.value.find { it.id == categoryId }
                ?: _categories.value.find { it.id in pro.categoryIds }
                ?: _categories.value.firstOrNull()
        } else {
            _categories.value.find { it.id in pro.categoryIds }
                ?: _categories.value.find { matchesCategoryTrade(it.name.lowercase(), pro.profession.lowercase()) }
                ?: _categories.value.firstOrNull()
        } ?: ServiceCategory(id = "330571e3-9665-4088-92da-0f2c98d7f8f5", name = pro.profession, iconName = "handyman", description = "Servicio")

        val authUserId = AuthService.currentUserId() ?: _currentUser.value.id
        var requestId = "req_" + UUID.randomUUID().toString().take(8)
        var insertedRecord: SupabaseServiceRequestDto? = null

        // Attempt remote write to service_requests table in Supabase
        if (SupabaseClient.isConfigured && AuthService.currentUserId() != null) {
            try {
                val dto = SupabaseServiceRequestDto(
                    clientId = authUserId,
                    professionalId = pro.id,
                    categoryId = cat.id,
                    description = description,
                    location = location,
                    status = "pending"
                )
                android.util.Log.d("OficiosSupabase", "Insertando service_request: client_id=$authUserId, professional_id=${pro.id}, category_id=${cat.id}")
                insertedRecord = SupabaseClient.client
                    .from("service_requests")
                    .insert(dto) {
                        select()
                    }
                    .decodeSingleOrNull<SupabaseServiceRequestDto>()

                if (insertedRecord?.id != null) {
                    requestId = insertedRecord.id
                }
                android.util.Log.d("OficiosSupabase", "Solicitud de servicio insertada en Supabase correctamente con ID: $requestId")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "No se pudo insertar en service_requests: ${e.message}", e)
                return Result.failure(e)
            }
        }

        val newRequest = ServiceRequest(
            id = requestId,
            clientId = authUserId,
            clientName = "${_currentUser.value.name} ${_currentUser.value.lastName}".trim(),
            clientPhone = _currentUser.value.phone,
            professionalId = pro.id,
            professionalName = pro.name,
            professionalProfession = pro.profession,
            categoryId = cat.id,
            categoryName = cat.name,
            description = description,
            location = location,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            status = ServiceStatus.PENDING,
            createdAt = insertedRecord?.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )

        _requests.update { listOf(newRequest) + it }

        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = "Solicitud enviada a ${pro.name}",
            message = "Servicio de ${cat.name}: esperando respuesta.",
            read = false,
            date = "Ahora",
            relatedRequestId = newRequest.id
        )
        _notifications.update { listOf(notif) + it }

        // Also insert notifications in Supabase when authenticated
        if (hasAuthenticatedSession()) {
            try {
                val clientNotifDto = SupabaseNotificationDto(
                    id = UUID.randomUUID().toString(),
                    profileId = authUserId,
                    title = notif.title,
                    message = notif.message,
                    isRead = false,
                    type = "service_request"
                )
                SupabaseClient.client
                    .from("notifications")
                    .insert(clientNotifDto)
            } catch (_: Exception) {
                // local notification remains
            }

            if (pro.userId.isNotBlank() && pro.userId != authUserId) {
                try {
                    val workerNotifDto = SupabaseNotificationDto(
                        id = UUID.randomUUID().toString(),
                        profileId = pro.userId,
                        title = "Nueva solicitud de ${cat.name}",
                        message = "${_currentUser.value.name} solicitó un servicio en $location.",
                        isRead = false,
                        type = "new_request"
                    )
                    SupabaseClient.client
                        .from("notifications")
                        .insert(workerNotifDto)
                } catch (_: Exception) {
                    // May be restricted by RLS unless Postgres trigger handles worker notifications
                }
            }
        }

        return Result.success(newRequest)
    }

    override suspend fun updateRequestStatus(requestId: String, newStatus: ServiceStatus): Result<ServiceRequest> {
        val currentReq = _requests.value.find { it.id == requestId }
            ?: return Result.failure(IllegalArgumentException("Solicitud no encontrada: $requestId"))

        if (currentReq.status != newStatus && !currentReq.status.canTransitionTo(newStatus)) {
            return Result.failure(
                IllegalStateException("No es posible cambiar de '${currentReq.status.labelEs}' a '${newStatus.labelEs}'.")
            )
        }

        if (hasAuthenticatedSession()) {
            try {
                SupabaseClient.client
                    .from("service_requests")
                    .update({
                        set("status", newStatus.code)
                    }) {
                        filter { eq("id", requestId) }
                    }
                android.util.Log.d("OficiosSupabase", "Estado de solicitud actualizado en Supabase: ${newStatus.code}")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "Error actualizando estado en Supabase: ${e.message}", e)
                return Result.failure(Exception("No se pudo actualizar el estado en Supabase: ${e.message ?: "Error de permisos o red"}"))
            }
        }

        val updatedReq = currentReq.copy(status = newStatus)
        _requests.update { list ->
            list.map { req -> if (req.id == requestId) updatedReq else req }
        }

        val statusMsg = when (newStatus) {
            ServiceStatus.ACCEPTED -> "${updatedReq.professionalName} aceptó tu solicitud."
            ServiceStatus.ON_THE_WAY -> "¡${updatedReq.professionalName} está en camino hacia tu ubicación!"
            ServiceStatus.IN_PROGRESS -> "El servicio está en progreso."
            ServiceStatus.COMPLETED -> "Tu servicio fue finalizado. ¡Recuerda calificarlo!"
            ServiceStatus.CANCELLED -> "La solicitud ha sido cancelada."
            ServiceStatus.PENDING -> "Solicitud pendiente."
        }
        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = "Actualización de servicio",
            message = statusMsg,
            read = false,
            date = "Ahora",
            relatedRequestId = updatedReq.id
        )
        _notifications.update { listOf(notif) + it }

        if (hasAuthenticatedSession() && updatedReq.clientId.isNotBlank()) {
            try {
                val notifDto = SupabaseNotificationDto(
                    id = UUID.randomUUID().toString(),
                    profileId = updatedReq.clientId,
                    title = "Actualización de servicio",
                    message = statusMsg,
                    isRead = false,
                    type = "status_update"
                )
                SupabaseClient.client
                    .from("notifications")
                    .insert(notifDto)
            } catch (ne: Exception) {
                android.util.Log.w("OficiosSupabase", "Aviso: notificación remota no insertada (${ne.message})")
            }
        }

        return Result.success(updatedReq)
    }

    override suspend fun submitReview(requestId: String, rating: Double, comment: String): Result<Review> {
        if (rating < 1.0 || rating > 5.0) {
            return Result.failure(IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas."))
        }

        val existingReq = _requests.value.find { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("No se encontró la solicitud a calificar."))

        if (existingReq.status != ServiceStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Solo puedes calificar servicios que ya han finalizado."))
        }
        if (existingReq.ratingGiven != null || reviewsByRequestId.containsKey(requestId)) {
            return Result.failure(IllegalStateException("Este servicio ya cuenta con una calificación registrada."))
        }

        val client = _currentUser.value
        val authUserId = AuthService.currentUserId() ?: client.id
        val targetProfId = existingReq.professionalId
        val cleanComment = comment.trim()
        var reviewId = "rev_" + UUID.randomUUID().toString().take(8)

        if (hasAuthenticatedSession()) {
            try {
                val existingRemote = SupabaseClient.client
                    .from("reviews")
                    .select(columns = Columns.list("id")) {
                        filter { eq("request_id", requestId) }
                    }
                    .decodeList<SupabaseReviewDto>()
                if (existingRemote.isNotEmpty()) {
                    return Result.failure(IllegalStateException("Ya enviaste una reseña para este servicio."))
                }

                val dto = SupabaseReviewDto(
                    clientId = authUserId,
                    professionalId = targetProfId,
                    requestId = requestId,
                    rating = rating,
                    comment = cleanComment
                )
                val inserted = SupabaseClient.client
                    .from("reviews")
                    .insert(dto) {
                        select()
                    }
                    .decodeSingleOrNull<SupabaseReviewDto>()
                if (inserted?.id != null) {
                    reviewId = inserted.id
                }
                android.util.Log.d("OficiosSupabase", "Reseña guardada en Supabase correctamente con ID: $reviewId")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "No se pudo guardar reseña en Supabase: ${e.message}", e)
                return Result.failure(Exception("No se pudo guardar la reseña en Supabase: ${e.message ?: "Error de red"}"))
            }
        }

        val newReview = Review(
            id = reviewId,
            serviceRequestId = requestId,
            clientName = "${client.name} ${client.lastName.take(1)}.".trim(),
            rating = rating,
            comment = cleanComment,
            date = "Hoy"
        )
        reviewsByRequestId[requestId] = newReview

        _requests.update { list ->
            list.map { req ->
                if (req.id == requestId) {
                    req.copy(ratingGiven = rating, reviewComment = cleanComment)
                } else req
            }
        }

        var updatedAvg = rating
        var updatedTotal = 1
        _professionals.update { proList ->
            proList.map { pro ->
                if (pro.id == targetProfId) {
                    val newReviews = listOf(newReview) + pro.reviews
                    val avg = if (pro.servicesCompleted > 0) {
                        (pro.rating * pro.servicesCompleted + rating) / (pro.servicesCompleted + 1)
                    } else {
                        newReviews.map { it.rating }.average()
                    }
                    updatedAvg = Math.round(avg * 10.0) / 10.0
                    updatedTotal = maxOf(pro.servicesCompleted + 1, newReviews.size)
                    pro.copy(
                        reviews = newReviews,
                        servicesCompleted = updatedTotal,
                        rating = updatedAvg
                    )
                } else pro
            }
        }

        // Best-effort sync of cached rating/total_reviews columns on professionals
        if (hasAuthenticatedSession() && targetProfId.isNotBlank()) {
            try {
                SupabaseClient.client
                    .from("professionals")
                    .update({
                        set("rating", updatedAvg)
                        set("total_reviews", updatedTotal)
                    }) {
                        filter { eq("id", targetProfId) }
                    }
            } catch (_: Exception) {
                // Usually handled by Postgres trigger if RLS restricts client updates on professionals
            }
        }

        return Result.success(newReview)
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        val previous = _notifications.value
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(read = true) else it }
        }

        if (hasAuthenticatedSession() && !notificationId.startsWith("notif_")) {
            try {
                SupabaseClient.client
                    .from("notifications")
                    .update({
                        set("is_read", true)
                    }) {
                        filter { eq("id", notificationId) }
                    }
                android.util.Log.d("OficiosSupabase", "Notificación marcada como leída en Supabase: $notificationId")
            } catch (e: Exception) {
                android.util.Log.e("OficiosSupabase", "Error al marcar notificación en Supabase: ${e.message}")
                _notifications.value = previous
                return Result.failure(e)
            }
        }
        return Result.success(Unit)
    }
}
