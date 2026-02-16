package com.zc.bakamitai.compose

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
import androidx.compose.ui.tooling.preview.Preview
import com.zc.bakamitai.compose.navigation.NavigationItem
import com.zc.bakamitai.compose.navigation.navigationItems

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun NavBar(
    onNavigate: (NavigationItem) -> Unit,
) {
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    onNavigate(item)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title.asString())
                },
                label = {
                    Text(
                        item.title.asString(),
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

@Preview
@Composable
private fun NavBarPreview() {
    NavBar(onNavigate = {})
}
