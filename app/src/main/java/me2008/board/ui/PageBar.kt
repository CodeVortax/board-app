package me2008.board.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me2008.board.model.BackgroundColorTheme
import me2008.board.model.BackgroundPattern

@Composable
fun PageBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onAddPage: () -> Unit,
    onDeleteCurrentPage: () -> Unit,
    onOpenBgSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: Backward
            FilledTonalIconButton(
                onClick = onPreviousPage,
                enabled = currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = "Previous Page"
                )
            }

            // Page Indicator
            Text(
                text = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Button 2: Forward
            FilledTonalIconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages - 1
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = "Next Page"
                )
            }

            // Button 3: Add Page
            FilledIconButton(
                onClick = onAddPage,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Page"
                )
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            // Background customization button
            IconButton(onClick = onOpenBgSettings) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Board Background"
                )
            }

            // Delete current page (if > 1 page)
            if (totalPages > 1) {
                IconButton(onClick = onDeleteCurrentPage) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Page",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
