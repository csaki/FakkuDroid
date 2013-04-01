package com.fakkudroid.adapter;

import java.io.File;
import java.util.LinkedList;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.util.Util;
import com.fakkudroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FavoriteListAdapter extends ArrayAdapter<DoujinBean> {
	
	LayoutInflater inf;
	LinkedList<DoujinBean> objects;
	boolean cacheMode;
	File cacheDir;

	public FavoriteListAdapter(Context context, int resource, int textViewResourceId,
			LinkedList<DoujinBean> objects, boolean cacheMode,File cacheDir) {
		super(context, resource, textViewResourceId, objects);
		this.inf = LayoutInflater.from(context);
		this.objects = objects;
		this.cacheMode = cacheMode;
		this.cacheDir = cacheDir;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		DoujinBean s = objects.get(position);
		if (convertView == null) {
			convertView = inf.inflate(R.layout.cell_favorite, null);
			holder = new ViewHolder();
			holder.tvDoujin = (TextView) convertView.findViewById(R.id.tvDoujin);
			holder.wvTitle = (WebView) convertView.findViewById(R.id.wvTitle);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDoujin.setText(Util.limitString(s.getTitle(), 36, "..."));
		
		holder.wvTitle.setFocusable(false);
		holder.wvTitle.setLongClickable(false);
		holder.wvTitle.setClickable(false);
		holder.wvTitle.setFocusableInTouchMode(false);
		if(cacheMode){
			File myFile = new File(cacheDir, s.getFileImageTitle());
			
			holder.wvTitle.loadDataWithBaseURL(null,Util.createHTMLImagePercentage("file://" + myFile.getAbsolutePath(),100,parent.getResources()),"text/html", "utf-8",null);
		}else
			holder.wvTitle.loadDataWithBaseURL(null,Util.createHTMLImagePercentage(s.getUrlImageTitle(),100,parent.getResources()),"text/html", "utf-8",null);		
		return convertView;
	}
	
	static class ViewHolder {
		TextView tvDoujin;
		
		WebView wvTitle;
	}
}
