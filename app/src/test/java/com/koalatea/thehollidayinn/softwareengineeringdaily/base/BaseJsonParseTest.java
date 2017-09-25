package com.koalatea.thehollidayinn.softwareengineeringdaily.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.ResponseTypeAdaptorFactory;

import org.junit.Before;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Kurian on 25-Sep-17.
 */

public abstract class BaseJsonParseTest extends BaseUnitTest {

    protected Gson gson;

    @Before
    public void setUp() {
        //Possibly receive this via DI rather than manual instance to keep consistency
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapterFactory(ResponseTypeAdaptorFactory.create())
                .create();
    }

    private File loadJsonFile(String filePath) {
        return new File(getClass().getClassLoader().getResource("api").getFile(), filePath);
    }

    protected JsonReader loadJson(String filename) throws FileNotFoundException {
        return new JsonReader(new FileReader(loadJsonFile(filename)));
    }

}
