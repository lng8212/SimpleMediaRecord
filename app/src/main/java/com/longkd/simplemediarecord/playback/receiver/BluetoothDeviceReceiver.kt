package com.longkd.simplemediarecord.playback.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothDeviceReceiver(private val onBluetoothStateChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onBluetoothStateChanged(true)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onBluetoothStateChanged(false)
            }
        }
    }
}