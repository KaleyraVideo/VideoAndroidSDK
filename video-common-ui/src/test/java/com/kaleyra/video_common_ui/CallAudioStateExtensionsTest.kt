package com.kaleyra.video_common_ui

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.telecom.CallAudioState
import com.kaleyra.video_common_ui.connectionservice.CallAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputs
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToBluetoothOutput
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToCurrentAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.supportCallAudioStateRoute
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
    fun emptySupportedRouteMask_mapToAvailableAudioOutputs_onlyMuteAudioOutput() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, 0)
        assertEquals(
            listOf(CallAudioOutput.Muted),
            callAudioState.mapToAvailableAudioOutputs()
        )
    }

    @Test
    fun routeMaskSupportsEarpiece_mapToAvailableAudioOutputs_earpieceOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_EARPIECE)
        assertEquals(
            listOf(CallAudioOutput.Muted, CallAudioOutput.Earpiece),
            callAudioState.mapToAvailableAudioOutputs()
        )
    }

    @Test
    fun routeMaskSupportsSpeaker_mapToAvailableAudioOutputs_speakerOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_SPEAKER)
        assertEquals(
            listOf(CallAudioOutput.Muted, CallAudioOutput.Speaker),
            callAudioState.mapToAvailableAudioOutputs()
        )
    }

    @Test
    fun routeMaskSupportsWiredHeadset_mapToAvailableAudioOutputs_wiredHeadsetOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_WIRED_HEADSET)
        assertEquals(
            listOf(CallAudioOutput.Muted, CallAudioOutput.WiredHeadset),
            callAudioState.mapToAvailableAudioOutputs()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun routeMaskSupportsBluetoothApi27_mapToAvailableAudioOutputs_bluetoothOutputAvailable() {
        val callAudioState = CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_BLUETOOTH)
        assertEquals(
            listOf(CallAudioOutput.Muted, CallAudioOutput.Bluetooth("")),
            callAudioState.mapToAvailableAudioOutputs()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun routeMaskSupportsBluetooth_mapToAvailableAudioOutputs_bluetoothOutputAvailable() {
        mockkObject(CallAudioStateExtensions)
        val device = mockk<BluetoothDevice>()
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_WIRED_OR_EARPIECE, CallAudioState.ROUTE_BLUETOOTH))
        every { callAudioState.supportedBluetoothDevices } returns listOf(device)
        every { device.mapToBluetoothOutput() } returns CallAudioOutput.Bluetooth("id")
        assertEquals(
            listOf(CallAudioOutput.Muted, CallAudioOutput.Bluetooth("id")),
            callAudioState.mapToAvailableAudioOutputs()
        )
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    fun mutedState_mapToCurrentAudioOutput_mutedOutput() {
        val callAudioState = spyk(CallAudioState(true, CallAudioState.ROUTE_WIRED_OR_EARPIECE, 0))
        assertEquals(
            CallAudioOutput.Muted,
            callAudioState.mapToCurrentAudioOutput()
        )
    }

    @Test
    fun earpieceRoute_mapToCurrentAudioOutput_earpieceOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_EARPIECE, 0))
        assertEquals(
            CallAudioOutput.Earpiece,
            callAudioState.mapToCurrentAudioOutput()
        )
    }

    @Test
    fun speakerRoute_mapToCurrentAudioOutput_speakerOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_SPEAKER, 0))
        assertEquals(
            CallAudioOutput.Speaker,
            callAudioState.mapToCurrentAudioOutput()
        )
    }

    @Test
    fun wiredHeadsetRoute_mapToCurrentAudioOutput_wiredHeadsetOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_WIRED_HEADSET, 0))
        assertEquals(
            CallAudioOutput.WiredHeadset,
            callAudioState.mapToCurrentAudioOutput()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun bluetoothRouteApi27_mapToCurrentAudioOutput_bluetoothOutput() {
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_BLUETOOTH, 0))
        assertEquals(
            CallAudioOutput.Bluetooth(""),
            callAudioState.mapToCurrentAudioOutput()
        )
    }

    @Test
    fun bluetoothRoute_mapToCurrentAudioOutput_bluetoothOutput() {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = spyk(CallAudioState(false, CallAudioState.ROUTE_BLUETOOTH, 0))
        val bluetoothDevice = mockk<BluetoothDevice>()
        val bluetoothOutput = mockk<CallAudioOutput.Bluetooth>()
        every { callAudioState.activeBluetoothDevice } returns bluetoothDevice
        every { bluetoothDevice.mapToBluetoothOutput() } returns bluetoothOutput
        assertEquals(
            bluetoothOutput,
            callAudioState.mapToCurrentAudioOutput()
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
            CallAudioOutput.Bluetooth(id = "address", name = "name"),
            bluetoothDevice.mapToBluetoothOutput()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun api31AndNoBluetoothPermission_mapToBluetoothOutput_bluetoothAudioOutputWithNoName() {
        mockkObject(ContextRetainer)
        val context = mockk<Context>()
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { ContextRetainer.context } returns context
        every { context.hasBluetoothPermission() } returns false
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice.name } returns "name"
        assertEquals(
            CallAudioOutput.Bluetooth(id = "address"),
            bluetoothDevice.mapToBluetoothOutput()
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun api31AndBluetoothPermission_mapToBluetoothOutput_bluetoothAudioOutputWithName() {
        mockkObject(ContextRetainer)
        val context = mockk<Context>()
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { ContextRetainer.context } returns context
        every { context.hasBluetoothPermission() } returns true
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice.name } returns "name"
        assertEquals(
            CallAudioOutput.Bluetooth(id = "address", name = "name"),
            bluetoothDevice.mapToBluetoothOutput()
        )
    }
}