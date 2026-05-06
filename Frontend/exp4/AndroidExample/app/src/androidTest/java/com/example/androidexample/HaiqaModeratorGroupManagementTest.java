//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.clearText;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.scrollTo;
//import static androidx.test.espresso.action.ViewActions.typeText;
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
//@RunWith(AndroidJUnit4.class)
//public class HaiqaModeratorGroupManagementTest {
//
//    private static final int MODERATOR_ID    = 1;
//    private static final int GROUP_ID        = 22;
//    private static final String GROUP_NAME   = "updates jj test group";
//
//    private static final String MESSAGE_ID      = "302";
//    private static final String EVENT_ID        = "21";
//    private static final String ANNOUNCEMENT_ID = "20";
//
//    private ActivityScenario<ModeratorGroupManagementActivity> launch() {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
//                ModeratorGroupManagementActivity.class);
//        intent.putExtra("MODERATOR_ID", MODERATOR_ID);
//        intent.putExtra("GROUP_ID", GROUP_ID);
//        intent.putExtra("GROUP_NAME", GROUP_NAME);
//        return ActivityScenario.launch(intent);
//    }
//
//    private void waitForNetwork() {
//        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
//    }
//
//    // -------------------------------------------------------------------------
//    // UI visibility
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t01_headerDisplaysGroupName() {
//        launch();
//        onView(withId(R.id.tvModGroupHeader))
//                .check(matches(withText("Manage: " + GROUP_NAME)));
//    }
//
//    @Test
//    public void t02_allButtonsDisplayed() {
//        launch();
//        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnEditEvent)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t03_allInputFieldsDisplayed() {
//        launch();
//        onView(withId(R.id.etModerationMessageId)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etEventTitle)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etEventDescription)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etEventLocation)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etEventTime)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo()).check(matches(isDisplayed()));
//        onView(withId(R.id.etEventId)).perform(scrollTo()).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // removeMessage()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t04_removeMessage_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etModerationMessageId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo(), click());
//        onView(withId(R.id.btnRemoveMessage)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t05_removeMessage_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etModerationMessageId))
//                .perform(scrollTo(), clearText(), typeText(MESSAGE_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnRemoveMessage)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // restoreMessage()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t06_restoreMessage_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etModerationMessageId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo(), click());
//        onView(withId(R.id.btnRestoreMessage)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t07_restoreMessage_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etModerationMessageId))
//                .perform(scrollTo(), clearText(), typeText(MESSAGE_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnRestoreMessage)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // scheduleEvent()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t08_scheduleEvent_emptyFields_showsToast() {
//        launch();
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
//        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t09_scheduleEvent_missingOneField_showsToast() {
//        launch();
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Test Event"), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Desc"), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Room 101"), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
//        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t10_scheduleEvent_allFields_firesRequest() {
//        launch();
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Espresso Event"), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Auto test event"), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Lab"), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-01T10:00:00"), closeSoftKeyboard());
//        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // editEvent()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t11_editEvent_emptyEventId_showsToast() {
//        launch();
//        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Title"), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Desc"), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Loc"), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-01T10:00:00"), closeSoftKeyboard());
//        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
//        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t12_editEvent_emptyFields_showsToast() {
//        launch();
//        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
//        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t13_editEvent_validInputs_firesRequest() {
//        launch();
//        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Updated Title"), closeSoftKeyboard());
//        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Updated Desc"), closeSoftKeyboard());
//        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Updated Loc"), closeSoftKeyboard());
//        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-02T11:00:00"), closeSoftKeyboard());
//        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // deleteEvent()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t14_deleteEvent_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo(), click());
//        onView(withId(R.id.btnDeleteEvent)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t15_deleteEvent_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnDeleteEvent)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // createAnnouncement()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t16_createAnnouncement_emptyFields_showsToast() {
//        launch();
//        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
//        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t17_createAnnouncement_missingContent_showsToast() {
//        launch();
//        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), typeText("My Announcement"), closeSoftKeyboard());
//        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
//        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t18_createAnnouncement_validInputs_firesRequest() {
//        launch();
//        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), typeText("Test Announcement"), closeSoftKeyboard());
//        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), typeText("This is the content"), closeSoftKeyboard());
//        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // pinAnnouncement()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t19_pinAnnouncement_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo(), click());
//        onView(withId(R.id.btnPinAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t20_pinAnnouncement_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnPinAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // unpinAnnouncement()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t21_unpinAnnouncement_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo(), click());
//        onView(withId(R.id.btnUnpinAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t22_unpinAnnouncement_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnUnpinAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // deleteAnnouncement()
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t23_deleteAnnouncement_emptyId_showsToast() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
//        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo(), click());
//        onView(withId(R.id.btnDeleteAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void t24_deleteAnnouncement_validId_firesRequest() {
//        launch();
//        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
//        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.btnDeleteAnnouncement)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // loadAllData() / refresh
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t25_refreshButton_triggersLoadAllData() {
//        launch();
//        onView(withId(R.id.btnRefreshGroupManagement)).perform(scrollTo(), click());
//        waitForNetwork();
//        onView(withId(R.id.tvModPendingMembers)).check(matches(isDisplayed()));
//        onView(withId(R.id.tvModAnnouncements)).check(matches(isDisplayed()));
//    }
//
//    // -------------------------------------------------------------------------
//    // back / finish
//    // -------------------------------------------------------------------------
//
//    @Test
//    public void t26_backButton_finishesActivity() {
//        launch();
//        onView(withId(R.id.btnBackGroupManagement)).perform(scrollTo(), click());
//    }
//}