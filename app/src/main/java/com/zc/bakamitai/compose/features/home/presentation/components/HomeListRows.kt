package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.compose.features.home.domain.model.Schedule

@Composable
fun HomeListRows(
    modifier: Modifier = Modifier,
    items: List<Schedule>
) {
    LazyRow(modifier = modifier) {
        items(items = items, key = {
            it.title
        }) { item ->
            EntryGridItem(item = item) { }
        }
    }
}
