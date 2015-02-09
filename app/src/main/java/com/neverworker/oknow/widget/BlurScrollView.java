package com.neverworker.oknow.widget;

import java.util.ArrayList;
import java.util.EventListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class BlurScrollView extends ScrollView {
	private ArrayList<OnScrollChangedListener> listeners = new ArrayList<OnScrollChangedListener>();

	public BlurScrollView(Context context) {
		super(context);
	}

	public BlurScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlurScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (OnScrollChangedListener listener : listeners) 
            listener.onChanged(t);
    }
	
	public void addOnScrollChangedListener(OnScrollChangedListener listener) {
		if (this.listeners.contains(listener) == false)
			this.listeners.add(listener);		
	}
	
	public interface OnScrollChangedListener extends EventListener {
		void onChanged(int distance);
	}
}
