package com.zhengdianfang.miracleframework;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

import com.zhengdianfang.miracleframework.fragment.SupportFragment;

public abstract class BaseFragment extends SupportFragment {

    private View navigationBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initNavigationBar();
    }

    private void initNavigationBar() {
        navigationBar = getView().findViewById(R.id.navigationBar);
        if (null != navigationBar) {
            navigationBar.findViewById(R.id.navigationBarPopButton)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!BaseFragment.this.popAction()) {
                                pop();
                            }
                        }
                    });
        }
    }

    boolean popAction() {
       return false;
    }
}
