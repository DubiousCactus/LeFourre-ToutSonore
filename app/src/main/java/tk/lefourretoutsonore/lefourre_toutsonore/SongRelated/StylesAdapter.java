package tk.lefourretoutsonore.lefourre_toutsonore.SongRelated;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tk.lefourretoutsonore.lefourre_toutsonore.R;

/**
 * Created by M4gicT0 on 09/01/2016.
 */
public class StylesAdapter extends ArrayAdapter<Style> {

    private static class ViewHolder {
        TextView style;
    }

    public StylesAdapter(Context context, ArrayList<Style> styles) {
        super(context, 0, styles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Style style = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.style_item, parent, false);
            viewHolder.style = (TextView) convertView.findViewById(R.id.list_style);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Lookup view for data population
        viewHolder.style.setText(style.getName());

        // Return the completed view to render on screen
        return convertView;
    }
}
