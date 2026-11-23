package me2008.board.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ToolMode(val label: String) {
    PEN("Pen"),
    HIGHLIGHTER("Marker"),
    PIN("Pin"),
    SHAPE("Shapes"),
    CIRCUIT("Physics"),
    RULER("Ruler"),
    PROTRACTOR("Angle"),
    STICKER("Stamps"),
    LASER("Laser"),
    ERASER("Eraser")
}

enum class PinShape(val label: String) {
    CIRCLE("Dot"),
    SQUARE("Block"),
    DIAMOND("Diamond"),
    STAR("Star"),
    PUSHPIN("Pin")
}

enum class ShapeType(val label: String) {
    LINE("Line"),
    ARROW("Arrow"),
    DOUBLE_ARROW("2-Arrow"),
    RECTANGLE("Rect"),
    CIRCLE("Circle"),
    TRIANGLE("Triangle"),
    COORDINATE_AXIS("X-Y Axis")
}

enum class CircuitElementType(val label: String, val symbol: String) {
    RESISTOR("Resistor", "R (1kΩ)"),
    BATTERY("DC Source", "9V ⎓"),
    AC_SOURCE("AC Source", "∿ ~"),
    BULB("Lamp", "💡"),
    SWITCH("Switch", "—/ —"),
    CAPACITOR("Capacitor", "10µF"),
    INDUCTOR("Inductor", "Coil L"),
    VOLTMETER("Voltmeter", "Ⓥ"),
    AMMETER("Ammeter", "Ⓐ"),
    DIODE("Diode", "▷|"),
    GROUND("Ground", "⏚")
}

enum class StickerType(val symbol: String, val label: String) {
    STAR("⭐", "Star"),
    CHECK("✅", "Check"),
    CROSS("❌", "Cross"),
    QUESTION("❓", "Question"),
    IDEA("💡", "Idea"),
    IMPORTANT("❗", "Note")
}

enum class BackgroundColorTheme(
    val displayName: String,
    val color: Color,
    val gridColor: Color,
    val textColor: Color
) {
    WHITE("Whiteboard", Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF0F172A)),
    BLACK("Blackboard", Color(0xFF0F172A), Color(0xFF334155), Color(0xFFF8FAFC)),
    GREEN("Chalk Green", Color(0xFF14532D), Color(0xFF166534), Color(0xFFF0FDF4)),
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
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
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

data class StickerItem(
    val id: Long = System.nanoTime(),
    val position: Offset,
    val type: StickerType,
    val size: Float = 36f
)

data class LaserPoint(
    val position: Offset,
    val timestamp: Long = System.currentTimeMillis()
)
