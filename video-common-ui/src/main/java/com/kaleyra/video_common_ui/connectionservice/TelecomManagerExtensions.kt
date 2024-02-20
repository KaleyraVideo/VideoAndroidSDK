package com.kaleyra.video_common_ui.connectionservice

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.kaleyra.video.conference.Call
import com.kaleyra.video.utils.logger.PHONE_CALL
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.getAppName
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasCallPhonePermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasManageOwnCallsPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasReadPhoneNumbersPermission
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger

object TelecomManagerExtensions {

    @RequiresApi(Build.VERSION_CODES.M)
    val kaleyraAddress: Uri = Uri.fromParts(PhoneAccount.SCHEME_SIP, "+1110000000000", null)

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.getPhoneAccountHandle(context: Context): PhoneAccountHandle {
        val componentName = ComponentName(context, KaleyraCallConnectionService::class.java)
        return PhoneAccountHandle(componentName, getConnectionServiceId(context))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.getOrRegisterPhoneAccount(
        context: Context,
        phoneAccountHandle: PhoneAccountHandle
    ): PhoneAccount {
        return getPhoneAccount(phoneAccountHandle) ?: let {
            PhoneAccount
                .builder(phoneAccountHandle, context.getAppName())
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .build()
                .apply { registerPhoneAccount(this) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.addCall(call: Call, logger: PriorityLogger? = null) {
        val context = ContextRetainer.context
        val participants = call.participants.value
        KaleyraCallConnectionService.logger = logger
        when {
            participants.let { it.creator() != it.me && it.creator() != null } -> addIncomingCall(context, kaleyraAddress, logger)
            participants.let { it.creator() == it.me } -> placeOutgoingCall(context, kaleyraAddress, logger)
            else -> logger?.error(PHONE_CALL, message = "No incoming or outgoing call found")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.addIncomingCall(
        context: Context,
        uri: Uri,
        logger: PriorityLogger? = null
    ) {
        if (!context.hasManageOwnCallsPermission()) {
            logger?.error(PHONE_CALL, message = "Missing MANAGE_OWN_CALLS permission")
            return
        }
        if (!context.hasReadPhoneNumbersPermission()) {
            logger?.error(PHONE_CALL, message = "Missing READ_PHONE_NUMBERS permission")
            return
        }

        val accountHandle = getPhoneAccountHandle(context)
        getOrRegisterPhoneAccount(context, accountHandle)
        val extras = Bundle().apply {
            putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
        }
        if (isIncomingCallPermitted(accountHandle)) {
            addNewIncomingCall(accountHandle, extras)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.placeOutgoingCall(
        context: Context,
        uri: Uri,
        logger: PriorityLogger? = null
    ) {
        if (!context.hasCallPhonePermission()) {
            logger?.error(PHONE_CALL, message = "Missing PHONE_CALL permission")
            return
        }
        if (!context.hasManageOwnCallsPermission()) {
            logger?.error(PHONE_CALL, message = "Missing MANAGE_OWN_CALLS permission")
            return
        }
        if (!context.hasReadPhoneNumbersPermission()) {
            logger?.error(PHONE_CALL, message = "Missing READ_PHONE_NUMBERS permission")
            return
        }

        val accountHandle = getPhoneAccountHandle(context)
        getOrRegisterPhoneAccount(context, accountHandle)
        val extras = Bundle().apply {
            putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
        }
        placeCall(uri, extras)
    }

    private fun getConnectionServiceId(context: Context): String {
        return context.applicationContext.packageName + ".connectionService"
    }
}