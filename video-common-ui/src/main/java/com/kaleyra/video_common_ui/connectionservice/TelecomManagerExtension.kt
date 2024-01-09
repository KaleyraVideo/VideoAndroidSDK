package com.kaleyra.video_common_ui.connectionservice

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.kaleyra.video_common_ui.connectionservice.ContextExtensions.hasManageOwnCallsPermission
import com.kaleyra.video_common_ui.connectionservice.ContextExtensions.hasReadPhoneNumbersPermission

object TelecomManagerExtension {

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.getPhoneAccountHandle(context: Context): PhoneAccountHandle {
        val componentName = ComponentName(context, PhoneConnectionService::class.java)
        return PhoneAccountHandle(componentName, getConnectionServiceId(context))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.getOrRegisterPhoneAccount(
        context: Context,
        phoneAccountHandle: PhoneAccountHandle
    ): PhoneAccount {
        return getPhoneAccount(phoneAccountHandle) ?: let {
            PhoneAccount
//                .builder(phoneAccountHandle, context.resources.getText(R.string.app_name))
                .builder(phoneAccountHandle, context.getAppName())
                .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                .build()
                .apply { registerPhoneAccount(this) }
        }
    }

    fun Context.getAppName(): CharSequence = applicationInfo.loadLabel(packageManager)

    @RequiresApi(Build.VERSION_CODES.O)
    fun TelecomManager.addIncomingCall(
        context: Context,
        uri: Uri,
        hasVideo: Boolean = false
    ) {
        if (!context.hasManageOwnCallsPermission()) {
            Log.e("TelecomExtensions", "Missing MANAGE_OWN_CALLS permission")
            return
        }
        if (!context.hasReadPhoneNumbersPermission()) {
            Log.e("TelecomExtensions", "Missing READ_PHONE_NUMBERS permission")
            return
        }

        val accountHandle = getPhoneAccountHandle(context)
        getOrRegisterPhoneAccount(context, accountHandle)
//        val callExtras = Bundle().apply {
//            putBoolean(Constants.EXTRA_HAS_VIDEO, hasVideo)
//        }
        val extras = Bundle().apply {
            putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
//            putParcelable(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS, callExtras)
        }
        if (isIncomingCallPermitted(accountHandle)) {
            addNewIncomingCall(accountHandle, extras)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(anyOf = ["android.permission.CALL_PHONE"])
    fun TelecomManager.placeOutgoingCall(
        context: Context,
        uri: Uri,
        hasVideo: Boolean = false
    ) {
        if (!context.hasManageOwnCallsPermission()) {
            Log.e("TelecomExtensions", "Missing MANAGE_OWN_CALLS permission")
            return
        }
        if (!context.hasReadPhoneNumbersPermission()) {
            Log.e("TelecomExtensions", "Missing READ_PHONE_NUMBERS permission")
            return
        }
//        if (!context.hasReadPhoneStatePermission()) {
//            Log.e("TelecomExtensions", "Missing READ_PHONE_STATE permission")
//            return
//        }
//
//        if (!context.hasCallPhonePermission()) {
//            Log.e("TelecomExtensions", "Missing CALL_PHONE permission")
//            return
//        }
//
        val accountHandle = getPhoneAccountHandle(context)
        getOrRegisterPhoneAccount(context, accountHandle)
//        val callExtras = Bundle().apply {
//            putBoolean(Constants.EXTRA_HAS_VIDEO, hasVideo)
//        }
        val extras = Bundle().apply {
            putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
//            putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callExtras)
        }
        placeCall(uri, extras)
    }

    private fun getConnectionServiceId(context: Context): String {
        return context.applicationContext.packageName + ".connectionService"
    }
}