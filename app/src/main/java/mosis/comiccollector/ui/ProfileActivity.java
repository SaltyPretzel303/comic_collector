package mosis.comiccollector.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.comic.PreviewListAdapter;
import mosis.comiccollector.ui.comic.UpdateAddComicActivity;
import mosis.comiccollector.ui.comic.ViewComic;
import mosis.comiccollector.ui.user.ProfileData;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.UserProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_DATA_EXTRA = "user_data";

    private PreviewListAdapter createdAdapter;
    private RecyclerView createdComicsRv;

    private PreviewListAdapter collectedAdapter;
    private RecyclerView collectedComicsRv;

    private PreviewListAdapter friendsAdapter;
    private RecyclerView friendsRv;

    private ActivityResultLauncher<String> loadPicActivityLauncher;

    private UserProfileViewModel viewModel;
    private String myId;

    private MutableLiveData<ViewUser> liveUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        this.viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        this.myId = this.viewModel.getMyId();

        this.initLoadPictureActivityLauncher();

        Intent intent = getIntent();
        if (intent.hasExtra(USER_DATA_EXTRA)) {
            ProfileData pData = (ProfileData) intent.getSerializableExtra(USER_DATA_EXTRA);
            ViewUser vData = ViewUser.fromProfileData(pData);
            this.liveUser = new MutableLiveData<>();
            this.liveUser.postValue(vData);
        } else {
            this.liveUser = viewModel.getUser(this.myId);
        }

        this.initView();
    }

    private void initLoadPictureActivityLauncher() {
        this.loadPicActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {

                    Log.e("Pic uri", "We got this pic uri: " + uri.toString());

                    ((ImageView) findViewById(R.id.profil_pic_iv)).setImageURI(uri);

                    viewModel.saveProfilePic(uri.toString());

                }
        );
    }

    private void initView() {
        this.liveUser.observe(this, (ViewUser user) -> {

            ((TextView) this.findViewById(R.id.profile_username_tv))
                    .setText(user.email);
            ((ProgressBar) this.findViewById(R.id.user_rating_pb))
                    .setProgress(user.rating);
            ((TextView) this.findViewById(R.id.user_rating_tv))
                    .setText(user.rating + "/100");

            Uri picUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

            Log.e("imgLoader", "Loading: " + picUri.toString());

            user.liveProfilePic.observe(this, (Bitmap bitmap) -> {
                if (bitmap != null) {
                    ((ImageView) this.findViewById(R.id.profil_pic_iv))
                            .setImageBitmap(bitmap);
                } else {
                    Log.e("imageView", "Image bitmap is null ... ");
                }
            });

            if (user.userId.equals(myId)) {
                // if displaying my acc

                this.findViewById(R.id.profil_pic_iv).setOnClickListener(v -> {
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
    }

    private void loadCreatedComics(String id) {

        RecyclerView rView = findViewById(R.id.profile_created_comics_rv);

        createdAdapter = new PreviewListAdapter(
                this,
                R.layout.comic_preview_list_item,
                this::collectedComicClick);

        rView.setAdapter(createdAdapter);

        viewModel.loadCreatedComics(id)
                .observe(this, (List<ViewComic> viewComics) -> {
                    if (viewComics == null) {
                        Log.e("createComics", "Got err as created comics ... ");
                        return;
                    }

                    for (ViewComic vComic : viewComics) {

                        vComic.liveTitlePage.observe(this, (Bitmap newBitmap) -> {

                                    if (newBitmap == null) {
                                        // i don't know ... do something
                                        // placeholder titlePage ...
                                        return;
                                    }

                                    createdAdapter.addItem(vComic.modelId, newBitmap);
                                }
                        );
                    }


                });

    }

    private void loadCollectedComics(String id) {

    }

    private void loadFriends(String id) {

    }

    // have no idea ... afraid to remove it ..
    // just copied from "previous version"
    private String getPicRealPath(Uri imageUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
        ((Cursor) cursor).moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);

        return picturePath;
    }

    private void addComicClick(View v) {
        Intent addIntent = new Intent(this, UpdateAddComicActivity.class);
        startActivity(addIntent);
    }

    private void collectedComicClick(String id) {
        // TODO implement
    }

    private void discoverComicClick(View v) {

    }

    private void findFriendsClick(View v) {

    }

}
