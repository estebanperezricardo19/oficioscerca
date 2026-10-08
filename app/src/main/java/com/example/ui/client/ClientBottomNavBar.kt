package com.example.ui.client

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.OficiosViewModel
import com.example.viewmodel.Screen

enum class ClientTab(val title: String, val targetScreen: Screen) {
    HOME("Inicio", Screen.ClientHome),
    SEARCH("Buscar", Screen.ClientSearch),
    SERVICES("Servicios", Screen.ClientHistory),
    PROFILE("Perfil", Screen.ClientProfile)
}

@Composable
fun ClientBottomNavBar(
    currentScreen: Screen,
    viewModel: OficiosViewModel
) {
    val currentTab = when (currentScreen) {
        Screen.ClientHome -> ClientTab.HOME
        Screen.ClientSearch, Screen.ClientResults, Screen.ClientMap -> ClientTab.SEARCH
        Screen.ClientHistory, Screen.ClientTracking -> ClientTab.SERVICES
        Screen.ClientProfile -> ClientTab.PROFILE
        else -> ClientTab.HOME
    }

    NavigationBar(
        containerColor = SurfaceWhite,
        contentColor = PrimaryNavy,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        ClientTab.entries.forEach { tab ->
            val selected = tab == currentTab

            val icon = when (tab) {
                ClientTab.HOME -> if (selected) Icons.Default.Home else Icons.Outlined.Home
                ClientTab.SEARCH -> if (selected) Icons.Default.Search else Icons.Outlined.Search
                ClientTab.SERVICES -> if (selected) Icons.Default.Build else Icons.Outlined.Build
                ClientTab.PROFILE -> if (selected) Icons.Default.Person else Icons.Outlined.Person
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        viewModel.navigateTo(tab.targetScreen)
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryNavy,
                    selectedTextColor = PrimaryNavy,
                    indicatorColor = AccentCyan.copy(alpha = 0.5f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
