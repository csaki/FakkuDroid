package com.fakkudroid;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import com.fakkudroid.adapter.URLListAdapter;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListOptionActivity extends ListActivity{

	FakkuDroidApplication app;
	LinkedList<URLBean> list;
	
	public final static String INTENT_VAR_TYPE_LIST = "intentVarTypeList";
	public final static String INTENT_VAR_URL = "intentVarUrl";
	public final static String INTENT_VAR_TITLE = "intentVarTitle";
	public final static int INTENT_VAR_TYPE_LIST_TAGS = 0;
	public final static int INTENT_VAR_TYPE_LIST_SERIES = 1;
	private View mFormView;
	private View mStatusView;
	
	String url, title;
	int nroPage, typeView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_option);

		app = (FakkuDroidApplication) getApplication();

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		
		nroPage = 1;
		typeView = getIntent().getIntExtra(INTENT_VAR_TYPE_LIST, -1);
		
		url = getIntent().getStringExtra(INTENT_VAR_URL);
		title = getIntent().getStringExtra(INTENT_VAR_TITLE);
		
		loadPage();
	}
	
	public void nextPage(View view){
		nroPage++;
		loadPage();
		Context context = getApplicationContext();
		CharSequence text = "Page " + nroPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public void previousPage(View view){
		if (nroPage-1 == 0) {
			
			Context context = getApplicationContext();
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			nroPage--;
			loadPage();
			Context context = getApplicationContext();
			CharSequence text = "Page " + nroPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
	
	public void viewInBrowser(View view){
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrl(nroPage, url)));
		this.startActivity(viewBrowser);
	}
	
	public void refresh(View view){
		loadPage();
	}

	
	private void loadPage(){
		setTitle(app.getTitle(nroPage, title));
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + nroPage);
		new DownloadList().execute(typeView);
	}
	
	private void setData() {
		this.setListAdapter(new URLListAdapter(this, R.layout.row_url, 0, list));
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		URLBean urlBean = list.get(position);
		
		String url = urlBean.getUrl();
		String title = urlBean.getDescription();
		
		Intent it = new Intent(ListOptionActivity.this,
				DoujinListActivity.class);
		it.putExtra(DoujinListActivity.INTENT_VAR_TITLE, title);
		it.putExtra(DoujinListActivity.INTENT_VAR_URL, url);
		startActivity(it);
	}
	
	class DownloadList extends AsyncTask<Integer, Float, Integer> {

		protected void onPreExecute() {
			showProgress(true);
		}
		
		protected Integer doInBackground(Integer... type) {
			
			try {
				Log.i(DownloadList.class.toString(), "URL List: " + app.getUrl(nroPage, url));
				if(typeView==INTENT_VAR_TYPE_LIST_SERIES)
					list = FakkuConnection.parseHTMLSeriesList(app.getUrl(nroPage, url));
				else
					list = FakkuConnection.parseHTMLTagsList(app.getUrl(nroPage, url));				
			} catch (ClientProtocolException e1) {
				Log.e(DownloadList.class.toString(), "Exception", e1);
			} catch (IOException e1) {
				Log.e(DownloadList.class.toString(), "Exception", e1);
			} 
			
			return list.size();
		}

		protected void onPostExecute(Integer bytes) {
			showProgress(false);
			setData();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mFormView.setVisibility(View.VISIBLE);
			mFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
