package mosis.comiccollector.activity;

import android.content.Intent;
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
    private Button friends_button;

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
                startActivity(list_intent);

            }
        });


    }


}
