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
    public static final int SNORING_PREVENTION_MODE = 1;
    public static final int APNEA_PREVENTION_MODE = 2;
    public static final int DISEASE_ALLEVIATION_MODE = 3;

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
                editor.putInt("mode", SNORING_PREVENTION_MODE).apply();
                editor.commit();
                setResult(1001);
                finish();
            }
        });

        // 무호흡 방지 모드
        modeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", APNEA_PREVENTION_MODE).apply();
                editor.commit();
                setResult(1002);
                finish();
            }
        });

        // 질환 완화 모드
        modeButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("mode", DISEASE_ALLEVIATION_MODE).apply();
                editor.commit();
                setResult(1003);
                finish();
            }
        });
    }

    // 뒤로가기 버튼을 누를시 아무 동작을 못하게 하기 위함
    @Override
    public void onBackPressed() {

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
