package com.longkd.simplemediarecord.audio_recorder.playback

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRouter
import android.os.Build
import com.longkd.simplemediarecord.audio_recorder.model.AudioDevicePair
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler

class AudioOutputDeviceHandler(private val context: Context) : AudioDeviceHandler {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var onDeviceChangedListener: ((AudioDevicePair) -> Unit)? = null
    private var onDeviceListChangedListener: ((List<AudioDevicePair>) -> Unit)? = null

    private var selectedDeviceId: Int? = null
    private var currentDevice: AudioDevicePair? = null

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesAdded(addedDevices)
            updateAudioRouting()
            notifyDeviceListChanged()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            super.onAudioDevicesRemoved(removedDevices)
            if (removedDevices.any { it.id == selectedDeviceId }) {
                selectedDeviceId = null
            }
            updateAudioRouting()
            notifyDeviceListChanged()
        }
    }

    init {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
    }

    override fun setupAudioRouting() {
        updateAudioRouting()
    }

    override fun getCurrentDevice(): AudioDevicePair? = currentDevice

    override fun getAvailableDevices(): List<AudioDevicePair> =
        getConnectedAudioDevices().map { it.toAudioDevicePair() }

    override fun setOnDeviceListChangedListener(listener: (List<AudioDevicePair>) -> Unit) {
        onDeviceListChangedListener = listener
    }

    override fun setOnDeviceChangedListener(listener: (AudioDevicePair) -> Unit) {
        onDeviceChangedListener = listener
    }

    override fun selectDevice(deviceId: Int): Boolean {
        val selected = getConnectedAudioDevices().find { it.id == deviceId } ?: return false

        selectedDeviceId = deviceId
        routeAudioToDevice(selected)
        currentDevice = selected.toAudioDevicePair()
        currentDevice?.let { onDeviceChangedListener?.invoke(it) }

        return true
    }

    override fun resetDeviceSelection() {
        selectedDeviceId = null
        updateAudioRouting()
    }

    override fun release() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    private fun updateAudioRouting() {
        val devices = getConnectedAudioDevices()
        val selected = selectedDeviceId?.let { id -> devices.find { it.id == id } }

        val routedDevice = when {
            selected != null -> selected
            devices.any { it.isHeadset() } -> devices.first { it.isHeadset() }
            devices.any { it.isBluetooth() } -> devices.first { it.isBluetooth() }
            else -> devices.firstOrNull { it.isSpeaker() }
        }

        routedDevice?.let {
            routeAudioToDevice(it)
            currentDevice = it.toAudioDevicePair()
            currentDevice?.let { pair -> onDeviceChangedListener?.invoke(pair) }
        }
    }

    private fun notifyDeviceListChanged() {
        onDeviceListChangedListener?.invoke(getAvailableDevices())
    }

    private fun getConnectedAudioDevices(): List<AudioDeviceInfo> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList().filter { device ->
            when (device.type) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> true

                else -> false
            }
        }

    private fun routeAudioToDevice(device: AudioDeviceInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                val mediaRouter =
                    context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
                val routeTypes = MediaRouter.ROUTE_TYPE_LIVE_AUDIO
                val deviceName = device.productName?.toString() ?: ""
                for (i in 0 until mediaRouter.routeCount) {
                    val route = mediaRouter.getRouteAt(i)
                    if (route.name.contains(deviceName, ignoreCase = true) ||
                        (deviceName.isNotEmpty() && route.description?.contains(
                            deviceName,
                            ignoreCase = true
                        ) == true)
                    ) {
                        mediaRouter.selectRoute(routeTypes, route)
                        break
                    }
                }
            } else {
                audioManager.setCommunicationDevice(device)
            }
        } else {
            @Suppress("DEPRECATION")
            when {
                device.isSpeaker() -> {
                    audioManager.mode = AudioManager.MODE_NORMAL
                    audioManager.isSpeakerphoneOn = true
                }

                device.isEarpiece() -> {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                    audioManager.isSpeakerphoneOn = false
                }

                device.isHeadset() -> {
                    audioManager.mode = AudioManager.MODE_NORMAL
                    audioManager.isSpeakerphoneOn = false
                }

                device.isBluetooth() -> {
                    audioManager.startBluetoothSco()
                    audioManager.isBluetoothScoOn = true
                }
            }
        }
    }

    private fun AudioDeviceInfo.toAudioDevicePair(): AudioDevicePair {
        val typeName = when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "SPEAKER"
            AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "HEADSET"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH"
            else -> "OTHER"
        }

        val name =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !productName.isNullOrBlank()) {
                productName.toString()
            } else {
                when (typeName) {
                    "SPEAKER" -> "Speaker"
                    "HEADSET" -> "Headphones"
                    "BLUETOOTH" -> "Bluetooth"
                    else -> "Audio Device"
                }
            }

        return AudioDevicePair(this, typeName, name)
    }

    private fun AudioDeviceInfo.isHeadset(): Boolean =
        type == AudioDeviceInfo.TYPE_WIRED_HEADSET || type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES

    private fun AudioDeviceInfo.isBluetooth(): Boolean =
        type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO

    private fun AudioDeviceInfo.isSpeaker(): Boolean =
        type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER

    private fun AudioDeviceInfo.isEarpiece(): Boolean =
        type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
}

