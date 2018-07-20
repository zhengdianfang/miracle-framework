package com.zhengdianfang.miracleframework.adapter.base.entity;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AbstractExpandableItem<T> implements IExpandable<T> {
    protected boolean expandable = false;
    protected List<T> subItems;

    @Override
    public boolean isExpanded() {
        return expandable;
    }

    @Override
    public void setExpanded(boolean expanded) {
        expandable = expanded;
    }

    @Override
    public List<T> getSubItems() {
        return subItems;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    public boolean hasSubItem() {
        return subItems != null && subItems.size() > 0;
    }

    public void setSubItems(List<T> list) {
        subItems = list;
    }

    public @NonNull T getSubItem(int position) {
        if (hasSubItem() && position < subItems.size()) {
            return subItems.get(position);
        }
        return null;
    }

    public int getSubItemPosition(T subItem) {
        return subItems != null ? subItems.indexOf(subItem) : -1;
    }

    public void addSubItem(T subItem) {
        if (subItems == null) {
            subItems = new ArrayList<>();
        }
        subItems.add(subItem);
    }

    public void addSubItem(int position, T subItem) {
        if (subItems != null && position >= 0 && position < subItems.size()) {
            subItems.add(position, subItem);
        } else {
            addSubItem(subItem);
        }
    }

    public boolean contains(T subItem) {
        return subItems != null && subItems.contains(subItem);
    }

    public boolean removeSubItem(T subItem) {
        return subItems != null && subItems.remove(subItem);
    }

    public boolean removeSubItem(int position) {
        if (subItems != null && position >= 0 && position < subItems.size()) {
            subItems.remove(position);
            return true;
        }
        return false;
    }
}
