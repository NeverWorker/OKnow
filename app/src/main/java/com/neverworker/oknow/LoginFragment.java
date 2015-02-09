package com.neverworker.oknow;

import java.util.Arrays;
import java.util.List;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;
	
	private EditText usernameView;
	private EditText passwordView;
	
	public LoginFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_login, container, false);
		
		usernameView = (EditText)rootView.findViewById(R.id.login_username);
		
		passwordView = (EditText)rootView.findViewById(R.id.login_password);
		passwordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
			            actionId == EditorInfo.IME_ACTION_DONE ||
			            event.getAction() == KeyEvent.ACTION_DOWN &&
			            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			        if (event == null || !event.isShiftPressed()) {
			        	parseLogin();
			        	return true;
			        }                
			    }
			    return false;
			}
		});
		
		ImageView loginFacebook = (ImageView) rootView.findViewById(R.id.login_facebook);
		loginFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				facebookLogin();
			}
			
		});
		
		ImageView loginTwitter = (ImageView) rootView.findViewById(R.id.login_twitter);
		loginTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				twitterLogin();
			}
			
		});
		
		ImageView signupView = (ImageView) rootView.findViewById(R.id.login_signup);
		signupView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.signup();
			}
			
		});
		
		if (ParseUser.getCurrentUser() != null) {
			loginSuccess();
		}
		return rootView;
	}
	
	private void parseLogin() {
		String username = usernameView.getText().toString();
		String password = passwordView.getText().toString();
		
		String loggingText = getResources().getString(R.string.login_logging_msg);
		final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", loggingText, true);
		ParseUser.logInInBackground(username, password, new LogInCallback() {
			public void done(ParseUser user, ParseException e) {
	        	dialog.dismiss();
	        	if (user != null) {
					loginSuccess();
				} else {
					String failureText = getResources().getString(R.string.login_failure);
					Toast.makeText(thisActivity, failureText + e, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private void facebookLogin() {
		String loggingText = getResources().getString(R.string.login_logging_msg);
		final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", loggingText, true);
		List<String> permissions = Arrays.asList("public_profile", "user_about_me");
	    ParseFacebookUtils.logIn(permissions, thisActivity, new LogInCallback() {
	        @Override
	        public void done(ParseUser user, ParseException err) {
	        	dialog.dismiss();
	            if (user == null) {
					String failureText = getResources().getString(R.string.login_failure);
					Toast.makeText(thisActivity, failureText + err, Toast.LENGTH_LONG).show();
	            } else if (user.isNew()) {
	            	//android.util.Log.d("", "User signed up and logged in through Facebook!");
	            	thisActivity.getKnowManager().makeFacebookProfile();
					loginSuccess();
	            } else {
	            	//android.util.Log.d("", "User logged in through Facebook!");
	            	thisActivity.getKnowManager().makeFacebookProfile();
					loginSuccess();
	            }
	        }
	    });
	}

	private void twitterLogin() {
		String loggingText = getResources().getString(R.string.login_logging_msg);
		final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", loggingText, true);
		ParseTwitterUtils.logIn(thisActivity, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException err) {
	        	dialog.dismiss();
				if (user == null) {
					String failureText = getResources().getString(R.string.login_failure);
					Toast.makeText(thisActivity, failureText + err, Toast.LENGTH_LONG).show();
				} else if (user.isNew()) {
					//android.util.Log.d("MyApp", "User signed up and logged in through Twitter!");
					loginSuccess();
				} else {
					//android.util.Log.d("MyApp", "User logged in through Twitter!");
					loginSuccess();
				}
			}
		});
	}
	
	private void loginSuccess() {
    	thisActivity.backFragment();
	}
}