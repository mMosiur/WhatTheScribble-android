package pl.mmorus.whatthescribble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import pl.mmorus.whatthescribble.navigation.AppNavKey
import pl.mmorus.whatthescribble.ui.*
import pl.mmorus.whatthescribble.ui.theme.WhatTheScribbleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatTheScribbleTheme {
                val backStack = rememberNavBackStack(AppNavKey.MainMenu)
                val gameViewModel: GameViewModel = viewModel(
                    factory = GameViewModel.Factory(applicationContext)
                )

                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        } else {
                            finish()
                        }
                    },
                    entryProvider = { key ->
                        when (key) {
                            is AppNavKey.MainMenu -> NavEntry(key) { _ ->
                                MainMenuScreen(
                                    viewModel = gameViewModel,
                                    onStartGame = {
                                        backStack.add(AppNavKey.PlayerSetup)
                                    }
                                )
                            }
                            is AppNavKey.PlayerSetup -> NavEntry(key) { _ ->
                                PlayerSetupScreen(
                                    viewModel = gameViewModel,
                                    onStartGame = {
                                        gameViewModel.startGame()
                                        backStack.add(AppNavKey.PassPhone)
                                    },
                                    onBack = {
                                        backStack.removeLastOrNull()
                                    }
                                )
                            }
                            is AppNavKey.PassPhone -> NavEntry(key) { _ ->
                                PassPhoneScreen(
                                    viewModel = gameViewModel,
                                    onReady = {
                                        if (gameViewModel.currentPlayerIndex.value == gameViewModel.players.size - 1) {
                                            backStack.add(AppNavKey.GuessPhase)
                                        } else {
                                            gameViewModel.startTurn()
                                            backStack.add(AppNavKey.Drawing)
                                        }
                                    }
                                )
                            }
                            is AppNavKey.Drawing -> NavEntry(key) { _ ->
                                GameScreen(
                                    viewModel = gameViewModel,
                                    onTurnComplete = { _ ->
                                        backStack.add(AppNavKey.PassPhone)
                                    }
                                )
                            }
                            is AppNavKey.GuessPhase -> NavEntry(key) { _ ->
                                GuessScreen(
                                    viewModel = gameViewModel,
                                    onGuessSubmitted = {
                                        backStack.add(AppNavKey.Summary)
                                    }
                                )
                            }
                            is AppNavKey.Summary -> NavEntry(key) { _ ->
                                SummaryScreen(
                                    viewModel = gameViewModel,
                                    onRestart = {
                                        gameViewModel.restartGame()
                                        backStack.clear()
                                        backStack.add(AppNavKey.MainMenu)
                                        backStack.add(AppNavKey.PlayerSetup)
                                        backStack.add(AppNavKey.PassPhone)
                                    },
                                    onMainMenu = {
                                        backStack.clear()
                                        backStack.add(AppNavKey.MainMenu)
                                    }
                                )
                            }
                            is AppNavKey.Rules -> NavEntry(key) { _ ->
                                RulesDialog(
                                    onDismiss = {
                                        backStack.removeLastOrNull()
                                    }
                                )
                            }
                            else -> error("Unknown navigation key: $key")
                        }
                    }
                )
            }
        }
    }
}
