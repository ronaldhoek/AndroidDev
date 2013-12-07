package nl.rhoek.mygpspos;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import nl.rhoek.mygpspos.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class GPSPosActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	/**
	 * Own code: 
	 * - GPS tracker
	 * - start time
	 */
	private GPSTracker gps;
	private long starttime = 0;
	
	/**
	 * Own code: update TextView with GPS location en time
	 * @param location
	 */
	public void ShowGPSLocation(Location location) {
		TextView t = (TextView) findViewById(R.id.fullscreen_content);
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		DecimalFormat df = new DecimalFormat("0.00000");
		
		if(location == null){
			t.setText(time.format("%Y:%m:%d") + "\n" + time.format("%H:%M:%S") + "\n\n" + "<no location>");
		} else {
			Time fixtime = new Time(Time.getCurrentTimezone());
			fixtime.set(location.getTime());
			t.setText(time.format("%Y:%m:%d %H:%M:%S") + "\n" + 
						"\nLONG: " + df.format(location.getLongitude())  + 
						"\nLAT: " + df.format(location.getLatitude())  +
						"\n" + fixtime.format("%H:%M:%S") );
		}
	}
	
	private void updateGPSStatus() {
		RadioButton rb = (RadioButton)findViewById(R.id.rbGPSStatus);
		rb.setChecked(!rb.isChecked());
		
		TextView tvGPSStatus = (TextView) findViewById(R.id.txtGPSStatus);
		if(gps.getStatus() == null || gps.getStatus().getTimeToFirstFix() <= 0) {
			long millis = System.currentTimeMillis() - starttime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds     = seconds % 60;
			tvGPSStatus.setText(String.format("No status: %d:%02d", minutes, seconds));
		} else {
			Time fixtime = new Time(Time.getCurrentTimezone());
			fixtime.set(gps.getStatus().getTimeToFirstFix());
			tvGPSStatus.setText(String.format("FixTime: %s.%d, Satelites: %d", fixtime.format("%M:%S"), gps.getStatus().getTimeToFirstFix() - fixtime.toMillis(false), gps.getStatus().getMaxSatellites()));
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gpspos);
				
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.refresh_button).setOnTouchListener(mDelayHideTouchListener);
		
		/**
		 * OWN CODE
		 */
		
		starttime = System.currentTimeMillis();
		
		// create GPS tracker
		gps = new GPSTracker(this);		
		
		// connect event for manual refresh
		findViewById(R.id.refresh_button).setOnClickListener(
				new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						// Manual refresh!
						if(gps.canGetLocation()){ // gps enabled ?
							ShowGPSLocation(gps.getLocation());	
						} else {
							gps.showSettingsAlert();
						}
					}
				});
		
		// get initial location (if any)
		ShowGPSLocation(gps.getLocation());
		
		// timer to update GPS status text
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
	        	@Override
	        	public void run() {
					GPSPosActivity.this.runOnUiThread(new Runnable() {
			        	@Override
			        	public void run() {
			        		GPSPosActivity.this.updateGPSStatus();
			        	}
			        });	        		
	        	}
			}, 0, 1000); // every second (1000 msec)	
		
		// Check location services
		if(!gps.canGetLocation()){ // gps enabled ?
			gps.showSettingsAlert();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
