package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Input
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.toNoiseFilerMode
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import org.junit.Assert
import org.junit.Test

class DeepFilterNetModuleLoaderMapperTest {

    @Test
    fun testNoiseFilterModeUiToNoiseFilterMode() {
        Assert.assertEquals(Input.Audio.My.NoiseFilterMode.DeepFilterAi, NoiseFilterModeUi.DeepFilterAi.toNoiseFilerMode())
        Assert.assertEquals(Input.Audio.My.NoiseFilterMode.Standard, NoiseFilterModeUi.Standard.toNoiseFilerMode())
    }
}