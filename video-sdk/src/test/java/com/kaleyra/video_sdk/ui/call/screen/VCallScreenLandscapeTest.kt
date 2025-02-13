package com.kaleyra.video_sdk.ui.call.screen

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// This class contains the same tests as VCallScreenTest but Robolectric is configured with a landscape device
@Config(qualifiers = "w360dp-h640dp-land")
@RunWith(RobolectricTestRunner::class)
class VCallScreenLandscapeTest: VCallScreenTest()