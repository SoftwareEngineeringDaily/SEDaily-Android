package com.koalatea.thehollidayinn.softwareengineeringdaily

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Kurian on 05-Nov-17.
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedKTest {

  @Test
  @Throws(Exception::class)
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getTargetContext()
    assertEquals("com.koalatea.thehollidayinn.softwareengineeringdaily.debug",
        appContext.packageName)
  }
}