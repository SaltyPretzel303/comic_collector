package mosis.comiccollector.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.viewmodel.ListViewModel;

public class ListItemsActivity extends AppCompatActivity {

    private static final int IMAGE_WIDTH = 100;
    private static final int IMAGE_HEIGHT = 100;

    public static final String LIST_TYPE_EXTRA = "list_type";
    public static final String TYPE_COMIC = "comic";
    public static final String TYPE_PEOPLE = "people";

    private ListViewModel viewModel;

    private ListAdapter adapter;

    private List<SortDialog.Sort> peopleSorts;
    private List<SortDialog.Sort> comicSorts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        this.viewModel = new ViewModelProvider(this).get(ListViewModel.class);

        Intent intent = getIntent();
        String listType = intent.getStringExtra(LIST_TYPE_EXTRA);
        if (listType.equals(TYPE_COMIC)) {
            initComicsAdapter();
            setupComicSorts();
            setupComicTextFilter();
        } else {
            initPeopleAdapter();
            setupPeopleSorts();
            setupPeopleTextFilter();
        }

    }

    private void initComicsAdapter() {

        var recView = (RecyclerView) findViewById(R.id.list_activity_items_holder);

        adapter = new ListAdapter(
            this,
            R.layout.list_item,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            new ListItemProvider() {
                @Override
                public ListItemData getItem(int index) {
                    var comic = viewModel.getComicAt(index);
                    if (comic != null) {
                        return new ListItemData(
                            comic.comicId,
                            comic.liveTitlePage,
                            comic.title,
                            comic.rating);
                    }
                    return null;
                }

                @Override
                public int getCount() {
                    return viewModel.getComicsCount();
                }
            },
            this::comicItemClickHandler);

        recView.setAdapter(adapter);

        viewModel
            .getUnknownComics()
            .observe(this, (comics) -> {
                adapter.notifyDataSetChanged();

                findViewById(R.id.list_activity_sorts_btn)
                    .setOnClickListener((v) -> {
                        Dialog sortDialog = new SortDialog(this, comicSorts);
                        sortDialog.show();
                    });
            });

    }

    private void initPeopleAdapter() {

        var recView = (RecyclerView) findViewById(R.id.list_activity_items_holder);

        adapter = new ListAdapter(
            this,
            R.layout.list_item,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            new ListItemProvider() {
                @Override
                public ListItemData getItem(int index) {
                    var user = viewModel.getUserAt(index);
                    if (user != null) {
                        return new ListItemData(
                            user.userId,
                            user.liveProfilePic,
                            user.name,
                            user.rating);
                    }
                    return null;
                }

                @Override
                public int getCount() {
                    return viewModel.getPeopleCount();
                }
            },
            this::personItemClickHandler);

        recView.setAdapter(adapter);

        viewModel
            .getUnknownPeople()
            .observe(this, (people) -> {
                adapter.notifyDataSetChanged();

                findViewById(R.id.list_activity_sorts_btn)
                    .setOnClickListener((v) -> {
                        Dialog sortDialog = new SortDialog(this, peopleSorts);
                        sortDialog.show();
                    });
            });

    }

    private void setupPeopleSorts() {
        peopleSorts = Arrays.asList(
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Name descending";
                }

                @Override
                public void performSort() {
                    viewModel.sortPeople((person1, person2) -> person1.name.compareTo(person2.name));
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Name ascending";

                }

                @Override
                public void performSort() {
                    viewModel.sortPeople((person1, person2) -> (person1.name.compareTo(person2.name)) * -1);
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Rating descending";

                }

                @Override
                public void performSort() {
                    viewModel.sortPeople((person1, person2) -> (int) (person1.rating - person2.rating) * -1);
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Rating ascending";

                }

                @Override
                public void performSort() {
                    viewModel.sortPeople((person1, person2) -> (int) (person1.rating - person2.rating));
                    adapter.notifyDataSetChanged();
                }
            }

        );
    }

    private void setupPeopleTextFilter() {
        ((EditText) findViewById(R.id.list_activity_text_filter))
            .addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    viewModel.setPeopleTextFilter(s.toString());
                }
            });
    }

    private void setupComicSorts() {
        comicSorts = Arrays.asList(
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Title descending";
                }

                @Override
                public void performSort() {
                    viewModel.sortComics((comic1, comic2) -> comic1.title.compareTo(comic2.title));
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Title ascending";
                }

                @Override
                public void performSort() {
                    viewModel.sortComics((comic1, comic2) -> comic1.title.compareTo(comic2.title) * -1);
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Rating descending";
                }

                @Override
                public void performSort() {
                    viewModel.sortComics((comic1, comic2) -> (int) (comic1.rating - comic2.rating) * -1);
                    adapter.notifyDataSetChanged();
                }
            },
            new SortDialog.Sort() {
                @Override
                public String getDisplayName() {
                    return "By Rating ascending";
                }

                @Override
                public void performSort() {
                    viewModel.sortComics((comic1, comic2) -> (int) (comic1.rating - comic2.rating));
                    adapter.notifyDataSetChanged();
                }
            }

        );
    }

    private void setupComicTextFilter() {
        ((EditText) findViewById(R.id.list_activity_text_filter))
            .addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    viewModel.setComicsTextFilter(s.toString());
                }
            });
    }

    private void comicItemClickHandler(String index) {

    }

    private void personItemClickHandler(String index) {

    }

}
