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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBoardApp() {
    // Multi-page state
    val pages = remember { mutableStateListOf(PageState()) }
    var currentPageIndex by remember { mutableStateOf(0) }
    val currentPage = pages[currentPageIndex.coerceIn(0, pages.size - 1)]

    // Tool selection state
    var currentTool by remember { mutableStateOf(ToolMode.PIN) }
    var selectedColor by remember { mutableStateOf(Color(0xFFE11D48)) }
    var pinSize by remember { mutableStateOf(36f) }
    var pinShape by remember { mutableStateOf(PinShape.CIRCLE) }
    var selectedShapeType by remember { mutableStateOf(ShapeType.LINE) }
    var selectedCircuitType by remember { mutableStateOf(CircuitElementType.RESISTOR) }

    // Interactive Ruler state
    var isRulerVisible by remember { mutableStateOf(false) }
    var rulerOffset by remember { mutableStateOf(Offset(400f, 600f)) }
    var rulerAngle by remember { mutableStateOf(0f) }

    // Drawing in progress
    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }
    var shapeDragStart by remember { mutableStateOf<Offset?>(null) }
    var previewShape by remember { mutableStateOf<DrawnShapeItem?>(null) }

    // Dialog for Background customization
    var showBgDialog by remember { mutableStateOf(false) }

    // Undo action
    fun performUndo() {
        if (currentPage.circuits.isNotEmpty()) {
            currentPage.circuits.removeAt(currentPage.circuits.size - 1)
        } else if (currentPage.shapes.isNotEmpty()) {
            currentPage.shapes.removeAt(currentPage.shapes.size - 1)
        } else if (currentPage.strokes.isNotEmpty()) {
            currentPage.strokes.removeAt(currentPage.strokes.size - 1)
        } else if (currentPage.pins.isNotEmpty()) {
            currentPage.pins.removeAt(currentPage.pins.size - 1)
        }
    }

    // Eraser helper
    fun eraseAt(point: Offset, radius: Float = 40f) {
        currentPage.pins.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (it.size + radius) }
        currentPage.circuits.removeAll { hypot(it.position.x - point.x, it.position.y - point.y) < (60f + radius) }
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
            activeStroke = activeStrokePoints,
            previewShape = previewShape,
            bgTheme = currentPage.bgTheme,
            bgPattern = currentPage.bgPattern,
            isRulerVisible = isRulerVisible,
            rulerOffset = rulerOffset,
            rulerAngle = rulerAngle,
            toolMode = currentTool,
            onTap = { offset ->
                when (currentTool) {
                    ToolMode.PIN -> {
                        currentPage.pins.add(
                            PinItem(
                                position = offset,
                                color = selectedColor,
                                size = pinSize,
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
                    ToolMode.ERASER -> {
                        eraseAt(offset)
                    }
                    ToolMode.RULER -> {
                        rulerAngle = (rulerAngle + 15f) % 360f
                    }
                    else -> {}
                }
            },
            onDragStart = { offset ->
                when (currentTool) {
                    ToolMode.PEN -> {
                        activeStrokePoints = listOf(offset)
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart = offset
                        previewShape = DrawnShapeItem(
                            start = offset,
                            end = offset,
                            shapeType = selectedShapeType,
                            color = selectedColor,
                            strokeWidth = pinSize.coerceIn(2f, 30f)
                        )
                    }
                    ToolMode.ERASER -> {
                        eraseAt(offset)
                    }
                    else -> {}
                }
            },
            onDrag = { currentOffset ->
                when (currentTool) {
                    ToolMode.PEN -> {
                        activeStrokePoints = (activeStrokePoints ?: emptyList()) + currentOffset
                    }
                    ToolMode.SHAPE -> {
                        shapeDragStart?.let { start ->
                            previewShape = DrawnShapeItem(
                                start = start,
                                end = currentOffset,
                                shapeType = selectedShapeType,
                                color = selectedColor,
                                strokeWidth = pinSize.coerceIn(2f, 30f)
                            )
                        }
                    }
                    ToolMode.ERASER -> {
                        eraseAt(currentOffset)
                    }
                    else -> {}
                }
            },
            onDragEnd = {
                when (currentTool) {
                    ToolMode.PEN -> {
                        activeStrokePoints?.let { pts ->
                            if (pts.size > 1) {
                                currentPage.strokes.add(
                                    StrokeItem(
                                        points = pts,
                                        color = selectedColor,
                                        strokeWidth = pinSize.coerceIn(2f, 40f)
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
            onRulerDrag = { dragDelta ->
                rulerOffset += dragDelta
            }
        )

        // 2. Top Header Bar with Board Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CastConnected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Touch Pin Board",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Quick Actions: Ruler Toggle, Rotate Ruler, Theme
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRulerVisible) {
                    FilledTonalIconButton(onClick = { rulerAngle = (rulerAngle + 45f) % 360f }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate Ruler")
                    }
                }
                FilledTonalIconButton(onClick = { showBgDialog = true }) {
                    Icon(Icons.Default.DashboardCustomize, contentDescription = "Background Settings")
                }
            }
        }

        // 3. Floating Bottom Controls: Tool Palette & Page Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Tool Palette
            FloatingToolBar(
                currentTool = currentTool,
                onToolSelected = { currentTool = it },
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                pinSize = pinSize,
                onPinSizeChange = { pinSize = it },
                pinShape = pinShape,
                onPinShapeChange = { pinShape = it },
                selectedShapeType = selectedShapeType,
                onShapeTypeChange = { selectedShapeType = it },
                selectedCircuitType = selectedCircuitType,
                onCircuitTypeChange = { selectedCircuitType = it },
                isRulerActive = isRulerVisible,
                onToggleRuler = { isRulerVisible = !isRulerVisible },
                bgTheme = currentPage.bgTheme,
                onBgThemeChange = { currentPage.bgTheme = it },
                bgPattern = currentPage.bgPattern,
                onBgPatternChange = { currentPage.bgPattern = it },
                onUndo = { performUndo() },
                onClearPage = {
                    currentPage.pins.clear()
                    currentPage.strokes.clear()
                    currentPage.shapes.clear()
                    currentPage.circuits.clear()
                }
            )

            Spacer(Modifier.height(4.dp))

            // Floating Pages Navigation Panel with 3 Main Buttons
            PageBar(
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
                onDeleteCurrentPage = {
                    if (pages.size > 1) {
                        pages.removeAt(currentPageIndex)
                        if (currentPageIndex >= pages.size) {
                            currentPageIndex = pages.size - 1
                        }
                    }
                },
                onOpenBgSettings = { showBgDialog = true }
            )
        }

        // Background & Theme Settings Dialog
        if (showBgDialog) {
            AlertDialog(
                onDismissRequest = { showBgDialog = false },
                title = { Text("Board Background Style") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Color Theme:", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BackgroundColorTheme.values().forEach { theme ->
                                FilterChip(
                                    selected = currentPage.bgTheme == theme,
                                    onClick = { currentPage.bgTheme = theme },
                                    label = { Text(theme.displayName) }
                                )
                            }
                        }

                        Text("Grid / Pattern:", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BackgroundPattern.values().forEach { pattern ->
                                FilterChip(
                                    selected = currentPage.bgPattern == pattern,
                                    onClick = { currentPage.bgPattern = pattern },
                                    label = { Text(pattern.displayName) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBgDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
