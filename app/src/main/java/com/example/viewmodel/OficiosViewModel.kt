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
}

class OficiosViewModel(
    private val repository: OficiosRepository = InMemoryOficiosRepository()
) : ViewModel() {

    // Current navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation backstack for seamless back button support
    private val screenStack = mutableListOf<Screen>(Screen.Welcome)

    // Data streams from repository
    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val categories: StateFlow<List<ServiceCategory>> = repository.categories
    val professionals: StateFlow<List<Professional>> = repository.professionals
    val requests: StateFlow<List<ServiceRequest>> = repository.requests
    val notifications: StateFlow<List<AppNotification>> = repository.notifications

    // Active selections in client flow
    private val _selectedProfessionalId = MutableStateFlow<String?>("pro_mateo")
    val selectedProfessionalId: StateFlow<String?> = _selectedProfessionalId.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Request drafting state
    private val _draftDescription = MutableStateFlow("Fuga de agua debajo del lavaplatos.")
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
        pros.filter { pro ->
            val matchesCategory = catId == null || catId in pro.categoryIds
            val matchesQuery = query.isBlank() ||
                pro.name.contains(query, ignoreCase = true) ||
                pro.profession.contains(query, ignoreCase = true) ||
                pro.servicesOffered.any { it.contains(query, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
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

    fun selectRoleAndProceed(role: UserRole) {
        repository.switchUserRole(role)
        screenStack.clear()
        if (role == UserRole.CLIENT) {
            navigateTo(Screen.ClientHome)
        } else {
            navigateTo(Screen.WorkerHome)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun openProfessionalDetail(professionalId: String) {
        _selectedProfessionalId.value = professionalId
        navigateTo(Screen.ClientProfessionalDetail)
    }

    fun startServiceRequest(professionalId: String) {
        _selectedProfessionalId.value = professionalId
        navigateTo(Screen.ClientRequestService)
    }

    fun updateDraft(description: String, date: String, time: String, location: String) {
        _draftDescription.value = description
        _draftDate.value = date
        _draftTime.value = time
        _draftLocation.value = location
    }

    fun confirmDraftAndSend() {
        val proId = _selectedProfessionalId.value ?: "pro_mateo"
        val newReq = repository.createServiceRequest(
            professionalId = proId,
            description = _draftDescription.value,
            scheduledDate = _draftDate.value,
            scheduledTime = _draftTime.value,
            location = _draftLocation.value
        )
        _activeTrackingRequestId.value = newReq.id
        _workerSelectedRequestId.value = newReq.id
        navigateTo(Screen.ClientRequestSent)
    }

    fun openTracking(requestId: String) {
        _activeTrackingRequestId.value = requestId
        navigateTo(Screen.ClientTracking)
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
        repository.updateRequestStatus(requestId, nextStatus)
    }

    fun updateRequestStatusExplicit(requestId: String, status: ServiceStatus) {
        repository.updateRequestStatus(requestId, status)
    }

    fun submitRatingAndReview(requestId: String, stars: Double, comment: String) {
        repository.submitReview(requestId, stars, comment)
        screenStack.clear()
        navigateTo(Screen.ClientHome)
    }

    fun openWorkerRequestDetail(requestId: String) {
        _workerSelectedRequestId.value = requestId
        navigateTo(Screen.WorkerRequestDetail)
    }

    fun openWorkerActiveService(requestId: String) {
        _workerSelectedRequestId.value = requestId
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
        repository.updateWorkerProfile(name, phone, profession, description, experienceYears, available, location)
        navigateBack()
    }

    fun updateClientProfile(name: String, phone: String, location: String) {
        repository.updateClientProfile(name, phone, location)
        navigateBack()
    }

    fun markNotificationRead(id: String) {
        repository.markNotificationAsRead(id)
    }
}
