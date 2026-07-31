package com.funnyenglish.core.settings

import com.russhwolf.settings.Settings

/**
 * Repository for app settings
 */
interface SettingsRepository {
    fun getString(key: String, default: String = ""): String
    fun setString(key: String, value: String)
    
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    
    fun getInt(key: String, default: Int = 0): Int
    fun setInt(key: String, value: Int)
    
    fun remove(key: String)
    fun clear()
}

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {
    
    override fun getString(key: String, default: String): String =
        settings.getString(key, default)
    
    override fun setString(key: String, value: String) {
        settings.putString(key, value)
    }
    
    override fun getBoolean(key: String, default: Boolean): Boolean =
        settings.getBoolean(key, default)
    
    override fun setBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }
    
    override fun getInt(key: String, default: Int): Int =
        settings.getInt(key, default)
    
    override fun setInt(key: String, value: Int) {
        settings.putInt(key, value)
    }
    
    override fun remove(key: String) {
        settings.remove(key)
    }
    
    override fun clear() {
        settings.clear()
    }
}
