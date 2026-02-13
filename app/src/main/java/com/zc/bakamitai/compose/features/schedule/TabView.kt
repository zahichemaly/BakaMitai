package com.zc.bakamitai.compose.features.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TabLayoutView(
    tabs: List<TabContent>,
    startPosition: Int = 0
) {
    val pagerState = rememberPagerState(initialPage = startPosition) { tabs.size }
    val scope = rememberCoroutineScope()

    Column {
        // Show scrollable tabs
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = tab.title)
                    }
                )
            }
        }
        // Show swipeable content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            tabs[page].content()
        }
    }
}

data class TabContent(
    val title: String,
    val content: @Composable () -> Unit
)

@Composable
@Preview
fun TabLayoutViewPreview() {
    val tabs = listOf(
        TabContent("Tab 1") {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                text = "Content for tab 1",
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        },
        TabContent("Tab 2") { Text("Content for tab 2") },
        TabContent("Tab 3") { Text("Content for tab 3") },
        TabContent("Tab 4") { Text("Content for tab 4") },
        TabContent("Tab 5") { Text("Content for tab 5") }
    )

    TabLayoutView(tabs = tabs, startPosition = 0)
}
