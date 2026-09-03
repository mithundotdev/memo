package dev.fleetworks.memo

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fleetworks.memo.ui.chat.ChatScreen
import dev.fleetworks.memo.ui.notes.NoteDetailScreen
import dev.fleetworks.memo.ui.notes.NotesListScreen
import dev.fleetworks.memo.ui.settings.SettingsScreen

@Composable
fun MemoNav(container: AppContainer) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "notes"
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = route.startsWith("notes") || route.startsWith("detail"),
                    onClick = { nav.navigate("notes") { launchSingleTop = true } },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Notes") }
                )
                NavigationBarItem(
                    selected = route == "chat",
                    onClick = { nav.navigate("chat") { launchSingleTop = true } },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = route == "settings",
                    onClick = { nav.navigate("settings") { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { inner ->
        NavHost(nav, startDestination = "notes", modifier = Modifier.padding(inner)) {
            composable("notes") {
                NotesListScreen(
                    repo = container.repo,
                    onOpen = { id -> nav.navigate("detail/$id") },
                    onNew = { nav.navigate("detail/new") }
                )
            }
            composable(
                "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                val id = it.arguments?.getString("id").orEmpty()
                NoteDetailScreen(repo = container.repo, noteId = id, onBack = { nav.popBackStack() })
            }
            composable("chat") {
                ChatScreen(container = container)
            }
            composable("settings") {
                SettingsScreen(profiles = container.profiles)
            }
        }
    }
}
