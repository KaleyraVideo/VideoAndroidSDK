package com.kaleyra.video_sdk.call.screen.view.vcallscreen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.FullScreenMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.bottomsheet.view.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.brandlogo.model.hasLogo
import com.kaleyra.video_sdk.call.brandlogo.view.BrandLogoComponent
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickBroadcastReceiver
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.screen.CompactScreenMaxActions
import com.kaleyra.video_sdk.call.screen.LargeScreenMaxActions
import com.kaleyra.video_sdk.call.screen.callScreenScaffoldPaddingValues
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheet
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.call.stream.StreamComponent
import com.kaleyra.video_sdk.call.stream.StreamItemSpacing
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.DownloadFileMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.SignatureMessage
import com.kaleyra.video_sdk.common.usermessages.model.ThermalWarningMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.extensions.DpExtensions.toPixel
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastExpandedWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastMediumWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isCompactInAnyDimension
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isLargeScreen

internal val PanelTestTag = "PanelTestTag"

internal val StreamMenuContentTestTag = "StreamMenuContentTestTag"

private val CallSheetEstimatedHeight = 76.dp
private val CallSheetEstimatedHeightWithHandle = 87.dp
private val StreamMenuEstimatedHeight = 100.dp

private val ContentSpacing = 8.dp
private val ContentExpandedSpacing = 12.dp

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
    onChatDeleted: () -> Unit,
    onChatCreationFailed: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    val callActionsViewModel: CallActionsViewModel = viewModel(
        factory = CallActionsViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    )
    val callActionsUiState by callActionsViewModel.uiState.collectAsStateWithLifecycle()
    val isRinging by remember { derivedStateOf { callActionsUiState.isRinging } }

    val isLargeScreen = remember(windowSizeClass) { windowSizeClass.isLargeScreen() }
    val isAtLeastExpandedWidth = remember(windowSizeClass) { windowSizeClass.isAtLeastExpandedWidth() }

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

    val contentSpacing = if (isLargeScreen) ContentExpandedSpacing else ContentSpacing
    var hasConnectedCallOnce by remember { mutableStateOf(false) }
    val brandLogoViewModel: BrandLogoViewModel = viewModel(factory = BrandLogoViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
    val isDarkTheme = isSystemInDarkTheme()
    val brandLogoUiState by brandLogoViewModel.uiState.collectAsStateWithLifecycle()
    hasConnectedCallOnce = hasConnectedCallOnce || brandLogoUiState.callStateUi == CallStateUi.Connected

    val hasLogo = brandLogoUiState.hasLogo(isDarkTheme)

    VCallScreenScaffold(
        modifier = modifier,
        windowSizeClass = windowSizeClass,
        sheetState = sheetState,
        // Avoid applying horizontal padding here to prevent it from affecting the bottom sheet.
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
                            inputPermissions = inputPermissions,
                            onAskInputPermissions = onAskInputPermissions,
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
                    onModularComponentRequest = onSideBarSheetComponentRequest,
                    contentPadding = PaddingValues(top = 8.dp, end = 14.dp, bottom = 14.dp, start = 14.dp),
                    onAskInputPermissions = onAskInputPermissions,
                    modifier = Modifier.animateContentSize()
                )
            }
        },
        brandLogo = {
            val shouldShowBrandLogoWithLargeScreen = with(brandLogoUiState.callStateUi) {
                this is CallStateUi.Connected
                    || (this is CallStateUi.Disconnecting && hasConnectedCallOnce)
                    || (this is CallStateUi.Disconnected.Ended && hasConnectedCallOnce)
            }
            if (!windowSizeClass.isCompactInAnyDimension() && (windowSizeClass.isAtLeastExpandedWidth() || (windowSizeClass.isAtLeastMediumWidth() && shouldShowBrandLogoWithLargeScreen))) {
                val windowInsets = WindowInsets.displayCutout.only(WindowInsetsSides.Start).asPaddingValues()
                BrandLogoComponent(
                    modifier = Modifier
                        .padding(windowInsets)
                        .fillMaxWidth()
                        .height(46.dp)
                        .align(Alignment.Center),
                    alignment = Alignment.CenterStart
                )
            }
        },
        sheetContent = {
            val isSheetExpanded by remember(sheetState) {
                derivedStateOf {
                    sheetState.targetValue == CallSheetValue.Expanded
                }
            }

            AnimatedContent(
                targetState = selectedStreamId,
                contentAlignment = Alignment.Center,
                label = "sheet content"
            ) { currentlySelectedStreamId ->
                if (currentlySelectedStreamId == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!hasSheetDragContent) {
                            LargeScreenInputMessageHost(Modifier.offset(y = 3.dp))
                        }

                        HSheetContent(
                            isLargeScreen = isLargeScreen,
                            isMoreToggled = isSheetExpanded || showSheetPanelContent,
                            maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                            inputPermissions = inputPermissions,
                            onAskInputPermissions = onAskInputPermissions,
                            onActionsOverflow = { sheetDragActions = it },
                            onModularComponentRequest = onSideBarSheetComponentRequest,
                            onMoreToggle = { isSheetCollapsed ->
                                if (hasSheetDragContent) onChangeSheetState(isSheetCollapsed)
                                else showSheetPanelContent = !showSheetPanelContent
                            },
                            modifier = Modifier.padding(
                                start = 14.dp,
                                top = if (!hasSheetDragContent) 14.dp else 6.5.dp,
                                end = 14.dp,
                                bottom = 14.dp
                            )
                        )
                    }
                } else {
                    HStreamMenuContent(
                        selectedStreamId = currentlySelectedStreamId,
                        onDismiss = {
                            isInFullscreenMode = false
                            onStreamSelected(null)
                        },
                        onFullscreen = {
                            isInFullscreenMode = true
                            onStreamSelected(null)
                        },
                        modifier = Modifier.testTag(StreamMenuContentTestTag)
                    )
                }
            }
        },
        sheetDragHandle = (@Composable { InputMessageHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val cutoutPaddingValues = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal).asPaddingValues()
        val streamComponentPadding = contentSpacing - StreamItemSpacing
        val layoutDirection = LocalLayoutDirection.current

        val leftPadding = paddingValues.calculateLeftPadding(layoutDirection) + streamComponentPadding
        val topPadding = paddingValues.calculateTopPadding() + streamComponentPadding
        val bottomPadding = contentSpacing + streamComponentPadding + when {
            selectedStreamId != null -> StreamMenuEstimatedHeight
            hasSheetDragContent -> CallSheetEstimatedHeightWithHandle
            else -> CallSheetEstimatedHeight
        }
        val rightPadding = paddingValues.calculateRightPadding(layoutDirection) + streamComponentPadding

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
            val streamViewModel: StreamViewModel = viewModel(
                factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
            )

            val signDocumentsViewModel: SignDocumentsViewModel = viewModel(
                factory = SignDocumentsViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
            )

            val fileShareViewModel: FileShareViewModel = viewModel(
                factory = FileShareViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration, filePickProvider = FilePickBroadcastReceiver)
            )

            val onUserMessageActionClick = remember(streamViewModel) {
                { message: UserMessage ->
                    when (message) {
                        is PinScreenshareMessage -> {
                            streamViewModel.pinStream(message.streamId, prepend = true, force = true); Unit
                        }

                        is SignatureMessage.New -> {
                            if (signDocumentsViewModel.uiState.value.signDocuments.value.fastFilter { it.signState !is SignDocumentUi.SignStateUi.Completed  }.size == 1) {
                                signDocumentsViewModel.signDocument(signDocumentsViewModel.uiState.value.signDocuments.value.first { it.id == message.signId })
                                onSideBarSheetComponentRequest(ModularComponent.SignDocumentView)
                            } else {
                                onSideBarSheetComponentRequest(ModularComponent.SignDocuments)
                            }
                        }

                        is DownloadFileMessage.New -> {
                            fileShareViewModel.download(message.downloadId)
                            onSideBarSheetComponentRequest(ModularComponent.FileShare)
                        }

                        else -> Unit
                    }
                }
            }

            LookaheadScope {
                BoxWithConstraints {
                    val constraints = constraints

                    Row {
                        val sidePanelWeight = remember(sidePanelComponent, isAtLeastExpandedWidth) {
                            when {
                                sidePanelComponent == ModularComponent.Whiteboard -> 4f
                                isAtLeastExpandedWidth -> 1f
                                else -> 2f
                            }
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
                                onStreamItemClick = { streamItem -> onStreamSelected(streamItem.id) },
                                onMoreParticipantClick = { onSideBarSheetComponentRequest(ModularComponent.Participants) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                                    .padding(cutoutPaddingValues)
                                    .padding(
                                        start = leftPadding,
                                        top = topPadding,
                                        end = if (sidePanelComponent != null) 0.dp else rightPadding,
                                        bottom = bottomPadding
                                    )
                            )

                            Column(Modifier.padding(top = topPadding)) {
                                val displayBrandLogo = !windowSizeClass.isAtLeastExpandedWidth() && shouldDisplayBrandLogo(brandLogoUiState.callStateUi, hasConnectedCallOnce)
                                if (displayBrandLogo) {
                                    val brandLogoViewModel: BrandLogoViewModel = viewModel(factory = BrandLogoViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
                                    val brandlogoUiState by brandLogoViewModel.uiState.collectAsStateWithLifecycle()
                                    val brandLogoUri = if (isDarkTheme) brandlogoUiState.logo.dark else brandlogoUiState.logo.light
                                    if (brandLogoUri != null && brandLogoUri != Uri.EMPTY) {
                                        Spacer(modifier = Modifier.height(if (isLargeScreen) 48.dp else 24.dp))
                                        BrandLogoComponent(
                                            viewModel = brandLogoViewModel,
                                            modifier = Modifier
                                                .height(if (isLargeScreen) 96.dp else 48.dp)
                                                .padding(horizontal = if (isLargeScreen) 16.dp else 8.dp)
                                                .fillMaxWidth()
                                        )
                                    }
                                }

                                CallInfoComponent(
                                    modifier = Modifier
                                        .padding(
                                            top = if (displayBrandLogo && hasLogo) 24.dp else 56.dp,
                                            bottom = 16.dp
                                        )
                                        .animateConstraints()
                                        .animatePlacement(this@LookaheadScope)
                                )
                                if (modalSheetComponent != ModularComponent.FileShare
                                    && modalSheetComponent != ModularComponent.Whiteboard
                                    && modalSheetComponent != ModularComponent.SignDocuments
                                    && modalSheetComponent != ModularComponent.SignDocumentView) {
                                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                                }
                            }
                        }

                        sidePanelComponent?.let { component ->
                            SidePanel(
                                modularComponent = component,
                                onDismiss = { onSidePanelComponentRequest(null) },
                                onRequestOtherModularComponent = { onSidePanelComponentRequest(it) },
                                onChatDeleted = {
                                    onSidePanelComponentRequest(null)
                                    onChatDeleted()
                                },
                                onChatCreationFailed = {
                                    onSidePanelComponentRequest(null)
                                    onChatCreationFailed()
                                },
                                onComponentDisplayed = onModularComponentDisplayed,
                                modifier = Modifier
                                    .weight(sidePanelWeight)
                                    .navigationBarsPadding()
                                    .displayCutoutPadding()
                                    .padding(
                                        start = 0.dp,
                                        top = topPadding,
                                        end = rightPadding,
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
                onRequestOtherModularComponent = { onModalSheetComponentRequest(it) },
                onAskInputPermissions = onAskInputPermissions,
                onUserMessageActionClick = onUserMessageActionClick,
                onComponentDisplayed = onModularComponentDisplayed,
                isTesting = isTesting
            )
        }
    }
}

internal fun shouldDisplayBrandLogo(callStateUi: CallStateUi, hasConnectedCallOnce: Boolean) = with(callStateUi) {
    this is CallStateUi.Disconnected.Companion
        || (this is CallStateUi.Connecting && !hasConnectedCallOnce)
        || this is CallStateUi.Ringing
        || this is CallStateUi.Dialing
        || this is CallStateUi.RingingRemotely
        || !hasConnectedCallOnce
}


private fun isSidePanelSupported(modularComponent: ModularComponent?): Boolean {
    return modularComponent.let {
        it == ModularComponent.Chat
            || it == ModularComponent.FileShare
            || it == ModularComponent.Whiteboard
            || it == ModularComponent.Participants
            || it == ModularComponent.SignDocuments
            || it == ModularComponent.SignDocumentView
    }
}

@Composable
private fun LargeScreenInputMessageHost(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(250.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        val messageModifier = Modifier.padding(top = 3.dp)
        InputMessageHost(
            micMessage = { enabled -> MicMessageText(enabled, messageModifier) },
            cameraMessage = { enabled -> CameraMessageText(enabled, messageModifier) },
            fullscreenMessage = { enabled -> FullScreenMessageText(enabled, messageModifier) }
        )
    }
}
