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
    cellContent: @Composable (index: Int, item: T, itemsPerRow: Int) -> Unit,
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface).then(modifier)
    ) {
        val rows = rowsCount(data.size, columnCount)
        val lastRowElementsCount = lastRowItemsCount(data.size, columnCount)
        LazyColumn {
            items((1 .. rows).toList()) { row ->
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (lastRowElementsCount != 0 && row == rows) {
                        items((1 .. lastRowElementsCount).toList()) { column ->
                            val index = row * column
                            cellContent(row * column, data[index - 1], lastRowElementsCount)
                        }
                    } else {
                        items((1 .. columnCount).toList()) { column ->
                            val index = row * column
                            cellContent(row * column, data[index - 1], columnCount)
                        }
                    }
                }
            }
        }
    }
}

internal fun rowsCount(dataSize: Int, columnCount: Int): Int = (dataSize / columnCount).plus(if (dataSize % columnCount != 0) 1 else 0)
internal fun lastRowItemsCount(dataSize: Int, columnCount: Int): Int = dataSize % columnCount
