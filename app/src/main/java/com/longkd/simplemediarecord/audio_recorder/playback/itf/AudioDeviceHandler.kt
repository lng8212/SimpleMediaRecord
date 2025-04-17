package com.longkd.simplemediarecord.audio_recorder.playback.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager
import com.longkd.simplemediarecord.audio_recorder.model.AudioDevicePair

interface AudioDeviceHandler : AudioManager {
    fun setupAudioRouting()
    fun getCurrentDevice(): AudioDevicePair?
    fun getAvailableDevices(): List<AudioDevicePair>
    fun setOnDeviceChangedListener(listener: (AudioDevicePair) -> Unit)
    fun setOnDeviceListChangedListener(listener: (List<AudioDevicePair>) -> Unit)
    fun selectDevice(deviceId: Int): Boolean
    fun resetDeviceSelection()
}