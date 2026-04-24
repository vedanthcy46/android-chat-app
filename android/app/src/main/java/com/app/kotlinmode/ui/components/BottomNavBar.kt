package com.app.kotlinmode.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.kotlinmode.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Feed : BottomNavItem("feed", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Reels : BottomNavItem("reels", "Reels", Icons.Filled.Movie, Icons.Outlined.Movie)
    object Search : BottomNavItem("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Create : BottomNavItem("create_post", "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircle)
    object Chat : BottomNavItem("chat_list", "Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    currentRoute: String?,
    unreadCount: Int = 0,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Reels,
        BottomNavItem.Search,
        BottomNavItem.Create,
        BottomNavItem.Chat,
        BottomNavItem.Profile
    )

    NavigationBar(containerColor = DarkSurface) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item is BottomNavItem.Chat && unreadCount > 0) {
                                Badge(containerColor = BrandPrimary) {
                                    Text(if (unreadCount > 99) "99+" else "$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandPrimary,
                    selectedTextColor = BrandPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = DarkCard
                )
            )
        }
    }
}
