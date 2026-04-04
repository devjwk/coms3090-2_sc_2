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

/**
 * Activity that provides functionality to delete a user account from the system.
 * It takes a User ID as input and sends a DELETE request to the backend server.
 */
public class DeleteUserActivity extends AppCompatActivity {

    /** Input field for the User ID to be deleted */
    private EditText etUserId;

    /** Button to trigger the delete operation */
    private Button btnDelete;

    /** Button to return to the previous screen */
    private Button btnBackToMain;

    /** Base URL for the user deletion endpoint */
    private static final String URL_DELETE_USER = "http://coms-3090-015.class.las.iastate.edu:8080/users/";

    /**
     * Initializes the activity, sets up the UI components, and defines
     * click listeners for the delete and back buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
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
                finish();
            }
        });
    }

    /**
     * Retrieves the User ID from the input field and sends a DELETE request
     * to the backend server. Displays a success message and closes the activity
     * upon successful deletion, or an error message if the operation fails.
     */
    private void performDelete() {
        String userId = etUserId.getText().toString().trim();
        if (userId.isEmpty()) {
            Toast.makeText(this, "Enter User ID", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = URL_DELETE_USER + userId;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Volley Delete Rsp", response);
                        Toast.makeText(getApplicationContext(), "User Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
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
