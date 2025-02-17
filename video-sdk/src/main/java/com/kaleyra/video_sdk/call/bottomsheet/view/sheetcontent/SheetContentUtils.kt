package com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent

import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.NotifiableCallAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal fun computeMoreActionNotificationCount(
    callActions: ImmutableList<CallActionUI>,
    persistentSheetActionCount: Int
): Int {
    val actions = callActions.value
    val dragSheetActions = actions - actions.take(persistentSheetActionCount).toSet()
    return dragSheetActions
        .filterIsInstance<NotifiableCallAction>()
        .sumOf { it.notificationCount }
}