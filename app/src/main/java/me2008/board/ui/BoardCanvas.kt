package me2008.board.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
    stickers: List<StickerItem>,
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
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(toolMode, isRulerVisible, isProtractorVisible, rulerOffset, protractorOffset) {
                detectTapGestures { offset ->
                    onTap(offset)
                }
            }
            .pointerInput(toolMode, isRulerVisible, isProtractorVisible, rulerOffset, protractorOffset) {
                var isDraggingRuler = false
                var isRotatingRuler = false
                var isDraggingProtractor = false
                var isRotatingProtractor = false

                detectDragGestures(
                    onDragStart = { offset ->
                        if (isRulerVisible && isNearRulerRotateHandle(offset, rulerOffset, rulerAngle)) {
                            isRotatingRuler = true
                        } else if (isRulerVisible && isNearRulerBody(offset, rulerOffset, rulerAngle)) {
                            isDraggingRuler = true
                        } else if (isProtractorVisible && isNearProtractorRotate(offset, protractorOffset, protractorAngle)) {
                            isRotatingProtractor = true
                        } else if (isProtractorVisible && isNearProtractorBody(offset, protractorOffset, protractorAngle)) {
                            isDraggingProtractor = true
                        } else {
                            onDragStart(offset)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        when {
                            isRotatingRuler -> {
                                val dx = change.position.x - rulerOffset.x
                                val dy = change.position.y - rulerOffset.y
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                onRulerRotate(angle)
                            }
                            isDraggingRuler -> {
                                onRulerDrag(dragAmount)
                            }
                            isRotatingProtractor -> {
                                val dx = change.position.x - protractorOffset.x
                                val dy = change.position.y - protractorOffset.y
                                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                onProtractorRotate(angle)
                            }
                            isDraggingProtractor -> {
                                onProtractorDrag(dragAmount)
                            }
                            else -> {
                                onDrag(change.position)
                            }
                        }
                    },
                    onDragEnd = {
                        isDraggingRuler = false
                        isRotatingRuler = false
                        isDraggingProtractor = false
                        isRotatingProtractor = false
                        onDragEnd()
                    }
                )
            }
    ) {
        // 1. Draw Background Color
        drawRect(color = bgTheme.color)

        // 2. Draw Background Pattern
        drawBackgroundPattern(bgPattern, bgTheme.gridColor)

        // 3. Draw Completed Strokes (with Bézier smoothing)
        strokes.forEach { stroke ->
            drawSmoothStroke(stroke)
        }

        // Active Stroke: Drawn with EXACT same color, width, and alpha
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

        // 4. Draw Shapes
        shapes.forEach { shape ->
            drawShapeItem(shape)
        }

        // Preview Shape
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

        // 7. Draw Stickers / Stamps
        stickers.forEach { sticker ->
            drawStickerItem(sticker)
        }

        // 8. Draw Laser Pointer Trail
        if (laserPoints.isNotEmpty()) {
            drawLaserTrail(laserPoints)
        }

        // 9. Draw Interactive Math Ruler
        if (isRulerVisible) {
            drawInteractiveRuler(rulerOffset, rulerAngle)
        }

        // 10. Draw Interactive Math Protractor
        if (isProtractorVisible) {
            drawInteractiveProtractor(protractorOffset, protractorAngle)
        }
    }
}

// Geometry helper for Bézier curve generation
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
            path.quadraticTo(prev.x, prev.y, midX, midY)
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

private fun DrawScope.drawBackgroundPattern(pattern: BackgroundPattern, color: Color) {
    val step = 40f
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
                    drawCircle(color, radius = 2f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        BackgroundPattern.LINES -> {
            var y = step
            while (y < size.height) {
                drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
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
            // Pushpin needle + head
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

            // X Axis
            drawLine(shape.color, Offset(left, midY), Offset(right, midY), strokeWidth = shape.strokeWidth)
            drawArrowHead(Offset(right, midY), Offset(left, midY), shape.color, shape.strokeWidth)

            // Y Axis
            drawLine(shape.color, Offset(midX, bottom), Offset(midX, top), strokeWidth = shape.strokeWidth)
            drawArrowHead(Offset(midX, top), Offset(midX, bottom), shape.color, shape.strokeWidth)

            // Ticks on X and Y
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
                quadraticTo(x - 6f, y - 10f, x, y)
                quadraticTo(x + 6f, y + 10f, x + 12f, y)
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
            // V letter inside
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
            // A letter inside
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

private fun DrawScope.drawLaserTrail(points: List<LaserPoint>) {
    val now = System.currentTimeMillis()
    val active = points.filter { now - it.timestamp < 1200L }
    if (active.isEmpty()) return

    for (i in active.indices) {
        val pt = active[i]
        val age = (now - pt.timestamp) / 1200f
        val alpha = (1f - age).coerceIn(0f, 1f)
        val radius = (12f * (1f - age * 0.5f)).coerceAtLeast(3f)

        drawCircle(
            color = Color(0xFFFF1744).copy(alpha = alpha),
            radius = radius,
            center = pt.position
        )
    }
    // Bright white core on latest point
    active.lastOrNull()?.let { last ->
        drawCircle(Color.White, radius = 5f, center = last.position)
    }
}

// Interactive Ruler
private fun DrawScope.drawInteractiveRuler(center: Offset, angle: Float) {
    val rulerWidth = 480f
    val rulerHeight = 72f

    rotate(angle, pivot = center) {
        val topLeft = Offset(center.x - rulerWidth / 2f, center.y - rulerHeight / 2f)

        // Semi-transparent acrylic ruler body
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

        // Scale markings
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
                end = Offset(cmX, topY + 22f),
                strokeWidth = 2f
            )

            // Millimeter ticks
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
                // 5mm half-cm tick
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

        // Center Drag Handle indicator
        drawCircle(
            color = Color(0xFFD97706).copy(alpha = 0.4f),
            radius = 16f,
            center = center
        )

        // Rotate Handle at right end
        val rotateHandle = Offset(topLeft.x + rulerWidth - 24f, center.y)
        drawCircle(Color(0xFFD97706), radius = 12f, center = rotateHandle)
        drawCircle(Color.White, radius = 5f, center = rotateHandle)
    }
}

// Interactive 180-degree Protractor
private fun DrawScope.drawInteractiveProtractor(center: Offset, angle: Float) {
    val radius = 200f
    rotate(angle, pivot = center) {
        // Semi-circle background
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

        // Baseline
        drawLine(
            Color(0xFF0284C7),
            Offset(center.x - radius, center.y),
            Offset(center.x + radius, center.y),
            strokeWidth = 2f
        )

        // Degree ticks around perimeter every 10 degrees
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

        // Center origin mark
        drawCircle(Color(0xFF0284C7), radius = 6f, center = center)
        drawCircle(Color.White, radius = 2f, center = center)

        // Rotate handle at top apex (90 degrees)
        val apex = Offset(center.x, center.y - radius + 22f)
        drawCircle(Color(0xFF0284C7), radius = 10f, center = apex)
        drawCircle(Color.White, radius = 4f, center = apex)
    }
}

private fun isNearRulerBody(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val rad = -Math.toRadians(angle.toDouble())
    val localX = dx * cos(rad) - dy * sin(rad)
    val localY = dx * sin(rad) + dy * cos(rad)
    return abs(localX) <= 240f && abs(localY) <= 36f
}

private fun isNearRulerRotateHandle(pt: Offset, center: Offset, angle: Float): Boolean {
    val dx = pt.x - center.x
    val dy = pt.y - center.y
    val rad = -Math.toRadians(angle.toDouble())
    val localX = dx * cos(rad) - dy * sin(rad)
    val localY = dx * sin(rad) + dy * cos(rad)
    return hypot(localX - (240f - 24f), localY) < 32f
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
    return hypot(localX, localY - (-200f + 22f)) < 30f
}
