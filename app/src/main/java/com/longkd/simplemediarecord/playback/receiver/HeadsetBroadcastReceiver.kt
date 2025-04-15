package com.longkd.simplemediarecord.playback.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HeadsetBroadcastReceiver(private val onHeadsetStateChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)
            val isConnected = state == 1
            onHeadsetStateChanged(isConnected)
        }
    }
}