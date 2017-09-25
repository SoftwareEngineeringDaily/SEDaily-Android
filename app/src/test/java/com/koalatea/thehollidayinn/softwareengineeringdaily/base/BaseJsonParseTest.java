package com.koalatea.thehollidayinn.softwareengineeringdaily.base;

import java.io.File;

/**
 * Created by Kurian on 25-Sep-17.
 */

public abstract class BaseJsonParseTest extends BaseUnitTest {

    protected File loadJsonFile(String filePath) {
        return new File(getClass().getClassLoader().getResource("api").getFile(), filePath);
    }
}
