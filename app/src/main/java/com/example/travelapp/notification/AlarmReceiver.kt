package com.example.travelapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.travelapp.view.ProfileNavigation

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID   = "travel_reminders"
        private const val CHANNEL_NAME = "Нагадування про подорожі"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title     = intent.getStringExtra(TravelAlarmManager.EXTRA_TITLE)     ?: return
        val message   = intent.getStringExtra(TravelAlarmManager.EXTRA_MESSAGE)   ?: return
        val bookingId = intent.getIntExtra(TravelAlarmManager.EXTRA_BOOKING_ID, 0)
        val typeName  = intent.getStringExtra(TravelAlarmManager.EXTRA_REMINDER_TYPE)
        val type      = runCatching {
            TravelAlarmManager.ReminderType.valueOf(typeName ?: "")
        }.getOrNull()

        createChannelIfNeeded(context)
        showNotification(context, bookingId, type, title, message)
    }


    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description          = "Нагадування про транспорт, заселення та локації"
            enableVibration(true)
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        context: Context,
        bookingId: Int,
        type: TravelAlarmManager.ReminderType?,
        title: String,
        message: String,
    ) {

        val tapIntent = buildTapIntent(context, type)

        val tapPi = tapIntent?.let {
            PendingIntent.getActivity(
                context, bookingId, it,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPi)
            .build()

        NotificationManagerCompat.from(context).notify(bookingId, notification)
    }

    private fun buildTapIntent(
        context: Context,
        type: TravelAlarmManager.ReminderType?,
    ): Intent? {
        return when (type) {
            TravelAlarmManager.ReminderType.LOCATION -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("travelapp://trips/active_trips"),
                ).apply {
                    setPackage(context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
            else -> {
                context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            }
        }
    }
}