package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val VSheetDragHorizontalPadding = 20.dp
internal val VSheetDragVerticalPadding = SheetItemsSpacing

@Composable
internal fun VSheetDragActions(
    actions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    itemsPerColumn: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val chunkedActions = actions.value.chunked(itemsPerColumn)
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(VSheetDragHorizontalPadding)
    ) {
        chunkedActions.fastForEach { actions ->
            Column(verticalArrangement = Arrangement.spacedBy(VSheetDragVerticalPadding)) {
                actions.fastForEachReversed { action ->
                    action(false, Modifier)
                }
            }
        }
    }
}