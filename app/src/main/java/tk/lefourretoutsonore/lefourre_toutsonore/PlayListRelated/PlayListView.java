package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import co.mobiwise.library.InteractivePlayerView;
import co.mobiwise.library.OnActionClickedListener;
import tk.lefourretoutsonore.lefourre_toutsonore.CustomRequest;
import tk.lefourretoutsonore.lefourre_toutsonore.Main;
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
    private PlayList.PlayListChoice choice;
    private User currentUser;
    private ProgressDialog dialog;
    private InteractivePlayerView ipv;
    private boolean playing;
    private TextView songInfo;
    private TextView sharerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playing = false;
        choice = (PlayList.PlayListChoice) getIntent().getSerializableExtra("choice");
        currentUser = (User) getIntent().getSerializableExtra("user");
        setTitle(choice.getLongName());
        setContentView(R.layout.activity_playlist);
        initDrawer();
        ipv = (InteractivePlayerView) findViewById(R.id.ipv);
        ipv.setMax(123);
        ipv.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(int i) {
                switch (i) {
                    case 2:
                        //Called when 2. action is clicked.
                        Log.i("Action", "like");
                        playlist.likeSong();
                        break;
                }
            }
        });
        initListeners();
        ((TextView) findViewById(R.id.user)).setText(currentUser.getName());
        populate(choice);
    }

    @Override
    public void onResume() {
        super.onResume();
        playing = false;
    }

    public void initListeners() {
        songInfo = (TextView) findViewById(R.id.songText);
        sharerInfo = (TextView) findViewById(R.id.singerText);
        ipv.setCoverDrawable(R.drawable.no_cover);
        (findViewById(R.id.next_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist.play(playlist.getSongIndex() + 1, songInfo, sharerInfo);
                playing = true;
            }
        });

        (findViewById(R.id.previous_song)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlist.getSongIndex() >= 0) {
                    playlist.play(playlist.getSongIndex() - 1, songInfo, sharerInfo);
                    playing = true;
                }
            }
        });

        (findViewById(R.id.play_button_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.pause);
                    playlist.play(playlist.getSongIndex(), songInfo, sharerInfo);
                    playing = true;
                } else {
                    (findViewById(R.id.control)).setBackgroundResource(R.drawable.play);
                    playlist.pause();
                    playing = false;
                }
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

    private void populate(PlayList.PlayListChoice choice) {
        playlist = new PlayList(choice, this, ipv, currentUser);
        if(choice == PlayList.PlayListChoice.LIKES) {
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
            String title = "", artist = "", styles = "", link = "";
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
                    }
                }
            } catch (JSONException e) { e.printStackTrace(); }

            Song songItem = new Song(this, id, likes, sharer, title, artist, styles, link, liked, playlist);
            playlist.addSong(songItem);
            count++;
        }
        playlist.setCount(count);
        playlist.saveOnDisk();
        listView = (ListView) findViewById(R.id.songsList);
        PlaylistAdapter adapter = new PlaylistAdapter(this, playlist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                (findViewById(R.id.play_button_layout)).callOnClick();
                playlist.pause();
                playlist.play(position, songInfo, sharerInfo);
            }
        });
        dialog.dismiss();
    }
}
