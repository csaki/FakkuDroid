package com.fakkudroid.fragment;

import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.fakkudroid.DoujinActivity;
import com.fakkudroid.DoujinListActivity;
import com.fakkudroid.LoginActivity;
import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.SettingsActivity;
import com.fakkudroid.adapter.MenuListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

public class MenuListFragment extends SherlockListFragment {

	private FakkuDroidApplication app;
	private MainActivity mainActivity;
	private int level = 1;
	private LinkedList<URLBean> lstURL;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_menu, container, false);
		createMainMenu();
		return view;
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
				R.layout.row_url, 0, lstURL, true));
	}

	private void createBrowseManga() {
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
				R.layout.row_url, 0, lstURL, false));
	}

	private void createBrowseDoujin() {
		level = 2;
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
				R.layout.row_url, 0, lstURL, false));
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		if (level == 1) {
			if(lstURL.get(position).getDescription().equals("Manga")){
				createBrowseManga();
			}else if(lstURL.get(position).getDescription().equals("Doujinshi")){
				createBrowseDoujin();
			}else if(position==0){
				mainActivity.loadPageDoujinList(getResources().getString(R.string.app_name), com.fakkudroid.util.Constants.SITEROOT);
			}else if(lstURL.get(position).getDescription().equals("Downloads")){
				mainActivity.goToDownload();
			}else if(lstURL.get(position).getDescription().startsWith("Sign")){
				Intent itLogin = new Intent(this.getActivity(), LoginActivity.class);
				this.startActivityForResult(itLogin, 1);
			}else if(lstURL.get(position).getDescription().equals("Settings")){
				Intent itSettings = new Intent(this.getActivity(), SettingsActivity.class);
				getActivity().startActivity(itSettings);
			}else if(lstURL.get(position).getDescription().equals("Logout")){
				UserBean sb = app.getSettingBean();
				sb.setChecked(false);
				new DataBaseHandler(getActivity()).updateSetting(sb);
				app.setSettingBean(null);
				FakkuConnection.disconnect();
				Toast.makeText(getActivity(), getResources().getString(R.string.loggedout),
						Toast.LENGTH_SHORT).show();
				createMainMenu();
			}else if(lstURL.get(position).getDescription().startsWith("Check")){
				Intent it3 = new Intent(Intent.ACTION_VIEW);
				it3.setData(Uri.parse(Constants.SITEDOWNLOAD));

				PackageInfo pInfo = null;
				try {
					pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
				} catch (NameNotFoundException e) {
					Log.e(DoujinListActivity.class.toString(),
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
			if(lstURL.get(position).getUrl()==null||lstURL.get(position).getUrl().equals("")){
				createMainMenu();
			}else if(lstURL.get(position).getDescription().startsWith("Sort")){
				
			}else{
				if(position==lstURL.size()-1){
					Intent it = new Intent(mainActivity,
							DoujinActivity.class);
					DoujinBean bean = new DoujinBean();
					bean.setUrl(lstURL.get(position).getUrl());
					app.setCurrent(bean);		
					mainActivity.startActivity(it);
				}else{
					mainActivity.loadPageDoujinList(lstURL.get(position).getDescription(), lstURL.get(position).getUrl());
				}
			}
		}
	}
}
