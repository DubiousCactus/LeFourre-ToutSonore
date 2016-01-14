package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
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
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;

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
public class PlayList implements ExoPlayer.Listener, Serializable {

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
    private TextView likesInfo;
    private TextView stylesInfo;
    private TextView descriptionInfo;
    private TextView songTitleInfo;
    private TextView songArtistInfo;
    private ExoPlayer exoPlayer;

    public PlayList(PlayListChoice choice, Context context, InteractivePlayerView ipv, User currentUser) {
        this.name = choice.toString();
        this.choice = choice;
        this.context = context;
        this.currentUser = currentUser;
        songList = new ArrayList<>();
        count = 0;
        this.ipv = ipv;
        songIndex = 0;
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.addListener(this);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == ExoPlayer.STATE_ENDED) {
            songIndex++;
            updateSongInfoDisplay();
            play(songIndex);
        } else if(playbackState == ExoPlayer.STATE_IDLE)
            ipv.stop();
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        ipv.start();
        ipv.setMax((int) getSongDuration());
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

    public void setSongInfoDisplay(TextView songInfo, TextView sharerInfo, TextView likesInfo, TextView stylesInfo, TextView descriptionInfo, TextView songArtistInfo, TextView songTitleInfo) {
        this.songInfo = songInfo;
        this.sharerInfo = sharerInfo;
        this.likesInfo = likesInfo;
        this.stylesInfo = stylesInfo;
        this.descriptionInfo = descriptionInfo;
        this.songTitleInfo = songTitleInfo;
        this.songArtistInfo = songArtistInfo;
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
            url = "http://lefourretoutsonore.tk/service/getPlaylistAsJson.php?choice=" + choice.getId() + "&user=" + currentUser.getId();
        CustomRequest jsObjRequest = new CustomRequest(url, params, (Response.Listener<JSONObject>) context, (Response.ErrorListener) context);
        requestQueue.add(jsObjRequest);
    }

    public void saveOnDisk() {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("playList" + name, Context.MODE_PRIVATE);
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
            fis = context.openFileInput("playList" + name);
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
                    Toast.makeText(context, "Son dé-liké !", Toast.LENGTH_SHORT).show();
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
                params.put("liked", String.valueOf(songList.get(songIndex).getLiked()));

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

    public void play(int songIndex) {
        pause();
        ipv.setAction2Selected(songList.get(songIndex).getLiked());
        ipv.setCoverDrawable(R.drawable.no_cover);
        ipv.setProgress(0);
        updateSongInfoDisplay();
        final Song currentSong = songList.get(songIndex);
        this.songIndex = songIndex;
        updateSongInfoDisplay();

        if(currentSong.getLink().contains("soundcloud")) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            CustomRequest request = new CustomRequest(Request.Method.GET, "https://api.soundcloud.com/resolve.json?url=" + currentSong.getLink() + "&client_id=c818b360defc350d7e45840b71e117e3" , null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final String BASE_URL = response.getString("stream_url");
                        String coverUrl = response.getString("artwork_url").replace("large.jpg", "t300x300.jpg");
                        currentSong.setCoverUrl(coverUrl);
                        String key = "c818b360defc350d7e45840b71e117e3";
                        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                                .appendQueryParameter("client_id", key)
                                .build();
                        // Build the sample source
                        FrameworkSampleSource sampleSource = new FrameworkSampleSource(context, builtUri, null);
                        // Build the track renderers
                        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true);
                        // Build the ExoPlayer and start playback
                        exoPlayer.prepare(audioRenderer);
                        exoPlayer.setPlayWhenReady(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Lecture impossible", Toast.LENGTH_SHORT).show();
                }
            });
            StringRequest sharerRequest = new StringRequest(Request.Method.GET, "http://lefourretoutsonore.tk/service/getSharer.php?sharer=" + String.valueOf(currentSong.getSharer()), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    sharerInfo.setText("Ajouté par " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(request);
            requestQueue.add(sharerRequest);
        } else {
            exoPlayer.release();
            Toast.makeText(context, "Son YouTube, lecture impossible", Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        exoPlayer.stop();
    }

    public long getSongDuration() {
        if(exoPlayer.getDuration() != ExoPlayer.UNKNOWN_TIME)
            return exoPlayer.getDuration()/1000;
        else
            return 123;
    }

    private void updateSongInfoDisplay() {
        Song currentSong = songList.get(songIndex);
        songInfo.setText(currentSong.getArtist() + " - " + songList.get(songIndex).getTitle());
        likesInfo.setText(currentSong.getLikes() + " ♥");
        stylesInfo.setText(currentSong.getStyles());
        descriptionInfo.setText(currentSong.getDescription());
        songArtistInfo.setText(currentSong.getArtist());
        songTitleInfo.setText(currentSong.getTitle());
    }
}
