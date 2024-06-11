package com.kaleyra.video_sdk.ui.call.feedback

import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.toRatingStringRes
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import org.junit.Assert
import org.junit.Test
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.feedbackUiValueFor
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.toSliderValue

class FeedbackUiMappingTest {

    @Test
    fun feedbackUiRatingExcellent_sliderValueFor_5() {
        Assert.assertEquals(5f, FeedbackUiRating.Excellent.toSliderValue())
    }

    @Test
    fun feedbackUiRatingGood_sliderValueFor_4() {
        Assert.assertEquals(4f, FeedbackUiRating.Good.toSliderValue())
    }

    @Test
    fun feedbackUiRatingNeutral_sliderValueFor_3() {
        Assert.assertEquals(3f, FeedbackUiRating.Neutral.toSliderValue())
    }

    @Test
    fun feedbackUiRatingPoor_sliderValueFor_2() {
        Assert.assertEquals(2f, FeedbackUiRating.Poor.toSliderValue())
    }

    @Test
    fun feedbackUiRatingAwful_sliderValueFor_1() {
        Assert.assertEquals(1f, FeedbackUiRating.Awful.toSliderValue())
    }

   @Test
   fun feedbackUiRatingExcellent_toRatingStringRes() {
       Assert.assertEquals(R.string.kaleyra_feedback_excellent, FeedbackUiRating.Excellent.toRatingStringRes())
   }

    @Test
    fun feedbackUiRatingGood_toRatingStringRes() {
        Assert.assertEquals(R.string.kaleyra_feedback_good, FeedbackUiRating.Good.toRatingStringRes())
    }

    @Test
    fun feedbackUiRatingNeutral_toRatingStringRes() {
        Assert.assertEquals(R.string.kaleyra_feedback_neutral, FeedbackUiRating.Neutral.toRatingStringRes())
    }

    @Test
    fun feedbackUiRatingPoor_toRatingStringRes() {
        Assert.assertEquals(R.string.kaleyra_feedback_poor, FeedbackUiRating.Poor.toRatingStringRes())
    }

    @Test
    fun feedbackUiRatingAwful_toRatingStringRes() {
        Assert.assertEquals(R.string.kaleyra_feedback_bad, FeedbackUiRating.Awful.toRatingStringRes())
    }

    @Test
    fun `5f_feedbackValueFor_FeedbackUiRatingExcellent`() {
        Assert.assertEquals(FeedbackUiRating.Excellent, feedbackUiValueFor(5f))
    }

    @Test
    fun `4f_feedbackValueFor_FeedbackUiRatingGood`() {
        Assert.assertEquals(FeedbackUiRating.Good, feedbackUiValueFor(4f))
    }

    @Test
    fun `3f_feedbackValueFor_FeedbackUiRatingNeutral`() {
        Assert.assertEquals(FeedbackUiRating.Neutral, feedbackUiValueFor(3f))
    }

    @Test
    fun `2f_feedbackValueFor_FeedbackUiRatingPoor`() {
        Assert.assertEquals(FeedbackUiRating.Poor, feedbackUiValueFor(2f))
    }

    @Test
    fun `1f_feedbackValueFor_FeedbackUiRatingAwful`() {
        Assert.assertEquals(FeedbackUiRating.Awful, feedbackUiValueFor(1f))
    }
}
