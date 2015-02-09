package com.neverworker.oknow;

import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainTab {
	private MainActivity thisActivity;
	private LinearLayout mainTab;

	private final int CHECK_COLOR;
	private final int UNCHECK_COLOR;
	enum TabStatus { CHAT, MAP, POST, INFO, SETTING }
	private TabStatus currentStatus = TabStatus.CHAT;

	private HashMap<String, TabItem> tabItems = new HashMap<String, TabItem>();
	private TabItem currentItem;

	public MainTab(Context context) {
		CHECK_COLOR = context.getResources().getColor(R.color.oknow_green);
		UNCHECK_COLOR = Color.GRAY;
		
		thisActivity = (MainActivity) context;
		mainTab = (LinearLayout) thisActivity.findViewById(R.id.main_tab);
		
		tabItems.put("chat", new NormalTabItem(mainTab.findViewById(R.id.main_tab_chat), new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.changeTab(TabStatus.CHAT);
			}
			
		}));
		tabItems.put("map", new NormalTabItem(mainTab.findViewById(R.id.main_tab_map), new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.changeTab(TabStatus.MAP);
			}
			
		}));
		tabItems.put("post", new ImageSwitchTabItem(mainTab.findViewById(R.id.main_tab_ov), new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.changeTab(TabStatus.POST);
			}
			
		}, R.drawable.main_tab_ov_on, R.drawable.main_tab_ov_off));
		tabItems.put("info", new NormalTabItem(mainTab.findViewById(R.id.main_tab_info), new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.changeTab(TabStatus.INFO);
			}
			
		}));
		tabItems.put("setting", new NormalTabItem(mainTab.findViewById(R.id.main_tab_setting), new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.changeTab(TabStatus.SETTING);
			}
			
		}));
		
		currentItem = tabItems.get("chat");
		currentItem.check();

		// 以下這段 code 使 mainTab 在完成 layout 的時候，對 main activity 呼叫 fragment refresh，
		// 原因在於 mainTab 在完成 layout 之前無法取得真實大小，會造成版面的偏差，需要重新整理
		ViewTreeObserver vto = mainTab.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

		    @SuppressLint("NewApi")
			@Override
		    public void onGlobalLayout() {
		        ViewTreeObserver obs = mainTab.getViewTreeObserver();
		        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
		            obs.removeOnGlobalLayoutListener(this);
		        } else {
		            obs.removeGlobalOnLayoutListener(this);
		        }
		        
		        thisActivity.refreshFragment();
		    }

		});
	}
	
	public int getActualHeight() {
		View subView1 = ((LinearLayout)mainTab.getChildAt(0)).getChildAt(1);
		View subView2 = ((LinearLayout)mainTab.getChildAt(0)).getChildAt(2);
		return (int) (subView1.getHeight() + subView2.getHeight());
	}
	
	public void hide() {
		mainTab.setVisibility(View.INVISIBLE);
	}
	
	public void show() {
		mainTab.setVisibility(View.VISIBLE);
	}

	public TabStatus updateStatus(TabStatus newStatus) {
		if (currentStatus == newStatus)
			return currentStatus;
		
		currentItem.uncheck();
		switch (newStatus) {
		case CHAT:
			currentItem = tabItems.get("chat");
			break;
		case MAP:
			currentItem = tabItems.get("map");
			break;
		case POST:
			currentItem = tabItems.get("post");
			break;
		case INFO:
			currentItem = tabItems.get("info");
			break;
		case SETTING:
			currentItem = tabItems.get("setting");
			break;
		}		
		currentItem.check();

		currentStatus = newStatus;
		return currentStatus;
	}
	
	private abstract class TabItem {
		protected View view;
		
		abstract void check();
		abstract void uncheck();
		
		public TabItem(View v, OnClickListener l) {
			view = v;
			view.setOnClickListener(l);
		}
		
	}
	
	private class NormalTabItem extends TabItem {
		public NormalTabItem(View v, OnClickListener l) {
			super(v, l);
			((ImageView)((LinearLayout)view).getChildAt(1)).setColorFilter(UNCHECK_COLOR);
			((TextView)((LinearLayout)view).getChildAt(2)).setTextColor(UNCHECK_COLOR);
		}
		
		@Override
		void check() {
			((ImageView)((LinearLayout)view).getChildAt(1)).setColorFilter(CHECK_COLOR);
			((TextView)((LinearLayout)view).getChildAt(2)).setTextColor(CHECK_COLOR);
		}

		@Override
		void uncheck() {
			((ImageView)((LinearLayout)view).getChildAt(1)).setColorFilter(UNCHECK_COLOR);
			((TextView)((LinearLayout)view).getChildAt(2)).setTextColor(UNCHECK_COLOR);
		}
	}
	
	private class ImageSwitchTabItem extends TabItem {
		private int checkImgId;
		private int uncheckImgId;
		public ImageSwitchTabItem(View v, OnClickListener l, int img1, int img2) {
			super(v, l);
			checkImgId = img1;
			uncheckImgId = img2;
		}
		
		@Override
		void check() {
			((ImageView)view).setImageResource(checkImgId);
		}

		@Override
		void uncheck() {
			((ImageView)view).setImageResource(uncheckImgId);
			
		}
	}
}
