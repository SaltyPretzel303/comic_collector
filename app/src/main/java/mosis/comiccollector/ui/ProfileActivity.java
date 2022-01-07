package mosis.comiccollector.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.comic.PreviewListAdapter;
import mosis.comiccollector.ui.user.ProfileData;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.UserProfileViewModel;
import mosis.comiccollector.util.DepProvider;

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
            this.liveUser = viewModel.loadUser(this.myId);
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
                    .setText(String.valueOf(user.rating) + "/100");

            user.liveLocalPicUri.observe(this, (String sUri) -> {
                if (sUri != null) {
                    ((ImageView) this.findViewById(R.id.profil_pic_iv))
                            .setImageURI(Uri.parse(sUri));
                } else {
                    Log.e("imageView", "Image uri is null ... ");
                }
            });

            if (user.userId.equals(myId)) {
                // if displaying my acc

                this.findViewById(R.id.profil_pic_iv).setOnClickListener(v -> {
                    this.loadPicActivityLauncher.launch("image/*");
                });

            }

            // start loading comics

        });


    }

    private void loadCreatedComics() {

    }

    private void loadCollectedComics() {

    }

    private void loadFriends() {

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

}
