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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
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

    private Button btnManageMembers;
    private Button btnBack;

    private int currentUserId;

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080/groups";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        msgResponse = findViewById(R.id.msgResponse);

        etGroupName = findViewById(R.id.etGroupName);
        etGroupDesc = findViewById(R.id.etGroupDesc);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        etGetGroupId = findViewById(R.id.etGetGroupId);
        btnGetAllGroups = findViewById(R.id.btnGetAllGroups);
        btnGetGroupById = findViewById(R.id.btnGetGroupById);

        etEditGroupId = findViewById(R.id.etEditGroupId);
        etEditGroupName = findViewById(R.id.etEditGroupName);
        etEditGroupDesc = findViewById(R.id.etEditGroupDesc);
        btnEditGroup = findViewById(R.id.btnEditGroup);

        etDeleteGroupId = findViewById(R.id.etDeleteGroupId);
        btnDeleteGroup = findViewById(R.id.btnDeleteGroup);

        btnManageMembers = findViewById(R.id.btnManageMembers);
        btnBack = findViewById(R.id.btnBack);

        btnCreateGroup.setOnClickListener(v -> createGroup());
        btnGetAllGroups.setOnClickListener(v -> getAllGroups());
        btnGetGroupById.setOnClickListener(v -> getGroupById());
        btnEditGroup.setOnClickListener(v -> editGroup());
        btnDeleteGroup.setOnClickListener(v -> deleteGroup());

        btnManageMembers.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, GroupMembershipActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
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
            body.put("groupName", name);
            body.put("description", desc);
            body.put("createdBy", currentUserId);
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
                                    obj.optInt("groupId"),
                                    obj.optString("groupName", ""),
                                    obj.optString("description", ""),
                                    obj.optInt("createdBy"),
                                    obj.optString("createdAt", "")
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
                    msgResponse.setText(
                            "ID: " + response.optInt("groupId") + "\n\n" +
                                    "Name: " + response.optString("groupName", "") + "\n\n" +
                                    "Description: " + response.optString("description", "") + "\n\n" +
                                    "Created By: " + response.optInt("createdBy") + "\n\n" +
                                    "Created At: " + response.optString("createdAt", "")
                    );
                },
                error -> {
                    Log.e("Get Group Error", error.toString());
                    msgResponse.setText("Failed to load group.");
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void editGroup() {
        String idStr = etEditGroupId.getText().toString().trim();
        String name = etEditGroupName.getText().toString().trim();
        String desc = etEditGroupDesc.getText().toString().trim();

        if (idStr.isEmpty()) {
            Toast.makeText(this, "Enter a group ID to edit.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/edit/" + idStr;

        JSONObject body = new JSONObject();
        try {
            body.put("groupName", name);
            body.put("description", desc);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    Log.d("Edit Group", response.toString());
                    msgResponse.setText("Group updated!\n\n" + response.toString());
                    Toast.makeText(this, "Group updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Edit Group Error", error.toString());
                    Toast.makeText(this, "Failed to update group.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void deleteGroup() {
        String idStr = etDeleteGroupId.getText().toString().trim();
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Enter a group ID to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/" + idStr;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d("Delete Group", response);
                    msgResponse.setText("Group deleted!");
                    Toast.makeText(this, "Group deleted!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("Delete Group Error", error.toString());
                    Toast.makeText(this, "Failed to delete group.", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

}