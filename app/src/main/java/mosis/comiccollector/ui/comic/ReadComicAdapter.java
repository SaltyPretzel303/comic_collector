package mosis.comiccollector.ui.comic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.PreviewItemData;
import mosis.comiccollector.ui.PreviewItemProvider;
import mosis.comiccollector.util.Units;

public class ReadComicAdapter extends RecyclerView.Adapter<ReadComicAdapter.PreviewHolder> {

    public static class PreviewHolder extends RecyclerView.ViewHolder {

        public String id;
        public ImageView pageView;

        public PreviewHolder(@NonNull View itemView) {
            super(itemView);

            this.pageView = itemView.findViewById(R.id.read_page_item_iv);
        }
    }

    private final Context context;
    private LayoutInflater inflater;

    private final int resId;
    private final int itemWidth;
    private final int itemHeight;
    private final PreviewClickHandler clickHandler;

    private final PreviewItemProvider itemProvider;

    public ReadComicAdapter(
            Context context,
            int resId,
            int itemWidth,
            int itemHeight,
            PreviewItemProvider dataProvider,
            @NotNull PreviewClickHandler clickHandler) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.resId = resId;
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.clickHandler = clickHandler;

        this.itemProvider = dataProvider;

    }

    @NonNull
    @Override
    public PreviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewHolder(inflater.inflate(resId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewHolder holder, int position) {
        holder.pageView.setMaxWidth(Units.dpToPx(context, itemWidth));
        holder.pageView.setMaxHeight(Units.dpToPx(context, itemHeight));

        PreviewItemData data = itemProvider.getItem(position);
        if (data == null) {
            return;
        }

        holder.id = data.getId();

        data.getBitmap().observe((LifecycleOwner) context, (bitmap) -> {
            holder.pageView.setImageBitmap(bitmap);
        });

        holder.pageView.setOnClickListener((v) -> {
            clickHandler.handleClick(holder.id);
        });

    }

    @Override
    public int getItemCount() {
        return itemProvider.getItemsCount();
    }


}
