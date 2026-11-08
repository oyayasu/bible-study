package com.oyayasu.biblestudy.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.oyayasu.biblestudy.MainActivity
import com.oyayasu.biblestudy.R
import com.oyayasu.biblestudy.data.LocalStudyStore
import java.time.ZonedDateTime

object ReminderTimes {
    fun next(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
        val scheduled = now.toLocalDate().atTime(hour, minute).atZone(now.zone)
        return if (scheduled.isAfter(now)) scheduled else scheduled.plusDays(1)
    }
}

object ReminderScheduler {
    const val MORNING = "morning"
    const val EVENING = "evening"
    const val CHANNEL_ID = "daily_medita"
    private const val ACTION_REMIND = "com.oyayasu.biblestudy.REMIND"

    fun hasExactPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .canScheduleExactAlarms()
    }

    fun scheduleAll(context: Context) {
        val store = LocalStudyStore(context)
        schedule(context, MORNING, 6, 0, store.morningEnabled(), 1001)
        schedule(context, EVENING, 20, 30, store.eveningEnabled(), 1002)
    }

    private fun schedule(
        context: Context,
        session: String,
        hour: Int,
        minute: Int,
        enabled: Boolean,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            putExtra("session", session)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
        if (!enabled) return

        val time = ReminderTimes.next(ZonedDateTime.now(), hour, minute)
        val triggerAt = time.toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasExactPermission(context)) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, pending
                    )
                    return
                } catch (_: SecurityException) {
                    // The user may revoke special alarm access while the app is active.
                }
            }
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val session = intent.getStringExtra("session") ?: ReminderScheduler.MORNING
        // Each delivered one-shot alarm schedules the next day in the current device timezone.
        ReminderScheduler.scheduleAll(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            "Meditações diárias",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Lembretes de meditação da manhã e da noite"
        }
        manager.createNotificationChannel(channel)

        val isMorning = session == ReminderScheduler.MORNING
        val title = if (isMorning) "Meditação da manhã" else "Meditação da noite"
        val body = if (isMorning) {
            "Comece o dia com a Palavra."
        } else {
            "Um momento para refletir sobre o dia diante de Deus."
        }
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val open = PendingIntent.getActivity(
            context, 2001, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        manager.notify(if (isMorning) 301 else 302, notification)
    }
}

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
            !ReminderScheduler.hasExactPermission(context)
        ) return
        ReminderScheduler.scheduleAll(context)
    }
}
