package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class JsonObjReqActivity extends AppCompatActivity {

    // UI Components
    private Button btnJsonObjReq, btnUpdateProfile;
    private TextView msgResponse;
    private EditText etName, etBio;

    // Make User a field so it's accessible everywhere
    private User user;

    // API URL
    private static final String URL_JSON_OBJECT = "http://10.0.2.2:3002/users/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_obj_req);

        // Initialize UI components
        btnJsonObjReq = findViewById(R.id.btnJsonObj);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        msgResponse = findViewById(R.id.msgResponse);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);

        // Fetch profile button
        btnJsonObjReq.setOnClickListener(v -> makeJsonObjReq());

        // Update profile button
        btnUpdateProfile.setOnClickListener(v -> {
            if (user != null) {
                updateUserProfile();
            } else {
                Toast.makeText(this, "Fetch profile first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeJsonObjReq() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                URL_JSON_OBJECT,
                null,
                response -> {
                    try {
                        Log.d("Volley Response", response.toString());

                        // Parse hobbies
                        List<String> hobbies = new ArrayList<>();
                        JSONArray hobbiesJson = response.getJSONArray("hobbies");
                        for (int i = 0; i < hobbiesJson.length(); i++) {
                            hobbies.add(hobbiesJson.getString(i));
                        }

                        // Assign to field
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

                        // Show info in UI
                        msgResponse.setText(
                                "Name: " + user.getName() + "\n\n" +
                                        "Email: " + user.getEmail() + "\n\n" +
                                        "Bio: " + user.getBio() + "\n\n" +
                                        "Hobbies: " + user.getHobbies().toString() + "\n\n" +
                                        "Role: " + user.getRole()
                        );

                        // Pre-fill EditTexts
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
        String URL_UPDATE_USER = "http://10.0.2.2:3002/users/" + user.getUserId();

        // Prepare JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", etName.getText().toString());
            jsonBody.put("bio", etBio.getText().toString());
            jsonBody.put("email", user.getEmail());
            jsonBody.put("hobbies", new JSONArray(user.getHobbies()));
            jsonBody.put("role", user.getRole());
            jsonBody.put("latitude", user.getLatitude());
            jsonBody.put("longitude", user.getLongitude());
            jsonBody.put("isActive", user.isActive());
            jsonBody.put("passwordHash", user.getPasswordHash());
            jsonBody.put("createdAt", user.getCreatedAt());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                URL_UPDATE_USER,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("PUT Response", response.toString());
                },
                error -> {
                    Log.e("PUT Error", error.toString());
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }
}