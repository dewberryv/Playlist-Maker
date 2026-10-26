package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TrackAdapter(
            onClick = { track -> startActivity(PlayerActivity.createIntent(requireContext(), track.id)) },
            onLongClick = { track -> confirmRemove(track) }
        )
        binding.favoritesList.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val favorites = MockRepository.getFavorites()
        adapter.submit(favorites)
        binding.emptyText.isVisible = favorites.isEmpty()
    }

    // УДАЛЕНИЕ ИЗ ИЗБРАННОГО (долгое нажатие)
    private fun confirmRemove(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle("Убрать из избранного?")
            .setMessage("«${track.name}» — ${track.artist}")
            .setPositiveButton("Убрать") { _, _ ->
                MockRepository.removeFromFavorites(track.id)
                refresh()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
