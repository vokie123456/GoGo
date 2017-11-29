package com.scrat.gogo.module.news.detail;

import com.scrat.gogo.data.api.Res;
import com.scrat.gogo.data.model.Comment;
import com.scrat.gogo.data.model.NewsDetail;
import com.scrat.gogo.framework.common.BaseContract;

/**
 * Created by scrat on 2017/11/12.
 */

public interface NewsDetailContract {
    interface Presenter {
        void loadNewsDetail();

        void loadComment(boolean refresh);

        void sendComment(String comment);
    }

    interface View extends BaseContract.BaseListView<Presenter, Res.CommentItem> {
        void showLoadingNewsDetail();

        void showLoadNewsDetailError(String msg);

        void showNewsDetail(NewsDetail detail);

        void showSendingComment();

        void showSendCommentError(String e);

        void showSendCommentSuccess(Comment comment);
    }
}