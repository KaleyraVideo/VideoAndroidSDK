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

    fun Context.hasReadPhoneStatePermission() = hasPermission(Manifest.permission.READ_PHONE_STATE)

    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.hasBluetoothPermission() = hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    fun Context.hasContactsPermissions() = hasPermission(Manifest.permission.READ_CONTACTS) && hasPermission(Manifest.permission.WRITE_CONTACTS)

    private fun Context.hasPermission(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED
    }
}