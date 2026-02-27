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
import com.android.volley.toolbox.StringRequest;

public class DeleteUserActivity extends AppCompatActivity {

    private EditText etUserId;
    private Button btnDelete, btnBackToMain;

    private static final String URL_DELETE_USER = "http://10.0.2.2:3002/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        etUserId = findViewById(R.id.etUserId);
        btnDelete = findViewById(R.id.btnDelete);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDelete();
            }
        });

        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteUserActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performDelete() {
        String userId = etUserId.getText().toString();
        String url = URL_DELETE_USER + userId;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Volley Delete Rsp", response);
                        Toast.makeText(getApplicationContext(), "User Deleted!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Delete Error", "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "Delete Failed! Check logs.", Toast.LENGTH_SHORT).show();
                    }
                });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}