package tk.lefourretoutsonore.lefourre_toutsonore;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;

/**
 * Created by M4gicT0 on 15/01/2016.
 */
public class MyNotification {

    private Context parent;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;

    public MyNotification(Context parent) {
        // TODO Auto-generated constructor stub
        this.parent = parent;
        nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle("Le Fourre-Tout Sonore")
                .setSmallIcon(R.drawable.logo)
                .setOngoing(true);

        remoteView = new RemoteViews(parent.getPackageName(), R.layout.player_notification);

        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, nBuilder.build());
    }


    public void setListeners(RemoteViews view){
        Intent play = new Intent(parent, HelperActivity.class);
        play.putExtra("DO", "play");
        PendingIntent btn1 = PendingIntent.getActivity(parent, 0, play, 0);
        view.setOnClickPendingIntent(R.id.notifPlay, btn1);
        //listener 2
        Intent stop = new Intent(parent, HelperActivity.class);
        stop.putExtra("DO", "stop");
        PendingIntent btn2 = PendingIntent.getActivity(parent, 1, stop, 0);
        view.setOnClickPendingIntent(R.id.notifPause, btn2);
    }

    public void notificationCancel() {
        nManager.cancel(2);
    }
}