package com.ramascript.allenconnect.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RippleView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Circle> circles = new ArrayList<>();
    private final int maxRippleCount = 3;
    private final int rippleDelay = 800; // ms
    private final int maxRadius = 150;
    private boolean isAnimating = false;

    public RippleView(Context context) {
        super(context);
        init();
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        startAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAnimating = false;
    }

    private void startAnimation() {
        if (isAnimating)
            return;

        isAnimating = true;
        createRipple();
    }

    private void createRipple() {
        if (!isAnimating)
            return;

        final Circle circle = new Circle();
        circles.add(circle);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            circle.radius = maxRadius * animation.getAnimatedFraction();
            circle.alpha = (int) (255 * (1 - animation.getAnimatedFraction()));
            invalidate();

            if (animation.getAnimatedFraction() == 1.0f) {
                circles.remove(circle);
            }
        });

        animator.start();

        if (circles.size() < maxRippleCount) {
            postDelayed(this::createRipple, rippleDelay);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 3; // Position in the top third of the screen

        for (Circle circle : circles) {
            paint.setAlpha(circle.alpha);
            canvas.drawCircle(centerX, centerY, circle.radius, paint);
        }

        if (circles.isEmpty() && isAnimating) {
            createRipple();
        }
    }

    private static class Circle {
        float radius;
        int alpha = 255;
    }
}