package com.longkd.simplemediarecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longkd.simplemediarecord.audio_recorder.playback.AudioPlayerController
import com.longkd.simplemediarecord.audio_recorder.playback.model.AudioDevicePair
import com.longkd.simplemediarecord.audio_recorder.recorder.AudioRecorderController
import com.longkd.simplemediarecord.util.millisecondsToStopwatchString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val audioRecorderController: AudioRecorderController,
    private val audioPlayerController: AudioPlayerController,
    @Named("audioFilePath") private val audioFilePath: String
) : ViewModel() {

    val recorderUiState = audioRecorderController.state.map {
        mapState(it)
    }

    val timerText = audioRecorderController.timer.map { milliseconds ->
        millisecondsToStopwatchString(milliseconds)
    }

    private val _playbackUiState = MutableStateFlow(PlaybackUiState())
    val playbackUiState = _playbackUiState.asStateFlow()

    private val _availableAudioDevices = MutableStateFlow<List<AudioDevicePair>>(emptyList())
    val availableAudioDevices: StateFlow<List<AudioDevicePair>> = _availableAudioDevices

    private var progressJob: Job? = null

    init {
        updateDeviceChange()
        updateListDeviceSelected()
    }

    fun updateListDeviceSelected() {
        audioPlayerController.setOnDeviceListChangedListener {
            _availableAudioDevices.value = it
        }
    }

    fun getCurrentDevice() = audioPlayerController.getCurrentDevice()

    fun selectAudioDevice(deviceId: Int): Boolean {
        return audioPlayerController.selectAudioDevice(deviceId)
    }

    private fun mapState(state: AudioRecorderController.State): RecorderUiState {
        return when (state) {
            AudioRecorderController.State.PREPARING -> RecorderUiState.Idle
            AudioRecorderController.State.RECORDING -> RecorderUiState.Recording
            AudioRecorderController.State.PAUSED -> RecorderUiState.Paused
            AudioRecorderController.State.ERROR -> RecorderUiState.Idle
        }
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
        audioPlayerController.loadAudio(
            audioFilePath,
            object : AudioPlayerController.OnPreparedListener {
                override fun onPrepared(duration: Int) {
                    _playbackUiState.value = _playbackUiState.value.copy(
                        totalDuration = millisecondsToStopwatchString(duration.toLong()),
                        currentPosition = millisecondsToStopwatchString(0L),
                        currentProcessByPercent = 0,
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
                currentProcessByPercent = 0,
                isPlaying = false
            )
        }
    }

    private fun startUpdatingProgress(duration: Long) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            val delayMillis = (duration / 100).coerceIn(250, 1000)

            while (_playbackUiState.value.isPlaying == true) {
                val currentPosition = audioPlayerController.getCurrentPosition()
                _playbackUiState.value = _playbackUiState.value.copy(
                    currentPosition = millisecondsToStopwatchString(currentPosition.toLong()),
                    currentProcessByPercent = (currentPosition * 100f / duration).toInt()
                )
                delay(delayMillis)
            }
        }
    }

    fun getCurrentState() = mapState(audioRecorderController.getCurrentState())

    fun onStartRecording() {
        audioPlayerController.stop()
        audioRecorderController.start()
    }

    fun onPauseOrResumeRecording() {
        audioRecorderController.toggleRecPause()
    }

    fun onStopRecording() {
        viewModelScope.launch {
            audioRecorderController.stop()
            loadAudioFile()
        }
    }

    override fun onCleared() {
        audioPlayerController.release()
        audioRecorderController.release()
        super.onCleared()
    }
}