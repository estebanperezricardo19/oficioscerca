package com.example.service

import com.example.data.MockData
import com.example.model.AppNotification
import com.example.model.Professional
import com.example.model.Review
import com.example.model.ServiceCategory
import com.example.model.ServiceRequest
import com.example.model.ServiceStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Repository interface and in-memory Supabase-ready implementation.
 * Designed to swap directly with Supabase PostgREST client and Realtime subscriptions.
 */
interface OficiosRepository {
    val currentUser: StateFlow<UserProfile>
    val categories: StateFlow<List<ServiceCategory>>
    val professionals: StateFlow<List<Professional>>
    val requests: StateFlow<List<ServiceRequest>>
    val notifications: StateFlow<List<AppNotification>>

    fun switchUserRole(role: UserRole)
    fun updateClientProfile(name: String, phone: String, location: String)
    fun updateWorkerProfile(name: String, phone: String, profession: String, description: String, experienceYears: Int, available: Boolean, location: String)
    fun createServiceRequest(
        professionalId: String,
        description: String,
        scheduledDate: String,
        scheduledTime: String,
        location: String
    ): ServiceRequest
    fun updateRequestStatus(requestId: String, newStatus: ServiceStatus)
    fun submitReview(requestId: String, rating: Double, comment: String)
    fun markNotificationAsRead(notificationId: String)
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

    override fun switchUserRole(role: UserRole) {
        if (role == UserRole.CLIENT) {
            _currentUser.value = MockData.defaultClientUser
        } else {
            _currentUser.value = MockData.defaultWorkerUser
        }
    }

    override fun updateClientProfile(name: String, phone: String, location: String) {
        _currentUser.update {
            it.copy(name = name, phone = phone, location = location)
        }
    }

    override fun updateWorkerProfile(
        name: String,
        phone: String,
        profession: String,
        description: String,
        experienceYears: Int,
        available: Boolean,
        location: String
    ) {
        _currentUser.update {
            it.copy(name = name, phone = phone, location = location)
        }
        _professionals.update { list ->
            list.map { pro ->
                if (pro.id == "pro_mateo") {
                    pro.copy(
                        name = name,
                        profession = profession,
                        description = description,
                        experienceYears = experienceYears,
                        available = available,
                        locationName = location
                    )
                } else pro
            }
        }
    }

    override fun createServiceRequest(
        professionalId: String,
        description: String,
        scheduledDate: String,
        scheduledTime: String,
        location: String
    ): ServiceRequest {
        val pro = _professionals.value.find { it.id == professionalId }
            ?: _professionals.value.first()
        val cat = _categories.value.find { it.id in pro.categoryIds }
            ?: _categories.value.first()

        val newRequest = ServiceRequest(
            id = "req_" + UUID.randomUUID().toString().take(8),
            clientId = _currentUser.value.id,
            clientName = "${_currentUser.value.name} ${_currentUser.value.lastName}",
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
            createdAt = "2026-10-08T09:30:00"
        )

        _requests.update { listOf(newRequest) + it }

        // Also create notification
        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = "Solicitud enviada a ${pro.name}",
            message = "Servicio de ${cat.name}: esperando respuesta.",
            read = false,
            date = "Ahora",
            relatedRequestId = newRequest.id
        )
        _notifications.update { listOf(notif) + it }

        return newRequest
    }

    override fun updateRequestStatus(requestId: String, newStatus: ServiceStatus) {
        var updatedReq: ServiceRequest? = null
        _requests.update { list ->
            list.map { req ->
                if (req.id == requestId) {
                    val u = req.copy(status = newStatus)
                    updatedReq = u
                    u
                } else req
            }
        }

        updatedReq?.let { req ->
            val statusMsg = when (newStatus) {
                ServiceStatus.ACCEPTED -> "${req.professionalName} aceptó tu solicitud."
                ServiceStatus.ON_THE_WAY -> "¡${req.professionalName} está en camino hacia tu ubicación!"
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
                relatedRequestId = req.id
            )
            _notifications.update { listOf(notif) + it }
        }
    }

    override fun submitReview(requestId: String, rating: Double, comment: String) {
        var profId: String? = null
        val client = _currentUser.value

        _requests.update { list ->
            list.map { req ->
                if (req.id == requestId) {
                    profId = req.professionalId
                    req.copy(ratingGiven = rating, reviewComment = comment, status = ServiceStatus.COMPLETED)
                } else req
            }
        }

        profId?.let { pId ->
            val newReview = Review(
                id = "rev_" + UUID.randomUUID().toString().take(8),
                serviceRequestId = requestId,
                clientName = "${client.name} ${client.lastName.take(1)}.",
                rating = rating,
                comment = comment,
                date = "Hoy"
            )

            _professionals.update { proList ->
                proList.map { pro ->
                    if (pro.id == pId) {
                        val newReviews = listOf(newReview) + pro.reviews
                        val newAvg = ((pro.rating * pro.servicesCompleted + rating) / (pro.servicesCompleted + 1))
                        val roundedAvg = Math.round(newAvg * 10.0) / 10.0
                        pro.copy(
                            reviews = newReviews,
                            servicesCompleted = pro.servicesCompleted + 1,
                            rating = roundedAvg
                        )
                    } else pro
                }
            }
        }
    }

    override fun markNotificationAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(read = true) else it }
        }
    }
}
