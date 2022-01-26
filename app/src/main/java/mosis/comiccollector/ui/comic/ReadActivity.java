package mosis.comiccollector.ui.comic;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.PreviewItemData;
import mosis.comiccollector.ui.PreviewItemProvider;
import mosis.comiccollector.ui.viewmodel.ReadViewModel;

public class ReadActivity extends AppCompatActivity {

    public static final String COMIC_EXTRA = "comic_to_read";
    public static final String COMIC_PAGE_EXTRA = "comic_page_to_read";

    private static final int PAGE_WIDTH = 400;
    private static final int PAGE_HEIDHT = 400;

    private ViewComic comic;

    private ReadViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_page);

        viewModel = new ViewModelProvider(this).get(ReadViewModel.class);
        viewModel.setComic((ViewComic) getIntent().getSerializableExtra(COMIC_EXTRA));

        setupPages(getIntent().getIntExtra(COMIC_PAGE_EXTRA, 0));

        // go fullscreen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void setupPages(int scroll) {
        RecyclerView rv = findViewById(R.id.read_comic_pages_holder);
        ReadComicAdapter adapter = new ReadComicAdapter(
                this,
                R.layout.read_page_item,
                PAGE_WIDTH,
                PAGE_HEIDHT,
                new PreviewItemProvider() {
                    @Override
                    public PreviewItemData getItem(int index) {
                        return new PreviewItemData(
                                "" + index,
                                "" + index,
                                "",
                                viewModel.getPage(
                                        index,
                                        PAGE_WIDTH,
                                        PAGE_HEIDHT)
                                        .livePage);
                    }

                    @Override
                    public int getItemsCount() {
                        return viewModel.getPagesCount();
                    }
                },
                this::pageClick);

        rv.setAdapter(adapter);
        rv.scrollToPosition(scroll);
        adapter.notifyDataSetChanged();
    }

    private void pageClick(String sIndex) {
        Log.e("readAct", "You clicked on page: " + sIndex);
    }

    private void enterFullscreenClick(View v) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // replace this handler with exit fullscreen handler
        v.setOnClickListener(this::leaveFullscreenClick);
    }

    private void leaveFullscreenClick(View v) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        // replace this handler with enter fullscreen handler
        v.setOnClickListener(this::enterFullscreenClick);
    }


}
