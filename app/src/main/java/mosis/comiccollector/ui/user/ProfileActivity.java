package mosis.comiccollector.ui.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.PreviewItemData;
import mosis.comiccollector.ui.PreviewItemProvider;
import mosis.comiccollector.ui.SortDialog;
import mosis.comiccollector.ui.comic.PreviewListAdapter;
import mosis.comiccollector.ui.comic.ComicPreviewAndEditActivity;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ShortProfileDialog;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.UserProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    // TODO extract next values to resources
    // and adjust them as well ...
    public static final int PREVIEW_PAGE_WIDTH = 150;
    public static final int PREVIEW_PAGE_HEIGHT = 120;

    public static final int PROFILE_PIC_WIDTH = 200;
    public static final int PROFILE_PIC_HEIGHT = 200;

    public static final String USER_DATA_EXTRA = "user_data";

    private PreviewListAdapter createdAdapter;
    private PreviewListAdapter collectedAdapter;
    private PreviewListAdapter friendsAdapter;

    private ActivityResultLauncher<String> loadPicActivityLauncher;

    private UserProfileViewModel viewModel;
    private String myId;

    private MutableLiveData<ViewUser> liveUser;

    private String createdSort;
    private List<SortDialog.Sort> createdSorts;

    private String collectedSort;
    private List<SortDialog.Sort> collectedSorts;

    private String friendsSort;
    private List<SortDialog.Sort> friendsSorts;

    private Dialog sortDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        this.viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        this.myId = this.viewModel.getMyId();

        this.initLoadPictureActivityLauncher();

        Intent intent = getIntent();
        if (intent.hasExtra(USER_DATA_EXTRA)) {

            ViewUser vData = (ViewUser) intent.getSerializableExtra(USER_DATA_EXTRA);
            this.liveUser = viewModel.getUser(
                    vData.userId,
                    PROFILE_PIC_WIDTH,
                    PROFILE_PIC_HEIGHT);
        } else {
            this.liveUser = viewModel.getUser(
                    this.myId,
                    PROFILE_PIC_WIDTH,
                    PROFILE_PIC_HEIGHT);
        }

        this.initView();
    }

    private void initLoadPictureActivityLauncher() {
        this.loadPicActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {
                    if (uri == null) {
                        return;
                    }
                    Log.e("Pic uri", "We got this pic uri: " + uri.toString());

                    ((ImageView) findViewById(R.id.profile_pic_iv)).setImageURI(uri);

                    viewModel.saveProfilePic(uri.toString());

                }
        );
    }

    private void initView() {
        this.liveUser.observe(this, (ViewUser user) -> {

            ((TextView) this.findViewById(R.id.profile_username_tv))
                    .setText(user.name);
            ((ProgressBar) this.findViewById(R.id.user_rating_pb))
                    .setProgress(user.rating);
            ((TextView) this.findViewById(R.id.user_rating_tv))
                    .setText(user.rating + "/100");

            // default profile pic
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_pic);
            RoundedBitmapDrawable roundedBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(), defaultBitmap);
            roundedBitmapDrawable.setCircular(true);
            ((ImageView) findViewById(R.id.profile_pic_iv))
                    .setImageDrawable(roundedBitmapDrawable);

            user.liveProfilePic.observe(this, (Bitmap bitmap) -> {
                if (bitmap != null) {

                    // https://stackoverflow.com/questions/31675420/set-round-corner-image-in-imageview
                    ImageView imageView = findViewById(R.id.profile_pic_iv);
                    RoundedBitmapDrawable rbd =
                            RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    rbd.setCircular(true);
                    imageView.setImageDrawable(rbd);

//                    ((ImageView) this.findViewById(R.id.profile_pic_iv))
//                            .setImageBitmap(bitmap);
                } else {
                    Log.e("imageView", "Image bitmap is null ... ");
                }
            });

            if (user.userId.equals(myId)) {
                // if displaying my acc

                this.findViewById(R.id.profile_pic_iv).setOnClickListener((View v) -> {
                    this.loadPicActivityLauncher.launch("image/*");
                });

                findViewById(R.id.add_comic_button).setVisibility(View.VISIBLE);
                findViewById(R.id.add_comic_button).setOnClickListener(this::addComicClick);

                findViewById(R.id.discover_comic_button).setVisibility(View.VISIBLE);
                findViewById(R.id.discover_comic_button).setOnClickListener(this::discoverComicClick);

                findViewById(R.id.find_friends_button).setVisibility(View.VISIBLE);
                findViewById(R.id.find_friends_button).setOnClickListener(this::findFriendsClick);

            } else {

                findViewById(R.id.add_comic_button).setVisibility(View.GONE);
                findViewById(R.id.discover_comic_button).setVisibility(View.GONE);
                findViewById(R.id.find_friends_button).setVisibility(View.GONE);

            }

            loadCreatedComics(user.userId);
            loadCollectedComics(user.userId);
            loadFriends(user.userId);

        });

        initCreatedSorts();
        initCollectedSorts();
        initFriendsSorts();

    }

    // region init sorts

    private void initCreatedSorts() {
        this.createdSorts = Arrays.asList(
                new SortDialog.Sort() {
                    @Override
                    public String getDisplayName() {
                        return "By Title descending";

                    }

                    @Override
                    public void performSort() {
                        createdSort = getDisplayName();
                        viewModel.sortCreatedComics((comic1, comic2) -> {
                            return comic1.title.compareTo(comic2.title);
                        });
                        createdAdapter.notifyDataSetChanged();
                    }
                },
                new SortDialog.Sort() {
                    @Override
                    public String getDisplayName() {
                        return "By Pages ascending";
                    }

                    @Override
                    public void performSort() {
                        collectedSort = getDisplayName();
                        viewModel.sortCreatedComics((comic1, comic2) -> {
                            return comic1.pagesCount - comic2.pagesCount;
                        });
                        createdAdapter.notifyDataSetChanged();
                    }
                }
        );

        findViewById(R.id.sort_crated_comics_btn).setOnClickListener((View v) -> {
            this.sortDialog = new SortDialog(this, this.createdSorts);
            this.sortDialog.show();
        });

    }

    private void initCollectedSorts() {
        this.collectedSorts = new ArrayList<>();
        this.collectedSorts.add(new SortDialog.Sort() {
            @Override
            public String getDisplayName() {
                return "By Title ascending";
            }

            @Override
            public void performSort() {
                collectedSort = getDisplayName();
                viewModel.sortCreatedComics((comic1, comic2) -> {
                    return comic1.title.compareTo(comic2.title) * -1;
                });
                collectedAdapter.notifyDataSetChanged();
            }
        });

        this.collectedSorts.add(new SortDialog.Sort() {
            @Override
            public String getDisplayName() {
                return "By Rating ascending";
            }

            @Override
            public void performSort() {
                collectedSort = getDisplayName();
                viewModel.sortCreatedComics((comic1, comic2) -> {
                    return comic1.rating - comic2.rating;
                });
                collectedAdapter.notifyDataSetChanged();
            }
        });

        this.collectedSorts.add(new SortDialog.Sort() {
            @Override
            public String getDisplayName() {
                return "By Pages ascending";
            }

            @Override
            public void performSort() {
                collectedSort = getDisplayName();
                viewModel.sortCreatedComics((comic1, comic2) -> {
                    return comic1.pagesCount - comic2.pagesCount;
                });
                collectedAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.sort_collected_comics_btn).setOnClickListener((View v) -> {
            this.sortDialog = new SortDialog(this, this.collectedSorts);
            this.sortDialog.show();
        });

    }

    private void initFriendsSorts() {
        this.friendsSorts = new ArrayList<>();
        this.friendsSorts.add(new SortDialog.Sort() {
            @Override
            public String getDisplayName() {
                return "By Name ascending";
            }

            @Override
            public void performSort() {
                friendsSort = getDisplayName();
                viewModel.sortFirends((user1, user2) -> {
                    return user1.name.compareTo(user2.name);
                });
                friendsAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.sort_friends_comics_btn).setOnClickListener((View v) -> {
            this.sortDialog = new SortDialog(this, this.friendsSorts);
            this.sortDialog.show();
        });
    }

    // endregion

    // region load images

    private void loadCreatedComics(String id) {

        RecyclerView rView = findViewById(R.id.profile_created_comics_rv);

        if (createdAdapter == null) {

            createdAdapter = new PreviewListAdapter(
                    this,
                    R.layout.comic_preview_list_item,
                    PREVIEW_PAGE_WIDTH,
                    PREVIEW_PAGE_HEIGHT,
                    new PreviewItemProvider() {
                        @Override
                        public PreviewItemData getItem(int index) {
                            ViewComic comic = viewModel.getCreatedComicAt(
                                    index,
                                    PREVIEW_PAGE_WIDTH,
                                    PREVIEW_PAGE_HEIGHT);

                            if (comic != null) {
                                return new PreviewItemData(
                                        comic.comicId,
                                        comic.title,
                                        comic.description,
                                        comic.liveTitlePage);
                            }

                            return null;
                        }

                        @Override
                        public int getItemsCount() {
                            return viewModel.getCreatedCount();
                        }
                    },
                    this::createdComicClick);
        }

        rView.setAdapter(createdAdapter);

        viewModel.loadCreatedComics(id)
                .observe(this, (List<ViewComic> viewComics) -> {

                    if (viewComics == null) {
                        Log.e("createComics", "Got err as created comics ... ");
                        return;
                    }

                    createdAdapter.notifyDataSetChanged();

                });

    }

    private void loadCollectedComics(String id) {
        RecyclerView rView = findViewById(R.id.profile_collected_comics_rv);

        if (collectedAdapter == null) {
            collectedAdapter = new PreviewListAdapter(
                    this,
                    R.layout.comic_preview_list_item,
                    PREVIEW_PAGE_WIDTH,
                    PREVIEW_PAGE_HEIGHT,
                    new PreviewItemProvider() {
                        @Override
                        public PreviewItemData getItem(int index) {
                            ViewComic comic = viewModel.getCollectedComicAt(
                                    index,
                                    PREVIEW_PAGE_WIDTH,
                                    PREVIEW_PAGE_HEIGHT);

                            if (comic != null) {
                                return new PreviewItemData(
                                        comic.comicId,
                                        comic.title,
                                        comic.description,
                                        comic.liveTitlePage);
                            }

                            return null;
                        }

                        @Override
                        public int getItemsCount() {
                            return viewModel.getCollectedCount();
                        }
                    },
                    this::collectedComicClick);
        }

        rView.setAdapter(collectedAdapter);
        viewModel.loadCollectedComics(id)
                .observe(this, (List<ViewComic> viewComics) -> {
                    if (viewComics == null) {
                        Log.e("collectedComics", "Got err as collected comics ... ");
                        return;
                    }

                    collectedAdapter.notifyDataSetChanged();
                });

    }

    private void loadFriends(String id) {
        RecyclerView rView = findViewById(R.id.profile_friends_rv);

        if (friendsAdapter == null) {

            friendsAdapter = new PreviewListAdapter(
                    this,
                    R.layout.comic_preview_list_item,
                    PREVIEW_PAGE_WIDTH,
                    PREVIEW_PAGE_HEIGHT,
                    new PreviewItemProvider() {
                        @Override
                        public PreviewItemData getItem(int index) {
                            ViewUser friend = viewModel.getFriendAt(
                                    index,
                                    PREVIEW_PAGE_WIDTH,
                                    PREVIEW_PAGE_HEIGHT);

                            if (friend != null) {
                                return new PreviewItemData(
                                        friend.userId,
                                        friend.email,
                                        friend.name,
                                        friend.liveProfilePic);
                            }

                            return null;
                        }

                        @Override
                        public int getItemsCount() {
                            return viewModel.getFriendsCount();
                        }
                    },
                    this::friendPreviewClick);
        }

        rView.setAdapter(friendsAdapter);

        viewModel.loadFriends(id)
                .observe(this, (List<ViewUser> viewComics) -> {

                    if (viewComics == null) {
                        Log.e("friendsAdapter", "Got err as friends ... ");
                        return;
                    }

                    friendsAdapter.notifyDataSetChanged();

                });
    }

    // endregion

    // region click handlers

    private void addComicClick(View v) {
        Intent addIntent = new Intent(this, ComicPreviewAndEditActivity.class);
        addIntent.putExtra(
                ComicPreviewAndEditActivity.PREVIEW_REASON,
                ComicPreviewAndEditActivity.PreviewReason.Create);

        startActivity(addIntent);
    }

    private void createdComicClick(String id) {
        Intent updateIntent = new Intent(this, ComicPreviewAndEditActivity.class);
        ViewComic comic = viewModel.getCreatedComic(id);
        updateIntent.putExtra(
                ComicPreviewAndEditActivity.COMIC_INFO_EXTRA,
                comic);
        if (liveUser.getValue().userId.equals(myId)) {
            updateIntent.putExtra(
                    ComicPreviewAndEditActivity.PREVIEW_REASON,
                    ComicPreviewAndEditActivity.PreviewReason.PreviewAndEdit);
        } else {
            updateIntent.putExtra(
                    ComicPreviewAndEditActivity.PREVIEW_REASON,
                    ComicPreviewAndEditActivity.PreviewReason.JustPreview);
        }

        startActivity(updateIntent);
    }

    private void discoverComicClick(View v) {
        // TODO implement ... I guess open map with comics
    }

    private void collectedComicClick(String id) {
        Intent previewIntent = new Intent(this, ComicPreviewAndEditActivity.class);
        ViewComic comic = viewModel.getCollectedComic(id);
        previewIntent.putExtra(
                ComicPreviewAndEditActivity.COMIC_INFO_EXTRA,
                comic);
        previewIntent.putExtra(
                ComicPreviewAndEditActivity.PREVIEW_REASON,
                ComicPreviewAndEditActivity.PreviewReason.JustPreview);

        startActivity(previewIntent);
    }

    private void findFriendsClick(View v) {

    }

    private void friendPreviewClick(String id) {
        ViewUser user = viewModel.getFriend(id);
        MutableLiveData<ViewUser> liveUser = new MutableLiveData<>();
        liveUser.postValue(user);

        Dialog shortProfile = new ShortProfileDialog(this, liveUser);
        shortProfile.show();
    }

    // endregion

}