package mosis.comiccollector.ui.map;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import java.util.List;

import mosis.comiccollector.R;

public class MapFiltersDialog extends Dialog {

    public interface BooleanFilter {
        boolean isActive();

        String getText();

        void handleStateChange(boolean state);
    }

    public interface TextFilter {
        String getLastText();

        String getText();

        void handleText(String text);
    }

    public interface RatingFilter {
        float getLastRating();

        void handlerRating(float rating);
    }

    private Context context;

    private List<BooleanFilter> booleanFilters;

    private TextFilter textFilter;

    private RatingFilter ratingFilter;

    public MapFiltersDialog(@NonNull Context context,
                            List<BooleanFilter> booleanFilters,
                            TextFilter textFilter,
                            RatingFilter ratingFilter) {
        super(context);

        this.context = context;

        this.booleanFilters = booleanFilters;
        this.textFilter = textFilter;
        this.ratingFilter = ratingFilter;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_filter_dialog);

        ArrayAdapter<BooleanFilter> adapter = new MapFilterAdapter(
            context,
            R.layout.map_filter_item,
            booleanFilters);

        ((ListView) findViewById(R.id.map_filter_items_holder))
            .setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (textFilter != null) {

            ((EditText) findViewById(R.id.map_text_filter))
                .setText(textFilter.getLastText());

            ((EditText) findViewById(R.id.map_text_filter))
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (textFilter != null) {
                            textFilter.handleText(s.toString());
                        }
                    }
                });

        }

        if (ratingFilter != null) {
            ((RatingBar) findViewById(R.id.map_rating_filter))
                .setRating(ratingFilter.getLastRating());

            ((RatingBar) findViewById(R.id.map_rating_filter))
                .setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                    ratingFilter.handlerRating(rating);
                });
        }

    }
}
