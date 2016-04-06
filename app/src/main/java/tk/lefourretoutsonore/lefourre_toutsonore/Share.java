package tk.lefourretoutsonore.lefourre_toutsonore;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;

import tk.lefourretoutsonore.lefourre_toutsonore.SongRelated.Song;
import tk.lefourretoutsonore.lefourre_toutsonore.SongRelated.Style;
import tk.lefourretoutsonore.lefourre_toutsonore.SongRelated.StylesAdapter;

public class Share extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject> {
    private StylesAdapter stylesAdapter;
    private ArrayList<Style> styles;
    private boolean stylesDone;
    private boolean title_artistDone;
    private Song song;
    private User currentUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stylesDone = false;
        title_artistDone = false;
        song = new Song();
        currentUser = DataHolder.getInstance().getCurrentUser();
        setContentView(R.layout.activity_share);

        initStyles();
        stylesAdapter = new StylesAdapter(this, styles);
        findViewById(R.id.share_styles).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowAlertDialogWithListView();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song.setDescription(((EditText) findViewById(R.id.share_description)).getText().toString());
                //song.setSharer();
                RequestQueue requestQueue = Volley.newRequestQueue(Share.this);
                StringRequest request = new StringRequest(Request.Method.POST, "http://lefourretoutsonore.tk/ajout.php", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Share.this, "Son ajouté !", Toast.LENGTH_SHORT).show();
                        Share.this.finish();
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
                        params.put("partageur", String.valueOf(currentUser.getId()));

                        return params;
                    }
                };
                Log.i("request", request.toString());
                requestQueue.add(request);
                progressDialog = ProgressDialog.show(Share.this, "",
                        "Ajout en cours...", true);
            }
        });
        progressDialog = ProgressDialog.show(this, "",
                "Chargement...", true);
        if(getIntent().getStringExtra("function").equals("parseSoundCloud"))
            parseSoundCloud(getIntent().getStringExtra("sharedText"));
        else if(getIntent().getStringExtra("function").equals("parseYoutube"))
            parseYoutube(getIntent().getStringExtra("sharedText"));
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    @Override
    protected void onResume() {
        if(stylesDone) {
            title_artistDone = false;
            progressDialog = ProgressDialog.show(this, "",
                    "Chargement...", true);
            if (getIntent().getStringExtra("function").equals("parseSoundCloud"))
                parseSoundCloud(getIntent().getStringExtra("sharedText"));
            else if (getIntent().getStringExtra("function").equals("parseYoutube"))
                parseYoutube(getIntent().getStringExtra("sharedText"));
        }
        super.onResume();
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
                stylesDone = true;
                progressDialog.dismiss();
            }
        }, this);
        requestQueue.add(jsObjRequest);
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
        params.put("client_id", getString(R.string.soundCloud_app_id));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, "https://api.soundcloud.com/resolve.json", params, this, this);
        requestQueue.add(jsObjRequest);
        song.setLink(url);
    }

    public void ShowAlertDialogWithListView()
    {
        final ArrayList<Integer> selectedItemsIndexList = new ArrayList<>();
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Styles")
                .setAdapter(stylesAdapter, null)
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validateChoice(selectedItemsIndexList);
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

    private void validateChoice(ArrayList<Integer> selectedItemsIndexList) {
        ArrayList<Style> selectedStyles = new ArrayList<>();
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
            findViewById(R.id.share_button).setClickable(true);

        String stringFormatStyles = "";
        for(Iterator i = selectedStyles.iterator(); i.hasNext();) {
            stringFormatStyles = stringFormatStyles.concat(String.valueOf(((Style) i.next()).getId()));
            if(i.hasNext())
                stringFormatStyles = stringFormatStyles.concat(",");
        }

        song.setStyles(stringFormatStyles);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        progressDialog.dismiss();
        Toast.makeText(Share.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        String title = "", artist = "";
        try {
            String fullTitle = response.getString("title");
            if(fullTitle.contains("-")) {
                title = fullTitle.split(" - ")[0];
                artist = fullTitle.split(" - ")[1];
            } else
                title = fullTitle;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        EditText vTitle = (EditText) findViewById(R.id.share_title);
        EditText vArtist = (EditText) findViewById(R.id.share_artist);
        vTitle.setText(title);
        vArtist.setText(artist);
        title_artistDone = true;
        if(stylesDone)
            findViewById(R.id.share_button).setClickable(true);
        song.setTitle(title);
        song.setArtist(artist);
    }
}
