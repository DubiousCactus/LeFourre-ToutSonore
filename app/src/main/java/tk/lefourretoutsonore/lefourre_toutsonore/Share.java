package tk.lefourretoutsonore.lefourre_toutsonore;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Share extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject> {
    private StylesAdapter stylesAdapter;
    private ArrayList<Style> styles;
    private ArrayList<Style> selectedStyles;
    private boolean stylesDone;
    private boolean title_artistDone;
    private String url, title, artist, desc, style;
    private int sharer;
    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stylesDone = false;
        title_artistDone = false;
        song = new Song();
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
        initStyles();
        stylesAdapter = new StylesAdapter(this, styles);
        ((Button) findViewById(R.id.share_styles)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowAlertDialogWithListview();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue requestQueue = Volley.newRequestQueue(Share.this);
                StringRequest request = new StringRequest(Request.Method.POST, "http://lefourretoutsonore.tk/ajout.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, Share.this) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("lien", song.getLink());
                        params.put("styles", song.getStyles());
                        params.put("titre", song.getTitle());
                        params.put("artiste", song.getArtist());
                        params.put("description", song.getDescription());
                        params.put("partageur", String.valueOf(song.getSharer()));

                        return params;
                    }
                };
                requestQueue.add(request);
            }
        });
    }

    private void initStyles() {
        //init styles arrayList from xml file
        styles = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest("http://lefourretoutsonore.tk/service/getStyles.php", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                for(Iterator it = response.keys(); it.hasNext();) { //Parsing list of styles
                    String styleNumber = (String) it.next(); //Selecting style
                    String name = "";
                    int id = 0, genre = 0;
                    try {
                        if(response.get(styleNumber) instanceof JSONObject) {
                            JSONObject style = response.getJSONObject(styleNumber);
                            id = style.getInt("id");
                            genre = style.getInt("genre");
                            name = style.getString("name");
                        }
                    } catch (JSONException e) { e.printStackTrace(); }

                    Style style = new Style(genre, id, name);
                    styles.add(style);
                }
            }
        }, this);
        requestQueue.add(jsObjRequest);
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
        song.setLink(url);
    }

    void parseSoundCloud(String text) {
        String url = text.split("#np on #SoundCloud")[1];
        ((TextView) findViewById(R.id.share_url)).setText(url);
        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("client_id", "@string/soundCloud_app_id");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "https://api.soundcloud.com/resolve.json", params, this, this);
        requestQueue.add(jsObjRequest);
        song.setLink(url);
    }

    public void ShowAlertDialogWithListview()
    {
        final ArrayList<Integer> selectedItemsIndexList = new ArrayList<>();
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Styles")
                .setAdapter(stylesAdapter, null)
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateChoie(selectedItemsIndexList);
                    }
                })
                .setNegativeButton("Annuler", null)
                .create();

        dialog.getListView().setItemsCanFocus(false);
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Manage selected items here
                System.out.println("clicked" + position);
                CheckedTextView textView = (CheckedTextView) view;
                if (textView.isChecked()) {
                    selectedItemsIndexList.add(position);
                } else if (selectedItemsIndexList.contains(position)) {
                    selectedItemsIndexList.remove(Integer.valueOf(position));
                }
            }
        });
        dialog.show();
    }

    private void validateChoie(ArrayList<Integer> selectedItemsIndexList) {
        selectedStyles = new ArrayList<>();
        TextView stylesDisplay = (TextView) findViewById(R.id.share_styles_display);
        for(Iterator it = selectedItemsIndexList.iterator(); it.hasNext();) {
            Style current = styles.get((int) it.next());
            stylesDisplay.append(current.getName());
            if(it.hasNext())
                stylesDisplay.append(", ");
            selectedStyles.add(current);
        }
        stylesDone = true;
        if(title_artistDone)
            ((Button) findViewById(R.id.share_button)).setClickable(true);
        song.setStyles(styles.toString());
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
        EditText vTitle = (EditText) findViewById(R.id.share_title);
        EditText vArtist = (EditText) findViewById(R.id.share_artist);
        vTitle.setText(title);
        vTitle.setFocusable(true);
        vTitle.setClickable(true);
        vArtist.setText(artist);
        vTitle.setFocusable(true);
        vTitle.setClickable(true);
        title_artistDone = true;
        if(stylesDone)
            ((Button) findViewById(R.id.share_button)).setClickable(true);
        song.setTitle(title);
        song.setArtist(artist);
    }
}
