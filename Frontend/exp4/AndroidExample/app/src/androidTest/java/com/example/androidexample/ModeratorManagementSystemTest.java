package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ModeratorManagementSystemTest {

    private static final int    MODERATOR_ID = 1;
    private static final int    GROUP_ID     = 22;
    private static final String GROUP_NAME   = "Updated JJ Test Group";

    private ActivityScenario<ModeratorGroupManagementActivity> scenario;

    @Before
    public void setUp() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ModeratorGroupManagementActivity.class);
        intent.putExtra("MODERATOR_ID", MODERATOR_ID);
        intent.putExtra("GROUP_ID",     GROUP_ID);
        intent.putExtra("GROUP_NAME",   GROUP_NAME);
        scenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (scenario != null) scenario.close();
    }

    private void waitForNetwork() throws InterruptedException {
        Thread.sleep(3000);
    }



    @Test public void t03_edit_and_delete_event_buttons() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteEvent)).check(matches(isDisplayed()));
    }

    @Test public void t04_single_member_approval() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etApproveMemberId))
                .perform(typeText("66"), closeSoftKeyboard());
        onView(withId(R.id.btnApproveMember)).perform(click());
    }

    @Test public void t05_sequential_member_approvals() throws InterruptedException {
        waitForNetwork();
        for (int i = 1; i <= 3; i++) {
            onView(withId(R.id.etApproveMemberId))
                    .perform(clearText(), typeText(String.valueOf(i)), closeSoftKeyboard());
            onView(withId(R.id.btnApproveMember)).perform(click());
        }
    }

    @Test public void t06_member_approval_with_validation() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etApproveMemberId))
                .perform(typeText("66"), closeSoftKeyboard());
        onView(withId(R.id.etApproveMemberId)).check(matches(withText("66")));
        onView(withId(R.id.btnApproveMember)).perform(click());
    }

    @Test public void t07_single_member_removal() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etRemoveMemberId))
                .perform(typeText("200"), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMember)).perform(click());
    }

    @Test public void t08_multiple_member_removals() throws InterruptedException {
        waitForNetwork();
        for (int i = 0; i < 2; i++) {
            onView(withId(R.id.etRemoveMemberId))
                    .perform(clearText(), typeText(String.valueOf(200 + i)), closeSoftKeyboard());
            onView(withId(R.id.btnRemoveMember)).perform(click());
        }
    }

    @Test public void t09_approve_then_remove_same_member() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etApproveMemberId))
                .perform(typeText("66"), closeSoftKeyboard());
        onView(withId(R.id.btnApproveMember)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.etRemoveMemberId))
                .perform(typeText("66"), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMember)).perform(click());
    }

    @Test public void t10_remove_single_message() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etModerationMessageId))
                .perform(typeText("5001"), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMessage)).perform(click());
    }

    @Test public void t11_restore_single_message() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etModerationMessageId))
                .perform(typeText("5002"), closeSoftKeyboard());
        onView(withId(R.id.btnRestoreMessage)).perform(click());
    }

    @Test public void t12_remove_and_restore_same_message() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etModerationMessageId))
                .perform(typeText("5003"), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMessage)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.etModerationMessageId))
                .perform(clearText(), typeText("5003"), closeSoftKeyboard());
        onView(withId(R.id.btnRestoreMessage)).perform(click());
    }

    @Test public void t13_multiple_message_removals() throws InterruptedException {
        waitForNetwork();
        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.etModerationMessageId))
                    .perform(clearText(), typeText(String.valueOf(5100 + i)), closeSoftKeyboard());
            onView(withId(R.id.btnRemoveMessage)).perform(click());
        }
    }

    @Test public void t14_create_minimal_event() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etEventTitle))
                .perform(typeText("Event"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription))
                .perform(typeText("Desc"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation))
                .perform(typeText("Loc"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime))
                .perform(typeText("2024-01-01"), closeSoftKeyboard());
        onView(withId(R.id.btnScheduleEvent)).perform(click());
    }

    @Test public void t15_create_event_with_rich_description() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etEventTitle))
                .perform(typeText("Annual Conference 2024"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription))
                .perform(typeText("A comprehensive conference."), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation))
                .perform(typeText("Convention Center"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime))
                .perform(typeText("2024-06-15 14:00"), closeSoftKeyboard());
        onView(withId(R.id.btnScheduleEvent)).perform(click());
    }

    @Test public void t16_multiple_event_creations() throws InterruptedException {
        waitForNetwork();
        String[] events = {"Meeting", "Workshop", "Seminar"};
        for (int i = 0; i < events.length; i++) {
            onView(withId(R.id.etEventTitle))
                    .perform(clearText(), typeText(events[i]), closeSoftKeyboard());
            onView(withId(R.id.etEventDescription))
                    .perform(clearText(), typeText("Description " + i), closeSoftKeyboard());
            onView(withId(R.id.etEventLocation))
                    .perform(clearText(), typeText("Location " + i), closeSoftKeyboard());
            onView(withId(R.id.etEventTime))
                    .perform(clearText(), typeText("2024-0" + (i + 1) + "-01"), closeSoftKeyboard());
            onView(withId(R.id.btnScheduleEvent)).perform(click());
            Thread.sleep(500);
        }
    }

    @Test public void t17_edit_event_change_title() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etEventId))
                .perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.etEventTitle))
                .perform(typeText("New Title"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription))
                .perform(typeText("New Desc"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation))
                .perform(typeText("New Loc"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime))
                .perform(typeText("2024-02-01"), closeSoftKeyboard());
        onView(withId(R.id.btnEditEvent)).perform(click());
    }

    @Test public void t18_edit_event_all_fields() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etEventId))
                .perform(typeText("101"), closeSoftKeyboard());
        onView(withId(R.id.etEventTitle))
                .perform(typeText("Updated Event"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription))
                .perform(typeText("Updated Description"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation))
                .perform(typeText("Updated Location"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime))
                .perform(typeText("2024-03-15"), closeSoftKeyboard());
        onView(withId(R.id.btnEditEvent)).perform(click());
    }

    @Test public void t19_delete_single_event() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etEventId))
                .perform(typeText("102"), closeSoftKeyboard());
        onView(withId(R.id.btnDeleteEvent)).perform(click());
    }

    @Test public void t20_delete_multiple_events() throws InterruptedException {
        waitForNetwork();
        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.etEventId))
                    .perform(clearText(), typeText(String.valueOf(102 + i)), closeSoftKeyboard());
            onView(withId(R.id.btnDeleteEvent)).perform(click());
            Thread.sleep(500);
        }
    }




    @Test public void t32_rapid_member_management() throws InterruptedException {
        waitForNetwork();
        for (int i = 0; i < 5; i++) {
            onView(withId(R.id.etApproveMemberId))
                    .perform(clearText(), typeText(String.valueOf(2000 + i)), closeSoftKeyboard());
            onView(withId(R.id.btnApproveMember)).perform(click());
            Thread.sleep(300);
        }
    }

    @Test public void t33_rapid_message_moderation() throws InterruptedException {
        waitForNetwork();
        for (int i = 0; i < 5; i++) {
            onView(withId(R.id.etModerationMessageId))
                    .perform(clearText(), typeText(String.valueOf(9100 + i)), closeSoftKeyboard());
            if (i % 2 == 0) {
                onView(withId(R.id.btnRemoveMessage)).perform(click());
            } else {
                onView(withId(R.id.btnRestoreMessage)).perform(click());
            }
            Thread.sleep(300);
        }
    }


    @Test public void t39_text_entry_preservation() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etApproveMemberId))
                .perform(typeText("5000"), closeSoftKeyboard());
        onView(withId(R.id.etApproveMemberId)).check(matches(withText("5000")));
    }

    @Test public void t40_clear_input_fields() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.etApproveMemberId))
                .perform(typeText("5001"), closeSoftKeyboard());
        onView(withId(R.id.etApproveMemberId))
                .perform(clearText())
                .check(matches(withText("")));
    }

    @Test public void t41_all_buttons_enabled() throws InterruptedException {
        waitForNetwork();
        onView(withId(R.id.btnApproveMember)).check(matches(isEnabled()));
        onView(withId(R.id.btnRemoveMember)).check(matches(isEnabled()));
        onView(withId(R.id.btnScheduleEvent)).check(matches(isEnabled()));
        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isEnabled()));
    }


}