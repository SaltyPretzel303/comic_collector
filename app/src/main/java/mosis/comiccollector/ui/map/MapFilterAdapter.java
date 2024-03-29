package mosis.comiccollector.ui.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.List;

import mosis.comiccollector.R;

public class MapFilterAdapter extends ArrayAdapter<MapFiltersDialog.BooleanFilter> {

    private Context context;
    private int resId;
    private List<MapFiltersDialog.BooleanFilter> booleanFilters;

    public MapFilterAdapter(@NonNull Context context,
                            int resource,
                            List<MapFiltersDialog.BooleanFilter> booleanFilters) {
        super(context, resource);

        this.context = context;
        this.resId = resource;

        this.booleanFilters = booleanFilters;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = (LayoutInflater.from(this.context))
                    .inflate(resId, parent, false);
        }

        CheckBox filterCb = convertView.findViewById(R.id.map_filter_item_cb);
        filterCb.setText(getFilter(position).getText());

        filterCb.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            getFilter(position).handleStateChange(isChecked);
        });

        filterCb.setChecked(getFilter(position).isActive());

        return convertView;
    }

    public int getCount() {
        return booleanFilters.size();
    }

    private MapFiltersDialog.BooleanFilter getFilter(int index) {
        return booleanFilters.get(index);
    }


}

