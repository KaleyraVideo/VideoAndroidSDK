package com.kaleyra.video_common_ui.utils.extensions

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.app.ActivityManager.RecentTaskInfo
import android.content.ComponentName
import android.content.Context
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasOtherTasks
import com.kaleyra.video_common_ui.utils.mockSdkInt
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContextExtensionsTest {

    private val mockkContext = mockk<Context>()
    private val mockActivityManager = mockk<ActivityManager>()

    @Test
    fun appHasOtherTasksApi23_hasOtherTasks_true() {
        mockSdkInt(23)
        val currentTaskActivityName = "testActivity"
        every { mockkContext.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager } returns mockActivityManager
        every { mockActivityManager.appTasks } returns ArrayList<ActivityManager.AppTask?>().apply {
            add(mockk<AppTask> {
                every { taskInfo } returns RecentTaskInfo().apply {
                    topActivity = mockk<ComponentName> {
                        every { className } returns currentTaskActivityName
                    }
                }
            })
            add(mockk<AppTask> {
                every { taskInfo } returns RecentTaskInfo().apply {
                    topActivity = mockk<ComponentName> {
                        every { className } returns "mainActivity"
                    }
                }
            })
            add(mockk<AppTask> {
                every { taskInfo } returns RecentTaskInfo().apply {
                    topActivity = null
                }
            })
            add(mockk<AppTask> {
                every { taskInfo } returns RecentTaskInfo().apply {
                    topActivity = mockk<ComponentName> {
                        every { className } returns "otherActivity"
                    }
                }
            })
        }

        val hasOtherTasks = mockkContext.hasOtherTasks(currentTaskActivityName)

        Assert.assertEquals(true, hasOtherTasks)
    }

    @Test
    fun appHasOtherTasksApi23_hasNotOtherTasks_false() {
        mockSdkInt(23)
        val currentTaskActivityName = "testActivity"
        every { mockkContext.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager } returns mockActivityManager
        every { mockActivityManager.appTasks } returns ArrayList<ActivityManager.AppTask?>().apply {
            add(mockk<AppTask> {
                every { taskInfo } returns RecentTaskInfo().apply {
                    topActivity = mockk<ComponentName> {
                        every { className } returns currentTaskActivityName
                    }
                }
                add(mockk<AppTask> {
                    every { taskInfo } returns RecentTaskInfo().apply {
                        topActivity = null
                    }
                })
            })
        }

        val hasOtherTasks = mockkContext.hasOtherTasks(currentTaskActivityName)

        Assert.assertEquals(false, hasOtherTasks)
    }

    @Test
    fun appHasOtherTasksApi21_hasOtherTasks_true() {
        mockSdkInt(21)
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk {
            every { packageName } returns "com.kaleyra.myapp"
        }
        val currentTaskActivityName = "testActivity"
        every { mockkContext.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager } returns mockActivityManager
        every { mockActivityManager.getRunningTasks(any()) } returns ArrayList<ActivityManager.RunningTaskInfo>().apply {
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { className } returns currentTaskActivityName
                    every { packageName } returns ContextRetainer.context.packageName
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { className } returns "mainActivity"
                    every { packageName } returns ContextRetainer.context.packageName
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { className } returns "otherActivity"
                    every { packageName } returns ContextRetainer.context.packageName
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { packageName } returns "com.example.other"
                    every { className } returns "otherActivity"
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = null
            })
        }

        val hasOtherTasks = mockkContext.hasOtherTasks(currentTaskActivityName)

        Assert.assertEquals(true, hasOtherTasks)
    }

    @Test
    fun appHasOtherTasksApi21_hasNotOtherTasks_false() {
        mockSdkInt(21)
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk {
            every { packageName } returns "com.kaleyra.myapp"
        }
        val currentTaskActivityName = "testActivity"
        every { mockkContext.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager } returns mockActivityManager
        every { mockActivityManager.getRunningTasks(any()) } returns ArrayList<ActivityManager.RunningTaskInfo>().apply {
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { className } returns currentTaskActivityName
                    every { packageName } returns ContextRetainer.context.packageName
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = mockk<ComponentName> {
                    every { packageName } returns "com.example.other"
                    every { className } returns "otherActivity"
                }
            })
            add(ActivityManager.RunningTaskInfo().apply {
                topActivity = null
            })
        }

        val hasOtherTasks = mockkContext.hasOtherTasks(currentTaskActivityName)

        Assert.assertEquals(false, hasOtherTasks)
    }
}
