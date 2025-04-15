package com.longkd.simplemediarecord.playback

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

class AudioDeviceManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onDeviceChangedListener: ((String) -> Unit)? = null

    private val audioDeviceCallback =
        object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                super.onAudioDevicesAdded(addedDevices)
                updateAudioRouting()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                super.onAudioDevicesRemoved(removedDevices)
                updateAudioRouting()
            }
        }

    init {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    fun setupAudioRouting() {
        updateAudioRouting()
    }

    private fun updateAudioRouting() {
        when {
            isHeadphonesConnected() -> {
                routeAudioToHeadset()
                onDeviceChangedListener?.invoke("HEADSET")
            }

            isBluetoothDeviceConnected() -> {
                routeAudioToBluetooth()
                onDeviceChangedListener?.invoke("BLUETOOTH")
            }

            else -> {
                routeAudioToSpeaker()
                onDeviceChangedListener?.invoke("SPEAKER")
            }
        }
    }

    fun setOnDeviceChangedListener(listener: (String) -> Unit) {
        onDeviceChangedListener = listener
    }

    fun release() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    private fun routeAudioToHeadset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val headsetDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            }

            headsetDevice?.let { device ->
                audioManager.setCommunicationDevice(device)
            }
        } else {
            // For older Android versions
            audioManager.mode = AudioManager.MODE_NORMAL

            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = false
            @Suppress("DEPRECATION")
            audioManager.isBluetoothScoOn = false
        }
    }

    private fun routeAudioToBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val bluetoothDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }

            bluetoothDevice?.let { device ->
                audioManager.setCommunicationDevice(device)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isBluetoothScoOn = true

            @Suppress("DEPRECATION")
            audioManager.startBluetoothSco()
        }

    }

    private fun routeAudioToSpeaker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val speakerDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }

            speakerDevice?.let { device ->
                audioManager.setCommunicationDevice(device)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = true

            @Suppress("DEPRECATION")
            audioManager.isBluetoothScoOn = false
        }

    }

    private fun isHeadphonesConnected(): Boolean {
        val devices = getConnectedAudioDevices()
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
        }
    }

    private fun isBluetoothDeviceConnected(): Boolean {
        val devices = getConnectedAudioDevices()
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
    }

    private fun getConnectedAudioDevices(): List<AudioDeviceInfo> {
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }
}