package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListChoice;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;

public class Main extends AppCompatActivity {

    private User currentUser;

    private PlayListChoice[] titleArray = new PlayListChoice[]{PlayListChoice.REGGAE, PlayListChoice.ELECTRO,
            PlayListChoice.TRANCE, PlayListChoice.POP, PlayListChoice.CORE, PlayListChoice.HIPHOP, PlayListChoice.ROCK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Le Fourre-Tout Sonore");
        setContentView(R.layout.activity_main);
        if(currentUser == null && DataHolder.getInstance().getCurrentUser() != null)
            currentUser = DataHolder.getInstance().getCurrentUser();
        else
            finish();
        ((TextView) findViewById(R.id.user)).setText(currentUser.getName());
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        SlidingUpPanelLayout slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, PlayListView.class);
                Main.this.startActivity(i);
            }
        });
        if(DataHolder.getInstance().getPlayer() == null) {
            DataHolder.getInstance().setPlayer(ExoPlayer.Factory.newInstance(1, 1000, 5000));
            DataHolder.getInstance().setPlaylist(new PlayList());
        }
        initCards();
        initDrawer();
    }

    protected void onResume() {
        super.onResume();
    }

    public void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                Intent myIntent = new Intent(Main.this, PlayListView.class);

                if (id == R.id.nav_all) {
                    DataHolder.getInstance().getPlaylist().setChoice(PlayListChoice.ALL);
                    Main.this.startActivity(myIntent);
                } else if (id == R.id.nav_my_songs) {
                    Toast.makeText(Main.this, "Fonction non implémentée", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_likes) {
                    DataHolder.getInstance().getPlaylist().setChoice(PlayListChoice.LIKES);
                    Main.this.startActivity(myIntent);
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

    public void initCards() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new RecyclerAdapter(titleArray, Main.this);
        recyclerView.setAdapter(adapter);
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
}
