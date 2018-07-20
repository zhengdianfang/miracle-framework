package com.zhengdianfang.miracleframework.adapter.base.provider;

import android.content.Context;

import com.zhengdianfang.miracleframework.adapter.base.BaseViewHolder;

import java.util.List;

public abstract class BaseItemProvider<T, V extends BaseViewHolder> {
   public Context context;
   public List<T> data;

   public abstract int viewType();


   public abstract int layout();

   public abstract void convert(V helper, T item, int position);

   public void onClick(V helper, T item, int position){}


   public boolean onLongClick(V helper, T item, int position){ return  false; }
}
