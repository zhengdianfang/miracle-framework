package com.zhengdianfang.miracleframework.adapter.base.listener;

import android.view.View;

import com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter;

public abstract class OnItemChildLongClickListener extends SimpleClickListener {
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {

        return false;
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
        return onSimpleItemChildLongClick(adapter, view, position);
    }

    public abstract boolean onSimpleItemChildLongClick(BaseQuickAdapter adapter, View view, int position);
}
