package com.longkd.simplemediarecord.audio_recorder.recorder

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.longkd.simplemediarecord.audio_recorder.model.AudioDevicePair

class AudioInputDeviceHandler(context: Context) {
    companion object {
        private val TAG = AudioInputDeviceHandler::class.simpleName
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onDeviceChangedListener: ((AudioDevicePair) -> Unit)? = null
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesAdded(addedDevices)
            updateAudioRouting()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesRemoved(removedDevices)
            updateAudioRouting()
        }
    }

    fun updateTheCurrentMicrophone() {
        updateAudioRouting()
    }

    fun setOnDeviceChangedListener(listener: (AudioDevicePair) -> Unit) {
        onDeviceChangedListener = listener
    }

    private fun updateAudioRouting() {
        try {
            val currentRecordingDevice = getCurrentActiveRecordingDevice()
            currentRecordingDevice?.toAudioDevicePair()?.let {
                onDeviceChangedListener?.invoke(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating audio routing: ${e.message}")
        }
    }

    private fun getCurrentActiveRecordingDevice(): AudioDeviceInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val activeDevice = audioManager.activeRecordingConfigurations
                .firstOrNull()?.audioDevice

            if (activeDevice != null) return activeDevice
            return audioManager.communicationDevice
        } else {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)

            val wiredHeadset = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET && it.isSource
            }
            if (wiredHeadset != null) return wiredHeadset

            val bluetoothHeadset = devices.firstOrNull {
                (it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) && it.isSource
            }
            if (bluetoothHeadset != null) return bluetoothHeadset

            return devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC && it.isSource
            }
        }
    }

    private fun AudioDeviceInfo.toAudioDevicePair(): AudioDevicePair {
        val typeName = when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> "BUILTIN_MIC"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "SPEAKER"
            AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "HEADSET"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH"
            AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB"
            else -> "OTHER"
        }

        val name =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !productName.isNullOrBlank()) {
                productName.toString()
            } else {
                when (typeName) {
                    "BUILTIN_MIC" -> "Phone Microphone"
                    "SPEAKER" -> "Speaker"
                    "HEADSET" -> "Headphones"
                    "BLUETOOTH" -> "Bluetooth"
                    "USB" -> "USB Audio"
                    else -> "Audio Device"
                }
            }

        return AudioDevicePair(this, typeName, name)
    }

    init {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    fun release() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }
}