package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {

    private Button btnUpdateProfile, btnBack;
    private TextView msgResponse;
    private EditText etName, etBio;

    private User user;
    private int userId;

    //private static final String EDIT_URL = "http://10.0.2.2:3002/users/";
    private static final String EDIT_URL   = "http://coms-3090-015.class.las.iastate.edu:8080/users/edit/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_obj_req);

        userId = getIntent().getIntExtra("USER_ID", 1);

        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnBack          = findViewById(R.id.btnBack);
        msgResponse      = findViewById(R.id.msgResponse);
        etName           = findViewById(R.id.etName);
        etBio            = findViewById(R.id.etBio);

        btnUpdateProfile.setEnabled(false);
        etName.setEnabled(false);
        etBio.setEnabled(false);

        String userJson = getIntent().getStringExtra("USER_JSON");
        if (userJson != null && !userJson.isEmpty()) {
            try {
                JSONObject jo = new JSONObject(userJson);
                user = parseUserFromJson(jo);
                if (user != null) {
                    refreshUI();
                    etName.setText(user.getName());
                    etBio.setText(user.getBio());
                    etName.setEnabled(true);
                    etBio.setEnabled(true);
                    btnUpdateProfile.setEnabled(true);
                }
            } catch (JSONException ignored) { }
        }

        btnUpdateProfile.setOnClickListener(v -> {
            if (user != null) {
                updateUserProfile();
            } else {
                Toast.makeText(this, "Profile not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private User parseUserFromJson(JSONObject response) throws JSONException {
        int id = response.optInt("userId", response.optInt("user_id", 1));
        if (id == 0 && response.has("user_id")) {
            try {
                id = Integer.parseInt(response.optString("user_id", "1"));
            } catch (NumberFormatException ignored) {
                id = 1;
            }
        }
        if (id == 0) id = 1;
        String name = response.optString("displayName", response.optString("name", ""));
        if (name == null || "null".equals(name)) name = "";
        String createdAt = response.optString("createdTs", response.optString("createdAt", ""));
        boolean isActive = true;
        if (response.has("isActive")) {
            Object a = response.opt("isActive");
            isActive = a instanceof Boolean ? (Boolean) a : "true".equalsIgnoreCase(String.valueOf(a));
        } else if (response.has("active")) {
            Object a = response.opt("active");
            isActive = a instanceof Boolean ? (Boolean) a : "true".equalsIgnoreCase(String.valueOf(a));
        }
        String major = response.optString("major", "");
        int age = response.optInt("age", 0);

        List<String> hobbies = new ArrayList<>();
        if (response.has("hobbies") && !response.isNull("hobbies")) {
            JSONArray arr = response.getJSONArray("hobbies");
            for (int i = 0; i < arr.length(); i++) hobbies.add(arr.optString(i, ""));
        }

        return new User(
                id,
                name,
                response.optString("email", ""),
                response.optString("passwordHash", ""),
                response.optString("bio", ""),
                hobbies,
                response.optString("role", ""),
                response.optDouble("latitude", 0.0),
                response.optDouble("longitude", 0.0),
                createdAt,
                isActive,
                major,
                age
        );
    }

    private void updateUserProfile() {
        String url = EDIT_URL + user.getUserId();

        String updatedBio = etBio.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("bio", updatedBio);
            if (user.getAge() != null && user.getAge() > 0) {
                jsonBody.put("age", user.getAge());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> {
                    Log.d("PUT Response", response.toString());
                    user.setBio(updatedBio);
                    refreshUI();
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("PUT Error", error.toString());
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }

    private void refreshUI() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(user.getName()).append("\n\n");
        sb.append("Email: ").append(user.getEmail()).append("\n\n");
        sb.append("Bio: ").append(user.getBio()).append("\n\n");
        sb.append("Major: ").append(user.getMajor()).append("\n\n");
        sb.append("Age: ").append(user.getAge()).append("\n\n");
        sb.append("Created: ").append(user.getCreatedAt()).append("\n\n");
        sb.append("Active: ").append(user.isActive());
        if (user.getHobbies() != null && !user.getHobbies().isEmpty()) {
            sb.append("\n\nHobbies: ").append(user.getHobbies().toString());
        }
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            sb.append("\n\nRole: ").append(user.getRole());
        }
        msgResponse.setText(sb.toString());
    }
}