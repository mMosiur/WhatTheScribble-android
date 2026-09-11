package com.mmosiur.whatthescribble.ui

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mmosiur.whatthescribble.data.SharedPreferencesTimingPreferencesRepository
import com.mmosiur.whatthescribble.data.TimingPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class DrawnPoint(
    val offset: Offset,
    val pressure: Float = 0.5f
)

data class Line(
    val points: List<DrawnPoint>,
    val color: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val isEraser: Boolean = false
)

data class GameTurn(
    val playerName: String,
    val word: String? = null,
    val drawing: List<Line> = emptyList(),
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f
)

enum class WordMode {
    RANDOM,
    CUSTOM
}

class GameViewModel(
    private val preferencesRepository: TimingPreferencesRepository? = null
) : ViewModel() {
    private val polishWords = listOf(
        "Kot", "Pies", "Dom", "Drzewo", "Słońce", "Auto", "Kwiatek", "Ryba", "Ptak", "Jabłko",
        "Banan", "Rower", "Książka", "Telefon", "Komputer", "Okno", "Drzwi", "Krzesło", "Stół", "Łóżko",
        "Piknik", "Zamek", "Statek", "Samolot", "Pizza", "Lody", "Gitara", "Grzyb", "Smok", "Rakieta",
        "Wulkan", "Kaktus", "Panda", "Pingwin", "Dinozaur", "Duch", "Robot", "Korona", "Parasol", "Bałwan",
        "Balon", "Okulary", "Tort", "Serce", "Pająk", "Ślimak", "Mysz", "Zając", "Żaba", "Motyl",
        "Gwiazda", "Księżyc", "Aparat", "Zegar", "Kapelusz", "But", "Nożyczki", "Most", "Lampa", "Wyspa"
    )

    private val _players = mutableStateListOf<String>()
    val players: List<String> get() = _players

    private val _turns = mutableStateListOf<GameTurn>()
    val turns: List<GameTurn> = _turns

    // Configurable timing in seconds
    private val _drawDuration = mutableIntStateOf(
        preferencesRepository?.getDrawDuration(DEFAULT_DRAW_DURATION) ?: DEFAULT_DRAW_DURATION
    )
    val drawDuration: State<Int> = _drawDuration

    private val _peekDuration = mutableIntStateOf(
        preferencesRepository?.getPeekDuration(DEFAULT_PEEK_DURATION) ?: DEFAULT_PEEK_DURATION
    )
    val peekDuration: State<Int> = _peekDuration

    private val _timeLeft = mutableIntStateOf(_drawDuration.intValue)
    val timeLeft: State<Int> = _timeLeft

    private val _peekTimeLeft = mutableIntStateOf(_peekDuration.intValue)
    val peekTimeLeft: State<Int> = _peekTimeLeft

    private val _isPeeking = mutableStateOf(false)
    val isPeeking: State<Boolean> = _isPeeking

    private val _isTurnActive = mutableStateOf(false)
    val isTurnActive: State<Boolean> = _isTurnActive

    private val _lines = mutableStateListOf<Line>()
    val lines: List<Line> = _lines

    private val _currentPlayerIndex = mutableIntStateOf(0)
    val currentPlayerIndex: State<Int> = _currentPlayerIndex

    private val _currentWord = mutableStateOf("")
    val currentWord: State<String> = _currentWord

    private val _wordMode = mutableStateOf(WordMode.RANDOM)
    val wordMode: State<WordMode> = _wordMode

    private val _customWordInput = mutableStateOf("")
    val customWordInput: State<String> = _customWordInput

    private val _finalGuess = mutableStateOf("")
    val finalGuess: State<String> = _finalGuess

    private var timerJob: Job? = null

    companion object {
        const val DEFAULT_DRAW_DURATION = 60
        const val DEFAULT_PEEK_DURATION = 10
        const val MAX_WORD_LENGTH = 30
        const val DEFAULT_ERASER_STROKE_WIDTH = 80f

        /**
         * Sanitizes word input to allow only letters, digits, spaces, hyphens, and apostrophes.
         * Restricts emojis, surrogate pairs, symbols, and caps length to MAX_WORD_LENGTH.
         */
        fun sanitizeWord(input: String): String {
            return input.filter { char ->
                char.isLetter() || char.isDigit() || char == ' ' || char == '-' || char == '\''
            }.take(MAX_WORD_LENGTH)
        }
    }

    fun setWordMode(mode: WordMode) {
        _wordMode.value = mode
    }

    fun setCustomWordInput(word: String) {
        _customWordInput.value = sanitizeWord(word)
    }

    fun rollRandomWord(): String {
        val word = polishWords.random()
        _currentWord.value = word
        return word
    }

    fun setCurrentWord(word: String) {
        val sanitized = sanitizeWord(word).trim()
        if (sanitized.isNotBlank()) {
            _currentWord.value = sanitized
        }
    }

    fun setDrawDuration(seconds: Int) {
        val coerced = seconds.coerceIn(
            SharedPreferencesTimingPreferencesRepository.MIN_DRAW_DURATION,
            SharedPreferencesTimingPreferencesRepository.MAX_DRAW_DURATION
        )
        _drawDuration.intValue = coerced
        if (!_isTurnActive.value && !_isPeeking.value) {
            _timeLeft.intValue = coerced
        }
        preferencesRepository?.saveDrawDuration(coerced)
    }

    fun setPeekDuration(seconds: Int) {
        val coerced = seconds.coerceIn(
            SharedPreferencesTimingPreferencesRepository.MIN_PEEK_DURATION,
            SharedPreferencesTimingPreferencesRepository.MAX_PEEK_DURATION
        )
        _peekDuration.intValue = coerced
        if (!_isTurnActive.value && !_isPeeking.value) {
            _peekTimeLeft.intValue = coerced
        }
        preferencesRepository?.savePeekDuration(coerced)
    }

    fun addPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !_players.contains(trimmed)) {
            _players.add(trimmed)
        }
    }

    fun removePlayer(name: String) {
        _players.remove(name)
    }

    fun startGame(customWord: String? = null) {
        _turns.clear()
        _finalGuess.value = ""
        _currentPlayerIndex.intValue = 0
        val wordToUse = customWord?.let { sanitizeWord(it).trim().ifBlank { null } }
            ?: if (_wordMode.value == WordMode.CUSTOM && _customWordInput.value.isNotBlank()) {
                sanitizeWord(_customWordInput.value).trim()
            } else {
                polishWords.random()
            }
        _currentWord.value = wordToUse
        _lines.clear()
        _timeLeft.intValue = _drawDuration.intValue
        _peekTimeLeft.intValue = _peekDuration.intValue
        _isPeeking.value = false
        _isTurnActive.value = false
        timerJob?.cancel()
    }

    fun restartGame() {
        startGame()
    }

    fun startTurn() {
        _lines.clear()
        _timeLeft.intValue = _drawDuration.intValue
        timerJob?.cancel()

        if (_currentPlayerIndex.intValue == 0) {
            // First player draws the prompt word immediately
            _peekTimeLeft.intValue = 0
            _isPeeking.value = false
            _isTurnActive.value = true

            timerJob = viewModelScope.launch {
                while (_timeLeft.intValue > 0) {
                    delay(1000.milliseconds)
                    _timeLeft.intValue -= 1
                }
                _isTurnActive.value = false
            }
        } else {
            // Subsequent players have a peek window before redrawing from memory
            _peekTimeLeft.intValue = _peekDuration.intValue
            _isPeeking.value = true
            _isTurnActive.value = false

            timerJob = viewModelScope.launch {
                while (_peekTimeLeft.intValue > 0) {
                    delay(1000.milliseconds)
                    _peekTimeLeft.intValue -= 1
                }
                _isPeeking.value = false
                _isTurnActive.value = true

                while (_timeLeft.intValue > 0) {
                    delay(1000.milliseconds)
                    _timeLeft.intValue -= 1
                }
                _isTurnActive.value = false
            }
        }
    }

    fun addLine(line: Line) {
        _lines.add(line)
    }

    fun updateLastLine(point: DrawnPoint) {
        if (_lines.isNotEmpty()) {
            val lastLine = _lines.last()
            val newPoints = lastLine.points + point
            _lines[_lines.lastIndex] = lastLine.copy(points = newPoints)
        }
    }

    fun undoLastLine() {
        if (_lines.isNotEmpty()) {
            _lines.removeAt(_lines.lastIndex)
        }
    }

    fun clearCanvas() {
        _lines.clear()
    }

    fun completeTurn(canvasWidth: Float = 0f, canvasHeight: Float = 0f): Boolean {
        timerJob?.cancel()
        val currentPlayer = _players.getOrNull(_currentPlayerIndex.intValue) ?: ""
        _turns.add(
            GameTurn(
                playerName = currentPlayer,
                word = if (_currentPlayerIndex.intValue == 0) _currentWord.value else null,
                drawing = _lines.toList(),
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        )

        return if (_currentPlayerIndex.intValue < _players.size - 2) {
            _currentPlayerIndex.intValue += 1
            true // Continue to next drawing player
        } else {
            // Move to the last player who will guess
            _currentPlayerIndex.intValue = _players.size - 1
            false // Drawing phase completed, move to guess phase
        }
    }

    fun setFinalGuess(guess: String) {
        _finalGuess.value = guess.trim()
    }

    fun getPreviousDrawing(): List<Line> {
        return _turns.lastOrNull()?.drawing ?: emptyList()
    }

    fun getPreviousTurn(): GameTurn? {
        return _turns.lastOrNull()
    }

    override fun onCleared() {
        timerJob?.cancel()
    }

    class Factory(
        private val context: Context,
        private val repository: TimingPreferencesRepository = SharedPreferencesTimingPreferencesRepository.create(context)
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository) as T
        }
    }
}
