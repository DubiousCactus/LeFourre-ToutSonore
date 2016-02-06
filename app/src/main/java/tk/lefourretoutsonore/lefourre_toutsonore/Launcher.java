package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import org.acra.ACRA;
import java.util.Collections;

import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListChoice;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;
import tk.lefourretoutsonore.lefourre_toutsonore.service.PollService;

public class Launcher extends AppCompatActivity {
    private CallbackManager callbackManager;
    private boolean toShareWeGo;
    private Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACRA.init(this.getApplication());
        toShareWeGo = false;
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }
        FacebookSdk.sdkInitialize(getApplicationContext());
        ProfileTracker mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                updateUI(); //this is the third piece of code I will discuss below
            }
        };
        mProfileTracker.startTracking();
        setContentView(R.layout.activity_launcher);
        callbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Collections.singletonList("public_profile, email"));
        PollService.setServiceAlarm(Launcher.this, true);
        updateUI();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();

        if (enableButtons && profile != null) {
            User user = new User(profile.getName(), Long.valueOf(profile.getId()));
            DataHolder.getInstance().setCurrentUser(user);
            if(getIntent().getStringExtra("playlist") != null) {
                myIntent = new Intent(Launcher.this, PlayListView.class);
                if(getIntent().getStringExtra("playlist").equals("all"))
                    myIntent.putExtra("choice", PlayListChoice.ALL);
            } else if(!toShareWeGo)
                myIntent = new Intent(Launcher.this, Main.class);

            Launcher.this.startActivity(myIntent);
            finish();
        }
    }

    void handleSendText(Intent intent) {
        Log.i("handleSendText", "Handling");
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        myIntent = new Intent(Launcher.this, Share.class);
        if (sharedText != null) {
            myIntent.putExtra("sharedText", sharedText);
            if(sharedText.contains("youtu"))
                myIntent.putExtra("function", "parseYoutube");
            else if(sharedText.contains("soundcloud"))
                myIntent.putExtra("function", "parseSoundCloud");

            toShareWeGo = true;
            updateUI();
        }
    }
}
