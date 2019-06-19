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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.R;
import mosis.comiccollector.login.LoginResponseType;
import mosis.comiccollector.login.OnResponseAction;
import mosis.comiccollector.manager.AppManager;

public class LoadImageActivity extends Activity {


    static private int GALLERY_CODE = 123;

    static private int READ_EXTERNAL_STORAGE_REQ = 1001;
    static private int WRITE_EXTERNAL_STORAGE_REQ = 1002;

    private ImageView image_preview;

    private Button browse_btn;
    private Button choose_btn;

    private Uri image_uri;
    private Bitmap picCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.load_image_dialog);

        this.initView();

    }

    private void initView() {

        this.image_preview = (ImageView) this.findViewById(R.id.image_preview_iv);
        this.image_preview.setImageBitmap(AppManager.getInstance().getUsersManager().getCurrentUser().getProfPicBitmap());

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

//                testWrite();

//                testShow();

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
            // need permission

            Log.e("ProfilePic", "NeedPermission");
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

                Toast.makeText(MyApplication.getAppContext(), "Permission not granted ... ", Toast.LENGTH_SHORT).show();
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


        if (resultCode == Activity.RESULT_OK) {

            Log.e("ProfilePic", "Ok result ");

            this.image_uri = data.getData();
            String picPath = this.getPicRealPath(this.image_uri);

            this.picCache = BitmapFactory.decodeFile(picPath);

            this.image_preview.setImageBitmap(this.picCache);

            this.choose_btn.setEnabled(true);

        } else {
            Log.e("ProfilePic", "Not ok result ... ");
        }

    }

    private String getPicRealPath(Uri selected_image) {

        this.image_uri = selected_image;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selected_image, filePathColumn, null, null, null);
        ((Cursor) cursor).moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);

        return picturePath;

    }

    private void chooseImage() {

        // TODO show some kind of loading screen would be great

        // save this image to the local storage
        AppManager.getInstance().getUsersManager().saveUserProfilePic(this.picCache);

        // update image reference in user class
        AppManager.getInstance().getUsersManager().getCurrentUser().setProfPicBitmap(this.picCache);

        // TODO MOVE THIS METHOD TO THE DATA STORAGE
        // upload image in to the firebase
        AppManager.getInstance().getUsersManager().uploadProfilePic(this.image_uri, new OnResponseAction() {
            @Override
            public void execute(LoginResponseType response) {

                Toast.makeText(MyApplication.getAppContext(), "Profile picture uploaded ", Toast.LENGTH_SHORT).show();

                // TODO stop loading screen and finish this activity with ok result
                setResult(Activity.RESULT_OK);
                finish();

            }
        });

    }


}
