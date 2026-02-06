package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

public class Exp2Activity extends AppCompatActivity {

    private TextView Exp2Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp2);

        Exp2Description = findViewById(R.id.exp2_desc_txt);

        Exp2Description.setText(
                "Demo1 exp2 is a simple Android example demonstrating navigation between activities using Intent."
        );
        Button backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());
    }
}
