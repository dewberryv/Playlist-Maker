package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.ItemTrackBinding

// Адаптер — "переводчик" между списком данных и RecyclerView:
// для каждого элемента списка он заполняет одну строку (item_track.xml).
class TrackAdapter(
    private val onClick: (Track) -> Unit,
    private val onLongClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var items: List<Track> = emptyList()

    fun submit(newItems: List<Track>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TrackViewHolder(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = items[position]
        with(holder.binding) {
            trackName.text = track.name
            trackInfo.text = "${track.artist} · ${formatDuration(track.durationSec)}"
            root.setOnClickListener { onClick(track) }
            root.setOnLongClickListener {
                onLongClick(track)
                true
            }
        }
    }
}
