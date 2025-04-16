package com.longkd.simplemediarecord

sealed interface RecorderUiState {
    data object Idle : RecorderUiState
    data object Recording : RecorderUiState
    data object Paused : RecorderUiState
}

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentPosition: String = "00:00",
    val totalDuration: String = "00:00",

    val device: String = "Speaker",
    val isReady: Boolean = false,
    val error: String? = null
)