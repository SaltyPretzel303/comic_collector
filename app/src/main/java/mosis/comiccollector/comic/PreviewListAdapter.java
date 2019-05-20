package mosis.comiccollector.comic;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import mosis.comiccollector.R;

public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.PreviewHolder> {

    public static class PreviewHolder extends RecyclerView.ViewHolder {

        public ImageView page;

        public PreviewHolder(@NonNull View itemView) {
            super(itemView);

            this.page = (ImageView) itemView.findViewById(R.id.comic_preview_list_item_image);
        }

    }

    private Context context;
    private LayoutInflater inflater;
    private List<Bitmap> pages;
    private int resource;

    public PreviewListAdapter(Context context, int resource, List<Bitmap> pages_source) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.pages = pages_source;
        this.resource = resource;

    }

    @NonNull
    @Override
    public PreviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = this.inflater.inflate(this.resource, viewGroup, false);
        PreviewHolder holder = new PreviewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewHolder previewHolder, int i) {

        previewHolder.page.setImageBitmap(this.pages.get(i));

    }

    @Override
    public int getItemCount() {
        return this.pages.size();
    }


}
