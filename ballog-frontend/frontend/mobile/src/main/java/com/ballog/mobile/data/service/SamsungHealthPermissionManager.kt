package com.ballog.mobile.data.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes

private const val TAG = "SamsungHealthPermissionManager"

class SamsungHealthPermissionManager(private val context: Context) {
    private var healthDataStore: HealthDataStore? = null

    // 필요한 권한 목록을 클래스 레벨에서 한 번만 정의
    private val requiredPermissions = setOf(
        Permission.of(DataTypes.EXERCISE, AccessType.READ),
        Permission.of(DataTypes.HEART_RATE, AccessType.READ),
        Permission.of(DataTypes.EXERCISE_LOCATION, AccessType.READ)
    )

    fun initialize(): Result<HealthDataStore> {
        return try {
            healthDataStore = HealthDataService.getStore(context)
            Log.d(TAG, "Samsung Health 초기화 성공")
            Result.success(healthDataStore!!)
        } catch (e: Exception) {
            Log.e(TAG, "Samsung Health 초기화 실패: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkPermissions(): Boolean {
        // 부여된 권한 확인
        val grantedPermissions = healthDataStore?.getGrantedPermissions(requiredPermissions)
        return grantedPermissions?.containsAll(requiredPermissions) ?: false
    }

    suspend fun requestPermissions(): Result<Set<Permission>> {
        return try {
            val permissions = setOf(
                Permission.of(DataTypes.EXERCISE, AccessType.READ),
                Permission.of(DataTypes.HEART_RATE, AccessType.READ),
                Permission.of(DataTypes.EXERCISE_LOCATION, AccessType.READ)
            )

            val grantedPermissions = healthDataStore?.requestPermissions(
                permissions,
                context as Activity
            )

            Log.d(TAG, "요청한 permissions: $permissions")
            Log.d(TAG, "grantedPermissions: $grantedPermissions")
            Log.d(TAG, "containsAll: ${grantedPermissions?.containsAll(permissions)}")

            if (grantedPermissions?.containsAll(permissions) == true) {
                Log.d(TAG, "모든 권한이 승인되었습니다")
                Result.success(grantedPermissions)
            } else {
                Log.e(TAG, "필요한 권한이 부여되지 않았습니다")
                Result.failure(Exception("필요한 권한이 부여되지 않았습니다"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "권한 요청 실패: ${e.message}")
            Result.failure(e)
        }
    }
}
