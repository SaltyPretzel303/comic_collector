package mosis.comiccollector.ui.comic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.UriWithId;

// used with recycler view
// TODO should be moved outside comic package
// since/if it is gonna be used for friends profile pics preview
public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.PreviewHolder> {

    public static class PreviewHolder extends RecyclerView.ViewHolder {

        // in case of comics title page preview, this is comic id
        // in case of user profile picture preview this is user id
        public String id;
        public ImageView page;

        public PreviewHolder(@NonNull View itemView) {
            super(itemView);

            this.page = (ImageView) itemView.findViewById(R.id.comic_preview_list_item_image);
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<UriWithId> pages;
    private int resource;
    private PreviewClickHandler clickHandler;

    public PreviewListAdapter(Context context,
                              int resource,
                              List<UriWithId> pages,
                              @NotNull PreviewClickHandler clickHandler) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.pages = pages;
        this.resource = resource;
        this.clickHandler = clickHandler;
    }

    public PreviewListAdapter(Context context,
                              int resource,
                              @NotNull PreviewClickHandler clickHandler) {

        this(context, resource, new ArrayList<>(), clickHandler);

    }

    @NonNull
    @Override
    public PreviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = this.inflater.inflate(this.resource, viewGroup, false);
        return new PreviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewHolder previewHolder, int i) {

        previewHolder.page.setImageURI(Uri.parse(this.pages.get(i).uri));
        previewHolder.id = this.pages.get(i).id;

        previewHolder.page.setOnClickListener((View v) -> {
            clickHandler.handleClick(previewHolder.id);
        });

    }

    @Override
    public int getItemCount() {
        return this.pages.size();
    }

    public void addItem(String id, String newUri) {
        this.pages.add(new UriWithId(id, newUri));
        this.notifyItemInserted(pages.size() - 1);
    }


}
