package com.mmosiur.whatthescribble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmosiur.whatthescribble.R
import com.mmosiur.whatthescribble.ui.theme.PlayerColors
import com.mmosiur.whatthescribble.ui.theme.WhatTheScribbleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onTurnComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeLeft by viewModel.timeLeft
    val peekTimeLeft by viewModel.peekTimeLeft
    val isPeeking by viewModel.isPeeking
    val lines = viewModel.lines
    val players = viewModel.players
    val currentPlayerIndex by viewModel.currentPlayerIndex
    val currentPlayer = players.getOrNull(currentPlayerIndex) ?: ""
    val isTurnActive by viewModel.isTurnActive
    val currentWord by viewModel.currentWord
    val previousTurn = viewModel.getPreviousTurn()
    val previousDrawing = viewModel.getPreviousDrawing()
    val avatarColor = PlayerColors.getOrElse(currentPlayerIndex % PlayerColors.size) { MaterialTheme.colorScheme.primary }

    var selectedColor by remember { mutableStateOf(Color(0xFF212121)) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(8f) }
    var measuredCanvasWidth by remember { mutableFloatStateOf(0f) }
    var measuredCanvasHeight by remember { mutableFloatStateOf(0f) }

    val paletteColors = listOf(
        Color(0xFF212121), // Black
        Color(0xFFE53935), // Red
        Color(0xFF1E88E5), // Blue
        Color(0xFF43A047), // Green
        Color(0xFFFB8C00), // Orange
        Color(0xFF8E24AA)  // Purple
    )

    BackHandler(enabled = true) {
        // Intercept back navigation during active game turn
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
                            Text(
                                text = (currentPlayerIndex + 1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = currentPlayer,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (!isPeeking) {
                        Surface(
                            shape = CircleShape,
                            color = if (timeLeft <= 10) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.seconds_format, timeLeft),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
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
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Time's up banner if expired
                    AnimatedVisibility(
                        visible = timeLeft == 0 && !isPeeking,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.times_up_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Drawing Tools Row (Palette & Actions)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Color palette chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            paletteColors.forEach { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable(enabled = isTurnActive && !isPeeking) {
                                            selectedColor = color
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        // Undo and Clear buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { viewModel.undoLastLine() },
                                enabled = isTurnActive && !isPeeking && lines.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                                    contentDescription = stringResource(R.string.undo_button)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearCanvas() },
                                enabled = isTurnActive && !isPeeking && lines.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.clear_canvas_button)
                                )
                            }
                        }
                    }

                    // Bottom Finish/Next Button - ALWAYS available when not peeking so no deadlock can ever occur!
                    if (!isPeeking) {
                        Button(
                            onClick = {
                                val hasNext = viewModel.completeTurn(
                                    canvasWidth = measuredCanvasWidth,
                                    canvasHeight = measuredCanvasHeight
                                )
                                onTurnComplete(hasNext)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (timeLeft == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (timeLeft == 0) {
                                        stringResource(R.string.finish_turn_button)
                                    } else {
                                        stringResource(R.string.next_turn_button)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header prompt info banner
                if (!isPeeking) {
                    if (currentPlayerIndex == 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.your_secret_word, currentWord),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = stringResource(R.string.redraw_from_memory),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Interactive Drawing Canvas
                DrawingCanvas(
                    lines = lines,
                    onDrawStart = { point ->
                        if (isTurnActive && !isPeeking) {
                            viewModel.addLine(
                                Line(
                                    points = listOf(point),
                                    color = selectedColor,
                                    strokeWidth = selectedStrokeWidth
                                )
                            )
                        }
                    },
                    onDrawMove = { point ->
                        if (isTurnActive && !isPeeking) {
                            viewModel.updateLastLine(point)
                        }
                    },
                    onCanvasSizeMeasured = { w, h ->
                        measuredCanvasWidth = w
                        measuredCanvasHeight = h
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    interactive = isTurnActive && !isPeeking
                )
            }

            // Peek Overlay for players > 0
            if (isPeeking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                        .pointerInput(Unit) {}, // Consume touches
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.peek_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Peek Countdown Timer Badge
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = peekTimeLeft.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.draw_this_drawing),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            DrawingCanvas(
                                lines = previousDrawing,
                                onDrawStart = {},
                                onDrawMove = {},
                                modifier = Modifier.fillMaxSize(),
                                interactive = false,
                                originalCanvasWidth = previousTurn?.canvasWidth ?: 0f,
                                originalCanvasHeight = previousTurn?.canvasHeight ?: 0f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    WhatTheScribbleTheme {
        val vm = GameViewModel().apply {
            addPlayer("Player 1")
            addPlayer("Player 2")
            addPlayer("Player 3")
        }
        GameScreen(
            viewModel = vm,
            onTurnComplete = {}
        )
    }
}
