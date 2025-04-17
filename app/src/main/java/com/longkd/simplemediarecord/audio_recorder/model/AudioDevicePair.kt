package com.longkd.simplemediarecord.audio_recorder.model

import android.media.AudioDeviceInfo

data class AudioDevicePair(
    val deviceInfo: AudioDeviceInfo,
    val deviceType: String,
    val deviceName: String
) {
    val id: Int = deviceInfo.id
}