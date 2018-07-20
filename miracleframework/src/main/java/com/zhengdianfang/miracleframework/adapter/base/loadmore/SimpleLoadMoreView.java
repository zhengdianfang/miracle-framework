package com.zhengdianfang.miracleframework.adapter.base.loadmore;

import com.zhengdianfang.miracleframework.R;

public class SimpleLoadMoreView extends LoadMoreView {

    @Override
    public int getLayoutId() {
        return R.layout.simple_loadmore_layout;
    }

    @Override
    public int getLoadingViewId() {
        return R.id.loadingStatusGroup;
    }

    @Override
    public int getLoadFailViewId() {
        return R.id.loadingFailStatus;
    }

    @Override
    public int getLoadEndViewId() {
        return R.id.loadingEndStatus;
    }
}
