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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;
import tk.lefourretoutsonore.lefourre_toutsonore.DataHolder;
import tk.lefourretoutsonore.lefourre_toutsonore.Main;
import tk.lefourretoutsonore.lefourre_toutsonore.MyNotification;
import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.Ranking;
import tk.lefourretoutsonore.lefourre_toutsonore.Song;
import tk.lefourretoutsonore.lefourre_toutsonore.User;

/**
 * Created by transpalette on 12/31/15.
 */

public class PlayListView extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener {

    private NavigationView navigationView;
    private ListView listView;
    private User currentUser;
    private ProgressDialog dialog;
    private InteractivePlayerView ipv;
    private boolean playing;
    private SlidingUpPanelLayout slidingLayout;
    private MyNotification notif;
    private ObjectAnimator colorFade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DataHolder.getInstance().getPlaylist().isPlaying())
            playing = true;
        else
            playing = false;
        currentUser = DataHolder.getInstance().getCurrentUser();
        setTitle(DataHolder.getInstance().getPlaylist().getChoice().getLongName());
        setContentView(R.layout.activity_playlist);
        initDrawer();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        notif = new MyNotification(this);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        listView = (ListView) findViewById(R.id.songsList);
        ipv = (InteractivePlayerView) findViewById(R.id.ipv);
        ipv.setMax(123);
        if(playing) {
            ipv.setMax((int) DataHolder.getInstance().getPlaylist().getSongDuration());
            ipv.setProgress(DataHolder.getInstance().getPlaylist().getCurrentPosition());
            ipv.start();
            ipv.setCoverURL(DataHolder.getInstance().getPlaylist().getSongList().get(DataHolder.getInstance().getPlaylist().getSongIndex()).getCoverUrl());
        } else
        ipv.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(int i) {
                switch (i) {
                    case 2:
                        DataHolder.getInstance().getPlaylist().likeSong();
                        break;
                }
            }
        });
        DataHolder.getInstance().setIpv(ipv);
        initListeners();
        if(playing)
            (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);

        ((TextView) findViewById(R.id.user)).setText(currentUser.getName());
        populate();
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

    public void stopBlinking(boolean error) {
        if(colorFade != null) {
            colorFade.cancel();
            colorFade.setTarget(null);
            colorFade = null;
            findViewById(R.id.imageBottom).setBackgroundColor(Color.parseColor("#262626"));
        }
        if(error)
            findViewById(R.id.play_button_layout).callOnClick();
    }

    @Override
    public void onPause() {
        ipv.stop();
        DataHolder.getInstance().setIpv(ipv);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i("PlayListView", "onResume called");
        super.onResume();
    }

    public void initListeners() {
        ipv.setCoverDrawable(R.drawable.no_cover);
        findViewById(R.id.next_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playing = true;
                (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                DataHolder.getInstance().getPlaylist().play(DataHolder.getInstance().getPlaylist().getSongIndex() + 1);
            }
        });

        findViewById(R.id.previous_song).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataHolder.getInstance().getPlaylist().getSongIndex() >= 0) {
                    playing = true;
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                    DataHolder.getInstance().getPlaylist().play(DataHolder.getInstance().getPlaylist().getSongIndex() - 1);
                }
            }
        });

        findViewById(R.id.play_button_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) { //Click on play
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                    ipv.stop();
                    if(DataHolder.getInstance().getPlaylist().isPlaying())
                        DataHolder.getInstance().getPlaylist().resume();
                    else
                        DataHolder.getInstance().getPlaylist().play(DataHolder.getInstance().getPlaylist().getSongIndex());
                    playing = true;
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else { //Click on pause
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
                    ipv.stop();
                    DataHolder.getInstance().getPlaylist().pause();
                    playing = false;
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                playing = true;
                ipv.stop();
                if(DataHolder.getInstance().getPlaylist().isPlaying())
                    DataHolder.getInstance().getPlaylist().pause();
                DataHolder.getInstance().getPlaylist().play(position);
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_ranking) {
                    Intent myIntent = new Intent(PlayListView.this, Ranking.class);
                    PlayListView.this.startActivity(myIntent);
                } else if (id == R.id.nav_all) {
                    setTitle(PlayListChoice.ALL.getLongName());
                    DataHolder.getInstance().getPlaylist().setChoice(PlayListChoice.ALL);
                    populate();
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else if (id == R.id.nav_home) {
                    Intent myIntent = new Intent(PlayListView.this, Main.class);
                    /*if(!playlist.getSongList().isEmpty()) {
                        myIntent.putExtra("choice", choice);
                        myIntent.putExtra("songIndex", playlist.getSongIndex());
                    }*/
                    PlayListView.this.startActivity(myIntent);
                } else if (id == R.id.nav_likes) {
                    setTitle(PlayListChoice.LIKES.getLongName());
                    DataHolder.getInstance().getPlaylist().setChoice(PlayListChoice.LIKES);
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
        TextView songInfo = (TextView) findViewById(R.id.songText);
        TextView sharerInfo = (TextView) findViewById(R.id.singerText);
        TextView likesInfo = (TextView) findViewById(R.id.likesCountText);
        TextView stylesInfo = (TextView) findViewById(R.id.stylesText);
        TextView descriptionInfo = (TextView) findViewById(R.id.descriptionText);
        TextView songTitleSlider = (TextView) findViewById(R.id.listHeader);
        TextView songArtistSlider = (TextView) findViewById(R.id.listSubHeader);
        DataHolder.getInstance().getPlaylist().setContext(this);
        DataHolder.getInstance().getPlaylist().setSongInfoDisplay(songInfo, sharerInfo, likesInfo, stylesInfo, descriptionInfo, songArtistSlider, songTitleSlider);
         dialog = ProgressDialog.show(this, "",
                "Chargement...", true);
        DataHolder.getInstance().getPlaylist().fetchSounds();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent myIntent = new Intent(PlayListView.this, Main.class);
            myIntent.putExtra("user", currentUser);
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
        if(DataHolder.getInstance().getPlaylist().retrieveFromDisk()) {
            listView = (ListView) findViewById(R.id.songsList);
            PlaylistAdapter adapter = new PlaylistAdapter(this, DataHolder.getInstance().getPlaylist());
            listView.setAdapter(adapter);
            Toast.makeText(PlayListView.this, "Chargement depuis le cache", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(PlayListView.this, "Aucun fichier cache", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onResponse(JSONObject response) {
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
                    if(song.getJSONObject("details") instanceof JSONObject) {
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

            Song songItem = new Song(id, likes, sharer, title, artist, styles, link, description, liked, DataHolder.getInstance().getPlaylist());
            DataHolder.getInstance().getPlaylist().addSong(songItem);
            count++;
        }
        DataHolder.getInstance().getPlaylist().setCount(count);
        DataHolder.getInstance().getPlaylist().saveOnDisk();
        PlaylistAdapter adapter = new PlaylistAdapter(this, DataHolder.getInstance().getPlaylist());
        listView.setAdapter(adapter);
        dialog.dismiss();
    }

}
