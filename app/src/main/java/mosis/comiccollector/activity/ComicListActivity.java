package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.comic.ComicListAdapter;
import mosis.comiccollector.manager.AppManager;
import mosis.comiccollector.manager.data.handler.DataRetrievedHandler;

public class ComicListActivity extends AppCompatActivity {

    private static int SORT_RESULT_CODE = 10;

    private List<Comic> comics;
    private ComicListAdapter adapter;
    private ListView list_view;

    private Button sort_button;

    private int selected;

    private ComicListContext list_context;
    // remove
    private Map<ComicListContext, ViewInitializer> view_initializers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_list);

//        this.populateInitializersMap();

        Intent intent = this.getIntent();
        this.list_context = ComicListContext.valueOf(intent.getStringExtra("list_context"));

        this.initView(this.list_context);
        this.initClickHandlers();

    }

    private void initView(ComicListContext list_context) {

        this.list_view = (ListView) findViewById(R.id.comics_container);
        this.sort_button = (Button) this.findViewById(R.id.sort_list_button);

        this.loadComics();
//        this.adapter = new ComicListAdapter(getApplicationContext(), R.layout.small_preview, this.comics);

//        this.list_view.setAdapter(this.adapter);

//        ViewInitializer initializer = this.view_initializers.get(list_context);
//        initializer.execute();

    }

    private void loadComics() {

        switch (this.list_context) {
            case CollectedComics:

                this.loadCollectedCommics();

                break;
            case DiscoverComics:

                this.loadDiscoverComics();

                break;
            case QueuedComics:

                this.loadQueuedComics();

                break;
            case MyComics:

                this.loadMyComics();

                break;
        }

    }

    // TODO overengineering, remove this method
    private void populateInitializersMap() {

        this.view_initializers = new HashMap<ComicListContext, ViewInitializer>();

        this.view_initializers.put(ComicListContext.CollectedComics, new ViewInitializer() {
            @Override
            public void execute() {

                sort_button = (Button) findViewById(R.id.sort_list_button);
                sort_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent sort_intent = new Intent(ComicListActivity.this, SortActivity.class);
                        startActivityForResult(sort_intent, ComicListActivity.SORT_RESULT_CODE);

                    }
                });

                list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent preview_intent = new Intent(ComicListActivity.this, FullPreviewActivity.class);

                        preview_intent.putExtra("comic_index", position);

                        startActivity(preview_intent);

                    }
                });

                loadCollectedCommics();

            }
        });

        // ----------------------------------

        this.view_initializers.put(ComicListContext.DiscoverComics, new ViewInitializer() {
            @Override
            public void execute() {

                sort_button = (Button) findViewById(R.id.sort_list_button);
                sort_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent sort_intent = new Intent(ComicListActivity.this, SortActivity.class);
                        startActivityForResult(sort_intent, ComicListActivity.SORT_RESULT_CODE);

                    }
                });

                list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent preview_intent = new Intent(ComicListActivity.this, FullPreviewActivity.class);

                        preview_intent.putExtra("comic_index", position);

                        startActivity(preview_intent);

                    }
                });

                loadDiscoverComics();
            }
        });

        this.view_initializers.put(ComicListContext.QueuedComics, new ViewInitializer() {
            @Override
            public void execute() {

                sort_button = (Button) findViewById(R.id.sort_list_button);
                sort_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent sort_intent = new Intent(ComicListActivity.this, SortActivity.class);
                        startActivityForResult(sort_intent, ComicListActivity.SORT_RESULT_CODE);

                    }
                });


            }
        });

    }

    private void loadCollectedCommics() {

        DataRetrievedHandler handler = new DataRetrievedHandler() {
            @Override
            public void onListRetrieved(List<Comic> retrieved_data) {

                comics = retrieved_data;
                adapter = new ComicListAdapter(getApplicationContext(), R.layout.small_preview, comics);
                list_view.setAdapter(adapter);

                adapter.notifyDataSetChanged();

                // cancel loading screen

            }

            @Override
            public void onComicRetrieved(int index, Comic comic) {

            }

        };

        boolean fetch_result = AppManager.getInstance().getStorage().fetchCollectedComics(0, handler);

        if (fetch_result == true) {
            // service available

            // TODO display some please wait message

        }


    }

    private void loadDiscoverComics() {

        DataRetrievedHandler handler = new DataRetrievedHandler() {
            @Override
            public void onListRetrieved(List<Comic> retrieved_data) {

                comics = retrieved_data;
                adapter = new ComicListAdapter(getApplicationContext(), R.layout.small_preview, comics);
                list_view.setAdapter(adapter);

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onComicRetrieved(int index, Comic comic) {

            }

        };

        boolean fetch_result = AppManager.getInstance().getStorage().fetchDiscoverComics(0, handler);

        if (fetch_result == true) {
            // service available

            // TODO display some please wait message

        }

    }

    // TODO implement
    private void loadQueuedComics() {
        this.loadCollectedCommics();
    }


    // TODO implement
    private void loadMyComics() {

    }

    private void initClickHandlers() {

        this.sort_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sort_intent = new Intent(ComicListActivity.this, SortActivity.class);

                sort_intent.putExtra("sort_context", String.valueOf(list_context));

                startActivityForResult(sort_intent, ComicListActivity.SORT_RESULT_CODE);

            }
        });

        this.list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent full_preview = new Intent(ComicListActivity.this, FullPreviewActivity.class);

                full_preview.putExtra("comic_index", position);
                full_preview.putExtra("preview_context", String.valueOf(list_context));

                startActivity(full_preview);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

}
