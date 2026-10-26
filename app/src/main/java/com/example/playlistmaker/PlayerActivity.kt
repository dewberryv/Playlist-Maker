package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.databinding.ActivityPlayerBinding

// Экран плеера — отдельная Activity. Воспроизведение имитируется таймером (мок).
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var track: Track? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var progressSec = 0

    // Эта задача раз в секунду "двигает" время воспроизведения
    private val progressRunnable = object : Runnable {
        override fun run() {
            progressSec++
            if (progressSec >= PREVIEW_SECONDS) {
                stopPlayback()
                return
            }
            binding.progressText.text = formatDuration(progressSec)
            handler.postDelayed(this, 1000L)
        }
    }

    // Переопределение кнопки Back (по аналогии со слайдом лекции):
    // пока играет музыка — первое нажатие только останавливает её.
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isPlaying) {
                pausePlayback()
                Toast.makeText(
                    this@PlayerActivity,
                    "Воспроизведение остановлено. Нажмите ещё раз, чтобы выйти",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                isEnabled = false                       // отключаем свой callback...
                onBackPressedDispatcher.onBackPressed() // ...и получаем стандартное поведение
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.playerRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, backCallback)
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.playButton.setOnClickListener {
            if (isPlaying) pausePlayback() else startPlayback()
        }
        binding.favoriteButton.setOnClickListener { onFavoriteClicked() }
        binding.addButton.setOnClickListener { showAddToPlaylistDialog() }
        binding.nextButton.setOnClickListener { openNextTrack() }
        binding.homeButton.setOnClickListener { goHome() }

        showTrack(intent.getIntExtra(EXTRA_TRACK_ID, -1))
    }

    // SINGLE_TOP: если Activity уже на вершине стека, система НЕ создаёт новую,
    // а присылает новый Intent сюда (onCreate при этом не вызывается).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("NAV", "PlayerActivity получила новый Intent (SINGLE_TOP)")
        showTrack(intent.getIntExtra(EXTRA_TRACK_ID, -1))
    }

    private fun showTrack(trackId: Int) {
        val t = MockRepository.getTrack(trackId)
        if (t == null) {
            Toast.makeText(this, "Трек не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        track = t
        MockRepository.addToHistory(t.id)
        stopPlayback()

        with(binding) {
            trackName.text = t.name
            artistName.text = t.artist
            durationValue.text = formatDuration(t.durationSec)
            albumValue.text = t.album
            yearValue.text = t.year.toString()
            genreValue.text = t.genre
            countryValue.text = t.country
        }
        updateFavoriteIcon()
    }

    // ----- Воспроизведение (имитация) -----
    private fun startPlayback() {
        isPlaying = true
        binding.playButton.setImageResource(R.drawable.ic_pause)
        handler.postDelayed(progressRunnable, 1000L)
    }

    private fun pausePlayback() {
        isPlaying = false
        handler.removeCallbacks(progressRunnable)
        binding.playButton.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun stopPlayback() {
        pausePlayback()
        progressSec = 0
        binding.progressText.text = formatDuration(0)
    }

    // ----- Избранное -----
    private fun onFavoriteClicked() {
        val t = track ?: return
        val added = MockRepository.toggleFavorite(t.id)
        updateFavoriteIcon()
        Toast.makeText(
            this,
            if (added) "Добавлено в избранное" else "Удалено из избранного",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateFavoriteIcon() {
        val t = track ?: return
        binding.favoriteButton.setImageResource(
            if (MockRepository.isFavorite(t.id)) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    // ----- Плейлисты -----
    private fun showAddToPlaylistDialog() {
        val t = track ?: return
        val playlists = MockRepository.getPlaylists()
        if (playlists.isEmpty()) {
            Toast.makeText(this, "Сначала создайте плейлист в разделе «Медиатека»", Toast.LENGTH_LONG).show()
            return
        }
        val names = playlists.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Добавить в плейлист")
            .setItems(names) { _, which ->
                val playlist = playlists[which]
                val added = MockRepository.addTrackToPlaylist(playlist.id, t.id)
                val message = if (added) {
                    "Добавлено в плейлист «${playlist.name}»"
                } else {
                    "Трек уже добавлен в плейлист «${playlist.name}»"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ----- Флаги Intent -----
    // "Следующий трек": запускаем ЭТУ ЖЕ Activity с флагом SINGLE_TOP.
    // Новый экземпляр не создаётся — сработает onNewIntent.
    private fun openNextTrack() {
        val t = track ?: return
        val nextId = MockRepository.nextTrackId(t.id) ?: return
        val intent = createIntent(this, nextId)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    // "На главную": CLEAR_TOP уничтожает всё, что лежит над MainActivity в стеке
    // (то есть плеер), а SINGLE_TOP не даёт создать второй экземпляр MainActivity.
    private fun goHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) pausePlayback()   // ушли с экрана — музыку на паузу
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
    }

    companion object {
        const val EXTRA_TRACK_ID = "extra_track_id"
        private const val PREVIEW_SECONDS = 30   // как в iTunes API: 30-секундный фрагмент

        // Явный Intent: указываем конкретный класс Activity и кладём id трека в extras
        fun createIntent(context: Context, trackId: Int): Intent =
            Intent(context, PlayerActivity::class.java).putExtra(EXTRA_TRACK_ID, trackId)
    }
}
