package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.Collections;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;

import android.widget.Toast;
public class GroupRecommendActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";
    private RecyclerView recyclerViewRecommend;

    private RecyclerView recyclerViewSearch;
    private EditText etSearchKeyword;

    private GroupRecommendAdapter searchAdapter;

    private List<RecommendGroup> allGroups = new ArrayList<>();
    private List<RecommendGroup> filteredGroups = new ArrayList<>();

    private GroupRecommendAdapter adapter;

    private Button btnShowSearch, btnShowRecommend;
    private LinearLayout layoutSearchSection, layoutRecommendSection;

    private TextView tvInterest1, tvInterest2, tvInterest3;

    private Spinner spinnerCategory;
    private String selectedCategory = "All";

    private Spinner spinnerSort;
    private String selectedSort = "Alphabetical";
    private TextView btnBack;

    private int userId;
    private String userJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_recommend);

        // getting intent data
        userId = getIntent().getIntExtra("USER_ID", 1);
        userJson = getIntent().getStringExtra("USER_JSON");

        // link view
        recyclerViewRecommend = findViewById(R.id.recyclerViewRecommend);

        btnShowSearch = findViewById(R.id.btnShowSearch);
        btnShowRecommend = findViewById(R.id.btnShowRecommend);

        layoutSearchSection = findViewById(R.id.layoutSearchSection);
        layoutRecommendSection = findViewById(R.id.layoutRecommendSection);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSort = findViewById(R.id.spinnerSort);

        tvInterest1 = findViewById(R.id.tvInterest1);
        tvInterest2 = findViewById(R.id.tvInterest2);
        tvInterest3 = findViewById(R.id.tvInterest3);
        //find view by id
        recyclerViewSearch = findViewById(R.id.recyclerViewSearch);
        etSearchKeyword = findViewById(R.id.etSearchKeyword);

        //search recyclerView setting
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new GroupRecommendAdapter(filteredGroups, group -> {
            joinGroup(group);
        });
        recyclerViewSearch.setAdapter(searchAdapter);


        btnBack = findViewById(R.id.btnBack);
        //set recyclerView
        recyclerViewRecommend.setLayoutManager(new LinearLayoutManager(this));

        String[] categories = {"All"};
        String[] sortsOptions = {"Alphabetical"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortsOptions);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);


        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        filteredGroups.addAll(allGroups);
        searchAdapter.updateData(filteredGroups);

        //link adapter to recyclerView
        adapter = new GroupRecommendAdapter(allGroups, group -> {
            joinGroup(group);
        });
        recyclerViewRecommend.setAdapter(adapter);
        fetchRecommendedGroups();
        searchGroupsFromBackend("");



        // button click - Search / Recommend conversion
        btnShowSearch.setOnClickListener(v -> {
            layoutSearchSection.setVisibility(View.VISIBLE);
            layoutRecommendSection.setVisibility(View.GONE);

            btnShowSearch.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            btnShowRecommend.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        btnShowRecommend.setOnClickListener(v -> {
            layoutSearchSection.setVisibility(View.GONE);
            layoutRecommendSection.setVisibility(View.VISIBLE);

            btnShowRecommend.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_light));
            btnShowSearch.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        etSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchGroupsFromBackend(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                searchGroupsFromBackend(etSearchKeyword.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "All";
            }
        });

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = parent.getItemAtPosition(position).toString();
                searchGroupsFromBackend(etSearchKeyword.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSort = "Alphabetical";
            }
        });

        // display user interests
        setUserInterests();



        btnBack.setOnClickListener(v -> finish());
    }


    // getting hobbies from user json and display it
    private void setUserInterests() {
        if (userJson == null) return;

        try {
            JSONObject jo = new JSONObject(userJson);

            if (jo.has("interests") && !jo.isNull("interests")) {
                JSONArray interests = jo.getJSONArray("interests");

                if (interests.length() > 0)
                    tvInterest1.setText(interests.getString(0));

                if (interests.length() > 1)
                    tvInterest2.setText(interests.getString(1));

                if (interests.length() > 2)
                    tvInterest3.setText(interests.getString(2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getUserInterests() {
        List<String> userInterests = new ArrayList<>();

        if (userJson == null) return userInterests;

        try {
            JSONObject jo = new JSONObject(userJson);

            if (jo.has("interests") && !jo.isNull("interests")) {
                JSONArray interests = jo.getJSONArray("interests");

                for (int i = 0; i < interests.length(); i++) {
                    userInterests.add(interests.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userInterests;
    }

    private int calculateMatchScore(List<String> userInterests, List<String> groupInterests) {
        if (userInterests == null || userInterests.isEmpty() ||
                groupInterests == null || groupInterests.isEmpty()) {
            return 0;
        }

        int overlap = 0;

        for (String userInterest : userInterests) {
            for (String groupInterest : groupInterests) {
                String u = userInterest.trim().toLowerCase();
                String g = groupInterest.trim().toLowerCase();

                if (u.equals(g) || u.contains(g) || g.contains(u)) {
                    overlap++;
                }
            }
        }

        return (overlap * 100) / userInterests.size();
    }

    private void filterGroups(String keyword) {
        filteredGroups.clear();

        String lowerKeyword = keyword.toLowerCase().trim();

        for (RecommendGroup group : allGroups) {
            boolean matchesCategory = selectedCategory.equals("All") ||
                    group.getCategory().equalsIgnoreCase(selectedCategory);

            boolean matchesKeyword;

            if (lowerKeyword.isEmpty()) {
                matchesKeyword = true;
            } else {
                boolean matchesName = group.getGroupName().toLowerCase().contains(lowerKeyword);
                boolean matchesDescription = group.getDescription().toLowerCase().contains(lowerKeyword);
                boolean matchesGroupCategory = group.getCategory().toLowerCase().contains(lowerKeyword);

                boolean matchesMatchedKeyword = false;
                for (String k : group.getMatchedKeywords()) {
                    if (k.toLowerCase().contains(lowerKeyword)) {
                        matchesMatchedKeyword = true;
                        break;
                    }
                }

                matchesKeyword = matchesName || matchesDescription || matchesGroupCategory || matchesMatchedKeyword;
            }

            if (matchesCategory && matchesKeyword) {
                filteredGroups.add(group);
            }
        }

        if (selectedSort.equals("Best Match")) {
            Collections.sort(filteredGroups, (g1, g2) ->
                    Integer.compare(g2.getMatchScore(), g1.getMatchScore()));
        } else if (selectedSort.equals("Most Members")) {
            Collections.sort(filteredGroups, (g1, g2) ->
                    Integer.compare(g2.getMemberCount(), g1.getMemberCount()));
        } else if (selectedSort.equals("Alphabetical")) {
            Collections.sort(filteredGroups, (g1, g2) ->
                    g1.getGroupName().compareToIgnoreCase(g2.getGroupName()));
        }

        searchAdapter.updateData(filteredGroups);
    }
    private void joinGroup(RecommendGroup group) {
        String url = BASE_URL + "/gm/join";

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("group_id", group.getGroupId());
            body.put("is_moderator", false);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build join request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("JOIN_GROUP", "URL: " + url);
        Log.d("JOIN_GROUP", "userId: " + userId);
        Log.d("JOIN_GROUP", "groupId: " + group.getGroupId());
        Log.d("JOIN_GROUP", "body: " + body.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Log.d("JOIN_GROUP_SUCCESS", response.toString());

                    String message = response.optString("message", "failure");
                    if (message.equals("success")) {
                        Toast.makeText(this,
                                "Joined group: " + group.getGroupName(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Join failed: " + response.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("JOIN_GROUP_ERROR", error.toString());

                    if (error.networkResponse != null) {
                        Log.e("JOIN_GROUP_ERROR", "Status code: " + error.networkResponse.statusCode);

                        try {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e("JOIN_GROUP_ERROR", "Error body: " + errorBody);
                            Toast.makeText(this,
                                    "Join failed (" + error.networkResponse.statusCode + "): " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this,
                                    "Join failed with status: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,
                                "Network error: " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    private void fetchRecommendedGroups() {
        String url = BASE_URL + "/groups/recommend/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allGroups.clear();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int groupId = obj.optInt("groupId");
                            String groupName = obj.optString("groupName", "");
                            String description = obj.optString("description", "");

                            JSONArray interestsArray = obj.optJSONArray("interests");
                            List<String> interests = new ArrayList<>();

                            if (interestsArray != null) {
                                for (int j = 0; j < interestsArray.length(); j++) {
                                    interests.add(interestsArray.optString(j));
                                }
                            }
                            List<String> userInterests = getUserInterests();
                            int matchScore = calculateMatchScore(userInterests, interests);

                            allGroups.add(new RecommendGroup(
                                    groupId,
                                    groupName,
                                    description,
                                    "All",
                                    0,
                                    interests,
                                    matchScore
                            ));
                        }

                        adapter.updateData(allGroups);
                        Log.d("RECOMMEND_FETCH", "Loaded recommended groups: " + allGroups.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse recommended groups", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("RECOMMEND_FETCH_ERROR", error.toString());
                    Toast.makeText(this, "Failed to load recommended groups", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void searchGroupsFromBackend(String keyword) {
        String url;

        if (keyword == null || keyword.trim().isEmpty()) {
            url = BASE_URL + "/groups";
        } else {
            url = BASE_URL + "/groups/search?keyword=" + keyword.trim();
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    filteredGroups.clear();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            int groupId = obj.optInt("groupId");
                            String groupName = obj.optString("groupName", "");
                            String description = obj.optString("description", "");

                            JSONArray interestsArray = obj.optJSONArray("interests");
                            List<String> interests = new ArrayList<>();

                            if (interestsArray != null) {
                                for (int j = 0; j < interestsArray.length(); j++) {
                                    interests.add(interestsArray.optString(j));
                                }
                            }
                            List<String> userInterests = getUserInterests();
                            int matchScore = calculateMatchScore(userInterests, interests);

                            filteredGroups.add(new RecommendGroup(
                                    groupId,
                                    groupName,
                                    description,
                                    "All",
                                    0,
                                    interests,
                                    matchScore
                            ));
                        }

                        if (selectedSort.equals("Alphabetical")) {
                            Collections.sort(filteredGroups, (g1, g2) ->
                                    g1.getGroupName().compareToIgnoreCase(g2.getGroupName()));
                        }

                        searchAdapter.updateData(filteredGroups);
                        Log.d("SEARCH_FETCH", "Loaded search groups: " + filteredGroups.size());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse search results", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SEARCH_FETCH_ERROR", error.toString());
                    Toast.makeText(this, "Failed to search groups", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}