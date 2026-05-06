package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * System tests for Android frontend features worked on by Jongwoo.
 *
 * Tests cover:
 * 1. Signup form + validation click flow
 * 2. Group membership admin button flow
 * 3. Group recommendation/search flow
 * 4. Report submission form + validation click flow
 */
@RunWith(AndroidJUnit4.class)
public class JongwooSystemTest {

    @Test
    public void signupFlow_entersInformationAndClicksCreateAccount() {
        ActivityScenario.launch(SignupActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.etEmail))
                .perform(scrollTo(), clearText(), typeText("jongwoo_test@example.com"), closeSoftKeyboard());
        SystemClock.sleep(600);

        /*
         * Password is intentionally left empty.
         * This allows the test to click Create Account and trigger validation
         * without sending a real backend signup request.
         */

        onView(withId(R.id.etDisplayName))
                .perform(scrollTo(), clearText(), typeText("Jongwoo Test"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.etMajor))
                .perform(scrollTo(), clearText(), typeText("Computer Engineering"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.etAge))
                .perform(scrollTo(), clearText(), typeText("22"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.etBio))
                .perform(scrollTo(), clearText(), typeText("Testing signup flow"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.etInterests))
                .perform(scrollTo(), clearText(), typeText("coding, games, hiking"), closeSoftKeyboard());
        SystemClock.sleep(800);

        onView(withId(R.id.etEmail))
                .check(matches(withText("jongwoo_test@example.com")));

        onView(withId(R.id.etDisplayName))
                .check(matches(withText("Jongwoo Test")));

        onView(withId(R.id.etMajor))
                .check(matches(withText("Computer Engineering")));

        onView(withId(R.id.etAge))
                .check(matches(withText("22")));

        onView(withId(R.id.btnSignup))
                .perform(scrollTo(), click());

        /*
         * After clicking, the activity should stay on the signup screen because
         * password is missing and validation prevents backend submission.
         */
        SystemClock.sleep(1200);

        onView(withId(R.id.btnSignup))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackToMain))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }

    @Test
    public void groupMembershipFlow_clicksAdminButtonsAndStaysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupMembershipActivity.class
        );
        intent.putExtra("USER_ID", 1);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.etGroupId))
                .perform(scrollTo(), clearText(), typeText("1"), closeSoftKeyboard());
        SystemClock.sleep(700);

        /*
         * Target User ID / Membership ID is intentionally left empty.
         * Clicking admin buttons triggers validation and avoids backend requests.
         */

        onView(withId(R.id.btnApprove))
                .perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etTargetUserId))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnToggleMod))
                .perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etTargetUserId))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBan))
                .perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etTargetUserId))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBack))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }

    @Test
    public void groupRecommendationFlow_searchShowsMatchingResultOrSearchScreen() throws Exception {
        JSONObject user = new JSONObject();
        JSONArray interests = new JSONArray();
        interests.put("coding");
        interests.put("games");
        interests.put("hiking");
        user.put("interests", interests);

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupRecommendActivity.class
        );
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", user.toString());

        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.recyclerViewRecommend))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvInterest1))
                .check(matches(withText("coding")));

        onView(withId(R.id.tvInterest2))
                .check(matches(withText("games")));

        onView(withId(R.id.tvInterest3))
                .check(matches(withText("hiking")));

        SystemClock.sleep(1000);

        onView(withId(R.id.btnShowSearch))
                .perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.etSearchKeyword))
                .perform(clearText(), typeText("coding"), closeSoftKeyboard());

        /*
         * Wait for backend search and RecyclerView update.
         */
        SystemClock.sleep(3500);

        onView(withId(R.id.etSearchKeyword))
                .check(matches(withText("coding")));

        onView(withId(R.id.recyclerViewSearch))
                .check(matches(isDisplayed()));

        /*
         * This verifies that a result item contains the searched value.
         * If backend data changes and this fails, replace "coding" with
         * the exact visible group name/keyword returned by the search.
         */
        onView(withId(R.id.recyclerViewSearch))
                .check(matches(hasDescendant(withText(containsString("coding")))));

        SystemClock.sleep(1000);

        onView(withId(R.id.btnShowRecommend))
                .perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.recyclerViewRecommend))
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }

    @Test
    public void reportSubmissionFlow_entersUserIdAndClicksSubmit() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ReportSubmitActivity.class
        );
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", "{}");

        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.tvReportTitle))
                .check(matches(isDisplayed()));

        SystemClock.sleep(700);

        onView(withId(R.id.etReportedUserId))
                .perform(scrollTo(), clearText(), typeText("2"), closeSoftKeyboard());

        SystemClock.sleep(800);

        /*
         * Description is intentionally left empty.
         * Clicking Submit triggers validation and avoids a real backend report request.
         */

        onView(withId(R.id.btnSubmitReport))
                .perform(scrollTo(), click());

        SystemClock.sleep(1200);

        /*
         * The report screen should remain visible because description is missing.
         */
        onView(withId(R.id.tvReportTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etReportedUserId))
                .check(matches(withText("2")));

        onView(withId(R.id.btnSubmitReport))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnBackReport))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        SystemClock.sleep(1000);
    }
}