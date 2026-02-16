package com.zc.bakamitai.compose.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * ExpandableText:
 * - Collapses to [collapsedMaxLines]
 * - Shows expand/collapse arrow only when the text actually overflows
 * - State-hoisted + convenient default state
 */
@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    collapsedMaxLines: Int,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    arrowColor: Color = Color.Unspecified,
    arrowAlignment: Alignment.Vertical = Alignment.Bottom,
    expanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit) = { },
) {
    require(collapsedMaxLines > 0) { "collapsedMaxLines must be > 0" }

    var isExpanded by rememberSaveable { mutableStateOf(expanded) }

    fun setExpanded() {
        isExpanded = !isExpanded
        onExpandedChange.invoke(isExpanded)
    }

    var hasOverflow by remember(text, collapsedMaxLines) { mutableStateOf(false) }

    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = arrowAlignment
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            style = textStyle,
            modifier = Modifier.weight(1f, fill = true),
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layoutResult ->
                hasOverflow = !isExpanded && layoutResult.hasVisualOverflow
            }
        )

        if (hasOverflow || isExpanded) {
            Spacer(Modifier.width(8.dp))

            val icon = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore
            val contentDesc = if (isExpanded) "Collapse" else "Expand"

            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = arrowColor,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { setExpanded() }
            )
        }
    }
}


@Composable
@Preview
private fun ExpandableTextTest() {
    ExpandableText(
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam tincidunt bibendum purus, sed congue sem aliquet non. Nullam tincidunt pretium gravida. Maecenas lacinia est justo, eget fringilla lectus interdum nec. Suspendisse molestie luctus elementum. Fusce id ante nec orci lobortis suscipit. Sed gravida, ligula eget sagittis sodales, risus lacus feugiat tortor, pulvinar euismod ante dolor nec mauris. Vivamus in eleifend ex. Duis eu odio sit amet purus finibus auctor. Ut a ultricies nisi. Donec elementum, nulla non finibus tincidunt, lorem nibh sollicitudin nisi, vel rhoncus metus tellus id leo. Morbi molestie nunc felis, pharetra vulputate elit feugiat et. Cras non diam maximus, pharetra nunc sit amet, tincidunt mauris. Sed at elementum sem, eget faucibus lorem. Etiam lobortis suscipit eros sed sodales. Donec luctus dictum augue, vel rhoncus purus tempus volutpat. Nunc et neque lectus.",
        collapsedMaxLines = 5,
        color = Color.White,
        arrowColor = Color.White,
        expanded = false,
    )
}
