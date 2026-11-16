package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppNotification
import com.example.model.Professional
import com.example.model.ServiceCategory
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.service.InMemoryOficiosRepository
import com.example.service.OficiosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.service.AuthService
import com.example.service.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Screen navigation routes for client and worker journeys
 */
sealed class Screen(val route: String) {
    // Auth screens
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object RoleSelect : Screen("role_select")

    // Client flow
    data object ClientHome : Screen("client_home")
    data object ClientSearch : Screen("client_search")
    data object ClientResults : Screen("client_results")
    data object ClientMap : Screen("client_map")
    data object ClientProfessionalDetail : Screen("client_pro_detail")
    data object ClientRequestService : Screen("client_request_service")
    data object ClientConfirmRequest : Screen("client_confirm_request")
    data object ClientRequestSent : Screen("client_request_sent")
    data object ClientTracking : Screen("client_tracking")
    data object ClientRating : Screen("client_rating")
    data object ClientHistory : Screen("client_history")
    data object ClientProfile : Screen("client_profile")
    data object ClientNotifications : Screen("client_notifications")

    // Worker flow
    data object WorkerHome : Screen("worker_home")
    data object WorkerRequests : Screen("worker_requests")
    data object WorkerRequestDetail : Screen("worker_request_detail")
    data object WorkerActiveService : Screen("worker_active_service")
    data object WorkerProfile : Screen("worker_profile")
    data object WorkerEditProfile : Screen("worker_edit_profile")
    data object WorkerReviews : Screen("worker_reviews")
    data object WorkerNotifications : Screen("worker_notifications")
}

sealed interface ProfessionalDetailUiState {
    data object Loading : ProfessionalDetailUiState
    data class Success(val professional: Professional) : ProfessionalDetailUiState
    data class Error(val message: String) : ProfessionalDetailUiState
    data object Empty : ProfessionalDetailUiState
}

sealed interface CreateRequestUiState {
    data object Idle : CreateRequestUiState
    data object Loading : CreateRequestUiState
    data class Success(val request: ServiceRequest) : CreateRequestUiState
    data class Error(val message: String) : CreateRequestUiState
}

sealed interface ClientRequestsUiState {
    data object Loading : ClientRequestsUiState
    data class Success(val requests: List<ServiceRequest>) : ClientRequestsUiState
    data class Error(val message: String) : ClientRequestsUiState
}

sealed interface WorkerRequestsUiState {
    data object Loading : WorkerRequestsUiState
    data class Success(val requests: List<ServiceRequest>) : WorkerRequestsUiState
    data class Error(val message: String) : WorkerRequestsUiState
}

sealed interface WorkerActionUiState {
    data object Idle : WorkerActionUiState
    data class Loading(val requestId: String, val action: String) : WorkerActionUiState
    data class Success(val requestId: String, val newStatus: ServiceStatus, val message: String) : WorkerActionUiState
    data class Error(val requestId: String, val message: String) : WorkerActionUiState
}

sealed interface ProfileActionUiState {
    data object Idle : ProfileActionUiState
    data object Loading : ProfileActionUiState
    data class Success(val message: String) : ProfileActionUiState
    data class Error(val message: String) : ProfileActionUiState
}

sealed interface ReviewActionUiState {
    data object Idle : ReviewActionUiState
    data object Loading : ReviewActionUiState
    data class Success(val message: String) : ReviewActionUiState
    data class Error(val message: String) : ReviewActionUiState
}

sealed interface ClientActionUiState {
    data object Idle : ClientActionUiState
    data object Loading : ClientActionUiState
    data class Success(val message: String) : ClientActionUiState
    data class Error(val message: String) : ClientActionUiState
}

class OficiosViewModel @JvmOverloads constructor(
    private val repository: OficiosRepository = InMemoryOficiosRepository(),
    private val signInHandler: suspend (String, String) -> Unit = { email, password ->
        AuthService.signIn(email, password)
    }
) : ViewModel() {

    // Current navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation backstack for seamless back button support
    private val screenStack = mutableListOf<Screen>(Screen.Login)

    // Data streams from repository
    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val categories: StateFlow<List<ServiceCategory>> = repository.categories
    val professionals: StateFlow<List<Professional>> = repository.professionals
    val requests: StateFlow<List<ServiceRequest>> = repository.requests
    val notifications: StateFlow<List<AppNotification>> = repository.notifications
    val supabaseStatus: StateFlow<com.example.service.SupabaseConnectionStatus> = repository.supabaseStatus

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    fun clearAuthErrors() {
        _authError.value = null
        _authMessage.value = null
    }

    // Active selections in client flow
    private val _selectedProfessionalId = MutableStateFlow<String?>("pro_mateo")
    val selectedProfessionalId: StateFlow<String?> = _selectedProfessionalId.asStateFlow()

    private val _professionalDetailState = MutableStateFlow<ProfessionalDetailUiState>(ProfessionalDetailUiState.Loading)
    val professionalDetailState: StateFlow<ProfessionalDetailUiState> = _professionalDetailState.asStateFlow()

    private val _createRequestState = MutableStateFlow<CreateRequestUiState>(CreateRequestUiState.Idle)
    val createRequestState: StateFlow<CreateRequestUiState> = _createRequestState.asStateFlow()

    private val _clientRequestsState = MutableStateFlow<ClientRequestsUiState>(ClientRequestsUiState.Loading)
    val clientRequestsState: StateFlow<ClientRequestsUiState> = _clientRequestsState.asStateFlow()

    private val _workerRequestsState = MutableStateFlow<WorkerRequestsUiState>(WorkerRequestsUiState.Loading)
    val workerRequestsState: StateFlow<WorkerRequestsUiState> = _workerRequestsState.asStateFlow()

    private val _workerActionState = MutableStateFlow<WorkerActionUiState>(WorkerActionUiState.Idle)
    val workerActionState: StateFlow<WorkerActionUiState> = _workerActionState.asStateFlow()

    private val _profileActionState = MutableStateFlow<ProfileActionUiState>(ProfileActionUiState.Idle)
    val profileActionState: StateFlow<ProfileActionUiState> = _profileActionState.asStateFlow()

    private val _reviewActionState = MutableStateFlow<ReviewActionUiState>(ReviewActionUiState.Idle)
    val reviewActionState: StateFlow<ReviewActionUiState> = _reviewActionState.asStateFlow()

    private val _clientActionState = MutableStateFlow<ClientActionUiState>(ClientActionUiState.Idle)
    val clientActionState: StateFlow<ClientActionUiState> = _clientActionState.asStateFlow()

    private val _notificationsLoading = MutableStateFlow(false)
    val notificationsLoading: StateFlow<Boolean> = _notificationsLoading.asStateFlow()

    private val _notificationsError = MutableStateFlow<String?>(null)
    val notificationsError: StateFlow<String?> = _notificationsError.asStateFlow()

    private val _selectedCategoryIdForRequest = MutableStateFlow<String?>(null)
    val selectedCategoryIdForRequest: StateFlow<String?> = _selectedCategoryIdForRequest.asStateFlow()

    private val _requestFormError = MutableStateFlow<String?>(null)
    val requestFormError: StateFlow<String?> = _requestFormError.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Request drafting state
    private val _draftDescription = MutableStateFlow("")
    val draftDescription: StateFlow<String> = _draftDescription.asStateFlow()

    private val _draftDate = MutableStateFlow("Por confirmar (Hoy)")
    val draftDate: StateFlow<String> = _draftDate.asStateFlow()

    private val _draftTime = MutableStateFlow("Por confirmar (Tarde)")
    val draftTime: StateFlow<String> = _draftTime.asStateFlow()

    private val _draftLocation = MutableStateFlow("Laureles, Medellín")
    val draftLocation: StateFlow<String> = _draftLocation.asStateFlow()

    // Tracking & rating selection
    private val _activeTrackingRequestId = MutableStateFlow<String?>("req_active_1")
    val activeTrackingRequestId: StateFlow<String?> = _activeTrackingRequestId.asStateFlow()

    // Worker active request selection
    private val _workerSelectedRequestId = MutableStateFlow<String?>("req_active_1")
    val workerSelectedRequestId: StateFlow<String?> = _workerSelectedRequestId.asStateFlow()

    // Filtered professionals based on search, category and sort
    val filteredProfessionals: StateFlow<List<Professional>> = combine(
        professionals,
        _searchQuery,
        _selectedCategoryFilter
    ) { pros, query, catId ->
        val cleanQuery = query.trim()
        val filtered = pros.filter { pro ->
            val matchesCategory = if (catId == null) {
                true
            } else {
                val activeCat = categories.value.find { it.id == catId }
                val activeCatName = activeCat?.name?.lowercase() ?: ""
                val proTrade = pro.profession.lowercase()
                catId in pro.categoryIds ||
                    (catId == "cat_plomeria" && pro.categoryIds.any { it.contains("330571e3") || it.contains("plomeria", ignoreCase = true) }) ||
                    (catId == "330571e3-9665-4088-92da-0f2c98d7f8f5" && pro.categoryIds.any { it == "cat_plomeria" || it.contains("plomeria", ignoreCase = true) }) ||
                    (activeCatName.isNotEmpty() && pro.servicesOffered.any { it.contains(activeCatName, ignoreCase = true) }) ||
                    (activeCatName.isNotEmpty() && proTrade.contains(activeCatName)) ||
                    matchesCategoryTrade(activeCatName, proTrade)
            }
            val matchesQuery = cleanQuery.isBlank() ||
                pro.name.contains(cleanQuery, ignoreCase = true) ||
                pro.profession.contains(cleanQuery, ignoreCase = true) ||
                pro.locationName.contains(cleanQuery, ignoreCase = true) ||
                pro.servicesOffered.any { it.contains(cleanQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
        android.util.Log.d("OficiosViewModel", "Filtro aplicado (catId=$catId, query='$cleanQuery'): ${filtered.size} de ${pros.size} profesionales visibles")
        filtered
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected professional object
    val selectedProfessional: StateFlow<Professional?> = combine(
        professionals,
        _selectedProfessionalId
    ) { pros, id ->
        pros.find { it.id == id } ?: pros.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Current tracking request object
    val trackingRequest: StateFlow<ServiceRequest?> = combine(
        requests,
        _activeTrackingRequestId
    ) { reqs, id ->
        reqs.find { it.id == id } ?: reqs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Worker selected request object
    val workerSelectedRequest: StateFlow<ServiceRequest?> = combine(
        requests,
        _workerSelectedRequestId
    ) { reqs, id ->
        reqs.find { it.id == id } ?: reqs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Trigger initial category, professional, client, and worker requests load from Supabase
        loadDataFromSupabase()
        loadClientRequests()
        loadWorkerRequests()
        checkActiveSession()
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            if (com.example.service.SupabaseClient.isConfigured && AuthService.currentUserId() != null) {
                val profileRes = repository.loadCurrentUserProfile()
                profileRes.onSuccess { profile ->
                    screenStack.clear()
                    repository.checkAndLoadSupabaseProfessionals()
                    refreshNotifications()
                    if (profile.role == UserRole.CLIENT) {
                        loadClientRequests(forceRefresh = true)
                        navigateTo(Screen.ClientHome)
                    } else {
                        loadWorkerRequests(forceRefresh = true)
                        navigateTo(Screen.WorkerHome)
                    }
                }.onFailure { error ->
                    val rawMsg = error.message.orEmpty()
                    if (rawMsg.contains("No se encontró ningún registro en profiles", ignoreCase = true) ||
                        rawMsg.contains("rol desconocido", ignoreCase = true)
                    ) {
                        try { AuthService.signOut() } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun loadDataFromSupabase() {
        viewModelScope.launch {
            repository.syncAllFromSupabase()
        }
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authError.value = "Ingresa tu correo electrónico y contraseña."
            return
        }
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(cleanEmail)) {
            _authError.value = "Ingresa un correo electrónico válido."
            return
        }

        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authMessage.value = null

            try {
                signInHandler(cleanEmail, password)

                val profileResult = repository.loadCurrentUserProfile()

                profileResult.onSuccess { profile ->
                    screenStack.clear()
                    repository.checkAndLoadSupabaseProfessionals()
                    refreshNotifications()
                    if (profile.role == UserRole.CLIENT) {
                        loadClientRequests(forceRefresh = true)
                        navigateTo(Screen.ClientHome)
                    } else {
                        loadWorkerRequests(forceRefresh = true)
                        navigateTo(Screen.WorkerHome)
                    }
                }.onFailure { error ->
                    val rawMsg = error.message.orEmpty()
                    when {
                        rawMsg.contains("Email not confirmed", ignoreCase = true) -> {
                            _authError.value = "Debes confirmar tu correo electrónico antes de ingresar. Revisa tu bandeja de entrada o spam."
                        }
                        rawMsg.contains("rol desconocido", ignoreCase = true) -> {
                            try { AuthService.signOut() } catch (_: Exception) {}
                            _authError.value = "Tu cuenta no tiene un rol válido asignado. Contacta a soporte para verificar tu perfil."
                        }
                        rawMsg.contains("No se encontró ningún registro en profiles", ignoreCase = true) -> {
                            try { AuthService.signOut() } catch (_: Exception) {}
                            _authError.value = "Iniciaste sesión, pero tu cuenta no tiene un perfil válido registrado. Regístrate o contacta a soporte."
                        }
                        else -> {
                            _authError.value = "Iniciaste sesión, pero no se pudo cargar tu perfil. Verifica tu conexión o intenta de nuevo."
                        }
                    }
                    android.util.Log.e("OficiosAuth", "Error al cargar el perfil: ${error.message}", error)
                }
            } catch (e: Exception) {
                val rawMsg = e.message.orEmpty()
                val friendlyMsg = when {
                    rawMsg.contains("Invalid login credentials", ignoreCase = true) ->
                        "Correo o contraseña incorrectos. Verifica tus datos."
                    rawMsg.contains("Email not confirmed", ignoreCase = true) ->
                        "Tu correo aún no ha sido confirmado. Revisa tu bandeja de entrada o spam."
                    rawMsg.contains("Unable to resolve host", ignoreCase = true) ||
                        rawMsg.contains("timeout", ignoreCase = true) ||
                        rawMsg.contains("connect", ignoreCase = true) ->
                        "No pudimos conectarnos con el servidor. Verifica tu conexión a internet."
                    else ->
                        "No se pudo iniciar sesión. Verifica tus datos e inténtalo de nuevo."
                }
                _authError.value = friendlyMsg
                android.util.Log.e("OficiosAuth", "Error de inicio de sesión", e)
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (cleanEmail.isBlank() || !emailRegex.matches(cleanEmail)) {
            _authError.value = "Ingresa un correo electrónico válido para recuperar tu contraseña."
            return
        }

        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authMessage.value = null
            try {
                AuthService.resetPassword(cleanEmail)
                _authMessage.value = "Si existe una cuenta asociada a $cleanEmail, recibirás un enlace para restablecer tu contraseña."
            } catch (e: Exception) {
                _authError.value = "No se pudo enviar el correo de recuperación: ${e.localizedMessage ?: "Verifica tu conexión."}"
            } finally {
                _authLoading.value = false
            }
        }
    }

    data class RegistrationDraft(
        val name: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val location: String = "Medellín",
        val password: String,
        val profession: String = "Plomero",
        val categoryId: String? = "330571e3-9665-4088-92da-0f2c98d7f8f5"
    )

    private val _registrationDraft = MutableStateFlow<RegistrationDraft?>(null)
    val registrationDraft: StateFlow<RegistrationDraft?> = _registrationDraft.asStateFlow()

    fun setRegistrationDraft(draft: RegistrationDraft) {
        _registrationDraft.value = draft
        _authError.value = null
        _authMessage.value = null
        navigateTo(Screen.RoleSelect)
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            val previous = screenStack.last()
            _currentScreen.value = previous
            return true
        }
        return false
    }

    fun selectRoleAndProceed(
        role: UserRole,
        profession: String = "Plomero",
        categoryId: String? = null
    ) {
        val draft = _registrationDraft.value
        if (draft != null && draft.email.isNotBlank() && draft.password.isNotBlank() && com.example.service.SupabaseClient.isConfigured) {
            viewModelScope.launch {
                _authLoading.value = true
                _authError.value = null
                _authMessage.value = null
                try {
                    val dbRole = if (role == UserRole.CLIENT) "client" else "worker"
                    val fullName = "${draft.name} ${draft.lastName}".trim()

                    // Attempt to reuse session or sign in if account was already created (e.g. retry after failure)
                    var newUserId = com.example.service.AuthService.currentUserId().orEmpty()
                    var session = com.example.service.AuthService.currentSession()

                    if (newUserId.isBlank()) {
                        try {
                            com.example.service.AuthService.signIn(draft.email, draft.password)
                            session = com.example.service.AuthService.currentSession()
                            newUserId = com.example.service.AuthService.currentUserId().orEmpty()
                        } catch (signInErr: Exception) {
                            // User not yet registered, execute signUp in Supabase Auth
                            val userInfo = com.example.service.AuthService.signUp(
                                email = draft.email,
                                password = draft.password,
                                name = fullName,
                                phone = draft.phone,
                                role = dbRole
                            )

                            session = com.example.service.AuthService.currentSession()
                            if (session == null) {
                                try {
                                    com.example.service.AuthService.signIn(draft.email, draft.password)
                                    session = com.example.service.AuthService.currentSession()
                                } catch (e2: Exception) {
                                    android.util.Log.d("OficiosAuth", "Sign-in right after sign-up: ${e2.message}")
                                }
                            }

                            newUserId = com.example.service.AuthService.currentUserId()
                                ?: userInfo?.id
                                ?: ""
                        }
                    }

                    val hasActiveSession = session != null || com.example.service.AuthService.currentUserId() != null

                    if (hasActiveSession && newUserId.isNotBlank()) {
                        // User is immediately authenticated in Supabase
                        val profileResult = repository.createProfileInSupabase(
                            userId = newUserId,
                            email = draft.email,
                            name = fullName,
                            phone = draft.phone,
                            role = role,
                            location = draft.location.ifBlank { "Medellín" },
                            profession = profession,
                            categoryId = categoryId
                        )

                        if (profileResult.isSuccess) {
                            _registrationDraft.value = null
                            repository.loadCurrentUserProfile()
                            repository.checkAndLoadSupabaseProfessionals()
                            screenStack.clear()
                            if (role == UserRole.CLIENT) {
                                navigateTo(Screen.ClientHome)
                            } else {
                                navigateTo(Screen.WorkerHome)
                            }
                        } else {
                            val err = profileResult.exceptionOrNull()
                            val errorDetails = err?.localizedMessage ?: err?.message ?: "Intenta de nuevo."
                            _authError.value = errorDetails
                        }
                    } else {
                        // Supabase project requires email confirmation
                        _registrationDraft.value = null
                        _authMessage.value = "¡Tu cuenta ha sido creada con éxito! Supabase ha enviado un enlace de confirmación a ${draft.email}. Por favor verifica tu correo para activar tu cuenta antes de iniciar sesión."
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OficiosAuth", "Error al registrar en Supabase: ${e.message}", e)
                    val rawMsg = e.message.orEmpty()
                    val friendlyMsg = when {
                        rawMsg.contains("email_address_invalid", ignoreCase = true) || rawMsg.contains("is invalid", ignoreCase = true) ->
                            "El correo electrónico '${draft.email}' no es válido o su dominio no puede recibir correos según Supabase. Por favor usa un correo real (ej: @gmail.com, @hotmail.com)."
                        rawMsg.contains("User already registered", ignoreCase = true) || rawMsg.contains("already registered", ignoreCase = true) || rawMsg.contains("user_already_exists", ignoreCase = true) ->
                            "El correo electrónico '${draft.email}' ya está registrado. Por favor inicia sesión."
                        rawMsg.contains("over_email_send_rate_limit", ignoreCase = true) || rawMsg.contains("rate limit", ignoreCase = true) ->
                            "Se ha alcanzado el límite de envíos de correo de Supabase. Por favor espera unos minutos antes de reintentar."
                        rawMsg.contains("Password should be", ignoreCase = true) || (rawMsg.contains("password", ignoreCase = true) && rawMsg.contains("characters", ignoreCase = true)) ->
                            "La contraseña debe contener al menos 6 caracteres."
                        else ->
                            "Error al crear tu cuenta: ${e.localizedMessage ?: e.message ?: "Verifica tus datos e inténtalo de nuevo."}"
                    }
                    _authError.value = friendlyMsg
                } finally {
                    _authLoading.value = false
                }
            }
        } else if (com.example.service.AuthService.currentUserId() != null && com.example.service.SupabaseClient.isConfigured) {
            val authUserId = com.example.service.AuthService.currentUserId()!!
            viewModelScope.launch {
                _authLoading.value = true
                _authError.value = null
                _authMessage.value = null
                try {
                    val user = com.example.service.SupabaseClient.client.auth.currentUserOrNull()
                    val email = user?.email.orEmpty()
                    val metaName = user?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                    val metaPhone = user?.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull
                    val resolvedName = currentUser.value.name.takeIf { it.isNotBlank() && it != "Laura" && it != "Mateo" }
                        ?: metaName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                        .ifEmpty { "Usuario" }
                    val resolvedPhone = currentUser.value.phone.takeIf { it.isNotBlank() } ?: metaPhone.orEmpty()
                    val resolvedLocation = currentUser.value.location.takeIf { it.isNotBlank() } ?: "Medellín"

                    val profileResult = repository.createProfileInSupabase(
                        userId = authUserId,
                        email = email,
                        name = resolvedName,
                        phone = resolvedPhone,
                        role = role,
                        location = resolvedLocation,
                        profession = profession,
                        categoryId = categoryId
                    )

                    if (profileResult.isSuccess) {
                        _registrationDraft.value = null
                        repository.loadCurrentUserProfile()
                        repository.checkAndLoadSupabaseProfessionals()
                        refreshNotifications()
                        screenStack.clear()
                        if (role == UserRole.CLIENT) {
                            loadClientRequests(forceRefresh = true)
                            navigateTo(Screen.ClientHome)
                        } else {
                            loadWorkerRequests(forceRefresh = true)
                            navigateTo(Screen.WorkerHome)
                        }
                    } else {
                        val err = profileResult.exceptionOrNull()
                        _authError.value = "No se pudo guardar el perfil: ${err?.localizedMessage ?: err?.message}"
                    }
                } catch (e: Exception) {
                    _authError.value = "Error al configurar perfil: ${e.localizedMessage ?: e.message}"
                } finally {
                    _authLoading.value = false
                }
            }
        } else {
            // Offline/test fallback (e.g. Robolectric unit tests where Supabase is unconfigured or no draft)
            repository.switchUserRole(role)
            screenStack.clear()
            if (role == UserRole.CLIENT) {
                loadClientRequests(forceRefresh = true)
                navigateTo(Screen.ClientHome)
            } else {
                loadWorkerRequests(forceRefresh = true)
                navigateTo(Screen.WorkerHome)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authLoading.value = true
            try {
                com.example.service.AuthService.signOut()
            } catch (e: Exception) {
                android.util.Log.e("OficiosAuth", "Error al cerrar sesión: ${e.message}")
            } finally {
                repository.clearSession()
                _authError.value = null
                _authMessage.value = null
                _registrationDraft.value = null
                _selectedProfessionalId.value = null
                _activeTrackingRequestId.value = null
                _workerSelectedRequestId.value = null
                _searchQuery.value = ""
                _selectedCategoryFilter.value = null
                _draftDescription.value = ""
                _requestFormError.value = null
                _createRequestState.value = CreateRequestUiState.Idle
                _workerActionState.value = WorkerActionUiState.Idle
                _profileActionState.value = ProfileActionUiState.Idle
                _reviewActionState.value = ReviewActionUiState.Idle
                _clientActionState.value = ClientActionUiState.Idle
                _clientRequestsState.value = ClientRequestsUiState.Loading
                _workerRequestsState.value = WorkerRequestsUiState.Loading
                _notificationsError.value = null
                _authLoading.value = false
                screenStack.clear()
                navigateTo(Screen.Login)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun toggleCategoryFilter(categoryId: String) {
        if (_selectedCategoryFilter.value == categoryId) {
            _selectedCategoryFilter.value = null
        } else {
            _selectedCategoryFilter.value = categoryId
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
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

    fun openProfessionalDetail(professionalId: String) {
        _selectedProfessionalId.value = professionalId
        _professionalDetailState.value = ProfessionalDetailUiState.Loading
        navigateTo(Screen.ClientProfessionalDetail)
        viewModelScope.launch {
            val result = repository.getProfessionalDetail(professionalId)
            result.fold(
                onSuccess = { pro ->
                    _professionalDetailState.value = ProfessionalDetailUiState.Success(pro)
                },
                onFailure = { err ->
                    val cached = professionals.value.find { it.id == professionalId }
                    if (cached != null) {
                        _professionalDetailState.value = ProfessionalDetailUiState.Success(cached)
                    } else {
                        _professionalDetailState.value = ProfessionalDetailUiState.Error(
                            err.message ?: "No se pudo cargar la información del profesional."
                        )
                    }
                }
            )
        }
    }

    fun retryLoadProfessionalDetail() {
        val proId = _selectedProfessionalId.value ?: return
        openProfessionalDetail(proId)
    }

    fun startServiceRequest(professionalId: String) {
        _selectedProfessionalId.value = professionalId
        _createRequestState.value = CreateRequestUiState.Idle
        _requestFormError.value = null
        _draftDescription.value = ""

        val pro = professionals.value.find { it.id == professionalId }
        if (pro != null) {
            when {
                pro.servicesList.isNotEmpty() -> {
                    _selectedCategoryIdForRequest.value = pro.servicesList.first().categoryId
                }
                pro.categoryIds.isNotEmpty() -> {
                    _selectedCategoryIdForRequest.value = pro.categoryIds.first()
                }
                else -> {
                    val matchingCat = categories.value.find { matchesCategoryTrade(it.name, pro.profession) }
                    _selectedCategoryIdForRequest.value = matchingCat?.id ?: categories.value.firstOrNull()?.id
                }
            }
        } else {
            _selectedCategoryIdForRequest.value = null
        }

        val userLoc = currentUser.value.location.trim()
        if (userLoc.isNotBlank() && (_draftLocation.value.isBlank() || _draftLocation.value == "Laureles, Medellín")) {
            _draftLocation.value = userLoc
        }

        navigateTo(Screen.ClientRequestService)
    }

    fun selectServiceCategory(categoryId: String) {
        _selectedCategoryIdForRequest.value = categoryId
        _requestFormError.value = null
    }

    fun updateDraft(description: String, date: String, time: String, location: String) {
        _draftDescription.value = description
        _draftDate.value = date
        _draftTime.value = time
        _draftLocation.value = location
    }

    fun validateAndProceedToConfirm(
        description: String,
        date: String,
        time: String,
        location: String,
        categoryId: String? = null
    ) {
        val cleanDesc = description.trim()
        val cleanLoc = location.trim()
        val proId = _selectedProfessionalId.value
        if (proId.isNullOrBlank()) {
            _requestFormError.value = "Debes seleccionar un profesional válido."
            return
        }
        if (cleanDesc.isBlank()) {
            _requestFormError.value = "Por favor explica el problema o trabajo que necesitas."
            return
        }
        if (cleanLoc.isBlank()) {
            _requestFormError.value = "Por favor indica la ubicación donde se realizará el servicio."
            return
        }
        _requestFormError.value = null
        _createRequestState.value = CreateRequestUiState.Idle
        if (!categoryId.isNullOrBlank()) {
            _selectedCategoryIdForRequest.value = categoryId
        }
        updateDraft(cleanDesc, date.trim().ifBlank { "Hoy" }, time.trim().ifBlank { "Por confirmar" }, cleanLoc)
        navigateTo(Screen.ClientConfirmRequest)
    }

    fun submitServiceRequest(
        description: String,
        location: String,
        categoryId: String? = null
    ) {
        val cleanDesc = description.trim()
        val cleanLoc = location.trim()
        val proId = _selectedProfessionalId.value

        if (proId.isNullOrBlank()) {
            _requestFormError.value = "Debes seleccionar un profesional válido."
            return
        }
        if (cleanDesc.isBlank()) {
            _requestFormError.value = "Por favor explica el problema o trabajo que necesitas."
            return
        }
        if (cleanLoc.isBlank()) {
            _requestFormError.value = "Por favor indica la ubicación donde se realizará el servicio."
            return
        }

        val resolvedCatId = categoryId ?: _selectedCategoryIdForRequest.value
        _requestFormError.value = null
        _createRequestState.value = CreateRequestUiState.Loading

        viewModelScope.launch {
            val result = repository.createServiceRequest(
                professionalId = proId,
                description = cleanDesc,
                scheduledDate = _draftDate.value.ifBlank { "Hoy" },
                scheduledTime = _draftTime.value.ifBlank { "Por confirmar" },
                location = cleanLoc,
                categoryId = resolvedCatId
            )
            result.fold(
                onSuccess = { newReq ->
                    _createRequestState.value = CreateRequestUiState.Success(newReq)
                    _activeTrackingRequestId.value = newReq.id
                    _workerSelectedRequestId.value = newReq.id
                    _draftDescription.value = cleanDesc
                    _draftLocation.value = cleanLoc
                    val currentList = if (_clientRequestsState.value is ClientRequestsUiState.Success) {
                        (_clientRequestsState.value as ClientRequestsUiState.Success).requests
                    } else emptyList()
                    _clientRequestsState.value = ClientRequestsUiState.Success(
                        listOf(newReq) + currentList.filter { it.id != newReq.id }
                    )
                    navigateTo(Screen.ClientRequestSent)
                },
                onFailure = { error ->
                    _createRequestState.value = CreateRequestUiState.Error(
                        error.message ?: "Ocurrió un error al enviar la solicitud a Supabase."
                    )
                }
            )
        }
    }

    fun confirmDraftAndSend() {
        submitServiceRequest(
            description = _draftDescription.value,
            location = _draftLocation.value,
            categoryId = _selectedCategoryIdForRequest.value
        )
    }

    fun loadClientRequests(forceRefresh: Boolean = false) {
        if (!forceRefresh && _clientRequestsState.value is ClientRequestsUiState.Success) {
            val list = (_clientRequestsState.value as ClientRequestsUiState.Success).requests
            if (list.isNotEmpty()) return
        }
        _clientRequestsState.value = ClientRequestsUiState.Loading
        viewModelScope.launch {
            val res = repository.checkAndLoadSupabaseRequests()
            res.fold(
                onSuccess = { allReqs ->
                    val filtered = if (currentUser.value.role == UserRole.CLIENT) {
                        val authId = com.example.service.AuthService.currentUserId()
                        if (authId != null) {
                            allReqs.filter { it.clientId == authId }
                        } else {
                            allReqs.filter { it.clientId == currentUser.value.id }
                        }
                    } else {
                        allReqs
                    }
                    _clientRequestsState.value = ClientRequestsUiState.Success(filtered.sortedByDescending { it.createdAt })
                },
                onFailure = { err ->
                    _clientRequestsState.value = ClientRequestsUiState.Error(
                        err.message ?: "No se pudieron cargar tus solicitudes. Verifica tu conexión."
                    )
                }
            )
        }
    }

    fun openTracking(requestId: String) {
        _activeTrackingRequestId.value = requestId
        _clientActionState.value = ClientActionUiState.Idle
        _reviewActionState.value = ReviewActionUiState.Idle
        viewModelScope.launch {
            repository.getServiceRequestDetail(requestId)
        }
        navigateTo(Screen.ClientTracking)
    }

    fun refreshCurrentTrackingRequest() {
        val reqId = _activeTrackingRequestId.value ?: return
        viewModelScope.launch {
            repository.getServiceRequestDetail(reqId)
        }
    }

    fun cancelClientRequest(requestId: String) {
        _clientActionState.value = ClientActionUiState.Loading
        viewModelScope.launch {
            val res = repository.updateRequestStatus(requestId, ServiceStatus.CANCELLED)
            res.fold(
                onSuccess = {
                    _clientActionState.value = ClientActionUiState.Success("Solicitud cancelada.")
                    loadClientRequests(forceRefresh = true)
                },
                onFailure = { err ->
                    _clientActionState.value = ClientActionUiState.Error(
                        err.message ?: "No se pudo cancelar la solicitud."
                    )
                }
            )
        }
    }

    fun resetClientActionState() {
        _clientActionState.value = ClientActionUiState.Idle
    }

    fun advanceServiceStatus(requestId: String) {
        val currentReq = requests.value.find { it.id == requestId } ?: return
        val nextStatus = when (currentReq.status) {
            ServiceStatus.PENDING -> ServiceStatus.ACCEPTED
            ServiceStatus.ACCEPTED -> ServiceStatus.ON_THE_WAY
            ServiceStatus.ON_THE_WAY -> ServiceStatus.IN_PROGRESS
            ServiceStatus.IN_PROGRESS -> ServiceStatus.COMPLETED
            ServiceStatus.COMPLETED -> ServiceStatus.COMPLETED
            ServiceStatus.CANCELLED -> ServiceStatus.CANCELLED
        }
        updateRequestStatusExplicit(requestId, nextStatus)
    }

    fun updateRequestStatusExplicit(requestId: String, status: ServiceStatus) {
        _workerActionState.value = WorkerActionUiState.Loading(requestId, status.code)
        viewModelScope.launch {
            val res = repository.updateRequestStatus(requestId, status)
            res.fold(
                onSuccess = { updated ->
                    _workerActionState.value = WorkerActionUiState.Success(
                        requestId = requestId,
                        newStatus = updated.status,
                        message = "Estado actualizado a ${updated.status.labelEs}."
                    )
                    loadWorkerRequests(forceRefresh = true)
                },
                onFailure = { err ->
                    _workerActionState.value = WorkerActionUiState.Error(
                        requestId = requestId,
                        message = err.message ?: "No se pudo actualizar el estado del servicio."
                    )
                }
            )
        }
    }

    fun submitRatingAndReview(requestId: String, stars: Double, comment: String) {
        _reviewActionState.value = ReviewActionUiState.Loading
        viewModelScope.launch {
            val res = repository.submitReview(requestId, stars, comment)
            res.fold(
                onSuccess = {
                    _reviewActionState.value = ReviewActionUiState.Success("¡Gracias por compartir tu reseña!")
                    loadClientRequests(forceRefresh = true)
                    screenStack.clear()
                    navigateTo(Screen.ClientHome)
                },
                onFailure = { err ->
                    _reviewActionState.value = ReviewActionUiState.Error(
                        err.message ?: "No se pudo guardar tu calificación. Intenta nuevamente."
                    )
                }
            )
        }
    }

    fun resetReviewActionState() {
        _reviewActionState.value = ReviewActionUiState.Idle
    }

    fun loadWorkerRequests(forceRefresh: Boolean = false) {
        if (!forceRefresh && _workerRequestsState.value is WorkerRequestsUiState.Success) {
            val list = (_workerRequestsState.value as WorkerRequestsUiState.Success).requests
            if (list.isNotEmpty()) return
        }
        _workerRequestsState.value = WorkerRequestsUiState.Loading
        viewModelScope.launch {
            val authId = AuthService.currentUserId()
            var workerPro = repository.professionals.value.find {
                it.userId == currentUser.value.id || (authId != null && it.userId == authId)
            }
            if (workerPro == null && SupabaseClient.isConfigured && authId != null) {
                repository.checkAndLoadSupabaseProfessionals()
                workerPro = repository.professionals.value.find {
                    it.userId == authId || it.userId == currentUser.value.id
                }
            }
            if (workerPro == null && (!SupabaseClient.isConfigured || authId == null)) {
                workerPro = repository.professionals.value.find { it.id == "pro_mateo" }
            }

            val res = repository.checkAndLoadSupabaseRequests()
            res.fold(
                onSuccess = { allReqs ->
                    val resolvedProId = workerPro?.id
                    val filtered = if (resolvedProId != null) {
                        allReqs.filter { it.professionalId == resolvedProId }
                            .sortedByDescending { it.createdAt }
                    } else {
                        emptyList()
                    }
                    _workerRequestsState.value = WorkerRequestsUiState.Success(filtered)
                },
                onFailure = { err ->
                    _workerRequestsState.value = WorkerRequestsUiState.Error(
                        err.message ?: "No se pudieron cargar las solicitudes recibidas."
                    )
                }
            )
        }
    }

    fun acceptWorkerRequest(requestId: String, onComplete: (() -> Unit)? = null) {
        _workerActionState.value = WorkerActionUiState.Loading(requestId, "accept")
        viewModelScope.launch {
            val res = repository.updateRequestStatus(requestId, ServiceStatus.ACCEPTED)
            res.fold(
                onSuccess = {
                    _workerActionState.value = WorkerActionUiState.Success(
                        requestId = requestId,
                        newStatus = ServiceStatus.ACCEPTED,
                        message = "Solicitud aceptada exitosamente."
                    )
                    loadWorkerRequests(forceRefresh = true)
                    onComplete?.invoke()
                },
                onFailure = { err ->
                    _workerActionState.value = WorkerActionUiState.Error(
                        requestId = requestId,
                        message = err.message ?: "No se pudo aceptar la solicitud."
                    )
                }
            )
        }
    }

    fun rejectWorkerRequest(requestId: String, onComplete: (() -> Unit)? = null) {
        _workerActionState.value = WorkerActionUiState.Loading(requestId, "reject")
        viewModelScope.launch {
            val res = repository.updateRequestStatus(requestId, ServiceStatus.CANCELLED)
            res.fold(
                onSuccess = {
                    _workerActionState.value = WorkerActionUiState.Success(
                        requestId = requestId,
                        newStatus = ServiceStatus.CANCELLED,
                        message = "Solicitud rechazada."
                    )
                    loadWorkerRequests(forceRefresh = true)
                    onComplete?.invoke()
                },
                onFailure = { err ->
                    _workerActionState.value = WorkerActionUiState.Error(
                        requestId = requestId,
                        message = err.message ?: "No se pudo rechazar la solicitud."
                    )
                }
            )
        }
    }

    fun resetWorkerActionState() {
        _workerActionState.value = WorkerActionUiState.Idle
    }

    fun refreshSelectedWorkerRequest() {
        val reqId = _workerSelectedRequestId.value ?: return
        viewModelScope.launch {
            repository.getServiceRequestDetail(reqId)
        }
    }

    fun openWorkerRequestDetail(requestId: String) {
        _workerSelectedRequestId.value = requestId
        _workerActionState.value = WorkerActionUiState.Idle
        viewModelScope.launch {
            repository.getServiceRequestDetail(requestId)
        }
        navigateTo(Screen.WorkerRequestDetail)
    }

    fun openWorkerActiveService(requestId: String) {
        _workerSelectedRequestId.value = requestId
        _workerActionState.value = WorkerActionUiState.Idle
        viewModelScope.launch {
            repository.getServiceRequestDetail(requestId)
        }
        navigateTo(Screen.WorkerActiveService)
    }

    fun updateWorkerProfile(
        name: String,
        phone: String,
        profession: String,
        description: String,
        experienceYears: Int,
        available: Boolean,
        location: String
    ) {
        _profileActionState.value = ProfileActionUiState.Loading
        viewModelScope.launch {
            val res = repository.updateWorkerProfile(name, phone, profession, description, experienceYears, available, location)
            res.fold(
                onSuccess = {
                    _profileActionState.value = ProfileActionUiState.Success("Perfil profesional actualizado.")
                    navigateBack()
                },
                onFailure = { err ->
                    _profileActionState.value = ProfileActionUiState.Error(
                        err.message ?: "No se pudieron guardar los cambios."
                    )
                }
            )
        }
    }

    fun updateClientProfile(name: String, phone: String, location: String, onSuccess: (() -> Unit)? = null) {
        _profileActionState.value = ProfileActionUiState.Loading
        viewModelScope.launch {
            val res = repository.updateClientProfile(name, phone, location)
            res.fold(
                onSuccess = {
                    _profileActionState.value = ProfileActionUiState.Success("Tu información se guardó correctamente.")
                    onSuccess?.invoke()
                },
                onFailure = { err ->
                    _profileActionState.value = ProfileActionUiState.Error(
                        err.message ?: "No se pudo actualizar tu perfil."
                    )
                }
            )
        }
    }

    fun resetProfileActionState() {
        _profileActionState.value = ProfileActionUiState.Idle
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            _notificationsLoading.value = true
            _notificationsError.value = null
            val res = repository.checkAndLoadSupabaseNotifications()
            res.onFailure { err ->
                _notificationsError.value = err.message ?: "No se pudieron actualizar las notificaciones."
            }
            _notificationsLoading.value = false
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }
}
