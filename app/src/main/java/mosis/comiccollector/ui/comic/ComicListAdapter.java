package mosis.comiccollector.ui.comic;

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

public class ComicListAdapter extends ArrayAdapter<ViewComic> {

    private Context context;
    private int itemTemplate;
    private List<ViewComic> comics;

    public ComicListAdapter(Context context, int itemTemplate, List<ViewComic> comics) {
        super(context, itemTemplate, comics);

        this.context = context;
        this.itemTemplate = itemTemplate;
        this.comics = comics;

    }

    public View getView(int pos, View old_view, ViewGroup parent) {

        // reuse old view if possible
        if (old_view == null) {
            old_view = (LayoutInflater.from(this.context)).inflate(itemTemplate, parent, false);
        }

        // get comic sample from data source
        ViewComic comic = comics.get(pos);

        // populate view with model data

//        ((ImageView) old_view.findViewById(R.id.comic_list_item_icon)).setImageBitmap(comic.icon);
//        ((TextView) old_view.findViewById(R.id.comic_list_item_title)).setText(comic.title);
//        ((TextView) old_view.findViewById(R.id.comic_list_item_author)).setText(comic.authorId);
//        ((ProgressBar) old_view.findViewById(R.id.comic_list_item_progress)).setProgress(comic.progress);

        return old_view;
    }


}
