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
package com.kaleyra.app_utilities.networking

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.kaleyra.app_configuration.model.Configuration
import com.kaleyra.app_configuration.model.PushProvider
import com.kaleyra.app_utilities.MultiDexApplication.Companion.okHttpClient
import com.kaleyra.app_utilities.networking.models.AccessToken
import com.kaleyra.app_utilities.networking.models.KaleyraVideoUsers
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager.getConfiguration
import com.kaleyra.app_utilities.storage.LoginManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType.Application
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * WARNING!!!
 * The networking package is used only to fetch the users, to make the demo app run out of the box,
 * with the least efforts.
 *
 *
 * MockedNetwork
 *
 * @author kristiyan
 */
class RestApi(val applicationContext: Context) {

    data class RestConfiguration(
        val apiKey: String,
        val appId: String,
        val environment: String,
        val region: String,
        val httpStack: OkHttpClient
    )

    private var restConfiguration: RestConfiguration? = null

    val scope =
        CoroutineScope(Dispatchers.IO) + CoroutineName("MockedNetwork") + SupervisorJob()

    private val configurationListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key != "configuration") return@OnSharedPreferenceChangeListener
            restConfiguration = getConfiguration(applicationContext).toRest()
        }

    companion object {
        const val TAG = "DemoAppRestApi"
    }

    init {
        updateConfiguration()
        applicationContext
            .getSharedPreferences(
                ConfigurationPrefsManager.CONFIGURATION_PREFS,
                Context.MODE_PRIVATE
            )
            .registerOnSharedPreferenceChangeListener(configurationListener)
    }

    private fun updateConfiguration() {
        restConfiguration = getConfiguration(applicationContext).toRest()
    }

    private fun Configuration.toRest(): RestConfiguration {
        return RestConfiguration(
            apiKey,
            appId,
            environment,
            region,
            okHttpClient
        )
    }

    val client by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }

            install(HttpTimeout)

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = Int.MAX_VALUE)
                exponentialDelay()
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }

    private val endpoint: String
        get() {
            val envName = restConfiguration?.environment.takeIf { it != "production" }?.prependIndent(".") ?: ""
            return "https://api$envName.${restConfiguration?.region}.bandyer.com"
        }
    private var apiKey = restConfiguration?.apiKey ?: ""
    private var appId = restConfiguration?.appId ?: ""
    private val userId: String
        get() = LoginManager.getLoggedUser(applicationContext)

    var configurationHeaders: HeadersBuilder.() -> Unit = {
        append("apikey", apiKey)
    }

    private var currentAccessToken: String? = null

    suspend fun listUsers(): List<String> {
        return try {
            updateConfiguration()
            val users: MutableList<String> = mutableListOf()

            var offset = 0
            while (client.get("$endpoint/v2/users?offset=$offset&limit=100") {
                    headers(configurationHeaders)
                    contentType(Application.Json)
                }.body<KaleyraVideoUsers>().users.apply {
                    offset += count()
                    users.addAll(this.map { it.id })
                }.isNotEmpty()) { }
            withContext(Dispatchers.Main) { users.sorted() }
        } catch (t: Throwable) {
            Log.e("GetListUsers", t.message, t)
            withContext(Dispatchers.Main) { emptyList() }
        }
    }

    fun registerDeviceForPushNotification(context: Context, pushProvider: PushProvider, devicePushToken: String) {
        scope.launch {
            updateConfiguration()
            kotlin.runCatching {
                val response: HttpResponse =
                    client.post("$endpoint/mobile_push_notifications/rest/device") {
                        headers(configurationHeaders)
                        timeout {
                            this.connectTimeoutMillis = 5000L
                            this.requestTimeoutMillis = 5000L
                            this.socketTimeoutMillis = 5000L
                        }
                        contentType(Application.Json)
                        setBody(
                            DeviceRegistrationInfo(
                                userId,
                                appId,
                                devicePushToken,
                                pushProvider.name
                            )
                        )
                    }

                if (response.status != HttpStatusCode.OK) {
                    val registerDeviceErrorMessage = "Failed to register device for push notifications!"
                    MainScope().launch { Toast.makeText(context, registerDeviceErrorMessage, Toast.LENGTH_LONG).show() }
                    Log.e("PushNotification", registerDeviceErrorMessage)
                } else {
                    Log.d("PushNotification", "Push token registered serverside.")
                }
            }.onFailure {
                MainScope().launch { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                Log.e("PushNotification", it.message, it)
            }
        }
    }

    fun unregisterDeviceForPushNotification(devicePushToken: String) {
        scope.launch {
            updateConfiguration()
            kotlin.runCatching {
                val response: HttpResponse =
                    client.delete("$endpoint/mobile_push_notifications/rest/device/$userId/$appId/$devicePushToken") {
                        headers(configurationHeaders)
                        contentType(Application.Json)
                    }
                if (response.status != HttpStatusCode.OK) {
                    Log.e("PushNotification", "Failed to unregister device for push notifications!")
                }
            }.onFailure { Log.e("PushNotification", it.message, it) }
        }
    }

    fun getAccessToken(userId:String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        scope.launch {
            updateConfiguration()
            kotlin.runCatching {
                val response: HttpResponse = client.post("$endpoint/v2/sdk/credentials") {
                    headers(configurationHeaders)
                    contentType(Application.Json)
                    setBody(AccessToken.Request(userId))
                }
                currentAccessToken = response.body<AccessToken.Response>().access_token
            }.onFailure {
                Log.e(TAG, it.message ?: "Unable to fetch access token")
                withContext(Dispatchers.Main) {
                    onError(Exception(it.message))
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                if (currentAccessToken.isNullOrBlank()) onError(Exception("Unable to get access token!"))
                else onSuccess(currentAccessToken!!)
            }
        }
    }

    fun cancel() = kotlin.runCatching {
        scope.coroutineContext.cancelChildren()
    }
}