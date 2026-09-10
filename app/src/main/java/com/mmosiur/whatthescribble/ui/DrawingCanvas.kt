package com.mmosiur.whatthescribble.ui

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun DrawingCanvas(
    lines: List<Line>,
    onDrawStart: (DrawnPoint) -> Unit,
    onDrawMove: (DrawnPoint) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    originalCanvasWidth: Float = 0f,
    originalCanvasHeight: Float = 0f,
    onCanvasSizeMeasured: ((Float, Float) -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 3.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    if (size.width > 0 && size.height > 0) {
                        onCanvasSizeMeasured?.invoke(size.width.toFloat(), size.height.toFloat())
                    }
                }
                .then(
                    if (interactive) {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                onDrawStart(DrawnPoint(down.position, down.pressure))
                                down.consume()

                                do {
                                    val event = awaitPointerEvent()
                                    val motionEvent = event.motionEvent

                                    val isButtonPressed = if (motionEvent != null) {
                                        (motionEvent.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0 ||
                                                (motionEvent.buttonState and MotionEvent.BUTTON_SECONDARY) != 0
                                    } else {
                                        event.buttons.isSecondaryPressed
                                    }

                                    event.changes.forEach { change ->
                                        val pressure = if (isButtonPressed) 1f else change.pressure
                                        if (change.pressed && change.position != change.previousPosition) {
                                            change.historical.forEach { historicalChange ->
                                                onDrawMove(
                                                    DrawnPoint(
                                                        offset = historicalChange.position,
                                                        pressure = pressure
                                                    )
                                                )
                                            }
                                            onDrawMove(
                                                DrawnPoint(
                                                    offset = change.position,
                                                    pressure = pressure
                                                )
                                            )
                                            change.consume()
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    } else Modifier
                )
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Calculate scaling and centering when rendering in read-only / preview / recap cards
            val (scale, offsetX, offsetY) = if (!interactive && lines.isNotEmpty()) {
                val refW = if (originalCanvasWidth > 0f) {
                    originalCanvasWidth
                } else {
                    val maxX = lines.flatMap { it.points }.maxOfOrNull { it.offset.x } ?: canvasW
                    maxOf(canvasW, maxX)
                }

                val refH = if (originalCanvasHeight > 0f) {
                    originalCanvasHeight
                } else {
                    val maxY = lines.flatMap { it.points }.maxOfOrNull { it.offset.y } ?: canvasH
                    maxOf(canvasH, maxY)
                }

                val calculatedScale = minOf(canvasW / refW, canvasH / refH)
                val calculatedOffsetX = (canvasW - refW * calculatedScale) / 2f
                val calculatedOffsetY = (canvasH - refH * calculatedScale) / 2f
                Triple(calculatedScale, calculatedOffsetX, calculatedOffsetY)
            } else {
                Triple(1f, 0f, 0f)
            }

            lines.forEach { line ->
                val strokeScale = if (interactive) 1f else maxOf(0.4f, scale)
                if (line.points.size > 1) {
                    for (i in 0 until line.points.size - 1) {
                        val pt1 = line.points[i]
                        val pt2 = line.points[i + 1]

                        val p1 = Offset(pt1.offset.x * scale + offsetX, pt1.offset.y * scale + offsetY)
                        val p2 = Offset(pt2.offset.x * scale + offsetX, pt2.offset.y * scale + offsetY)

                        drawLine(
                            color = line.color,
                            start = p1,
                            end = p2,
                            strokeWidth = maxOf(1.5f, line.strokeWidth * strokeScale * pt1.pressure),
                            cap = StrokeCap.Round
                        )
                    }
                } else if (line.points.size == 1) {
                    val pt = line.points[0]
                    val p = Offset(pt.offset.x * scale + offsetX, pt.offset.y * scale + offsetY)
                    drawCircle(
                        color = line.color,
                        radius = maxOf(1.5f, (line.strokeWidth * strokeScale) / 2f),
                        center = p
                    )
                }
            }
        }
    }
}
