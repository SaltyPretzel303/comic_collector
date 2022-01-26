package mosis.comiccollector.ui.comic;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.PreviewItemData;
import mosis.comiccollector.ui.PreviewItemProvider;
import mosis.comiccollector.util.Units;

// used with recycler view
// TODO should be moved outside comic package
// since/if it is gonna be used for friends profile pics preview
public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.PreviewHolder> {

    public static class PreviewHolder extends RecyclerView.ViewHolder {

        // in case of comics title page preview, this is comic id
        // in case of user profile picture preview this is user id
        public String id;

        public View textHolder;

        public TextView upperText;
        public TextView lowerText;

        public ImageView page;
//        public View button;

        public PreviewHolder(@NonNull View itemView) {
            super(itemView);

            this.upperText = itemView.findViewById(R.id.item_upper_text_content);
            this.lowerText = itemView.findViewById(R.id.item_lower_text_content);

            this.page = itemView.findViewById(R.id.comic_preview_list_item_image);

//            this.button = itemView.findViewById(R.id.preview_item_button);

            this.textHolder = itemView.findViewById(R.id.preview_item_text_holder);

        }

        public void showPage() {
            this.textHolder.setVisibility(View.GONE);
            this.page.setVisibility(View.VISIBLE);
        }

        public void showText() {
            this.textHolder.setVisibility(View.VISIBLE);
            this.page.setVisibility(View.GONE);
        }


    }

    private final Context context;
    private final LayoutInflater inflater;
    private final int resource;
    private final int maxWidth;
    private final int maxHeight;

    private final PreviewClickHandler clickHandler;

    private final PreviewItemProvider itemProvider;

    public PreviewListAdapter(Context context,
                              int resource,
                              int maxWidth,
                              int maxHeight,
                              PreviewItemProvider dataProvider,
                              @NotNull PreviewClickHandler clickHandler) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemProvider = dataProvider;
        this.resource = resource;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public PreviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = this.inflater.inflate(this.resource, viewGroup, false);
        return new PreviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewHolder previewHolder, int index) {

        previewHolder.page.setMaxWidth(Units.dpToPx(this.context, this.maxWidth));
        previewHolder.page.setMaxHeight(Units.dpToPx(this.context, this.maxHeight));

        PreviewItemData data = this.itemProvider.getItem(index);
        if (data == null) {
            Log.e("previewAdapter", "Preview holder got null as data at: " + index);
            return;
        }

        previewHolder.showText();

        data.getBitmap().observe((LifecycleOwner) context, (Bitmap bitmap) -> {
            previewHolder.page.setImageBitmap(bitmap);
            previewHolder.showPage();

        });

        previewHolder.upperText.setText(data.getUpperText());
        previewHolder.id = data.getId();

        previewHolder.page.setOnClickListener((View v) -> {
            clickHandler.handleClick(previewHolder.id);
        });

    }

    @Override
    public int getItemCount() {
        return itemProvider.getItemsCount();
    }

    public int getMaxWidth() {
        return this.maxWidth;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

}

