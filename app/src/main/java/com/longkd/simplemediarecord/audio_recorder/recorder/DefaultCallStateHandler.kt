package com.longkd.simplemediarecord.audio_recorder.recorder

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.CallStateHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultCallStateHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : CallStateHandler {
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private var modernCallback: TelephonyCallback? = null

    @Suppress("DEPRECATION")
    private var legacyListener: PhoneStateListener? = null

    override fun startListening(onCallDetected: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modernCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        onCallDetected.invoke()
                    }
                }
            }
            telephonyManager.registerTelephonyCallback(
                ContextCompat.getMainExecutor(context),
                modernCallback as TelephonyCallback
            )
        } else {
            @Suppress("DEPRECATION")
            legacyListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        onCallDetected.invoke()
                    }
                }
            }
            @Suppress("DEPRECATION")
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    override fun stopListening() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modernCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
            }
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    override fun release() {
        stopListening()
    }
}