package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlaylistAdapter { playlist -> openPlaylist(playlist) }
        binding.playlistsList.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsList.adapter = adapter

        binding.newPlaylistButton.setOnClickListener {
            // ВАЖНО: parentFragmentManager здесь — это менеджер LibraryFragment (дочерний),
            // а нам нужно заменить ВЕСЬ экран. Поэтому берём менеджер Activity.
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreatePlaylistFragment())
                .addToBackStack("create_playlist")   // Back вернёт на медиатеку
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val playlists = MockRepository.getPlaylists()
        adapter.submit(playlists)
        binding.emptyText.isVisible = playlists.isEmpty()
    }

    private fun openPlaylist(playlist: Playlist) {
        // Передаём id плейлиста через Bundle (см. PlaylistFragment.newInstance)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlaylistFragment.newInstance(playlist.id))
            .addToBackStack("playlist")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
