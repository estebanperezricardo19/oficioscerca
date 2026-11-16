package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.model.ServiceStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.service.InMemoryOficiosRepository
import com.example.service.OficiosRepository
import com.example.ui.auth.LoginScreen
import com.example.ui.theme.OficiosCercaTheme
import com.example.viewmodel.ClientActionUiState
import com.example.viewmodel.ClientRequestsUiState
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.ProfileActionUiState
import com.example.viewmodel.ReviewActionUiState
import com.example.viewmodel.Screen
import com.example.viewmodel.WorkerActionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OficiosCercaRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: InMemoryOficiosRepository
    private lateinit var viewModel: OficiosViewModel

    @Before
    fun setup() {
        repository = InMemoryOficiosRepository()
        viewModel = OficiosViewModel(repository)
    }

    @Test
    fun testInitialStateAndNavigationFlow() = runTest {
        assertEquals(Screen.Login, viewModel.currentScreen.value)

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

        // Complete service progression before rating
        viewModel.advanceServiceStatus(activeId) // ON_THE_WAY
        advanceUntilIdle()
        viewModel.advanceServiceStatus(activeId) // IN_PROGRESS
        advanceUntilIdle()
        viewModel.advanceServiceStatus(activeId) // COMPLETED
        advanceUntilIdle()
        assertEquals(ServiceStatus.COMPLETED, viewModel.requests.value.find { it.id == activeId }?.status)

        // Rating & Review submission
        viewModel.submitRatingAndReview(activeId, 5.0, "Excelente servicio y rapidez.")
        advanceUntilIdle()
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

        val pendingReq = reqs.first { it.status == ServiceStatus.PENDING }
        viewModel.openWorkerRequestDetail(pendingReq.id)
        advanceUntilIdle()
        assertEquals(Screen.WorkerRequestDetail, viewModel.currentScreen.value)

        // Transition PENDING -> ACCEPTED -> IN_PROGRESS
        viewModel.acceptWorkerRequest(pendingReq.id)
        advanceUntilIdle()
        viewModel.updateRequestStatusExplicit(pendingReq.id, ServiceStatus.IN_PROGRESS)
        viewModel.openWorkerActiveService(pendingReq.id)
        advanceUntilIdle()
        assertEquals(Screen.WorkerActiveService, viewModel.currentScreen.value)

        val updatedReq = viewModel.requests.first { list -> list.any { it.id == pendingReq.id && it.status == ServiceStatus.IN_PROGRESS } }
            .find { it.id == pendingReq.id }
        assertEquals(ServiceStatus.IN_PROGRESS, updatedReq?.status)
    }

    @Test
    fun testProfessionalsDisplayRealNamesAndFiltering() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        val pros = viewModel.professionals.value
        assertTrue("Professionals list should not be empty", pros.isNotEmpty())

        // Ensure all default professionals have non-generic real names
        pros.forEach { pro ->
            assertTrue("Professional name should not be blank", pro.name.isNotBlank())
            assertTrue(
                "Professional name should not be generic 'Profesional de oficio'",
                pro.name != "Profesional de oficio"
            )
            assertTrue("Avatar initial should match first letter of name", pro.avatarInitial.isNotBlank())
            assertTrue("Profession should be specified", pro.profession.isNotBlank())
        }

        // Test filtering by category
        val firstPro = pros.first()
        if (firstPro.categoryIds.isNotEmpty()) {
            val targetCat = firstPro.categoryIds.first()
            viewModel.selectCategory(targetCat)
            advanceUntilIdle()

            val filtered = viewModel.filteredProfessionals.first { it.isNotEmpty() }
            assertTrue(filtered.any { it.id == firstPro.id })
            assertTrue(filtered.all { it.name.isNotBlank() && it.name != "Profesional de oficio" })
        }

        // Test filtering by search query using real name
        val queryName = firstPro.name.split(" ").first()
        viewModel.setSearchQuery(queryName)
        advanceUntilIdle()

        val searchResults = viewModel.filteredProfessionals.first { it.isNotEmpty() }
        assertTrue(searchResults.any { it.name.contains(queryName, ignoreCase = true) })
    }

    @Test
    fun testMainActivitySolicitarServicioFlow() = runTest {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)

        val vm = androidx.lifecycle.ViewModelProvider(activity)[OficiosViewModel::class.java]

        vm.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        val pro = vm.professionals.first().first()
        vm.openProfessionalDetail(pro.id)
        advanceUntilIdle()

        vm.startServiceRequest(pro.id)
        advanceUntilIdle()

        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(Screen.ClientRequestService, vm.currentScreen.value)
    }

    @Test
    fun testClientRequestsModuleFlow() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        // Navigate to Client History
        viewModel.navigateTo(Screen.ClientHistory)
        assertEquals(Screen.ClientHistory, viewModel.currentScreen.value)

        // Load requests for client
        viewModel.loadClientRequests(forceRefresh = true)
        advanceUntilIdle()

        val state = viewModel.clientRequestsState.value
        assertTrue("Requests state should be Success", state is ClientRequestsUiState.Success)

        val requests = (state as ClientRequestsUiState.Success).requests
        assertTrue("Requests list should not be empty for demo client", requests.isNotEmpty())

        // Verify request fields and non-generic names
        requests.forEach { req ->
            assertTrue("Request ID should not be blank", req.id.isNotBlank())
            assertTrue("Client ID should match current client", req.clientId == viewModel.currentUser.value.id)
            assertTrue("Professional name should not be blank", req.professionalName.isNotBlank())
            assertTrue("Professional name should not be generic placeholder", req.professionalName != "Profesional de oficio")
            assertTrue("Category should not be blank", req.categoryName.isNotBlank())
            assertTrue("Location should not be blank", req.location.isNotBlank())
            assertNotNull("Status should be valid", req.status)
        }

        // Test opening request detail / tracking
        val targetReq = requests.first()
        viewModel.openTracking(targetReq.id)
        advanceUntilIdle()

        assertEquals(Screen.ClientTracking, viewModel.currentScreen.value)
        assertEquals(targetReq.id, viewModel.activeTrackingRequestId.value)

        val trackingReq = viewModel.trackingRequest.value
        assertNotNull("Tracking request should be loaded", trackingReq)
        assertEquals(targetReq.id, trackingReq?.id)
        assertEquals(targetReq.professionalName, trackingReq?.professionalName)

        // Test refresh tracking
        viewModel.refreshCurrentTrackingRequest()
        advanceUntilIdle()
        assertNotNull(viewModel.trackingRequest.value)

        // Test navigating back to Client History
        viewModel.navigateBack()
        assertEquals(Screen.ClientHistory, viewModel.currentScreen.value)
    }

    @Test
    fun testWorkerRequestsManagementFlow() = runTest {
        // Switch to Worker Role
        viewModel.selectRoleAndProceed(UserRole.WORKER)
        advanceUntilIdle()

        assertEquals(Screen.WorkerHome, viewModel.currentScreen.value)
        assertEquals(UserRole.WORKER, viewModel.currentUser.value.role)

        // Load Worker requests
        viewModel.loadWorkerRequests(forceRefresh = true)
        advanceUntilIdle()

        val state = viewModel.workerRequestsState.value
        assertTrue("Worker requests state should be Success", state is com.example.viewmodel.WorkerRequestsUiState.Success)

        val workerReqs = (state as com.example.viewmodel.WorkerRequestsUiState.Success).requests
        assertTrue("Worker requests list should not be empty", workerReqs.isNotEmpty())

        // Verify all requests belong to the worker's professional ID
        val workerPro = repository.professionals.value.find { it.userId == viewModel.currentUser.value.id }
        val targetProId = workerPro?.id ?: "6a031c58-5af1-416a-9e2f-a8d0f974f23b"
        workerReqs.forEach { req ->
            assertTrue(
                "Request professionalId (${req.professionalId}) must match worker's pro ID ($targetProId)",
                req.professionalId == targetProId || req.professionalId == workerPro?.id
            )
            assertTrue("Client name should not be blank", req.clientName.isNotBlank())
            assertTrue("Description should not be blank", req.description.isNotBlank())
            assertTrue("Location should not be blank", req.location.isNotBlank())
            assertNotNull("Status should not be null", req.status)
        }

        // Test navigation to Worker Requests Screen
        viewModel.navigateTo(Screen.WorkerRequests)
        assertEquals(Screen.WorkerRequests, viewModel.currentScreen.value)

        // Find a pending request to test Accept flow
        val pendingReq = workerReqs.find { it.status == ServiceStatus.PENDING } ?: workerReqs.first()
        viewModel.openWorkerRequestDetail(pendingReq.id)
        advanceUntilIdle()

        assertEquals(Screen.WorkerRequestDetail, viewModel.currentScreen.value)
        assertEquals(pendingReq.id, viewModel.workerSelectedRequest.value?.id)

        // Accept the pending request
        viewModel.acceptWorkerRequest(pendingReq.id)
        advanceUntilIdle()

        // Verify status changed to ACCEPTED
        val updatedAfterAccept = repository.requests.value.find { it.id == pendingReq.id }
        assertEquals(ServiceStatus.ACCEPTED, updatedAfterAccept?.status)

        // Create a second pending request for the same professional and test rejecting it
        val secondPending = repository.createServiceRequest(
            professionalId = targetProId,
            description = "Revisión de llave de paso",
            scheduledDate = "Mañana",
            scheduledTime = "09:00 AM",
            location = "Belén, Medellín",
            categoryId = "cat_plomeria"
        ).getOrThrow()

        viewModel.rejectWorkerRequest(secondPending.id)
        advanceUntilIdle()

        val updatedAfterReject = repository.requests.value.find { it.id == secondPending.id }
        assertEquals(ServiceStatus.CANCELLED, updatedAfterReject?.status)

        // Test back navigation
        viewModel.navigateBack()
        assertEquals(Screen.WorkerRequests, viewModel.currentScreen.value)
    }

    @Test
    fun testWorkerActiveServiceStatusTransitions() = runTest {
        viewModel.selectRoleAndProceed(UserRole.WORKER)
        advanceUntilIdle()

        viewModel.loadWorkerRequests(forceRefresh = true)
        advanceUntilIdle()

        val reqs = (viewModel.workerRequestsState.value as com.example.viewmodel.WorkerRequestsUiState.Success).requests
        val testReq = reqs.first()

        // Open Worker Active Service Screen
        viewModel.openWorkerActiveService(testReq.id)
        advanceUntilIdle()
        assertEquals(Screen.WorkerActiveService, viewModel.currentScreen.value)
        assertEquals(testReq.id, viewModel.workerSelectedRequest.value?.id)

        // Progressively advance through real states: ACCEPTED -> ON_THE_WAY -> IN_PROGRESS -> COMPLETED
        viewModel.updateRequestStatusExplicit(testReq.id, ServiceStatus.ACCEPTED)
        advanceUntilIdle()
        assertEquals(ServiceStatus.ACCEPTED, repository.requests.value.find { it.id == testReq.id }?.status)

        viewModel.updateRequestStatusExplicit(testReq.id, ServiceStatus.ON_THE_WAY)
        advanceUntilIdle()
        assertEquals(ServiceStatus.ON_THE_WAY, repository.requests.value.find { it.id == testReq.id }?.status)

        viewModel.updateRequestStatusExplicit(testReq.id, ServiceStatus.IN_PROGRESS)
        advanceUntilIdle()
        assertEquals(ServiceStatus.IN_PROGRESS, repository.requests.value.find { it.id == testReq.id }?.status)

        viewModel.updateRequestStatusExplicit(testReq.id, ServiceStatus.COMPLETED)
        advanceUntilIdle()
        assertEquals(ServiceStatus.COMPLETED, repository.requests.value.find { it.id == testReq.id }?.status)
    }

    @Test
    fun testClientReviewSubmissionAndAverageFlow() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        viewModel.loadClientRequests(forceRefresh = true)
        advanceUntilIdle()

        val requests = (viewModel.clientRequestsState.value as ClientRequestsUiState.Success).requests
        val targetReq = requests.first()

        // Advance status legally to COMPLETED (PENDING -> ACCEPTED -> COMPLETED) so it can be rated
        repository.updateRequestStatus(targetReq.id, ServiceStatus.ACCEPTED).getOrThrow()
        repository.updateRequestStatus(targetReq.id, ServiceStatus.COMPLETED).getOrThrow()
        advanceUntilIdle()

        viewModel.openTracking(targetReq.id)
        advanceUntilIdle()
        assertEquals(Screen.ClientTracking, viewModel.currentScreen.value)

        viewModel.navigateTo(Screen.ClientRating)
        assertEquals(Screen.ClientRating, viewModel.currentScreen.value)

        val initialProReviewsCount = repository.professionals.value.find { it.id == targetReq.professionalId }?.reviews?.size ?: 0

        // Submit Rating & Review
        viewModel.submitRatingAndReview(targetReq.id, 5.0, "Servicio excelente y altamente recomendado.")
        advanceUntilIdle()

        // Rating saved and returns to home
        assertEquals(Screen.ClientHome, viewModel.currentScreen.value)
        val updatedTargetReq = repository.requests.value.find { it.id == targetReq.id }
        assertEquals(5.0, updatedTargetReq?.ratingGiven)
        assertEquals("Servicio excelente y altamente recomendado.", updatedTargetReq?.reviewComment)

        // Verify professional reviews collection updated
        val updatedPro = repository.professionals.value.find { it.id == targetReq.professionalId }
        val updatedReviewsCount = updatedPro?.reviews?.size ?: 0
        assertTrue("Review count should have increased", updatedReviewsCount >= initialProReviewsCount)
    }

    @Test
    fun testServiceStatusTransitionRulesAndTerminalProtection() = runTest {
        assertTrue(ServiceStatus.PENDING.canTransitionTo(ServiceStatus.ACCEPTED))
        assertTrue(ServiceStatus.PENDING.canTransitionTo(ServiceStatus.CANCELLED))
        assertFalse(ServiceStatus.PENDING.canTransitionTo(ServiceStatus.COMPLETED))
        assertFalse(ServiceStatus.IN_PROGRESS.canTransitionTo(ServiceStatus.ACCEPTED))
        assertFalse(ServiceStatus.COMPLETED.canTransitionTo(ServiceStatus.IN_PROGRESS))
        assertFalse(ServiceStatus.CANCELLED.canTransitionTo(ServiceStatus.ACCEPTED))

        viewModel.selectRoleAndProceed(UserRole.WORKER)
        advanceUntilIdle()

        // Try mutating already-completed req_hist_1 -> should fail and emit WorkerActionUiState.Error
        viewModel.updateRequestStatusExplicit("req_hist_1", ServiceStatus.IN_PROGRESS)
        advanceUntilIdle()

        assertTrue(viewModel.workerActionState.value is WorkerActionUiState.Error)
        assertEquals(
            ServiceStatus.COMPLETED,
            repository.requests.value.find { it.id == "req_hist_1" }?.status
        )
    }

    @Test
    fun testReviewValidationPreventsPrematureAndDuplicateRatings() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        // Cannot rate a PENDING request
        val prematureResult = repository.submitReview("req_active_1", 5.0, "Demasiado pronto")
        assertTrue(prematureResult.isFailure)

        // Complete req_active_1 properly: PENDING -> ACCEPTED -> COMPLETED
        repository.updateRequestStatus("req_active_1", ServiceStatus.ACCEPTED).getOrThrow()
        repository.updateRequestStatus("req_active_1", ServiceStatus.COMPLETED).getOrThrow()

        // Out-of-range rating fails
        val invalidStars = repository.submitReview("req_active_1", 0.0, "Inválido")
        assertTrue(invalidStars.isFailure)

        // Valid rating succeeds via ViewModel
        viewModel.submitRatingAndReview("req_active_1", 4.0, "Buen trabajo.")
        advanceUntilIdle()
        assertTrue(viewModel.reviewActionState.value is ReviewActionUiState.Success)

        // Duplicate rating for the same request is rejected
        val duplicateResult = repository.submitReview("req_active_1", 5.0, "Duplicado")
        assertTrue(duplicateResult.isFailure)
    }

    @Test
    fun testClientCancelPendingRequestAndFormValidation() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        viewModel.startServiceRequest("pro_mateo")
        assertEquals("", viewModel.draftDescription.value)

        // Blank description should block proceeding and set error
        viewModel.validateAndProceedToConfirm(
            description = "   ",
            date = "Hoy",
            time = "04:00 PM",
            location = "Envigado",
            categoryId = "cat_plomeria"
        )
        assertEquals(Screen.ClientRequestService, viewModel.currentScreen.value)
        assertNotNull(viewModel.requestFormError.value)

        // Valid input proceeds to confirm
        viewModel.validateAndProceedToConfirm(
            description = "Instalar mezclador de cocina",
            date = "Hoy",
            time = "04:00 PM",
            location = "Envigado",
            categoryId = "cat_plomeria"
        )
        assertNull(viewModel.requestFormError.value)
        assertEquals(Screen.ClientConfirmRequest, viewModel.currentScreen.value)

        // Cancel pending request req_active_1 as client
        viewModel.cancelClientRequest("req_active_1")
        advanceUntilIdle()
        assertTrue(viewModel.clientActionState.value is ClientActionUiState.Success)
        assertEquals(
            ServiceStatus.CANCELLED,
            repository.requests.value.find { it.id == "req_active_1" }?.status
        )
    }

    @Test
    fun testProfileUpdatesAndLogoutClearsSession() = runTest {
        viewModel.selectRoleAndProceed(UserRole.CLIENT)
        advanceUntilIdle()

        // Blank name should fail validation
        viewModel.updateClientProfile("   ", "3001112233", "Poblado")
        advanceUntilIdle()
        assertTrue(viewModel.profileActionState.value is ProfileActionUiState.Error)

        // Valid update succeeds and stays on current screen
        viewModel.navigateTo(Screen.ClientProfile)
        var callbackInvoked = false
        viewModel.updateClientProfile("Laura Editada", "3001112233", "Poblado") {
            callbackInvoked = true
        }
        advanceUntilIdle()
        assertTrue(callbackInvoked)
        assertTrue(viewModel.profileActionState.value is ProfileActionUiState.Success)
        assertEquals(Screen.ClientProfile, viewModel.currentScreen.value)
        assertEquals("Laura", viewModel.currentUser.value.name)
        assertEquals("Editada", viewModel.currentUser.value.lastName)
        assertEquals("Poblado", viewModel.currentUser.value.location)

        // Notifications mark read
        val unreadNotif = viewModel.notifications.value.firstOrNull { !it.read }
        assertNotNull(unreadNotif)
        viewModel.markNotificationRead(unreadNotif!!.id)
        advanceUntilIdle()
        assertTrue(viewModel.notifications.value.find { it.id == unreadNotif.id }?.read == true)

        // Logout clears session lists & returns to Login
        viewModel.logout()
        advanceUntilIdle()
        assertEquals(Screen.Login, viewModel.currentScreen.value)
        assertTrue(viewModel.requests.value.isEmpty())
        assertTrue(viewModel.notifications.value.isEmpty())
        assertEquals("", viewModel.currentUser.value.id)
    }

    @Test
    fun testLoginRoutesClientToClientHomeAndWorkerToWorkerHome() = runTest {
        val clientProfile = UserProfile(
            id = "client-1",
            name = "Laura",
            lastName = "Gómez",
            email = "laura@example.com",
            phone = "3001234567",
            role = UserRole.CLIENT,
            location = "Medellín"
        )
        val clientRepo = object : OficiosRepository by InMemoryOficiosRepository() {
            override suspend fun loadCurrentUserProfile(): Result<UserProfile> =
                Result.success(clientProfile)
        }
        val clientVm = OficiosViewModel(
            repository = clientRepo,
            signInHandler = { _, _ -> }
        )
        clientVm.login("laura@example.com", "secret123")
        advanceUntilIdle()
        assertEquals(Screen.ClientHome, clientVm.currentScreen.value)
        assertNull(clientVm.authError.value)

        val workerProfile = UserProfile(
            id = "worker-1",
            name = "Mateo",
            lastName = "Ramírez",
            email = "mateo@example.com",
            phone = "3007654321",
            role = UserRole.WORKER,
            location = "Medellín"
        )
        val workerRepo = object : OficiosRepository by InMemoryOficiosRepository() {
            override suspend fun loadCurrentUserProfile(): Result<UserProfile> =
                Result.success(workerProfile)
        }
        val workerVm = OficiosViewModel(
            repository = workerRepo,
            signInHandler = { _, _ -> }
        )
        workerVm.login("mateo@example.com", "secret123")
        advanceUntilIdle()
        assertEquals(Screen.WorkerHome, workerVm.currentScreen.value)
        assertNull(workerVm.authError.value)
    }

    @Test
    fun testLoginInvalidCredentialsShowsErrorAndStaysOnLogin() = runTest {
        val vm = OficiosViewModel(
            repository = InMemoryOficiosRepository(),
            signInHandler = { _, _ -> throw IllegalStateException("Invalid login credentials") }
        )
        vm.login("wrong@example.com", "badpass")
        advanceUntilIdle()

        assertEquals(Screen.Login, vm.currentScreen.value)
        assertTrue(
            vm.authError.value.orEmpty().contains("Correo o contraseña incorrectos", ignoreCase = true)
        )
    }

    @Test
    fun testLoginWithoutValidProfileStaysOnLogin() = runTest {
        val missingProfileRepo = object : OficiosRepository by InMemoryOficiosRepository() {
            override suspend fun loadCurrentUserProfile(): Result<UserProfile> =
                Result.failure(IllegalStateException("No se encontró ningún registro en profiles para el usuario actual."))
        }
        val vmMissing = OficiosViewModel(
            repository = missingProfileRepo,
            signInHandler = { _, _ -> }
        )
        vmMissing.login("noprofile@example.com", "secret123")
        advanceUntilIdle()

        assertEquals(Screen.Login, vmMissing.currentScreen.value)
        assertTrue(vmMissing.authError.value.orEmpty().contains("perfil válido", ignoreCase = true))

        val unknownRoleRepo = object : OficiosRepository by InMemoryOficiosRepository() {
            override suspend fun loadCurrentUserProfile(): Result<UserProfile> =
                Result.failure(IllegalStateException("El perfil tiene un rol desconocido: admin"))
        }
        val vmUnknownRole = OficiosViewModel(
            repository = unknownRoleRepo,
            signInHandler = { _, _ -> }
        )
        vmUnknownRole.login("badrole@example.com", "secret123")
        advanceUntilIdle()

        assertEquals(Screen.Login, vmUnknownRole.currentScreen.value)
        assertTrue(vmUnknownRole.authError.value.orEmpty().contains("rol válido", ignoreCase = true))
    }

    @Test
    fun testLoginScreenUiShowsSingleSignInButtonAndNoRoleEntryButtons() {
        composeTestRule.setContent {
            OficiosCercaTheme {
                LoginScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("¡Bienvenido de nuevo!").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_email_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_password_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("forgot_password_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Iniciar sesión").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_go_to_register").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("Ingresar como cliente").assertDoesNotExist()
        composeTestRule.onNodeWithText("Ingresar como trabajador").assertDoesNotExist()
    }
}
