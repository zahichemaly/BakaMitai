package com.zc.bakamitai.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zc.bakamitai.R

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    onSearchClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(title, maxLines = 1)
        },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options"
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {

                // item: action_search
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onSearchClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.refresh)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onRefreshClick()
                    }
                )

                // item: action_settings
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.title_settings)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onSettingsClick()
                    }
                )
            }
        },
    )
}

@Preview
@Composable
fun AppBarPreview() {
    AppBar(title = "My Screen Title")
}
