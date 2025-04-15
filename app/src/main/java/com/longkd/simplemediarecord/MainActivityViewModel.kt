package com.longkd.simplemediarecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.longkd.simplemediarecord.recorder.MediaRecorderController
import com.longkd.simplemediarecord.util.millisecondsToStopwatchString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val mediaRecorderController: MediaRecorderController
) : ViewModel() {

    init {
        mediaRecorderController.initRecorder()
    }

    val uiState = mediaRecorderController.state.map {
        mapState(it)
    }

    private fun mapState(state: MediaRecorderController.State): UiState {
        return when (state) {
            MediaRecorderController.State.PREPARING -> UiState.Idle
            MediaRecorderController.State.RECORDING -> UiState.Recording
            MediaRecorderController.State.PAUSED -> UiState.Paused
            MediaRecorderController.State.ERROR -> UiState.Idle
        }
    }

    val timerText = mediaRecorderController.timer.map { milliseconds ->
        millisecondsToStopwatchString(milliseconds)
    }

    fun getCurrentState() = mapState(mediaRecorderController.getCurrentState())

    fun onStartRecording() {
        mediaRecorderController.start()
    }

    fun onPauseOrResumeRecording() {
        mediaRecorderController.toggleRecPause()
    }

    fun onStopRecording() {
        viewModelScope.launch {
            mediaRecorderController.stop()
        }
    }

    override fun onCleared() {
        mediaRecorderController.release()
        super.onCleared()
    }

}