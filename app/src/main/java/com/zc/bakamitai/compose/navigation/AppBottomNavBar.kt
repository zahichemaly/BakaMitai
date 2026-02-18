package com.zc.bakamitai.compose.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.zc.bakamitai.compose.common.UiText

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun AppBottomNavBar(
    onNavigate: (TopLevelDestination) -> Unit,
) {
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

    val topLevelDestinations = TopLevelDestination.entries

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        topLevelDestinations.forEachIndexed { index, item ->
            val title = UiText.StringResource(item.titleId).asString()
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    onNavigate(item)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = title)
                },
                label = {
                    Text(
                        title,
                        color = if (index == selectedNavigationIndex.intValue)
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
