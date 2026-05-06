//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.*;
//import static androidx.test.espresso.action.ViewActions.*;
//import static androidx.test.espresso.assertion.ViewAssertions.*;
//import static androidx.test.espresso.matcher.ViewMatchers.*;
//import static org.hamcrest.Matchers.not;
//import static org.hamcrest.Matchers.containsString;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//
//import android.content.Intent;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class GroupChatActivitySystemTest {
//
//    private static final int USER_ID = 66;
//    private static final int GROUP_ID = 22;
//    private static final int MODERATOR_ID = 1;
//    private static final String GROUP_NAME = "Updated JJ Test Group";
//
//    private ActivityScenario<GroupChatActivity> scenario;
//
//    private ActivityScenario<GroupChatActivity> launchAsUser() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GroupChatActivity.class);
//        intent.putExtra("USER_ID", USER_ID);
//        intent.putExtra("GROUP_ID", GROUP_ID);
//        intent.putExtra("GROUP_NAME", GROUP_NAME);
//        intent.putExtra("MODERATOR_ID", -1);
//        intent.putExtra("IS_MODERATOR", false);
//        return ActivityScenario.launch(intent);
//    }
//
//    private ActivityScenario<GroupChatActivity> launchAsModerator() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GroupChatActivity.class);
//        intent.putExtra("USER_ID", USER_ID);
//        intent.putExtra("GROUP_ID", GROUP_ID);
//        intent.putExtra("GROUP_NAME", GROUP_NAME);
//        intent.putExtra("MODERATOR_ID", MODERATOR_ID);
//        intent.putExtra("IS_MODERATOR", true);
//        return ActivityScenario.launch(intent);
//    }
//
//    private void waitForNetwork() throws InterruptedException {
//        Thread.sleep(3000);
//    }
//
//    @Before
//    public void setUp() {
//        scenario = launchAsUser();
//    }
//
//    @After
//    public void tearDown() {
//        if (scenario != null) scenario.close();
//    }
//
//    @Test public void t01() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//        onView(withId(R.id.sendBtn)).check(matches(isEnabled()));
//    }
//
//    @Test public void t02() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvGroupName)).check(matches(withText(GROUP_NAME)));
//    }
//
//    @Test public void t03() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvMemberCount)).check(matches(isDisplayed()));
//        onView(withId(R.id.tvMemberCount)).check(matches(not(withText(""))));
//    }
//
//    @Test public void t04() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvMemberCount)).check(matches(withSubstring("members")));
//    }
//
//    @Test public void t05() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t06() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
//        onView(withId(R.id.msgEdt)).perform(typeText("hi"), closeSoftKeyboard());
//        onView(withId(R.id.msgEdt)).check(matches(withText("hi")));
//    }
//
//    @Test public void t07() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnManageGroupChat))
//                .check(matches(withEffectiveVisibility(Visibility.GONE)));
//    }
//
//    @Test public void t08() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewEvents)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t09() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t10() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.layoutPinnedAnnouncement)).check(matches(isDisplayed()));
//        // passes whether VISIBLE or GONE — we just confirm the view is in the hierarchy
//    }
//
//    // t11 - only assert text if banner is actually visible
//    @Test
//    public void t11() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvPinnedAnnouncement)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
//    }
//
//    @Test public void t12() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.recyclerChat)).check(matches(hasMinimumChildCount(1)));
//    }
//
//    @Test public void t13() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.recyclerChat)).perform(swipeUp());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t14() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.recyclerChat)).perform(swipeUp());
//        onView(withId(R.id.recyclerChat)).perform(swipeDown());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t15() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.msgEdt)).check(matches(withText("")));
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t16() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.msgEdt)).perform(typeText("   "), closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t17() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.msgEdt))
//                .perform(typeText("System test message " + System.currentTimeMillis()),
//                        closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.msgEdt)).check(matches(withText("")));
//    }
//
//    @Test public void t18() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.msgEdt))
//                .perform(typeText("Coverage test " + System.currentTimeMillis()),
//                        closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        Thread.sleep(1500);
//        onView(withId(R.id.recyclerChat)).check(matches(hasMinimumChildCount(1)));
//    }
//
//    @Test public void t19() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.msgEdt)).perform(typeText("First"), closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        Thread.sleep(800);
//        onView(withId(R.id.msgEdt)).perform(typeText("Second"), closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.msgEdt)).check(matches(withText("")));
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t20() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvMemberCount)).perform(click());
//        onView(withText("Close")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t21() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvMemberCount)).perform(click());
//        onView(withId(R.id.membersContainer)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t22() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.tvMemberCount)).perform(click());
//        onView(withText("Close")).perform(click());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t23() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewEvents)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Group Events")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t24() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewEvents)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Group Events")).check(matches(isDisplayed()));
//        onView(withText("Close")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t25() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewEvents)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Close")).perform(click());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t26() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Announcements")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t27() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Announcements")).check(matches(isDisplayed()));
//        onView(withText("Close")).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t28() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).perform(click());
//        Thread.sleep(2000);
//        onView(withSubstring("[PINNED]")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t29() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).perform(click());
//        Thread.sleep(2000);
//        onView(withText("Close")).perform(click());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t30() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.btnManageGroupChat)).check(matches(isDisplayed()));
//    }
//
//    @Test public void t31() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.recyclerChat)).check(matches(hasMinimumChildCount(1)));
//    }
//
//    @Test public void t32() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.tvGroupName)).check(matches(withText(GROUP_NAME)));
//    }
//
//    @Test public void t33() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.btnViewEvents)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t34() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.btnViewAnnouncements)).perform(click());
//        Thread.sleep(2000);
//        onView(withSubstring("[PINNED]")).check(matches(isDisplayed()));
//    }
//
//    @Test public void t35() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.btnManageGroupChat)).perform(click());
//        Thread.sleep(1500);
//        onView(withId(R.id.btnManageGroupChat)).check(doesNotExist());
//    }
//
//    @Test public void t36() throws InterruptedException {
//        waitForNetwork();
//        onView(withId(R.id.backBtn)).perform(click());
//        // activity is now finished — no further Espresso assertions possible
//    }
//    @Test public void t37() throws InterruptedException {
//        scenario.close();
//        scenario = launchAsModerator();
//        waitForNetwork();
//        onView(withId(R.id.backBtn)).perform(click());
//        // activity is now finished — no further Espresso assertions possible
//    }
//}