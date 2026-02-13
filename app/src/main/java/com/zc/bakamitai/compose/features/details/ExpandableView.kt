package com.zc.bakamitai.compose.features.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExpandableView(
    modifier: Modifier = Modifier,
    collapsedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    expanded: Boolean = false,
    onClick: () -> Unit = {}
) {

    var expanded by remember { mutableStateOf(expanded) }

    Column(
        modifier = modifier
            .clickable {
                expanded = !expanded
                onClick()
            }) {
        collapsedContent()
        if (expanded) {
            expandedContent()
        }
    }
}


@Composable
@Preview
fun ExpandableViewPreview() {
    ExpandableView(
        collapsedContent = { Text(text = "Click to expand!") },
        expandedContent = { Text(text = "Expanded content goes here") },
        expanded = true,
    )
}
