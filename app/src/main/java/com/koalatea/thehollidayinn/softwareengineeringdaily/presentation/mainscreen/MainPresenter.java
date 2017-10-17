package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.mainscreen;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract;

/**
 * Created by Kurian on 27-Sep-17.
 */

public interface MainPresenter extends MVPContract.Presenter<MainView> {
    void searchPodcast(String searchString);
}
