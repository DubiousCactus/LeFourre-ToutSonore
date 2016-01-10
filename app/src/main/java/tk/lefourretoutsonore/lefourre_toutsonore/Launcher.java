package tk.lefourretoutsonore.lefourre_toutsonore;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Launcher extends AppCompatActivity {
    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;
    private boolean toShareWeGo;
    private Intent myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                updateUI(); //this is the third piece of code I will discuss below
            }
        };
        mProfileTracker.startTracking();
        setContentView(R.layout.activity_launcher);
        callbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Launcher.this, "Connexion annulée", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Launcher.this, "Connexion échouée", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        Profile profile = Profile.getCurrentProfile();
        if (profile == null) {
            Log.e("Profile", "null");
        }
        if (enableButtons && profile != null) {
            Log.i("toShareWeGo", String.valueOf(toShareWeGo));
            User user = new User(profile.getName(), Long.valueOf(profile.getId()));
            if(!toShareWeGo)
                myIntent = new Intent(Launcher.this, Main.class);

            myIntent.putExtra("user", user);
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
