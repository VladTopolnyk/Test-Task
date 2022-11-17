package com.vladtop.game_task.data

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.vladtop.game_task.BuildConfig
import com.vladtop.game_task.R

const val WEB_LINK_KEY = "web_link"
private const val GAME_PASS_KEY = "game_pass"

class RemoteConfigRepository(
    private val onResultListener: OnResultListener
) {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init() {
        remoteConfig = getFirebaseRemoteConfig()
    }

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            fetchTimeoutInSeconds = 5
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                0 // Kept 0 for quick debug
            } else {
                3600 // Change this based on your requirement
            }
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                onResultListener.onSuccess(getGamePass(), getWebLink())
            } else {
                onResultListener.onError("Error getting configuration")
            }
        }

        return remoteConfig
    }

    private fun getGamePass(): Boolean = remoteConfig.getBoolean(GAME_PASS_KEY)

    private fun getWebLink(): String = remoteConfig.getString(WEB_LINK_KEY)

    interface OnResultListener {
        fun onSuccess(gamePass: Boolean, webLink: String)
        fun onError(message: String)
    }
}