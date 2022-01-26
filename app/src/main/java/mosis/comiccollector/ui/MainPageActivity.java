package mosis.comiccollector.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.map.DiscoverMapActivity;
import mosis.comiccollector.ui.user.ProfileActivity;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.AuthUserViewModel;
import mosis.comiccollector.util.Toaster;

public class MainPageActivity extends AppCompatActivity {

    private AuthUserViewModel userViewModel;

    private ActivityResultLauncher<Intent> loginActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        this.initImageLoader();

        this.userViewModel = new ViewModelProvider(this).get(AuthUserViewModel.class);

        this.initButtons();

        this.resolveUser();

    }

    private void initButtons() {

        findViewById(R.id.logout_btn).setOnClickListener(v -> {

            (new AlertDialog.Builder(this))
                    .setMessage("After logout all your cached comics will be removed !!!")
                    .setPositiveButton("Logout", (dialogInterface, i) -> {
                        userViewModel.removeUser();
                        dialogInterface.dismiss();

                        Log.e("logout", "User removed ... ");
                    })
                    .setNegativeButton("Dismiss", (dialogInterface, i) -> {
                        dialogInterface.dismiss();

                        Log.e("logout", "Logout dismissed ... ");
                    })
                    .create()
                    .show();


            // this should trigger observer in authViewModel

        });

        findViewById(R.id.discover_main_btn).setOnClickListener(v -> {

//            Intent discover_intent = new Intent(MainPageActivity.this, ComicListActivity.class);
//            discover_intent.putExtra("list_context", String.valueOf(ComicListContext.DiscoverComics));

            Intent discover_intent = new Intent(MainPageActivity.this, DiscoverMapActivity.class);
            startActivity(discover_intent);

        });

        findViewById(R.id.profile_main_btn).setOnClickListener(v -> {

            Intent profileIntent = new Intent(MainPageActivity.this, ProfileActivity.class);
            startActivity(profileIntent);

        });

    }

    private void resolveUser() {
        this.userViewModel.getCurrentUser().observe(this, (ViewUser responseUser) -> {

            if (responseUser == null) {
                // there is no user
                Toaster.makeToast(getApplicationContext(), "This app does not have user ... ");

                Intent loginIntent = new Intent(
                        getApplicationContext(),
                        LoginActivity.class);
                startActivity(loginIntent);
//                loginActivityLauncher.launch(loginIntent);

                return;
            }

            Toaster.makeToast(this, "There is an user: " + responseUser.email);

        });
    }

    private void initLoginActivity() {

        this.loginActivityLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {

                    if (result.getResultCode() != Activity.RESULT_OK) {
                        Log.e("Login activity", "Something went wrong with login activity: " + result.getResultCode());

                        return;
                    }

                    Toaster.makeToast(getApplicationContext(), "You Are SignedIn");

                    return;
                }
        );
    }

    private void initImageLoader() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading()
//                .showImageOnFail()
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);

    }


}
