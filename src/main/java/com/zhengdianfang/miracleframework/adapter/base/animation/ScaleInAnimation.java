package com.zhengdianfang.miracleframework.adapter.base.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

public class ScaleInAnimation implements BaseAnimation {
    private static final float DEFAULT_SCALE_FROM = .5f;
    private final float from;

    public ScaleInAnimation() {
        this(DEFAULT_SCALE_FROM);
    }

    public ScaleInAnimation(float from) {
        this.from = from;
    }

    @Override
    public Animator[] getAnimators(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", from, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", from, 1f);
        return new ObjectAnimator[]{scaleX, scaleY};
    }
}

