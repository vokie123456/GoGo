package com.scrat.gogo.module.home;

import com.scrat.gogo.data.model.News;
import com.scrat.gogo.framework.common.BaseContract;

import java.util.List;

/**
 * Created by scrat on 2017/11/12.
 */

public interface HomeContract {
    interface HomePresenter {
        void loadBanner();
        void loadData(boolean refresh);
    }

    interface HomeView extends BaseContract.BaseListView<HomePresenter, News> {
        void showBanner(List<News> list);
    }
}
