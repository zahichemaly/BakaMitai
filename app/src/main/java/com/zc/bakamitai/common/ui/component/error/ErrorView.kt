package com.zc.bakamitai.common.ui.component.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** Full-area error placeholder: the message, plus a retry button when [ErrorUIModel.isRetryable]. */
@Composable
fun ErrorView(model: ErrorUIModel, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(BakaMitaiTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = model.message.asString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (model.isRetryable) {
            Button(onClick = onRetry) {
                Text(UiText.StringResource(StringResource.action_retry).asString())
            }
        }
    }
}

private class ErrorUIModelProvider : PreviewParameterProvider<ErrorUIModel> {
    override val values = sequenceOf(
        ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true),
        ErrorUIModel(UiText.StringResource(StringResource.error_not_found), isRetryable = false),
    )
}

@AdaptivePreviews
@Composable
private fun ErrorViewPreview(@PreviewParameter(ErrorUIModelProvider::class) model: ErrorUIModel) {
    BakaMitaiTheme {
        Surface {
            ErrorView(model = model, onRetry = {})
        }
    }
}
