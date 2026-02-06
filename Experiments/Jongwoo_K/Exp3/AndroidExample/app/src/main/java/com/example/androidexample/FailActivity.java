package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class FailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail);

        TextView failMsg = findViewById(R.id.fail_msg_txt);
        Button backBtn = findViewById(R.id.fail_back_btn);

        String msg = getIntent().getStringExtra("FAIL_MSG");
        if(msg == null) msg  = "login failed";

        failMsg.setText(msg);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } );

    }
}
