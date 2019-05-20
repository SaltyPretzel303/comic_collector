package mosis.comiccollector.activity;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import mosis.comiccollector.R;
import mosis.comiccollector.manager.AppManager;

public class MainPageActivity extends AppCompatActivity {

    private Button read_button;
    private Button collect_button;
    private Button discover_button;
    private Button profile_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        AppManager.getInstance().context = getApplicationContext();

        this.initButtons();

    }

    private void initButtons() {

        this.read_button = (Button) this.findViewById(R.id.read_btn);
        this.read_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent list_intent = new Intent(MainPageActivity.this, ComicListActivity.class);

                list_intent.putExtra("list_context", String.valueOf(ComicListContext.CollectedComicsList));

                startActivity(list_intent);

            }
        });

        this.collect_button = (Button) this.findViewById(R.id.collect_main_btn);
        this.collect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.discover_button = (Button) this.findViewById(R.id.discover_main_btn);
        this.discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent discover_intent = new Intent(MainPageActivity.this, ComicListActivity.class);

                discover_intent.putExtra("list_context", String.valueOf(ComicListContext.DiscoverComicsList));

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


}
