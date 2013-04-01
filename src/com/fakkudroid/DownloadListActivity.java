package com.fakkudroid;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.fakkudroid.adapter.DownloadListAdapter;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

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
