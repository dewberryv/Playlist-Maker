package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.playlistmaker.databinding.ActivityMainBinding

// Главная Activity — контейнер для фрагментов (подход, близкий к Single Activity).
// Внутри: FragmentContainerView + нижняя панель навигации (Bottom navigation bar).
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отступы под статус-бар и панель навигации системы (нужно на новых версиях Android)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Первый запуск: кладём в контейнер стартовый фрагмент.
        // При повороте экрана savedInstanceState != null — FragmentManager сам всё восстановит.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, SearchFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_search -> SearchFragment()
                R.id.nav_library -> LibraryFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            // Переключение вкладок — это "боковая" (lateral) навигация.
            // Стек подэкранов очищаем и меняем фрагмент БЕЗ addToBackStack:
            // Back с вкладки не должен возвращать на предыдущую вкладку.
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        // Повторное нажатие на текущую вкладку — возврат на её корневой экран
        binding.bottomNav.setOnItemReselectedListener {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    // Вызывается, когда PlayerActivity запускает MainActivity с флагами
    // FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP: новый экземпляр не создаётся,
    // всё, что лежало выше в стеке (плеер), уничтожается, а Intent приходит сюда.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("NAV", "MainActivity получила новый Intent (CLEAR_TOP + SINGLE_TOP)")
    }
}
