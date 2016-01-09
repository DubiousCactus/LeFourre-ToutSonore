package tk.lefourretoutsonore.lefourre_toutsonore;

import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class Ranking extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        return false;
    }
}
