package mosis.comiccollector.ui.comic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.ImageWithId;

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
            this.page.setMaxHeight(120);
            this.page.setMaxWidth(80);
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<ImageWithId> pages;
    private int resource;
    private PreviewClickHandler clickHandler;

    public PreviewListAdapter(Context context,
                              int resource,
                              List<ImageWithId> pages,
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

        ImageWithId img = pages.get(i);
        if (img.hasBitmap()) {
            previewHolder.page.setImageBitmap(pages.get(i).getBitmap());
        } else {
            previewHolder.page.setImageURI(Uri.parse(pages.get(i).getUri()));
        }

        previewHolder.id = this.pages.get(i).getId();

        previewHolder.page.setOnClickListener((View v) -> {
            clickHandler.handleClick(previewHolder.id);
        });

    }

    @Override
    public int getItemCount() {
        return this.pages.size();
    }

    public void addItem(String id, Bitmap newBitmap) {
        this.pages.add(new ImageWithId(id, newBitmap));
        this.notifyItemInserted(pages.size() - 1);
    }

    public void addItem(String id, String uri) {
        this.pages.add(new ImageWithId(id, uri));
        this.notifyItemInserted(pages.size() - 1);
    }
}

