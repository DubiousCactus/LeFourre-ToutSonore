package tk.lefourretoutsonore.lefourre_toutsonore;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.Arrays;

public class Launcher extends AppCompatActivity {
    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;
    private boolean shouldExecuteOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldExecuteOnResume = false;
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
            User user = new User(profile.getName(), Long.valueOf(profile.getId()));
            Intent i = new Intent(Launcher.this, Main.class);
            i.putExtra("user", user);
            Launcher.this.startActivity(i);
            finish();
        }
    }
}
