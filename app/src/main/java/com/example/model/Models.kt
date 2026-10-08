package com.example.model

/**
 * Enums and domain models aligned with Supabase schema specifications
 */

enum class UserRole {
    CLIENT,
    WORKER
}

enum class ServiceStatus(val code: String, val labelEs: String) {
    PENDING("pending", "Pendiente"),
    ACCEPTED("accepted", "Aceptado"),
    ON_THE_WAY("on_the_way", "En camino"),
    IN_PROGRESS("in_progress", "En progreso"),
    COMPLETED("completed", "Finalizado"),
    CANCELLED("cancelled", "Cancelado");

    companion object {
        fun fromCode(code: String): ServiceStatus =
            entries.find { it.code == code } ?: PENDING
    }
}

data class ServiceCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val suggestedKeywords: List<String> = emptyList()
)

data class Review(
    val id: String,
    val serviceRequestId: String,
    val clientName: String,
    val rating: Double,
    val comment: String,
    val date: String
)

data class Professional(
    val id: String,
    val userId: String,
    val name: String,
    val profession: String,
    val description: String,
    val experienceYears: Int,
    val rating: Double,
    val servicesCompleted: Int,
    val distanceKm: Double,
    val available: Boolean,
    val categoryIds: List<String>,
    val servicesOffered: List<String>,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val reviews: List<Review> = emptyList(),
    val avatarInitial: String = name.firstOrNull()?.toString() ?: "P"
)

data class ServiceRequest(
    val id: String,
    val clientId: String,
    val clientName: String,
    val clientPhone: String = "300 123 4567",
    val professionalId: String,
    val professionalName: String,
    val professionalProfession: String,
    val categoryId: String,
    val categoryName: String,
    val description: String,
    val location: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val status: ServiceStatus,
    val ratingGiven: Double? = null,
    val reviewComment: String? = null,
    val createdAt: String
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val date: String,
    val relatedRequestId: String? = null
)

data class UserProfile(
    val id: String,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val location: String
)
