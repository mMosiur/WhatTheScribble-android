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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

private fun isEraserEvent(
    event: PointerEvent,
    motionEvent: MotionEvent? = event.motionEvent,
    change: PointerInputChange? = null
): Boolean {
    if (motionEvent != null) {
        val buttonState = motionEvent.buttonState
        val isStylusButton = (buttonState and (
            MotionEvent.BUTTON_STYLUS_PRIMARY or
            MotionEvent.BUTTON_STYLUS_SECONDARY or
            MotionEvent.BUTTON_SECONDARY or
            MotionEvent.BUTTON_TERTIARY
        )) != 0
        if (isStylusButton) return true

        val pointerCount = motionEvent.pointerCount
        for (i in 0 until pointerCount) {
            if (motionEvent.getToolType(i) == MotionEvent.TOOL_TYPE_ERASER) {
                return true
            }
        }
    }

    if (event.buttons.isSecondaryPressed || event.buttons.isTertiaryPressed) {
        return true
    }

    if (change != null && change.type == PointerType.Eraser) {
        return true
    }

    return event.changes.any { it.type == PointerType.Eraser }
}

@Composable
fun DrawingCanvas(
    lines: List<Line>,
    onDrawStart: (DrawnPoint, isEraser: Boolean) -> Unit,
    onDrawMove: (DrawnPoint) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    isEraserMode: Boolean = false,
    eraserStrokeWidth: Float = GameViewModel.DEFAULT_ERASER_STROKE_WIDTH,
    onSpenEraserActiveChanged: ((Boolean) -> Unit)? = null,
    originalCanvasWidth: Float = 0f,
    originalCanvasHeight: Float = 0f,
    onCanvasSizeMeasured: ((Float, Float) -> Unit)? = null
) {
    val currentOnDrawStart by rememberUpdatedState(onDrawStart)
    val currentOnDrawMove by rememberUpdatedState(onDrawMove)
    val currentIsEraserMode by rememberUpdatedState(isEraserMode)
    val currentEraserStrokeWidth by rememberUpdatedState(eraserStrokeWidth)
    val currentOnSpenEraserActiveChanged by rememberUpdatedState(onSpenEraserActiveChanged)
    val currentOnCanvasSizeMeasured by rememberUpdatedState(onCanvasSizeMeasured)

    var isSpenEraserActiveInternal by remember { mutableStateOf(false) }
    var eraserCursorPosition by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(isEraserMode) {
        if (!isEraserMode) {
            eraserCursorPosition = null
        }
    }

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
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .onSizeChanged { size ->
                    if (size.width > 0 && size.height > 0) {
                        currentOnCanvasSizeMeasured?.invoke(size.width.toFloat(), size.height.toFloat())
                    }
                }
                .then(
                    if (interactive) {
                        Modifier
                            .pointerInput(interactive) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        if (event.type == PointerEventType.Exit) {
                                            isSpenEraserActiveInternal = false
                                            currentOnSpenEraserActiveChanged?.invoke(false)
                                            eraserCursorPosition = null
                                        } else if (event.changes.none { it.pressed }) {
                                            val isEraser = isEraserEvent(event, event.motionEvent)
                                            isSpenEraserActiveInternal = isEraser
                                            currentOnSpenEraserActiveChanged?.invoke(isEraser)
                                            val isHover = event.changes.any {
                                                it.type == PointerType.Stylus ||
                                                it.type == PointerType.Eraser ||
                                                it.type == PointerType.Mouse
                                            }
                                            if (isHover && (currentIsEraserMode || isEraser)) {
                                                eraserCursorPosition = event.changes.firstOrNull()?.position
                                            } else {
                                                eraserCursorPosition = null
                                            }
                                        }
                                    }
                                }
                            }
                            .pointerInput(interactive) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val isSpenEraserAtDown = isEraserEvent(currentEvent, currentEvent.motionEvent, down)
                                    var currentErasing = currentIsEraserMode || isSpenEraserAtDown

                                    isSpenEraserActiveInternal = isSpenEraserAtDown
                                    currentOnSpenEraserActiveChanged?.invoke(isSpenEraserAtDown)
                                    if (currentErasing) {
                                        eraserCursorPosition = down.position
                                    } else {
                                        eraserCursorPosition = null
                                    }
                                    val initialPressure = if (currentErasing) 1f else down.pressure
                                    currentOnDrawStart(DrawnPoint(down.position, initialPressure), currentErasing)
                                    down.consume()

                                    do {
                                        val event = awaitPointerEvent()
                                        val motionEvent = event.motionEvent
                                        val spenEraserNow = isEraserEvent(event, motionEvent)
                                        isSpenEraserActiveInternal = spenEraserNow
                                        currentOnSpenEraserActiveChanged?.invoke(spenEraserNow)

                                        val isErasingNow = currentIsEraserMode || spenEraserNow

                                        if (isErasingNow != currentErasing) {
                                            currentErasing = isErasingNow
                                            val primaryChange = event.changes.firstOrNull { it.pressed } ?: event.changes.first()
                                            val p = if (currentErasing) 1f else primaryChange.pressure
                                            currentOnDrawStart(DrawnPoint(primaryChange.position, p), currentErasing)
                                        }

                                        if (currentErasing) {
                                            val currentPos = event.changes.firstOrNull { it.pressed }?.position
                                                ?: event.changes.firstOrNull()?.position
                                            if (currentPos != null) {
                                                eraserCursorPosition = currentPos
                                            }
                                        } else {
                                            eraserCursorPosition = null
                                        }

                                        event.changes.forEach { change ->
                                            val pressure = if (currentErasing) 1f else change.pressure
                                            if (change.pressed && change.position != change.previousPosition) {
                                                change.historical.forEach { historicalChange ->
                                                    currentOnDrawMove(
                                                        DrawnPoint(
                                                            offset = historicalChange.position,
                                                            pressure = pressure
                                                        )
                                                    )
                                                }
                                                currentOnDrawMove(
                                                    DrawnPoint(
                                                        offset = change.position,
                                                        pressure = pressure
                                                    )
                                                )
                                                change.consume()
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    eraserCursorPosition = null
                                    isSpenEraserActiveInternal = false
                                    currentOnSpenEraserActiveChanged?.invoke(false)
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
                val isEraser = line.isEraser
                val blendMode = if (isEraser) BlendMode.Clear else DrawScope.DefaultBlendMode
                val drawColor = if (isEraser) Color.Black else line.color

                if (line.points.size > 1) {
                    for (i in 0 until line.points.size - 1) {
                        val pt1 = line.points[i]
                        val pt2 = line.points[i + 1]

                        val p1 = Offset(pt1.offset.x * scale + offsetX, pt1.offset.y * scale + offsetY)
                        val p2 = Offset(pt2.offset.x * scale + offsetX, pt2.offset.y * scale + offsetY)

                        val width = if (isEraser) {
                            maxOf(4f, line.strokeWidth * strokeScale)
                        } else {
                            maxOf(1.5f, line.strokeWidth * strokeScale * pt1.pressure)
                        }

                        drawLine(
                            color = drawColor,
                            start = p1,
                            end = p2,
                            strokeWidth = width,
                            cap = StrokeCap.Round,
                            blendMode = blendMode
                        )
                    }
                } else if (line.points.size == 1) {
                    val pt = line.points[0]
                    val p = Offset(pt.offset.x * scale + offsetX, pt.offset.y * scale + offsetY)
                    val radius = if (isEraser) {
                        maxOf(2f, (line.strokeWidth * strokeScale) / 2f)
                    } else {
                        maxOf(1.5f, (line.strokeWidth * strokeScale) / 2f)
                    }
                    drawCircle(
                        color = drawColor,
                        radius = radius,
                        center = p,
                        blendMode = blendMode
                    )
                }
            }

            val isRubberActive = currentIsEraserMode || isSpenEraserActiveInternal
            if (interactive && isRubberActive) {
                eraserCursorPosition?.let { cursorPos ->
                    val radius = currentEraserStrokeWidth / 2f
                    drawCircle(
                        color = Color(0x18000000),
                        radius = radius,
                        center = cursorPos
                    )
                    drawCircle(
                        color = Color(0xCC333333),
                        radius = radius,
                        center = cursorPos,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xE6FFFFFF),
                        radius = maxOf(1f, radius - 1.5.dp.toPx()),
                        center = cursorPos,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xAA333333),
                        radius = 2.dp.toPx(),
                        center = cursorPos
                    )
                }
            }
        }
    }
}

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
    DrawingCanvas(
        lines = lines,
        onDrawStart = { pt, _ -> onDrawStart(pt) },
        onDrawMove = onDrawMove,
        modifier = modifier,
        interactive = interactive,
        isEraserMode = false,
        eraserStrokeWidth = GameViewModel.DEFAULT_ERASER_STROKE_WIDTH,
        onSpenEraserActiveChanged = null,
        originalCanvasWidth = originalCanvasWidth,
        originalCanvasHeight = originalCanvasHeight,
        onCanvasSizeMeasured = onCanvasSizeMeasured
    )
}
