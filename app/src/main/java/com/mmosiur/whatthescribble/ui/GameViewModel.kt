package com.mmosiur.whatthescribble.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val strokeWidth: Float = 5f
)

data class GameTurn(
    val playerName: String,
    val word: String? = null,
    val drawing: List<Line> = emptyList(),
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f
)

class GameViewModel : ViewModel() {
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
    private val _drawDuration = mutableIntStateOf(60)
    val drawDuration: State<Int> = _drawDuration

    private val _peekDuration = mutableIntStateOf(10)
    val peekDuration: State<Int> = _peekDuration

    private val _timeLeft = mutableIntStateOf(60)
    val timeLeft: State<Int> = _timeLeft

    private val _peekTimeLeft = mutableIntStateOf(10)
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

    private val _finalGuess = mutableStateOf("")
    val finalGuess: State<String> = _finalGuess

    private var timerJob: Job? = null

    fun setDrawDuration(seconds: Int) {
        _drawDuration.intValue = seconds.coerceIn(15, 300)
    }

    fun setPeekDuration(seconds: Int) {
        _peekDuration.intValue = seconds.coerceIn(3, 60)
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

    fun startGame() {
        _turns.clear()
        _finalGuess.value = ""
        _currentPlayerIndex.intValue = 0
        _currentWord.value = polishWords.random()
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
}
