package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    private long sharer;
    private String title;
    private String artist;
    private String styles;
    private String link;
    private String description;
    private Context context;
    private PlayList playlist;
    private ExoPlayer exoPlayer;
    private String coverUrl;
    private boolean liked;

    public Song(Context context, int id, int likes, long sharer, String title, String artist, String styles, String link, boolean liked, PlayList playlist) {
        this.context = context;
        this.id = id;
        this.likes = likes;
        this.title = title;
        this.artist = artist;
        this.styles = styles;
        this.link = link;
        this.playlist = playlist;
        this.sharer = sharer;
        this.liked = liked;
        coverUrl = "";
        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.addListener(playlist);
    }

    public Song() {
    }

    public void play(final TextView sharerInfo) {
        if(link.contains("soundcloud")) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            CustomRequest request = new CustomRequest(Request.Method.GET, "https://api.soundcloud.com/resolve.json?url=" + link + "&client_id=c818b360defc350d7e45840b71e117e3" , null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final String BASE_URL = response.getString("stream_url");
                        coverUrl = response.getString("artwork_url");
                        if(coverUrl != "")
                            coverUrl = coverUrl.replace("large.jpg", "t300x300.jpg");
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

                }
            });
            StringRequest sharerRequest = new StringRequest(Request.Method.GET, "http://lefourretoutsonore.tk/service/getSharer.php?sharer=" + String.valueOf(sharer), new Response.Listener<String>() {
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

    public void stop() {
        exoPlayer.stop();
    }

    public long getDuration() {
        if(exoPlayer.getDuration() != ExoPlayer.UNKNOWN_TIME)
            return exoPlayer.getDuration()/1000;
        else
            return 123;
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

    public boolean getLiked() {
        return liked;
    }

    public String getDescription() {
        return description;
    }

    public long getSharer() {
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
