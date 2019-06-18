package mosis.comiccollector.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mosis.comiccollector.R;

public class LoadImageActivity extends Activity {


    static private int GALLERY_CODE = 123;

    private Button browse_btn;
    private Button choose_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.load_image_dialog);

        this.initView();

    }

    private void initView() {

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

    }


    private void showImagePicker() {

        Intent picker_intent = new Intent(Intent.ACTION_PICK);
        picker_intent.setType("image/*");

        startActivityForResult(picker_intent, LoadImageActivity.GALLERY_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            // change image preview

        }

    }

    private void chooseImage() {

        // upload image in firebase
        // save this image to the local storage
        // update image reference in user class

    }

}
