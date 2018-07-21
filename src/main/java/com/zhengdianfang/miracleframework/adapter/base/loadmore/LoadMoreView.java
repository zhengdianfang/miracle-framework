package com.zhengdianfang.miracleframework.adapter.base.loadmore;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import com.zhengdianfang.miracleframework.adapter.base.BaseViewHolder;

public abstract class LoadMoreView {
    public static final int STATUS_DEFAULT = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_END = 4;

    private int loadMoreStatus = STATUS_DEFAULT;
    private boolean loadMoreEndGone = false;

    public void setLoadMoreStatus(int loadMoreStatus) {
        this.loadMoreStatus = loadMoreStatus;
    }

    public int getLoadMoreStatus() {
        return loadMoreStatus;
    }

    public void convert(BaseViewHolder holder) {
        switch (loadMoreStatus) {
            case STATUS_LOADING:
                visibleLoading(holder, true);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_FAIL:
                visibleLoading(holder, false);
                visibleLoadFail(holder, true);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_END:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, true);
                break;
            case STATUS_DEFAULT:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
        }
    }

    public void setLoadMoreEndGone(boolean loadMoreEndGone) {
        this.loadMoreEndGone = loadMoreEndGone;
    }

    public final boolean isLoadEndMoreGone() {
        if (getLoadEndViewId() == 0) {
            return true;
        }
        return loadMoreEndGone;
    }

    private void visibleLoading(BaseViewHolder holder, boolean visible) {
        if (getLoadingViewId() != 0) {
            holder.setGone(getLoadingViewId(), visible);
        }
    }

    private void visibleLoadFail(BaseViewHolder holder, boolean visible) {
        if (getLoadFailViewId() != 0) {
            holder.setGone(getLoadFailViewId(), visible);
        }
    }


    private void visibleLoadEnd(BaseViewHolder holder, boolean visible) {
        final int loadEndViewId = getLoadEndViewId();
        if (loadEndViewId != 0) {
            holder.setGone(loadEndViewId, visible);
        }
    }

    public abstract @LayoutRes int getLayoutId();
    public abstract @IdRes int getLoadingViewId();
    public abstract @IdRes int getLoadFailViewId();
    public abstract @IdRes int getLoadEndViewId();
}
