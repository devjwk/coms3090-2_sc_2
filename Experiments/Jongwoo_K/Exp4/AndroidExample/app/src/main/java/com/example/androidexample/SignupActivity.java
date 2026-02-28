package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    // UI Components
    private EditText etEmail, etPassword, etDisplayName, etBio, etMajor, etAge, etInterests;
    private Button btnSignup, btnBackToMain;

    // English: Set your Mockoon server URL here.
    // Korean: 여기에 Mockoon 서버 URL을 설정하세요.
    private static final String URL_SIGNUP = "http://your_mock_server_ip:port/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDisplayName = findViewById(R.id.etDisplayName);
        etBio = findViewById(R.id.etBio);
        etMajor = findViewById(R.id.etMajor);
        etAge = findViewById(R.id.etAge);
        etInterests = findViewById(R.id.etInterests);
        btnSignup = findViewById(R.id.btnSignup);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        // Set click listener for the Signup button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // English: When the button is clicked, start the signup process.
                // Korean: 버튼이 클릭되면, 회원가입 절차를 시작합니다.
                performSignup();
            }
        });

        // Set click listener for the "Back to Main" button
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // English: Go back to MainActivity.
                // Korean: MainActivity로 돌아갑니다.
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * English: Gathers user input and sends a POST request to the server.
     * Korean: 사용자 입력을 모아 서버로 POST 요청을 보냅니다.
     */
    private void performSignup() {
        // English: Create a HashMap to hold the parameters for the request.
        // Korean: 요청에 필요한 파라미터들을 담을 HashMap을 생성합니다.
        final Map<String, String> params = new HashMap<>();
        params.put("email", etEmail.getText().toString());
        params.put("password", etPassword.getText().toString());
        params.put("displayName", etDisplayName.getText().toString());
        params.put("bio", etBio.getText().toString());
        params.put("major", etMajor.getText().toString());
        params.put("age", etAge.getText().toString());
        params.put("interests", etInterests.getText().toString());

        // English: Create the JSON request.
        // Korean: JSON 요청을 생성합니다.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST,
                URL_SIGNUP,
                new JSONObject(params), // Pass the parameters as a JSONObject
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // English: Handle successful response.
                        // Korean: 성공적인 응답을 처리합니다.
                        Log.d("Volley Signup Rsp", response.toString());
                        Toast.makeText(getApplicationContext(), "Signup Successful!", Toast.LENGTH_SHORT).show();
                        // English: Optionally, you can navigate back to the main screen or a login screen.
                        // Korean: 선택적으로, 메인 화면이나 로그인 화면으로 돌아갈 수 있습니다.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // English: Handle error response.
                        // Korean: 에러 응답을 처리합니다.
                        Log.e("Volley Signup Error", "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Signup Failed! Check logs.", Toast.LENGTH_SHORT).show();
                    }
                });

        // English: Add the request to the Volley request queue.
        // Korean: Volley 요청 큐에 요청을 추가합니다.
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }
}
