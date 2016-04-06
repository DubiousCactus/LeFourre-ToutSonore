package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import java.util.EventListener;

/**
 * Created by M4gicT0 on 06/04/2016.
 */
public interface StateListener extends EventListener {

    void onSongPlay();
    void onSongPause();
    void onSongIdle();
    void onSongError();
    void onSongStop();
    void onSongBuffering();

}
