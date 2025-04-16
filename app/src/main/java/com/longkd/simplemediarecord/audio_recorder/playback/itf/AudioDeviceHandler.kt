package com.longkd.simplemediarecord.audio_recorder.playback.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager
import com.longkd.simplemediarecord.audio_recorder.playback.model.AudioDevicePair


interface AudioDeviceHandler : AudioManager {
    fun setupAudioRouting()
    fun setOnDeviceChangedListener(listener: (AudioDevicePair) -> Unit)
    fun getCurrentDevice(): AudioDevicePair?
    fun getAvailableOutputDevices(): List<AudioDevicePair>
    fun setOnDeviceListChangedListener(listener: (List<AudioDevicePair>) -> Unit)
    fun selectAudioDevice(deviceId: Int): Boolean
    fun resetDeviceSelection()
}