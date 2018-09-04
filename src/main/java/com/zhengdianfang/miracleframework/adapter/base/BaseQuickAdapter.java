package com.zhengdianfang.miracleframework.adapter.base;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zhengdianfang.miracleframework.adapter.base.animation.AlphaAnimation;
import com.zhengdianfang.miracleframework.adapter.base.animation.BaseAnimation;
import com.zhengdianfang.miracleframework.adapter.base.animation.ScaleInAnimation;
import com.zhengdianfang.miracleframework.adapter.base.animation.SlideInBottomAnimation;
import com.zhengdianfang.miracleframework.adapter.base.animation.SlideInLeftAnimation;
import com.zhengdianfang.miracleframework.adapter.base.animation.SlideInRightAnimation;
import com.zhengdianfang.miracleframework.adapter.base.entity.IExpandable;
import com.zhengdianfang.miracleframework.adapter.base.listener.OnItemChildClickListener;
import com.zhengdianfang.miracleframework.adapter.base.listener.OnItemChildLongClickListener;
import com.zhengdianfang.miracleframework.adapter.base.listener.OnItemClickListener;
import com.zhengdianfang.miracleframework.adapter.base.listener.OnItemLongClickListener;
import com.zhengdianfang.miracleframework.adapter.base.loadmore.LoadMoreView;
import com.zhengdianfang.miracleframework.adapter.base.loadmore.SimpleLoadMoreView;
import com.zhengdianfang.miracleframework.adapter.base.util.MultiTypeDelegate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class BaseQuickAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    private boolean nextLoadEnable = false;
    private boolean loadMoreEnable = false;
    private boolean loading = false;
    private boolean loadMoreEndClick = false;

    public static final int ALPHA_IN = 0x00000001;
    public static final int SCALE_IN = 0x00000002;
    public static final int SLIDE_IN_BOTTOM = 0x00000003;
    public static final int SLIDE_IN_LEFT = 0x00000004;
    public static final int SLIDE_IN_RIGHT = 0x00000005;
    private LoadMoreView loadMoreView = new SimpleLoadMoreView();
    private MultiTypeDelegate<T> multiTypeDelegate;
    private BaseAnimation customAnimation;
    private BaseAnimation selectAnimation;
    private boolean headerViewAsFlow;
    private boolean footerViewAsFlow;
    private SpanSizeLookup spanSizeLookup;
    private int startRefreshPosition = 1;
    private int preLoadNumber = 1;

    @IntDef({ALPHA_IN, SCALE_IN, SLIDE_IN_BOTTOM, SLIDE_IN_LEFT, SLIDE_IN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }


    public static final int HEADER_VIEW = 0x00000111;
    public static final int LOADING_VIEW = 0x00000222;
    public static final int FOOTER_VIEW = 0x00000333;
    public static final int EMPTY_VIEW = 0x00000555;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemChildClickListener onItemChildClickListener;
    private OnItemChildLongClickListener onItemChildLongClickListener;
    private UpFetchListener upFetchListener;
    private RequestLoadMoreListener requestLoadMoreListener;

    private boolean firstOnlyEnable = false;
    private boolean openAnimationEnable = false;
    private Interpolator interpolator = new LinearInterpolator();
    private int duration = 300;
    private int lastPosition = -1;

    private LinearLayout headerLayout;
    private LinearLayout footerLayout;

    private FrameLayout emptyLayout;
    private boolean isUseEmpty = true;
    private boolean headAndEmptyEnable = false;
    private boolean footAndEmptyEnable = false;
    private boolean upFetchEnable = false;
    private boolean upFetching = false;
    private boolean enableLoadMoreEndClick;

    protected Context context;
    protected int layoutResId;
    protected LayoutInflater layoutInflater;
    protected List<T> data;

    private RecyclerView recyclerView;

    public interface RequestLoadMoreListener {

        void onLoadMore();

    }

    public interface UpFetchListener {
        void upFetch();
    }

    public interface SpanSizeLookup {

        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    protected abstract void convert(K helper, T item);


    @Override
    public void onBindViewHolder(K holder, int position) {
        autoRefresh(position);
        //Do not move position, need to change before LoadMoreView binding
        autoLoadMore(position);
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case 0:
                convert(holder, getItem(position - getHeaderLayoutCount()));
                break;
            case LOADING_VIEW:
                loadMoreView.convert(holder);
                break;
            case HEADER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert(holder, getItem(position - getHeaderLayoutCount()));
                break;
        }
    }

    public void setPreLoadNumber(int preLoadNumber) {
        if (preLoadNumber > 1) {
            this.preLoadNumber = preLoadNumber;
        }
    }

    private void autoLoadMore(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        if (position < getItemCount() - preLoadNumber) {
            return;
        }
        if (loadMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }
        loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        if (!loading) {
            loading = true;
            if (getRecyclerView() != null) {
                getRecyclerView().post(new Runnable() {
                    @Override
                    public void run() {
                        requestLoadMoreListener.onLoadMore();
                    }
                });
            } else {
                requestLoadMoreListener.onLoadMore();
           }
        }
    }

    public void setUpFetchListener(UpFetchListener upFetchListener) {
        this.upFetchListener = upFetchListener;
    }

    private void autoRefresh(int position) {
        if (!isUpFetchEnable() || isUpFetching()) {
            return;
        }
        if (position <= startRefreshPosition && upFetchListener != null) {
            upFetchListener.upFetch();
        }
    }

    public void setStartRefreshPosition(int startRefreshPosition) {
        this.startRefreshPosition = startRefreshPosition;
    }

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.spanSizeLookup = spanSizeLookup;
    }

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == 1) {
            count = 1;
            if (headAndEmptyEnable && getHeaderLayoutCount() != 0) {
                count++;
            }
            if (footAndEmptyEnable && getFooterLayoutCount() != 0) {
                count++;
            }
        } else {
            count = getHeaderLayoutCount() + data.size() + getFooterLayoutCount() + getLoadMoreViewCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            boolean header = headAndEmptyEnable && getHeaderLayoutCount() != 0;
            switch (position) {
                case 0:
                    if (header) {
                        return HEADER_VIEW;
                    } else {
                        return EMPTY_VIEW;
                    }
                case 1:
                    if (header) {
                        return EMPTY_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                case 2:
                    return FOOTER_VIEW;
                default:
                    return EMPTY_VIEW;
            }
        }
        int numHeaders = getHeaderLayoutCount();
        if (position < numHeaders) {
            return HEADER_VIEW;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = data.size();
            if (adjPosition < adapterCount) {
                return getDefItemViewType(adjPosition);
            } else {
                adjPosition = adjPosition - adapterCount;
                int numFooters = getFooterLayoutCount();
                if (adjPosition < numFooters) {
                    return FOOTER_VIEW;
                } else {
                    return LOADING_VIEW;
                }
            }
        }
    }

    protected int getDefItemViewType(int position) {
        if (multiTypeDelegate != null) {
            return multiTypeDelegate.getDefItemViewType(data, position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder;
        this.context = parent.getContext();
        this.layoutInflater = LayoutInflater.from(context);
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            case HEADER_VIEW:
                baseViewHolder = createBaseViewHolder(headerLayout);
                break;
            case EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(emptyLayout);
                break;
            case FOOTER_VIEW:
                baseViewHolder = createBaseViewHolder(footerLayout);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
                bindViewClickListener(baseViewHolder);
        }
        baseViewHolder.setAdapter(this);
        return baseViewHolder;

    }
    private void bindViewClickListener(final BaseViewHolder baseViewHolder) {
        if (baseViewHolder == null) {
            return;
        }
        final View view = baseViewHolder.itemView;
        if (view == null) {
            return;
        }
        if (getOnItemClickListener() != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnItemClick(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }
        if (getOnItemLongClickListener() != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return setOnItemLongClick(v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
            });
        }
    }

    public void setOnItemClick(View v, int position) {
        getOnItemClickListener().onItemClick(BaseQuickAdapter.this, v, position);
    }

    public boolean setOnItemLongClick(View v, int position) {
        return getOnItemLongClickListener().onItemLongClick(BaseQuickAdapter.this, v, position);
    }


    public void setLoadMoreEndClick(boolean loadMoreEndClick) {
        this.loadMoreEndClick = loadMoreEndClick;
    }

    public LinearLayout getHeaderLayout() {
        return headerLayout;
    }

    public LinearLayout getFooterLayout() {
        return footerLayout;
    }

    public int addHeaderView(View header) {
       return addHeaderView(header, -1);
    }

    private int addHeaderView(View header, int index) {
       return addHeaderView(header, index, LinearLayout.VERTICAL);
    }

    private int addHeaderView(View header, int index, int orientation) {
        if (headerLayout == null) {
            headerLayout = new LinearLayout(header.getContext());
            if (orientation == LinearLayout.VERTICAL) {
               headerLayout.setOrientation(LinearLayout.VERTICAL);
               headerLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else if (orientation == LinearLayout.HORIZONTAL) {
                headerLayout.setOrientation(LinearLayout.HORIZONTAL);
                headerLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        int childCount = headerLayout.getChildCount();
        if (index < 0 || index > childCount) {
           index = childCount;
        }
        headerLayout.addView(header, index);
        if (headerLayout.getChildCount() == 1) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setHeaderView(View header) {
        return setHeaderView(header, 0, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index) {
        return setHeaderView(header, index, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer) {
        return addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index) {
        return addFooterView(footer, index, LinearLayout.VERTICAL);
    }

    /**
     * Add footer view to mFooterLayout and set footer view position in mFooterLayout.
     * When index = -1 or index >= child count in mFooterLayout,
     * the effect of this method is the same as that of {@link #addFooterView(View)}.
     *
     * @param footer
     * @param index  the position in mFooterLayout of this footer.
     *               When index = -1 or index >= child count in mFooterLayout,
     *               the effect of this method is the same as that of {@link #addFooterView(View)}.
     */
    public int addFooterView(View footer, int index, int orientation) {
        if (footerLayout == null) {
            footerLayout = new LinearLayout(footer.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                footerLayout.setOrientation(LinearLayout.VERTICAL);
                footerLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                footerLayout.setOrientation(LinearLayout.HORIZONTAL);
                footerLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = footerLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        footerLayout.addView(footer, index);
        if (footerLayout.getChildCount() == 1) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setFooterView(View header) {
        return setFooterView(header, 0, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index) {
        return setFooterView(header, index, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index, int orientation) {
        if (footerLayout == null || footerLayout.getChildCount() <= index) {
            return addFooterView(header, index, orientation);
        } else {
            footerLayout.removeViewAt(index);
            footerLayout.addView(header, index);
            return index;
        }
    }

    private int getFooterViewPosition() {
        //Return to footer view notify position
        if (getEmptyViewCount() == 1) {
            int position = 1;
            if (headAndEmptyEnable && getHeaderLayoutCount() != 0) {
                position++;
            }
            if (footAndEmptyEnable) {
                return position;
            }
        } else {
            return getHeaderLayoutCount() + data.size();
        }
        return -1;
    }

    public int setHeaderView(View header, int index, int orientation) {
        if (headerLayout == null || headerLayout.getChildCount() <= index) {
            return addHeaderView(header, index, orientation);
        } else {
            headerLayout.removeViewAt(index);
            headerLayout.addView(header, index);
            return index;
        }
    }

    public void removeHeaderView(View header) {
        if (getHeaderLayoutCount() == 0) return;

        headerLayout.removeView(header);
        if (headerLayout.getChildCount() == 0) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    public void removeFooterView(View footer) {
        if (getFooterLayoutCount() == 0) return;

        footerLayout.removeView(footer);
        if (footerLayout.getChildCount() == 0) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    public void removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0)
            return;

        headerLayout.removeAllViews();
        int position = getHeaderViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }



    private int getHeaderViewPosition() {
        if (getEmptyViewCount() == 1) {
            if (headAndEmptyEnable) {
                return 0;
            }
        } else {
            return 0;
        }
        return -1;
    }

    private K getLoadingView(ViewGroup parent) {
        View view = getItemView(loadMoreView.getLayoutId(), parent);
        K holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                    notifyLoadMoreToLoading();
                }
                if (enableLoadMoreEndClick && loadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_END) {
                    notifyLoadMoreToLoading();
                }
            }
        });
        return holder;
    }

    public void setEnableLoadMoreEndClick(boolean enableLoadMoreEndClick) {
        this.enableLoadMoreEndClick = enableLoadMoreEndClick;
    }

    public void notifyLoadMoreToLoading() {
        if (loadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_LOADING) {
            return;
        }
        loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        int layoutId = layoutResId;
        if (multiTypeDelegate != null) {
            layoutId = multiTypeDelegate.getLayoutId(viewType);
        }
        return createBaseViewHolder(parent, layoutId);
    }

    protected K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }


    protected K createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K k;
        // 泛型擦除会导致z为null
        if (z == null) {
            k = (K) new BaseViewHolder(view);
        } else {
            k = createGenericKInstance(z, view);
        }
        return k != null ? k : (K) new BaseViewHolder(view);
    }

    private K createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            // inner and unstatic class
            if (z.isMemberClass() && !Modifier.isStatic(z.getModifiers())) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * get generic parameter K
     *
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                } else if (temp instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) temp).getRawType();
                    if (rawType instanceof Class && BaseViewHolder.class.isAssignableFrom((Class<?>) rawType)) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }

    protected View getItemView(@LayoutRes int layoutResId, ViewGroup parent) {
        return layoutInflater.inflate(layoutResId, parent, false);
    }

    public void bindToRecyclerView(RecyclerView recyclerView) {
        if (this.recyclerView != null) {
            throw new RuntimeException("Don't bind twice");
        }
        setRecyclerView(recyclerView);
        getRecyclerView().setAdapter(this);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }


    public int getHeaderLayoutCount() {
        if (headerLayout == null || headerLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public OnItemChildClickListener getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public OnItemChildLongClickListener getOnItemChildLongClickListener() {
        return onItemChildLongClickListener;
    }

    public void setRequestLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener, RecyclerView recyclerView) {
        openLoadMore(requestLoadMoreListener);
        if (getRecyclerView() == null) {
            setRecyclerView(recyclerView);
        }
    }
    
    public void disableLoadMoreIfNotFullPage() {
        if (getRecyclerView() != null) {
            disableLoadMoreIfNotFullPage(getRecyclerView());
        }
    }
    
    public void setEnableLoadMore(boolean enable) {
        int oldLoadMoreCount = getLoadMoreViewCount();
        loadMoreEnable = enable;
        int newLoadMoreCount = getLoadMoreViewCount();
        if (oldLoadMoreCount == 1) {
            if (newLoadMoreCount == 0) {
                notifyItemRemoved(getLoadMoreViewPosition());
            }
        } else {
            if (newLoadMoreCount == 1) {
                loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemInserted(getLoadMoreViewPosition());
            }
        }

    }

    public int getLoadMoreViewPosition() {
        return getHeaderLayoutCount() + data.size() + getFooterLayoutCount();
    }

    public int getFooterLayoutCount() {
        if (footerLayout == null || footerLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    public void setUpFetchEnable(boolean upFetchEnable) {
        this.upFetchEnable = upFetchEnable;
    }

    public boolean isUpFetchEnable() {
        return upFetchEnable;
    }

    public boolean isUpFetching() {
        return upFetching;
    }


    public void setLoadMoreView(LoadMoreView loadMoreView) {
        this.loadMoreView = loadMoreView;
    }

    public void loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        loading = false;
        nextLoadEnable = true;
        loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public final void refreshNotifyItemChanged(int position) {
        notifyItemChanged(position + getHeaderLayoutCount());
    }

    public void loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        loading = false;
        loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
        notifyItemChanged(getLoadMoreViewPosition());
    }

    public BaseQuickAdapter(@LayoutRes int layoutResId, @Nullable List<T> datas) {
        this.data = datas == null ? new ArrayList<T>() : datas;
        if (layoutResId != 0) {
            this.layoutResId = layoutResId;
        }
    }

    public BaseQuickAdapter(@Nullable List<T> data) {
        this(0, data);
    }

    public BaseQuickAdapter(@LayoutRes int layoutResId) {
        this(layoutResId, null);
    }

    public void setNewData(@Nullable List<T> data) {
        this.data = data == null ? new ArrayList<T>() : data;
        if (requestLoadMoreListener != null) {
            nextLoadEnable = true;
            loadMoreEnable = true;
            loading = false;
            loadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }
        lastPosition = -1;
        notifyDataSetChanged();
    }


    public void addData(@IntRange(from = 0) int position, @NonNull T data) {
        this.data.add(position, data);
        notifyItemInserted(position + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    public void addData(@NonNull T data) {
        this.data.add(data);
        notifyItemInserted(this.data.size() + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    public void remove(@IntRange(from = 0) int position) {
        data.remove(position);
        int internalPosition = position + getHeaderLayoutCount();
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, data.size() - internalPosition);
    }

    public void setData(@IntRange(from = 0) int index, @NonNull T data) {
        this.data.set(index, data);
        notifyItemChanged(index + getHeaderLayoutCount());
    }

    public void addData(@IntRange(from = 0) int position, @NonNull Collection<? extends T> newData) {
        data.addAll(position, newData);
        notifyItemRangeInserted(position + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * add new data to the end of mData
     *
     * @param newData the new data collection
     */
    public void addData(@NonNull Collection<? extends T> newData) {
        data.addAll(newData);
        notifyItemRangeInserted(data.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    public void replaceData(@NonNull Collection<? extends T> newDatas) {
        // 不是同一个引用才清空列表
        if (newDatas != this.data) {
            data.clear();
            data.addAll(newDatas);
        }
        notifyDataSetChanged();
    }

    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = data == null ? 0 : data.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    public List<T> getData() {
        return data;
    }

    @Nullable
    public T getItem(@IntRange(from = 0) int position) {
        if (position >= 0 && position < data.size()) {
            return data.get(position);
        } else {
            return null;
        }
    }

    private int getLoadMoreViewCount() {
        if (requestLoadMoreListener == null || !loadMoreEnable) {
            return 0;
        }
        if (!nextLoadEnable && loadMoreView.isLoadEndMoreGone()) {
            return 0;
        }
        if (data.size() == 0) {
            return 0;
        }
        return 1;
    }

    public int getEmptyViewCount() {
        if (emptyLayout == null || emptyLayout.getChildCount() == 0) {
            return 0;
        }
        if (!isUseEmpty) {
            return 0;
        }
        if (data.size() != 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == HEADER_VIEW && isHeaderViewAsFlow()) {
                        return 1;
                    }
                    if (type == FOOTER_VIEW && isFooterViewAsFlow()) {
                        return 1;
                    }
                    if (spanSizeLookup == null) {
                        return isFixedViewType(type) ? gridLayoutManager.getSpanCount() : 1;
                    } else {
                        return (isFixedViewType(type)) ? gridLayoutManager.getSpanCount() : spanSizeLookup.getSpanSize(gridLayoutManager,
                                position - getHeaderLayoutCount());
                    }
                }
            });
        }
    }

    public void setMultiTypeDelegate(MultiTypeDelegate<T> multiTypeDelegate) {
        this.multiTypeDelegate = multiTypeDelegate;
    }

    public MultiTypeDelegate<T> getMultiTypeDelegate() {
        return multiTypeDelegate;
    }

    protected boolean isFixedViewType(int type) {
        return type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type ==
                LOADING_VIEW;
    }

    public boolean isFooterViewAsFlow() {
        return footerViewAsFlow;
    }

    public boolean isHeaderViewAsFlow() {
        return headerViewAsFlow;
    }

    public void setHeaderViewAsFlow(boolean headerViewAsFlow) {
        this.headerViewAsFlow = headerViewAsFlow;
    }

    public void setFooterViewAsFlow(boolean footerViewAsFlow) {
        this.footerViewAsFlow = footerViewAsFlow;
    }

    public void setEmptyView(@LayoutRes int layoutResId, ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        setEmptyView(view);
    }

    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (emptyLayout == null) {
            emptyLayout = new FrameLayout(emptyView.getContext());
            final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            emptyLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        emptyLayout.removeAllViews();
        emptyLayout.addView(emptyView);
        isUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == 1) {
                int position = 0;
                if (headAndEmptyEnable && getHeaderLayoutCount() != 0) {
                    position++;
                }
                notifyItemInserted(position);
            }
        }
    }

    public void setHeaderAndEmpty(boolean isHeadAndEmpty) {
        setHeaderFooterEmpty(isHeadAndEmpty, false);
    }

    public void setHeaderFooterEmpty(boolean isHeadAndEmpty, boolean isFootAndEmpty) {
        headAndEmptyEnable = isHeadAndEmpty;
        footAndEmptyEnable = isFootAndEmpty;
    }


    public void setUseEmpty(boolean useEmpty) {
        isUseEmpty = useEmpty;
    }

    private void checkNotNull() {
        if (getRecyclerView() == null) {
            throw new RuntimeException("please bind recyclerView first!");
        }
    }

    @Nullable
    public View getViewByPosition(int position, @IdRes int viewId) {
        checkNotNull();
        return getViewByPosition(getRecyclerView(), position, viewId);
    }

    @Nullable
    public View getViewByPosition(RecyclerView recyclerView, int position, @IdRes int viewId) {
        if (recyclerView == null) {
            return null;
        }
        BaseViewHolder viewHolder = (BaseViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
        if (viewHolder == null) {
            return null;
        }
        return viewHolder.getView(viewId);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemChildLongClickListener(OnItemChildLongClickListener onItemChildLongClickListener) {
        this.onItemChildLongClickListener = onItemChildLongClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private boolean hasSubItems(IExpandable item) {
        if (item == null) {
            return false;
        }
        List list = item.getSubItems();
        return list != null && list.size() > 0;
    }

    private int recursiveExpand(int position, @NonNull List list) {
        int count = list.size();
        int pos = position + list.size() - 1;
        for (int i = list.size() - 1; i >= 0; i--, pos--) {
            if (list.get(i) instanceof IExpandable) {
                IExpandable item = (IExpandable) list.get(i);
                if (item.isExpanded() && hasSubItems(item)) {
                    List subList = item.getSubItems();
                    data.addAll(pos + 1, subList);
                    int subItemCount = recursiveExpand(pos + 1, subList);
                    count += subItemCount;
                }
            }
        }
        return count;

    }

    public boolean isExpandable(T item) {
        return item != null && item instanceof IExpandable;
    }

    private @Nullable IExpandable getExpandableItem(int position) {
        T item = getItem(position);
        if (isExpandable(item)) {
            return (IExpandable) item;
        } else {
            return null;
        }
    }

    public int expand(@IntRange(from = 0) int position, boolean animate, boolean shouldNotify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }
        int subItemCount = 0;
        if (!expandable.isExpanded()) {
            List list = expandable.getSubItems();
            data.addAll(position + 1, list);
            subItemCount += recursiveExpand(position + 1, list);
            expandable.setExpanded(true);
        }
        int parentPos = position + getHeaderLayoutCount();
        if (shouldNotify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeInserted(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    public int expand(@IntRange(from = 0) int position, boolean animate) {
        return expand(position, animate, true);
    }

    public int expand(@IntRange(from = 0) int position) {
        return expand(position, true, true);
    }

    public int expandAll(int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        T endItem = null;
        if (position + 1 < this.data.size()) {
            endItem = getItem(position + 1);
        }

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }

        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }

        int count = expand(position + getHeaderLayoutCount(), false, false);
        for (int i = position + 1; i < this.data.size(); i++) {
            T item = getItem(i);

            if (item == endItem) {
                break;
            }
            if (isExpandable(item)) {
                count += expand(i + getHeaderLayoutCount(), false, false);
            }
        }

        if (notify) {
            if (animate) {
                notifyItemRangeInserted(position + getHeaderLayoutCount() + 1, count);
            } else {
                notifyDataSetChanged();
            }
        }
        return count;
    }

    public int expandAll(int position, boolean init) {
        return expandAll(position, true, !init);
    }

    public void expandAll() {
        for (int i = data.size() - 1 + getHeaderLayoutCount(); i >= getHeaderLayoutCount(); i--) {
            expandAll(i, false, false);
        }
    }

    private int getItemPosition(T item) {
        return item != null && data != null && !data.isEmpty() ? data.indexOf(item) : -1;
    }

    private int recursiveCollapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (!isExpandable(item)) {
            return 0;
        }
        IExpandable expandable = (IExpandable) item;
        int subItemCount = 0;
        if (expandable.isExpanded()) {
            List<T> subItems = expandable.getSubItems();
            if (null == subItems) return 0;

            for (int i = subItems.size() - 1; i >= 0; i--) {
                T subItem = subItems.get(i);
                int pos = getItemPosition(subItem);
                if (pos < 0) {
                    continue;
                }
                if (subItem instanceof IExpandable) {
                    subItemCount += recursiveCollapse(pos);
                }
                data.remove(pos);
                subItemCount++;
            }
        }
        return subItemCount;
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @param animate  collapse with animation or not.
     * @param notify   notify the recyclerView refresh UI or not.
     * @return the number of subItems collapsed.
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        int subItemCount = recursiveCollapse(position);
        expandable.setExpanded(false);
        int parentPos = position + getHeaderLayoutCount();
        if (notify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeRemoved(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    public int collapse(@IntRange(from = 0) int position) {
        return collapse(position, true, true);
    }

    public int collapse(@IntRange(from = 0) int position, boolean animate) {
        return collapse(position, animate, true);
    }

    public int getParentPosition(@NonNull T item) {
        int position = getItemPosition(item);
        if (position == -1) {
            return -1;
        }

        // if the item is IExpandable, return a closest IExpandable item position whose level smaller than this.
        // if it is not, return the closest IExpandable item position whose level is not negative
        int level;
        if (item instanceof IExpandable) {
            level = ((IExpandable) item).getLevel();
        } else {
            level = Integer.MAX_VALUE;
        }
        if (level == 0) {
            return position;
        } else if (level == -1) {
            return -1;
        }

        for (int i = position; i >= 0; i--) {
            T temp = data.get(i);
            if (temp instanceof IExpandable) {
                IExpandable expandable = (IExpandable) temp;
                if (expandable.getLevel() >= 0 && expandable.getLevel() < level) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void openLoadAnimation(@AnimationType int animationType) {
        this.openAnimationEnable = true;
        this.customAnimation = null;
        switch (animationType) {
            case ALPHA_IN:
                selectAnimation = new AlphaAnimation();
                break;
            case SCALE_IN:
                selectAnimation = new ScaleInAnimation();
                break;
            case SLIDE_IN_BOTTOM:
                selectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDE_IN_LEFT:
                selectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDE_IN_RIGHT:
                selectAnimation = new SlideInRightAnimation();
                break;
        }
    }

    public void openLoadAnimaton(BaseAnimation animation) {
        this.openAnimationEnable = true;
        this.customAnimation = animation;
    }

    public void openLoadAnimation() {
       this.openAnimationEnable = true;
    }

    public void closeLoadAnimation() {
        this.openAnimationEnable = false;
    }

    private void addAnimation(K holder) {
        if (openAnimationEnable) {
            if (!firstOnlyEnable || holder.getLayoutPosition() > lastPosition) {
                BaseAnimation animation;
                if (customAnimation != null) {
                    animation = customAnimation;
                } else {
                    animation = selectAnimation;
                }
                for (Animator animator : animation.getAnimators(holder.itemView)) {
                    startAnim(animator);
                }
                lastPosition = holder.getLayoutPosition();

            }
        }
    }

    private void startAnim(Animator animator) {
        animator.setDuration(this.duration).start();
        animator.setInterpolator(this.interpolator);
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }


    private void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        setEnableLoadMore(false);
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) {
           return;
        }
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                   if (isFullScreen(linearLayoutManager)) {
                       setEnableLoadMore(true);
                   }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(positions);
                    int pos = getTheBiggestNumber(positions) + 1;
                    if (pos != getItemCount()) {
                        setEnableLoadMore(true);
                    }
                }
            }, 50);
        }
    }


    private int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    private boolean isFullScreen(LinearLayoutManager linearLayoutManager) {
        return linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1 != getItemCount()
                || linearLayoutManager.findFirstCompletelyVisibleItemPosition() != 0;
    }

    private void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.requestLoadMoreListener = requestLoadMoreListener;
        nextLoadEnable = true;
        loadMoreEnable = true;
        loading = false;
    }

}
