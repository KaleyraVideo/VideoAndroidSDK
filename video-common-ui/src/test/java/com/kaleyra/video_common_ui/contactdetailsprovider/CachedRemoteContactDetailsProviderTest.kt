/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.contactdetailsprovider

import com.kaleyra.video_common_ui.contactdetails.cachedprovider.CachedRemoteContactDetailsProvider
import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import com.kaleyra.video_common_ui.contactdetailsprovider.ContactDetailsTestHelper.assertEqualsContactDetails
import com.kaleyra.video_common_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser1
import com.kaleyra.video_common_ui.contactdetailsprovider.RemoteContactDetailsProviderTestHelper.uriUser2
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CachedRemoteContactDetailsProviderTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `test retrieve cached user contact details`() = runTest(testDispatcher) {
        val contact1 = spyk(RemoteContactDetailsProviderTestHelper.ContactMock("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)))
        val contact2 = spyk(RemoteContactDetailsProviderTestHelper.ContactMock("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2)))
        val contacts = RemoteContactDetailsProviderTestHelper.ContactsMock(hashMapOf("userId1" to contact1, "userId2" to contact2))
        val provider = CachedRemoteContactDetailsProvider(contacts = contacts)
        val result = provider.fetchContactsDetails("userId1")
        val expected = listOf(ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)))
        assertEqualsContactDetails(expected, result)

        val newResult = provider.fetchContactsDetails("userId1", "userId2")
        val newExpected = listOf(
            ContactDetails("userId1", MutableStateFlow("username1"), MutableStateFlow(uriUser1)),
            ContactDetails("userId2", MutableStateFlow("username2"), MutableStateFlow(uriUser2)),
        )
        assertEqualsContactDetails(newExpected, newResult)

        coVerify(exactly = 1) { contact1.displayName }
        coVerify(exactly = 1) { contact1.displayImage }
        coVerify(exactly = 1) { contact2.displayName }
        coVerify(exactly = 1) { contact2.displayImage }
    }
}