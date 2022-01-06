package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.user.ViewUser;

public class PeopleMapDialog extends Dialog {

    private LiveData<ViewUser> liveUserData;

    private ImageView profilePicIv;
    private TextView usernameTv;
    private ProgressBar ratingPb;
    private TextView ratingTv;

    private Button profileButton;

    private Context context;

    public PeopleMapDialog(@NonNull Context context, LiveData<ViewUser> liveData) {
        super(context);

        this.context = context;

        this.liveUserData = liveData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.people_map_dialog);

//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.liveUserData.observe((LifecycleOwner) context, this::initViews);
    }

    private void initViews(ViewUser user) {
        user.liveLocalPicUri.observe((LifecycleOwner) context, this::setupProfilePic);

        ((TextView) findViewById(R.id.username_people_map)).setText(user.email);

        ((ProgressBar) findViewById(R.id.rating_people_map_pb)).setProgress(user.rating);
        ((TextView) findViewById(R.id.rating_people_map_tv)).setText(user.rating + "/100");

        ((Button) findViewById(R.id.open_profile_people_map)).setOnClickListener(
                (View v) -> {
                    // TODO open profile activity with user id in intent
                }
        );

    }

    private void setupProfilePic(String uri) {
        if (uri != null) {
            ((ImageView) findViewById(R.id.profile_pic_people_map)).setImageURI(Uri.parse(uri));
        }
    }

}
