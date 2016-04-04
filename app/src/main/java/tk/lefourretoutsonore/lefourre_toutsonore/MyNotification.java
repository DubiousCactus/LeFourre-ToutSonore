package tk.lefourretoutsonore.lefourre_toutsonore;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import br.com.goncalves.pugnotification.notification.PugNotification;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;

/**
 * Created by M4gicT0 on 15/01/2016.
 */
public class MyNotification {

    private Context parent;
    private NotificationManager nManager;

    public MyNotification(Context parent) {
        // TODO Auto-generated constructor stub
        this.parent = parent;
        Song current = DataHolder.getInstance().getPlaylist().getPlayingSong();

        Intent previous = new Intent(parent, HelperActivity.class);
        previous.putExtra("DO", "previous");
        PendingIntent btn1 = PendingIntent.getActivity(parent, 0, previous, 0);

        Intent pause = new Intent(parent, HelperActivity.class);
        pause.putExtra("DO", "pause");
        PendingIntent btn2 = PendingIntent.getActivity(parent, 1, pause, 0);

        Intent next = new Intent(parent, HelperActivity.class);
        next.putExtra("DO", "next");
        PendingIntent btn3 = PendingIntent.getActivity(parent, 2, next, 0);

        PugNotification.with(parent)
                .load()
                .title(current.getTitle())
                .message("Son en cours")
                .bigTextStyle(current.getArtist())
                .vibrate(new long[0])
                .smallIcon(R.drawable.logo_icon)
                .largeIcon(R.drawable.logo)
                .flags(Notification.FLAG_ONGOING_EVENT)
                .button(R.drawable.icon_back, "", btn1)
                .button(R.drawable.pause, "", btn2)
                .button(R.drawable.icon_forward, "", btn3)
                .click(PlayListView.class, null)
                .color(R.color.blue)
                .autoCancel(false)
                .simple()
                .build();

        /*NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle("Le Fourre-Tout Sonore")
                .setSmallIcon(R.drawable.logo_icon)
                .setOngoing(true);

        RemoteViews remoteView = new RemoteViews(parent.getPackageName(), R.layout.player_notification);
        //RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, nBuilder.build());*/
    }


    private void setListeners(RemoteViews view){
        Intent previous = new Intent(parent, HelperActivity.class);
        previous.putExtra("DO", "previous");
        PendingIntent btn1 = PendingIntent.getActivity(parent, 0, previous, 0);
        view.setOnClickPendingIntent(R.id.notifPrevious, btn1);


        Intent pause = new Intent(parent, HelperActivity.class);
        pause.putExtra("DO", "pause");
        PendingIntent btn2 = PendingIntent.getActivity(parent, 1, pause, 0);
        view.setOnClickPendingIntent(R.id.notifPause, btn2);

        Intent play = new Intent(parent, HelperActivity.class);
        play.putExtra("DO", "play");
        PendingIntent btn3 = PendingIntent.getActivity(parent, 2, play, 0);
        view.setOnClickPendingIntent(R.id.notifPlay, btn3);
        //listener 2

        Intent next = new Intent(parent, HelperActivity.class);
        next.putExtra("DO", "next");
        PendingIntent btn4 = PendingIntent.getActivity(parent, 3, next, 0);
        view.setOnClickPendingIntent(R.id.notifNext, btn4);
    }

    public void notificationCancel() {
        nManager.cancel(2);
    }
}