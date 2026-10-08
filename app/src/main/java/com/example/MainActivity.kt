package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.auth.RoleSelectScreen
import com.example.ui.auth.WelcomeScreen
import com.example.ui.client.ClientBottomNavBar
import com.example.ui.client.ClientConfirmRequestScreen
import com.example.ui.client.ClientHistoryScreen
import com.example.ui.client.ClientHomeScreen
import com.example.ui.client.ClientMapScreen
import com.example.ui.client.ClientNotificationsScreen
import com.example.ui.client.ClientProfessionalDetailScreen
import com.example.ui.client.ClientProfileScreen
import com.example.ui.client.ClientRatingScreen
import com.example.ui.client.ClientRequestSentScreen
import com.example.ui.client.ClientRequestServiceScreen
import com.example.ui.client.ClientResultsScreen
import com.example.ui.client.ClientSearchScreen
import com.example.ui.client.ClientTrackingScreen
import com.example.ui.theme.OficiosCercaTheme
import com.example.ui.worker.WorkerActiveServiceScreen
import com.example.ui.worker.WorkerEditProfileScreen
import com.example.ui.worker.WorkerHomeScreen
import com.example.ui.worker.WorkerProfileScreen
import com.example.ui.worker.WorkerRequestDetailScreen
import com.example.ui.worker.WorkerRequestsScreen
import com.example.ui.worker.WorkerReviewsScreen
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: OficiosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OficiosCercaTheme {
                OficiosApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OficiosApp(viewModel: OficiosViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle system back navigation cleanly
    BackHandler(enabled = currentScreen !is Screen.Welcome) {
        viewModel.navigateBack()
    }

    val showClientBottomBar = currentScreen in listOf(
        Screen.ClientHome,
        Screen.ClientSearch,
        Screen.ClientHistory,
        Screen.ClientProfile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showClientBottomBar) {
                ClientBottomNavBar(
                    currentScreen = currentScreen,
                    viewModel = viewModel
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                // Auth screens
                is Screen.Welcome -> WelcomeScreen(viewModel)
                is Screen.Login -> LoginScreen(viewModel)
                is Screen.Register -> RegisterScreen(viewModel)
                is Screen.RoleSelect -> RoleSelectScreen(viewModel)

                // Client flow
                is Screen.ClientHome -> ClientHomeScreen(viewModel)
                is Screen.ClientSearch -> ClientSearchScreen(viewModel)
                is Screen.ClientResults -> ClientResultsScreen(viewModel)
                is Screen.ClientMap -> ClientMapScreen(viewModel)
                is Screen.ClientProfessionalDetail -> ClientProfessionalDetailScreen(viewModel)
                is Screen.ClientRequestService -> ClientRequestServiceScreen(viewModel)
                is Screen.ClientConfirmRequest -> ClientConfirmRequestScreen(viewModel)
                is Screen.ClientRequestSent -> ClientRequestSentScreen(viewModel)
                is Screen.ClientTracking -> ClientTrackingScreen(viewModel)
                is Screen.ClientRating -> ClientRatingScreen(viewModel)
                is Screen.ClientHistory -> ClientHistoryScreen(viewModel)
                is Screen.ClientProfile -> ClientProfileScreen(viewModel)
                is Screen.ClientNotifications -> ClientNotificationsScreen(viewModel)

                // Worker flow
                is Screen.WorkerHome -> WorkerHomeScreen(viewModel)
                is Screen.WorkerRequests -> WorkerRequestsScreen(viewModel)
                is Screen.WorkerRequestDetail -> WorkerRequestDetailScreen(viewModel)
                is Screen.WorkerActiveService -> WorkerActiveServiceScreen(viewModel)
                is Screen.WorkerProfile -> WorkerProfileScreen(viewModel)
                is Screen.WorkerEditProfile -> WorkerEditProfileScreen(viewModel)
                is Screen.WorkerReviews -> WorkerReviewsScreen(viewModel)
            }
        }
    }
}
