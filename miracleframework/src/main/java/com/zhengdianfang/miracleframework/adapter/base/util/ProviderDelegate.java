package com.zhengdianfang.miracleframework.adapter.base.util;

import android.util.SparseArray;

import com.zhengdianfang.miracleframework.adapter.base.provider.BaseItemProvider;


public class ProviderDelegate {
    private SparseArray<BaseItemProvider> itemProviders = new SparseArray();

    public void registerProvider(BaseItemProvider provider){
        if (provider == null){
            throw new ItemProviderException("ItemProvider can not be null");
        }

        int viewType = provider.viewType();

        if (itemProviders.get(viewType) == null){
            itemProviders.put(viewType,provider);
        }
    }

    public SparseArray<BaseItemProvider> getItemProviders(){
        return itemProviders;
    }

}
