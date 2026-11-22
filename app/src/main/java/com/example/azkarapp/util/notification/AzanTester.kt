package com.example.azkarapp.util.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.azkarapp.presentation.azan.AzanActivity

/** أدوات تجربة شاشة الأذان من غير ما تستنى وقت الصلاة الحقيقي. */
object AzanTester {

    private const val TEST_PRAYER = "الظهر (تجربة)"
    private const val REQUEST_CODE = 9001

    /** بيفتح شاشة الأذان فوراً من غير صوت (لمراجعة التصميم بس). الاستدعاء لازم يكون والتطبيق ظاهر. */
    fun previewScreen(context: Context) {
        context.startActivity(
            Intent(context, AzanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AzanActivity.EXTRA_PRAYER_NAME, TEST_PRAYER)
            }
        )
    }

    /**
     * بيجدول أذان تجريبي كامل (صوت + إشعار + شاشة) بعد [delaySeconds] ثانية، بنفس مسار الأذان الحقيقي
     * (منبه بالظبط ← خدمة). اقفل الشاشة بعد ما تدوس علشان تشوف الشاشة فوق قفل الموبايل.
     * بيرجع false لو صلاحية المنبهات الدقيقة مش ممنوحة (وبيفتح إعدادها).
     */
    fun scheduleTest(context: Context, delaySeconds: Int = 10): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        if (!alarmManager.canScheduleExactAlarms()) {
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return false
        }
        val intent = Intent(context, AzanTestReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delaySeconds * 1000L,
            pendingIntent
        )
        return true
    }
}

/** بيستقبل المنبه التجريبي ويشغّل خدمة الأذان زي الأذان الحقيقي. */
class AzanTestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, AzanService::class.java).putExtra("PRAYER_NAME", "الظهر (تجربة)")
        )
    }
}