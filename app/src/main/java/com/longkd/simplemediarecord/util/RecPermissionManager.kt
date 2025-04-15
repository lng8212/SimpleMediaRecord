package com.longkd.simplemediarecord.util

import android.Manifest
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.longkd.simplemediarecord.R
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

@ActivityScoped
class RecPermissionManager @Inject constructor() {

    suspend fun checkOrRequestRecordingPermission(
        activity: FragmentActivity
    ): Boolean = suspendCancellableCoroutine {
        val permissionsToGet = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
        )

        PermissionX.init(activity)
            .permissions(permissionsToGet)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    message = activity.getString(R.string.permissions_rationale),
                    positiveText = activity.getString(android.R.string.ok)
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    message = activity.getString(
                        R.string.permissions_rationale_grant_in_settings,
                        activity.getString(R.string.permissions_rationale)
                    ),
                    positiveText = activity.getString(android.R.string.ok)
                )
            }.request { allGranted: Boolean, grantedList, deniedList ->
                it.resume(allGranted)
            }
    }

}