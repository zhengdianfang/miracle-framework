package com.zhengdianfang.miracleframework.adapter.base.listener;

import android.view.View;

import com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter;

public abstract class OnItemClickListener extends SimpleClickListener {
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        onSimpleItemClick(adapter, view, position);
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

        return false;
    }

    public abstract void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position);
}
