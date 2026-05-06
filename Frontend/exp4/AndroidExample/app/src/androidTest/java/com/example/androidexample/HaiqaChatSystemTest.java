package com.example.androidexample;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class HaiqaChatSystemTest {

    private static Intent getIntent() {

        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                ChatActivity.class);

        i.putExtra("USER_ID",1);
        i.putExtra("OTHER_USER_ID",2);
        i.putExtra("OTHER_USERNAME","Test");

        return i;
    }

    @Rule
    public ActivityScenarioRule<ChatActivity> rule =
            new ActivityScenarioRule<>(getIntent());

    @Test
    public void t01_sendEmptyMessage() {
        onView(withId(R.id.sendBtn)).perform(click());
    }

    @Test
    public void t02_typeMessage() {
        onView(withId(R.id.msgEdt))
                .perform(typeText("hello"),
                        closeSoftKeyboard());
    }

    @Test
    public void t03_sendTypedMessage() {
        onView(withId(R.id.msgEdt))
                .perform(typeText("hello"),
                        closeSoftKeyboard());

        onView(withId(R.id.sendBtn)).perform(click());
    }

    @Test
    public void t04_clearAndSend() {
        onView(withId(R.id.msgEdt))
                .perform(typeText("hello"),
                        clearText(),
                        closeSoftKeyboard());

        onView(withId(R.id.sendBtn)).perform(click());
    }

    @Test
    public void t05_backButton() {
        onView(withId(R.id.backBtn)).perform(click());
    }

    @Test
    public void t06_chatRecyclerExists() {
        onView(withId(R.id.recyclerChat))
                .check(matches(isDisplayed()));
    }

    @Test
    public void t07_chatWithExists() {
        onView(withId(R.id.tvChatWith))
                .check(matches(isDisplayed()));
    }
}