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

import mosis.comiccollector.MyApplication;
import mosis.comiccollector.R;
import mosis.comiccollector.manager.handler.JobDoneHandler;
import mosis.comiccollector.manager.AppManager;

import mosis.comiccollector.storage.model.User;

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

    @Override
    protected void onStart() {
        super.onStart();

        MyApplication.getInstance().registerActivityContext(LoadImageActivity.this);

    }

    private void initView() {

        this.image_preview = (ImageView) this.findViewById(R.id.image_preview_iv);
        User user = AppManager.getInstance().getUsersManager().getCurrentUser();
        if (user.hasProfilePic()) {
            this.image_preview.setImageBitmap(user.getProfPicBitmap());
        }
        // else default pic

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

                Toast.makeText(MyApplication.getInstance().getActivityContext(), "Permission not granted ... ", Toast.LENGTH_SHORT).show();
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

            Log.w("ProfilePic", "Ok result ");

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

        AppManager.getInstance().showLoadingScreen();

        AppManager.getInstance().getUsersManager().updateUserProfilePic(this.image_uri, this.picCache, new JobDoneHandler() {
            @Override
            public void execute(String message) {

                AppManager.getInstance().hideLoadingScreen();

                Toast.makeText(MyApplication.getInstance().getActivityContext(), "Profile pic updated", Toast.LENGTH_SHORT).show();

                setResult(Activity.RESULT_OK);

                finish();

            }
        });

    }


}
