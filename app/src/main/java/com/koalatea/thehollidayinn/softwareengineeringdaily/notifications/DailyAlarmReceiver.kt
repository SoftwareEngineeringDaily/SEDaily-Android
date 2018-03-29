package com.koalatea.thehollidayinn.softwareengineeringdaily.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity
import com.koalatea.thehollidayinn.softwareengineeringdaily.R

/**
 * Created by keithholliday on 3/23/18.
 */
class DailyAlarmReceiver: BroadcastReceiver() {
    var DAILY_REMINDER_REQUEST_CODE = 198762999

    override fun onReceive(context: Context, intent: Intent) {
        // @TODO: Restart on reboot
//        if (intent.action != null)
//        {
//            if (intent.action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
//            {
//                val localData = LocalData(context)
//                NotificationScheduler.setReminder(context, AlarmReceiver::class.java,
//                        localData.get_hour(), localData.get_min())
//                return
//            }
//        }

        showNotification(context, "You have 5 unwatched videos", "Watch them now?")
    }

    fun showNotification(context: Context, title: String, content: String) {
        var alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationIntent: Intent  = Intent(context, MainActivity::class.java)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
                DAILY_REMINDER_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context)
        val notification = builder.setContentTitle(title)
                .setContentText(content).setAutoCancel(true)
                .setSound(alarmSound).setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent).build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification)
    }
}