package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import co.mobiwise.library.InteractivePlayerView;
import tk.lefourretoutsonore.lefourre_toutsonore.CustomRequest;
import tk.lefourretoutsonore.lefourre_toutsonore.DataHolder;
import tk.lefourretoutsonore.lefourre_toutsonore.MyNotification;
import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.SongRelated.Song;
import tk.lefourretoutsonore.lefourre_toutsonore.User;

/**
 * Created by transpalette on 1/3/16.
 */
public class PlayList implements ExoPlayer.Listener, Serializable, ManifestFetcher.ManifestCallback<MediaPresentationDescription>, UtcTimingElementResolver.UtcTimingCallback {

    private int songIndex;
    private ArrayList<Song> songList;
    private Song playingSong;
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
    private RequestQueue requestQueue;
    private PlayListState state;
    private StateListener listener;
    //YouTube
    private MediaPresentationDescription manifest;
    private DefaultUriDataSource manifestDataSource;
    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private long elapsedRealtimeOffset;
    private String userAgent;
    private String videoUrl;
    private Uri contentUri;


    public PlayList() {
        currentUser = DataHolder.getInstance().getCurrentUser();
        state = PlayListState.IDLE;
        songList = new ArrayList<>();
        count = 0;
        songIndex = 0;
        if(exoPlayer == null) {
            exoPlayer = DataHolder.getInstance().getPlayer();
            exoPlayer.addListener(this);
        }
    }

    public void setListener(StateListener listener) {
        this.listener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void setChoice(PlayListChoice choice) {
        this.choice = choice;
        this.name = choice.toString();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) { //Maybe use ExoPlayer's state instead ??
        if(playbackState == ExoPlayer.STATE_READY && playWhenReady && state != PlayListState.PLAYING) {
            listener.onSongPlay();
            state = PlayListState.PLAYING;
            ipv.setMax((int) getSongDuration());
            ipv.start();
            if(!songList.get(songIndex).getCoverUrl().isEmpty())
                ipv.setCoverURL(songList.get(songIndex).getCoverUrl());
            else
                ipv.setCoverDrawable(R.drawable.no_cover);
        }
        else if(playbackState == ExoPlayer.STATE_ENDED) {
            listener.onSongStop();
            songIndex++;
            updateSongInfoDisplay();
            play(songIndex);
            state = PlayListState.IDLE;
        } else if(playbackState == ExoPlayer.STATE_IDLE) {
            listener.onSongIdle();
            state = PlayListState.IDLE;
        } else if(playbackState == ExoPlayer.STATE_BUFFERING && state != PlayListState.PLAYING && state != PlayListState.IDLE) {
            listener.onSongBuffering();
            state = PlayListState.BUFFERING;
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        listener.onSongError();
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

    public PlayListChoice getChoice() {
        return choice;
    }

    public PlayListState getState() {
        return state;
    }

    public void fetchSounds() {
        Map<String, String> params = new HashMap<>();
        params.put("genre", choice.getId());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url;
        if(choice == PlayListChoice.LIKES)
            url = "https://lefourretoutsonore.tk/service/getLikesPlaylist.php?sharer=" + currentUser.getId();
        else if(choice == PlayListChoice.MY_SONGS)
            url = "https://lefourretoutsonore.tk/service/getMySongs.php?sharer=" + currentUser.getId();
        else
            url = "https://lefourretoutsonore.tk/service/getPlaylistAsJson.php?choice=" + choice.getId() + "&user=" + currentUser.getId();
        CustomRequest jsObjRequest = new CustomRequest(url, params, (Response.Listener<JSONObject>) context, (Response.ErrorListener) context);
        requestQueue.add(jsObjRequest);
    }

    public void saveOnDisk() {
        try {
            FileOutputStream fos = context.openFileOutput("playList" + name, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(count);
            oos.writeObject(songList);
            oos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean retrieveFromDisk() {
        boolean success = true;
        try {
            FileInputStream fis = context.openFileInput("playList" + name);
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
        StringRequest likeRequest = new StringRequest(Request.Method.POST, "https://lefourretoutsonore.tk/service/addLike.php", new Response.Listener<String>() {
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
                Map<String,String> params = new HashMap<>();
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

        exoPlayer.stop();
        ipv = DataHolder.getInstance().getIpv();
        if(choice == PlayListChoice.LIKES)
            ipv.setAction2Selected(true);
        else
            ipv.setAction2Selected(songList.get(songIndex).getLiked());

        ipv.setCoverDrawable(R.drawable.no_cover);
        ipv.setProgress(0);
        ipv.stop();
        playingSong = songList.get(songIndex);
        this.songIndex = songIndex;
        updateSongInfoDisplay();
        ((PlayListView) context).blink();

        if(playingSong.getLink().contains("soundcloud"))
            playSoundCloud();
        else
            playYoutube();


        final StringRequest sharerRequest = new StringRequest(Request.Method.GET, "https://lefourretoutsonore.tk/service/getSharer.php?sharer=" + String.valueOf(playingSong.getSharer()), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals(DataHolder.getInstance().getCurrentUser().getName()))
                    sharerInfo.setText("Ajouté par vous-même");
                else
                    sharerInfo.setText("Ajouté par " + response);
                songList.get(songIndex).setSharerName(response);
                playingSong.setSharerName(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        if(playingSong.getSharerName().equals("none"))
            requestQueue.add(sharerRequest);

        new MyNotification(context, PlayListState.PLAYING);
    }

    private void playSoundCloud() {
        final String soundCloudKey = "c818b360defc350d7e45840b71e117e3";

        if(!playingSong.getStreamUrl().equals("none") && !playingSong.getCoverUrl().equals("none")) {
            Uri builtUri = Uri.parse(playingSong.getStreamUrl()).buildUpon()
                    .appendQueryParameter("client_id", soundCloudKey)
                    .build();
            // Build the sample source
            FrameworkSampleSource sampleSource = new FrameworkSampleSource(context, builtUri, null);
            // Build the track renderers
            TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true);
            // Build the ExoPlayer and start playback
            exoPlayer.prepare(audioRenderer);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.seekTo(0);
        } else {
            CustomRequest request = new CustomRequest(Request.Method.GET, "https://api.soundcloud.com/resolve.json?url=" + playingSong.getLink() + "&client_id=c818b360defc350d7e45840b71e117e3", null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getString("streamable").equals("true")) { //not every song is streamable
                            final String streamUrl = response.getString("stream_url");
                            String coverUrl = response.getString("artwork_url").replace("large.jpg", "t300x300.jpg");
                            songList.get(songIndex).setCoverUrl(coverUrl);
                            songList.get(songIndex).setStreamUrl(streamUrl);
                            playingSong.setCoverUrl(coverUrl);

                            Uri builtUri = Uri.parse(streamUrl).buildUpon()
                                    .appendQueryParameter("client_id", soundCloudKey)
                                    .build();
                            // Build the sample source
                            FrameworkSampleSource sampleSource = new FrameworkSampleSource(context, builtUri, null);
                            // Build the track renderers
                            TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true);
                            // Build the ExoPlayer and start playback
                            exoPlayer.prepare(audioRenderer);
                            exoPlayer.setPlayWhenReady(true);
                            exoPlayer.seekTo(0);
                        } else {
                            Toast.makeText(context, "Lecture impossible", Toast.LENGTH_SHORT).show();
                            listener.onSongError();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Lecture impossible", Toast.LENGTH_SHORT).show();
                    listener.onSongError();
                }
            });
            requestQueue.add(request);
        }
    }

    private void playYoutube() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String videoId = playingSong.getLink().substring(playingSong.getLink().indexOf("=") + 1);
        String coverUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
        songList.get(songIndex).setCoverUrl(coverUrl);
        playingSong.setCoverUrl(coverUrl);
        StringRequest request = new StringRequest(Request.Method.GET, "https://www.youtube.com/get_video_info?&video_id=" + videoId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String result = URLDecoder.decode(response, "UTF-8");
                    if(!result.startsWith("error") && !result.contains("fail")) {
                        String firstPart = URLDecoder.decode(result.substring(result.indexOf("dashmpd=") + 8), "UTF-8");
                        Log.i("playYoutube", "url = " + firstPart);
                        videoUrl = URLDecoder.decode(firstPart.substring(0, firstPart.indexOf("&")), "UTF-8");
                        contentUri = Uri.parse(videoUrl);
                        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();
                        userAgent = Util.getUserAgent(context, "LeFourre-ToutSonore");
                        manifestDataSource = new DefaultUriDataSource(context, userAgent);
                        manifestFetcher = new ManifestFetcher<>(contentUri.toString(), manifestDataSource, parser);
                        preparePlayer();
                    } else {
                        Toast.makeText(context, "Contenu protégé - Lecture impossible", Toast.LENGTH_SHORT).show();
                        listener.onSongError();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(playingSong.getLink())));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
                listener.onSongError();
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
                1);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource,
                null, true, mainHandler, null, AudioCapabilities.getCapabilities(context));

        exoPlayer.prepare(audioRenderer);
    }

    private void preparePlayer() {
        manifestFetcher.singleLoad(exoPlayer.getPlaybackLooper(), this);
        exoPlayer.seekTo(0);
        exoPlayer.setPlayWhenReady(true);
    }

    public void reset() {
        songList.clear();
    }

    public void pause() {
        new MyNotification(context, PlayListState.PAUSED);
        exoPlayer.setPlayWhenReady(false);
        state = PlayListState.PAUSED;
        listener.onSongPause();
        if(ipv != null)
            ipv.stop();
    }

    public void reloadIpv() {
        ipv = DataHolder.getInstance().getIpv();
    }

    public void resume() {
        if(state == PlayListState.PAUSED)
            exoPlayer.setPlayWhenReady(true);
    }

    public void previous() {
        if(songIndex > 0)
            play(songIndex-1);
    }

    public void next() {
        if(songIndex < songList.size() - 1)
            play(songIndex+1);
    }

    public long getSongDuration() {
        if(exoPlayer.getDuration() != ExoPlayer.UNKNOWN_TIME)
            return exoPlayer.getDuration()/1000;
        else
            return 123;
    }

    public Song getPlayingSong() {
        return playingSong;
    }

    void updateSongInfoDisplay() { //Move to playlistview
        if(playingSong.getSharerName().equals(DataHolder.getInstance().getCurrentUser().getName()))
            sharerInfo.setText("Ajouté par vous-même");
        else
            sharerInfo.setText("Ajouté par " + playingSong.getSharerName());
        songInfo.setText(playingSong.getArtist() + " - " + playingSong.getTitle());
        likesInfo.setText(playingSong.getLikes() + " ♥");
        stylesInfo.setText(playingSong.getStyles());
        descriptionInfo.setText(playingSong.getDescription());
        songArtistInfo.setText(playingSong.getArtist());
        songTitleInfo.setText(playingSong.getTitle());
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
