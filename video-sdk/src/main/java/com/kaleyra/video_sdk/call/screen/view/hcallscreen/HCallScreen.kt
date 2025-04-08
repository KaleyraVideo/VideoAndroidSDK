package com.kaleyra.video_sdk.call.screen.view.hcallscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.kaleyra.video_sdk.call.brandlogo.model.hasLogo
import com.kaleyra.video_sdk.call.brandlogo.view.BrandLogoComponent
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickBroadcastReceiver
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.screen.callScreenScaffoldPaddingValues
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheet
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.shouldDisplayBrandLogo
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.call.stream.StreamComponent
import com.kaleyra.video_sdk.call.stream.StreamItemSpacing
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.DownloadFileMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.SignatureMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent

private val CallSheetEstimatedWidth = 76.dp
private val CallSheetEstimatedWidthWithHandle = 87.dp
private val StreamMenuEstimatedWidth = CallSheetEstimatedWidth

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
    isTesting: Boolean = false,
) {
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember(selectedStreamId) { derivedStateOf { selectedStreamId == null && sheetDragActions.value.isNotEmpty() } }
    var isInFullscreenMode by remember { mutableStateOf(false) }

    val contentSpacing = 8.dp
    HCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(top = contentSpacing, left = contentSpacing, right = contentSpacing),
        topAppBar = {
            CallAppBarComponent(
                onParticipantClick = { onModalSheetComponentRequest(ModularComponent.Participants) },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(end = contentSpacing)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                VSheetDragContent(
                    callActions = sheetDragActions,
                    inputPermissions = inputPermissions,
                    onAskInputPermissions = onAskInputPermissions,
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
                            onAskInputPermissions = onAskInputPermissions,
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
        brandLogo = brandLogo@{
            var hasConnectedCallOnce by remember { mutableStateOf(false) }
            val brandLogoViewModel: BrandLogoViewModel = viewModel(factory = BrandLogoViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
            val brandLogoUiState by brandLogoViewModel.uiState.collectAsStateWithLifecycle()
            hasConnectedCallOnce = hasConnectedCallOnce || brandLogoUiState.callStateUi == CallStateUi.Connected

            if (!shouldDisplayBrandLogo(brandLogoUiState.callStateUi, hasConnectedCallOnce)) return@brandLogo

            val isDarkTheme = isSystemInDarkTheme()
            val hasLogo = brandLogoUiState.hasLogo(isDarkTheme)
            if (!hasLogo) return@brandLogo

            val windowInsets = WindowInsets.displayCutout.only(WindowInsetsSides.Start + WindowInsetsSides.Bottom).asPaddingValues()
            BrandLogoComponent(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(windowInsets)
                    .padding(start = 12.dp, bottom = 12.dp)
                    .height(80.dp)
                    .width(142.dp),
                alignment = Alignment.BottomStart
            )
        },
        sheetDragHandle = (@Composable { CallBottomSheetDefaults.VDragHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val horizontalPaddingValues = WindowInsets.navigationBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
            .asPaddingValues()

        val leftPadding = paddingValues.calculateLeftPadding(layoutDirection) - StreamItemSpacing
        val topPadding = paddingValues.calculateTopPadding() + contentSpacing - StreamItemSpacing
        val bottomPadding =  paddingValues.calculateBottomPadding() + contentSpacing - StreamItemSpacing
        val rightPadding = horizontalPaddingValues.calculateRightPadding(layoutDirection) + contentSpacing + StreamItemSpacing + when {
            selectedStreamId != null -> StreamMenuEstimatedWidth
            hasSheetDragContent -> CallSheetEstimatedWidthWithHandle
            else -> CallSheetEstimatedWidth
        }

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
                            signDocumentsViewModel.signDocument(signDocumentsViewModel.uiState.value.signDocuments.value.first())
                            onModalSheetComponentRequest(ModularComponent.SignDocumentView)
                        } else {
                            onModalSheetComponentRequest(ModularComponent.SignDocuments)
                        }
                    }

                    is DownloadFileMessage.New -> {
                        fileShareViewModel.download(message.downloadId)
                        onModalSheetComponentRequest(ModularComponent.FileShare)
                    }

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
                onStreamItemClick = { streamItem -> onStreamSelected(streamItem.id) },
                onMoreParticipantClick = { onModalSheetComponentRequest(ModularComponent.Participants) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = leftPadding,
                        top = topPadding,
                        end = rightPadding,
                        bottom = bottomPadding,
                    )
            )

            Column(
                modifier = Modifier.padding(
                    top = topPadding,
                    start = leftPadding,
                    bottom = bottomPadding,
                    end = rightPadding
                )
            ) {
                CallInfoComponent(
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                if (modalSheetComponent != ModularComponent.FileShare && modalSheetComponent != ModularComponent.Whiteboard) {
                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                }
            }

            InputMessageHost(
                modifier = Modifier
                    .padding(bottom = 16.dp, end = rightPadding)
                    .navigationBarsPadding()
                    .displayCutoutPadding()
                    .align(Alignment.BottomCenter)
            )

            CallScreenModalSheet(
                modularComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentRequest(null) },
                onRequestOtherModularComponent = { onModalSheetComponentRequest(it) },
                onComponentDisplayed = onModularComponentDisplayed,
                onAskInputPermissions = onAskInputPermissions,
                onUserMessageActionClick = onUserMessageActionClick,
                isTesting = isTesting
            )
        }
    }
}