package com.zhengdianfang.miracleframework.adapter.base;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import com.zhengdianfang.miracleframework.adapter.base.entity.IExpandable;
import com.zhengdianfang.miracleframework.adapter.base.entity.MultiItemEntity;

import java.util.List;

public abstract class BaseMultiItemQuickAdapter<T extends MultiItemEntity, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    private SparseIntArray layouts;

    private static final int DEFAULT_VIEW_TYPE = -0xff;
    public static final int TYPE_NOT_FOUND = -404;

    public BaseMultiItemQuickAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected int getDefItemViewType(int position) {
        T item = data.get(position);
        if (item != null) {
            return item.getItemType();
        }
        return DEFAULT_VIEW_TYPE;
    }

    protected void setDefaultViewTypeLayout(@LayoutRes int layoutResId) {
        addItemType(DEFAULT_VIEW_TYPE, layoutResId);
    }

    @Override
    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, getLayoutId(viewType));
    }

    private int getLayoutId(int viewType) {
        return layouts.get(viewType, TYPE_NOT_FOUND);
    }

    protected void addItemType(int type, @LayoutRes int layoutResId) {
        if (layouts == null) {
            layouts = new SparseIntArray();
        }
        layouts.put(type, layoutResId);
    }


    @Override
    public void remove(@IntRange(from = 0L) int position) {
        if (data == null
                || position < 0
                || position >= data.size()) return;

        T entity = data.get(position);
        if (entity instanceof IExpandable) {
            removeAllChild((IExpandable) entity, position);
        }
        removeDataFromParent(entity);
        super.remove(position);
    }

    protected void removeAllChild(IExpandable parent, int parentPosition) {
        if (parent.isExpanded()) {
            List<MultiItemEntity> chidChilds = parent.getSubItems();
            if (chidChilds == null || chidChilds.size() == 0) return;

            int childSize = chidChilds.size();
            for (int i = 0; i < childSize; i++) {
                remove(parentPosition + 1);
            }
        }
    }

    protected void removeDataFromParent(T child) {
        int position = getParentPosition(child);
        if (position >= 0) {
            IExpandable parent = (IExpandable) data.get(position);
            parent.getSubItems().remove(child);
        }
    }
}


