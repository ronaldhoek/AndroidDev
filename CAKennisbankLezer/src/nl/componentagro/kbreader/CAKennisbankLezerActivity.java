package nl.componentagro.kbreader;


import java.util.List;

import nl.componentagro.feedparser.FeedParser;
import nl.componentagro.feedparser.Message;
import nl.componentagro.feedparser.XmlPullFeedParser;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class CAKennisbankLezerActivity extends ListActivity {

    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    /*  Actual list of messages
	 *  This list will be kept in memory, when the Activity is destroyed/closed
	 *  - when the rotation of a screen has changed, the Activity will be destroyed */
	private static List<Message> _messages;

	private FeedLoadTask _loadTask;	
	private ProgressDialog progDailog;
	    
	private void ShowMessage(String message) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(message);
    	builder.setPositiveButton("OK", null);
    	builder.setTitle("Informatie");
    	builder.setIcon(android.R.drawable.ic_dialog_info);
    	
    	AlertDialog alert = builder.create();
    	alert.setOwnerActivity(this);
    	alert.show();
	}
	
	private void ShowProgDialog() {
		if (progDailog == null) {	
			progDailog = ProgressDialog.show(this, "Even geduld aub", "De berichten worden ingeladen...", true, false);
		}
	}
	
	private void HideProgDialog() {
		if (progDailog != null) {
			progDailog.dismiss();
			progDailog = null;
		}
	}
	
//	private void lockOrientation() {
//	
//		Boolean IsLandscape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//		
//        switch (this.getWindowManager().getDefaultDisplay().getRotation())
//        {
//	    	case Surface.ROTATION_0: 
//	    		if (IsLandscape) {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//	    		} else {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    			
//	    		}
//	    		break;
//	    	case Surface.ROTATION_90:
//	    		if (IsLandscape) {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//	    		} else {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    			
//	    		}	    		
//	    		break;
//	    	case Surface.ROTATION_180: 
//	    		if (IsLandscape) {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//	    		} else {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);    			
//	    		}	    		
//	    		break;
//	    	case Surface.ROTATION_270:
//	    		if (IsLandscape) {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//	    		} else {
//	    			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);    			
//	    		}	    		
//	    		break;
//        }	    		
//	}
//	
//	private void unlockOrientation() {
//		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//	}
	
	private class FeedLoadTask extends AsyncTask<String, Void, List<Message>> {

		private String _username;
		private String _password;
		protected CAKennisbankLezerActivity _activity;
		
		public FeedLoadTask(String Username, String Password) {
			super();
			_username = Username;
			_password = Password;			
		}
		
		/** Task to run in sync with UI */
		protected void onPreExecute() {
			if (_activity != null) {
				_activity.ShowProgDialog();
			}
		}		
		
		/** Task to run ASync */
		protected List<Message> doInBackground(String... FeedURLs) {		
			// Create empty list...
			List<Message> msgs = null;
			
			if (FeedURLs.length == 1) {
				try {
		    		// Initialize parser
		    		Log.i("KBReader", "FeedURL="+FeedURLs[0]);
			    	FeedParser parser = new XmlPullFeedParser(FeedURLs[0]);
			    	parser.setCustomDateFormat("dd-MM-yyyy");
			    	
			    	// Execute parser
			    	long start = System.currentTimeMillis();
			    	if (_username.length() > 0 && _password.length() > 0) {
				    	msgs = parser.parse("_LOGINNAME=" + _username + "&_LOGINPASS=" + _password);
			    	} else {
			    		msgs = parser.parse();
			    	}
			    	long duration = System.currentTimeMillis() - start;
			    	Log.i("KBReader", "Parser duration=" + duration);

		    	} catch (Throwable t){
		    		Log.e("KBReader",t.getMessage(),t);
		    	}
			}
			return msgs;	    	
	    }
        
		/** Task to run in sync with UI */
        protected void onPostExecute(List<Message> result) {   	
        	if (_activity != null) {
        		_activity._loadTask = null; // Release ref!
        		_activity.SetMessageList(result);
        		_activity.HideProgDialog();
        	}
        }        
	}
	
    private class MessageAdapter extends ArrayAdapter<Message> {

        private List<Message> items;

        /** Constructor */
        public MessageAdapter(Context context, int textViewResourceId, List<Message> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                Message m = items.get(position);
                if (m != null) {
                    TextView tt = (TextView) v.findViewById(R.id.toptext);
                    TextView dt = (TextView) v.findViewById(R.id.datetext);
                    TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                    ImageView iv = (ImageView) v.findViewById(R.id.icon);
                    if (tt != null) {
                          tt.setText(m.getTitle());                           
                    }
                    if (dt != null) {
                    	dt.setText(m.getDateStr());
                    }
                    if(bt != null){
                    	bt.setText(m.getDescription());
                    }
                    if (iv != null) {
                    	if (m.getImage().title != null) {
                        	if(m.getImage().title.equalsIgnoreCase("ca-1reg")) {
                        		iv.setImageResource(R.drawable.ca_1reg);
                        	} else if(m.getImage().title.equalsIgnoreCase("ca-1fis")) {
                        		iv.setImageResource(R.drawable.ca_1fis);
                        	}
                    	}                        	
                    }
                }
                return v;
        }
    }

	private void SetMessageList(List<Message> messages) {
		
		if (_messages != messages) {
			// Remove current adapter
			this.setListAdapter(null);
			// Copy reference...
			_messages = messages;
			// Refresh adapter
			if (_messages != null) {
				UpdateAdapter();
			}
		}
	}
	
	private void UpdateAdapter() {
		// Create new adapter based on the message list			
		MessageAdapter adapter = new MessageAdapter(this, R.layout.row, _messages);
        this.setListAdapter(adapter);	        
        
//    	// Create title list
//    	List<String> titles = new ArrayList<String>(messages.size());
//    	for (Message msg : messages){
//    		titles.add(msg.getTitle());
//    	}			
//		
//		// Make adapter of list
//    	ArrayAdapter<String> adapter = 
//    		new ArrayAdapter<String>(this, R.layout.row, titles);
//    	
//    	// Set adapter for the list...
//    	setListAdapter(adapter);
	}
	
	private void LoadMessages() {
    	// Is there already a task running?
    	if (_loadTask == null) {
    		// Kick of feed loading thread
    		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        	_loadTask = new FeedLoadTask(
        			prefs.getString(PREF_USERNAME,""), prefs.getString(PREF_PASSWORD,"") );
        	// Link current Activity
        	_loadTask._activity = this;
        	// Execute Task
        	_loadTask.execute("http://www.componentagro.nl/rss.asp?A1PID=306179TBCARH");
        	//_loadTask.execute("http://feeds.feedburner.com/DilbertDailyStrip"); 
    	}	
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Get the last task if it was still going
        if (getLastNonConfigurationInstance() != null)
        {
        	_loadTask = (FeedLoadTask)getLastNonConfigurationInstance();
        	_loadTask._activity = this;
            ShowProgDialog();        		
        } else {
        	if (_messages == null) {        
	        	LoadMessages();
	        } else {
	        	UpdateAdapter();
	        }
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() 
    {
    	// Check that there is a task running that needs preserving
		if (_loadTask != null)
		{
			// Remove reference to this activity
			_loadTask._activity = null;
			// Return the instance to be retained
		    return _loadTask;
		}
		return super.onRetainNonConfigurationInstance();
    } 
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Menuklik afhandelen
        switch (item.getItemId()) {
            case R.id.refresh: 
            	//Log.v("ttt", "You pressed 'REFRESH'!");
            	LoadMessages();
            	return true;
            case R.id.settings: 
            	//Log.v("ttt", "You pressed 'SETTINGS'!");
            	AlertDialog.Builder alert = new AlertDialog.Builder(this);

            	alert.setTitle("Aanmeldgegevens");
            	alert.setMessage("Vul uw aanmeldgegevens in");

            	// Set an EditText view to get user input 
            	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            	View layout = inflater.inflate(R.layout.logoninfodialog,
            	                               (ViewGroup) findViewById(R.id.layout_root));

            	final SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);

            	final EditText edtUsername = (EditText) layout.findViewById(R.id.edtUsername);
        		edtUsername.setText(prefs.getString(PREF_USERNAME,""));
        		final EditText edtPassword = (EditText) layout.findViewById(R.id.edtPassword);
        		edtPassword.setText(prefs.getString(PREF_PASSWORD,""));
            	
            	alert.setPositiveButton("Opslaan", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {
            			SharedPreferences.Editor prefedit = prefs.edit();
            			prefedit.putString(PREF_USERNAME, edtUsername.getText().toString());
            			prefedit.putString(PREF_PASSWORD, edtPassword.getText().toString());
            			prefedit.commit();
            			
            			LoadMessages();
            		}
            	});

            	alert.setNegativeButton("Annuleer", new DialogInterface.OnClickListener() {
            	  public void onClick(DialogInterface dialog, int whichButton) {
            	    // Canceled.
            	  }
            	});

            	alert.setView(layout);
            	alert.show();
        		
            	return true;
            case R.id.info: 
            	//Log.v("ttt", "You pressed 'VERSIONINFO'!");
                try
            	{
			        Display display = this.getWindowManager().getDefaultDisplay();
			        DisplayMetrics metrics = new DisplayMetrics();
			        display.getMetrics(metrics);
			        
            		ShowMessage(
            		  "Versie: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n" +
            	      "Scherminformatie:\n" +
            		  "- afmeting: " + display.getWidth() + "x" + display.getHeight() + "\n" +
            	      "- pixelformat: " + display.getPixelFormat() + "\n" +
            		  "- dichtheid(dpi/schaal): " + metrics.density + "/" + metrics.densityDpi + "/" + metrics.scaledDensity + "\n" +
            		  "- DPI (x/y): " + metrics.xdpi + "/" + metrics.ydpi );
            	}
            	catch (NameNotFoundException e)
            	{
            	    ShowMessage(e.getMessage());
            	}
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent viewMessage = new Intent(Intent.ACTION_VIEW, 
				Uri.parse(_messages.get(position).getLink().toExternalForm()));
		this.startActivity(viewMessage);
	}  
}