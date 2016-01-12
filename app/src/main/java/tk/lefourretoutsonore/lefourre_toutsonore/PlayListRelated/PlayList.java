package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
    private TextView songInfo;
    private TextView sharerInfo;

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
            songInfo.setText(songList.get(songIndex).getArtist() + " - " + songList.get(songIndex).getTitle());
            songList.get(songIndex).play(sharerInfo);
        } else if(playbackState == ExoPlayer.STATE_IDLE)
            ipv.stop();
        else
            ipv.setProgress((int) songList.get(songIndex).getProgress());
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        ipv.start();
        ipv.setMax((int) songList.get(songIndex).getDuration());
        if(!songList.get(songIndex).getCoverUrl().isEmpty())
            ipv.setCoverURL(songList.get(songIndex).getCoverUrl());
        else
            ipv.setCoverDrawable(R.drawable.no_cover);
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

    public int getSongIndex() {
        return songIndex;
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

    public void likeSong() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest likeRequest = new StringRequest(Request.Method.POST, "http://lefourretoutsonore.tk/service/addLike.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("true"))
                    Toast.makeText(context, "Son liké !", Toast.LENGTH_SHORT).show();
                else if(response.equals("false"))
                    Toast.makeText(context, "Son déjà liké !", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("son", String.valueOf(songList.get(songIndex).getId()));
                params.put("partageur", String.valueOf(currentUser.getId()));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }};
        requestQueue.add(likeRequest);
    }

    public boolean getLiked() {
        final boolean[] isLiked = {false};
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest likeRequest = new StringRequest(Request.Method.POST, "http://lefourretoutsonore.tk/service/addLike.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("false"))
                    isLiked[0] = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("son", String.valueOf(songList.get(songIndex).getId()));
                params.put("partageur", String.valueOf(currentUser.getId()));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }};
        requestQueue.add(likeRequest);

        return isLiked[0];
    }

    public void play(int songIndex, TextView songInfo, TextView sharerInfo) {
        this.songInfo = songInfo;
        this.sharerInfo = sharerInfo;
        if(this.songIndex < songIndex && songIndex > 0) //Next song
            songList.get(songIndex-1).stop();
        else if(this.songIndex > songIndex) //Previous song
            songList.get(songIndex+1).stop();

        songInfo.setText(songList.get(songIndex).getArtist() + " - " + songList.get(songIndex).getTitle());
        songList.get(songIndex).play(sharerInfo);
        ipv.setAction2Selected(getLiked());
        this.songIndex = songIndex;
    }

    public void pause() {
        songList.get(songIndex).pause();
    }
}
