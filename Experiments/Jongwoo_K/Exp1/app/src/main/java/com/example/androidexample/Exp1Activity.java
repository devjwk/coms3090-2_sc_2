package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

public class Exp1Activity extends AppCompatActivity {

    private TextView Exp1Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp1);

        Exp1Description = findViewById(R.id.exp1_desc_txt);

        Exp1Description.setText(
                "Demo1 exp1 is a simple Android example demonstrating navigation between activities using Intent."
        );
        Button backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }
}
