package mosis.comiccollector.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Function;

import mosis.comiccollector.R;

public class SortItemAdapter extends ArrayAdapter<SortDialog.Sort> {

    private List<SortDialog.Sort> sorts;
    private Context context;
    private int itemTemplate;

    private SortDialog.SortClickHandler clickHandler;

    public SortItemAdapter(@NonNull Context context,
                           int resource,
                           List<SortDialog.Sort> sorts,
                           SortDialog.SortClickHandler clickHandler) {
        super(context, resource);

        this.context = context;
        this.itemTemplate = resource;
        this.sorts = sorts;

        this.clickHandler = clickHandler;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = (LayoutInflater.from(this.context)).inflate(itemTemplate, parent, false);
        }

        ((Button) convertView.findViewById(R.id.sort_type_item_rb))
                .setText(sorts.get(position).getDisplayName());

        convertView.findViewById(R.id.sort_type_item_rb).setOnClickListener((View v) -> {
            clickHandler.handleSortPick(sorts.get(position));
        });

        return convertView;
    }

    public int getCount() {

        return this.sorts.size();
    }

    public SortDialog.Sort getItem(int position) {
        return this.sorts.get(position);
    }
}
