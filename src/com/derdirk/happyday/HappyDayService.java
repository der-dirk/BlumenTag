package com.derdirk.happyday;
import android.app.IntentService;
import android.content.Intent;


public class HappyDayService extends IntentService
{
  public HappyDayService()
  {
    super("HappyDayService");
  }

  @Override
  protected void onHandleIntent(Intent workIntent)
  {
     NotificationManager.setNotification(this);
  }
}
