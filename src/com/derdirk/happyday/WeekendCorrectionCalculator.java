package com.derdirk.happyday;

import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class WeekendCorrectionCalculator
{

  public static long getAlarmTime(long referenceTimeMs)
  {
    Log.d("WeekendCorrectionCalculator", "getAlarmTime: Reference time: " + new Date(referenceTimeMs).toString());

    // Get current time and setup calendar
    
    Calendar calNow = Calendar.getInstance();    
    Log.d("WeekendCorrectionCalculator", "getAlarmTime: Now: " + calNow.getTime().toString());

    // Calculate alert time

    Calendar calAlarmTime = Calendar.getInstance();
    calAlarmTime.setTimeInMillis(referenceTimeMs);

    if (!correctionNecessary(calAlarmTime))
        return referenceTimeMs;

    if (isFriday(calNow) || isWeekend(calNow))
    {
      calAlarmTime.roll(Calendar.WEEK_OF_YEAR, 1);
      calAlarmTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      Log.d("WeekendCorrectionCalculator", "getAlarmTime: Next alarm on a weekend and today is friday or weekend: Set alarm to next monday");
    }
    else
    {
      calAlarmTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
      Log.d("WeekendCorrectionCalculator", "getAlarmTime: Next alarm on a weekend and today is not friday or weekend: Set alarm to friday");
    }

    long alarmTimeAdjustedMs = calAlarmTime.getTimeInMillis();
    Log.d("WeekendCorrectionCalculator", "getAlarmTime: Then adjusted: " + calAlarmTime.getTime().toString());

    // Return Alarm time
    return alarmTimeAdjustedMs;
  }

  public static Boolean correctionNecessary(Calendar calTime)
  {
    return isWeekend(calTime);
  }

  private static Boolean isFriday(Calendar calTime)
  {
    return calTime.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
  }

  private static Boolean isWeekend(Calendar calTime)
  {
    return calTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
  }

}
