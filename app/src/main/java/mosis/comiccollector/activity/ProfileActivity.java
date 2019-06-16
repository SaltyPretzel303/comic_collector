package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import mosis.comiccollector.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profil_pic_iv;

    private LoadImageDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);


    }

    private void initView() {

        this.profil_pic_iv = (ImageView) this.findViewById(R.id.profil_pic_iv);
        this.profil_pic_iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog = new LoadImageDialog(getApplicationContext());

                dialog.show();

            }

        });


    }

}
