package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentPlaylistBinding

// Экран одного плейлиста. Данные (id плейлиста) приходят через Bundle в arguments.
// Конструктор у фрагмента ПУСТОЙ — это обязательное правило.
class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TrackAdapter

    private val playlistId: Int
        get() = requireArguments().getInt(KEY_PLAYLIST_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrackAdapter(
            onClick = { track -> startActivity(PlayerActivity.createIntent(requireContext(), track.id)) },
            onLongClick = { track -> confirmRemoveTrack(track) }
        )
        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = adapter

        binding.backButton.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.deletePlaylistButton.setOnClickListener { confirmDeletePlaylist() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val playlist = MockRepository.getPlaylist(playlistId)
        if (playlist == null) {            // плейлиста больше нет — выходим
            parentFragmentManager.popBackStack()
            return
        }
        binding.playlistTitle.text = playlist.name
        binding.playlistDescription.text = playlist.description
        binding.playlistDescription.isVisible = playlist.description.isNotEmpty()

        val tracks = MockRepository.getPlaylistTracks(playlistId)
        binding.playlistCount.text = tracksCountText(tracks.size)
        adapter.submit(tracks)
        binding.emptyText.isVisible = tracks.isEmpty()
    }

    // УДАЛЕНИЕ ТРЕКА ИЗ ПЛЕЙЛИСТА (долгое нажатие)
    private fun confirmRemoveTrack(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить трек из плейлиста?")
            .setMessage("«${track.name}» — ${track.artist}")
            .setPositiveButton("Удалить") { _, _ ->
                MockRepository.removeTrackFromPlaylist(playlistId, track.id)
                refresh()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // УДАЛЕНИЕ САМОГО ПЛЕЙЛИСТА
    private fun confirmDeletePlaylist() {
        val playlist = MockRepository.getPlaylist(playlistId) ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист?")
            .setMessage("Плейлист «${playlist.name}» будет удалён. Сами треки останутся в каталоге.")
            .setPositiveButton("Удалить") { _, _ ->
                MockRepository.deletePlaylist(playlistId)
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_PLAYLIST_ID = "playlist_id"

        // Фабричный метод: создаём фрагмент с пустым конструктором и кладём данные в arguments
        fun newInstance(playlistId: Int): PlaylistFragment = PlaylistFragment().apply {
            arguments = bundleOf(KEY_PLAYLIST_ID to playlistId)
        }
    }
}
