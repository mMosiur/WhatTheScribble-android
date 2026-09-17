package pl.mmorus.whatthescribble.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.mmorus.whatthescribble.R
import pl.mmorus.whatthescribble.ui.theme.PlayerColors
import pl.mmorus.whatthescribble.ui.theme.WhatTheScribbleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit = onRestart
) {
    val turns = viewModel.turns
    val originalWord by viewModel.currentWord
    val finalGuess by viewModel.finalGuess
    val guesserName = viewModel.players.lastOrNull() ?: ""
    val isMatch = originalWord.trim().equals(finalGuess.trim(), ignoreCase = true)

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Story (krok po kroku), 1: Gallery (wszystkie)
    val totalStorySteps = turns.size + 1
    var currentStoryStep by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = true) {
        onMainMenu()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.summary_screen_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMainMenu) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = stringResource(R.string.main_menu_button)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onMainMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.main_menu_button))
                    }

                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Replay, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.play_again_button))
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Grand Reveal & Verdict Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMatch) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Match verdict pill
                    Surface(
                        shape = CircleShape,
                        color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isMatch) Icons.Rounded.Celebration else Icons.Rounded.PhoneInTalk,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(if (isMatch) R.string.verdict_success else R.string.verdict_close),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Side-by-side or stacked word comparison
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.original_word_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = originalWord,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.final_guess_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = finalGuess.ifBlank { "—" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.guessed_by_label, guesserName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Mode Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Slideshow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.recap_tab_story))
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.recap_tab_gallery))
                        }
                    }
                )
            }

            // Content based on tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (selectedTab == 0) {
                    // Storyline Presentation Mode (Step-by-step recap)
                    StorylineRecapView(
                        turns = turns,
                        originalWord = originalWord,
                        finalGuess = finalGuess,
                        guesserName = guesserName,
                        isMatch = isMatch,
                        currentStep = currentStoryStep,
                        totalSteps = totalStorySteps,
                        onPrevStep = { if (currentStoryStep > 0) currentStoryStep -= 1 },
                        onNextStep = { if (currentStoryStep < totalStorySteps - 1) currentStoryStep += 1 }
                    )
                } else {
                    // All Drawings Gallery Mode (Connected Evolution Chain)
                    GalleryRecapView(
                        turns = turns,
                        originalWord = originalWord,
                        finalGuess = finalGuess,
                        guesserName = guesserName,
                        isMatch = isMatch
                    )
                }
            }
        }
    }
}

@Composable
fun StorylineRecapView(
    turns: List<GameTurn>,
    originalWord: String,
    finalGuess: String,
    guesserName: String,
    isMatch: Boolean,
    currentStep: Int,
    totalSteps: Int,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Indicator Pill
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.step_indicator, currentStep + 1, totalSteps),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }

        // Active Step Presentation Card
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = "storyStepAnimation"
        ) { stepIndex ->
            if (stepIndex < turns.size) {
                val turn = turns[stepIndex]
                val avatarColor = PlayerColors.getOrElse(stepIndex % PlayerColors.size) { MaterialTheme.colorScheme.primary }

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(avatarColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (stepIndex + 1).toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (stepIndex == 0) {
                                    stringResource(R.string.step_first_drawer, turn.playerName, originalWord)
                                } else {
                                    stringResource(R.string.step_redrawer, turn.playerName)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Drawing Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        ) {
                            DrawingCanvas(
                                lines = turn.drawing,
                                onDrawStart = {},
                                onDrawMove = {},
                                modifier = Modifier.fillMaxSize(),
                                interactive = false,
                                originalCanvasWidth = turn.canvasWidth,
                                originalCanvasHeight = turn.canvasHeight
                            )
                        }
                    }
                }
            } else {
                // Final Step: Guesser Card
                val guesserAvatarColor = PlayerColors.getOrElse((totalSteps - 1) % PlayerColors.size) { MaterialTheme.colorScheme.tertiary }

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(guesserAvatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.step_guesser, guesserName),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "\"$finalGuess\"",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isMatch) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isMatch) {
                                        "Gratulacje! Łańcuch nie został zerwany!"
                                    } else {
                                        "Oryginalnym hasłem było: $originalWord"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stepper Navigation Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(
                onClick = onPrevStep,
                enabled = currentStep > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.prev_step))
            }

            // Dots indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(totalSteps) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentStep) 10.dp else 6.dp)
                            .background(
                                color = if (idx == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            FilledTonalButton(
                onClick = onNextStep,
                enabled = currentStep < totalSteps - 1,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.next_step))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun GalleryRecapView(
    turns: List<GameTurn>,
    originalWord: String,
    finalGuess: String,
    guesserName: String,
    isMatch: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(turns) { index, turn ->
            val avatarColor = PlayerColors.getOrElse(index % PlayerColors.size) { MaterialTheme.colorScheme.primary }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Column {
                            Text(
                                text = turn.playerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (turn.word != null) {
                                Text(
                                    text = stringResource(R.string.draw_this_word, turn.word),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.redraw_from_memory),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        DrawingCanvas(
                            lines = turn.drawing,
                            onDrawStart = {},
                            onDrawMove = {},
                            modifier = Modifier.fillMaxSize(),
                            interactive = false,
                            originalCanvasWidth = turn.canvasWidth,
                            originalCanvasHeight = turn.canvasHeight
                        )
                    }
                }
            }

            // Downward arrow connector between turns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.South,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Final Guess Item Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMatch) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.step_guesser, guesserName),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\"$finalGuess\"",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    WhatTheScribbleTheme {
        SummaryScreen(
            viewModel = GameViewModel().apply {
                addPlayer("Gracz 1")
                addPlayer("Gracz 2")
                addPlayer("Gracz 3")
            },
            onRestart = {}
        )
    }
}
