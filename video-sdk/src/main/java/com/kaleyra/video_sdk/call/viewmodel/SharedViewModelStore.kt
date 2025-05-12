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

object SharedViewModelStore {

    private val viewModelStoreMap = hashMapOf<KClass<*>, ViewModelStore>()
    private val activityLifecycleCallbacksMap = hashMapOf<KClass<*>, ActivityLifecycleCallbacks>()
    private val boundActivitiesMap = hashMapOf<KClass<*>, MutableSet<String>>()

    private fun getActivityLifecycleCallbacks(kClass: KClass<*>, activities: Array<out String>): ActivityLifecycleCallbacks {
        if (activityLifecycleCallbacksMap[kClass] != null) {
            boundActivitiesMap[kClass]!!.addAll(activities)
            return activityLifecycleCallbacksMap[kClass]!!
        }
        val application = (ContextRetainer.context as Application)
        val activityLifecycleCallbacks = object: ActivityLifecycleCallbacks {
            private var destroyedActivities = mutableListOf<String>()
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = markActivityAsActive(activity)
            override fun onActivityStarted(activity: Activity) = markActivityAsActive(activity)
            private fun markActivityAsActive(activity: Activity) {
                destroyedActivities.remove(activity::class.java.name)
            }
            override fun onActivityDestroyed(activity: Activity) {
                if (activity::class.java.name in boundActivitiesMap[kClass]!!) destroyedActivities.add(activity::class.java.name)
                if (boundActivitiesMap[kClass]!!.all { it in destroyedActivities }) {
                    application.unregisterActivityLifecycleCallbacks(this)
                    clear(kClass)
                }
            }
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        }
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        activityLifecycleCallbacksMap[kClass] = activityLifecycleCallbacks
        boundActivitiesMap[kClass] = activities.toMutableSet()
        return activityLifecycleCallbacks
    }

    @Synchronized
    fun <T : ViewModel> getViewModel(
        kClass: KClass<T>,
        viewModelFactory: ViewModelProvider.Factory,
        vararg boundActivities: String
    ): T {
        val viewModelStore = viewModelStoreMap[kClass] ?: ViewModelStore().apply { viewModelStoreMap[kClass] = this }
        getActivityLifecycleCallbacks(kClass, boundActivities)
        return ViewModelProvider(
            store = viewModelStore,
            factory = viewModelFactory
        )[kClass]
    }

    @Synchronized
    fun clear(kClass: KClass<*>) {
        viewModelStoreMap[kClass]?.clear()
        boundActivitiesMap.remove(kClass)
        activityLifecycleCallbacksMap.remove(kClass)
    }
}