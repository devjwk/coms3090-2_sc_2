package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable
    private Button goMainButton;        // define backToMain button variable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML
        goMainButton = findViewById(R.id.goMainButton); // 1. need to define Login activity XML first []

        /* click listener on login button pressed */

        loginButton.setOnClickListener(v -> {

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // From SharedPreferences get save USER_INFO
            String savedUser = getSharedPreferences("USER_INFO", MODE_PRIVATE)
                    .getString("USERNAME", "");

            String savedPass = getSharedPreferences("USER_INFO", MODE_PRIVATE)
                    .getString("PASSWORD", "");

            if(username.equals(savedUser) && password.equals(savedPass)){

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();

            }else{
                Intent intent = new Intent(LoginActivity.this, FailActivity.class);
                intent.putExtra("FAIL_MSG", "Wrong username or password");
                startActivity(intent);
            }
        });
        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });

        /*click listener when press back to main button*/
        goMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                finish(); // Login activity finished.
            }
        });

    }
}