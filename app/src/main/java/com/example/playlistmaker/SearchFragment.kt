package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    // Правило для фрагментов: binding живёт, пока жив View фрагмента (onCreateView..onDestroyView)
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrackAdapter(
            onClick = { track -> openPlayer(track) },
            onLongClick = { track -> confirmDeleteTrack(track) }
        )
        binding.resultsList.layoutManager = LinearLayoutManager(requireContext())
        binding.resultsList.adapter = adapter

        // Каждое изменение текста в строке поиска -> обновляем список
        binding.searchInput.doAfterTextChanged { refresh() }
        binding.clearButton.setOnClickListener { binding.searchInput.text.clear() }
        binding.clearHistoryButton.setOnClickListener {
            MockRepository.clearHistory()
            refresh()
        }
    }

    // onResume вызывается и при возврате из плеера — история могла измениться
    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val query = binding.searchInput.text.toString().trim()
        binding.clearButton.isVisible = binding.searchInput.text.isNotEmpty()

        if (query.isEmpty()) {
            // Пустая строка поиска -> показываем историю
            val history = MockRepository.getHistory()
            adapter.submit(history)
            binding.historyRow.isVisible = history.isNotEmpty()
            binding.emptyText.isVisible = false
        } else {
            val results = MockRepository.search(query)
            adapter.submit(results)
            binding.historyRow.isVisible = false
            binding.emptyText.isVisible = results.isEmpty()
        }
    }

    private fun openPlayer(track: Track) {
        // Явный Intent: внутри приложения указываем конкретный класс Activity
        startActivity(PlayerActivity.createIntent(requireContext(), track.id))
    }

    // УДАЛЕНИЕ ТРЕКА (долгое нажатие)
    private fun confirmDeleteTrack(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить трек?")
            .setMessage("«${track.name}» будет удалён из каталога, избранного и всех плейлистов.")
            .setPositiveButton("Удалить") { _, _ ->
                MockRepository.deleteTrack(track.id)
                refresh()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // чтобы не держать в памяти уничтоженные View
    }
}
