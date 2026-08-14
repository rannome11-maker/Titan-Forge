package com.dangerdan.titanforge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("daily_forge", "Daily Forge", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(117, NotificationCompat.Builder(context, "daily_forge")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("THE FORGE IS WAITING")
            .setContentText("Complete today's trials. Your hero does not evolve from intention.")
            .setContentIntent(open).setAutoCancel(true).build())
    }
}
