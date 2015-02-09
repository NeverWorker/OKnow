package com.neverworker.oknow.widget;

import java.util.ArrayList;

import com.neverworker.oknow.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class HorizontalSelector extends HorizontalScrollView {
	private LinearLayout container;
	private LinearLayout.LayoutParams paddingLayoutParams;
	private View paddingLeftView;
	private View paddingRightView;
	
	private boolean isPosInit = false;
	private ArrayList<OnSelectChangedListener> onSelectChangedListeners = new ArrayList<OnSelectChangedListener>();
	private int currentIndex = 0;
	
	public HorizontalSelector(Context context) {
		super(context);
		init(context);
	}
	public HorizontalSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public HorizontalSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void init(Context context) {
		container = new LinearLayout(context);
		container.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		this.addView(container, params);
		
		paddingLeftView = new View(context);
		paddingRightView = new View(context);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		paddingLayoutParams = new LinearLayout.LayoutParams(screenWidth/2, LayoutParams.MATCH_PARENT);
		
	}

	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPosInit == false) {
        	setIndex(currentIndex);
        	isPosInit = true;
        }
    }
	
	public void setAdapter(ArrayAdapter<?> mAdapter) {
		if (getChildCount() == 0 || mAdapter == null)
			return;
		
		container.removeAllViews();

		container.addView(paddingLeftView, paddingLayoutParams);
		for (int i = 0; i < mAdapter.getCount(); i++) {
			container.addView(mAdapter.getView(i, null, container));
		}
		container.addView(paddingRightView, paddingLayoutParams);
		
		isPosInit = false;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			int currentScrollX = this.getScrollX();
			
			int itemWidth = container.getChildAt(1).getWidth();
			int offsetWidth = (container.getChildAt(0).getWidth() + itemWidth/2) - this.getWidth()/2;
			int index = (int) Math.floor(((float)(currentScrollX-offsetWidth) / (float)itemWidth) + 0.5);
			if (index < 0)
				index = 0;
			if (index >= container.getChildCount()-2)
				index = container.getChildCount()-3;
			
			container.getChildAt(currentIndex + 1).setBackgroundColor(Color.TRANSPARENT);
			container.getChildAt(index + 1).setBackgroundColor(color.oknow_green);
			this.smoothScrollTo((int) ((index)*itemWidth + offsetWidth), 0);
			currentIndex = index;
			
			OnSelectChanged(currentIndex);
			return false;
		}
		
		return super.onTouchEvent(ev);
	}

	public int getIndex() {
		return currentIndex;
	}
	
	public void setIndex(int index) {
		int itemWidth = container.getChildAt(1).getWidth();
		int offsetWidth = (container.getChildAt(0).getWidth() + itemWidth/2) - this.getWidth()/2;
		container.getChildAt(currentIndex + 1).setBackgroundColor(Color.TRANSPARENT);
		container.getChildAt(index + 1).setBackgroundColor(color.oknow_green);
		this.smoothScrollTo((int) ((index)*itemWidth + offsetWidth), 0);
		currentIndex = index;
		
		OnSelectChanged(currentIndex);
	}
	
	public void addOnSelectChangedListeners(OnSelectChangedListener listener) {
		onSelectChangedListeners.add(listener);
	}
	
	public void OnSelectChanged(int index) {
		for (OnSelectChangedListener listener : onSelectChangedListeners)
			listener.OnChanged(index);
	}
	
	public interface OnSelectChangedListener {
		void OnChanged(int index);
	}
}
