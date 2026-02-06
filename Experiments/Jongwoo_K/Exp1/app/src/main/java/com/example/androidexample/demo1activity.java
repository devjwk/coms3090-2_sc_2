package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

public class demo1activity extends AppCompatActivity {
    private TextView demo1Description;
    private Button exp1Btn;
    private Button exp2Btn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo1);
        exp1Btn = findViewById(R.id.exp1_btn);
        exp2Btn = findViewById(R.id.exp2_btn);
        demo1Description = findViewById(R.id.demo1_desc_txt);

        demo1Description.setText(
                "Demo1 is a simple Android Example intialize and modify them for showing understand basic knowledge about android studio."

        );

        exp1Btn.setOnClickListener(v -> {
            Intent intent = new Intent(demo1activity.this, Exp1Activity.class);
            startActivity(intent);
        });

        exp2Btn.setOnClickListener(v -> {
            Intent intent = new Intent(demo1activity.this, Exp2Activity.class);
            startActivity(intent);
        });

        Button backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());

    }
}
