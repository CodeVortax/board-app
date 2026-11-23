package me2008.board.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import me2008.board.model.*
import kotlin.math.*

@Composable
fun BoardCanvas(
    pins: List<PinItem>,
    strokes: List<StrokeItem>,
    shapes: List<DrawnShapeItem>,
    circuits: List<CircuitItem>,
    activeStroke: List<Offset>?,
    previewShape: DrawnShapeItem?,
    bgTheme: BackgroundColorTheme,
    bgPattern: BackgroundPattern,
    isRulerVisible: Boolean,
    rulerOffset: Offset,
    rulerAngle: Float,
    toolMode: ToolMode,
    onTap: (Offset) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRulerDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(toolMode, isRulerVisible) {
                detectTapGestures { offset ->
                    onTap(offset)
                }
            }
            .pointerInput(toolMode, isRulerVisible) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (isRulerVisible && isNearRuler(offset, rulerOffset, rulerAngle)) {
                            // ruler drag handled
                        }
                        onDragStart(offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (isRulerVisible && toolMode == ToolMode.RULER) {
                            onRulerDrag(dragAmount)
                        } else {
                            onDrag(change.position)
                        }
                    },
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            }
    ) {
        // 1. Draw Background Color
        drawRect(color = bgTheme.color)

        // 2. Draw Background Pattern
        drawBackgroundPattern(bgPattern, bgTheme.gridColor)

        // 3. Draw Completed Strokes
        strokes.forEach { stroke ->
            drawStrokeItem(stroke)
        }

        // Active Freehand Stroke in progress
        activeStroke?.let { pts ->
            if (pts.size > 1) {
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.Red,
                    style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // 4. Draw Shapes
        shapes.forEach { shape ->
            drawShapeItem(shape)
        }

        // Preview Shape in progress
        previewShape?.let { shape ->
            drawShapeItem(shape)
        }

        // 5. Draw Pins
        pins.forEach { pin ->
            drawPinItem(pin)
        }

        // 6. Draw Circuit Components
        circuits.forEach { circuit ->
            drawCircuitElement(circuit)
        }

        // 7. Draw Math Ruler if visible
        if (isRulerVisible) {
            drawInteractiveRuler(rulerOffset, rulerAngle, bgTheme.textColor)
        }
    }
}

private fun isNearRuler(point: Offset, rulerOffset: Offset, angle: Float): Boolean {
    val dx = point.x - rulerOffset.x
    val dy = point.y - rulerOffset.y
    val dist = sqrt(dx * dx + dy * dy)
    return dist < 300f
}

private fun DrawScope.drawBackgroundPattern(pattern: BackgroundPattern, color: Color) {
    val step = 48f
    when (pattern) {
        BackgroundPattern.PLAIN -> {}
        BackgroundPattern.GRID -> {
            var x = 0f
            while (x < size.width) {
                drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }
        BackgroundPattern.DOTS -> {
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(color, radius = 2.5f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        BackgroundPattern.LINES -> {
            var y = step
            while (y < size.height) {
                drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2f)
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
            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = radius * 0.4f, center = pin.position - Offset(radius * 0.2f, radius * 0.2f))
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
        PinShape.CROSS -> {
            drawLine(pin.color, Offset(pin.position.x - radius, pin.position.y - radius), Offset(pin.position.x + radius, pin.position.y + radius), strokeWidth = radius * 0.6f, cap = StrokeCap.Round)
            drawLine(pin.color, Offset(pin.position.x + radius, pin.position.y - radius), Offset(pin.position.x - radius, pin.position.y + radius), strokeWidth = radius * 0.6f, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawStrokeItem(stroke: StrokeItem) {
    if (stroke.points.size < 2) return
    val path = Path().apply {
        moveTo(stroke.points[0].x, stroke.points[0].y)
        for (i in 1 until stroke.points.size) {
            lineTo(stroke.points[i].x, stroke.points[i].y)
        }
    }
    drawPath(
        path = path,
        color = stroke.color,
        style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawShapeItem(shape: DrawnShapeItem) {
    when (shape.shapeType) {
        ShapeType.LINE -> {
            drawLine(
                color = shape.color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )
        }
        ShapeType.ARROW -> {
            drawLine(
                color = shape.color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrowhead calculations
            val dx = shape.end.x - shape.start.x
            val dy = shape.end.y - shape.start.y
            val angle = atan2(dy, dx)
            val arrowLength = (shape.strokeWidth * 4f).coerceAtLeast(24f)
            val arrowAngle = Math.PI / 6.0
            val p1 = Offset(
                (shape.end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
                (shape.end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
            )
            val p2 = Offset(
                (shape.end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
                (shape.end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
            )
            val path = Path().apply {
                moveTo(shape.end.x, shape.end.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }
            drawPath(path, color = shape.color)
        }
        ShapeType.RECTANGLE -> {
            val left = min(shape.start.x, shape.end.x)
            val top = min(shape.start.y, shape.end.y)
            val width = abs(shape.end.x - shape.start.x)
            val height = abs(shape.end.y - shape.start.y)
            drawRect(
                color = shape.color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = shape.strokeWidth)
            )
        }
        ShapeType.CIRCLE -> {
            val left = min(shape.start.x, shape.end.x)
            val top = min(shape.start.y, shape.end.y)
            val width = abs(shape.end.x - shape.start.x)
            val height = abs(shape.end.y - shape.start.y)
            drawOval(
                color = shape.color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = shape.strokeWidth)
            )
        }
    }
}

private fun DrawScope.drawCircuitElement(circuit: CircuitItem) {
    val x = circuit.position.x
    val y = circuit.position.y
    val c = circuit.color
    val stroke = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (circuit.type) {
        CircuitElementType.RESISTOR -> {
            // Lead in and lead out with standard zigzag in center
            drawLine(c, Offset(x - 60f, y), Offset(x - 30f, y), strokeWidth = 4f)
            val path = Path().apply {
                moveTo(x - 30f, y)
                lineTo(x - 20f, y - 18f)
                lineTo(x - 10f, y + 18f)
                lineTo(x, y - 18f)
                lineTo(x + 10f, y + 18f)
                lineTo(x + 20f, y - 18f)
                lineTo(x + 30f, y)
            }
            drawPath(path, color = c, style = stroke)
            drawLine(c, Offset(x + 30f, y), Offset(x + 60f, y), strokeWidth = 4f)
        }
        CircuitElementType.BATTERY -> {
            // Left lead wire
            drawLine(c, Offset(x - 50f, y), Offset(x - 12f, y), strokeWidth = 4f)
            // Positive long plate
            drawLine(c, Offset(x - 12f, y - 30f), Offset(x - 12f, y + 30f), strokeWidth = 4f, cap = StrokeCap.Square)
            // Negative short thick plate
            drawLine(c, Offset(x + 12f, y - 18f), Offset(x + 12f, y + 18f), strokeWidth = 8f, cap = StrokeCap.Square)
            // Right lead wire
            drawLine(c, Offset(x + 12f, y), Offset(x + 50f, y), strokeWidth = 4f)
        }
        CircuitElementType.BULB -> {
            drawLine(c, Offset(x - 55f, y), Offset(x - 25f, y), strokeWidth = 4f)
            drawCircle(color = c, radius = 25f, center = Offset(x, y), style = Stroke(4f))
            drawLine(c, Offset(x - 16f, y - 16f), Offset(x + 16f, y + 16f), strokeWidth = 3f)
            drawLine(c, Offset(x + 16f, y - 16f), Offset(x - 16f, y + 16f), strokeWidth = 3f)
            drawLine(c, Offset(x + 25f, y), Offset(x + 55f, y), strokeWidth = 4f)
        }
        CircuitElementType.SWITCH -> {
            drawLine(c, Offset(x - 50f, y), Offset(x - 20f, y), strokeWidth = 4f)
            drawCircle(c, radius = 5f, center = Offset(x - 20f, y), style = Stroke(3f))
            // Angled open switch arm
            drawLine(c, Offset(x - 18f, y), Offset(x + 15f, y - 22f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawCircle(c, radius = 5f, center = Offset(x + 20f, y), style = Stroke(3f))
            drawLine(c, Offset(x + 20f, y), Offset(x + 50f, y), strokeWidth = 4f)
        }
        CircuitElementType.CAPACITOR -> {
            drawLine(c, Offset(x - 50f, y), Offset(x - 8f, y), strokeWidth = 4f)
            drawLine(c, Offset(x - 8f, y - 26f), Offset(x - 8f, y + 26f), strokeWidth = 5f)
            drawLine(c, Offset(x + 8f, y - 26f), Offset(x + 8f, y + 26f), strokeWidth = 5f)
            drawLine(c, Offset(x + 8f, y), Offset(x + 50f, y), strokeWidth = 4f)
        }
        CircuitElementType.GROUND -> {
            drawLine(c, Offset(x, y - 30f), Offset(x, y), strokeWidth = 4f)
            drawLine(c, Offset(x - 30f, y), Offset(x + 30f, y), strokeWidth = 4f)
            drawLine(c, Offset(x - 18f, y + 8f), Offset(x + 18f, y + 8f), strokeWidth = 4f)
            drawLine(c, Offset(x - 8f, y + 16f), Offset(x + 8f, y + 16f), strokeWidth = 4f)
        }
    }
}

private fun DrawScope.drawInteractiveRuler(center: Offset, angle: Float, textColor: Color) {
    val rulerWidth = 500f
    val rulerHeight = 90f

    rotate(angle, pivot = center) {
        val topLeft = Offset(center.x - rulerWidth / 2f, center.y - rulerHeight / 2f)

        // Ruler translucent body
        drawRect(
            color = Color(0xDDFFFBEB),
            topLeft = topLeft,
            size = Size(rulerWidth, rulerHeight)
        )
        drawRect(
            color = Color(0xFFD97706),
            topLeft = topLeft,
            size = Size(rulerWidth, rulerHeight),
            style = Stroke(3f)
        )

        // Draw centimeter and millimeter markings along top edge
        val numCm = 10
        val cmSpacing = (rulerWidth - 40f) / numCm
        val startX = topLeft.x + 20f
        val topY = topLeft.y

        for (cm in 0..numCm) {
            val cmX = startX + cm * cmSpacing
            // Major CM tick
            drawLine(
                color = Color(0xFFB45309),
                start = Offset(cmX, topY),
                end = Offset(cmX, topY + 28f),
                strokeWidth = 2.5f
            )

            // Millimeter ticks
            if (cm < numCm) {
                for (mm in 1..4) {
                    val mmX = cmX + mm * (cmSpacing / 10f)
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(mmX, topY),
                        end = Offset(mmX, topY + 12f),
                        strokeWidth = 1f
                    )
                }
                // 5mm half-cm tick
                val halfX = cmX + 5 * (cmSpacing / 10f)
                drawLine(
                    color = Color(0xFFB45309),
                    start = Offset(halfX, topY),
                    end = Offset(halfX, topY + 18f),
                    strokeWidth = 1.5f
                )
                for (mm in 6..9) {
                    val mmX = cmX + mm * (cmSpacing / 10f)
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(mmX, topY),
                        end = Offset(mmX, topY + 12f),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}
