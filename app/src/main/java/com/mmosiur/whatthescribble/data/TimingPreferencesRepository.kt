package com.mmosiur.whatthescribble.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

interface TimingPreferencesRepository {
    fun getDrawDuration(defaultDuration: Int = 60): Int
    fun getPeekDuration(defaultDuration: Int = 10): Int
    fun saveDrawDuration(duration: Int)
    fun savePeekDuration(duration: Int)
}

class SharedPreferencesTimingPreferencesRepository(
    private val sharedPreferences: SharedPreferences
) : TimingPreferencesRepository {

    override fun getDrawDuration(defaultDuration: Int): Int {
        val duration = sharedPreferences.getInt(KEY_DRAW_DURATION, defaultDuration)
        return duration.coerceIn(MIN_DRAW_DURATION, MAX_DRAW_DURATION)
    }

    override fun getPeekDuration(defaultDuration: Int): Int {
        val duration = sharedPreferences.getInt(KEY_PEEK_DURATION, defaultDuration)
        return duration.coerceIn(MIN_PEEK_DURATION, MAX_PEEK_DURATION)
    }

    override fun saveDrawDuration(duration: Int) {
        sharedPreferences.edit {
            putInt(KEY_DRAW_DURATION, duration.coerceIn(MIN_DRAW_DURATION, MAX_DRAW_DURATION))
        }
    }

    override fun savePeekDuration(duration: Int) {
        sharedPreferences.edit {
            putInt(KEY_PEEK_DURATION, duration.coerceIn(MIN_PEEK_DURATION, MAX_PEEK_DURATION))
        }
    }

    companion object {
        const val PREFS_NAME = "what_the_scribble_timing_prefs"
        const val KEY_DRAW_DURATION = "pref_draw_duration"
        const val KEY_PEEK_DURATION = "pref_peek_duration"

        const val MIN_DRAW_DURATION = 15
        const val MAX_DRAW_DURATION = 300
        const val MIN_PEEK_DURATION = 3
        const val MAX_PEEK_DURATION = 60

        fun create(context: Context): SharedPreferencesTimingPreferencesRepository {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return SharedPreferencesTimingPreferencesRepository(prefs)
        }
    }
}
