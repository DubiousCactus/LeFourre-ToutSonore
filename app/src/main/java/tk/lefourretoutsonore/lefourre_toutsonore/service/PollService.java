package tk.lefourretoutsonore.lefourre_toutsonore.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import okio.Timeout;
import tk.lefourretoutsonore.lefourre_toutsonore.Launcher;
import tk.lefourretoutsonore.lefourre_toutsonore.R;

/**
 * Created by M4gicT0 on 11/01/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 3; // 5 minutes

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

        String lastSongSaved = getLastSaved();
        String resultId = fetchLastSound();
        if (!resultId.equals("") && !resultId.equals(lastSongSaved)) {
            Log.i(TAG, "Got a new result: " + resultId);
            Intent myIntent = new Intent(this, Launcher.class);
            myIntent.putExtra("playlist", "all");
            PendingIntent pi = PendingIntent
                    .getActivity(this, 0, myIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker("Le Fourre-Tout Sonore - Nouveauté")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Le Fourre-Tout Sonore")
                    .setContentText("Nouveau son !")
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
            FileOutputStream fos;
            try {
                fos = openFileOutput("lastSong", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeInt(Integer.valueOf(resultId));
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Got an old result: " + resultId);
        }

        Log.i(TAG, "Received an intent: " + intent);
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

    public String fetchLastSound() {
        String lastSong = "0";
        RequestFuture<String> future = RequestFuture.newFuture();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://lefourretoutsonore.tk/service/getLastSong.php";
        StringRequest jsObjRequest = new StringRequest(Request.Method.GET , url, future, future);
        requestQueue.add(jsObjRequest);
        try {
            lastSong = future.get(); // this will block (forever)
            if(lastSong.equals("") || lastSong == null)
                lastSong = "0";
            Log.i(TAG, "lastSongID = " + lastSong);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return lastSong;
    }

    public String getLastSaved() {
        FileInputStream fis = null;
        String lastSongSaved = "0";
        try {
            fis = openFileInput("lastSong");
            ObjectInputStream ois = new ObjectInputStream(fis);
            lastSongSaved = String.valueOf(ois.readInt());
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastSongSaved;
    }
}
