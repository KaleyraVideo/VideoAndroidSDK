package com.kaleyra.video_sdk.viewmodel.call

import android.app.Activity
import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.kaleyra.video_sdk.call.PhoneCallActivity
import com.kaleyra.video_sdk.call.viewmodel.SharedViewModelStore
import com.kaleyra.video_sdk.call.viewmodel.SharedViewModelStoreConfig
import com.kaleyra.video_sdk.chat.PhoneChatActivity
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.assertIsTrue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass

// A dummy ViewModel for testing
class TestViewModel : ViewModel() {
    var initialized = false

    override fun onCleared() {
        initialized = false
        super.onCleared()
    }
}

// Custom ViewModelProvider.Factory for our TestViewModel
class TestViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TestViewModel().apply { initialized = true } as T
    }
}

class SharedViewModelStoreTest {

    private lateinit var mockApplication: Application
    private lateinit var mockViewModelStore: ViewModelStore

    @Before
    fun setUp() {
        SharedViewModelStore.clear(TestViewModel::class)
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk {
            every { thread } returns Thread.currentThread()
        }
        mockApplication = mockk<Application>(relaxed = true)
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockApplication
        mockViewModelStore = mockk(relaxed = true)
    }

    @Test
    fun `getViewModel should create and return a ViewModel if it doesn't exist`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf("ActivityA")

        val viewModel = SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        Assert.assertNotNull(viewModel)
        assertIsTrue(viewModel.initialized)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        Assert.assertNotNull(config!!.viewModelStore)
        Assert.assertEquals(setOf("ActivityA"), config.boundActivities)
    }

    @Test
    fun `getViewModel should return the existing ViewModel if it exists`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities1 = arrayOf("ActivityA")
        val boundActivities2 = arrayOf("ActivityB")

        val viewModel1 = SharedViewModelStore.getViewModel(kClass, factory, *boundActivities1)
        Assert.assertNotNull(viewModel1)

        val viewModel2 = SharedViewModelStore.getViewModel(kClass, factory, *boundActivities2)

        Assert.assertEquals(viewModel1, viewModel2)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        Assert.assertEquals(setOf("ActivityA", "ActivityB"), config!!.boundActivities)
    }

    @Test
    fun `getViewModel should register ActivityLifecycleCallbacks the first time`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf("ActivityA")

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        verify(exactly = 1) { mockApplication.registerActivityLifecycleCallbacks(any()) }

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        Assert.assertNotNull(config!!.activitiesLifecycleCallbacks)
    }

    @Test
    fun `getViewModel should not register ActivityLifecycleCallbacks if already registered`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities1 = arrayOf("ActivityA")
        val boundActivities2 = arrayOf("ActivityB")

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities1)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities2)

        verify(exactly = 1) { mockApplication.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun `clear should clear the ViewModelStore and remove entries from maps`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf(PhoneCallActivity::class.qualifiedName!!)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        val initialConfig = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(initialConfig)
        val mockViewModelStore: ViewModelStore = mockk(relaxed = true) // Mock the one in the config
        val configWithMockedStore = initialConfig!!.copy(viewModelStore = mockViewModelStore)
        replaceSharedViewModelStoreConfig(kClass, configWithMockedStore)

        SharedViewModelStore.clear(kClass)

        verify(exactly = 1) { mockViewModelStore.clear() }

        verify(exactly = 1) { mockApplication.unregisterActivityLifecycleCallbacks(any()) }

        val configAfterClear = getSharedViewModelStoreConfig(kClass)
        Assert.assertNull(configAfterClear)
    }

    @Test
    fun `onActivityDestroyed should clear ViewModel if all bound activities are destroyed`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf(PhoneCallActivity::class.qualifiedName!!, PhoneChatActivity::class.qualifiedName!!)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        val lifecycleCallbacks = config!!.activitiesLifecycleCallbacks as Application.ActivityLifecycleCallbacks // Cast for calling methods
        val mockedViewModelStore: ViewModelStore = mockk(relaxed = true) // Mock the one in the config
        replaceSharedViewModelStoreConfig(kClass, config.copy(viewModelStore = mockedViewModelStore))

        val mockActivityA: Activity = PhoneCallActivity()
        lifecycleCallbacks.onActivityDestroyed(mockActivityA)

        verify(exactly = 0) { mockedViewModelStore.clear() }
        Assert.assertNotNull(getSharedViewModelStoreConfig(kClass))

        val mockActivityB: Activity = PhoneChatActivity()
        lifecycleCallbacks.onActivityDestroyed(mockActivityB)

        verify(exactly = 1) { mockedViewModelStore.clear() }
        Assert.assertNull(getSharedViewModelStoreConfig(kClass))
        verify(exactly = 1) { mockApplication.unregisterActivityLifecycleCallbacks(lifecycleCallbacks) }
    }

    @Test
    fun `onActivityDestroyed should not clear ViewModel if not all bound activities are destroyed`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf(PhoneCallActivity::class.qualifiedName!!, PhoneChatActivity::class.qualifiedName!!)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        val lifecycleCallbacks = config!!.activitiesLifecycleCallbacks as Application.ActivityLifecycleCallbacks
        val mockedViewModelStore: ViewModelStore = mockk(relaxed = true)
        replaceSharedViewModelStoreConfig(kClass, config.copy(viewModelStore = mockedViewModelStore))

        val mockActivityA: Activity = PhoneCallActivity()
        lifecycleCallbacks.onActivityDestroyed(mockActivityA)

        verify(exactly = 0) { mockedViewModelStore.clear() }
        Assert.assertNotNull(getSharedViewModelStoreConfig(kClass))
        verify(exactly = 0) { mockApplication.unregisterActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun `onActivityDestroyed should not clear ViewModel if destroyed activity is not bound`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf(PhoneCallActivity::class.qualifiedName!!)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        val lifecycleCallbacks = config!!.activitiesLifecycleCallbacks as Application.ActivityLifecycleCallbacks
        val mockViewModelStore: ViewModelStore = mockk(relaxed = true)
        replaceSharedViewModelStoreConfig(kClass, config.copy(viewModelStore = mockViewModelStore))

        val mockActivity = Activity()
        lifecycleCallbacks.onActivityDestroyed(mockActivity)

        verify(exactly = 0) { mockViewModelStore.clear() }
        Assert.assertNotNull(getSharedViewModelStoreConfig(kClass))
        verify(exactly = 0) { mockApplication.unregisterActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun `onActivityCreated and onActivityStarted should mark activities as active`() {
        val kClass = TestViewModel::class
        val factory = TestViewModelFactory()
        val boundActivities = arrayOf(PhoneCallActivity::class.qualifiedName!!, PhoneChatActivity::class.qualifiedName!!)

        SharedViewModelStore.getViewModel(kClass, factory, *boundActivities)

        val config = getSharedViewModelStoreConfig(kClass)
        Assert.assertNotNull(config)
        val lifecycleCallbacks = config!!.activitiesLifecycleCallbacks as Application.ActivityLifecycleCallbacks

        val mockActivityA: Activity = PhoneCallActivity()
        lifecycleCallbacks.onActivityCreated(mockActivityA, null)

        val mockActivityB: Activity = PhoneChatActivity()
        lifecycleCallbacks.onActivityCreated(mockActivityB, null)

        val viewModelStoreInConfig: ViewModelStore = mockk(relaxed = true)
        replaceSharedViewModelStoreConfig(kClass, config.copy(viewModelStore = viewModelStoreInConfig))
        Assert.assertNotNull(getSharedViewModelStoreConfig(kClass))

        lifecycleCallbacks.onActivityStarted(mockActivityA)
        lifecycleCallbacks.onActivityStarted(mockActivityB)
        verify(exactly = 0) { viewModelStoreInConfig.clear() }

        lifecycleCallbacks.onActivityDestroyed(mockActivityA)
        lifecycleCallbacks.onActivityDestroyed(mockActivityB)

        verify(exactly = 1) { viewModelStoreInConfig.clear() }
        Assert.assertNull(getSharedViewModelStoreConfig(kClass))
    }

    private fun getSharedViewModelStoreConfig(kClass: KClass<*>): SharedViewModelStoreConfig? {
        val sharedViewModelStoreConfigMapField = SharedViewModelStore::class.java.getDeclaredField("sharedViewModelStoreConfigMap")
        sharedViewModelStoreConfigMapField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val map = sharedViewModelStoreConfigMapField.get(SharedViewModelStore) as HashMap<KClass<*>, SharedViewModelStoreConfig>
        return map[kClass]
    }

    private fun replaceSharedViewModelStoreConfig(kClass: KClass<*>, config: SharedViewModelStoreConfig) {
        val sharedViewModelStoreConfigMapField = SharedViewModelStore::class.java.getDeclaredField("sharedViewModelStoreConfigMap")
        sharedViewModelStoreConfigMapField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val map = sharedViewModelStoreConfigMapField.get(SharedViewModelStore) as HashMap<KClass<*>, SharedViewModelStoreConfig>
        map[kClass] = config
    }
}
