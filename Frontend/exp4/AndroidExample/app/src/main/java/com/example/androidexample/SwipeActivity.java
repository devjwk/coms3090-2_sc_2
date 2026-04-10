package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SwipeActivity extends AppCompatActivity {

    private LinearLayout cardView;
    private TextView tvName, tvBio, tvMajor, tvAge, tvHobbies, tvRole;
    private ImageView ivProfilePhoto;
    private Button btnLike, btnDislike, btnBack;

    private int currentUserId;

    private int nextSwipeUserId = -1;
    private int currentMatchId = -1;
    private String currentMatchStatus = "UNMATCHED";
    private boolean isLoading = false;

    private final Set<Integer> swipedUserIds = new HashSet<>();

    private GestureDetector gestureDetector;

    private static final String BASE_URL = "http://coms-3090-015.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        currentUserId = getIntent().getIntExtra("USER_ID", 1);

        cardView  = findViewById(R.id.cardView);
        tvName    = findViewById(R.id.tvName);
        tvBio     = findViewById(R.id.tvBio);
        tvMajor   = findViewById(R.id.tvMajor);
        tvAge     = findViewById(R.id.tvAge);
        tvHobbies = findViewById(R.id.tvHobbies);
        tvRole    = findViewById(R.id.tvRole);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnLike    = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);
        btnBack    = findViewById(R.id.btnBack);

        fetchNextSwipe();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                }
                return true;
            }
        });

        cardView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        btnLike.setOnClickListener(v -> onSwipeRight());
        btnDislike.setOnClickListener(v -> onSwipeLeft());
        btnBack.setOnClickListener(v -> finish());
    }

    private void onSwipeRight() {
        if (isLoading || currentMatchId < 0) return;
        isLoading = true;
        animateCard(800);

        swipedUserIds.add(nextSwipeUserId);

        String newStatus;
        if ("PENDING".equalsIgnoreCase(currentMatchStatus)) {
            newStatus = "ACCEPTED";
        } else {
            newStatus = "PENDING";
        }

        updateMatchStatus(currentMatchId, newStatus, () -> {
            if ("ACCEPTED".equals(newStatus)) {
                runOnUiThread(() ->
                    Toast.makeText(this, "🎉 It's a match!", Toast.LENGTH_LONG).show()
                );
            } else {
                runOnUiThread(() ->
                    Toast.makeText(this, "Liked! ♥", Toast.LENGTH_SHORT).show()
                );
            }
            fetchNextSwipe();
        });
    }

    private void onSwipeLeft() {
        if (isLoading || currentMatchId < 0) return;
        isLoading = true;
        animateCard(-800);

        swipedUserIds.add(nextSwipeUserId);

        updateMatchStatus(currentMatchId, "DECLINED", this::fetchNextSwipe);
    }

    private void animateCard(float toX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "translationX", 0f, toX);
        animator.setDuration(300);
        animator.start();
        cardView.postDelayed(() -> cardView.setTranslationX(0f), 350);
    }

    private void fetchNextSwipe() {
        String url = BASE_URL + "/matches/next/" + currentUserId;
        Log.d("SwipeActivity", "Fetching next swipe: " + url);

        runOnUiThread(() -> tvName.setText("Loading..."));

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    Log.d("SwipeActivity", "/matches/next raw: " + response);
                    try {
                        JSONObject jsonResponse;
                        String trimmed = response.trim();
                        if (trimmed.startsWith("[")) {
                            JSONArray arr = new JSONArray(trimmed);
                            if (arr.length() == 0) {
                                runOnUiThread(this::showNoMoreUsers);
                                return;
                            }
                            jsonResponse = arr.getJSONObject(0);
                        } else {
                            jsonResponse = new JSONObject(trimmed);
                        }

                        nextSwipeUserId = jsonResponse.optInt("userId",
                                jsonResponse.optInt("user_id",
                                        jsonResponse.optInt("id", -1)));
                        currentMatchId  = jsonResponse.optInt("matchId",
                                jsonResponse.optInt("match_id", -1));

                        String status = jsonResponse.optString("status", "");
                        currentMatchStatus = status.isEmpty() ? "UNMATCHED" : status;

                        Log.d("SwipeActivity", "Next swipe: userId=" + nextSwipeUserId
                                + " matchId=" + currentMatchId + " status=" + currentMatchStatus);

                        if (nextSwipeUserId < 0 || currentMatchId < 0) {
                            runOnUiThread(this::showNoMoreUsers);
                            return;
                        }

                        if (swipedUserIds.contains(nextSwipeUserId)) {
                            Log.d("SwipeActivity", "Already swiped user " + nextSwipeUserId + " — no more profiles");
                            runOnUiThread(this::showNoMoreUsers);
                            return;
                        }

                        if (status.isEmpty()) {
                            fetchMatchStatus(currentMatchId, () -> fetchUser(nextSwipeUserId));
                        } else {
                            fetchUser(nextSwipeUserId);
                        }

                    } catch (Exception e) {
                        Log.e("SwipeActivity", "Error parsing next: " + e.getMessage());
                        runOnUiThread(this::showNoMoreUsers);
                    }
                },
                error -> {
                    Log.e("SwipeActivity", "No more users to swipe: " + error.toString());
                    isLoading = false;
                    runOnUiThread(this::showNoMoreUsers);
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void fetchMatchStatus(int matchId, Runnable onComplete) {
        String url = BASE_URL + "/matches/" + matchId;

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        currentMatchStatus = obj.optString("status", "UNMATCHED");
                        Log.d("SwipeActivity", "Match " + matchId + " status: " + currentMatchStatus);
                    } catch (Exception e) {
                        currentMatchStatus = "UNMATCHED";
                    }
                    onComplete.run();
                },
                error -> {
                    Log.e("SwipeActivity", "Could not fetch match status, defaulting to UNMATCHED");
                    currentMatchStatus = "UNMATCHED";
                    onComplete.run();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void updateMatchStatus(int matchId, String newStatus, Runnable onComplete) {
        String url = BASE_URL + "/matches/edit/" + matchId;
        Log.d("SwipeActivity", "PUT " + url + " → " + newStatus);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Log.d("SwipeActivity", "Match " + matchId + " updated to " + newStatus);
                    onComplete.run();
                },
                error -> {
                    Log.e("SwipeActivity", "Failed to update match " + matchId + ": " + error.toString());
                    onComplete.run();
                }
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject body = new JSONObject();
                    body.put("status", newStatus);
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void showNoMoreUsers() {
        isLoading = false;
        tvName.setText("No more profiles!");
        tvBio.setText("");
        tvMajor.setText("");
        tvAge.setText("");
        tvHobbies.setText("");
        tvRole.setText("");
        ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);
    }

    private void fetchUser(int userId) {
        String url = BASE_URL + "/users/" + userId;
        Log.d("SwipeActivity", "Fetching user profile: " + url);

        runOnUiThread(() -> ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_gallery));

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    Log.d("SwipeActivity", "User response: " + response);
                    try {
                        JSONObject userObj = new JSONObject(response);

                        String name = safeString(userObj, "name");
                        if (name.isEmpty()) name = safeString(userObj, "displayName");
                        if (name.isEmpty()) name = "User " + userId;

                        String bio     = safeString(userObj, "bio");
                        String major   = safeString(userObj, "major");
                        int age        = userObj.optInt("age", 0);
                        String hobbies = safeString(userObj, "hobbies");
                        String role    = safeString(userObj, "role");

                        final String displayName = name;
                        runOnUiThread(() -> {
                            tvName.setText(displayName);
                            tvBio.setText(bio.isEmpty() ? "" : "Bio: " + bio);
                            tvMajor.setText(major.isEmpty() ? "" : "Major: " + major);
                            tvAge.setText(age > 0 ? "Age: " + age : "");
                            tvHobbies.setText(hobbies.isEmpty() ? "" : "Hobbies: " + hobbies);
                            tvRole.setText(role.isEmpty() ? "" : "Role: " + role);

                            btnLike.setEnabled(true);
                            btnDislike.setEnabled(true);
                            isLoading = false;
                        });

                        fetchUserPhoto(userId);
                    } catch (Exception e) {
                        Log.e("SwipeActivity", "Error parsing user " + userId + ": " + e.getMessage());
                        isLoading = false;
                        runOnUiThread(this::showNoMoreUsers);
                    }
                },
                error -> {
                    Log.e("SwipeActivity", "Error fetching user " + userId + ": " + error.toString());
                    isLoading = false;
                    runOnUiThread(this::showNoMoreUsers);
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private String safeString(JSONObject obj, String key) {
        if (obj.isNull(key)) return "";
        return obj.optString(key, "");
    }

    private void fetchUserPhoto(int userId) {
        String url = BASE_URL + "/image/user/" + userId;
        Log.d("SwipeActivity", "GET photo: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    Log.d("SwipeActivity", "Image raw for user " + userId + ": " + response);
                    try {
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() == 0) {
                            loadPlaceholderAvatar(userId);
                            return;
                        }

                        JSONObject imgObj = arr.getJSONObject(0);
                        Log.d("SwipeActivity", "Image obj: " + imgObj);

                        String path = "";
                        Iterator<String> keys = imgObj.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String val = imgObj.optString(key, "");
                            if (val.contains("/files/") || val.contains("/images/")
                                    || val.endsWith(".jpg") || val.endsWith(".png")
                                    || val.endsWith(".jpeg") || val.endsWith(".webp")) {
                                path = val;
                                Log.d("SwipeActivity", "Path found in key '" + key + "' → " + val);
                                break;
                            }
                        }

                        if (path.isEmpty()) {
                            String[] tryKeys = {"path", "filePath", "url", "imageUrl", "imagePath", "link", "file", "fileName", "image"};
                            for (String k : tryKeys) {
                                if (imgObj.has(k) && !imgObj.isNull(k)) {
                                    path = imgObj.getString(k);
                                    break;
                                }
                            }
                        }

                        String imageUrl;
                        if (!path.isEmpty()) {
                            imageUrl = path.startsWith("http") ? path
                                    : BASE_URL + (path.startsWith("/") ? path : "/" + path);
                        } else {
                            int imageId = imgObj.optInt("imageId", imgObj.optInt("image_id", imgObj.optInt("id", -1)));
                            if (imageId > 0) {
                                imageUrl = BASE_URL + "/files/images/" + imageId + "/" + imageId + ".jpg";
                            } else {
                                loadPlaceholderAvatar(userId);
                                return;
                            }
                        }

                        Log.d("SwipeActivity", "Loading photo: " + imageUrl);
                        final String fUrl = imageUrl;
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) {
                                Glide.with(SwipeActivity.this)
                                        .load(fUrl)
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_menu_gallery)
                                        .centerCrop()
                                        .into(ivProfilePhoto);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("SwipeActivity", "Error parsing image: " + e.getMessage());
                        loadPlaceholderAvatar(userId);
                    }
                },
                error -> {
                    Log.d("SwipeActivity", "Image API unavailable for user " + userId);
                    loadPlaceholderAvatar(userId);
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void loadPlaceholderAvatar(int userId) {
        String placeholderUrl = "https://picsum.photos/seed/user" + userId + "/300/300";
        Log.d("SwipeActivity", "Loading placeholder: " + placeholderUrl);
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                Glide.with(SwipeActivity.this)
                        .load(placeholderUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(ivProfilePhoto);
            }
        });
    }
}
