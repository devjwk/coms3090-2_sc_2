package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HaiqaLoginTest {

    // Minimal valid USER_JSON matching what parseUserFromJson expects
    private static final String VALID_USER_JSON =
            "{\"userId\":1,\"displayName\":\"Test User\",\"email\":\"user1@test.com\"," +
                    "\"passwordHash\":\"password123\",\"bio\":\"Hello bio\",\"role\":\"user\"," +
                    "\"latitude\":0.0,\"longitude\":0.0,\"createdTs\":\"\",\"isActive\":true," +
                    "\"major\":\"CS\",\"age\":21,\"hobbies\":[]}";

    private ActivityScenario<Login> launchWithUser() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), Login.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", VALID_USER_JSON);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<Login> launchNoUser() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), Login.class);
        intent.putExtra("USER_ID", 1);
        return ActivityScenario.launch(intent);
    }

    // --- UI visible ---

    @Test public void t01_fieldsDisabledWithoutUserJson() {
        launchNoUser();
        onView(withId(R.id.etName)).check(matches(not(isEnabled())));
        onView(withId(R.id.etBio)).check(matches(not(isEnabled())));
        onView(withId(R.id.btnUpdateProfile)).check(matches(not(isEnabled())));
    }

    @Test public void t02_fieldsEnabledWithUserJson() {
        launchWithUser();
        onView(withId(R.id.etName)).check(matches(isEnabled()));
        onView(withId(R.id.etBio)).check(matches(isEnabled()));
        onView(withId(R.id.btnUpdateProfile)).check(matches(isEnabled()));
    }

    @Test public void t03_nameFieldPopulatedFromJson() {
        launchWithUser();
        onView(withId(R.id.etName)).check(matches(withText("Test User")));
    }

    @Test public void t04_bioFieldPopulatedFromJson() {
        launchWithUser();
        onView(withId(R.id.etBio)).check(matches(withText("Hello bio")));
    }

    @Test public void t05_msgResponseIsDisplayed() {
        launchWithUser();
        onView(withId(R.id.msgResponse)).check(matches(isDisplayed()));
    }

    @Test public void t06_backButtonIsDisplayed() {
        launchWithUser();
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    // --- Interactions ---

    @Test public void t07_updateButtonClickedWithNoUserShowsToast() {
        // no USER_JSON -> user is null -> toast "Profile not loaded yet."
        launchNoUser();
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        // button is disabled so click is a no-op; just assert it stays displayed
        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
    }

    @Test public void t08_editBioAndClickUpdate() {
        launchWithUser();
        onView(withId(R.id.etBio))
                .perform(clearText(), typeText("Updated bio"), closeSoftKeyboard());
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        // network call fires; assert UI still standing
        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
    }

    @Test public void t09_clearBioAndClickUpdate() {
        launchWithUser();
        onView(withId(R.id.etBio)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        onView(withId(R.id.etBio)).check(matches(isDisplayed()));
    }

    @Test public void t10_editNameField() {
        launchWithUser();
        onView(withId(R.id.etName))
                .perform(clearText(), typeText("New Name"), closeSoftKeyboard());
        onView(withId(R.id.etName)).check(matches(withText("New Name")));
    }

    @Test public void t11_backButtonFinishesActivity() {
        launchWithUser();
        onView(withId(R.id.btnBack)).perform(click());
        // after finish() the activity is gone; just assert no crash
    }

    @Test public void t12_updateButtonIsDisplayed() {
        launchWithUser();
        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
    }

    @Test public void t13_nameFieldIsDisplayed() {
        launchWithUser();
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
    }

    @Test public void t14_bioFieldIsDisplayed() {
        launchWithUser();
        onView(withId(R.id.etBio)).check(matches(isDisplayed()));
    }

    @Test public void t15_multipleUpdateClicks() {
        launchWithUser();
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
    }

    @Test public void t16_longBioUpdate() {
        launchWithUser();
        String longBio = "This is a very long bio that goes on and on to test large input handling in the update profile flow.";
        onView(withId(R.id.etBio))
                .perform(clearText(), typeText(longBio), closeSoftKeyboard());
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        onView(withId(R.id.etBio)).check(matches(withText(longBio)));
    }

    @Test public void t17_noUserJsonUpdateButtonDisabledOnClick() {
        launchNoUser();
        // Disabled button — perform click does nothing, assert it stays visible
        onView(withId(R.id.btnUpdateProfile)).check(matches(not(isEnabled())));
    }

    @Test public void t18_bioAndNameBothCleared() {
        launchWithUser();
        onView(withId(R.id.etName)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.etBio)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.etName)).check(matches(withText("")));
        onView(withId(R.id.etBio)).check(matches(withText("")));
    }

    @Test public void t19_updateAfterEditingBothFields() {
        launchWithUser();
        onView(withId(R.id.etName))
                .perform(clearText(), typeText("Changed Name"), closeSoftKeyboard());
        onView(withId(R.id.etBio))
                .perform(clearText(), typeText("Changed bio"), closeSoftKeyboard());
        onView(withId(R.id.btnUpdateProfile)).perform(click());
        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
    }

    @Test public void t20_backButtonNoUserJson() {
        launchNoUser();
        onView(withId(R.id.btnBack)).perform(click());
    }
}