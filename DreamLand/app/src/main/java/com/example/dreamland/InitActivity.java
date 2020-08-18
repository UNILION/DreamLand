package com.example.dreamland;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class InitActivity extends AppCompatActivity {

    LinearLayout modeButton1;
    LinearLayout modeButton2;
    LinearLayout modeButton3;
    SharedPreferences sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        modeButton1 = (LinearLayout) findViewById(R.id.button_layout1);
        modeButton2 = (LinearLayout) findViewById(R.id.button_layout2);
        modeButton3 = (LinearLayout) findViewById(R.id.button_layout3);

        sf = getSharedPreferences("bed", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sf.edit();

        // 코골이 방지 모드
        modeButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", 1);
                editor.commit();
                setResult(1001);
                finish();
            }
        });

        // 무호흡 방지 모드
        modeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", 2);
                editor.commit();
                setResult(1001);
                finish();
            }
        });

        // 질환 완화 모드
        modeButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", 3);
                editor.commit();
                finish();
            }
        });
    }

    // 뒤로가기 버튼을 누를시 아무 동작을 못하게 하기 위함
    @Override
    public void onBackPressed() {

    }
}
