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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.fakkudroid.adapter.FavoriteListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Util;
import com.fakkudroid.DoujinActivity;
import com.fakkudroid.R;

public class FavoriteFragment extends SherlockFragment implements
		AdapterView.OnItemClickListener {

	/**
	 * constante para identificar la llave con la que env�o datos a trav�s de
	 * intents para comunicar entre las dos actividades: Main y ShowElement
	 */

	public final static String INTENT_VAR_USER = "intentVarUser";

	FakkuDroidApplication app;
	LinkedList<DoujinBean> llDoujin;
	FavoriteListAdapter da;
	GridView gvFavorites;
	static String user;
	static String title;
	static int nroPage = 1;
	private View mFormView;
	private View mStatusView;
	private View view;

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
	public void onStart(){
		super.onStart();
		if(llDoujin==null||llDoujin.isEmpty())
			loadPage();
	}
	
	private View findViewById(int resource){
		return view.findViewById(resource);
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_favorite, container,
				false);
		gvFavorites = (GridView) findViewById(R.id.gvFavorites);
		gvFavorites.setOnItemClickListener(this);

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		return view;
	}

	@SuppressLint("NewApi")
	private void loadPage() {
		title = getResources().getString(R.string.favorite);
		title = title.replace("usr", user);
		
		getActivity().setTitle(app.getTitle(nroPage, title));
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + nroPage);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new DownloadCatalog()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,app.getUrlFavorite(nroPage, user));
		} else {
			new DownloadCatalog().execute(app.getUrlFavorite(nroPage, user));
		}
	}

	public void nextPage(View view) {
		nroPage++;
		loadPage();
		CharSequence text = "Page " + nroPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getActivity(), text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		if (nroPage - 1 == 0) {
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		} else {
			nroPage--;
			loadPage();
			CharSequence text = "Page " + nroPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(getActivity(), text, duration);
			toast.show();
		}
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrlFavorite(nroPage, user)));
		this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		loadPage();
	}

	/**
	 * Funci�n auxiliar que recibe una lista de mapas, y utilizando esta data
	 * crea un adaptador para poblar al ListView del dise�o
	 * */
	private void setData() {
		da = new FavoriteListAdapter(this.getActivity(), R.layout.row_doujin, 0, llDoujin);
		gvFavorites.setAdapter(da);
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
				llDoujin = FakkuConnection.parseHTMLFavorite(urls[0]);
			} catch (ClientProtocolException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			} catch (IOException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			} catch (URISyntaxException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			} catch (Exception e) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e);
			}
			if (llDoujin == null)
				llDoujin = new LinkedList<DoujinBean>();

			for (DoujinBean bean : llDoujin) {
				try {
					File dir = getActivity().getCacheDir();

					File myFile = new File(dir, bean.getFileImageTitle());
					Util.saveInStorage(myFile, bean.getUrlImageTitle());
				} catch (Exception e) {
					Log.e(DownloadCatalog.class.toString(), "Exception", e);
				}
			}

			return llDoujin.size();
		}

		protected void onPostExecute(Integer size) {
			setData();
			showProgress(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		DoujinBean data = llDoujin.get(arg2);
		app.setCurrent(data);
		Intent it = new Intent(getActivity(), DoujinActivity.class);
		this.startActivityForResult(it, 1);
	}

}