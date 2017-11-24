package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

/**
 * Created by Kurian on 27-Sep-17.
 */

public interface MVPContract {

    interface View {
    }

    interface Presenter<V extends View> {

        /**
         * Bind a view to the presenter
         * @param view
         */
        void bindView(V view);

        /**
         * Unbind the current view from the presenter
         */
        void unbind();

        /**
         * Invoke at the point just before the presenter is destroyed
         */
        void destroy();

        /**
         * Get the current view bound to the presenter (if exists)
         * @return a view of type {@link com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base.MVPContract.View}
         */
        V getView();

        /**
         * Get a string tag that identifies the presenter
         * @return presenter identifier
         */
        String presenterTag();
    }
}
