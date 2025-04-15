package com.longkd.simplemediarecord.playback

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.longkd.simplemediarecord.playback.receiver.BluetoothDeviceReceiver
import com.longkd.simplemediarecord.playback.receiver.HeadsetBroadcastReceiver

class AudioDeviceManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onDeviceChangedListener: ((String) -> Unit)? = null
    private var headsetReceiver: HeadsetBroadcastReceiver? = null
    private var bluetoothReceiver: BluetoothDeviceReceiver? = null

    // For devices running Android 6.0 (API 23) and above
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
        registerReceivers()
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
        unregisterReceivers()
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
            // Use communication device API for Android 12+
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val bluetoothDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }

            bluetoothDevice?.let { device ->
                audioManager.setCommunicationDevice(device)
            }
        } else {
            // For older Android versions
            @Suppress("DEPRECATION")
            audioManager.isBluetoothScoOn = true

            @Suppress("DEPRECATION")
            audioManager.startBluetoothSco()
        }

    }

    private fun routeAudioToSpeaker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use communication device API for Android 12+
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val speakerDevice = devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }

            speakerDevice?.let { device ->
                audioManager.setCommunicationDevice(device)
            }
        } else {
            // For older Android versions
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

    private fun registerReceivers() {
        // Register headset connection receiver
        headsetReceiver = HeadsetBroadcastReceiver { isConnected ->
            if (isConnected) {
                routeAudioToHeadset()
                onDeviceChangedListener?.invoke("HEADSET")
            } else {
                if (isBluetoothDeviceConnected()) {
                    routeAudioToBluetooth()
                } else {
                    routeAudioToSpeaker()
                }
                onDeviceChangedListener?.invoke("SPEAKER")
            }
        }

        // Register bluetooth connection receiver
        bluetoothReceiver = BluetoothDeviceReceiver { isConnected ->
            if (isConnected && !isHeadphonesConnected()) {
                routeAudioToBluetooth()
                onDeviceChangedListener?.invoke("BLUETOOTH")
            }
        }

        context.registerReceiver(
            headsetReceiver,
            IntentFilter(Intent.ACTION_HEADSET_PLUG)
        )

        context.registerReceiver(
            bluetoothReceiver,
            IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED).apply {
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    private fun unregisterReceivers() {
        headsetReceiver?.let { context.unregisterReceiver(it) }
        bluetoothReceiver?.let { context.unregisterReceiver(it) }
        headsetReceiver = null
        bluetoothReceiver = null
    }
}