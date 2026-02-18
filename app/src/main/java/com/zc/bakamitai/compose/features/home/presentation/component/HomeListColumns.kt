package com.zc.bakamitai.compose.features.home.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.compose.features.home.presentation.ReleaseUiModel

@Composable
fun HomeListColumns(
    modifier: Modifier = Modifier,
    items: List<ReleaseUiModel>,
    onItemClick: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            EntryListItem(item = item, onClick = onItemClick)
            HorizontalDivider()
        }
    }
}
