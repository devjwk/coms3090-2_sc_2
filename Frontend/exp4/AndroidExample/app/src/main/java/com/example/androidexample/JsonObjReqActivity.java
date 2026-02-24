package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

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

public class JsonObjReqActivity extends AppCompatActivity {

    // UI Components
    private Button btnJsonObjReq, btnUpdateProfile;
    private TextView msgResponse;
    private EditText etName, etBio;

    private User user;
    private int userId;

    private static final String BASE_URL = "http://10.0.2.2:3002/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_obj_req);

        userId = getIntent().getIntExtra("USER_ID", 1);

        btnJsonObjReq    = findViewById(R.id.btnJsonObj);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        msgResponse      = findViewById(R.id.msgResponse);
        etName           = findViewById(R.id.etName);
        etBio            = findViewById(R.id.etBio);

        btnJsonObjReq.setOnClickListener(v -> makeJsonObjReq());

        btnUpdateProfile.setOnClickListener(v -> {
            if (user != null) {
                updateUserProfile();
            } else {
                Toast.makeText(this, "Fetch profile first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeJsonObjReq() {
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + userId;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("Volley Response", response.toString());

                        List<String> hobbies = new ArrayList<>();
                        JSONArray hobbiesJson = response.getJSONArray("hobbies");
                        for (int i = 0; i < hobbiesJson.length(); i++) {
                            hobbies.add(hobbiesJson.getString(i));
                        }

                        user = new User(
                                response.getInt("userId"),
                                response.getString("name"),
                                response.getString("email"),
                                response.getString("passwordHash"),
                                response.getString("bio"),
                                hobbies,
                                response.getString("role"),
                                response.getDouble("latitude"),
                                response.getDouble("longitude"),
                                response.getString("createdAt"),
                                response.getBoolean("isActive")
                        );

                        refreshUI();

                        // Pre-fill EditTexts for editing
                        etName.setText(user.getName());
                        etBio.setText(user.getBio());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        msgResponse.setText("Error parsing user data.");
                    }
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    msgResponse.setText("Failed to load data. Please try again.");
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    private void updateUserProfile() {
        String url = BASE_URL + user.getUserId();

        String updatedName = etName.getText().toString().trim();
        String updatedBio  = etBio.getText().toString().trim();

        if (updatedName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name",         updatedName);
            jsonBody.put("bio",          updatedBio);
            jsonBody.put("email",        user.getEmail());
            jsonBody.put("hobbies",      new JSONArray(user.getHobbies()));
            jsonBody.put("role",         user.getRole());
            jsonBody.put("latitude",     user.getLatitude());
            jsonBody.put("longitude",    user.getLongitude());
            jsonBody.put("isActive",     user.isActive());
            jsonBody.put("passwordHash", user.getPasswordHash());
            jsonBody.put("createdAt",    user.getCreatedAt());
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

                    user.setName(updatedName);
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
        msgResponse.setText(
                "Name: "    + user.getName()               + "\n\n" +
                        "Email: "   + user.getEmail()              + "\n\n" +
                        "Bio: "     + user.getBio()                + "\n\n" +
                        "Hobbies: " + user.getHobbies().toString() + "\n\n" +
                        "Role: "    + user.getRole()
        );
    }
}