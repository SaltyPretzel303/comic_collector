package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mosis.comiccollector.R;

public class ImagePreviewWithActionsDialog extends Dialog {

    public interface Action {
        String getText();

        void handlePress(View v);
    }


    public static final int PAGE_WIDTH = 400;
    public static final int PAGE_HEIGHT = 400;

    private Context context;

    private Bitmap image;
    private List<Action> actions;

    public ImagePreviewWithActionsDialog(
            @NonNull Context context,
            Bitmap image,
            List<Action> actions) {
        super(context);

        this.context = context;

        this.image = image;
        this.actions = actions;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_preview_with_actions);

        ((ImageView) findViewById(R.id.image_preview_iv)).setImageBitmap(this.image);

        if (actions == null || actions.size() == 0) {
            findViewById(R.id.buttons_holder).setVisibility(View.GONE);
            return;
        }

        LinearLayout holder = findViewById(R.id.buttons_holder);
        for (Action action : this.actions) {

            // TODO this should be custom inflated view/button
            Button newButton = new Button(context);

            newButton.setText(action.getText());
            newButton.setOnClickListener(action::handlePress);

            holder.addView(newButton);
        }

    }

}
