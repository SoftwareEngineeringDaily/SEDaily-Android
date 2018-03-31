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
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository
import kotlinx.android.synthetic.main.activity_notification.*
import java.util.*

// http://droidmentor.com/schedule-notifications-using-alarmmanager/

class NotificationActivity : AppCompatActivity() {

    var DAILY_REMINDER_REQUEST_CODE = 198762999
    var DAILY_REMINDER_REQUEST_CODE2 = 198762998
    var DAILY_REMINDER_REQUEST_CODE3 = 198762998

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val userRepo: UserRepository = UserRepository.getInstance(this);

        if (userRepo.subscribed) {
            switch1.isChecked = true
        }

        switch1.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (userRepo.subscribed) {
                    userRepo.subscribed = false
                    cancelReminder(applicationContext, DAILY_REMINDER_REQUEST_CODE)
                    cancelReminder(applicationContext, DAILY_REMINDER_REQUEST_CODE2)
                    cancelReminder(applicationContext, DAILY_REMINDER_REQUEST_CODE3)
                    return
                }

                setReminder(applicationContext, Calendar.MONDAY,10, 0, DAILY_REMINDER_REQUEST_CODE)
                setReminder(applicationContext, Calendar.WEDNESDAY, 10, 0, DAILY_REMINDER_REQUEST_CODE2)
                setReminder(applicationContext, Calendar.FRIDAY, 10, 0, DAILY_REMINDER_REQUEST_CODE3)
                userRepo.subscribed = true
            }
        })
    }

    fun setReminder(context: Context, day: Int, hour: Int, min: Int, code: Int) {
        var setCalendar: Calendar = Calendar.getInstance()

        setCalendar.set(Calendar.DAY_OF_WEEK, day)
        setCalendar.set(Calendar.HOUR_OF_DAY, hour)
        setCalendar.set(Calendar.MINUTE, min)
        setCalendar.set(Calendar.SECOND, 0)

        cancelReminder(context, code)

        val receiver = ComponentName(context, MainActivity::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)

        val intent1 = Intent(context, DailyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context,
                code,
                intent1,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, setCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    fun cancelReminder(context:Context, code: Int) {
        // Disable a receiver
        val receiver = ComponentName(context, MainActivity::class.java)

        val pm = context.packageManager
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP)

        val intent1 = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent1,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)

        pendingIntent.cancel()
    }
}
