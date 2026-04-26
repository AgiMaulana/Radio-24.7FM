package io.github.agimaulana.radio.core.design

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val PermissionPreferencesName = "core_design_permissions"
private const val PermissionAskedKeyPrefix = "has_asked_permission_"

@Composable
fun rememberMultiplePermissionsState(
    vararg permissions: String,
    onPermissionResolved: ((Boolean) -> Unit)? = null
): MultiplePermissionState {
    val context = LocalContext.current
    val permissionList = remember(*permissions) { permissions.toList() }
    val permissionKey = remember(permissionList) { permissionList.sorted().joinToString(separator = "|") }

    var permissionsStatus by remember(permissionKey, context) {
        mutableStateOf(context.checkPermissionStatuses(permissionList))
    }
    var shouldShowRationale by remember(permissionKey, context) {
        mutableStateOf(context.shouldShowAnyPermissionRationale(permissionList))
    }
    var hasAskedPermission by rememberSaveable(permissionKey) {
        mutableStateOf(
            context.hasAskedPermission(permissionKey) ||
                shouldShowRationale ||
                permissionsStatus.values.any { it }
        )
    }
    var requestResultCount by rememberSaveable(permissionKey) { mutableStateOf(0) }

    fun refreshPermissionSnapshot() {
        val latestStatuses = context.checkPermissionStatuses(permissionList)
        val latestShouldShowRationale = context.shouldShowAnyPermissionRationale(permissionList)
        permissionsStatus = latestStatuses
        shouldShowRationale = latestShouldShowRationale
        val latestHasAskedPermission = context.hasAskedPermission(permissionKey) ||
            latestShouldShowRationale ||
            latestStatuses.values.any { it }
        if (!hasAskedPermission && latestHasAskedPermission) {
            hasAskedPermission = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        // ActivityResult callback is the source of truth for whether permissions were granted.
        val isGranted = result.values.all { it }
        context.markPermissionAsked(permissionKey)
        hasAskedPermission = true
        permissionsStatus = context.checkPermissionStatuses(permissionList)
        shouldShowRationale = context.shouldShowAnyPermissionRationale(permissionList)
        requestResultCount += 1
        // Notify caller immediately so they can act on the definitive result (avoids race conditions).
        onPermissionResolved?.invoke(isGranted)
    }

    return remember(permissionsStatus, shouldShowRationale, hasAskedPermission, requestResultCount, permissionKey) {
        MultiplePermissionState(
            statusMap = permissionsStatus,
            shouldShowRationale = shouldShowRationale,
            hasAskedPermission = hasAskedPermission,
            requestResultCount = requestResultCount,
            launchRequest = { launcher.launch(permissionList.toTypedArray()) },
        )
    }
}

@Stable
class MultiplePermissionState internal constructor(
    val statusMap: Map<String, Boolean>,
    val shouldShowRationale: Boolean,
    val hasAskedPermission: Boolean,
    val requestResultCount: Int,
    private val launchRequest: () -> Unit,
) {
    val allGranted: Boolean get() = statusMap.values.all { it }

    fun launchPermissionRequest() = launchRequest()
}

private fun Context.checkPermissionStatuses(permissions: List<String>): Map<String, Boolean> =
    permissions.associateWith { permission ->
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

private fun Context.shouldShowAnyPermissionRationale(permissions: List<String>): Boolean =
    findActivity()?.let { activity ->
        permissions.any { permission -> activity.shouldShowRequestPermissionRationale(permission) }
    } ?: false

private fun Context.hasAskedPermission(permissionKey: String): Boolean =
    permissionPreferences().getBoolean(PermissionAskedKeyPrefix + permissionKey, false)

private fun Context.markPermissionAsked(permissionKey: String) {
    permissionPreferences()
        .edit()
        .putBoolean(PermissionAskedKeyPrefix + permissionKey, true)
        .apply()
}

private fun Context.permissionPreferences() =
    applicationContext.getSharedPreferences(PermissionPreferencesName, Context.MODE_PRIVATE)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
