package com.kaleyra.video_sdk.call.viewmodel

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.kaleyra.video_utils.ContextRetainer
import kotlin.reflect.KClass

internal data class SharedViewModelStoreConfig(
    val viewModelStore: ViewModelStore,
    val activitiesLifecycleCallbacks: ActivityLifecycleCallbacks? = null,
    val boundActivities: Set<String> = setOf()
)

object SharedViewModelStore {

    private val application: Application
        get() = ContextRetainer.context as Application

    private val sharedViewModelStoreConfigMap = hashMapOf<KClass<*>, SharedViewModelStoreConfig>()

    private fun getActivityLifecycleCallbacks(kClass: KClass<*>, activities: Array<out String>): ActivityLifecycleCallbacks {
        with (sharedViewModelStoreConfigMap[kClass]) {
            this ?: return@with
            this.activitiesLifecycleCallbacks ?: return@with
            sharedViewModelStoreConfigMap.replace(kClass, this.copy(boundActivities = this.boundActivities.plus(activities)))
            return this.activitiesLifecycleCallbacks
        }

        val activityLifecycleCallbacks = object: ActivityLifecycleCallbacks {
            private var destroyedActivities = mutableListOf<String>()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = markActivityAsActive(activity)
            override fun onActivityStarted(activity: Activity) = markActivityAsActive(activity)
            private fun markActivityAsActive(activity: Activity) {
                destroyedActivities.remove(activity::class.java.name)
            }
            override fun onActivityDestroyed(activity: Activity) {
                val boundActivities = sharedViewModelStoreConfigMap[kClass]!!.boundActivities
                if (activity::class.java.name in boundActivities) destroyedActivities.add(activity::class.java.name)
                if (boundActivities.all { it in destroyedActivities }) clear(kClass)
            }
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        }
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        sharedViewModelStoreConfigMap.replace(
            kClass,
            sharedViewModelStoreConfigMap[kClass]!!.copy(
                activitiesLifecycleCallbacks = activityLifecycleCallbacks,
                boundActivities = activities.toSet()))

        return activityLifecycleCallbacks
    }

    @Synchronized
    fun <T : ViewModel> getViewModel(
        kClass: KClass<T>,
        viewModelFactory: ViewModelProvider.Factory,
        vararg boundActivities: String
    ): T {
        val viewModelStore =
            sharedViewModelStoreConfigMap[kClass]?.viewModelStore
                ?: ViewModelStore().apply {
                    sharedViewModelStoreConfigMap[kClass] = SharedViewModelStoreConfig(this)
                }
        getActivityLifecycleCallbacks(kClass, boundActivities)
        return ViewModelProvider(
            store = viewModelStore,
            factory = viewModelFactory
        )[kClass]
    }

    @Synchronized
    fun clear(kClass: KClass<*>) {
        sharedViewModelStoreConfigMap[kClass]?.viewModelStore?.clear()
        sharedViewModelStoreConfigMap[kClass]?.activitiesLifecycleCallbacks?.let {
            application.unregisterActivityLifecycleCallbacks(it)
        }
        sharedViewModelStoreConfigMap.remove(kClass)
    }
}