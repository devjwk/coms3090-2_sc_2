package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonArrReqActivity extends AppCompatActivity {

    private MaterialButton btnJsonArrReq;
    private TextView tvStatus;
    private LinearLayout containerUserList;

    private static final String URL_JSON_ARRAY = "https://jsonplaceholder.typicode.com/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_arr_req);

        btnJsonArrReq = findViewById(R.id.btnJsonArrReq);
        tvStatus = findViewById(R.id.tvStatus);
        containerUserList = findViewById(R.id.containerUserList);

        btnJsonArrReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeJsonArrayReq();
            }
        });

        MaterialButton btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JsonArrReqActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void makeJsonArrayReq() {
        tvStatus.setVisibility(View.GONE);

        JsonArrayRequest jsonArrReq = new JsonArrayRequest(
                Request.Method.GET,
                URL_JSON_ARRAY,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Volley Response", response.toString());

                        if (response.length() > 0) {
                            try {
                                bindAllUsersBasic(response);
                            } catch (Exception e) {
                                Log.e("Volley Error", "JSON parse error", e);
                                tvStatus.setText("Response parsing failed");
                                tvStatus.setVisibility(View.VISIBLE);
                            }
                        } else {
                            tvStatus.setText("No data available");
                            tvStatus.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        tvStatus.setText("Failed to load data. Please try again.");
                        tvStatus.setVisibility(View.VISIBLE);
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrReq);
    }

    private void bindAllUsersBasic(JSONArray array) {
        containerUserList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < array.length(); i++) {
            JSONObject user = array.optJSONObject(i);
            if (user == null) continue;

            View card = inflater.inflate(R.layout.item_user_basic, containerUserList, false);

            TextView tvId = card.findViewById(R.id.tvProfileId);
            TextView tvName = card.findViewById(R.id.tvProfileName);
            TextView tvUsername = card.findViewById(R.id.tvProfileUsername);

            int id = user.optInt("id", Integer.MIN_VALUE);
            tvId.setText(id == Integer.MIN_VALUE ? "-" : String.valueOf(id));
            tvName.setText(user.optString("name", "-"));
            tvUsername.setText(user.optString("username", "-"));

            containerUserList.addView(card);
        }
    }
}
