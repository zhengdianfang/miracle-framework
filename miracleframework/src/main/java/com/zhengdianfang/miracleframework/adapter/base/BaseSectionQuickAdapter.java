package com.zhengdianfang.miracleframework.adapter.base;

import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.zhengdianfang.miracleframework.adapter.base.entity.SectionEntity;

import java.util.List;

public abstract class BaseSectionQuickAdapter<T extends SectionEntity, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    protected int sectionHeadResId;
    protected static final int SECTION_HEAD_VIEW = 0x0000044;

    public BaseSectionQuickAdapter(int layoutResId, @Nullable List<T> datas) {
        super(layoutResId, datas);
        this.sectionHeadResId = layoutResId;
    }

    @Override
    protected int getDefItemViewType(int position) {
        return data.get(position).isHeader ? SECTION_HEAD_VIEW : 0;
    }

    @Override
    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SECTION_HEAD_VIEW) {
            return createBaseViewHolder(getItemView(sectionHeadResId, parent));
        }
        return super.onCreateDefViewHolder(parent, viewType);
    }

    @Override
    protected boolean isFixedViewType(int type) {
        return super.isFixedViewType(type) || type == SECTION_HEAD_VIEW;
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        switch (holder.getItemViewType()){
            case SECTION_HEAD_VIEW:
                setFullSpan(holder);
                convertHead(holder, getItem(position - getHeaderLayoutCount()));
                break;
            default:
                super.onBindViewHolder(holder, position);
                break;
        }
    }

    protected abstract void convertHead(K helper, T item);

}
