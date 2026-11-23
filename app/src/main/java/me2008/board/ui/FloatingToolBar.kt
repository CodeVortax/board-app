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

// Vibrant palette featuring true pure black and high-contrast teacher colors
val PaletteColors = listOf(
    Color(0xFF000000), // Pure Black
    Color(0xFFFFFFFF), // Pure White
    Color(0xFFE11D48), // Vivid Red
    Color(0xFFFF6D00), // Neon Orange
    Color(0xFFFACC15), // Warm Yellow
    Color(0xFF16A34A), // Emerald Green
    Color(0xFF00F5D4), // Neon Cyan
    Color(0xFF2563EB), // Royal Blue
    Color(0xFF9333EA), // Purple
    Color(0xFFEC4899)  // Pink
)

@Composable
fun UnifiedFloatingBottomBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onAddPage: () -> Unit,
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
    selectedFormulaType: FormulaType,
    onFormulaTypeChange: (FormulaType) -> Unit,
    isRulerActive: Boolean,
    onToggleRuler: () -> Unit,
    onDrawLineAlongRuler: () -> Unit,
    isProtractorActive: Boolean,
    onToggleProtractor: () -> Unit,
    bgTheme: BackgroundColorTheme,
    onBgThemeChange: (BackgroundColorTheme) -> Unit,
    bgPattern: BackgroundPattern,
    onBgPatternChange: (BackgroundPattern) -> Unit,
    zoomScale: Float,
    onResetZoom: () -> Unit,
    onUndo: () -> Unit,
    onClearPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showToolPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .wrapContentWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compact Sub-Tray above Dock (auto-applies changes immediately)
        AnimatedVisibility(
            visible = showToolPopup,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = 520.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentTool) {
                        ToolMode.PEN, ToolMode.HIGHLIGHTER, ToolMode.STRAIGHT_LINE -> {
                            PenSubTray(
                                mode = currentTool,
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
                        ToolMode.FORMULA -> {
                            FormulaSubTray(
                                selectedType = selectedFormulaType,
                                onTypeChange = onFormulaTypeChange,
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
                                onDrawLineAlongRuler = onDrawLineAlongRuler,
                                isProtractorActive = isProtractorActive,
                                onToggleProtractor = onToggleProtractor
                            )
                        }
                        ToolMode.BACKGROUND -> {
                            // Instant background changer: no dialog, immediate update!
                            BackgroundSubTray(
                                currentTheme = bgTheme,
                                onThemeChange = onBgThemeChange,
                                currentPattern = bgPattern,
                                onPatternChange = onBgPatternChange
                            )
                        }
                        ToolMode.LASER -> {
                            Text(
                                "Neon laser pointer leaves a glowing orange trail for live presentations",
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
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Clear All", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unified Horizontal Bottom Bar (Compact for 1600x900 / landscape)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Page Navigation (Next to tools)
                CompactIconButton(
                    icon = Icons.Default.NavigateBefore,
                    contentDescription = "Previous Page",
                    enabled = currentPage > 0,
                    onClick = onPreviousPage
                )

                Text(
                    text = "${currentPage + 1}/${totalPages}",
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall,
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

                VerticalDivider(modifier = Modifier.height(18.dp).padding(horizontal = 1.dp))

                // 2. Tools (In Middle)
                // Pen
                ToolItemButton(
                    icon = Icons.Default.Edit,
                    isSelected = currentTool == ToolMode.PEN,
                    onClick = {
                        if (currentTool == ToolMode.PEN) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.PEN); showToolPopup = true }
                    }
                )

                // Marker / Highlighter
                ToolItemButton(
                    icon = Icons.Default.Brush,
                    isSelected = currentTool == ToolMode.HIGHLIGHTER,
                    onClick = {
                        if (currentTool == ToolMode.HIGHLIGHTER) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.HIGHLIGHTER); showToolPopup = true }
                    }
                )

                // Straight Line
                ToolItemButton(
                    icon = Icons.Default.HorizontalRule,
                    isSelected = currentTool == ToolMode.STRAIGHT_LINE,
                    onClick = {
                        if (currentTool == ToolMode.STRAIGHT_LINE) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.STRAIGHT_LINE); showToolPopup = true }
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

                // Physics Circuit
                ToolItemButton(
                    icon = Icons.Default.ElectricBolt,
                    isSelected = currentTool == ToolMode.CIRCUIT,
                    onClick = {
                        if (currentTool == ToolMode.CIRCUIT) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.CIRCUIT); showToolPopup = true }
                    }
                )

                // Math Formulas
                ToolItemButton(
                    icon = Icons.Default.Functions,
                    isSelected = currentTool == ToolMode.FORMULA,
                    onClick = {
                        if (currentTool == ToolMode.FORMULA) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.FORMULA); showToolPopup = true }
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

                // Stamps
                ToolItemButton(
                    icon = Icons.Default.Star,
                    isSelected = currentTool == ToolMode.STICKER,
                    onClick = {
                        if (currentTool == ToolMode.STICKER) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.STICKER); showToolPopup = true }
                    }
                )

                // Laser
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

                VerticalDivider(modifier = Modifier.height(18.dp).padding(horizontal = 1.dp))

                // 3. Board Utilities
                // Background Style (Instant Sub-Tray)
                ToolItemButton(
                    icon = Icons.Default.Palette,
                    isSelected = currentTool == ToolMode.BACKGROUND,
                    onClick = {
                        if (currentTool == ToolMode.BACKGROUND) showToolPopup = !showToolPopup
                        else { onToolSelected(ToolMode.BACKGROUND); showToolPopup = true }
                    }
                )

                // Zoom Reset (if zoomed)
                if (zoomScale != 1f) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(onClick = onResetZoom)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(zoomScale * 100).toInt()}% ⟲",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

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
            .size(28.dp)
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
            modifier = Modifier.size(16.dp),
            tint = if (!enabled) Color.Gray.copy(alpha = 0.35f)
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
            .size(30.dp)
            .clip(RoundedCornerShape(7.dp))
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
            modifier = Modifier.size(16.dp),
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
                    .size(if (isCurrent) 22.dp else 17.dp)
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
    mode: ToolMode,
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
            val label = when (mode) {
                ToolMode.HIGHLIGHTER -> "Marker:"
                ToolMode.STRAIGHT_LINE -> "Line:"
                else -> "Pen:"
            }
            Text(label, fontSize = 11.sp)
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = if (mode == ToolMode.HIGHLIGHTER) 10f..50f else 2f..30f,
                modifier = Modifier.weight(1f).height(22.dp)
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
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PinShape.values().forEach { shape ->
                FilterChip(
                    selected = selectedShape == shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.label, fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
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
                valueRange = 16f..72f,
                modifier = Modifier.weight(1f).height(22.dp)
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
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShapeType.values().forEach { shape ->
                FilterChip(
                    selected = selectedShape == shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.label, fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Width:", fontSize = 11.sp)
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..20f,
                modifier = Modifier.weight(1f).height(22.dp)
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
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CircuitElementType.values().forEach { element ->
                FilterChip(
                    selected = selectedType == element,
                    onClick = { onTypeChange(element) },
                    label = { Text("${element.symbol} ${element.label}", fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
        CompactColorPalette(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun FormulaSubTray(
    selectedType: FormulaType,
    onTypeChange: (FormulaType) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap board to place math formula stamp:", fontSize = 10.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FormulaType.values().forEach { item ->
                FilterChip(
                    selected = selectedType == item,
                    onClick = { onTypeChange(item) },
                    label = { Text(item.formula, fontSize = 11.sp) },
                    modifier = Modifier.height(24.dp)
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
        Text("Tap board to stamp reaction sticker:", fontSize = 10.sp)
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StickerType.values().forEach { sticker ->
                FilterChip(
                    selected = selectedType == sticker,
                    onClick = { onTypeChange(sticker) },
                    label = { Text("${sticker.symbol} ${sticker.label}", fontSize = 11.sp) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }
    }
}

@Composable
private fun MathToolsSubTray(
    isRulerActive: Boolean,
    onToggleRuler: () -> Unit,
    onDrawLineAlongRuler: () -> Unit,
    isProtractorActive: Boolean,
    onToggleProtractor: () -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onToggleRuler,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(26.dp)
        ) {
            Text(if (isRulerActive) "Hide Ruler" else "Show Ruler 📏", fontSize = 10.sp)
        }

        if (isRulerActive) {
            FilledTonalButton(
                onClick = onDrawLineAlongRuler,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text("Draw Ruler Line ✍️", fontSize = 10.sp)
            }
        }

        Button(
            onClick = onToggleProtractor,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(26.dp)
        ) {
            Text(if (isProtractorActive) "Hide Angle" else "Protractor 📐", fontSize = 10.sp)
        }
    }
}

@Composable
private fun BackgroundSubTray(
    currentTheme: BackgroundColorTheme,
    onThemeChange: (BackgroundColorTheme) -> Unit,
    currentPattern: BackgroundPattern,
    onPatternChange: (BackgroundPattern) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Theme selection (instant change)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BackgroundColorTheme.values().forEach { theme ->
                FilterChip(
                    selected = currentTheme == theme,
                    onClick = { onThemeChange(theme) },
                    label = { Text(theme.displayName, fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        // Pattern selection (instant change)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BackgroundPattern.values().forEach { pattern ->
                FilterChip(
                    selected = currentPattern == pattern,
                    onClick = { onPatternChange(pattern) },
                    label = { Text(pattern.displayName, fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}
