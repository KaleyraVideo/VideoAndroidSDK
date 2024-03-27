package com.kaleyra.video_common_ui

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.kaleyra.video_common_ui.connectionservice.ContactsController
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasContactsPermissions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class ContactsControllerTest {

    private val contextMock = mockk<Context>()

    private val contentResolverMock = mockk<ContentResolver>(relaxed = true)

    private val contentProviderBuilderMock = mockk<ContentProviderOperation.Builder>()

    private val uriMock = mockk<Uri>()

    @Before
    fun setUp() {
        mockkObject(ContextExtensions)
        mockkStatic(ContentProviderOperation::newInsert)
        mockkStatic(ContentProviderOperation::newUpdate)
        every { uriMock.schemeSpecificPart } returns "scheme"
        every { contextMock.applicationContext.contentResolver } returns contentResolverMock
        every { contentResolverMock.applyBatch(any(), any()) } returns arrayOf()
        every { contentResolverMock.delete(any(), any(), any()) } returns 1
        every { contentResolverMock.applyBatch(any(), any()) } returns arrayOf()
        every { ContentProviderOperation.newInsert(any()) } returns contentProviderBuilderMock
        every { ContentProviderOperation.newUpdate(any()) } returns contentProviderBuilderMock
        every { contentProviderBuilderMock.withValue(any(), any())} returns contentProviderBuilderMock
        every { contentProviderBuilderMock.withValueBackReference(any(), any())} returns contentProviderBuilderMock
        every { contentProviderBuilderMock.withSelection(any(), any())} returns contentProviderBuilderMock
        every { contentProviderBuilderMock.build() } returns mockk()
        every { contextMock.hasContactsPermissions() } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCreateConnectionServiceContact() {
        val username = "username"
        ContactsController.createConnectionServiceContact(
            contextMock,
            uriMock,
            username
        )

        val expected = ArrayList<ContentProviderOperation>()
        expected.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.DIRTY, 0)
                .withValue(ContactsContract.RawContacts.DELETED, 0)
                .build()
        )
        expected.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, username)
                .build()
        )
        expected.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, uriMock.schemeSpecificPart)
                .build()
        )

        verify(exactly = 1) {
            contentResolverMock.applyBatch(
                ContactsContract.AUTHORITY,
                withArg { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun testUpdateConnectionServiceContact() {
        val contactId = 22
        val username = "username"
        ContactsController.updateConnectionServiceContact(
            contextMock,
            contactId,
            username
        )

        val expected = ArrayList<ContentProviderOperation>()
        expected.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.RawContacts._ID + "=?",
                    arrayOf(contactId.toString())
                )
                .withValue(ContactsContract.RawContacts.DELETED, 0)
                .build()
        )
        expected.add(
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

        verify(exactly = 1) {
            contentResolverMock.applyBatch(
                ContactsContract.AUTHORITY,
                withArg { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun contactPermissionDenied_createOrUpdateConnectionServiceContact_fails() {
        mockkObject(ContactsController)
        val contactId = 10
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns contactId
        every { ContactsController.createConnectionServiceContact(any(), any(), any()) } returns Unit
        every { ContactsController.updateConnectionServiceContact(any(), any(), any()) } returns Unit
        every { contextMock.hasContactsPermissions() } returns false

        ContactsController.createOrUpdateConnectionServiceContact(contextMock, uriMock, "username")
        verify(exactly = 0) {
            ContactsController.getConnectionServiceContactId(any(), any())
        }
        verify(exactly = 0) {
            ContactsController.updateConnectionServiceContact(any(), any(), any())
        }
        verify(exactly = 0) {
            ContactsController.createConnectionServiceContact(any(), any(), any())
        }
        unmockkObject(ContactsController)
    }

    @Test
    fun contactNotExisting_createOrUpdateConnectionServiceContact_contactCreated() {
        mockkObject(ContactsController)
        val username = "username"
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns null
        every { ContactsController.createConnectionServiceContact(any(), any(), any()) } returns Unit
        every { ContactsController.updateConnectionServiceContact(any(), any(), any()) } returns Unit

        ContactsController.createOrUpdateConnectionServiceContact(contextMock, uriMock, username)
        verifyOrder {
            ContactsController.getConnectionServiceContactId(contextMock, uriMock)
            ContactsController.createConnectionServiceContact(contextMock, uriMock, username)
        }
        verify(exactly = 0) {
            ContactsController.updateConnectionServiceContact(any(), any(), any())
        }
        unmockkObject(ContactsController)
    }

    @Test
    fun contactAlreadyExisting_createOrUpdateConnectionServiceContact_contactUpdated() {
        mockkObject(ContactsController)
        val contactId = 10
        val username = "username"
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns contactId
        every { ContactsController.createConnectionServiceContact(any(), any(), any()) } returns Unit
        every { ContactsController.updateConnectionServiceContact(any(), any(), any()) } returns Unit

        ContactsController.createOrUpdateConnectionServiceContact(contextMock, uriMock, username)
        verifyOrder {
            ContactsController.getConnectionServiceContactId(contextMock, uriMock)
            ContactsController.updateConnectionServiceContact(contextMock, contactId, username)
        }
        verify(exactly = 0) {
            ContactsController.createConnectionServiceContact(any(), any(), any())
        }
        unmockkObject(ContactsController)
    }

    @Test
    fun queryCursorNull_getConnectionServiceContactId_contactIdNull() {
        every { contentResolverMock.query(any(), any(), any(), any(), any()) } returns null

        val result = ContactsController.getConnectionServiceContactId(contextMock, uriMock)
        verify(exactly = 1) {
            contentResolverMock.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                arrayOf("scheme"),
                null
            )
        }
        assertEquals(null, result)
    }

    @Test
    fun queryCursorRowFound_getConnectionServiceContactId_contactId() {
        val contactId = 10
        val cursorMock = mockk<Cursor>(relaxed = true)
        every { contentResolverMock.query(any(), any(), any(), any(), any()) } returns cursorMock
        every { cursorMock.moveToFirst() } returns true
        every { cursorMock.getInt(any()) } returns contactId

        val result = ContactsController.getConnectionServiceContactId(contextMock, uriMock)
        verifyOrder {
            contentResolverMock.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                arrayOf("scheme"),
                null
            )
            cursorMock.moveToFirst()
            cursorMock.getInt(0)
            cursorMock.close()
        }
        assertEquals(contactId, result)
    }

    @Test
    fun queryCursorRowEmpty_getConnectionServiceContactId_contactId() {
        val cursorMock = mockk<Cursor>(relaxed = true)
        every { contentResolverMock.query(any(), any(), any(), any(), any()) } returns cursorMock
        every { cursorMock.moveToFirst() } returns false

        val result = ContactsController.getConnectionServiceContactId(contextMock, uriMock)
        verifyOrder {
            contentResolverMock.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                arrayOf("scheme"),
                null
            )
            cursorMock.moveToFirst()
        }
        assertEquals(null, result)
    }

    @Test
    fun contactPermissionDenied_deleteConnectionServiceContact_fails() {
        mockkObject(ContactsController)
        every { contextMock.hasContactsPermissions() } returns false
        val contactId = 10
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns contactId

        ContactsController.deleteConnectionServiceContact(contextMock, uriMock)
        verify(exactly = 0) {
            ContactsController.getConnectionServiceContactId(contextMock, uriMock)
        }
        verify(exactly = 0) {
            contentResolverMock.delete(any(), any(), any())
        }
        unmockkObject(ContactsController)
    }

    @Test
    fun contactNotExisting_deleteConnectionServiceContact_deleteIsNotPerformed() {
        mockkObject(ContactsController)
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns null

        ContactsController.deleteConnectionServiceContact(contextMock, uriMock)
        verify(exactly = 0) {
            contentResolverMock.delete(any(), any(), any())
        }
        unmockkObject(ContactsController)
    }

    @Test
    fun contactExisting_deleteConnectionServiceContact_contactDeleted() {
        mockkObject(ContactsController)
        val contactId = 10
        every { ContactsController.getConnectionServiceContactId(any(), any()) } returns contactId

        ContactsController.deleteConnectionServiceContact(contextMock, uriMock)
        verify(exactly = 1) {
            contentResolverMock.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts._ID + "=?",
                arrayOf(contactId.toString())
            )
        }
        unmockkObject(ContactsController)
    }


}