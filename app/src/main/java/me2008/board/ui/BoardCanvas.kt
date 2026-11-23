package me2008.board.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import me2008.board.model.*
import kotlin.math.*

@Composable
fun BoardCanvas(
    pins: List<PinItem>,
    strokes: List<StrokeItem>,
    shapes: List<DrawnShapeItem>,
    circuits: List<CircuitItem>,
    stickers: List<StickerItem>,
    formulas: List<FormulaItem>,
    activeStroke: List<Offset>?,
    activeStrokeColor: Color,
    activeStrokeWidth: Float,
    isActiveHighlighter: Boolean,
    previewShape: DrawnShapeItem?,
    laserPoints: List<LaserPoint>,
    bgTheme: BackgroundColorTheme,
    bgPattern: BackgroundPattern,
    isRulerVisible: Boolean,
    rulerOffset: Offset,
    rulerAngle: Float,
    isProtractorVisible: Boolean,
    protractorOffset: Offset,
    protractorAngle: Float,
    toolMode: ToolMode,
    zoomScale: Float,
    panOffset: Offset,
    onTransform: (panChange: Offset, zoomChange: Float) -> Unit,
    onTap: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRulerDrag: (Offset) -> Unit,
    onRulerRotate: (Float) -> Unit,
    onProtractorDrag: (Offset) -> Unit,
    onProtractorRotate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep updated state references so gestures never restart or cancel on state mutation
    val currentRulerOffset by rememberUpdatedState(rulerOffset)
    val currentRulerAngle by rememberUpdatedState(rulerAngle)
    val currentIsRulerVisible by rememberUpdatedState(isRulerVisible)
    val currentProtractorOffset by rememberUpdatedState(protractorOffset)
    val currentProtractorAngle by rememberUpdatedState(protractorAngle)
    val currentIsProtractorVisible by rememberUpdatedState(isProtractorVisible)
    val currentToolMode by rememberUpdatedState(toolMode)
    val currentZoom by rememberUpdatedState(zoomScale)
    val currentPan by rememberUpdatedState(panOffset)

    val currentOnRulerDrag by rememberUpdatedState(onRulerDrag)
    val currentOnRulerRotate by rememberUpdatedState(onRulerRotate)
    val currentOnProtractorDrag by rememberUpdatedState(onProtractorDrag)
    val currentOnProtractorRotate by rememberUpdatedState(onProtractorRotate)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnTransform by rememberUpdatedState(onTransform)

    fun screenToBoard(screen: Offset): Offset {
        return (screen - currentPan) / currentZoom
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // Pinch-to-zoom & two-finger pan detection
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f || pan != Offset.Zero) {
                        currentOnTransform(pan, zoom)
                    }
                }
            }
            // Tap gestures with coordinate transformation
            .pointerInput(Unit) {
                detectTapGestures { screenOffset ->
                    val boardOffset = screenToBoard(screenOffset)
                    currentOnTap(boardOffset)
                }
            }
            // Drawing & tool drag gestures
            .pointerInput(Unit) {
                var isDraggingRuler = false
                var isRotatingRuler = false
                var isDraggingProtractor = false
                var isRotatingProtractor = false

                detectDragGestures(
                    onDragStart = { screenOffset ->
                        val boardOffset = screenToBoard(screenOffset)
                        if (currentIsRulerVisible && isNearRulerRotateHandle(boardOffset, currentRulerOffset, currentRulerAngle)) {
                            isRotatingRuler = true
                        } else if (currentIsRulerVisible && isNearRulerBody(boardOffset, currentRulerOffset, currentRulerAngle)) {
                            isDraggingRuler = true
                        } else if (currentIsProtractorVisible && isNearProtractorRotate(boardOffset, currentProtractorOffset, currentProtractorAngle)) {
                            isRotatingProtractor = true
                        } else if (currentIsProtractorVisible && isNearProtractorBody(boardOffset, currentProtractorOffset, currentProtractorAngle)) {
                            isDraggingProtractor = true
                        } else {
                            currentOnDragStart(boardOffset)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val boardPos = screenToBoard(change.position)
                        val scaledDragAmount = dragAmount / currentZoom

                        when {
                            isRotatingRuler -> {
                                val dx = boardPos.x - currentRulerOffset.x
                                val dy = boardPos.y - currentRulerOffset.y
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                currentOnRulerRotate(angle)
                            }
                            isDraggingRuler -> {
                                currentOnRulerDrag(scaledDragAmount)
                            }
                            isRotatingProtractor -> {
                                val dx = boardPos.x - currentProtractorOffset.x
                                val dy = boardPos.y - currentProtractorOffset.y
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                currentOnProtractorRotate(angle)
                            }
                            isDraggingProtractor -> {
                                currentOnProtractorDrag(scaledDragAmount)
                            }
                            else -> {
                                currentOnDrag(boardPos)
                            }
                        }
                    },
                    onDragEnd = {
                        isDraggingRuler = false
                        isRotatingRuler = false
                        isDraggingProtractor = false
                        isRotatingProtractor = false
                        currentOnDragEnd()
                    }
                )
            }
    ) {
        // Base screen background color
        drawRect(color = bgTheme.color)

        // Apply Zoom and Pan transformations to everything drawn on board
        translate(left = panOffset.x, top = panOffset.y) {
            scale(scale = zoomScale, pivot = Offset.Zero) {
                // Background Pattern
                drawBackgroundPattern(bgPattern, bgTheme.gridColor, size, zoomScale, panOffset)

                // Completed Strokes
                strokes.forEach { stroke ->
                    drawSmoothStroke(stroke)
                }

                // Active live stroke
                activeStroke?.let { pts ->
                    if (pts.isNotEmpty()) {
                        val liveStroke = StrokeItem(
                            points = pts,
                            color = activeStrokeColor,
                            strokeWidth = activeStrokeWidth,
                            isHighlighter = isActiveHighlighter
                        )
                        drawSmoothStroke(liveStroke)
                    }
                }

                // Shapes
                shapes.forEach { shape ->
                    drawShapeItem(shape)
                }

                // Shape Preview
                previewShape?.let { shape ->
                    drawShapeItem(shape)
                }

                // Pins
                pins.forEach { pin ->
                    drawPinItem(pin)
                }

                // Circuit Components
                circuits.forEach { circuit ->
                    drawCircuitElement(circuit)
                }

                // Formulas
                formulas.forEach { formula ->
                    drawFormulaItem(formula)
                }

                // Stamps / Stickers
                stickers.forEach { sticker ->
                    drawStickerItem(sticker)
                }

                // Interactive Math Ruler
                if (isRulerVisible) {
                    drawInteractiveRuler(rulerOffset, rulerAngle)
                }

                // Interactive Math Protractor
                if (isProtractorVisible) {
                    drawInteractiveProtractor(protractorOffset, protractorAngle)
                }
            }
        }

        // Laser pointer drawn in screen space for maximum crisp neon glow
        if (laserPoints.isNotEmpty()) {
            drawContinuousLaserTrail(laserPoints, zoomScale, panOffset)
        }
    }
}

// Bézier curve smoothing helper
fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) {
        path.moveTo(points[0].x, points[0].y)
        path.addOval(Rect(points[0].x - 1f, points[0].y - 1f, points[0].x + 1f, points[0].y + 1f))
        return path
    }
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        if (i == 1) {
            path.lineTo(midX, midY)
        } else {
            path.quadraticBezierTo(prev.x, prev.y, midX, midY)
        }
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

private fun DrawScope.drawSmoothStroke(stroke: StrokeItem) {
    val path = buildSmoothPath(stroke.points)
    val alpha = if (stroke.isHighlighter) 0.38f else 1.0f
    val cap = if (stroke.isHighlighter) StrokeCap.Square else StrokeCap.Round
    drawPath(
        path = path,
        color = stroke.color.copy(alpha = alpha),
        style = Stroke(width = stroke.strokeWidth, cap = cap, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawBackgroundPattern(
    pattern: BackgroundPattern,
    color: Color,
    viewportSize: Size,
    zoom: Float,
    pan: Offset
) {
    val step = 44f
    val startX = ((-pan.x) / zoom).coerceAtLeast(-5000f)
    val endX = ((viewportSize.width - pan.x) / zoom).coerceAtMost(10000f)
    val startY = ((-pan.y) / zoom).coerceAtLeast(-5000f)
    val endY = ((viewportSize.height - pan.y) / zoom).coerceAtMost(10000f)

    when (pattern) {
        BackgroundPattern.PLAIN -> {}
        BackgroundPattern.GRID -> {
            var x = (floor(startX / step) * step)
            while (x < endX) {
                drawLine(color, Offset(x, startY), Offset(x, endY), strokeWidth = 1f)
                x += step
            }
            var y = (floor(startY / step) * step)
            while (y < endY) {
                drawLine(color, Offset(startX, y), Offset(endX, y), strokeWidth = 1f)
                y += step
            }
        }
        BackgroundPattern.DOTS -> {
            var x = (floor(startX / step) * step)
            while (x < endX) {
                var y = (floor(startY / step) * step)
                while (y < endY) {
                    drawCircle(color, radius = 2f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        BackgroundPattern.LINES -> {
            var y = (floor(startY / step) * step)
            while (y < endY) {
                drawLine(color, Offset(startX, y), Offset(endX, y), strokeWidth = 1f)
                y += step
            }
        }
    }
}

private fun DrawScope.drawPinItem(pin: PinItem) {
    val radius = pin.size / 2f
    when (pin.shape) {
        PinShape.CIRCLE -> {
            drawCircle(color = pin.color, radius = radius, center = pin.position)
            drawCircle(color = Color.White.copy(alpha = 0.6f), radius = radius * 0.35f, center = pin.position - Offset(radius * 0.25f, radius * 0.25f))
        }
        PinShape.SQUARE -> {
            drawRect(
                color = pin.color,
                topLeft = Offset(pin.position.x - radius, pin.position.y - radius),
                size = Size(pin.size, pin.size)
            )
        }
        PinShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(pin.position.x, pin.position.y - radius * 1.2f)
                lineTo(pin.position.x + radius * 1.2f, pin.position.y)
                lineTo(pin.position.x, pin.position.y + radius * 1.2f)
                lineTo(pin.position.x - radius * 1.2f, pin.position.y)
                close()
            }
            drawPath(path, color = pin.color)
        }
        PinShape.STAR -> {
            val path = Path()
            val spikes = 5
            val outerRadius = radius * 1.2f
            val innerRadius = outerRadius * 0.45f
            for (i in 0 until spikes * 2) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = i * Math.PI / spikes - Math.PI / 2
                val px = (pin.position.x + r * cos(angle)).toFloat()
                val py = (pin.position.y + r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, color = pin.color)
        }
        PinShape.PUSHPIN -> {
            drawLine(Color.Gray, pin.position, pin.position + Offset(radius * 0.8f, radius * 0.8f), strokeWidth = 3f)
            drawCircle(pin.color, radius = radius * 0.8f, center = pin.position)
            drawCircle(Color.White.copy(alpha = 0.5f), radius = radius * 0.3f, center = pin.position - Offset(radius * 0.2f, radius * 0.2f))
        }
    }
}

private fun DrawScope.drawShapeItem(shape: DrawnShapeItem) {
    val stroke = Stroke(width = shape.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (shape.shapeType) {
        ShapeType.LINE -> {
            drawLine(shape.color, shape.start, shape.end, strokeWidth = shape.strokeWidth, cap = StrokeCap.Round)
        }
        ShapeType.ARROW -> {
            drawLine(shape.color, shape.start, shape.end, strokeWidth = shape.strokeWidth, cap = StrokeCap.Round)
            drawArrowHead(shape.end, shape.start, shape.color, shape.strokeWidth)
        }
        ShapeType.DOUBLE_ARROW -> {
            drawLine(shape.color, shape.start, shape.end, strokeWidth = shape.strokeWidth, cap = StrokeCap.Round)
            drawArrowHead(shape.end, shape.start, shape.color, shape.strokeWidth)
            drawArrowHead(shape.start, shape.end, shape.color, shape.strokeWidth)
        }
        ShapeType.RECTANGLE -> {
            val left = min(shape.start.x, shape.end.x)
            val top = min(shape.start.y, shape.end.y)
            val width = abs(shape.end.x - shape.start.x)
            val height = abs(shape.end.y - shape.start.y)
            drawRect(shape.color, Offset(left, top), Size(width, height), style = stroke)
        }
        ShapeType.CIRCLE -> {
            val left = min(shape.start.x, shape.end.x)
            val top = min(shape.start.y, shape.end.y)
            val width = abs(shape.end.x - shape.start.x)
            val height = abs(shape.end.y - shape.start.y)
            drawOval(shape.color, Offset(left, top), Size(width, height), style = stroke)
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo((shape.start.x + shape.end.x) / 2f, shape.start.y)
                lineTo(shape.end.x, shape.end.y)
                lineTo(shape.start.x, shape.end.y)
                close()
            }
            drawPath(path, shape.color, style = stroke)
        }
        ShapeType.COORDINATE_AXIS -> {
            val midX = (shape.start.x + shape.end.x) / 2f
            val midY = (shape.start.y + shape.end.y) / 2f
            val left = min(shape.start.x, shape.end.x)
            val right = max(shape.start.x, shape.end.x)
            val top = min(shape.start.y, shape.end.y)
            val bottom = max(shape.start.y, shape.end.y)

            drawLine(shape.color, Offset(left, midY), Offset(right, midY), strokeWidth = shape.strokeWidth)
            drawArrowHead(Offset(right, midY), Offset(left, midY), shape.color, shape.strokeWidth)

            drawLine(shape.color, Offset(midX, bottom), Offset(midX, top), strokeWidth = shape.strokeWidth)
            drawArrowHead(Offset(midX, top), Offset(midX, bottom), shape.color, shape.strokeWidth)

            val tickStep = 32f
            var tx = midX + tickStep
            while (tx < right - 20f) {
                drawLine(shape.color, Offset(tx, midY - 6f), Offset(tx, midY + 6f), strokeWidth = 2f)
                tx += tickStep
            }
            tx = midX - tickStep
            while (tx > left + 20f) {
                drawLine(shape.color, Offset(tx, midY - 6f), Offset(tx, midY + 6f), strokeWidth = 2f)
                tx -= tickStep
            }
            var ty = midY - tickStep
            while (ty > top + 20f) {
                drawLine(shape.color, Offset(midX - 6f, ty), Offset(midX + 6f, ty), strokeWidth = 2f)
                ty -= tickStep
            }
            ty = midY + tickStep
            while (ty < bottom - 20f) {
                drawLine(shape.color, Offset(midX - 6f, ty), Offset(midX + 6f, ty), strokeWidth = 2f)
                ty += tickStep
            }
        }
    }
}

private fun DrawScope.drawArrowHead(tip: Offset, from: Offset, color: Color, strokeWidth: Float) {
    val dx = tip.x - from.x
    val dy = tip.y - from.y
    val angle = atan2(dy, dx)
    val length = (strokeWidth * 3.8f).coerceAtLeast(18f)
    val arrowAngle = Math.PI / 6.0
    val p1 = Offset(
        (tip.x - length * cos(angle - arrowAngle)).toFloat(),
        (tip.y - length * sin(angle - arrowAngle)).toFloat()
    )
    val p2 = Offset(
        (tip.x - length * cos(angle + arrowAngle)).toFloat(),
        (tip.y - length * sin(angle + arrowAngle)).toFloat()
    )
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawCircuitElement(circuit: CircuitItem) {
    val x = circuit.position.x
    val y = circuit.position.y
    val c = circuit.color
    val stroke = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (circuit.type) {
        CircuitElementType.RESISTOR -> {
            drawLine(c, Offset(x - 50f, y), Offset(x - 25f, y), strokeWidth = 3.5f)
            val path = Path().apply {
                moveTo(x - 25f, y)
                lineTo(x - 17f, y - 14f)
                lineTo(x - 8f, y + 14f)
                lineTo(x, y - 14f)
                lineTo(x + 8f, y + 14f)
                lineTo(x + 17f, y - 14f)
                lineTo(x + 25f, y)
            }
            drawPath(path, color = c, style = stroke)
            drawLine(c, Offset(x + 25f, y), Offset(x + 50f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.BATTERY -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 10f, y), strokeWidth = 3.5f)
            drawLine(c, Offset(x - 10f, y - 24f), Offset(x - 10f, y + 24f), strokeWidth = 3.5f, cap = StrokeCap.Square)
            drawLine(c, Offset(x + 10f, y - 14f), Offset(x + 10f, y + 14f), strokeWidth = 7f, cap = StrokeCap.Square)
            drawLine(c, Offset(x + 10f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.AC_SOURCE -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 20f, y), strokeWidth = 3.5f)
            drawCircle(c, radius = 20f, center = Offset(x, y), style = Stroke(3.5f))
            val path = Path().apply {
                moveTo(x - 12f, y)
                quadraticBezierTo(x - 6f, y - 10f, x, y)
                quadraticBezierTo(x + 6f, y + 10f, x + 12f, y)
            }
            drawPath(path, c, style = Stroke(2.5f, cap = StrokeCap.Round))
            drawLine(c, Offset(x + 20f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.BULB -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 20f, y), strokeWidth = 3.5f)
            drawCircle(color = c, radius = 20f, center = Offset(x, y), style = Stroke(3.5f))
            drawLine(c, Offset(x - 13f, y - 13f), Offset(x + 13f, y + 13f), strokeWidth = 3f)
            drawLine(c, Offset(x + 13f, y - 13f), Offset(x - 13f, y + 13f), strokeWidth = 3f)
            drawLine(c, Offset(x + 20f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.SWITCH -> {
            drawLine(c, Offset(x - 40f, y), Offset(x - 15f, y), strokeWidth = 3.5f)
            drawCircle(c, radius = 4f, center = Offset(x - 15f, y), style = Stroke(2.5f))
            drawLine(c, Offset(x - 13f, y), Offset(x + 12f, y - 18f), strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawCircle(c, radius = 4f, center = Offset(x + 15f, y), style = Stroke(2.5f))
            drawLine(c, Offset(x + 15f, y), Offset(x + 40f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.CAPACITOR -> {
            drawLine(c, Offset(x - 40f, y), Offset(x - 7f, y), strokeWidth = 3.5f)
            drawLine(c, Offset(x - 7f, y - 20f), Offset(x - 7f, y + 20f), strokeWidth = 4.5f)
            drawLine(c, Offset(x + 7f, y - 20f), Offset(x + 7f, y + 20f), strokeWidth = 4.5f)
            drawLine(c, Offset(x + 7f, y), Offset(x + 40f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.INDUCTOR -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 28f, y), strokeWidth = 3.5f)
            val path = Path().apply {
                moveTo(x - 28f, y)
                cubicTo(x - 28f, y - 18f, x - 14f, y - 18f, x - 14f, y)
                cubicTo(x - 14f, y - 18f, x, y - 18f, x, y)
                cubicTo(x, y - 18f, x + 14f, y - 18f, x + 14f, y)
                cubicTo(x + 14f, y - 18f, x + 28f, y - 18f, x + 28f, y)
            }
            drawPath(path, c, style = Stroke(3.5f, cap = StrokeCap.Round))
            drawLine(c, Offset(x + 28f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.VOLTMETER -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 20f, y), strokeWidth = 3.5f)
            drawCircle(c, radius = 20f, center = Offset(x, y), style = Stroke(3.5f))
            val path = Path().apply {
                moveTo(x - 8f, y - 8f)
                lineTo(x, y + 8f)
                lineTo(x + 8f, y - 8f)
            }
            drawPath(path, c, style = Stroke(2.5f, cap = StrokeCap.Round))
            drawLine(c, Offset(x + 20f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.AMMETER -> {
            drawLine(c, Offset(x - 45f, y), Offset(x - 20f, y), strokeWidth = 3.5f)
            drawCircle(c, radius = 20f, center = Offset(x, y), style = Stroke(3.5f))
            val path = Path().apply {
                moveTo(x - 8f, y + 8f)
                lineTo(x, y - 8f)
                lineTo(x + 8f, y + 8f)
            }
            drawPath(path, c, style = Stroke(2.5f, cap = StrokeCap.Round))
            drawLine(c, Offset(x - 4f, y + 2f), Offset(x + 4f, y + 2f), strokeWidth = 2.5f)
            drawLine(c, Offset(x + 20f, y), Offset(x + 45f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.DIODE -> {
            drawLine(c, Offset(x - 40f, y), Offset(x - 14f, y), strokeWidth = 3.5f)
            val path = Path().apply {
                moveTo(x - 14f, y - 16f)
                lineTo(x + 14f, y)
                lineTo(x - 14f, y + 16f)
                close()
            }
            drawPath(path, c, style = Stroke(3f))
            drawLine(c, Offset(x + 14f, y - 16f), Offset(x + 14f, y + 16f), strokeWidth = 3.5f)
            drawLine(c, Offset(x + 14f, y), Offset(x + 40f, y), strokeWidth = 3.5f)
        }
        CircuitElementType.GROUND -> {
            drawLine(c, Offset(x, y - 24f), Offset(x, y), strokeWidth = 3.5f)
            drawLine(c, Offset(x - 24f, y), Offset(x + 24f, y), strokeWidth = 3.5f)
            drawLine(c, Offset(x - 15f, y + 7f), Offset(x + 15f, y + 7f), strokeWidth = 3.5f)
            drawLine(c, Offset(x - 7f, y + 14f), Offset(x + 7f, y + 14f), strokeWidth = 3.5f)
        }
    }
}

private fun DrawScope.drawStickerItem(sticker: StickerItem) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            textSize = sticker.size
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText(
            sticker.type.symbol,
            sticker.position.x,
            sticker.position.y + sticker.size / 3f,
            paint
        )
    }
}

private fun DrawScope.drawFormulaItem(formula: FormulaItem) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            textSize = formula.fontSize
            textAlign = Paint.Align.LEFT
            color = formula.color.toArgb()
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText(
            formula.text,
            formula.position.x,
            formula.position.y,
            paint
        )
    }
}

// Continuous Glowing Neon Orange Laser Trail
private fun DrawScope.drawContinuousLaserTrail(
    points: List<LaserPoint>,
    zoom: Float,
    pan: Offset
) {
    val now = System.currentTimeMillis()
    val active = points.filter { now - it.timestamp < 1200L }
    if (active.size < 2) return

    val path = Path()
    val first = (active.first().position * zoom) + pan
    path.moveTo(first.x, first.y)

    for (i in 1 until active.size) {
        val pt = (active[i].position * zoom) + pan
        path.lineTo(pt.x, pt.y)
    }

    // Outer neon glow
    drawPath(
        path = path,
        color = Color(0xFFFF6D00).copy(alpha = 0.45f),
        style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Core neon orange streak
    drawPath(
        path = path,
        color = Color(0xFFFF9100),
        style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Hot white-gold center
    drawPath(
        path = path,
        color = Color(0xFFFFFBEB),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Leading pointer tip
    val tip = (active.last().position * zoom) + pan
    drawCircle(Color(0xFFFF9100), radius = 10f, center = tip)
    drawCircle(Color.White, radius = 5f, center = tip)
}

// Interactive Ruler
private fun DrawScope.drawInteractiveRuler(center: Offset, angle: Float) {
    val rulerWidth = 480f
    val rulerHeight = 72f

    rotate(angle, pivot = center) {
        val topLeft = Offset(center.x - rulerWidth / 2f, center.y - rulerHeight / 2f)

        drawRoundRect(
            color = Color(0xE6FFFBEB),
            topLeft = topLeft,
            size = Size(rulerWidth, rulerHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Color(0xFFD97706),
            topLeft = topLeft,
            size = Size(rulerWidth, rulerHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
            style = Stroke(2f)
        )

        val numCm = 10
        val cmSpacing = (rulerWidth - 40f) / numCm
        val startX = topLeft.x + 20f
        val topY = topLeft.y

        for (cm in 0..numCm) {
            val cmX = startX + cm * cmSpacing
            drawLine(
                color = Color(0xFFB45309),
                start = Offset(cmX, topY),
                end = Offset(cmX, topY + 22f),
                strokeWidth = 2f
            )

            if (cm < numCm) {
                for (mm in 1..4) {
                    val mmX = cmX + mm * (cmSpacing / 10f)
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(mmX, topY),
                        end = Offset(mmX, topY + 10f),
                        strokeWidth = 1f
                    )
                }
                val halfX = cmX + 5 * (cmSpacing / 10f)
                drawLine(
                    color = Color(0xFFB45309),
                    start = Offset(halfX, topY),
                    end = Offset(halfX, topY + 15f),
                    strokeWidth = 1.5f
                )
                for (mm in 6..9) {
                    val mmX = cmX + mm * (cmSpacing / 10f)
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(mmX, topY),
                        end = Offset(mmX, topY + 10f),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Center Drag Handle
        drawCircle(
            color = Color(0xFFD97706).copy(alpha = 0.5f),
            radius = 16f,
            center = center
        )

        // Rotate Handle at right edge
        val rotateHandle = Offset(topLeft.x + rulerWidth - 24f, center.y)
        drawCircle(Color(0xFFD97706), radius = 13f, center = rotateHandle)
        drawCircle(Color.White, radius = 5f, center = rotateHandle)
    }
}

// Interactive 180-degree Protractor
private fun DrawScope.drawInteractiveProtractor(center: Offset, angle: Float) {
    val radius = 200f
    rotate(angle, pivot = center) {
        val rect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
        drawArc(
            color = Color(0xD9E0F2FE),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = rect.topLeft,
            size = rect.size
        )
        drawArc(
            color = Color(0xFF0284C7),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(2f)
        )

        drawLine(
            Color(0xFF0284C7),
            Offset(center.x - radius, center.y),
            Offset(center.x + radius, center.y),
            strokeWidth = 2f
        )

        for (deg in 0..180 step 10) {
            val rad = Math.toRadians((180 - deg).toDouble())
            val outerX = (center.x + radius * cos(rad)).toFloat()
            val outerY = (center.y - radius * sin(rad)).toFloat()
            val tickLen = if (deg % 30 == 0 || deg == 90 || deg == 45 || deg == 135) 20f else 12f
            val innerX = (center.x + (radius - tickLen) * cos(rad)).toFloat()
            val innerY = (center.y - (radius - tickLen) * sin(rad)).toFloat()

            drawLine(
                Color(0xFF0369A1),
                Offset(innerX, innerY),
                Offset(outerX, outerY),
                strokeWidth = if (deg % 30 == 0) 2f else 1f
            )
        }

        drawCircle(Color(0xFF0284C7), radius = 6f, center = center)
        drawCircle(Color.White, radius = 2f, center = center)

        val apex = Offset(center.x, center.y - radius + 22f)
        drawCircle(Color(0xFF0284C7), radius = 11f, center = apex)
        drawCircle(Color.White, radius = 4f, center = apex)
    }
}

private fun isNearRulerBody(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val rad = -Math.toRadians(angle.toDouble())
    val localX = dx * cos(rad) - dy * sin(rad)
    val localY = dx * sin(rad) + dy * cos(rad)
    return abs(localX) <= 240f && abs(localY) <= 40f
}

private fun isNearRulerRotateHandle(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val rad = -Math.toRadians(angle.toDouble())
    val localX = dx * cos(rad) - dy * sin(rad)
    val localY = dx * sin(rad) + dy * cos(rad)
    return hypot(localX - (240f - 24f), localY) < 36f
}

private fun isNearProtractorBody(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    return hypot(dx, dy) <= 200f
}

private fun isNearProtractorRotate(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val rad = -Math.toRadians(angle.toDouble())
    val localX = dx * cos(rad) - dy * sin(rad)
    val localY = dx * sin(rad) + dy * cos(rad)
    return hypot(localX, localY - (-200f + 22f)) < 32f
}
