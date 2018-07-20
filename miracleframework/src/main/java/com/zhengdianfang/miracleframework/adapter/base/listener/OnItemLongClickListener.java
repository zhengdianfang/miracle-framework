package com.zhengdianfang.miracleframework.adapter.base.listener;

import android.view.View;

import com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter;

public abstract class OnItemLongClickListener extends SimpleClickListener {
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
        return onSimpleItemLongClick(adapter, view, position);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

    }

    @Override
    public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
        return false;
    }

    public abstract boolean onSimpleItemLongClick(BaseQuickAdapter adapter, View view, int position);
}
