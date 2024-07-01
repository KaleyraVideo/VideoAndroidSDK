package com.kaleyra.video_common_ui

/**
 * VoicePrompts represents the option to enable or disable the reproduction
 * of a vocal message indicating some salient events occurring in the sdk usage.
 * Voice prompt messages includes the message alert when a call recording starts or ends
 * and the alert message indicating that the call recording will start as soon as the call will be connected as well as the message indicating
 * that you are the solely user in the call awaiting for other participants.
 * Voice prompts are played only when the call is an audio call or the screen is off, so the
 * user cannot see the message on the UI.
 * This option is useful fot those use cases in which an operator will declare the starting and stopping of the call recording anyway.
 */
enum class VoicePrompts {

    /**
     * The voice prompts messages will be played if the call is and audio only call or the screen is off
     */
    Enabled,

    /**
     * The voice prompts messages will not be played
     */
    Disabled
}