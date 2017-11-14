package com.scrat.gogo.module.me;

import android.support.annotation.NonNull;

import com.scrat.gogo.data.DataRepository;
import com.scrat.gogo.data.api.Res;
import com.scrat.gogo.data.callback.DefaultLoadObjCallback;
import com.scrat.gogo.data.local.Preferences;
import com.scrat.gogo.data.model.UserInfo;

/**
 * Created by scrat on 2017/11/14.
 */

public class MePresenter implements MeContract.Presenter {
    private MeContract.View view;

    public MePresenter(MeContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void logout() {
        String refreshToken = Preferences.getInstance().getRefreshToken();
        DataRepository.getInstance().getApi().logout(refreshToken);
        Preferences.getInstance().clearAuth();
        view.showNotLogin();
    }

    @Override
    public void loadUserInfo() {
        if (!Preferences.getInstance().isLogin()) {
            view.showNotLogin();
            return;
        }

        DataRepository.getInstance().getApi().getUserInfo
                (new DefaultLoadObjCallback<UserInfo, Res.UserInfoRes>() {
                    @Override
                    protected void onSuccess(UserInfo userInfo) {
                        view.showUserInfo(userInfo);
                    }

                    @Override
                    public void onError(Exception e) {
                        // ignore
                    }

                    @NonNull
                    @Override
                    protected Class<Res.UserInfoRes> getResClass() {
                        return Res.UserInfoRes.class;
                    }
                });
    }
}
