package mosis.comiccollector.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;

import mosis.comiccollector.R;

public class LoadingScreen extends Dialog {

    private Handler handler;

    private Date show_date;

    private long min_diff = 500;

    public LoadingScreen(Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.loading_screen);

        this.setCanceledOnTouchOutside(false);

        this.handler = new Handler();

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    @Override
    public void show() {

        if (this.isShowing()) {
            Log.e("LoadingDialog", "show: ALREADY SHOWING DIALOG ");
            return;
        }

        if (this.show_date == null) {

            Log.e("LoadingDialog", "show: SHOW LOADING DIALOG ");
            this.show_date = Calendar.getInstance().getTime();
            super.show();

        } else {

            Log.e("LoadingScreen", "show: Dialog already shown ... ");

        }


    }

    @Override
    public void hide() {

        Date now = Calendar.getInstance().getTime();
        if (this.show_date != null) {

            long diff = Math.abs(now.getTime() - this.show_date.getTime());

            if (diff < min_diff) {
                Log.w("LoadingScreen", "hide: Waiting for min diff ");

                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        normalHide();

                    }
                }, this.min_diff - diff);

            } else {

                normalHide();

            }

        } else {

            Log.e("Loading dialog", "hide: Dialog is not shown");

        }

    }

    @Override
    public void onBackPressed() {
        this.show_date = null;
        super.onBackPressed();
    }

    private void normalHide() {

        Log.e("LoadingScreen", "normalHide: Hide loading screen");

        this.show_date = null;
        cancel();

    }


}
