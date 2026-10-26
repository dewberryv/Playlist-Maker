package com.example.playlistmaker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ----- Тёмная тема -----
        val prefs = requireContext().getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE)
        val systemIsDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        // Сначала выставляем состояние переключателя, и только потом вешаем слушатель
        binding.darkSwitch.isChecked = prefs.getBoolean(App.KEY_DARK_THEME, systemIsDark)
        binding.darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(App.KEY_DARK_THEME, isChecked).apply()
            App.applyTheme(isChecked)   // Activity пересоздастся уже в новой теме
        }

        // ----- Неявные Intent: "из приложения наружу" -----
        binding.shareRow.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Попробуй приложение Playlist Maker — собирай плейлисты из любимых треков!")
            }
            launch(Intent.createChooser(intent, "Поделиться приложением"))
        }

        binding.supportRow.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")   // только почтовые приложения
                putExtra(Intent.EXTRA_EMAIL, arrayOf("student@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Сообщение разработчикам приложения Playlist Maker")
                putExtra(Intent.EXTRA_TEXT, "Спасибо за разработку приложения!")
            }
            launch(intent)
        }

        binding.agreementRow.setOnClickListener {
            launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/legal/practicum_offer/")))
        }
    }

    // Если на устройстве нет подходящего приложения, startActivity бросит исключение
    private fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Не найдено подходящее приложение", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
