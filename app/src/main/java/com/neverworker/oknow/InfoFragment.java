package com.neverworker.oknow;

import java.util.HashMap;

import com.parse.ParseUser;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InfoFragment extends Fragment {
	private MainActivity thisActivity;
	private View rootView;

	private com.neverworker.oknow.widget.RoundedImageView pictureView;
	private TextView usernameView;
	private TextView pointsView;
	private TextView levelView;
	private TextView postAmountView;
	private TextView popularityUsedView;
	private TextView popularityGotView;

	public InfoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		thisActivity = (MainActivity) getActivity();
		if (rootView == null)
			rootView = inflater.inflate(R.layout.fragment_info, container, false);
		
		pictureView = (com.neverworker.oknow.widget.RoundedImageView)rootView.findViewById(R.id.info_picture);
		usernameView = (TextView)rootView.findViewById(R.id.info_username);
		pointsView = (TextView)rootView.findViewById(R.id.info_points);
		levelView = (TextView)rootView.findViewById(R.id.info_level);
		postAmountView = (TextView)rootView.findViewById(R.id.info_posts);
		popularityUsedView = (TextView)rootView.findViewById(R.id.info_popularity_used);
		popularityGotView = (TextView)rootView.findViewById(R.id.info_popularity_got);
		
		((LinearLayout)rootView.findViewById(R.id.info_post_records)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thisActivity.history();
			}
			
		});
		
		if (ParseUser.getCurrentUser() == null)
			thisActivity.login();
		else {
			String showMessage = getResources().getString(R.string.info_fetch_data_msg);
			final ProgressDialog dialog = ProgressDialog.show(thisActivity, "", showMessage, true);
			new PersonalFetchTask() {
				protected void onPostExecute(HashMap<String, Object> result) {
					dialog.dismiss();
					if (result != null && result.size() >= 4) {
						updateInfo(((Number)result.get("popularityCount")).intValue(),
								   ((Number)result.get("ownPopularityCount")).intValue(),
								   ((Number)result.get("totalKnowCount")).intValue(),
								   result.get("username").toString(),
								   ((Bitmap)result.get("personalImage"))
								   );
					} else {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(thisActivity);
						dialogBuilder.setMessage(getResources().getString(R.string.info_fetch_data_failure));
						dialogBuilder.setCancelable(false);
						dialogBuilder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								thisActivity.backFragment();
							}
						});
						dialogBuilder.create().show();
					}
				}
			}.execute();
		}
		
		return rootView;
	}
	
	private void updateInfo(int popularityCount, int ownPopularityCount, int totalKnowCount, String username, Bitmap image) {
		int points = totalKnowCount* 5 + popularityCount + ownPopularityCount*3;
		int level = points/100 + 1;
		pointsView.setText(getResources().getString(R.string.info_score, points));
		levelView.setText(getResources().getString(R.string.info_level, level));
		postAmountView.setText(getResources().getString(R.string.info_posts, totalKnowCount));
		popularityUsedView.setText(getResources().getString(R.string.info_popularity_use, popularityCount));
		popularityGotView.setText(getResources().getString(R.string.info_popularity_get, ownPopularityCount));
		
		usernameView.setText(username);
		if (image == null)
			pictureView.setImageResource(R.drawable.post_unknownuser);
		else
			pictureView.setImageBitmap(image);
	}
	
	public class PersonalFetchTask extends AsyncTask<Void, Void, HashMap<String, Object>> {
		@Override
		protected HashMap<String, Object> doInBackground(Void... params) {
			return thisActivity.getKnowManager().getPersonal();
		}
	}
}
