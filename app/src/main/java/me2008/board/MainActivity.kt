package me2008.board

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import me2008.board.model.*
import me2008.board.ui.*
import me2008.board.ui.theme.BoardTheme
import me2008.board.viewmodel.BoardViewModel
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: BoardViewModel = viewModel()
                    SmartBoardApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SmartBoardApp(viewModel: BoardViewModel) {
    val currentPage = viewModel.currentPage

    // Auto-fade laser points every 100ms
    LaunchedEffect(viewModel.laserPoints.size) {
        if (viewModel.laserPoints.isNotEmpty()) {
            delay(100L)
            val now = System.currentTimeMillis()
            viewModel.laserPoints.removeAll { now - it.timestamp > 1200L }
        }
    }

    // Active in-progress stroke & shape preview
    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }
    var shapeDragStart by remember { mutableStateOf<Offset?>(null) }
    var previewShape by remember { mutableStateOf<DrawnShapeItem?>(null) }

    // Consistent live stroke width
    val liveStrokeWidth = if (viewModel.currentTool == ToolMode.HIGHLIGHTER) viewModel.strokeSize * 2.2f else viewModel.strokeSize
    val isHighlighterMode = viewModel.currentTool == ToolMode.HIGHLIGHTER

    // Helper: draw straight line along the ruler measurement edge
    fun drawLineAlongRuler() {
        val rad = Math.toRadians(viewModel.rulerAngle.toDouble())
        val halfLen = 220f
        val perpX = (36f * sin(rad)).toFloat()
        val perpY = (-36f * cos(rad)).toFloat()

        val p1 = Offset(
            (viewModel.rulerOffset.x - halfLen * cos(rad)).toFloat() + perpX,
            (viewModel.rulerOffset.y - halfLen * sin(rad)).toFloat() + perpY
        )
        val p2 = Offset(
            (viewModel.rulerOffset.x + halfLen * cos(rad)).toFloat() + perpX,
            (viewModel.rulerOffset.y + halfLen * sin(rad)).toFloat() + perpY
        )
        currentPage.strokes.add(
            StrokeItem(
                points = listOf(p1, p2),
                color = viewModel.selectedColor,
                strokeWidth = viewModel.strokeSize.coerceAtLeast(3f)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Drawing Canvas with Pan & Zoom support
        BoardCanvas(
            pins = currentPage.pins,
            strokes = currentPage.strokes,
            shapes = currentPage.shapes,
            circuits = currentPage.circuits,
            stickers = currentPage.stickers,
            formulas = currentPage.formulas,
            activeStroke = activeStrokePoints,
            activeStrokeColor = viewModel.selectedColor,
            activeStrokeWidth = liveStrokeWidth,
            isActiveHighlighter = isHighlighterMode,
            previewShape = previewShape,
            laserPoints = viewModel.laserPoints,
            bgTheme = currentPage.bgTheme,
            bgPattern = currentPage.bgPattern,
            isRulerVisible = viewModel.isRulerVisible,
            rulerOffset = viewModel.rulerOffset,
            rulerAngle = viewModel.rulerAngle,
            isProtractorVisible = viewModel.isProtractorVisible,
            protractorOffset = viewModel.protractorOffset,
            protractorAngle = viewModel.protractorAngle,
            toolMode = viewModel.currentTool,
            zoomScale = viewModel.zoomScale,
            panOffset = viewModel.panOffset,
            onTransform = { panChange, zoomChange ->
                viewModel.panOffset += panChange
                viewModel.zoomScale = (viewModel.zoomScale * zoomChange).coerceIn(0.4f, 4.0f)
            },
            onTap = { boardOffset ->
                when (viewModel.currentTool) {
                    ToolMode.PIN -> {
                        currentPage.pins.add(
                            PinItem(
                                position = boardOffset,
                                color = viewModel.selectedColor,
                                size = viewModel.strokeSize.coerceAtLeast(16f),
                                shape = viewModel.pinShape
                            )
                        )
                    }
                    ToolMode.CIRCUIT -> {
                        currentPage.circuits.add(
                            CircuitItem(
                                position = boardOffset,
                                type = viewModel.selectedCircuitType,
                                color = viewModel.selectedColor
                            )
                        )
                    }
                    ToolMode.FORMULA -> {
                        currentPage.formulas.add(
                            FormulaItem(
                                position = boardOffset,
                                text = viewModel.selectedFormulaType.formula,
                                color = viewModel.selectedColor,
                                fontSize = (viewModel.strokeSize * 3f).coerceIn(18f, 48f)
                            )
                        )
                    }
                    ToolMode.STICKER -> {
                        currentPage.stickers.add(
                            StickerItem(
                                position = boardOffset,
                                type = viewModel.selectedStickerType,
                                size = (viewModel.strokeSize * 3.5f).coerceIn(24f, 60f)
                            )
                        )
                    }
                    ToolMode.ERASER -> {
                        viewModel.eraseAt(boardOffset)
                    }
                    else -> {}
                }
            },
            onDragStart = { boardOffset ->
                when (viewModel.currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints = listOf(boardOffset)
                    }
                    ToolMode.STRAIGHT_LINE -> {
                        shapeDragStart = boardOffset
                        previewShape = DrawnShapeItem(
                            start = boardOffset,
                            end = boardOffset,
                            shapeType = ShapeType.LINE,
                            color = viewModel.selectedColor,
                            strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                        )
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart = boardOffset
                        previewShape = DrawnShapeItem(
                            start = boardOffset,
                            end = boardOffset,
                            shapeType = viewModel.selectedShapeType,
                            color = viewModel.selectedColor,
                            strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                        )
                    }
                    ToolMode.LASER -> {
                        viewModel.laserPoints.add(LaserPoint(boardOffset))
                    }
                    ToolMode.ERASER -> {
                        viewModel.eraseAt(boardOffset)
                    }
                    else -> {}
                }
            },
            onDrag = { currentBoardOffset ->
                when (viewModel.currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints = (activeStrokePoints ?: emptyList()) + currentBoardOffset
                    }
                    ToolMode.STRAIGHT_LINE -> {
                        shapeDragStart?.let { start ->
                            // Snap to 0, 45, 90 degrees if close
                            val dx = currentBoardOffset.x - start.x
                            val dy = currentBoardOffset.y - start.y
                            val dist = hypot(dx, dy)
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                            val snapTargets = listOf(0.0, 45.0, 90.0, 135.0, 180.0, -45.0, -90.0, -135.0, -180.0)
                            for (target in snapTargets) {
                                if (abs(angle - target) < 6.0) {
                                    angle = target
                                    break
                                }
                            }
                            val rad = Math.toRadians(angle)
                            val snappedEnd = Offset(
                                (start.x + dist * cos(rad)).toFloat(),
                                (start.y + dist * sin(rad)).toFloat()
                            )
                            previewShape = DrawnShapeItem(
                                start = start,
                                end = snappedEnd,
                                shapeType = ShapeType.LINE,
                                color = viewModel.selectedColor,
                                strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                            )
                        }
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart?.let { start ->
                            previewShape = DrawnShapeItem(
                                start = start,
                                end = currentBoardOffset,
                                shapeType = viewModel.selectedShapeType,
                                color = viewModel.selectedColor,
                                strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                            )
                        }
                    }
                    ToolMode.LASER -> {
                        viewModel.laserPoints.add(LaserPoint(currentBoardOffset))
                    }
                    ToolMode.ERASER -> {
                        viewModel.eraseAt(currentBoardOffset)
                    }
                    else -> {}
                }
            },
            onDragEnd = {
                when (viewModel.currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints?.let { pts ->
                            if (pts.size > 1) {
                                currentPage.strokes.add(
                                    StrokeItem(
                                        points = pts,
                                        color = viewModel.selectedColor,
                                        strokeWidth = liveStrokeWidth,
                                        isHighlighter = isHighlighterMode
                                    )
                                )
                            }
                        }
                        activeStrokePoints = null
                    }
                    ToolMode.STRAIGHT_LINE, ToolMode.SHAPE -> {
                        previewShape?.let { shape ->
                            currentPage.shapes.add(shape)
                        }
                        previewShape = null
                        shapeDragStart = null
                    }
                    else -> {}
                }
            },
            onRulerDrag = { dragDelta -> viewModel.rulerOffset += dragDelta },
            onRulerRotate = { newAngle -> viewModel.rulerAngle = newAngle },
            onProtractorDrag = { dragDelta -> viewModel.protractorOffset += dragDelta },
            onProtractorRotate = { newAngle -> viewModel.protractorAngle = newAngle }
        )

        // 2. Compact Top Bar with Board Name & Rotate Helpers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CastConnected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "Touch Board",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick rotation helper icons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (viewModel.isRulerVisible) {
                    FilledTonalIconButton(
                        onClick = { viewModel.rulerAngle = (viewModel.rulerAngle + 45f) % 360f },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate Ruler", modifier = Modifier.size(15.dp))
                    }
                }
                if (viewModel.isProtractorVisible) {
                    FilledTonalIconButton(
                        onClick = { viewModel.protractorAngle = (viewModel.protractorAngle + 45f) % 360f },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rotate Protractor", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        // 3. Unified Bottom Bar (Pages next to tools in one sleek row, auto-updating styles)
        UnifiedFloatingBottomBar(
            currentPage = viewModel.currentPageIndex,
            totalPages = viewModel.pages.size,
            onPreviousPage = { viewModel.previousPage() },
            onNextPage = { viewModel.nextPage() },
            onAddPage = { viewModel.addPage() },
            currentTool = viewModel.currentTool,
            onToolSelected = { viewModel.currentTool = it },
            selectedColor = viewModel.selectedColor,
            onColorSelected = { viewModel.selectedColor = it },
            strokeSize = viewModel.strokeSize,
            onStrokeSizeChange = { viewModel.strokeSize = it },
            pinShape = viewModel.pinShape,
            onPinShapeChange = { viewModel.pinShape = it },
            selectedShapeType = viewModel.selectedShapeType,
            onShapeTypeChange = { viewModel.selectedShapeType = it },
            selectedCircuitType = viewModel.selectedCircuitType,
            onCircuitTypeChange = { viewModel.selectedCircuitType = it },
            selectedStickerType = viewModel.selectedStickerType,
            onStickerTypeChange = { viewModel.selectedStickerType = it },
            selectedFormulaType = viewModel.selectedFormulaType,
            onFormulaTypeChange = { viewModel.selectedFormulaType = it },
            isRulerActive = viewModel.isRulerVisible,
            onToggleRuler = { viewModel.isRulerVisible = !viewModel.isRulerVisible },
            onDrawLineAlongRuler = { drawLineAlongRuler() },
            isProtractorActive = viewModel.isProtractorVisible,
            onToggleProtractor = { viewModel.isProtractorVisible = !viewModel.isProtractorVisible },
            bgTheme = currentPage.bgTheme,
            onBgThemeChange = { currentPage.bgTheme = it },
            bgPattern = currentPage.bgPattern,
            onBgPatternChange = { currentPage.bgPattern = it },
            zoomScale = viewModel.zoomScale,
            onResetZoom = { viewModel.resetZoom() },
            onUndo = { viewModel.performUndo() },
            onClearPage = { viewModel.clearCurrentPage() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 3.dp)
        )
    }
}
