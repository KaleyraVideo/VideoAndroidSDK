package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.VStreamMenuContent
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun HCallScreen(
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
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember(selectedStreamId) { derivedStateOf { selectedStreamId == null && sheetDragActions.value.isNotEmpty() } }

    HCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 8.dp, vertical = 4.dp),
        topAppBar = {
            CallAppBarComponent(
                onParticipantClick = { onModalSheetComponentRequest(ModalSheetComponent.Participants) },
                onBackPressed = onBackPressed,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .statusBarsPadding()
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                VSheetDragContent(
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
                    Box(Modifier.animateContentSize()) {
                        val isSheetExpanded by remember(sheetState) {
                            derivedStateOf {
                                sheetState.targetValue == CallSheetValue.Expanded
                            }
                        }
                        VSheetContent(
                            isMoreToggled = isSheetExpanded,
                            onActionsOverflow = { sheetDragActions = it },
                            onModalSheetComponentRequest = onModalSheetComponentRequest,
                            onMoreToggle = onChangeSheetState,
                            modifier = Modifier
                                .padding(
                                    start = 5.dp,
                                    top = 14.dp,
                                    end = 14.dp,
                                    bottom = 14.dp
                                )
                        )
                    }
                } else {
                    VStreamMenuContent(
                        selectedStreamId = currentlySelectedStreamId,
                        onDismiss = { onStreamSelected(null) },
                        modifier = Modifier.testTag(StreamMenuContentTestTag)
                    )
                }
            }
        },
        sheetDragHandle = (@Composable { CallBottomSheetDefaults.VDragHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)
        val bottom = paddingValues.calculateBottomPadding()

        val streamViewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
        )
        val onUserMessageActionClick = remember(streamViewModel) {
            { message: UserMessage ->
                when (message) {
                    is PinScreenshareMessage -> { streamViewModel.pin(message.streamId, prepend = true, force = true); Unit }
                    else -> Unit
                }
            }
        }

        Box(
            modifier = Modifier
                .pointerInteropFilter {
                    // TODO test this
                    if (selectedStreamId != null) {
                        onStreamSelected(null)
                        true
                    } else false
                }
                .clearAndSetSemantics {}
        ) {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = windowSizeClass,
                selectedStreamId = selectedStreamId,
                onStreamClick = { stream -> onStreamSelected(stream.id) },
                onMoreParticipantClick = { onModalSheetComponentRequest(ModalSheetComponent.Participants) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(
                        start = left,
                        top = top,
                        end = 116.dp,
                        bottom = bottom,
                    )
                    .padding(top = 14.dp)
            )

            Column(
                modifier = Modifier
                    .padding(top = top, end = 116.dp)
                    .padding(vertical = 24.dp)
            ) {
                CallInfoComponent(Modifier.padding(vertical = 12.dp))
                if (modalSheetComponent != ModalSheetComponent.FileShare && modalSheetComponent != ModalSheetComponent.Whiteboard) {
                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                }
            }

            InputMessageHost(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.BottomCenter)
            )

            CallScreenModalSheet(
                modalSheetComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentRequest(null) },
                onAskInputPermissions = onAskInputPermissions,
                onUserMessageActionClick = onUserMessageActionClick
            )
        }
    }
}