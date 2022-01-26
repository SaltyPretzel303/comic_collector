package mosis.comiccollector.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.util.List;

import mosis.comiccollector.R;

public class MapFiltersDialog extends Dialog {

    public interface Filter {

        boolean isActive();

        String getText();

        void handleStateChange(boolean state);
    }

    private Context context;

    private List<Filter> filters;

    public MapFiltersDialog(@NonNull Context context, List<Filter> filters) {
        super(context);

        this.context = context;
        this.filters = filters;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_filter_dialog);

        ArrayAdapter<Filter> adapter = new MapFilterAdapter(
                context,
                R.layout.map_filter_item,
                filters);

        ((ListView) findViewById(R.id.map_filter_items_holder))
                .setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }
}
