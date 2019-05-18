package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.comic.Comic;
import mosis.comiccollector.comic.ComicListAdapter;

public class ComicListActivity extends AppCompatActivity {

    private static int SORT_RESULT_CODE = 10;

    private List<Comic> comics;
    private ComicListAdapter adapter;

    private ListView comics_list_view;

    private Button sort_button;

    private int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comic_list);

        this.loadCommics();
        this.initView();

    }

    private void loadCommics() {

        this.comics = new ArrayList<Comic>();


    }

    private void initView() {

        this.sort_button = (Button) this.findViewById(R.id.sort_list_button);

        this.sort_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sort_intent = new Intent(ComicListActivity.this, SortActivity.class);
                startActivityForResult(sort_intent, ComicListActivity.SORT_RESULT_CODE);

            }
        });

        this.comics_list_view = (ListView) this.findViewById(R.id.comics_container);

        for (int i = 0; i < 20; i++) {
            this.comics.add(new Comic());
        }

        this.adapter = new ComicListAdapter(getApplicationContext(), R.layout.comic_list_item, this.comics);
        this.comics_list_view.setAdapter(this.adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
