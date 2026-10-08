package com.example

import com.example.model.ServiceStatus
import com.example.model.UserRole
import com.example.service.InMemoryOficiosRepository
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OficiosCercaRobolectricTest {

    private lateinit var repository: InMemoryOficiosRepository
    private lateinit var viewModel: OficiosViewModel

    @Before
    fun setup() {
        repository = InMemoryOficiosRepository()
        viewModel = OficiosViewModel(repository)
    }

    @Test
    fun testInitialStateAndNavigationFlow() = runTest {
        assertEquals(Screen.Welcome, viewModel.currentScreen.value)

        // Select Client Role
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        assertEquals(Screen.ClientHome, viewModel.currentScreen.value)
        assertEquals("Laura", viewModel.currentUser.value.name)
        assertEquals(UserRole.CLIENT, viewModel.currentUser.value.role)

        // Navigation to Search & Results
        viewModel.navigateTo(Screen.ClientSearch)
        assertEquals(Screen.ClientSearch, viewModel.currentScreen.value)

        viewModel.setSearchQuery("plomero")
        advanceUntilIdle()

        val filtered = viewModel.filteredProfessionals.first { it.isNotEmpty() }
        assertTrue(filtered.any { it.name.contains("Mateo") })

        // Open Detail
        viewModel.openProfessionalDetail("pro_mateo")
        advanceUntilIdle()
        assertEquals(Screen.ClientProfessionalDetail, viewModel.currentScreen.value)
        val selectedPro = viewModel.selectedProfessional.first { it != null }
        assertEquals("Mateo Ramírez", selectedPro?.name)

        // Request Service Flow
        viewModel.startServiceRequest("pro_mateo")
        assertEquals(Screen.ClientRequestService, viewModel.currentScreen.value)

        viewModel.updateDraft(
            description = "Fuga de agua debajo del lavaplatos.",
            date = "Hoy",
            time = "10:00 AM",
            location = "Laureles, Medellín"
        )
        viewModel.navigateTo(Screen.ClientConfirmRequest)
        assertEquals(Screen.ClientConfirmRequest, viewModel.currentScreen.value)

        // Confirm & Send
        viewModel.confirmDraftAndSend()
        assertEquals(Screen.ClientRequestSent, viewModel.currentScreen.value)

        // Tracking & Status advancement
        val activeId = viewModel.activeTrackingRequestId.value
        assertNotNull(activeId)
        viewModel.openTracking(activeId!!)
        advanceUntilIdle()
        assertEquals(Screen.ClientTracking, viewModel.currentScreen.value)

        viewModel.advanceServiceStatus(activeId)
        advanceUntilIdle()
        val updatedReq = viewModel.requests.first { list -> list.any { it.id == activeId && it.status == ServiceStatus.ACCEPTED } }
            .find { it.id == activeId }
        assertNotNull(updatedReq)
        assertEquals(ServiceStatus.ACCEPTED, updatedReq?.status)

        // Rating & Review submission
        viewModel.submitRatingAndReview(activeId, 5.0, "Excelente servicio y rapidez.")
        assertEquals(Screen.ClientHome, viewModel.currentScreen.value)
    }

    @Test
    fun testWorkerFlow() = runTest {
        viewModel.selectRoleAndProceed(UserRole.WORKER)
        assertEquals(Screen.WorkerHome, viewModel.currentScreen.value)
        assertEquals(UserRole.WORKER, viewModel.currentUser.value.role)

        // Worker requests
        viewModel.navigateTo(Screen.WorkerRequests)
        assertEquals(Screen.WorkerRequests, viewModel.currentScreen.value)

        val reqs = viewModel.requests.value
        assertTrue(reqs.isNotEmpty())

        val firstReq = reqs.first()
        viewModel.openWorkerRequestDetail(firstReq.id)
        advanceUntilIdle()
        assertEquals(Screen.WorkerRequestDetail, viewModel.currentScreen.value)

        viewModel.updateRequestStatusExplicit(firstReq.id, ServiceStatus.IN_PROGRESS)
        viewModel.openWorkerActiveService(firstReq.id)
        advanceUntilIdle()
        assertEquals(Screen.WorkerActiveService, viewModel.currentScreen.value)
        
        val updatedReq = viewModel.requests.first { list -> list.any { it.id == firstReq.id && it.status == ServiceStatus.IN_PROGRESS } }
            .find { it.id == firstReq.id }
        assertEquals(ServiceStatus.IN_PROGRESS, updatedReq?.status)
    }
}
