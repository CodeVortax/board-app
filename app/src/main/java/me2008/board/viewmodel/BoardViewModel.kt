package me2008.board.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import me2008.board.model.*
import kotlin.math.hypot

class PageState(
    var bgTheme: BackgroundColorTheme = BackgroundColorTheme.BLACK,
    var bgPattern: BackgroundPattern = BackgroundPattern.GRID
) {
    val pins = mutableStateListOf<PinItem>()
    val strokes = mutableStateListOf<StrokeItem>()
    val shapes = mutableStateListOf<DrawnShapeItem>()
    val circuits = mutableStateListOf<CircuitItem>()
    val stickers = mutableStateListOf<StickerItem>()
    val formulas = mutableStateListOf<FormulaItem>()
}

class BoardViewModel : ViewModel() {
    val pages = mutableStateListOf(PageState())
    var currentPageIndex by mutableStateOf(0)

    val currentPage: PageState
        get() = pages[currentPageIndex.coerceIn(0, pages.size - 1)]

    // Active tool state
    var currentTool by mutableStateOf(ToolMode.PEN)
    var selectedColor by mutableStateOf(Color(0xFFFFFFFF)) // Pure White on Black board by default
    var strokeSize by mutableStateOf(6f)
    var pinShape by mutableStateOf(PinShape.CIRCLE)
    var selectedShapeType by mutableStateOf(ShapeType.LINE)
    var selectedCircuitType by mutableStateOf(CircuitElementType.RESISTOR)
    var selectedStickerType by mutableStateOf(StickerType.STAR)
    var selectedFormulaType by mutableStateOf(FormulaType.OHM)

    // Ruler & Protractor
    var isRulerVisible by mutableStateOf(false)
    var rulerOffset by mutableStateOf(Offset(400f, 350f))
    var rulerAngle by mutableStateOf(0f)

    var isProtractorVisible by mutableStateOf(false)
    var protractorOffset by mutableStateOf(Offset(800f, 350f))
    var protractorAngle by mutableStateOf(0f)

    // Zoom & Pan state
    var zoomScale by mutableStateOf(1.0f)
    var panOffset by mutableStateOf(Offset.Zero)

    // Laser trail
    val laserPoints = mutableStateListOf<LaserPoint>()

    fun resetZoom() {
        zoomScale = 1.0f
        panOffset = Offset.Zero
    }

    fun addPage() {
        pages.add(PageState(bgTheme = currentPage.bgTheme, bgPattern = currentPage.bgPattern))
        currentPageIndex = pages.size - 1
    }

    fun previousPage() {
        if (currentPageIndex > 0) currentPageIndex--
    }

    fun nextPage() {
        if (currentPageIndex < pages.size - 1) currentPageIndex++
    }

    fun deleteCurrentPage() {
        if (pages.size > 1) {
            pages.removeAt(currentPageIndex)
            if (currentPageIndex >= pages.size) {
                currentPageIndex = pages.size - 1
            }
        }
    }

    fun clearCurrentPage() {
        currentPage.pins.clear()
        currentPage.strokes.clear()
        currentPage.shapes.clear()
        currentPage.circuits.clear()
        currentPage.stickers.clear()
        currentPage.formulas.clear()
    }

    fun performUndo() {
        when {
            currentPage.formulas.isNotEmpty() -> currentPage.formulas.removeAt(currentPage.formulas.size - 1)
            currentPage.stickers.isNotEmpty() -> currentPage.stickers.removeAt(currentPage.stickers.size - 1)
            currentPage.circuits.isNotEmpty() -> currentPage.circuits.removeAt(currentPage.circuits.size - 1)
            currentPage.shapes.isNotEmpty() -> currentPage.shapes.removeAt(currentPage.shapes.size - 1)
            currentPage.strokes.isNotEmpty() -> currentPage.strokes.removeAt(currentPage.strokes.size - 1)
            currentPage.pins.isNotEmpty() -> currentPage.pins.removeAt(currentPage.pins.size - 1)
        }
    }

    fun eraseAt(point: Offset, radius: Float = 36f) {
        currentPage.pins.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (it.size / 2f + radius) }
        currentPage.stickers.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (it.size + radius) }
        currentPage.formulas.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (40f + radius) }
        currentPage.circuits.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (50f + radius) }
        currentPage.shapes.removeAll {
            val midX = (it.start.x + it.end.x) / 2f
            val midY = (it.start.y + it.end.y) / 2f
            hypot(midX - point.x, midY - point.y) < radius * 2f
        }
        currentPage.strokes.removeAll { stroke ->
            stroke.points.any { hypot(it.x - point.x, it.y - point.y) < radius }
        }
    }
}
