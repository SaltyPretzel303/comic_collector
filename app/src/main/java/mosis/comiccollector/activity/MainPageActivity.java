package mosis.comiccollector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mosis.comiccollector.R;
import mosis.comiccollector.login.BackPressHandler;
import mosis.comiccollector.login.LoginDialog;
import mosis.comiccollector.manager.AppManager;

public class MainPageActivity extends AppCompatActivity {

    private Button read_button;
    private Button collect_button;
    private Button discover_button;
    private Button profile_button;

    private LoginDialog login_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        // TODO REMOVE; used just for testing
        AppManager.getInstance().context = getApplicationContext();

        this.initButtons();

        if (!this.isLoggedIn()) {
            this.showLoginDialog();
        }

    }

    private void initButtons() {

        this.read_button = (Button) this.findViewById(R.id.read_btn);
        this.read_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent list_intent = new Intent(MainPageActivity.this, ComicListActivity.class);

                list_intent.putExtra("list_context", String.valueOf(ComicListContext.CollectedComics));

                startActivity(list_intent);

            }
        });

        this.collect_button = (Button) this.findViewById(R.id.collect_main_btn);
        this.collect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent list_indent = new Intent(MainPageActivity.this, ComicListActivity.class);

                list_indent.putExtra("list_context", String.valueOf(ComicListContext.QueuedComics));

                startActivity(list_indent);

            }
        });

        this.discover_button = (Button) this.findViewById(R.id.discover_main_btn);
        this.discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent discover_intent = new Intent(MainPageActivity.this, ComicListActivity.class);

                discover_intent.putExtra("list_context", String.valueOf(ComicListContext.DiscoverComics));

                startActivity(discover_intent);

            }
        });


        this.profile_button = (Button) this.findViewById(R.id.profile_main_btn);
        this.profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profile_intent = new Intent(MainPageActivity.this, ProfileActivity.class);

                startActivity(profile_intent);

            }
        });

    }

    private boolean isLoggedIn() {

        // TODO check localstoreage

        return true;
    }


    private void showLoginDialog() {

        final boolean forced = false;

        this.login_dialog = new LoginDialog(this, forced, new BackPressHandler() {
            @Override
            public void execute() {

                if (forced) {

                    login_dialog.dismiss();
                    finish();

                } else {

                    login_dialog.dismiss();

                }


            }

        });

        this.login_dialog.show();

    }

}
