package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HaiqaLoginSystemTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> rule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void t01_emptyLoginClick_keepsLoginScreenVisible() {
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void t02_emailOnlyLoginClick_keepsLoginScreenVisible() {
        onView(withId(R.id.login_username_edt))
                .perform(typeText("a"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void t03_passwordOnlyLoginClick_keepsLoginScreenVisible() {
        onView(withId(R.id.login_password_edt))
                .perform(typeText("a"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void t04_signupButtonClick_launchesSignupFlow() {
        onView(withId(R.id.login_signup_btn)).perform(click());
    }

    @Test
    public void t05_moderatorButtonClick_launchesModeratorFlow() {
        onView(withId(R.id.login_moderator_btn)).perform(click());
    }

    @Test
    public void t06_moderatorLoginButtonClick_launchesModeratorLoginFlow() {
        onView(withId(R.id.login_moderator_login_btn)).perform(click());
    }

    @Test
    public void t07_deleteUserButtonClick_launchesDeleteUserFlow() {
        onView(withId(R.id.login_delete_user_btn)).perform(click());
    }

    @Test
    public void t08_spacesOnlyEmail_keepsLoginScreenVisible() {
        onView(withId(R.id.login_username_edt))
                .perform(typeText("   "), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void t09_spacesOnlyPassword_keepsLoginScreenVisible() {
        onView(withId(R.id.login_password_edt))
                .perform(typeText("   "), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void t10_invalidCredentials_keepsLoginScreenVisible() {
        onView(withId(R.id.login_username_edt))
                .perform(typeText("test@test.com"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(typeText("bad"), closeSoftKeyboard());

        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }
}
