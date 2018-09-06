package com.koalatea.thehollidayinn.softwareengineeringdaily

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            internal var currentIndex = 0

            override fun describeTo(description: Description) {
                description.appendText("with index: ")
                description.appendValue(index)
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                return matcher.matches(view) && currentIndex++ == index
            }
        }
    }

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun viewsEpisodeDetail() {
        // Type text and then press the button.
        onView(withId(R.id.search))
                .perform(click())

        onView(withIndex(withId(R.id.my_recycler_view), 0))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Check that the text was changed.
        onView(withId(R.id.scoreTextView))
                .check(matches(isDisplayed()))

        Thread.sleep(2500)

//        checkBookmarking()
    }

    fun checkBookmarking() {
        onView(withId(R.id.menu_item_bookmark))
                .check(matches(withContentDescription("NotBookmarked")))
        onView(withId(R.id.menu_item_bookmark))
                .perform(click())
        onView(withId(R.id.menu_item_bookmark))
                .check(matches(withContentDescription("Bookmarked")))
    }
}

