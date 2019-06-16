package mosis.comiccollector.activity;

import android.app.Dialog;
import android.content.Context;

import mosis.comiccollector.R;

public class LoadImageDialog extends Dialog {

    private Context context;

    public LoadImageDialog(Context context) {
        super(context);

        this.setContentView(R.layout.load_image_dialog);

        this.context = context;

    }


}
