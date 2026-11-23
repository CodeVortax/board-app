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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me2008.board.model.*
import me2008.board.ui.*
import me2008.board.ui.theme.BoardTheme
import kotlin.math.hypot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartBoardApp()
                }
            }
        }
    }
}

class PageState(
    var bgTheme: BackgroundColorTheme = BackgroundColorTheme.WHITE,
    var bgPattern: BackgroundPattern = BackgroundPattern.GRID
) {
    val pins = mutableStateListOf<PinItem>()
    val strokes = mutableStateListOf<StrokeItem>()
    val shapes = mutableStateListOf<DrawnShapeItem>()
    val circuits = mutableStateListOf<CircuitItem>()
    val stickers = mutableStateListOf<StickerItem>()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBoardApp() {
    // Multi-page state
    val pages = remember { mutableStateListOf(PageState()) }
    var currentPageIndex by remember { mutableStateOf(0) }
    val currentPage = pages[currentPageIndex.coerceIn(0, pages.size - 1)]

    // Tool selection state
    var currentTool by remember { mutableStateOf(ToolMode.PEN) }
    var selectedColor by remember { mutableStateOf(Color(0xFFE11D48)) }
    var strokeSize by remember { mutableStateOf(6f) }
    var pinShape by remember { mutableStateOf(PinShape.CIRCLE) }
    var selectedShapeType by remember { mutableStateOf(ShapeType.LINE) }
    var selectedCircuitType by remember { mutableStateOf(CircuitElementType.RESISTOR) }
    var selectedStickerType by remember { mutableStateOf(StickerType.STAR) }

    // Math measurement tools state
    var isRulerVisible by remember { mutableStateOf(false) }
    var rulerOffset by remember { mutableStateOf(Offset(350f, 300f)) }
    var rulerAngle by remember { mutableStateOf(0f) }

    var isProtractorVisible by remember { mutableStateOf(false) }
    var protractorOffset by remember { mutableStateOf(Offset(750f, 300f)) }
    var protractorAngle by remember { mutableStateOf(0f) }

    // Laser pointer active points
    val laserPoints = remember { mutableStateListOf<LaserPoint>() }

    // Auto-fade laser points
    LaunchedEffect(laserPoints.size) {
        if (laserPoints.isNotEmpty()) {
            delay(100L)
            val now = System.currentTimeMillis()
            laserPoints.removeAll { now - it.timestamp > 1200L }
        }
    }

    // Active in-progress drawing
    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }
    var shapeDragStart by remember { mutableStateOf<Offset?>(null) }
    var previewShape by remember { mutableStateOf<DrawnShapeItem?>(null) }

    // Background style dialog
    var showBgDialog by remember { mutableStateOf(false) }

    // Calculate live stroke width (consistent during drawing and after release)
    val liveStrokeWidth = if (currentTool == ToolMode.HIGHLIGHTER) strokeSize * 2.2f else strokeSize
    val isHighlighterMode = currentTool == ToolMode.HIGHLIGHTER

    // Undo action
    fun performUndo() {
        when {
            currentPage.stickers.isNotEmpty() -> currentPage.stickers.removeAt(currentPage.stickers.size - 1)
            currentPage.circuits.isNotEmpty() -> currentPage.circuits.removeAt(currentPage.circuits.size - 1)
            currentPage.shapes.isNotEmpty() -> currentPage.shapes.removeAt(currentPage.shapes.size - 1)
            currentPage.strokes.isNotEmpty() -> currentPage.strokes.removeAt(currentPage.strokes.size - 1)
            currentPage.pins.isNotEmpty() -> currentPage.pins.removeAt(currentPage.pins.size - 1)
        }
    }

    // Eraser helper
    fun eraseAt(point: Offset, radius: Float = 36f) {
        currentPage.pins.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (it.size / 2f + radius) }
        currentPage.stickers.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (it.size + radius) }
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

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Drawing Canvas
        BoardCanvas(
            pins = currentPage.pins,
            strokes = currentPage.strokes,
            shapes = currentPage.shapes,
            circuits = currentPage.circuits,
            stickers = currentPage.stickers,
            activeStroke = activeStrokePoints,
            activeStrokeColor = selectedColor,
            activeStrokeWidth = liveStrokeWidth,
            isActiveHighlighter = isHighlighterMode,
            previewShape = previewShape,
            laserPoints = laserPoints,
            bgTheme = currentPage.bgTheme,
            bgPattern = currentPage.bgPattern,
            isRulerVisible = isRulerVisible,
            rulerOffset = rulerOffset,
            rulerAngle = rulerAngle,
            isProtractorVisible = isProtractorVisible,
            protractorOffset = protractorOffset,
            protractorAngle = protractorAngle,
            toolMode = currentTool,
            onTap = { offset ->
                when (currentTool) {
                    ToolMode.PIN -> {
                        currentPage.pins.add(
                            PinItem(
                                position = offset,
                                color = selectedColor,
                                size = strokeSize.coerceAtLeast(16f),
                                shape = pinShape
                            )
                        )
                    }
                    ToolMode.CIRCUIT -> {
                        currentPage.circuits.add(
                            CircuitItem(
                                position = offset,
                                type = selectedCircuitType,
                                color = selectedColor
                            )
                        )
                    }
                    ToolMode.STICKER -> {
                        currentPage.stickers.add(
                            StickerItem(
                                position = offset,
                                type = selectedStickerType,
                                size = strokeSize.coerceIn(24f, 64f)
                            )
                        )
                    }
                    ToolMode.ERASER -> {
                        eraseAt(offset)
                    }
                    else -> {}
                }
            },
            onDragStart = { offset ->
                when (currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints = listOf(offset)
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart = offset
                        previewShape = DrawnShapeItem(
                            start = offset,
                            end = offset,
                            shapeType = selectedShapeType,
                            color = selectedColor,
                            strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                        )
                    }
                    ToolMode.LASER -> {
                        laserPoints.add(LaserPoint(offset))
                    }
                    ToolMode.ERASER -> {
                        eraseAt(offset)
                    }
                    else -> {}
                }
            },
            onDrag = { currentOffset ->
                when (currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints = (activeStrokePoints ?: emptyList()) + currentOffset
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart?.let { start ->
                            previewShape = DrawnShapeItem(
                                start = start,
                                end = currentOffset,
                                shapeType = selectedShapeType,
                                color = selectedColor,
                                strokeWidth = liveStrokeWidth.coerceIn(2f, 24f)
                            )
                        }
                    }
                    ToolMode.LASER -> {
                        laserPoints.add(LaserPoint(currentOffset))
                    }
                    ToolMode.ERASER -> {
                        eraseAt(currentOffset)
                    }
                    else -> {}
                }
            },
            onDragEnd = {
                when (currentTool) {
                    ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                        activeStrokePoints?.let { pts ->
                            if (pts.size > 1) {
                                currentPage.strokes.add(
                                    StrokeItem(
                                        points = pts,
                                        color = selectedColor,
                                        strokeWidth = liveStrokeWidth,
                                        isHighlighter = isHighlighterMode
                                    )
                                )
                            }
                        }
                        activeStrokePoints = null
                    }
                    ToolMode.SHAPE -> {
                        previewShape?.let { shape ->
                            currentPage.shapes.add(shape)
                        }
                        previewShape = null
                        shapeDragStart = null
                    }
                    else -> {}
                }
            },
            onRulerDrag = { dragDelta -> rulerOffset += dragDelta },
            onRulerRotate = { newAngle -> rulerAngle = newAngle },
            onProtractorDrag = { dragDelta -> protractorOffset += dragDelta },
            onProtractorRotate = { newAngle -> protractorAngle = newAngle }
        )

        // 2. Ultra-Compact Top Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CastConnected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Touch Board",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick ruler angle / clear indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isRulerVisible) {
                    FilledTonalIconButton(
                        onClick = { rulerAngle = (rulerAngle + 45f) % 360f },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate Ruler", modifier = Modifier.size(16.dp))
                    }
                }
                if (isProtractorVisible) {
                    FilledTonalIconButton(
                        onClick = { protractorAngle = (protractorAngle + 45f) % 360f },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rotate Protractor", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 3. Unified Bottom Bar (Pages next to tools in one compact row)
        UnifiedFloatingBottomBar(
            currentPage = currentPageIndex,
            totalPages = pages.size,
            onPreviousPage = {
                if (currentPageIndex > 0) currentPageIndex--
            },
            onNextPage = {
                if (currentPageIndex < pages.size - 1) currentPageIndex++
            },
            onAddPage = {
                pages.add(PageState(bgTheme = currentPage.bgTheme, bgPattern = currentPage.bgPattern))
                currentPageIndex = pages.size - 1
            },
            currentTool = currentTool,
            onToolSelected = { currentTool = it },
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            strokeSize = strokeSize,
            onStrokeSizeChange = { strokeSize = it },
            pinShape = pinShape,
            onPinShapeChange = { pinShape = it },
            selectedShapeType = selectedShapeType,
            onShapeTypeChange = { selectedShapeType = it },
            selectedCircuitType = selectedCircuitType,
            onCircuitTypeChange = { selectedCircuitType = it },
            selectedStickerType = selectedStickerType,
            onStickerTypeChange = { selectedStickerType = it },
            isRulerActive = isRulerVisible,
            onToggleRuler = { isRulerVisible = !isRulerVisible },
            isProtractorActive = isProtractorVisible,
            onToggleProtractor = { isProtractorVisible = !isProtractorVisible },
            onOpenBgSettings = { showBgDialog = true },
            onUndo = { performUndo() },
            onClearPage = {
                currentPage.pins.clear()
                currentPage.strokes.clear()
                currentPage.shapes.clear()
                currentPage.circuits.clear()
                currentPage.stickers.clear()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 4.dp)
        )

        // Compact Background & Pattern Settings Dialog
        if (showBgDialog) {
            AlertDialog(
                onDismissRequest = { showBgDialog = false },
                title = { Text("Board Style", fontSize = 15.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Theme:", fontSize = 12.sp, style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BackgroundColorTheme.values().forEach { theme ->
                                FilterChip(
                                    selected = currentPage.bgTheme == theme,
                                    onClick = { currentPage.bgTheme = theme },
                                    label = { Text(theme.displayName, fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }

                        Text("Grid / Pattern:", fontSize = 12.sp, style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BackgroundPattern.values().forEach { pattern ->
                                FilterChip(
                                    selected = currentPage.bgPattern == pattern,
                                    onClick = { currentPage.bgPattern = pattern },
                                    label = { Text(pattern.displayName, fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBgDialog = false }) {
                        Text("Done", fontSize = 12.sp)
                    }
                }
            )
        }
    }
}
