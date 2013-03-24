package com.fakkudroid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fakkudroid.bean.SettingBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.ExceptionNotLoggedIn;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.fragment.CommentListFragment;
import com.fakkudroid.fragment.DoujinDetailFragment;
import com.fakkudroid.util.ActionImageButton;
import com.fakkudroid.util.Constants;

public class DoujinActivity extends FragmentActivity {

	private FakkuDroidApplication app;
	ViewPager mViewPager;
	DoujinPagerAdapter adapter;
	private View mFormView;
	private View mStatusView;
	boolean alreadyDownloaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doujin);

		app = (FakkuDroidApplication) getApplication();

		mFormView = findViewById(R.id.view_form);
		mStatusView = findViewById(R.id.view_status);
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
		alreadyDownloaded = isAlreadyDownloaded();
		if(alreadyDownloaded){
			ImageButton btnDownload = (ImageButton)findViewById(R.id.btnDownload);
			btnDownload.setImageResource(R.drawable.content_discard);
			btnDownload.setContentDescription(getResources().getString(R.string.delete));
		}
			
	}

	private boolean isAlreadyDownloaded() {
		List<String> lstFiles = app.getCurrent().getImagesFiles();
		String folder = app.getCurrent().getId();
		for (int i = 0; i < lstFiles.size(); i++) {
			File dir = getDir(folder, Context.MODE_PRIVATE);
			File myFile = new File(dir, lstFiles.get(i));
			if (!myFile.exists()) {
				return false;
			}
		}
		return true;
	}

	public void viewInBrowser(View view) {
		Intent viewBrowser = new Intent(Intent.ACTION_VIEW);
		viewBrowser.setData(Uri.parse(app.getCurrent().getUrl()));
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
		if(!alreadyDownloaded)
			new DownloadDoujin().execute("");
		else{
			List<String> lstFiles = app.getCurrent().getImagesFiles();
			String folder = app.getCurrent().getId();
			for (int i = 0; i < lstFiles.size(); i++) {
				File dir = getDir(folder, Context.MODE_PRIVATE);
				File myFile = new File(dir, lstFiles.get(i));
				if (myFile.exists()) {
					myFile.delete();
				}
			}
			ImageButton btnDownload = (ImageButton)findViewById(R.id.btnDownload);
			btnDownload.setImageResource(R.drawable.av_download);
			btnDownload.setContentDescription(getResources().getString(R.string.download));
			Toast.makeText(this, getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
			alreadyDownloaded = false;
		}
	}

	public void read(View view) {
		Intent it = new Intent(this, GallerySwipeActivity.class);
		this.startActivity(it);
	}

	public void relatedContent(View view) {
		Intent it = new Intent(this, RelatedContentListActivity.class);
		this.startActivity(it);
	}

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
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									return;
								}
							}).create().show();
		}
		app.setSettingBean(null);
		if (app.getSettingBean().isChecked())
			if (!app.getCurrent().isAddedInFavorite()) {
				new FavoriteDoujin().execute(true);
			} else {
				new FavoriteDoujin().execute(false);
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
				SettingBean s = app.getSettingBean();
				s.setChecked(false);
				new DataBaseHandler(DoujinActivity.this).updateSetting(s);
				app.setSettingBean(null);
			} else {
				Boolean b = bool[0];
				try {
					if (b)
						FakkuConnection.transaction(app.getCurrent()
								.urlFavorite(Constants.SITEADDFAVORITE));
					else
						FakkuConnection.transaction(app.getCurrent()
								.urlFavorite(Constants.SITEREMOVEFAVORITE));
				} catch (ExceptionNotLoggedIn e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getLocalizedMessage(),
							e);
				}
				app.getCurrent().setAddedInFavorite(b);
			}
			return isConnected;
		}

		protected void onPostExecute(Boolean bytes) {
			showProgress(false);
			if (bytes) {
				adapter.getDoujinDetail().setComponents();
				String text = null;

				if (app.getCurrent().isAddedInFavorite())
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
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								}).create().show();
			}
		}
	}

	class DownloadDoujin extends AsyncTask<String, Integer, String> {

		ProgressDialog dialog;

		protected void onPreExecute() {
			dialog = new ProgressDialog(DoujinActivity.this);
			dialog.setMax(app.getCurrent().getQtyPages());
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.setTitle(R.string.download);
			dialog.setIndeterminate(false);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			List<String> lstUrls = app.getCurrent().getImages();
			List<String> lstFiles = app.getCurrent().getImagesFiles();
			String folder = app.getCurrent().getId();
			for (int i = 0; i < lstUrls.size(); i++) {
				try {

					File dir = getDir(folder, Context.MODE_PRIVATE);
					File myFile = new File(dir, lstFiles.get(i));
					if (!myFile.exists()) {
						URL url = new URL(lstUrls.get(i));
						URLConnection connection = url.openConnection();
						connection.connect();

						InputStream input = new BufferedInputStream(
								url.openStream());

						OutputStream output = new FileOutputStream(myFile);

						byte data[] = new byte[1024];
						int count;
						while ((count = input.read(data)) != -1) {
							output.write(data, 0, count);
						}

						output.flush();
						output.close();
						input.close();
					}
					publishProgress(i + 1);
				} catch (Exception e) {
					Log.e(DownloadDoujin.class.toString(), e.getMessage(), e);
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			dialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String bytes) {
			dialog.hide();
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
