package mosis.comiccollector.ui;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.repository.ComicRepository;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.comic.ComicListAdapter;
import mosis.comiccollector.util.DepProvider;

public class ComicListActivity extends AppCompatActivity {

    private static int SORT_RESULT_CODE = 10;

    private List<ViewComic> comics;
    private ComicListAdapter adapter;
    private ListView list_view;

    private Button sort_button;

    private int selected;

    private ComicListContext list_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_list);

        Intent intent = this.getIntent();
        this.list_context = ComicListContext.valueOf(intent.getStringExtra("list_context"));

        this.initView(this.list_context);
        this.initClickHandlers();

    }

    private void initView(ComicListContext list_context) {

        this.list_view = (ListView) findViewById(R.id.comics_container);
        this.sort_button = (Button) this.findViewById(R.id.sort_list_button);

        this.loadComics();

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

    private void loadCollectedCommics() {

//        ComicRepository.ComicsHandler handler = new ComicRepository.ComicsHandler() {
//            @Override
//            public void handleComics(List<Comic> retrieved_data) {
//
//                comics = retrieved_data;
//                adapter = new ComicListAdapter(getApplicationContext(), R.layout.small_preview, comics);
//                list_view.setAdapter(adapter);
//
//                adapter.notifyDataSetChanged();
//
//                // cancel loading screen
//
//            }
//
//        };
//
//        boolean fetch_result = DepProvider
//                .getComicRepository()
//                .fetchCollectedComics(0, handler);
//
//        if (fetch_result == true) {
//            // service available
//
//            // TODO display some please wait message
//
//        }


    }

    private void loadDiscoverComics() {

        // replace with ComicRepository.ComicsHandler

//        DataRetrievedHandler handler = new DataRetrievedHandler() {
//            @Override
//            public void onListRetrieved(List<ViewComic> retrieved_data) {
//
//                comics = retrieved_data;
//                adapter = new ComicListAdapter(getApplicationContext(), R.layout.small_preview, comics);
//                list_view.setAdapter(adapter);
//
//                adapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onComicRetrieved(int index, ViewComic comic) {
//
//            }
//
//        };
//
//        boolean fetch_result = DepProvider
//                .getComicRepository()
//                .fetchDiscoverComics(0, handler);
//
//        if (fetch_result == true) {
//            // service available
//
//            // TODO display some please wait message
//
//        }

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
