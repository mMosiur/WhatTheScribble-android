package pl.mmorus.whatthescribble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.mmorus.whatthescribble.R
import pl.mmorus.whatthescribble.ui.theme.PlayerColors
import pl.mmorus.whatthescribble.ui.theme.WhatTheScribbleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuessScreen(
    viewModel: GameViewModel,
    onGuessSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastTurn = viewModel.getPreviousTurn()
    val lastDrawing = viewModel.getPreviousDrawing()
    val guesserName = viewModel.players.lastOrNull() ?: ""
    val guesserIndex = maxOf(0, viewModel.players.size - 1)
    val avatarColor = PlayerColors.getOrElse(guesserIndex % PlayerColors.size) { MaterialTheme.colorScheme.tertiary }
    var guess by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = true) {
        // Intercept back navigation during guess phase
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.guess_screen_title),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setFinalGuess(guess)
                            onGuessSubmitted()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = guess.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.see_summary_button),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize().imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Guesser spotlight banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.guessed_by_label, guesserName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = stringResource(R.string.guess_screen_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Drawing canvas preview framed properly
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DrawingCanvas(
                    lines = lastDrawing,
                    onDrawStart = {},
                    onDrawMove = {},
                    modifier = Modifier.fillMaxSize(),
                    interactive = false,
                    originalCanvasWidth = lastTurn?.canvasWidth ?: 0f,
                    originalCanvasHeight = lastTurn?.canvasHeight ?: 0f
                )
            }

            // Guess input field
            OutlinedTextField(
                value = guess,
                onValueChange = { guess = it },
                label = { Text(stringResource(R.string.guess_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (guess.isNotBlank()) {
                            viewModel.setFinalGuess(guess)
                            onGuessSubmitted()
                        }
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GuessScreenPreview() {
    WhatTheScribbleTheme {
        GuessScreen(
            viewModel = GameViewModel().apply {
                addPlayer("Gracz 1")
                addPlayer("Gracz 2")
                addPlayer("Gracz 3")
            },
            onGuessSubmitted = {}
        )
    }
}
