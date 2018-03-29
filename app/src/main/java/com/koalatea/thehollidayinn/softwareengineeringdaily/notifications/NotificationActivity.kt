package com.koalatea.thehollidayinn.softwareengineeringdaily.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity
import com.koalatea.thehollidayinn.softwareengineeringdaily.R
import kotlinx.android.synthetic.main.activity_notification.*
import java.util.*

// http://droidmentor.com/schedule-notifications-using-alarmmanager/

class NotificationActivity : AppCompatActivity() {

    var DAILY_REMINDER_REQUEST_CODE = 198762999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        switch1.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                setReminder(applicationContext, 8, 30)
            }
        })
    }

    fun setReminder(context: Context, hour: Int, min: Int) {
        var calendar: Calendar = Calendar.getInstance()
        var setCalendar: Calendar = Calendar.getInstance()
        setCalendar.set(Calendar.HOUR_OF_DAY, hour)
        setCalendar.set(Calendar.MINUTE, min)
        setCalendar.set(Calendar.SECOND, 0)

        cancelReminder(context);

        if (setCalendar.before(calendar)) setCalendar.add(Calendar.DATE, 1)


        val receiver = ComponentName(context, MainActivity::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)

        val intent1 = Intent(context, DailyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context,
                DAILY_REMINDER_REQUEST_CODE, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, setCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent)

    }

    fun cancelReminder(context:Context) {
        // Disable a receiver
        val receiver = ComponentName(context, MainActivity::class.java)

        val pm = context.packageManager
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)

        val intent1 = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context,
                DAILY_REMINDER_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)

        pendingIntent.cancel()
    }
}
