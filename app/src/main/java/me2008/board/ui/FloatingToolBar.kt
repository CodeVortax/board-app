package me2008.board.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me2008.board.model.*

val PaletteColors = listOf(
    Color(0xFFE11D48), // Rose / Red
    Color(0xFFEA580C), // Orange
    Color(0xFFFACC15), // Yellow
    Color(0xFF16A34A), // Green
    Color(0xFF0284C7), // Sky Blue
    Color(0xFF2563EB), // Royal Blue
    Color(0xFF7C3AED), // Purple
    Color(0xFF0F172A), // Slate Black
    Color(0xFFFFFFFF)  // White
)

@Composable
fun UnifiedFloatingBottomBar(
    // Page controls
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onAddPage: () -> Unit,
    // Tool controls
    currentTool: ToolMode,
    onToolSelected: (ToolMode) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    strokeSize: Float,
    onStrokeSizeChange: (Float) -> Unit,
    pinShape: PinShape,
    onPinShapeChange: (PinShape) -> Unit,
    selectedShapeType: ShapeType,
    onShapeTypeChange: (ShapeType) -> Unit,
    selectedCircuitType: CircuitElementType,
    onCircuitTypeChange: (CircuitElementType) -> Unit,
    selectedStickerType: StickerType,
    onStickerTypeChange: (StickerType) -> Unit,
    isRulerActive: Boolean,
    onToggleRuler: () -> Unit,
    isProtractorActive: Boolean,
    onToggleProtractor: () -> Unit,
    onOpenBgSettings: () -> Unit,
    onUndo: () -> Unit,
    onClearPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showToolPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compact Floating Popup Pill above the dock
        AnimatedVisibility(
            visible = showToolPopup,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentTool) {
                        ToolMode.PEN, ToolMode.HIGHLIGHTER -> {
                            PenSubTray(
                                isHighlighter = currentTool == ToolMode.HIGHLIGHTER,
                                size = strokeSize,
                                onSizeChange = onStrokeSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.PIN -> {
                            PinSubTray(
                                selectedShape = pinShape,
                                onShapeChange = onPinShapeChange,
                                size = strokeSize,
                                onSizeChange = onStrokeSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.SHAPE -> {
                            ShapeSubTray(
                                selectedShape = selectedShapeType,
                                onShapeChange = onShapeTypeChange,
                                strokeWidth = strokeSize,
                                onStrokeWidthChange = onStrokeSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.CIRCUIT -> {
                            CircuitSubTray(
                                selectedType = selectedCircuitType,
                                onTypeChange = onCircuitTypeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.STICKER -> {
                            StickerSubTray(
                                selectedType = selectedStickerType,
                                onTypeChange = onStickerTypeChange
                            )
                        }
                        ToolMode.RULER, ToolMode.PROTRACTOR -> {
                            MathToolsSubTray(
                                isRulerActive = isRulerActive,
                                onToggleRuler = onToggleRuler,
                                isProtractorActive = isProtractorActive,
                                onToggleProtractor = onToggleProtractor
                            )
                        }
                        ToolMode.LASER -> {
                            Text(
                                "Laser pointer leaves a temporary glowing trace for presentations",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                        ToolMode.ERASER -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Tap or drag over elements to erase", fontSize = 11.sp)
                                Button(
                                    onClick = {
                                        onClearPage()
                                        showToolPopup = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Clear All", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unified Horizontal Bottom Dock (Pages next to tools)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- 1. Compact Page Navigation (Next to tools) ---
                CompactIconButton(
                    icon = Icons.Default.NavigateBefore,
                    contentDescription = "Previous Page",
                    enabled = currentPage > 0,
                    onClick = onPreviousPage
                )

                Text(
                    text = "${currentPage + 1}/${totalPages}",
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CompactIconButton(
                    icon = Icons.Default.NavigateNext,
                    contentDescription = "Next Page",
                    enabled = currentPage < totalPages - 1,
                    onClick = onNextPage
                )

                CompactIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add Page",
                    isPrimary = true,
                    onClick = onAddPage
                )

                VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp))

                // --- 2. Main Tools (In the middle) ---
                // Pen (Freehand)
                ToolItemButton(
                    icon = Icons.Default.Edit,
                    isSelected = currentTool == ToolMode.PEN,
                    onClick = {
                        if (currentTool == ToolMode.PEN) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.PEN); showToolPopup = true }
                    }
                )

                // Highlighter
                ToolItemButton(
                    icon = Icons.Default.Brush,
                    isSelected = currentTool == ToolMode.HIGHLIGHTER,
                    onClick = {
                        if (currentTool == ToolMode.HIGHLIGHTER) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.HIGHLIGHTER); showToolPopup = true }
                    }
                )

                // Pin
                ToolItemButton(
                    icon = Icons.Default.Place,
                    isSelected = currentTool == ToolMode.PIN,
                    onClick = {
                        if (currentTool == ToolMode.PIN) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.PIN); showToolPopup = true }
                    }
                )

                // Shapes
                ToolItemButton(
                    icon = Icons.Default.Category,
                    isSelected = currentTool == ToolMode.SHAPE,
                    onClick = {
                        if (currentTool == ToolMode.SHAPE) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.SHAPE); showToolPopup = true }
                    }
                )

                // Physics Circuits
                ToolItemButton(
                    icon = Icons.Default.ElectricBolt,
                    isSelected = currentTool == ToolMode.CIRCUIT,
                    onClick = {
                        if (currentTool == ToolMode.CIRCUIT) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.CIRCUIT); showToolPopup = true }
                    }
                )

                // Ruler
                ToolItemButton(
                    icon = Icons.Default.Straighten,
                    isSelected = isRulerActive,
                    onClick = {
                        onToggleRuler()
                        onToolSelected(ToolMode.RULER)
                        showToolPopup = true
                    }
                )

                // Protractor
                ToolItemButton(
                    icon = Icons.Default.ChangeHistory,
                    isSelected = isProtractorActive,
                    onClick = {
                        onToggleProtractor()
                        onToolSelected(ToolMode.PROTRACTOR)
                        showToolPopup = true
                    }
                )

                // Stamps / Stickers
                ToolItemButton(
                    icon = Icons.Default.Star,
                    isSelected = currentTool == ToolMode.STICKER,
                    onClick = {
                        if (currentTool == ToolMode.STICKER) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.STICKER); showToolPopup = true }
                    }
                )

                // Laser Pointer
                ToolItemButton(
                    icon = Icons.Default.Highlight,
                    isSelected = currentTool == ToolMode.LASER,
                    onClick = {
                        onToolSelected(ToolMode.LASER)
                        showToolPopup = false
                    }
                )

                // Eraser
                ToolItemButton(
                    icon = Icons.Default.AutoFixHigh,
                    isSelected = currentTool == ToolMode.ERASER,
                    onClick = {
                        if (currentTool == ToolMode.ERASER) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.ERASER); showToolPopup = true }
                    }
                )

                VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp))

                // --- 3. Board Utilities ---
                // Background
                CompactIconButton(
                    icon = Icons.Default.Palette,
                    contentDescription = "Background Settings",
                    onClick = onOpenBgSettings
                )

                // Undo
                CompactIconButton(
                    icon = Icons.Default.Undo,
                    contentDescription = "Undo",
                    onClick = onUndo
                )

                // Clear
                CompactIconButton(
                    icon = Icons.Default.DeleteOutline,
                    contentDescription = "Clear Page",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onClearPage
                )
            }
        }
    }
}

@Composable
private fun CompactIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                if (isPrimary) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(17.dp),
            tint = if (!enabled) Color.Gray.copy(alpha = 0.4f)
                   else if (isPrimary) MaterialTheme.colorScheme.onPrimary
                   else tint
        )
    }
}

@Composable
private fun ToolItemButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactColorPalette(selectedColor: Color, onColorChange: (Color) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaletteColors.forEach { color ->
            val isCurrent = color == selectedColor
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 22.dp else 18.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isCurrent) 2.5.dp else 1.dp,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onColorChange(color) }
            )
        }
    }
}

@Composable
private fun PenSubTray(
    isHighlighter: Boolean,
    size: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(if (isHighlighter) "Marker:" else "Pen:", fontSize = 11.sp)
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = if (isHighlighter) 12f..60f else 2f..36f,
                modifier = Modifier.weight(1f).height(24.dp)
            )
            Text("${size.toInt()}px", fontSize = 10.sp)
        }
        CompactColorPalette(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun PinSubTray(
    selectedShape: PinShape,
    onShapeChange: (PinShape) -> Unit,
    size: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PinShape.values().forEach { shape ->
                FilterChip(
                    selected = selectedShape == shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.label, fontSize = 10.sp) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Pin Size:", fontSize = 11.sp)
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = 16f..80f,
                modifier = Modifier.weight(1f).height(24.dp)
            )
            Text("${size.toInt()}px", fontSize = 10.sp)
        }
        CompactColorPalette(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun ShapeSubTray(
    selectedShape: ShapeType,
    onShapeChange: (ShapeType) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShapeType.values().forEach { shape ->
                FilterChip(
                    selected = selectedShape == shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.label, fontSize = 10.sp) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Line Width:", fontSize = 11.sp)
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..24f,
                modifier = Modifier.weight(1f).height(24.dp)
            )
            Text("${strokeWidth.toInt()}px", fontSize = 10.sp)
        }
        CompactColorPalette(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun CircuitSubTray(
    selectedType: CircuitElementType,
    onTypeChange: (CircuitElementType) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap board to drop physics schematic element:", fontSize = 10.sp)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CircuitElementType.values().forEach { element ->
                FilterChip(
                    selected = selectedType == element,
                    onClick = { onTypeChange(element) },
                    label = { Text("${element.symbol} ${element.label}", fontSize = 10.sp) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }
        CompactColorPalette(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun StickerSubTray(
    selectedType: StickerType,
    onTypeChange: (StickerType) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap board to stamp symbol:", fontSize = 10.sp)
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StickerType.values().forEach { sticker ->
                FilterChip(
                    selected = selectedType == sticker,
                    onClick = { onTypeChange(sticker) },
                    label = { Text("${sticker.symbol} ${sticker.label}", fontSize = 12.sp) },
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}

@Composable
private fun MathToolsSubTray(
    isRulerActive: Boolean,
    onToggleRuler: () -> Unit,
    isProtractorActive: Boolean,
    onToggleProtractor: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onToggleRuler,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Text(if (isRulerActive) "Hide Ruler" else "Show Ruler 📏", fontSize = 11.sp)
        }

        Button(
            onClick = onToggleProtractor,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Text(if (isProtractorActive) "Hide Protractor" else "Show Protractor 📐", fontSize = 11.sp)
        }
    }
}
