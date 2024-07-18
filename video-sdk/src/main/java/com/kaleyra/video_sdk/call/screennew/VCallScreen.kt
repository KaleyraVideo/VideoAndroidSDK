package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val PanelTestTag = "PanelTestTag"

internal val StreamMenuContentTestTag = "StreamMenuContentTestTag"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    modalSheetState: SheetState,
    onChangeSheetState: (Boolean) -> Unit,
    selectedStreamId: String?,
    onStreamSelected: (String?) -> Unit,
    modalSheetComponent: ModalSheetComponent?,
    onModalSheetComponentRequest: (ModalSheetComponent?) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded)

    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember(isLargeScreen, selectedStreamId) { derivedStateOf { !isLargeScreen && selectedStreamId == null && sheetDragActions.value.isNotEmpty() } }

    var showSheetPanelContent by remember(isLargeScreen) { mutableStateOf(false) }

    VCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 4.dp, vertical = 8.dp),
        topAppBar = {
            CallAppBarComponent(
                // TODO test this
                onParticipantClick = { /*TODO*/ },
                onBackPressed = onBackPressed,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .statusBarsPadding()
            )
        },
        sheetPanelContent = if (isLargeScreen) {
            {
                AnimatedVisibility(
                    visible = showSheetPanelContent,
                    enter = fadeIn(tween()),
                    exit = fadeOut(tween()),
                    content = {
                        SheetPanelContent(
                            callActions = sheetDragActions,
                            onModalSheetComponentRequest = onModalSheetComponentRequest,
                            modifier = Modifier.testTag(PanelTestTag)
                        )
                    }
                )
            }
        } else null,
        sheetDragContent = {
            if (hasSheetDragContent) {
                HSheetDragContent(
                    callActions = sheetDragActions,
                    onModalSheetComponentRequest = onModalSheetComponentRequest,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(14.dp)
                )
            }
        },
        sheetContent = {
            AnimatedContent(
                targetState = selectedStreamId,
                contentAlignment = Alignment.Center,
                label = "sheet content"
            ) { currentlySelectedStreamId ->
                if (currentlySelectedStreamId == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        if (isLargeScreen) {
                            LargeScreenInputMessageHost()
                        }

                        Box(Modifier.animateContentSize()) {
                            val isSheetExpanded by remember(sheetState) {
                                derivedStateOf {
                                    sheetState.targetValue == CallSheetValue.Expanded
                                }
                            }
                            HSheetContent(
                                isLargeScreen = isLargeScreen,
                                isMoreToggled = isSheetExpanded || showSheetPanelContent,
                                maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                                onActionsOverflow = { sheetDragActions = it },
                                onModalSheetComponentRequest = onModalSheetComponentRequest,
                                onMoreToggle = { isSheetCollapsed ->
                                    if (hasSheetDragContent) onChangeSheetState(isSheetCollapsed)
                                    else showSheetPanelContent = !showSheetPanelContent
                                },
                                modifier = Modifier
                                    .padding(
                                        start = 14.dp,
                                        top = if (isLargeScreen) 14.dp else 5.dp,
                                        end = 14.dp,
                                        bottom = 14.dp
                                    )
                            )
                        }
                    }
                } else {
                    HStreamMenuContent(
                        selectedStreamId = currentlySelectedStreamId,
                        onDismiss = { onStreamSelected(null) },
                        modifier = Modifier.testTag(StreamMenuContentTestTag)
                    )
                }
            }
        },
        // TODO test this
        sheetDragHandle = (@Composable { InputMessageHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)
        val right = paddingValues.calculateRightPadding(layoutDirection)

        Box(
            modifier = Modifier
                .pointerInteropFilter {
                    // TODO test this
                    when {
                        showSheetPanelContent -> {
                            showSheetPanelContent = false
                            true
                        }
                        selectedStreamId != null -> {
                            onStreamSelected(null)
                            true
                        }
                        else -> false
                    }
                }
            .clearAndSetSemantics {}
        ) {
            StreamComponent(
                windowSizeClass = windowSizeClass,
                highlightedStreamId = selectedStreamId,
                onStreamClick = { stream -> onStreamSelected(stream.id) },
                // TODO test this
                onMoreParticipantClick = {},
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(
                        start = left,
                        top = top,
                        end = right,
                        bottom = 116.dp
                    )
                    .padding(top = 14.dp)
            )

            CallInfoComponent(
                modifier = Modifier
                    .padding(top = top)
                    .padding(horizontal = 8.dp, vertical = 48.dp)
            )

            CallScreenModalSheet(
                modalSheetComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentRequest(null) },
                onAskInputPermissions = onAskInputPermissions,
                // TODO test this
                onUserMessageActionClick = { }
            )
        }
    }
}

@Composable
private fun LargeScreenInputMessageHost() {
    Box(
        modifier = Modifier
            .width(250.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // TODO test this
        InputMessageHost(
            micMessage = { enabled -> MicMessageText(enabled) },
            cameraMessage = { enabled -> CameraMessageText(enabled) }
        )
    }
}
