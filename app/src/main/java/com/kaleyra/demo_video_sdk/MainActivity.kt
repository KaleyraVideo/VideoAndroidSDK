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
package com.kaleyra.demo_video_sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.kaleyra.app_configuration.activities.BaseConfigurationActivity
import com.kaleyra.app_configuration.activities.ConfigurationActivity
import com.kaleyra.app_configuration.model.CallOptionsType
import com.kaleyra.app_configuration.model.Configuration
import com.kaleyra.app_utilities.notification.NotificationProxy
import com.kaleyra.app_utilities.notification.requestFullscreenPermissionActivityApi34
import com.kaleyra.app_utilities.notification.requestPushNotificationPermissionApi33
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.app_utilities.storage.LoginManager
import com.kaleyra.app_utilities.storage.LoginManager.isUserLogged
import com.kaleyra.demo_video_sdk.R.id
import com.kaleyra.demo_video_sdk.R.layout
import com.kaleyra.demo_video_sdk.R.string
import com.kaleyra.demo_video_sdk.databinding.ActivityMainBinding
import com.kaleyra.demo_video_sdk.notification.MissedNotificationPayloadWorker
import com.kaleyra.demo_video_sdk.storage.DefaultConfigurationManager
import com.kaleyra.demo_video_sdk.ui.activities.CollapsingToolbarActivity
import com.kaleyra.demo_video_sdk.ui.adapter_items.NoUserSelectedItem
import com.kaleyra.demo_video_sdk.ui.adapter_items.SelectedUserItem
import com.kaleyra.demo_video_sdk.ui.adapter_items.UserSelectionItem
import com.kaleyra.demo_video_sdk.ui.custom_views.CallConfiguration
import com.kaleyra.demo_video_sdk.ui.custom_views.CustomConfigurationDialog
import com.kaleyra.video.State
import com.kaleyra.video.State.Disconnected
import com.kaleyra.video.Synchronization
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Call.Recording
import com.kaleyra.video.conference.Conference
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_utils.ContextRetainer
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.extensions.ExtensionsFactories
import com.mikepenz.fastadapter.listeners.ItemFilterListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.SelectExtensionFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * This Activity will be called after the user has logged or if an external url was opened with this app.
 * It's main job is to redirect to the dialing(outgoing) or ringing(ringing) call activities.
 *
 * @author kristiyan
 */
class MainActivity : CollapsingToolbarActivity(), OnQueryTextListener, OnRefreshListener {
    private val TAG = "MainActivity"
    private var itemAdapter: ItemAdapter<UserSelectionItem>? = null
    private var fastAdapter: FastAdapter<UserSelectionItem>? = null
    private var calleeSelected = ArrayList<String>()
    private val selectedUsersItemAdapter: ItemAdapter<IItem<*>> = ItemAdapter()
    private var configuration: Configuration? = null
    private var binding: ActivityMainBinding? = null
    var searchView: SearchView? = null
    private val usersList: ArrayList<UserSelectionItem> = ArrayList()

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showConfirmDialog(string.logout, string.logout_confirmation) { _: DialogInterface?, i: Int -> logout() }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configuration = ConfigurationPrefsManager.getConfiguration(this)
        if (LoginManager.isUserLogged(this)) {
            requestPushNotificationPermissionApi33 {
                MainScope().launch { delay(1500); requestFullscreenPermissionActivityApi34() }
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // inflate main layout and keep a reference to it in case of use with dpad navigation
        setContentView(layout.activity_main)
        binding = ActivityMainBinding.bind(window.decorView)

        // If the user is already logged, setup the activity.
        setUpRecyclerView()

        // get the user that is currently logged in the sample app
        val userAlias: String = LoginManager.getLoggedUser(this)

        // customize toolbar
        setCollapsingToolbarTitle(String.format(resources.getString(string.pick_users), userAlias), userAlias)

        // If FCM is not being used as the default notification service.
        // We need to launch the other notification services in the main launcher activity.
        NotificationProxy.listen(this)

        // in case the MainActivity has been shown by opening an external link, handle it
        handleOpenUrl(intent)

        // in case the MainActivity has been shown by an action of missed call notification
        handleMissedCall(intent)
        binding!!.ongoingCallLabel.setOnClickListener { v: View? ->
            lifecycleScope.launch {
                with(KaleyraVideo.conference.call.first()) {
                    if (!setDisplayMode(CallUI.DisplayMode.Foreground)) this.show()
                }
            }
        }
        val selectedUsersAdapter = FastAdapter.with(selectedUsersItemAdapter)
        val selectExtension = selectedUsersAdapter.getOrCreateExtension(SelectExtension::class.java)
        selectExtension?.isSelectable = true
        selectedUsersAdapter.onClickListener = { view: View?, _: IAdapter<IItem<*>>?, iItem: IItem<*>?, integer: Int? ->
            if (iItem is SelectedUserItem) deselectUser(iItem.userAlias, iItem.position)
            true
        }
        binding!!.selectedUsersChipgroup.isFocusable = false
        binding!!.selectedUsersChipgroup.layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
        binding!!.selectedUsersChipgroup.adapter = selectedUsersAdapter
        selectedUsersItemAdapter.add(NoUserSelectedItem())

        // Update the button colors based on their current module status to avoid interaction before the modules are ready.
        KaleyraVideo.conference.state.combine(KaleyraVideo.synchronization) { state, sync -> binding?.let { setButtonColor(it.call, state, sync) } }.launchIn(lifecycleScope)
        KaleyraVideo.conversation.state.combine(KaleyraVideo.synchronization) { state, sync -> binding?.let { setButtonColor(it.chat, state, sync) } }.launchIn(lifecycleScope)
        KaleyraVideo.state.filter { it is Disconnected.Error }.onEach { showErrorDialog(it.toString()) }.launchIn(lifecycleScope)

        KaleyraVideo.conference.call.flatMapLatest { it.state }.onEach {
            if (it is Call.State.Disconnected.Ended) hideOngoingCallLabel()
            if (it is Call.State.Disconnected.Companion || it is Call.State.Connecting) showOngoingCallLabel()
        }.launchIn(lifecycleScope)

        KaleyraVideo.conference.call
            .flatMapLatest { it.recording }
            .flatMapLatest { it.state }
            .dropWhile { it is Recording.State.Stopped }
            .onEach {
                Snackbar.make(window.decorView, it.toString(), Snackbar.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(true)
    }

    override fun onResume() {
        super.onResume()

        if (configuration!!.isMockConfiguration()) {
            ConfigurationActivity.Companion.showNew(this, configuration, true)
            return
        }
        if (!isUserLogged(this) && !isHandlingExternalUrl(intent)) {
            LoginActivity.show(this)
            finish()
            return
        }
        if (usersList.isEmpty()) loadUsersList()

        if (isHandlingExternalUrl(intent)) intent.data = null

        DemoAppKaleyraVideoInitializer.connect(this)
    }

    private fun isHandlingExternalUrl(intent: Intent): Boolean = intent.data != null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        handleOpenUrl(intent)
        handleMissedCall(intent)
    }

    private fun handleMissedCall(intent: Intent) {
        val notificationId: Int = intent.getIntExtra(MissedNotificationPayloadWorker.notificationId, 0)
        if (notificationId == 0) return
        MissedNotificationPayloadWorker.cancelNotification(this, notificationId)
        if (!LoginManager.isUserLogged(this)) return
        intent.getStringArrayListExtra(MissedNotificationPayloadWorker.startCall)?.let {
            KaleyraVideo.conference.call(it)
        }
        intent.getStringExtra(MissedNotificationPayloadWorker.startChat)?.let {
            KaleyraVideo.conversation.chat(this, it)
        }
        intent.action = null
    }

    /**
     * There are two ways to use a link:
     *    - connect with a specific scope(in this case a call).
     *    - connect to Kaleyra's services and then join the call. In this case the connection does not have a scope so it's not bounded to a scoped lifecycle.
     */
    private fun handleOpenUrl(intent: Intent) {
        if (Intent.ACTION_VIEW != intent.action) return
        if (!isHandlingExternalUrl(intent)) return
        val joinUrl = intent.data.toString()
        when {
            !LoginManager.isUserLogged(this@MainActivity) -> connectWithAccessLink(joinUrl)
            else                                          -> openJoinUrl(joinUrl)
        }
        intent.action = null
    }

    /**
     * To connect using an access link the Kaleyra Video needs to be first configured.
     */
    private fun connectWithAccessLink(joinUrl: String) = lifecycleScope.launch {
        kotlin.runCatching { KaleyraVideo.connect(joinUrl).await() }
    }

    /**
     * To use the joinUrl you will need to connect first using the userId and tokenProvider
     */
    private fun openJoinUrl(joinUrl: String) {
        lifecycleScope.launch {
            when {
                !KaleyraVideo.isConfigured -> {
                    val loggedUserId = LoginManager.getLoggedUser(this@MainActivity)
                    KaleyraVideo.connect(loggedUserId) { requestToken(loggedUserId) }.await()
                }

                KaleyraVideo.connectedUser.value == null -> {
                    val loggedUserId = LoginManager.getLoggedUser(this@MainActivity)
                    KaleyraVideo.connect(loggedUserId) { requestToken(loggedUserId) }.await()
                }

                else -> Unit
            }
            val result = KaleyraVideo.conference.join(joinUrl)
            val exception = result.exceptionOrNull()
            if (exception != null) {
                Toast.makeText(ContextRetainer.context, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        searchView = menu.findItem(id.searchMain).actionView as SearchView?
        searchView!!.setOnSearchClickListener { v: View? -> (findViewById<View>(id.appbar_toolbar) as AppBarLayout).setExpanded(false, true) }
        searchView!!.queryHint = getString(string.search)
        searchView!!.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        itemAdapter?.filter(newText)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            id.call_configuration -> showCallConfigurationDialog()
            id.logout             -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCallConfigurationDialog() {
        val (_, _, _, _, _, _, _, _, _, _, _, _, _, defaultCallType) = ConfigurationPrefsManager.getConfiguration(this)
        CustomConfigurationDialog.showCallConfigurationDialog(this, defaultCallType)
        supportFragmentManager.setFragmentResultListener("customize_configuration", this) { requestKey: String?, result: Bundle ->
            val callConfiguration: CallConfiguration = result.getParcelable("call_configuration") ?: return@setFragmentResultListener
            DefaultConfigurationManager.saveDefaultCallConfiguration(callConfiguration)
            saveAppConfiguration(result.getSerializable("app_configuration") as Configuration)
            KaleyraVideo.conference.settings.camera =
                if (callConfiguration.options.backCameraAsDefault) Conference.Settings.Camera.Back
                else Conference.Settings.Camera.Front
        }
    }

    private fun logout() {
        LoginManager.logout(this)
        KaleyraVideo.reset()
        DefaultConfigurationManager.clearAll()
        binding!!.ongoingCallLabel.visibility = View.GONE
        LoginActivity.show(this)
        finish()
    }

    private fun setUpRecyclerView() {
        if (fastAdapter != null && usersList.isNotEmpty()) return
        itemAdapter = ItemAdapter.items()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        ExtensionsFactories.register(SelectExtensionFactory())
        val selectExtension = fastAdapter!!.getOrCreateExtension(SelectExtension::class.java)
        selectExtension?.isSelectable = true

        // on user selection put it in a list to be called on click on call button.
        fastAdapter!!.onPreClickListener = { view: View?, userSelectionItemIAdapter: IAdapter<UserSelectionItem>, userSelectionItem: UserSelectionItem, position: Int ->
            if (!userSelectionItem.isSelected) selectUser(userSelectionItem.name, position) else deselectUser(userSelectionItem.name, position)
            true
        }
        binding!!.contactsList.itemAnimator = null
        binding!!.contactsList.layoutManager = LinearLayoutManager(this)
        binding!!.contactsList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding!!.contactsList.adapter = fastAdapter
        itemAdapter!!.itemFilter.filterPredicate = { userSelectionItem: UserSelectionItem, constraint: CharSequence? -> userSelectionItem.name.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault())) }
        itemAdapter!!.itemFilter.itemFilterListener = object : ItemFilterListener<UserSelectionItem> {
            override fun itemsFiltered(constraint: CharSequence?, results: List<UserSelectionItem>?) {
                binding!!.noResults.post {
                    results ?: return@post
                    if (results.isNotEmpty()) binding!!.noResults.visibility = View.GONE else binding!!.noResults.visibility = View.VISIBLE
                }
            }

            override fun onReset() {
                binding!!.noResults.post { binding!!.noResults.visibility = View.GONE }
            }
        }
    }

    private fun loadUsersList() = onRefresh()

    override fun onRefresh() {
        if (itemAdapter == null) return
        usersList.clear()
        itemAdapter!!.clear()
        binding!!.contactsList.invalidateItemDecorations()
        binding!!.loading.visibility = View.VISIBLE

        calleeSelected = ArrayList()
        selectedUsersItemAdapter.clear()
        selectedUsersItemAdapter.add(NoUserSelectedItem())

        lifecycleScope.launch {
            // Fetch the sample users you can use to login with.
            // Add each user(except the logged one) to the recyclerView adapter to be displayed in the list.
            restApi.listUsers().filter { it != LoginManager.getLoggedUser(this@MainActivity) }.apply {
                usersList.addAll(map { UserSelectionItem(it) })
                itemAdapter!!.setNewList(usersList)
                for (userSelected in calleeSelected) {
                    selectUser(userSelected, usersList.indexOf(UserSelectionItem(userSelected)))
                }
                searchView?.query?.let { itemAdapter!!.filter(searchView!!.query) }
                binding!!.loading.visibility = View.GONE
                setRefreshing(false)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) (findViewById<View>(id.appbar_toolbar) as AppBarLayout).setExpanded(false) else if (searchView != null && searchView!!.isIconified) (findViewById<View>(id.appbar_toolbar) as AppBarLayout).setExpanded(true)
    }

    private fun selectUser(userAlias: String, position: Int) {
        val selectExtension = fastAdapter?.getExtension(SelectExtension::class.java)
        selectExtension?.select(position)
        if (calleeSelected.contains(userAlias)) return
        calleeSelected.add(userAlias)
        val selectedUserItem = SelectedUserItem(userAlias, position)
        selectedUsersItemAdapter.add(0, selectedUserItem)
        binding!!.selectedUsersChipgroup.smoothScrollToPosition(0)
        selectedUsersItemAdapter.removeByIdentifier(NoUserSelectedItem.NO_USER_SELECTED_ITEM_IDENTIFIER)
    }

    private fun deselectUser(userAlias: String, position: Int) {
        val selectExtension = fastAdapter?.getExtension(SelectExtension::class.java)
        selectExtension?.deselect(position)
        calleeSelected.remove(userAlias)
        selectedUsersItemAdapter.removeByIdentifier(userAlias.hashCode().toLong())
        if (selectedUsersItemAdapter.adapterItemCount == 0) selectedUsersItemAdapter.add(NoUserSelectedItem())
    }

    private fun showOngoingCallLabel() {
        binding!!.ongoingCallLabel.visibility = View.VISIBLE
    }

    private fun hideOngoingCallLabel() {
        binding!!.ongoingCallLabel.visibility = View.GONE
    }

    fun onChatClicked(view: View?) {
        if (calleeSelected.isEmpty()) {
            showErrorDialog(resources.getString(string.oto_chat_error_no_selected_user))
            return
        }
        hideKeyboard(this)
        if (calleeSelected.size == 1) {
            KaleyraVideo.conversation.chat(this, calleeSelected[0]).getOrNull()
        } else {
            KaleyraVideo.conversation.chat(this, calleeSelected).getOrNull()
        }

    }

    /**
     * This is how a call is started. You must provide one users alias identifying the user your user wants to communicate with.
     * Starting a chat is an asynchronous process, failure or success is reported in the callback provided.
     */
    fun onCallClicked(v: View?) {
        if (calleeSelected.isEmpty()) {
            showErrorDialog(resources.getString(string.oto_call_error_no_selected_user))
            return
        }
        hideKeyboard(this)
        val demoAppConfiguration = ConfigurationPrefsManager.getConfiguration(this)
        val type = when (demoAppConfiguration.defaultCallType) {
            CallOptionsType.AUDIO_ONLY       -> Call.Type.AudioOnly
            CallOptionsType.AUDIO_UPGRADABLE -> Call.Type.AudioUpgradable
            CallOptionsType.AUDIO_VIDEO      -> Call.Type.AudioVideo
        }

        val configuration = DefaultConfigurationManager.getDefaultCallConfiguration()
        KaleyraVideo.conference.call(calleeSelected) {
            callType = type
            recordingType = if (configuration.options.recordingEnabled) Call.Recording.Type.Automatic else Call.Recording.Type.Never
        }.getOrNull()
    }

    private fun setButtonColor(view: FloatingActionButton, state: State?, synchronization: Synchronization?) {
        // the chat module is offline first, which means that you can interact with it even when you are not connected to internet
        // here we color the button in black until the module gets online
        when (state) {
            is State.Connecting         -> ContextCompat.getColor(this, R.color.stateConnecting)
            is State.Disconnected.Error -> ContextCompat.getColor(this, R.color.stateError)
            is State.Disconnected       -> ContextCompat.getColor(this, R.color.stateDisconnected)
            is State.Disconnecting      -> ContextCompat.getColor(this, R.color.stateDisconnecting)
            else                        -> null
        }?.let { view.backgroundTintList = ColorStateList.valueOf(it) }

        when (synchronization) {
            is Synchronization.Active.Completed -> ContextCompat.getColor(this, R.color.stateConnected)
            else                                -> null
        }?.let { view.backgroundTintList = ColorStateList.valueOf(it) }
    }

    ////////////////////////////////////////////////////// UTILS /////////////////////////////////////////////////////////////
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) view = View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveAppConfiguration(configuration: Configuration) {
        val resultIntent = Intent()
        resultIntent.setPackage(this@MainActivity.packageName)
        resultIntent.putExtra(ConfigurationActivity.CONFIGURATION_RESULT, configuration.toJson())
        resultIntent.action = BaseConfigurationActivity.CONFIGURATION_CALL_SETTINGS_ACTION_UPDATE
        sendBroadcast(resultIntent)
    }

    companion object {
        fun show(context: Activity) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}