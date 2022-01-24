package mosis.comiccollector.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import mosis.comiccollector.R;
import mosis.comiccollector.ui.user.ViewUser;
import mosis.comiccollector.ui.viewmodel.AuthUserViewModel;

public class LoginActivity extends AppCompatActivity {

    private TextView loginStatusTv;

    private TextView usernameTv;
    private EditText usernameEt;
    private EditText emailEt;
    private EditText passwordEt;

    private TextView emailTv;
    private TextView repPasswordTv;
    private EditText repPasswordEt;

    private Button registerBtn;
    private Button loginButton;
    private SignInButton googleLoginBtn;

    private ActivityResultLauncher<Intent> googleLoginActivity;

    private AuthUserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        this.userViewModel = new ViewModelProvider(this).get(AuthUserViewModel.class);

        this.initGoogleLoginActivity();
        this.findViews();
        this.initButtons();
    }

    private void initGoogleLoginActivity() {
        this.googleLoginActivity = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::googleLoginHandler);

    }

    private void googleLoginHandler(ActivityResult result) {

        if (result.getResultCode() != Activity.RESULT_OK) {
            Log.e("google login", "Something went wrong with google login ... " + result.getResultCode());
            showMessage("SOMETHING WENT WRONG ... ");

            return;
        }

        try {

            // this is jut selected google account
            GoogleSignInAccount googleAcc = GoogleSignIn
                    .getSignedInAccountFromIntent(result.getData())
                    .getResult(ApiException.class);

            if (googleAcc == null) {
                Log.e("google login", "Selected google acc is null ... ");
                showMessage("SELECTED ACC IS NULL ... ???");

                return;
            }

            String googleIdToken = googleAcc.getIdToken();
            AuthCredential googleAuthCredential = GoogleAuthProvider
                    .getCredential(googleIdToken, null);

            Log.e("LOGIN ACT", "Gonna sign in with google ... all good");
            this.userViewModel.loginWithGoogle(googleAuthCredential)
                    .observe(this, (ViewUser response) ->
                    {
                        if (response == null || response.errorMessage != null) {
                            Log.e("Google login result", "You failed to login with google ... "
                                    + response.errorMessage);

                            return;
                        }

                        showMessage("YOU DID IT");
                        Log.e("Google login result", "You loggedIn with google ... ");

                        finish();
                        return;
                    });


        } catch (ApiException e) {
            Log.e("GOOGLE LOGIN", "Exception in googleLoginActivity: " + e.getMessage());
        }


    }

    private void findViews() {

        this.loginStatusTv = this.findViewById(R.id.login_status_field);

        this.emailTv = this.findViewById(R.id.login_email_tv);
        this.emailEt = this.findViewById(R.id.login_email_et);

        this.usernameTv = this.findViewById(R.id.username_tv);
        this.usernameTv.setVisibility(View.GONE);
        this.usernameEt = this.findViewById(R.id.login_username_et);
        this.usernameEt.setVisibility(View.GONE);

        this.passwordEt = this.findViewById(R.id.login_password_et);

        this.repPasswordTv = this.findViewById(R.id.login_rep_password_tw);
        this.repPasswordTv.setVisibility(View.GONE);
        this.repPasswordEt = this.findViewById(R.id.login_rep_password_et);
        this.repPasswordEt.setVisibility(View.GONE);

        this.loginButton = this.findViewById(R.id.login_pos_btn);
        this.registerBtn = this.findViewById(R.id.register_btn);
        this.googleLoginBtn = this.findViewById(R.id.google_sign_in_btn);
    }

    private void initButtons() {
        this.loginButton.setOnClickListener(this::loginButtonClick);

        this.registerBtn.setOnClickListener(this::registerButtonClick);

        this.googleLoginBtn.setOnClickListener(this::googleSignInClick);
    }

    // TODO this method/action should be removed ...
    private void showMessage(String message) {

        new Handler(Looper.getMainLooper()).post(() -> {
            this.loginStatusTv.setText(message);
        });

    }

    private void loginButtonClick(View v) {
        if (repPasswordEt.getVisibility() == View.VISIBLE) {

            usernameTv.setVisibility(View.GONE);
            usernameEt.setVisibility(View.GONE);

            repPasswordTv.setVisibility(View.GONE);
            repPasswordEt.setVisibility(View.GONE);

            usernameEt.setText("");
            passwordEt.setText("");

            return;
        }

        final String email = emailEt.getText().toString();
        final String password = passwordEt.getText().toString();

        // TODO add additional rules with text watchers and such ...
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Username and password are required ... ");

            return;
        }


        this.userViewModel.loginWithEmail(email, password)
                .observe(this, (ViewUser response) -> {

                    if (response == null || response.errorMessage != null) {
                        Log.e("login screen", "Failed to login with username and password");

                        return;
                    }

                    showMessage("YOU DID IT ... ");

                    finish();

                    return;
                });
    }

    private void registerButtonClick(View v) {
        if (repPasswordEt.getVisibility() != View.VISIBLE) {

            // should always be visible
//            emailTv.setVisibility(View.VISIBLE);
//            emailEt.setVisibility(View.VISIBLE);

            usernameTv.setVisibility(View.VISIBLE);
            usernameEt.setVisibility(View.VISIBLE);

            repPasswordTv.setVisibility(View.VISIBLE);
            repPasswordEt.setVisibility(View.VISIBLE);

            emailEt.setText("");
            usernameEt.setText("");
            passwordEt.setText("");
            repPasswordEt.setText("");

            return;
        }

        final String email = emailEt.getText().toString();
        final String username = usernameEt.getText().toString();
        final String password = passwordEt.getText().toString();
        final String repPassword = repPasswordEt.getText().toString();

        if (username.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || !password.equals(repPassword)) {

            showMessage("Missing username or not matching password ... ");

            return;
        }

        this.userViewModel.registerWithEmail(email, username, password)
                .observe(this, (ViewUser response) -> {

                    if (response == null || response.errorMessage != null) {
                        showMessage("You did something wrong ... ");
                        Log.e("register result", "You FAILED to registered ... " + response.errorMessage);

                        return;
                    }

                    showMessage("YOU DID IT ... ");
                    Log.e("register result", "You are registered ... ");

                    usernameTv.setVisibility(View.GONE);
                    usernameEt.setVisibility(View.GONE);

                    repPasswordTv.setVisibility(View.GONE);
                    repPasswordEt.setVisibility(View.GONE);

                    return;
                });
    }

    private void googleSignInClick(View v) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                this, googleSignInOptions);

        this.googleLoginActivity.launch(googleSignInClient.getSignInIntent());
    }

    @Override
    public void onBackPressed() {

        // do nothing ... you have to login first ...
        // maybe show some message that you have to login first

    }

}