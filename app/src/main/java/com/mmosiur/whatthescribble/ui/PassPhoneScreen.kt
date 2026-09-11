package com.mmosiur.whatthescribble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mmosiur.whatthescribble.R
import com.mmosiur.whatthescribble.ui.theme.PlayerColors
import com.mmosiur.whatthescribble.ui.theme.WhatTheScribbleTheme

@Composable
fun PassPhoneScreen(
    viewModel: GameViewModel,
    onReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players = viewModel.players
    val currentPlayerIndex by viewModel.currentPlayerIndex
    val currentPlayer = players.getOrNull(currentPlayerIndex) ?: ""
    val avatarColor = PlayerColors.getOrElse(currentPlayerIndex % PlayerColors.size) { MaterialTheme.colorScheme.primary }
    val currentWord by viewModel.currentWord

    var firstPlayerWord by rememberSaveable { mutableStateOf(currentWord) }

    LaunchedEffect(currentWord) {
        if (firstPlayerWord.isBlank()) {
            firstPlayerWord = currentWord
        }
    }

    val roleDescription = when (currentPlayerIndex) {
        0 -> stringResource(R.string.rule_1_desc)
        players.size - 1 if players.size >= 3 -> stringResource(R.string.rule_4_desc)
        else -> stringResource(R.string.rule_2_desc)
    }

    BackHandler(enabled = true) {
        // Intercept back navigation during handoff
    }

    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top icon & title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhonelinkRing,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = stringResource(R.string.pass_phone_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = stringResource(R.string.pass_phone_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Central Spotlight Player Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Big Player Avatar
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(avatarColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentPlayerIndex + 1).toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Text(
                        text = currentPlayer,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    if (currentPlayerIndex == 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Draw,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.enter_your_own_word_prompt),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                OutlinedTextField(
                                    value = firstPlayerWord,
                                    onValueChange = { input ->
                                        val sanitized = GameViewModel.sanitizeWord(input)
                                        firstPlayerWord = sanitized
                                        viewModel.setCurrentWord(sanitized)
                                    },
                                    label = { Text(stringResource(R.string.custom_word_field_label)) },
                                    placeholder = { Text(stringResource(R.string.custom_word_hint)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                val rolled = viewModel.rollRandomWord()
                                                firstPlayerWord = rolled
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Casino,
                                                contentDescription = stringResource(R.string.reroll_word_button),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )

                                FilledTonalButton(
                                    onClick = {
                                        val rolled = viewModel.rollRandomWord()
                                        firstPlayerWord = rolled
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Casino,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.reroll_word_button),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = roleDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Bottom section: warning & ready button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.no_peeking_warning),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Button(
                    onClick = {
                        if (currentPlayerIndex == 0) {
                            if (firstPlayerWord.isNotBlank()) {
                                viewModel.setCurrentWord(firstPlayerWord)
                            } else {
                                viewModel.rollRandomWord()
                            }
                        }
                        onReady()
                    },
                    enabled = currentPlayerIndex != 0 || firstPlayerWord.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ready_button),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PassPhoneScreenPreview() {
    WhatTheScribbleTheme {
        PassPhoneScreen(
            viewModel = GameViewModel().apply {
                addPlayer("Gracz 1")
            },
            onReady = {}
        )
    }
}
