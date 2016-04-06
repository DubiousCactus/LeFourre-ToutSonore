package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;
import tk.lefourretoutsonore.lefourre_toutsonore.DataHolder;
import tk.lefourretoutsonore.lefourre_toutsonore.Main;
import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.Ranking;
import tk.lefourretoutsonore.lefourre_toutsonore.SongRelated.Song;

/**
 * Created by transpalette on 12/31/15.
 */

public class PlayListView extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener, StateListener {

    private ListView listView;
    private ProgressDialog dialog;
    private InteractivePlayerView ipv;
    private PlayList playList;
    private SlidingUpPanelLayout slidingLayout;
    private ObjectAnimator colorFade;
    private TextView songInfo;
    private TextView sharerInfo;
    private TextView likesInfo;
    private TextView stylesInfo;
    private TextView descriptionInfo;
    private TextView songTitleSlider;
    private TextView songArtistSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        playList = DataHolder.getInstance().getPlaylist();
        playList.setListener(this);
        initAds();
        setTitle(playList.getChoice().getLongName());
        initTextViews();
        initDrawer();
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        listView = (ListView) findViewById(R.id.songsList);
        ipv = (InteractivePlayerView) findViewById(R.id.ipv);
        ipv.setMax(123);
        ipv.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(int i) {
                switch (i) {
                    case 2:
                        playList.likeSong();
                        break;
                }
            }
        });
        DataHolder.getInstance().setIpv(ipv);
        initListeners();
        ((TextView) findViewById(R.id.user)).setText(DataHolder.getInstance().getCurrentUser().getName());
        (findViewById(R.id.previous_song)).setVisibility(View.INVISIBLE);
        populate();
    }

    private void initAds() {
        final InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.inter_ad_pl_unit_id));
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest2);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdClosed() {
                if (playList.getState() == PlayListState.PLAYING) {
                    InteractivePlayerView ipv = DataHolder.getInstance().getIpv();
                    ipv.setProgress((int) DataHolder.getInstance().getPlayer().getCurrentPosition() / 1000);
                    ipv.start();
                }
            }
        });
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void initTextViews() {
        sharerInfo = (TextView) findViewById(R.id.singerText);
        songInfo = (TextView) findViewById(R.id.songText);
        likesInfo = (TextView) findViewById(R.id.likesCountText);
        stylesInfo = (TextView) findViewById(R.id.stylesText);
        descriptionInfo = (TextView) findViewById(R.id.descriptionText);
        songTitleSlider = (TextView) findViewById(R.id.listHeader);
        songArtistSlider = (TextView) findViewById(R.id.listSubHeader);
    }

    @Override
    protected void onStop() {
        ipv.stop();
        DataHolder.getInstance().setIpv(ipv);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void blink() {
        colorFade = ObjectAnimator.ofObject(findViewById(R.id.imageBottom), "backgroundColor", new ArgbEvaluator(), Color.argb(200, 0, 0, 210), Color.parseColor("#262626"));
        colorFade.setDuration(2000);
        colorFade.setRepeatCount(ObjectAnimator.INFINITE);
        colorFade.setRepeatMode(ObjectAnimator.REVERSE);
        colorFade.start();
    }

    public void stopBlinking() {
        if(colorFade != null) {
            colorFade.cancel();
            colorFade.setTarget(null);
            colorFade = null;
            findViewById(R.id.imageBottom).setBackgroundColor(Color.parseColor("#262626"));
        }
    }

    @Override
    public void onPause() {
        ipv.stop();
        DataHolder.getInstance().setIpv(ipv);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(playList.getState() == PlayListState.PLAYING || playList.getState() == PlayListState.PAUSED) {
            ipv.setMax((int) playList.getSongDuration());
            ipv.setCoverDrawable(R.drawable.no_cover);
            ipv.setProgress((int) DataHolder.getInstance().getPlayer().getCurrentPosition() / 1000);
            if(playList.getState() == PlayListState.PLAYING)
                ipv.start();
            if(DataHolder.getInstance().getPreviousView() != playList.getChoice()) {
                (findViewById(R.id.previous_song)).setVisibility(View.INVISIBLE);
                (findViewById(R.id.next_song)).setVisibility(View.INVISIBLE);
            }
            playList.setSongInfoDisplay(songInfo, sharerInfo, likesInfo, stylesInfo, descriptionInfo, songArtistSlider, songTitleSlider);
            playList.updateSongInfoDisplay();
            String coverURl = playList.getPlayingSong().getCoverUrl();
            if(coverURl != null)
                ipv.setCoverURL(coverURl);
            DataHolder.getInstance().setIpv(ipv);
            playList.reloadIpv();
        } else {
            (findViewById(R.id.next_song)).setVisibility(View.VISIBLE);
        }
    }

    public void initListeners() {
        findViewById(R.id.next_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playList.play(playList.getSongIndex() + 1);
                (findViewById(R.id.previous_song)).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.previous_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playList.getSongIndex() > 0) {
                    playList.play(playList.getSongIndex() - 1);
                }
                (findViewById(R.id.next_song)).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.play_button_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playList.getState() == PlayListState.PAUSED)
                    playList.resume();
                else if (playList.getState() != PlayListState.PLAYING && playList.getState() != PlayListState.BUFFERING) { //Click on play
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                    playList.play(playList.getSongIndex());
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else //Click on pause
                    playList.pause();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playList.getState() == PlayListState.PLAYING || playList.getState() == PlayListState.BUFFERING) {
                    playList.pause();
                    (findViewById(R.id.next_song)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.previous_song)).setVisibility(View.VISIBLE);
                }
                (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                playList.play(position);
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                if(position == 0) {
                    (findViewById(R.id.previous_song)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.next_song)).setVisibility(View.VISIBLE);
                }
                else if(position == playList.getSongList().size() - 1) {
                    (findViewById(R.id.next_song)).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.previous_song)).setVisibility(View.VISIBLE);
                } else {
                    (findViewById(R.id.previous_song)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.next_song)).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                DataHolder.getInstance().setPreviousView(playList.getChoice());
                if (id == R.id.nav_my_songs) {
                    Intent myIntent = new Intent(PlayListView.this, Ranking.class);
                    PlayListView.this.startActivity(myIntent);
                } else if (id == R.id.nav_all) {
                    setTitle(PlayListChoice.ALL.getLongName());
                    playList.setChoice(PlayListChoice.ALL);
                    populate();
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else if (id == R.id.nav_home) {
                    Intent myIntent = new Intent(PlayListView.this, Main.class);
                    PlayListView.this.startActivity(myIntent);
                } else if (id == R.id.nav_likes) {
                    setTitle(PlayListChoice.LIKES.getLongName());
                    playList.setChoice(PlayListChoice.LIKES);
                    populate();
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return true;
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void populate() {
        playList.setContext(this);
        playList.setSongInfoDisplay(songInfo, sharerInfo, likesInfo, stylesInfo, descriptionInfo, songArtistSlider, songTitleSlider);
         dialog = ProgressDialog.show(this, "",
                "Chargement...", true);
        playList.fetchSounds();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        DataHolder.getInstance().setPreviousView(playList.getChoice());
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent myIntent = new Intent(PlayListView.this, Main.class);
            PlayListView.this.startActivity(myIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
        if(playList.retrieveFromDisk()) {
            listView = (ListView) findViewById(R.id.songsList);
            PlaylistAdapter adapter = new PlaylistAdapter(this, playList);
            listView.setAdapter(adapter);
            Toast.makeText(PlayListView.this, "Chargement depuis le cache", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(PlayListView.this, "Aucun fichier cache", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onResponse(JSONObject response) {
        playList.reset();
        int count = 0;
        for(Iterator it = response.keys(); it.hasNext();) { //Parsing list of songs
            String songId = (String) it.next(); //Selecting song
            String title = "", artist = "", styles = "", link = "", description = "Aucune description";
            int id = 0, likes = 0;
            long sharer = 0;
            boolean liked = false;
            try {
                if(response.get(songId) instanceof JSONObject) {
                    JSONObject song = response.getJSONObject(songId);
                    id = song.getInt("id");
                    if(song.getJSONObject("details") != null) {
                        JSONObject songDetails = song.getJSONObject("details");
                        likes = songDetails.getInt("likes");
                        title = songDetails.getString("title");
                        artist = songDetails.getString("artist");
                        styles = songDetails.getString("styles");
                        link = songDetails.getString("link");
                        sharer = songDetails.getLong("sharerId");
                        liked = songDetails.getBoolean("liked");
                        if(!songDetails.getString("description").equals(""))
                            description = songDetails.getString("description");
                    }
                }
            } catch (JSONException e) { e.printStackTrace(); }

            Song songItem = new Song(id, likes, sharer, title, artist, styles, link, description, liked);
            playList.addSong(songItem);
            count++;
        }
        playList.setCount(count);
        playList.saveOnDisk();
        PlaylistAdapter adapter = new PlaylistAdapter(this, playList);
        listView.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onSongPlay() {
        (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
        stopBlinking();
    }

    @Override
    public void onSongPause() {
        (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
        stopBlinking();
    }

    @Override
    public void onSongIdle() {
        //(findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
        //stopBlinking();
    }

    @Override
    public void onSongError() {
        (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
        stopBlinking();
    }

    @Override
    public void onSongStop() {
        (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
        stopBlinking();
    }
    
    @Override
    public void onSongBuffering() {
        (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
        blink();
    }
}
