package com.fakkudroid;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;


import com.fakkudroid.adapter.DownloadListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuConnection;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class DownloadListActivity extends ListActivity{

	private FakkuDroidApplication app;
	DownloadListAdapter da;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_list);

		app = (FakkuDroidApplication) getApplication();
		
		setData();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_download_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			new ScanFolder().execute(true);
			break;
		}
		return true;
	}
	
	@Override
	public File getDir(String dir, int mode){
		File file = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String settingDir = prefs.getString("dir_download", "0");
		if(settingDir.equals(Constants.EXTERNAL_STORAGE + "")){
			String state = Environment.getExternalStorageState();
			if(Environment.MEDIA_MOUNTED.equals(state)){
				file = new File(Environment.getExternalStorageDirectory() + Constants.LOCAL_DIRECTORY + "/" + dir);
				boolean success = true;
				if(!file.exists()){
					success = file.mkdirs();
				}
				
				if(!success)
					file = null;
			}
		}
		if(file == null)
			file = new File(Environment.getRootDirectory() + Constants.LOCAL_DIRECTORY + "/" + dir);
		
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}
	
	private void setData(){
		DataBaseHandler db = new DataBaseHandler(this);
		LinkedList<DoujinBean> llDoujin = db.getDoujinList();
		
		da = new DownloadListAdapter(this, R.layout.row_download, 0, llDoujin, this);
		this.setListAdapter(da);
	}
	
	public void delete(final DoujinBean bean){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ask_delete)
               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
           			String folder = bean.getId();
       				File dir = getDir(folder, Context.MODE_PRIVATE);
       				try {
						FileUtils.deleteDirectory(dir);
					} catch (IOException e) {
						Log.e(ScanFolder.class.toString(),
								"Exception", e);
					}
           			DataBaseHandler db = new DataBaseHandler(DownloadListActivity.this);
           			db.deleteDoujin(bean.getId());
           			
           			Toast.makeText(DownloadListActivity.this, getResources().getString(R.string.deleted),
           					Toast.LENGTH_SHORT).show();
           			setData();
                   }
               })
               .setNegativeButton(android.R.string.no, null).create().show();
	}
	public void showDetails(DoujinBean bean){
		app.setCurrent(bean);
		Intent it = new Intent(this, DoujinActivity.class);
		this.startActivity(it);
	}
	public void read(DoujinBean bean){
		app.setCurrent(bean);
		Intent it = new Intent(this, GallerySwipeActivity.class);
		this.startActivity(it);
	}

	class ScanFolder extends AsyncTask<Boolean, Integer, Boolean> {
		
		ProgressDialog dialog;
		boolean cancel;
		File folder;
		
		ScanFolder(){
			folder = getDir("", Context.MODE_PRIVATE);
		}
		
		protected void onPreExecute() {
			dialog = new ProgressDialog(DownloadListActivity.this);
			dialog.setMax(folder.listFiles().length);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.setTitle(R.string.scan);
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources()
					.getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel = true;
							dialog.dismiss();
						}
					});
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Boolean... arg0) {
			DataBaseHandler db = new DataBaseHandler(DownloadListActivity.this);
			int i = 0;
			for (File doujinFolder : folder.listFiles()) {
				String id = doujinFolder.getName();
				if (!cancel) {
					if(doujinFolder.listFiles().length==0){
						doujinFolder.delete();
					}else{
						DoujinBean bean = db.getDoujinBean(id);
						if(bean==null){
							String url = Constants.SITEDOUJINSHI+"/"+id;
							bean = new DoujinBean();
							bean.setUrl(url);
							boolean error = false;
							try {
								FakkuConnection.parseHTMLDoujin(bean);
							} catch (Exception e) {
								error = true;
								Log.e(ScanFolder.class.toString(), "Exception", e);
							} 
							if(error){								
								url = Constants.SITEMANGA+"/"+id;
								try {
									FakkuConnection.parseHTMLDoujin(bean);
									error = false;
								} catch (Exception e) {
									Log.e(ScanFolder.class.toString(), "Exception", e);
								}
							}
							if(!error){
								db.deleteDoujin(bean.getId());
								db.addDoujin(bean);
							}else{
								try {
									FileUtils.deleteDirectory(doujinFolder);
								} catch (IOException e) {
									Log.e(ScanFolder.class.toString(), "Exception", e);
								}
							}
						}
					}
				}else
					return true;
				publishProgress(++i);
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			dialog.setProgress(progress[0]);
		}
		
		protected void onPostExecute(Boolean bytes) {
			if(!cancel)
				dialog.dismiss();
			setData();
		}
	}
}
