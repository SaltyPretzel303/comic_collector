package mosis.comiccollector.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mosis.comiccollector.R;

public class SortActivity extends AppCompatActivity {

    private Button cancel_btn;
    private Button sort_btn;

    private String sort_type;

    private ComicListContext sort_context;

    // TODO create map with viewInitializers


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sort_activity_dialog);

        Intent intent = this.getIntent();
        this.sort_context = ComicListContext.valueOf(intent.getStringExtra("sort_context"));

        this.initView();

    }

    private void initView() {

        this.cancel_btn = (Button) this.findViewById(R.id.cancel_sort_btn);
        this.sort_btn = (Button) this.findViewById(R.id.execute_sort_btn);

        this.cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(Activity.RESULT_CANCELED);

                finish();

            }
        });

        this.sort_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sort_result = new Intent();
                sort_result.putExtra("sort_type", sort_type);

                setResult(Activity.RESULT_OK, sort_result);

                finish();

            }
        });


    }

}

