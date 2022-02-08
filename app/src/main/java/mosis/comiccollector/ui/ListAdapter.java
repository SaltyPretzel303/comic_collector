package mosis.comiccollector.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.comic.PreviewListAdapter;
import mosis.comiccollector.util.Units;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListItemHolder> {

    public static class ListItemHolder extends RecyclerView.ViewHolder {

        public String id;

        public View root;

        public ImageView imageView;
        public TextView textView;
        public RatingBar ratingBar;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            this.root = itemView.findViewById(R.id.list_item_root);

            this.imageView = itemView.findViewById(R.id.list_item_image);
            this.textView = itemView.findViewById(R.id.list_item_text);
            this.ratingBar = itemView.findViewById(R.id.list_item_rating);

        }

    }

    private Context context;
    private final LayoutInflater inflater;
    private int res;

    private int width;
    private int height;

    private ListItemProvider itemProvider;
    private ListItemClickHandler clickHandler;

    public ListAdapter(Context context,
                       int itemTemplate,
                       int width,
                       int height,
                       ListItemProvider itemProvider,
                       ListItemClickHandler clickHandler) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.res = itemTemplate;

        this.width = width;
        this.height = height;

        this.itemProvider = itemProvider;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ListAdapter.ListItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = this.inflater.inflate(res, viewGroup, false);
        return new ListAdapter.ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ListItemHolder previewHolder, int index) {

        previewHolder.imageView.setMaxWidth(Units.dpToPx(context, width));
        previewHolder.imageView.setMaxHeight(Units.dpToPx(context, height));

        ListItemData data = itemProvider.getItem(index);
        if (data == null) {
            Log.e("previewAdapter", "Preview holder got null as data at: " + index);
            return;
        }

        previewHolder.id = data.id;

        data.liveImage.observe((LifecycleOwner) context, (Bitmap bitmap) -> {
            previewHolder.imageView.setImageBitmap(bitmap);
        });

        previewHolder.textView.setText(data.text);
        previewHolder.ratingBar.setRating(data.rating);

        previewHolder.root.setOnClickListener((v) -> {
            clickHandler.handleClick(previewHolder.id);
        });

    }

    @Override
    public int getItemCount() {
        return itemProvider.getCount();
    }

}
