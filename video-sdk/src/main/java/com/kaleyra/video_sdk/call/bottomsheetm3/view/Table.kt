package com.kaleyra.video_sdk.call.bottomsheetm3.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> Table(
    modifier: Modifier = Modifier,
    columnCount: Int,
    data: List<T>,
    cellContent: @Composable (index: Int, item: T, currentRowItemsCount: Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .then(modifier)
    ) {
        val rows = rowsCount(data.size, columnCount)
        val lastRowItemsCount = lastRowItemsCount(data.size, columnCount)
        LazyColumn {
            items((1..rows).toList()) { row ->
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val rowIndex = row - 1
                    val isLastRow = row == rows
                    val rowStartIndex = columnCount * rowIndex
                    if (isLastRow) items((0..<lastRowItemsCount).toList()) { column ->
                        val index = rowStartIndex + column
                        cellContent(index, data[index], lastRowItemsCount)
                    }
                    else items((0..<columnCount).toList()) { column ->
                        val index = rowStartIndex + column
                        cellContent(index, data[index], columnCount)
                    }
                }
            }
        }
    }
}

internal fun rowsCount(dataSize: Int, columnCount: Int): Int = (dataSize / columnCount).plus(if (dataSize % columnCount != 0) 1 else 0)
internal fun lastRowItemsCount(dataSize: Int, columnCount: Int): Int = if (dataSize == columnCount) dataSize else dataSize % columnCount
