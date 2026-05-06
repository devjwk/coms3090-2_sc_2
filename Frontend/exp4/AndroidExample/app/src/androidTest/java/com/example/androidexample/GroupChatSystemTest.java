//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.clearText;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import android.content.Intent;
//import android.os.SystemClock;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class GroupChatSystemTest {
//
//    private Intent createIntent() {
//        Intent intent = new Intent(
//                InstrumentationRegistry.getInstrumentation().getTargetContext(),
//                GroupChatActivity.class
//        );
//
//        intent.putExtra("USER_ID", 1);
//        intent.putExtra("GROUP_ID", 1);
//        intent.putExtra("GROUP_NAME", "Test Group");
//
//        return intent;
//    }
//
//
//    /**
//     * Test Case 1
//     *
//     * Launch group chat screen.
//     * Verify basic UI loads correctly.
//     */
//    @Test
//    public void groupChat_launchesCorrectly() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(2000);
//
//        onView(withId(R.id.recyclerChat))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.sendBtn))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.backBtn))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.tvGroupName))
//                .check(matches(withText("Test Group")));
//    }
//
//
//
//    /**
//     * Test Case 2
//     *
//     * Empty message should not send.
//     * User remains on chat screen.
//     */
//    @Test
//    public void sendMessage_emptyMessage_staysOnChatScreen() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(2000);
//
//        onView(withId(R.id.sendBtn))
//                .perform(click());
//
//        SystemClock.sleep(1000);
//
//        onView(withId(R.id.sendBtn))
//                .check(matches(isDisplayed()));
//
//        onView(withId(R.id.msgEdt))
//                .check(matches(isDisplayed()));
//    }
//
//
//
//    /**
//     * Test Case 3
//     *
//     * Valid message input.
//     */
//
//
//
//
//    /**
//     * Test Case 4
//     *
//     * Members dialog opens.
//     */
//    @Test
//    public void memberDialog_opensSuccessfully() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(2500);
//
//        onView(withId(R.id.tvMemberCount))
//                .perform(click());
//
//        SystemClock.sleep(1000);
//
//        onView(withText("Close"))
//                .check(matches(isDisplayed()));
//    }
//
//
//
//    /**
//     * Test Case 5
//     *
//     * Events dialog opens.
//     */
//
//
//
//
//    /**
//     * Test Case 6
//     *
//     * Announcements dialog opens.
//     */
//    @Test
//    public void announcementsDialog_opensSuccessfully() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(2500);
//
//        onView(withId(R.id.btnViewAnnouncements))
//                .perform(click());
//
//        SystemClock.sleep(1500);
//
//        onView(withText("Announcements"))
//                .check(matches(isDisplayed()));
//    }
//
//
//
//    /**
//     * Test Case 7
//     *
//     * Back button exits activity.
//     */
//    @Test
//    public void backButton_finishesActivity() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(1500);
//
//        onView(withId(R.id.backBtn))
//                .perform(click());
//
//        SystemClock.sleep(1000);
//    }
//
//
//
//    /**
//     * Test Case 8
//     *
//     * Pinned announcement area renders.
//     */
//
//
//
//
//    /**
//     * Test Case 9
//     *
//     * Recycler loads successfully.
//     */
//    @Test
//    public void recyclerView_visibleAfterLaunch() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(2000);
//
//        onView(withId(R.id.recyclerChat))
//                .check(matches(isDisplayed()));
//    }
//
//
//
//    /**
//     * Test Case 10
//     *
//     * Send button remains enabled.
//     */
//    @Test
//    public void sendButton_enabledAfterLaunch() {
//
//        ActivityScenario.launch(createIntent());
//
//        SystemClock.sleep(1500);
//
//        onView(withId(R.id.sendBtn))
//                .check(matches(isDisplayed()));
//    }
//}