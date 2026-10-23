package com.example.ui.quran

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.quran.PlaybackMode
import com.example.data.quran.QuranReciter
import com.example.data.quran.goldenReciters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "QuranAudioPlayer"

data class AudioPlayerState(
  val isPlaying: Boolean = false,
  val isLoading: Boolean = false,
  val currentSurahNumber: Int? = null,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val playbackMode: PlaybackMode = PlaybackMode.AUTO_NEXT,
  val reciter: QuranReciter = goldenReciters.first(),
  val reciterName: String = goldenReciters.first().name,
  val errorMessage: String? = null
)

class QuranAudioPlayer(
  private val scope: CoroutineScope
) {
  private var mediaPlayer: MediaPlayer? = null
  private var progressTrackerJob: Job? = null
  private var onNextSurahCallback: (() -> Unit)? = null

  private val _state = MutableStateFlow(AudioPlayerState())
  val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

  fun selectReciter(reciter: QuranReciter, surahNumber: Int? = _state.value.currentSurahNumber, onNextSurah: (() -> Unit)? = onNextSurahCallback) {
    if (surahNumber != null) {
      playSurah(surahNumber = surahNumber, reciter = reciter, onNextSurah = onNextSurah)
    } else {
      _state.update { it.copy(reciter = reciter, reciterName = reciter.name) }
    }
  }

  fun playSurah(
    surahNumber: Int,
    reciter: QuranReciter = _state.value.reciter,
    onNextSurah: (() -> Unit)? = null
  ) {
    if (onNextSurah != null) {
      onNextSurahCallback = onNextSurah
    }

    val isSameSurahAndReciter = _state.value.currentSurahNumber == surahNumber &&
      _state.value.reciter.id == reciter.id && mediaPlayer != null

    if (isSameSurahAndReciter) {
      if (!_state.value.isPlaying) {
        mediaPlayer?.start()
        _state.update { it.copy(isPlaying = true) }
        startProgressTracker()
      }
      return
    }

    _state.update {
      it.copy(
        isLoading = true,
        reciter = reciter,
        reciterName = reciter.name,
        currentSurahNumber = surahNumber,
        currentPositionMs = 0L,
        durationMs = 0L,
        errorMessage = null
      )
    }

    releasePlayer()

    val audioUrl = reciter.getAudioUrl(surahNumber)

    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(audioUrl)
        setOnPreparedListener { mp ->
          _state.update {
            it.copy(
              isLoading = false,
              isPlaying = true,
              durationMs = mp.duration.toLong()
            )
          }
          mp.start()
          startProgressTracker()
        }
        setOnCompletionListener {
          handlePlaybackCompletion()
        }
        setOnErrorListener { _, what, extra ->
          Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
          _state.update {
            it.copy(
              isLoading = false,
              isPlaying = false,
              errorMessage = "تعذر تشغيل التلاوة الصوتية، يرجى التحقق من الاتصال بالإنترنت"
            )
          }
          true
        }
        prepareAsync()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize MediaPlayer", e)
      _state.update {
        it.copy(
          isLoading = false,
          isPlaying = false,
          errorMessage = "حدث خطأ أثناء تحميل التلاوة"
        )
      }
    }
  }

  fun togglePlayPause() {
    val player = mediaPlayer ?: return
    if (player.isPlaying) {
      player.pause()
      _state.update { it.copy(isPlaying = false) }
      stopProgressTracker()
    } else {
      player.start()
      _state.update { it.copy(isPlaying = true) }
      startProgressTracker()
    }
  }

  fun seekTo(positionMs: Long) {
    val player = mediaPlayer ?: return
    val clamped = positionMs.coerceIn(0L, _state.value.durationMs)
    player.seekTo(clamped.toInt())
    _state.update { it.copy(currentPositionMs = clamped) }
  }

  fun seekForward10s() {
    val current = _state.value.currentPositionMs
    seekTo(current + 10_000L)
  }

  fun seekRewind10s() {
    val current = _state.value.currentPositionMs
    seekTo(current - 10_000L)
  }

  fun setPlaybackMode(mode: PlaybackMode) {
    _state.update { it.copy(playbackMode = mode) }
  }

  fun cyclePlaybackMode() {
    val nextMode = when (_state.value.playbackMode) {
      PlaybackMode.AUTO_NEXT -> PlaybackMode.LOOP_SURAH
      PlaybackMode.LOOP_SURAH -> PlaybackMode.STOP_ON_END
      PlaybackMode.STOP_ON_END -> PlaybackMode.AUTO_NEXT
    }
    setPlaybackMode(nextMode)
  }

  private fun handlePlaybackCompletion() {
    when (_state.value.playbackMode) {
      PlaybackMode.LOOP_SURAH -> {
        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()
        _state.update { it.copy(isPlaying = true, currentPositionMs = 0L) }
      }
      PlaybackMode.AUTO_NEXT -> {
        _state.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
        stopProgressTracker()
        onNextSurahCallback?.invoke()
      }
      PlaybackMode.STOP_ON_END -> {
        _state.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
        stopProgressTracker()
      }
    }
  }

  private fun startProgressTracker() {
    stopProgressTracker()
    progressTrackerJob = scope.launch(Dispatchers.Main) {
      while (isActive) {
        mediaPlayer?.let { player ->
          if (player.isPlaying) {
            _state.update {
              it.copy(
                currentPositionMs = player.currentPosition.toLong(),
                durationMs = player.duration.toLong()
              )
            }
          }
        }
        delay(500)
      }
    }
  }

  private fun stopProgressTracker() {
    progressTrackerJob?.cancel()
    progressTrackerJob = null
  }

  fun release() {
    stopProgressTracker()
    releasePlayer()
  }

  private fun releasePlayer() {
    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
  }
}
