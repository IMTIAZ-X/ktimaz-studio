package com.ktimazstudio.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.ktimazstudio.agent.AgentActivity
import com.ktimazstudio.enums.Screen

@Composable
fun BottomNav(items: List<Screen>, currentRoute: String?, navController: NavController) {
    val context = LocalContext.current
    BottomNavigation {
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = screen.route == currentRoute,
                onClick = {
                    // If this is the Agent item, open AgentActivity
                    if (screen == Screen.Agent) {
                        context.startActivity(AgentActivity.createIntent(context))
                    } else {
                        // Navigate inside your navController for other screens
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}