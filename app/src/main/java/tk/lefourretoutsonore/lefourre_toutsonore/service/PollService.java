package tk.lefourretoutsonore.lefourre_toutsonore.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;
import okio.Timeout;
import tk.lefourretoutsonore.lefourre_toutsonore.DataHolder;
import tk.lefourretoutsonore.lefourre_toutsonore.Launcher;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListChoice;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;
import tk.lefourretoutsonore.lefourre_toutsonore.R;

/**
 * Created by M4gicT0 on 11/01/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 3; // 3 minutes

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;

        if (!isNetworkAvailable)
            return;


        int lastAddedSong = fetchLastSound();
        if(lastAddedSong > getLastSaved()) {
            PugNotification.with(this)
                    .load()
                    .title("Nouveauté")
                    .message("Un nouveau son a été ajouté !")
                    .bigTextStyle("Du nouveau !")
                    .smallIcon(R.drawable.logo_icon)
                    .largeIcon(R.drawable.logo)
                    .flags(Notification.DEFAULT_ALL)
                    .click(PlayListView.class, null)
                    .simple()
                    .build();

            saveLastSong(lastAddedSong);
        }
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public int fetchLastSound() {
        String lastSong = "0";
        RequestFuture<String> future = RequestFuture.newFuture();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://lefourretoutsonore.tk/service/getLastSong.php";
        StringRequest jsObjRequest = new StringRequest(Request.Method.GET , url, future, future);
        requestQueue.add(jsObjRequest);
        try {
            lastSong = future.get(); // this will block (forever)
            if(lastSong.equals(""))
                lastSong = "0";
            Log.i(TAG, "lastSongID = " + lastSong);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(lastSong);
    }

    private void createSharedPref() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("lastSongSaved", 0);
        editor.commit();
    }

    private void saveLastSong(int id) {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("lastSongSaved", id);
        editor.commit();
    }

    public int getLastSaved() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int lastSongSaved = sharedPref.getInt("lastSongSaved", 0);
        if(lastSongSaved == 0)
            createSharedPref();

        return lastSongSaved;
    }
}
