package com.derdirk.happyday;

import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class DodgeCalculator
{

  public static long getAlarmTime(long referenceTimeMs)
  {
    Log.d("DodgeCalculator", "getAlarmTime: Reference time: " + new Date(referenceTimeMs).toString());

    // Get current time and setup calendar
    
    Calendar calNow = Calendar.getInstance();    
    Log.d("DodgeCalculator", "getAlarmTime: Now: " + calNow.getTime().toString());

    // Calculate alert time

    Calendar calAlarmTime = Calendar.getInstance();
    calAlarmTime.setTimeInMillis(referenceTimeMs);

    if (referenceTimeMs != 0 && calNow.after(calAlarmTime))
      calAlarmTime.roll(Calendar.DAY_OF_WEEK, 1);

    long alarmTimeAdjustedMs = calAlarmTime.getTimeInMillis();
    Log.d("DodgeCalculator", "getAlarmTime: Then adjusted: " + calAlarmTime.getTime().toString());

    // Return Alarm time
    return alarmTimeAdjustedMs;
  }

}
