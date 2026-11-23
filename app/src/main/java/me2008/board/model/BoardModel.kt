package me2008.board.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ToolMode(val label: String) {
    PEN("Pen"),
    HIGHLIGHTER("Marker"),
    STRAIGHT_LINE("Line"),
    PIN("Pin"),
    SHAPE("Shapes"),
    CIRCUIT("Physics"),
    FORMULA("Math"),
    STICKER("Stamps"),
    RULER("Ruler"),
    PROTRACTOR("Angle"),
    BACKGROUND("Style"),
    LASER("Laser"),
    ERASER("Eraser")
}

enum class PinShape(val label: String) {
    CIRCLE("Circle"),
    SQUARE("Square"),
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

enum class FormulaType(val formula: String, val title: String) {
    OHM("V = I · R", "Ohm's Law"),
    NEWTON("F = m · a", "Newton 2nd"),
    EINSTEIN("E = m · c²", "Mass-Energy"),
    POWER("P = V · I", "Elec. Power"),
    PYTHAGORAS("a² + b² = c²", "Pythagoras"),
    VELOCITY("v = d / t", "Velocity"),
    KINETIC("Ek = ½ · m · v²", "Kinetic E"),
    QUADRATIC("x = (-b ± √Δ) / 2a", "Quadratic")
}

enum class BackgroundColorTheme(
    val displayName: String,
    val color: Color,
    val gridColor: Color,
    val textColor: Color
) {
    BLACK("Pure Black", Color(0xFF000000), Color(0xFF262626), Color(0xFFFFFFFF)),
    CHALKBOARD("Chalkboard", Color(0xFF18181B), Color(0xFF27272A), Color(0xFFF4F4F5)),
    WHITE("Whiteboard", Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF09090B)),
    GREEN("Chalk Green", Color(0xFF0F3D24), Color(0xFF14532D), Color(0xFFF0FDF4)),
    BLUEPRINT("Blueprint", Color(0xFF0C2461), Color(0xFF1E3799), Color(0xFFEFF6FF)),
    WARM("Cream Paper", Color(0xFFFAF5EF), Color(0xFFE8DFC8), Color(0xFF451A03))
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

data class FormulaItem(
    val id: Long = System.nanoTime(),
    val position: Offset,
    val text: String,
    val color: Color,
    val fontSize: Float = 22f
)

data class LaserPoint(
    val position: Offset,
    val timestamp: Long = System.currentTimeMillis()
)
