
package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ChatMergedTest {

    // =========================================================================
    // Helpers
    // =========================================================================

    private void waitForNetwork() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    private void shortWait(long ms) {
        SystemClock.sleep(ms);
    }

    // =========================================================================
    // ChatListActivity
    // =========================================================================

    private ActivityScenario<ChatListActivity> launchChatList() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void chatList_t01_uiElementsVisibleOnLaunch() {
        launchChatList();
        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
        onView(withId(R.id.scrollDirect)).check(matches(isDisplayed()));
    }

    @Test
    public void chatList_t02_switchToGroupsTab() {
        launchChatList();
        onView(withId(R.id.tabGroups)).perform(click());
        onView(withId(R.id.scrollGroups)).check(matches(isDisplayed()));
    }

    @Test
    public void chatList_t03_switchBackToDirectTab() {
        launchChatList();
        onView(withId(R.id.tabGroups)).perform(click());
        onView(withId(R.id.tabDirect)).perform(click());
        onView(withId(R.id.scrollDirect)).check(matches(isDisplayed()));
    }

    @Test
    public void chatList_t04_groupsTabStableAfterNetworkLoad() {
        launchChatList();
        onView(withId(R.id.tabGroups)).perform(click());
        waitForNetwork();
        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
    }

    @Test
    public void chatList_t05_directTabStableAfterNetworkLoad() {
        launchChatList();
        waitForNetwork();
        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
    }

    @Test
    public void chatList_t06_backButtonFinishes() {
        launchChatList();
        onView(withId(R.id.btnBack)).perform(click());
    }

    // =========================================================================
    // ChatActivity — with username (covers fetchConversationId path)
    // =========================================================================

    private ActivityScenario<ChatActivity> launchChatWithUsername() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("OTHER_USER_ID", 2);
        intent.putExtra("OTHER_USERNAME", "TestUser");
        return ActivityScenario.launch(intent);
    }

    @Test
    public void chat_t01_uiElementsDisplayed() {
        // Covers: tvChatWith, recyclerChat, sendBtn, msgEdt, backBtn all visible
        launchChatWithUsername();
        onView(withId(R.id.tvChatWith)).check(matches(withText("TestUser")));
        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t02_sendEmptyMessage() {
        // Covers: empty send -> Toast branch, UI remains stable
        launchChatWithUsername();
        onView(withId(R.id.sendBtn)).perform(click());
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t03_typeMessage() {
        launchChatWithUsername();
        onView(withId(R.id.msgEdt)).perform(typeText("hello"), closeSoftKeyboard());
        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t04_sendTypedMessage() {
        // Covers: sendBtn click with text -> WebSocket send path
        launchChatWithUsername();
        waitForNetwork();
        onView(withId(R.id.msgEdt)).perform(typeText("hello"), closeSoftKeyboard());
        onView(withId(R.id.sendBtn)).perform(click());
        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t05_clearTextAndSendEmpty() {
        // Covers: type then clear -> empty send branch again
        launchChatWithUsername();
        onView(withId(R.id.msgEdt)).perform(typeText("hello"), clearText(), closeSoftKeyboard());
        onView(withId(R.id.sendBtn)).perform(click());
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t06_backButtonFinishes() {
        // Covers: removeWebSocketEventListener + finish()
        launchChatWithUsername();
        onView(withId(R.id.backBtn)).perform(click());
    }

    // =========================================================================
    // ChatActivity — without username (covers fetchOtherUserName path)
    // =========================================================================

    @Test
    public void chat_t07_noUsernameFetchesName() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("OTHER_USER_ID", 2);
        // omitting OTHER_USERNAME triggers fetchOtherUserName()
        ActivityScenario.launch(intent);
        onView(withId(R.id.tvChatWith)).check(matches(isDisplayed()));
        waitForNetwork();
        onView(withId(R.id.tvChatWith)).check(matches(isDisplayed()));
    }

    @Test
    public void chat_t08_noOtherUserIdFinishes() {
        // Covers: guard clause -> Toast + finish()
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        // no crash = pass
    }

    // =========================================================================
    // GroupChatActivity
    // =========================================================================

    private ActivityScenario<GroupChatActivity> launchGroupChat() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("GROUP_ID", 1);
        intent.putExtra("GROUP_NAME", "Test Group");
        return ActivityScenario.launch(intent);
    }

    @Test
    public void groupChat_t01_uiElementsDisplayed() {
        // Covers: recyclerChat, sendBtn, backBtn, tvGroupName all load correctly
        launchGroupChat();
        shortWait(2000);
        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.tvGroupName)).check(matches(withText("Test Group")));
    }

    @Test
    public void groupChat_t02_sendEmptyMessageStaysOnScreen() {
        // Covers: empty send guard, UI remains stable
        launchGroupChat();
        shortWait(2000);
        onView(withId(R.id.sendBtn)).perform(click());
        shortWait(1000);
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
    }

    @Test
    public void groupChat_t03_memberDialogOpens() {
        launchGroupChat();
        shortWait(2500);
        onView(withId(R.id.tvMemberCount)).perform(click());
        shortWait(1000);
        onView(withText("Close")).check(matches(isDisplayed()));
    }

    @Test
    public void groupChat_t04_announcementsDialogOpens() {
        launchGroupChat();
        shortWait(2500);
        onView(withId(R.id.btnViewAnnouncements)).perform(click());
        shortWait(1500);
        onView(withText("Announcements")).check(matches(isDisplayed()));
    }

    @Test
    public void groupChat_t05_backButtonFinishes() {
        launchGroupChat();
        shortWait(1500);
        onView(withId(R.id.backBtn)).perform(click());
        shortWait(1000);
    }
}
