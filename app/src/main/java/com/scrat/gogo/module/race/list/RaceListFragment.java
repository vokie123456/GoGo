package com.scrat.gogo.module.race.list;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.scrat.gogo.R;
import com.scrat.gogo.data.model.Race;
import com.scrat.gogo.data.model.RaceGroupItem;
import com.scrat.gogo.databinding.FragmentRaceListBinding;
import com.scrat.gogo.databinding.ListItemRaceBinding;
import com.scrat.gogo.framework.common.BaseFragment;
import com.scrat.gogo.framework.common.BaseRecyclerViewAdapter;
import com.scrat.gogo.framework.common.BaseRecyclerViewHolder;
import com.scrat.gogo.framework.common.BaseRecyclerViewOnScrollListener;
import com.scrat.gogo.framework.common.GlideApp;
import com.scrat.gogo.framework.common.GlideRequest;
import com.scrat.gogo.framework.common.GlideRequests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by scrat on 2017/11/14.
 */

public class RaceListFragment extends BaseFragment implements RaceListContract.View {
    private FragmentRaceListBinding binding;
    private RaceListContract.Presenter presenter;
    private BaseRecyclerViewOnScrollListener loadMoreListener;
    private Adapter adapter;
    private GlideRequests glideRequests;

    public static RaceListFragment newInstance() {
        Bundle args = new Bundle();
        RaceListFragment fragment = new RaceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    protected String getFragmentName() {
        return "RaceListFragment";
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentRaceListBinding.inflate(inflater, container, false);

        new RaceListPresenter(this);
        presenter.loadData(true);
        binding.srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadData(true);
            }
        });
        binding.list.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.list.setLayoutManager(layoutManager);
        loadMoreListener = new BaseRecyclerViewOnScrollListener(
                layoutManager, new BaseRecyclerViewOnScrollListener.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                presenter.loadData(false);
            }
        });
        binding.list.addOnScrollListener(loadMoreListener);
        glideRequests = GlideApp.with(this);
        adapter = new Adapter(glideRequests);
        binding.list.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void setPresenter(RaceListContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoadingList() {
        showLoading();
    }

    @Override
    public void showListData(List<RaceGroupItem> list, boolean replace) {
        hideLoading();
        adapter.setData(list, replace);
    }

    @Override
    public void showNoMoreListData() {
        hideLoading();
    }

    @Override
    public void showEmptyList() {
        hideLoading();
    }

    @Override
    public void showLoadingListError(String e) {
        hideLoading();
        showMsg(e);
    }

    private void showLoading() {
        binding.srl.setRefreshing(true);
    }

    private void hideLoading() {
        binding.srl.setRefreshing(false);
        loadMoreListener.setLoading(false);
    }

    private static class Adapter extends BaseRecyclerViewAdapter<RaceGroupItem> {
        private GlideRequest<Drawable> request;
        private SimpleDateFormat sdf;

        private Adapter(GlideRequests requests) {
            request = requests.asDrawable().centerCrop();
            sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        }

        private String formatGroupTitle(String dt) {
            try {
                Date date = sdf.parse(dt);
                return DateFormat.format("M月d日", date).toString();
            } catch (ParseException e) {
                return dt;
            }
        }

        @Override
        protected void onBindItemViewHolder(
                BaseRecyclerViewHolder holder, int position, RaceGroupItem raceGroupItem) {
            LinearLayout layout = holder.getView(R.id.group_list);
            holder.setText(R.id.group_name, formatGroupTitle(raceGroupItem.getDt()));
            layout.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(layout.getContext());
            for (Race race : raceGroupItem.getItems()) {
                ListItemRaceBinding binding = ListItemRaceBinding.inflate(inflater, layout, false);
                request.load(race.getTeamA().getLogo()).into(binding.logoA);
                binding.date.setText(DateFormat.format("H:mm", race.getRaceTs()));
                request.load(race.getTeamB().getLogo()).into(binding.logoB);
                layout.addView(binding.getRoot());
            }

        }

        @Override
        protected BaseRecyclerViewHolder onCreateRecycleItemView(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group_item_race, parent, false);
            return new BaseRecyclerViewHolder(v);
        }
    }
}