package com.longkd.simplemediarecord

import com.longkd.simplemediarecord.audio_recorder.playback.model.AudioDevicePair

sealed interface RecorderUiState {
    data object Idle : RecorderUiState
    data object Recording : RecorderUiState
    data object Paused : RecorderUiState
}

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentPosition: String = "00:00",
    val totalDuration: String = "00:00",
    val currentProcessByPercent: Int = 0,
    val device: AudioDevicePair? = null,
    val isReady: Boolean = false,
    val error: String? = null
)