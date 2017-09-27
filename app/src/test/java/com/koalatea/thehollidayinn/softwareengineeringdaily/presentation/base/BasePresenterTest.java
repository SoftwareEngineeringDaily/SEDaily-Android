package com.koalatea.thehollidayinn.softwareengineeringdaily.presentation.base;

import com.koalatea.thehollidayinn.softwareengineeringdaily.test.BaseUnitTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by Kurian on 27-Sep-17.
 */
public class BasePresenterTest extends BaseUnitTest {

    private StubPresenter presenter;

    @Override
    public void setUp() {
        super.setUp();
        presenter = new StubPresenter();
    }

    @Test
    public void view_is_not_bound_on_start() throws Exception {
        assertFalse(presenter.isViewBound());
    }

    @Test
    public void view_is_bound() throws Exception {
        presenter.bindView(mock(StubView.class));
        assertTrue(presenter.isViewBound());
    }

    @Test
    public void unbounded_view_returns_false() throws Exception {
        presenter.bindView(mock(StubView.class));
        assertTrue(presenter.isViewBound());
        presenter.unbind();
        assertFalse(presenter.isViewBound());
    }

    @Test
    public void getView_returns_expected_instance_of_view() throws Exception {
        presenter.bindView(mock(StubView.class));
        assertTrue(presenter.getView() instanceof StubView);
    }

    @Test
    public void presenter_tag_is_expected() throws Exception {
        assertEquals("presenter tag", presenter.presenterTag());
    }

    private static class StubPresenter extends BasePresenter<StubView> {
        @Override
        public String presenterTag() {
            return "presenter tag";
        }
    }

    private interface StubView extends MVPContract.View {
    }
}