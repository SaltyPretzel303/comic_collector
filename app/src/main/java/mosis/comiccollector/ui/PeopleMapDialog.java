package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.user.ProfileData;
import mosis.comiccollector.ui.user.ViewUser;

public class PeopleMapDialog extends Dialog {

    private enum LoadStages {
        DataLoaded, ImageLoaded
    }

    private List<LoadStages> loadStages;

    private LiveData<ViewUser> liveUserData;

    private Context context;

    private Button openProfileButton;

    public PeopleMapDialog(@NonNull Context context, LiveData<ViewUser> liveData) {
        super(context);

        this.context = context;
        this.liveUserData = liveData;
        this.loadStages = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.people_map_dialog);

//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        this.openProfileButton = findViewById(R.id.open_profile_people_map);
        this.openProfileButton.setOnClickListener(this::buttonClick);
        this.openProfileButton.setEnabled(false);

        this.liveUserData.observe((LifecycleOwner) context, this::initViews);

    }

    private void initViews(ViewUser user) {
        this.loadStages.add(LoadStages.DataLoaded);

        user.liveLocalPicUri.observe((LifecycleOwner) context, this::setupProfilePic);

        ((TextView) findViewById(R.id.username_people_map)).setText(user.email);

        ((ProgressBar) findViewById(R.id.rating_people_map_pb)).setProgress(user.rating);
        ((TextView) findViewById(R.id.rating_people_map_tv)).setText(user.rating + "/100");

        if (this.loadStages.contains(LoadStages.DataLoaded)
                && this.loadStages.contains(LoadStages.ImageLoaded)) {

            this.openProfileButton.setEnabled(true);
        }

    }

    private void setupProfilePic(String uri) {
        this.loadStages.add(LoadStages.ImageLoaded);

        if (uri != null) {
            ((ImageView) findViewById(R.id.profile_pic_people_map)).setImageURI(Uri.parse(uri));
        }

        if (this.loadStages.contains(LoadStages.DataLoaded)
                && this.loadStages.contains(LoadStages.ImageLoaded)) {

            this.openProfileButton.setEnabled(true);
        }

    }

    private void buttonClick(View v) {
        ProfileData pData = new ProfileData();
        ViewUser vUser = this.liveUserData.getValue();
        pData.userId = vUser.userId;
        pData.email = vUser.email;
        pData.rating = vUser.rating;
        pData.picUri = vUser.liveLocalPicUri.getValue();

        Intent profileIntent = new Intent(context, ProfileActivity.class);
        profileIntent.putExtra(ProfileActivity.USER_DATA_EXTRA, pData);
        context.startActivity(profileIntent);
    }

}