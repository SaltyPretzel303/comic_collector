package mosis.comiccollector.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import mosis.comiccollector.R;
import mosis.comiccollector.manager.AppManager;

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

                Intent intent = new Intent(ProfileActivity.this, LoadImageActivity.class);

                startActivityForResult(intent, ProfileActivity.LOAD_IMAGE_ACTIVITY);

            }

        });

        this.profil_pic_iv.setImageBitmap(AppManager.getInstance().getUsersManager().getCurrentUser().getProfPicBitmap());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Log.e("ProfilePic", "Result ok ... ");

            Bitmap pic = AppManager.getInstance().getUsersManager().getCurrentUser().getProfPicBitmap();
            this.profil_pic_iv.setImageBitmap(pic);

        } else {

            Log.e("ProfilePic", "Not ok result ... ");

        }

    }
}
