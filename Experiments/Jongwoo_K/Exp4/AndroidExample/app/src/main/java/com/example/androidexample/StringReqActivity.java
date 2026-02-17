package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

public class StringReqActivity extends AppCompatActivity {

    // UI components
    private MaterialButton btnStringReq;
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

    // API URL for fetching string response
    private static final String URL_STRING_REQ = "https://jsonplaceholder.typicode.com/users/1";
    // Alternative URLs for testing purposes
    // public static final String URL_STRING_REQ = "https://2aa87adf-ff7c-45c8-89bc-f3fbfaa16d15.mock.pstmn.io/users/1";
    // public static final String URL_STRING_REQ = "http://10.0.2.2:8080/users/1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_string_req);

        // Initializing UI components
        btnStringReq = findViewById(R.id.btnStringReq);
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

        // Setting click listener on the button to trigger the string request
        btnStringReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeStringReq();
            }
        });
    }

    /**
     * Makes a string request using Volley library
     **/
    private void makeStringReq() {
        tvStatus.setVisibility(View.GONE);

        // Creating a new String request
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, // HTTP method (GET request)
                URL_STRING_REQ, // API URL
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the response for debugging purposes
                        Log.d("Volley Response", response);

                        bindUserJsonToCards(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log the error details
                        Log.e("Volley Error", error.toString());

                        // Show an error message in the UI
                        tvStatus.setText("Failed to load data. Please try again.");
                        tvStatus.setVisibility(View.VISIBLE);
                    }
                }
        );

        // Adding request to the Volley request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void bindUserJsonToCards(String response) {
        try {
            JSONObject root = new JSONObject(response);

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
        } catch (JSONException e) {
            Log.e("Volley Error", "JSON parse error", e);
            tvStatus.setText("Response parsing failed");
            tvStatus.setVisibility(View.VISIBLE);
        }
    }
}
