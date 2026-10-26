package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    // Свой обработчик кнопки Back (OnBackPressedCallback из лекции).
    // Включён только если пользователь уже что-то ввёл — тогда спрашиваем подтверждение.
    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            showExitDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Привязываем callback к жизненному циклу View фрагмента — отписка произойдёт сама
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.createButton.isEnabled = false

        binding.nameInput.doAfterTextChanged {
            binding.createButton.isEnabled = !binding.nameInput.text.isNullOrBlank()
            updateBackCallback()
        }
        binding.descriptionInput.doAfterTextChanged { updateBackCallback() }

        // Стрелка "назад" делает то же, что и системная кнопка Back
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.createButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            MockRepository.createPlaylist(name, description)
            Toast.makeText(requireContext(), "Плейлист «${name.trim()}» создан", Toast.LENGTH_SHORT).show()
            backCallback.isEnabled = false
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateBackCallback() {
        backCallback.isEnabled = !binding.nameInput.text.isNullOrBlank() ||
                !binding.descriptionInput.text.isNullOrBlank()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все несохранённые данные будут потеряны")
            .setPositiveButton("Завершить") { _, _ ->
                // Отключаем свой callback -> dispatcher вернёт стандартное поведение Back
                backCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
