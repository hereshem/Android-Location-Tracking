package com.hereshem.locationservice;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class UpdateLocation extends Service implements LocationListener{
	
  private Looper mServiceLooper;
  private ServiceHandler mServiceHandler;
  private final String DEBUG_TAG = "UpdateLocation::Service";
  private LocationManager mgr;
  private String best;
 
  
  private final class ServiceHandler extends Handler {
      public ServiceHandler(Looper looper) {
          super(looper);
      }
      
      @Override
      public void handleMessage(Message msg) {
          Location location = mgr.getLastKnownLocation(best);
          mServiceHandler.post(new SendCast(location));
          // Stop the service using the startId, so that we don't stop
          // the service in the middle of handling another job
          //stopSelf(msg.arg1);
      }
  }

  @Override
  public void onCreate() {
    // Start up the thread running the service.  Note that we create a
    // separate thread because the service normally runs in the process's
    // main thread, which we don't want to block.  We also make it
    // background priority so CPU-intensive work will not disrupt our UI.
    HandlerThread thread = new HandlerThread("ServiceStartArguments",
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    Log.d(DEBUG_TAG, ">>>onCreate()");
    // Get the HandlerThread's Looper and use it for our Handler
    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);
    mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    best = mgr.getBestProvider(criteria, true);
    mgr.requestLocationUpdates(best, 10000, 10, this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
      // For each start request, send a message to start a job and deliver the
      // start ID so we know which request we're stopping when we finish the job
      Message msg = mServiceHandler.obtainMessage();
      msg.arg1 = startId;
      mServiceHandler.sendMessage(msg);
      Log.d(DEBUG_TAG, ">>>onStartCommand()");
      // If we get killed, after returning from here, restart
      return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
      // We don't provide binding, so return null
      return null;
  }
  
  @Override
  public void onDestroy() {
	  Log.d(DEBUG_TAG, ">>>onDestroy()");
      mgr.removeUpdates(this);
      super.onDestroy();
  }

  private class SendCast implements Runnable {
		Location location;
		
		public SendCast(Location location){
		    this.location = location;
		}
		public void run(){
            //Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
            Intent i = new Intent("myLocationTracker");
            if(location != null) {
                i.putExtra("gotLocation", true)
                        .putExtra("latitude", location.getLatitude())
                        .putExtra("longitude", location.getLongitude())
                        .putExtra("altitude", location.getAltitude())
                        .putExtra("accuracy", location.getAccuracy())
                        .putExtra("bearing", location.getBearing())
                        .putExtra("speed", location.getSpeed())
                        .putExtra("time", location.getTime())
                        .putExtra("provider", location.getProvider());
            }
            else {
                i.putExtra("gotLocation", false).putExtra("message", "Location currently unavailable.");
            }
            sendBroadcast(i);
        }
  }
  
	@Override
	public void onLocationChanged(Location location) {
        Log.w(DEBUG_TAG, ">>>location changed: ");
        mServiceHandler.post(new SendCast(location));
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.w(DEBUG_TAG, ">>>provider disabled: " + provider);
	}


	@Override
	public void onProviderEnabled(String provider) {
		Log.w(DEBUG_TAG, ">>>provider enabled: " + provider);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w(DEBUG_TAG, ">>>provider status changed: " + provider);
	}
}