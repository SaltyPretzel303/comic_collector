package mosis.comiccollector.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.UserProfileViewModel;
import mosis.comiccollector.util.DepProvider;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_ID_EXTRA = "user_id";

    private String myId;
    private String userId;

    private ImageView profilePicIv;

    private TextView usernameTv;

    private ProgressBar userRatingPb;
    private TextView userRatingTv;

    // these should be horizontal lists
    private Button myComicsBtn;
    private Button friendsBtn;
    private Button newUploadBtn;

    private ActivityResultLauncher<String> loadPicActivityLauncher;

    private UserProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        this.myId = this.userId = DepProvider
                .getAuthRepository()
                .getCurrentUser()
                .user.getUserId();

        this.profileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        this.initLoadPictureActivityLauncher();

        Intent intent = getIntent();
        if (intent.hasExtra(USER_ID_EXTRA)) {
            this.userId = intent.getExtras().getString(USER_ID_EXTRA);
        } else {
            this.userId = myId;
        }

        this.initView();
    }

    private void initLoadPictureActivityLauncher() {
        this.loadPicActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {

                    Log.e("Pic uri", "We got this pic uri: " + uri.toString());

                    ((ImageView) findViewById(R.id.profil_pic_iv)).setImageURI(uri);

                    profileViewModel.saveProfilePic(uri.toString());

                }
        );
    }

    private void initView() {

        profileViewModel.loadUser(this.userId)
                .observe(this, (ViewUser user) -> {

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

                });

        this.findViewById(R.id.profil_pic_iv).setOnClickListener(v -> {
            this.loadPicActivityLauncher.launch("image/*");
        });


        this.newUploadBtn = (Button) this.findViewById(R.id.new_upload_btn);
        this.newUploadBtn.setOnClickListener((v) -> {

            Toast.makeText(this, "Not implemented yed ... ", Toast.LENGTH_SHORT).show();

        });

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
