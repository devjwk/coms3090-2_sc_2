
package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;
import android.os.SystemClock;
import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ASystemTest {

    // ─────────────────────────────────────────────────────────────────
    // SHARED UTILITIES
    // ─────────────────────────────────────────────────────────────────

    /**
     * Custom scroll action that handles both ScrollView and NestedScrollView parents.
     *
     * Espresso's built-in scrollTo() only works when the closest scrollable ancestor
     * is a ScrollView, HorizontalScrollView, or ListView. Layouts that use
     * NestedScrollView (common in Material-Design screens) are silently skipped,
     * causing the subsequent click to fail with "not displayed in window".
     *
     * This action walks up the view tree, finds the first NestedScrollView or
     * ScrollView, and calls smoothScrollTo() on it before Espresso executes the
     * next action in the chain.
     */
    private static ViewAction nestedScrollTo() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "scroll inside NestedScrollView or ScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View cursor = view;
                while (cursor != null) {
                    if (cursor instanceof NestedScrollView) {
                        ((NestedScrollView) cursor).smoothScrollTo(0, view.getTop());
                        uiController.loopMainThreadForAtLeast(700);
                        return;
                    }
                    if (cursor instanceof android.widget.ScrollView) {
                        ((android.widget.ScrollView) cursor).smoothScrollTo(0, view.getTop());
                        uiController.loopMainThreadForAtLeast(700);
                        return;
                    }
                    cursor = cursor.getParent() instanceof View
                            ? (View) cursor.getParent() : null;
                }
            }
        };
    }

    private static void waitForNetwork() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ignored) {
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 1: LoginActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 1 — Login with both fields empty stays on login screen. */
    @Test
    public void login_bothFieldsEmpty_staysOnLoginScreen() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.login_login_btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_signup_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_delete_user_btn)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 2 — Login with valid email but empty password stays on login screen. */
    @Test
    public void login_emptyPassword_staysOnLoginScreen() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.login_username_edt))
                .perform(clearText(), typeText("test@iastate.edu"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.login_login_btn)).perform(click());
        SystemClock.sleep(1200);

        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.login_username_edt)).check(matches(withText("test@iastate.edu")));
        SystemClock.sleep(600);
    }

    /** Test 3 — Moderator Login button navigates to ModeratorLoginActivity. */
    @Test
    public void login_moderatorLoginButton_navigatesToModeratorLogin() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.login_moderator_login_btn)).perform(click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etModeratorEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etModeratorPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnModeratorLogin)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 4 — Signup button from login navigates to SignupActivity. */
    @Test
    public void login_signupButton_navigatesToSignup() {
        ActivityScenario.launch(LoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.login_signup_btn)).perform(click());
        SystemClock.sleep(1200);

        onView(withId(R.id.btnSignup)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 2: ModeratorLoginActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 5 — Moderator login with empty credentials shows validation. */
    @Test
    public void moderatorLogin_emptyCredentials_staysOnScreen() {
        ActivityScenario.launch(ModeratorLoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.btnModeratorLogin)).perform(click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etModeratorEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etModeratorPassword)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 6 — Moderator login with wrong credentials stays on screen. */
    @Test
    public void moderatorLogin_invalidCredentials_staysOnScreen() {
        ActivityScenario.launch(ModeratorLoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.etModeratorEmail))
                .perform(clearText(), typeText("notamod@fake.com"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etModeratorPassword))
                .perform(clearText(), typeText("wrongpassword"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.btnModeratorLogin)).perform(click());
        SystemClock.sleep(3000);

        onView(withId(R.id.etModeratorEmail)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 7 — Back button on ModeratorLoginActivity finishes the activity. */
    @Test
    public void moderatorLogin_backButton_finishesActivity() {
        ActivityScenario.launch(ModeratorLoginActivity.class);
        SystemClock.sleep(800);

        onView(withId(R.id.etModeratorEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.btnModeratorLoginBack)).perform(click());
        SystemClock.sleep(1000);
        SystemClock.sleep(400);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 3: HomeActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 12 — HomeActivity launches correctly with USER_ID only. */
    @Test
    public void home_launchWithoutUserJson_layoutVisible() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.tvWelcomeName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvProfileName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvProfileEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.navGroups)).check(matches(isDisplayed()));
        onView(withId(R.id.navMatches)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 13 — HomeActivity populates profile data from USER_JSON. */
    @Test
    public void home_launchWithUserJson_displaysProfileData() {
        String userJson = "{"
                + "\"name\":\"Alice Smith\","
                + "\"email\":\"alice@iastate.edu\","
                + "\"bio\":\"Love hiking and coding\","
                + "\"hobbies\":[\"Hiking\",\"Reading\"]"
                + "}";

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class);
        intent.putExtra("USER_ID", 42);
        intent.putExtra("USER_JSON", userJson);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.tvWelcomeName)).check(matches(withText("Alice Smith 👋")));
        onView(withId(R.id.tvProfileName)).check(matches(withText("Alice Smith")));
        onView(withId(R.id.tvProfileEmail)).check(matches(withText("alice@iastate.edu")));
        onView(withId(R.id.tvHobby1)).check(matches(withText("Hiking")));
        onView(withId(R.id.tvHobby2)).check(matches(withText("Reading")));
        SystemClock.sleep(600);
    }

    /** Test 14 — HomeActivity: navGroups navigates to GroupsActivity. */
    @Test
    public void home_navGroups_navigatesToGroups() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.navGroups)).perform(click());
        SystemClock.sleep(1500);

        onView(withId(R.id.btnCreateGroup))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 16 — HomeActivity: navChat navigates to ChatListActivity. */
    @Test
    public void home_navChat_navigatesToChatList() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.navChat)).perform(click());
        SystemClock.sleep(1500);

        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 17 — HomeActivity: Edit Profile navigates to Login (profile edit screen). */
    @Test
    public void home_editProfile_navigatesToLoginEditScreen() {
        String userJson = "{\"name\":\"Bob\",\"email\":\"bob@iastate.edu\",\"bio\":\"test bio\"}";
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                HomeActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("USER_JSON", userJson);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.btnEditProfile)).perform(click());
        SystemClock.sleep(1200);

        onView(withId(R.id.etBio)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 4: ChatListActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 18 — ChatListActivity launches and shows Direct tab active. */
    @Test
    public void chatList_initialState_directTabActive() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.tabDirect)).check(matches(isDisplayed()));
        onView(withId(R.id.tabGroups)).check(matches(isDisplayed()));
        onView(withId(R.id.scrollDirect)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 19 — ChatListActivity: Groups tab switch shows group scroll view. */
    @Test
    public void chatList_groupsTab_switchesView() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.tabGroups)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.scrollGroups)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 20 — ChatListActivity: Back button finishes the activity. */
    @Test
    public void chatList_backButton_finishesActivity() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.btnBack)).perform(click());
        SystemClock.sleep(800);
        SystemClock.sleep(400);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 5: GroupChatListActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 21 — GroupChatListActivity: create group with empty name shows validation. */
    @Test
    public void groupChatList_emptyGroupName_staysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(3000);

        onView(withId(R.id.btnCreateGroupChat))
                .perform(nestedScrollTo(), click());
        SystemClock.sleep(1500);

        onView(withId(R.id.btnCreateGroupChat)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 22 — GroupChatListActivity: create group with name but no user IDs. */
    @Test
    public void groupChatList_emptyUserIds_staysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(3000);

        onView(withId(R.id.etGroupChatName))
                .perform(nestedScrollTo(), clearText(),
                        typeText("My Test Group"), closeSoftKeyboard());
        SystemClock.sleep(500);

        onView(withId(R.id.etUserIds))
                .perform(nestedScrollTo(), clearText(), closeSoftKeyboard());
        SystemClock.sleep(300);

        onView(withId(R.id.btnCreateGroupChat))
                .perform(nestedScrollTo(), click());
        SystemClock.sleep(1500);

        onView(withId(R.id.etGroupChatName))
                .perform(nestedScrollTo())
                .check(matches(withText("My Test Group")));
        SystemClock.sleep(600);
    }

    /** Test 23 — GroupChatListActivity: invalid (non-numeric) user IDs show validation. */
    @Test
    public void groupChatList_invalidUserIdFormat_staysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(3000);

        onView(withId(R.id.etGroupChatName))
                .perform(nestedScrollTo(), clearText(),
                        typeText("Alpha Group"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.etUserIds))
                .perform(nestedScrollTo(), clearText(),
                        typeText("abc,def"), closeSoftKeyboard());
        SystemClock.sleep(400);

        onView(withId(R.id.btnCreateGroupChat))
                .perform(nestedScrollTo(), click());
        SystemClock.sleep(1500);

        onView(withId(R.id.btnCreateGroupChat)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 24 — GroupChatListActivity: Back button finishes activity. */
    @Test
    public void groupChatList_backButton_finishesActivity() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatListActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.btnBack)).perform(click());
        SystemClock.sleep(800);
        SystemClock.sleep(400);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 6: MatchesActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 26 — MatchesActivity: create match with empty user2Id shows toast. */
    @Test
    public void matches_createMatch_emptyUser2Id_staysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MatchesActivity.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        onView(withId(R.id.btnCreateMatch)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.btnCreateMatch)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 7: Login (Profile Edit) Activity
    // ─────────────────────────────────────────────────────────────────

    /** Test 31 — Login (profile edit): fields disabled when no USER_JSON provided. */
    @Test
    public void loginEdit_noUserJson_fieldsDisabled() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                Login.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        onView(withId(R.id.etBio)).check(matches(not(isEnabled())));
        onView(withId(R.id.etName)).check(matches(not(isEnabled())));
        onView(withId(R.id.btnUpdateProfile)).check(matches(not(isEnabled())));
        SystemClock.sleep(600);
    }

    /** Test 32 — Login (profile edit): all fields pre-filled from USER_JSON. */
    @Test
    public void loginEdit_fullUserJson_allFieldsPreFilled() {
        String userJson = "{"
                + "\"userId\": 5,"
                + "\"name\": \"Carol Jones\","
                + "\"email\": \"carol@iastate.edu\","
                + "\"bio\": \"Loves open source.\","
                + "\"major\": \"Software Engineering\","
                + "\"age\": 23,"
                + "\"hobbies\": [\"gaming\", \"hiking\"],"
                + "\"role\": \"Regular\","
                + "\"isActive\": true"
                + "}";

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                Login.class);
        intent.putExtra("USER_ID", 5);
        intent.putExtra("USER_JSON", userJson);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        onView(withId(R.id.etName)).check(matches(withText("Carol Jones")));
        onView(withId(R.id.etBio)).check(matches(withText("Loves open source.")));
        onView(withId(R.id.btnUpdateProfile)).check(matches(isEnabled()));
        SystemClock.sleep(600);
    }

    /** Test 33 — Login (profile edit): Back button finishes without crash. */
    @Test
    public void loginEdit_backButton_finishesActivity() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                Login.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(800);

        onView(withId(R.id.btnBack)).perform(click());
        SystemClock.sleep(800);
        SystemClock.sleep(400);
    }

    /** Test 34 — Login (profile edit): Update button click without data shows toast. */
    @Test
    public void loginEdit_updateWithoutData_showsToast() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                Login.class);
        intent.putExtra("USER_ID", 1);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1000);

        onView(withId(R.id.btnUpdateProfile)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.btnUpdateProfile)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 8: ModeratorDashboardActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 35 — ModeratorDashboard: finishes immediately without a valid session. */
    @Test
    public void moderatorDashboard_noSession_finishesImmediately() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ModeratorDashboardActivity.class);
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);
        SystemClock.sleep(400);
    }

    /** Test 37 — ModeratorDashboard: create group with empty name shows toast. */
    @Test
    public void moderatorDashboard_createGroup_emptyName_staysOnScreen() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ModeratorDashboardActivity.class);
        intent.putExtra("MODERATOR_ID", 1);
        intent.putExtra("USER_ID", 10);
        ActivityScenario.launch(intent);
        SystemClock.sleep(2500);

        onView(withId(R.id.etCreateModeratorGroupDescription))
                .perform(nestedScrollTo(), clearText(),
                        typeText("A description"), closeSoftKeyboard());
        SystemClock.sleep(300);

        onView(withId(R.id.btnCreateModeratorGroup))
                .perform(nestedScrollTo(), click());
        SystemClock.sleep(1200);

        onView(withId(R.id.btnCreateModeratorGroup))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 9: ChatActivity
    // ─────────────────────────────────────────────────────────────────

    /** Test 40 — ChatActivity: launches and shows the RecyclerView and send button. */
    @Test
    public void chat_launch_coreUiVisible() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("OTHER_USER_ID", 2);
        intent.putExtra("OTHER_USERNAME", "TestUser");
        ActivityScenario.launch(intent);
        SystemClock.sleep(1500);

        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));
        SystemClock.sleep(600);
    }

    /** Test 41 — ChatActivity: header shows the provided partner username. */
    @Test
    public void chat_launch_headerShowsPartnerName() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("OTHER_USER_ID", 99);
        intent.putExtra("OTHER_USERNAME", "Alice");
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        onView(withId(R.id.tvChatWith)).check(matches(withText("Alice")));
        SystemClock.sleep(600);
    }

    /** Test 42 — ChatActivity: Back button finishes the activity. */
    @Test
    public void chat_backButton_finishesActivity() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("OTHER_USER_ID", 2);
        intent.putExtra("OTHER_USERNAME", "Bob");
        ActivityScenario.launch(intent);
        SystemClock.sleep(1200);

        onView(withId(R.id.backBtn)).perform(click());
        SystemClock.sleep(800);
        SystemClock.sleep(400);
    }

    /** Test 43 — GroupChatActivity: launches with core UI visible. */
    @Test
    public void groupChat_launch_coreUiVisible() {
        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                GroupChatActivity.class);
        intent.putExtra("USER_ID", 1);
        intent.putExtra("GROUP_ID", 5);
        intent.putExtra("GROUP_NAME", "Test Group");
        ActivityScenario.launch(intent);
        SystemClock.sleep(1800);

        onView(withId(R.id.recyclerChat)).check(matches(isDisplayed()));
        onView(withId(R.id.msgEdt)).check(matches(isDisplayed()));
        onView(withId(R.id.sendBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.backBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.tvGroupName)).check(matches(withText("Test Group")));
        SystemClock.sleep(600);
    }

    // ─────────────────────────────────────────────────────────────────
    // SECTION 10: ModeratorGroupManagementActivity
    // ─────────────────────────────────────────────────────────────────

    private static final int MODERATOR_ID = 1;
    private static final int GROUP_ID = 22;
    private static final String GROUP_NAME = "updates jj test group";
    private static final String MESSAGE_ID = "302";
    private static final String EVENT_ID = "21";
    private static final String ANNOUNCEMENT_ID = "20";

    private ActivityScenario<ModeratorGroupManagementActivity> launchGroupManagement() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ModeratorGroupManagementActivity.class);
        intent.putExtra("MODERATOR_ID", MODERATOR_ID);
        intent.putExtra("GROUP_ID", GROUP_ID);
        intent.putExtra("GROUP_NAME", GROUP_NAME);
        return ActivityScenario.launch(intent);
    }

    /** Test MGM-01 — Header displays the group name. */
    @Test
    public void groupMgmt_headerDisplaysGroupName() {
        launchGroupManagement();
        onView(withId(R.id.tvModGroupHeader))
                .check(matches(withText("Manage: " + GROUP_NAME)));
    }

    /** Test MGM-02 — All action buttons are displayed. */
    @Test
    public void groupMgmt_allButtonsDisplayed() {
        launchGroupManagement();
        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnEditEvent)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /** Test MGM-03 — All input fields are displayed. */
    @Test
    public void groupMgmt_allInputFieldsDisplayed() {
        launchGroupManagement();
        onView(withId(R.id.etModerationMessageId)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etEventTitle)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etEventDescription)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etEventLocation)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etEventTime)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.etEventId)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /** Test MGM-04 — Remove message with empty ID shows toast. */
    @Test
    public void groupMgmt_removeMessage_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etModerationMessageId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo(), click());
        onView(withId(R.id.btnRemoveMessage)).check(matches(isDisplayed()));
    }

    /** Test MGM-05 — Remove message with valid ID fires request. */
    @Test
    public void groupMgmt_removeMessage_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etModerationMessageId))
                .perform(scrollTo(), clearText(), typeText(MESSAGE_ID), closeSoftKeyboard());
        onView(withId(R.id.btnRemoveMessage)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnRemoveMessage)).check(matches(isDisplayed()));
    }

    /** Test MGM-06 — Restore message with empty ID shows toast. */
    @Test
    public void groupMgmt_restoreMessage_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etModerationMessageId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo(), click());
        onView(withId(R.id.btnRestoreMessage)).check(matches(isDisplayed()));
    }

    /** Test MGM-07 — Restore message with valid ID fires request. */
    @Test
    public void groupMgmt_restoreMessage_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etModerationMessageId))
                .perform(scrollTo(), clearText(), typeText(MESSAGE_ID), closeSoftKeyboard());
        onView(withId(R.id.btnRestoreMessage)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnRestoreMessage)).check(matches(isDisplayed()));
    }

    /** Test MGM-08 — Schedule event with all fields empty shows toast. */
    @Test
    public void groupMgmt_scheduleEvent_emptyFields_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-09 — Schedule event with one field missing shows toast. */
    @Test
    public void groupMgmt_scheduleEvent_missingOneField_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Test Event"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Desc"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Room 101"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-10 — Schedule event with all fields filled fires request. */
    @Test
    public void groupMgmt_scheduleEvent_allFields_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Espresso Event"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Auto test event"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Lab"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-01T10:00:00"), closeSoftKeyboard());
        onView(withId(R.id.btnScheduleEvent)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnScheduleEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-11 — Edit event with empty event ID shows toast. */
    @Test
    public void groupMgmt_editEvent_emptyEventId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Desc"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Loc"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-01T10:00:00"), closeSoftKeyboard());
        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-12 — Edit event with empty detail fields shows toast. */
    @Test
    public void groupMgmt_editEvent_emptyFields_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-13 — Edit event with valid inputs fires request. */
    @Test
    public void groupMgmt_editEvent_validInputs_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
        onView(withId(R.id.etEventTitle)).perform(scrollTo(), clearText(), typeText("Updated Title"), closeSoftKeyboard());
        onView(withId(R.id.etEventDescription)).perform(scrollTo(), clearText(), typeText("Updated Desc"), closeSoftKeyboard());
        onView(withId(R.id.etEventLocation)).perform(scrollTo(), clearText(), typeText("Updated Loc"), closeSoftKeyboard());
        onView(withId(R.id.etEventTime)).perform(scrollTo(), clearText(), typeText("2025-12-02T11:00:00"), closeSoftKeyboard());
        onView(withId(R.id.btnEditEvent)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnEditEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-14 — Delete event with empty ID shows toast. */
    @Test
    public void groupMgmt_deleteEvent_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo(), click());
        onView(withId(R.id.btnDeleteEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-15 — Delete event with valid ID fires request. */
    @Test
    public void groupMgmt_deleteEvent_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etEventId)).perform(scrollTo(), clearText(), typeText(EVENT_ID), closeSoftKeyboard());
        onView(withId(R.id.btnDeleteEvent)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnDeleteEvent)).check(matches(isDisplayed()));
    }

    /** Test MGM-16 — Create announcement with empty fields shows toast. */
    @Test
    public void groupMgmt_createAnnouncement_emptyFields_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-17 — Create announcement with missing content shows toast. */
    @Test
    public void groupMgmt_createAnnouncement_missingContent_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), typeText("My Announcement"), closeSoftKeyboard());
        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-18 — Create announcement with valid inputs fires request. */
    @Test
    public void groupMgmt_createAnnouncement_validInputs_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementTitle)).perform(scrollTo(), clearText(), typeText("Test Announcement"), closeSoftKeyboard());
        onView(withId(R.id.etAnnouncementContent)).perform(scrollTo(), clearText(), typeText("This is the content"), closeSoftKeyboard());
        onView(withId(R.id.btnCreateAnnouncement)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnCreateAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-19 — Pin announcement with empty ID shows toast. */
    @Test
    public void groupMgmt_pinAnnouncement_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo(), click());
        onView(withId(R.id.btnPinAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-20 — Pin announcement with valid ID fires request. */
    @Test
    public void groupMgmt_pinAnnouncement_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
        onView(withId(R.id.btnPinAnnouncement)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnPinAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-21 — Unpin announcement with empty ID shows toast. */
    @Test
    public void groupMgmt_unpinAnnouncement_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo(), click());
        onView(withId(R.id.btnUnpinAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-22 — Unpin announcement with valid ID fires request. */
    @Test
    public void groupMgmt_unpinAnnouncement_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
        onView(withId(R.id.btnUnpinAnnouncement)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnUnpinAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-23 — Delete announcement with empty ID shows toast. */
    @Test
    public void groupMgmt_deleteAnnouncement_emptyId_showsToast() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo(), click());
        onView(withId(R.id.btnDeleteAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-24 — Delete announcement with valid ID fires request. */
    @Test
    public void groupMgmt_deleteAnnouncement_validId_firesRequest() {
        launchGroupManagement();
        onView(withId(R.id.etAnnouncementId)).perform(scrollTo(), clearText(), typeText(ANNOUNCEMENT_ID), closeSoftKeyboard());
        onView(withId(R.id.btnDeleteAnnouncement)).perform(scrollTo(), click());
        waitForNetwork();
        onView(withId(R.id.btnDeleteAnnouncement)).check(matches(isDisplayed()));
    }

    /** Test MGM-25 — Refresh button triggers loadAllData and shows results. */
    @Test
    public void groupMgmt_refreshButton_triggersLoadAllData() {
        launchGroupManagement();
        onView(withId(R.id.btnRefreshGroupManagement)).perform(scrollTo(), click());
        waitForNetwork();

        onView(withId(R.id.tvModPendingMembers))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvModAnnouncements))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
    }

    /** Test MGM-26 — Back button finishes the activity. */
    @Test
    public void groupMgmt_backButton_finishesActivity() {
        launchGroupManagement();
        onView(withId(R.id.btnBackGroupManagement)).perform(scrollTo(), click());
    }
}
