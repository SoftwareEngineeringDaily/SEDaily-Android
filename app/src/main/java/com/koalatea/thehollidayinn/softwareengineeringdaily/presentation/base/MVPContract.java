package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

/**
 * Created by Kurian on 27-Sep-17.
 */

public interface MVPContract {

    interface View {
    }

    interface Presenter<V extends View> {
        void bindView(V view);
        void unbind();
    }
}
