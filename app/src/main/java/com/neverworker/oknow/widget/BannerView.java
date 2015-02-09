package com.neverworker.oknow.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BannerView extends ImageView {

	public BannerView(Context context) {
		super(context);
	}

	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BannerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable logo = this.getDrawable();
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = width * logo.getIntrinsicHeight() / logo.getIntrinsicWidth();
		setMeasuredDimension(width, height);
	}
}
