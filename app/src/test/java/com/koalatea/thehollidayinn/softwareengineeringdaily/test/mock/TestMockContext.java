package com.koalatea.thehollidayinn.softwareengineeringdaily.test.mock;

import android.content.Context;
import android.test.mock.MockContext;

/**
 * Created by Kurian on 25-Sep-17.
 */

public class TestMockContext extends MockContext {

    @Override
    public Context getApplicationContext() {
        return this;
    }
}
