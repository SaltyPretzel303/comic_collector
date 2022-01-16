package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.util.List;

import mosis.comiccollector.R;

public class SortDialog extends Dialog {

    public interface Sort {
        String getDisplayName();

        void performSort();
    }

    public interface SortClickHandler {
        void handleSortPick(Sort pickedSort);
    }

    private Context context;
    private SortItemAdapter adapter;
    private List<Sort> sorts;

    public SortDialog(@NonNull Context context, List<Sort> sorts) {
        super(context);

        this.context = context;
        this.sorts = sorts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sort_dialog);

        adapter = new SortItemAdapter(
                context,
                R.layout.sort_type_item,
                sorts,
                (Sort sort) -> {
                    sort.performSort();

                    dismiss();
                });
        ((ListView) findViewById(R.id.sort_types_holder)).setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}

