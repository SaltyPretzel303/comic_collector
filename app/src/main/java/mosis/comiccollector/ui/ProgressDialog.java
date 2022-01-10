package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.health.ProcessHealthStats;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import mosis.comiccollector.R;

public class ProgressDialog extends Dialog implements ProgressDisplay {

    private int maxProgress;
    private int currentProgress;

    private ProgressBar progressBar;

    public ProgressDialog(@NonNull Context context, int maxProgress) {
        super(context);

        this.maxProgress = maxProgress;
        this.currentProgress = 0;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);

        setCanceledOnTouchOutside(false);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(this.maxProgress);
        progressBar.setProgress(this.currentProgress);
    }

    @Override
    public int getMaxValue() {
        return maxProgress;
    }

    @Override
    public int getProgress() {
        return currentProgress;
    }

    @Override
    public int addProgress(int progress) {
        progressBar.incrementProgressBy(progress);
        this.currentProgress += progress;
        return this.currentProgress;
    }

    @Override
    public boolean isDone() {
        return (currentProgress >= maxProgress);
    }
}
