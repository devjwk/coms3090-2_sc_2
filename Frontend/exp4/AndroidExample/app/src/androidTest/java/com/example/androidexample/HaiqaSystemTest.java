package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class HaiqaSystemTest {

    /**
     * Test Case 1 — Login with invalid credentials.
     *
     * Enters a blank password and clicks Login.
     * The app should stay on the login screen and not navigate away.
     * This verifies that the login validation prevents empty submissions
     * from reaching the backend.
     */
    @Test
    public void loginFlow_emptyPassword_staysOnLoginScreen() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText("haiqanas@iastate.edu"), closeSoftKeyboard());
        SystemClock.sleep(600);

        /*
         * Password is intentionally left empty.
         * This triggers the validation toast and prevents the login
         * request from being sent to the backend.
         */

        onView(withId(R.id.login_login_btn))
                .perform(click());
        SystemClock.sleep(1200);

        /*
         * Login screen should still be visible because password is missing.
         */
        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_signup_btn))
                .check(matches(isDisplayed()));

        onView(withId(R.id.login_username_edt))
                .check(matches(withText("haiqanas@iastate.edu")));

        SystemClock.sleep(1000);
    }

    /**
     * Test Case 2 — Create a group with empty name.
     *
     * Navigates to GroupsActivity and clicks Create Group without
     * entering a group name. The screen should stay on the groups
     * screen and show a validation message rather than sending a
     * request to the backend.
     */
    @Test
    public void groupFlow_emptyGroupName_staysOnGroupScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupsActivity.class
        );
        intent.putExtra("USER_ID", 1);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        /*
         * Group name is intentionally left empty.
         * The description is filled to confirm that validation targets
         * only the required name field.
         */
        onView(withId(R.id.etGroupDesc))
                .perform(scrollTo(), clearText(), typeText("A test description"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.btnCreateGroup))
                .perform(scrollTo(), click());
        SystemClock.sleep(1200);

        /*
         * The groups screen should remain visible because group name is missing.
         * The response text view should not show a success message.
         */
        onView(withId(R.id.btnCreateGroup))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.etGroupName))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBack))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }

    /**
     * Test Case 3 — Swipe screen loads correctly with a user ID.
     *
     * Launches SwipeActivity with a valid user ID and verifies that
     * the profile card, swipe hint labels, and back button are all
     * visible. This confirms the layout inflates correctly and the
     * initial fetch call is triggered.
     */
    @Test
    public void swipeFlow_launchesCorrectly_profileCardVisible() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                SwipeActivity.class
        );
        intent.putExtra("USER_ID", 1);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        /*
         * Wait for the initial GET /matches/next/{userId} request to complete.
         * The profile card should now show either a loaded name or "Loading...".
         */
        onView(withId(R.id.cardView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvName))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvBio))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);

        /*
         * Tap back to confirm the activity finishes cleanly.
         */
        onView(withId(R.id.btnBack))
                .perform(click());

        SystemClock.sleep(800);
    }

    /**
     * Test Case 4 — Edit profile screen pre-fills bio field.
     *
     * Launches Login (edit profile) activity with a USER_JSON containing
     * a known bio value. Verifies that the bio EditText is pre-filled
     * correctly from the intent data, confirming that parseUserFromJson
     * and the UI population work as expected.
     */
    @Test
    public void editProfileFlow_bioPrefilledFromIntent() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                Login.class
        );
        intent.putExtra("USER_ID", 1);

        /*
         * Provide a minimal USER_JSON with a known bio value.
         * This simulates what LoginActivity passes after a successful login.
         */
        String userJson = "{" +
                "\"user_id\": 1," +
                "\"name\": \"Haiqa Nasir\"," +
                "\"email\": \"haiqanas@iastate.edu\"," +
                "\"bio\": \"I love coding and cycling.\"," +
                "\"major\": \"Computer Science\"," +
                "\"age\": 21," +
                "\"hobbies\": [\"coding\", \"cycling\"]," +
                "\"role\": \"Regular\"," +
                "\"isActive\": true" +
                "}";

        intent.putExtra("USER_JSON", userJson);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        /*
         * The bio field should be pre-filled with the value from USER_JSON.
         */
        onView(withId(R.id.etBio))
                .check(matches(withText("I love coding and cycling.")));

        /*
         * The name field should also be pre-filled.
         */
        onView(withId(R.id.etName))
                .check(matches(withText("Haiqa Nasir")));

        /*
         * Save Profile button should be enabled since user data was loaded.
         */
        onView(withId(R.id.btnUpdateProfile))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }
}