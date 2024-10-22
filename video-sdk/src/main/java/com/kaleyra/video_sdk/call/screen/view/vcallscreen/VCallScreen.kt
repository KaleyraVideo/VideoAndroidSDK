package com.kaleyra.video_sdk.call.screen.view.vcallscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.bottomsheet.view.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.screen.CompactScreenMaxActions
import com.kaleyra.video_sdk.call.screen.LargeScreenMaxActions
import com.kaleyra.video_sdk.call.screen.callScreenScaffoldPaddingValues
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheet
import com.kaleyra.video_sdk.call.stream.StreamComponent
import com.kaleyra.video_sdk.call.stream.StreamItemSpacing
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.extensions.DpExtensions.toPixel
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastMediumWidth

internal val PanelTestTag = "PanelTestTag"

internal val StreamMenuContentTestTag = "StreamMenuContentTestTag"

private val BottomSheetHeight = 76.dp
private val BottomSheetWithHandleHeight = 87.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    modalSheetState: SheetState,
    onChangeSheetState: (Boolean) -> Unit,
    selectedStreamId: String?,
    onStreamSelected: (String?) -> Unit,
    modalSheetComponent: ModularComponent?,
    sidePanelComponent: ModularComponent?,
    inputPermissions: InputPermissions,
    onModalSheetComponentRequest: (ModularComponent?) -> Unit,
    onSidePanelComponentRequest: (ModularComponent?) -> Unit,
    onModularComponentDisplayed: (ModularComponent?) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val callActionsViewModel: CallActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallActionsViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    )
    val callActionsUiState by callActionsViewModel.uiState.collectAsStateWithLifecycle()
    val isRinging by remember { derivedStateOf { callActionsUiState.isRinging } }

    val isLargeScreen = remember(windowSizeClass) { windowSizeClass.isAtLeastMediumWidth() }

    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember(isLargeScreen, selectedStreamId) {
        derivedStateOf { (!isLargeScreen || isRinging) && selectedStreamId == null && sheetDragActions.value.isNotEmpty() }
    }
    var showSheetPanelContent by remember(isLargeScreen) { mutableStateOf(false) }

    var isInFullscreenMode by remember { mutableStateOf(false) }

    if (!isRinging && isLargeScreen) {
        LaunchedEffect(Unit) {
            sheetState.collapse()
        }
    }

    val onSideBarSheetComponentRequest: (ModularComponent?) -> Unit = { component ->
        if (isLargeScreen && isSidePanelSupported(component)) onSidePanelComponentRequest(component)
        else onModalSheetComponentRequest(component)
    }

    val contentSpacing =  if (isLargeScreen) 16.dp else 8.dp
    VCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(top = contentSpacing, bottom = contentSpacing),
        topAppBar = {
            CallAppBarComponent(
                onParticipantClick = { onSideBarSheetComponentRequest(ModularComponent.Participants) },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = contentSpacing)
            )
        },
        sheetPanelContent = if (!isRinging && isLargeScreen) {
            {
                AnimatedVisibility(
                    visible = showSheetPanelContent,
                    enter = fadeIn(tween()),
                    exit = fadeOut(tween()),
                    content = {
                        SheetPanelContent(
                            callActions = sheetDragActions,
                            onModularComponentRequest = { component ->
                                onSideBarSheetComponentRequest(component)
                                showSheetPanelContent = false
                            },
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
                    isLargeScreen = isLargeScreen,
                    inputPermissions = inputPermissions,
                    onModularComponentRequest = onSideBarSheetComponentRequest ,
                    contentPadding = PaddingValues(top = 8.dp, end = 14.dp, bottom = 14.dp, start = 14.dp),
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(
                            start = 14.dp,
                            top = if (!hasSheetDragContent) 14.dp else 5.dp,
                            end = 14.dp,
                            bottom = 14.dp
                        )
                    ) {
                        if (isLargeScreen && !isRinging) {
                            LargeScreenInputMessageHost()
                        }

                        Box {
                            val isSheetExpanded by remember(sheetState) {
                                derivedStateOf {
                                    sheetState.targetValue == CallSheetValue.Expanded
                                }
                            }
                            HSheetContent(
                                isLargeScreen = isLargeScreen,
                                isMoreToggled = isSheetExpanded || showSheetPanelContent,
                                maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                                inputPermissions = inputPermissions,
                                onActionsOverflow = { sheetDragActions = it },
                                onModularComponentRequest = onSideBarSheetComponentRequest,
                                onMoreToggle = { isSheetCollapsed ->
                                    if (hasSheetDragContent) onChangeSheetState(isSheetCollapsed)
                                    else showSheetPanelContent = !showSheetPanelContent
                                }
                            )
                        }
                    }
                } else {
                    HStreamMenuContent(
                        selectedStreamId = currentlySelectedStreamId,
                        onDismiss = { onStreamSelected(null) },
                        onFullscreen = { isInFullscreenMode = true },
                        modifier = Modifier.testTag(StreamMenuContentTestTag)
                    )
                }
            }
        },
        sheetDragHandle = (@Composable { InputMessageHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val streamItemSpacing = StreamItemSpacing
        val bottomSheetExpectedHeight = remember(hasSheetDragContent) { if (hasSheetDragContent) BottomSheetWithHandleHeight else BottomSheetHeight }
        val topPadding = remember(paddingValues, contentSpacing) { paddingValues.calculateTopPadding() + contentSpacing - streamItemSpacing }
        val bottomPadding = remember(bottomSheetExpectedHeight, contentSpacing) { bottomSheetExpectedHeight + contentSpacing * 2 - streamItemSpacing }
        val horizontalPadding = remember(contentSpacing) { contentSpacing - streamItemSpacing }

        Box(
            modifier = Modifier
                .pointerInteropFilter {
                    // TODO test this
                    when {
                        showSheetPanelContent -> {
                            showSheetPanelContent = false
                            true
                        }

                        selectedStreamId != null && !isInFullscreenMode -> {
                            onStreamSelected(null)
                            true
                        }

                        else -> false
                    }
                }
                .clearAndSetSemantics {}
        ) {
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

            LookaheadScope {
                BoxWithConstraints {
                    val constraints = constraints

                    Row {
                        val sidePanelWeight = remember(sidePanelComponent) {
                            if (sidePanelComponent == ModularComponent.Whiteboard) 4f else 1f
                        }

                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .clipToBounds()
                        ) {
                            StreamComponent(
                                viewModel = streamViewModel,
                                windowSizeClass = windowSizeClass,
                                selectedStreamId = selectedStreamId,
                                onStreamClick = { stream -> onStreamSelected(stream.id) },
                                onMoreParticipantClick = { onSideBarSheetComponentRequest(ModularComponent.Participants) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                                    .padding(
                                        start = horizontalPadding,
                                        top = topPadding,
                                        end = if (sidePanelComponent != null) 0.dp else horizontalPadding,
                                        bottom = bottomPadding
                                    )
                            )


                            Column(
                                modifier = Modifier
                                    .padding(top = topPadding)
                                    .animateConstraints()
                                    .animatePlacement(this@LookaheadScope)
                            ) {
                                CallInfoComponent(
                                    modifier = Modifier
                                        .padding(top = 56.dp, bottom = 16.dp)
                                        .animateConstraints()
                                        .animatePlacement(this@LookaheadScope)
                                )
                                if (modalSheetComponent != ModularComponent.FileShare && modalSheetComponent != ModularComponent.Whiteboard) {
                                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                                }
                            }
                        }

                        sidePanelComponent?.let { component ->
                            SidePanel(
                                modularComponent = component,
                                onDismiss = { onSidePanelComponentRequest(null) },
                                onComponentDisplayed = onModularComponentDisplayed,
                                modifier = Modifier
                                    .weight(sidePanelWeight)
                                    .navigationBarsPadding()
                                    .padding(
                                        start = 0.dp,
                                        top = topPadding,
                                        end = horizontalPadding,
                                        bottom = bottomPadding
                                    )
                                    .animatePlacement(IntOffset(constraints.maxWidth, topPadding.toPixel.toInt()))
                            )
                        }
                    }
                }
            }

            CallScreenModalSheet(
                modularComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentRequest(null) },
                onAskInputPermissions = onAskInputPermissions,
                onUserMessageActionClick = onUserMessageActionClick,
                onComponentDisplayed = onModularComponentDisplayed,
            )
        }
    }
}

private fun isSidePanelSupported(modularComponent: ModularComponent?): Boolean {
    return modularComponent.let {
       it == ModularComponent.Chat || it == ModularComponent.FileShare || it == ModularComponent.Whiteboard || it == ModularComponent.Participants
    }
}

@Composable
private fun LargeScreenInputMessageHost() {
    Box(
        modifier = Modifier
            .width(250.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        InputMessageHost(
            micMessage = { enabled -> MicMessageText(enabled) },
            cameraMessage = { enabled -> CameraMessageText(enabled) }
        )
    }
}
