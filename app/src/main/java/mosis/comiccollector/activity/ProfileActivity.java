package mosis.comiccollector.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import mosis.comiccollector.R;

public class ProfileActivity extends AppCompatActivity {

    static private int LOAD_IMAGE_ACTIVITY = 123;

    private ImageView profil_pic_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        this.initView();

    }

    private void initView() {

        this.profil_pic_iv = (ImageView) this.findViewById(R.id.profil_pic_iv);
        this.profil_pic_iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.e("WorkspaceAct", "Image click ");

                Intent intent = new Intent(ProfileActivity.this, LoadImageActivity.class);

                startActivityForResult(intent, ProfileActivity.LOAD_IMAGE_ACTIVITY);

            }

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            // set new picture in previewImageView

        }

    }
}
