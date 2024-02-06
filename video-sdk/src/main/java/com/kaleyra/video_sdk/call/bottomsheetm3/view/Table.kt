package com.kaleyra.video_sdk.call.bottomsheetm3.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
    cellContent: @Composable (index: Int, item: T) -> Unit,
) {
    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface).then(modifier)
    ) {
        var rows = rowsCount(data.size, columnCount)
        val lastRowColumns = data.size % columnCount
        if (data.size % columnCount > 0) rows++
        LazyColumn {
            items((1 .. rows).toList()) { row ->
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (lastRowColumns != 0 && row == rows) {
                        items((1 .. lastRowColumns).toList()) { column ->
                            val index = row * column
                            cellContent(row * column, data[index - 1])
                        }
                    } else {
                        items((1 .. columnCount).toList()) { column ->
                            val index = row * column
                            cellContent(row * column, data[index - 1])
                        }
                    }
                }
            }
        }
    }
}

internal fun rowsCount(dataSize: Int, columnCount: Int): Int = (dataSize / columnCount).plus(if (dataSize % columnCount != 0) 1 else 0)
