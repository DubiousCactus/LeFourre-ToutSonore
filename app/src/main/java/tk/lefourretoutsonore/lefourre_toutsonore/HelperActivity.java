package tk.lefourretoutsonore.lefourre_toutsonore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;

/**
 * Created by M4gicT0 on 15/01/2016.
 */
public class HelperActivity extends Activity {

    private HelperActivity ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i("debug", "helperactivity called");
        String action = (String) getIntent().getExtras().get("DO");
        assert action != null;
        if (action.equals("play")) {
            PlayList.resume();
            Log.i("NotificationReturnSlot", "play");
            //Your code
        } else if (action.equals("stop")) {
            //Your code
            PlayList.pause();
            Log.i("NotificationReturnSlot", "stopNotification");
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}