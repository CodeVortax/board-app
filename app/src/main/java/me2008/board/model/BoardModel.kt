package me2008.board.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ToolMode(val label: String) {
    PIN("Pin"),
    PEN("Pen"),
    SHAPE("Shape"),
    CIRCUIT("Circuit"),
    RULER("Ruler"),
    ERASER("Eraser")
}

enum class PinShape(val label: String) {
    CIRCLE("Circle"),
    SQUARE("Square"),
    DIAMOND("Diamond"),
    CROSS("Cross")
}

enum class ShapeType(val label: String) {
    LINE("Line"),
    ARROW("Arrow"),
    RECTANGLE("Rectangle"),
    CIRCLE("Circle")
}

enum class CircuitElementType(val label: String, val symbol: String) {
    RESISTOR("Resistor", "R (1kΩ)"),
    BATTERY("Battery", "DC (9V)"),
    BULB("Lamp / Bulb", "💡"),
    SWITCH("Switch", "—/ —"),
    CAPACITOR("Capacitor", "10µF"),
    GROUND("Ground", "⏚")
}

enum class BackgroundColorTheme(
    val displayName: String,
    val color: Color,
    val gridColor: Color,
    val textColor: Color
) {
    WHITE("Whiteboard", Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF0F172A)),
    BLACK("Blackboard", Color(0xFF0F172A), Color(0xFF334155), Color(0xFFF8FAFC)),
    GREEN("Green Chalk", Color(0xFF14532D), Color(0xFF166534), Color(0xFFF0FDF4)),
    BLUEPRINT("Blueprint", Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFFEFF6FF)),
    WARM("Cream Paper", Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFF78350F))
}

enum class BackgroundPattern(val displayName: String) {
    PLAIN("Plain"),
    GRID("Math Grid"),
    DOTS("Dots"),
    LINES("Ruled Lines")
}

data class PinItem(
    val id: Long = System.nanoTime(),
    val position: Offset,
    val color: Color,
    val size: Float,
    val shape: PinShape
)

data class StrokeItem(
    val id: Long = System.nanoTime(),
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class DrawnShapeItem(
    val id: Long = System.nanoTime(),
    val start: Offset,
    val end: Offset,
    val shapeType: ShapeType,
    val color: Color,
    val strokeWidth: Float
)

data class CircuitItem(
    val id: Long = System.nanoTime(),
    val position: Offset,
    val type: CircuitElementType,
    val color: Color
)
