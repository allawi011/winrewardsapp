package io.kodular.allawi.getmoney.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("getmoney_prefs", Context.MODE_PRIVATE)

    fun setString(key: String, value: String) {
        pref.edit().putString(key, value).apply()
    }

    fun getString(key: String, def: String = ""): String {
        return pref.getString(key, def) ?: def
    }

    fun setLong(key: String, value: Long) {
        pref.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, def: Long = 0L): Long {
        return pref.getLong(key, def)
    }

    fun setBoolean(key: String, value: Boolean) {
        pref.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, def: Boolean = false): Boolean {
        return pref.getBoolean(key, def)
    }

    fun clear() {
        pref.edit().clear().apply()
    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_UID = "uid"
        const val KEY_INVITE_CODE = "invite_code"
    }
}
