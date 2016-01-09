package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Share extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

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

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if(sharedText.contains("youtu"))
                parseYoutube(sharedText);
            else if(sharedText.contains("soundcloud"))
                parseSoundCloud(sharedText);
        }
    }

    void parseYoutube(String url) {
        ((TextView) findViewById(R.id.share_url)).setText(url);
        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("format", "json");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "http://www.youtube.com/oembed", params, this, this);
        requestQueue.add(jsObjRequest);
    }

    void parseSoundCloud(String text) {
        String url = text.split("#np on #SoundCloud")[1];
        ((TextView) findViewById(R.id.share_url)).setText(url);
        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("client_id", "@string/soundcloud_app_id");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "https://api.soundcloud.com/resolve.json", params, this, this);
        requestQueue.add(jsObjRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(Share.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        String title = "", artist = "";
        try {
            String fullTitle = response.getString("title");
            title = fullTitle.split(" - ")[0];
            artist = fullTitle.split(" - ")[1];

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.share_title)).setText(title);
        ((TextView) findViewById(R.id.share_artist)).setText(artist);
    }
}
