package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.comic.PreviewListAdapter;
import mosis.comiccollector.manager.AppManager;

public class ComicPreviewActivity extends AppCompatActivity {

    private RecyclerView preview_list;
    private PreviewListAdapter adapter;

    private Button close_btn;
    private Button save_pages_btn;
    private Button read_btn;
    private Button rate_btn;

    private Comic comic;
    private int comic_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_preview_dialog);

        this.extractData();
        this.initView();

    }

    private void extractData() {
        Intent intent = this.getIntent();

        this.comic_index = Integer.parseInt(intent.getExtras().get("comic_index").toString());

        this.comic = AppManager.getInstance().getStorage().getSingleComic(this.comic_index);


    }

    private void initView() {

        View layout = (View) this.findViewById(R.id.basic_preview);

        ((ImageView) layout.findViewById(R.id.comic_list_item_icon)).setImageBitmap(this.comic.icon);
        ((TextView) layout.findViewById(R.id.comic_list_item_title)).setText(this.comic.title);
        ((TextView) layout.findViewById(R.id.comic_list_item_author)).setText(this.comic.author);
        ((ProgressBar) layout.findViewById(R.id.comic_list_item_progress)).setProgress(this.comic.progress);

        this.adapter = new PreviewListAdapter(getApplicationContext(),
                                              R.layout.comic_preview_list_item,
                                              this.comic.sample_pages);

        this.preview_list = (RecyclerView) this.findViewById(R.id.preview_pages_list);
        this.preview_list.setAdapter(this.adapter);

        this.close_btn = (Button) this.findViewById(R.id.close_preview_btn);
        this.close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.save_pages_btn = (Button) this.findViewById(R.id.save_pages_btn);
        this.save_pages_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.read_btn = (Button) this.findViewById(R.id.read_preview_btn);
        this.read_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent read_intent = new Intent(ComicPreviewActivity.this, ReadActivity.class);

                read_intent.putExtra("comic_index", comic_index);

                startActivity(read_intent);

            }
        });

        this.rate_btn = (Button) this.findViewById(R.id.rate_button);
        this.rate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
