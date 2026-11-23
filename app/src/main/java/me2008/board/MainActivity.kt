package me2008.board

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me2008.board.ui.theme.BoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BoardScreen()
                }
            }
        }
    }
}

@Composable
fun BoardScreen() {
    var currentPage by remember { mutableStateOf(0) }
    val pages = remember { mutableStateListOf<MutableList<Pin>>() }
    if (pages.isEmpty()) pages.add(mutableListOf()) // page 0

    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas area where pins are drawn
        PinCanvas(pins = pages[currentPage])

        // Floating tool panel
        ToolPanel(
            onAddPin = { pin -> pages[currentPage].add(pin) },
            onDeleteAll = { pages[currentPage].clear() }
        )

        // Page navigation buttons
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                if (currentPage > 0) currentPage--
            }, enabled = currentPage > 0) {
                Text("Back")
            }
            Button(onClick = {
                currentPage++
                if (pages.size <= currentPage) pages.add(mutableListOf())
            }) {
                Text("Forward")
            }
        }
    }
}

// Simple data class for a pin – you can extend with size, colour, shape etc.
data class Pin(val x: Float, val y: Float, val colour: Color = Color.Red, val size: Float = 30f)

@Composable
fun PinCanvas(pins: List<Pin>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF0F0F0))
        .pointerInput(Unit) {
            // TODO: handle touch to place pins based on selected tool
        }) {
        pins.forEach { pin ->
            Box(
                modifier = Modifier
                    .size(pin.size.dp)
                    .offset(x = pin.x.dp - pin.size.dp / 2, y = pin.y.dp - pin.size.dp / 2)
                    .background(pin.colour, shape = CircleShape)
            )
        }
    }
}

@Composable
fun ToolPanel(onAddPin: (Pin) -> Unit, onDeleteAll: () -> Unit) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var selectedSize by remember { mutableStateOf(30f) }
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .background(Color.White.copy(alpha = 0.9f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Tools", style = MaterialTheme.typography.titleMedium)
        // Colour picker (simplified)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(Color.Red, Color.Green, Color.Blue, Color.Black).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(c, shape = CircleShape)
                        .border(1.dp, Color.Gray, shape = CircleShape)
                        .clickable { selectedColor = c }
                )
            }
        }
        // Size selector
        Slider(
            value = selectedSize,
            onValueChange = { selectedSize = it },
            valueRange = 10f..80f,
            steps = 6,
            modifier = Modifier.width(120.dp)
        )
        Button(onClick = {
            // Add a dummy pin at centre for demo
            onAddPin(Pin(x = 200f, y = 400f, colour = selectedColor, size = selectedSize))
        }) {
            Text("Add Pin")
        }
        OutlinedButton(onClick = onDeleteAll) {
            Text("Delete All")
        }
    }
}
