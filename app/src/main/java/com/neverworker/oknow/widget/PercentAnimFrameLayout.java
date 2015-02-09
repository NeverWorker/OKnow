package com.neverworker.oknow.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class PercentAnimFrameLayout extends FrameLayout {

	public PercentAnimFrameLayout(Context context) {
		super(context);
	}
	public PercentAnimFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public PercentAnimFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public float getXFraction() {
        return getX() / getWidth();
    }

    public void setXFraction(float xFraction) {
        final int width = getWidth();
        setX((int)((width > 0) ? (xFraction * width) : 0));
    }
    
	public float getYFraction() {
        return getY() / getHeight();
    }

    public void setYFraction(float yFraction) {
        final int height = getHeight();
        setY((int)((height > 0) ? (yFraction * height) : 0));
    }
}
