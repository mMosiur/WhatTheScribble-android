package pl.mmorus.whatthescribble.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.mmorus.whatthescribble.R

data class RuleItemData(
    val step: Int,
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int
)

@Composable
fun RulesContent(modifier: Modifier = Modifier) {
    val rules = listOf(
        RuleItemData(1, Icons.Rounded.Edit, R.string.rule_1_title, R.string.rule_1_desc),
        RuleItemData(2, Icons.Rounded.Visibility, R.string.rule_2_title, R.string.rule_2_desc),
        RuleItemData(3, Icons.Rounded.Brush, R.string.rule_3_title, R.string.rule_3_desc),
        RuleItemData(4, Icons.Rounded.Psychology, R.string.rule_4_title, R.string.rule_4_desc),
        RuleItemData(5, Icons.Rounded.AutoAwesome, R.string.rule_5_title, R.string.rule_5_desc)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rules.forEach { rule ->
            RuleRow(rule = rule)
        }
    }
}

@Composable
private fun RuleRow(rule: RuleItemData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rule.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(rule.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(rule.descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RulesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.rules_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 420.dp)) {
                RulesContent()
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.rules_close))
            }
        }
    )
}
