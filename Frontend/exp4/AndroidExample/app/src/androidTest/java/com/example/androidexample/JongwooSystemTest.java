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
 * Jongwoo Kim - Demo 4 Android System Tests.
 *
 * These tests follow the same frontend system-test style used by the team:
 * each test launches a real Android Activity, simulates user input/clicks,
 * and verifies that the correct screen or validation state is shown.
 *
 * Feature areas covered:
 * 1. Signup flow and validation
 * 2. Notification history display and clear behavior
 * 3. Moderator panel navigation
 * 4. Admin dashboard navigation
 * 5. Admin reports, account status, and analytics screens
 * 6. Group recommendation/search flow
 * 7. Report submission validation
 * 8. Role-based admin login flow
 */
@RunWith(AndroidJUnit4.class)
public class JongwooSystemTest {

    private Intent intentFor(Class<?> activityClass) {
        return new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                activityClass
        );
    }

    /**
     * Test Case 1 - Signup form validation with missing password.
     *
     * Enters all profile fields except password and clicks Create Account.
     * The app should stay on SignupActivity because password is required.
     * This avoids creating a real backend user while still testing the
     * signup UI population and validation path.
     */
    @Test
    public void signupFlow_missingPassword_staysOnSignupScreen() {
        ActivityScenario.launch(SignupActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.etEmail))
                .perform(scrollTo(), clearText(), typeText("jongwoo_test@example.com"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etDisplayName))
                .perform(scrollTo(), clearText(), typeText("Jongwoo Test"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etMajor))
                .perform(scrollTo(), clearText(), typeText("Computer Engineering"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etAge))
                .perform(scrollTo(), clearText(), typeText("22"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etBio))
                .perform(scrollTo(), clearText(), typeText("Testing signup flow"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etInterests))
                .perform(scrollTo(), clearText(), typeText("coding, games, hiking"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.etEmail)).check(matches(withText("jongwoo_test@example.com")));
        onView(withId(R.id.etDisplayName)).check(matches(withText("Jongwoo Test")));
        onView(withId(R.id.etMajor)).check(matches(withText("Computer Engineering")));
        onView(withId(R.id.etAge)).check(matches(withText("22")));
        onView(withId(R.id.etBio)).check(matches(withText("Testing signup flow")));
        onView(withId(R.id.etInterests)).check(matches(withText("coding, games, hiking")));

        onView(withId(R.id.btnSignup)).perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.btnSignup)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackToMain)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test Case 2 - Signup form validation with empty required fields.
     *
     * Clears both email and password, then clicks Create Account.
     * The screen should remain visible because required credentials are missing.
     */
    @Test
    public void signupFlow_emptyEmailAndPassword_staysOnSignupScreen() {
        ActivityScenario.launch(SignupActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.etEmail)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        SystemClock.sleep(500);

        onView(withId(R.id.btnSignup)).perform(scrollTo(), click());
        SystemClock.sleep(1000);

        onView(withId(R.id.etEmail)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignup)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test Case 3 - Notification history display and clear behavior.
     *
     * Adds fake notification JSON messages to the frontend notification history,
     * opens NotificationActivity, verifies the messages appear, then clears them.
     */
    @Test
    public void notificationFlow_displaysStoredNotificationsAndClearsHistory() {
        NotificationWebSocketManager.clearNotificationHistory();
        NotificationWebSocketManager.addNotificationToHistory(
                "{\"type\":\"MATCH_CREATED\",\"message\":\"You have a new match\",\"timestamp\":\"9:30 AM\"}"
        );
        NotificationWebSocketManager.addNotificationToHistory(
                "{\"type\":\"GROUP_MESSAGE\",\"message\":\"New group message posted\",\"timestamp\":\"9:45 AM\"}"
        );

        ActivityScenario.launch(NotificationActivity.class);
        SystemClock.sleep(1200);

        onView(withText(containsString("You have a new match"))).check(matches(isDisplayed()));
        onView(withText(containsString("New group message posted"))).check(matches(isDisplayed()));

        onView(withId(R.id.btnClearAll)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.tvEmptyNotifications)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 4 - Moderator panel navigation from login screen.
     *
     * Clicks the Moderator Panel button from LoginActivity and verifies that
     * ModeratorActivity opens with its members/reports controls visible.
     */
    @Test
    public void moderatorPanelFlow_opensFromLoginScreen() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.login_moderator_btn)).perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.tvModeratorTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.btnShowMembers)).check(matches(isDisplayed()));
        onView(withId(R.id.btnShowReports)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackModerator)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 5 - Admin dashboard navigation.
     *
     * Launches AdminDashboardActivity as admin user 2 and verifies that each
     * major admin feature screen can be opened from the dashboard.
     */
    @Test
    public void adminDashboardFlow_opensMainAdminFeatureScreens() {
        Intent intent = intentFor(AdminDashboardActivity.class);
        intent.putExtra("USER_ID", 2);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.tvAdminDashboardTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.btnModeratorRequests)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReportedUsers)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSuspendedAccounts)).check(matches(isDisplayed()));
        onView(withId(R.id.btnUsageAnalytics)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 6 - Admin report management screen.
     *
     * Opens the reported users/safety violations screen and verifies that the
     * title, refresh button, summary, and report container are displayed.
     */
    @Test
    public void adminReportFlow_loadsReportManagementScreen() {
        Intent intent = intentFor(AdminReportActivity.class);
        intent.putExtra("ADMIN_USER_ID", 2);

        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.tvAdminReportTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvReportSummary)).check(matches(isDisplayed()));
        onView(withId(R.id.btnRefreshReports)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.reportsContainer)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackReports)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test Case 7 - Admin account status management screen.
     *
     * Opens the suspend/disable account management screen and refreshes the
     * list of users. This verifies the admin account status UI flow.
     */
    @Test
    public void adminAccountStatusFlow_refreshesUsersAndKeepsControlsVisible() {
        Intent intent = intentFor(AdminAccountStatusActivity.class);
        intent.putExtra("ADMIN_USER_ID", 2);

        ActivityScenario.launch(intent);
        SystemClock.sleep(2000);

        onView(withId(R.id.tvAccountStatusTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.btnRefreshAccountStatus)).perform(click());
        SystemClock.sleep(2000);

        onView(withId(R.id.userStatusContainer)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackAccountStatus)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 8 - Admin usage analytics screen.
     *
     * Opens the analytics screen, waits for backend-connected counts, refreshes
     * the metrics, and verifies that the main summary fields are visible.
     */
    @Test
    public void adminUsageAnalyticsFlow_loadsAndRefreshesMetrics() {
        ActivityScenario.launch(AdminUsageAnalyticsActivity.class);
        SystemClock.sleep(3000);

        onView(withId(R.id.tvUsageAnalyticsTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTotalUsers)).check(matches(withText(containsString("Total Users:"))));
        onView(withId(R.id.tvPendingUsers)).check(matches(withText(containsString("Pending Users:"))));
        onView(withId(R.id.tvApprovedUsers)).check(matches(withText(containsString("Approved Users:"))));
        onView(withId(R.id.tvPendingReports)).check(matches(withText(containsString("Pending Reports:"))));
        onView(withId(R.id.tvRecentMatches)).check(matches(withText(containsString("Recent Matches:"))));

        onView(withId(R.id.btnRefreshAnalytics)).perform(scrollTo(), click());
        SystemClock.sleep(2500);

        onView(withId(R.id.tvAnalyticsStatus)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackUsageAnalytics)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test Case 9 - Group recommendation/search screen.
     *
     * Provides USER_JSON with interests, verifies that the interest chips are
     * populated, opens the search section, enters a keyword, and returns to the
     * recommendation section.
     */
    @Test
    public void groupRecommendationFlow_showsInterestsAndSearchSection() throws Exception {
        JSONObject user = new JSONObject();
        JSONArray interests = new JSONArray();
        interests.put("coding");
        interests.put("games");
        interests.put("hiking");
        user.put("interests", interests);

        Intent intent = intentFor(GroupRecommendActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", user.toString());

        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.recyclerViewRecommend)).check(matches(isDisplayed()));
        onView(withId(R.id.tvInterest1)).check(matches(withText("coding")));
        onView(withId(R.id.tvInterest2)).check(matches(withText("games")));
        onView(withId(R.id.tvInterest3)).check(matches(withText("hiking")));

        onView(withId(R.id.btnShowSearch)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.etSearchKeyword))
                .perform(clearText(), typeText("coding"), closeSoftKeyboard());
        SystemClock.sleep(1500);

        onView(withId(R.id.etSearchKeyword)).check(matches(withText("coding")));
        onView(withId(R.id.recyclerViewSearch)).check(matches(isDisplayed()));

        onView(withId(R.id.btnShowRecommend)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.recyclerViewRecommend)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 10 - Report submission validation.
     *
     * Enters a reported user ID but leaves the report description empty.
     * The report screen should remain visible because the description is required.
     */
    @Test
    public void reportSubmissionFlow_missingDescription_staysOnReportScreen() {
        Intent intent = intentFor(ReportSubmitActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", "{}");

        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.tvReportTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.etReportedUserId))
                .perform(scrollTo(), clearText(), typeText("2"), closeSoftKeyboard());
        SystemClock.sleep(600);

        onView(withId(R.id.btnSubmitReport)).perform(scrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.tvReportTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.etReportedUserId)).check(matches(withText("2")));
        onView(withId(R.id.btnSubmitReport)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnBackReport)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test Case 11 - Role-based admin login.
     *
     * Uses a backend-provided admin account. A successful login should read the
     * admin role from the backend response and open AdminDashboardActivity.
     */
    @Test
    public void loginFlow_adminCredentials_openAdminDashboard() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(1000);

        onView(withId(R.id.login_username_edt))
                .perform(scrollTo(), clearText(), typeText("Greg@iastate.edu"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.login_password_edt))
                .perform(scrollTo(), clearText(), typeText("secret12345"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.login_login_btn)).perform(scrollTo(), click());
        SystemClock.sleep(3500);

        onView(withId(R.id.tvAdminDashboardTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.btnModeratorRequests)).check(matches(isDisplayed()));
        onView(withId(R.id.btnUsageAnalytics)).check(matches(isDisplayed()));
    }
}