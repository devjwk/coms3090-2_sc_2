//package com.example.androidexample;
//
//import android.content.Intent;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.platform.app.InstrumentationRegistry;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static org.junit.Assert.assertNotNull;
//
//@RunWith(AndroidJUnit4.class)
//public class MultipleActivitiesSmokeTest {
//
//    @Test
//    public void launchCommonActivities_noCrash() {
//        launchAndAssert(MainActivity.class);
//    }
//
//    private void launchAndAssert(Class<?> activityClass) {
//        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), activityClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try (ActivityScenario<?> scenario = ActivityScenario.launch(intent)) {
//            scenario.onActivity(activity -> assertNotNull(activity));
//        }
//    }
//}
//
