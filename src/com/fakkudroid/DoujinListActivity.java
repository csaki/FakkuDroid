package com.fakkudroid;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;

import com.fakkudroid.adapter.DoujinListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;
import com.fakkudroid.R;

import android.util.Log;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DoujinListActivity extends ListActivity {

	/**
	 * constante para identificar la llave con la que env�o datos a trav�s
	 * de intents para comunicar entre las dos actividades: Main y ShowElement
	 */

	public final static String INTENT_VAR_URL = "intentVarUrl";
	public final static String INTENT_VAR_TITLE = "intentVarTitle";

	FakkuDroidApplication app;
	LinkedList<DoujinBean> llDoujin;
	DoujinListAdapter da;
	String url;
	String title;
	int numPage = 1;
	private View mFormView;
	private View mStatusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doujin_list);
		app = (FakkuDroidApplication) getApplication();

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);

		title = getIntent().getStringExtra(INTENT_VAR_TITLE);
		url = getIntent().getStringExtra(INTENT_VAR_URL);

		if (title == null) {
			String s = getResources().getString(R.string.app_name);
			title = s;
		}
		if (url == null) {
			url = Constants.SITEROOT;
		}

		loadPage();
	}

	@Override
	public File getCacheDir() {
		File file = null;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String settingDir = prefs.getString("dir_download", "0");
		if (settingDir.equals(Constants.EXTERNAL_STORAGE + "")) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				file = new File(Environment.getExternalStorageDirectory()
						+ Constants.CACHE_DIRECTORY);
				boolean success = true;
				if (!file.exists()) {
					success = file.mkdirs();
				}

				if (!success)
					file = null;
			}
		}
		if (file == null)
			file = new File(Environment.getRootDirectory()
					+ Constants.CACHE_DIRECTORY);

		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public void nextPage(View view) {
		numPage++;
		loadPage();
		Context context = getApplicationContext();
		CharSequence text = "Page " + numPage;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public void previousPage(View view) {
		if (numPage - 1 == 0) {

			Context context = getApplicationContext();
			CharSequence text = "There aren't more pages.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			numPage--;
			loadPage();
			Context context = getApplicationContext();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getUrl(numPage, url)));
		DoujinListActivity.this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		loadPage();
	}

	private void loadPage() {
		setTitle(app.getTitle(numPage, title));
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		new DownloadCatalog().execute(app.getUrl(numPage, url));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_doujin_list, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isConnected = app.getSettingBean().isChecked();
		if (!isConnected)
			isConnected = FakkuConnection.isConnected();
		if (isConnected) {
			menu.findItem(R.id.menu_login).setVisible(false);
			menu.findItem(R.id.menu_logout).setVisible(true);
			menu.findItem(R.id.menu_myfavorites).setVisible(true);
		} else {
			menu.findItem(R.id.menu_login).setVisible(true);
			menu.findItem(R.id.menu_logout).setVisible(false);
			menu.findItem(R.id.menu_myfavorites).setVisible(false);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			(menu.findItem(R.id.menu_search)).setIcon(R.drawable.action_search);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:
			UserBean sb = app.getSettingBean();
			sb.setChecked(false);
			new DataBaseHandler(this).updateSetting(sb);
			app.setSettingBean(null);
			FakkuConnection.disconnect();
			Toast.makeText(this, getResources().getString(R.string.loggedout),
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_login:
			Intent itLogin = new Intent(this, LoginActivity.class);
			this.startActivity(itLogin);
			break;
		case R.id.menu_downloads:
			Intent itDownloads = new Intent(this, DownloadListActivity.class);
			this.startActivity(itDownloads);
			break;
		case R.id.menu_search:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Get the layout inflater
			LayoutInflater inflater = getLayoutInflater();

			final View view = inflater.inflate(R.layout.dialog_search, null);
			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			builder.setView(view)
					// Add action buttons
					.setPositiveButton(R.string.search,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									EditText etSearch = (EditText) view
											.findViewById(R.id.etSearch);
									String query = etSearch.getText()
											.toString();

									String strSearch = getResources()
											.getString(R.string.search);

									Intent it = new Intent(
											DoujinListActivity.this,
											DoujinListActivity.class);
									it.putExtra(
											INTENT_VAR_URL,
											Constants.SITESEARCH
													+ Util.escapeURL(query
															.trim()));
									it.putExtra(INTENT_VAR_TITLE, strSearch
											+ ": " + query.trim());
									DoujinListActivity.this.startActivity(it);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
								}
							}).setTitle(R.string.search).create().show();
			break;
		case R.id.menu_doujinshis:
			Intent it = new Intent(DoujinListActivity.this, MenuActivity.class);
			it.putExtra(MenuActivity.INTENT_VAR_URL, Constants.SITEDOUJINSHI);
			DoujinListActivity.this.startActivity(it);
			break;
		case R.id.menu_mangas:
			Intent it2 = new Intent(DoujinListActivity.this, MenuActivity.class);
			it2.putExtra(MenuActivity.INTENT_VAR_URL, Constants.SITEMANGA);
			DoujinListActivity.this.startActivity(it2);
			break;
		case R.id.menu_about:
			Intent itAbout = new Intent(Intent.ACTION_VIEW);
			itAbout.setData(Uri.parse(Constants.SITEABOUT));
			DoujinListActivity.this.startActivity(itAbout);
			break;
		case R.id.menu_settings:
			Intent itSettings = new Intent(DoujinListActivity.this,
					SettingsActivity.class);
			DoujinListActivity.this.startActivity(itSettings);
			break;
		case R.id.menu_check_for_updates:
			Intent it3 = new Intent(Intent.ACTION_VIEW);
			it3.setData(Uri.parse(Constants.SITEDOWNLOAD));

			PackageInfo pInfo = null;
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			} catch (NameNotFoundException e) {
				Log.e(DoujinListActivity.class.toString(),
						"onOptionsItemSelected", e);
			}
			String version = pInfo.versionName;

			DoujinListActivity.this.startActivity(it3);
			Context context = getApplicationContext();
			CharSequence text = "Your current version is " + version;
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();

			break;
		case R.id.menu_myfavorites:
			if (!app.getSettingBean().isChecked()) {
				AlertDialog.Builder builderLogin = new AlertDialog.Builder(this);
				builderLogin
						.setMessage(R.string.login_please)
						.setPositiveButton(R.string.login,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Intent it = new Intent(
												DoujinListActivity.this,
												LoginActivity.class);
										DoujinListActivity.this
												.startActivity(it);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								}).create().show();
			}
			app.setSettingBean(null);
			if (app.getSettingBean().isChecked()) {
				Intent itFavorites = new Intent(DoujinListActivity.this,
						FavoriteActivity.class);
				itFavorites.putExtra(FavoriteActivity.INTENT_VAR_USER, app
						.getSettingBean().getUser());
				DoujinListActivity.this.startActivity(itFavorites);
			}
			break;
		}
		return true;
	}

	/**
	 * Funci�n auxiliar que recibe una lista de mapas, y utilizando esta data
	 * crea un adaptador para poblar al ListView del dise�o
	 * */
	private void setData() {
		da = new DoujinListAdapter(this, R.layout.row_doujin, 0, llDoujin);
		this.setListAdapter(da);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		DoujinBean data = llDoujin.get(position);
		app.setCurrent(data);
		Intent it = new Intent(this, DoujinActivity.class);
		this.startActivity(it);
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
			} catch (ClientProtocolException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			} catch (IOException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			} catch (URISyntaxException e1) {
				Log.e(DownloadCatalog.class.toString(), "Exception", e1);
			}
			if (llDoujin == null)
				llDoujin = new LinkedList<DoujinBean>();
			for (DoujinBean bean : llDoujin) {
				try {
					File dir = getCacheDir();

					File myFile = new File(dir, bean.getFileImageTitle());
					Util.saveInStorage(myFile, bean.getUrlImageTitle());

					myFile = new File(dir, bean.getFileImagePage());
					Util.saveInStorage(myFile, bean.getUrlImagePage());
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

}
