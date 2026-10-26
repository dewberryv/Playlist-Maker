package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

// Класс Application создаётся один раз при запуске приложения — раньше любой Activity.
// Здесь мы применяем сохранённую тему (светлая/тёмная).
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        // Если пользователь уже выбирал тему — применяем её,
        // иначе приложение следует за настройкой телефона.
        if (prefs.contains(KEY_DARK_THEME)) {
            applyTheme(prefs.getBoolean(KEY_DARK_THEME, false))
        }
    }

    companion object {
        const val PREFS_NAME = "playlist_maker_prefs"
        const val KEY_DARK_THEME = "dark_theme"

        fun applyTheme(dark: Boolean) {
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}
