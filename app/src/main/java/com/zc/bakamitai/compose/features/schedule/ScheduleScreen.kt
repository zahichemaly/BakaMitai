package com.zc.bakamitai.compose.features.schedule


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun ScheduleScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Schedule Screen",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}
