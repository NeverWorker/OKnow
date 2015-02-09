package com.neverworker.oknow;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SignupFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;
	
	private TextView usernameView;
	private TextView passwordView;
	private TextView emailView;
	private TextView phoneNumberView;
	
	public SignupFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_signup, container, false);
		
		((ImageView)rootView.findViewById(R.id.signup_exit)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.backFragment();
			}
		});
		
		usernameView = (TextView)rootView.findViewById(R.id.signup_username);
		passwordView = (TextView)rootView.findViewById(R.id.signup_password);
		emailView = (TextView)rootView.findViewById(R.id.signup_email);
		phoneNumberView = (TextView)rootView.findViewById(R.id.signup_phone_number);

		((ImageView)rootView.findViewById(R.id.signup_confirm)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				signUp();
			}
		});
		
		return rootView;
	}
	
	private void signUp() {
		String username = usernameView.getText().toString();
		String password = passwordView.getText().toString();
		String email = emailView.getText().toString();
		String phoneNumber = phoneNumberView.getText().toString();
		
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);		 
		user.put("phone", phoneNumber);
		
		String signingMsg = getResources().getString(R.string.signup_signing_msg);
		final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", signingMsg, true);
		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				dialog.dismiss();
				if (e == null) {
					String msg = getResources().getString(R.string.signup_success);
					Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
					thisActivity.backFragment();
				} else {
					String msg = getResources().getString(R.string.signup_failure);
					Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
}