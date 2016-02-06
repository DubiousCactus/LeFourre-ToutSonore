package tk.lefourretoutsonore.lefourre_toutsonore;

/**
 * Created by transpalette on 12/30/15.
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayList;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListChoice;
import tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated.PlayListView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.EventViewHolder> {
    private PlayListChoice[] choiceSource;
    private Context context;
    InterstitialAd mInterstitialAd;

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView ban;
        TextView title;
        TextView styles;
        TextView counter;
        View strip;
        Context context;

        EventViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            itemView.setTag(itemView.findViewById(R.id.playListCard));
            cv = (CardView) itemView.findViewById(R.id.playListCard);
            title = (TextView) itemView.findViewById(R.id.card_title);
            counter = (TextView) itemView.findViewById(R.id.card_ajouts);
            styles = (TextView) itemView.findViewById(R.id.card_styles);
            strip = itemView.findViewById(R.id.strip);
            ban = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    public RecyclerAdapter(PlayListChoice[] choiceArgs, Context context) {
        this.context = context;
        choiceSource = choiceArgs;
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);

        return new EventViewHolder(view, context);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, final int position) {

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        holder.ban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, PlayListView.class);
                DataHolder.getInstance().getPlaylist().setChoice(choiceSource[position]);
                context.startActivity(myIntent);
                if (mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
            }
        });
        holder.ban.setImageResource(choiceSource[position].getBanId());
        holder.title.setText(choiceSource[position].getLongName());
        holder.counter.setText("--- ajouts");
        holder.styles.setText(choiceSource[position].getDesc());
        switch(choiceSource[position]) {
            case REGGAE:
                holder.strip.setBackgroundColor(Color.GREEN);
                break;
            case ELECTRO:
                holder.strip.setBackgroundColor(Color.WHITE);
                break;
            case TRANCE:
                holder.strip.setBackgroundColor(Color.GREEN);
                break;
            case POP:
                holder.strip.setBackgroundColor(Color.YELLOW);
                break;
            case CORE:
                holder.strip.setBackgroundColor(Color.RED);
                break;
            case HIPHOP:
                holder.strip.setBackgroundColor(Color.rgb(255, 128, 0));
                break;
            case ROCK:
                holder.strip.setBackgroundColor(Color.MAGENTA);
                break;
        }
        holder.strip.setAlpha(0.65f);
        fetchCount(holder, position);
    }

    public void fetchCount(final EventViewHolder holder, final int position) {
        Log.i("fetchCount", "Fetching count for " + holder.title.getText());
        PlayList pl = DataHolder.getInstance().getPlaylist();
        pl.setChoice(choiceSource[position]);
        if(pl.retrieveFromDisk())
            holder.counter.setText(pl.getCount() + " ajouts");

    }

    @Override
    public int getItemCount() {
        return choiceSource.length;
    }
}