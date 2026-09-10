package com.mmosiur.whatthescribble.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppNavKey : NavKey {
    @Serializable
    data object MainMenu : AppNavKey

    @Serializable
    data object PlayerSetup : AppNavKey

    @Serializable
    data object PassPhone : AppNavKey

    @Serializable
    data object Drawing : AppNavKey

    @Serializable
    data object GuessPhase : AppNavKey

    @Serializable
    data object Summary : AppNavKey

    @Serializable
    data object Rules : AppNavKey
}
