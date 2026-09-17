package pl.mmorus.whatthescribble.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import pl.mmorus.whatthescribble.data.TimingPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    @Test
    fun `addPlayer adds a player to the list`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        assertEquals(1, viewModel.players.size)
        assertEquals("Alice", viewModel.players[0])
    }

    @Test
    fun `addPlayer does not add blank player`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("")
        viewModel.addPlayer("   ")
        assertTrue(viewModel.players.isEmpty())
    }

    @Test
    fun `addPlayer does not add duplicate player`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Alice")
        assertEquals(1, viewModel.players.size)
    }

    @Test
    fun `removePlayer removes a player from the list`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.removePlayer("Alice")
        assertEquals(1, viewModel.players.size)
        assertEquals("Bob", viewModel.players[0])
    }

    @Test
    fun `setDrawDuration updates duration within allowed bounds`() {
        val viewModel = GameViewModel()
        viewModel.setDrawDuration(45)
        assertEquals(45, viewModel.drawDuration.value)

        // Below minimum bounds (15)
        viewModel.setDrawDuration(5)
        assertEquals(15, viewModel.drawDuration.value)

        // Above maximum bounds (300)
        viewModel.setDrawDuration(500)
        assertEquals(300, viewModel.drawDuration.value)
    }

    @Test
    fun `setPeekDuration updates duration within allowed bounds`() {
        val viewModel = GameViewModel()
        viewModel.setPeekDuration(15)
        assertEquals(15, viewModel.peekDuration.value)

        // Below minimum bounds (3)
        viewModel.setPeekDuration(1)
        assertEquals(3, viewModel.peekDuration.value)

        // Above maximum bounds (60)
        viewModel.setPeekDuration(100)
        assertEquals(60, viewModel.peekDuration.value)
    }

    @Test
    fun `startGame initializes game state without activating turn premature`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")

        viewModel.setDrawDuration(45)
        viewModel.setPeekDuration(15)
        viewModel.startGame()

        assertEquals(0, viewModel.currentPlayerIndex.value)
        assertTrue(viewModel.currentWord.value.isNotBlank())
        assertEquals(45, viewModel.timeLeft.value)
        assertEquals(15, viewModel.peekTimeLeft.value)
        assertFalse(viewModel.isTurnActive.value)
        assertFalse(viewModel.isPeeking.value)
        assertTrue(viewModel.turns.isEmpty())
    }

    @Test
    fun `drawing operations support add, update, undo, and clear`() {
        val viewModel = GameViewModel()
        val line1 = Line(points = listOf(DrawnPoint(Offset(10f, 10f))), color = Color.Red, strokeWidth = 5f)
        val line2 = Line(points = listOf(DrawnPoint(Offset(20f, 20f))), color = Color.Blue, strokeWidth = 8f)

        viewModel.addLine(line1)
        viewModel.addLine(line2)
        assertEquals(2, viewModel.lines.size)

        // Update last line
        viewModel.updateLastLine(DrawnPoint(Offset(25f, 25f)))
        assertEquals(2, viewModel.lines.last().points.size)

        // Undo
        viewModel.undoLastLine()
        assertEquals(1, viewModel.lines.size)
        assertEquals(Color.Red, viewModel.lines.first().color)

        // Clear
        viewModel.clearCanvas()
        assertTrue(viewModel.lines.isEmpty())
    }

    @Test
    fun `drawing lines support eraser attribute and default values`() {
        val viewModel = GameViewModel()
        val defaultLine = Line(points = listOf(DrawnPoint(Offset(10f, 10f))))
        assertFalse(defaultLine.isEraser)
        assertEquals(80f, GameViewModel.DEFAULT_ERASER_STROKE_WIDTH)

        val penLine = Line(
            points = listOf(DrawnPoint(Offset(10f, 10f))),
            color = Color.Blue,
            strokeWidth = 8f,
            isEraser = false
        )
        val eraserLine = Line(
            points = listOf(DrawnPoint(Offset(20f, 20f))),
            color = Color.Transparent,
            strokeWidth = GameViewModel.DEFAULT_ERASER_STROKE_WIDTH,
            isEraser = true
        )

        viewModel.addLine(penLine)
        viewModel.addLine(eraserLine)

        assertEquals(2, viewModel.lines.size)
        assertFalse(viewModel.lines[0].isEraser)
        assertEquals(Color.Blue, viewModel.lines[0].color)
        assertEquals(8f, viewModel.lines[0].strokeWidth)

        assertTrue(viewModel.lines[1].isEraser)
        assertEquals(80f, viewModel.lines[1].strokeWidth)

        // Undo removes eraser line first, leaving pen line
        viewModel.undoLastLine()
        assertEquals(1, viewModel.lines.size)
        assertFalse(viewModel.lines[0].isEraser)
    }

    @Test
    fun `completeTurn preserves canvas dimensions and advances through player chain`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Player 1")
        viewModel.addPlayer("Player 2")
        viewModel.addPlayer("Player 3")

        viewModel.startGame()
        val line = Line(points = listOf(DrawnPoint(Offset(50f, 50f))))
        viewModel.addLine(line)

        // Player 1 finishes turn
        val hasNextAfterP1 = viewModel.completeTurn(canvasWidth = 1080f, canvasHeight = 1920f)
        assertTrue(hasNextAfterP1)
        assertEquals(1, viewModel.turns.size)
        val turn1 = viewModel.turns[0]
        assertEquals("Player 1", turn1.playerName)
        assertEquals(1080f, turn1.canvasWidth)
        assertEquals(1920f, turn1.canvasHeight)
        assertNotNull(turn1.word) // Player 1 has prompt word recorded

        // Player 2 finishes turn (since total players = 3, Player 2 is last drawer, Player 3 will guess)
        viewModel.addLine(line)
        val hasNextAfterP2 = viewModel.completeTurn(canvasWidth = 1080f, canvasHeight = 1920f)
        assertFalse(hasNextAfterP2) // False means drawing phase ended, ready for guesser
        assertEquals(2, viewModel.turns.size)
        assertEquals(2, viewModel.currentPlayerIndex.value) // Index 2 is Player 3 (guesser)
    }

    @Test
    fun `setFinalGuess trims guess input`() {
        val viewModel = GameViewModel()
        viewModel.setFinalGuess("   Kot   ")
        assertEquals("Kot", viewModel.finalGuess.value)
    }

    @Test
    fun `restartGame clears turns and picks a new word while preserving players and timing`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")
        viewModel.setDrawDuration(90)

        viewModel.startGame()
        viewModel.addLine(Line(points = listOf(DrawnPoint(Offset(1f, 1f)))))
        viewModel.completeTurn()
        viewModel.setFinalGuess("Pies")

        viewModel.restartGame()
        assertEquals(0, viewModel.currentPlayerIndex.value)
        assertTrue(viewModel.turns.isEmpty())
        assertEquals("", viewModel.finalGuess.value)
        assertEquals(90, viewModel.drawDuration.value)
        assertEquals(3, viewModel.players.size)
    }

    @Test
    fun `startGame with explicit custom word uses provided word`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")

        viewModel.startGame(customWord = "Statek Kosmiczny")
        assertEquals("Statek Kosmiczny", viewModel.currentWord.value)
    }

    @Test
    fun `startGame with WordMode CUSTOM uses customWordInput`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")

        viewModel.setWordMode(WordMode.CUSTOM)
        viewModel.setCustomWordInput("Żyrafa")
        viewModel.startGame()
        assertEquals("Żyrafa", viewModel.currentWord.value)
    }

    @Test
    fun `setCurrentWord and rollRandomWord update currentWord`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")

        viewModel.startGame()
        viewModel.setCurrentWord("Klawiatura")
        assertEquals("Klawiatura", viewModel.currentWord.value)

        val rolled = viewModel.rollRandomWord()
        assertEquals(rolled, viewModel.currentWord.value)
        assertTrue(rolled.isNotBlank())
    }

    @Test
    fun `sanitizeWord filters out emojis and special symbols`() {
        // Emojis and symbols
        val resultWithEmojis = GameViewModel.sanitizeWord("Kot 🐱 Pies 🐶!")
        assertEquals("Kot  Pies ", resultWithEmojis)

        // Polish diacritics and allowed characters (letters, digits, spaces, hyphens, apostrophes)
        val validPolishWord = GameViewModel.sanitizeWord("Zażółć gęślą jaźń-123 'test'")
        assertEquals("Zażółć gęślą jaźń-123 'test'", validPolishWord)

        // Only emojis results in empty string
        val onlyEmojis = GameViewModel.sanitizeWord("😀🎉🚀🔥")
        assertEquals("", onlyEmojis)
    }

    @Test
    fun `sanitizeWord caps length to MAX_WORD_LENGTH`() {
        val longWord = "A".repeat(50)
        val sanitized = GameViewModel.sanitizeWord(longWord)
        assertEquals(GameViewModel.MAX_WORD_LENGTH, sanitized.length)
        assertEquals(30, sanitized.length)
    }

    @Test
    fun `setCurrentWord and setCustomWordInput sanitize input`() {
        val viewModel = GameViewModel()
        viewModel.setCurrentWord("Słońce 🌞")
        assertEquals("Słońce", viewModel.currentWord.value)

        viewModel.setCustomWordInput("Rakieta 🚀")
        assertEquals("Rakieta ", viewModel.customWordInput.value)
    }

    @Test
    fun `startGame with custom word sanitizes emojis`() {
        val viewModel = GameViewModel()
        viewModel.addPlayer("P1")
        viewModel.addPlayer("P2")
        viewModel.addPlayer("P3")

        viewModel.startGame(customWord = "Pizza 🍕")
        assertEquals("Pizza", viewModel.currentWord.value)
    }

    @Test
    fun `given saved timing preferences when viewModel initialized then durations loaded from repository`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 45, initialPeek = 15)
        val viewModel = GameViewModel(fakeRepo)

        assertEquals(45, viewModel.drawDuration.value)
        assertEquals(15, viewModel.peekDuration.value)
        assertEquals(45, viewModel.timeLeft.value)
        assertEquals(15, viewModel.peekTimeLeft.value)
    }

    @Test
    fun `given preferences repository when setDrawDuration called then duration updated and saved`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 60, initialPeek = 10)
        val viewModel = GameViewModel(fakeRepo)

        viewModel.setDrawDuration(90)
        assertEquals(90, viewModel.drawDuration.value)
        assertEquals(90, viewModel.timeLeft.value)
        assertEquals(90, fakeRepo.savedDraw)
    }

    @Test
    fun `given preferences repository when setPeekDuration called then duration updated and saved`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 60, initialPeek = 10)
        val viewModel = GameViewModel(fakeRepo)

        viewModel.setPeekDuration(20)
        assertEquals(20, viewModel.peekDuration.value)
        assertEquals(20, viewModel.peekTimeLeft.value)
        assertEquals(20, fakeRepo.savedPeek)
    }

    @Test
    fun `given preferences repository when setDrawDuration out of bounds then coerced and saved`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 60, initialPeek = 10)
        val viewModel = GameViewModel(fakeRepo)

        viewModel.setDrawDuration(500)
        assertEquals(300, viewModel.drawDuration.value)
        assertEquals(300, fakeRepo.savedDraw)

        viewModel.setDrawDuration(5)
        assertEquals(15, viewModel.drawDuration.value)
        assertEquals(15, fakeRepo.savedDraw)
    }

    @Test
    fun `given preferences repository when setPeekDuration out of bounds then coerced and saved`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 60, initialPeek = 10)
        val viewModel = GameViewModel(fakeRepo)

        viewModel.setPeekDuration(100)
        assertEquals(60, viewModel.peekDuration.value)
        assertEquals(60, fakeRepo.savedPeek)

        viewModel.setPeekDuration(1)
        assertEquals(3, viewModel.peekDuration.value)
        assertEquals(3, fakeRepo.savedPeek)
    }

    @Test
    fun `given saved preferences when game restarted then selected timings are remembered`() {
        val fakeRepo = FakeTimingPreferencesRepository(initialDraw = 45, initialPeek = 15)
        val viewModel = GameViewModel(fakeRepo)
        viewModel.addPlayer("Alice")
        viewModel.addPlayer("Bob")
        viewModel.addPlayer("Charlie")

        viewModel.startGame()
        assertEquals(45, viewModel.timeLeft.value)
        assertEquals(15, viewModel.peekTimeLeft.value)

        viewModel.restartGame()
        assertEquals(45, viewModel.drawDuration.value)
        assertEquals(15, viewModel.peekDuration.value)
        assertEquals(45, viewModel.timeLeft.value)
        assertEquals(15, viewModel.peekTimeLeft.value)
    }
}

private class FakeTimingPreferencesRepository(
    var initialDraw: Int = 60,
    var initialPeek: Int = 10
) : TimingPreferencesRepository {
    var savedDraw: Int? = null
    var savedPeek: Int? = null

    override fun getDrawDuration(defaultDuration: Int): Int = initialDraw
    override fun getPeekDuration(defaultDuration: Int): Int = initialPeek

    override fun saveDrawDuration(duration: Int) {
        savedDraw = duration
        initialDraw = duration
    }

    override fun savePeekDuration(duration: Int) {
        savedPeek = duration
        initialPeek = duration
    }
}
