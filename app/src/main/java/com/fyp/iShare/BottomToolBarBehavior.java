package com.fyp.iShare;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

public class BottomToolBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private int height;

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, LinearLayout child, int layoutDirection) {
        height = child.getHeight();
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       LinearLayout child, @NonNull
                                               View directTargetChild, @NonNull View target,
                                       int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull LinearLayout child,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed,
                               @ViewCompat.NestedScrollType int type) {
        if (dyConsumed > 0) {
            slideDown(child);
        } else if (dyConsumed < 0) {
            slideUp(child);
        }
    }

    private void slideUp(LinearLayout child) {
        child.clearAnimation();
        child.animate().translationY(0).setDuration(100);
    }

    private void slideDown(LinearLayout child) {
        child.clearAnimation();
        child.animate().translationY(height).setDuration(100);
    }
}
