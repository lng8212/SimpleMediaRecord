package com.longkd.simplemediarecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longkd.simplemediarecord.playback.AudioPlayerController
import com.longkd.simplemediarecord.recorder.MediaRecorderController
import com.longkd.simplemediarecord.util.millisecondsToStopwatchString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val mediaRecorderController: MediaRecorderController,
    private val audioPlayerController: AudioPlayerController,
    private val audioFilePath: String
) : ViewModel() {

    val recorderUiState = mediaRecorderController.state.map {
        mapState(it)
    }

    private val _playbackUiState = MutableStateFlow(PlaybackUiState())
    val playbackUiState = _playbackUiState.asStateFlow()

    init {
        updateDeviceChange()
    }

    private var progressJob: Job? = null

    private fun mapState(state: MediaRecorderController.State): RecorderUiState {
        return when (state) {
            MediaRecorderController.State.PREPARING -> RecorderUiState.Idle
            MediaRecorderController.State.RECORDING -> RecorderUiState.Recording
            MediaRecorderController.State.PAUSED -> RecorderUiState.Paused
            MediaRecorderController.State.ERROR -> RecorderUiState.Idle
        }
    }

    val timerText = mediaRecorderController.timer.map { milliseconds ->
        millisecondsToStopwatchString(milliseconds)
    }

    private fun updateDeviceChange() {
        audioPlayerController.setOnAudioDeviceChangedListener { deviceType ->
            _playbackUiState.value = _playbackUiState.value.copy(device = deviceType)
        }
    }

    fun playPauseAudio() {
        if (_playbackUiState.value.isPlaying) {
            audioPlayerController.pause()
            _playbackUiState.value = _playbackUiState.value.copy(isPlaying = false)
            progressJob?.cancel()
            progressJob = null
        } else {
            audioPlayerController.play()
            _playbackUiState.value = _playbackUiState.value.copy(isPlaying = true)
            startUpdatingProgress(audioPlayerController.getDuration().toLong())
        }
    }

    private fun loadAudioFile() {
        audioPlayerController.loadAudio(audioFilePath, object : AudioPlayerController.OnPreparedListener {
            override fun onPrepared(duration: Int) {
                _playbackUiState.value = _playbackUiState.value.copy(
                    totalDuration = millisecondsToStopwatchString(duration.toLong()),
                    currentPosition = millisecondsToStopwatchString(0L),
                    isPlaying = false,
                    isReady = true
                )
            }

            override fun onError(error: String) {
                _playbackUiState.value = _playbackUiState.value.copy(error = error)
            }

        })

        audioPlayerController.setOnCompletionListener {
            _playbackUiState.value = _playbackUiState.value.copy(
                currentPosition = millisecondsToStopwatchString(0L),
                isPlaying = false
            )
        }
    }

    private fun startUpdatingProgress(duration: Long) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            val duration = duration
            val delayMillis = (duration / 60).coerceIn(250, 1000)

            while (_playbackUiState.value.isPlaying == true) {
                val currentPosition = audioPlayerController.getCurrentPosition()
                _playbackUiState.value = _playbackUiState.value.copy(
                    currentPosition = millisecondsToStopwatchString(currentPosition.toLong())
                )
                delay(delayMillis)
            }
        }
    }

    fun getCurrentState() = mapState(mediaRecorderController.getCurrentState())

    fun onStartRecording() {
        audioPlayerController.stop()
        mediaRecorderController.start()
    }

    fun onPauseOrResumeRecording() {
        mediaRecorderController.toggleRecPause()
    }

    fun onStopRecording() {
        viewModelScope.launch {
            mediaRecorderController.stop()
            loadAudioFile()
        }
    }

    override fun onCleared() {
        audioPlayerController.release()
        super.onCleared()
    }
}