package com.example.travelapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        restoreAlarms(context)
    }

    private fun restoreAlarms(context: Context) {
        val prefs = context.getSharedPreferences("travel_alarms", Context.MODE_PRIVATE)
        val all   = prefs.all
        all.forEach { (key, value) ->
            if (value !is String) return@forEach

            val parts     = key.split("_")
            if (parts.size < 2) return@forEach

            val bookingId = parts[0].toIntOrNull() ?: return@forEach
            val typeName  = parts.drop(1).joinToString("_")
            val type      = runCatching {
                TravelAlarmManager.ReminderType.valueOf(typeName)
            }.getOrNull() ?: return@forEach
            val triggerMs = value.toLongOrNull() ?: return@forEach

            if (triggerMs <= System.currentTimeMillis()) {
                prefs.edit { remove(key) }
                return@forEach
            }

            when (type) {
                TravelAlarmManager.ReminderType.TRANSPORT ->
                    TravelAlarmManager.scheduleTransportReminder(
                        context, bookingId,
                        triggerMs - 3 * 60 * 60 * 1000L
                    )
                TravelAlarmManager.ReminderType.CHECK_IN ->
                    TravelAlarmManager.scheduleCheckInReminder(context, bookingId, triggerMs)
                TravelAlarmManager.ReminderType.CHECK_OUT ->
                    TravelAlarmManager.scheduleCheckOutReminder(context, bookingId, triggerMs)
                TravelAlarmManager.ReminderType.LOCATION ->
                    TravelAlarmManager.scheduleLocationReminder(context, bookingId, triggerMs)
            }
        }
    }
}

fun saveAlarm(context: Context, bookingId: Int, type: TravelAlarmManager.ReminderType, triggerMs: Long) {
    context.getSharedPreferences("travel_alarms", Context.MODE_PRIVATE)
        .edit {
            putString("${bookingId}_${type.name}", triggerMs.toString())
        }
}

fun removeAlarm(context: Context, bookingId: Int, type: TravelAlarmManager.ReminderType) {
    context.getSharedPreferences("travel_alarms", Context.MODE_PRIVATE)
        .edit {
            remove("${bookingId}_${type.name}")
        }
}