package com.koalatea.thehollidayinn.softwareengineeringdaily

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun changeText_sameActivity() {
        // Type text and then press the button.
        onView(withId(R.id.search))
                .perform(click())
//        onView(withId(R.id.changeTextBt)).perform(click())
//
//        // Check that the text was changed.
//        onView(withId(R.id.textToBeChanged))
//                .check(matches(withText(mStringToBetyped)))
    }
}