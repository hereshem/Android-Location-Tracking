package com.hereshem.locationservice;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LocationServiceActivity extends Activity {

	private long UPDATE_INTERVAL = 10000;
	private int START_DELAY = 5;
	private String DEBUG_TAG = "LocationServiceActivity";
    TextView text;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        text = (TextView) findViewById(R.id.text);
        start(null);
    }

    public void start(View view){
        Log.d(DEBUG_TAG, ">>>Start tracking()");
        setRecurringAlarm();
    }

    public void stop(View view){
        Log.d(DEBUG_TAG, ">>>Stop tracking()");
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent tracking = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(tracking);
    }

    private void setRecurringAlarm() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, START_DELAY);
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent tracking = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), UPDATE_INTERVAL, tracking);
//        alarms.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), tracking);
    }

    Double lat = 0.0, lon = 0.0;
    BroadcastReceiver myRcv = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("gotLocation", false)) {
                Double newLat = intent.getDoubleExtra("latitude", 0);
                Double newLon = intent.getDoubleExtra("longitude", 0);
                String report = "Latitude = " + newLat
                        + "\nLongitude = " + newLon
                        + "\n\nDelta lat = " + (newLat-lat)
                        + "\nDelta lon = " + (newLon-lon)
                        + "\nAccuracy = " + intent.getFloatExtra("accuracy", 0)
                        + "\nAltitude = " + intent.getDoubleExtra("altitude", 0)
                        + "\nBearing = " + intent.getFloatExtra("bearing", 0)
                        + "\n\nProvider = " + intent.getStringExtra("provider")
                        ;
                lat = newLat;
                lon = newLon;
                text.setText(report);
                Toast.makeText(context, "Latitude = " + lat + "\nLongitude = " + lon, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context,
                        "Error message = " + intent.getStringExtra("message"),
                        Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(myRcv, new IntentFilter("myLocationTracker"));
    }
    
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(myRcv);
    }

    @Override
    public void finish() {
        super.finish();
        stop(null);
    }
}