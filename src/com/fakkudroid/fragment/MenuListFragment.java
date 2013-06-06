package com.fakkudroid.fragment;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.fakkudroid.DoujinActivity;
import com.fakkudroid.LoginActivity;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.SettingsActivity;
import com.fakkudroid.adapter.MenuListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.component.ActionImageButton;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

public class MenuListFragment extends SherlockListFragment {

	private FakkuDroidApplication app;
	private MainActivity mainActivity;
	private int level = 1;
	private LinkedList<URLBean> lstURL;
	private View mFormView;
	private View mStatusView;
	private View ll;
	private View view;
	int nroPage = 1;
	int currentList = 0;
	int typeView;
	private String url;
	public final static int BROWSER_MANGA = 0;
	public final static int BROWSER_DOUJIN = 0;
	public final static int TYPE_LIST_TAGS = 0;
	public final static int TYPE_LIST_SERIES = 1;

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	private View findViewById(int resource){
		return view.findViewById(resource);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_menu, container, false);
		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		ll = findViewById(R.id.ll);
		ActionImageButton btnPreviousPage = (ActionImageButton) findViewById(R.id.btnPreviousPage);
		ActionImageButton btnNextPage = (ActionImageButton) findViewById(R.id.btnNextPage);
		
		btnPreviousPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				previousPage();
			}
		});
		
		btnNextPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				nextPage();
			}
		});
		
		createMainMenu();
		return view;
	}
	
	public void nextPage(){
		nroPage++;
		loadPage();
		CharSequence text = "Page " + nroPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(getActivity(), text, duration);
		toast.show();
	}
	
	@SuppressLint("NewApi")
	private void loadPage(){
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + nroPage);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new DownloadList().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, typeView);
		}else{
			new DownloadList().execute(typeView);
		}
	}
	
	public void previousPage(){
		if (nroPage-1 == 0) {
			
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

	public void createMainMenu() {
		level = 1;
		
		boolean connected = app.getSettingBean().isChecked();
		if (!connected)
			connected = FakkuConnection.isConnected();

		String[] lstMainMenu = getActivity().getResources().getStringArray(
				R.array.main_menu);
		int[] lstIcons = new int[] { R.drawable.home,
				R.drawable.navigation_forward, R.drawable.rating_important,
				R.drawable.navigation_back, -1, R.drawable.av_play,
				R.drawable.av_play, R.drawable.device_access_sd_storage,
				R.drawable.av_upload, R.drawable.action_settings };
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstMainMenu.length; i++) {
			URLBean bean = new URLBean(lstMainMenu[i]);
			bean.setIcon(lstIcons[i]);
			lstURL.add(bean);
		}
		if (connected) {
			lstURL.remove(1);
		} else {
			lstURL.remove(3);
			lstURL.remove(2);
		}
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, true));
	}

	private void createBrowseManga() {
		ll.setVisibility(View.GONE);
		currentList = BROWSER_MANGA;
		level = 2;
		String[] lstBrowseManga = getActivity().getResources().getStringArray(
				R.array.browse_manga);
		String[] lstURLBrowseManga = getActivity().getResources()
				.getStringArray(R.array.url_browse_manga);
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstBrowseManga.length; i++) {
			URLBean bean = new URLBean(lstURLBrowseManga[i], lstBrowseManga[i]);			
			lstURL.add(bean);
		}
		lstURL.get(0).setIcon(R.drawable.content_undo);
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, false));
	}

	private void createBrowseDoujin() {
		ll.setVisibility(View.GONE);
		level = 2;
		currentList = BROWSER_DOUJIN;
		String[] lstBrowseDoujin = getActivity().getResources().getStringArray(
				R.array.browse_doujinshi);
		String[] lstURLBrowseDoujin = getActivity().getResources()
				.getStringArray(R.array.url_browse_doujinshis);
		lstURL = new LinkedList<URLBean>();

		for (int i = 0; i < lstBrowseDoujin.length; i++) {
			URLBean bean = new URLBean(lstURLBrowseDoujin[i],
					lstBrowseDoujin[i]);
			lstURL.add(bean);
		}
		lstURL.get(0).setIcon(R.drawable.content_undo);
		this.setListAdapter(new MenuListAdapter(this.getActivity(),
				R.layout.row_menu, 0, lstURL, false));
	}

	@SuppressLint("NewApi")
	public void onListItemClick(ListView l, View v, int position, long id) {
		URLBean bean = lstURL.get(position);
		if (level == 1) {
			if(bean.getDescription().equals("Manga")){
				createBrowseManga();
			}else if(bean.getDescription().equals("Doujinshi")){
				createBrowseDoujin();
			}else if(position==0){
				mainActivity.loadPageDoujinList(getResources().getString(R.string.app_name), com.fakkudroid.util.Constants.SITEROOT);
			}else if(bean.getDescription().equals("Downloads")){
				mainActivity.goToDownloads();
			}else if(bean.getDescription().equals("My favorites")){
				mainActivity.goToFavorites(app.getSettingBean().getUser());
			}else if(bean.getDescription().startsWith("Sign")){
				Intent itLogin = new Intent(this.getActivity(), LoginActivity.class);
				this.startActivityForResult(itLogin, 2);
			}else if(bean.getDescription().equals("Settings")){
				Intent itSettings = new Intent(this.getActivity(), SettingsActivity.class);
				getActivity().startActivity(itSettings);
			}else if(bean.getDescription().equals("Logout")){
				UserBean sb = app.getSettingBean();
				sb.setChecked(false);
				new DataBaseHandler(getActivity()).updateSetting(sb);
				app.setSettingBean(null);
				FakkuConnection.disconnect();
				Toast.makeText(getActivity(), getResources().getString(R.string.loggedout),
						Toast.LENGTH_SHORT).show();
				createMainMenu();
			}else if(bean.getDescription().startsWith("Check")){
				Intent it3 = new Intent(Intent.ACTION_VIEW);
				it3.setData(Uri.parse(Constants.SITEDOWNLOAD));

				PackageInfo pInfo = null;
				try {
					pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
				} catch (NameNotFoundException e) {
					Log.e(MenuListFragment.class.toString(),
							"onOptionsItemSelected", e);
				}
				String version = pInfo.versionName;

				getActivity().startActivity(it3);
				CharSequence text = "Your current version is " + version;
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(getActivity(), text, duration);
				toast.show();
			}
		} else if (level == 2) {
			if(bean.getUrl()==null||bean.getUrl().equals("")){
				createMainMenu();
			}else if(bean.getDescription().endsWith("Tags")||bean.getDescription().endsWith("Artist")||bean.getDescription().endsWith("Series")){
				url = bean.getUrl();
				nroPage = 1;
				if(bean.getDescription().contains("Tags")){
					typeView = TYPE_LIST_TAGS;
				}else{
					typeView = TYPE_LIST_SERIES;
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new DownloadList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, typeView);
				}else{
					new DownloadList().execute(typeView);
				}
			}else{
				if(position==lstURL.size()-1){
					Intent it = new Intent(mainActivity,
							DoujinActivity.class);
					DoujinBean dBean = new DoujinBean();
					dBean.setUrl(bean.getUrl());
					app.setCurrent(dBean);		
					mainActivity.startActivity(it);
				}else{
					mainActivity.loadPageDoujinList(bean.getDescription(), bean.getUrl());
				}
			}
		}else if (level == 3) {
			if(bean.getUrl()==null){
				if(currentList==BROWSER_DOUJIN){
					createBrowseDoujin();
				}else{
					createBrowseManga();
				}
			}else{
				mainActivity.loadPageDoujinList(bean.getDescription(), bean.getUrl());
			}
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
	
	private void setData(){
		level = 3;
		URLBean bean = new URLBean();
		bean.setDescription(getResources().getString(R.string.back));
		bean.setUrl(null);
		bean.setIcon(R.drawable.content_undo);
		lstURL.add(0, bean);
		this.setListAdapter(new MenuListAdapter(getActivity(), R.layout.row_menu, 0, lstURL, false));
	}
	
	class DownloadList extends AsyncTask<Integer, Float, Integer> {

		protected void onPreExecute() {
			showProgress(true);
		}
		
		protected Integer doInBackground(Integer... type) {
			
			try {
				Log.i(DownloadList.class.toString(), "URL List: " + app.getUrl(nroPage, url));
				if(typeView==TYPE_LIST_SERIES)
					lstURL = FakkuConnection.parseHTMLSeriesList(app.getUrl(nroPage, url));
				else
					lstURL = FakkuConnection.parseHTMLTagsList(app.getUrl(nroPage, url));				
			} catch (ClientProtocolException e1) {
				Log.e(DownloadList.class.toString(), "Exception", e1);
			} catch (IOException e1) {
				Log.e(DownloadList.class.toString(), "Exception", e1);
			} 
			if(lstURL==null)
				lstURL = new LinkedList<URLBean>();
			return lstURL.size();
		}

		protected void onPostExecute(Integer bytes) {
			ll.setVisibility(View.VISIBLE);
			showProgress(false);
			setData();
		}
	}
}
