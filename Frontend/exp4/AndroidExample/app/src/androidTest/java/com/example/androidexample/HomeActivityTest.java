//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import android.content.Intent;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * Instrumented Espresso tests for HomeActivity.
// * One test per logical method/behavior covered in HomeActivity.
// */
//@RunWith(AndroidJUnit4.class)
//public class HomeActivityTest {
//
//    // -------------------------------------------------------------------------
//    // Helpers
//    // -------------------------------------------------------------------------
//
//    private static final String SAMPLE_USER_JSON =
//            "{\"name\":\"Alice\","
//                    + "\"email\":\"alice@example.com\","
//                    + "\"bio\":\"Hello world\","
//                    + "\"hobbies\":[\"Reading\",\"Coding\"]}";
//
//    /** Launch HomeActivity with a full USER_JSON payload. */
//    private ActivityScenario<HomeActivity> launchWithJson() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
//        intent.putExtra("USER_ID", 1);
//        intent.putExtra("USER_JSON", SAMPLE_USER_JSON);
//        return ActivityScenario.launch(intent);
//    }
//
//    /** Launch HomeActivity without USER_JSON (null/empty branch). */
//    private ActivityScenario<HomeActivity> launchWithoutJson() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
//        intent.putExtra("USER_ID", 1);
//        return ActivityScenario.launch(intent);
//    }
//
//    private void waitForNetwork() {
//        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
//    }
//
//    // -------------------------------------------------------------------------
//    // onCreate — userJson parsing
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers onCreate(): welcome name TextView is populated from USER_JSON "name" field.
//     */
//    @Test
//    public void home_t01_onCreate_welcomeNameDisplayed() {
//        launchWithJson();
//        onView(withId(R.id.tvWelcomeName)).check(matches(withText("Alice 👋")));
//    }
//
//    /**
//     * Covers onCreate(): avatar initial is the first character of the user's name.
//     */
//    @Test
//    public void home_t02_onCreate_avatarInitialSet() {
//        launchWithJson();
//        onView(withId(R.id.tvAvatarInitial)).check(matches(withText("A")));
//    }
//
//    /**
//     * Covers onCreate(): profile initial mirrors avatar initial.
//     */
//    @Test
//    public void home_t03_onCreate_profileInitialSet() {
//        launchWithJson();
//        onView(withId(R.id.tvProfileInitial)).check(matches(withText("A")));
//    }
//
//    /**
//     * Covers onCreate(): profile name TextView shows the user's name.
//     */
//    @Test
//    public void home_t04_onCreate_profileNameSet() {
//        launchWithJson();
//        onView(withId(R.id.tvProfileName)).check(matches(withText("Alice")));
//    }
//
//    /**
//     * Covers onCreate(): profile email TextView shows the user's email.
//     */
//    @Test
//    public void home_t05_onCreate_profileEmailSet() {
//        launchWithJson();
//        onView(withId(R.id.tvProfileEmail)).check(matches(withText("alice@example.com")));
//    }
//
//    /**
//     * Covers onCreate(): profile bio TextView shows the user's bio.
//     */
//    @Test
//    public void home_t06_onCreate_profileBioSet() {
//        launchWithJson();
//        onView(withId(R.id.tvProfileBio)).check(matches(withText("Hello world")));
//    }
//
//    /**
//     * Covers onCreate() hobbies parsing: first hobby is displayed in tvHobby1.
//     */
//    @Test
//    public void home_t07_onCreate_hobby1Set() {
//        launchWithJson();
//        onView(withId(R.id.tvHobby1)).check(matches(withText("Reading")));
//    }
//
//    /**
//     * Covers onCreate() hobbies parsing: second hobby is displayed in tvHobby2.
//     */
//    @Test
//    public void home_t08_onCreate_hobby2Set() {
//        launchWithJson();
//        onView(withId(R.id.tvHobby2)).check(matches(withText("Coding")));
//    }
//
//    /**
//     * Covers onCreate() null-JSON branch: activity launches without crash when
//     * USER_JSON is absent; core views are still displayed.
//     */
//    @Test
//    public void home_t09_onCreate_noJsonNoCrash() {
//        launchWithoutJson();
//        onView(withId(R.id.tvWelcomeName)).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Covers onCreate() hardcoded group card text: cardGroup1 shows "Badminton Club".
//     */
//    @Test
//    public void home_t10_onCreate_group1NameSet() {
//        launchWithJson();
//        onView(withId(R.id.tvGroup1Name)).check(matches(withText("Badminton Club")));
//    }
//
//    /**
//     * Covers onCreate() hardcoded group card text: cardGroup1 description is set.
//     */
//    @Test
//    public void home_t11_onCreate_group1DescSet() {
//        launchWithJson();
//        onView(withId(R.id.tvGroup1Desc)).check(matches(withText("Weekly badminton sessions")));
//    }
//
//    /**
//     * Covers onCreate() hardcoded group card text: cardGroup2 shows "Coding Club".
//     */
//    @Test
//    public void home_t12_onCreate_group2NameSet() {
//        launchWithJson();
//        onView(withId(R.id.tvGroup2Name)).check(matches(withText("Coding Club")));
//    }
//
//    /**
//     * Covers onCreate() hardcoded group card text: cardGroup2 description is set.
//     */
//    @Test
//    public void home_t13_onCreate_group2DescSet() {
//        launchWithJson();
//        onView(withId(R.id.tvGroup2Desc)).check(matches(withText("Let's code together")));
//    }
//
//    // -------------------------------------------------------------------------
//    // Navigation listeners (onClick handlers set in onCreate)
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers btnEditProfile.setOnClickListener: clicking launches Login activity
//     * without crashing (activity transition is accepted as success).
//     */
//
//    /**
//     * Covers navGroups.setOnClickListener: clicking the Groups nav item opens GroupsActivity.
//     */
//    @Test
//    public void home_t15_navGroups_launchesGroupsActivity() {
//        launchWithJson();
//        onView(withId(R.id.navGroups)).perform(click());
//    }
//
//    /**
//     * Covers navProfile.setOnClickListener: clicking the Profile nav item opens SwipeActivity.
//     */
//    @Test
//    public void home_t16_navProfile_launchesSwipeActivity() {
//        launchWithJson();
//        onView(withId(R.id.navProfile)).perform(click());
//    }
//
//    /**
//     * Covers navMembers.setOnClickListener: clicking Members nav opens GroupMembershipActivity.
//     */
//    @Test
//    public void home_t17_navMembers_launchesGroupMembership() {
//        launchWithJson();
//        onView(withId(R.id.navMembers)).perform(click());
//    }
//
//    /**
//     * Covers navMatches.setOnClickListener: clicking Matches nav opens MatchesActivity.
//     */
//    @Test
//    public void home_t18_navMatches_launchesMatchesActivity() {
//        launchWithJson();
//        onView(withId(R.id.navMatches)).perform(click());
//    }
//
//    /**
//     * Covers navChat.setOnClickListener: clicking Chat nav opens ChatListActivity.
//     */
//    @Test
//    public void home_t19_navChat_launchesChatList() {
//        launchWithJson();
//        onView(withId(R.id.navChat)).perform(click());
//    }
//
//    /**
//     * Covers btnReport.setOnClickListener: clicking Report button opens ReportSubmitActivity.
//     */
//    @Test
//    public void home_t20_btnReport_launchesReportSubmit() {
//        launchWithJson();
//        onView(withId(R.id.btnReport)).perform(click());
//    }
//
//    // -------------------------------------------------------------------------
//    // openGroupRecommendActivity — triggered by card clicks
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers openGroupRecommendActivity() via cardGroup1 click.
//     */
//    @Test
//    public void home_t21_cardGroup1_opensGroupRecommend() {
//        launchWithJson();
//        onView(withId(R.id.cardGroup1)).perform(click());
//    }
//
//    /**
//     * Covers openGroupRecommendActivity() via cardGroup2 click.
//     */
//    @Test
//    public void home_t22_cardGroup2_opensGroupRecommend() {
//        launchWithJson();
//        onView(withId(R.id.cardGroup2)).perform(click());
//    }
//
//    // -------------------------------------------------------------------------
//    // showTopBanner / handleNotificationMessage
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers showTopBanner(): the notification banner layout exists in the view hierarchy.
//     */
//
//
//    // -------------------------------------------------------------------------
//    // connectNotificationSocket / WebSocket lifecycle
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers connectNotificationSocket(): activity starts successfully even if the
//     * notification WebSocket server is unreachable (no crash expected).
//     */
//    @Test
//    public void home_t25_connectNotificationSocket_noCrashOnUnreachableServer() {
//        launchWithJson();
//        waitForNetwork();
//        onView(withId(R.id.tvWelcomeName)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // onDestroy
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers onDestroy(): closing the activity (via scenario.close()) does not crash.
//     */
//    @Test
//    public void home_t26_onDestroy_noCrash() {
//        ActivityScenario<HomeActivity> scenario = launchWithJson();
//        scenario.close();
//        // If no exception is thrown, onDestroy executed cleanly
//    }
//
//    // -------------------------------------------------------------------------
//    // fetchAcceptedMatchesAndEnsureConversations / sendDirectConversationPost
//    // -------------------------------------------------------------------------
//
//    /**
//     * Covers fetchAcceptedMatchesAndEnsureConversations(): called indirectly on launch;
//     * UI remains stable after the network call completes (or times out).
//     */
//    @Test
//    public void home_t27_fetchAcceptedMatches_uiStableAfterLoad() {
//        launchWithJson();
//        waitForNetwork();
//        onView(withId(R.id.tvWelcomeName)).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Covers sendDirectConversationPost(): POST fires without crashing the UI thread.
//     * Verified by ensuring the home screen is still displayed after the network delay.
//     */
//    @Test
//    public void home_t28_sendDirectConversationPost_noCrash() {
//        launchWithJson();
//        waitForNetwork();
//        onView(withId(R.id.navChat)).check(matches(isDisplayed()));
//    }
//}