package com.zhengdianfang.miracleframework.adapter.base.listener;

import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter;
import com.zhengdianfang.miracleframework.adapter.base.BaseViewHolder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter.EMPTY_VIEW;
import static com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter.FOOTER_VIEW;
import static com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter.HEADER_VIEW;
import static com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter.LOADING_VIEW;

public abstract class SimpleClickListener implements RecyclerView.OnItemTouchListener {

    private RecyclerView recyclerView;
    private BaseQuickAdapter baseQuickAdapter;
    private GestureDetectorCompat gestureDetectorCompat;
    private boolean isShowPress, isPrePressed = false;
    private View pressedView = null;

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (null == recyclerView) {
            this.recyclerView = rv;
            this.baseQuickAdapter = (BaseQuickAdapter) recyclerView.getAdapter();
            gestureDetectorCompat = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener(recyclerView));
        } else if (recyclerView != rv) {
            this.recyclerView = rv;
            this.baseQuickAdapter = (BaseQuickAdapter) recyclerView.getAdapter();
            gestureDetectorCompat = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener(recyclerView));
        }
        if (!gestureDetectorCompat.onTouchEvent(e) && e.getActionMasked() == MotionEvent.ACTION_UP && isShowPress) {
            if (pressedView != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(pressedView);
                if (childViewHolder == null || ! !isHeaderOrFooterView(childViewHolder.getItemViewType())) {
                    pressedView.setPressed(false);
                }
            }
            isShowPress = false;
            isPrePressed = false;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetectorCompat.onTouchEvent(e);
    }

    private class ItemTouchHelperGestureListener implements GestureDetector.OnGestureListener {

        private RecyclerView recyclerView;

        public ItemTouchHelperGestureListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            isPrePressed = true;
            pressedView = recyclerView.findChildViewUnder(e.getX(), e.getY());
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (isPrePressed && pressedView != null) {
                isShowPress = true;
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isPrePressed && pressedView != null) {
                if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    return false;
                }
                final View itemView = pressedView;
                BaseViewHolder childViewHolder = (BaseViewHolder) recyclerView.getChildViewHolder(itemView);
                if (isHeaderOrFooterPosition(childViewHolder.getLayoutPosition())) {
                    return false;
                }
                LinkedHashSet<Integer> childClickViewIds = childViewHolder.getChildClickViewIds();
                HashSet<Integer> nestViews = childViewHolder.getNestViews();
                if (childClickViewIds != null && !childClickViewIds.isEmpty()) {
                    for (Integer childClickViewId : childClickViewIds) {
                        View childView = itemView.findViewById(childClickViewId);
                        if (childView != null) {
                            if (isRangeOfView(childView, e) && childView.isEnabled()) {
                                if (nestViews != null && nestViews.contains(childClickViewId)) {
                                   return false;
                                }
                                setPressViewHotSpot(e, childView);
                                childView.setPressed(true);
                                onItemChildClick(baseQuickAdapter, childView, childViewHolder.getLayoutPosition() - baseQuickAdapter.getHeaderLayoutCount());
                                resetPressedView(childView);
                                return true;
                            } else {
                                childView.setPressed(false);
                            }
                        }
                    }
                    setPressViewHotSpot(e, itemView);
                    itemView.setPressed(true);
                    for (Integer childClickViewId : childClickViewIds) {
                        View viewById = itemView.findViewById(childClickViewId);
                        if (viewById != null) {
                            viewById.setPressed(false);
                        }
                    }
                    onItemClick(baseQuickAdapter, itemView, childViewHolder.getLayoutPosition() - baseQuickAdapter.getHeaderLayoutCount());
                } else {
                    setPressViewHotSpot(e, itemView);
                    itemView.setPressed(true);
                    onItemClick(baseQuickAdapter, itemView, childViewHolder.getLayoutPosition() - baseQuickAdapter.getHeaderLayoutCount());
                }
                resetPressedView(pressedView);
            }
            return true;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            boolean isChildLongClick = false;
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                return;
            }
            if (isPrePressed && pressedView != null) {
                pressedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(pressedView);
                if (!isHeaderOrFooterPosition(vh.getLayoutPosition())) {
                    Set<Integer> longClickViewIds = vh.getItemChildLongViewIds();
                    Set<Integer> nestViewIds = vh.getNestViews();
                    if (longClickViewIds != null && longClickViewIds.size() > 0) {
                        for (Integer longClickViewId : longClickViewIds) {
                            View childView = pressedView.findViewById(longClickViewId);
                            if (isRangeOfView(childView, e) && childView.isEnabled()) {
                                if (nestViewIds != null && nestViewIds.contains(longClickViewId)) {
                                    isChildLongClick = true;
                                    break;
                                }
                                setPressViewHotSpot(e, childView);
                                onItemChildLongClick(baseQuickAdapter, childView, vh.getLayoutPosition() - baseQuickAdapter.getHeaderLayoutCount());
                                childView.setPressed(true);
                                isShowPress = true;
                                isChildLongClick = true;
                                break;
                            }
                        }
                    }
                    if (!isChildLongClick) {
                        onItemLongClick(baseQuickAdapter, pressedView, vh.getLayoutPosition() - baseQuickAdapter.getHeaderLayoutCount());

                        setPressViewHotSpot(e, pressedView);
                        pressedView.setPressed(true);
                        if (longClickViewIds != null) {
                            for (Integer longClickViewId : longClickViewIds) {
                                View childView = pressedView.findViewById(longClickViewId);
                                if (childView != null) {
                                    childView.setPressed(false);
                                }
                            }
                        }
                        isShowPress = true;
                    }
                }
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }

    private void resetPressedView(final View childView) {
        if (childView != null) {
            childView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (childView != null) {
                        childView.setPressed(true);
                    }
                }
            }, 50);
        }
    }

    private void setPressViewHotSpot(MotionEvent e, View childView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (childView != null && childView.getBackground() != null) {
                childView.getBackground().setHotspot(e.getRawX(), e.getY() - pressedView.getY());
            }
        }
    }

    private boolean isRangeOfView(View childView, MotionEvent e) {
        int[] location = new int[2];
        if (childView == null || !childView.isShown()) {
            return false;
        }
        childView.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (e.getRawX() < x || e.getRawX() > (x + childView.getWidth())
                || e.getRawY() < y || e.getRawY() > (y + childView.getHeight())) {
            return false;
        }
        return true;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        /**
         *  have a headview and EMPTY_VIEW FOOTER_VIEW LOADING_VIEW
         */
        if (baseQuickAdapter == null) {
            if (recyclerView != null) {
                baseQuickAdapter = (BaseQuickAdapter) recyclerView.getAdapter();
            } else {
                return false;
            }
        }
        int type = baseQuickAdapter.getItemViewType(position);
        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW);
    }

    private boolean isHeaderOrFooterView(int type) {
        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW);
    }



    public abstract void onItemChildClick(BaseQuickAdapter adapter, View view, int position);

    public abstract void onItemClick(BaseQuickAdapter adapter, View view, int position);

    public abstract boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position);

    public abstract boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position);

}
