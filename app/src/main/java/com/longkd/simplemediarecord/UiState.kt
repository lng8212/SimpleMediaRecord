package com.longkd.simplemediarecord

sealed interface UiState {
    data object Idle : UiState
    data object Recording : UiState
    data object Paused : UiState
}