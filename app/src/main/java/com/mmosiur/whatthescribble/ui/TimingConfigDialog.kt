package com.mmosiur.whatthescribble.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mmosiur.whatthescribble.R

@Composable
fun TimingSettingsContent(
    drawDuration: Int,
    peekDuration: Int,
    onDrawDurationChange: (Int) -> Unit,
    onPeekDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawOptions = listOf(30, 45, 60, 90, 120)
    val peekOptions = listOf(5, 10, 15, 20)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Draw Duration
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.draw_time_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.seconds_format, drawDuration),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                drawOptions.forEach { seconds ->
                    FilterChip(
                        selected = drawDuration == seconds,
                        onClick = { onDrawDurationChange(seconds) },
                        label = { Text(stringResource(R.string.seconds_format, seconds)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Peek Duration
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.peek_time_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.seconds_format, peekDuration),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                peekOptions.forEach { seconds ->
                    FilterChip(
                        selected = peekDuration == seconds,
                        onClick = { onPeekDurationChange(seconds) },
                        label = { Text(stringResource(R.string.seconds_format, seconds)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimingConfigDialog(
    drawDuration: Int,
    peekDuration: Int,
    onDrawDurationChange: (Int) -> Unit,
    onPeekDurationChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.timing_settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            TimingSettingsContent(
                drawDuration = drawDuration,
                peekDuration = peekDuration,
                onDrawDurationChange = onDrawDurationChange,
                onPeekDurationChange = onPeekDurationChange
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.save_timing_button))
            }
        }
    )
}
