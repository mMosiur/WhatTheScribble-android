package com.mmosiur.whatthescribble.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
}
