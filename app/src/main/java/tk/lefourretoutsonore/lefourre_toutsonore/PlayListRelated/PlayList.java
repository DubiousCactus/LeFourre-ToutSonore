package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;
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
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.DefaultDashTrackSelector;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.dash.mpd.UtcTimingElement;
import com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.Util;

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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.mobiwise.library.InteractivePlayerView;
import tk.lefourretoutsonore.lefourre_toutsonore.CustomRequest;
import tk.lefourretoutsonore.lefourre_toutsonore.Main;
import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.Song;
import tk.lefourretoutsonore.lefourre_toutsonore.User;
import android.support.v4.app.FragmentManager;

import tk.lefourretoutsonore.lefourre_toutsonore.YoutubeVideo;
import tk.lefourretoutsonore.lefourre_toutsonore.callback.VideoControlCallback;
import tk.lefourretoutsonore.lefourre_toutsonore.player.DemoPlayer;

/**
 * Created by transpalette on 1/3/16.
 */
public class PlayList implements ExoPlayer.Listener, Serializable, ManifestFetcher.ManifestCallback<MediaPresentationDescription>, UtcTimingElementResolver.UtcTimingCallback {

    private int songIndex;
    private ArrayList<Song> songList;
    private int count;
    private String name;
    private Context context;
    private PlayListChoice choice;
    private User currentUser;
    private static InteractivePlayerView ipv;
    private TextView songInfo;
    private TextView sharerInfo;
    private TextView likesInfo;
    private TextView stylesInfo;
    private TextView descriptionInfo;
    private TextView songTitleInfo;
    private TextView songArtistInfo;
    private static ExoPlayer exoPlayer;

    //YouTube
    private MediaPresentationDescription manifest;
    private DefaultUriDataSource manifestDataSource;
    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private long elapsedRealtimeOffset;
    private String userAgent;
    private String videoUrl;
    private Uri contentUri;

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
        if(playbackState == ExoPlayer.STATE_READY) {
            ipv.start();
            ipv.setMax((int) getSongDuration());
            if(!songList.get(songIndex).getCoverUrl().isEmpty())
                ipv.setCoverURL(songList.get(songIndex).getCoverUrl());
            else
                ipv.setCoverDrawable(R.drawable.no_cover);
        }
        else if(playbackState == ExoPlayer.STATE_ENDED) {
            songIndex++;
            updateSongInfoDisplay();
            play(songIndex);
        } else if(playbackState == ExoPlayer.STATE_IDLE) {
            ipv.stop();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
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

    public void play(final int songIndex) {
        if(songList.isEmpty())
            return;
        pause();
        ipv.setAction2Selected(songList.get(songIndex).getLiked());
        ipv.setCoverDrawable(R.drawable.no_cover);
        ipv.setProgress(0);
        Song currentSong = songList.get(songIndex);
        this.songIndex = songIndex;
        updateSongInfoDisplay();

        if(currentSong.getLink().contains("soundcloud")) {
            releasePlayer();
            playSoundCloud(currentSong);
        } else {
            releasePlayer();
            playYoutube(currentSong);
        }
    }

    private void playSoundCloud(final Song currentSong) {
        if(!currentSong.getStreamUrl().equals("none") && !currentSong.getCoverUrl().equals("none") && !currentSong.getSharerName().equals("none")) {
            String key = "c818b360defc350d7e45840b71e117e3";

            ipv.setMax((int) getSongDuration());
            if(!currentSong.getCoverUrl().isEmpty())
                ipv.setCoverURL(currentSong.getCoverUrl());
            else
                ipv.setCoverDrawable(R.drawable.no_cover);

            Uri builtUri = Uri.parse(currentSong.getStreamUrl()).buildUpon()
                    .appendQueryParameter("client_id", key)
                    .build();
            // Build the sample source
            FrameworkSampleSource sampleSource = new FrameworkSampleSource(context, builtUri, null);
            // Build the track renderers
            TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true);
            // Build the ExoPlayer and start playback
            exoPlayer.prepare(audioRenderer);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.seekTo(0);
            sharerInfo.setText("Ajouté par " + currentSong.getSharerName());
        } else {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            CustomRequest request = new CustomRequest(Request.Method.GET, "https://api.soundcloud.com/resolve.json?url=" + currentSong.getLink() + "&client_id=c818b360defc350d7e45840b71e117e3", null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        final String BASE_URL = response.getString("stream_url");
                        String coverUrl = response.getString("artwork_url").replace("large.jpg", "t300x300.jpg");
                        songList.get(songIndex).setCoverUrl(coverUrl);
                        String key = "c818b360defc350d7e45840b71e117e3";

                        ipv.setMax((int) getSongDuration());
                        if (!songList.get(songIndex).getCoverUrl().isEmpty())
                            ipv.setCoverURL(songList.get(songIndex).getCoverUrl());
                        else
                            ipv.setCoverDrawable(R.drawable.no_cover);

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
                        exoPlayer.seekTo(0);
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
                    songList.get(songIndex).setSharerName(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            requestQueue.add(request);
            requestQueue.add(sharerRequest);
        }
    }

    private void playYoutube(final Song currentSong) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, "http://www.youtube.com/get_video_info?&video_id=" + currentSong.getLink().substring(currentSong.getLink().indexOf("=") + 1), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String result = URLDecoder.decode(response, "UTF-8");
                    if(!result.startsWith("error")) {
                        String firstPart = URLDecoder.decode(result.substring(result.indexOf("dashmpd=") + 8), "UTF-8");
                        videoUrl = URLDecoder.decode(firstPart.substring(0, firstPart.indexOf("&")), "UTF-8");
                        contentUri = Uri.parse(videoUrl);
                        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();
                        userAgent = Util.getUserAgent(context, "LeFourre-ToutSonore");
                        manifestDataSource = new DefaultUriDataSource(context, userAgent);
                        manifestFetcher = new ManifestFetcher<>(contentUri.toString(), manifestDataSource, parser);
                        preparePlayer();
                    } else {
                        Toast.makeText(context, "Contenu protégé - Lecture impossible", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentSong.getLink())));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
    }

    private void buildRenderer() {
        if (manifest.dynamic && manifest.utcTiming != null) {
            UtcTimingElementResolver.resolveTimingElement(manifestDataSource, manifest.utcTiming,
                    manifestFetcher.getManifestLoadCompleteTimestamp(), this);
        }
        Handler mainHandler = new Handler();
        LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(64 * 1024));

        // Build the audio renderer.
        DataSource audioDataSource = new DefaultUriDataSource(context, userAgent);
        ChunkSource audioChunkSource = new DashChunkSource(manifestFetcher, DefaultDashTrackSelector.newAudioInstance(), audioDataSource, null, 30000, elapsedRealtimeOffset, mainHandler, null);
        ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
                54 * (64 * 1024), mainHandler, null,
                DemoPlayer.TYPE_AUDIO);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource,
                null, true, mainHandler, null, AudioCapabilities.getCapabilities(context));

        exoPlayer.prepare(audioRenderer);
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void preparePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Factory.newInstance(1, 1000, 5000);
            manifestFetcher.singleLoad(exoPlayer.getPlaybackLooper(), this);
            exoPlayer.addListener(this);
            exoPlayer.seekTo(0);
            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void destroy() {
        exoPlayer.release();
    }

    public static void pause() {
        exoPlayer.setPlayWhenReady(false);
        ipv.stop();
        Log.i("isPlaying", String.valueOf(ipv.isPlaying()));
    }

    public static void resume() {
        exoPlayer.setPlayWhenReady(true);
        ipv.start();
        ipv.setProgress((int) exoPlayer.getCurrentPosition() / 1000);
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

    @Override
    public void onSingleManifest(MediaPresentationDescription manifest) {
        this.manifest = manifest;
        if (manifest.dynamic && manifest.utcTiming != null) {
            UtcTimingElementResolver.resolveTimingElement(manifestDataSource, manifest.utcTiming,
                    manifestFetcher.getManifestLoadCompleteTimestamp(), this);
        } else {
            buildRenderer();
        }
    }

    @Override
    public void onSingleManifestError(IOException e) {
    }

    @Override
    public void onTimestampResolved(UtcTimingElement utcTiming, long elapsedRealtimeOffset) {
        this.elapsedRealtimeOffset = elapsedRealtimeOffset;
        buildRenderer();
    }

    @Override
    public void onTimestampError(UtcTimingElement utcTiming, IOException e) {
        Log.e("", "Failed to resolve UtcTiming element [" + utcTiming + "]", e);
        // Be optimistic and continue in the hope that the device clock is correct.
        buildRenderer();
    }
}
