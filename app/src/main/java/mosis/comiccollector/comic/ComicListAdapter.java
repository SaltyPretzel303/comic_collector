package mosis.comiccollector.comic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import mosis.comiccollector.R;

public class ComicListAdapter extends ArrayAdapter<Comic> {

    private Context context;
    private int item_template;
    private List<Comic> comics;

    public ComicListAdapter(Context context, int item_template, List<Comic> comics) {
        super(context, item_template, comics);

        this.context = context;
        this.item_template = item_template;
        this.comics = comics;

    }

    public View getView(int pos, View old_view, ViewGroup parent) {

        // reuse old view if possible
        if (old_view == null) {
            old_view = (LayoutInflater.from(this.context)).inflate(item_template, parent, false);
        }

        // get comic sample from data source
        Comic comic = this.comics.get(pos);

        // populate view with model data

        ((ImageView) old_view.findViewById(R.id.comic_list_item_icon)).setImageBitmap(comic.icon);
        ((TextView) old_view.findViewById(R.id.comic_list_item_comic_title)).setText(comic.title);
        ((TextView) old_view.findViewById(R.id.comic_list_item_author_name)).setText(comic.author);
        ((ProgressBar) old_view.findViewById(R.id.comic_list_item_progress_bar)).setProgress(comic.progress);

        return old_view;
    }


}
