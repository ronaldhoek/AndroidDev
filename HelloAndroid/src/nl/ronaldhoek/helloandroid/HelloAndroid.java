package nl.ronaldhoek.helloandroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloAndroid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hallo, Jacqueline Hoek");
        setContentView(tv);        
    
        // setContentView(R.layout.main);    
    }
}