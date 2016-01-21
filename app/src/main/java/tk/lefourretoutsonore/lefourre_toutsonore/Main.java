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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListChoice;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlaylistAdapter;

public class Main extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private NavigationView navigationView;
    private User currentUser;
    private SlidingUpPanelLayout slidingLayout;

    private PlayListChoice[] titleArray = new PlayListChoice[]{PlayListChoice.REGGAE, PlayListChoice.ELECTRO,
            PlayListChoice.TRANCE, PlayListChoice.POP, PlayListChoice.CORE, PlayListChoice.HIPHOP, PlayListChoice.ROCK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Le Fourre-Tout Sonore");
        setContentView(R.layout.activity_main);
        if(currentUser == null)
            currentUser = DataHolder.getInstance().getCurrentUser();
        ((TextView) findViewById(R.id.user)).setText(currentUser.getName());
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        initCards();
        initDrawer();
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if(DataHolder.getInstance().getPlayer() == null) {
            Log.i("main", "first run !");
            DataHolder.getInstance().setPlayer(ExoPlayer.Factory.newInstance(1, 1000, 5000));
            DataHolder.getInstance().setPlaylist(new PlayList());
        }
    }

    protected void onResume() {
        super.onResume();
    }

    public void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                Intent myIntent = new Intent(Main.this, PlayListView.class);

                if (id == R.id.nav_all) {
                    myIntent.putExtra("choice", PlayListChoice.ALL);
                    Main.this.startActivity(myIntent);
                } else if (id == R.id.nav_ranking) {
                    myIntent = new Intent(Main.this, Ranking.class);
                    myIntent.putExtra("playlist", 3);
                    Main.this.startActivity(myIntent);
                } else if (id == R.id.nav_likes) {
                    myIntent.putExtra("choice", PlayListChoice.LIKES);
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
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(titleArray, Main.this, currentUser);
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
