package com.zc.bakamitai.compose.features.schedule


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun ScheduleScreen() {
    val days = listOf(
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat",
        "Sun"
    )
    val tabs = mutableListOf<TabContent>()
    days.forEach {
        tabs.add(TabContent(title = it, content = {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                text = "Content for $it",
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }))
    }
    TabLayoutView(tabs = tabs, startPosition = 0)
}


@Preview
@Composable
private fun ScheduleScreenPreview() {
    ScheduleScreen()
}
