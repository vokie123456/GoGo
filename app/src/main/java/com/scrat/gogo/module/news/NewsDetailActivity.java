package com.scrat.gogo.module.news;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scrat.gogo.R;
import com.scrat.gogo.data.api.Res;
import com.scrat.gogo.data.model.Comment;
import com.scrat.gogo.data.model.News;
import com.scrat.gogo.data.model.NewsDetail;
import com.scrat.gogo.databinding.ActivityNewsDetailBinding;
import com.scrat.gogo.databinding.HeaderNewsDetailBinding;
import com.scrat.gogo.framework.common.BaseActivity;
import com.scrat.gogo.framework.common.BaseRecyclerViewAdapter;
import com.scrat.gogo.framework.common.BaseRecyclerViewHolder;
import com.scrat.gogo.framework.common.BaseRecyclerViewOnScrollListener;
import com.scrat.gogo.framework.common.GlideApp;
import com.scrat.gogo.framework.common.GlideRequest;
import com.scrat.gogo.framework.common.GlideRequests;
import com.scrat.gogo.framework.util.L;
import com.scrat.gogo.framework.util.Utils;

import java.util.List;

/**
 * Created by scrat on 2017/11/12.
 */

public class NewsDetailActivity extends BaseActivity implements NewsDetailContract.View {

    private static final String NEWS = "news";
    private ActivityNewsDetailBinding binding;
    private Adapter adapter;
    private NewsDetailContract.Presenter presenter;
    private HeaderNewsDetailBinding headerBinding;
    private GlideRequests glideRequests;
    private BaseRecyclerViewOnScrollListener loadMoreListener;

    public static void show(Activity activity, int requestCode, News news) {
        Intent i = new Intent(activity, NewsDetailActivity.class);
        i.putExtra(NEWS, news);
        activity.startActivityForResult(i, requestCode);
    }

    @NonNull
    @Override
    protected String getActivityName() {
        return "NewsDetailActivity";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_news_detail);

        glideRequests = GlideApp.with(this);

        binding.list.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.list.setLayoutManager(layoutManager);
        adapter = new Adapter(glideRequests);
        headerBinding = HeaderNewsDetailBinding.inflate(getLayoutInflater(), binding.list, false);
        adapter.setHeader(headerBinding.getRoot());
        binding.list.setAdapter(adapter);

        loadMoreListener = new BaseRecyclerViewOnScrollListener(
                layoutManager, new BaseRecyclerViewOnScrollListener.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                presenter.loadComment(false);
            }
        });
        binding.list.addOnScrollListener(loadMoreListener);

        News news = (News) getIntent().getSerializableExtra(NEWS);
        showNews(news);
        new NewsDetailPresenter(this, news);
        presenter.loadNewsDetail();
        presenter.loadComment(true);

        binding.comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    binding.sendBtn.setVisibility(View.VISIBLE);
                } else {
                    binding.sendBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void setPresenter(NewsDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoadingList() {
        showLoadingComment();
    }

    @Override
    public void showListData(List<Res.CommentItem> list, boolean replace) {
        hideLoadingComment();
        adapter.setData(list, replace);
    }

    @Override
    public void showNoMoreListData() {
        hideLoadingComment();
    }

    @Override
    public void showEmptyList() {
        hideLoadingComment();
    }

    @Override
    public void showLoadingListError(String e) {
        hideLoadingComment();
        showMessage(e);
    }

    @Override
    public void showSendingComment() {

    }

    @Override
    public void showSendCommentError(String e) {
        showMessage(e);
    }

    @Override
    public void showSendCommentSuccess(Comment comment) {
        toast("发送成功");
        binding.comment.setText("");
        binding.sendBtn.setVisibility(View.GONE);
        presenter.loadNewsDetail();
        presenter.loadComment(true);
        binding.comment.clearFocus();
    }

    @Override
    public void showLoadingNewsDetail() {

    }

    @Override
    public void showLoadNewsDetailError(String msg) {
        showMessage(msg);
    }

    private void showLoadingComment() {

    }

    private void hideLoadingComment() {
        loadMoreListener.setLoading(false);
    }

    @Override
    public void showNewsDetail(NewsDetail detail) {
        if (detail.isWebViewNews()) {
            toast("web");
        } else if (detail.isVideoNews()) {
            headerBinding.body.setVisibility(View.GONE);
            glideRequests.load(detail.getCover()).into(headerBinding.cover);
            headerBinding.coverItem.setVisibility(View.VISIBLE);
            final Uri uri = Uri.parse(detail.getVideo());
            headerBinding.coverItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //调用系统自带的播放器
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "video/mp4");
                        startActivity(intent);
                    } catch (Exception e) {
                        L.e(e);
                    }
                }
            });
        } else {
            headerBinding.body.fromHtml(detail.getBody());
        }

        showNews(detail);
    }

    private void showNews(News news) {
        binding.topBar.subject.setText(news.getTitle());
        if (news.getTotalComment() > 0) {
            headerBinding.groupTitle.setVisibility(View.VISIBLE);
            headerBinding.groupTitle.setText(String.format("评论 %s", news.getTotalComment()));
        } else {
            headerBinding.groupTitle.setVisibility(View.GONE);
        }
    }

    public void sendCommend(View view) {
        hideSoftInput();
        presenter.sendComment(binding.comment.getText().toString());
    }

    private static class Adapter extends BaseRecyclerViewAdapter<Res.CommentItem> {

        private final GlideRequest<Drawable> request;

        private Adapter(GlideRequests glideRequests) {
            request = glideRequests.asDrawable();
        }

        @Override
        protected void onBindItemViewHolder(
                BaseRecyclerViewHolder holder, int position, Res.CommentItem item) {

            holder.setText(R.id.nickname, item.getUser().getUsername())
                    .setText(R.id.date, Utils.formatDate(item.getComment().getCreateTs()))
                    .setText(R.id.comment, item.getComment().getContent());

            request.load(item.getUser().getAvatar())
                    .into(holder.getImageView(R.id.img));
        }

        @Override
        protected BaseRecyclerViewHolder onCreateRecycleItemView(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_comment, parent, false);
            return new BaseRecyclerViewHolder(v);
        }
    }
}
