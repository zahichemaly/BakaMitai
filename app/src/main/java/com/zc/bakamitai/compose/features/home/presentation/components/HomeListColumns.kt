package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.compose.features.home.domain.model.Release

@Composable
fun HomeListColumns(
    modifier: Modifier = Modifier,
    items: List<Release>
) {
    LazyColumn(modifier = modifier) {
        items(items = items, key = {
            it.show
        }) { item ->
            EntryListItem(release = item) { }
            HorizontalDivider()
        }
    }
}
