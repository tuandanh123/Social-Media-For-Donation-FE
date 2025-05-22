package com.example.socialmediamobieapp.utils

import android.content.Context
import androidx.core.content.edit

open class TokenManager(context: Context?) {
    private val prefs = context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_PROFILE_ID = "profile_id"
    }

    open fun saveAccessToken(token: String) {
        prefs?.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    open fun getAccessToken(): String? {
        return prefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    open fun saveProfileId(profileId: String) {
        prefs?.edit { putString(KEY_PROFILE_ID, profileId) }
    }

    open fun getProfileId(): String? {
        return prefs?.getString(KEY_PROFILE_ID, null)
    }

    open fun clear() {
        prefs?.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_PROFILE_ID)
        }
    }
}