package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
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
import java.io.IOException;
import tk.lefourretoutsonore.lefourre_toutsonore.player.DemoPlayer;

public class YoutubeVideo implements ExoPlayer.Listener, ManifestFetcher.ManifestCallback<MediaPresentationDescription>, UtcTimingElementResolver.UtcTimingCallback  {
    private String videoUrl;
    private Uri contentUri;
    private ExoPlayer player;
    private long playerPosition;
    private MediaPresentationDescription manifest;
    private DefaultUriDataSource manifestDataSource;
    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private long elapsedRealtimeOffset;
    private String userAgent;
    private Context context;

    public YoutubeVideo(Context context) {
        this.context = context;
        videoUrl = "http://manifest.googlevideo.com/api/manifest/dash/sparams/as,hfr,id,ip,ipbits,itag,mm,mn,ms,mv,pl,playback_host,source,expire/key/yt6/ip/188.180.86.74/upn/OLpQve8eO2Y/fexp/3300134,3300161,3312381,9405191,9405349,9406993,9408545,9416126,9416985,9419444,9420452,9422431,9422540,9422596,9423455,9423662,9424298,9425534/itag/0/mm/31/id/o-APJk8nQuX73DcZfokVGRTi4MvFSfAvu06aezE9n-YH24/playback_host/r4---sn-uqj-55gl.googlevideo.com/pl/24/mv/m/hfr/1/ms/au/ipbits/0/as/fmp4_audio_clear,webm_audio_clear,fmp4_sd_hd_clear,webm_sd_hd_clear,webm2_sd_hd_clear/signature/3B9E84719DD6F3E8AF23206797B1A0222F56E351.162D46CCCC467B1C4778F24D44E6B0AF9462C75D/source/youtube/sver/3/expire/1453075543/mn/sn-uqj-55gl/mt/1453053845";
        contentUri = Uri.parse(videoUrl);
        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();
        userAgent = Util.getUserAgent(context, "LeFourre-ToutSonore");
        manifestDataSource = new DefaultUriDataSource(context, userAgent);
        manifestFetcher = new ManifestFetcher<>(contentUri.toString(), manifestDataSource, parser);
        releasePlayer();
        preparePlayer();
    }

    public void buildRenderer() {
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

        player.prepare(audioRenderer);
    }

    /*
     * Prepare the player for being played
     * Add its listeners and set its media player
     */
    private void preparePlayer() {
        if (player == null) {
            player = ExoPlayer.Factory.newInstance(1, 1000, 5000);
            manifestFetcher.singleLoad(player.getPlaybackLooper(), this);
            player.addListener(this);
            player.seekTo(playerPosition);
            player.setPlayWhenReady(true);
        }
    }

    /*
     * This method is used when the player is no longer needed
     */
    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

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
