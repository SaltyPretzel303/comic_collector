package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.comic.PreviewListAdapter;
import mosis.comiccollector.manager.AppManager;

public class FullPreviewActivity extends AppCompatActivity {

    private RecyclerView preview_list;
    private PreviewListAdapter adapter;

    private Comic comic;
    private int comic_index;

    private ComicListContext preview_context;
    private Map<ComicListContext, ViewInitializer> controls_initializers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_preview);

        this.initControlsInitializers();

        Intent intent = this.getIntent();
        this.preview_context = ComicListContext.valueOf(intent.getStringExtra("preview_context"));

        ViewInitializer initializer = this.controls_initializers.get(this.preview_context);
        if (initializer != null) {
            initializer.execute();
        } else {
            Toast.makeText(getApplicationContext(), "View initializer is NULL", Toast.LENGTH_SHORT).show();
        }

        this.extractData();
        this.initBasicPreview();
        this.initPreviewPages();

    }

    private void initControlsInitializers() {

        this.controls_initializers = new HashMap<ComicListContext, ViewInitializer>();

        this.controls_initializers.put(ComicListContext.CollectedComics, new ViewInitializer() {
            @Override
            public void execute() {

                LinearLayout layout = (LinearLayout) findViewById(R.id.full_preview_controls_container);
                View controls = getLayoutInflater().inflate(R.layout.collected_preview_controls, layout);

                ((Button) controls.findViewById(R.id.collected_preview_close_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                ((Button) controls.findViewById(R.id.collected_preview_down_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AppManager.getInstance().getStorage().downloadCollectedPaged(comic_index);

                    }
                });

                ((Button) controls.findViewById(R.id.collected_preview_read_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent read_intent = new Intent(FullPreviewActivity.this, ReadActivity.class);

                        read_intent.putExtra("comic_index", comic_index);

                        startActivity(read_intent);

                    }
                });

                ((Button) controls.findViewById(R.id.collected_preview_rate_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getApplicationContext(), "Rate comic click ... ", Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });

        this.controls_initializers.put(ComicListContext.QueuedComics, new ViewInitializer() {
            @Override
            public void execute() {

                LinearLayout layout = (LinearLayout) findViewById(R.id.full_preview_controls_container);
                View controls = getLayoutInflater().inflate(R.layout.queued_preview_controls, layout);

                ((Button) controls.findViewById(R.id.queued_preview_remove_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getApplicationContext(), "Remove from queue ... ", Toast.LENGTH_SHORT).show();

                    }
                });

                ((Button) controls.findViewById(R.id.queued_preview_go_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getApplicationContext(), "Show map activity ... ", Toast.LENGTH_SHORT).show();

                    }
                });

                ((Button) controls.findViewById(R.id.queued_preview_read_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getApplicationContext(), "Read activity all over again ... ", Toast.LENGTH_SHORT)
                                .show();

                    }
                });

            }
        });

        this.controls_initializers.put(ComicListContext.DiscoverComics, new ViewInitializer() {
            @Override
            public void execute() {

            }
        });

    }

    private void initBasicPreview() {

        View layout = (View) this.findViewById(R.id.basic_preview);

        ((ImageView) layout.findViewById(R.id.comic_list_item_icon)).setImageBitmap(this.comic.icon);
        ((TextView) layout.findViewById(R.id.comic_list_item_title)).setText(this.comic.title);
        ((TextView) layout.findViewById(R.id.comic_list_item_author)).setText(this.comic.author);
        ((ProgressBar) layout.findViewById(R.id.comic_list_item_progress)).setProgress(this.comic.progress);

    }

    private void extractData() {

        Intent intent = this.getIntent();

        this.comic_index = Integer.parseInt(intent.getExtras().get("comic_index").toString());

        this.comic = AppManager.getInstance().getStorage().getComicAt(this.comic_index);


    }

    private void initPreviewPages() {

        this.adapter = new PreviewListAdapter(getApplicationContext(),
                                              R.layout.comic_preview_list_item,
                                              this.comic.sample_pages);

        this.preview_list = (RecyclerView) this.findViewById(R.id.preview_pages_list);
        this.preview_list.setAdapter(this.adapter);

    }

}
