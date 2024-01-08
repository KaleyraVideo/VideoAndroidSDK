package com.kaleyra.video_common_ui.connectionservice

import android.Manifest
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker

object ContextExtensions {

    fun Context.getTelecomManager(): TelecomManager =
        getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager

//    fun Context.hasReadPhoneStatePermission() = hasPermission(Manifest.permission.READ_PHONE_STATE)

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.hasReadPhoneNumbersPermission() = hasPermission(Manifest.permission.READ_PHONE_NUMBERS)

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.hasManageOwnCallsPermission() = hasPermission(Manifest.permission.MANAGE_OWN_CALLS)

//    fun Context.hasCallPhonePermission() = hasPermission(Manifest.permission.CALL_PHONE)

    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.hasBluetoothPermission() = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    fun Context.hasContactsPermissions() = hasPermission(Manifest.permission.READ_CONTACTS) && hasPermission(Manifest.permission.WRITE_CONTACTS)

    private fun Context.hasPermission(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(applicationContext, permission) == PermissionChecker.PERMISSION_GRANTED
    }
}