package com.zhengdianfang.miracleframework.adapter.base.util;

import android.support.annotation.LayoutRes;
import android.util.SparseIntArray;

import java.util.List;

public abstract class MultiTypeDelegate<T> {

    public static final int DEFAULT_VIEW_TYPE = 0x000331;
    public static final int TYPE_NOT_FOUND = -0x000022;
    private SparseIntArray layouts;
    private boolean autoMode;
    private boolean selfMode;

    public MultiTypeDelegate() {
    }

    public MultiTypeDelegate(SparseIntArray layouts) {
        this.layouts = layouts;
    }

    public final int getDefItemViewType(List<T> datas, int position) {
        T item = datas.get(position);
        return item != null ? getItemType(item) : DEFAULT_VIEW_TYPE;
    }

    public final int getLayoutId(int viewType) {
        return this.layouts.get(viewType, TYPE_NOT_FOUND);
    }

    private void addItemType(int type, @LayoutRes int layoutResId) {
        if (this.layouts == null) {
            this.layouts = new SparseIntArray();
        }
        this.layouts.put(type, layoutResId);
    }

    public MultiTypeDelegate registerItemTypeAutoIncrease(@LayoutRes int... layoutResIds) {
        if (selfMode) {
            throw new RuntimeException("Don't register two mode ");
        }
        autoMode = true;
        for (int i = 0; i < layoutResIds.length; i++) {
            addItemType(i, layoutResIds[i]);
        }
        return this;
    }

    public MultiTypeDelegate registerItemType(int type, @LayoutRes int layoutResId) {
        if (autoMode) {
            throw new RuntimeException("Don't register two mode ");
        }
        selfMode = true;
        addItemType(type, layoutResId);
        return this;
    }



    protected abstract int getItemType(T item);

}
