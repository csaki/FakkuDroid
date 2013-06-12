package com.fakkudroid.fragment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.adapter.DoujinListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Helper;

public class DoujinListFragment extends SherlockListFragment {

	FakkuDroidApplication app;
	LinkedList<DoujinBean> llDoujin;
	DoujinListAdapter da;
	static String url = Constants.SITEROOT;;
	static int numPage = 1;
	private View mFormView;
	private View mStatusView;
	private View view;
	static boolean related = false;
	private MainActivity mainActivity;
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (FakkuDroidApplication) getActivity().getApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadPage();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater
				.inflate(R.layout.fragment_doujin_list, container, false);

		mFormView = view.findViewById(R.id.view_form);
		mStatusView = view.findViewById(R.id.view_status);
		return view;
	}

	public void nextPage(View view) {
		numPage++;
		loadPage();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this.getActivity(), text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		if (numPage - 1 == 0) {
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		} else {
			numPage--;
			loadPage();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this.getActivity(), text, duration);
			toast.show();
		}
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrl(numPage, url)));
		this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		loadPage();
	}

	public void setRelated(boolean related) {
		numPage = 1;
		DoujinListFragment.related = related;
	}

	@SuppressLint("NewApi")
	public void loadPage() {
		TextView tvPage = (TextView) view.findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		if (related)
			Helper.executeAsyncTask(new DownloadCatalog(), app.getCurrent()
								.urlRelated(numPage));
		else 
			Helper.executeAsyncTask(new DownloadCatalog(), app.getUrl(numPage, url));
	}

	public void setUrl(String url) {
		related = false;
		DoujinListFragment.numPage = 1;
		DoujinListFragment.url = url;
	}

	private void setData() {
		da = new DoujinListAdapter(this.getActivity(), R.layout.row_doujin, 0,
				llDoujin, related);
		this.setListAdapter(da);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		DoujinBean data = llDoujin.get(position);
		app.setCurrent(data);
		mainActivity.goToDoujin();
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

	class DownloadCatalog extends AsyncTask<String, Float, Integer> {

		protected void onPreExecute() {
			showProgress(true);
		}

		protected Integer doInBackground(String... urls) {

			try {
				Log.i(DownloadCatalog.class.toString(), "URL Catalog: "
						+ urls[0]);
				llDoujin = FakkuConnection.parseHTMLCatalog(urls[0]);
			} catch (ClientProtocolException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (IOException e) {
				Helper.logError(this, e.getMessage(), e);
			} catch (URISyntaxException e) {
				Helper.logError(this, e.getMessage(), e);
			}
			if (llDoujin == null)
				llDoujin = new LinkedList<DoujinBean>();
			if (related)
				llDoujin.add(0, app.getCurrent());
			for (DoujinBean bean : llDoujin) {
				if(DoujinListFragment.this.getActivity()!=null){
					try {
						File dir = Helper.getCacheDir(getActivity());

						File myFile = new File(dir, bean.getFileImageTitle());
						Helper.saveInStorage(myFile, bean.getUrlImageTitle());

						myFile = new File(dir, bean.getFileImagePage());
						Helper.saveInStorage(myFile, bean.getUrlImagePage());
						
						bean.loadImages(dir);
					} catch (Exception e) {
						Helper.logError(this, e.getMessage(), e);
					}
				}
			}
			return llDoujin.size();
		}

		protected void onPostExecute(Integer size) {
			if(DoujinListFragment.this.getActivity()!=null){
				setData();
				showProgress(false);	
			}
		}
	}

}
