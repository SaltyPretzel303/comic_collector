package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import mosis.comiccollector.R;

// TODO remove, this one won't be used I think
// is kinda brokern
public class LoadingScreenDialog extends Dialog {

    private Handler handler;

    private Date showDate;

    private TextView messageTv;

    private final static long MIN_DIFF = 500;

    // this is kinda broken (not able to display message, showDialog is called before onCreate)
    // should not be used (even android doesn't suggest this approach)
    public LoadingScreenDialog(Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.loading_screen);

        this.setCanceledOnTouchOutside(false);

        this.messageTv = this.findViewById(R.id.loading_message_tv);

        this.handler = new Handler();

        // has to be done this way ... no workaround
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog(String message) {
        if (this.isShowing()) {
            Log.e("LoadingDialog", "show: ALREADY SHOWING DIALOG ");
            return;
        }

        if (this.showDate == null) {

            Log.e("LoadingDialog", "show: SHOW LOADING DIALOG ");
            this.showDate = Calendar.getInstance().getTime();
//            this.messageTv.setText(message);
            super.show();

        } else {

            Log.e("LoadingScreen", "show: Dialog already shown ... ");

        }

    }

    public void hideDialog() {

        Date now = Calendar.getInstance().getTime();
        if (this.showDate != null) {

            long diff = Math.abs(now.getTime() - this.showDate.getTime());

            if (diff < MIN_DIFF) {
                Log.w("LoadingScreen", "hide: Waiting for min diff ");

                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        normalHide();

                    }
                }, this.MIN_DIFF - diff);

            } else {

                normalHide();

            }

        } else {

            Log.e("Loading dialog", "hide: Dialog is not shown");

        }

    }

    @Override
    public void onBackPressed() {
        this.showDate = null;
        super.onBackPressed();
    }

    private void normalHide() {

        Log.e("LoadingScreen", "normalHide: Hide loading screen");

        this.showDate = null;
        cancel();

    }

}
