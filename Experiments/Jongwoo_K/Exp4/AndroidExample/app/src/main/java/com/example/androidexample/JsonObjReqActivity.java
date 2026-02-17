package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

public class JsonObjReqActivity extends AppCompatActivity {

    // UI components
    private MaterialButton btnJsonObjReq;
    private TextView tvStatus;

    private TextView tvProfileId;
    private TextView tvProfileName;
    private TextView tvProfileUsername;

    private TextView tvContactEmail;
    private TextView tvContactPhone;
    private TextView tvContactWebsite;

    private TextView tvAddressStreet;
    private TextView tvAddressSuite;
    private TextView tvAddressCity;
    private TextView tvAddressZip;

    private TextView tvGeoLat;
    private TextView tvGeoLng;

    private TextView tvCompanyName;
    private TextView tvCompanyCatchPhrase;
    private TextView tvCompanyBs;

    // API URL to fetch JSON object data
    private static final String URL_JSON_OBJECT = "https://jsonplaceholder.typicode.com/users/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_obj_req);

        // Initializing UI components
        btnJsonObjReq = findViewById(R.id.btnJsonObjReq);
        tvStatus = findViewById(R.id.tvStatus);

        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);

        tvContactEmail = findViewById(R.id.tvContactEmail);
        tvContactPhone = findViewById(R.id.tvContactPhone);
        tvContactWebsite = findViewById(R.id.tvContactWebsite);

        tvAddressStreet = findViewById(R.id.tvAddressStreet);
        tvAddressSuite = findViewById(R.id.tvAddressSuite);
        tvAddressCity = findViewById(R.id.tvAddressCity);
        tvAddressZip = findViewById(R.id.tvAddressZip);

        tvGeoLat = findViewById(R.id.tvGeoLat);
        tvGeoLng = findViewById(R.id.tvGeoLng);

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyCatchPhrase = findViewById(R.id.tvCompanyCatchPhrase);
        tvCompanyBs = findViewById(R.id.tvCompanyBs);

        // Set click listener to trigger JSON object request
        btnJsonObjReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeJsonObjReq();
            }
        });

        // Back to Main button
        MaterialButton btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JsonObjReqActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Method to make a JSON object request using the Volley library
     */
    private void makeJsonObjReq() {
        tvStatus.setVisibility(View.GONE);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                URL_JSON_OBJECT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Volley Response", response.toString());
                        bindUserJsonToCards(response);
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

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
    }

    private void bindUserJsonToCards(JSONObject root) {
        try {
            int id = root.optInt("id", Integer.MIN_VALUE);
            tvProfileId.setText(id == Integer.MIN_VALUE ? "-" : String.valueOf(id));
            tvProfileName.setText(root.optString("name", "-"));
            tvProfileUsername.setText(root.optString("username", "-"));

            tvContactEmail.setText(root.optString("email", "-"));
            tvContactPhone.setText(root.optString("phone", "-"));
            tvContactWebsite.setText(root.optString("website", "-"));

            JSONObject address = root.optJSONObject("address");
            tvAddressStreet.setText(address != null ? address.optString("street", "-") : "-");
            tvAddressSuite.setText(address != null ? address.optString("suite", "-") : "-");
            tvAddressCity.setText(address != null ? address.optString("city", "-") : "-");
            tvAddressZip.setText(address != null ? address.optString("zipcode", "-") : "-");

            JSONObject geo = address != null ? address.optJSONObject("geo") : null;
            tvGeoLat.setText(geo != null ? geo.optString("lat", "-") : "-");
            tvGeoLng.setText(geo != null ? geo.optString("lng", "-") : "-");

            JSONObject company = root.optJSONObject("company");
            tvCompanyName.setText(company != null ? company.optString("name", "-") : "-");
            tvCompanyCatchPhrase.setText(company != null ? company.optString("catchPhrase", "-") : "-");
            tvCompanyBs.setText(company != null ? company.optString("bs", "-") : "-");
        } catch (Exception e) {
            Log.e("Volley Error", "JSON bind error", e);
            tvStatus.setText("Response parsing failed");
            tvStatus.setVisibility(View.VISIBLE);
        }
    }
}
