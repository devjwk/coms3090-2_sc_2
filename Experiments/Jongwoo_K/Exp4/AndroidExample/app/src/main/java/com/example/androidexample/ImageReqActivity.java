package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ImageReqActivity extends AppCompatActivity {

    private MaterialButton btnImageReq;
    private ImageView imageView;
    private TextInputEditText etImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_image_req);

        btnImageReq = findViewById(R.id.btnImageReq);
        imageView = findViewById(R.id.imgView);
        etImageUrl = findViewById(R.id.etImageUrl);

        btnImageReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeImageRequest();
            }
        });

        MaterialButton btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImageReqActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void makeImageRequest() {
        String url = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Please enter an image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        // Called when the image is successfully fetched
                        // Set the fetched image (Bitmap) to the ImageView
                        imageView.setImageBitmap(response);
                        // Log a success message
                        Log.d("Image Request", "Image loaded successfully.");
                    }
                },
                0, // Width of the image (0 to use the original width)
                0, // Height of the image (0 to use the original height)
                ImageView.ScaleType.FIT_XY, // Scale type for the image (adjust to fit)
                Bitmap.Config.RGB_565, // Bitmap configuration for better memory usage
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Called when an error occurs during the image request
                        // Log the error for debugging purposes
                        Log.e("Volley Error", error.toString());
                        // Display a Toast message to the user about the error
                        Toast.makeText(ImageReqActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the ImageRequest to the Volley request queue for processing
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }
}
