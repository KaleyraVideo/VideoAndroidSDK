package com.kaleyra.video_sdk.call.screen.view.hcallscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheet.view.streammenu.VStreamMenuContent
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.screen.callScreenScaffoldPaddingValues
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheet
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.stream.StreamComponent
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
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
    modalSheetComponent: ModularComponent?,
    inputPermissions: InputPermissions,
    onModalSheetComponentRequest: (ModularComponent?) -> Unit,
    onModularComponentDisplayed: (ModularComponent?) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember(selectedStreamId) { derivedStateOf { selectedStreamId == null && sheetDragActions.value.isNotEmpty() } }
    var isInFullscreenMode by remember { mutableStateOf(false) }

    HCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(top = 8.dp, right = 8.dp),
        topAppBar = {
            CallAppBarComponent(
                onParticipantClick = { onModalSheetComponentRequest(ModularComponent.Participants) },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                VSheetDragContent(
                    callActions = sheetDragActions,
                    inputPermissions = inputPermissions,
                    onModularComponentRequest = onModalSheetComponentRequest,
                    contentPadding = PaddingValues(top = 14.dp, end = 14.dp, bottom = 14.dp, start = 8.dp),
                    modifier = Modifier.animateContentSize()
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
                    Box {
                        val isSheetExpanded by remember(sheetState) {
                            derivedStateOf {
                                sheetState.targetValue == CallSheetValue.Expanded
                            }
                        }
                        VSheetContent(
                            isMoreToggled = isSheetExpanded,
                            inputPermissions = inputPermissions,
                            onActionsOverflow = { sheetDragActions = it },
                            onModularComponentRequest = onModalSheetComponentRequest,
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
                        onFullscreen = { isInFullscreenMode = true },
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
                    if (selectedStreamId != null && !isInFullscreenMode) {
                        onStreamSelected(null)
                        true
                    } else false
                }
                .clearAndSetSemantics {},
        ) {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = windowSizeClass,
                selectedStreamId = selectedStreamId,
                onStreamClick = { stream -> onStreamSelected(stream.id) },
                onMoreParticipantClick = { onModalSheetComponentRequest(ModularComponent.Participants) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .padding(
                        start = left,
                        top = top,
                        end = 104.dp,
                        bottom = bottom,
                    )
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )

            Column(Modifier.padding(top = top, end = 96.dp)) {
                CallInfoComponent(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .navigationBarsPadding()
                        .displayCutoutPadding()
                )
                if (modalSheetComponent != ModularComponent.FileShare && modalSheetComponent != ModularComponent.Whiteboard) {
                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                }
            }

            InputMessageHost(
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 96.dp)
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .align(Alignment.BottomCenter)
            )

            CallScreenModalSheet(
                modularComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentRequest(null) },
                onComponentDisplayed = onModularComponentDisplayed,
                onAskInputPermissions = onAskInputPermissions,
                onUserMessageActionClick = onUserMessageActionClick
            )
        }
    }
}