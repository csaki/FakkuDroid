package com.fakkudroid.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fakkudroid.MainActivity;
import com.fakkudroid.R;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Helper;

public class DownloadManagerService extends Service {

	private FakkuDroidApplication app;
	public static boolean started;
	public static DoujinBean currentBean;
	public static int percent;
	public static boolean cancel;
	SharedPreferences preferenceManager;

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		started = true;
		app = (FakkuDroidApplication) getApplication();
		DoujinMap.add(app.getCurrent());
		Helper.executeAsyncTask(new DownloadDoujin());
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

	class DownloadDoujin extends AsyncTask<String, Integer, Integer> {

		NotificationManager mNotificationManager;

		DoujinBean bean;

		public DownloadDoujin() {
			cancel = false;
			this.bean = DoujinMap.next();
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}

		protected void onPreExecute() {
			percent = 0;
			showNotification(0, R.string.downloading);
		}

		private void showNotification(int percent, int resource) {
			Intent resultIntent = null;
			if(resource==R.string.downloading){
				resultIntent= new Intent(DownloadManagerService.this,
						MainActivity.class);				
				resultIntent.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOWNLOADS_QUEUE);
			}else if(resource==R.string.download_completed){
				resultIntent= new Intent(DownloadManagerService.this,
						MainActivity.class);				
				resultIntent.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOWNLOADS);
			}else if(resource==R.string.download_cancelled||resource==R.string.download_error){
				resultIntent= new Intent(DownloadManagerService.this,
						MainActivity.class);
                resultIntent.putExtra(MainActivity.INTENT_VAR_CURRENT_CONTENT, MainActivity.DOUJIN);
				resultIntent.putExtra(MainActivity.INTENT_VAR_URL, bean.getUrl());
			}
			
			// Adds the Intent to the top of the stack
			// Gets a PendingIntent containing the entire back stack
			PendingIntent resultPendingIntent = PendingIntent.getActivity(DownloadManagerService.this,
					0, resultIntent,PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					DownloadManagerService.this).setSmallIcon(
					R.drawable.ic_launcher).setContentTitle(bean.getTitle());

			if (percent >= 0) {
				mBuilder.setContentText(getResources().getString(resource)
						+ percent + "%");
				mBuilder.setProgress(100, percent, false);
			} else {
				mBuilder.setContentText(getResources().getString(resource));
			}
			Notification notif = mBuilder.build();
			notif.contentIntent = resultPendingIntent;
			if (percent >= 0)
				notif.flags = Notification.FLAG_ONGOING_EVENT;
			else
				notif.flags = notif.flags | Notification.DEFAULT_LIGHTS
					| Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(bean.getId().hashCode(), notif);
		}

		@Override
		protected Integer doInBackground(String... args) {
			List<String> lstUrls = bean.getImages();
			List<String> lstFiles = bean.getImagesFiles();
			String folder = bean.getId();
			File dir = Helper.getDir(folder, Context.MODE_PRIVATE,
					getApplicationContext());
//			boolean doujinAlreadyExists = dir.listFiles().length>3;
			File cacheDir = Helper.getCacheDir(getApplicationContext());
			
			OutputStream output = null;
			InputStream input = null;
			File tempZip = new File(dir, bean.getId() + ".zip");
			
			// Copy thumb file to folder
			File titleBitmap = new File(cacheDir, bean.getFileImageTitle());
			File titleBitmapCP = new File(dir, "thumb.jpg");
			try {
				if (!titleBitmapCP.exists())
					FileUtils.copyFile(titleBitmap, titleBitmapCP);
			} catch (Exception e) {
				Helper.logError(this, e.getMessage(), e);
			}

			// Save data.json
			try {
				Helper.saveJsonDoujin(bean, dir);
			} catch (Exception e) {
				Helper.logError(this, e.getMessage(), e);
			}

			try {
//				try{
//					if(bean.getUrlDownload()==null){
//						throw new RuntimeException("Not exists download link.");
//					}else{
//						if(!doujinAlreadyExists){
//							if(tempZip.exists())
//								tempZip.delete();
//							URL url = new URL(bean.getUrlDownload());
//							URLConnection connection = url.openConnection();
//							connection.connect();
//
//							input = new BufferedInputStream(url.openStream());
//
//							output = new FileOutputStream(tempZip);
//
//							byte data[] = new byte[1024];
//							int count;
//							int downloaded = 0;
//							int total = connection.getContentLength();
//							while ((count = input.read(data)) != -1) {
//								if (!cancel) {
//									output.write(data, 0, count);
//									downloaded+=count;
//									publishProgress(downloaded, total);
//								}else{
//									return R.string.download_cancelled;
//								}
//							}
//							output.flush();
//							Helper.zipDecompress(tempZip.getAbsolutePath(), dir.getAbsolutePath());
//						}
//					}
//				}catch(Exception e){
					for (int i = 0; i < lstUrls.size(); i++) {
						File myFile = new File(dir, lstFiles.get(i));
						if (!cancel) {
							if (!myFile.exists()) {
								Helper.saveInStorage(myFile, lstUrls.get(i));
								publishProgress(i + 1, bean.getQtyPages());
							}
						} else
							return R.string.download_cancelled;
					}
//				}

				DataBaseHandler db = new DataBaseHandler(
						DownloadManagerService.this);
				db.deleteDoujin(bean.getId());
				db.addDoujin(bean);
				return R.string.download_completed;
			} catch (Exception e) {
				Helper.logError(this, e.getMessage(), e);
				return R.string.download_error;
			} finally{
				if (output != null) {
					try {
						output.close();
					} catch (IOException e1) {
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (IOException e1) {
					}
				}
				if(tempZip.exists())
					tempZip.delete();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			if (DoujinMap.exists(bean)) {
				int downloaded = progress[0];
				int total = progress[1];
				int percent = downloaded * 100 / total;
				if(percent>DownloadManagerService.percent){
					showNotification(percent, R.string.downloading);
				}
				DownloadManagerService.percent = percent;
			}
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Integer result) {
			if (DoujinMap.exists(bean)) {
				showNotification(-1, result);
				DoujinMap.remove(bean);

				if (result == R.string.download_completed)
					Toast.makeText(
							DownloadManagerService.this,
							getResources().getString(
									R.string.completed_download).replace(
									"@doujin", bean.getTitle()),
							Toast.LENGTH_SHORT).show();
				else if (result == R.string.download_error)
					Toast.makeText(
							DownloadManagerService.this,
							getResources()
									.getString(R.string.error_downloading)
									.replace("@doujin", bean.getTitle()),
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(
							DownloadManagerService.this,
							getResources().getString(
									R.string.download_cancelled).replace(
									"@doujin", bean.getTitle()),
							Toast.LENGTH_SHORT).show();
			}
			if (DoujinMap.next() != null) {
				Helper.executeAsyncTask(new DownloadDoujin());
			} else {
				stopSelf();
			}
		}
	}

	public static class DoujinMap {

		static LinkedList<DoujinBean> list = new LinkedList<DoujinBean>();

		public static void add(DoujinBean bean) {
			list.add(bean);
		}

		public static void remove(DoujinBean bean) {
			list.remove(bean);
		}

		public static boolean exists(DoujinBean bean) {
			if (list.indexOf(bean) != -1)
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

		public static LinkedList<DoujinBean> getList() {
			return list;
		}
	}

}
