package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.mainscreen;

import com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.BasePresenter;

/**
 * Created by Kurian on 27-Sep-17.
 */
class MainPresenterImpl extends BasePresenter<MainView> implements MainPresenter {

    @Override
    public void searchPodcast(String searchString) {

    }

    @Override
    public String presenterTag() {
        return MainPresenterImpl.class.getCanonicalName();
    }
}
