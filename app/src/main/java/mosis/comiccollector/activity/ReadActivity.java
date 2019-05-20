package mosis.comiccollector.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.manager.AppManager;

public class ReadActivity extends AppCompatActivity {

    private Comic comic;

    private ImageView image_view;
    private View options_container;

    private Button exit_btn;
    private Button fullscreen_btn;

    private Button continue_btn;

    private Button next_page_btn;
    private Button prev_page_btn;

    private Animation show_options_anim;
    private Animation hide_options_anim;


    private View.OnClickListener show_options_handler;
    private View.OnClickListener hide_options_handler;

    private View.OnClickListener enter_fullscreen_handler;
    private View.OnClickListener exit_fullscreen_handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_activity);


        this.extreactInfo();
        this.initView();

    }

    private void extreactInfo() {

        Intent data_intent = this.getIntent();
        int comic_index = Integer.parseInt(data_intent.getExtras().get("comic_index").toString());

        this.comic = AppManager.getInstance().getStorage().getSingleComic(comic_index);

    }

    private void initView() {

        this.image_view = (ImageView) this.findViewById(R.id.page_preview_imgv);
        this.options_container = this.findViewById(R.id.page_options_container);

        this.exit_btn = (Button) this.findViewById(R.id.exit_read_btn);

        this.next_page_btn = (Button) this.findViewById(R.id.next_page_btn);
        this.prev_page_btn = (Button) this.findViewById(R.id.preview_pages_list);

        this.continue_btn = (Button) this.findViewById(R.id.continue_reading);

        // SHOW HIDE OPTIONS CONTAINER

        this.options_container.setVisibility(View.VISIBLE);

        this.show_options_handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options_container.setVisibility(View.VISIBLE);

//                    options_container.startAnimation(show_options_anim);

                // replace this handler with hide options handler
                image_view.setOnClickListener(hide_options_handler);

            }
        };

        this.hide_options_handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options_container.setVisibility(View.INVISIBLE);
//                    options_container.startAnimation(hide_options_anim);

                // replace this handler with show options handler
                image_view.setOnClickListener(show_options_handler);
            }
        };

        this.image_view.setOnClickListener(this.hide_options_handler);

        // FULLSCREEN BUTTON

        this.fullscreen_btn = (Button) this.findViewById(R.id.fullscreen_read_btn);

        this.enter_fullscreen_handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                     WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // replace this handler with exit fullscreen handler
                fullscreen_btn.setOnClickListener(exit_fullscreen_handler);

            }
        };

        this.exit_fullscreen_handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                     WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

                // replace this handler with enter fullscreen handler
                fullscreen_btn.setOnClickListener(enter_fullscreen_handler);

            }
        };

        this.fullscreen_btn.setOnClickListener(this.enter_fullscreen_handler);

        // OPTIONS CONTAINER ANIMATIONS

        this.show_options_anim = new TranslateAnimation(0, this.options_container.getWidth(), 0, 0);
        this.show_options_anim.setDuration(500);
        this.show_options_anim.setFillAfter(true);

        this.hide_options_anim = new TranslateAnimation(0, this.options_container.getWidth(), 0, 0);
        this.hide_options_anim.setDuration(500);
//        this.hide_options_anim.setFillAfter(true);

    }

}
