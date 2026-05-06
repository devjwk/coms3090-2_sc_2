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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Jongwoo system tests.
 *
 * Purpose:
 * 1. Run more than 4 system test cases.
 * 2. Produce Android method coverage.
 * 3. Verify the user-facing system flow.
 *
 * Covered scenarios:
 * 1. Signup -> admin pending approval -> approve -> login
 * 2. Admin pending request refresh
 * 3. Group recommendation/search
 * 4. Report submission -> moderator report verification
 * 5. Home notification bell -> notification center -> clear all
 *
 * Important:
 * Search Logcat with tag "COVERAGE_PROOF" after running coverage.
 */
@RunWith(AndroidJUnit4.class)
public class JongwooSystemTest {

    private static final String TAG = "COVERAGE_PROOF";

    private static final int TEST_USER_ID = 2;
    private static final int REPORTED_USER_ID = 4;

    private static final int ADMIN_USER_ID = 2;
    private static final int ADMIN_ID = 1;

    private static final String SEARCH_KEYWORD = "coding";
    private static final String EXPECTED_GROUP_NAME = "coding club 2";

    /**
     * Test Case 1:
     * Signup -> admin pending list -> approve -> login.
     *
     * This test intentionally keeps these steps together because each step
     * depends on the result of the previous step.
     */
    @Test
    public void signupAdminApprovalLoginFlow_succeeds() {
        String uniqueTime = String.valueOf(System.currentTimeMillis());

        String uniqueEmail = "jongwoo.signup." + uniqueTime + "@iastate.edu";
        String uniquePassword = "JwPass" + uniqueTime;
        String displayName = "Jongwoo Espresso " + uniqueTime;

        /*
         * ============================================================
         * 1. Signup
         * ============================================================
         */
        ActivityScenario<SignupActivity> signupScenario =
                ActivityScenario.launch(SignupActivity.class);

        signupScenario.onActivity(activity -> {
            Log.d(TAG, "Signup actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(1500);

        onView(withId(R.id.etEmail))
                .perform(scrollTo(), clearText(), typeText(uniqueEmail), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etPassword))
                .perform(scrollTo(), clearText(), typeText(uniquePassword), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etDisplayName))
                .perform(scrollTo(), clearText(), typeText(displayName), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etMajor))
                .perform(scrollTo(), clearText(), typeText("Computer Engineering"), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etAge))
                .perform(scrollTo(), clearText(), typeText("22"), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etBio))
                .perform(scrollTo(), clearText(), typeText("Created by Espresso system test"), closeSoftKeyboard());

        SystemClock.sleep(400);

        onView(withId(R.id.etInterests))
                .perform(scrollTo(), clearText(), typeText("coding, music, test"), closeSoftKeyboard());

        SystemClock.sleep(800);

        onView(withId(R.id.etEmail))
                .check(matches(withText(uniqueEmail)));

        onView(withId(R.id.etPassword))
                .check(matches(withText(uniquePassword)));

        onView(withId(R.id.etInterests))
                .check(matches(withText("coding, music, test")));

        onView(withId(R.id.btnSignup))
                .perform(scrollTo(), click());

        /*
         * Wait for backend signup request.
         */
        SystemClock.sleep(6000);

        /*
         * Close SignupActivity before launching the next Activity.
         * This prevents ActivityScenario lifecycle/intent mismatch.
         */
        signupScenario.close();
        SystemClock.sleep(1000);

        /*
         * ============================================================
         * 2. Admin approval
         * ============================================================
         */
        Intent adminIntent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AdminModeratorRequestsActivity.class
        );
        adminIntent.putExtra("USER_ID", ADMIN_USER_ID);
        adminIntent.putExtra("ADMIN_ID", ADMIN_ID);

        ActivityScenario<AdminModeratorRequestsActivity> adminScenario =
                ActivityScenario.launch(adminIntent);

        adminScenario.onActivity(activity -> {
            Log.d(TAG, "Admin actual activity in signup flow = " + activity.getClass().getName());
        });

        /*
         * Wait for /users/status/NEED_APPROVAL.
         */
        SystemClock.sleep(9000);

        onView(withId(R.id.tvAdminRequestsTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.pendingRequestsContainer))
                .check(matches(isDisplayed()));

        /*
         * Show the exact newly-created pending account.
         */
        onView(withText("Email: " + uniqueEmail))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        SystemClock.sleep(1500);

        /*
         * Approve the exact account created by this test.
         */
        onView(withId(R.id.pendingRequestsContainer))
                .perform(approvePendingRequestForEmail(uniqueEmail));

        /*
         * Wait for approve PUT request.
         */
        SystemClock.sleep(5000);

        adminScenario.close();
        SystemClock.sleep(1000);

        /*
         * ============================================================
         * 3. Login
         * ============================================================
         */
        ActivityScenario<LoginActivity> loginScenario =
                ActivityScenario.launch(LoginActivity.class);

        loginScenario.onActivity(activity -> {
            Log.d(TAG, "Login actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(1500);

        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText(uniqueEmail), closeSoftKeyboard());

        SystemClock.sleep(500);

        onView(withId(R.id.login_password_edt))
                .perform(clearText(), typeText(uniquePassword), closeSoftKeyboard());

        SystemClock.sleep(700);

        onView(withId(R.id.login_login_btn))
                .perform(click());

        /*
         * Wait for login backend request and HomeActivity transition.
         */
        SystemClock.sleep(5000);

        /*
         * If this fails, replace "AntiSocial" with a stable visible HomeActivity text
         * or a stable HomeActivity view ID from the actual home layout.
         */
        onView(withText(containsString("AntiSocial")))
                .check(matches(isDisplayed()));

        loginScenario.close();
        SystemClock.sleep(1000);
    }

    /**
     * Test Case 2:
     * Admin pending request screen refresh.
     */
    @Test
    public void adminRequestsRefreshFlow_keepsPendingListUsable() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AdminModeratorRequestsActivity.class
        );
        intent.putExtra("USER_ID", ADMIN_USER_ID);
        intent.putExtra("ADMIN_ID", ADMIN_ID);

        ActivityScenario<AdminModeratorRequestsActivity> scenario =
                ActivityScenario.launch(intent);

        scenario.onActivity(activity -> {
            Log.d(TAG, "Admin refresh actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(5000);

        onView(withId(R.id.tvAdminRequestsTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.pendingRequestsContainer))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnRefreshModeratorRequests))
                .check(matches(isDisplayed()))
                .perform(click());

        SystemClock.sleep(3000);

        onView(withId(R.id.tvAdminRequestsTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.pendingRequestsContainer))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnRefreshModeratorRequests))
                .check(matches(isDisplayed()));

        scenario.close();
        SystemClock.sleep(1000);
    }

    /**
     * Test Case 3:
     * Group recommendation/search.
     */
    @Test
    public void groupSearchFlow_searchCodingShowsCodingClub() throws Exception {
        JSONObject user = new JSONObject();
        JSONArray interests = new JSONArray();

        interests.put("coding");
        interests.put("music");
        interests.put("test");
        user.put("interests", interests);

        Intent groupIntent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupRecommendActivity.class
        );
        groupIntent.putExtra("USER_ID", TEST_USER_ID);
        groupIntent.putExtra("USER_JSON", user.toString());

        ActivityScenario<GroupRecommendActivity> scenario =
                ActivityScenario.launch(groupIntent);

        scenario.onActivity(activity -> {
            Log.d(TAG, "Group actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(4000);

        onView(withId(R.id.recyclerViewRecommend))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvInterest1))
                .check(matches(withText("coding")));

        onView(withId(R.id.tvInterest2))
                .check(matches(withText("music")));

        onView(withId(R.id.tvInterest3))
                .check(matches(withText("test")));

        onView(withId(R.id.btnShowSearch))
                .check(matches(isDisplayed()))
                .perform(click());

        SystemClock.sleep(1500);

        onView(withId(R.id.etSearchKeyword))
                .perform(clearText(), typeText(SEARCH_KEYWORD), closeSoftKeyboard());

        SystemClock.sleep(6000);

        onView(withId(R.id.etSearchKeyword))
                .check(matches(withText(SEARCH_KEYWORD)));

        onView(withId(R.id.recyclerViewSearch))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerViewSearch))
                .check(matches(hasDescendant(withText(EXPECTED_GROUP_NAME))));

        scenario.close();
        SystemClock.sleep(1000);
    }

    /**
     * Test Case 4:
     * Report submission -> moderator report panel verification.
     */
    @Test
    public void reportSubmitFlow_moderatorPanelShowsSubmittedReport() {
        String uniqueTime = String.valueOf(System.currentTimeMillis());
        String uniqueReportDescription = "Espresso report test " + uniqueTime;

        /*
         * ============================================================
         * 1. Submit report
         * ============================================================
         */
        Intent reportIntent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ReportSubmitActivity.class
        );
        reportIntent.putExtra("USER_ID", TEST_USER_ID);
        reportIntent.putExtra("USER_JSON", "{}");

        ActivityScenario<ReportSubmitActivity> reportScenario =
                ActivityScenario.launch(reportIntent);

        reportScenario.onActivity(activity -> {
            Log.d(TAG, "Report actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(2000);

        onView(withId(R.id.tvReportTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.etReportedUserId))
                .perform(scrollTo(), clearText(), typeText(String.valueOf(REPORTED_USER_ID)), closeSoftKeyboard());

        SystemClock.sleep(500);

        onView(withId(R.id.etReportDescription))
                .perform(scrollTo(), clearText(), typeText(uniqueReportDescription), closeSoftKeyboard());

        SystemClock.sleep(700);

        onView(withId(R.id.etReportedUserId))
                .check(matches(withText(String.valueOf(REPORTED_USER_ID))));

        onView(withId(R.id.etReportDescription))
                .check(matches(withText(uniqueReportDescription)));

        onView(withId(R.id.btnSubmitReport))
                .perform(scrollTo(), click());

        /*
         * ReportSubmitActivity may finish itself after success.
         * Wait for backend request, then close the scenario before launching ModeratorActivity.
         */
        SystemClock.sleep(7000);

        reportScenario.close();
        SystemClock.sleep(1000);

        /*
         * ============================================================
         * 2. Moderator verifies report
         * ============================================================
         */
        ActivityScenario<ModeratorActivity> moderatorScenario =
                ActivityScenario.launch(ModeratorActivity.class);

        moderatorScenario.onActivity(activity -> {
            Log.d(TAG, "Moderator actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(3500);

        onView(withId(R.id.tvModeratorTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnShowReports))
                .perform(click());

        SystemClock.sleep(9000);

        onView(withId(R.id.recyclerModerator))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerModerator))
                .perform(RecyclerViewActions.scrollTo(
                        hasDescendant(withText(containsString(uniqueReportDescription)))
                ));

        SystemClock.sleep(1500);

        onView(withText(containsString(uniqueReportDescription)))
                .check(matches(isDisplayed()));

        moderatorScenario.close();
        SystemClock.sleep(1000);
    }

    /**
     * Test Case 5:
     * Notification bell -> notification center -> clear all.
     */
    @Test
    public void notificationBellFlow_opensCenterAndClearsHistory() {
        NotificationWebSocketManager.clearNotificationHistory();

        String testNotificationJson =
                "{\"type\":\"MATCH_CREATED\"," +
                        "\"message\":\"You have a new match!\"," +
                        "\"timestamp\":\"2026-05-05T16:40:00\"}";

        NotificationWebSocketManager.addNotificationToHistory(testNotificationJson);

        Intent homeIntent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class
        );
        homeIntent.putExtra("USER_ID", TEST_USER_ID);
        homeIntent.putExtra("USER_JSON", "{}");

        ActivityScenario<HomeActivity> homeScenario =
                ActivityScenario.launch(homeIntent);

        homeScenario.onActivity(activity -> {
            Log.d(TAG, "Home actual activity = " + activity.getClass().getName());
        });

        SystemClock.sleep(3000);

        onView(withId(R.id.btnNotificationCenter))
                .check(matches(isDisplayed()))
                .perform(click());

        SystemClock.sleep(2500);

        onView(withId(R.id.btnBack))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnClearAll))
                .check(matches(isDisplayed()));

        onView(withText(containsString("You have a new match")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnClearAll))
                .perform(click());

        SystemClock.sleep(1500);

        onView(withText("No new notifications"))
                .check(matches(isDisplayed()));

        homeScenario.close();
        SystemClock.sleep(1000);
    }

    /**
     * Finds the dynamically generated admin request card for the given email
     * and clicks its Approve button.
     *
     * AdminModeratorRequestsActivity creates cards in Java, so the Approve
     * button does not have a fixed XML ID. This helper searches inside the
     * pendingRequestsContainer and clicks the Approve button inside the card
     * containing the exact generated email.
     */
    private static ViewAction approvePendingRequestForEmail(String targetEmail) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "approve pending request for email: " + targetEmail;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!(view instanceof LinearLayout)) {
                    throw new AssertionError("pendingRequestsContainer is not a LinearLayout");
                }

                LinearLayout container = (LinearLayout) view;
                String expectedEmailText = "Email: " + targetEmail;

                for (int i = 0; i < container.getChildCount(); i++) {
                    View card = container.getChildAt(i);

                    if (viewTreeContainsExactText(card, expectedEmailText)) {
                        Button approveButton = findButtonWithText(card, "Approve");

                        if (approveButton == null) {
                            throw new AssertionError("Approve button not found for email: " + targetEmail);
                        }

                        approveButton.performClick();
                        uiController.loopMainThreadUntilIdle();
                        return;
                    }
                }

                throw new AssertionError("Pending request card not found for email: " + targetEmail);
            }
        };
    }

    private static boolean viewTreeContainsExactText(View view, String expectedText) {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            return text != null && expectedText.contentEquals(text);
        }

        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;

            for (int i = 0; i < layout.getChildCount(); i++) {
                if (viewTreeContainsExactText(layout.getChildAt(i), expectedText)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Button findButtonWithText(View view, String targetText) {
        if (view instanceof Button) {
            CharSequence text = ((Button) view).getText();

            if (text != null && targetText.contentEquals(text)) {
                return (Button) view;
            }
        }

        if (view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;

            for (int i = 0; i < layout.getChildCount(); i++) {
                Button result = findButtonWithText(layout.getChildAt(i), targetText);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}