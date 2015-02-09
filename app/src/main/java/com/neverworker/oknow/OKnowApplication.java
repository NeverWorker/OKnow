package com.neverworker.oknow;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;

import android.app.Application;

public class OKnowApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "2TW0sA2UmiyAGPJ2wXahopIoFtfmLEC7yrY87jJT", "YZTUxADGrPcM9Lokjvrram8KnENewL1bjpQzIPXK");
		ParseFacebookUtils.initialize("800680196670908");
		ParseTwitterUtils.initialize("8pkQT3zlyLTEkhy20ZMkrPnnf", "Tu0zQOru2j2E6QUHhfw6j9DgKxX4CDAB0RQ88B4djJv8ge6ejz");
	}
}
