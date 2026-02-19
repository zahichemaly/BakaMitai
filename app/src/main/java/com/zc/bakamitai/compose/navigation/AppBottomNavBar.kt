package com.zc.bakamitai.compose.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.compose.common.UiText

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun AppBottomNavBar(
    currentRoute: NavKey,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    val topLevelDestinations = TopLevelDestination.entries

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        topLevelDestinations.forEach { item ->
            val isSelected = item.route == currentRoute
            val title = UiText.StringResource(item.titleId).asString()
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    onNavigate(item)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = title)
                },
                label = {
                    Text(
                        title,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}
