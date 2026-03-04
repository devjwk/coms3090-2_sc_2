package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private TextView msgResponse;

    private EditText etGroupName, etGroupDesc;
    private Button btnCreateGroup;

    private EditText etGetGroupId;
    private Button btnGetAllGroups, btnGetGroupById;

    private EditText etEditGroupId, etEditGroupName, etEditGroupDesc;
    private Button btnEditGroup;

    private EditText etDeleteGroupId;
    private Button btnDeleteGroup;

    private Button btnBack;

    private int currentUserId;

   // private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/groups";
       private static final String BASE_URL = "http://10.0.2.2:3002/groups/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        msgResponse = findViewById(R.id.msgResponse);

        etGroupName   = findViewById(R.id.etGroupName);
        etGroupDesc   = findViewById(R.id.etGroupDesc);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        etGetGroupId    = findViewById(R.id.etGetGroupId);
        btnGetAllGroups = findViewById(R.id.btnGetAllGroups);
        btnGetGroupById = findViewById(R.id.btnGetGroupById);

        etEditGroupId   = findViewById(R.id.etEditGroupId);
        etEditGroupName = findViewById(R.id.etEditGroupName);
        etEditGroupDesc = findViewById(R.id.etEditGroupDesc);
        btnEditGroup    = findViewById(R.id.btnEditGroup);

        etDeleteGroupId = findViewById(R.id.etDeleteGroupId);
        btnDeleteGroup  = findViewById(R.id.btnDeleteGroup);

        btnBack = findViewById(R.id.btnBack);

        btnCreateGroup.setOnClickListener(v -> createGroup());
        btnGetAllGroups.setOnClickListener(v -> getAllGroups());
        btnGetGroupById.setOnClickListener(v -> getGroupById());


    }

    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String desc = etGroupDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("name",        name);
            body.put("description", desc);
            body.put("created_by",  currentUserId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                body,
                response -> {
                    Log.d("Create Group", response.toString());
                    msgResponse.setText("Group created!\n\n" + response.toString());
                    Toast.makeText(this, "Group created!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Create Group Error", error.toString());
                    Toast.makeText(this, "Failed to create group.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void getAllGroups() {
        msgResponse.setText("Loading all groups...");


        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    try {
                        List<Group> groups = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            groups.add(new Group(
                                    obj.optInt("group_id"),
                                    obj.optString("name", ""),
                                    obj.optString("description", ""),
                                    obj.optInt("created_by"),
                                    obj.optString("created_at", "")
                            ));
                        }


                        StringBuilder sb = new StringBuilder("All Groups:\n\n");
                        for (Group g : groups) {
                            sb.append("ID: ").append(g.getGroupId()).append("\n");
                            sb.append("Name: ").append(g.getName()).append("\n");
                            sb.append("Description: ").append(g.getDescription()).append("\n");
                            sb.append("Created By: ").append(g.getCreatedBy()).append("\n");
                            sb.append("Created At: ").append(g.getCreatedAt()).append("\n\n");
                        }


                        msgResponse.setText(sb.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        msgResponse.setText("Error parsing groups.");
                    }
                },
                error -> {
                    Log.e("Get Groups Error", error.toString());
                    msgResponse.setText("Failed to load groups.");
                }
        );


        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void getGroupById() {
        String idStr = etGetGroupId.getText().toString().trim();
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Enter a group ID.", Toast.LENGTH_SHORT).show();
            return;
        }


        String url = BASE_URL + "/" + idStr;
        msgResponse.setText("Loading group...");


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        msgResponse.setText(
                                "ID: "          + response.optInt("group_id")          + "\n\n" +
                                        "Name: "        + response.optString("name", "")       + "\n\n" +
                                        "Description: " + response.optString("description", "") + "\n\n" +
                                        "Created By: "  + response.optInt("created_by")        + "\n\n" +
                                        "Created At: "  + response.optString("created_at", "")
                        );
                    } catch (Exception e) {
                        msgResponse.setText("Error parsing group.");
                    }
                },
                error -> {
                    Log.e("Get Group Error", error.toString());
                    msgResponse.setText("Failed to load group.");
                }
        );


        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }




}