package com.derdirk.happyday;

import java.util.Calendar;
import java.util.Date;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.derdirk.happyday.UnitChooserDialogFragment.UnitChooserDialogListener;
import com.derdirk.happyday.ValueChooserDialogFragment.ValueChooserClient;

public class MainActivity extends    FragmentActivity
                          implements UnitChooserDialogListener, 
                                     ValueChooserClient,
                                     OnClickListener
{
  public static final String NextIntervalUnitSettingsTag    = "NextIntervalUnit";
  public static final String NextIntervalValueSettingsTag   = "NextIntervalValue";
  public static final String CurrentAlertTimeSettingsTag    = "CurrentAlertTime";
  public static final String CurrentIntervalUnitSettingsTag = "CurrentIntervalUnit";
  public static final String WeekendCorrectionSettingsTag   = "WeekendCorrection";
  
  protected int      mNextIntervalUnit    = Calendar.SECOND;
  protected int      mNextIntervalValue   = 5;
  protected long     mCurrentAlertTimeMs  = 0;
  protected int      mCurrentIntervalUnit = Calendar.WEEK_OF_YEAR;
  protected Boolean  mWeekendCorrection   = true;
  
  protected TextView                  mValueLabelTextView        = null;
  protected TextView                  mUnitLabelTextView         = null;
  protected LinearLayout              mIntervalValueLayout       = null;
  protected LinearLayout              mIntervalUnitLayout        = null;
  protected CheckBox                  mWeekendCorrectionCheckBox = null;
  protected TextView                  mNextReminderTextView      = null;
  protected UnitToResourceMapping     mUnitToResourceMapping     = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    mValueLabelTextView        = (TextView)     findViewById(R.id.interval_value_text_view);
    mUnitLabelTextView         = (TextView)     findViewById(R.id.interval_unit_text_view);
    mIntervalValueLayout       = (LinearLayout) findViewById(R.id.interval_value_layout);
    mWeekendCorrectionCheckBox = (CheckBox)     findViewById(R.id.weekendcorrection_checkbox);
    mIntervalUnitLayout        = (LinearLayout) findViewById(R.id.interval_unit_layout);
    mNextReminderTextView      = (TextView)     findViewById(R.id.next_reminder_text_view);
    mUnitToResourceMapping     = new UnitToResourceMapping(this);
    
    mIntervalValueLayout.setOnClickListener(this);
    mIntervalUnitLayout.setOnClickListener(this);
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    // Restore preferences
    SharedPreferences settings = getSharedPreferences("HappyDay", MODE_PRIVATE);
    mNextIntervalUnit    = settings.getInt(    NextIntervalUnitSettingsTag,    Calendar.DAY_OF_YEAR);
    mNextIntervalValue   = settings.getInt(    NextIntervalValueSettingsTag,   2);
    mCurrentAlertTimeMs  = settings.getLong(   CurrentAlertTimeSettingsTag,    0);
    mCurrentIntervalUnit = settings.getInt(    CurrentIntervalUnitSettingsTag, Calendar.WEEK_OF_YEAR);
    mWeekendCorrection   = settings.getBoolean(WeekendCorrectionSettingsTag,   true);
    
    mValueLabelTextView.setText(Integer.toString(mNextIntervalValue));
    mUnitLabelTextView.setText(mUnitToResourceMapping.getResource(mNextIntervalUnit));
    mWeekendCorrectionCheckBox.setChecked(mWeekendCorrection);
    
    // Update reminder text
    updateNextAlertText();
  }
  
  @Override
  protected void onPause()
  {
    super.onPause();
    
    // Save preferences
    SharedPreferences settings = getSharedPreferences("HappyDay", MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt(    NextIntervalUnitSettingsTag,    mNextIntervalUnit);
    editor.putInt(    NextIntervalValueSettingsTag,   mNextIntervalValue);
    editor.putLong(   CurrentAlertTimeSettingsTag,    mCurrentAlertTimeMs);
    editor.putInt(    CurrentIntervalUnitSettingsTag, mCurrentIntervalUnit);
    editor.putBoolean(WeekendCorrectionSettingsTag,   mWeekendCorrection);
    
    editor.commit();
  }
  
  public void onDoneButtonPressed(View view)
  {
    AlertManager.cancelAlert(this);
    NotificationManager.clearNotification(this);
    
    if (Calendar.getInstance().getTimeInMillis() < mCurrentAlertTimeMs)
      Log.d("MainActivity", "Reset reference time due to early 'done'");
    
    // Reset reference time if the done button was pressed before an alert was issued
    // or if the reference time was not set yet
    if (mCurrentAlertTimeMs == 0 || Calendar.getInstance().getTimeInMillis() < mCurrentAlertTimeMs)
      mCurrentAlertTimeMs = Calendar.getInstance().getTimeInMillis();
      
    mCurrentAlertTimeMs = AlarmTimeCalculator.getAlarmTime(mCurrentAlertTimeMs, mNextIntervalUnit, mNextIntervalValue);
    mCurrentIntervalUnit = mNextIntervalUnit;
    
    if (mWeekendCorrection)
      mCurrentAlertTimeMs = WeekendCorrectionCalculator.getAlarmTime(mCurrentAlertTimeMs);
      
    AlertManager.setAlert(this, mCurrentAlertTimeMs);
    
    updateNextAlertText();
  }
  
  public void onDodgeButtonPressed(View view)
  {
    long tmpAlertTimeMs = mCurrentAlertTimeMs;
    mCurrentAlertTimeMs = DodgeCalculator.getAlarmTime(mCurrentAlertTimeMs);
    
    if (mCurrentAlertTimeMs != tmpAlertTimeMs)
    {
      AlertManager.cancelAlert(this);
      NotificationManager.clearNotification(this);
      AlertManager.setAlert(this, mCurrentAlertTimeMs);
      updateNextAlertText();
    }
  }
  
  public void onStopButtonPressed(View view)
  {
    mCurrentAlertTimeMs = 0;
    
    AlertManager.cancelAlert(this);
    NotificationManager.clearNotification(this);
    
    updateNextAlertText();
  }

  public void onWeekendCorrectionCheckboxClicked(View view)
  {
    mWeekendCorrection = ((CheckBox)view).isChecked();
  }
  
  protected void updateNextAlertText()
  {
    if (mCurrentAlertTimeMs != 0)
      mNextReminderTextView.setText(DateFormat.format(new Date(mCurrentAlertTimeMs), mCurrentIntervalUnit));
    else
      mNextReminderTextView.setText(getString(R.string.no_next_reminder_text));    
  }

  protected void showValueChooser()
  {
    // Create and show the dialog.
    DialogFragment newFragment = new ValueChooserDialogFragment();
    newFragment.show(getSupportFragmentManager(), "valuechooser");
  }  
  
  protected void showUnitChooser()
  {
    // Create and show the dialog.
    DialogFragment newFragment = new UnitChooserDialogFragment();
    newFragment.show(getSupportFragmentManager(), "unitchooser");
  }
  
  // Value chooser callback
  @Override
  public int provideInitialValue(DialogFragment dialog)
  {
    return mNextIntervalValue;
  }
  
  // Value chooser callback
  @Override
  public void onValueSelected(DialogFragment dialog, int value)
  {
    mNextIntervalValue = value;
    mValueLabelTextView.setText(Integer.toString(mNextIntervalValue));
  }
  
  // Unit chooser callback
  @Override
  public void onUnitSelected(DialogFragment dialog, int which)
  {
    switch (which) // TODO: Make this more generic
    {
      case 0: mNextIntervalUnit = Calendar.SECOND; break;
      case 1: mNextIntervalUnit = Calendar.DAY_OF_YEAR; break;
      case 2: mNextIntervalUnit = Calendar.WEEK_OF_YEAR; break;
    }
    mUnitLabelTextView.setText(mUnitToResourceMapping.getResource(mNextIntervalUnit));
  }

  @Override
  public void onClick(View v)
  {
    if (v.getId() == R.id.interval_unit_layout)
      showUnitChooser();
    else if (v.getId() == R.id.interval_value_layout)
      showValueChooser();
  }  
}
