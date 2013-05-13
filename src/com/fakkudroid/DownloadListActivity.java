package com.fakkudroid;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import com.fakkudroid.adapter.DownloadListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
	int numPage = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_list);

		app = (FakkuDroidApplication) getApplication();
		
		setData();
	}
	
	public void nextPage(View view) {
		numPage++;
		TextView tvPage = (TextView) findViewById(R.id.tvPage);
		tvPage.setText("Page " + numPage);
		setData();
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
			TextView tvPage = (TextView) findViewById(R.id.tvPage);
			tvPage.setText("Page " + numPage);
			setData();
			Context context = getApplicationContext();
			CharSequence text = "Page " + numPage;
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
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
		LinkedList<DoujinBean> llDoujin = db.getDoujinList(numPage);
		
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
						Log.e(DownloadListActivity.class.toString(),
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
}
