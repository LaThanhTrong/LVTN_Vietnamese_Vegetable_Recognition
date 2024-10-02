package com.lathanhtrong.lvtn.Others;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Animation {
    public static void animateArrow(ImageView imageView, float startAngle, float endAngle) {
        ValueAnimator animator = ValueAnimator.ofFloat(startAngle, endAngle);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                imageView.setRotation(value);
            }
        });
        animator.start();
    }
}
