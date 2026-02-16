package com.zc.bakamitai.compose.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * ExpandableContent:
 * - Shows [collapsedContent] always
 * - Shows [expandedContent] only when expanded
 * - Arrow toggles expand/collapse
 * - Keeps the same "state-hoisted + default state" approach as ExpandableText
 */
@Composable
fun ExpandableContent(
    modifier: Modifier = Modifier,
    arrowColor: Color = Color.Unspecified,
    arrowAlignment: Alignment.Vertical = Alignment.Top,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    collapsedContent: @Composable ColumnScope.() -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(expanded) }

    fun toggleExpanded() {
        isExpanded = !isExpanded
        onExpandedChange(isExpanded)
    }

    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = arrowAlignment,
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = true),
        ) {
            collapsedContent()
            if (isExpanded) {
                expandedContent()
            }
        }

        Spacer(Modifier.width(8.dp))

        val icon = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore
        val contentDesc = if (isExpanded) "Collapse" else "Expand"

        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = arrowColor,
            modifier = Modifier
                .size(24.dp)
                .clickable { toggleExpanded() }
        )
    }
}


@Composable
@Preview
fun ExpandableContentPreview() {
    ExpandableContent(
        collapsedContent = { Text(text = "Click to expand!") },
        expandedContent = { Text(text = "Expanded content goes here") },
        expanded = true,
    )
}
