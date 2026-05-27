package com.example.travelapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.travelapp.utils.AppStrings
import com.example.travelapp.utils.LocaleManager
import com.example.travelapp.utils.toStrings
import java.util.Calendar
import java.util.Date

object TravelAlarmManager {

    enum class ReminderType {
        TRANSPORT, CHECK_IN, CHECK_OUT, LOCATION;

        fun getTitle(strings: AppStrings) = when (this) {
            TRANSPORT -> strings.notifTransportTitle
            CHECK_IN  -> strings.notifCheckInTitle
            CHECK_OUT -> strings.notifCheckOutTitle
            LOCATION  -> strings.notifLocationTitle
        }

        fun getMessage(strings: AppStrings) = when (this) {
            TRANSPORT -> strings.notifTransportMessage
            CHECK_IN  -> strings.notifCheckInMessage
            CHECK_OUT -> strings.notifCheckOutMessage
            LOCATION  -> strings.notifLocationMessage
        }
    }

    const val EXTRA_REMINDER_TYPE = "reminder_type"
    const val EXTRA_TITLE = "title"
    const val EXTRA_MESSAGE = "message"
    const val EXTRA_BOOKING_ID = "booking_id"


    fun scheduleTransportReminder(
        context: Context,
        bookingId: Int,
        departureMs: Long,
    ) {
        val triggerAt = departureMs - 3 * 60 * 60 * 1000L
        schedule(context, bookingId, ReminderType.TRANSPORT, triggerAt)
    }

    fun scheduleCheckInReminder(
        context: Context,
        bookingId: Int,
        checkInDayMs: Long,
    ) {
        val triggerAt = buildTimeOnDay(checkInDayMs, hour = 9, minute = 0)
        schedule(context, bookingId, ReminderType.CHECK_IN, triggerAt)
    }

    fun scheduleCheckOutReminder(
        context: Context,
        bookingId: Int,
        checkOutDayMs: Long,
    ) {
        val triggerAt = buildTimeOnDay(checkOutDayMs, hour = 9, minute = 0)
        schedule(context, bookingId, ReminderType.CHECK_OUT, triggerAt)
    }

    fun scheduleLocationReminder(
        context: Context,
        bookingId: Int,
        visitDayMs: Long,
    ) {
        val triggerAt = buildTimeOnDay(visitDayMs, hour = 8, minute = 0)
        schedule(context, bookingId, ReminderType.LOCATION, triggerAt)
    }

    fun cancel(context: Context, bookingId: Int, type: ReminderType) {
        val strings = LocaleManager.getSavedLocale(context).toStrings()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, requestCode(bookingId, type), type, bookingId, strings)
        am.cancel(pi)
        pi.cancel()
    }

    private fun schedule(
        context: Context,
        bookingId: Int,
        type: ReminderType,
        triggerAtMs: Long,
    ) {
        if (triggerAtMs <= System.currentTimeMillis()) {
            Log.w("TravelAlarm", "[$type] Час уже минув, пропускаємо")
            return
        }
        saveAlarm(context, bookingId, type, triggerAtMs)

        val strings = LocaleManager.getSavedLocale(context).toStrings()

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, requestCode(bookingId, type), type, bookingId, strings)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("TravelAlarm", "[$type] canScheduleExactAlarms=${am.canScheduleExactAlarms()}")

            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
                Log.d("TravelAlarm", "[$type] Заплановано ТОЧНО на ${Date(triggerAtMs)}")
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
                Log.w("TravelAlarm", "[$type] Заплановано НЕТОЧНО (немає дозволу) на ${Date(triggerAtMs)}")
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            Log.d("TravelAlarm", "[$type] Заплановано на ${Date(triggerAtMs)}")
        }
        scheduleSystemAlarm(context, bookingId, type, triggerAtMs)
    }

    private fun scheduleSystemAlarm(
        context: Context,
        bookingId: Int,
        type: ReminderType,
        triggerAtMs: Long,
    ) {
        val strings = LocaleManager.getSavedLocale(context).toStrings()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, requestCode(bookingId, type), type, bookingId, strings)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }
    fun cancelSystemAlarmOnly(context: Context, bookingId: Int, type: ReminderType) {
        val strings = LocaleManager.getSavedLocale(context).toStrings()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, requestCode(bookingId, type), type, bookingId, strings)
        am.cancel(pi)
        pi.cancel()
    }

    fun cancelAllSystemAlarms(context: Context) {
        val prefs = context.getSharedPreferences("travel_alarms", Context.MODE_PRIVATE)
        prefs.all.keys.forEach { key ->
            val parts = key.split("_")
            if (parts.size < 2) return@forEach
            val bookingId = parts[0].toIntOrNull() ?: return@forEach
            val type = runCatching {
                ReminderType.valueOf(parts.drop(1).joinToString("_"))
            }.getOrNull() ?: return@forEach
            cancelSystemAlarmOnly(context, bookingId, type)
        }
    }
    fun restoreAllSavedAlarms(context: Context) {
        val prefs = context.getSharedPreferences("travel_alarms", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        prefs.all.forEach { (key, value) ->
            Log.d("TravelAlarm", "  key=$key value=$value")
            if (value !is String) return@forEach
            val parts = key.split("_")
            if (parts.size < 2) return@forEach
            val bookingId = parts[0].toIntOrNull() ?: return@forEach
            val type = runCatching {
                ReminderType.valueOf(parts.drop(1).joinToString("_"))
            }.getOrNull() ?: return@forEach
            val triggerMs = value.toLongOrNull() ?: return@forEach

            if (triggerMs <= now) {
                removeAlarm(context, bookingId, type)
                return@forEach
            }

            scheduleSystemAlarm(context, bookingId, type, triggerMs)
        }
    }
    private fun buildPendingIntent(
        context: Context,
        requestCode: Int,
        type: ReminderType,
        bookingId: Int,
        strings: AppStrings,
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_TITLE,         type.getTitle(strings))
            putExtra(EXTRA_MESSAGE,       type.getMessage(strings))
            putExtra(EXTRA_BOOKING_ID,    bookingId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun buildTimeOnDay(dayMs: Long, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dayMs
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE,      minute)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun requestCode(bookingId: Int, type: ReminderType): Int =
        bookingId * 10 + type.ordinal
}