package com.ballog.watch.data.util

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log

object VibrationUtil {
    // 일관된 로그 태그 추가
    private const val TAG = "BallogVibration"

    fun vibrate(context: Context) {
        try {
            Log.d(TAG, "진동 시작")
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            // EFFECT_HEAVY_CLICK는 동작하지 않음, EFFECT_CLICK은 약함
            // 진동 길이(ms), 강도(1-255, 255가 최대)
            val effect = VibrationEffect.createOneShot(200, 200)
            val attributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            vibrator.vibrate(effect, attributes)
            Log.d(TAG, "진동 완료")
        } catch (e: Exception) {
            Log.e(TAG, "진동 실패: ${e.message}")
        }
    }
}
