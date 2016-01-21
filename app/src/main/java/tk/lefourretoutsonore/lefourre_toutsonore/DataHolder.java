package tk.lefourretoutsonore.lefourre_toutsonore;

import com.google.android.exoplayer.ExoPlayer;

import co.mobiwise.library.InteractivePlayerView;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;

/**
 * Created by M4gicT0 on 20/01/2016.
 */
public class DataHolder {
    private ExoPlayer player;
    private InteractivePlayerView ipv;
    private PlayList playlist;
    private User currentUser;

    private static final DataHolder holder  = new DataHolder();

    public static DataHolder getInstance() {
        return holder;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public InteractivePlayerView getIpv() {
        return ipv;
    }

    public void setIpv(InteractivePlayerView ipv) {
        this.ipv = ipv;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User u)  {
        currentUser = u;
    }

    public PlayList getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayList pl) {
        playlist = pl;
    }
}
