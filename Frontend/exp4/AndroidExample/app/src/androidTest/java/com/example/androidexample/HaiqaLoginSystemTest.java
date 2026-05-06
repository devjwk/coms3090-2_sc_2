//package com.example.androidexample;
//
//import static androidx.test.espresso.Espresso.*;
//import static androidx.test.espresso.action.ViewActions.*;
//import static androidx.test.espresso.assertion.ViewAssertions.*;
//import static androidx.test.espresso.matcher.ViewMatchers.*;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class HaiqaLoginSystemTest {
//
//    @Rule
//    public ActivityScenarioRule<LoginActivity> rule =
//            new ActivityScenarioRule<>(LoginActivity.class);
//
//    @Test public void t01(){
//        onView(withId(R.id.login_login_btn)).perform(click());
//        onView(withId(R.id.login_login_btn))
//                .check(matches(isDisplayed()));
//    }
//
//    @Test public void t02(){
//        onView(withId(R.id.login_username_edt))
//                .perform(typeText("a"), closeSoftKeyboard());
//        onView(withId(R.id.login_login_btn)).perform(click());
//        onView(withId(R.id.login_login_btn))
//                .check(matches(isDisplayed()));
//    }
//
//    @Test public void t03(){
//        onView(withId(R.id.login_password_edt))
//                .perform(typeText("a"), closeSoftKeyboard());
//        onView(withId(R.id.login_login_btn)).perform(click());
//        onView(withId(R.id.login_login_btn))
//                .check(matches(isDisplayed()));
//    }
//
//    @Test public void t04(){
//        onView(withId(R.id.login_signup_btn)).perform(click());
//    }
//
//    @Test public void t05(){
//        onView(withId(R.id.login_moderator_btn)).perform(click());
//    }
//
//    @Test public void t06(){
//        onView(withId(R.id.login_moderator_login_btn)).perform(click());
//    }
//
//    @Test public void t07(){
//        onView(withId(R.id.login_delete_user_btn)).perform(click());
//    }
//
//    @Test public void t08(){
//        onView(withId(R.id.login_username_edt))
//                .perform(typeText("   "), closeSoftKeyboard());
//        onView(withId(R.id.login_login_btn)).perform(click());
//    }
//
//    @Test public void t09(){
//        onView(withId(R.id.login_password_edt))
//                .perform(typeText("   "), closeSoftKeyboard());
//        onView(withId(R.id.login_login_btn)).perform(click());
//    }
//
//    @Test public void t10(){
//        onView(withId(R.id.login_username_edt))
//                .perform(typeText("test@test.com"),
//                        closeSoftKeyboard());
//
//        onView(withId(R.id.login_password_edt))
//                .perform(typeText("bad"),
//                        closeSoftKeyboard());
//
//        onView(withId(R.id.login_login_btn))
//                .perform(click());
//    }
//
//    @Test public void t11(){ onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));}
//    @Test public void t12(){ onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));}
//    @Test public void t13(){ onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));}
//    @Test public void t14(){ onView(withId(R.id.login_signup_btn)).check(matches(isDisplayed()));}
//    @Test public void t15(){ onView(withId(R.id.login_delete_user_btn)).check(matches(isDisplayed()));}
//
//    @Test public void t16(){ onView(withId(R.id.login_username_edt)).perform(clearText());}
//    @Test public void t17(){ onView(withId(R.id.login_password_edt)).perform(clearText());}
//
//    @Test public void t18(){ onView(withId(R.id.login_login_btn)).perform(click(), click());}
//    @Test public void t19(){ onView(withId(R.id.login_signup_btn)).perform(click());}
//    @Test public void t20(){ onView(withId(R.id.login_moderator_btn)).perform(click());}
//}