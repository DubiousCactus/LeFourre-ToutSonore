package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;
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
    private PlayList playlist;
    private ListView listView;
    private PlayListChoice choice;
    private User currentUser;
    private ProgressDialog dialog;
    private InteractivePlayerView ipv;
    private boolean playing;
    private SlidingUpPanelLayout slidingLayout;
    private MyNotification notif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playing = false;
        choice = (PlayListChoice) getIntent().getSerializableExtra("choice");
        currentUser = (User) getIntent().getSerializableExtra("user");
        setTitle(choice.getLongName());
        setContentView(R.layout.activity_playlist);
        initDrawer();
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        listView = (ListView) findViewById(R.id.songsList);
        ipv = (InteractivePlayerView) findViewById(R.id.ipv);
        ipv.setMax(123);
        ipv.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(int i) {
                switch (i) {
                    case 2:
                        playlist.likeSong();
                        break;
                }
            }
        });
        initListeners();
        ((TextView) findViewById(R.id.user)).setText(currentUser.getName());
        populate(choice);
        notif = new MyNotification(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause()  {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notif.notificationCancel();
        playlist.destroy();
    }


    public void initListeners() {
        ipv.setCoverDrawable(R.drawable.no_cover);
        (findViewById(R.id.next_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist.play(playlist.getSongIndex() + 1);
                playing = true;
            }
        });

        (findViewById(R.id.previous_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playlist.getSongIndex() >= 0) {
                    playlist.play(playlist.getSongIndex() - 1);
                    playing = true;
                }
            }
        });

        (findViewById(R.id.play_button_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                    playlist.play(playlist.getSongIndex());
                    playing = true;
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
                    playlist.pause();
                    ipv.stop();
                    playing = false;
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                playing = true;
                playlist.pause();
                playlist.play(position);
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
                    setTitle(choice.ALL.getLongName());
                    populate(choice.ALL);
                } else if (id == R.id.nav_home) {
                    Intent myIntent = new Intent(PlayListView.this, Main.class);
                    myIntent.putExtra("user", currentUser);
                    if(!playlist.getSongList().isEmpty()) {
                        myIntent.putExtra("choice", choice);
                        myIntent.putExtra("songIndex", playlist.getSongIndex());
                    }
                    PlayListView.this.startActivity(myIntent);
                } else if (id == R.id.nav_likes) {
                    setTitle(choice.LIKES.getLongName());
                    populate(choice.LIKES);
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

    private void populate(PlayListChoice choice) {
        TextView songInfo = (TextView) findViewById(R.id.songText);
        TextView sharerInfo = (TextView) findViewById(R.id.singerText);
        TextView likesInfo = (TextView) findViewById(R.id.likesCountText);
        TextView stylesInfo = (TextView) findViewById(R.id.stylesText);
        TextView descriptionInfo = (TextView) findViewById(R.id.descriptionText);
        TextView songTitleSlider = (TextView) findViewById(R.id.listHeader);
        TextView songArtistSlider = (TextView) findViewById(R.id.listSubHeader);
        playlist = new PlayList(choice, this, ipv, currentUser);
        playlist.setSongInfoDisplay(songInfo, sharerInfo, likesInfo, stylesInfo, descriptionInfo, songArtistSlider, songTitleSlider);
        if(choice == PlayListChoice.LIKES) {
            playlist.setCurrentUser(currentUser);
            Log.i("id", "id = " + currentUser.getId());
        }
         dialog = ProgressDialog.show(this, "",
                "Chargement...", true);
        playlist.fetchSounds();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        if(playlist.retrieveFromDisk()) {
            listView = (ListView) findViewById(R.id.songsList);
            PlaylistAdapter adapter = new PlaylistAdapter(this, playlist);
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

            Song songItem = new Song(id, likes, sharer, title, artist, styles, link, description, liked, playlist);
            playlist.addSong(songItem);
            count++;
        }
        playlist.setCount(count);
        playlist.saveOnDisk();
        PlaylistAdapter adapter = new PlaylistAdapter(this, playlist);
        listView.setAdapter(adapter);
        dialog.dismiss();
    }

}
