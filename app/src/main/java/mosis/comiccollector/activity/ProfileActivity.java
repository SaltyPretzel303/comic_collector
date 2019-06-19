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

        Log.e("ProfilePic", "Result ");

        if (resultCode == Activity.RESULT_OK) {

            Log.e("ProfilePic", "Ok result ");

            Uri selected_image = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selected_image, filePathColumn, null, null, null);
            ((Cursor) cursor).moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);

            cursor.close();


            Bitmap b_map = BitmapFactory.decodeFile(picturePath);
            this.profil_pic_iv.setImageBitmap(b_map);
        }else{
            Log.e("ProfilePic", "Not ok result ... ");
        }

    }
}
