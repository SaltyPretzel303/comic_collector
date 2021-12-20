package mosis.comiccollector.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import mosis.comiccollector.R;
import mosis.comiccollector.model.user.UserResponse;
import mosis.comiccollector.model.user.UserResponseType;
import mosis.comiccollector.ui.viewmodel.UserViewModel;
import mosis.comiccollector.model.user.User;
import mosis.comiccollector.util.Toaster;

public class MainPageActivity extends AppCompatActivity {

    private Button logoutBtn;

    private Button readBtn;
    private Button collectBtn;
    private Button discoverBtn;
    private Button profileButton;

    private UserViewModel userViewModel;

    private ActivityResultLauncher loginActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        this.userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        this.initLoginActivity();

        this.initButtons();

        this.resolveUser();

    }

    private void resolveUser() {
        this.userViewModel.getCurrentUser().observe(this, (UserResponse response) -> {

            if (response.user == null
                    && response.responseType == UserResponseType.Success) {

                // there is no user
                Toaster.makeToast(getApplicationContext(), "This app does not have user ... ");

                Intent loginIntent = new Intent(
                        getApplicationContext(),
                        LoginActivity.class);
                loginActivityLauncher.launch(loginIntent);

                return;
            }

            Toaster.makeToast(this, "There is an user: " + response.user.getEmail());

        });
    }

    private void initButtons() {

        this.logoutBtn = (Button) this.findViewById(R.id.logout_btn);
        this.logoutBtn.setOnClickListener(v -> {

            // TODO show dialog with warning message
            // all comic will be deleted after this operation

            userViewModel.removeUser();
            // this will trigger observer setup in resolve user

        });

        this.readBtn = (Button) this.findViewById(R.id.read_btn);
        this.readBtn.setOnClickListener(v -> {

            Intent list_intent = new Intent(MainPageActivity.this, ComicListActivity.class);

            list_intent.putExtra("list_context", String.valueOf(ComicListContext.CollectedComics));

            startActivity(list_intent);

        });

        this.collectBtn = (Button) this.findViewById(R.id.collect_main_btn);
        this.collectBtn.setOnClickListener(v -> {

            Intent list_indent = new Intent(MainPageActivity.this, ComicListActivity.class);

            list_indent.putExtra("list_context", String.valueOf(ComicListContext.QueuedComics));

            startActivity(list_indent);

        });

        this.discoverBtn = (Button) this.findViewById(R.id.discover_main_btn);
        this.discoverBtn.setOnClickListener(v -> {

//            Intent discover_intent = new Intent(MainPageActivity.this, ComicListActivity.class);
//
//            discover_intent.putExtra("list_context", String.valueOf(ComicListContext.DiscoverComics));

            Intent discover_intent = new Intent(MainPageActivity.this, MapActivity.class);
            startActivity(discover_intent);

        });


        this.profileButton = (Button) this.findViewById(R.id.profile_main_btn);
        this.profileButton.setOnClickListener(v -> {

            Intent profile_intent = new Intent(MainPageActivity.this, ProfileActivity.class);

            startActivity(profile_intent);

        });

    }

    private void initLoginActivity() {
        this.loginActivityLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        Log.e("Login activity", "Something went wrong with login activity: " + result.getResultCode());
                        Toaster.makeToast(getApplicationContext(), "Something went wrog with login activity");

                        return;
                    }

                    User newUser = (User) result
                            .getData()
                            .getExtras()
                            .getSerializable("user");

                    Toaster.makeToast(getApplicationContext(), "You Are SignedIn");

                    return;
                }
        );
    }

}
