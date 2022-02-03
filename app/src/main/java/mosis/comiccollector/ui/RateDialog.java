package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import mosis.comiccollector.R;
import mosis.comiccollector.util.Toaster;

public class RateDialog extends Dialog {

    public interface FinishRate {
        void rate(float comicRate, float authorRate);
    }

    private Context context;
    private FinishRate onFinish;

    public RateDialog(@NonNull Context context, @NonNull FinishRate onRate) {
        super(context);

        this.context = context;
        this.onFinish = onRate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_dialog);

        findViewById(R.id.finish_rating_btn).setOnClickListener(this::finishRateClick);
    }

    private void finishRateClick(View v) {

        RatingBar comicBar = findViewById(R.id.rate_comic_rb);
        RatingBar authorBar = findViewById(R.id.rate_author_rb);

        if (comicBar.getRating() > 0 && authorBar.getRating() > 0) {
            onFinish.rate(comicBar.getRating(), authorBar.getRating());
            dismiss();
        } else {
            Toaster.makeToast(context, "Please rate first ... ");
        }

    }


}
