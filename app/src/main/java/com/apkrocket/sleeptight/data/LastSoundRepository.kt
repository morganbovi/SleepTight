package com.apkrocket.sleeptight.data

import android.content.Context
import com.apkrocket.sleeptight.audio.SoundType

/** Remembers the last sound the user picked so the app can reopen straight into it. */
class LastSoundRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): SoundType? {
        val name = prefs.getString(KEY_LAST_SOUND, null) ?: return null
        return runCatching { SoundType.valueOf(name) }.getOrNull()
    }

    fun save(type: SoundType) {
        prefs.edit().putString(KEY_LAST_SOUND, type.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "sleeptight_state"
        private const val KEY_LAST_SOUND = "last_sound"
    }
}
