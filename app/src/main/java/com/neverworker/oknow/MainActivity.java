package com.neverworker.oknow;

import java.util.HashMap;
import java.util.Stack;

import com.neverworker.oknow.MainTab.TabStatus;
import com.neverworker.oknow.common.FileManager;

import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

public class MainActivity extends ActionBarActivity {
	private MainTab mainTab;
	private HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();
	
	private static LocationHelper locHelper;
	private static KnowManager knowManager;
	
	private Stack<FragPage_Status> statusLogs = new Stack<FragPage_Status>();
	private FragPage_Status currentStatus = FragPage_Status.CHAT;
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.getSupportActionBar().hide();
        mainTab = new MainTab(this);
		hideTab();
		
		FileManager.Initial(this);
		locHelper = new LocationHelper(this);
		knowManager = new KnowManager(this, locHelper);
		updateFragment(currentStatus);
	}

	@Override
    protected void onPause() {
		locHelper.onPause();
		super.onPause();
	}
	@Override
    protected void onStart() {
		locHelper.onStart();
		super.onStart();
	}
	@Override
    protected void onResume() {
		locHelper.onResume();
		super.onResume();
	}
	@Override
    protected void onStop() {
		locHelper.onStop();
		super.onStop();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		backFragment();
	}
	
	private void hideTab() {
	    mainTab.hide();
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    View container = this.findViewById(R.id.container);
	    container.setLayoutParams(layoutParams);
	}
	
	private void showTab() {
	    mainTab.show();
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    layoutParams.setMargins(0, 0, 0, mainTab.getActualHeight());
	    View container = this.findViewById(R.id.container);
	    container.setLayoutParams(layoutParams);
	}
	
	enum FragPage_Status { SIGNUP, LOGIN, CHAT, MAP, POST, INFO, SETTING, VOICE, HISTORY }
	private void updateFragment(FragPage_Status status) {
		Fragment nextFragment = null;
		
		switch (status) {
		case SIGNUP:
			hideTab();
			if (fragments.containsKey("SIGNUP") == false)
				fragments.put("SIGNUP", new SignupFragment());
			nextFragment = fragments.get("SIGNUP");
			break;
		case LOGIN:
			hideTab();
			if (fragments.containsKey("LOGIN") == false)
				fragments.put("LOGIN", new LoginFragment());
			nextFragment = fragments.get("LOGIN");
			break;
		case CHAT:
			mainTab.updateStatus(TabStatus.CHAT);
			showTab();
			if (fragments.containsKey("CHAT") == false)
				fragments.put("CHAT", new ChatFragment());
			nextFragment = fragments.get("CHAT");
			break;
		case MAP:
			mainTab.updateStatus(TabStatus.MAP);
			showTab();
			if (fragments.containsKey("MAP") == false) {
				fragments.put("MAP", new CustomMapFragment());
			}
			nextFragment = fragments.get("MAP");
			break;
		case POST:
			mainTab.updateStatus(TabStatus.POST);
			hideTab();
			if (fragments.containsKey("POST") == false)
				fragments.put("POST", new PostFragment());
			nextFragment = fragments.get("POST");
			break;
		case INFO:
			mainTab.updateStatus(TabStatus.INFO);
			showTab();
			if (fragments.containsKey("INFO") == false)
				fragments.put("INFO", new InfoFragment());
			nextFragment = fragments.get("INFO");
			break;
		case SETTING:
			mainTab.updateStatus(TabStatus.SETTING);
			showTab();
			if (fragments.containsKey("SETTING") == false)
				fragments.put("SETTING", new SettingFragment());
			nextFragment = fragments.get("SETTING");
			break;
		case VOICE:
			hideTab();
			if (fragments.containsKey("VOICE") == false)
				fragments.put("VOICE", new KnowFragment());
			nextFragment = fragments.get("VOICE");
			break;
		case HISTORY:
			hideTab();
			if (fragments.containsKey("HISTORY") == false)
				fragments.put("HISTORY", new SimpleListFragment());
			nextFragment = fragments.get("HISTORY");
			break;
		default:
			showTab();
			if (fragments.containsKey("CHAT") == false)
				fragments.put("CHAT", new ChatFragment());
			nextFragment = fragments.get("CHAT");
			break;
		}
		CustomAnimationPair animatePair = measureAnimate(currentStatus, status);
		getFragmentManager().beginTransaction()
							.setCustomAnimations(animatePair.Enter, animatePair.Exit) 
							.replace(R.id.container, nextFragment)
							.commit();
		
		if (statusLogs.size() > 5) {
			statusLogs.remove(0);
		}
		statusLogs.push(currentStatus);
		currentStatus = status;
	}
	
	private class CustomAnimationPair {
		public final int Enter;
		public final int Exit;
		public CustomAnimationPair(int enter, int exit) {
			Enter = enter;
			Exit = exit;
		}
	}
	
	private int getStatusAnimateFactor(FragPage_Status status) {
		switch (status) {
		case SIGNUP:
			return -2;
		case LOGIN:
			return -1;
		case CHAT:
			return 0;
		case MAP:
			return 1;
		case POST:
			return 2;
		case INFO:
			return 3;
		case SETTING:
			return 4;
		case VOICE:
			return 99;
		case HISTORY:
			return 99;
		default:
			return -99;
		}
	}
	
	private CustomAnimationPair measureAnimate(FragPage_Status source, FragPage_Status dest) {
		int enter = 0;
		int exit = 0;
		int srcFactor = getStatusAnimateFactor(source);
		int destFactor = getStatusAnimateFactor(dest);
		
		if (srcFactor < destFactor) {
			if (destFactor < 0 || srcFactor < 0) {
				exit = R.anim.slide_out_bottom;
			} else {
				enter = R.anim.slide_in_right;
			}
		} else {
			if (srcFactor < 0 || destFactor < 0) {
				enter = R.anim.slide_in_bottom;
			} else {
				enter = R.anim.slide_in_left;
			}
		}
		
		return new CustomAnimationPair(enter, exit);
	}
	
	public void changeTab(MainTab.TabStatus tabStatus) {
		switch(tabStatus) {
		case CHAT:
			updateFragment(FragPage_Status.CHAT);
			break;
		case MAP:
			updateFragment(FragPage_Status.MAP);
			break;
		case POST:
			updateFragment(FragPage_Status.POST);
			break;
		case INFO:
			updateFragment(FragPage_Status.INFO);
			break;
		case SETTING:
			updateFragment(FragPage_Status.SETTING);
			break;
		}
	}
	
	public void main() {
		updateFragment(FragPage_Status.CHAT);
	}
	
	public void login() {
		updateFragment(FragPage_Status.LOGIN);
	}
	
	public void signup() {
		updateFragment(FragPage_Status.SIGNUP);
	}
	
	public void history() {
		updateFragment(FragPage_Status.HISTORY);
	}
	
	public void switchToKnowFragment(String objectId, String tagName, String message, ParseGeoPoint location) {
		updateFragment(FragPage_Status.VOICE);
		if (fragments.containsKey("VOICE") == false)
			fragments.put("VOICE", new KnowFragment());
		KnowFragment knowFragment = (KnowFragment) fragments.get("VOICE");
		
		knowFragment.feedDetail(objectId, tagName, message, location);

	}
	
	public void refreshFragment() {
		updateFragment(currentStatus);
	}
	
	public void backFragment() {
		if (ParseUser.getCurrentUser() != null) {
			if (statusLogs.contains(FragPage_Status.LOGIN))
				statusLogs.remove(FragPage_Status.LOGIN);
			if (statusLogs.contains(FragPage_Status.SIGNUP))
				statusLogs.remove(FragPage_Status.SIGNUP);
		} else {
			if (currentStatus == FragPage_Status.LOGIN)
				statusLogs.pop();
		}
		
		if (currentStatus != FragPage_Status.CHAT) {
			updateFragment(statusLogs.pop());
			statusLogs.pop();
		}
		else
			super.onBackPressed();
	}
	
	public ProgressDialog startLoading(String message) {
		return ProgressDialog.show(this, "", message, true);
	}
	
	public KnowManager getKnowManager() {
		return knowManager;
	}
}
