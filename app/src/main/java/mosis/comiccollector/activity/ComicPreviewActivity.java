package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.manager.AppManager;

public class ComicPreviewActivity extends AppCompatActivity {

    private Comic comic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_preview_dialog);

        this.extractData();
        this.initView();

    }

    private void extractData() {
        Intent intent = this.getIntent();

        int comic_index = Integer.parseInt(intent.getExtras().get("comic_index").toString());

        // get comic from app manager


    }

    private void initView() {

    }

}
