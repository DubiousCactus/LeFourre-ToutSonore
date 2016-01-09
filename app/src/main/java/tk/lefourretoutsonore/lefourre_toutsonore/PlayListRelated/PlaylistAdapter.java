package tk.lefourretoutsonore.lefourre_toutsonore.PlayListRelated;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import tk.lefourretoutsonore.lefourre_toutsonore.R;
import tk.lefourretoutsonore.lefourre_toutsonore.Song;

/**
 * Created by transpalette on 1/2/16.
 */
public class PlaylistAdapter extends ArrayAdapter<Song> {
    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView artist;
        TextView styles;
        TextView likes;
    }

    public PlaylistAdapter(Context context, PlayList playlist) {
        super(context, 0, playlist.getSongList());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            Song song = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.song_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.row_title);
                viewHolder.artist = (TextView) convertView.findViewById(R.id.row_artist);
                viewHolder.styles = (TextView) convertView.findViewById(R.id.row_styles);
                viewHolder.likes = (TextView) convertView.findViewById(R.id.row_likes);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Lookup view for data population
            viewHolder.title.setText(song.getTitle());
            viewHolder.artist.setText(song.getArtist());
            viewHolder.styles.setText(song.getStyles());
            viewHolder.likes.setText(String.valueOf(song.getLikes()) + "  ❤");


        // Return the completed view to render on screen
        return convertView;
    }

}
