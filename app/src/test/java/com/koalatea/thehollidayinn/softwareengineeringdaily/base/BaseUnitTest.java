package com.koalatea.thehollidayinn.softwareengineeringdaily.base;

import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Base test class to initiate any globally required components
 * Created by Kurian on 25-Sep-17.
 */
public abstract class BaseUnitTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
}
