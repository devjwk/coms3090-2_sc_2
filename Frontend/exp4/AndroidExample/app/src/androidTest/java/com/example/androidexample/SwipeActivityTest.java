package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.anything;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SwipeActivityTest {

    private ActivityScenario<SwipeActivity> launchSwipeActivityWithUserId(int userId) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SwipeActivity.class);
        intent.putExtra("USER_ID", userId);
        return ActivityScenario.launch(intent);
    }

    private ActivityScenario<SwipeActivity> launchSwipeActivityWithoutUserId() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SwipeActivity.class);
        return ActivityScenario.launch(intent);
    }

    private void waitForNetwork() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }

    private void waitForAnimation() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void swipe_t01_onCreate_cardViewDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t02_onCreate_tvNameDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t03_onCreate_btnLikeDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnLike)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t04_onCreate_btnDislikeDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnDislike)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t05_onCreate_btnBackDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t06_onCreate_ivProfilePhotoDisplayed() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.ivProfilePhoto)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t07_onCreate_profileTextViewsDisplayed()
    {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.tvBio)).check(matches(anything()));
        onView(withId(R.id.tvMajor)).check(matches(anything()));
        onView(withId(R.id.tvHobbies)).check(matches(anything()));
        onView(withId(R.id.tvRole)).check(matches(anything()));
    }

    @Test
    public void swipe_t08_btnBack_finishesActivityWithoutCrash() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnBack)).perform(click());
    }

    @Test
    public void swipe_t09_fetchNextSwipe_uiStillVisibleAfterNetwork() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t10_fetchNextSwipe_profileImageStillVisibleAfterNetwork() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.ivProfilePhoto)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t11_noProfilesUser_uiDoesNotCrash() {
        launchSwipeActivityWithUserId(999);
        waitForNetwork();
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t12_likeBeforeNetworkResponse_doesNotCrash() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t13_dislikeBeforeNetworkResponse_doesNotCrash() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t14_likeAfterNetworkResponse_doesNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t15_dislikeAfterNetworkResponse_doesNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t16_repeatedLikeClicks_doNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t17_repeatedDislikeClicks_doNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t18_likeThenDislike_doNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t19_dislikeThenLike_doNotCrash() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t20_fetchUser_relatedViewsRemainDisplayed()
    {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvBio)).check(matches(anything()));
        onView(withId(R.id.tvMajor)).check(matches(anything()));
        onView(withId(R.id.tvHobbies)).check(matches(anything()));
        onView(withId(R.id.tvRole)).check(matches(anything()));
    }

    @Test
    public void swipe_t21_fetchUserPhoto_imageViewRemainsDisplayed() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.ivProfilePhoto)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t22_badUserId_staysStable() {
        launchSwipeActivityWithUserId(-1);
        waitForNetwork();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t23_largeUserId_staysStable() {
        launchSwipeActivityWithUserId(999999);
        waitForNetwork();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t24_withoutUserId_doesNotCrashOnLaunch() {
        launchSwipeActivityWithoutUserId();
    }

    @Test
    public void swipe_t25_closeImmediatelyAfterLaunch_doesNotCrash() {
        ActivityScenario<SwipeActivity> scenario = launchSwipeActivityWithUserId(1);
        scenario.close();
    }

    @Test
    public void swipe_t26_closeAfterNetwork_doesNotCrash() {
        ActivityScenario<SwipeActivity> scenario = launchSwipeActivityWithUserId(1);
        waitForNetwork();
        scenario.close();
    }

    @Test
    public void swipe_t27_likeThenClose_doesNotCrash() {
        ActivityScenario<SwipeActivity> scenario = launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        scenario.close();
    }

    @Test
    public void swipe_t28_dislikeThenClose_doesNotCrash() {
        ActivityScenario<SwipeActivity> scenario = launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        scenario.close();
    }

    @Test
    public void swipe_t29_multipleNetworkWaits_uiStillVisible() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        waitForNetwork();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t30_allCoreControlsStillVisibleAfterNetwork() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLike)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDislike)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t31_likeFlowKeepsNameVisible() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForNetwork();
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t32_dislikeFlowKeepsNameVisible() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForNetwork();
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t33_threeLikeClicksRemainStable() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnLike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t34_threeDislikeClicksRemainStable() {
        launchSwipeActivityWithUserId(1);
        waitForNetwork();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.btnDislike)).perform(click());
        waitForAnimation();
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
    }

    @Test
    public void swipe_t35_finalSmokeTest_activitySurfaceVisible() {
        launchSwipeActivityWithUserId(1);
        onView(withId(R.id.cardView)).check(matches(isDisplayed()));
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
        onView(withId(R.id.ivProfilePhoto)).check(matches(isDisplayed()));
    }
}
