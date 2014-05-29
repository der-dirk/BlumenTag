package com.derdirk.blumentag;
import android.app.IntentService;
import android.content.Intent;


public class BlumenTagService extends IntentService
{
  public BlumenTagService()
  {
    super("BlumenTagService");
  }

  @Override
  protected void onHandleIntent(Intent workIntent)
  {
     NotificationManager.setNotification(this);
  }
}
