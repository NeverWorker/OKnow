package com.neverworker.oknow;

import com.parse.ParseUser;

import com.neverworker.oknow.widget.HorizontalSelector;
import com.neverworker.oknow.widget.HorizontalSelector.OnSelectChangedListener;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PostFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;
	private TextView categoryTitle;
	private EditText postContent;
	
	private String[] postCategories;
	
	public PostFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		thisActivity = (MainActivity) this.getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_post, container, false);

		TextView cancelButton = (TextView) rootView.findViewById(R.id.post_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.backFragment();
			}
			
		});

		TextView postButton = (TextView) rootView.findViewById(R.id.post_confirm);
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				post();
			}
			
		});
		
		categoryTitle = (TextView) rootView.findViewById(R.id.post_category_title);

		postCategories = getResources().getStringArray(R.array.post_categories);
		HorizontalSelector categorySelecotr = (HorizontalSelector) rootView.findViewById(R.id.post_category_selector);
		categorySelecotr.setAdapter(
			new ArrayAdapter<String>(thisActivity, R.layout.post_selector_item, postCategories)
		);
		categorySelecotr.addOnSelectChangedListeners(new OnSelectChangedListener() {
			@Override
			public void OnChanged(int index) {
				changeSelectCategory(index);
			}
		});

		postContent = (EditText) rootView.findViewById(R.id.post_content);
		
		if (ParseUser.getCurrentUser() == null)
			thisActivity.login();
		
		return rootView;
	}
	
	private void changeSelectCategory(int index) {
		categoryTitle.setText(postCategories[index]);		
	}
	
	private void post() {
		String postingMsg = getResources().getString(R.string.post_posting);
		final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", postingMsg, true);
		new PostingTask() {
			protected void onPostExecute(Boolean result) {
				dialog.dismiss();
				String postingResult = "";
				if (result) 
					postingResult = getResources().getString(R.string.post_success);
				else
					postingResult = getResources().getString(R.string.post_failure);
				Toast.makeText(thisActivity, postingResult, Toast.LENGTH_LONG).show();
				thisActivity.main();
			}
		}.execute(categoryTitle.getText().toString(), postContent.getText().toString());
	}
	
	private class PostingTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			return thisActivity.getKnowManager().postKnow(params[0], params[1]);
		}
	}			
}
