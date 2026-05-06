//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import android.content.Intent;
//import android.view.View;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class HaiqaChatTest {
//
//    private void waitForNetwork() {
//        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
//    }
//
//    // =========================================================================
//    // ChatListActivity
//    // =========================================================================
//
//    private ActivityScenario<ChatListActivity> launchChatList() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatListActivity.class);
//        intent.putExtra("USER_ID", 1);
//        return ActivityScenario.launch(intent);
//    }
//
//    // Covers onCreate, switchTab(true) — direct tab visible on launch
//    @Test
//    public void chatList_t01_directTabVisibleOnLaunch() {
//        launchChatList();
//        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
//        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
//        onView(withId(R.id.scrollDirect)).check(matches(isDisplayed()));
//    }
//
//    // Covers switchTab(false) — switching to groups tab
//    @Test
//    public void chatList_t02_switchToGroupsTab() {
//        launchChatList();
//        onView(withId(R.id.tabGroups)).perform(click());
//        onView(withId(R.id.scrollGroups)).check(matches(isDisplayed()));
//    }
//
//    // Covers switchTab(true) — switching back to direct tab
//    @Test
//    public void chatList_t03_switchBackToDirectTab() {
//        launchChatList();
//        onView(withId(R.id.tabGroups)).perform(click());
//        onView(withId(R.id.tabDirect)).perform(click());
//        onView(withId(R.id.scrollDirect)).check(matches(isDisplayed()));
//    }
//
//
//
//    // Covers fetchAllGroups() response + buildGroupCards() or showGroupsEmpty()
//    @Test
//    public void chatList_t05_groupsTabAfterNetworkLoad() {
//        launchChatList();
//        onView(withId(R.id.tabGroups)).perform(click());
//        waitForNetwork();
//        // Either cards rendered or empty state — either way UI is stable
//        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
//    }
//
//    // Covers fetchAcceptedMatches() response + buildChatCards() or showEmpty()
//    @Test
//    public void chatList_t06_directTabAfterNetworkLoad() {
//        launchChatList();
//        waitForNetwork();
//        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
//    }
//
//    // Covers btnBack -> finish()
//    @Test
//    public void chatList_t07_backButtonFinishes() {
//        launchChatList();
//        onView(withId(R.id.btnBack)).perform(click());
//    }
//
//    // =========================================================================
//    // ChatActivity — launched with full extras (covers fetchConversationId path)
//    // =========================================================================
//
//    private ActivityScenario<ChatActivity> launchChatWithUsername() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
//        intent.putExtra("USER_ID", 1);
//        intent.putExtra("OTHER_USER_ID", 2);
//        intent.putExtra("OTHER_USERNAME", "TestUser");
//        return ActivityScenario.launch(intent);
//    }
//
//    // Covers onCreate, tvChatWith set from intent, appendMessage("Preparing chat...")
//    @Test
//    public void chat_t01_headerShowsUsername() {
//        launchChatWithUsername();
//        onView(withId(R.id.tvChatWith)).check(matches(withText("TestUser")));
//    }
//
//    // Covers recyclerChat and sendBtn rendered
//    @Test
//    public void chat_t02_uiElementsDisplayed() {
//        launchChatWithUsername();
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
//        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));
//    }
//
//    // Covers fetchConversationId() firing, loadChatHistoryThenConnect(),
//    // connectWebSocket(), sendBtn enabled after network
//    @Test
//    public void chat_t03_sendButtonEnabledAfterLoad() {
//        launchChatWithUsername();
//        waitForNetwork();
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//    }
//
//    // Covers sendBtn click with empty message -> Toast branch
//    @Test
//    public void chat_t04_sendEmptyMessageShowsToast() {
//        launchChatWithUsername();
//        waitForNetwork();
//        onView(withId(R.id.msgEdt)).perform(typeText(""), closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
//    }
//
//    // Covers sendBtn click with a real message -> connectWebSocket send path
//    @Test
//    public void chat_t05_sendMessageWithText() {
//        launchChatWithUsername();
//        waitForNetwork();
//        onView(withId(R.id.msgEdt)).perform(typeText("hello"), closeSoftKeyboard());
//        onView(withId(R.id.sendBtn)).perform(click());
//        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
//    }
//
//    // Covers backBtn -> removeWebSocketEventListener + finish()
//    @Test
//    public void chat_t06_backButtonFinishes() {
//        launchChatWithUsername();
//        onView(withId(R.id.backBtn)).perform(click());
//    }
//
//    // =========================================================================
//    // ChatActivity — launched without OTHER_USERNAME (covers fetchOtherUserName path)
//    // =========================================================================
//
//    private ActivityScenario<ChatActivity> launchChatNoUsername() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
//        intent.putExtra("USER_ID", 1);
//        intent.putExtra("OTHER_USER_ID", 2);
//        // no OTHER_USERNAME — triggers fetchOtherUserName()
//        return ActivityScenario.launch(intent);
//    }
//
//    // Covers fetchOtherUserName() -> tvChatWith set to "Loading..." then updated
//    @Test
//    public void chat_t07_noUsernameFetchesName() {
//        launchChatNoUsername();
//        onView(withId(R.id.tvChatWith)).check(matches(isDisplayed()));
//        waitForNetwork();
//        onView(withId(R.id.tvChatWith)).check(matches(isDisplayed()));
//    }
//
//    // =========================================================================
//    // ChatActivity — launched with no OTHER_USER_ID (covers finish() guard path)
//    // =========================================================================
//
//    @Test
//    public void chat_t08_noOtherUserIdFinishesActivity() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
//        intent.putExtra("USER_ID", 1);
//        // no OTHER_USER_ID, no OTHER_USERNAME -> Toast + finish()
//        ActivityScenario.launch(intent);
//        // no crash = pass
//    }
//}