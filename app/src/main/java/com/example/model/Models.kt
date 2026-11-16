package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enums and domain models aligned with Supabase schema specifications
 */

enum class UserRole {
    CLIENT,
    WORKER
}

enum class ServiceStatus(val code: String, val labelEs: String) {
    PENDING("pending", "Pendiente"),
    ACCEPTED("accepted", "Aceptada"),
    ON_THE_WAY("on_the_way", "En camino"),
    IN_PROGRESS("in_progress", "En proceso"),
    COMPLETED("completed", "Finalizada"),
    CANCELLED("cancelled", "Cancelada");

    fun canTransitionTo(target: ServiceStatus): Boolean {
        if (this == target) return false
        return when (this) {
            PENDING -> target == ACCEPTED || target == CANCELLED
            ACCEPTED -> target == ON_THE_WAY || target == IN_PROGRESS || target == COMPLETED || target == CANCELLED
            ON_THE_WAY -> target == IN_PROGRESS || target == COMPLETED || target == CANCELLED
            IN_PROGRESS -> target == COMPLETED
            COMPLETED -> false
            CANCELLED -> false
        }
    }

    companion object {
        fun fromCode(code: String?): ServiceStatus {
            if (code.isNullOrBlank()) return PENDING
            val normalized = code.trim().lowercase()
            return when {
                normalized in listOf("pending", "pendiente") -> PENDING
                normalized in listOf("accepted", "aceptada", "aceptado") -> ACCEPTED
                normalized in listOf("on_the_way", "on the way", "en camino", "camino") -> ON_THE_WAY
                normalized in listOf("in_progress", "in progress", "en proceso", "en progreso", "proceso", "progreso") -> IN_PROGRESS
                normalized in listOf("completed", "finalizada", "finalizado", "completada", "completado") -> COMPLETED
                normalized in listOf("cancelled", "canceled", "cancelada", "cancelado") -> CANCELLED
                else -> entries.find { it.code.equals(normalized, ignoreCase = true) } ?: PENDING
            }
        }
    }
}

/**
 * DTO matching Supabase public.categories table
 */
@Serializable
data class SupabaseCategoryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * DTO matching Supabase public.profiles table
 */
@Serializable
data class SupabaseProfileDto(
    val id: String,
    val name: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("nombre")
    val nombre: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val location: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val displayName: String?
        get() = name?.takeIf { it.isNotBlank() }
            ?: fullName?.takeIf { it.isNotBlank() }
            ?: nombre?.takeIf { it.isNotBlank() }
}

/**
 * DTO matching Supabase public.professional_services table
 */
@Serializable
data class SupabaseProfessionalServiceDto(
    val id: String,
    @SerialName("professional_id")
    val professionalId: String? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("service_description")
    val serviceDescription: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val categories: SupabaseCategoryDto? = null
)

/**
 * Domain item representing a specific service/trade offered by a professional
 */
@Serializable
data class ProfessionalServiceItem(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val serviceDescription: String
)

/**
 * DTO matching Supabase public.professionals table with joined relations
 */
@Serializable
data class SupabaseProfessionalDto(
    val id: String,
    @SerialName("profile_id")
    val profileId: String? = null,
    val profession: String? = null,
    val description: String? = null,
    @SerialName("experience_years")
    val experienceYears: Int? = null,
    val rating: Double? = null,
    @SerialName("total_reviews")
    val totalReviews: Int? = null,
    val available: Boolean? = true,
    val location: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val profiles: SupabaseProfileDto? = null,
    val profile: SupabaseProfileDto? = null,
    @SerialName("professional_services")
    val professionalServices: List<SupabaseProfessionalServiceDto> = emptyList()
)

/**
 * DTO matching Supabase public.professionals columns for inserting new records
 */
@Serializable
data class SupabaseProfessionalInsertDto(
    val id: String,
    @SerialName("profile_id")
    val profileId: String,
    val profession: String,
    val description: String,
    @SerialName("experience_years")
    val experienceYears: Int = 1,
    val available: Boolean = true,
    val location: String = "Medellín",
    val rating: Double = 5.0,
    @SerialName("total_reviews")
    val totalReviews: Int = 0
)

/**
 * DTO matching Supabase public.service_requests table
 */
@Serializable
data class SupabaseServiceRequestDto(
    val id: String? = null,
    @SerialName("client_id")
    val clientId: String? = null,
    @SerialName("professional_id")
    val professionalId: String? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    val description: String? = null,
    val location: String? = null,
    val status: String? = "pending",
    @SerialName("scheduled_at")
    val scheduledAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val professionals: SupabaseProfessionalDto? = null,
    val categories: SupabaseCategoryDto? = null,
    val profiles: SupabaseProfileDto? = null
)

/**
 * DTO matching Supabase public.reviews table
 */
@Serializable
data class SupabaseReviewDto(
    val id: String? = null,
    @SerialName("client_id")
    val clientId: String? = null,
    @SerialName("professional_id")
    val professionalId: String? = null,
    @SerialName("request_id")
    val requestId: String? = null,
    val rating: Double? = null,
    val comment: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val profiles: SupabaseProfileDto? = null
)

/**
 * DTO matching Supabase public.notifications table
 */
@Serializable
data class SupabaseNotificationDto(
    val id: String,
    @SerialName("profile_id")
    val profileId: String? = null,
    val title: String,
    val message: String,
    @SerialName("is_read")
    val isRead: Boolean = false,
    val type: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

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
    val servicesList: List<ProfessionalServiceItem> = emptyList(),
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
