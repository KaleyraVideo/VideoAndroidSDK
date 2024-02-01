package com.kaleyra.video_common_ui.connectionservice

import android.content.ContentProviderOperation
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasContactsPermissions

object ContactsController {

    fun createOrUpdateConnectionServiceContact(
        context: Context,
        uri: Uri,
        username: String
    ) {
        if (!context.hasContactsPermissions()) return
        try {
            val contactId = getConnectionServiceContactId(context, uri)
            if (contactId != null) {
                updateConnectionServiceContact(context, contactId, username)
            } else {
                createConnectionServiceContact(context, uri, username)
            }
        } catch (_: Exception) {

        }
    }

    fun getConnectionServiceContactId(context: Context, uri: Uri): Int? {
        val resolver = context.applicationContext.contentResolver
        val cursor = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
            ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
            arrayOf(uri.schemeSpecificPart),
            null
        )
        val contactId = if (cursor != null && cursor.moveToFirst()) cursor.getInt(0) else null
        cursor?.close()
        return contactId
    }

    fun updateConnectionServiceContact(
        context: Context,
        contactId: Int,
        username: String
    ) {
        val resolver = context.applicationContext.contentResolver
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.RawContacts._ID + "=?",
                    arrayOf(contactId.toString())
                )
                .withValue(ContactsContract.RawContacts.DELETED, 0)
                .build()
        )
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                    arrayOf(
                        contactId.toString(),
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, username)
                .build()
        )
        resolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }

    fun createConnectionServiceContact(
        context: Context,
        uri: Uri,
        username: String
    ) {
        val resolver = context.applicationContext.contentResolver
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.DIRTY, 0)
                .withValue(ContactsContract.RawContacts.DELETED, 0)
                .build()
        )
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, username)
                .build()
        )
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, uri.schemeSpecificPart)
                .build()
        )
        resolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }

    fun deleteConnectionServiceContact(context: Context, uri: Uri) {
        if (!context.hasContactsPermissions()) return
        try {
            val resolver = context.applicationContext.contentResolver
            val contactId = getConnectionServiceContactId(context, uri) ?: return
            resolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts._ID + "=?",
                arrayOf(contactId.toString())
            )
        } catch (_: Exception) {

        }
    }
}
