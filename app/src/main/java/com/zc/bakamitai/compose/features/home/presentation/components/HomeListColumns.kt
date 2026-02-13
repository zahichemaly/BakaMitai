package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.ReleaseUiModel

@Composable
fun HomeListColumns(
    modifier: Modifier = Modifier,
    items: List<ReleaseUiModel>
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            EntryListItem(item = item) { }
            HorizontalDivider()
        }
    }
}
