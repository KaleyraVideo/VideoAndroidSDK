package com.kaleyra.video_common_ui

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.telecom.CallAudioState
import androidx.test.core.app.ApplicationProvider
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputDevices
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToBluetoothDevice
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToCurrentAudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.supportCallAudioStateRoute
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasBluetoothPermission
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CallAudioStateExtensionsTest {

    @Test
    fun maskSupportRoute_hasCallAudioStateRoute_true() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, (CallAudioState.ROUTE_EARPIECE or CallAudioState.ROUTE_WIRED_HEADSET))
        assertEquals(
            true,
            callAudioState.supportCallAudioStateRoute(CallAudioState.ROUTE_EARPIECE)
        )
    }

    @Test
    fun maskDoNotSupportRoute_hasCallAudioStateRoute_false() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, (CallAudioState.ROUTE_SPEAKER or CallAudioState.ROUTE_WIRED_HEADSET))
        assertEquals(
            false,
            callAudioState.supportCallAudioStateRoute(CallAudioState.ROUTE_EARPIECE)
        )
    }

    @Test
    fun emptySupportedRouteMask_mapToAvailableAudioOutputDevices_onlyMuteAudioOutput() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, 0)
        assertEquals(
            listOf(AudioOutputDevice.None()),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
    }

    @Test
    fun routeMaskSupportsEarpiece_mapToAvailableAudioOutputDevices_earpieceOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_EARPIECE)
        assertEquals(
            listOf(AudioOutputDevice.None(), AudioOutputDevice.Earpiece()),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
    }

    @Test
    fun routeMaskSupportsSpeaker_mapToAvailableAudioOutputDevices_speakerOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_SPEAKER)
        assertEquals(
            listOf(AudioOutputDevice.None(), AudioOutputDevice.Loudspeaker()),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
    }

    @Test
    fun routeMaskSupportsWiredHeadset_mapToAvailableAudioOutputDevices_wiredHeadsetOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_WIRED_HEADSET)
        assertEquals(
            listOf(AudioOutputDevice.None(), AudioOutputDevice.WiredHeadset()),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun routeMaskSupportsBluetoothApi27_mapToAvailableAudioOutputDevices_bluetoothOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_BLUETOOTH)
        assertEquals(
            listOf(AudioOutputDevice.None(), AudioOutputDevice.Bluetooth("")),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun routeMaskSupportsBluetooth_mapToAvailableAudioOutputDevices_bluetoothOutputAvailable() {
        mockkObject(CallAudioStateExtensions)
        val device = mockk<BluetoothDevice>()
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_BLUETOOTH))
        every { callAudioState.supportedBluetoothDevices } returns listOf(device)
        every { device.mapToBluetoothDevice(any()) } returns AudioOutputDevice.Bluetooth("id")
        assertEquals(
            listOf(AudioOutputDevice.None(), AudioOutputDevice.Bluetooth("id")),
            callAudioState.mapToAvailableAudioOutputDevices()
        )
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    fun mutedState_mapToCurrentAudioOutput_mutedOutput() {
        val callAudioState = spyk(CallAudioState(true, CallAudioState.ROUTE_WIRED_OR_EARPIECE, 0))
        assertEquals(
            AudioOutputDevice.None(),
            callAudioState.mapToCurrentAudioOutputDevice()
        )
    }

    @Test
    fun earpieceRoute_mapToCurrentAudioOutput_earpieceOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_EARPIECE, 0))
        assertEquals(
            AudioOutputDevice.Earpiece(),
            callAudioState.mapToCurrentAudioOutputDevice()
        )
    }

    @Test
    fun speakerRoute_mapToCurrentAudioOutput_speakerOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_SPEAKER, 0))
        assertEquals(
            AudioOutputDevice.Loudspeaker(),
            callAudioState.mapToCurrentAudioOutputDevice()
        )
    }

    @Test
    fun wiredHeadsetRoute_mapToCurrentAudioOutput_wiredHeadsetOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, 0))
        assertEquals(
            AudioOutputDevice.WiredHeadset(),
            callAudioState.mapToCurrentAudioOutputDevice()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun bluetoothRouteApi27_mapToCurrentAudioOutput_bluetoothOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_BLUETOOTH, 0))
        assertEquals(
            AudioOutputDevice.Bluetooth(""),
            callAudioState.mapToCurrentAudioOutputDevice()
        )
    }

    @Test
    fun bluetoothRoute_mapToCurrentAudioOutput_bluetoothOutput() {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_BLUETOOTH, 0))
        val bluetoothDevice = mockk<BluetoothDevice>()
        val bluetoothOutput = mockk<AudioOutputDevice.Bluetooth>()
        every { callAudioState.activeBluetoothDevice } returns bluetoothDevice
        every { bluetoothDevice.mapToBluetoothDevice(listOf()) } returns bluetoothOutput
        assertEquals(
            bluetoothOutput,
            callAudioState.mapToCurrentAudioOutputDevice()
        )
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun api30AndNoBluetoothPermission_mapToBluetoothOutput_bluetoothAudioOutput() {
        mockkObject(ContextRetainer)
        val context = mockk<Context>()
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { ContextRetainer.context } returns context
        every { context.hasBluetoothPermission() } returns false
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice.name } returns "name"
        assertEquals(
            AudioOutputDevice.Bluetooth(identifier = "address").apply { name = "name" },
            bluetoothDevice.mapToBluetoothDevice(listOf())
        )
        unmockkObject(ContextRetainer)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun api31AndNoBluetoothPermission_mapToBluetoothOutput_bluetoothAudioOutputWithNoName() {
        mockkObject(ContextRetainer)
        mockkObject(ContextExtensions)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { ContextRetainer.context } returns context
        every { context.hasBluetoothPermission() } returns false
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice.name } returns "name"
        val text = context.getString(com.bandyer.android_audiosession.R.string.bandyer_audio_device_type_bluetooth)
        assertEquals(
            AudioOutputDevice.Bluetooth(identifier = "address").apply { name = "$text 1" },
            bluetoothDevice.mapToBluetoothDevice(listOf(bluetoothDevice))
        )
        unmockkObject(ContextRetainer)
        unmockkObject(ContextExtensions)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun api31AndBluetoothPermission_mapToBluetoothOutput_bluetoothAudioOutputWithName() {
        mockkObject(ContextRetainer)
        mockkObject(ContextExtensions)
        val context = mockk<Context>()
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { ContextRetainer.context } returns context
        every { context.hasBluetoothPermission() } returns true
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice.name } returns "name"
        assertEquals(
            AudioOutputDevice.Bluetooth(identifier = "address").apply { name = "name" },
            bluetoothDevice.mapToBluetoothDevice(listOf())
        )
        unmockkObject(ContextRetainer)
        unmockkObject(ContextExtensions)
    }
}