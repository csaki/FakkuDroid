package com.fakkudroid.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.fakkudroid.DoujinActivity;
import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;

public class DownloadManagerService extends Service {

	private final String TAG = DownloadManagerService.class.toString();
	private FakkuDroidApplication app;
	public static boolean started;
	public static DoujinBean currentBean;
	public static int percent;
	SharedPreferences preferenceManager;

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		started = true;
		app = (FakkuDroidApplication) getApplication();
		DoujinMap.add(app.getCurrent());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new DownloadDoujin()
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else
			new DownloadDoujin().execute("");
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		started = false;
	}

	@Override
	public File getCacheDir() {
		File file = null;
		;
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

	class DownloadDoujin extends AsyncTask<String, Integer, String> {

		NotificationCompat.Builder mBuilder;
		NotificationManager mNotificationManager;

		boolean cancel;
		DoujinBean bean;

		public DownloadDoujin() {
			this.bean = DoujinMap.next();
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}

		protected void onPreExecute() {
			percent = 0;
			showNotification(0, R.string.downloading);
		}

		private void showNotification(int percent, int resource) {
			Intent resultIntent = new Intent(DownloadManagerService.this,
					DoujinActivity.class);
			resultIntent.putExtra(DoujinActivity.INTENT_VAR_URL, bean.getUrl());
			
			TaskStackBuilder stackBuilder = TaskStackBuilder
					.create(DownloadManagerService.this);
			// Adds the back stack
			stackBuilder.addParentStack(DoujinActivity.class);
			// Adds the Intent to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			// Gets a PendingIntent containing the entire back stack
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder = new NotificationCompat.Builder(
					DownloadManagerService.this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(bean.getTitle());
					
			if (percent >= 0){
				mBuilder.setContentText(
						getResources().getString(resource) + percent + "%");
				mBuilder.setProgress(100, 0, false);
			}else{
				mBuilder.setContentText(
						getResources().getString(resource));
			}
			Notification notif = mBuilder.build();
			notif.contentIntent = resultPendingIntent;
			if (percent >= 0)
				notif.flags = Notification.FLAG_ONGOING_EVENT;

			mNotificationManager.notify(bean.getId().hashCode(), notif);
		}

		@Override
		protected String doInBackground(String... args) {
			List<String> lstUrls = bean.getImages();
			List<String> lstFiles = bean.getImagesFiles();
			String folder = bean.getId();
			File dir = getDir(folder, Context.MODE_PRIVATE);
			for (int i = 0; i < lstUrls.size(); i++) {
				File myFile = new File(dir, lstFiles.get(i));
				if (!cancel) {
					if (!myFile.exists()) {
						try {
							Util.saveInStorage(myFile, lstUrls.get(i));
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
						}
						publishProgress(i + 1);
					}
				} else
					return null;
			}
			DataBaseHandler db = new DataBaseHandler(
					DownloadManagerService.this);
			db.deleteDoujin(bean.getId());
			db.addDoujin(bean);
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			percent = progress[0] * 100 / bean.getQtyPages();
			showNotification(percent, R.string.downloading);
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(String bytes) {
			DoujinMap.remove(bean);
			if (!cancel) {
				showNotification(-1, R.string.download_complete);
			} else {
				showNotification(-1, R.string.download_cancelled);
			}
			Toast.makeText(DownloadManagerService.this,
					bean.getTitle() + " is finished.", Toast.LENGTH_SHORT)
					.show();
			if (DoujinMap.next() != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					new DownloadDoujin()
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new DownloadDoujin().execute("");
				}
				new DownloadDoujin().execute("");
			} else {
				stopSelf();
			}
		}
	}

	public static class DoujinMap {

		static List<DoujinBean> list = new ArrayList<DoujinBean>();

		public static void add(DoujinBean bean) {
			list.add(bean);
		}

		public static void remove(DoujinBean bean) {
			list.remove(bean);
		}

		public static boolean exists(DoujinBean bean) {

			for (DoujinBean b : list) {
				if (b.getId().hashCode() == bean.getId().hashCode()) {
					return true;
				}
			}
			return false;
		}

		public static DoujinBean next() {
			if (list.size() > 0)
				return (currentBean = list.get(0));
			else
				return (currentBean = null);
		}
	}

}
