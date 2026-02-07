package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FriendActivity extends AppCompatActivity {

    private TextView friendNameTxt;
    private Button likeBtn;
    private Button dislikeBtn;
    private Button nextBtn;
    private Button finishBtn;

    private int score = 0;
    private int friendIndex = 0;
    private String[] friends = {"Alice", "Bob", "Charlie", "Diana"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        friendNameTxt = findViewById(R.id.friend_name_txt);
        likeBtn = findViewById(R.id.like_btn);
        dislikeBtn = findViewById(R.id.dislike_btn);
        nextBtn = findViewById(R.id.next_btn);
        finishBtn = findViewById(R.id.finish_btn);

        showFriend();

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score += 1; // Like adds 1 point
            }
        });

        dislikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score += 0; // Dislike adds 0 point (or you can do -1 if you like)
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendIndex++;
                if(friendIndex >= friends.length){
                    friendIndex = 0; // loop back to first friend
                }
                showFriend();
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendActivity.this, MainActivity.class);
                intent.putExtra("SCORE", score); // pass the score back
                startActivity(intent);
            }
        });

    }

    private void showFriend() {
        friendNameTxt.setText("Friend: " + friends[friendIndex]);
    }
}