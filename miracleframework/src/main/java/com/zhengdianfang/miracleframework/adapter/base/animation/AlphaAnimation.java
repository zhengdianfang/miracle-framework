package com.zhengdianfang.miracleframework.adapter.base.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class AlphaAnimation implements BaseAnimation {
    public static final float DEFAULT_ALPHA_FROM = 0f;
    private final float from;

    public AlphaAnimation() {
        this(DEFAULT_ALPHA_FROM);
    }

    public AlphaAnimation(float from) {
        this.from = from;
    }

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[]{ObjectAnimator.ofFloat(view, "alpha", from, 1f)};
    }
}
