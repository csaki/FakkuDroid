package com.fakkudroid;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.fakkudroid.adapter.DownloadListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ListActivity;
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
                	   List<String> lstFiles = bean.getImagesFiles();
           			String folder = bean.getId();
           			for (int i = 0; i < lstFiles.size(); i++) {
           				File dir = getDir(folder, Context.MODE_PRIVATE);
           				File myFile = new File(dir, lstFiles.get(i));
           				if (myFile.exists()) {
           					myFile.delete();
           				}
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
}
