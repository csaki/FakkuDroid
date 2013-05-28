package com.fakkudroid;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.component.ActionImageButton;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.ExceptionNotLoggedIn;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.fragment.CommentListFragment;
import com.fakkudroid.fragment.DoujinDetailFragment;
import com.fakkudroid.service.DownloadManagerService;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;

public class DoujinActivity extends SherlockFragmentActivity {

	public final static String INTENT_VAR_URL = "INTENT_VAR_URL";
	private FakkuDroidApplication app;
	ViewPager mViewPager;
	DoujinPagerAdapter adapter;
	private View mFormView;
	private View mStatusView;
	private DoujinBean currentBean;
	private ProgressBar progressBar;

	public DoujinBean getCurrentBean() {
		return currentBean;
	}

	SharedPreferences preferenceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.activity_doujin);

		app = (FakkuDroidApplication) getApplication();

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		if(getIntent().getStringExtra(INTENT_VAR_URL)!=null){
			DoujinBean bean = new DoujinBean();
			bean.setUrl(getIntent().getStringExtra(INTENT_VAR_URL));
			app.setCurrent(bean);
		}
		
		currentBean = app.getCurrent();

		adapter = new DoujinPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setAdapter(adapter);
		final ActionImageButton btnComments = (ActionImageButton) findViewById(R.id.btnComments);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 1) {
					if (!adapter.getCommentList().isListCharged()) {
						adapter.getCommentList().refresh();
					}
					btnComments
							.setImageResource(R.drawable.navigation_previous_item);
					btnComments.setContentDescription(getResources().getString(
							R.string.come_back));
				} else {
					btnComments.setImageResource(R.drawable.social_chat);
					btnComments.setContentDescription(getResources().getString(
							R.string.comments));
				}
			}
		});
		setTitle(currentBean.getTitle());
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);

		if (DownloadManagerService.DoujinMap.exists(currentBean)) {
			startThread();
		}
	}

	@SuppressLint("NewApi")
	private void startThread() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new UpdateStatus()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			new UpdateStatus().execute(true);
		}
	}

	@Override
	public File getCacheDir() {
		File file = null;
		String settingDir = preferenceManager.getString("dir_download", "0");
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

	@Override
	public File getDir(String dir, int mode) {
		File file = null;
		String settingDir = preferenceManager.getString("dir_download", "0");
		if (settingDir.equals(Constants.EXTERNAL_STORAGE + "")) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				file = new File(Environment.getExternalStorageDirectory()
						+ Constants.LOCAL_DIRECTORY + "/" + dir);
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
					+ Constants.LOCAL_DIRECTORY + "/" + dir);

		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(currentBean.getUrl()));
		this.startActivity(viewBrowser);
	}

	public void refresh(View view) {
		if (adapter.getCurrentPosition() == 0) {
			adapter.getDoujinDetail().refresh();
		} else {
			adapter.getCommentList().refresh();
		}
	}

	public void download(View view) {
		if (!adapter.getDoujinDetail().isAlreadyDownloaded()) {
			if (DownloadManagerService.started) {
				if (!DownloadManagerService.DoujinMap.exists(app.getCurrent())) {
					Toast.makeText(this,
							getResources().getString(R.string.in_queue),
							Toast.LENGTH_SHORT).show();
					DownloadManagerService.DoujinMap.add(app.getCurrent());
				} else {
					Toast.makeText(this,
							getResources().getString(R.string.already_queue),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				startService(new Intent(this, DownloadManagerService.class));
				startThread();
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.ask_delete)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String folder = currentBean.getId();
									File dir = getDir(folder,
											Context.MODE_PRIVATE);
									try {
										FileUtils.deleteDirectory(dir);
									} catch (IOException e) {
										Log.e(DoujinActivity.class.toString(),
												"Exception", e);
									}
									DataBaseHandler db = new DataBaseHandler(
											DoujinActivity.this);
									db.deleteDoujin(currentBean.getId());

									ImageButton btnDownload = (ImageButton) findViewById(R.id.btnDownload);
									btnDownload
											.setImageResource(R.drawable.av_download);
									btnDownload
											.setContentDescription(getResources()
													.getString(
															R.string.download));
									Toast.makeText(
											DoujinActivity.this,
											getResources().getString(
													R.string.deleted),
											Toast.LENGTH_SHORT).show();
									adapter.getDoujinDetail()
											.setAlreadyDownloaded(false);
								}
							}).setNegativeButton(android.R.string.no, null)
					.create().show();
		}
	}

	public void read(View view) {
		if (preferenceManager.getBoolean("perfect_viewer_checkbox", false)&&adapter.getDoujinDetail().isAlreadyDownloaded()) {
			List<String> lstFiles = app.getCurrent().getImagesFiles();
			File dir = getDir(app.getCurrent().getId(), Context.MODE_PRIVATE);
			File myFile = new File(dir, lstFiles.get(0));
			Util.openPerfectViewer(myFile.getAbsolutePath(), this);
		} else {
			Intent it = new Intent(this, GallerySwipeActivity.class);
			this.startActivity(it);
		}
	}

	public void relatedContent(View view) {
		setResult(1, null);
		finish();
	}

	public void goToList(Intent data) {
		setResult(2, data);
		finish();
	}

	public void goToFavorite(Intent data) {
		setResult(3, data);
		finish();
	}

	@SuppressLint("NewApi")
	public void addOrRemoveFavorite(View view) {

		if (!app.getSettingBean().isChecked()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.login_please)
					.setPositiveButton(R.string.login,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent it = new Intent(DoujinActivity.this,
											LoginActivity.class);
									DoujinActivity.this.startActivity(it);
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
		if (app.getSettingBean().isChecked())
			if (!currentBean.isAddedInFavorite()) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new FavoriteDoujin()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,true);
				} else {
					new FavoriteDoujin().execute(true);
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new FavoriteDoujin()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,false);
				} else {
					new FavoriteDoujin().execute(false);
				}
			}
	}

	public void comments(View view) {
		if (mViewPager.getCurrentItem() == 0)
			mViewPager.setCurrentItem(1);
		else
			mViewPager.setCurrentItem(0);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
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

	class UpdateStatus extends AsyncTask<Boolean, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			try {
				Thread.sleep(1000);
				while (DownloadManagerService.DoujinMap.exists(currentBean)) {
					if (isFinishing()) {
						break;
					}
					if (DownloadManagerService.currentBean != null
							&& DownloadManagerService.currentBean.getId()
									.hashCode() == currentBean.getId()
									.hashCode())
						publishProgress(DownloadManagerService.percent);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(DownloadManagerService.percent);
		}

		protected void onPostExecute(Boolean bytes) {
			if (isFinishing()) {
				return;
			}
			progressBar.setProgress(100);
			ImageButton btnDownload = (ImageButton) findViewById(R.id.btnDownload);
			btnDownload.setImageResource(R.drawable.content_discard);
			btnDownload.setContentDescription(getResources().getString(
					R.string.delete));
			adapter.getDoujinDetail().setAlreadyDownloaded(true);
		}

	}

	class FavoriteDoujin extends AsyncTask<Boolean, Float, Boolean> {

		protected void onPreExecute() {
			showProgress(true);
		}

		protected Boolean doInBackground(Boolean... bool) {
			boolean isConnected = false;
			if (app.getSettingBean().isChecked())
				try {
					isConnected = FakkuConnection.connect(app.getSettingBean()
							.getUser(), app.getSettingBean().getPassword());
				} catch (ClientProtocolException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}

			if (!isConnected) {
				UserBean s = app.getSettingBean();
				s.setChecked(false);
				new DataBaseHandler(DoujinActivity.this).updateSetting(s);
				app.setSettingBean(null);
			} else {
				Boolean b = bool[0];
				try {
					if (b)
						FakkuConnection.transaction(currentBean
								.urlFavorite(Constants.SITEADDFAVORITE));
					else
						FakkuConnection.transaction(currentBean
								.urlFavorite(Constants.SITEREMOVEFAVORITE));
				} catch (ExceptionNotLoggedIn e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}
				currentBean.setAddedInFavorite(b);
			}
			return isConnected;
		}

		protected void onPostExecute(Boolean bytes) {
			showProgress(false);
			if (bytes) {
				adapter.getDoujinDetail().setComponents();
				String text = null;

				if (currentBean.isAddedInFavorite())
					text = getResources().getString(R.string.added_favorite);
				else
					text = getResources().getString(R.string.removed_favorite);
				Toast.makeText(DoujinActivity.this, text, Toast.LENGTH_SHORT)
						.show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DoujinActivity.this);
				builder.setMessage(R.string.login_please)
						.setPositiveButton(R.string.login,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Intent it = new Intent(
												DoujinActivity.this,
												LoginActivity.class);
										DoujinActivity.this.startActivity(it);
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
		}
	}

	class DoujinPagerAdapter extends FragmentPagerAdapter {

		DoujinDetailFragment doujinDetail;
		CommentListFragment commentList;
		int currentPosition = 0;

		public DoujinPagerAdapter(FragmentManager fm) {
			super(fm);

			doujinDetail = new DoujinDetailFragment(DoujinActivity.this);
			commentList = new CommentListFragment(DoujinActivity.this);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int arg0) {
			currentPosition = arg0;
			if (currentPosition == 0) {
				return doujinDetail;
			} else {
				return commentList;
			}
		}

		public DoujinDetailFragment getDoujinDetail() {
			return doujinDetail;
		}

		public int getCurrentPosition() {
			return currentPosition;
		}

		public CommentListFragment getCommentList() {
			return commentList;
		}
	}
}
