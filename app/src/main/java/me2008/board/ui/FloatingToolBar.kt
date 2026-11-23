package me2008.board.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
fun FloatingToolBar(
    currentTool: ToolMode,
    onToolSelected: (ToolMode) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    pinSize: Float,
    onPinSizeChange: (Float) -> Unit,
    pinShape: PinShape,
    onPinShapeChange: (PinShape) -> Unit,
    selectedShapeType: ShapeType,
    onShapeTypeChange: (ShapeType) -> Unit,
    selectedCircuitType: CircuitElementType,
    onCircuitTypeChange: (CircuitElementType) -> Unit,
    isRulerActive: Boolean,
    onToggleRuler: () -> Unit,
    bgTheme: BackgroundColorTheme,
    onBgThemeChange: (BackgroundColorTheme) -> Unit,
    bgPattern: BackgroundPattern,
    onBgPatternChange: (BackgroundPattern) -> Unit,
    onUndo: () -> Unit,
    onClearPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSubMenu by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sub-Tray for Active Tool Customizations
        AnimatedVisibility(visible = expandedSubMenu) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentTool) {
                        ToolMode.PIN -> {
                            PinToolTray(
                                selectedShape = pinShape,
                                onShapeChange = onPinShapeChange,
                                size = pinSize,
                                onSizeChange = onPinSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.PEN -> {
                            PenToolTray(
                                size = pinSize,
                                onSizeChange = onPinSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.SHAPE -> {
                            ShapeToolTray(
                                selectedShape = selectedShapeType,
                                onShapeChange = onShapeTypeChange,
                                strokeWidth = pinSize,
                                onStrokeWidthChange = onPinSizeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.CIRCUIT -> {
                            CircuitToolTray(
                                selectedType = selectedCircuitType,
                                onTypeChange = onCircuitTypeChange,
                                color = selectedColor,
                                onColorChange = onColorSelected
                            )
                        }
                        ToolMode.RULER -> {
                            RulerToolTray(
                                isRulerActive = isRulerActive,
                                onToggleRuler = onToggleRuler
                            )
                        }
                        ToolMode.ERASER -> {
                            EraserToolTray(onClearPage = onClearPage)
                        }
                    }
                }
            }
        }

        // Primary Floating Dock with Tools
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 10.dp,
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin Tool
                ToolIconButton(
                    icon = Icons.Default.Place,
                    label = "Pin",
                    isSelected = currentTool == ToolMode.PIN,
                    onClick = {
                        onToolSelected(ToolMode.PIN)
                        expandedSubMenu = true
                    }
                )

                // Pen Tool
                ToolIconButton(
                    icon = Icons.Default.Edit,
                    label = "Pen",
                    isSelected = currentTool == ToolMode.PEN,
                    onClick = {
                        onToolSelected(ToolMode.PEN)
                        expandedSubMenu = true
                    }
                )

                // Shape Tool
                ToolIconButton(
                    icon = Icons.Default.Category,
                    label = "Shapes",
                    isSelected = currentTool == ToolMode.SHAPE,
                    onClick = {
                        onToolSelected(ToolMode.SHAPE)
                        expandedSubMenu = true
                    }
                )

                // Physics Circuit Tool
                ToolIconButton(
                    icon = Icons.Default.ElectricBolt,
                    label = "Physics",
                    isSelected = currentTool == ToolMode.CIRCUIT,
                    onClick = {
                        onToolSelected(ToolMode.CIRCUIT)
                        expandedSubMenu = true
                    }
                )

                // Ruler Tool
                ToolIconButton(
                    icon = Icons.Default.Straighten,
                    label = "Ruler",
                    isSelected = isRulerActive,
                    onClick = {
                        onToggleRuler()
                        onToolSelected(ToolMode.RULER)
                    }
                )

                // Eraser Tool
                ToolIconButton(
                    icon = Icons.Default.AutoFixHigh,
                    label = "Eraser",
                    isSelected = currentTool == ToolMode.ERASER,
                    onClick = {
                        onToolSelected(ToolMode.ERASER)
                        expandedSubMenu = true
                    }
                )

                // Undo
                IconButton(onClick = onUndo) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }

                // Delete / Clear
                IconButton(onClick = onClearPage) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Clear Page",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription = label)
    }
}

@Composable
private fun ColorPaletteRow(selectedColor: Color, onColorChange: (Color) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaletteColors.forEach { color ->
            val isCurrent = color == selectedColor
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 32.dp else 26.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isCurrent) 3.dp else 1.dp,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorChange(color) }
            )
        }
    }
}

@Composable
private fun PinToolTray(
    selectedShape: PinShape,
    onShapeChange: (PinShape) -> Unit,
    size: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pin Shape:", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PinShape.values().forEach { shape ->
                    FilterChip(
                        selected = selectedShape == shape,
                        onClick = { onShapeChange(shape) },
                        label = { Text(shape.label, fontSize = 11.sp) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Size:", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = 15f..90f,
                modifier = Modifier.weight(1f)
            )
        }
        ColorPaletteRow(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun PenToolTray(
    size: Float,
    onSizeChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Thickness:", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = size,
                onValueChange = onSizeChange,
                valueRange = 3f..45f,
                modifier = Modifier.weight(1f)
            )
        }
        ColorPaletteRow(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun ShapeToolTray(
    selectedShape: ShapeType,
    onShapeChange: (ShapeType) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShapeType.values().forEach { shape ->
                FilterChip(
                    selected = selectedShape == shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.label) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Width:", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 3f..30f,
                modifier = Modifier.weight(1f)
            )
        }
        ColorPaletteRow(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun CircuitToolTray(
    selectedType: CircuitElementType,
    onTypeChange: (CircuitElementType) -> Unit,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tap board to place Physics Circuit Element:", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CircuitElementType.values().forEach { element ->
                FilterChip(
                    selected = selectedType == element,
                    onClick = { onTypeChange(element) },
                    label = { Text("${element.symbol} ${element.label}", fontSize = 11.sp) }
                )
            }
        }
        ColorPaletteRow(selectedColor = color, onColorChange = onColorChange)
    }
}

@Composable
private fun RulerToolTray(isRulerActive: Boolean, onToggleRuler: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Interactive Math Measurement Ruler (Drag & Rotate)", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onToggleRuler) {
            Text(if (isRulerActive) "Hide Ruler" else "Show Ruler")
        }
    }
}

@Composable
private fun EraserToolTray(onClearPage: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Drag or tap on items to erase them", style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = onClearPage,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Clear Page")
        }
    }
}
