package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.mobiwise.library.InteractivePlayerView;
import tk.lefourretoutsonore.lefourre_toutsonore.CustomRequest;
import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.Song;
import tk.lefourretoutsonore.lefourre_toutsonore.User;

/**
 * Created by transpalette on 1/3/16.
 */
public class PlayList implements Serializable, ExoPlayer.Listener {

    public enum PlayListChoice {
        ALL("ALL", "Tous les sons", "", -1),
        LIKES("LIKES", "Mes likes", "", -1),
        REGGAE("1", "Reggae", "Roots - Dub - Raga - Jungle", R.drawable.ban_reggae),
        ELECTRO("2", "Electro", "House - Techno - Minimale - Tribe", R.drawable.ban_electro),
        TRANCE("3", "Trance", "Progressive - Goa - Forest - Tribal", R.drawable.ban_trance),
        POP("5", "Pop", "Variété française - Soul - Country - Pop Rock", R.drawable.ban_pop),
        CORE("6", "Core", "Frenchcore - Hardcore - Hardtek - Acidcore", R.drawable.ban_core),
        HIPHOP("7", "Hip-Hop", "Trap - Rap - R'n'B - Trip Hop", R.drawable.ban_hiphop),
        ROCK("4", "Rock n' Roll", "Folk - Hard Rock - Blues - Jazz", R.drawable.ban_rock);

        private String id;
        private String longName;
        private String desc;
        private int banId;

        PlayListChoice(String id, String longName, String desc, int banId) {
            this.id = id;
            this.longName = longName;
            this.desc = desc;
            this.banId = banId;
        }

        public String getId() { return id; }
        public String getLongName() { return longName; }
        public String getDesc() { return desc; }
        public int getBanId() { return banId; }
    }


    private int songIndex;
    private ArrayList<Song> songList;
    private int count;
    private String name;
    private Context context;
    private PlayListChoice choice;
    private User currentUser;
    private InteractivePlayerView ipv;

    public PlayList(PlayListChoice choice, Context context, InteractivePlayerView ipv) {
        this.name = choice.toString();
        this.choice = choice;
        this.context = context;
        songList = new ArrayList<>();
        count = 0;
        this.ipv = ipv;
        songIndex = 0;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == ExoPlayer.STATE_ENDED) {
            songIndex++;
            songList.get(songIndex).play();
        } else if(playbackState == ExoPlayer.STATE_IDLE)
            ipv.stop();
        else
            ipv.setProgress((int) songList.get(songIndex).getProgress());
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        ipv.start();
        ipv.setMax((int) songList.get(songIndex).getDuration());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    public void addSong(Song song) {
        songList.add(song);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void fetchSounds() {
        Map<String, String> params = new HashMap<>();
        params.put("genre", choice.getId());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = "";
        if(choice == PlayListChoice.LIKES)
            url = "http://lefourretoutsonore.tk/service/getLikesPlaylist.php?sharer=" + currentUser.getId();
        else
            url = "http://lefourretoutsonore.tk/service/getPlaylistAsJson.php?choice=" + choice.getId();
        CustomRequest jsObjRequest = new CustomRequest(url, params, (Response.Listener<JSONObject>) context, (Response.ErrorListener) context);
        requestQueue.add(jsObjRequest);
    }

    public void saveOnDisk() {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("playList"+name+".lst", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(count);
            oos.writeObject(songList);
            oos.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean retrieveFromDisk() {
        FileInputStream fis = null;
        boolean success = true;
        try {
            fis = context.openFileInput("playList" + name + ".lst");
            ObjectInputStream ois = new ObjectInputStream(fis);
            songList.clear();
            count = ois.readInt();
            songList = (ArrayList<Song>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public void play(int songIndex) {
        songList.get(songIndex).play();
        Log.i("PlayList", "play");
    }

    public void pause() {
        songList.get(songIndex).pause();
    }
}
