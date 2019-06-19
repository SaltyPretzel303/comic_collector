package mosis.comiccollector.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;

import mosis.comiccollector.R;
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.storage.DataStorage;

public class LoadImageActivity extends Activity {


    static private int GALLERY_CODE = 123;

    static private int READ_EXTERNAL_STORAGE_REQ = 1001;
    static private int WRITE_EXTERNAL_STORAGE_REQ = 1002;

    private ImageView image_preview;

    private Button browse_btn;
    private Button choose_btn;

    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.load_image_dialog);

        this.initView();

    }

    private void initView() {

        this.image_preview = (ImageView) this.findViewById(R.id.image_preview_iv);

        this.browse_btn = (Button) this.findViewById(R.id.load_image_browse_btn);
        this.choose_btn = (Button) this.findViewById(R.id.load_image_choose_btn);

        this.browse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        this.choose_btn = (Button) this.findViewById(R.id.load_image_choose_btn);
        this.choose_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        this.choose_btn.setEnabled(false);

    }


    private void showImagePicker() {

        // Assume this Activity is the current activity
        int permission_result = (int) ContextCompat.checkSelfPermission(LoadImageActivity.this,
                                                                        Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission_result != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoadImageActivity.this,
                                              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                              LoadImageActivity.READ_EXTERNAL_STORAGE_REQ);

        } else {
            // has permission


            Intent picker_intent = new Intent(Intent.ACTION_PICK);
            picker_intent.setType("image/*");

            startActivityForResult(picker_intent, LoadImageActivity.GALLERY_CODE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {

        if (requestCode == LoadImageActivity.READ_EXTERNAL_STORAGE_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission granted
                this.showImagePicker();

            } else {

                // permission not granted
                // close change image dialog
                finish();
            }

        } else if (requestCode == LoadImageActivity.WRITE_EXTERNAL_STORAGE_REQ) {


        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // change image preview
        Log.e("ProfilePic", "Result ");

        if (resultCode == Activity.RESULT_OK) {

            Log.e("ProfilePic", "Ok result ");

            Uri selected_image = data.getData();
            this.image_uri = selected_image;

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selected_image, filePathColumn, null, null, null);
            ((Cursor) cursor).moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);

            cursor.close();


            Bitmap b_map = BitmapFactory.decodeFile(picturePath);
            this.image_preview.setImageBitmap(b_map);

            this.choose_btn.setEnabled(true);

        } else {
            Log.e("ProfilePic", "Not ok result ... ");
        }

    }

    private void chooseImage() {

        // upload image in to the firebase
        StorageReference storage = FirebaseStorage.getInstance().getReference("profile_pics");

        String username = AppManager.getInstance().getUsersManager().getCurrentUser().getUsername();
        String name = username + "-profile_pic";

        StorageReference child_ref = storage.child(name);
        child_ref.putFile(image_uri);


        // save this image to the local storage
        // update image reference in user class

    }


}
