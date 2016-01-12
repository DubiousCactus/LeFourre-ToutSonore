package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;

/**
 * Created by transpalette on 1/2/16.
 */
public class Song implements Serializable {
    private int id;
    private int likes;
    private int sharer;
    private String title;
    private String artist;
    private String styles;
    private String link;
    private String description;
    private Context context;
    private PlayList playlist;
    private ExoPlayer exoPlayer;
    private String coverUrl;

    public Song(Context context, int id, int likes, String title, String artist, String styles, String link, PlayList playlist) {
        this.context = context;
        this.id = id;
        this.likes = likes;
        this.title = title;
        this.artist = artist;
        this.styles = styles;
        this.link = link;
        this.playlist = playlist;
        coverUrl = "";
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.addListener(playlist);
    }

    public Song() {
    }

    public void play() {
        Log.i("Song", "playing");
        if(link.contains("soundcloud")) {
            Log.i("Soundcloud ?", "yep");
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            CustomRequest request = new CustomRequest(Request.Method.GET, "https://api.soundcloud.com/resolve.json?url=" + link + "&client_id=c818b360defc350d7e45840b71e117e3" , null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final String BASE_URL = response.getString("stream_url");
                        final String CLIENTID_PARAM = "client_id";
                        coverUrl = response.getString("artwork_url");
                        String key = "c818b360defc350d7e45840b71e117e3";
                        Log.i("SongPLay", "uri : " + BASE_URL);
                        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                                .appendQueryParameter(CLIENTID_PARAM, key)
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

                }
            });
            requestQueue.add(request);
        } else {
            exoPlayer.stop();
            Log.i("Soundcloud ?", "nope");
        }
    }

    public void pause() {
        exoPlayer.stop();
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public long getProgress() {
        return exoPlayer.getCurrentPosition();
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getStyles() {
        return styles;
    }

    public String getLink() {
        return link;
    }

    public int getLikes() {
        return likes;
    }

    public String getDescription() {
        return description;
    }

    public int getSharer() {
        return sharer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setSharer(int sharer) {
        this.sharer = sharer;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
