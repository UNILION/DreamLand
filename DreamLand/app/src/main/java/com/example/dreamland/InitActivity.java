package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class InitActivity extends AppCompatActivity {

    LinearLayout modeButton1;
    LinearLayout modeButton2;
    SharedPreferences sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        modeButton1 = findViewById(R.id.button_layout1);
        modeButton2 = findViewById(R.id.button_layout2);

        sf = getSharedPreferences("bed", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sf.edit();

        modeButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", 1);
                editor.commit();
                finish();
            }
        });

        modeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", 2);
                editor.commit();
                setResult(1001);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
